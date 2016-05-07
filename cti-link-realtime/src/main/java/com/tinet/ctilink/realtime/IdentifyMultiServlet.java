package com.tinet.ctilink.realtime;

import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class IdentifyMultiServlet extends HttpServlet {

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
        IdentifyRealtime identifyRealtime = new IdentifyRealtime();
        long start = System.currentTimeMillis();
        out.print(identifyRealtime.queryByHttpServletRequest(request));
        long end = System.currentTimeMillis();
        long time = end - start;
        IdentifyRealtime.accessCount++;
        IdentifyRealtime.totalTime += time;
        IdentifyRealtime.curTime = time;
        if (time > IdentifyRealtime.maxTime) {
            IdentifyRealtime.maxTime = time;
        }
        out.flush();
        out.close();
    }

}
