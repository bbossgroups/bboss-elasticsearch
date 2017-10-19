package org.frameworkset.elasticsearch.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.remote.http.HttpRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * es节点健康检查
 */
public class HealthCheck implements Runnable{
	private Collection<ESAddress> esAddresses;
	private static final String healthCheckHttpPool = "healthCheckHttpPool";
	private static Logger logger = LoggerFactory.getLogger(HealthCheck.class);
	private long checkInterval = 5000;
	private List<HCRunable> checkThreads ;
	private Map<String, String> headers;
	public HealthCheck(Collection<ESAddress> esAddresses,long checkInterval,Map<String, String> headers){
		this.esAddresses = esAddresses;
		this.checkInterval = checkInterval;
		this.headers = headers;

	}
	public void stopCheck(){
		HCRunable t = null;
		for(int i = 0; i < checkThreads.size(); i ++){
			t = checkThreads.get(i);
			t.stopRun();
		}
	}
	class HCRunable extends Thread {
		ESAddress address;
		boolean stop = false;
		public HCRunable(ESAddress address){
			this.address = address;
		}
		public void stopRun(){
			this.stop = true;
			this.interrupt();
		}
		@Override
		public void run() {
			 while (true){
			 	if(this.stop)
			 		break;
			 	 if(!address.ok()){			
			 		 try {		
			 			 if(logger.isDebugEnabled())
			 				 logger.debug(new StringBuilder().append("Check dead elasticsearch server[").append(address.toString()).append("] status.").toString());
						 HttpRequestUtil.httpGet(healthCheckHttpPool,address.getHealthPath(),headers,new ResponseHandler<Void>(){
	
							 @Override
							 public Void handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
								 int status = response.getStatusLine().getStatusCode();
								 if (status >= 200 && status < 300) {
									 if(logger.isInfoEnabled())
										 logger.info(new StringBuilder().append("Dead elasticsearch server[").append(address.toString()).append("] recovered to normal server.").toString());
									 address.setStatus(0);
								 } else {
									address.setStatus(1);
								 }
								 return null;
							 }
						 });
					
					 } catch (Exception e) {
						 if(logger.isInfoEnabled())
							 logger.info(new StringBuilder().append("Elasticsearch server[").append(address.toString()).append("] is dead.").toString());
						 address.setStatus(1);
					 }
			 	 }
				 if(this.stop)
				 		break;
				 try {
					 sleep(checkInterval);
				 } catch (InterruptedException e) {					 
					 break;
				 }
			 }
		}
	}
	@Override
	public void run() {
		Iterator<ESAddress> iterable = this.esAddresses.iterator();
		checkThreads = new ArrayList<HCRunable>();
		HCRunable t = null;
		ESAddress address = null;
		while(iterable.hasNext()){
			address = iterable.next();
			t = new HCRunable(address);
			t.start();
			checkThreads.add(t);
		}
		BaseApplicationContext.addShutdownHook(new Runnable() {
			@Override
			public void run() {
				stopCheck();
			}
		});
	}
}
