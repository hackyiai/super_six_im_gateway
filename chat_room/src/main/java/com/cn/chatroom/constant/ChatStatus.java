package com.cn.chatroom.constant;

import org.jim.common.Status;
/**
 * 
* @ClassName: ChatStatus
* @Description: TODO(聊天信息状态)
* @author BING
* @date 2018年11月6日
*
 */
public enum ChatStatus implements Status {
		
	MESSAGE_IS_NULL(10001,"message is null!","不可以发送空消息"),
	MESSAGE_IS_OVERFLOW(10002,"message is overflow!","消息内容溢出");
	
	private int status;
	
	private String description;
	
	private String text;
	
	private ChatStatus(int status, String description, String text) {
		this.status = status;
		this.description = description;
		this.text = text;
	}

	public int getStatus() {
		return status;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public int getCode() {
		return this.status;
	}

	@Override
	public String getMsg() {
		return this.getDescription()+" "+this.getText();
	}
}
