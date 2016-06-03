package com.tinet.ctilink.realtime.ivr;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseSetting;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.inc.EnterpriseSettingConst;
import com.tinet.ctilink.json.JSONArray;
import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author fengwei //
 * @date 16/6/3 17:35
 */
@Component
public class GetCdrUserFieldServlet extends HttpServlet{
    @Autowired
    private RedisService redisService;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        /**取得通道传过来的企业id */
        int enterpriseId = Integer.parseInt(request.getParameter("enterpriseId"));

        JSONObject jsonObject = new JSONObject();
        EnterpriseSetting enterpriseSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_SETTING_ENTERPRISE_ID_NAME
                , enterpriseId, EnterpriseSettingConst.ENTERPRISE_SETTING_NAME_CDR_USER_FIELD), EnterpriseSetting.class);
        if (enterpriseSetting != null
                && StringUtils.isNotEmpty(enterpriseSetting.getValue())) {
            JSONArray jsonArray = JSONArray.fromObject(enterpriseSetting.getValue());
            if (jsonArray != null) {
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < jsonArray.size(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    sb.append(object.getString("name")).append("=").append("${")
                            .append(object.getString("value")).append("}");
                    if (i != jsonArray.size() - 1) {
                        sb.append("&");
                    }
                }
                jsonObject.put("cdr_user_field", sb.toString());
            }
        }
        out.append(jsonObject.toString());
        out.flush();
        out.close();
    }
}
