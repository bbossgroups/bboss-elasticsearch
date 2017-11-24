package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityCustomSerializationFactory extends BeanSerializerFactory {
	private static final long serialVersionUID = 1L;



	public EntityCustomSerializationFactory() {
		this(null);
	}
	protected EntityCustomSerializationFactory(SerializerFactoryConfig config) {
		super(config);
	}
	protected abstract  String[] getFilterFields();

	// ignored fields
	@Override
	protected void processViews(SerializationConfig config, BeanSerializerBuilder builder) {
		super.processViews(config, builder);
		// ignore fields only for concrete class
		// note, that you can avoid or change this check
		Class<?> beanClass = builder.getBeanDescription().getBeanClass();
		// if (builder.getBeanDescription().getBeanClass().equals(Entity.class))
		// {
		// get original writer
		List<BeanPropertyWriter> originalWriters = builder.getProperties();
		// create actual writers
		List<BeanPropertyWriter> writers = new ArrayList<BeanPropertyWriter>();
		String[] fs = this.getFilterFields();
		for (BeanPropertyWriter writer : originalWriters) {
			final String propName = writer.getName();
			// if it isn't ignored field, add to actual writers list
			boolean find = false;
			for(String f:fs) {
				if (f.equals(propName)) {
					find = true;
					break;
				}
			}
			if(!find){
				writers.add(writer);
			}
		}


		builder.setProperties(writers);
	}
}
