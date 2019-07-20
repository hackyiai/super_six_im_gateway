package com.cn.interceptor;
import java.io.Serializable;
import java.sql.Timestamp;
/**
 * 
 * 用戶API Token緩存
 * 
 */
public class UserToken implements Serializable {
	private static final long serialVersionUID = -1716297412245522649L;
	private String token;
	private Timestamp genTime;
	private Timestamp expireTime;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Timestamp getGenTime() {
		return genTime;
	}
	public void setGenTime(Timestamp genTime) {
		this.genTime = genTime;
	}
	public Timestamp getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Timestamp expireTime) {
		this.expireTime = expireTime;
	}
}
