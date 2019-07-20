package com.cn.chatroom.command;

import com.alibaba.fastjson.JSONObject;
import com.cn.chatroom.constant.ChatStatus;
import com.cn.chatroom.constant.Constant;
import com.cn.chatroom.service.UserInfoService;
import com.cn.chatroom.task.SaveChatTask;
import com.cn.chatroom.utils.SensitiveWordUtil;
import com.cn.chatroom.utils.TaskUtil;
import com.cn.enums.UserStatusEnum;
import com.cn.interceptor.UserToken;
import com.cn.kit.StrKit;
import com.cn.model.UserInfo;
import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.ImStatus;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Command;
import org.jim.common.packets.MsgType;
import org.jim.common.packets.RespBody;
import org.jim.server.command.handler.processor.chat.DefaultChatProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
/**
 * 
    * @ClassName: FBImWsChatProcessor
    * @Description: TODO(聊天业务处理类)
    * @author BING
    * @date 2018年11月5日
    *
 */
public class FBImWsChatProcessor extends DefaultChatProcessor{
	
	private final static Logger logger = LoggerFactory.getLogger(FBImWsChatProcessor.class);

	@Override
	public void doHandler(ChatBody chatBody, ChannelContext channelContext) {
		logger.info("=========doHandler===========");
		if(chatBody.getMsgType() == MsgType.MSG_TYPE_REDPACKET.getNumber()){
			logger.info("红包信息chatBody:{}",JSONObject.toJSONString(chatBody));
		}else if(chatBody.getMsgType() == MsgType.MSG_TYPE_TEXT.getNumber()){
			logger.info("文本信息chatBody:{}",JSONObject.toJSONString(chatBody));
		}
	}
	
	@Override
	public RespBody beforeHandler(ChatBody chatBody, ChannelContext channelContext) {
//		if(chatBody.getMsgType() == MsgType.MSG_TYPE_REDPACKET.getNumber())return new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
//		RespBody respBody = null;
//		if(StringUtils.isBlank(chatBody.getContent())){
//			respBody = new RespBody(Command.COMMAND_CHAT_REQ,ChatStatus.MESSAGE_IS_NULL);
//		}else if(chatBody.getContent().length()>Constant.CHAT_CONTENT_LEN){
//			respBody = new RespBody(Command.COMMAND_CHAT_REQ,ChatStatus.MESSAGE_IS_OVERFLOW);
//		}else{
//			respBody = new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
//		}
//		return respBody;
		chatBody.setCreateTime(System.currentTimeMillis());
		JSONObject jsonObject = chatBody.getExtras();
		long userId = Constant.NO_LOGIN_DEFAULT;
		if(null != jsonObject)userId = jsonObject.getLongValue(Constant.USER_FIELD_USERID);
		UserToken userToken = UserInfoService.reGenToken(userId);
		if(null == userToken){
			return new RespBody(Command.COMMAND_CHAT_REQ,ImStatus.C10010);
		}else{
			if(StrKit.isBlank(chatBody.getContent())){
				logger.info("不許發送空的內容，用戶{}",userToken.getToken());
				return new RespBody(Command.COMMAND_CHAT_REQ,ChatStatus.MESSAGE_IS_NULL);
			}
			if(chatBody.getContent().length()>Constant.CHAT_CONTENT_LEN){
				logger.info("不許發送超出長度的內容，用戶{}",userToken.getToken());
				return new RespBody(Command.COMMAND_CHAT_REQ,ChatStatus.MESSAGE_IS_OVERFLOW);
			}
			UserInfo userInfo = UserInfoService.getUserInfo(userId);
			if(null != userInfo&&userInfo.getStatus()==UserStatusEnum.STOPTALK.getKey()){
				return new RespBody(Command.COMMAND_CHAT_REQ,ImStatus.C10003);
			}else if(null != userInfo){
				jsonObject.put(Constant.USER_FIELD_LEVEL, userInfo.getLevel());
				jsonObject.put(Constant.USER_FIELD_HEADIMG, userInfo.getHeadImg());
				jsonObject.put(Constant.USER_FIELD_NICKNAME, userInfo.getNickname());
				chatBody.setExtras(jsonObject);
				//敏感词过滤
				chatBody.setContent(SensitiveWordUtil.replaceSensitiveWord(chatBody.getContent()));
				Object targetUserId = jsonObject.get(Constant.USER_FIELD_TARGETUSERID);
				if(targetUserId!=null){
					String[] ids = targetUserId.toString().split(",");
					for (String id : ids) {
						UserInfo targetUserInfo = UserInfoService.getUserInfo(Long.valueOf(id));
						if (null == targetUserInfo) {
							logger.info("@用户targetUserId:{} 不存在", id);
						} else {
							RespBody respBody =
									new RespBody(Command.COMMAND_CALL_REQ, userInfo.getNickname()+"："+chatBody.getContent());
							ImPacket imPacket = new ImPacket(Command.COMMAND_CALL_REQ, respBody.toByte());
							logger.info("发送@通知给targetUserId:{}", id);
							ImAio.sendToUser(UserInfoService.reGenToken(targetUserInfo.getUserId()).getToken(), imPacket);
						}
					}
				}
				try {
					saveChatBody(chatBody);
				} catch (Exception e) {
					logger.error("缓存聊天记录失败!",e);
				}
			}
			return new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
		}
	}
	
	/**
	 *
	    * @Title: saveChatBody
	    * @Description: TODO(保存聊天信息)
	    * @param @param chatBody
	    * @param @throws Exception    参数
	    * @return void    返回类型
	    * @throws
	 */
	private void saveChatBody(ChatBody chatBody) throws Exception{
		TaskUtil.execute(new SaveChatTask(chatBody));
	}

	/**
	 * @消息
	 * @param userId
	 * @param imPacket
	 * @throws Exception
	 */
	private void notifyToUser(String userId,ImPacket imPacket){
		TaskUtil.execute(() -> ImAio.sendToUser(userId,imPacket));
	}
}