package org.frameworkset.tran.kafka.codec;
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

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.frameworkset.tran.ESDataImportException;
import org.frameworkset.tran.kafka.KafkaImportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/20 20:31
 * @author biaoping.yin
 * @version 1.0
 */
public abstract class CodecUtil {
	private static final Logger logger = LoggerFactory.getLogger(CodecUtil.class);
	public static Deserializer getDeserializer(String codecType){
		if(codecType == null || codecType.equals(KafkaImportConfig.CODEC_TEXT)){
			return new StringDeserializer();
		}
		else if(codecType.equals(KafkaImportConfig.CODEC_JSON)){
			return new JsonDeserializer();
		}
		else if(codecType.equals(KafkaImportConfig.CODEC_LONG)){
			return new LongDeserializer();
		}
		else if(codecType.equals(KafkaImportConfig.CODEC_INTEGER)){
			return new IntegerDeserializer();
		}
		else{
			try {
				Class<Deserializer> clazz = (Class<Deserializer>) Class.forName(codecType);
				return clazz.newInstance();
			}
			catch (Exception e){
				if(logger.isErrorEnabled()) {
					logger.error("", e);
				}
				throw new ESDataImportException("",e);
			}
		}
	}
}
