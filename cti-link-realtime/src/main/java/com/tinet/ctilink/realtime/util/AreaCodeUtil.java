package com.tinet.ctilink.realtime.util;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.realtime.entity.Caller;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.conf.model.AreaCode;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.conf.model.SystemSetting;
import com.tinet.ctilink.util.ContextUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author fengwei //
 * @date 16/4/22 10:02
 */
@Component
public class AreaCodeUtil {

    public static Caller updateGetAreaCode(String number, String gateway) {
        RedisService redisService = ContextUtil.getBean(RedisService.class);
        Caller caller = new Caller(number);
        caller.setTelType(Const.TEL_TYPE_LANDLINE);

        if (Pattern.compile("^010").matcher(number).find()
                || Pattern.compile("^02").matcher(number).find()) {
            caller.setAreaCode(number.substring(0, 3));
            caller.setCallerNumber(number);
            caller.setTelType(Const.TEL_TYPE_LANDLINE);
        } else if (Pattern.compile("^0[3-9]").matcher(number).find()) {
            caller.setAreaCode(number.substring(0, 4));
            caller.setCallerNumber(number);
            caller.setTelType(Const.TEL_TYPE_LANDLINE);
        } else if (Pattern.compile("^00").matcher(number).find()) {
            caller.setAreaCode(number.substring(0, 2));
            caller.setCallerNumber(number);
            caller.setTelType(Const.TEL_TYPE_LANDLINE);
        } else if (Pattern.compile(Const.PATTERN_MOBILE_WITH_PREFIX0).matcher(number).find()) {
            AreaCode areaCode = redisService.get(Const.REDIS_DB_AREA_CODE_INDEX, String.format(CacheKey.AREA_CODE_PREFIX, number.substring(2, 7)),
                    AreaCode.class);
            if (areaCode != null) {
                caller.setAreaCode(areaCode.getAreaCode());
            }else{
                String numberPrefix = number.substring(1, 8);
                String areaCodeFromIp138 = getAreaCodeFromIp138(numberPrefix);
                if(StringUtils.isNotEmpty(areaCodeFromIp138)){
                    //TODO 从ip138获取的信息 存入缓存和库
//                    List<AreaCode> areaCodeList = (List<AreaCode>) areaCodeDao.findByHqlCache("from AreaCode where prefix='" + areaCode + "'");
//                    String city = "";
//                    String province = "";
//                    if(areaCodeList.size() > 0){
//                        city = areaCodeList.get(0).getCity();
//                        province = areaCodeList.get(0).getProvince();
//                    }
//                    this.saveNewAreaCode(numberPrefix, areaCode, province, city);
                    caller.setAreaCode(areaCodeFromIp138);
                }
            }
            caller.setCallerNumber(number.substring(1));
            caller.setTelType(Const.TEL_TYPE_MOBILE);
        } else if (Pattern.compile(Const.PATTERN_MOBILE_WITHOUT_PREFIX0).matcher(number).find()) {
            AreaCode areaCode = redisService.get(Const.REDIS_DB_AREA_CODE_INDEX, String.format(CacheKey.AREA_CODE_PREFIX, number.substring(1, 7)),
                    AreaCode.class);
            if (areaCode != null) {
                caller.setAreaCode(areaCode.getAreaCode());
            }else{
                String numberPrefix = number.substring(0, 7);
                String areaCodeFromIp138 = getAreaCodeFromIp138(numberPrefix);
                if(StringUtils.isNotEmpty(areaCodeFromIp138)){
//                    AreaCodeDao areaCodeDao = (AreaCodeDao) ContextUtil.getContext().getBean("areaCodeDao");
//                    List<AreaCode> areaCodeList = (List<AreaCode>) areaCodeDao.findByHqlCache("from AreaCode where prefix='" + areaCode + "'");
//                    String city = "";
//                    String province = "";
//                    if(areaCodeList.size() > 0){
//                        city = areaCodeList.get(0).getCity();
//                        province = areaCodeList.get(0).getProvince();
//                    }
//                    this.saveNewAreaCode(numberPrefix, areaCode, province, city);
                    caller.setAreaCode(areaCodeFromIp138);
                }
            }
            caller.setCallerNumber(number);
            caller.setTelType(Const.TEL_TYPE_MOBILE);
        }else if(Pattern.compile(Const.PATTERN_NUMBER_400).matcher(number).find()) {
            caller.setAreaCode("400");
            caller.setCallerNumber(number);
            caller.setTelType(Const.TEL_TYPE_LANDLINE);
        }else if (!StringUtils.isNumeric(number)) {
            caller.setAreaCode("");
            caller.setCallerNumber(Const.UNKNOWN_NUMBER);
        } else {
            if(number.length() >=3 ){//大于3表示没有区号的固话，给一个区号，或者95555/10086/10010/110/112/999等号码都认为是本地固话
                if(!StringUtils.isEmpty(gateway)){
                    boolean find = false;
                    List<Gateway> gatewayList = redisService.getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
                    for(Gateway gw : gatewayList){
                        if(gw.getIpAddr().equals(gateway)){
                            caller.setAreaCode(gw.getAreaCode());
                            caller.setCallerNumber(caller.getAreaCode() + caller.getCallerNumber());
                            find = true;
                            break;
                        }
                    }
                    if(!find){
                        SystemSetting systemSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME
                                , Const.SYSTEM_SETTING_NAME_DEFAULT_AREA_CODE), SystemSetting.class);
                        if (systemSetting != null) {
                            caller.setAreaCode(systemSetting.getValue());
                            caller.setCallerNumber(caller.getAreaCode() + caller.getCallerNumber());
                        }
                    }
                }
            }else{//分机号


            }
        }
        if ("".equals(caller.getAreaCode())) {
            caller.setProvince(Const.UNKNOWN_AREA);
            caller.setCity(Const.UNKNOWN_AREA);
        } else {
            AreaCode areaCode = redisService.get(Const.REDIS_DB_AREA_CODE_INDEX, String.format(CacheKey.AREA_CODE_PREFIX, caller.getAreaCode()),
                    AreaCode.class);
            if (areaCode != null) {
                caller.setProvince(areaCode.getProvince());
                caller.setCity(areaCode.getCity());
            }
        }
        return caller;
    }

    /**
     * 获取电话号码前缀
     * @param tel 电话号
     * @return 此号码对应的区号
     */
    public static String getPrefix(String tel){
        if (Pattern.compile("^01").matcher(tel).find() || Pattern.compile("^02").matcher(tel).find()) {
            return tel.substring(0, 3);
        } else if (Pattern.compile("^0[3-9]").matcher(tel).find()) {
            return tel.substring(0, 4);
        } else if (Pattern.compile("^00").matcher(tel).find()) {
            return tel.substring(0, 2);
        }else{
            return tel.substring(0, 7);
        }
    }


    public static String getAreaCodeFromIp138(String prefix){
        String areaCode = "";
        String ip138Url = "http://www.ip138.com:8080/search.asp";

        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(ip138Url + "?action=mobile"
                    + "&mobile=" + prefix);
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(2000).build();
            httpget.setConfig(requestConfig);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            String text = EntityUtils.toString(entity, "gb2312");

            if (text != null && !text.equals("")) {
                text = text.substring(text.indexOf("区 号"));
                text = text.substring(text.indexOf("class"));
                text = text.substring(text.indexOf(">") + 1, text.indexOf("<"));
                areaCode = text;
                //areaCode是非数字, 设置为空
                if (StringUtils.isNotEmpty(areaCode) && !StringUtils.isNumeric(areaCode.trim())) {
                    areaCode = "";
                }
            }
        } catch (Exception e) {
            System.out.println("从 ip138.com 获取区号错误, 号段prefix- " + prefix);
            e.printStackTrace();
        }
        return areaCode;
    }

    public static String getSP(String mobile){
        if(mobile != null && mobile.length() == 11) {
            RedisService redisService = ContextUtil.getBean(RedisService.class);
            String prefix = mobile.substring(0, 3);
            SystemSetting  mobileSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME
                    , Const.SYSTEM_SETTING_NAME_MOBILE_SEGMENT), SystemSetting.class);
            String[] mobileList = mobileSetting.getValue().split(",");
            for(String segment: mobileList){
                if(segment.equals(prefix)){
                    return "mobile";
                }
            }
            SystemSetting  unicomSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME
                    , Const.SYSTEM_SETTING_NAME_UNICOM_SEGMENT), SystemSetting.class);
            String[] unicomList = unicomSetting.getValue().split(",");
            for(String segment: unicomList){
                if(segment.equals(prefix)){
                    return "unicom";
                }
            }
            SystemSetting  telecomSetting = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.SYSTEM_SETTING_NAME
                    , Const.SYSTEM_SETTING_NAME_TELECOM_SEGMENT), SystemSetting.class);
            String[] telecomList = telecomSetting.getValue().split(",");
            for(String segment: telecomList){
                if(segment.equals(prefix)){
                    return "telecom";
                }
            }
        }
        return "mobile";
    }
}
