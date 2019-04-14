package org.frameworkset.spi.geoip;
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
 * @Date 2019/4/14 21:04
 * @author biaoping.yin
 * @version 1.0
 */
public class GeoIPHandlerException extends RuntimeException{
	public GeoIPHandlerException() {
	}

	public GeoIPHandlerException(String message) {
		super(message);
	}

	public GeoIPHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	public GeoIPHandlerException(Throwable cause) {
		super(cause);
	}

	public GeoIPHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
