package com.tinet.ctilink.realtime;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author 罗尧 Email:j2ee.xiao@gmail.com
 * @version V1.0 2011-9-14  下午02:15:14
 * @Title:single.java
 * @Package:com.tinet.ccic.interfaces.realtime.moh
 */
@SuppressWarnings("serial")
@Component
public class MohSingleServlet extends HttpServlet {

    @Autowired
    private MusicOnHoldRealtime musicOnHoldRealtime;


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

        long start = System.currentTimeMillis();
        out.print(musicOnHoldRealtime.queryByHttpServletRequest(request));
        long end = System.currentTimeMillis();
        long time = end - start;

        MusicOnHoldRealtime.accessCount++;
        MusicOnHoldRealtime.totalTime += time;
        MusicOnHoldRealtime.curTime = time;
        if (time > MusicOnHoldRealtime.maxTime) {
            MusicOnHoldRealtime.maxTime = time;
        }

        out.flush();
        out.close();
    }

}
