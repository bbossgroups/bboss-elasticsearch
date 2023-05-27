package org.frameworkset.elasticsearch.serial;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import org.frameworkset.util.ClassUtil;

public class DefaultEntityCustomSerializationFactory extends EntityCustomSerializationFactory{
	@Override
	protected String[] getFilterFields(ClassUtil.ClassInfo classInfo) {
		return null;
	}
    public DefaultEntityCustomSerializationFactory() {
        super();
    }
    public DefaultEntityCustomSerializationFactory(SerializerFactoryConfig config) {
        super(config);
    }
    /**
     * Method used by module registration functionality, to attach additional
     * serializer providers into this serializer factory. This is typically
     * handled by constructing a new instance with additional serializers,
     * to ensure thread-safe access.
     */
    @Override
    public SerializerFactory withConfig(SerializerFactoryConfig config)
    {
        if (_factoryConfig == config) {
            return this;
        }

        return new DefaultEntityCustomSerializationFactory(config);
    }
}
