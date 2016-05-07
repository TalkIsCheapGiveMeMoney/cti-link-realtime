package com.tinet.ctilink.realtime.ivr;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.Agent;
import com.tinet.ctilink.conf.model.EnterpriseHotline;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.realtime.entity.InvestigationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Title SetInvestigation.java
 * @Package com.tinet.ccic.ivr
 * @author 罗尧 Email:j2ee.xiao@gmail.com
 * @since 创建时间：2011-10-20 下午04:14:50
 * @serial 意度调查结果写入数据库
 */
@Component
public class SaveInvestigationServlet extends HttpServlet {

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
		// JSONObject jsonObject = new JSONObject();
		// 获取企业id
		String enterpriseId = request.getParameter("enterpriseId");
		// 通道标识
		String mainUniqueId = request.getParameter("mainUniqueId");
		// 呼叫类型
		int callType = Integer.parseInt(request.getParameter("callType"));
		// 获取开始时间
		String startTime = request.getParameter("startTime");
		// 获取结束时间
		String endTime = request.getParameter("endTime");
		// 获取座席工号
		String cno = request.getParameter("cno");
		// 按键值
		String keys = request.getParameter("keys");
		// 目的码
		String numberTrunk = request.getParameter("numberTrunk");
		// 客户号码
		String customerNumber = request.getParameter("customerNumber");
		// 转移号码 座席号或电话号码
		String transferObject = request.getParameter("transferObject");

		String clientCrmId = null;
		String clientName = null;
		Agent agent = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.AGENT_ENTERPRISE_ID_CNO, Integer.parseInt(enterpriseId), cno), Agent.class);
		if (agent != null) {
			clientCrmId = agent.getCrmId();
			clientName = agent.getName();
		}

		String hotline = "";
		/** 获取到企业的热线号码 */
		List<EnterpriseHotline> enterpriseHotlineList = redisService.getList(Const.REDIS_DB_CONF_INDEX
				, String.format(CacheKey.ENTERPRISE_HOTLINE_ENTERPRISE_ID, Integer.parseInt(enterpriseId)), EnterpriseHotline.class);
		if (enterpriseHotlineList != null && enterpriseHotlineList.size() > 0) {
			hotline = enterpriseHotlineList.get(0).getHotline();
		}

		InvestigationRecord investigationRecord = new InvestigationRecord();
		investigationRecord.setHotline(hotline);
		investigationRecord.setMainUniqueId(mainUniqueId); // 通道标识
		investigationRecord.setEnterpriseId(Integer.valueOf(enterpriseId)); // 企业id
		investigationRecord.setCallType(callType);
		investigationRecord.setStartTime(Long.valueOf(startTime));// 开始时间
		investigationRecord.setEndTime(Long.valueOf(endTime)); // 结束时间
		investigationRecord.setDuration((int) (investigationRecord.getEndTime() - investigationRecord.getStartTime()));
		investigationRecord.setCno(cno); // 座席工号
		investigationRecord.setTransferObject(transferObject == null ? "" : transferObject);
		investigationRecord.setKeys(keys); // 评分
		investigationRecord.setClientCrmId(clientCrmId);
		investigationRecord.setClientName(clientName);
		investigationRecord.setCustomerNumber(customerNumber);

		//investigationRecordService.saveOrUpdate(investigationRecord);

		out.flush();
		out.close();
	}

}
