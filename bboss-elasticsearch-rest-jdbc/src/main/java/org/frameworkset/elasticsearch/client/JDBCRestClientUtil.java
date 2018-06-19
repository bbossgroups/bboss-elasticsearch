package org.frameworkset.elasticsearch.client;

import com.frameworkset.common.poolman.handle.ValueExchange;
import com.frameworkset.common.poolman.sql.PoolManResultSetMetaData;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.elasticsearch.template.ESUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

public class JDBCRestClientUtil {
	private static Logger logger = LoggerFactory.getLogger(JDBCRestClientUtil.class);
	private ClientInterface clientInterface;
	public JDBCRestClientUtil( ) {
		clientInterface = ElasticSearchHelper.getRestClientUtil();
	}

	public JDBCRestClientUtil( String esCluster) {
		clientInterface = ElasticSearchHelper.getRestClientUtil(esCluster);
	}
	public String addDocuments(String indexName, String indexType, ESJDBC jdbcResultSet, String refreshOption, int batchsize) throws ElasticSearchException {
		if(jdbcResultSet == null || jdbcResultSet.getResultSet() == null)
			return null;
		try{
			if (batchsize <= 0) {
				StringBuilder builder = new StringBuilder();
				BBossStringWriter writer = new BBossStringWriter(builder);
				try {
					while (jdbcResultSet.next()) {
						try {
							evalBuilk(writer, indexName, indexType, jdbcResultSet, "index");
						} catch (Exception e) {
							throw new ElasticSearchException(e);
						}
					}
				} catch (SQLException e) {
					throw new ElasticSearchException(e);
				}
				writer.flush();
				if (refreshOption == null)
					return this.clientInterface.executeHttp("_bulk", builder.toString(), ClientUtil.HTTP_POST);
				else
					return this.clientInterface.executeHttp("_bulk?" + refreshOption, builder.toString(), ClientUtil.HTTP_POST);
			} else {
				int count = 0;
				StringBuilder builder = new StringBuilder();
				BBossStringWriter writer = new BBossStringWriter(builder);
				boolean hasData = false;
				String ret = null;
				try {

					while (jdbcResultSet.next()) {
						try {
							evalBuilk(writer, indexName, indexType, jdbcResultSet, "index");
							count++;
							hasData = true;
							if (count == batchsize) {
								writer.flush();
								if (refreshOption == null)
									ret = this.clientInterface.executeHttp("_bulk", builder.toString(), ClientUtil.HTTP_POST);
								else
									ret = this.clientInterface.executeHttp("_bulk?" + refreshOption, builder.toString(), ClientUtil.HTTP_POST);
								builder.setLength(0);
								writer.close();
								writer = new BBossStringWriter(builder);
								hasData = false;
							}
						} catch (Exception e) {
							throw new ElasticSearchException(e);
						}
					}
				} catch (SQLException e) {
					throw new ElasticSearchException(e);
				}

				if (hasData) {
					writer.flush();
					if (refreshOption == null)
						ret = this.clientInterface.executeHttp("_bulk", builder.toString(), ClientUtil.HTTP_POST);
					else
						ret = this.clientInterface.executeHttp("_bulk?" + refreshOption, builder.toString(), ClientUtil.HTTP_POST);
				}
				return ret;
			}
		}
		finally {
			jdbcResultSet.destroy();
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
	private static Object getValue(  ResultSet row, int i, String colName) throws Exception
	{
		Object value = row.getObject(i+1);


		return value;
	}

	private static Object getFileValue(ESJDBC jdbcResultSet,String fileName) throws SQLException {
		if(fileName != null){
			Object id = jdbcResultSet.getResultSet().getObject(fileName);
			return  id;
		}
		return null;
	}
	public static void buildMeta(Writer writer ,String indexType,String indexName, ESJDBC jdbcResultSet,String action) throws Exception {

		Object id = getFileValue(jdbcResultSet,jdbcResultSet.getEsIdField());
		Object parentId = getFileValue(jdbcResultSet,jdbcResultSet.getEsParentIdField());
		Object routing = getFileValue(jdbcResultSet,jdbcResultSet.getRoutingField());
		if(routing == null)
			routing = jdbcResultSet.getRoutingValue();
		Object esRetryOnConflict = jdbcResultSet.getEsRetryOnConflict();


		buildMeta(  writer ,  indexType,  indexName,   jdbcResultSet,  action,  id,  parentId,routing,esRetryOnConflict);
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
			Object version = getFileValue(esjdbc,esjdbc.getEsVersionField());
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
				Object version = getFileValue(esjdbc,esjdbc.getEsVersionField());
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
			BuildTool.buildMeta(  writer ,  indexType,  indexName,   jdbcResultSet,action);

			if(!action.equals("update")) {
//				SerialUtil.object2json(param,writer);
				serialResult(  writer,jdbcResultSet);
			}
			else
			{

				writer.write("{\"doc\":");
				serialResult(  writer,jdbcResultSet);
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

	private static void serialResult( Writer writer, ESJDBC esjdbc) throws Exception {
		PoolManResultSetMetaData metaData = esjdbc.getMetaData();
		int counts = metaData.getColumnCount();
		writer.write("{");
		Boolean useJavaName = esjdbc.getUseJavaName();
		if(useJavaName == null)
			useJavaName = true;
		for(int i =0; i < counts; i++)
		{

			String colName = metaData.getColumnLabelUpperByIndex(i);

			if("ROWNUM__".equals(colName))//去掉oracle的行伪列
				continue;
			String javaName = null;
			FieldMeta fieldMeta = esjdbc.getMappingName(colName);

			if(fieldMeta != null) {
				if(fieldMeta.getIgnore() != null && fieldMeta.getIgnore() == true)
					continue;
				javaName = fieldMeta.getEsFieldName();
			}
			else
				javaName = useJavaName ?metaData.getColumnJavaNameByIndex(i):colName;
			if(javaName == null){
				javaName = colName;
			}
			if(i > 0)
				writer.write(",");
			writer.write("\"");
			writer.write(javaName);
			writer.write("\":");
			int colType = metaData.getColumnTypeByIndex(i);
			Object value = getValue(      esjdbc.getResultSet(),  i,  colName);
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
		writer.write("}\n");
	}


}
