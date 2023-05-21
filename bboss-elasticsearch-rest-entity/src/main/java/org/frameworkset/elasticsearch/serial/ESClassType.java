package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.databind.JavaType;

import java.util.List;

public class ESClassType extends ESClass{
	protected  Class<?> hitClass;
    protected List<Class> mhitClass;
    protected JavaType javaType;
    protected  Class<?> aggClass;
	public ESClassType(Class<?> hitClass){
		this.hitClass = hitClass;
	}
    public ESClassType(JavaType javaType) {
        this.javaType = javaType;
    }
    public ESClassType(List<Class> mhitClass){
        this.mhitClass = mhitClass;
    }
	public Class<?> getHitClass() {
		return hitClass;
	}


    public JavaType getJavaType() {
        return javaType;
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
