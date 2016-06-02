package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.inc.EnterpriseSettingConst;
import com.tinet.ctilink.inc.SystemSettingConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseSetting;
import com.tinet.ctilink.conf.model.SystemSetting;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;

@Component
public class GetQueueRememberServlet extends HttpServlet {

	@Autowired
	RedisService redisService;
	
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
		JSONObject jsonObject = new JSONObject();
		
        // 取得当前通道里面的企业ID
        String enterpriseId = request.getParameter("enterpriseId");
        String qno = request.getParameter("qno");
        String customerNumber = request.getParameter("customerNumber");
        	
        Integer rememberTime;
        EnterpriseSetting callRememberTimeEnterpriseSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME
				, Integer.parseInt(enterpriseId), EnterpriseSettingConst.ENTERPRISE_SETTING_NAME_IB_CALL_REMEMBER_TIME), EnterpriseSetting.class);
        if(callRememberTimeEnterpriseSetting == null){
        	SystemSetting callRememberTimeSystemSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME
				, SystemSettingConst.SYSTEM_SETTING_NAME_IB_CALL_REMEMBER_TIME), SystemSetting.class);
        	rememberTime = Integer.parseInt(callRememberTimeSystemSetting.getValue());
        }else{
        	rememberTime = Integer.parseInt(callRememberTimeEnterpriseSetting.getValue());
        }
        //从kv库读取cdr，判断是否remember

     	if (false) {
     			jsonObject.put("queue_remember_cno", "9999");//to be modified
     	} 
 		out.append(jsonObject.toString());
 		out.flush();
 		out.close();
    }
}
