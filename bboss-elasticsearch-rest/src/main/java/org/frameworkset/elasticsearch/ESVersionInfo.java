package org.frameworkset.elasticsearch;
/**
 * Copyright 2020 bboss
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: Get bboss elasticsearch client version info.</p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2021/7/24 9:48
 * @author biaoping.yin
 * @version 1.0
 */
public class ESVersionInfo {
    private static final String ES_VERSION = "7.5.6";
    private static final String ES_RELEASEDATE = "20251127";
    private static Logger logger = LoggerFactory.getLogger(ESVersionInfo.class);
    static {
        logger.info(getVersionDescription());
    }
	public static String getESVersion756(){
		return ES_VERSION+"_"+ES_RELEASEDATE;
	}

    public static String getESVersion(){
        return ES_VERSION;
    }

    /**
     * Returns the catenation of the description and cvs fields.
     * @return String with description
     */
    public static String getVersionDescription() {
        return "bboss elasticsearch client " + " Version: \t" + ES_VERSION + ",Release Date:\t" + ES_RELEASEDATE ;
    }
}
