package com.cn.chatroom.bean;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * 
* @ClassName: ZkNode
* @Description: TODO(zookeeper节点)
* @author BING
* @date 2018年11月6日
*
 */
public class ZkNode implements Serializable{
	
	private String path;
	
	private ZkData data;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ZkData getData() {
		return data;
	}

	public void setData(ZkData data) {
		this.data = data;
	}
	
	public void setHost(String host) {
		if(StringUtils.isBlank(host))return;
		if(this.getData() == null)this.data = new ZkData();
		this.data.setHost(host);
	}
	
	public void setPort(int port) {
		if(this.getData() == null)this.data = new ZkData();
		this.data.setPort(port);
	}
	
	public void setCreateTime(Long createTime) {
		if(this.getData() == null)this.data = new ZkData();
		this.data.setCreateTime(createTime);
	}
	
	@Override
	public String toString() {
		return (null == data) ? "" : data.toString();
	}
}
