package com.cn.chatroom.listener;

import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.jim.common.cache.redis.JedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.constant.SysConstant;
/**
 * 
* @ClassName: MQConsumeMsgListenerProcessor
* @Description: (MQ消費者)
* @author BING
* @date 2018年11月23日
*
 */
public class MQConsumeMsgListenerProcessor implements MessageListenerConcurrently{
	
	private static final Logger logger = LoggerFactory.getLogger(MQConsumeMsgListenerProcessor.class);
	
	private static JedisTemplate jedisTemplate = null;
	
	static{
		try {
			jedisTemplate = JedisTemplate.me();
		} catch (Exception e) {
			logger.error("init redis is error ",e);
		}
	}
	
	/**
	 *  默认msgs里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息<br/>
	 *  不要抛异常，如果没有return CONSUME_SUCCESS ，consumer会重新消费该消息，直到return CONSUME_SUCCESS
	 */
	@Override
	public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
		if(msgs.isEmpty()){
			logger.info("接受到的消息为空，不处理，直接返回成功");
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
		//是否是重复消费
		MessageExt messageExt = msgs.get(0);
		int reconsume = messageExt.getReconsumeTimes();
		String msg = new String(messageExt.getBody());
		logger.info("接受到的消息內容：消息msgId:{}，主题topic:{}，标识tags:{}，重试次数reconsumeTimes:{}，消息体msg:{}",
				messageExt.getMsgId(),messageExt.getTopic(),messageExt.getTags(),messageExt.getReconsumeTimes(),msg);
		//消息已经重试了5次，如果不需要再次消费，则返回成功
		if(reconsume ==SysConstant.MQ_RETRY_SEND_FAILED_COUNT){
			logger.info("多次重复的记录，内容 : "+msg);
			return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
		}
		
		//这里才是自己需要处理的业务
		

		// 如果没有return success ，consumer会重新消费该消息，直到return success
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
	}
	

}
