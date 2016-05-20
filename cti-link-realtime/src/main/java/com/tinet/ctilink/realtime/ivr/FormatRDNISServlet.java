package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.inc.SystemSettingConst;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.conf.model.SystemSetting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@SuppressWarnings("serial")
@Component
public class FormatRDNISServlet extends HttpServlet {

	@Autowired
	RedisService redisService;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		String rdnis = request.getParameter("rdnis");
		String gwIp = request.getParameter("gwIp");
		String areaCode = "";

		if (!StringUtils.isEmpty(gwIp)) {
			boolean find = false;
			List<Gateway> gatewayList = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
			for (Gateway gw : gatewayList) {
				if (gw.getIpAddr().equals(gwIp)) {
					areaCode = gw.getAreaCode();
					find = true;
					break;
				}
			}
			if (!find) {
				SystemSetting systemSetting = redisService.get(Const.REDIS_DB_CONF_INDEX,
						String.format(CacheKey.SYSTEM_SETTING_NAME, SystemSettingConst.SYSTEM_SETTING_NAME_DEFAULT_AREA_CODE), SystemSetting.class);
				if (systemSetting != null) {
					areaCode = systemSetting.getValue();
				}
			}
		}

		JSONObject jsonObject = new JSONObject();

		rdnis = formatRDNIS(rdnis, areaCode);
		jsonObject.put("__" + Const.RDNIS, rdnis); // 电话类型
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

	private String formatRDNIS(String rdnis, String areaCode) {
		if (Pattern.compile("^86").matcher(rdnis).find()) {
			rdnis = rdnis.substring(2);
		}
		if (Pattern.compile(Const.PATTERN_MOBILE_WITH_PREFIX0).matcher(rdnis).find()) {
			rdnis = rdnis.substring(1);
		} else if (Pattern.compile(Const.PATTERN_MOBILE_WITHOUT_PREFIX0).matcher(rdnis).find()) {

		} else {
			if (rdnis.length() <= 8) {
				rdnis = areaCode + rdnis;
			} else {
				if (!rdnis.subSequence(0, 1).equals("0")) {
					rdnis = "0" + rdnis;
				}
			}
		}
		return rdnis;
	}

}
