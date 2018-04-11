package org.frameworkset.elasticsearch.serial;

public class ESBaseDataEntityCustomSerializationFactory extends EntityCustomSerializationFactory{
	public static final String[] esBaseDataIgnoreField = new String[]{"type",
			"id",
			"fields",
			"version",
			"index",
			"highlight",
			"sort",
			"score",
			"parent",
			"routing",
			"found","nested","innerHits"
			};
	@Override
	protected String[] getFilterFields() {
		return esBaseDataIgnoreField;
	}
}
