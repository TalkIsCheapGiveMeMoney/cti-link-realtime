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
import com.tinet.ctilink.json.JSONArray;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.conf.model.EnterpriseInvestigation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Title GetInvestigation.java
 * @Package com.tinet.ccic.ivr
 * @author 罗尧 Email:j2ee.xiao@gmail.com
 * @since 创建时间：2011-10-20 下午02:12:07
 * @serial 获取满意度调查
 * 
 *         访问方式：http://ip+端口/interface/ivr/GetInvestigation
 *         http://172.16.203.194/interface/ivr/GetInvestigation
 *         这个类主要获取满意度调查功能信息，如下： …… ……
 */
@SuppressWarnings("serial")
@Component
public class GetInvestigationServlet extends HttpServlet {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
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
		// 获取企业id
		String enterpriseId = request.getParameter("enterpriseId");
		logger.debug("企业id为：" + enterpriseId);
		// 根据企业id或者对于的满意度调查配置
		List<EnterpriseInvestigation> eninlists = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_INVESTIGATION_ENTERPRISE_ID, Integer.parseInt(enterpriseId))
				, EnterpriseInvestigation.class);
		if (eninlists!= null && eninlists.size() > 0) {
			for (EnterpriseInvestigation investigation : eninlists) {
				JSONObject jsonObj = JSONObject.fromObject(investigation.getProperty());
				if (jsonObj != null) {
					if (investigation.getAction() == 1) { /** 播放节点 */
						jsonObject.put("sv_" + investigation.getPath() + "_action", investigation.getAction());
						jsonObject.put("sv_" + investigation.getPath() + "_anchor", investigation.getAnchor());
						jsonObject.put("sv_" + investigation.getPath() + "_play_type", jsonObj.get("play_type")); // 播放类型
						jsonObject.put("sv_" + investigation.getPath() + "_play_file", jsonObj.get("play_file")); // 播放类型对应的值
						jsonObject.put("sv_" + investigation.getPath() + "_next", jsonObj.get("next")); // 下一跳
					}
					if (investigation.getAction() == 2) { /** 选择节点 */
						jsonObject.put("sv_" + investigation.getPath() + "_action", investigation.getAction());
						jsonObject.put("sv_" + investigation.getPath() + "_anchor", investigation.getAnchor());
						jsonObject.put("sv_" + investigation.getPath() + "_select_file", jsonObj.get("select_file"));
						jsonObject.put("sv_" + investigation.getPath() + "_select_retries", jsonObj.get("select_retries"));
						JSONArray jsonSelectArray = jsonObj.getJSONArray("select");
						for(int i = 0; i < jsonSelectArray.size(); i++){
							JSONObject jsonTmp = jsonSelectArray.getJSONObject(i);
							String key = jsonTmp.get("key").toString();
							String next = jsonTmp.get("next").toString();
							jsonObject.put("sv_" + investigation.getPath() + "_select_" + key + "_next", next);
						}
					}
				}

			}

		} else {
			out.append("此企业id没有对应的满意度调查配置");
			logger.debug("此企业id没有对应的满意度调查配置");
		}
		out.append(jsonObject.toString());
		out.flush();
		out.close();
	}

}
