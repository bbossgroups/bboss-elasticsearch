package org.frameworkset.elasticsearch.template;

import bboss.org.apache.velocity.runtime.resource.Resource;
import com.frameworkset.util.VariableHandler;

public class ESInfo {
	private TemplateMeta templatePro;
	private boolean tpl ;
	private ESTemplate estpl;
	private String templateName;
	private String template;
	private boolean multiparser;
	private ConfigDSLUtil configDSLUtil;
	private boolean cache;
	public ESInfo(String templateName, String template, boolean istpl, boolean multiparser, TemplateMeta templatePro, boolean cache) {
		this.template = template;
		this.templateName = templateName;
		this.tpl = istpl;
		this.multiparser = multiparser;
		this.templatePro = templatePro;
		this.cache = cache;
	}
	public String getDslFile(){
		return this.configDSLUtil.templateFile;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public boolean isTpl() {
		return tpl;
	}
	public void setTpl(boolean tpl) {
		this.tpl = tpl;
	}
	public ESTemplate getEstpl() {
		return estpl;
	}
	public void setEstpl(ESTemplate estpl) {
		this.estpl = estpl;
	}
	public TemplateMeta getTemplatePro() {
		return templatePro;
	}
	public void setTemplatePro(TemplateMeta templatePro) {
		this.templatePro = templatePro;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public boolean isMultiparser() {
		return multiparser;
	}
	public void setMultiparser(boolean multiparser) {
		this.multiparser = multiparser;
	}
	public ConfigDSLUtil getConfigDSLUtil() {
		return configDSLUtil;
	}
	public void setConfigDSLUtil(ConfigDSLUtil configDSLUtil) {
		this.configDSLUtil = configDSLUtil;
	}
	
	public boolean equals(Object obj)
	{
		if(obj == null)
			return false;
		if(obj instanceof ESInfo)
		{
			ESInfo o = (ESInfo)obj;
			return this.getTemplate().equals(o.getTemplate());
		}
		else
		{
			return false;
		}
	}
	public boolean fromConfig()
	{
		return this.configDSLUtil != null && this.configDSLUtil.fromConfig();
	}
	public int compareTo(ESInfo queryDSL)
	{
		return this.template.compareTo(queryDSL.getTemplate());
	}
	
	public ESInfo getESInfo(String sqlname)
	{
		return this.configDSLUtil.getESInfo( sqlname);
	}
	
	public String getPlainQueryDSL(String sqlname)
	{
		return this.configDSLUtil.getPlainTemplate( sqlname);
	}

	public VariableHandler.URLStruction getTemplateStruction(String template){
		return this.configDSLUtil.getTempateStruction(this,template);
	}

	public int hashCode(){
		return this.getTemplate().hashCode();
	}


	public boolean isCache() {
		return cache;
	}
}
