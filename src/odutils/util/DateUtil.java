/* 

Copyright 2021 aholinch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package odutils.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateUtil 
{
	public static void setDefaultTimeZone()
	{
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));	
	}
	
	/**
	 * Attempt to parse date
	 * 
	 * 
	 * @param str
	 * @return
	 */
	public static Date getDate(String str)
	{
		DateUtil.setDefaultTimeZone();
		Date d = null;
		str = str.trim();
		if(str.length()>10)
		{
			if(!str.contains("T"))
			{
				str = str.replace(' ','T');
			}
			d = DateUtil.parseISODate(str);
		}
		else if(str.length()>7)
		{
			String sa[] = null;
			if(str.contains("-"))
			{
				sa = str.split("-");
			}
			else if(str.contains("/"))
			{
				sa = str.split("/");
			}
			else if(str.length() == 8)
			{
				sa = new String[3];
				sa[0]=str.substring(0,4);
				sa[1]=str.substring(4,6);
				sa[2]=str.substring(6);
			}
			
			if(sa != null && sa.length>2)
			{
				int yr = gi(sa[0]);
				int mn = gi(sa[1]);
				int dy = gi(sa[2]);
				d = DateUtil.getDate(yr, mn, dy);
			}
		}
		
		return d;
	}

	public static int gi(String str)
	{
		int num = 0;
		try {num = Integer.parseInt(str);}catch(Exception ex) {};
		return num;
	}
	
	//"2017-01-14T00:30:00Z"
	public static Date parseISODate(String str)
	{
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	
		if(str.startsWith("\""))str = str.substring(1);
		if(str.endsWith("\"")) str = str.substring(0, str.length()-1);
		if(str.length()<19) return null;
		
		int y = 0;
		int m = 0;
		int d = 0;
		int hh = 0;
		int mm = 0;
		int ss = 0;
		int ms = 0;
		try
		{
			y = Integer.parseInt(str.substring(0,4));
			m = Integer.parseInt(str.substring(5,7));
			d = Integer.parseInt(str.substring(8,10));
			hh = Integer.parseInt(str.substring(11,13));
			mm = Integer.parseInt(str.substring(14,16));
			ss = Integer.parseInt(str.substring(17,19));
			str = str.substring(19);
			
			if(str.length()>0)
			{
				if(str.startsWith("."))
				{
					ms = (int)(1000.0d*Double.parseDouble("0"+str.trim()));
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return getDate(y,m,d,hh,mm,ss,ms);
	}
	
	public static int getYear(Date d)
	{
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(d);
    	return gc.get(Calendar.YEAR);
	}
	
    public static Date getDate(int year, int month, int day)
    {
    	return getDate(year,month,day,0,0,0);
    }
    
    public static Date getDate(int year, int month, int day, int hour, int minute, int second)
    {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.set(Calendar.YEAR, year);
    	gc.set(Calendar.MONTH, month-1);
    	gc.set(Calendar.DAY_OF_MONTH, day);

    	gc.set(Calendar.MILLISECOND, 0);
    	gc.set(Calendar.SECOND,second);
    	gc.set(Calendar.MINUTE, minute);
    	gc.set(Calendar.HOUR_OF_DAY, hour);
    	return new java.sql.Timestamp(gc.getTimeInMillis());
    }
    public static Date getDate(int year, int month, int day, int hour, int minute, int second, int milli)
    {
    	GregorianCalendar gc = new GregorianCalendar();
    	gc.set(Calendar.YEAR, year);
    	gc.set(Calendar.MONTH, month-1);
    	gc.set(Calendar.DAY_OF_MONTH, day);

    	gc.set(Calendar.MILLISECOND, milli);
    	gc.set(Calendar.SECOND,second);
    	gc.set(Calendar.MINUTE, minute);
    	gc.set(Calendar.HOUR_OF_DAY, hour);
    	return new java.sql.Timestamp(gc.getTimeInMillis());
    }
    
    public static Date roundDownToMidnight(Date d)
    {
    	return roundDownToMidnight(new GregorianCalendar(),d);
    }
    
    public static Date roundDownToMidnight(GregorianCalendar gc, Date d)
    {
    	if(d == null) return null;
    	if(gc==null)gc = new GregorianCalendar();
    	
    	gc.setTime(d);
    	gc.set(Calendar.HOUR_OF_DAY, 0);
    	gc.set(Calendar.MINUTE,0);
    	gc.set(Calendar.SECOND, 0);
    	gc.set(Calendar.MILLISECOND,0);
    	return gc.getTime();
    }

	public static double getJD(Date d) 
	{
		// seconds since 1970/01/01
		double t = d.getTime()/1000.0d;
		
		t = t / 86400.0d;
		
		t += 2440587.5;
	
		return t;
	}
	
	public static Date getDate(double jd)
	{
		double dt = jd-2440587.5;
		dt = 86400.0d*dt;
		dt *= 1000.0d;
		long t = (long)dt;
		return new java.sql.Timestamp(t);
	}
}
