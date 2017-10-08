package org.frameworkset.elasticsearch.template;

import com.frameworkset.util.VariableHandler;

import java.util.Map;

public class ESTemplateCache {
	private Object lock = new Object();
	private Map<String,VariableHandler.URLStruction> parserTempateStructions = new java.util.WeakHashMap<String,VariableHandler.URLStruction>();
	public ESTemplateCache() {
		// TODO Auto-generated constructor stub
	}


	public void clear()
	{
		parserTempateStructions.clear();
	}


	public VariableHandler.URLStruction getTemplateStruction(ESInfo sqlinfo, String template)
	{

		String key = null;
		if(sqlinfo.isTpl() )
		{
			key = template;
		}
		else
		{
			key = sqlinfo.getTemplateName();
		}

		VariableHandler.URLStruction sqlstruction =  parserTempateStructions.get(key);
		if(sqlstruction == null)
		{
			synchronized(lock)
			{
				sqlstruction =  parserTempateStructions.get(key);
				if(sqlstruction == null)
				{
					sqlstruction = VariableHandler.parserTempateStruction(template);
					parserTempateStructions.put(key,sqlstruction);
				}
			}
		}
		return sqlstruction;
	}

}
