package com.tinet.ctilink.realtime.ivr;

import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

@Component
public class IsTelNumberServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(IsTelNumberServlet.class.getName());

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		logger.info("***************************************进入IsTelNumber接口*******************************************************");
		/**需要验证的clid	*/	
		String telNumber = request.getParameter("tel");
		
		if(StringUtils.isEmpty(telNumber)) {
			responseRes(response,"-1", "提交失败，tel不能为空");
			return ;
		}else{
			if (!StringUtils.isNumeric(telNumber)) {
				responseRes(response, "-1", "提交失败，参数tel格式不正确");
				return ;
			}
		}
		 if(Pattern.compile(Const.TEL_NUMBER_VALIDATION).matcher(telNumber).find()){
				responseRes(response,"0", "tel符合号码规则");
				return;
		 }else{
				responseRes(response,"-1", "tel不符合号码规则");
				return;
		 }
	}
	public void responseRes( HttpServletResponse response, String result, String description){
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
