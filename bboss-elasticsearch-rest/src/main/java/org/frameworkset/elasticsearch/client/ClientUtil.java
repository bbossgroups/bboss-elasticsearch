package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.template.TemplateContainer;

/**
 * document
 * https://esdoc.bbossgroups.com/#/README
 */
public abstract class ClientUtil implements ClientInterface{

	public TemplateContainer getTemplatecontext(){
		return null;
	}
	public String evalConfigDsl(String dslName,Object params){
		return null;
	}
}
