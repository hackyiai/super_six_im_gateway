package com.cn.chatroom.task;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.component.MQProducer;
import com.cn.chatroom.config.RocketmqConfig;

public class MQProducerTask implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(MQProducerTask.class);
	
	@Override
	public void run() {
		  DefaultMQProducer producer = new DefaultMQProducer(RocketmqConfig.getGroupName());
		  producer.setNamesrvAddr(RocketmqConfig.getNamesrvAddr());
		  producer.setMaxMessageSize(RocketmqConfig.getProducerMaxMessageSize());
		  producer.setSendMsgTimeout(RocketmqConfig.getProducerSendMsgTimeout());
		  producer.setRetryTimesWhenSendFailed(RocketmqConfig.getProducerRetryTimesWhenSendFailed());
		  try {
			producer.start();
			MQProducer.init(producer);
			logger.error("Producer is start");
		} catch (MQClientException e) {
			logger.error("Producer start is error",e);
		}
	}
	
}
