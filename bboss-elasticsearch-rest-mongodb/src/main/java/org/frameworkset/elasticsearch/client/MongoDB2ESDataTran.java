package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.elasticsearch.client.tran.BaseElasticsearchDataTran;

public class MongoDB2ESDataTran extends BaseElasticsearchDataTran {

	public MongoDB2ESDataTran(MongoDB2ESResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}
	public MongoDB2ESDataTran(MongoDB2ESResultSet jdbcResultSet, ImportContext importContext,String cluster) {
		super(jdbcResultSet,importContext, cluster);
	}










}
