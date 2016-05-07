package com.tinet.ctilink.realtime.util;

import com.tinet.ctilink.realtime.entity.LogTts;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.SystemSetting;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.util.ContextUtil;
import com.tinet.ctilink.util.DateUtil;
import com.tinet.ctilink.util.MD5Encoder;
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

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TtsSendGetUtil {
	static String soundPath = "/var/nfs/tts_cache";
	static String soundPathNoac = "/var/nfs/tts_cache_noac";
	static String logPath = "/var/log/ccic/ttsc";
	static String noFile = "/var/lib/ivr_voice/no";

	public static void ttsLog(String logFile, String uniqueId, String msg){
		//TODO 改成存到kv里面
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
	
	public static String ttsSendGet(String uniqueId, String ttsText, boolean returnFile, LogTts logTts){
		logTts.setStartTime(new Date());
		logTts.setText(ttsText);
		logTts.setUniqueId(uniqueId);
		
		
		String day = DateUtil.format(new Date(), DateUtil.FMT_DATE_YYYY_MM_DD);
	    String logFile = logPath + "/" + "ttsc_send.log." + day;
		RedisService redisService = (RedisService) ContextUtil.getContext().getBean("redisService");
		SystemSetting systemSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME, Const.SYSTEM_SETTING_NAME_TTSSC_URL)
				, SystemSetting.class);
		//String TTSC_URL = "http://internal-vocp-ttssc-internal-8477452.cn-north-1.elb.amazonaws.com.cn/interface/ttsGet";
		String TTSC_URL = systemSetting.getValue() + "/interface/ttsGet";
		
		Integer timeout = 10;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		Long deadLine = new Date().getTime() + timeout * 1000;
		StringBuilder returnFileList = new StringBuilder();
		List<String> httpFailCurlList = new ArrayList<String>();
		List<String> httpFailAgainCurlList = new ArrayList<String>();
		List<String> needCheckList = new ArrayList<String>();
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
				String md5File = soundPath + "/" + md5.substring(0, 2) + "/" + md5 + ".wav";
				String md5FileNoac = soundPathNoac + "/" + md5.substring(0, 2) + "/" + md5 + ".wav";
				
				File file = new File(md5File);
				File fileNoac = new File(md5FileNoac);
				try{
					if(file.exists()){
						/* 暂时取消文件格式检查
						FileInputStream fin = new FileInputStream(md5File);
						byte[] wavHead = new byte[44];
						if(fin.read(wavHead, 0, 44) == 44){
							fin.close();
							byte[] waveFmt = new byte[8];
							for(int i=0;i<8;i++){
								waveFmt[i] = wavHead[8+i];
							}
							if(new String(waveFmt).trim().equals("WAVEfmt")){
								ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + md5File + " 存在,格式正确!");
								continue;
							}else{
								ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + md5File + " 存在,格式不正确!");
							}
						}
						fin.close();
				        file.delete();
				        */
						hitCacheCount++;
						returnFileList.append(soundPath + "/" + md5.substring(0, 2) + "/" + md5 + "&" + noFile + "&");
						ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + md5File + " 存在!");
						continue;
					}else if(fileNoac.exists()){
						if(returnFile){
							returnFileList.append(soundPathNoac + "/" + md5.substring(0, 2) + "/" + md5 + "&" + noFile + "&");
						}else{
							returnFileList.append(soundPathNoac + "/" + md5.substring(0, 2) + "/" + md5 + "&" + noFile + "&");
						}
						hitCacheCount++;
						ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + md5File + " 存在!");
						continue;
					}else{
						if(returnFile){
							returnFileList.append(soundPathNoac + "/" + md5.substring(0, 2) + "/" + md5 + "&" + noFile + "&");
						}else{
							returnFileList.append(soundPath + "/" + md5.substring(0, 2) + "/" + md5 + "&" + noFile + "&");
						}
						ttsLog(logFile, uniqueId, "分片:" + text + " md5:" + md5File + " 不存在");
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				needCheckList.add(md5FileNoac);
				//文件不存在
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
				nv = new BasicNameValuePair("vid", "1");
				list.add(nv);
				nv = new BasicNameValuePair("volume", "10");
				list.add(nv);
				nv = new BasicNameValuePair("redirect", "0");
				list.add(nv);
				nv = new BasicNameValuePair("sync", "0");
				list.add(nv);
				String curl = TTSC_URL + "?" + URLEncodedUtils.format(list, HTTP.UTF_8);
				
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
		        	if(statusCode.equals(200) && "success".equals(res)){
		        		ttsLog(logFile, uniqueId, "分片:" + text + " 请求url:" + curl + " 成功");
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
		        	if(statusCode.equals(200) && "success".equals(res)){
		        		ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 成功");
		        	}else{
		        		httpFailAgainCurlList.add(failCurl);
		        		ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 失败 statusCode:" + statusCode + " res:" + res);
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
		        	if(statusCode.equals(200) && "success".equals(res)){
		        		ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 成功");
		        	}else{
		        		ttsLog(logFile, uniqueId, "重试 请求url:" + failCurl + " 失败 statusCode:" + statusCode + " res:" + res);
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
			while(true){
				if(new Date().getTime() > deadLine){
					logTts.setResult(-2);
					ttsLog(logFile, uniqueId, "等待合成文件超时 ttsText=" + ttsText);
					logTts.setEndTime(new Date());
					return "";
				}
				boolean needWait = false;
				for(String md5File: needCheckList){
					File file = new File(md5File);
					try{
						if(!file.exists()){
							needWait = true;
							break;
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				if(needWait){
					try{
						Thread.sleep(500);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					break;
				}
			}
			ttsLog(logFile, uniqueId, "合成文件完成 ttsText=" + ttsText);
		}
		logTts.setResult(0);
		logTts.setEndTime(new Date());
		
		if(returnFileList.toString().length() > 0){
			return returnFileList.toString().substring(0, returnFileList.toString().length()-1);
		}
		return "";
	}
	
	public static void main(String[] argv){
		String file= "/var/nfs/1.wav&/var/nfs2.wav&";
		System.out.println(file.substring(0,file.length()-1));
	}
}
