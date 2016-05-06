package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.realtime.entity.Caller;
import com.tinet.ctilink.realtime.util.AreaCodeUtil;
import com.tinet.ctilink.realtime.util.RouterUtil;
import org.springframework.stereotype.Component;

@Component
public class GetPrePostServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		/** 取得通道传过来的企业id和ivrid去查询相关的IVR节点配置 */
		int enterpriseId = Integer.parseInt(request.getParameter("enterpriseId"));
		String tel = request.getParameter("tel");
		int routerClidCallType = Integer.parseInt(request.getParameter("routerClidCallType"));

		JSONObject jsonObject = new JSONObject();
		Caller caller = AreaCodeUtil.updateGetAreaCode(tel, "");

		Gateway gateway = RouterUtil.getRouterGateway(enterpriseId, routerClidCallType, caller);
		if (gateway != null) {
			jsonObject.put("pre", gateway.getPrefix());
			jsonObject.put("post", gateway.getName());
			jsonObject.put("gw_ip", gateway.getIpAddr());
			jsonObject.put("cdr_callee_area_code", caller.getAreaCode());
			jsonObject.put("dial_interface_cust", "PJSIP/" + gateway.getName()+"/sip:"+gateway.getPrefix() + caller.getCallerNumber() + "@" + gateway.getIpAddr()+":"+gateway.getPort());

		}

		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
