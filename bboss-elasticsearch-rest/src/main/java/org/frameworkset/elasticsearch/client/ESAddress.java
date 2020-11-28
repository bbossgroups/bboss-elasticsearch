package org.frameworkset.elasticsearch.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESAddress {
	private String address;
	private String healthPath;
	private transient Thread healthCheck;
	private static final Logger logger = LoggerFactory.getLogger(ESAddress.class);
	/**
	 * 服务器状态：0 正常 1 异常 2 removed
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
		synchronized (this) {
			if(status == 0){
				this.status = status;
				return;
			}
			else if (this.status == 2) {//删除节点，无需修改状态
				return;
			}
			this.status = status;
		}


		if(status == 1 && healthCheck != null){
			synchronized(healthCheck){
				healthCheck.notifyAll();
			}
		}
	}
	
	public void onlySetStatus(int status) {
		synchronized (this) {
			if(status == 0) {
				this.status = status;
			}
			else if (this.status != 2)//如果没有移除，则设置故障状态
				this.status = status;
		}
		
	}
	/**
	 * 没有配置health检查地址的情况下，被动恢复异常状态节点为正常状态
	 *
	 */
	public void recover(){
		if(status == 1){//failed
			synchronized (this){
				if(status == 1){
					status = 0;
					logger.info("Recover failed node {} to normal node.",address);
				}
			}
		}
	}
	public String toString(){
		return this.address;
	}
	public boolean ok(){
		return this.status == 0;
	}

	public boolean okOrFailed(){
		return this.status == 0 || this.status == 1;
	}

	public boolean failedCheck(){
		return this.status == 1;
	}

	public void setHealthCheck(Thread healthCheck) {
		this.healthCheck = healthCheck;
	}
	public boolean equals(Object obj){
		if(obj == null)
			return false;
		if(obj instanceof  ESAddress){
			String other = ((ESAddress)obj).getAddress();
			return other != null && other.equals(this.getAddress());
		}
		return  false;
	}
}
