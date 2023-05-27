package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import org.frameworkset.elasticsearch.entity.ESBaseData;
import org.frameworkset.util.ClassUtil;

import java.util.List;

public class ESBaseDataEntityCustomSerializationFactory extends EntityCustomSerializationFactory{
//	public static final String[] esBaseDataIgnoreField = new String[]{"type",
//			"id",
//			"fields",
//			"version",
//			"index",
//			"highlight",
//			"sort",
//			"score",
//			"parent",
//			"routing",
//			"found","nested","innerHits"
//			};
	public static final String[] esBaseDataIgnoreField ;
	static {
		ClassUtil.ClassInfo classInfo =  ClassUtil.getClassInfo(ESBaseData.class);
		List<ClassUtil.PropertieDescription> fields = classInfo.getPropertyDescriptors();
		esBaseDataIgnoreField = new String[fields.size()];
		for(int i =0 ; i < fields.size();i ++){
			ClassUtil.PropertieDescription field = fields.get(i);
			esBaseDataIgnoreField[i] = field.getName();
		}
	}
    public ESBaseDataEntityCustomSerializationFactory() {
        super();
    }
    public ESBaseDataEntityCustomSerializationFactory(SerializerFactoryConfig config) {
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

        return new ESBaseDataEntityCustomSerializationFactory(config);
    }
	@Override
	protected String[] getFilterFields(ClassUtil.ClassInfo classInfo) {
		return esBaseDataIgnoreField;
	}

	public static void main(String[] args){
		ClassUtil.ClassInfo classInfo =  ClassUtil.getClassInfo(ESBaseData.class);
		List<ClassUtil.PropertieDescription> test = classInfo.getPropertyDescriptors();
		for(ClassUtil.PropertieDescription desc:test){
			System.out.println(desc.getName());
		}
	}
}
