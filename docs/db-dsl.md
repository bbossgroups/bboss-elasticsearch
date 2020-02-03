# 基于数据库管理dsl介绍

# 1.ConfigRestClientUtil和dsl结构及关系

ConfigRestClientUtil和dsl结构及关系说明如下：

![](images\db-dsl1.png)



# 2.基于数据库管理的dsl创建ClientInterface

```java
//创建一个从数据库加载命名空间为testnamespace的所有dsl语句的ClientInterface组件实例
		ClientInterface clientInterface = ElasticSearchHelper.getConfigRestClientUtil(new BaseTemplateContainerImpl("testnamespace") {
			@Override
			protected Map<String, TemplateMeta> loadTemplateMetas(String namespace) {
				try {
					List<BaseTemplateMeta> templateMetas = SQLExecutor.queryListWithDBName(BaseTemplateMeta.class,
																	"testdslconfig","select * from dslconfig where namespace = ?",namespace);
					if(templateMetas == null){
						return null;
					}
					else{
						Map<String,TemplateMeta> templateMetaMap = new HashMap<String, TemplateMeta>(templateMetas.size());
						for(BaseTemplateMeta baseTemplateMeta: templateMetas){
							templateMetaMap.put(baseTemplateMeta.getName(),baseTemplateMeta);
						}
						return templateMetaMap;
					}
				} catch (Exception e) {
					throw new DSLParserException(e);
				}
			}

			@Override
			protected long getLastModifyTime(String namespace) {
				// 获取dsl更新时间戳：模拟每次都更新，返回当前时间戳
				// 如果检测到时间戳有变化，框架就将调用loadTemplateMetas方法加载最新的dsl配置
				return System.currentTimeMillis();
			}
		});
```

关键点说明：

- loadTemplateMetas方法从数据库（也可以从redis等其他数据源加载）加载给定namespace下面的所有的dsl语句
- getLastModifyTime方法获取配置在namespace下面的dsl语句最近修改时间搓（可以从数据库中获取时间戳、也可以从其他保存时间戳的地方获取时间戳）

![](images\db-dsl.png)

# 3.DSL表脚本及将xml文件中管理的dsl转换

DSL表脚本及将xml文件中管理的dsl转换:

```java
//在sqlite数据源testdslconfig中创建保存dsl语句的数据库表dslconfig
		String createStatusTableSQL = new StringBuilder()
				.append("create table dslconfig (ID string,name string,namespace string,dslTemplate TEXT,vtpl number(1),multiparser number(1) ")
				.append(",referenceNamespace string,referenceTemplateName string,PRIMARY KEY (ID))").toString();

		try {
			String exist = "select 1 from dslconfig";
			//SQLExecutor.updateWithDBName("gencode","drop table BBOSS_GENCODE");

			SQLExecutor.queryObjectWithDBName(int.class,"testdslconfig", exist);
			logger.info("重建建dslconfig表："+createStatusTableSQL+"。");
			SQLExecutor.updateWithDBName("testdslconfig","drop table dslconfig");
			SQLExecutor.updateWithDBName("testdslconfig",createStatusTableSQL);
			logger.info("重建建dslconfig表成功。");
		} catch (Exception e) {

			logger.info("dslconfig table 不存在，创建dslconfig表："+createStatusTableSQL+"。");
			try {
				SQLExecutor.updateWithDBName("testdslconfig",createStatusTableSQL);
				logger.info("创建dslconfig表成功："+createStatusTableSQL+"。");
			} catch (SQLException e1) {
				logger.info("创建dslconfig表失败："+createStatusTableSQL+"。",e1);
				e1.printStackTrace();
			}
		}

		//初始化dsl配置：将配置文件中的sql转存到数据库中
		String dslpath = "esmapper/demo.xml";
	    final String namespace = "testnamespace";//一个命名空间的dsl可以对应为一个ClientInterface实例
		AOPTemplateContainerImpl aopTemplateContainer = new AOPTemplateContainerImpl(dslpath);
		int perKeyDSLStructionCacheSize = aopTemplateContainer.getPerKeyDSLStructionCacheSize();
		boolean alwaysCacheDslStruction = aopTemplateContainer.isAlwaysCacheDslStruction();
		List<TemplateMeta> templateMetaList = aopTemplateContainer.getTemplateMetas(namespace);//将demo.xml文件中配置的dsl转换为属于namespace命名空间的对象列表

		//保存dsl到表dslconfig
		SQLExecutor.insertBeans("testdslconfig",
				"insert into dslconfig(ID,name,namespace,dslTemplate,vtpl,multiparser,referenceNamespace,referenceTemplateName) " +
						"values(#[id]," + //主键
						"#[name]," + //dsl名称
						"#[namespace]," + //dsl所属命名空间
						"#[dslTemplate]," + //dsl语句
						"#[vtpl]," + //一般设置为true， dsl语句中是否包含velocity语法内容，包含为true，否则为false（避免进行velocity语法解析，提升性能），默认为true
						"#[multiparser]," + // 一般设置为true，dsl如果包含velocity动态语法，是否需要对每次动态生成的dsl进行模板变量#[xxxx]语法解析，true 需要，false不需要，默认true
						"#[referenceNamespace]," + // 如果对应的配置是一个引用，则需要通过referenceNamespace指定引用的dsl所属的命名空间
						"#[referenceTemplateName])",// 如果对应的配置是一个引用，则需要通过referenceTemplateName指定引用的dsl对应的名称name
				templateMetaList);
```

