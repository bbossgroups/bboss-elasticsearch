package org.frameworkset.elasticsearch.template;
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

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/17 14:42
 * @author biaoping.yin
 * @version 1.0
 * @see org.frameworkset.elasticsearch.template.AOPTemplateMeta
 */
public interface TemplateMeta {
	/**
	 * 如果模板是一个引用，则需要指定引用对应的namespace，通过referenceTemplateName指定对应namespace下面的dsl模板名称
	 * @return
	 */
	String getReferenceNamespace();
	/**
	 * 如果模板是一个引用，则需要指定引用对应的namespace，通过referenceTemplateName指定对应namespace下面的dsl模板名称
	 * @return
	 */
	String getReferenceTemplateName();

	/**
	 * 标识DSl模板是否包含velocity语法，如果包含则需要进行velocity解析，否则不需要则设置为false
	 * @return
	 */
	Boolean getVtpl();

	/**
	 * 生成的dsl模板是否都要解析一次包含的#[XXX]变量信息，默认值为isTPL()方法返回值
	 * 只有包含velocity动态语法的dsl template才需要设置该标识
	 * @return
	 */
	Boolean getMultiparser();

	/**
	 * 返回dsl 模板值
	 * @return
	 */
	Object getDslTemplate();

	/**
	 * 返回模板名称
	 * @return
	 */
	String getName();

}
