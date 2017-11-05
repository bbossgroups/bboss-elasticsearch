package org.frameworkset.elasticsearch.entity;

public class FloatAggRangeHit extends FloatAggHit{
	private Float from ;
	private Float to;

	public Float getFrom() {
		return from;
	}

	public void setFrom(Float from) {
		this.from = from;
	}

	public Float getTo() {
		return to;
	}

	public void setTo(Float to) {
		this.to = to;
	}
}
