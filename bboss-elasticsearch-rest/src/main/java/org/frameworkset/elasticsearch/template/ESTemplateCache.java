package org.frameworkset.elasticsearch.template;

import com.frameworkset.util.SimpleStringUtil;
import com.frameworkset.util.VariableHandler;
import org.frameworkset.cache.EdenConcurrentCache;
import org.frameworkset.cache.MissingStaticCache;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class ESTemplateCache {
	private static Logger logger = LoggerFactory.getLogger(ESTemplateCache.class);
	private Lock lock = new ReentrantLock();
	private Lock vtplLock = new ReentrantLock();
	private Map<String,VariableHandler.URLStruction> parserTempateStructions = new java.util.HashMap<String,VariableHandler.URLStruction>();
	private Map<String, EdenConcurrentCache<String,VariableHandler.URLStruction>> parserVTPLTempateStructions;
	private Map<String, MissingStaticCache<String,VariableHandler.URLStruction>> parserVTPLTempateStructionsMissingCache;
	private static TempateStructionBuiler tempateStructionBuiler = new TempateStructionBuiler();
	private int perKeyDSLStructionCacheSize;
	private boolean alwaysCacheDslStruction = false;
	private long warnInterval = 500;
	public ESTemplateCache(int perKeyDSLStructionCacheSize,boolean alwaysCacheDslStruction) {
		this.perKeyDSLStructionCacheSize = perKeyDSLStructionCacheSize;
		this.alwaysCacheDslStruction = alwaysCacheDslStruction;
		if(this.alwaysCacheDslStruction) {
			parserVTPLTempateStructions = new java.util.HashMap<String, EdenConcurrentCache<String, VariableHandler.URLStruction>>();
		}
		else{
			parserVTPLTempateStructionsMissingCache = new java.util.HashMap<String,MissingStaticCache<String,VariableHandler.URLStruction>>();
		}
	}


	public void clear()
	{
		parserTempateStructions.clear();
		if(parserVTPLTempateStructions != null)
			parserVTPLTempateStructions.clear();
		if(parserVTPLTempateStructionsMissingCache != null)
			parserVTPLTempateStructionsMissingCache.clear();

	}


	public VariableHandler.URLStruction getTemplateStruction(ESInfo sqlinfo, String template)
	{
		if(sqlinfo.isTpl() )
		{
			if(this.alwaysCacheDslStruction) {
				return this._getVTPLTemplateStructionAlwaysCache(sqlinfo, template);
			}
			else {
				return this._getVTPLTemplateStructionStopCache(sqlinfo,template);
			}
		}
		else
		{
			return _getTemplateStruction(sqlinfo, template);
		}


	}

	public static class TempateVariable extends VariableHandler.Variable{
		/**
		 * 控制字符串变量是否需要添加""
		 * true 添加，默认添加
		 * false 不添加
		 * 模板变量的命名格式可以为：aaa,noquoted,dateformat=yyyy-MM-dd HH:mm:ss
		 */
		protected boolean quoted = true;
		protected String dateFormat ;
		protected String locale;
		protected String timeZone;
		private Boolean escape;





		/**
		 * 对elasticsearch关键字符进行转义处理
		 */
		private boolean esEncode = false;
		private Boolean serialJson ;
		protected DateFormateMeta dateFormateMeta;
		/**
		 * 在变量左边追加lpad对应的字符
		 */
		protected String lpad;
		/**
		 * 在变量的右边追加rpad对应的字符
		 */
		protected String rpad;
		private int escapeCount = 1;
		public TempateVariable(){
			super();
		}

		/**
		 * 处理pad数据
		 * @param pad_
		 * @return
		 */
		private String handlePad(String pad_){
			int idx = pad_.indexOf("|");
			String pad = null;

			if(idx > 0 ){
				String value = pad_.substring(0,idx);
				int count = Integer.parseInt(pad_.substring(idx+1));
				pad = value;
				if(count > 0) {
					for (int j = 1; j < count; j++) {
						pad = pad + value;
					}
				}

			}
			else{
				pad = pad_;
			}
			return pad;
		}

		public Boolean getEsEncode() {
			return esEncode;
		}
		public void after(){
			super.after();
			if(this.attributes != null) {
//				int pos = this.attributes.indexOf(",");
				String[] ts = attributes.split(",");

				for (int i = 0; i < ts.length; i ++) {
					String t = ts[i];
					if (t.startsWith("quoted=")) {
						String q = t.substring("quoted=".length()).trim();
						if(q.equals("false"))
							quoted = false;
					}
					else if(t.startsWith("dateformat=")){
						dateFormat= t.substring("dateformat=".length()).trim();
					}
					else if(t.startsWith("locale=")){
						locale= t.substring("locale=".length()).trim();
					}
					else if(t.startsWith("timezone=")){
						timeZone = t.substring("timezone=".length()).trim();
					}
					else if(t.startsWith("lpad=")){
						String lpad_= t.substring("lpad=".length()).trim();
						this.lpad = handlePad(lpad_);

					}
					else if(t.startsWith("rpad=")){
						String rpad_ = t.substring("rpad=".length()).trim();
						this.rpad = handlePad(rpad_);
					}
					else if(t.startsWith("escape=")){
						String escape_ = t.substring("escape=".length()).trim();
						if(escape_.equals("false")) {
                            escape = false;
                        }
						else if(escape_.equals("true")) {
                            escape = true;
                        }

					}
					else if(t.startsWith("esEncode=")){
						String esEncode_ = t.substring("esEncode=".length()).trim();
						if(esEncode_.equals("true"))
							esEncode = true;

					}
					else if(t.startsWith("serialJson=")){
						String serialJson_ = t.substring("serialJson=".length()).trim();
						if(serialJson_.equals("false"))
							serialJson = false;
						else if(serialJson_.equals("true"))
							serialJson = true;

					}else if(t.startsWith("escapeCount=")){
						String escapeCount_ = t.substring("escapeCount=".length()).trim();
						if(SimpleStringUtil.isNotEmpty(escapeCount_)) {
							try {
								escapeCount = Integer.parseInt(escapeCount_);
							}
							catch (Exception e){
								logger.error("escapeCount must be a nummber:"+escapeCount_,e);
							}
						}
					}

				}

				if(this.dateFormat != null){
					this.dateFormateMeta = DateFormateMeta.buildDateFormateMeta(this.dateFormat,this.locale);
				}

			}

		}

		public boolean isQuoted() {
			return quoted;
		}

		public String getDateFormat() {
			return dateFormat;
		}

		public String getLocale() {
			return locale;
		}

		public DateFormateMeta getDateFormateMeta() {
			return dateFormateMeta;
		}

		public String getTimeZone() {
			return timeZone;
		}

		public String getLpad() {
			return lpad;
		}

		public String getRpad() {
			return rpad;
		}

		public Boolean getEscape() {
			return escape;
		}

		public void setEscape(Boolean escape) {
			this.escape = escape;
		}

		public int getEscapeCount() {
			return escapeCount;
		}

		public void setEscapeCount(int escapeCount) {
			this.escapeCount = escapeCount;
		}

		public Boolean getSerialJson() {
			return serialJson;
		}

		public void setSerialJson(Boolean serialJson) {
			this.serialJson = serialJson;
		}
	}
	static class TempateStructionBuiler extends VariableHandler.URLStructionBuiler {
		@Override
		public VariableHandler.Variable buildVariable() {
			return new TempateVariable();
		}

	}
	private VariableHandler.URLStruction _getTemplateStruction(ESInfo sqlinfo, String template)
	{

		String key = sqlinfo.getTemplateName();
		VariableHandler.URLStruction sqlstruction =  parserTempateStructions.get(key);
		if(sqlstruction == null)
		{
			lock.lock();
			try
			{

				sqlstruction =  parserTempateStructions.get(key);
				if(sqlstruction == null)
				{
					sqlstruction = VariableHandler.parserStruction(template,tempateStructionBuiler);
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
	 * @param dslinfo
	 * @param dsl
	 * @return
	 */
	private VariableHandler.URLStruction _getVTPLTemplateStructionStopCache(ESInfo dslinfo, String dsl)
	{

		VariableHandler.URLStruction urlStruction = null;
		if(dslinfo.isCache()) {
			String ikey = dsl;
			String okey = dslinfo.getTemplateName();
			MissingStaticCache<String, VariableHandler.URLStruction> sqlstructionMap = this.parserVTPLTempateStructionsMissingCache.get(okey);
			if (sqlstructionMap == null) {
				this.vtplLock.lock();
				try {

					sqlstructionMap = this.parserVTPLTempateStructionsMissingCache.get(okey);
					if (sqlstructionMap == null) {
						sqlstructionMap = new MissingStaticCache<String, VariableHandler.URLStruction>(perKeyDSLStructionCacheSize);
						parserVTPLTempateStructionsMissingCache.put(okey, sqlstructionMap);
					}
				} finally {
					vtplLock.unlock();
				}
			}
			if (sqlstructionMap.stopCache()) {
				long missing = sqlstructionMap.increamentMissing();
				if (logger.isWarnEnabled() && sqlstructionMap.needLogWarn(missing,warnInterval)) {
					logDslStructionWarn(dslinfo, dsl, okey, sqlstructionMap.getMissesMax(),missing);
				}
				return VariableHandler.parserStruction(dsl, tempateStructionBuiler);
			}
			urlStruction = sqlstructionMap.get(ikey);
			if (urlStruction == null) {
				this.vtplLock.lock();
				try {

					urlStruction = sqlstructionMap.get(ikey);
					if (urlStruction == null) {
						long missing = sqlstructionMap.increamentMissing();
						urlStruction = VariableHandler.parserStruction(dsl, tempateStructionBuiler);
						if (!sqlstructionMap.stopCache()) {
							sqlstructionMap.put(ikey, urlStruction);
						} else {
							if (logger.isWarnEnabled()&& sqlstructionMap.needLogWarn(missing,warnInterval)) {
								logDslStructionWarn(dslinfo, dsl, okey, sqlstructionMap.getMissesMax(),missing);
							}
						}

					}
				} finally {
					this.vtplLock.unlock();
				}
			}
		}
		else{
			urlStruction = VariableHandler.parserStruction(dsl, tempateStructionBuiler);
		}

		return urlStruction;
	}

	/**
	 * vtpl需要进行分级缓存
	 * @param dslinfo
	 * @param dsl
	 * @return
	 */
	private VariableHandler.URLStruction _getVTPLTemplateStructionAlwaysCache(ESInfo dslinfo, String dsl)
	{
		VariableHandler.URLStruction urlStruction = null;
		if(dslinfo.isCache()) {
			String ikey = dsl;
			String okey = dslinfo.getTemplateName();
			EdenConcurrentCache<String, VariableHandler.URLStruction> sqlstructionMap = this.parserVTPLTempateStructions.get(okey);
			if (sqlstructionMap == null) {
				this.vtplLock.lock();
				try {

					sqlstructionMap = this.parserVTPLTempateStructions.get(okey);
					if (sqlstructionMap == null) {
						sqlstructionMap = new EdenConcurrentCache<String, VariableHandler.URLStruction>(perKeyDSLStructionCacheSize);
						parserVTPLTempateStructions.put(okey, sqlstructionMap);
					}
				} finally {
					vtplLock.unlock();
				}
			}

			urlStruction = sqlstructionMap.get(ikey);
			boolean outOfSize = false;
			long missing = 0l;
			if (urlStruction == null) {
				this.vtplLock.lock();
				try {

					urlStruction = sqlstructionMap.get(ikey);
					if (urlStruction == null) {
						missing = sqlstructionMap.increamentMissing();
						urlStruction = VariableHandler.parserStruction(dsl, tempateStructionBuiler);

						outOfSize = sqlstructionMap.put(ikey, urlStruction);


					}
				} finally {
					this.vtplLock.unlock();
				}
				if (outOfSize && logger.isWarnEnabled() && sqlstructionMap.needLogWarn(missing,warnInterval)) {
					logDslStructionWarn(dslinfo, dsl, okey, sqlstructionMap.getMaxSize(),missing);
				}
			}
		}
		else{
			urlStruction = VariableHandler.parserStruction(dsl, tempateStructionBuiler);
		}

		return urlStruction;
	}

	private void logDslStructionWarn(ESInfo dslinfo,String dsl,String okey,int maxSize,long missing){
		StringBuilder info = new StringBuilder();

		info.append("\n\r**********************************************************************\r\n")
				.append("*********************************WARNING:Missing cache ").append(missing).append(" times of DSL [").append(okey).append("@").append(dslinfo.getDslFile()).append("]*********************************\r\n")
				.append(dslinfo.getTemplate())
				.append("\r\n**********************************************************************\r\n")
				.append("When calling method _getVTPLTemplateStruction to obtain [")
				.append(dsl).append("]'s DSL structure information from DSL construction cache, ")
				.append("it was detected that the number of real dsl cache records exceeded the maximum cache size ")
				.append(maxSize)
				.append(" allowed by DSL structure cache.")
				.append("\r\nCause analysis of WARNING:")
				.append("\r\n1.Frequently varying value parameters may exist in this DSL;")
				.append("\r\n2.Variables of the $var pattern that may exist in this DSL and the value of $var changes frequently;")
				.append("\r\nOptimization suggestion：Change $var to #[var]\r\nIn order to improve the system performance, we can convert the value parameters that may change frequently in this DSL into #[variable] variables or the variables that may exist in the $var mode in DSL into #[varibale] mode variables.")
				.append("\r\nIf you need to convert an array or a list, use the variable #[variable] first and set the serialJson attribute: #[variable, serialJson = true] to improve system performance!")
				.append("\r\nHow to use of #[varibale] pattern variables in foreach loops refers to the section [5.3.3 Logical Judgment and Foreach Loop Example] in the document: https://esdoc.bbossgroups.com/#/development?id=_533-application%E5%8F%98%E9%87%8F%E4%BD%BF%E7%94%A8")
				.append("\r\nYou can also close the cache function by set cacheDsl=\"false\" of DSL [").append(okey).append("@").append(dslinfo.getDslFile()).append("]")

				.append("\n\r**********************************************************************")
				.append("\n\r**********************************************************************");

		logger.warn(info.toString());

	}

}
