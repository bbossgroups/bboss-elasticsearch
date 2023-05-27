package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import org.frameworkset.util.ClassUtil;

public class ESIdEntityCustomSerializationFactory extends EntityCustomSerializationFactory{
	public static final String[] esIDIgnoreField = new String[]{"id"};
	@Override
	protected String[] getFilterFields(ClassUtil.ClassInfo classInfo) {
		return esIDIgnoreField;
	}
    public ESIdEntityCustomSerializationFactory() {
        super();
    }
    public ESIdEntityCustomSerializationFactory(SerializerFactoryConfig config) {
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

        return new ESIdEntityCustomSerializationFactory(config);
    }
}
