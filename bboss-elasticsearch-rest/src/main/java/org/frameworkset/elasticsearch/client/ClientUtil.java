package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.template.TemplateContainer;

/**
 * document
 * https://esdoc.bbossgroups.com/#/README
 */
public abstract class ClientUtil implements ClientInterface{

	@Override
	public TemplateContainer getTemplatecontext(){
		return null;
	}
	@Override
	public String evalConfigDsl(String dslName,Object params){
		return null;
	}
}
