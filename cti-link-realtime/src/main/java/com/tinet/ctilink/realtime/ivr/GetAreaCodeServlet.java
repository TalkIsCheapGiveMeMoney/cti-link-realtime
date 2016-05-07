package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.realtime.entity.Caller;
import com.tinet.ctilink.realtime.util.AreaCodeUtil;
import org.springframework.stereotype.Component;


@Component
public class GetAreaCodeServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String customerNumber = request.getParameter("tel");
		String gateway = request.getParameter("gateway");

		JSONObject jsonObject = new JSONObject();

		Caller caller = AreaCodeUtil.updateGetAreaCode(customerNumber, gateway);
		String areaCode = caller.getAreaCode();
		jsonObject.put("__" + Const.CDR_CUSTOMER_NUMBER, caller.getCallerNumber()); // 客户号码
		jsonObject.put("__" + Const.CDR_CUSTOMER_NUMBER_TYPE, caller.getTelType()); // 电话类型
		jsonObject.put("__" + Const.CDR_CUSTOMER_AREA_CODE, areaCode); // 区号

		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
