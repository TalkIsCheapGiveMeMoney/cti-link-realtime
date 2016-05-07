package com.tinet.ctilink.realtime.entity;

import java.util.Date;

/**
* TTS日志表
*<p>
* 文件名：LogTts.java
*<p>
* Copyright (c) 2006-2014 T&I Net Communication CO.,LTD.  All rights reserved.
* @author MyEclipse Persistence Tools
* @since 1.0
* @version 1.0
*/
public class LogTts implements java.io.Serializable {

	private Integer enterpriseId;
	private String uniqueId;
	private String text;
	private Integer hitCache;
	private Date requestTime;
	private Date startTime;
	private Date endTime;
	private Integer result;
	private Date createTime;
	private Integer callFrom;

	// Constructors

	/** default constructor */
	public LogTts() {
		this.createTime = new Date();
	}

	public Integer getEnterpriseId() {
		return enterpriseId;
	}

	public void setEnterpriseId(Integer enterpriseId) {
		this.enterpriseId = enterpriseId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getHitCache() {
		return hitCache;
	}

	public void setHitCache(Integer hitCache) {
		this.hitCache = hitCache;
	}

	public Date getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getCallFrom() {
		return callFrom;
	}

	public void setCallFrom(Integer callFrom) {
		this.callFrom = callFrom;
	}
}