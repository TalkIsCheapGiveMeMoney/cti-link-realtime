package com.tinet.ctilink.realtime.ivr;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.entity.Caller;
import com.tinet.ctilink.conf.model.EnterpriseSetting;
import com.tinet.ctilink.conf.model.Trunk;
import com.tinet.ctilink.conf.util.AreaCodeUtil;
import com.tinet.ctilink.conf.util.ClidUtil;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.inc.EnterpriseSettingConst;
import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
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
		
		String customerNumber = request.getParameter("customerNumber");
		
		int routerClidCallType = Integer.parseInt(request.getParameter("routerClidCallType"));
		
		if(StringUtils.isEmpty(clid)) {
			responseError(response,false, "提交失败，clid不能为空");
			return ;
		}else{
			if (!StringUtils.isNumeric(clid)) {
				responseError(response, false, "提交失败，参数clid格式不正确");
				return ;
			}
		}
		if(!ClidUtil.isClidValid(Integer.parseInt(enterpriseId), routerClidCallType, customerNumber, clid)){
			jsonObject.put("clidResult","1");
		}
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