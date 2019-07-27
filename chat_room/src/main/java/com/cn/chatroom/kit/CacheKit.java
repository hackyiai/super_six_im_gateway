package com.cn.chatroom.kit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 缓存管理
 * 
 * @author caiqh
 *
 */
public class CacheKit {
	@SuppressWarnings("rawtypes")
	private static Map<String, CacheData> CACHE_DATA = new ConcurrentHashMap<>();
	/**
	 * 
	 * @param key
	 *            缓存key
	 * @param load
	 *            加载数据方法
	 * @param expire
	 *            过期时间
	 * @return
	 */
	public static <T> T getData(String key, Load<T> load, int expire) {
		// 获取缓存数据
		T data = getData(key);
		// 如果缓存数据为空，重新加载数据
		if (data == null && load != null) {
			data = load.load();
			if (data != null) {
				setData(key, data, expire);
			}
		}
		return data;
	}
	
	/**
	 * 获取缓存数据
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getData(String key) {
		CacheData<T> data = CACHE_DATA.get(key);
		if (data != null && (data.getExpire() <= 0 || data.getSaveTime() >= System.currentTimeMillis())) {
			return data.getData();
		}
		return null;
	}
	
	/**
	 * 设置缓存数据
	 * 
	 * @param key
	 *            缓存key
	 * @param data
	 *            缓存数据
	 * @param expire
	 *            过期时间單位秒
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T> void setData(String key, T data, int expire) {
		CacheData<T> cacheData = CACHE_DATA.get(key);
		if (cacheData != null) {
			cacheData.setCacheData(data, expire);
			CACHE_DATA.put(key, cacheData);
		}else{
			CACHE_DATA.put(key, new CacheData(data, expire));
		}
		
	}
	/**
	 * 清理单个缓存
	 * 
	 * @param key
	 */
	public static void clear(String key) {
		CACHE_DATA.remove(key);
	}
	/**
	 * 清理所有缓存
	 */
	public static void clearAll() {
		CACHE_DATA.clear();
	}
	/**
	 * 数据载体
	 * 
	 * @author Administrator
	 *
	 * @param <T>
	 */
	public interface Load<T> {
		T load();
	}
	/**
	 * 缓存基类
	 * 
	 * @author Administrator
	 *
	 * @param <T>
	 */
	private static class CacheData<T> {
		/**
		 * 
		 * @param t
		 *            緩存數據對象
		 * @param expire
		 *            過期時間單位秒
		 */
		CacheData(T t, int expire) {
			this.data = t;
			this.expire = expire <= 0 ? 0 : expire * 1000;
			this.saveTime = System.currentTimeMillis() + this.expire;
		}
		private T data; // 数据载体
		private long saveTime; // 存活时间
		private long expire; // 过期时间 小于等于0标识永久存活
		public T getData() {
			return data;
		}
		public long getExpire() {
			return expire;
		}
		public long getSaveTime() {
			return saveTime;
		}
		public void setCacheData(T t, int expire) {
			this.data = t;
			this.expire = expire <= 0 ? 0 : expire * 1000;
			this.saveTime = System.currentTimeMillis() + this.expire;
		}
	}
}
