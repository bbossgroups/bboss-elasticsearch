package org.frameworkset.elasticsearch.serial;

public class ESClassType extends ESClass{
	protected  Class<?> hitClass;
	protected  Class<?> aggClass;
	public ESClassType(Class<?> hitClass){
		this.hitClass = hitClass;
	}
	public Class<?> getHitClass() {
		return hitClass;
	}




	public Class<?> getAggClass() {
		return aggClass;
	}

	public void setHitClass(Class<?> hitClass) {
		this.hitClass = hitClass;
	}

	public void setAggClass(Class<?> aggClass) {
		this.aggClass = aggClass;
	}
}
