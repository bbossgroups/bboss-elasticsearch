package org.frameworkset.elasticsearch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.frameworkset.orm.annotation.Column;
import com.frameworkset.orm.annotation.PrimaryKey;
import org.frameworkset.elasticsearch.entity.ESBaseData;

import java.util.Date;

@JsonIgnoreProperties(allowSetters=true,value={"sfiled","sfiled1"})
public class Demo extends ESBaseData{
	@PrimaryKey
	private long demoId;
	private String contentbody;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	@Column(dataformat = "yyyy-MM-dd HH:mm:ss.SSS")
	private Date agentStarttime;
	private String applicationName;
	 
	private String sfiled;
	private String sfiled1;
	public String getContentbody() {
		return contentbody;
	}

	public void setContentbody(String contentbody) {
		this.contentbody = contentbody;
	}

	public Date getAgentStarttime() {
		return agentStarttime;
	}

	public void setAgentStarttime(Date agentStarttime) {
		this.agentStarttime = agentStarttime;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public long getDemoId() {
		return demoId;
	}

	public void setDemoId(long demoId) {
		this.demoId = demoId;
	}

	public String getSfiled() {
		return sfiled;
	}

	public void setSfiled(String sfiled) {
		this.sfiled = sfiled;
	}

	public String getSfiled1() {
		return sfiled1;
	}

	public void setSfiled1(String sfiled1) {
		this.sfiled1 = sfiled1;
	}
}
