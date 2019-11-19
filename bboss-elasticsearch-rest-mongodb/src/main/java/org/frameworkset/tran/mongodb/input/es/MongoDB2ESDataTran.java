package org.frameworkset.tran.mongodb.input.es;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.BaseElasticsearchDataTran;
import org.frameworkset.tran.TranResultSet;

public class MongoDB2ESDataTran extends BaseElasticsearchDataTran {

	public MongoDB2ESDataTran(TranResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}
	public MongoDB2ESDataTran(TranResultSet jdbcResultSet, ImportContext importContext, String cluster) {
		super(jdbcResultSet,importContext, cluster);
	}










}
