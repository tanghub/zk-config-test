package com.tanghub.config.controller;

import com.tanghub.config.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <br>
 *
 * @author tang
 * @version v1.0 2017.03.07
 */
@RestController
public class ConfigTestController {
    @Autowired
    private ConfigService configService;

    @RequestMapping(value = "/getC", method = RequestMethod.GET)
    public String getCname() {
        return configService.getC_name();
    }
    @RequestMapping("/getApp")
    public String getAppname() {
        return configService.getZk_name();
    }
    @RequestMapping("/getOther")
    public String getOtherAppname() {
        return configService.getOther_name();
    }
    @RequestMapping("/getProp")
    public String getProp() {
        return configService.getProp();
    }
}
