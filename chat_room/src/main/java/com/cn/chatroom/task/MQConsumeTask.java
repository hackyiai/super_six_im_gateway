package com.cn.chatroom.task;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.config.RocketmqConfig;
import com.cn.chatroom.constant.Constant;
import com.cn.chatroom.listener.MQConsumeMsgListenerProcessor;
import com.cn.enums.MQTagsEnum;
/**
 * 
* @ClassName: MQConsumeTask
* @Description: (MQ任務啟動)
* @author BING
* @date 2018年11月23日
*
 */
public class MQConsumeTask implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(MQConsumeTask.class);
    
	private MQConsumeMsgListenerProcessor mqMessageListenerProcessor;
	
	public MQConsumeTask(MQConsumeMsgListenerProcessor mqMessageListenerProcessor ){
		this.mqMessageListenerProcessor = mqMessageListenerProcessor;
	}

	@Override
	public void run() {
		DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketmqConfig.getGroupName());
        consumer.setNamesrvAddr(RocketmqConfig.getNamesrvAddr());
        consumer.setConsumeThreadMin(RocketmqConfig.getConsumeThreadMin());
        consumer.setConsumeThreadMax(RocketmqConfig.getConsumeThreadMax());
        consumer.setConsumeMessageBatchMaxSize(RocketmqConfig.getConsumeMessageBatchMaxSize());
        try {
        	StringBuilder sb = new StringBuilder();
        	for(MQTagsEnum tag : MQTagsEnum.values()){
        		sb.append(tag.getType()).append(Constant.SPLIT_MQ_TAG);
        	}
        	String str = sb.toString();
        	if(str.length()>0){
        		int index = str.lastIndexOf(Constant.SPLIT_MQ_TAG);
        		str = str.substring(0, index);
        	}
        	logger.info("subscribe topic : "+RocketmqConfig.getTopics()+" | tags : "+str);
        	consumer.subscribe(RocketmqConfig.getTopics(),sb.toString());
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.registerMessageListener(mqMessageListenerProcessor);
            consumer.start();
            logger.info("start consumer success!!!");
        } catch (MQClientException e) {
            logger.error("consumer subscribe error , " + e);
        }
	}
	
}
