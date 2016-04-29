package com.tinet.ctilink.realtime.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.conf.model.EnterpriseClid;
import com.tinet.ctilink.conf.model.EnterpriseHotline;
import com.tinet.ctilink.conf.model.Trunk;
import com.tinet.ctilink.util.ContextUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author fengwei //
 * @date 16/4/22 10:56
 */
public class ClidUtil {

    public static String getClid (int enterpriseId, int routerClidCallType, String customerNumber, StringBuilder clidBack) {
        RedisService redisService = ContextUtil.getContext().getBean(RedisService.class);
        EnterpriseClid enterpriseClid = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_CLID_ENTERPRISE_ID, enterpriseId)
                , EnterpriseClid.class);

        String resClid = "";
        int clidType = 0;
        String clidNumber = "";
        if(enterpriseClid != null){
            switch(routerClidCallType){
                case Const.ROUTER_CLID_CALL_TYPE_IB_RIGHT://1呼入
                    clidType = enterpriseClid.getIbClidRightType();
                    clidNumber = enterpriseClid.getIbClidRightNumber();

                    if(clidType == 2){//外显客户号码
                        if(customerNumber.equals(Const.UNKNOWN_NUMBER)){
                            Trunk trunk = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TRUNK_ENTERPRISE_ID_FIRST, enterpriseId), Trunk.class);
                            if(null != trunk){
                                resClid = trunk.getAreaCode() + trunk.getNumberTrunk();
                            }
                        }else{
                            resClid = customerNumber;
                        }
                    }else if(clidType == 3){//外显固定号码
                        if(StringUtils.isNotEmpty(clidNumber)){
                            String sp="other";
                            if (Pattern.compile(Const.PATTERN_MOBILE_WITHOUT_PREFIX0).matcher(customerNumber).find()) {
                                sp = AreaCodeUtil.getSP(customerNumber);
                            }
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root;
                            try {
                                root = mapper.readTree(clidNumber);
                                JsonNode spObject = root.get(sp);
                                JsonNode mainArray = spObject.get("main");
                                if(mainArray != null && mainArray.size() > 0){
                                    Random r=new Random(); //实例化一个Random类
                                    //随机产生一个整数
                                    resClid = mainArray.get(r.nextInt(mainArray.size())).asText();
                                }else{
                                    Trunk trunk = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TRUNK_ENTERPRISE_ID_FIRST, enterpriseId), Trunk.class);
                                    if(null != trunk){
                                        resClid = trunk.getAreaCode() + trunk.getNumberTrunk();
                                    }
                                }
                                JsonNode secondaryArray = spObject.get("secondary");

                                if(secondaryArray != null && secondaryArray.size() > 0){
                                    Random r=new Random(); //实例化一个Random类
                                    //随机产生一个整数
                                    clidBack.append(secondaryArray.get(r.nextInt(secondaryArray.size())).asText());
                                }else{
                                    Trunk trunk = redisService.get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.TRUNK_ENTERPRISE_ID_FIRST, enterpriseId), Trunk.class);
                                    if(null != trunk){
                                        clidBack.append(trunk.getAreaCode() + trunk.getNumberTrunk());
                                    }else{
                                        clidBack.append("");
                                    }
                                }
                            } catch (JsonProcessingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
                    break;
                case Const.ROUTER_CLID_CALL_TYPE_OB_LEFT://2预览外呼客户侧*
                    clidType = enterpriseClid.getObClidLeftType();
                    clidNumber = enterpriseClid.getObClidLeftNumber();
                    if(clidType == 3){//外显固定号码
                        if(StringUtils.isNotEmpty(clidNumber)){
                            String sp="other";
                            if (Pattern.compile(Const.PATTERN_MOBILE_WITHOUT_PREFIX0).matcher(customerNumber).find()) {
                                sp = AreaCodeUtil.getSP(customerNumber);
                            }

                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root;
                            try {
                                root = mapper.readTree(clidNumber);
                                JsonNode spObject = root.get(sp);
                                JsonNode mainArray = spObject.get("main");
                                if(mainArray != null && mainArray.size() > 0){
                                    Random r=new Random(); //实例化一个Random类
                                    //随机产生一个整数
                                    resClid = mainArray.get(r.nextInt(mainArray.size())).asText();
                                }

                                JsonNode secondaryArray = spObject.get("secondary");
                                if(secondaryArray != null && secondaryArray.size() > 0){
                                    Random r=new Random(); //实例化一个Random类
                                    //随机产生一个整数
                                    clidBack.append(secondaryArray.get(r.nextInt(secondaryArray.size())).asText());
                                }
                            } catch (JsonProcessingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
                    break;
                case Const.ROUTER_CLID_CALL_TYPE_OB_RIGHT://3预览外呼座席侧
                    clidType = enterpriseClid.getObClidRightType();
                    clidNumber = enterpriseClid.getObClidRightNumber();

                    if(clidType == 2){//外显客户号码
                        resClid = customerNumber;
                    }else if(clidType == 3){//外显固定号码
                        if(StringUtils.isNotEmpty(clidNumber)){
                            String sp="other";
                            if (Pattern.compile(Const.PATTERN_MOBILE_WITHOUT_PREFIX0).matcher(customerNumber).find()) {
                                sp = AreaCodeUtil.getSP(customerNumber);
                            }

                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode root;
                            try {
                                root = mapper.readTree(clidNumber);
                                JsonNode spObject = root.get(sp);
                                JsonNode mainArray = spObject.get("main");
                                if(mainArray != null && mainArray.size() > 0){
                                    Random r=new Random(); //实例化一个Random类
                                    //随机产生一个整数
                                    resClid = mainArray.get(r.nextInt(mainArray.size())).asText();
                                }

                                JsonNode secondaryArray = spObject.get("secondary");
                                if(secondaryArray != null && secondaryArray.size() > 0){
                                    Random r=new Random(); //实例化一个Random类
                                    //随机产生一个整数
                                    clidBack.append(secondaryArray.get(r.nextInt(secondaryArray.size())).asText());
                                }
                            } catch (JsonProcessingException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }

                        }
                    }
                    break;
            }
        }
        return resClid;
    }
}
