package com.cn.chatroom.service;

import org.jim.common.Jackson2JsonRedisSerializer;
import org.jim.common.cache.redis.JedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.constant.RedisKeyConstant;
import com.cn.interceptor.UserToken;
import com.cn.model.UserInfo;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserInfoService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);
	
	private static JedisTemplate redisTemplate = null;
	
	static{
		try {
			redisTemplate = JedisTemplate.me();
		} catch (Exception e) {
			logger.error("init redis is error ",e);
		}
	}
	
	public static UserInfo getUserInfo(Long userId){
		String key = String.format(RedisKeyConstant.HASH_USER_INFO, userId);
		return (UserInfo)getOpsForHash(key,userId);
	}
	
	public static UserToken reGenToken(Long userId){
		UserToken token = null;
		String key = String.format(RedisKeyConstant.HASH_USER_TOKEN, userId);
		Object obj = getOpsForHash(key,userId);
		if (obj != null) {
			token = (UserToken) obj;
		} 
		return token;
	}
	
	private static Object getOpsForHash(String key,Long userId){
		String objStr = redisTemplate.hashGet(key,userId.toString());
		if(objStr==null){
			return null;
		}
		Object result = null;
        try {
			Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
			ObjectMapper om = new ObjectMapper();
			om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
			om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
			jackson2JsonRedisSerializer.setObjectMapper(om);
        	result = jackson2JsonRedisSerializer.deserialize(objStr.getBytes());
		} catch (Exception e) {
			logger.info("redis serializer is error",e);
		}
        return result;
	}
}
