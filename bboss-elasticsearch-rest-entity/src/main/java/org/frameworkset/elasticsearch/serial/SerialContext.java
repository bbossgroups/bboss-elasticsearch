package org.frameworkset.elasticsearch.serial;
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

import java.util.Map;

/**
 * <p>Description: slice asyn context hold data serial type infomations.</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/14 19:24
 * @author biaoping.yin
 * @version 1.0
 */
public class SerialContext {
	private ESClass innerHitTypeLocals;
	private Map<String,ESClass> innerHitTypeLocalsByType;

	public SerialContext(ESClass innerHitTypeLocals, Map<String, ESClass> innerHitTypeLocalsByType) {
		this.innerHitTypeLocals = innerHitTypeLocals;
		this.innerHitTypeLocalsByType = innerHitTypeLocalsByType;
	}

	public void continueSerialTypes(){
		if(innerHitTypeLocals != null)
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences(innerHitTypeLocals);
		if(innerHitTypeLocalsByType != null){
			ESInnerHitSerialThreadLocal.setESInnerTypeReferences(innerHitTypeLocalsByType);
		}
	}
	public void clean(){
		ESInnerHitSerialThreadLocal.clean();
	}

}
