package org.frameworkset.tran.kafka;
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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.frameworkset.tran.Data;

import java.util.List;

/**
 * <p>Description: </p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/11/17 22:39
 * @author biaoping.yin
 * @version 1.0
 */
public class KafkaData implements Data<ConsumerRecord<Object,Object>> {
		private List<ConsumerRecord<Object,Object>> datas;
		public KafkaData(List<ConsumerRecord<Object,Object>> datas){
			this.datas = datas;
		}
		@Override
		public List<ConsumerRecord<Object,Object>> getDatas() {
			return datas;
		}
}
