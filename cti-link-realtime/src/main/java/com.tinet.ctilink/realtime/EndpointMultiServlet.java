package com.tinet.ctilink.realtime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class EndpointMultiServlet extends HttpServlet {

    @Autowired
    EndpointRealtime endpointRealtime;

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
        out.print(endpointRealtime.queryByHttpServletRequest(request));
        long end = System.currentTimeMillis();
        long time = end - start;
        EndpointRealtime.accessCount++;
        EndpointRealtime.totalTime += time;
        EndpointRealtime.curTime = time;
        if (time > EndpointRealtime.maxTime) {
            EndpointRealtime.maxTime = time;
        }
        out.flush();
        out.close();
    }

}
