package org.frameworkset.elasticsearch.client;
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

import org.frameworkset.elasticsearch.client.schedule.CallInterceptor;
import org.frameworkset.elasticsearch.client.schedule.ScheduleConfig;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/10/9 15:12
 * @author biaoping.yin
 * @version 1.0
 */
public interface ImportContext {
	boolean isPrintTaskLog();

	void destroy();

	void importData();

	boolean isContinueOnError();

	boolean assertCondition();

	Boolean getFixedRate();

	ScheduleConfig getScheduleConfig();

	List<CallInterceptor> getCallInterceptors();

	void doImportData();

	int getStatusTableId();

	boolean isFromFirst();

	String getDateLastValueColumn();

	String getNumberLastValueColumn();

	Object getConfigLastValue();

	String getLastValueStoreTableName();

	String getLastValueStorePath();

	Integer getLastValueType();

	DBConfig getStatusDbConfig();

	boolean isExternalTimer();
//	public Object getValue(String columnName) throws ESDataImportException;
//	public Object getDateTimeValue(String columnName) throws ESDataImportException;

}
