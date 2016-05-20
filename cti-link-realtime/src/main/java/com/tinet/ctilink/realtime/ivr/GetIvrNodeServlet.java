package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.CtiLinkEnterpriseIvrAnchor;
import com.tinet.ctilink.conf.model.EnterpriseArea;
import com.tinet.ctilink.conf.model.EnterpriseIvr;
import com.tinet.ctilink.conf.model.EnterpriseTime;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONArray;
import com.tinet.ctilink.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;


/**
 * @Title:GetIvrNode.java
 * @Package:com.tinet.ccic.ivr
 * @author 罗尧 Email:j2ee.xiao@gmail.com
 * @version V1.0 2011-9-20 下午07:16:51
 * 
 *          访问方式：http://ip+端口/interface/ivr/GetIvrNode
 *          http://172.16.203.194/interface/ivr/GetIvrNode 这个类获得IVR的具体节点配置，如下：
 *          [path]_action ivr节点类型 [path]_next ivr下一节点 [path]_play_type
 *          播放节点类型1:语音文件 2:数字 3:数值 4:tts [path]_paly_pre_file
 *          播放前段文件[path]_play_type=2/3/4时有效 [path]_paly_file
 *          播放内容[path]_play_type=1时为语音文件名去除文件后缀，支持多个文件以逗号分隔
 *          [path]_play_type=2时为数字例如1234 [path]_play_type=3时为数字例如1234
 *          [path]_play_type=4时为字符串例如牛排套餐 [path]_play_post_file
 *          播放后段文件[path]_play_type=2/3/4时有效 [path]_play_times 播放次数
 *          [path]_select_file 选择文件 [path]_select_retries 重试次数
 *          [path]_select_[key]_next 按键下一跳 [path]_select_multi_key_type
 *          多余一个按键时的类型 0:不允许 1:座席号 2:队列号 3:分机号 4:crm_id [path]_select_error_file
 *          按键错误提示语音文件 [path]_queue ivr中队列名 [path]_queue_file 进队列前播放语音文件
 *          [path]_queue_empty_next 队列空溢出节点 [path]_queue_full_next 队列满溢出节点
 *          [path]_queue_timeout_next 队列超时溢出节点 [path]_queue_timeout 队列超时时间
 *          [path]_voicemail_file 留言前语音文件 [path]_voicemail_id 语音信箱id
 *          [path]_read_file 收号节点语音文件 [path]_read_variable
 *          变量名企业私有变量名都会在配置时转换为enterprise_id开头 [path]_read_retries 重试次数
 *          [path]_read_max_digits 最大收号数 [path]_read_timeout 超时时间秒
 *          [path]_curl_url curl接口地址 [path]_curl_param curl参数 [path]_curl_error
 *          curl错误溢出节点，包括超时 [path]_curl_tag curl节点标识 [path]_time_next 时间节点下一跳
 *          [path]_area_next 地区节点下一跳 [path]_branch_expression 分支节点表达式
 *          [path]_branch_true 分支节点true跳转节点 [path]_branch_false 分支节点false跳转节点
 *          [path]_atxfer_auto 咨询是否是前台送过来的指令 必须为YES时方是前台送来指令
 */
@SuppressWarnings("serial")
@Component
public class GetIvrNodeServlet extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private RedisService redisService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		logger.info(
				"***************************************进入GetIvrNode接口*******************************************************");
		// 取得通道里面的企业ID
		String ccEnterpriseId = request.getParameter("enterpriseId");
		// System.out.println("企业id为---："+ccEnterpriseId);

		// 取得通道里面的IVRID
		String ccIvrId = request.getParameter("ivrId");
		// System.out.println("ivrId为---："+ccIvrId);

		// 获取地区区号
		String areaCode = request.getParameter("customerAreaCode");

		JSONObject jsonObject = getIvrNodeJSON(Integer.parseInt(ccEnterpriseId), Integer.parseInt(ccIvrId), areaCode);

		getIvrAnchorJSON(Integer.parseInt(ccEnterpriseId), Integer.parseInt(ccIvrId), jsonObject);
		String ivrJson = jsonObject.toString();
		out.append(ivrJson);
		out.flush();
		out.close();
	}


	public JSONObject getIvrNodeJSON(Integer enterpriseId, Integer ivrId, String areaCode){
		JSONObject jsonObject = new JSONObject();
		/** 取得通道传过来的企业id和ivrid去查询相关的IVR节点配置 */
		List<EnterpriseIvr> ivrList = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_IVR_ENTERPRISE_ID_IVR_ID, enterpriseId, ivrId)
				, EnterpriseIvr.class);
		if (ivrList.size() > 0) {
			logger.info("数据库查询OK");
			for (EnterpriseIvr enterpriseIvr : ivrList) {
				/* 通配格式不行，按照节点类型进行解析 */
				JSONObject jsonObj = JSONObject.fromObject(enterpriseIvr.getProperty());
				jsonObject.put(enterpriseIvr.getPath() + "_action", enterpriseIvr.getAction());
				Set<String> propertyKeySet = jsonObj.keySet();
				switch (enterpriseIvr.getAction()) {
					case 1:/******************************************* 播放节点 *********************************************/
						JSONArray jsonPlayVoiceArray = jsonObj.getJSONArray("play_voice");
						jsonObject.put(enterpriseIvr.getPath() + "_play_voice_count", jsonPlayVoiceArray.size());
						for(int i = 0; i < jsonPlayVoiceArray.size(); i++){
							JSONObject jsonTmp = jsonPlayVoiceArray.getJSONObject(i);
							String type = jsonTmp.get("type").toString();
							String file = jsonTmp.get("file").toString();

							jsonObject.put(enterpriseIvr.getPath() + "_play_voice_" +(i+1)+"_type", type);
							jsonObject.put(enterpriseIvr.getPath() + "_play_voice_" +(i+1)+"_file", file);

						}
						jsonObject.put(enterpriseIvr.getPath() + "_play_retries", jsonObj.getString("play_retries")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_next", jsonObj.getString("next")); // 下一节点

						break;
					case 2:/******************************************* 选择节点 *********************************************/
						JSONArray jsonSelectVoiceArray = jsonObj.getJSONArray("select_voice");
						jsonObject.put(enterpriseIvr.getPath() + "_select_voice_count", jsonSelectVoiceArray.size());
						for(int i = 0; i < jsonSelectVoiceArray.size(); i++){
							JSONObject jsonTmp = jsonSelectVoiceArray.getJSONObject(i);
							String type = jsonTmp.get("type").toString();
							String file = jsonTmp.get("file").toString();

							jsonObject.put(enterpriseIvr.getPath() + "_select_voice_" +(i+1)+"_type", type);
							jsonObject.put(enterpriseIvr.getPath() + "_select_voice_" +(i+1)+"_file", file);

						}
						jsonObject.put(enterpriseIvr.getPath() + "_select_retries", jsonObj.getString("select_retries")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_select_multi_key_type", jsonObj.getString("select_multi_key_type")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_select_error_file", jsonObj.getString("select_error_file")); // 下一节点
						JSONArray jsonSelectArray = jsonObj.getJSONArray("select");
						for(int i = 0; i < jsonSelectArray.size(); i++){
							JSONObject jsonTmp = jsonSelectArray.getJSONObject(i);
							String key = jsonTmp.get("key").toString();
							String next = jsonTmp.get("next").toString();
							jsonObject.put(enterpriseIvr.getPath() + "_select_" +key+"_next", next);
						}
						break;
					case 3:/******************************************* 留言节点 *********************************************/
						for(String key : propertyKeySet) {
							String value = jsonObj.getString(key);
							jsonObject.put(enterpriseIvr.getPath() + "_" + key, value);
						}
						break;
					case 4:/******************************************* 队列节点 *********************************************/
						for(String key : propertyKeySet) {
							String value = jsonObj.getString(key);
							jsonObject.put(enterpriseIvr.getPath() + "_" + key, value);
						}
						break;
					case 5:/******************************************* 时间节点 *********************************************/
						/*
						 * 获取当前时间------ 获取日期类型 获取时间 获取星期
						 */
						Date nowDate = new Date();
						Calendar cc = Calendar.getInstance();
						DateFormat dateFormat = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm");
						String newNowDate = dateFormat.format(nowDate); // 格式化时间，转换为String
						logger.info("当前完整日期时间=======" + newNowDate);
						// 取小时和分钟
						String overNowTime = newNowDate.substring(newNowDate.length() - 5, newNowDate.length()); // 处理截取字符串
						logger.info("当前时间======" + overNowTime);
						// 取年月日
						String overNowDate = newNowDate.substring(0, 11);
						logger.info("当前日期======" + overNowDate);
						String week = null;
						switch (cc.get(Calendar.DAY_OF_WEEK)) {
							case 1:
								week = "1";
								break;
							case 2:
								week = "2";
								break;
							case 3:
								week = "3";
								break;
							case 4:
								week = "4";
								break;
							case 5:
								week = "5";
								break;
							case 6:
								week = "6";
								break;
							case 7:
								week = "7";
								break;
						}

						JSONArray jsonArray = jsonObj.getJSONArray("time");
						boolean isMatch =false;
						for(int i = 0; i < jsonArray.size(); i++){
							JSONObject jsonTmp = jsonArray.getJSONObject(i);
							Integer timeId = Integer.parseInt(jsonTmp.get("id").toString());
							String next = jsonTmp.get("next").toString();
							EnterpriseTime eTime = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_TIME_ENTERPRISE_ID_ID, enterpriseId, timeId)
									, EnterpriseTime.class);
							boolean accordingWeek;
							boolean accordingSpecialDate;
							// int priority=eTime.getPriority(); //优先级
							if (eTime.getType() == 1) { // 按照星期
								int idOne = eTime.getId();
								String dayOfWeekOne = eTime.getDayOfWeek(); // 配置的星期
								String numDayOfWeekOne[] = dayOfWeekOne.split(","); // 取得星期对应的数字
								String startOne = eTime.getStartTime(); // 开始时间
								String endTimeOne = eTime.getEndTime(); // 结束时间
								// int priority=eTime.getPriority(); //优先级
								accordingWeek = (isStringHave(numDayOfWeekOne, week)) && (overNowTime.compareTo(startOne) >= 0) && (overNowTime.compareTo(endTimeOne) < 0);
								if (accordingWeek) {
									jsonObject.put(enterpriseIvr.getPath() + "_next", next);
									isMatch = true;
									break;
								}
							}
							if (eTime.getType() == 2) { // 按照特殊日期
								int idTwo = eTime.getId();
								accordingSpecialDate = (newNowDate.compareTo(eTime.getFromDay() + " " + eTime.getStartTime()) >= 0) && (newNowDate.compareTo(eTime.getToDay() + " " + eTime.getEndTime()) < 0);

								if (accordingSpecialDate) {
									jsonObject.put(enterpriseIvr.getPath() + "_next", next);
									isMatch = true;
									break;
								}
							}
						}
						if(!isMatch)
						{
							jsonObject.put(enterpriseIvr.getPath() + "_next", jsonObj.get("next"));
						}
						break;
					case 6:/******************************************* 地区节点 *********************************************/
						JSONArray jsonAreaArray = jsonObj.getJSONArray("area");
						isMatch =false;
						for(int i=0; i<jsonAreaArray.size(); i++){
							JSONObject jsonTmp = jsonAreaArray.getJSONObject(i);
							Integer areaGroupId = Integer.parseInt(jsonTmp.get("id").toString());
							String next = jsonTmp.get("next").toString();
							EnterpriseArea area = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_AREA_ENTERPRISE_ID_GROUP_ID_AREA_CODE, enterpriseId, areaGroupId, areaCode)
									, EnterpriseArea.class);
							if(area!=null){
								jsonObject.put(enterpriseIvr.getPath() + "_next", next);
								isMatch = true;
							}
						}
						if (!isMatch){
							jsonObject.put(enterpriseIvr.getPath() + "_next", jsonObj.get("next"));
						}
						break;
					case 7:/******************************************* 传真节点 *********************************************/
						for (String key : propertyKeySet) {
							String value = jsonObj.getString(key);
							jsonObject.put(enterpriseIvr.getPath() + "_" + key, value);
						}
						break;
					case 8:/****************************************** 设置变量节点 ******************************************/
						JSONArray jsonSetArray = jsonObj.getJSONArray("set");
						jsonObject.put(enterpriseIvr.getPath() + "_set_count", jsonSetArray.size());
						BASE64Decoder base64Decoder = new BASE64Decoder();
						for(int i=0; i<jsonSetArray.size(); i++){
							JSONObject jsonTmp = jsonSetArray.getJSONObject(i);
							String name = jsonTmp.get("name").toString();
							String value = jsonTmp.get("value").toString();
							String nameType = jsonTmp.get("name_type").toString();
							String valueType = jsonTmp.get("value_type").toString();
							try {
								jsonObject.put(enterpriseIvr.getPath() + "_set_"+(i+1) +"_name_type", nameType);
								jsonObject.put(enterpriseIvr.getPath() + "_set_"+(i+1) +"_name", new String(base64Decoder.decodeBuffer(name)));
								jsonObject.put(enterpriseIvr.getPath() + "_set_"+(i+1) +"_value_type", valueType);
								jsonObject.put(enterpriseIvr.getPath() + "_set_"+(i+1)+"_value", new String(base64Decoder.decodeBuffer(value)));
							} catch (IOException e) {
								logger.error("GetIvrNodeAgiScript.getIvrNodeJSON action=8, error", e);
							}
						}
						jsonObject.put(enterpriseIvr.getPath() + "_set_next", jsonObj.get("next")); // 下一节点
						break;
					case 9:/******************************************* 收号节点 *********************************************/
						JSONArray jsonReadArray = jsonObj.getJSONArray("read_voice");
						jsonObject.put(enterpriseIvr.getPath() + "_read_voice_count", jsonReadArray.size());
						for(int i=0; i<jsonReadArray.size(); i++){
							JSONObject jsonTmp = jsonReadArray.getJSONObject(i);
							String type = jsonTmp.get("type").toString();
							String file = jsonTmp.get("file").toString();

							jsonObject.put(enterpriseIvr.getPath() + "_read_voice_" +(i+1)+"_type", type);
							jsonObject.put(enterpriseIvr.getPath() + "_read_voice_" +(i+1)+"_file", file);

						}
						jsonObject.put(enterpriseIvr.getPath() + "_read_variable", jsonObj.get("read_variable")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_read_retries", jsonObj.get("read_retries")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_read_max_digits", jsonObj.get("read_max_digits")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_read_timeout", jsonObj.get("read_timeout")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_next", jsonObj.get("next")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_read_hidden", jsonObj.get("read_hidden")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_read_delay", jsonObj.get("read_delay")); // 下一节点
						break;
					case 10:/********************************************* 等待节点 *******************************************/
						for (String key : propertyKeySet) {
							String value = jsonObj.getString(key);
							jsonObject.put(enterpriseIvr.getPath() + "_" + key, value);
						}
						break;
					case 11:/****************************************** 分支节点branch节点 ************************************/
						JSONArray jsonBranchArray = jsonObj.getJSONArray("branch_expression");
						String exp = "";
						String r = "&&";
						for(int i=0; i<jsonBranchArray.size(); i++){
							JSONObject jsonTmp = jsonBranchArray.getJSONObject(i);
							String name = jsonTmp.get("name").toString();
							String operator = jsonTmp.get("operator").toString();
							String value = jsonTmp.get("value").toString();
							String relation = jsonTmp.get("relation").toString();

							if (relation.equals("1")) {
								r = "&&";
							} else {
								r = "||";
							}
							//正则  "${REGEX(";",${tel})}"
							if (i != jsonBranchArray.size() - 1) {
								if (operator.equals("regex")) {
									exp += "("+"${REGEX(\"" + value + "\" ${" + name + "})})" + r;
								} else {
									exp += "("+"${" + name + "}" + operator + value + ")" + r;
								}
							} else {
								if (operator.equals("regex")) {
									exp += "("+"${REGEX(\"" + value + "\" ${" + name + "})})";
								} else {
									exp += "("+"${" + name + "}" + operator + value + ")";
								}
							}
						}
						jsonObject.put(enterpriseIvr.getPath() + "_branch_expression", exp);
						jsonObject.put(enterpriseIvr.getPath() + "_branch_true_next", jsonObj.get("branch_true_next"));
						jsonObject.put(enterpriseIvr.getPath() + "_branch_false_next", jsonObj.get("branch_false_next"));
						break;
					case 12:/******************************************* curl节点 ********************************************/
						for (String key : propertyKeySet) {
							String value = jsonObj.getString(key);
							jsonObject.put(enterpriseIvr.getPath() + "_" + key, value);//所有语音个数
						}
						break;
					case 13:/****************************************** 直呼号码节点 *****************************************/
						JSONArray jsonDialArray = jsonObj.getJSONArray("dial_voice");
						jsonObject.put(enterpriseIvr.getPath() + "_dial_voice_count", jsonDialArray.size());
						for(int i=0; i<jsonDialArray.size(); i++){
							JSONObject jsonTmp = jsonDialArray.getJSONObject(i);
							String type = jsonTmp.get("type").toString();
							String file = jsonTmp.get("file").toString();

							jsonObject.put(enterpriseIvr.getPath() + "_dial_voice_" +(i+1)+"_type", type);
							jsonObject.put(enterpriseIvr.getPath() + "_dial_voice_" +(i+1)+"_file", file);

						}

						jsonObject.put(enterpriseIvr.getPath() + "_dial_type", jsonObj.get("dial_type")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_tel", jsonObj.get("dial_tel")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_fail_next", jsonObj.get("dial_fail_next")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_timeout", jsonObj.get("dial_timeout")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_callee_voice_type", jsonObj.get("dial_callee_voice_type")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_callee_voice_file", jsonObj.get("dial_callee_voice_file")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_moh", jsonObj.get("dial_moh")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_max_talk_time", jsonObj.get("dial_max_talk_time")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_pre_remind_time", jsonObj.get("dial_pre_remind_time")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_remind_voice_type", jsonObj.get("dial_remind_voice_type")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_remind_voice_file", jsonObj.get("dial_remind_voice_file")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_call_sequence", jsonObj.get("dial_call_sequence")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_caller_hangup_next", jsonObj.get("dial_caller_hangup_next")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_dial_callee_hangup_next", jsonObj.get("dial_callee_hangup_next")); // 下一节点
						break;
					case 14:/****************************************** 连接数据库节点 ************************************/
						jsonObject.put(enterpriseIvr.getPath() + "_db_sql", jsonObj.get("db_sql"));
						jsonObject.put(enterpriseIvr.getPath() + "_db_next", jsonObj.get("db_next"));
						jsonObject.put(enterpriseIvr.getPath() + "_db_enterprise_ivr_id", enterpriseIvr.getId());

						break;
					case 15:/****************************************** 会议节点 *****************************************/
						for (String key : propertyKeySet) {
							String value = jsonObj.getString(key);
							jsonObject.put(enterpriseIvr.getPath() + "_" + key, value);//所有语音个数
						}
						break;
					case 16:/****************************************** Switch节点 *****************************************/
						JSONArray jsonSwitchArray = jsonObj.getJSONArray("value");
						jsonObject.put(enterpriseIvr.getPath() + "_switch_value_count", jsonSwitchArray.size());
						for(int i=0; i<jsonSwitchArray.size(); i++){
							JSONObject jsonTmp = jsonSwitchArray.getJSONObject(i);
							String value = jsonTmp.get("value").toString();
							String next = jsonTmp.get("next").toString();

							jsonObject.put(enterpriseIvr.getPath() + "_switch_" +(i+1)+"_value", value);
							jsonObject.put(enterpriseIvr.getPath() + "_switch_" +(i+1)+"_next", next);

						}
						jsonObject.put(enterpriseIvr.getPath() + "_switch_name", jsonObj.get("switch_name")); // 下一节点
						jsonObject.put(enterpriseIvr.getPath() + "_switch_other_next", jsonObj.get("switch_other_next")); // 下一节点
						break;
				}
			}
		} else {
			logger.debug("通过通道传过来的企业id和ivrid去查询相关的IVR节点配置 失败，请查询表：EnterpriseIvr");
		}
		return jsonObject;
	}

	private void getIvrAnchorJSON(int enterpriseId, int ivrId, JSONObject jsonObject) {
		List<CtiLinkEnterpriseIvrAnchor> enterpriseIvrAnchorList = redisService.getList(Const.REDIS_DB_CONF_INDEX
				, String.format(CacheKey.ENTERPRISE_IVR_ANCHOR_ENTERPRISE_ID_IVR_ID, enterpriseId, ivrId), CtiLinkEnterpriseIvrAnchor.class);
		if (enterpriseIvrAnchorList != null && !enterpriseIvrAnchorList.isEmpty()) {
			int i = 0;
			String path = null;
			for (CtiLinkEnterpriseIvrAnchor enterpriseIvrAnchor : enterpriseIvrAnchorList) {
				if (path == null) {
					path = enterpriseIvrAnchor.getPath();
				}

				if (path.equals(enterpriseIvrAnchor.getPath())) {
					i++;
					jsonObject.put(enterpriseIvrAnchor.getPath() + "_anchor_" + i + "_event", enterpriseIvrAnchor.getEvent());
					jsonObject.put(enterpriseIvrAnchor.getPath() + "_anchor_" + i + "_data", enterpriseIvrAnchor.getData());
				} else {
					jsonObject.put(path + "_anchor_count", i);

					i = 1;
					path = enterpriseIvrAnchor.getPath();
					jsonObject.put(enterpriseIvrAnchor.getPath() + "_anchor_" + i + "_event", enterpriseIvrAnchor.getEvent());
					jsonObject.put(enterpriseIvrAnchor.getPath() + "_anchor_" + i + "_data", enterpriseIvrAnchor.getData());
				}
			}
			jsonObject.put(path + "_anchor_count", i);
		}
	}

	/**查找数组的一个方法*/
	private boolean isStringHave(String[] strs, String s){
		  /*此方法有两个参数，第一个是要查找的字符串数组，第二个是要查找的字符或字符串* */
		for(String str : strs){
			if(str.contains(s)){//循环查找字符串数组中的每个字符串中是否包含所有查找的内容
				return true;//查找到了就返回真，不在继续查询
			}
		}
		return false;//没找到返回false
	}
}
