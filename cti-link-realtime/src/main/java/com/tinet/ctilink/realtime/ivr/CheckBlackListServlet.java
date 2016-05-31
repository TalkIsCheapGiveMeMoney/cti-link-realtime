package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tinet.ctilink.ami.inc.AmiChanVarNameConst;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseSetting;
import com.tinet.ctilink.conf.model.RestrictTel;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.inc.EnterpriseSettingConst;
import com.tinet.ctilink.json.JSONObject;

/**
 *@Title CheckBlackList.java
 *@Package com.tinet.ccic.interfaces
 *@author 罗尧 Email:j2ee.xiao@gmail.com
 *@version V1.0 2011-9-14  下午01:36:28
 * 访问方式：http://ip+端口/interface/ivr/CheckBlackList
 * http://172.16.203.194/interface/ivr/CheckBlackList
 * 这个类主要用以判断呼入的号码是否是黑名单
 * 需要的参数，这个呼入动作是呼入的那个企业，这个企业的ID是，这个企业的黑名单配置里面是否有此号码
 */
@Component
public class CheckBlackListServlet extends HttpServlet {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	RedisService redisService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		JSONObject jsonObject = new JSONObject();
		/*
		 * 拿到企业(租户)ID   enterprise_id
		 * 来电号码 		  caller_number
		 * 来电区号			  caller_areacode
		 * 去判断这个号码在这个租户中是否为黑名单号码
		 * 如果此来电号码所对应的值为1，表示此号码为黑名单，否则不是黑名单
		 */
		logger.debug("***************************************进入CheckBlackList接口*******************************************************");
		//通过企业id和呼入号码，查询这个呼入号码在此企业id对应的配置中是否为黑名单，0不是黑名单，1为黑名单，其他按照黑名单处理
		String ccEnterpriseId = request.getParameter("enterpriseId");
		logger.debug(ccEnterpriseId + ":------取得通道里面的企业ID---CheckBlackList");
		String ccCallerNumber = request.getParameter("customerNumber");
		logger.debug(ccCallerNumber + ":----------当前呼入的号码---CheckBlackList");
		//获取地区区号
		String areaCode = request.getParameter("customerAreaCode");

		//首先看启用了黑名单还是白名单，或者什么都没有启用
		EnterpriseSetting enterpriseSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME,
				Integer.parseInt(ccEnterpriseId), EnterpriseSettingConst.ENTERPRISE_SETTING_NAME_RESTRICT_TEL_TYPE), EnterpriseSetting.class);
		int isRestrictTel = 0;
		if (null != enterpriseSetting) {
			String value = enterpriseSetting.getValue();
			if (!"".equals(value) && null != value) {
				Integer restrictType;
				if (value.equals("1")) {  //黑名单
					restrictType = 1;
					RestrictTel restrictTel = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.RESTRICT_TEL_ENTERPRISE_ID_TYPE_RESTRICT_TYPE_TEL,
							Integer.parseInt(ccEnterpriseId), Const.RESTRICT_TEL_TYPE_IB, restrictType, ccCallerNumber), RestrictTel.class);
					if(restrictTel != null){
						isRestrictTel = 1;
					} else {
						restrictTel = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.RESTRICT_TEL_ENTERPRISE_ID_TYPE_RESTRICT_TYPE_TEL,
								Integer.parseInt(ccEnterpriseId), Const.RESTRICT_TEL_TYPE_IB, restrictType, areaCode), RestrictTel.class);
						if (restrictTel != null) {
							isRestrictTel = 1;
						}
					}
				}else if (value.equals("2")) {  //白名单
					isRestrictTel = 1;
					restrictType = 2;
					RestrictTel restrictTel = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.RESTRICT_TEL_ENTERPRISE_ID_TYPE_RESTRICT_TYPE_TEL,
							Integer.parseInt(ccEnterpriseId), Const.RESTRICT_TEL_TYPE_IB, restrictType, ccCallerNumber), RestrictTel.class);
					if(restrictTel != null){
						isRestrictTel = 0;
					} else {
						restrictTel = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.RESTRICT_TEL_ENTERPRISE_ID_TYPE_RESTRICT_TYPE_TEL,
								Integer.parseInt(ccEnterpriseId), Const.RESTRICT_TEL_TYPE_IB, restrictType, areaCode), RestrictTel.class);
						if (restrictTel != null) {
							isRestrictTel = 0;
						}
					}

				}


			}
		} else {
			logger.debug("调用第三方接口失败-------getEntSetByName");
		}

		jsonObject.put(AmiChanVarNameConst.IS_RESTRICT_TEL, isRestrictTel);
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
