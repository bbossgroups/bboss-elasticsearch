package org.frameworkset.elasticsearch.client.db2es;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.tran.BaseElasticsearchDataTran;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB2ESDataTran extends BaseElasticsearchDataTran {
	private static Logger logger = LoggerFactory.getLogger(DB2ESDataTran.class);

	public DB2ESDataTran(JDBCResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}
	public DB2ESDataTran(JDBCResultSet jdbcResultSet, ImportContext importContext, String esCluster) {
		super(jdbcResultSet,   importContext,  esCluster);
	}


}
