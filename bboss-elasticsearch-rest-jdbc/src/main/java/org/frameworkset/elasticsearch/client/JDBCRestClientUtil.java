package org.frameworkset.elasticsearch.client;

import com.frameworkset.common.poolman.handle.ValueExchange;
import com.frameworkset.common.poolman.sql.PoolManResultSetMetaData;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.elasticsearch.template.ESUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class JDBCRestClientUtil extends ErrorWrapper{
	private static Logger logger = LoggerFactory.getLogger(JDBCRestClientUtil.class);
	private ClientInterface clientInterface;
	private ESJDBC jdbcResultSet;

	public JDBCRestClientUtil( ) {
		clientInterface = ElasticSearchHelper.getRestClientUtil();
	}

	public JDBCRestClientUtil( String esCluster) {
		clientInterface = ElasticSearchHelper.getRestClientUtil(esCluster);
	}


	/**
	 * 并行批处理导入
	 * @param indexName
	 * @param indexType
	 * @param batchsize
	 * @param refreshOption
	 * @return
	 */
	private String parallelBatchExecute(String indexName,String indexType,int batchsize,String refreshOption){
		int count = 0;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		String ret = null;
		ExecutorService	service = jdbcResultSet.buildThreadPool();
		List<Future> tasks = new ArrayList<Future>();
		int taskNo = 0;
		ImportCount totalCount = new ImportCount();
		Exception exception = null;
		Object lastValue = null;
		try {

			while (jdbcResultSet.next()) {
				if(!assertCondition()) {
					throw error;
				}
				lastValue = jdbcResultSet.getLastValue();
				evalBuilk(writer, indexName, indexType, jdbcResultSet, "index");
				count++;
				if (count == batchsize) {
					writer.flush();
					String datas = builder.toString();
					builder.setLength(0);
					writer.close();
					writer = new BBossStringWriter(builder);
					count = 0;
					tasks.add(service.submit(new TaskCall(refreshOption,  datas,this,taskNo,totalCount,batchsize)));

					taskNo ++;

				}

			}
			if (count > 0) {
				if(this.error != null && !jdbcResultSet.isContinueOnError()) {
					throw error;
				}
				writer.flush();
				String datas = builder.toString();
				tasks.add(service.submit(new TaskCall(refreshOption,datas,this,taskNo,totalCount,count)));
				taskNo ++;
				if(logger.isInfoEnabled())
					logger.info(new StringBuilder().append("submit tasks:").append((taskNo)).toString());
			}
			else{
				if(logger.isInfoEnabled())
					logger.info(new StringBuilder().append("submit tasks:").append(taskNo).toString());
			}

		} catch (SQLException e) {
			exception = e;
			throw new ElasticSearchException(e);

		} catch (ElasticSearchException e) {
			exception = e;
			throw e;
		} catch (Exception e) {
			exception = e;
			throw new ElasticSearchException(e);
		}
		finally {
			waitTasksComplete(  jdbcResultSet, tasks,  service,exception,  lastValue,totalCount );
			try {
				writer.close();
			} catch (Exception e) {

			}
		}

		return ret;
	}
	/**
	 * 串行批处理导入
	 * @param indexName
	 * @param indexType
	 * @param batchsize
	 * @param refreshOption
	 * @return
	 */
	private String batchExecute(String indexName,String indexType,int batchsize,String refreshOption){
		int count = 0;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		String ret = null;
		int taskNo = 0;
		Exception exception = null;
		Object lastValue = null;
		try {

			while (jdbcResultSet.next()) {
				lastValue = jdbcResultSet.getLastValue();
				evalBuilk(writer, indexName, indexType, jdbcResultSet, "index");
				count++;
				if (count == batchsize) {
					writer.flush();
					String datas = builder.toString();
					builder.setLength(0);
					writer.close();
					writer = new BBossStringWriter(builder);
					count = 0;
					taskNo ++;

					ret = TaskCall.call(refreshOption,clientInterface,datas,jdbcResultSet);
					jdbcResultSet.flushLastValue(lastValue);
				}

			}
			if (count > 0) {
				writer.flush();
				String datas = builder.toString();

				ret = TaskCall.call(refreshOption,clientInterface,datas,jdbcResultSet);
				jdbcResultSet.flushLastValue(lastValue);
			}
		} catch (SQLException e) {
			exception = e;
			throw new ElasticSearchException(e);

		} catch (ElasticSearchException e) {
			exception = e;
			throw e;
		} catch (Exception e) {
			exception = e;
			throw new ElasticSearchException(e);
		}
		finally {
			if(exception != null && !getESJDBC().isContinueOnError()){
				getESJDBC().stop();
			}
			try {
				writer.close();
			} catch (Exception e) {

			}
		}

		return ret;
	}
	public String addDocuments(String indexName, String indexType, ESJDBC jdbcResultSet, String refreshOption, int batchsize) throws ElasticSearchException {
		if(jdbcResultSet == null || jdbcResultSet.getResultSet() == null)
			return null;
		this.jdbcResultSet = jdbcResultSet;

		if (batchsize <= 0) {
			StringBuilder builder = new StringBuilder();
			BBossStringWriter writer = new BBossStringWriter(builder);
			Object lastValue = null;
			Exception exception = null;
			try {

				while (jdbcResultSet.next()) {
					try {
						lastValue = jdbcResultSet.getLastValue();
						evalBuilk(writer, indexName, indexType, jdbcResultSet, "index");
					} catch (Exception e) {
						throw new ElasticSearchException(e);
					}

				}
				writer.flush();
				String ret = TaskCall.call(refreshOption,clientInterface,builder.toString(),jdbcResultSet);
				jdbcResultSet.flushLastValue(lastValue);
				return ret;
			} catch (Exception e) {
				exception = e;
				throw new ElasticSearchException(e);
			}
			finally {
				if(exception != null && !getESJDBC().isContinueOnError()){
					getESJDBC().stop();
				}
			}


		} else {
			if(jdbcResultSet.getThreadCount() > 0 && jdbcResultSet.isParallel()){
				return this.parallelBatchExecute(indexName,indexType,batchsize,refreshOption);
			}
			else{
				return this.batchExecute(indexName,indexType,batchsize,refreshOption);
			}

		}



	}
	private void jobComplete(ExecutorService service,Exception exception,Object lastValue ){
		if (jdbcResultSet.getScheduleService() == null) {//作业定时调度执行的话，需要关闭线程池
			service.shutdown();
		}
		else{
			if(this.assertCondition(exception)){
				jdbcResultSet.flushLastValue( lastValue );
			}
			else{
				service.shutdown();
				this.getESJDBC().stop();
			}
		}
	}

	private void waitTasksComplete(ESJDBC jdbcResultSet,final List<Future> tasks,
								   final ExecutorService service,Exception exception,Object lastValue,final ImportCount totalCount  ){
		if(!jdbcResultSet.isAsyn() || jdbcResultSet.getScheduleService() != null) {
			int count = 0;
			for (Future future : tasks) {
				try {
					future.get();
					count ++;
				} catch (ExecutionException e) {
					if(exception == null)
						exception = e;
					if(e.getCause() != null)
						logger.error("",e.getCause());
					else
						logger.error("",e);
				}catch (Exception e) {
					if(exception == null)
						exception = e;
					logger.error("",e);
				}
			}
			if(logger.isInfoEnabled())
				logger.info(new StringBuilder().append("Complete tasks:").append(count).append(",Total import data ").append(totalCount.getTotalCount()).append("条").toString());

			jobComplete(  service,exception,lastValue );
		}
		else{
			Thread completeThread = new Thread(new Runnable() {
				@Override
				public void run() {
					int count = 0;
					for (Future future : tasks) {
						try {
							future.get();
							count ++;
						} catch (ExecutionException e) {
							if(e.getCause() != null)
								logger.error("",e.getCause());
							else
								logger.error("",e);
						}catch (Exception e) {
							logger.error("",e);
						}
					}
					if(logger.isInfoEnabled())
						logger.info(new StringBuilder().append("Complete tasks:").append(count).append(",Total import data ").append(totalCount.getTotalCount()).append("条").toString());

					jobComplete(  service,null,null);
				}
			});
			completeThread.start();
		}
	}

	private Object handleDate(ResultSet row,int i)
	{
		Object value = null;
		try {
			try {
				value = row.getTimestamp(i+1);
				if(value != null)
					value = ((java.sql.Timestamp)value).getTime();
				else
					value  = 0;
			} catch (Exception e) {
				value = row.getDate(i+1);
				if(value != null)
					value = ((java.sql.Date)value).getTime();
				else
					value  = 0;

			}

		} catch (Exception e) {
			value  = 0;
		}
		return value;
	}

	private static Object getEsId(ESJDBC jdbcResultSet) throws Exception {
		if(jdbcResultSet.getEsIdField() != null)
			return jdbcResultSet.getValue(jdbcResultSet.getEsIdField());
		return null;
	}
	private static Object getEsParentId(ESJDBC jdbcResultSet) throws Exception {
		if(jdbcResultSet.getEsParentIdField() != null) {
			return jdbcResultSet.getValue(jdbcResultSet.getEsParentIdField());
		}
		else
			return jdbcResultSet.getEsParentIdValue();
	}

	public static void buildMeta(Writer writer ,String indexType,String indexName, ESJDBC jdbcResultSet,String action) throws Exception {

		Object id = getEsId(jdbcResultSet) ;
		Object parentId = getEsParentId(  jdbcResultSet);
		Object routing = jdbcResultSet.getValue(jdbcResultSet.getRoutingField());
		if(routing == null)
			routing = jdbcResultSet.getRoutingValue();
		Object esRetryOnConflict = jdbcResultSet.getEsRetryOnConflict();


		buildMeta(  writer ,  indexType,  indexName,   jdbcResultSet,  action,  id,  parentId,routing,esRetryOnConflict);
	}
	private static Object getVersion(ESJDBC esjdbc) throws Exception {
		Object version = esjdbc.getEsVersionField() !=null? esjdbc.getValue(esjdbc.getEsVersionField()):esjdbc.getEsVersionValue();
		return version;
	}
	public static void buildMeta(Writer writer ,String indexType,String indexName, ESJDBC esjdbc,String action,
								 Object id,Object parentId,Object routing,Object esRetryOnConflict) throws Exception {

		if(id != null) {
			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\", \"_id\" : ");
			BuildTool.buildId(id,writer,true);
			if(parentId != null){
				writer.write(", \"parent\" : ");
				BuildTool.buildId(parentId,writer,true);
			}
			if(routing != null){

				writer.write(", \"_routing\" : ");
				BuildTool.buildId(routing,writer,true);
			}

//			if(action.equals("update"))
//			{
			if (esRetryOnConflict != null) {
				writer.write(",\"_retry_on_conflict\":");
				writer.write(String.valueOf(esRetryOnConflict));
			}
			Object version = getVersion(  esjdbc);

			if (version != null) {

				writer.write(",\"_version\":");

				writer.write(String.valueOf(version));

			}

			Object versionType = esjdbc.getEsVersionType();
			if(versionType != null) {
				writer.write(",\"_version_type\":\"");
				writer.write(String.valueOf(versionType));
				writer.write("\"");
			}



			writer.write(" } }\n");
		}
		else {

			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			if(parentId != null){
				writer.write(", \"parent\" : ");
				BuildTool.buildId(parentId,writer,true);
			}
			if(routing != null){

				writer.write(", \"_routing\" : ");
				BuildTool.buildId(routing,writer,true);
			}
//			if(action.equals("update"))
//			{
			{
				if (esRetryOnConflict != null) {
					writer.write(",\"_retry_on_conflict\":");
					writer.write(String.valueOf(esRetryOnConflict));
				}
				Object version = getVersion(  esjdbc);
				if (version != null) {

					writer.write(",\"_version\":");

					writer.write(String.valueOf(version));

				}

				Object versionType = esjdbc.getEsVersionType();
				if(versionType != null) {
					writer.write(",\"_version_type\":\"");
					writer.write(String.valueOf(versionType));
					writer.write("\"");
				}

			}
			writer.write("\" } }\n");
		}
	}

	public static void evalBuilk(Writer writer, String indexName, String indexType, ESJDBC jdbcResultSet, String action) throws Exception {


		if (jdbcResultSet != null) {
			Context context = new ContextImpl(jdbcResultSet);
			jdbcResultSet.refactorData(context);
			buildMeta(  writer ,  indexType,  indexName,   jdbcResultSet,action);

			if(!action.equals("update")) {
//				SerialUtil.object2json(param,writer);
				serialResult(  writer,jdbcResultSet,context);
			}
			else
			{

				writer.write("{\"doc\":");
				serialResult(  writer,jdbcResultSet,context);
				if(jdbcResultSet.getEsDocAsUpsert() != null){
					writer.write(",\"doc_as_upsert\":");
					writer.write(String.valueOf(jdbcResultSet.getEsDocAsUpsert()));
				}

				if(jdbcResultSet.getEsReturnSource() != null){
					writer.write(",\"_source\":");
					writer.write(String.valueOf(jdbcResultSet.getEsReturnSource()));
				}
				writer.write("}\n");



			}
		}

	}

	private static void serialResult( Writer writer, ESJDBC esjdbc,Context context) throws Exception {
		PoolManResultSetMetaData metaData = esjdbc.getMetaData();
		int counts = metaData.getColumnCount();
		writer.write("{");
		Boolean useJavaName = esjdbc.getUseJavaName();
		if(useJavaName == null)
			useJavaName = true;

		Boolean useLowcase = esjdbc.getUseLowcase();


		if(useJavaName == null) {
			useJavaName = false;
		}
		if(useLowcase == null)
		{
			useLowcase = false;
		}
		boolean hasSeted = false;

		for(int i =0; i < counts; i++)
		{
			String colName = metaData.getColumnLabelByIndex(i);
//			if("ROWNUM__".equals(colName))//去掉oracle的行伪列
//				continue;
			String javaName = null;
			FieldMeta fieldMeta = context.getMappingName(colName);
			if(fieldMeta != null) {
				if(fieldMeta.getIgnore() != null && fieldMeta.getIgnore() == true)
					continue;
				javaName = fieldMeta.getEsFieldName();
			}
			else {
				if(useJavaName) {
					javaName = metaData.getColumnJavaNameByIndex(i);
				}
				else{
					javaName =  !useLowcase ?colName:metaData.getColumnLabelLowerByIndex(i);
				}
			}
			if(javaName == null){
				javaName = colName;
			}
			if(hasSeted )
				writer.write(",");
			else
				hasSeted = true;
			writer.write("\"");
			writer.write(javaName);
			writer.write("\":");
//			int colType = metaData.getColumnTypeByIndex(i);
			Object value = esjdbc.getValue(     i,  colName);
			if(value != null) {
				if (value instanceof String) {
					writer.write("\"");
					CharEscapeUtil charEscapeUtil = new CharEscapeUtil(writer);
					charEscapeUtil.writeString((String) value, true);
					writer.write("\"");
				} else if (value instanceof Date) {
					DateFormat dateFormat = null;
					if(fieldMeta != null){
						DateFormateMeta dateFormateMeta = fieldMeta.getDateFormateMeta();
						if(dateFormateMeta != null){
							dateFormat = dateFormateMeta.toDateFormat();
						}
					}
					if(dateFormat == null)
						dateFormat = esjdbc.getFormat();
					String dataStr = ESUtil.getDate((Date) value,dateFormat);
					writer.write("\"");
					writer.write(dataStr);
					writer.write("\"");
				}
				else if(value instanceof Clob)
				{
					String dataStr = ValueExchange.getStringFromClob((Clob)value);
					writer.write("\"");
					CharEscapeUtil charEscapeUtil = new CharEscapeUtil(writer);
					charEscapeUtil.writeString(dataStr, true);
					writer.write("\"");

				}
				else if(value instanceof Blob){
					String dataStr = ValueExchange.getStringFromBlob((Blob)value);
					writer.write("\"");
					CharEscapeUtil charEscapeUtil = new CharEscapeUtil(writer);
					charEscapeUtil.writeString(dataStr, true);
					writer.write("\"");
				}
				else {
					writer.write(String.valueOf(value));
				}
			}
			else{
				writer.write("null");
			}

		}
		List<FieldMeta> fieldValueMetas = esjdbc.getFieldValues();
		hasSeted = appendFieldValues(  writer,   esjdbc, fieldValueMetas,  hasSeted);
		fieldValueMetas = context.getFieldValues();
		appendFieldValues(  writer,   esjdbc, fieldValueMetas,  hasSeted);
		writer.write("}\n");
	}
	private static boolean appendFieldValues(Writer writer, ESJDBC esjdbc,
										  List<FieldMeta> fieldValueMetas,boolean hasSeted) throws IOException {
		if(fieldValueMetas != null && fieldValueMetas.size() > 0){
			for(int i =0; i < fieldValueMetas.size(); i++)
			{

				FieldMeta fieldMeta = fieldValueMetas.get(i);
				String javaName = fieldMeta.getEsFieldName();
				Object value = fieldMeta.getValue();
//				if(value == null)
//					continue;
				if(hasSeted)
					writer.write(",");
				else{
					hasSeted = true;
				}
				writer.write("\"");
				writer.write(javaName);
				writer.write("\":");
//			int colType = metaData.getColumnTypeByIndex(i);

				if(value != null) {
					if (value instanceof String) {
						writer.write("\"");
						CharEscapeUtil charEscapeUtil = new CharEscapeUtil(writer);
						charEscapeUtil.writeString((String) value, true);
						writer.write("\"");
					} else if (value instanceof Date) {
						DateFormat dateFormat = null;
						if(fieldMeta != null){
							DateFormateMeta dateFormateMeta = fieldMeta.getDateFormateMeta();
							if(dateFormateMeta != null){
								dateFormat = dateFormateMeta.toDateFormat();
							}
						}
						if(dateFormat == null)
							dateFormat = esjdbc.getFormat();
						String dataStr = ESUtil.getDate((Date) value,dateFormat);
						writer.write("\"");
						writer.write(dataStr);
						writer.write("\"");
					}
					else if(isBasePrimaryType(value.getClass())){
						writer.write(String.valueOf(value));
					}

					else {
						SimpleStringUtil.object2json(value,writer);
					}
				}
				else{
					writer.write("null");
				}

			}
		}
		return hasSeted;
	}
	public static final Class[] basePrimaryTypes = new Class[]{Integer.TYPE, Long.TYPE,
								Boolean.TYPE, Float.TYPE, Short.TYPE, Double.TYPE,
								Character.TYPE, Byte.TYPE, BigInteger.class, BigDecimal.class};

	public static boolean isBasePrimaryType(Class type) {
		if (!type.isArray()) {
			if (type.isEnum()) {
				return true;
			} else {
				Class[] var1 = basePrimaryTypes;
				int var2 = var1.length;

				for(int var3 = 0; var3 < var2; ++var3) {
					Class primaryType = var1[var3];
					if (primaryType.isAssignableFrom(type)) {
						return true;
					}
				}

				return false;
			}
		} else {
			return false;
		}
	}


	@Override
	public ClientInterface getClientInterface() {
		return this.clientInterface;
	}
	public ESJDBC getESJDBC(){
		return this.jdbcResultSet;
	}


}
