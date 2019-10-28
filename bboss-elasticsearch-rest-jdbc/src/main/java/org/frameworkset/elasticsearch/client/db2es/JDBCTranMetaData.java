package org.frameworkset.elasticsearch.client.db2es;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.frameworkset.common.poolman.sql.PoolManResultSetMetaData;
import org.frameworkset.elasticsearch.client.ESDataImportException;
import org.frameworkset.elasticsearch.client.tran.TranMeta;

import java.sql.SQLException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/27 12:13
 * @author biaoping.yin
 * @version 1.0
 */
public class JDBCTranMetaData implements TranMeta {
	public JDBCTranMetaData(PoolManResultSetMetaData poolManResultSetMetaData) {
		this.poolManResultSetMetaData = poolManResultSetMetaData;
	}

	private PoolManResultSetMetaData poolManResultSetMetaData;
	@Override
	public int getColumnCount()  throws ESDataImportException {
		try {
			return poolManResultSetMetaData.getColumnCount();
		} catch (SQLException e) {
			throw new  ESDataImportException(e);
		}
	}

	@Override
	public String getColumnLabelByIndex(int i)  throws ESDataImportException{
		try {
			return poolManResultSetMetaData.getColumnLabelByIndex(i);
		} catch (SQLException e) {
			throw new  ESDataImportException(e);
		}
	}

	@Override
	public int getColumnTypeByIndex(int i)  throws ESDataImportException{
		try {
			return poolManResultSetMetaData.getColumnTypeByIndex(i);
		} catch (SQLException e) {
			throw new  ESDataImportException(e);
		}
	}

	@Override
	public String getColumnJavaNameByIndex(int i)  throws ESDataImportException{
			return poolManResultSetMetaData.getColumnJavaNameByIndex(  i);

	}

	@Override
	public String getColumnLabelLowerByIndex(int i)  throws ESDataImportException{
		return poolManResultSetMetaData.getColumnLabelLowerByIndex( i);

	}
}
