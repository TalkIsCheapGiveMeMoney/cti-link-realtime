package com.tinet.ctilink.realtime.ivr;

import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import com.tinet.ctilink.realtime.entity.LogTts;
import com.tinet.ctilink.realtime.util.TtsSendGetUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * @author fengwei //
 * @date 16/4/29 13:48
 */
@Component
public class TtsSendGetServlet extends HttpServlet {
    @Autowired
    private RedisService redisService;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doPost(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonObject = new JSONObject();

        String ttsText = request.getParameter("ttsText");
        String enterpriseId = request.getParameter("enterprsieId");
        String uniqueId = request.getParameter("uniqueId");

        LogTts logTts = new LogTts();
        logTts.setEnterpriseId(Integer.parseInt(enterpriseId));
        logTts.setRequestTime(new Date());
        logTts.setCallFrom(Const.LOG_TTS_CALL_FROM_DIALPLAN);
        String res = TtsSendGetUtil.ttsSendGet(uniqueId, ttsText, true, logTts);
        if (StringUtils.isNotEmpty(res)) {
            jsonObject.put("TTS_FILE", res);
            redisService.incrby(Const.REDIS_DB_CTI_INDEX, "system.tts.success_count", 1);
        } else {
            redisService.incrby(Const.REDIS_DB_CTI_INDEX, "system.tts.fail_count", 1);
        }

        //TODO 日志记录到kv
//		LogTtsService logTtsService = (LogTtsService) ContextUtil.getContext().getBean("logTtsService");
//		logTtsService.saveOrUpdate(logTts);

        out.append(jsonObject.toString());
        out.flush();
        out.close();
    }
}
