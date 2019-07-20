package com.cn.chatroom.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * 
    * @ClassName: ZkData
    * @Description: TODO(zookeeper数据)
    * @author BING
    * @date 2018年11月6日
    *
 */
public class ZkData {
	/**
	 * 主机
	 */
	private String host;
	/**
	 * 端口
	 */
	private int port;
	/**
	 * 创建时间
	 */
	private Long createTime;
	
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
}
