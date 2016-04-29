package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.curl.CurlData;
import com.tinet.ctilink.curl.CurlPushClient;
import com.tinet.ctilink.curl.CurlResponse;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

@SuppressWarnings("serial")
@Component
public class CurlServlet extends HttpServlet {
	private CloseableHttpClient httpClient;
	
	public CurlServlet() {
		// 通过HttpClient连接池来保持对相同目标（同一个推送目的地服务器）的连接，减少因为每次建立HTTP连接而导致的性能损耗
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(3);

		this.httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();

		Integer enterpriseId = 0;
		Integer retry = 0;
		Integer timeout = 30;
		try {
			enterpriseId = Integer.parseInt(request.getParameter("enterpriseId"));
			retry = Integer.parseInt(request.getParameter("retry"));
			timeout = Integer.parseInt(request.getParameter("timeout"));
		} catch (Exception e) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("curl_result", 0);
			jsonObject.put("msg", "参数格式错误");
			out.append(jsonObject.toString());
			out.flush();
			out.close();
			return;
		}
		String uniqueId = request.getParameter("uniqueId");
		String userField = request.getParameter("userField");
		String url = request.getParameter("url");
		String tag = request.getParameter("tag"); // 客户标识
		if (StringUtils.isEmpty(tag)) {
			tag = "";
		}

		String params = request.getParameter("params");
		String sync = request.getParameter("sync");
		String method = request.getParameter("method");
		if (StringUtils.isEmpty(url) || StringUtils.isEmpty(sync)) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("curl_result", 0);
			jsonObject.put("msg", "缺少参数");
			out.append(jsonObject.toString());
			out.flush();
			out.close();
			return;
		}

		CurlData curlData = new CurlData();
		curlData.setEnterpriseId(enterpriseId);
		curlData.setUrl(url);
		curlData.setRetry(retry);
		curlData.setTimeout(timeout);
		curlData.setUniqueId(uniqueId);
		curlData.setUserField(userField);
		curlData.setMethod(method);
		Map<String,String> map = new HashMap<String,String>();
		boolean tagFlag = true;
		if (StringUtils.isNotEmpty(params)) {
			try {
				String parameters[] = StringUtils.split(params, "&");
				for (int i = 0; i < parameters.length; i++) {
					String tmp[] = StringUtils.split(parameters[i], "=");
					if (tmp.length == 2 && StringUtils.isNotEmpty(tmp[1])) {
						// 如果 params 中存在tag, 不再添加系统中的 客户标识
						if ("tag".equals(tmp[0])) {
							tagFlag = false;
						}
						map.put(tmp[0], tmp[1]);
					} else if (tmp.length > 2) {
						map.put(tmp[0],parameters[i].substring(tmp[0].length() + 1));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// add tag to params
		if (tagFlag) {
			map.put("tag", tag);
		}
		curlData.setParams(map);
		curlData.setType(Const.CURL_TYPE_IVR);
		if (sync.equals("1")) {
			curlData.setPosition(0);
			curlData.setStartTime(new Date());
			CurlResponse curlResponse = CurlPushClient.sendRequest(httpClient, curlData);
			if (curlResponse.getResult()) {
				out.append(curlResponse.getResultText());
				out.flush();
				out.close();
			} else {
				out.flush();
				out.close();
			}
		} else {
			curlData.setLevel(0);
			CurlPushClient.addPushQueue(curlData);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("curl_result", 1);
			out.append(jsonObject.toString());
			out.flush();
			out.close();
		}
	}
}
