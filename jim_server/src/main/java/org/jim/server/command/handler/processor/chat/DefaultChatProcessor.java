package org.jim.server.command.handler.processor.chat;

import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Command;
import org.jim.common.packets.RespBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
/**
 * @author WChao
 * @date 2018年4月3日 下午1:12:30
 */
public class DefaultChatProcessor extends AbstractChatProcessor{
	
	Logger log = LoggerFactory.getLogger(DefaultChatProcessor.class);
	
	@Override
	public void doHandler(ChatBody chatBody, ChannelContext channelContext){
	}

	@Override
	public RespBody beforeHandler(ChatBody chatBody, ChannelContext channelContext) {
		return new RespBody(Command.COMMAND_CHAT_REQ,chatBody);
	}
}
