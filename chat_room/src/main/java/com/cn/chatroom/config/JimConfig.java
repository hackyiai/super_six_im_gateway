package com.cn.chatroom.config;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.constant.Constant;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;

public class JimConfig {
	
	private final static Logger logger = LoggerFactory.getLogger(JimConfig.class);
	
	
	public static String bindIp;
	
	public static int port;
	
	public static String ip;
	
	static{
		PropKit.use(Constant.JIM_PROP_FILENAME);
		init();
	}

	private JimConfig(){}
	
	private static void init(){
		final Prop prop = PropKit.getProp(Constant.JIM_PROP_FILENAME);
		if(null == prop){
			logger.info("jim properties file is not found ! ");
			return;
		}
		Properties properties = prop.getProperties();
		ip = properties.getProperty("jim.ip");
		bindIp = properties.getProperty("jim.bind.ip");
		String portStr = properties.getProperty("jim.port");
		port = StringUtils.isBlank(portStr) ? 0 : Integer.valueOf(portStr);
	}
}
