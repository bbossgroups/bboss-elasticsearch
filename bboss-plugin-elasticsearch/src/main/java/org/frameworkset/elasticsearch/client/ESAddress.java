package org.frameworkset.elasticsearch.client;

public class ESAddress {
	private String address;
	/**
	 * 服务器状态：0 正常 1 异常
	 */
	private int status= 0;
	public ESAddress(){

	}
	public ESAddress(String address){
		this.address = address;
	}
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	public String toString(){
		return this.address;
	}
	public boolean ok(){
		return this.status == 0;
	}
}
