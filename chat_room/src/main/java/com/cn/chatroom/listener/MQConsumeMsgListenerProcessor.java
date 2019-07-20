package com.cn.chatroom.listener;

import java.util.List;

import com.cn.chatroom.config.RocketmqConfig;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.ImStatus;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.ChatType;
import org.jim.common.packets.Command;
import org.jim.common.packets.Group;
import org.jim.common.packets.LoginRespBody;
import org.jim.common.packets.MsgType;
import org.jim.common.packets.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cn.beans.ChatMQ;
import com.cn.beans.RedPacketMQ;
import com.cn.chatroom.bean.TempUserMQ;
import com.cn.chatroom.config.GroupConfig;
import com.cn.chatroom.constant.Constant;
import com.cn.chatroom.service.UserInfoService;
import com.cn.constant.RedisKeyConstant;
import com.cn.constant.SysConstant;
import com.cn.constant.UserConstant;
import com.cn.enums.MQTagsEnum;
import com.cn.enums.UserLevelEnum;
import com.cn.interceptor.UserToken;
import com.cn.kit.UUIDKit;
import com.cn.model.UserInfo;
import com.jfinal.kit.StrKit;
/**
 * 
* @ClassName: MQConsumeMsgListenerProcessor
* @Description: (MQ消費者)
* @author BING
* @date 2018年11月23日
*
 */
public class MQConsumeMsgListenerProcessor implements MessageListenerConcurrently{
	
	private static final Logger logger = LoggerFactory.getLogger(MQConsumeMsgListenerProcessor.class);
	
	private static JedisTemplate jedisTemplate = null;
	
	static{
		try {
			jedisTemplate = JedisTemplate.me();
		} catch (Exception e) {
			logger.error("init redis is error ",e);
		}
	}
	
	/**
	 *  默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息<br/>
	 *  不要抛异常，如果没有return CONSUME_SUCCESS ，consumer会重新消费该消息，直到return CONSUME_SUCCESS
	 */
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
		if(msgs.isEmpty()){
			logger.info("接受到的消息为空，不处理，直接返回成功");
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
		//是否是重复消费
		boolean isNotRepeat = true;
		MessageExt messageExt = msgs.get(0);
		int reconsume = messageExt.getReconsumeTimes();
		String msg = new String(messageExt.getBody());
		logger.info("接受到的消息內容：消息msgId:{}，主题topic:{}，标识tags:{}，重试次数reconsumeTimes:{}，消息体msg:{}",
				messageExt.getMsgId(),messageExt.getTopic(),messageExt.getTags(),messageExt.getReconsumeTimes(),msg);
		if(reconsume ==SysConstant.MQ_RETRY_SEND_FAILED_COUNT){//消息已经重试了5次，如果不需要再次消费，则返回成功
			logger.info("多次重复的记录，内容 : "+msg);
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
		//红包信息
		if(messageExt.getTopic().equals(RocketmqConfig.getTopics())){
			if(messageExt.getTags().equals(MQTagsEnum.SEND_RED_PACKET.getType())){
				RedPacketMQ  redPacketMQ = JSONObject.parseObject(msg, RedPacketMQ.class);
				String key = "";
				if(null != jedisTemplate){
					key = Constant.REDIS_REDPACKET_MQ+Constant.SPLIT_KEY_TAG+redPacketMQ.getRedPacketId();
					String value = jedisTemplate.getString(key);
					isNotRepeat = StrKit.isBlank(value);
				}
				if(null != jedisTemplate&&isNotRepeat){
					boolean flag = sendRedPacket(redPacketMQ);
					String redPacketKey;
					if(flag){
						redPacketKey = String.format(SysConstant.MQ_CONSUME_REDPACKET,redPacketMQ.getRedPacketId());
						jedisTemplate.setString(redPacketKey, System.currentTimeMillis()+"", Constant.MQ_REDPACKET_EXPIRE_TIME);
					}
					redPacketKey = String.format(RedisKeyConstant.CREATE_REDPACKET_LIST,redPacketMQ.getRedPacketId());
					if(jedisTemplate.listGetAll(redPacketKey)==null){
						logger.info("該紅包在MQ裡面已經丟失.......");
				        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
					}
					if(flag&&StrKit.notBlank(key)){
						try {
							jedisTemplate.setString(key, msg, Constant.REDIS_REDPACKET_VALIDITY);

						} catch (Exception e) {
							logger.error("save redPacket MQ message error , ",e);
						}
						
					}else{
						logger.info("发送红包失败，消息为  ："+msg);
					}
				}
				// 如果没有return success ，consumer会重新消费该消息，直到return success
				//return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			//文本内容
			}else if(messageExt.getTags().equals(MQTagsEnum.SEND_CHAT.getType())){
				ChatMQ  chatMQ = JSONObject.parseObject(msg, ChatMQ.class);
				boolean flag = sendChat(chatMQ);
				if(!flag){
					logger.info("发送文本信息失败，消息为 : "+msg);
				}
			//匿名用戶T除
			}else if(messageExt.getTags().equals(MQTagsEnum.SEND_REMOVE_CHAT.getType())){
				TempUserMQ  tempUserMQ = JSONObject.parseObject(msg, TempUserMQ.class);
				if(null != tempUserMQ){
					try {
						LoginRespBody loginRespBody = new LoginRespBody(Command.COMMAND_LOGIN_RESP,ImStatus.C10022);
						ImPacket loginRespPacket = new ImPacket(Command.COMMAND_LOGIN_RESP, loginRespBody.toByte());
						ImAio.sendToUser(tempUserMQ.getToken(), loginRespPacket);
						logger.info("匿名用戶T除token : {}",tempUserMQ.getToken());
						Thread.sleep(1000);
						ImAio.remove(tempUserMQ.getToken(), "匿名受限，請登錄");
					} catch (Exception e) {
						logger.error("匿名用戶T除失敗token : {}",tempUserMQ.getToken(),e);
					}
				}
			}
		}
		// 如果没有return success ，consumer会重新消费该消息，直到return success
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
	
	private boolean sendRedPacket(RedPacketMQ  redPacketMQ){
		String token;
		UserInfo userInfo;
		if(UserLevelEnum.SYS.getKey()==redPacketMQ.getLevel()){
			token = UUIDKit.getUUID();
			userInfo = new UserInfo();
			userInfo.setUserId(redPacketMQ.getUserId());
			userInfo.setLevel(UserLevelEnum.SYS.getKey());
			userInfo.setHeadImg(UserConstant.DEFAULT_SYS_IMG);
			userInfo.setNickname(Constant.SYS_NICKNAME);
		}else{
			UserToken userToken = UserInfoService.reGenToken(redPacketMQ.getUserId());
			if(null==userToken)return false;
			token = userToken.getToken();
			userInfo =  UserInfoService.getUserInfo(redPacketMQ.getUserId());
		}
		JSONObject extras = new JSONObject();
		extras.put(Constant.USER_FIELD_USERID, userInfo.getUserId());
		extras.put(Constant.USER_FIELD_LEVEL, userInfo.getLevel());
		extras.put(Constant.USER_FIELD_HEADIMG, userInfo.getHeadImg());
		extras.put(Constant.USER_FIELD_NICKNAME, userInfo.getNickname());
		extras.put(Constant.RED_PACKET_FIELD_AMOUNT, redPacketMQ.getAmount());
		extras.put(Constant.RED_PACKET_FIELD_COUNT, redPacketMQ.getCount());
		extras.put(Constant.RED_PACKET_FIELD_ID, redPacketMQ.getRedPacketId());
		extras.put(Constant.RED_PACKET_FIELD_STATUS,redPacketMQ.getStatus());
		extras.put(Constant.RED_PACKET_FIELD_CLICKED,redPacketMQ.getClicked());

		ChatBody chatBody = ChatBody.newBuilder().build();
		chatBody.setCreateTime(redPacketMQ.getCreateTime().getTime());
		chatBody.setChatType(ChatType.CHAT_TYPE_PUBLIC.getNumber());
		chatBody.setCmd(null);
		chatBody.setContent(redPacketMQ.getDesc());
		chatBody.setExtras(extras);
		chatBody.setFrom(token);
		chatBody.setGroup_id(redPacketMQ.getGroupId());
		chatBody.setId(UUIDKit.getUUID());
		chatBody.setMsgType(MsgType.MSG_TYPE_REDPACKET.getNumber());
		sendToGroup(redPacketMQ.getGroupId(), chatBody);
		logger.info("MQ聊天室紅包發送成功...");
		return true;
	}
	
	private boolean sendChat(ChatMQ  chatMQ){
		String token;
		UserInfo userInfo;
		if(UserLevelEnum.SYS.getKey()==chatMQ.getLevel()){
			token = UUIDKit.getUUID();
			userInfo = new UserInfo();
			userInfo.setUserId(chatMQ.getUserId());
			userInfo.setLevel(UserLevelEnum.SYS.getKey());
			userInfo.setHeadImg(UserConstant.DEFAULT_SYS_IMG);
			userInfo.setNickname(Constant.SYS_NICKNAME);
		}else{
			UserToken userToken = UserInfoService.reGenToken(chatMQ.getUserId());
			if(null==userToken)return false;
			token = userToken.getToken();
			userInfo =  UserInfoService.getUserInfo(chatMQ.getUserId());
		}
		JSONObject extras = new JSONObject();
		extras.put(Constant.USER_FIELD_USERID, userInfo.getUserId());
		extras.put(Constant.USER_FIELD_LEVEL, userInfo.getLevel());
		extras.put(Constant.USER_FIELD_HEADIMG, userInfo.getHeadImg());
		extras.put(Constant.USER_FIELD_NICKNAME, userInfo.getNickname());
		
		ChatBody chatBody = ChatBody.newBuilder().build();
		chatBody.setCreateTime(chatMQ.getCreateTime().getTime());
		chatBody.setChatType(ChatType.CHAT_TYPE_PUBLIC.getNumber());
		chatBody.setCmd(null);
		chatBody.setContent(chatMQ.getContent());
		chatBody.setExtras(extras);
		chatBody.setFrom(token);
		chatBody.setGroup_id(chatMQ.getGroupId());
		chatBody.setId(UUIDKit.getUUID());
		chatBody.setMsgType(MsgType.MSG_TYPE_TEXT.getNumber());
		sendToGroup(chatMQ.getGroupId(), chatBody);
		logger.info("MQ聊天室文本信息發送成功...");
		return true;
	}
	
	private void sendToGroup(String groupId,ChatBody chatBody){
		if(StrKit.isBlank(groupId)){
			List<Group> groups = GroupConfig.getGroups();
			RespBody respBody = null;
			ImPacket imPacket = null;
			for(Group group : groups){
				chatBody.setGroup_id(group.getGroup_id());
				respBody = new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
				imPacket = new ImPacket(respBody.toByte());
				ImAio.sendToGroup(group.getGroup_id(), imPacket);
				if(jedisTemplate!=null){
					String redisChatkey = Constant.REDIS_CHAT_BODY_LIST+Constant.SPLIT_KEY_TAG+chatBody.getGroup_id();
					jedisTemplate.listPushHeadAndTrim(redisChatkey, JSONObject.toJSONString(chatBody), Constant.REDIS_SAVE_CHAT_SIZE);
				}
			}
		}else{
			RespBody respBody = new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
			ImPacket imPacket = new ImPacket(respBody.toByte());
			ImAio.sendToGroup(groupId, imPacket);
			if(jedisTemplate!=null){
				String redisChatkey = Constant.REDIS_CHAT_BODY_LIST+Constant.SPLIT_KEY_TAG+chatBody.getGroup_id();
				jedisTemplate.listPushHeadAndTrim(redisChatkey, JSONObject.toJSONString(chatBody), Constant.REDIS_SAVE_CHAT_SIZE);
			}
		}
	}
}
