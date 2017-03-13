package com.tanghub.config.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <br>
 *
 * @author tang
 * @version v1.0 2017.03.07
 */
@Service
public class ConfigService {

    @Value("${common_name}")
    private String c_name; // /config/application  通用配置可获取

    @Value("${zk_name}")
    private String zk_name;// /config/<spring.application.name>  当前app可获取
    @Autowired
    private ZkConfigService cfgService;

    //@Value("${other_name}")
    private String other_name;// /config/<other_app_name> 其他app不可获取

    public String getC_name() {
        return c_name;
    }

    public String getZk_name() {
        return zk_name;
    }

    public String getOther_name() {
        return other_name;
    }

    public String getProp() {
        return cfgService.getCfgProp("app.test");
    }
}
