package org.frameworkset.elasticsearch.serial;

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
