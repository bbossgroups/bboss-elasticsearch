package org.frameworkset.elasticsearch.serial;

public class ESIdEntityCustomSerializationFactory extends EntityCustomSerializationFactory{
	public static final String[] esIDIgnoreField = new String[]{"id"};
	@Override
	protected String[] getFilterFields() {
		return esIDIgnoreField;
	}
}
