package com.tinet.ctilink.realtime.ivr;

import com.alibaba.dubbo.config.annotation.Reference;
import com.tinet.ctilink.conf.model.EnterpriseVoice;
import com.tinet.ctilink.conf.service.v1.EnterpriseVoiceService;
import com.tinet.ctilink.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * **********************************************
 *
 * @author 罗尧   Email:j2ee.xiao@gmail.com
 * @Title SaveSelfRecordServlet.java
 * @Pageage com.tinet.ccic.ivr
 * @since 1.0 创建时间 2011-12-8 下午5:53:31
 * **********************************************
 */
public class SaveSelfRecordServlet extends HttpServlet {

    @Reference
    EnterpriseVoiceService enterpriseVoiceService;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        this.doPost(request, response);
    }

    /**
     * 自助录音信息记录
     *
     * @param request  the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException      if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();


        String enterpriseId = request.getParameter("enterpriseId");
        String path = request.getParameter("path");


        EnterpriseVoice enterpristVoice = new EnterpriseVoice();
        enterpristVoice.setEnterpriseId(Integer.valueOf(enterpriseId));
        enterpristVoice.setVoiceName("[自助录音]" + (new Date()).getTime() + ".wav");
        enterpristVoice.setPath(path);
        enterpristVoice.setDescription("客户的自助录音");
        enterpristVoice.setAuditStatus(3);
        enterpriseVoiceService.createEnterpriseVoice(null, enterpristVoice);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", "success");
        out.append(jsonObject.toString());
        out.flush();
        out.close();
    }

}
