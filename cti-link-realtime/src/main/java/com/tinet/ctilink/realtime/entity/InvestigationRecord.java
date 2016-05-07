package com.tinet.ctilink.realtime.entity;

import java.util.Date;

/**
 * 满意度调查记录表实体类
 * <p>
 * fileName：InvestigationRecord.java
 * <p>
 * Copyright (c) 2006-2011 T&I Net Communication CO.,LTD. All rights reserved.
 * 
 * @author wanghl
 * @since 1.0
 * @version 1.0
 */
public class InvestigationRecord implements java.io.Serializable {

	// Fields

	private String mainUniqueId;
	private Integer enterpriseId;
	private String hotline;
	private Integer callType;
	private String customerNumber;
	private Long startTime;
	private Long endTime;
	private int duration;
	private Date startTimeGMT;
	private Date endTimeGMT;
	private String cno;
	private String transferObject;
	private String clientCrmId;
	private String clientName;
	private String keys;
	private Date createTime;
	private Date startCreateTime;
	private Date endCreateTime;
	private String callTypeDesc;
	private String recordFile;

	private String taskName; // 任务名称

	// Constructors

	/** default constructor */
	public InvestigationRecord() {
		this.keys = null;
		this.createTime = new Date();
	}

	public String getMainUniqueId() {
		return mainUniqueId;
	}

	public void setMainUniqueId(String mainUniqueId) {
		this.mainUniqueId = mainUniqueId;
	}

	public Integer getEnterpriseId() {
		return enterpriseId;
	}

	public void setEnterpriseId(Integer enterpriseId) {
		this.enterpriseId = enterpriseId;
	}

	public String getHotline() {
		return hotline;
	}

	public void setHotline(String hotline) {
		this.hotline = hotline;
	}

	public Integer getCallType() {
		return callType;
	}

	public void setCallType(Integer callType) {
		this.callType = callType;
	}

	public String getCustomerNumber() {
		return customerNumber;
	}

	public void setCustomerNumber(String customerNumber) {
		this.customerNumber = customerNumber;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Date getStartTimeGMT() {
		return startTimeGMT;
	}

	public void setStartTimeGMT(Date startTimeGMT) {
		this.startTimeGMT = startTimeGMT;
	}

	public Date getEndTimeGMT() {
		return endTimeGMT;
	}

	public void setEndTimeGMT(Date endTimeGMT) {
		this.endTimeGMT = endTimeGMT;
	}

	public String getCno() {
		return cno;
	}

	public void setCno(String cno) {
		this.cno = cno;
	}

	public String getTransferObject() {
		return transferObject;
	}

	public void setTransferObject(String transferObject) {
		this.transferObject = transferObject;
	}

	public String getClientCrmId() {
		return clientCrmId;
	}

	public void setClientCrmId(String clientCrmId) {
		this.clientCrmId = clientCrmId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getStartCreateTime() {
		return startCreateTime;
	}

	public void setStartCreateTime(Date startCreateTime) {
		this.startCreateTime = startCreateTime;
	}

	public Date getEndCreateTime() {
		return endCreateTime;
	}

	public void setEndCreateTime(Date endCreateTime) {
		this.endCreateTime = endCreateTime;
	}

	public String getCallTypeDesc() {
		return callTypeDesc;
	}

	public void setCallTypeDesc(String callTypeDesc) {
		this.callTypeDesc = callTypeDesc;
	}

	public String getRecordFile() {
		return recordFile;
	}

	public void setRecordFile(String recordFile) {
		this.recordFile = recordFile;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}