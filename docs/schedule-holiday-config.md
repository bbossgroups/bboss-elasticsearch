# 数据交换-工作流-智能体定时调度及节假日设置

## 1.数据交换作业配置

### 1.1 定时机制配置

bboss提供jdk timer、quartz以及xxl-job三种定时调度机制，以下是jdk timer的配置示例：

```java
//定时任务配置，
		importBuilder.setFixedRate(false)//参考jdk timer task文档对fixedRate的说明
//					 .setScheduleDate(date) //指定任务开始执行时间：日期
				.setDeyLay(1000L) // 任务延迟执行deylay毫秒后执行
				.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
		//定时任务配置结束
```

### 1.2 节假日配置

如果需要在节假日或者指定的日期忽略执行作业，则需按如下配置：

```java
//定时任务配置
        HolidayScheduleConfig holidayScheduleConfig = new HolidayScheduleConfig();

        holidayScheduleConfig.setFixedRate(false);//参考jdk timer task文档对fixedRate的说明
//               .setScheduleDate(date) //指定任务开始执行时间：日期
        holidayScheduleConfig.setDelay(1000L); // 任务延迟执行deylay毫秒后执行
        holidayScheduleConfig.setPeriod(5000L); //每隔period毫秒执行，如果不设置，只执行一次
        holidayScheduleConfig.addCustomHoliday("2026-06-10");
		holidayScheduleConfig.skipAllHolidays();
        importBuilder.setScheduleConfig(holidayScheduleConfig);
```

holidayScheduleConfig提供了以下API来灵活控制节假日和特定日期不执行：

```java
/**
 * 设置跳过星期六
 * @return
 */
public HolidayScheduleConfig skipSaturday()

/**
 * 设置跳过星期天
 * @return
 */
public HolidayScheduleConfig skipSunday()

/**
 * 设置跳过周末（星期六和星期天）
 * @return
 */
public HolidayScheduleConfig skipWeekends()

/**
 * 设置跳过元旦（1月1日）
 * @return
 */
public HolidayScheduleConfig skipNewYearsDay()

/**
 * 设置跳过劳动节（5月1日）
 * @return
 */
public HolidayScheduleConfig skipLaborDay()

/**
 * 设置跳过端午节
 * @return
 */
public HolidayScheduleConfig skipDragonBoatFestival()

/**
 * 设置跳过中秋节
 * @return
 */
public HolidayScheduleConfig skipMidAutumnFestival()

/**
 * 设置跳过国庆节（10月1日）
 * @return
 */
public HolidayScheduleConfig skipNationalDay()

/**
 * 设置跳过春节
 * @return
 */
public HolidayScheduleConfig skipSpringFestival()

/**
 * 设置跳过所有内置节假日（元旦、劳动节、端午节、中秋节、国庆节、春节）及周末
 * @return
 */
public HolidayScheduleConfig skipAllHolidays()

/**
 * 添加自定义节假日日期
 * @param holidayDate 格式：yyyy-MM-dd
 * @return
 */
public HolidayScheduleConfig addCustomHoliday(String holidayDate)
//添加多个日期    
public HolidayScheduleConfig setCustomHolidays(List<String> customHolidays)    
```

节假日配置案例：

https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/Db2DBHolidaySkipDemo.java

## 2.工作流调度配置

### 2.1 工作流调度策略设置

```java
       // 设置工作流调度策略
        JobFlowScheduleConfig jobFlowScheduleConfig = new JobFlowScheduleConfig();
//        jobFlowScheduleConfig.setScheduleDate(TimeUtil.addDateHours(new Date(),2));//2小时后开始执行
        jobFlowScheduleConfig.setScheduleDate(TimeUtil.addDateSeconds(new Date(),5));//5秒后开始执行
//        jobFlowScheduleConfig.setScheduleEndDate(TimeUtil.addDates(new Date(),10));//10天后结束
//        jobFlowScheduleConfig.setScheduleEndDate(TimeUtil.addDateMinitues(new Date(),10));//2分钟后结束
        jobFlowScheduleConfig.setPeriod(30000L);
//        jobFlowScheduleConfig.setExecuteOneTime(true);
//        jobFlowScheduleConfig.setExecuteOneTimeSyn(true);
        jobFlowBuilder.setJobFlowScheduleConfig(jobFlowScheduleConfig);
```

参考案例：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowTParrelxtZipFileDownload.java

### 2.2 节假日或者某天不执行的定时调度策略

```java
//定义节假日或者某天不执行的定时调度策略
HolidayJobFlowScheduleConfig jobFlowScheduleConfig = new HolidayJobFlowScheduleConfig();
jobFlowScheduleConfig.setDelay(1000L);
jobFlowScheduleConfig.setFixedRate(true);
jobFlowScheduleConfig.setPeriod(30000L);
jobFlowScheduleConfig.addCustomHoliday("2026-06-10");//设定某天不执行，可以调用多次添加多天不执行
jobFlowScheduleConfig.setSkipSunday(true);
jobFlowScheduleConfig.setSkipSaturday(true);

//设置工作流定时调度策略
      jobFlowBuilder.setJobFlowScheduleConfig(jobFlowScheduleConfig);
```

参考案例：https://gitee.com/bboss/bboss-datatran-demo/blob/main/src/main/java/org/frameworkset/datatran/imp/jobflow/JobFlowTParrelxtZipFileHolidayScheduleDownload.java

## 3.智能体工作流调度配置

智能体工作流同样支持一般定时调度和节假日忽略调度设置，参考文档：https://esdoc.bbossgroups.com/#/bboss-ai?id=_1412-%e6%b5%81%e7%a8%8b%e8%b0%83%e5%ba%a6