package com.cn.chatroom.zookeeper;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.chatroom.constant.Constant;

public class ZkSubscribe implements Watcher {
	
	private final static Logger logger = LoggerFactory.getLogger(ZkSubscribe.class);

	private static CountDownLatch latch = new CountDownLatch(1);
	private static Stat stat = new Stat();
	private static ZooKeeper zk = null;

	/**
	 * 
    * @Title: main
    * @Description: TODO(启动zk订阅服务)
    * @param @param args    参数
    * @return void    返回类型
    * @throws
	 */
	public static void main(String[] args) {
		try {
			String path = Constant.ZK_CHAT_ROOM_ROOT+Constant.ZK_CHAT_ROOM_SERVER;
			zk = new ZooKeeper(Constant.ZK_SERVICE_HOST+Constant.SPLIT_KEY_TAG+Constant.ZK_SERVICE_PROT, Constant.ZK_SESSION_TIMEOUT, new ZkSubscribe());
			latch.await();
			byte[] temp = zk.getData(path, true, stat);
			logger.info("init data :pulish node data" + new String(temp));
			while (true) {
				Thread.sleep(Integer.MAX_VALUE);
			}
		} catch (Exception e) {
			logger.error("订阅ZK异常 : ",e);
		}

	}

	/**
	 * 订阅通知回调
	 */
	public void process(WatchedEvent event) {
		if (Event.KeeperState.SyncConnected == event.getState()) {
			if (Event.EventType.None == event.getType() && event.getPath() == null) {
				latch.countDown();
			} else if (event.getType() == Event.EventType.NodeDataChanged) {
				try {
					byte[] newByte = zk.getData(event.getPath(), true, stat);
					logger.info("path:" + event.getPath() + "\tdata has changed.\t new Data :" + new String(newByte));
				} catch (Exception e) {
					logger.error("订阅ZK回调异常 : ",e);
				}
			}
		}
	}
}
