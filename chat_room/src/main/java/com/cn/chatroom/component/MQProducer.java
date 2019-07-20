package com.cn.chatroom.component;

import com.cn.chatroom.config.RocketmqConfig;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cn.beans.MQInfo;
import com.cn.enums.MQTagsEnum;
import com.cn.enums.MQTopicTypeEnum;
import com.cn.kit.StrKit;
/**
 * 
* @ClassName: MQProducer
* @Description: (消息生產者)
* @author BING
* @date 2018年12月24日
*
 */
public class MQProducer {
	
	private final static Logger logger = LoggerFactory.getLogger(MQProducer.class);
	
	private static DefaultMQProducer producer = null;
	
	private MQProducer(){}
	
	public static void init(final DefaultMQProducer producer){
		if(null != MQProducer.producer)return;
		MQProducer.producer = producer;
	}
	
	public static DefaultMQProducer getInstance(){
		return MQProducer.producer;
	}
	
	/**
	 * 
	 * @Title: send
	 * @Description: (發送MQ消息)
	 * @param @param
	 *            type
	 * @param @param
	 *            msg
	 * @param @return
	 *            参数
	 * @return boolean 返回类型
	 *
	 */
	public static boolean send(MQTopicTypeEnum type, MQTagsEnum tag, MQInfo mqInfo) {
		SendStatus sendStatus = null;
		//默认是等级0，普通用户
		if(mqInfo.getLevel()==null)mqInfo.setLevel(0);
		try {
			SendResult sendResult = send(RocketmqConfig.getTopics(), tag.getType(), JSON.toJSONBytes(mqInfo),0);
			if (null == sendResult)
				return false;
			sendStatus = sendResult.getSendStatus();
		} catch (Exception e) {
			logger.error("MQ send topic " + type.getType() + " is error ", e);
			return false;
		}
		return SendStatus.SEND_OK == sendStatus;
	}
	
	/**
	 * 
	 * @Title: send
	 * @Description: (發送延時MQ消息30分鐘)
	 * @param @param
	 *            type
	 * @param @param
	 *            msg
	 * @param @return
	 *            参数
	 * @return boolean 返回类型
	 *
	 */
	public static boolean sendDelayedThirtyminute(MQTopicTypeEnum type, MQTagsEnum tag, MQInfo mqInfo) {
		SendStatus sendStatus = null;
		//默认是等级0，普通用户
		if(mqInfo.getLevel()==null)mqInfo.setLevel(0);
		try {
			logger.info("send MQ...，message:{}", JSON.toJSON(mqInfo));
			SendResult sendResult = send(RocketmqConfig.getTopics(), tag.getType(), JSON.toJSONBytes(mqInfo),16);
			if (null == sendResult)
				return false;
			sendStatus = sendResult.getSendStatus();
		} catch (Exception e) {
			logger.error("MQ send topic " + type.getType() + " is error ", e);
			return false;
		}
		return SendStatus.SEND_OK == sendStatus;
	}
	
	/**
	 * 
	 * @Title: send
	 * @Description: 消息发送
	 * @param @param
	 *            type
	 * @param @param
	 *            msg
	 * @param @return
	 *            参数
	 * @return boolean 返回类型
	 *
	 */
	public static boolean send(MQTopicTypeEnum type, MQTagsEnum tag, String msg) {
		SendStatus sendStatus = null;
		try {
			SendResult sendResult = send(RocketmqConfig.getTopics(), tag.getType(), msg);
			if (null == sendResult)
				return false;
			sendStatus = sendResult.getSendStatus();
		} catch (Exception e) {
			logger.error("MQ send topic " + type.getType() + " is error ", e);
			return false;
		}
		return SendStatus.SEND_OK == sendStatus;
	}
	
	/**
	 * 
    * @Title: send
    * @Description: (发送信息)
    * @param @param topic
    * @param @param tags
    * @param @param msg
    * @param @return
    * @param @throws Exception    参数
    * @return SendResult    返回类型
    *
	 */
	private static SendResult send(String topic, String tags, String msg) throws Exception {
		if (StrKit.isBlank(msg)) {
			logger.info("you send empty content , what do you mean ?");
			return null;
		}
		logger.info("send MQ...，message:{}", msg);
		return send(topic, tags, msg.getBytes(),0);
	}
	
	/**
	 * 
    * @Title: send
    * @Description: (发送信息)
    * @param @param topic
    * @param @param tags
    * @param @param msg
    * @param @return
    * @param @throws Exception    参数
    * @return SendResult    返回类型
    *
	 */
	private static SendResult send(String topic, String tags, byte[] msg,int delayTimeLevel) throws Exception {
		if (msg.length <= 0) {
			logger.info("you send empty content , what do you mean ?");
			return null;
		}
		Message sendMsg = new Message(topic, tags, msg);
		// 默认3秒超时
		SendResult sendResult = null;
		if(delayTimeLevel>0){
			sendMsg.setDelayTimeLevel(delayTimeLevel);
			sendResult = getInstance().send(sendMsg);
		}else{
			sendResult = getInstance().send(sendMsg);
		}
		logger.info("MQ response message ：" + sendResult.toString());
		return sendResult;
	}
}
