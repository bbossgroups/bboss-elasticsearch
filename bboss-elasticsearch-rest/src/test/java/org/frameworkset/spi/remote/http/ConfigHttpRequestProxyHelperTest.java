package org.frameworkset.spi.remote.http;
/**
 * Copyright 2026 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.frameworkset.spi.feishu.FeishuHelper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author biaoping.yin
 * @Date 2026/5/2
 */
public class ConfigHttpRequestProxyHelperTest {
    private static Logger logger = LoggerFactory.getLogger(ConfigHttpRequestProxyHelperTest.class);
    /**
     * 初始化一个http微服务客户端，名称为：feishu
     * startPool方法在单个进程中只要执行一次即可
     */
    public static void initFeishu(){
//		HttpRequestProxy.startHttpPools("application.properties");
        /**
         * 1.服务健康检查
         * 2.服务负载均衡
         * 3.服务容灾故障恢复
         * 4.服务自动发现（nacos,apollo，zk，etcd，consul，eureka，db，其他第三方注册中心）
         * 配置了两个连接池：default,report
         * 本示例演示基于apollo提供配置管理、服务自动发现以及灰度/生产，主备切换功能
         */
        Map<String,Object> configs = new HashMap<String,Object>();
        configs.put("http.poolNames","feishu");

        //如果指定hosts那么就会采用配置的地址作为初始化地址清单
        configs.put("feishu.http.hosts","https://open.feishu.cn");
        configs.put("feishu.http.maxTotal",100);
        configs.put("feishu.http.defaultMaxPerRoute",100);
        configs.put("feishu.http.authorTokenFunction","org.frameworkset.spi.feishu.FeishuAuthorTokenFunction");
//# 25分钟自动刷新token
//# tenant_access_token 的最大有效期是 2 小时。
//# 剩余有效期小于 30 分钟时，调用本接口会返回一个新的 tenant_access_token，这会同时存在两个有效的 tenant_access_token。
//# 剩余有效期大于等于 30 分钟时，调用本接口会返回原有的 tenant_access_token
        configs.put("feishu.http.authorTokenExpiredTime", 25*60*1000L);
        configs.put("feishu.http.extendConfigs.appId", "cli_a90feb5b89bc2");
        configs.put("feishu.http.extendConfigs.appSecret", "RNhMgsTgV5tmK21J6Q5LPtGeKZIsB");

        configs.put("feishu.http.showDsl",false);
        HttpRequestProxy.startHttpPools(configs);

    }
    public static void main(String[] args){
        initFeishu();
        ConfigHttpRequestProxy client = ConfigHttpRequestProxyHelper.getHttpConfigClientProxy("feishu","feishudsl.xml");
        Map params = new HashMap();
        params.put("发送时间戳",0);
        params.put("发送时间戳__endTime",2222222222222222222L);

        String data  = client.sendJsonBody(FeishuHelper.buildSearchUrl("N0tMboDHOaSWAwsXh0ucIoARnnc","tblCzBSEvUXKYMTI",100),"requestBody",params);
        logger.info(data);
    }

}
