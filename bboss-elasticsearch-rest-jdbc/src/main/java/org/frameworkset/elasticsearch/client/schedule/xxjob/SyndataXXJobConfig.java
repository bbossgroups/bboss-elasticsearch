package org.frameworkset.elasticsearch.client.schedule.xxjob;

import com.xxl.job.core.executor.XxlJobExecutor;
import org.frameworkset.spi.assemble.PropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SyndataXXJobConfig {
    private static Logger logger = LoggerFactory.getLogger(SyndataXXJobConfig.class);


    private static SyndataXXJobConfig instance = new SyndataXXJobConfig();
    public static SyndataXXJobConfig getInstance() {
        return instance;
    }


    private XxlJobExecutor xxlJobExecutor = null;

    /**
     * init
     */
    public void initXxlJobExecutor() {
        PropertiesContainer propertiesContainer = new PropertiesContainer();
        propertiesContainer.addConfigPropertiesFile("application.properties");

        Map<Object, Object>  objectMap =propertiesContainer.getAllProperties();
        if(objectMap != null) {
            // registry jobhandler
            Set<Map.Entry<Object, Object>> entrySet = objectMap.entrySet();
            Iterator<Map.Entry<Object, Object>>  iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<Object, Object> entry = iterator.next();
                String name = (String)entry.getKey();
                String orineName = name;
                if(name.startsWith("xxl.job.task.")) {
                    name = name.substring("xxl.job.task.".length()).trim();
                    String value = (String)entry.getValue();
                    String orignValue = value;
                    if(value != null ) {
                        value = value.trim();
                        if(!value.equals("")) {
                            try {
                                AbstractDB2ESXXJobHandler abstractDB2ESXXJobHandler = (AbstractDB2ESXXJobHandler)Class.forName(value).newInstance();
                                XxlJobExecutor.registJobHandler(name, abstractDB2ESXXJobHandler);
                            }
                            catch (Exception e){
                                logger.error(new StringBuilder().append("registJobHandler [").append(orineName).append("=").append(orignValue).append("] failed:").toString(),e);
                            }

                        }
                    }
                }
            }
        }


        // load executor prop

//        Properties xxlJobProp = loadProperties("xxl-job-executor.properties");


        // init executor
        xxlJobExecutor = new XxlJobExecutor();
        xxlJobExecutor.setAdminAddresses(propertiesContainer.getProperty("xxl.job.admin.addresses"));
        xxlJobExecutor.setAppName(propertiesContainer.getProperty("xxl.job.executor.appname"));
        xxlJobExecutor.setIp(propertiesContainer.getProperty("xxl.job.executor.ip"));
        xxlJobExecutor.setPort(Integer.valueOf(propertiesContainer.getProperty("xxl.job.executor.port")));
        xxlJobExecutor.setAccessToken(propertiesContainer.getProperty("xxl.job.accessToken"));
        xxlJobExecutor.setLogPath(propertiesContainer.getProperty("xxl.job.executor.logpath"));
        xxlJobExecutor.setLogRetentionDays(Integer.valueOf(propertiesContainer.getProperty("xxl.job.executor.logretentiondays")));

        // start executor
        try {
            xxlJobExecutor.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * destory
     */
    public void destoryXxlJobExecutor() {
        if (xxlJobExecutor != null) {
            xxlJobExecutor.destroy();
        }
    }



}
