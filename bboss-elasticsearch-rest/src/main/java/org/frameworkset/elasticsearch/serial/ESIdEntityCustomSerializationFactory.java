package org.frameworkset.elasticsearch.serial;

import org.frameworkset.util.ClassUtil;

public class ESIdEntityCustomSerializationFactory extends EntityCustomSerializationFactory{
	public static final String[] esIDIgnoreField = new String[]{"id"};
	@Override
	protected String[] getFilterFields(ClassUtil.ClassInfo classInfo) {
		return esIDIgnoreField;
	}
}
