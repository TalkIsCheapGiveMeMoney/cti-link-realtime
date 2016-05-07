package com.tinet.ctilink.realtime.entity;

import java.io.Serializable;

/**
 * Radius Server辅助类，实现主叫信息的获取及设置.
 * <p>
 * 每路通话对应一条话单，同时对应一个Caller对象
 * <p>
 * 文件名： Caller.java
 * <p>
 * Copyright (c) 2006-2010 T&I Net Communication CO.,LTD. All rights reserved.
 * 
 * @author 安静波
 * @since 1.0
 * @version 1.0
 */

public class Caller implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4916722887722966354L;
	private String callerNumber = "";
	private Integer telType = 0;
	private String areaCode = "";
	private String province = "";
	private String city = "";

	public Caller() {
	}
	public Caller(String number) {
		this.callerNumber = number;
	}

	public String getCallerNumber() {
		return callerNumber;
	}

	public void setCallerNumber(String callerNumber) {
		this.callerNumber = callerNumber;
	}

	public Integer getTelType() {
		return telType;
	}

	public void setTelType(Integer telType) {
		this.telType = telType;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRealNumber()
	{
		if(callerNumber.startsWith(areaCode)){
			return callerNumber.substring(areaCode.length());
		}else{
			return callerNumber;
		}
	}
}
