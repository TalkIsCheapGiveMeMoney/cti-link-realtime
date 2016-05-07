package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.realtime.util.ClidUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class GetClidServlet extends HttpServlet {

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

        /**取得通道传过来的企业id和ivrid去查询相关的IVR节点配置 */
        int enterpriseId = Integer.parseInt(request.getParameter("enterpriseId"));
        String customerNumber = request.getParameter("customerNumber");
        int routerClidCallType = Integer.parseInt(request.getParameter("routerClidCallType"));

        JSONObject jsonObject = new JSONObject();
        StringBuilder clidBack = new StringBuilder();
        String clid = ClidUtil.getClid(enterpriseId, routerClidCallType, customerNumber, clidBack);
        if (StringUtils.isNotEmpty(clid)) {
            jsonObject.put("clid", clid);
        }
        out.append(jsonObject.toString());
        out.flush();
        out.close();
    }

}
