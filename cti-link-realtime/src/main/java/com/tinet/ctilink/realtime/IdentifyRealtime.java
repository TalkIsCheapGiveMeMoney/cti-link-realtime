package com.tinet.ctilink.realtime;


import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.conf.model.SipProxy;
import com.tinet.ctilink.inc.Const;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class IdentifyRealtime {
    private final Logger logger = LoggerFactory.getLogger(IdentifyRealtime.class.getName());

    public static Integer accessCount = 0;
    public static long maxTime = 0;
    public static long totalTime = 0;
    public static long curTime = 0;

    @Autowired
    private RedisService redisService;

    public String queryByHttpServletRequest(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (StringUtils.isNotEmpty(id)) {
            if (id.contains("@")) {
                String[] ids = id.split("@");
                if (ids.length == 2) {
                    String fromIp = ids[1];

                    List<Gateway> gatewayList = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
                    if (null != gatewayList && gatewayList.size() > 0) {
                        Gateway gateway = null;
                        for (Gateway g : gatewayList) {
                            if (g.getIpAddr().equals(fromIp)) {
                                if (gateway == null) {
                                    gateway = g;
                                    //取前缀最大的一条
                                } else if (g.getPrefix().compareTo(gateway.getPrefix()) > 0) {
                                    gateway = g;
                                }
                            }
                        }
                        // 取第一条
                        return this.dataRes(gateway);
                    }
                }
            }
        }else{
        	 List<SipProxy> sipProxyList = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.SIP_PROXY, SipProxy.class);
             String resStr = "";
             for (SipProxy sipProxy : sipProxyList) {
                 resStr = resStr + this.dataRes(sipProxy);
             }
             return resStr;
        }
        return "";
    }

    public String dataRes(SipProxy sipProxy) {
        StringBuffer res = new StringBuffer();
        if (sipProxy != null) {
            res.append("id=" + sipProxy.getName());
            res.append("&type=identify");
            res.append("&endpoint=" + sipProxy.getName());
            res.append("&match=" + sipProxy.getIpAddr());
            res.append("\n");
        }

        return res.toString();
    }
    public String dataRes(Gateway gateway) {
    	StringBuffer res = new StringBuffer();
        if (gateway != null) {
            res.append("id=" + gateway.getName());
            res.append("&type=identify");
            res.append("&endpoint=" + gateway.getName());
            res.append("&match=" + gateway.getIpAddr());
            res.append("\n");
        }

        return res.toString();
    }
}
