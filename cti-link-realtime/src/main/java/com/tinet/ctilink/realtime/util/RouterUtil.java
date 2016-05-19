package com.tinet.ctilink.realtime.util;

import com.tinet.ctilink.cache.CacheKey;
import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.realtime.entity.Caller;
import com.tinet.ctilink.inc.Const;
import com.tinet.ctilink.conf.model.EnterpriseRouter;
import com.tinet.ctilink.conf.model.Gateway;
import com.tinet.ctilink.conf.model.Router;
import com.tinet.ctilink.util.ContextUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author fengwei //
 * @date 16/4/22 10:18
 */
@Component
public class RouterUtil {

    public static Router getRouter (int enterpriseId, int routerClidCallType, Caller caller) {

        EnterpriseRouter enterpriseRouter = ContextUtil.getBean(RedisService.class).get(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ENTERPRISE_ROUTER_ENTERPRISE_ID, enterpriseId)
                , EnterpriseRouter.class);
        Router router = null;
        if(enterpriseRouter == null){
            return null;
        }
        int routersetId;
        switch(routerClidCallType){
            case Const.ROUTER_CLID_CALL_TYPE_IB_RIGHT:
                routersetId = enterpriseRouter.getIbRouterRight();
                break;
            case Const.ROUTER_CLID_CALL_TYPE_OB_LEFT:
                routersetId = enterpriseRouter.getObRouterLeft();
                break;
            case Const.ROUTER_CLID_CALL_TYPE_OB_RIGHT:
                routersetId = enterpriseRouter.getObRouterRight();
                break;
            default:
                routersetId = enterpriseRouter.getObRouterLeft();
        }
        router = getRouter(routersetId, caller);

        return router;
    }

    public static Router getRouter(int routersetId, Caller caller) {
        Router router = null;

        String routerTel = caller.getCallerNumber();
        if (caller.getTelType() == Const.TEL_TYPE_MOBILE) {
            routerTel = caller.getAreaCode() + caller.getCallerNumber();
        }
        List<Router> routerList = ContextUtil.getBean(RedisService.class)
                .getList(Const.REDIS_DB_CONF_INDEX, String.format(CacheKey.ROUTER_ROUTERSET_ID, routersetId), Router.class);
        //找prefix
        for (Router r : routerList) {
            if (routerTel.startsWith(r.getPrefix())) {
                if (router == null) {
                    router = r;
                } else {
                    //最长匹配
                    if (r.getPrefix().length() > router.getPrefix().length()) {
                        router = r;
                    } else if (r.getPrefix().equals(router.getPrefix())
                            && r.getPriority() > router.getPriority()) {
                        router = r;
                    }
                }
            }
        }

        return router;
    }

    public static Gateway getRouterGateway(int enterpriseId, int routerClidCallType, Caller caller) {
        Router router = getRouter(enterpriseId, routerClidCallType, caller);

        if (router != null) {
            List<Gateway> gatewayList = ContextUtil.getBean(RedisService.class)
                    .getList(Const.REDIS_DB_CONF_INDEX, CacheKey.GATEWAY, Gateway.class);
            for (Gateway gw : gatewayList) {
                if (gw.getId().equals(router.getGatewayId())) {
                    if (caller.getTelType() == Const.TEL_TYPE_MOBILE) {
                        gw.setPrefix(gw.getPrefix() + caller.getAreaCode());
                    }
                    return gw;
                }
            }
        }
        return null;
    }
}
