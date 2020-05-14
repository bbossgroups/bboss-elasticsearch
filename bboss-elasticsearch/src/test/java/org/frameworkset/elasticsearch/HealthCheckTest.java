package org.frameworkset.elasticsearch;

import org.frameworkset.elasticsearch.client.ESAddress;
import org.frameworkset.elasticsearch.client.HealthCheck;
import org.frameworkset.spi.remote.http.ClientConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class HealthCheckTest {
	@Test
	public void testCheck(){
		final List<ESAddress> esAddresses = new ArrayList<ESAddress>();
		esAddresses.add(new ESAddress("localhost:9200"));
//		esAddresses.add(new ESAddress("localhost2:9200"));
//		esAddresses.add(new ESAddress("localhost3:9200"));
		String elasticUser = "elastic", elasticPassword = "changeme";
//		Map<String,String> headers = new HashMap<String,String>();
//		headers.put("Authorization", ElasticSearchRestClient.getHeader(elasticUser, elasticPassword));
		String healthPool = ClientConfiguration.getHealthPoolName("default");
		final HealthCheck healthCheck = new HealthCheck("default",healthPool,esAddresses,5000);
		healthCheck.run();
		
		Thread r = new Thread(new Runnable(){

			@Override
			public void run() {
				
				esAddresses.get(0).setStatus(1);
				healthCheck.stopCheck();
			}
			
		});
		r.start();
	}
	
	public static void main(String[] args){
		HealthCheckTest test = new HealthCheckTest();
		test.testCheck();
	}

}
