package com.cn.chatroom.zookeeper;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.bean.ZkNode;
import com.cn.chatroom.config.JimConfig;
import com.cn.chatroom.constant.Constant;

public class ZkService {
	
	private final static Logger logger = LoggerFactory.getLogger(ZkService.class);
	
	private ZkService(){}

	/** 信号量，阻塞程序执行，用于等待zookeeper连接成功，发送成功信号 */
	static final CountDownLatch connectedSemaphore = new CountDownLatch(1);

	/**
	 * 
    * @Title: connnect
    * @Description: TODO(获取zk连接)
    * @param @return
    * @param @throws Exception    参数
    * @return ZooKeeper    返回类型
    * @throws
	 */
	public static ZooKeeper connnect() throws Exception {
		ZooKeeper zk = new ZooKeeper(Constant.ZK_SERVICE_HOST+Constant.SPLIT_KEY_TAG+Constant.ZK_SERVICE_PROT, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
			@Override
			public void process(WatchedEvent event) {
				// 获取事件的状态
				KeeperState keeperState = event.getState();
				EventType eventType = event.getType();
				// 如果是建立连接
				if (KeeperState.SyncConnected == keeperState) {
					if (EventType.None == eventType) {
						// 如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
						logger.info("zk建立连接成功......");
						connectedSemaphore.countDown();
					}
				}
			}
		});
		//阻塞
		connectedSemaphore.await();
		return zk;
	}
	
	/**
	 * 
    * @Title: createZkNode
    * @Description: TODO(创建ZK节点)
    * @param @param zkNode
    * @param @throws Exception    参数
    * @return void    返回类型
    * @throws
	 */
	public static void createZkNode(ZkNode zkNode) throws Exception{
		ZooKeeper zk = null;
		String code = null;
		try {
			zk = ZkService.connnect();
			if(!isExist(zk,zkNode.getPath())){
				code = zk.create(zkNode.getPath(),zkNode.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			logger.info("create code : "+code);
		} catch (Exception e) {
			logger.error("zk连接错误",e);
		}finally {
			if(null != zk)zk.close();
		}
	}
	
	/**
	 * 
    * @Title: updateZkNode
    * @Description: TODO(修改ZK节点)
    * @param @param zkNode
    * @param @throws Exception    参数
    * @return void    返回类型
    * @throws
	 */
	public static void updateZkNode(ZkNode zkNode)throws Exception{
		ZooKeeper zk = null;
		try {
			zk = ZkService.connnect();
			if(isExist(zk,zkNode.getPath())){
				logger.info("update content is : "+zkNode.toString());
				zk.setData(zkNode.getPath(),zkNode.toString().getBytes(), -1);
			}
		} catch (Exception e) {
			logger.error("zk连接错误",e);
		}finally {
			if(null != zk)zk.close();
		}
	}
	
	/**
	 * 
    * @Title: delZkNode
    * @Description: TODO(删除ZK节点)
    * @param @param path
    * @param @throws Exception    参数
    * @return void    返回类型
    * @throws
	 */
	public static void delZkNode(String path) throws Exception{
		ZooKeeper zk = null;
		try {
			zk = ZkService.connnect();
			if(isExist(zk,path)){
				zk.delete(path,-1);
			}
		} catch (Exception e) {
			logger.error("zk连接错误",e);
		}finally {
			if(null != zk)zk.close();
		}
	}
	
	/**
	 * 
    * @Title: isExist
    * @Description: TODO(判断ZK目录是否存在)
    * @param @param zk
    * @param @param path
    * @param @return
    * @param @throws Exception    参数
    * @return boolean    返回类型
    * @throws
	 */
	public static boolean isExist(ZooKeeper zk,String path)throws Exception{
		String arr[] = path.split(Constant.SPLIT_PATH_TAG);
		if(arr.length<2)return false;
		List<String> list = null;
		if(arr.length>2){
			path = path.substring(0, path.lastIndexOf(Constant.SPLIT_PATH_TAG));
		}else if(arr.length == 2){
			path = Constant.SPLIT_PATH_TAG;
		}
		list = zk.getChildren(path, true);
		long count = 0;
		if(!list.isEmpty()){
			final String tag = arr[arr.length-1];
			count = list.stream().filter(str -> str.contains(tag)).count();
		}
		return count > 0 ? true : false;
	}
	
	/**
	 * 
    * @Title: listNode
    * @Description: TODO(获取zk指定目录节点)
    * @param @param path
    * @param @return
    * @param @throws Exception    参数
    * @return List<String>    返回类型
    * @throws
	 */
	public static List<String> listNode(String path) throws Exception{
		ZooKeeper zk = null;
		List<String> list = null;
		try {
			zk = ZkService.connnect();
			list = zk.getChildren(path, true);
		} catch (Exception e) {
			logger.error("zk连接错误",e);
		}finally {
			if(null != zk)zk.close();
		}
		return list;
	}
	
	/**
	 * 
    * @Title: initZKChatRoom
    * @Description: TODO(加载zk聊天室配置)
    * @param @throws Exception    参数
    * @return void    返回类型
    * @throws
	 */
	public static void initZKChatRoom() throws Exception{
		ZooKeeper zk = ZkService.connnect();
		boolean flag = isExist(zk,Constant.ZK_CHAT_ROOM_ROOT);
		if(!flag){
			ZkNode  zkNode = new ZkNode();
			zkNode.setPath(Constant.ZK_CHAT_ROOM_ROOT);
			zk.create(zkNode.getPath(),zkNode.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			logger.info("create chatroom zk root path success ! ");
		}
		flag = isExist(zk,Constant.ZK_CHAT_ROOM_ROOT+Constant.ZK_CHAT_ROOM_SERVER);
		if(!flag){
			ZkNode  zkNode = new ZkNode();
			zkNode.setPath(Constant.ZK_CHAT_ROOM_ROOT+Constant.ZK_CHAT_ROOM_SERVER);
			zkNode.setHost(JimConfig.bindIp);
			zkNode.setPort(JimConfig.port);
			zk.create(zkNode.getPath(),zkNode.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			logger.info("create chatroom zk server path success ! ");
		}
	}
}
