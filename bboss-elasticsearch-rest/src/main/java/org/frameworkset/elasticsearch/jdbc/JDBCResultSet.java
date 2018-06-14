package org.frameworkset.elasticsearch.jdbc;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCResultSet {
	private String esIdField;
	private String esParentIdField;
	private String routingField;
	private String routingValue;
	private Boolean esDocAsUpsert;
	private Integer esRetryOnConflict;
	private ResultSet resultSet;

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public Integer getEsRetryOnConflict() {
		return esRetryOnConflict;
	}

	public void setEsRetryOnConflict(Integer esRetryOnConflict) {
		this.esRetryOnConflict = esRetryOnConflict;
	}

	public Boolean getEsDocAsUpsert() {
		return esDocAsUpsert;
	}

	public void setEsDocAsUpsert(Boolean esDocAsUpsert) {
		this.esDocAsUpsert = esDocAsUpsert;
	}

	public String getRoutingValue() {
		return routingValue;
	}

	public void setRoutingValue(String routingValue) {
		this.routingValue = routingValue;
	}

	public String getRoutingField() {
		return routingField;
	}

	public void setRoutingField(String routingField) {
		this.routingField = routingField;
	}

	public String getEsParentIdField() {
		return esParentIdField;
	}

	public void setEsParentIdField(String esParentIdField) {
		this.esParentIdField = esParentIdField;
	}

	public String getEsIdField() {
		return esIdField;
	}

	public void setEsIdField(String esIdField) {
		this.esIdField = esIdField;
	}
}
