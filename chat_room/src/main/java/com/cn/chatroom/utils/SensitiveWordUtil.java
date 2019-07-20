package com.cn.chatroom.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jim.common.cache.redis.JedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cn.constant.RedisKeyConstant;
import com.cn.kit.StrKit;

/***
 * 敏感字工具类字
 * @author leiming
 *
 */
public class SensitiveWordUtil {

	private static Logger log = LoggerFactory.getLogger(SensitiveWordUtil.class);
	
	@SuppressWarnings("rawtypes")
	private static Map sensitiveWordMap = null;
	public static int minMatchTYpe = 1;      //最小匹配规则
	public static int maxMatchType = 2;      //最大匹配规则
	private static String replaceChar = "*";        //敏感词替换字符
	private static int expire = 60*5;				 //缓存有效期,单位秒
	private static String status = "1";			 //关键词过滤开关 1 开启  0关闭
	private static JedisTemplate jedisTemplate;
	/**
	 * 替换敏感字字符,只匹配指定字段：title，content
	 * @param name 字段名称
	 * @param txt 需要替换的内容  
	 * @return
	 */
	public static String replaceSensitiveWord(String name,String txt){
		if ("title".equals(name) || "content".equals(name)) {
			return replaceSensitiveWord(txt);
		}
		return txt;
		
	}
	
	/**
	 * 替换敏感字字符,替换字符，默认*
	 * @param txt 需要替换的内容 
	 */
	@SuppressWarnings("unchecked")
	public static String replaceSensitiveWord(String txt){
		if(jedisTemplate==null) {
			try {
				jedisTemplate = JedisTemplate.me();
			} catch (Exception e) {
				log.error("jedisTemplate 初始化异常 e:{}",e);
			}
		}

		int matchType = 1;
		//判断是否包含敏感词
		if (!isContaintSensitiveWord(txt, matchType)) {
			return txt;
		}

		status = jedisTemplate.getString(RedisKeyConstant.STRING_SENSITIVE_WORD_STATUS);
		status = jedisTemplate.serializerString(status).toString();
		if ("0".equals(jedisTemplate.serializerString(status))) {
			return txt;
		}
		String resultTxt = txt;
		Set<String> set = getSensitiveWord(txt, matchType);     //获取所有的敏感词
		log.info("语句中包含敏感词的个数为：" + set.size() + "。包含：" + set);
		Iterator<String> iterator = set.iterator();
		String word = null;
		String replaceString = null;
		while (iterator.hasNext()) {
			word = iterator.next();
			replaceString = getReplaceChars(replaceChar, word.length());
//			replaceString = getReplaceChars(replaceChar, word);
			resultTxt = resultTxt.replaceAll(word, replaceString);
		}
		
		return resultTxt;
	}
	
	/**
	 * 判断文字是否包含敏感字符
	 * @param txt  文字
	 * @param matchType  匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 * @return 若包含返回true，否则返回false
	 */
	private static boolean isContaintSensitiveWord(String txt,int matchType){
		boolean flag = false;
		for(int i = 0 ; i < txt.length() ; i++){
			int matchFlag = CheckSensitiveWord(txt, i, matchType); //判断是否包含敏感字符
			if(matchFlag > 0){    //大于0存在，返回true
				flag = true;
			}
		}
		return flag;
	}
	
	/**
	 * 获取文字中的敏感词
	 * @param txt 文字
	 * @param matchType 匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 */
	private static Set<String> getSensitiveWord(String txt , int matchType){
		Set<String> sensitiveWordList = new HashSet<String>();
		
		for(int i = 0 ; i < txt.length() ; i++){
			int length = CheckSensitiveWord(txt, i, matchType);    //判断是否包含敏感字符
			if(length > 0){    //存在,加入list中
				sensitiveWordList.add(txt.substring(i, i+length));
				i = i + length - 1;    //减1的原因，是因为for会自增
			}
		}
		
		return sensitiveWordList;
	}
	
	/**
	 * 获取替换字符串
	 * @param replaceChar
	 * @param length
	 */
	private static String getReplaceChars(String replaceChar,int length){
		String resultReplace = replaceChar;
		for(int i = 1 ; i < length ; i++){
			resultReplace += replaceChar;
		}
		
		return resultReplace;
	}
	
	/**
	 * 获取替换字符串
	 * @param replaceChar
	 * @param wordstr
	 */
	@SuppressWarnings("unused")
	private static String getReplaceChars(String replaceChar,String wordstr){
		String resultReplace = "";
		for(int i = 0 ; i < wordstr.length() ; i++){
			String word = wordstr.substring(i, i+1);
			if (" ".equals(word)) {
				resultReplace += " ";
			}else{
				resultReplace += replaceChar;
			}
			
		}
		
		return resultReplace;
	}
	
	/**
	 * 检查文字中是否包含敏感字符，检查规则如下：<br>
	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return，如果存在，则返回敏感词字符的长度，不存在返回0
	 */
	@SuppressWarnings({ "rawtypes"})
	private static int CheckSensitiveWord(String txt,int beginIndex,int matchType){

		
//		boolean  flag = false;    //敏感词结束标识位：用于敏感词只有1位的情况
		int matchFlag = 0;     //匹配标识数默认为0
//		char word = 0;
//		sensitiveWordMap = CacheKit.getData(RedisKeyConstant.STRING_SENSITIVE_WORD);
//		if (sensitiveWordMap == null) {
//			sensitiveWordMap = initKeyWord();
//			if (sensitiveWordMap == null || sensitiveWordMap.size()<=0) {
//				return matchFlag;
//			}
//			if(null != jedisTemplate.getString(RedisKeyConstant.STRING_SENSITIVE_WORD_CACHE_TIME)){
//				expire = Integer.valueOf(jedisTemplate.getString(RedisKeyConstant.STRING_SENSITIVE_WORD_CACHE_TIME));
//			} else {
//				jedisTemplate.setString(RedisKeyConstant.STRING_SENSITIVE_WORD_CACHE_TIME,expire+"");
//			}
//
//			CacheKit.setData(RedisKeyConstant.STRING_SENSITIVE_WORD, sensitiveWordMap, expire);
//		}
//		Map nowMap = sensitiveWordMap;
//		for(int i = beginIndex; i < txt.length() ; i++){
//			word = txt.charAt(i);
//			if (Character.isSpaceChar(word)) {
//				if (matchFlag==0) {
//					break;
//				}
//				matchFlag++;
//				continue;
//			}
//			nowMap = (Map) nowMap.get(word);     //获取指定key
//			if(nowMap != null){     //存在，则判断是否为最后一个
//				matchFlag++;     //找到相应key，匹配标识+1 
//				if("1".equals(nowMap.get("isEnd"))){       //如果为最后一个匹配规则,结束循环，返回匹配标识数
//					flag = true;       //结束标志位为true   
//					if(minMatchTYpe == matchType){    //最小规则，直接返回,最大规则还需继续查找
//						break;
//					}
//				}
//			}
//			else{     //不存在，直接返回
//				break;
//			}
//		}
//		if(matchFlag < 2 || !flag){        //长度必须大于等于1，为词 
//			matchFlag = 0;
//		}
		return matchFlag;
	}
	
	/**
	 */
	@SuppressWarnings("rawtypes")
	private static Map initKeyWord(){
		try {
			//读取敏感词库
			Set<String> keyWordSet = getSensitiveWord();
			//将敏感词库加入到HashMap中
			addSensitiveWordToHashMap(keyWordSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sensitiveWordMap;
	}

	/**
	 * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
	 * 中 = {
	 *      isEnd = 0
	 *      国 = {<br>
	 *      	 isEnd = 1
	 *           人 = {isEnd = 0
	 *                民 = {isEnd = 1}
	 *                }
	 *           男  = {
	 *           	   isEnd = 0
	 *           		人 = {
	 *           			 isEnd = 1
	 *           			}
	 *           	}
	 *           }
	 *      }
	 *  五 = {
	 *      isEnd = 0
	 *      星 = {
	 *      	isEnd = 0
	 *      	红 = {
	 *              isEnd = 0
	 *              旗 = {
	 *                   isEnd = 1
	 *                  }
	 *              }
	 *      	}
	 *      }
	 * @param keyWordSet  敏感词库
	 * @version 1.0
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void addSensitiveWordToHashMap(Set<String> keyWordSet) {
		sensitiveWordMap = new HashMap(keyWordSet.size());     //初始化敏感词容器，减少扩容操作
		String key = null;  
		Map nowMap = null;
		Map<String, String> newWorMap = null;
		//迭代keyWordSet
		Iterator<String> iterator = keyWordSet.iterator();
		while(iterator.hasNext()){
			key = iterator.next();    //关键字
			nowMap = sensitiveWordMap;
			for(int i = 0 ; i < key.length() ; i++){
				char keyChar = key.charAt(i);       //转换成char型
				Object wordMap = nowMap.get(keyChar);       //获取
				
				if(wordMap != null){        //如果存在该key，直接赋值
					nowMap = (Map) wordMap;
				}
				else{     //不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					newWorMap = new HashMap<String,String>();
					newWorMap.put("isEnd", "0");     //不是最后一个
					nowMap.put(keyChar, newWorMap);
					nowMap = newWorMap;
				}
				
				if(i == key.length() - 1){
					nowMap.put("isEnd", "1");    //最后一个
				}
			}
		}
	}

	
	/**
	 * 读取敏感词库中的内容，将内容添加到set集合中
	 * @return
	 * @throws Exception 
	 */
	private static Set<String> getSensitiveWord(){
		Set<String> set = new HashSet<String>();
		String sensitiveWord = jedisTemplate.getString(RedisKeyConstant.STRING_SENSITIVE_WORD);
		if (null != sensitiveWord) {
			sensitiveWord = jedisTemplate.serializerString(sensitiveWord).toString();
			log.info("获取最新的敏感词库:"+sensitiveWord);
			if (StrKit.notBlank(sensitiveWord)) {
				String[] str = sensitiveWord.split(",");
				Collections.addAll(set, str);
			}
		}else{
			jedisTemplate.setString(RedisKeyConstant.STRING_SENSITIVE_WORD,"");
		}

		return set;
	}

	public static void main(String[] args) throws Exception {
		/*JedisTemplate jedisTemplate = JedisTemplate.me();
		String str = jedisTemplate.getString(RedisKeyConstant.STRING_SENSITIVE_WORD_CACHE_TIME);
		str = jedisTemplate.serializerString(str).toString();
		System.out.println(str);*/
	}
}
