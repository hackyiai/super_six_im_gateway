package com.cn.chatroom.controller;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.http.HttpConfig;
import org.jim.common.http.HttpRequest;
import org.jim.common.http.HttpResponse;
import org.jim.server.http.annotation.RequestPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.constant.Constant;
/**
 * 
    * @ClassName: ChatRoomController
    * @Description: TODO(聊天室HTTP协议接口)
    * @author BING
    * @date 2018年11月5日
    *
 */
@RequestPath("chatroom")
public class ChatRoomController {
	
	private final static Logger logger = LoggerFactory.getLogger(ChatRoomController.class);
	
	@RequestPath("getNewChats")
	public HttpResponse getNewChats(HttpRequest request, HttpConfig httpConfig){
		HttpResponse result = new HttpResponse(request,httpConfig);
		try {
			Object obj[] = request.getParams().get(Constant.LOGIN_PRO_GROUPID);
			Object groupObj = obj !=null ? obj[0] : 0; 
			String groupId = groupObj.toString();
			JedisTemplate jedisTemplate = JedisTemplate.me();
			String jsonString = jedisTemplate.getString(Constant.REDIS_CHAT_BODY_LIST+Constant.SPLIT_KEY_TAG+groupId);
			if(StringUtils.isBlank(jsonString)){
				result.setBody(null);
				return result;
			}
			result.setBody(jsonString.getBytes());
		} catch (Exception e) {
			logger.error("getNewChats error : ",e);
		}
		return result;
	}
	
}
