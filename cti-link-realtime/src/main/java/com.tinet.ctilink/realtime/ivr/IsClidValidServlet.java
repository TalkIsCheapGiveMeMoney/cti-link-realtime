package com.tinet.ctilink.realtime.ivr;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseSetting;
import com.tinet.ctilink.conf.model.Trunk;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.realtime.entity.Caller;
import com.tinet.ctilink.realtime.util.AreaCodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *@Title IsClidValidServlet.java
 *@Package com.tinet.ccic.ivr

	 * 访问方式：http://ip+端口/interface/ivr/IsClidValidServlet
	 * http://172.16.203.194/interface/ivr/IsClidValid
	 * 这个类主要获取clid是否有效信息，如下：
*/
public class IsClidValidServlet extends HttpServlet {

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
		logger.info("***************************************进入IsClidValid接口*******************************************************");

		/**企业号*/
		String enterpriseId = request.getParameter("enterpriseId");
		
		/**需要验证的clid	*/	
		String clid = request.getParameter("clid");
		
		if(StringUtils.isEmpty(clid)) {
			responseError(response,false, "提交失败，clid不能为空");
			return ;
		}else{
			if (!StringUtils.isNumeric(clid)) {
				responseError(response, false, "提交失败，参数clid格式不正确");
				return ;
			}
		}
		Integer isClidValidFlag = 0;

		EnterpriseSetting setting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME
				, Integer.parseInt(enterpriseId), Const.ENTERPRISE_SETTING_NAME_CLID_LIST), EnterpriseSetting.class);
		if(null != setting && setting.getValue() != null){
			String[] clidArray = setting.getValue().split(",");
			for(String str: clidArray){
				if(clid.equals(str)){
					isClidValidFlag = 1;
				}
			}
		}
		if(isClidValidFlag != 1){
			Caller caller = AreaCodeUtil.updateGetAreaCode(clid, "");
			
			if(caller !=null){
				Trunk trunk = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TRUNK_NUMBER_TRUNK
						, caller.getRealNumber()), Trunk.class);
				if(trunk != null && StringUtils.isNotEmpty(caller.getAreaCode()) && clid.startsWith(caller.getAreaCode())){
					if(trunk.getAreaCode() != null && trunk.getAreaCode().equals(caller.getAreaCode()) 
							&& trunk.getEnterpriseId() != null && Integer.toString(trunk.getEnterpriseId()).equals(enterpriseId)){
						isClidValidFlag = 1;
					}
				}
			}
		}
		jsonObject.put("clidResult",isClidValidFlag);
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}
	public void responseError( HttpServletResponse response, boolean result, String description){
		PrintWriter out;
		try {
			out = response.getWriter();
		
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result",result);																
			jsonObject.put("description", description);																
			out.append(jsonObject.toString());
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}