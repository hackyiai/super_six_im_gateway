package com.cn.chatroom.listener;

import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.ImSessionContext;
import org.jim.common.packets.Client;
import org.jim.common.packets.Command;
import org.jim.common.packets.ExitGroupNotifyRespBody;
import org.jim.common.packets.RespBody;
import org.jim.common.packets.User;
import org.jim.server.listener.ImGroupListener;
import org.tio.core.ChannelContext;
/**
 * 
    * @ClassName: FBImGroupListener
    * @Description: TODO(群组监听器)
    * @author BING
    * @date 2018年11月5日
    *
 */
public class FBImGroupListener extends ImGroupListener{
	/**
	 * 退出群组异步通知
	 */
	@Override
	public void onAfterUnbind(ChannelContext channelContext, String group) throws Exception {
		//发退出房间通知  COMMAND_EXIT_GROUP_NOTIFY_RESP
		ImSessionContext imSessionContext = (ImSessionContext)channelContext.getAttribute();
		ExitGroupNotifyRespBody exitGroupNotifyRespBody = new ExitGroupNotifyRespBody();
		exitGroupNotifyRespBody.setGroup(group);
		Client client = imSessionContext.getClient();
		if(client == null)
			return;
		User clientUser = client.getUser();
		if(clientUser == null)
			return;
		User notifyUser = new User(clientUser.getId(),clientUser.getNick());
		exitGroupNotifyRespBody.setUser(notifyUser);
		
		RespBody respBody = new RespBody(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP,exitGroupNotifyRespBody);
		ImPacket imPacket = new ImPacket(Command.COMMAND_EXIT_GROUP_NOTIFY_RESP, respBody.toByte());
		ImAio.sendToGroup(group, imPacket);
		
	}
}
