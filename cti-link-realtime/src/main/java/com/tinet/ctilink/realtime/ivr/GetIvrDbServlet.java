package com.tinet.ctilink.realtime.ivr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseIvr;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * @author fengwei //
 * @date 16/4/29 13:38
 */
@Component
public class GetIvrDbServlet extends HttpServlet {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    RedisService redisService;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonObject = new JSONObject();

        int enterpriseId = Integer.parseInt(request.getParameter("enterpriseId"));
        int ivrId = Integer.parseInt(request.getParameter("ivrId"));
        int enterpriseIvrId = Integer.parseInt(request.getParameter("enterpriseIvrId"));
        String sql = request.getParameter("sql");

        List<EnterpriseIvr> enterpriseIvrList = redisService.getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_IVR_ENTERPRISE_ID_IVR_ID
                , enterpriseId, ivrId), EnterpriseIvr.class);
        if (enterpriseIvrList != null) {
            EnterpriseIvr enterpriseIvr = null;
            for (EnterpriseIvr e : enterpriseIvrList) {
                if (e.getId() == enterpriseIvrId) {
                    enterpriseIvr = e;
                    break;
                }
            }
            if (enterpriseIvr != null) {
                ObjectMapper mapper = new ObjectMapper();
                Map map = null;
                try {
                    map = mapper.readValue(enterpriseIvr.getProperty(), Map.class);
                } catch (Exception e) {
                    logger.error("GetIvrDbServlet.doPost mapper.readValue error:", e);
                }
                if (map != null && map.size() > 0) {
                    String ivrDbType = map.get("db_type").toString();
                    String ivrDbUrl = map.get("db_url").toString();
                    String ivrDbPort = map.get("db_port").toString();
                    String ivrDbName = map.get("db_name").toString();
                    String ivrDbUser = map.get("db_user").toString();
                    String ivrDbPwd = map.get("db_pwd").toString();
                    String ivrDbVariable = map.get("db_variable").toString();

                    Connection conn = null;
                    PreparedStatement pstmt = null;
                    ResultSet rs = null;
                    try {
                        if (ivrDbType.equals(String.valueOf(Const.ENTERPRISE_IVR_OP_ACTION_DB_TYPE_ORACLE))) {//oracle
                            Class.forName("oracle.jdbc.driver.OracleDriver");
                            String url = "jdbc:oracle:thin:@" + ivrDbUrl + ":" + ivrDbPort + ":" + ivrDbName;
                            conn = DriverManager.getConnection(url, ivrDbUser, ivrDbPwd);
                        } else if (ivrDbType.equals(String.valueOf(Const.ENTERPRISE_IVR_OP_ACTION_DB_TYPE_MYSQL))) {//mysql
                            Class.forName("com.mysql.jdbc.Driver");
                            //配置数据源
                            String url = "jdbc:mysql://" + ivrDbUrl + "/" + ivrDbName;
                            conn = DriverManager.getConnection(url, ivrDbUser, ivrDbPwd);
                        } else if (ivrDbType.equals(String.valueOf(Const.ENTERPRISE_IVR_OP_ACTION_DB_TYPE_POSTGRESQL))) {//postgresql
                            String url = "jdbc:postgresql://" + ivrDbUrl + ":" + ivrDbPort + "/" + ivrDbName;
                            Class.forName("org.postgresql.Driver");
                            // 连接数据库
                            conn = DriverManager.getConnection(url, ivrDbUser, ivrDbPwd);
                        }
                        if (conn != null) {
                            pstmt = conn.prepareStatement(sql);
                            rs = pstmt.executeQuery();
                            String variables[] = StringUtils.split(ivrDbVariable, ",");
                            if (rs.next()) {
                                for (int i = 0; i < variables.length; i++) {
                                    jsonObject.put(variables[i], rs.getObject(i + 1).toString());
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("GetIvrDbServlet.doPost conn error:", e);
                    } finally {
                        try {
                            if (rs != null) {
                                rs.close();
                            }
                            if (pstmt != null) {
                                pstmt.close();
                            }
                            if (conn != null) {
                                conn.close();
                            }
                        } catch (Exception e) {
                            logger.error("GetIvrDbServlet.doPost close error:", e);
                        }
                    }
                }
            }
        }

        out.append(jsonObject.toString());
        out.flush();
        out.close();

    }
}