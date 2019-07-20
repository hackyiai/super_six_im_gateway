package com.cn.chatroom;

import com.alibaba.fastjson.JSONObject;
import com.cn.beans.ChatGroup;
import com.cn.beans.ChatRoom;
import com.cn.chatroom.command.FBImWsChatProcessor;
import com.cn.chatroom.command.FBImWsHandshakeProcessor;
import com.cn.chatroom.command.FBImWsLoginServiceProcessor;
import com.cn.chatroom.config.GroupConfig;
import com.cn.chatroom.config.JimConfig;
import com.cn.chatroom.listener.MQConsumeMsgListenerProcessor;
import com.cn.chatroom.task.MQConsumeTask;
import com.cn.chatroom.task.MQProducerTask;
import com.cn.chatroom.utils.TaskUtil;
import com.cn.constant.SysConstant;
import org.jim.common.ImConfig;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.config.PropertyImConfigBuilder;
import org.jim.common.packets.Command;
import org.jim.common.packets.Group;
import org.jim.server.ImServerStarter;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.ChatReqHandler;
import org.jim.server.command.handler.HandshakeReqHandler;
import org.jim.server.command.handler.LoginReqHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @ClassName: IMServerStart
 * @Description: (聊天室启动类)
 * @author BING
 * @date 2018年11月6日
 *
 */
public class IMServerStart {
	private final static Logger logger = LoggerFactory.getLogger(IMServerStart.class);
	public static void main(String[] args) {
		ImConfig imConfig = new PropertyImConfigBuilder("jim.properties").build();
		// 初始化SSL;(开启SSL之前,你要保证你有SSL证书哦...)
		// initSsl(imConfig);
		// 设置群组监听器，非必须，根据需要自己选择性实现;
		// imConfig.setImGroupListener(new FBImGroupListener());
		ImServerStarter imServerStarter = new ImServerStarter(imConfig);
		/***************** start 以下处理器根据业务需要自行添加与扩展 **********************************/
		HandshakeReqHandler handshakeReqHandler = CommandManager.getCommand(Command.COMMAND_HANDSHAKE_REQ, HandshakeReqHandler.class);
		// 添加自定义握手处理器;
		handshakeReqHandler.addProcessor(new FBImWsHandshakeProcessor());
		LoginReqHandler loginReqHandler = CommandManager.getCommand(Command.COMMAND_LOGIN_REQ, LoginReqHandler.class);
		// 添加登录业务处理器;
		loginReqHandler.addProcessor(new FBImWsLoginServiceProcessor());
		// 添加聊天业务处理器
		ChatReqHandler chatReqHandler = CommandManager.getCommand(Command.COMMAND_CHAT_REQ, ChatReqHandler.class);
		chatReqHandler.addProcessor(new FBImWsChatProcessor());
		/***************** end *******************************************************************************************/
		try {
			imServerStarter.start();
			// 加载聊天室配置
			initChatRoom();
		} catch (Exception e) {
			logger.error("系统错误", e);
		}
		//啟動消費者任務
		TaskUtil.execute(new MQConsumeTask(new MQConsumeMsgListenerProcessor()));
		//啟動生產者任務
		TaskUtil.execute(new MQProducerTask());
		logger.info("系統初始化完成.....");
		
	}
	private static void initChatRoom() {
		JedisTemplate jedisTemplate = null;
		try {
			jedisTemplate = JedisTemplate.me();
		} catch (Exception e) {
			logger.error("加载聊天室配置失败", e);
			return;
		}

		String jsonStr = jedisTemplate.getString(SysConstant.CHAT_ROOM_CONFIG);
		if(jsonStr==null){
			String host = JimConfig.ip;
			int port = JimConfig.port;
			ChatRoom chatRoom = new ChatRoom();
			chatRoom.setHost(host);
			chatRoom.setPort(port);
			List<Group> groups = GroupConfig.getGroups();
			List<ChatGroup> chatGroups = new ArrayList<ChatGroup>(groups.size());
			ChatGroup chatGroup = null;
			for (Group group : groups) {
				chatGroup = new ChatGroup();
				chatGroup.setGroupId(group.getGroup_id());
				chatGroup.setGroupName(group.getName());
				chatGroups.add(chatGroup);
			}
			chatRoom.setGroups(chatGroups);
			jedisTemplate.set(SysConstant.CHAT_ROOM_CONFIG, JSONObject.toJSONString(chatRoom));
			logger.info("加载聊天室配置成功......");
		}
	}
}
