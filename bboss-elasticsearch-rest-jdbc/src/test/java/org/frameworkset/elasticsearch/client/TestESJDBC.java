package org.frameworkset.elasticsearch.client;

import com.frameworkset.common.poolman.SQLExecutor;
import com.frameworkset.common.poolman.StatementInfo;
import com.frameworkset.common.poolman.handle.ResultSetHandler;
import com.frameworkset.common.poolman.util.SQLUtil;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TestESJDBC {

	private void initDBSource(){
		SQLUtil.startNoPool("test",//数据源名称
				"com.mysql.jdbc.Driver",//oracle驱动
				"jdbc:mysql://localhost:3306/bboss",//mysql链接串
				"root","123456",//数据库账号和口令
				"select 1 " //数据库连接校验sql
		);
	}
	@Test
	public void testDB2ES() throws SQLException {
		initDBSource();
		try {
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbdemo");
		}
		catch (Exception e){

		}
		SQLExecutor.queryByNullRowHandler(new ResultSetHandler() {
			@Override
			public void handleResult(ResultSet resultSet, StatementInfo statementInfo) throws Exception {
				ESJDBC esjdbcResultSet = new ESJDBC();
				esjdbcResultSet.setResultSet(resultSet);
				esjdbcResultSet.setMetaData(statementInfo.getMeta());
				JDBCRestClientUtil jdbcRestClientUtil = new JDBCRestClientUtil();
				jdbcRestClientUtil.addDocuments("dbdemo","dbdemo",esjdbcResultSet,"refresh",98);
			}
		},"select * from td_sm_log");

		long dbcount = SQLExecutor.queryObject(long.class,"select count(*) from td_sm_log");
		long escount = ElasticSearchHelper.getRestClientUtil().countAll("dbdemo");
		System.out.println("dbcount:"+dbcount+","+"escount:"+escount);

	}

	@Test
	public void testDB2ESClob() throws SQLException {
		initDBSource();
		try {
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbclobdemo");
		}
		catch (Exception e){

		}

		SQLExecutor.queryByNullRowHandler(new ResultSetHandler() {
			@Override
			public void handleResult(ResultSet resultSet, StatementInfo statementInfo) throws Exception {
				ESJDBC esjdbcResultSet = new ESJDBC();
				esjdbcResultSet.setResultSet(resultSet);
				esjdbcResultSet.setMetaData(statementInfo.getMeta());
				JDBCRestClientUtil jdbcRestClientUtil = new JDBCRestClientUtil();
				jdbcRestClientUtil.addDocuments("dbclobdemo","dbclobdemo",esjdbcResultSet,"refresh",1000);
			}
		},"select * from td_cms_document");

		long dbcount = SQLExecutor.queryObject(long.class,"select count(*) from td_cms_document");
		long escount = ElasticSearchHelper.getRestClientUtil().countAll("dbclobdemo");
		System.out.println("dbcount:"+dbcount+","+"escount:"+escount);

	}
	@Test
	public void testDB2ESImportBuilder(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		try {
			//清除测试表
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbclobdemo");
		}
		catch (Exception e){

		}
		//数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss")
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(false);//是否使用连接池


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑
		importBuilder.setSql("select * from td_cms_document");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //必填项
				.setRefreshOption(null)//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setEsIdField("documentId")//可选项
				.setEsParentIdField(null) //可选项,如果不指定，es自动为文档产生id
				.setRoutingValue(null) //可选项		importBuilder.setRoutingField(null);
				.setEsDocAsUpsert(true)//可选项
				.setEsRetryOnConflict(3)//可选项
				.setEsReturnSource(false)//可选项
				.setEsVersionField(null)//可选项
				.setEsVersionType(null)//可选项
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") //可选项,默认日期格式
				.setLocale("zh_CN")  //可选项,默认locale
				.setTimeZone("Etc/UTC")  //可选项,默认时区
				.setBatchSize(1000);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理

		/**
		 * db-es mapping 表字段名称到es 文档字段的映射：比如document_id -> docId
		 * 可以配置mapping，也可以不配置，默认基于java 驼峰规则进行db field-es field的映射和转换
		 */
		importBuilder.addFieldMapping("document_id","docId")
					 .addFieldMapping("docwtime","docwTime")
					 .addIgnoreFieldMapping("channel_id");//添加忽略字段

		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.db2es();
	}

	@Test
	public void testSimpleDB2ESImportBuilder(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		try {
			//清除测试表
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbclobdemo");
		}
		catch (Exception e){

		}
		//数据源相关配置，可选项，可以在外部启动数据源
		importBuilder.setDbName("test")
				.setDbDriver("com.mysql.jdbc.Driver") //数据库驱动程序，必须导入相关数据库的驱动jar包
				.setDbUrl("jdbc:mysql://localhost:3306/bboss")
				.setDbUser("root")
				.setDbPassword("123456")
				.setValidateSQL("select 1")
				.setUsePool(false);//是否使用连接池


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑
		importBuilder.setSql("select * from td_cms_document");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //必填项
				.setRefreshOption(null)//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(1000);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理


		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.db2es();
	}

	/**
	 * 从外部application.properties文件中加载数据源配置和es配置
	 */
	@Test
	public void testSimpleDB2ESImportBuilderFromExternalDBConfig(){
		DB2ESImportBuilder importBuilder = DB2ESImportBuilder.newInstance();
		try {
			//清除测试表
			ElasticSearchHelper.getRestClientUtil().dropIndice("dbclobdemo");
		}
		catch (Exception e){

		}


		//指定导入数据的sql语句，必填项，可以设置自己的提取逻辑
		importBuilder.setSql("select * from td_cms_document");
		/**
		 * es相关配置
		 */
		importBuilder
				.setIndex("dbclobdemo") //必填项
				.setIndexType("dbclobdemo") //必填项
				.setRefreshOption(null)//可选项，null表示不实时刷新，importBuilder.setRefreshOption("refresh");表示实时刷新
				.setUseJavaName(true) //可选项,将数据库字段名称转换为java驼峰规范的名称，例如:doc_id -> docId
				.setBatchSize(1000);  //可选项,批量导入es的记录数，默认为-1，逐条处理，> 0时批量处理


		/**
		 * 执行数据库表数据导入es操作
		 */
		DataStream dataStream = importBuilder.builder();
		dataStream.db2es();
	}
}
