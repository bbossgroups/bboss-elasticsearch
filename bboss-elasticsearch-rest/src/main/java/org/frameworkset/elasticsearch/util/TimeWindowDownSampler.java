package org.frameworkset.elasticsearch.util;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.util.concurrent.TimeUnit;

public class TimeWindowDownSampler  implements TimeWindowSampler {

	private static final long ONE_SECOND = 1000;
	private static final long FIVE_SECOND = 5000;
	private static final long TEN_SECOND = 10000;
	private static final long THREE_MINUTE = 3*60*1000;
	private static final long TWENTY_SECOND = 20*1000;
	private static final long THIRTY_SECOND = 30*1000;
	private static final long FIFTY_SECOND = 50*1000;
	private static final long SIX_MINUTE = 6*60*1000;
	private static final long TEN_MINUTE = 10*60*1000;
	private static final long FIFTY_MINUTE = 50*60*1000;
	private static final long TWENTY_MINUTE = 20*60*1000;
	private static final long TWENTY_FIVE_MINUTE = 25*60*1000;
	private static final long THIRTY_MINUTE = 30*60*1000;
	private static final long ONE_MINUTE = 6000 * 10;
	private static final long ONE_HOUR = TimeUnit.HOURS.toMillis(1);
	private static final long SIX_HOURS = TimeUnit.HOURS.toMillis(6);
	private static final long TWELVE_HOURS = TimeUnit.HOURS.toMillis(12);
	private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
	private static final long TWO_DAY = TimeUnit.DAYS.toMillis(2);
	private static final String ONE_SECOND_INTERVAL = "1s";
	private static final String FIVE_SECOND_INTERVAL = "5s";
	private static final String TEN_SECOND_INTERVAL = "10s";
	private static final String TWENTY_SECOND_INTERVAL = "20s";
	private static final String THIRTY_SECOND_INTERVAL = "30s";
	private static final String FIFTY_SECOND_INTERVAL = "50s";
	private static final String ONE_MINUTE_INTERVAL = "1m";
	private static final String THREE_MINUTE_INTERVAL = "3m";
	private static final String SIX_MINUTE_INTERVAL = "6m";
	private static final String TEN_MINUTE_INTERVAL = "10m";
	private static final String FIFTY_MINUTE_INTERVAL = "15m";
	private static final String TWENTY_MINUTE_INTERVAL = "20m";
	private static final String TWENTY_FIVE_MINUTE_INTERVAL = "25m";
	private static final String THIRTY_MINUTE_INTERVAL = "30m";
	private static final String ONE_HOUR_INTERVAL = "1h";
	private static final String SIX_HOURS_INTERVAL = "6h";
	private static final String TWELVE_HOURS_INTERVAL = "12h";
	private static final String ONE_DAY_INTERVAL = "1d";
	private static final String TWO_DAY_INTERVAL = "2d";


	public static final TimeWindowSampler SAMPLER = new TimeWindowDownSampler();

	@Override
	public long getWindowSize(long from ,long to) {
		final long diff = to - from;
		long size;
		if (diff <= ONE_HOUR) {
			size = ONE_MINUTE;
		} else if (diff <= SIX_HOURS) {
			size = ONE_MINUTE * 5;
		} else if (diff <= TWELVE_HOURS) {
			size = ONE_MINUTE * 10;
		} else if (diff <= ONE_DAY) {
			size = ONE_MINUTE * 20;
		} else if (diff <= TWO_DAY) {
			size = ONE_MINUTE * 30;
		} else {
			size = ONE_MINUTE * 60;
		}

		return size;
	}

	@Override
	public String getWindowInterval(long from, long to) {
		final long diff = to - from;
		String interval = TEN_SECOND_INTERVAL;
		if(diff <= TimeWindowDownSampler.ONE_HOUR)
			return interval;
		else if(diff <= TimeWindowDownSampler.SIX_HOURS)
			return TimeWindowDownSampler.THIRTY_SECOND_INTERVAL;
		if (diff <= TWELVE_HOURS) {
			return TimeWindowDownSampler.ONE_MINUTE_INTERVAL;

		} else if (diff <= ONE_DAY) {
			return TimeWindowDownSampler.SIX_MINUTE_INTERVAL;
		} else if (diff <= TWO_DAY) {
			return TimeWindowDownSampler.THIRTY_MINUTE_INTERVAL;
		} else {
			return TimeWindowDownSampler.ONE_HOUR_INTERVAL;
		}


	}
}
