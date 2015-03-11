/*
 * Copyright 2013 Alex Lin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opoo.press.util;

import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Alex Lin
 *
 */
public abstract class LinkUtils {
	
	public static void addDateParams(Map<String,Object> params, Date date){
		if(date == null){
			date = new Date();
		}
		if(params == null){
			return;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int year = c.get(Calendar.YEAR);
		int monthnum = c.get(Calendar.MONTH) + 1;
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		
		params.put("year", year + "");
		params.put("month", StringUtils.leftPad(monthnum + "", 2, '0'));
		params.put("day", StringUtils.leftPad(day + "", 2, '0'));
		params.put("hour", StringUtils.leftPad(hour + "", 2, '0'));
		params.put("minute", StringUtils.leftPad(minute + "", 2, '0'));
		params.put("second", StringUtils.leftPad(second + "", 2, '0'));
	}
}
