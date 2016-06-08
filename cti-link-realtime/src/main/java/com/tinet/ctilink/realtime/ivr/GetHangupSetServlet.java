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
import com.tinet.ctilink.conf.model.EnterprisePushAction;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.conf.model.EnterpriseHangupSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;

@Component
public class GetHangupSetServlet extends HttpServlet {

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

		String enterpriseId = request.getParameter("enterpriseId");
		String callType = request.getParameter("callType");
		Integer type;
		// 判断外呼或呼入挂机推送
		if (String.valueOf(Const.CDR_CALL_TYPE_IB).equals(callType)) {
			type = Const.HANGUP_SET_TYPE_IB;
		} else {
			type = Const.HANGUP_SET_TYPE_OB;
		}

		List<EnterpriseHangupSet> setList = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_HANGUP_SET_ENTERPRISE_ID_TYPE
				, Integer.parseInt(enterpriseId), type), EnterpriseHangupSet.class);
		if (setList != null) {
			jsonObject.put("hangup_set_count", setList.size());
			for (int i = 0; i < setList.size(); i++) {
				jsonObject.put("hangup_set_" + i + "_name",
						StringEscapeUtils.unescapeHtml4((setList.get(i).getVariableName())));
				jsonObject.put("hangup_set_" + i + "_value",
						StringEscapeUtils.unescapeHtml4(setList.get(i).getVariableValue()));
				jsonObject.put("hangup_set_" + i + "_value_type", setList.get(i).getVariableValueType());
			}
		}else{
			jsonObject.put("hangup_set_count", 0);
		}

		//挂机推送设置
		if (String.valueOf(Const.CDR_CALL_TYPE_IB).equals(callType)) {
			type = Const.ENTERPRISE_PUSH_TYPE_HANGUP_IB;
		} else {
			type = Const.ENTERPRISE_PUSH_TYPE_HANGUP_OB;
		}

		List<EnterprisePushAction> hangupActionList = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_HANGUP_ACTION_ENTERPRISE_ID_TYPE
				, Integer.parseInt(enterpriseId), type), EnterprisePushAction.class);
		if (hangupActionList != null) {
			jsonObject.put("hangup_action_count", hangupActionList.size());
			for (int i = 0; i < hangupActionList.size(); i++) {
				EnterprisePushAction hangupAction = hangupActionList.get(i);
				String paramName[] = StringUtils.split(hangupAction.getParamName(), ",");
				String paramVariable[] = StringUtils.split(hangupAction.getParamVariable(), ",");
				jsonObject.put("hangup_action_" + i + "_param_count", paramName.length);
				jsonObject.put("hangup_action_" + i + "_id", hangupAction.getId());
				BASE64Decoder base64Decoder = new BASE64Decoder();
				for (int j = 0; j < paramName.length; j++) {
					String param = new String(base64Decoder.decodeBuffer(paramName[j]));
					String variable = new String(base64Decoder.decodeBuffer(paramVariable[j]));
					jsonObject.put("hangup_action_" + i + "_param_" + j + "_name", param);
					jsonObject.put("hangup_action_" + i + "_param_" + j + "_value", variable);
				}
			}
		} else {
			jsonObject.put("hangup_action_count", 0);
		}
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
