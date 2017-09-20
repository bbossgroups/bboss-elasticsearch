package org.frameworkset.elasticsearch.entity;

public class DoubleAggRangeHit extends DoubleAggHit{
	private Double from ;
	private Double to;

	public Double getFrom() {
		return from;
	}

	public void setFrom(Double from) {
		this.from = from;
	}

	public Double getTo() {
		return to;
	}

	public void setTo(Double to) {
		this.to = to;
	}
}
