package com.cn.chatroom.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
    * @ClassName: RedisSaveTask
    * @Description: (redis缓存任务)
    * @author BING
    * @date 2018年11月6日
    *
 */
public class TaskUtil{
	
	private final static Logger logger = LoggerFactory.getLogger(TaskUtil.class);
	
	private final static int COREPOOLSIZE = 2;
	
	private final static int KEEPALIVETIME = 60;
	
	private final static int QUEUE_LEN = 200;
	
	private TaskUtil(){}
	
	private static final ThreadPoolExecutor threadPool;
	
	static{
		threadPool = new ThreadPoolExecutor(COREPOOLSIZE,
				Runtime.getRuntime().availableProcessors()*2,
				KEEPALIVETIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(QUEUE_LEN),
                new MyRejected()
                );
	}
	
	public static void execute(Runnable task){
		threadPool.execute(task);
	}
	
	static class MyRejected implements RejectedExecutionHandler{

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			logger.error("隊列線程池已經滿了...，請趕緊處理");
		}
		
	}
}
