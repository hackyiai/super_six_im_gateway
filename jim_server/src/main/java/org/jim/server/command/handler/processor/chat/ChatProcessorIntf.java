package org.jim.server.command.handler.processor.chat;

import org.jim.common.ImPacket;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.RespBody;
import org.jim.server.command.handler.processor.ProcessorIntf;
import org.tio.core.ChannelContext;
/**
 * @author WChao
 * @date 2018年4月2日 下午3:21:01
 */
public interface ChatProcessorIntf extends ProcessorIntf{
	public RespBody beforeHandler(ChatBody chatBody, ChannelContext channelContext);
	public void handler(ImPacket chatPacket,ChannelContext channelContext)  throws Exception;
}
