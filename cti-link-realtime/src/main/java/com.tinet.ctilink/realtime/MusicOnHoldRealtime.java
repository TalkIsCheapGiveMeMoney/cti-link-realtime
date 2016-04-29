package com.tinet.ctilink.realtime;

import javax.servlet.http.HttpServletRequest;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.conf.model.EnterpriseMoh;
import com.tinet.ctilink.conf.model.PublicMoh;
import com.tinet.ctilink.inc.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 查询music_on_hold表返回相应字段供CTI模块调用
 * <p/>
 * 文件名： MusicOnHoldRealtime.java
 * <p/>
 * Copyright (c) 2006-2010 T&I Net Communication CO.,LTD. All rights reserved.
 *
 * @author 安静波
 * @version 1.0
 * @since 1.0
 */
@Component
public class MusicOnHoldRealtime {
    private static final Logger logger = LoggerFactory.getLogger(MusicOnHoldRealtime.class);
    public static Integer accessCount = 0;
    public static long maxTime = 0;
    public static long totalTime = 0;
    public static long curTime = 0;

    @Autowired
    private RedisService redisService;

    public String queryByHttpServletRequest(HttpServletRequest request) {

        String nameValue = request.getParameter("name");
        logger.debug("request:name=" + nameValue);

        StringBuffer res = new StringBuffer();

        PublicMoh publicMoh = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.PUBLIC_MOH_NAME, nameValue), PublicMoh.class);
        if (publicMoh != null) {
            res.append("name=");
            res.append(publicMoh.getName());
            res.append("&mode=");
            res.append(publicMoh.getMode());
            res.append("&directory=");
            res.append(publicMoh.getDirectory());
            /* all the fields below are reserved.
			res.append("&application=");
			res.append(musicOnHold.getApplication());
			res.append("&digit=");
			res.append(musicOnHold.getDigit());
			res.append("&sort=");
			res.append(musicOnHold.getSort());
			res.append("&format=");
			res.append(musicOnHold.getFormat());
			*/
            res.append("\n");
        } else {
            EnterpriseMoh enterpriseMoh = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_MOH_NAME, nameValue), EnterpriseMoh.class);
            if (enterpriseMoh != null) {
                res.append("name=");
                res.append(enterpriseMoh.getName());
                res.append("&mode=");
                res.append(enterpriseMoh.getMode());
                res.append("&directory=");
                res.append(enterpriseMoh.getDirectory());
				/* all the fields below are reserved.
				res.append("&application=");
				res.append(musicOnHold.getApplication());
				res.append("&digit=");
				res.append(musicOnHold.getDigit());
				res.append("&sort=");
				res.append(musicOnHold.getSort());
				res.append("&format=");
				res.append(musicOnHold.getFormat());
				*/
                res.append("\n");
            }
        }

        logger.debug("response:" + res);

        return res.toString();
    }

}
