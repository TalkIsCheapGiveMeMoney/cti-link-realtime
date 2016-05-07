package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetPastCalleeServlet extends HttpServlet {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        this.doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 取得当前通道里面的企业ID
        String ccEnterpriseId = request.getParameter("enterpriseId");

        if (StringUtils.isEmpty(ccEnterpriseId) || !Pattern.compile(Const.IS_NUMBER_VALIDATION).matcher(ccEnterpriseId).find()) {
            responseRes(response, "1", "", "", "", "appId格式错误");
            return;
        }

        // 取得主叫记忆时间
        String validTime = request.getParameter("validTime");
        if (StringUtils.isNotEmpty(validTime)) {
            if (!Pattern.compile(Const.IS_NUMBER_VALIDATION).matcher(validTime).find()) {
                responseRes(response, "1", "", "", "", "memoryTime格式错误");
                return;
            }
        } else {
            validTime = "24";//默认记忆时间为24小时
        }

        String cdrType = request.getParameter("cdrType");
        if (StringUtils.isNotEmpty(cdrType)) {
            if ((!cdrType.equals("inbound") && !cdrType.equals("outbound") && !cdrType.equals("both"))) {
                responseRes(response, "1", "", "", "", "memoryType参数错误");
                return;
            }
        } else {
            cdrType = "outbound";//默认记忆类型为外呼
        }

        // 来电号码
        String callerNumber = request.getParameter("callerNumber");
        if (StringUtils.isEmpty(callerNumber) || !Pattern.compile(Const.TEL_VALIDATION).matcher(callerNumber).find()) {
            responseRes(response, "1", "", "", "", "callerNumber号码格式错误");
            return;
        }

        int Duration = Integer.parseInt(validTime) * 60 * 60;
        //查询呼入主叫记忆
        long nowTime = new Date().getTime() / 1000; // 当前时间戳
        long Time = nowTime - Duration; // 24小时
        List cdrIbList = null;
        String inTel = null;
        String inTime = null;
        if (cdrType.equals("inbound") || cdrType.equals("both")) {
//			// 客户呼入
//			CdrIbService cdrIbService = (CdrIbService) ContextUtil.getContext().getBean("cdrIbService");
//			cdrIbList = cdrIbService.findBySql("select customer_number,start_time from cdr_ib_"
//					+ ccEnterpriseId + " where callee_number='"
//					+ callerNumber + "' and status in (1,2) and start_time >  " + Time
//					+ "  order by start_time desc  limit 1");
            if (!cdrIbList.isEmpty()) {
                Object[] data = (Object[]) cdrIbList.get(0);
                if (data.length == 2) {
                    inTel = data[0].toString();
                    inTime = data[1].toString();
                }
            }
        }

        List cdrObList = null;
        String outTel = null;
        String outTime = null;
        if (cdrType.equals("outbound") || cdrType.equals("both")) {
            // 外呼客户
//			CdrObService cdrObService = (CdrObService) ContextUtil.getContext().getBean("cdrObService");
//			cdrObList = cdrObService.findBySql("select customer_number,start_time from cdr_ob_"
//					+ ccEnterpriseId + " where callee_number='"
//					+ callerNumber + "' and status in (23,24) and start_time >  " + Time
//					+ "  order by start_time desc  limit 1");
            if (!cdrObList.isEmpty()) {
                Object[] data = (Object[]) cdrObList.get(0);
                if (data.length == 2) {
                    outTel = data[0].toString();
                    outTime = data[1].toString();
                }
            }
        }

        // 判断开始，如果呼入存在的话
        if (cdrIbList != null && cdrIbList.size() > 0) {
            if (cdrObList != null && cdrObList.size() > 0) {
                if (inTime.compareTo(outTime) > 0) {
                    responseRes(response, "0", inTime, "inbound", inTel, "");
                } else {
                    responseRes(response, "0", outTime, "outbound", outTel, "");
                }
            } else {
                responseRes(response, "0", inTime, "inbound", inTel, "");
            }
        } else {
            if (cdrObList != null && cdrObList.size() > 0) {
                responseRes(response, "0", outTime, "outbound", outTel, "");
            } else {
                responseRes(response, "1", "", "", "", "no datas found");
            }
        }
    }

    private void responseRes(HttpServletResponse response, String result,
                             String validTime, String cdrType, String calleeNumber, String description) {

        PrintWriter out;

        try {
            out = response.getWriter();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("result", result);
            jsonObject.put("cdrType", cdrType);
            jsonObject.put("validTime", validTime);
            jsonObject.put("calleeNumber", calleeNumber);
            jsonObject.put("description", description);

            out.append(jsonObject.toString());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
