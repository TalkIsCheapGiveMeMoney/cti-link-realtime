package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseHotline;
import com.tinet.ctilink.conf.model.EnterpriseIvr;
import com.tinet.ctilink.conf.model.EnterpriseIvrRouter;
import com.tinet.ctilink.conf.model.EnterpriseSetting;
import com.tinet.ctilink.conf.model.EnterpriseTime;
import com.tinet.ctilink.conf.model.Entity;
import com.tinet.ctilink.conf.model.Trunk;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.realtime.entity.Caller;
import com.tinet.ctilink.realtime.util.AreaCodeUtil;


/**
 * @Title GetIVR.java
 * @Package com.tinet.ccic.interfaces
 * @author 罗尧 Email:j2ee.xiao@gmail.com
 * @version V1.0 2011-9-14 下午01:33:52
 * 
 *          访问方式：http://ip+端口/interface/ivr/GetIvrOption
 *          http://172.16.203.194/interface/ivr/GetIvrOption
 *          这个类主要获取一些IVR功能信息，如下： 自定义channel变量定义：
 * 
 *          CDR(main_unique_id) 主通道id CDR(customer_number) 来电或外呼客户号码格式规整过
 *          CDR(customer_area_code) 呼入或外呼座席接听后的座席区号 CDR(customer_number_type)
 *          来电或外呼客户号码类型 手机/固话 CDR(client_number) 呼入或外呼座席接听后的座席 CDR(cno)
 *          呼入或外呼座席接听后的座席号 CDR(callee_number) 呼入或外呼座席号码或咨询转移监听等号码
 *          CDR(start_time) 进入系统时间 CDR(answer_time) 系统接听时间 CDR(join_queue_time)
 *          进队列时间 CDR(bridge_time) 桥接时间，留言时用作留言开始时间 CDR(end_time) 挂机时间
 *          CDR(ivr_id) ivr id CDR(ivr_flow) ivr流程 CDR(queue_name) 队列名称
 *          CDR(record_file) 录音文件 CDR(call_type) 呼叫类型 CDR(status) 通话状态 CDR(mark)
 *          标识 CDR(end_reason) 挂机原因 CDR(gateway) 网关地址 CDR(trunk_number） 目的码
 *          CDR(enterprise_id) 通话记录企业id
 * 
 * 
 *          valid_ivr ivr是否有效 __enterprise_id 企业id enterprise_status 企业目前业务状态
 *          __main_channel 当前呼入主channel名 __main_unique_id 当前主unique_id
 *          __customer_number 规整过的来电号码 __customer_number_type 来电类型 1固话 2手机
 *          __customer_area_code 来电区号 webcall 是否是网上400呼叫 __predictive_outcall
 *          是否是预测外呼 call_type 呼叫类型 webcall_tel 网上呼叫的客户号码 webcall_subtel
 *          网上呼叫客户的分机号码 ivr_id ivr_id call_status 呼叫状态
 * 
 *          is_restrict_check 是否需要设置了黑白名单 is_restrict_tel 是否在黑名单/不再白名单中
 *          is_record_ivr 是否从IVR开始录音 1开启 0关闭 ivr_type ivr类型：是否为彩铃 1:ivr 2:彩铃
 *          cur_node 当前ivr节点，cur_node从1开始 is_own_type 是否支持所属座席或所属队列 0不支持 1:所属座席
 *          2:所属队列 belong_client 如果is_own_type=1 呼入的号码属于的座席号 belong_queue
 *          如果is_own_type=2 呼入的号码属于的队列号
 * 
 *          caller_priority 来电是否是VIP bridged_cno 桥接的座席号
 * 
 *          is_call_failed_msg_send 未接来电短信提醒是否打开 is_call_success_msg_send
 *          已接来电短信是否打开 is_tail_msg_send 尾巴短信是否打开
 * 
 *          cur_queue 当前的queue dial_interface 直接呼叫号码时使用的变量例如
 *          SIP/11101041005960@172.16.15.245 dial_cno 直接呼叫座席时的座席号 dial_timeout
 *          直接呼叫的超时时间 queue_timeout 直接呼叫queue的超时时间 queue_say_cno 是否语音报号
 * 
 *          atxfer_auto 咨询是否是前台送过来的指令 必须为YES时方是前台送来指令 is_crbt 呼入到目的码系统是否自定应答 0
 *          自定应答 1 不自动应答
 */
@Component
public class GetIvrOptionServlet extends HttpServlet {
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
		JSONObject jsonObject = new JSONObject();
		logger.info("***************************************进入GetIvrOption接口*******************************************************");

		/**拿到底层传入的中继绑定的号码--------去查询此号码所属企业的Enterprise_id和Ivr_Id*/
		String ccTrunkNumber = request.getParameter("numberTrunk");

		/**呼入号码*/
		String customerNumber = request.getParameter("customerNumber");

		/**网关	*/
		String gateway = request.getParameter("gwIp");

		/**呼叫类型 呼入和网上400呼入*/
		String callType = request.getParameter("callType");
		/**网上400接口使用的ivrId*/
		String webcallIvrId = request.getParameter("webcallIvrId");
		String strEnterpriseId = request.getParameter("enterpriseId");

		Caller caller = AreaCodeUtil.updateGetAreaCode(customerNumber, gateway);
		/**来电区号*/
		String areaCode = caller.getAreaCode();
		jsonObject.put("__" + Const.CDR_CUSTOMER_NUMBER, caller.getCallerNumber()); //客户号码
		jsonObject.put("__" + Const.CDR_CUSTOMER_NUMBER_TYPE, caller.getTelType()); //电话类型
		jsonObject.put("__" + Const.CDR_CUSTOMER_AREA_CODE, areaCode); //区号

		Integer enterpriseId = null;
		Trunk trunk = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TRUNK_NUMBER_TRUNK, ccTrunkNumber), Trunk.class);
		if(trunk != null){
			jsonObject.put(Const.NUMBER_TRUNK_AREA_CODE, trunk.getAreaCode());
		}
		if(callType.equals(Const.CDR_CALL_TYPE_IB+"")){
			/* 通过呼入号码去得到它的ivr_id和enterprise_id*/
			if (trunk != null) {
				enterpriseId = trunk.getEnterpriseId();
			}
		}else if(callType.equals(Const.CDR_CALL_TYPE_OB_WEBCALL+"")){
			enterpriseId = Integer.parseInt(strEnterpriseId);
		}
		if(enterpriseId == null){
			jsonObject.put(Const.ENTERPRISE_STATUS, Const.ENTITY_STATUS_NO_SERVICE);//enterprise_status 企业目前无中继状态
		}else{
			//把企业id设置到通道里面，在ivrNode和黑名称接口中有用到
			jsonObject.put("__" + Const.ENTERPRISE_ID, String.valueOf(enterpriseId));
			EnterpriseHotline enterpriseHotline = redisService.get(Const.REDIS_DB_CONF_INDEX
					, String.format(CacheKey.ENTERPRISE_HOTLINE_ENTERPRISE_ID_NUMBER_TRUNK, enterpriseId, ccTrunkNumber), EnterpriseHotline.class);
			if( enterpriseHotline != null && StringUtils.isNotEmpty(enterpriseHotline.getHotline())){
				//根据numberTrunk获取hotline，当numberTrunk对应的hotline不存在时，用numberTrunk区号加numberTrunk代替
				jsonObject.put(Const.CDR_HOTLINE, enterpriseHotline.getHotline());
			}else{
				if(trunk != null){
					jsonObject.put(Const.CDR_HOTLINE, trunk.getAreaCode() + trunk.getNumberTrunk());
				}
			}

			Entity entity = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTITY_ENTERPRISE_ID, enterpriseId), Entity.class);
			Integer enterpriseStatus = null;
			if (entity != null) {
				enterpriseStatus = entity.getStatus();   //企业状态
				//这里企业状态不对的后面不需要再查询，之后的数据都不再需要
				if (enterpriseStatus == Const.ENTITY_STATUS_OK) {//0:未开通 1:正常 2:欠费 3:停机 4:注销
					//jsonObject.put(IvrStaticParameters.ENTERPRISE_CALL_LIMIT_IB, entity.getIbCallLimit());//企业状态
					jsonObject.put(Const.ENTERPRISE_STATUS, enterpriseStatus);//企业状态

					EnterpriseSetting s;
					s = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME, enterpriseId
							, Const.ENTERPRISE_SETTING_NAME_RESTRICT_TEL_TYPE), EnterpriseSetting.class);
					if (s!=null && s.getName().equals(Const.ENTERPRISE_SETTING_NAME_RESTRICT_TEL_TYPE) && (s.getValue().equals("1") || s.getValue().equals("2"))) {//开启了黑白名单功能
						jsonObject.put(Const.IS_RESTRICT_CHECK, "1");
					}
					s = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME, enterpriseId
							, Const.ENTERPRISE_SETTING_NAME_IS_RECORD), EnterpriseSetting.class);
					if (s != null && s.getName().equals(Const.ENTERPRISE_SETTING_NAME_IS_RECORD) && s.getValue().equals("1")) { //开启IVR录音功能
						jsonObject.put(Const.IS_RECORD, "1");
						jsonObject.put(Const.RECORD_SCOPE, s.getProperty());
					}
					s = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME, enterpriseId
							, Const.ENTERPRISE_SETTING_NAME_IS_CRBT_OPEN), EnterpriseSetting.class);
					if (s != null && s.getName().equals(Const.ENTERPRISE_SETTING_NAME_IS_CRBT_OPEN) && s.getValue().equals("1")) {  //开启彩铃, 呼入不自动answer
						jsonObject.put(Const.IS_CRBT_OPEN, "1");
					}

					if(callType.equals(String.valueOf(Const.CDR_CALL_TYPE_OB_WEBCALL))){

						if(StringUtils.isNotEmpty(webcallIvrId)){
							jsonObject.put("__" + Const.IVR_ID, webcallIvrId);
							jsonObject.put(Const.VALID_IVR, "1");
						}else{
							s = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME, enterpriseId
									, Const.ENTERPRISE_SETTING_NAME_WEBCALL_DEFAULT_IVR), EnterpriseSetting.class);
							jsonObject.put("__" + Const.IVR_ID, s.getValue());
							jsonObject.put(Const.VALID_IVR, "1");
						}
					}else{//按照呼入检查
						Date nowDate = new Date();
						Calendar cc = Calendar.getInstance();
						DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
						String newNowDate = dateFormat.format(nowDate); //格式化时间，转换为String
						//取小时和分钟
						String overNowTime = newNowDate.substring(newNowDate.length() - 5, newNowDate.length()); //处理截取字符串
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

						List<EnterpriseIvrRouter> routers = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(
								CacheKey.ENTERPRISE_IVR_ROUTER_ENTERPRISE_ID, enterpriseId), EnterpriseIvrRouter.class);
						if (routers.size() > 0) {
							boolean found = false; //是否找到相应数据 找到应该跳出循环
							for (EnterpriseIvrRouter routerLists : routers) {
								String ruleTime = routerLists.getRuleTimeProperty(); //时间配置
								String ruleAreaNumber = routerLists.getRuleAreaProperty();//地区配置
								String ruleTrunkNumber = routerLists.getRuleTrunkProperty();//中继号码配置

								String ruleProperty = routerLists.getRouterProperty(); //IVR路由目的地	1:IVR 2:固定电话 3:分机
								Integer ruleType = routerLists.getRouterType(); //IVR路由类型


								boolean areaBl = false;
								boolean trunkBl = false;
								boolean accordingWeek = false;
								boolean accordingSpecialDate = false;
								if (StringUtils.isNotEmpty(ruleTime)) {
									String ruleTimes[] = StringUtils.split(ruleTime, ";");
									for(int i=0; i<ruleTimes.length; i++){
										Integer timeId = Integer.parseInt(ruleTimes[i]);
										EnterpriseTime eTime =redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_TIME_ENTERPRISE_ID_ID
												, enterpriseId, timeId), EnterpriseTime.class);
										if (eTime.getType() == 1) { //按照星期
											String dayOfWeekOne = eTime.getDayOfWeek(); //配置的星期
											String numDayOfWeekOne[] = dayOfWeekOne.split(","); //取得星期对应的数字
											String startOne = eTime.getStartTime(); //开始时间
											String endTimeOne = eTime.getEndTime(); //结束时间
											accordingWeek = isStringHave(numDayOfWeekOne, week)
													&& overNowTime.compareTo(startOne) >= 0
													&& overNowTime.compareTo(endTimeOne) <= 0;
											if (accordingWeek) {
												break;
											}
										} else if (eTime.getType() == 2) { //按照特殊日期
											String startTime = eTime.getFromDay() + " " + eTime.getStartTime();
											String endTime = eTime.getToDay() + " " + eTime.getEndTime(); //结束日期

											accordingSpecialDate = newNowDate.compareTo(startTime) >= 0
													&& newNowDate.compareTo(endTime) <= 0;
											if (accordingSpecialDate) {
												break;
											}
										}
									}
								}
								String ruleAreaNumbers[] = StringUtils.split(ruleAreaNumber, ";");
								String ruleTrunkNumbers[] = StringUtils.split(ruleTrunkNumber, ";");
								if (StringUtils.isNotEmpty(ruleAreaNumber)) {
									for (int i = 0; i < ruleAreaNumbers.length; i++) {
										areaBl = caller.getCallerNumber().startsWith(ruleAreaNumbers[i]);
										if (areaBl) {
											break;
										}
									}
								}
								if (StringUtils.isNotEmpty(ruleTrunkNumber)) {
									for (int i = 0; i < ruleTrunkNumbers.length; i++) {
										trunkBl = ruleTrunkNumbers[i].equals(ccTrunkNumber);
										if (trunkBl) {
											break;
										}
									}
								}

								if ((accordingWeek || accordingSpecialDate) || StringUtils.isEmpty(ruleTime)) {
									if (areaBl || StringUtils.isEmpty(ruleAreaNumber)) {
										if (trunkBl || StringUtils.isEmpty(ruleTrunkNumber)) {
											jsonObject.put("" + Const.IVR_ROUTER_TYPE, ruleType);
											switch (ruleType) {
												case 1://******************1:IVR*****************************//*

													jsonObject.put("__" + Const.IVR_ID, ruleProperty);
													List<EnterpriseIvr> enterpriseIvrList = redisService.getList(Const.REDIS_DB_CONF_INDEX
															, String.format(CacheKey.ENTERPRISE_IVR_ENTERPRISE_ID_IVR_ID, enterpriseId, Integer.parseInt(ruleProperty)), EnterpriseIvr.class);
													if (enterpriseIvrList == null || enterpriseIvrList.size() == 0) {
														jsonObject.put(Const.VALID_IVR, "0");
													} else {
														jsonObject.put(Const.VALID_IVR, "1");
													}
													found = true;
													break;
												case 2://*****************2:固定电话 **************************//*
													jsonObject.put(Const.IVR_ROUTER_TEL, ruleProperty);
													found = true;
													break;
												case 3://*****************3:分机*******************************//*
													jsonObject.put(Const.IVR_ROUTER_EXTEN, ruleProperty);
													found = true;
													break;
											}
										}
									}
								}
								if(found){
									break;
								}

							}
						}
					}

				} else {
					jsonObject.put(Const.ENTERPRISE_STATUS, enterpriseStatus);
				}
			} else {
				logger.debug("数据库查询相应数据为空！请检查表：businesses   ");
			}
		}
		out.append(jsonObject.toString());
		out.flush();
		out.close();
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
