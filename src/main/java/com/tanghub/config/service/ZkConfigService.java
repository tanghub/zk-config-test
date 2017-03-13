package com.tanghub.config.service;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * <br>
 *
 * @author tang
 * @version v1.0 2017.03.09
 */
@Component
public class ZkConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    CuratorFramework client;

    @Value("${spring.application.name}")
    String appName;

    private Map<String, String> appCfgMap = new HashMap<>();

    @PostConstruct
    public void registerListener() {

        String commonPath = "/config/application";
        watchPathTree(client, commonPath);

        String appPath = "/config/" + appName;
        getZkConfigMap(appPath);
        watchPathTree(client, appPath);

        appCfgMap = genAppMap();
    }

    private void watchPathTree(CuratorFramework client, String path) {
        TreeCache cache = new TreeCache(client, path);
        try {
            cache.start();
            TreeCacheListener listener = (cfClient, event) -> {
                ChildData childData = event.getData();
                if (childData == null) {
                    return;
                }

                switch (event.getType()) {
                    case NODE_ADDED: {
                        onZKChange(path);
                        break;
                    }
                    case NODE_UPDATED: {
                        onZKChange(path);
                        break;
                    }
                    case NODE_REMOVED: {
                        onZKChange(path);
                        break;
                    }
                    default:
                        //do nothing
                }
            };

            cache.getListenable().addListener(listener);
        } catch (Exception e) {
            log.error("监控节点[{}]异常", path, e);
        }
    }

    private void onZKChange(String path) {
        appCfgMap.putAll(getZkConfigMap(path));
    }


    private Map<String, String> getZkConfigMap(String path) {
        Map<String, String> cfgMap = new HashMap<>();
        try {
            List<String> childList = client.getChildren().forPath(path);
            for (String child : childList) {
                cfgMap.put(child, new String(client.getData().forPath(path + "/" + child), "utf-8"));
            }
        } catch (Exception e) {
            log.error("获取[{}]下的子节点异常", path, e);
        }
        return cfgMap;
    }

    private Map<String, String> loadLocalCfg() {
        Map<String, String> localMap = new HashMap<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        try {
            yaml.setResources(resolver.getResources("classpath*:/config/app_*.yml"));
        } catch (IOException e) {
            log.error("YML配置文件[{}]获取失败", e);
        }
        Properties ymlCfg = yaml.getObject();
        //PropertiesFactoryBean prop = new PropertiesFactoryBean();
        //prop.setLocations(new ClassPathResource("app_*.properties"));
        Properties propCfg = ymlCfg;//prop.getObject();
        Iterator iterator = null;
        Map.Entry<String, Object> entry = null;

        iterator = propCfg.entrySet().iterator();
        while (iterator.hasNext()) {
            entry = (Map.Entry) iterator.next();
            localMap.put(entry.getKey(), entry.getValue().toString());
        }

        return localMap;
    }

    private Map<String, String> genAppMap() {
        Map<String, String> cfgMap = getZkConfigMap("/config/" + appName);
        Map<String, String> localMap = loadLocalCfg();
        for (String key : localMap.keySet()) {
            if (!cfgMap.containsKey(key)) {

                try {
                    client.create().forPath("/config/" + appName + "/" + key, localMap.get(key).getBytes());
                } catch (Exception e) {
                    log.error("创建节点[{}]异常", key, e);
                }
                cfgMap.put(key, localMap.get(key));
            }
        }
        return cfgMap;
    }

    public String getCfgProp(String propKey) {
        return appCfgMap.get(propKey);
    }
}
