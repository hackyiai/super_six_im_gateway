package com.cn.chatroom.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.constant.Constant;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

public class RocketmqConfig {
	
	private RocketmqConfig(){}
	
	private static volatile boolean isLoad = false;
	
	private final static Logger logger = LoggerFactory.getLogger(RocketmqConfig.class);
	//该应用是否启用消费者
	private static String isOnOff = "off";
	//組名
	private static String groupName;
	//mq的nameserver地址
	private static String namesrvAddr;
	//该消费者订阅的主题topic;topic2;
	private static String topics;
	//消費者最小線程數
	private static int consumeThreadMin = 20;
	//消費者最大線程數
	private static int consumeThreadMax = 64;
	//设置一次消费消息的条数，默认为1条
	private static int consumeMessageBatchMaxSize = 1;
	//消息最大长度 默认1024*4(4M)
	private static int producerMaxMessageSize = 4096;
	//发送消息超时时间,默认5秒
	private static int producerSendMsgTimeout = 5000;
	//发送消息失败重试次数，默认5
	private static int producerRetryTimesWhenSendFailed = 5;
	
	static{
		PropKit.use(Constant.APP_PROP_FILENAME);
		init();
	}
	
	private static void init(){
		if(isLoad){
			isLoad = true;
			return;
		}
		final Prop prop = PropKit.getProp(Constant.APP_PROP_FILENAME);
		if(null == prop){
			logger.info("group properties file is not found ! ");
			return;
		}
		Properties properties = prop.getProperties();
		
		String strIsOnOff = null,strGroupName=null,strNamesrvAddr=null,strTopics=null,strConsumeThreadMin=null,strConsumeThreadMax=null,strConsumeMessageBatchMaxSize=null,
				strProducerMaxMessageSize=null,strProducerSendMsgTimeout = null,strProducerRetryTimesWhenSendFailed = null;
		strIsOnOff = properties.getProperty("rocketmq.consumer.isOnOff");
		strGroupName = properties.getProperty("rocketmq.consumer.groupName");
		strNamesrvAddr = properties.getProperty("rocketmq.consumer.namesrvAddr");
		strTopics = properties.getProperty("rocketmq.consumer.topic");
		strConsumeThreadMin = properties.getProperty("rocketmq.consumer.consumeThreadMin");
		strConsumeThreadMax = properties.getProperty("rocketmq.consumer.consumeThreadMax");
		strConsumeMessageBatchMaxSize = properties.getProperty("rocketmq.consumer.consumeMessageBatchMaxSize");
		strProducerMaxMessageSize = properties.getProperty("rocketmq.producer.maxMessageSize");
		strProducerSendMsgTimeout = properties.getProperty("rocketmq.producer.sendMsgTimeout");
		strProducerRetryTimesWhenSendFailed = properties.getProperty("rocketmq.producer.retryTimesWhenSendFailed");
		if(!StrKit.isBlank(strIsOnOff))isOnOff = strIsOnOff;
		if(!StrKit.isBlank(strGroupName))groupName = strGroupName;
		if(!StrKit.isBlank(strNamesrvAddr))namesrvAddr = strNamesrvAddr;
		if(!StrKit.isBlank(strTopics))topics = strTopics;
		if(!StrKit.isBlank(strConsumeThreadMin))consumeThreadMin = Integer.valueOf(strConsumeThreadMin);
		if(!StrKit.isBlank(strConsumeThreadMax))consumeThreadMax =  Integer.valueOf(strConsumeThreadMax);
		if(!StrKit.isBlank(strConsumeMessageBatchMaxSize))consumeMessageBatchMaxSize =  Integer.valueOf(strConsumeMessageBatchMaxSize);
		if(!StrKit.isBlank(strProducerMaxMessageSize))producerMaxMessageSize =  Integer.valueOf(strProducerMaxMessageSize);
		if(!StrKit.isBlank(strProducerSendMsgTimeout))producerSendMsgTimeout =  Integer.valueOf(strProducerSendMsgTimeout);
		if(!StrKit.isBlank(strProducerRetryTimesWhenSendFailed))producerRetryTimesWhenSendFailed =  Integer.valueOf(strProducerRetryTimesWhenSendFailed);
	}
	
	
	public static int getProducerMaxMessageSize() {
		return producerMaxMessageSize;
	}
	public static int getProducerSendMsgTimeout() {
		return producerSendMsgTimeout;
	}
	public static int getProducerRetryTimesWhenSendFailed() {
		return producerRetryTimesWhenSendFailed;
	}
	public static String getIsOnOff(){
		return isOnOff;
	} 
	
	public static String getGroupName(){
		return groupName;
	} 
	
	public static String getNamesrvAddr(){
		return namesrvAddr;
	} 
	
	public static String getTopics(){
		return topics;
	} 
	
	public static int getConsumeThreadMin(){
		return consumeThreadMin;
	} 
	
	public static int getConsumeThreadMax(){
		return consumeThreadMax;
	} 
	
	public static int getConsumeMessageBatchMaxSize(){
		return consumeMessageBatchMaxSize;
	} 
}
