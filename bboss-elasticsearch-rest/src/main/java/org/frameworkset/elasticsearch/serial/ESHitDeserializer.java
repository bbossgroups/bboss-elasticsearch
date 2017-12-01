package org.frameworkset.elasticsearch.serial;

public class ESHitDeserializer extends BaseESHitDeserializer {
	public ESHitDeserializer(){

	}
	@Override
	protected ESClass getESInnerTypeReferences() {
		return ESSerialThreadLocal.getESTypeReferences();
	}

}
