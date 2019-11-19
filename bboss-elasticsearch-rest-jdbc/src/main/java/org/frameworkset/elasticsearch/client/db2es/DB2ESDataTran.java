package org.frameworkset.elasticsearch.client.db2es;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.BaseElasticsearchDataTran;

public class DB2ESDataTran extends BaseElasticsearchDataTran {

	public DB2ESDataTran(JDBCResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}
	public DB2ESDataTran(JDBCResultSet jdbcResultSet, ImportContext importContext, String esCluster) {
		super(jdbcResultSet,   importContext,  esCluster);
	}


}
