package org.frameworkset.elasticsearch.bulk;
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

import org.frameworkset.elasticsearch.client.ClientOptions;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/12/7 15:24
 * @author biaoping.yin
 * @version 1.0
 */
public class BulkData{
	public static final int INSERT = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;
	/**
	 * 0 - insert
	 * 1 - update
	 * 2 - delete
	 */
	private int type;
	private Object data;
	private boolean collection;
	private String indexType;
	private String index;
	private ClientOptions clientOptions;
	public List<Object> getDatas() {
		return datas;
	}
	public boolean isInsert(){
		return type == INSERT;
	}
	public boolean isDelete(){
		return type == DELETE;
	}
	public boolean isUpdate(){
		return type == UPDATE;
	}

	public String getElasticsearchBulkType(){
		if(isInsert())
			return "index";
		else if(isUpdate())
			return "update";
		else{
			return "delete";
		}
	}
	private List<Object> datas;
	public BulkData(int type,Object data){
		this.type = type;
		this.data = data;
	}
	public BulkData(int type,List<Object> datas){
		this.type = type;
		this.datas = datas;
		collection = true;
	}
	public int getType() {
		return type;
	}



	public Object getData() {
		return data;
	}


	public boolean isCollection() {
		return collection;
	}

	public String getIndexType() {
		return indexType;
	}

	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public ClientOptions getClientOptions() {
		return clientOptions;
	}

	public void setClientOptions(ClientOptions clientOptions) {
		this.clientOptions = clientOptions;
	}

	/**
	 * 如果是集合，返回集合中记录数，如果是单对象，返回1
	 * @return
	 */
	public int getDataSize(){
		if(this.isCollection()){
			return datas != null ?this.datas.size():0;
		}
		else{
			return 1;
		}
	}
}
