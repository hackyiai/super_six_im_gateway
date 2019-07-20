package com.cn.chatroom.task;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.Jackson2JsonRedisSerializer;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.packets.ChatBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cn.chatroom.constant.Constant;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * 
    * @ClassName: SaveChatTask
    * @Description: TODO(保存聊天记录任务)
    * @author BING
    * @date 2018年11月6日
    *
 */
public class SaveChatTask implements Runnable {
	
	private final static Logger logger = LoggerFactory.getLogger(SaveChatTask.class);

	private ChatBody chatBody;
	
	public SaveChatTask(ChatBody chatBody){
		this.chatBody = chatBody;
	}
	
	@Override
	public void run() {
		if(null == chatBody)return;
		try {
			saveChatBody(chatBody);
		} catch (Exception e) {
			logger.error("保存聊天任务失败",e);
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
		if(StringUtils.isBlank(chatBody.getGroup_id())){
			logger.info("groupId is null , save chat message is fail !");
			return;
		}
		
		JedisTemplate jedisTemplate = JedisTemplate.me();
		String redisChatkey = Constant.REDIS_CHAT_BODY_LIST+Constant.SPLIT_KEY_TAG+chatBody.getGroup_id();
		jedisTemplate.listPushHeadAndTrim(redisChatkey, JSONObject.toJSONString(chatBody), Constant.REDIS_SAVE_CHAT_SIZE);
	}
	
	private static void pushOpsForHash(Object obj,String key){
		if(obj==null)return;
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        try {
        	JedisTemplate jedisTemplate = JedisTemplate.me();
        	byte[] body = jackson2JsonRedisSerializer.serialize(obj);
        	jedisTemplate.listPushHeadAndTrim(key,new String(body), Constant.REDIS_SAVE_CHAT_SIZE);
		} catch (Exception e) {
			logger.error("redis serialize is error",e);
		}
       
	}
}
