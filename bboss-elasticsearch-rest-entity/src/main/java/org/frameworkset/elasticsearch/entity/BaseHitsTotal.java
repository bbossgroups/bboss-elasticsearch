package org.frameworkset.elasticsearch.entity;
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

import java.io.Serializable;
import java.util.Map;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/4/11 23:06
 * @author biaoping.yin
 * @version 1.0
 */
public class BaseHitsTotal implements Serializable {
	/**
	 * 总的记录数
	 */
	protected long totalSize;
	/**
	 * since Elasticsearch 7.0
	 */
	protected String totalRelation;
	public long getTotalSize() {
		return totalSize;
	}

	public void setTotal(Object total) {
		if(total == null){
			return;
		}
		if(total instanceof Long){
			this.totalSize = ((Long)total).longValue();
		}
		else if(total instanceof Integer){
			this.totalSize = ((Integer)total).longValue();
		}
		else if(total instanceof Map){
			Map _total = (Map)total;
			Object t = _total.get("value");
			if(t != null) {
				if (t instanceof Long) {
					this.totalSize = ((Long) t).longValue();
				} else if (t instanceof Integer) {
					this.totalSize = ((Integer) t).longValue();
				} else {
					this.totalSize = Long.parseLong(t.toString());
				}
			}
			totalRelation = (String)_total.get("relation");
		}

//		this.totalSize = totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
}
