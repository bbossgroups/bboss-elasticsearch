package org.frameworkset.elasticsearch.entity.sql;
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

import org.frameworkset.elasticsearch.client.ClientInterface;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2018/9/17 14:56
 * @author biaoping.yin
 * @version 1.0
 */
public class SQLResult<T> {
	private ColumnMeta[] columns;
	private List<Object[]> rows;
	private String cursor;
	private List<T> datas;
	private transient Class<T> beanType;
	private transient ClientInterface clientInterface;
	public ColumnMeta[] getColumns() {
		return columns;
	}

	public void setColumns(ColumnMeta[] columns) {
		this.columns = columns;
	}

	public List<Object[]> getRows() {
		return rows;
	}

	public void setRows(List<Object[]> rows) {
		this.rows = rows;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}

	public List<T> getDatas() {
		return datas;
	}

	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

	/**
	 * 主动关闭分页游标
	 */
	public String closeCursor(){
		if(cursor != null)
			return clientInterface.closeSQLCursor(this.cursor);
		return null;
	}

	/**
	 * 获取下一页数据
	 * @return
	 */
	public SQLResult<T> nextPage(){
		if(cursor == null || datas == null || datas.size() == 0)
			return null;
		else
		{
			return clientInterface.fetchQueryByCursor(this.beanType,this);
		}
	}

	public Class<T> getBeanType() {
		return beanType;
	}

	public void setBeanType(Class<T> beanType) {
		this.beanType = beanType;
	}

	public void setClientInterface(ClientInterface clientInterface) {
		this.clientInterface = clientInterface;
	}
}
