package org.frameworkset.elasticsearch.template;

import com.frameworkset.util.VariableHandler;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ESTemplateCache {
	private Lock lock = new ReentrantLock();
	private Lock vtplLock = new ReentrantLock();
	private Map<String,VariableHandler.URLStruction> parserTempateStructions = new java.util.HashMap<String,VariableHandler.URLStruction>();
	private Map<String,Map<String,VariableHandler.URLStruction>> parserVTPLTempateStructions = new java.util.HashMap<String,Map<String,VariableHandler.URLStruction>>();

	public ESTemplateCache() {
		// TODO Auto-generated constructor stub
	}


	public void clear()
	{
		parserTempateStructions.clear();
		parserVTPLTempateStructions.clear();
	}


	public VariableHandler.URLStruction getTemplateStruction(ESInfo sqlinfo, String template)
	{
		if(sqlinfo.isTpl() )
		{
			return this._getVTPLTemplateStruction(sqlinfo,template);
		}
		else
		{
			return _getTemplateStruction(sqlinfo, template);
		}

//		VariableHandler.URLStruction sqlstruction =  _parserTempateStructions.get(key);
//		if(sqlstruction == null)
//		{
//			try
//			{
//				_lock.lock();
//				sqlstruction =  _parserTempateStructions.get(key);
//				if(sqlstruction == null)
//				{
//					sqlstruction = VariableHandler.parserTempateStruction(template);
//					_parserTempateStructions.put(key,sqlstruction);
//				}
//			}
//			finally {
//				_lock.unlock();
//			}
//		}
//		return sqlstruction;
	}

	private VariableHandler.URLStruction _getTemplateStruction(ESInfo sqlinfo, String template)
	{

		String key = sqlinfo.getTemplateName();
		VariableHandler.URLStruction sqlstruction =  parserTempateStructions.get(key);
		if(sqlstruction == null)
		{
			try
			{
				lock.lock();
				sqlstruction =  parserTempateStructions.get(key);
				if(sqlstruction == null)
				{
					sqlstruction = VariableHandler.parserTempateStruction(template);
					parserTempateStructions.put(key,sqlstruction);
				}
			}
			finally {
				lock.unlock();
			}
		}
		return sqlstruction;
	}

	/**
	 * vtpl需要进行分级缓存
	 * @param sqlinfo
	 * @param template
	 * @return
	 */
	private VariableHandler.URLStruction _getVTPLTemplateStruction(ESInfo sqlinfo, String template)
	{

		String ikey = template;
		String okey = sqlinfo.getTemplateName();
		Map<String,VariableHandler.URLStruction> sqlstructionMap =  this.parserVTPLTempateStructions.get(okey);
		if(sqlstructionMap == null)
		{
			try
			{
				this.vtplLock.lock();
				sqlstructionMap =  this.parserVTPLTempateStructions.get(okey);
				if(sqlstructionMap == null)
				{
					sqlstructionMap = new   java.util.WeakHashMap<String,VariableHandler.URLStruction>();
					parserVTPLTempateStructions.put(okey,sqlstructionMap);
				}
			}
			finally {
				vtplLock.unlock();
			}
		}
		VariableHandler.URLStruction urlStruction = sqlstructionMap.get(ikey);
		if(urlStruction == null){
			try
			{
				this.vtplLock.lock();
				urlStruction = sqlstructionMap.get(ikey);
				if(urlStruction == null){
					urlStruction = VariableHandler.parserTempateStruction(template);
					sqlstructionMap.put(ikey,urlStruction);
				}
			}
			finally {
				this.vtplLock.unlock();
			}
		}
		return urlStruction;
	}

}
