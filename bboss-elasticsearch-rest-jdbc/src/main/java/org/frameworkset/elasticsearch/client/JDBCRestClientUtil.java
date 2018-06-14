package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.jdbc.JDBCResultSet;
import org.frameworkset.soa.BBossStringWriter;

import java.io.IOException;
import java.sql.SQLException;

public class JDBCRestClientUtil extends RestClientUtil {
	public JDBCRestClientUtil(ElasticSearchClient client, IndexNameBuilder indexNameBuilder) {
		super(client, indexNameBuilder);
	}
	public String addDocuments(String indexName, String indexType, JDBCResultSet jdbcResultSet, String refreshOption, int batchsize) throws ElasticSearchException {
		if(jdbcResultSet == null || jdbcResultSet.getResultSet() == null)
			return null;
		StringBuilder builder = new StringBuilder();
		BBossStringWriter writer = new BBossStringWriter(builder);
		try {
			while (jdbcResultSet.next()) {
				try {
					BuildTool.evalBuilk(writer, indexName, indexType, jdbcResultSet, "index");
				} catch (IOException e) {
					throw new ElasticSearchException(e);
				}
			}
		}
		catch (SQLException e){
			throw new ElasticSearchException(e);
		}
		writer.flush();
		if(refreshOption == null)
			return this.client.executeHttp("_bulk",builder.toString(),ClientUtil.HTTP_POST);
		else
			return this.client.executeHttp("_bulk?"+refreshOption,builder.toString(),ClientUtil.HTTP_POST);
	}
}
