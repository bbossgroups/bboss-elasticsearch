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

import org.frameworkset.elasticsearch.ElasticSearchException;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2020/1/23 20:26
 * @author biaoping.yin
 * @version 1.0
 */
public class DSLParserException extends ElasticSearchException {
	public DSLParserException(int httpStatusCode) {
		super(httpStatusCode);
	}

	public DSLParserException(String message, int httpStatusCode) {
		super(message, httpStatusCode);
	}

	public DSLParserException(Throwable cause, int httpStatusCode) {
		super(cause, httpStatusCode);
	}

	public DSLParserException(String message, Throwable cause, int httpStatusCode) {
		super(message, cause, httpStatusCode);
	}

	public DSLParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int httpStatusCode) {
		super(message, cause, enableSuppression, writableStackTrace, httpStatusCode);
	}

	public DSLParserException() {
	}

	public DSLParserException(String message) {
		super(message);
	}

	public DSLParserException(Throwable cause) {
		super(cause);
	}

	public DSLParserException(String message, Throwable cause) {
		super(message, cause);
	}

	public DSLParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
