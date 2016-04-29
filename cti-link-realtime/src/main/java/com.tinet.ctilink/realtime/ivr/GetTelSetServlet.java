package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.conf.model.TelSet;
import com.tinet.ctilink.conf.model.TelSetTel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GetTelSetServlet extends HttpServlet {

	@Autowired
	RedisService redisService;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		JSONObject jsonObject = new JSONObject();

		String enterpriseId = request.getParameter("enterpriseId");
		String tsno = request.getParameter("telSet");

		if (!StringUtils.isEmpty(tsno)) {
			TelSet telset = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TEL_SET_ENTERPRISE_ID_TSNO, Integer.parseInt(enterpriseId), tsno)
					, TelSet.class);
			if (telset != null) {
				List<TelSetTel> telSetTelList = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TEL_SET_TEL_ENTERPRISE_TSNO, Integer.parseInt(enterpriseId), tsno)
						, TelSetTel.class);
				if (!telset.getIsStop().equals(Const.TEL_SET_STATUS_STOP)) {
					String telStr = "";
					int count = 0;
					for (TelSetTel t : telSetTelList) {
						telStr += t.getTel() + "," + t.getTimeout() + ";";
						count++;
					}
					jsonObject.put("tel_set_count", count);
					jsonObject.put("tel_set_timeout", telset.getTimeout());
					jsonObject.put("tel_set_strategy", telset.getStrategy());
					jsonObject.put("tel_set_tel", telStr.substring(0, telStr.length() - 1));
				}
				}
		}
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
