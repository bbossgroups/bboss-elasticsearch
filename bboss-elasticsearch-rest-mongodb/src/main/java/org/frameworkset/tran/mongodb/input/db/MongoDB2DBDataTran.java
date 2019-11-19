package org.frameworkset.tran.mongodb.input.db;

import org.frameworkset.elasticsearch.client.context.ImportContext;
import org.frameworkset.tran.TranResultSet;
import org.frameworkset.tran.db.output.DBOutPutDataTran;

import java.util.List;
import java.util.Map;

public class MongoDB2DBDataTran extends DBOutPutDataTran<List<Map<String,Object>>> {

	public MongoDB2DBDataTran(TranResultSet jdbcResultSet, ImportContext importContext) {
		super(jdbcResultSet,importContext);
	}



}
