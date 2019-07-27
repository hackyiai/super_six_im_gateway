package com.cn.chatroom.command;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.http.HttpConst;
import org.jim.common.http.HttpRequest;
import org.jim.common.packets.Command;
import org.jim.common.packets.LoginReqBody;
import org.jim.common.utils.JsonKit;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.LoginReqHandler;
import org.jim.server.command.handler.processor.handshake.WsHandshakeProcessor;
import org.tio.core.ChannelContext;

import com.alibaba.fastjson.JSONObject;
import com.cn.chatroom.constant.Constant;
/**
 * 
    * @ClassName: FBImWsHandshakeProcessor
    * @Description: TODO(握手业务处理类)
    * @author BING
    * @date 2018年11月5日
    *
 */
public class FBImWsHandshakeProcessor extends WsHandshakeProcessor {

	//private final static Logger logger = LoggerFactory.getLogger(FBImWsHandshakeProcessor.class);
	
	@Override
	public void onAfterHandshaked(ImPacket packet, ChannelContext channelContext) throws Exception {
		LoginReqHandler loginHandler = (LoginReqHandler)CommandManager.getCommand(Command.COMMAND_LOGIN_REQ);
		HttpRequest request = (HttpRequest)packet;
		Object []arrNmae , arrPassword , arrToken , arrExtras;
		arrNmae = request.getParams().get(Constant.LOGIN_PRO_USERNAME);
		arrPassword = request.getParams().get(Constant.LOGIN_PRO_PASSWORD);
		arrToken = request.getParams().get(Constant.LOGIN_PRO_TOKEN);
		arrExtras = request.getParams().get(Constant.LOGIN_PRO_EXTRAS);
		
		String username = arrNmae == null ? null : (String)arrNmae[0];
		String password = arrPassword == null ? null : (String)arrPassword[0];
		String token = arrToken == null ? null : (String)arrToken[0];
		String extrasStr = arrExtras == null ? null : (String)arrExtras[0];
		
		LoginReqBody loginBody = new LoginReqBody(username,password,token);
		//设置扩展字段值
		if(StringUtils.isNotBlank(extrasStr)){
			loginBody.setExtras(JSONObject.parseObject(extrasStr));
		}
		byte[] loginBytes = JsonKit.toJsonBytes(loginBody);
		request.setBody(loginBytes);
		request.setBodyString(new String(loginBytes,HttpConst.CHARSET_NAME));
		ImPacket loginRespPacket = loginHandler.handler(request, channelContext);
		if(loginRespPacket != null){
			ImAio.send(channelContext, loginRespPacket);
		}
	}
	
}
