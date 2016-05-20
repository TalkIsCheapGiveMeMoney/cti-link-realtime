package com.tinet.ctilink.realtime.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.tinet.ctilink.inc.SystemSettingConst;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseHangupSet;
import com.tinet.ctilink.conf.model.SystemSetting;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.realtime.entity.LogTts;
import com.tinet.ctilink.util.ContextUtil;
import com.tinet.ctilink.util.DateUtil;
import com.tinet.ctilink.util.MD5Encoder;
import com.tinet.ctilink.util.SystemCmd;


public class TtsSendGetUtil {

	static String logPath = "/var/log/cti-link/ttsc";
	
	static String ttsGetURL = "/interface/v2/ttsGet";
	static String ttsWaitURL = "/interface/v2/ttsWait";

	public static void init(){
		SystemCmd.executeCmd("mkdir -p " + logPath);
	}
	public static void ttsLog(String logFile, String uniqueId, String msg){
		File logFileFile=new File(logFile);
		
		try{
			if(logFileFile.exists()){
			}else{
				logFileFile.createNewFile();
			}
			FileWriter fileWriter=new FileWriter(logFileFile, true);
			fileWriter.write(DateUtil.format(new Date(), DateUtil.FMT_DATE_YYYYMMDDHHmmss) + " " + uniqueId + " " + msg +"\r\n");
			fileWriter.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String ttsSendGet(String uniqueId, String ttsText,String ttslanguagetype, boolean returnFile, LogTts logTts,StringBuffer ttsurllists){
		logTts.setStartTime(new Date());
		logTts.setText(ttsText);
		logTts.setUniqueId(uniqueId);
		
		
		String day = DateUtil.format(new Date(), DateUtil.FMT_DATE_YYYY_MM_DD);
	    String logFile = logPath + "/" + "ttsc_send.log." + day;
	    RedisService redisService = ContextUtil.getBean(RedisService.class);
	    
		SystemSetting ttsProxyUrlSystemSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME,
				SystemSettingConst.SYSTEM_SETTING_NAME_TTSS_PROXY_URL), SystemSetting.class);

		String TTSC_GET_URL = ttsProxyUrlSystemSetting.getValue() + ttsGetURL;
		
		String vidtype = ttslanguagetype;
		
		Integer timeout = 10;
		CloseableHttpClient httpclient = HttpClients.createDefault();		

		Long deadLine = new Date().getTime() + timeout * 1000;
		StringBuilder returnFileList = new StringBuilder();
		List<String> httpFailCurlList = new ArrayList<String>();
		List<String> httpFailAgainCurlList = new ArrayList<String>();
		List<String> needCheckList = new ArrayList<String>();
		List<String> ttsUrlPathList = new ArrayList<String>();
		if(StringUtils.isNotEmpty(ttsText)){
			ttsLog(logFile, uniqueId, "开始转换ttsText:" + ttsText);
			ttsText = ttsText.replace("\"", "").replace(","," ").replace("."," ").replace(";"," ").replace(":"," ").replace("!"," ").replace("?"," ")
					.replace("，"," ").replace("。"," ").replace("；"," ").replace("："," ").replace("！"," ").replace("？"," ").trim();
			String[] textArray = ttsText.split(" ");
			Integer totalCount = textArray.length;
			Integer hitCacheCount = 0;
			for(String text: textArray){
				text = text.trim();
				if(text.equals("")){//过滤掉空
					continue;
				}
				String md5 = MD5Encoder.encode(text);
				String md5key = vidtype+"-" + md5;
				
				returnFileList.append(vidtype+"/" + md5.substring(0, 2) + "/" + md5 + ";");
				

				//发送ttsget请求，检查响应消息是否成功
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				BasicNameValuePair nv = null;
				nv = new BasicNameValuePair("text", text);
				list.add(nv);
				nv = new BasicNameValuePair("priority", "6");
				list.add(nv);
				nv = new BasicNameValuePair("uniqueId", uniqueId);
				list.add(nv);
				nv = new BasicNameValuePair("timeout", String.valueOf(timeout));
				list.add(nv);
				nv = new BasicNameValuePair("vid", vidtype);
				list.add(nv);
				nv = new BasicNameValuePair("volume", "10");
				list.add(nv);
				nv = new BasicNameValuePair("redirect", "0");
				list.add(nv);
				nv = new BasicNameValuePair("sync", "0");
				list.add(nv);
				String curl = TTSC_GET_URL + "?" + URLEncodedUtils.format(list, HTTP.UTF_8);
				
				HttpResponse response = null;
				HttpGet httpGet = new HttpGet(curl);
				int socketTimeout = timeout * 1000;
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(2000).build();
				httpGet.setConfig(requestConfig);
				try{
					response = httpclient.execute(httpGet);
					 //解析返回
					HttpEntity entity = response.getEntity();
		        	Integer statusCode = response.getStatusLine().getStatusCode();
		        	String res = EntityUtils.toString(entity, "UTF-8");  
		        	ObjectMapper mapper = new ObjectMapper();
		    		HashMap<String, String> map = null;
		    		try {
		    			map= mapper.readValue(res, HashMap.class);			
		    		} catch (JsonParseException e) {
		    			e.printStackTrace();
		    		} catch (JsonMappingException e) {
		    			e.printStackTrace();
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		}
		        	if(statusCode.equals(200) && "0".equals(map.get("result"))){
		        		ttsLog(logFile, uniqueId, "分片:" + text + " 请求url:" + curl + " 成功");
		        		String ttsUrlPath = map.get("url");
	        			if(StringUtils.isNotEmpty(ttsUrlPath))
	        				ttsUrlPathList.add(ttsUrlPath);
		        		
		        		if("0".equals(map.get("hitCache")))
		        		{
		        			String needchecking = map.get("key");
		        			if(StringUtils.isNotEmpty(needchecking))
		        				needCheckList.add(needchecking);
		        			ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + needchecking + " 不存在");		        			
		        		}
		        		else if("1".equals(map.get("hitCache")))
		        		{
		        			hitCacheCount++;
				            ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + md5 + " 存在!");				            
							continue;
		        		}
		        		
		        	}else{
		        		httpFailCurlList.add(curl);
		        		ttsLog(logFile, uniqueId, "分片:" + text + " 请求url:" + curl + " 失败 statusCode:" + statusCode + " res:" + res);
		        	}
				}catch(Exception e){
					e.printStackTrace();
				}
				if(new Date().getTime() > deadLine){
					logTts.setResult(-1);
					ttsLog(logFile, uniqueId, "分隔过程中超时 ttsText=" + ttsText);
					logTts.setEndTime(new Date());
					return "";
				}
			}
			logTts.setHitCache(hitCacheCount*100/totalCount);
			//重试一次，防止ttssc宕机不能合成
			for(String failCurl :httpFailCurlList){
				HttpResponse response = null;
				HttpGet httpGet = new HttpGet(failCurl);
				int socketTimeout = timeout * 1000;
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(2000).build();
				httpGet.setConfig(requestConfig);
				try{
					response = httpclient.execute(httpGet);
					 //解析返回
					HttpEntity entity = response.getEntity();
		        	Integer statusCode = response.getStatusLine().getStatusCode();
		        	String res = EntityUtils.toString(entity, "UTF-8"); 
	        	
		        	ObjectMapper mapper = new ObjectMapper();
		    		
		    		HashMap<String, String> map = null;
		    		try {
		    			map= mapper.readValue(res, HashMap.class);			
		    		} catch (JsonParseException e) {
		    			e.printStackTrace();
		    		} catch (JsonMappingException e) {
		    			e.printStackTrace();
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		}
		    		
		    		if(statusCode.equals(200) && "0".equals(map.get("result"))){
		    			ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 成功");
		        		String ttsUrlPath = map.get("url");
	        			if(StringUtils.isNotEmpty(ttsUrlPath))
	        				ttsUrlPathList.add(ttsUrlPath);
		        		
		        		if("0".equals(map.get("hitCache")))
		        		{
		        			String needchecking = map.get("key");
		        			if(StringUtils.isNotEmpty(needchecking))
		        				needCheckList.add(needchecking);
		        				        			
		        		}
		        		else if("1".equals(map.get("hitCache")))
		        		{
		        			hitCacheCount++;				           			            
							continue;
		        		}
		        		
		        	}else{
		        		httpFailAgainCurlList.add(failCurl);
		        		ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 失败 statusCode:" + statusCode + " res:" + map.get("result"));
		        	}
				}catch(Exception e){
					e.printStackTrace();
				}
				if(new Date().getTime() > deadLine){
					logTts.setResult(-1);
					ttsLog(logFile, uniqueId, "重试http过程中超时 ttsText=" + ttsText);
					logTts.setEndTime(new Date());
					return "";
				}
			}
			//再重试一次，一共试3次
			for(String failCurl :httpFailAgainCurlList){
				HttpResponse response = null;
				HttpGet httpGet = new HttpGet(failCurl);
				int socketTimeout = timeout * 1000;
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(2000).build();
				httpGet.setConfig(requestConfig);
				try{
					response = httpclient.execute(httpGet);
					 //解析返回
					HttpEntity entity = response.getEntity();
		        	Integer statusCode = response.getStatusLine().getStatusCode();
		        	String res = EntityUtils.toString(entity, "UTF-8"); 
		        	ObjectMapper mapper = new ObjectMapper();
		    		
		    		HashMap<String, String> map = null;
		    		try {
		    			map= mapper.readValue(res, HashMap.class);			
		    		} catch (JsonParseException e) {
		    			e.printStackTrace();
		    		} catch (JsonMappingException e) {
		    			e.printStackTrace();
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		}
		    		
		    		if(statusCode.equals(200) && "0".equals(map.get("result"))){
		    			ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 成功");
		        		String ttsUrlPath = map.get("url");
	        			if(StringUtils.isNotEmpty(ttsUrlPath))
	        				ttsUrlPathList.add(ttsUrlPath);
		        		
		        		if("0".equals(map.get("hitCache")))
		        		{
		        			String needchecking = map.get("key");
		        			if(StringUtils.isNotEmpty(needchecking))
		        				needCheckList.add(needchecking);
		        				        			
		        		}
		        		else if("1".equals(map.get("hitCache")))
		        		{
		        			hitCacheCount++;				           			            
							continue;
		        		}
		        		
		        	}else{		        		
		        		httpFailAgainCurlList.add(failCurl);
		        		ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 失败 statusCode:" + statusCode + " res:" + map.get("result"));
		        	}
				}catch(Exception e){
					e.printStackTrace();
				}
				if(new Date().getTime() > deadLine){
					logTts.setResult(-1);
					ttsLog(logFile, uniqueId, "重试http过程中超时 ttsText=" + ttsText);
					logTts.setEndTime(new Date());
					return "";
				}
			}

				
			String needCheckingKey = StringUtils.join(needCheckList,",");
			ttsLog(logFile, uniqueId, "需要检查是否转化成功的keys=" + needCheckingKey);
			if(StringUtils.isNotEmpty(needCheckingKey))
			{
				
				String TTSC_WAIT_URL = ttsProxyUrlSystemSetting.getValue() + ttsWaitURL;
				HttpResponse response = null;
				
				//发送ttsget请求，检查响应消息是否成功
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				BasicNameValuePair nv = null;
				nv = new BasicNameValuePair("keys", needCheckingKey);					
				list.add(nv);
				String curl = TTSC_WAIT_URL + "?" + URLEncodedUtils.format(list, HTTP.UTF_8);
				ttsLog(logFile, uniqueId, "TTSC_WAIT_URL=" + curl);
				HttpGet httpGet = new HttpGet(curl);
				int socketTimeout = timeout * 1000;
				RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(2000).build();
				httpGet.setConfig(requestConfig);
				try{
					response = httpclient.execute(httpGet);
					 //解析返回
					HttpEntity entity = response.getEntity();
		        	Integer statusCode = response.getStatusLine().getStatusCode();
		        	String res = EntityUtils.toString(entity, "UTF-8"); 
		        	ObjectMapper mapper = new ObjectMapper();
		    		
		    		HashMap<String, String> map = null;
		    		try {
		    			map= mapper.readValue(res, HashMap.class);			
		    		} catch (JsonParseException e) {
		    			e.printStackTrace();
		    		} catch (JsonMappingException e) {
		    			e.printStackTrace();
		    		} catch (IOException e) {
		    			e.printStackTrace();
		    		}
		        	if(statusCode.equals(200) && "0".equals(map.get("result"))){
		        		ttsLog(logFile, uniqueId, "keys"+ needCheckingKey +" 请求url:" + curl + " 成功");
		        	}
		        	else
		        	{
		        		logTts.setResult(-2);
		        		ttsLog(logFile, uniqueId, "keys"+ needCheckingKey +" 请求url:" + curl + " 等待合成文件超时");

						logTts.setEndTime(new Date());
						return "";
		        	}
		        	
				}catch(Exception e){
					e.printStackTrace();
					ttsLog(logFile, uniqueId, "等待合成文件超时 ttsText=" + ttsText);
					return "";
				}
				
			}
			ttsLog(logFile, uniqueId, "合成文件完成 ttsText=" + ttsText);
		}
		logTts.setResult(0);
		logTts.setEndTime(new Date());
		
		if(returnFileList.toString().length() > 0){
			
			String ttsurllist = StringUtils.join(ttsUrlPathList,";");
			if(StringUtils.isNotEmpty(ttsurllist))
				ttsurllists.append(ttsurllist);
			return returnFileList.toString().substring(0, returnFileList.toString().length()-1);
		}
		return "";
	}
	
	public static void main(String[] argv){
		String file= "/var/nfs/1.wav&/var/nfs2.wav&";
		System.out.println(file.substring(0,file.length()-1));
	}
}
