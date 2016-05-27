package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.conf.entity.Caller;
import com.tinet.ctilink.conf.util.AreaCodeUtil;
import com.tinet.ctilink.conf.util.ExtenUtil;
import com.tinet.ctilink.conf.util.RouterUtil;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.conf.model.CtiLinkExten;
import com.tinet.ctilink.conf.model.Gateway;
import org.springframework.stereotype.Component;

@Component
public class GetAgentServlet extends HttpServlet {

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
		String exten = request.getParameter("exten");
		int routerClidCallType = Integer.parseInt(request.getParameter("routerClidCallType"));
		
		JSONObject jsonObject = new JSONObject();
		CtiLinkExten ctiLinkExten = ExtenUtil.getExten(enterpriseId, exten);
		if (ctiLinkExten != null) {
        	Gateway gateway = RouterUtil.getRouterGatewayInternal(enterpriseId, routerClidCallType, exten);
            if(gateway != null){
    			jsonObject.put("gw_ip", gateway.getIpAddr());
    			jsonObject.put("cdr_callee_area_code", ctiLinkExten.getAreaCode());
    			jsonObject.put("dial_interface", "PJSIP/" + gateway.getName()+"/sip:" + enterpriseId + exten + "@"
                        + gateway.getIpAddr() + ":" + gateway.getPort());
            }
		}

		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
