package com.cn.chatroom.command;

import java.util.ArrayList;
import java.util.List;

import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.ImSessionContext;
import org.jim.common.ImStatus;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.ChatType;
import org.jim.common.packets.Command;
import org.jim.common.packets.Group;
import org.jim.common.packets.LoginReqBody;
import org.jim.common.packets.LoginRespBody;
import org.jim.common.packets.MsgType;
import org.jim.common.packets.RespBody;
import org.jim.common.packets.User;
import org.jim.common.utils.JsonKit;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.JoinGroupReqHandler;
import org.jim.server.command.handler.processor.login.LoginProcessorIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

import com.alibaba.fastjson.JSONObject;
import com.cn.chatroom.bean.TempUserMQ;
import com.cn.chatroom.component.MQProducer;
import com.cn.chatroom.config.GroupConfig;
import com.cn.chatroom.constant.Constant;
import com.cn.chatroom.service.UserInfoService;
import com.cn.constant.RedisKeyConstant;
import com.cn.constant.UserConstant;
import com.cn.enums.MQTagsEnum;
import com.cn.enums.MQTopicTypeEnum;
import com.cn.enums.UserLevelEnum;
import com.cn.interceptor.UserToken;
import com.cn.kit.StrKit;
import com.cn.kit.UUIDKit;
import com.cn.model.UserInfo;
/**
 * 
* @ClassName: FBImWsLoginServiceProcessor
* @Description: TODO(登录业务处理类)
* @author BING
* @date 2018年11月5日
*
 */
public class FBImWsLoginServiceProcessor implements LoginProcessorIntf{

	private static Logger logger = LoggerFactory.getLogger(FBImWsLoginServiceProcessor.class);
	
	private static int count = 0;
	
	@Override
	public boolean isProtocol(ChannelContext channelContext) {
		return true;
	}

	@Override
	public String name() {
		return "default";
	}

	@Override
	public LoginRespBody doLogin(LoginReqBody loginReqBody, ChannelContext channelContext) {
//		String loginname = loginReqBody.getLoginname();
//		String password = loginReqBody.getPassword();
		String token = loginReqBody.getToken();
		String groupId = loginReqBody.getExtras().getString(Constant.LOGIN_PRO_GROUPID);
		String userId = loginReqBody.getExtras().getString(Constant.USER_FIELD_USERID);
//		ImSessionContext imSessionContext = (ImSessionContext)channelContext.getAttribute();
		User user = getUser(userId,token);
//		else{
//			user = getUser(loginname,password);
//		}
		if(!GroupConfig.getGroupConfig().containsKey(groupId)){
			logger.info("join group is fail , because groupId is not exist ! ");
			return new LoginRespBody(Command.COMMAND_LOGIN_RESP,ImStatus.C10008);
		}
		LoginRespBody loginRespBody = null;
		if(user == null){
			loginRespBody = new LoginRespBody(Command.COMMAND_LOGIN_RESP,ImStatus.C10008);
		}else{
			channelContext.setAttribute(Constant.LOGIN_PRO_GROUPID,groupId);
			channelContext.setAttribute(Constant.USER_FIELD_USERID,userId);
			//为用户绑到组里面
			List<Group> groups = new ArrayList<Group>(1);
			groups.add(GroupConfig.getGroupConfig().get(groupId));
			user.setGroups(groups);
			loginRespBody = new LoginRespBody(Command.COMMAND_LOGIN_RESP,ImStatus.C10007,user);
		}
		return loginRespBody;
	}

	@Override
	public void onSuccess(ChannelContext channelContext) {
		Object obj = channelContext.getAttribute(Constant.LOGIN_PRO_GROUPID);
		logger.info("登录成功回调方法，登錄次數{}，加入群組{}",(++count),obj);
		if(null != obj){
			//發送歷史記錄
			sendHistory(channelContext.getUserid(),obj.toString(),channelContext.getAttribute(Constant.USER_FIELD_USERID).toString());
		}
		//joinGroupNotify(channelContext);
	}
     
	/**
	 * 
    * @Title: getUser
    * @Description: TODO(验证用户有效性)
    * @param @return    参数
    * @return User    返回类型
    * @throws
	 */
	private User getUser(String userId,String token){
		//判断token有效性
		boolean flag = isValidateToken(userId,token);
		//long id = StrKit.isBlank(userId) ? Constant.NO_LOGIN_DEFAULT : Long.valueOf(userId);

		User user = new User();
		if(flag && null != userId){
            UserInfo userInfo = UserInfoService.getUserInfo(Long.valueOf(userId));
            if(userInfo==null){
                return null;
            }
			user.setId(token);
			user.setNick(userInfo.getNickname());
			user.setAvatar(StrKit.isBlank(userInfo.getHeadImg())?UserConstant.DEFAULT_USER_IMG:userInfo.getHeadImg());
			JSONObject extras = new JSONObject();
			extras.put(Constant.USER_FIELD_USERID, userInfo.getUserId());
			extras.put(Constant.USER_FIELD_LEVEL, userInfo.getLevel());
			user.setExtras(extras);
		}else if(!StrKit.isBlank(token)&&hasKey(String.format(RedisKeyConstant.STRING_CHAT_EXPIRE_TOKEN,token))){
			user.setId(token);
			user.setNick(Constant.NO_LOGIN_NAME);
			user.setAvatar(UserConstant.DEFAULT_USER_IMG);
			JSONObject extras = new JSONObject();
			extras.put(Constant.USER_FIELD_USERID, Constant.NO_LOGIN_DEFAULT);
			extras.put(Constant.USER_FIELD_LEVEL, UserLevelEnum.DEFAULT);
			user.setExtras(extras);
			//發送延時MQ，移除長時間匿名用戶佔用連接
			TempUserMQ tempUser = new TempUserMQ();
			tempUser.setToken(token);
			tempUser.setUserId(0l);
			tempUser.setLevel(UserLevelEnum.SYS.getKey());
			boolean isSend = MQProducer.sendDelayedThirtyminute(MQTopicTypeEnum.CHAT_ROOM, MQTagsEnum.SEND_REMOVE_CHAT, tempUser);
			logger.info("sendDelayedThirtyminute token {} is {}",token,isSend);
		}else{
			user = null;
		}
		return user;
	}
	
	/**
	 * 
    * @Title: getUser
    * @Description: TODO(验证用户有效性)
    * @param @return    参数
    * @return User    返回类型
    * @throws
	 */
//	private User getUser(String loginname,String password){
//		String text = loginname+password;
//		String key = Const.authkey;
//		String token = Md5.sign(text, key, HttpConst.CHARSET_NAME);
//		
//		User user = new User();
//		user.setId("abc");
//		user.setNick(NameUtil.getCName());
//		user.setAvatar(Constant.USER_DEFAULT_HEADIMG);
//		return user;
//	}
	
	/**
	 * 
	    * @Title: joinGroupNotify
	    * @Description: TODO(登录成功回调方法)
	    * @param @param channelContext    参数
	    * @return void    返回类型
	    * @throws
	 */
	private void joinGroupNotify(ChannelContext channelContext){
		ImSessionContext imSessionContext = (ImSessionContext)channelContext.getAttribute();
		User user = imSessionContext.getClient().getUser();
		if(user.getGroups() != null){
			for(Group group : user.getGroups()){//发送加入群组通知
				ImPacket groupPacket = new ImPacket(Command.COMMAND_JOIN_GROUP_REQ,JsonKit.toJsonBytes(group));
				try {
					JoinGroupReqHandler joinGroupReqHandler = CommandManager.getCommand(Command.COMMAND_JOIN_GROUP_REQ, JoinGroupReqHandler.class);
					joinGroupReqHandler.joinGroupNotify(groupPacket, channelContext);
				} catch (Exception e) {
					logger.error("登录成功回调方法異常",e);
				}
			}
		}
	}
	
	private void sendHistory(String token,String groupId,String userId){
		logger.info("用户token:{}，userId:{}",token,userId);
		if(StrKit.isBlank(groupId))return;
		JedisTemplate jedisTemplate = null;
		try {
			jedisTemplate = JedisTemplate.me();
		} catch (Exception e) {
			logger.error("get redis error",e);
			return;
		}
		String key = Constant.REDIS_CHAT_BODY_LIST+Constant.SPLIT_KEY_TAG+groupId;
		List<String> list = jedisTemplate.listGetAll(key);
		if(list.isEmpty()){
			logger.info("該聊天室沒有聊天記錄... , groupId , {}",groupId);
			return;
		}
		List<ChatBody> bodys = new ArrayList<>(list.size());
		for(String item : list){
			ChatBody chatBody = JSONObject.parseObject(item, ChatBody.class);
			Object redPacketId = chatBody.getExtras().get(Constant.RED_PACKET_FIELD_ID);
			if(redPacketId != null){
				String gradKey = String.format(RedisKeyConstant.GRAB_REDPACKET_SET,redPacketId);
				String createkey = String.format(RedisKeyConstant.CREATE_REDPACKET_LIST,redPacketId);
				String viewKey = String.format(RedisKeyConstant.SET_VIEW_REDPACKET,redPacketId);
				//红包状态
				if(jedisTemplate.setGetAll(gradKey)==null){
					logger.info("該紅包已过期 gradKey is null");
					chatBody.getExtras().put(Constant.RED_PACKET_FIELD_STATUS,4);
				}else if(jedisTemplate.listGetAll(createkey)==null){
					logger.info("該紅包已过期 createkey is null");
					chatBody.getExtras().put(Constant.RED_PACKET_FIELD_STATUS,4);
				}else if(jedisTemplate.isMember(gradKey,userId)){
					logger.info("您已搶過該紅包");
					chatBody.getExtras().put(Constant.RED_PACKET_FIELD_STATUS,3);
				}else if(jedisTemplate.listGetAll(createkey).size()==0){
					logger.info("您來晚了，紅包已被搶完");
					chatBody.getExtras().put(Constant.RED_PACKET_FIELD_STATUS,1);
				}
				//设置点击状态
				chatBody.getExtras().put(Constant.RED_PACKET_FIELD_CLICKED,jedisTemplate.isMember(viewKey,userId));
			}
			bodys.add(chatBody);
		}
		JSONObject extras = new JSONObject();
		extras.put("list",bodys);
		
		ChatBody chatBody = ChatBody.newBuilder().build();
		chatBody.setChatType(ChatType.CHAT_TYPE_PUBLIC.getNumber());
		chatBody.setCmd(null);
		chatBody.setContent("history");
		chatBody.setExtras(extras);
		chatBody.setFrom("sys");
		chatBody.setGroup_id(groupId);
		chatBody.setId(UUIDKit.getUUID());
		chatBody.setMsgType(MsgType.MSG_TYPE_HISTORY.getNumber());
		RespBody respBody = new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
		ImPacket imPacket = new ImPacket(respBody.toByte());
		ImAio.sendToUser(token, imPacket);
	}
	
	private boolean isValidateToken(String userId,String token){
		boolean flag = false;
		if(StrKit.notBlank(token)&&StrKit.notBlank(userId)){
			UserToken userToken = UserInfoService.reGenToken(Long.valueOf(userId));
			if(null != userToken){
				return token.equals(userToken.getToken());
			}
		}
		return flag;
	}
	
	private boolean hasKey(String key){
		JedisTemplate jedisTemplate = null;
		try {
			jedisTemplate = JedisTemplate.me();
			return jedisTemplate.getString(key)!=null;
		} catch (Exception e) {
			logger.error("get redis error",e);
			return false;
		}
	}
}
