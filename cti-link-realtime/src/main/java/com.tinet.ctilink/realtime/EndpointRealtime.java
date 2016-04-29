package com.tinet.ctilink.realtime;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.inc.Const;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 获取路由信息
 * <p/>
 * 文件名： EndpointRealtime.java
 * <p/>
 * Copyright (c) 2006-2010 T&I Net Communication CO.,LTD.  All rights reserved.
 *
 * @author 安静波
 * @version 1.0
 * @since 1.0
 */
@Component
public class EndpointRealtime {
    private final Logger logger = LoggerFactory.getLogger(EndpointRealtime.class.getName());

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
                        return this.dataRes(gateway, true);
                    }
                }
            } else {
                List<Gateway> gatewayList = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
                Gateway gateway = null;
                for (Gateway g : gatewayList) {
                    if (g.getId().toString().equals(id)) {
                        gateway = g;
                        break;
                    }
                }
                return this.dataRes(gateway, false);
            }
        } else {
            List<Gateway> gatewayList = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
            String resStr = "";
            for (Gateway gateway : gatewayList) {
                resStr = resStr + this.dataRes(gateway, false);
            }
            return resStr;
        }
        return "";
    }

    public String dataRes(Gateway gateway, boolean nameIsIp) {
        StringBuffer res = new StringBuffer();
        if (gateway != null) {
            res.append("id=");
            res.append(gateway.getName());
            res.append("&type=endpoint");
            res.append("&transport=transport-udp-nat");
            res.append("&context=default");
            res.append("&disallow=");
            res.append(gateway.getDisallow());
            res.append("&allow=");
            res.append(gateway.getAllow());
            res.append("&dtmf_mode=rfc4733");
//			res.append(gateway.getDtmfMode());
            res.append("&device_state_busy_at=");
            res.append(gateway.getCallLimit());
            res.append("\n");
        }

        return res.toString();
    }
}
