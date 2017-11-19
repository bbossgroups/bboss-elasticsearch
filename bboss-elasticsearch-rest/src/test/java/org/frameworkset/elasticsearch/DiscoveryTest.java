package org.frameworkset.elasticsearch;

import org.frameworkset.elasticsearch.client.HostDiscover;

public class DiscoveryTest {
	public static void main(String[] args){
		Thread t = new Thread(new HostDiscover(null));
		t.start();
	}
}
