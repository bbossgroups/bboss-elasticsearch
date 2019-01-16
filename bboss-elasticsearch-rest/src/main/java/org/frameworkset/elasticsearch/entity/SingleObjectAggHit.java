package org.frameworkset.elasticsearch.entity;

public class SingleObjectAggHit extends AggHit{
	private Object value;


	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
