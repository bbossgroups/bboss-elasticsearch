package org.frameworkset.elasticsearch.entity.sql;

import java.util.List;

public class SQLRestResponse<T> {
	private ColumnMeta[] columns;
	private List<Object[]> rows;
	private String cursor;
	private List<T> datas;
	public SQLRestResponse() {
		// TODO Auto-generated constructor stub
	}
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
}
