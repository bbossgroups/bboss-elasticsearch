package org.frameworkset.tran.es;
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


import org.frameworkset.tran.Record;

import java.util.Map;
import java.util.Set;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/19 11:24
 * @author biaoping.yin
 * @version 1.0
 */
public class ESRecord implements Record {
	private Map<String,Object> data;
	public ESRecord(Object data){

		this.data = (Map<String, Object>) data;
	}
	@Override
	public Object getValue(String colName) {
		return data.get(colName);
	}

	@Override
	public Set<String> getKeys() {
		return data.keySet();
	}
	public Object getData(){
		return data;
	}

}
