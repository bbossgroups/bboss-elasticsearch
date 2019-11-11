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

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.client.config.BaseImportBuilder;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/11 21:29
 * @author biaoping.yin
 * @version 1.0
 */
public class MongoDB2ESExportBuilder extends BaseImportBuilder {

	private String name;
	private String serverAddresses;
	private String option;
	private String writeConcern;
	private String readPreference;
	private Boolean autoConnectRetry = true;

	private int connectionsPerHost = 50;

	private int maxWaitTime = 120000;
	private int socketTimeout = 0;
	private int connectTimeout = 15000;


	/**是否启用sql日志，true启用，false 不启用，*/
	private int threadsAllowedToBlockForConnectionMultiplier;
	private Boolean socketKeepAlive = false;

	private String mode;



	public String getName() {
		return name;
	}

	public MongoDB2ESExportBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public String getServerAddresses() {
		return serverAddresses;
	}

	public MongoDB2ESExportBuilder setServerAddresses(String serverAddresses) {
		this.serverAddresses = serverAddresses;
		return this;
	}

	public String getOption() {
		return option;
	}

	public MongoDB2ESExportBuilder setOption(String option) {
		this.option = option;
		return this;
	}

	public String getWriteConcern() {
		return writeConcern;
	}

	public MongoDB2ESExportBuilder setWriteConcern(String writeConcern) {
		this.writeConcern = writeConcern;
		return this;
	}

	public String getReadPreference() {
		return readPreference;
	}

	public MongoDB2ESExportBuilder setReadPreference(String readPreference) {
		this.readPreference = readPreference;
		return this;
	}

	public Boolean getAutoConnectRetry() {
		return autoConnectRetry;
	}

	public MongoDB2ESExportBuilder setAutoConnectRetry(Boolean autoConnectRetry) {
		this.autoConnectRetry = autoConnectRetry;
		return this;
	}

	public int getConnectionsPerHost() {
		return connectionsPerHost;
	}

	public MongoDB2ESExportBuilder setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
		return this;
	}

	public int getMaxWaitTime() {
		return maxWaitTime;
	}

	public MongoDB2ESExportBuilder setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
		return this;
	}

	public int getSocketTimeout() {
		return socketTimeout;
	}

	public MongoDB2ESExportBuilder setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
		return this;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public MongoDB2ESExportBuilder setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}

	public MongoDB2ESExportBuilder setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
		return this;
	}

	public Boolean getSocketKeepAlive() {
		return socketKeepAlive;
	}

	public MongoDB2ESExportBuilder setSocketKeepAlive(Boolean socketKeepAlive) {
		this.socketKeepAlive = socketKeepAlive;
		return this;
	}

	public String getMode() {
		return mode;
	}

	public MongoDB2ESExportBuilder setMode(String mode) {
		this.mode = mode;
		return this;
	}

	public String toString(){
		StringBuilder ret = new StringBuilder();
		ret.append(SimpleStringUtil.object2json(this));
		return ret.toString();
	}

	@Override
	protected WrapedExportResultHandler buildExportResultHandler(ExportResultHandler exportResultHandler) {
		return new MongoDB2ESExportResultHandler(exportResultHandler);
	}


	public DataStream builder(){
		super.builderConfig();
//		this.buildDBConfig();
//		this.buildStatusDBConfig();
		try {
			logger.info("ES2DB Import Configs:");
			logger.info(this.toString());
		}
		catch (Exception e){

		}
		MongoDB2ESImportConfig es2DBImportConfig = new MongoDB2ESImportConfig();
		super.buildImportConfig(es2DBImportConfig);
		es2DBImportConfig.setName(this.name);
		es2DBImportConfig.setServerAddresses(serverAddresses);
		es2DBImportConfig.setOption(option);//private String option;
		es2DBImportConfig.setWriteConcern(writeConcern);//private String writeConcern;
		es2DBImportConfig.setReadPreference(readPreference);//private String readPreference;
		es2DBImportConfig.setAutoConnectRetry(autoConnectRetry);//private Boolean autoConnectRetry = true;

		es2DBImportConfig.setConnectionsPerHost(connectionsPerHost);//private int connectionsPerHost = 50;

		es2DBImportConfig.setMaxWaitTime(maxWaitTime);//private int maxWaitTime = 120000;
		es2DBImportConfig.setSocketTimeout(socketTimeout);//private int socketTimeout = 0;
		es2DBImportConfig.setConnectTimeout(connectTimeout);//private int connectTimeout = 15000;


		/**是否启用sql日志，true启用，false 不启用，*/
		es2DBImportConfig.setThreadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);//private int threadsAllowedToBlockForConnectionMultiplier;
		es2DBImportConfig.setSocketKeepAlive(socketKeepAlive);//private Boolean socketKeepAlive = false;

		es2DBImportConfig.setMode( mode);
		MongoDB2ESDataStreamImpl dataStream = new MongoDB2ESDataStreamImpl();
		dataStream.setMongoDB2ESImportConfig(es2DBImportConfig);
		return dataStream;
	}




}
