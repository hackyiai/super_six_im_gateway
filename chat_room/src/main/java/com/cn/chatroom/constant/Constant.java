package com.cn.chatroom.constant;

import com.cn.constant.SysConstant;

/**
 * 
    * @ClassName: Constant
    * @Description: TODO(项目常量)
    * @author BING
    * @date 2018年11月5日
    *
 */
public class Constant {
	//路径分割符号
	public static final String SPLIT_PATH_TAG = "/";
	//分割符号
	public static final String SPLIT_KEY_TAG = "_";
	//分割tags
	public static final String SPLIT_MQ_TAG = "||";
	//登录属性
	public static final String LOGIN_PRO_USERNAME = "username";
	//密码属性
	public static final String LOGIN_PRO_PASSWORD = "password";
	//Token
	public static final String LOGIN_PRO_TOKEN = "token";
	//扩展字段值Extras
	public static final String LOGIN_PRO_EXTRAS = "extras";
	//redis聊天记录key
	public static final String REDIS_CHAT_BODY_LIST = "list_jim_chatbody";
	//聊天群配置文件
	public static final String GROUP_PROP_FILENAME = "jim_group.properties";
	//聊天服务器配置文件
	public static final String JIM_PROP_FILENAME = "jim.properties";
	//項目配置文件
	public static final String APP_PROP_FILENAME = "application.properties";
	//群组属性
	public static final String LOGIN_PRO_GROUPID = "groupId";
	//聊天内容长度
	public static final int CHAT_CONTENT_LEN = 3000;
	//用户默认头像
	public static final String USER_DEFAULT_HEADIMG = "http://images.rednet.cn/articleimage/2013/01/23/1403536948.jpg";
	//zookeeper服务地址
	public static final String ZK_SERVICE_HOST = "127.0.0.1";
	//zookeeper服务端口
	public static final int ZK_SERVICE_PROT = 2181;
	//zookeeper会话超时
	public static final int ZK_SESSION_TIMEOUT = 2000;
	//聊天室zookeeper根节点名字
	public static final String ZK_CHAT_ROOM_ROOT = "/chatroom";
	//聊天室zookeeper服务节点名字
	public static final String ZK_CHAT_ROOM_SERVER = "/server";
	//保存聊天记录条数
	public static final int REDIS_SAVE_CHAT_SIZE = 30;
	//没登录的用户昵称
	public static final String NO_LOGIN_NAME = "游客";
	//未登录用户默认ID
	public static final int NO_LOGIN_DEFAULT = 0;
	//用户属性
	public static final String USER_FIELD_USERID = "userId";
	//用户属性
	public static final String USER_FIELD_LEVEL = "level";
	//用户属性
	public static final String USER_FIELD_HEADIMG = "headImg";
	//用户属性
	public static final String USER_FIELD_NICKNAME = "nickname";
	//@用户ID
	public static final String USER_FIELD_TARGETUSERID = "targetUserId";
	//系統暱稱
	public static final String SYS_NICKNAME = "溫馨提示";
	//@他人提示语
	public static final String CHAT_CALL_MSG = "%s刚刚@了你,请注意查看消息！";
}
