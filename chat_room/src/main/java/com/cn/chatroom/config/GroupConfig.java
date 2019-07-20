package com.cn.chatroom.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jim.common.packets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.enums.ChatGroupEnum;
/**
 * 
* @ClassName: GroupConfig
* @Description: TODO(群聊配置)
* @author BING
* @date 2018年11月2日
*
 */
public class GroupConfig {
	
	private final static Logger logger = LoggerFactory.getLogger(GroupConfig.class);
	
	public static List<Group> groups = null;
	
	public static Map<String,Group> groupMap = null;
	
	static{
		init();
	}
	
	private GroupConfig(){}
	
	/**
	 * 
    * @Title: init
    * @Description: TODO(加载群聊配置到集合)
    * @param     参数
    * @return void    返回类型
    * @throws
	 */
	private static void init(){
		ChatGroupEnum arr[] = ChatGroupEnum.values();
		groups = new ArrayList<>(arr.length);
		groupMap = new HashMap<>(arr.length);
		Group group = null;
		String key = "";
		for(ChatGroupEnum item : arr){
			key = item.getType()+"";
			group = new Group();
			group.setGroup_id(key);
			group.setName(item.getDesc());
			groups.add(group);
			groupMap.put(key,group);
		}
	}
	
	/**
	 * 
    * @Title: getGroupConfig
    * @Description: TODO(返回群聊配置)
    * @param @return    参数
    * @return List<Group>    返回类型
    * @throws
	 */
	public static List<Group> getGroups(){
		return GroupConfig.groups;
	}
	
	/**
	 * 
    * @Title: getGroupConfig
    * @Description: TODO(返回群聊配置)
    * @param @return    参数
    * @return List<Group>    返回类型
    * @throws
	 */
	public static Map<String,Group> getGroupConfig(){
		return GroupConfig.groupMap;
	}
}
