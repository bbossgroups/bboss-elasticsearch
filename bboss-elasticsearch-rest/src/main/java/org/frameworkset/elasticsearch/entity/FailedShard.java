package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
public class FailedShard  implements Serializable {
	private int shard;//": 0,
    private String index;//": "trace-2017.08.31",
    private String node;//": "ecJr-Zj3SQqJAIRyhYkdHQ",
    private RootCause reason;
	public FailedShard() {
		// TODO Auto-generated constructor stub
	}
	public int getShard() {
		return shard;
	}
	public void setShard(int shard) {
		this.shard = shard;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public RootCause getReason() {
		return reason;
	}
	public void setReason(RootCause reason) {
		this.reason = reason;
	}

}
