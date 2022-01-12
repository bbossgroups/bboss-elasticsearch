package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2020 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Description: 日期解析类，多线程不安全，只能单线程上下文使用</p>
 * <p></p>
 * <p>Copyright (c) 2020</p>
 * @Date 2022/1/11 19:56
 * @author biaoping.yin
 * @version 1.0
 */

public class DateFormats {
	private SimpleDateFormat dayDateFormat ;

	private SimpleDateFormat monthDateFormat;

	private SimpleDateFormat yearDateFormat ;
	public static final int indiceSplitPolicy_unKnown = -1;
	public static final int indiceSplitPolicy_splitByDay = 1;
	public static final int indiceSplitPolicy_splitByMonth = 2;
	public static final int indiceSplitPolicy_splitByYear = 3;
	/**
	 * 分表策略：
	 * yyyy  年 3
	 * yyyy.MM 月 2
	 * yyyy.MM.dd 天 1
	 * 未知 -1或者null
	 */
	private Integer indiceSplitPolicy ;

	/**
	 * 重置分表策略
	 */
	public void resetIndiceSplitPolicy(){
		indiceSplitPolicy = null;
	}

	public Date parserDate(String dateStr){
		if(dateStr == null || dateStr.equals("")){
			this.indiceSplitPolicy = indiceSplitPolicy_unKnown;
			return null;
		}
		Date date = null;
		try{
			date = dayDateFormat.parse(dateStr);
			this.indiceSplitPolicy = indiceSplitPolicy_splitByDay;
		}
		catch (Exception e){
			try {
				date = monthDateFormat.parse(dateStr);
				this.indiceSplitPolicy = indiceSplitPolicy_splitByMonth;
			}
			catch (Exception e1){
				try {
					date = yearDateFormat.parse(dateStr);
					this.indiceSplitPolicy = indiceSplitPolicy_splitByYear;
				}
				catch (Exception e2){
					this.indiceSplitPolicy = indiceSplitPolicy_unKnown;
				}
			}
		}
		return date;
	}
	public SimpleDateFormat getDayDateFormat() {
		return dayDateFormat;
	}

	public void setDayDateFormat(SimpleDateFormat dayDateFormat) {
		this.dayDateFormat = dayDateFormat;
	}

	public SimpleDateFormat getMonthDateFormat() {
		return monthDateFormat;
	}

	public void setMonthDateFormat(SimpleDateFormat monthDateFormat) {
		this.monthDateFormat = monthDateFormat;
	}

	public SimpleDateFormat getYearDateFormat() {
		return yearDateFormat;
	}

	public void setYearDateFormat(SimpleDateFormat yearDateFormat) {
		this.yearDateFormat = yearDateFormat;
	}

	public Integer getIndiceSplitPolicy() {
		return indiceSplitPolicy;
	}
}
