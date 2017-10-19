package org.frameworkset.elasticsearch.client;

public class ESAddress {
	private String address;
	private String healthPath;
	/**
	 * 服务器状态：0 正常 1 异常
	 */
	private volatile int status= 0;
	public ESAddress(){

	}

	public String getHealthPath() {
		return healthPath;
	}

	public void setHealthPath(String healthPath) {
		this.healthPath = healthPath;
	}

	public ESAddress(String address){
		if (!address.contains("http://") && !address.contains("https://")) {
			address = "http://" + address;
		}
		this.address = address;
		this.healthPath = this.getPath(address,"/");
	}
	private String getPath(String host,String path){
		String url = path.equals("") || path.startsWith("/")?
				new StringBuilder().append(host).append(path).toString()
				:new StringBuilder().append(host).append("/").append(path).toString();
		return url;
	}
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
		this.healthPath = this.getPath(address,"/");
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
