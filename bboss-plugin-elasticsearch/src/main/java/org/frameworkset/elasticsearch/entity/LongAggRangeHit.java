package org.frameworkset.elasticsearch.entity;

public class LongAggRangeHit extends LongAggHit{
	private Long from ;
	private Long to;

	public Long getFrom() {
		return from;
	}

	public void setFrom(Long from) {
		this.from = from;
	}

	public Long getTo() {
		return to;
	}

	public void setTo(Long to) {
		this.to = to;
	}
}
