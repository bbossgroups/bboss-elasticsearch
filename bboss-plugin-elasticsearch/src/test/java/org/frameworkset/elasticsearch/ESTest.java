package org.frameworkset.elasticsearch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.spi.DefaultApplicationContext;
import org.frameworkset.spi.remote.http.MapResponseHandler;
import org.frameworkset.spi.remote.http.StringResponseHandler;
import org.frameworkset.util.FastDateFormat;
import org.junit.Test;

public class ESTest {

	public ESTest() {
		// TODO Auto-generated constructor stub
	}
	@Test
	public void testFastDateFormat() throws ParseException{
		String data = "2005-01-10 12:00:00";
		String format = "yyyy-MM-dd HH:mm:ss";
		FastDateFormat df = FastDateFormat.getInstance(format,TimeZone.getTimeZone("Asia/Shanghai"));
		Object ojb = df.parseObject(data);
		System.out.println();
	}
	@Test
	public void test() throws Exception{
		DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
		ElasticSearch elasticSearchSink = context.getTBeanObject("elasticSearch", ElasticSearch.class);
//		ElasticSearch restelasticSearchSink = context.getTBeanObject("restelasticSearch", ElasticSearch.class);
		
		ClientUtil clientUtil = elasticSearchSink.getRestClientUtil();
		String entity = "{"+
    "\"aggs\": {"+
    "\"top_tags\": {"+
		    "\"terms\": {"+
		    "\"field\": \"rpc.keyword\","+
		    "\"size\": 30"+
		    "},"+
    "\"aggs\": {"+
		    "\"top_sales_hits\": {"+
		    "\"top_hits\": {"+
		    "\"sort\": ["+
		               "{"+
    "\"collectorAcceptTime\": {"+
		            	    "\"order\": \"desc\""+
		            	        "}"+
    "}"+
    "],"+
    "\"_source\": {"+
		            	    "\"includes\": [ \"collectorAcceptTime\", \"rpc\" ]"+
		            	    	    "},"+
    "\"size\" : 1"+
    "}"+
    "}"+
    "}"+
    "}"+
    "}"+
    "}";
		String response = (String) clientUtil.executeRequest("trace-*/_search?size=0",entity);
		
		System.out.println(response);
		
	}
	@Test
	public void querey() throws Exception
	{
		DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
		ElasticSearch elasticSearchSink = context.getTBeanObject("elasticSearch", ElasticSearch.class);
//		ElasticSearch restelasticSearchSink = context.getTBeanObject("restelasticSearch", ElasticSearch.class);
		
		ClientUtil clientUtil = elasticSearchSink.getRestClientUtil();
		String entiry = "{\"query\" : {\"term\" : { \"rpc\" : \"content.page\" }}}";
		String response = (String) clientUtil.executeRequest("trace-*/_search",entiry);
		
		System.out.println(response);
	}
	@Test
	public void testConfig() throws ParseException{
		
		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("org/frameworkset/elasticsearch/ESTracesMapper.xml");
		TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
		traceExtraCriteria.setApplication("testweb1");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
		traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-10 00:00:00").getTime());
		 String data = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new StringResponseHandler());
	        System.out.println("------------------------------");
	        System.out.println(data);
	        System.out.println("------------------------------");
	        
	        Map<String,Object> response = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new MapResponseHandler());
	        if(response.containsKey("error")){
	            return ;
	        }
	}
	
	@Test
	public void testSearh() throws ParseException{
		
		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("org/frameworkset/elasticsearch/ESTracesMapper.xml");
		TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
		traceExtraCriteria.setApplication("testweb1");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
		traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-10 00:00:00").getTime());
		 String data = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new StringResponseHandler());
	        System.out.println("------------------------------");
	        System.out.println(data);
	        System.out.println("------------------------------");
	        
	        Map<String,Object> response = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new MapResponseHandler());
	        if(response.containsKey("error")){
	            return ;
	        }
	}

}
