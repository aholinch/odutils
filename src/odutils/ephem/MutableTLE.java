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
package odutils.ephem;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import odutils.util.DateUtil;

import sgp4.ElsetRec;
import sgp4.TLE;

public class MutableTLE extends TLE
{
    protected TLE tle = null;
    protected boolean needsCommit = false;
    
    public MutableTLE()
    {
    	needsCommit = true;
    	tle = new TLE();
    }
    
    public MutableTLE(String line1, String line2)
    {
    	setLines(line1,line2);
    }
    
    public void setLines(String line1, String line2)
    {
    	tle = new TLE(line1,line2);
    	needsCommit = false;
    }
    
    public void commit()
    {
    	// we need to write the two lines
    	//          1         2         3         4         5         6
    	//0123456789012345678901234567890123456789012345678901234567890123456789
    	//1 NNNNNC NNNNNAAA NNNNN.NNNNNNNN +.NNNNNNNN +NNNNN-N +NNNNN-N N NNNNN
    	//          1         2         3         4         5         6
    	//012345678901234567890123456789012345678901234567890123456789012 3456789
    	//2 NNNNN NNN.NNNN NNN.NNNN NNNNNNN NNN.NNNN NNN.NNNN NN.NNNNNNNN NNNNNN
    	
    	String str = null;
    	
    	str = tle.getObjectID();
    	if(str == null) str = "";
    	str = str.trim();
    	if(str.length()>5)str = str.substring(0,5);
    	while(str.length()<5)str = " "+str;
    	
    	String l1 = "1 "+str+"U ";
    	String l2 = "2 "+str;
    	
    	str = tle.getIntlID();
    	if(str == null) str = "";
    	str = str.trim();
    	if(str.length()>8)str = str.substring(0,8);
    	while(str.length()<8)str = str+" ";
    	l1 += str;
    	l1 += " "+formatEpochDate(tle.getEpoch());
    	
    	String sgn = "+";
    	DecimalFormat df1 = new DecimalFormat("0.00000000");
    	str = df1.format(tle.getNDot());
    	if(tle.getNDot()<0)sgn = "-";
    	if(str.startsWith("-"))str = str.substring(1);
    	str = sgn+str.substring(1);
    	l1 += " "+str;
    	
    	l1 += " " + formatImpDec(tle.getNDDot());
    	l1 += " " + formatImpDec(tle.getBstar());
    	
    	l1 += " " + tle.getElType();
    	
    	str = String.valueOf(tle.getElNum());
    	if(str.length()>4)str = str.substring(0,4);
    	while(str.length()<4)str = " "+str;
    	
    	l1 += " " + str + "0";
    	
		DecimalFormat df = new DecimalFormat("000.0000");
		DecimalFormat df2 = new DecimalFormat("0.0000000");
		DecimalFormat df3 = new DecimalFormat("00.00000000");
	
		l2 += " " + df.format(tle.getIncDeg());
		l2 += " " + df.format(tle.getRaanDeg());
		l2 += " " + df2.format(tle.getEcc()).substring(2);
		l2 += " " + df.format(tle.getArgpDeg());
		l2 += " " + df.format(tle.getMaDeg());
		l2 += " " + df3.format(tle.getN());
		str = String.valueOf(tle.getRevNum());
		if(str.length()>5)str = str.substring(0,5);
		while(str.length()<5)str = " "+str;
		l2 += str+"0";

    	//System.out.println(l1);
    	//System.out.println(l2);
    	
    	tle = new TLE(l1,l2);
    	needsCommit = false;
    }
    
    protected String formatImpDec(double val)
    {
    	double av = Math.abs(val);
    	int exp = (av < 1e-9) ? -9 : (int)Math.floor(Math.log10(av));
    	long mantissa = Math.round(av * Math.pow(10.0d, 5-exp));
    	
    	if(mantissa == 0)
    	{
    		exp = 0;
    	}
    	else if (mantissa > (Math.pow(10, 5) - 1))
    	{
    	    // rare case: if d has a single digit like d = 1.0e-4 with mantissaSize = 5
    		// the above computation finds exponent = -4 and mantissa = 100000 which
    		// doesn't fit in a 5 digits string
    		exp++;
    		mantissa = Math.round(av * Math.pow(10.0, 5 - exp));
    	}

    	String str = String.valueOf(mantissa);
    	while(str.length()<5)str = "0"+str;
    	if(val < 0)
    	{
    		str = "-"+str;
    	}
    	else
    	{
    		str = "+"+str;
    	}
    	
    	String ex = String.valueOf(exp);
    	if(exp == 0) ex = "-"+ex;
    	if(exp > 0) ex = "+"+ex;
    	
    	str += ex;
    	
    	return str;
    }
    
    protected String formatEpochDate(Date d)
    {
    	DateUtil.setDefaultTimeZone();
    	GregorianCalendar gc = new GregorianCalendar();
    	
    	gc.setTime(d);
    	int year = gc.get(Calendar.YEAR);
    	if(year < 2000) year -=1900;
    	else year -= 2000;
    	
    	String str = String.valueOf(year);
    	while(str.length()<2)str = "0"+str;
    	
    	String tmp = null;
    	
    	int doy = gc.get(Calendar.DAY_OF_YEAR);
    	tmp = String.valueOf(doy);
    	while(tmp.length()<3)tmp = "0"+tmp;
    	str += tmp;
    	
    	double dfrac = gc.get(Calendar.MILLISECOND);
    	dfrac /= 1000.0d;
    	dfrac += gc.get(Calendar.SECOND);
    	dfrac /= 60.0d;
    	dfrac += gc.get(Calendar.MINUTE);
    	dfrac /= 60.0d;
    	dfrac += gc.get(Calendar.HOUR_OF_DAY);
    	dfrac /= 24.0d;
    	
    	DecimalFormat df = new DecimalFormat("0.00000000");
    	tmp = df.format(dfrac);
    	str += tmp.substring(1);
    	while(str.length()<14)str = str+"0";
    	return str;
    }
    
    public double[][] getRV(Date d)
    {
    	if(needsCommit) commit();
    	
    	int eltype = tle.getElType();
    	if(eltype < 4)
    	{
    		return tle.getRV(d);
    	}
    	
    	// we don't do type 6 but type 4 can be handled
    	CartesianState cs = USSFSGP4.getCart(d, tle);
    	
    	double rv[][] = new double[][]{cs.getRVec(),cs.getVVec()};
    	return rv;
    }

    public double[][] getRV(double minutesAfterEpoch)
    {
    	if(needsCommit) commit();

    	int eltype = tle.getElType();
    	if(eltype < 4)
    	{
    		return tle.getRV(minutesAfterEpoch);
    	}
    	
    	// we don't do type 6 but type 4 can be handled
    	CartesianState cs = USSFSGP4.getCart(minutesAfterEpoch, tle.getLine1(), tle.getLine2());
    	
    	double rv[][] = new double[][]{cs.getRVec(),cs.getVVec()};
    	return rv;
    }
    
    public CartesianState getCart(Date d)
    {
    	if(needsCommit) commit();
    	
    	double rv[][] = getRV(d);
    	CartesianState cs = new CartesianState();
    	cs.setEpoch(new java.sql.Timestamp(d.getTime()));
    	cs.setRVec(rv[0]);
    	cs.setVVec(rv[1]);
    	return cs;
    }
    
    public CartesianState getCart(double minutesAfterEpoch)
    {
    	if(tle.getElType()<4)
    	{
	    	double rv[][] = getRV(minutesAfterEpoch);
	    	CartesianState cs = new CartesianState();
	    	cs.setRVec(rv[0]);
	    	cs.setVVec(rv[1]);
	    	
	    	long t = (long)(minutesAfterEpoch*60.0*1000.0);
	    	t += tle.getEpoch().getTime();
	    	cs.setEpoch(new java.sql.Timestamp(t));
	    	return cs;
    	}
    	return USSFSGP4.getCart(0, getLine1(), getLine2());
    }
    
    public int getSgp4Error()
    {
    	return tle.getSgp4Error();
    }
    
    public ElsetRec getElsetRec()
    {
    	return tle.getElsetRec();
    }
    
    public void setElsetRec(ElsetRec er)
    {
    	needsCommit = true;
    	tle.setElsetRec(er);
    }
    
    public String getParseErrors()
    {
    	return tle.getParseErrors();
    }
    
    public String getIntlID()
    {
    	return tle.getIntlID();
    }
    
    public void setIntlID(String id)
    {
    	needsCommit = true;
    	if(id == null) id = "";
    	id = id.trim();
    	if(id.length()>8)id = id.substring(0,8);
    	tle.setIntlID(id);
    }
    
    public String getObjectID()
    {
    	return tle.getObjectID();
    }
    
    public void setObjectID(String id)
    {
    	needsCommit = true;
    	if(id == null) id = "";
    	id = id.trim();
    	if(id.length()>5) id = id.substring(0,5);
    	tle.setObjectID(id);
    }
    
    public Date getEpoch()
    {
    	return tle.getEpoch();
    }
    
    public void setEpoch(Date d)
    {
    	needsCommit = true;
    	tle.setEpoch(d);
    }
    
    public double getNDot()
    {
    	return tle.getNDot();
    }
    
    public void setNDot(double val)
    {
    	needsCommit = true;
    	if(val < -1) val = -0.1;
    	if(val > 1) val = 0.1;
    	tle.setNDot(val);
    }
    
    public double getNDDot()
    {
    	return tle.getNDDot();
    }
    
    public void setNDDot(double val)
    {
    	needsCommit = true;
    	tle.setNDDot(val);
    }
    
    public double getBstar()
    {
    	return tle.getBstar();
    }
    
    public void setBstar(double val)
    {
    	needsCommit = true;
    	tle.setBstar(val);
    }
    
    public int getElType()
    {
    	return tle.getElType();
    }
    
    public void setElType(int num)
    {
    	needsCommit = true;
    	num = (int)inRange(num,6);
    	tle.setElType(num);
    }
    
    public int getElNum()
    {
    	return tle.getElNum();
    }
    
    public void setElNum(int num)
    {
    	needsCommit = true;
    	num = (int)inRange(num,9999);
    	tle.setElNum(num);
    }
    
    public double getIncDeg()
    {
    	return tle.getIncDeg();
    }
    
    public void setIncDeg(double val)
    {
    	needsCommit = true;
    	val = inRange(val,180.0d);
    	tle.setIncDeg(val);
    }
    
    public double getRaanDeg()
    {
    	return tle.getRaanDeg();
    }
    
    public void setRaanDeg(double val)
    {
    	needsCommit = true;
    	val = inRange(val,360);
    	tle.setRaanDeg(val);
    }
    
    public double getEcc()
    {
    	return tle.getEcc();
    }
    
    public void setEcc(double val)
    {
    	needsCommit = true;
    	if(val <0) val = 0;
    	if(val >= 1)val = 0.99;
    	tle.setEcc(val);
    }
    
    public double getArgpDeg()
    {
    	return tle.getArgpDeg();
    }
    
    public void setArgpDeg(double val)
    {
    	needsCommit = true;
    	val = inRange(val,360);
    	tle.setArgpDeg(val);
    }
    
    public double getMaDeg()
    {
    	return tle.getMaDeg();
    }
    
    public void setMaDeg(double val)
    {
    	needsCommit = true;
    	val = inRange(val,360);
    	tle.setMaDeg(val);
    }
    
    public double getN()
    {
    	return tle.getN();
    }
    
    public void setN(double val)
    {
    	needsCommit = true;
    	if(val <0) val = 0;
    	if(val > 17) val = 17;
    	tle.setN(val);
    }
    
    public int getRevNum()
    {
    	return tle.getRevNum();
    }
    
    public void setRevNum(int val)
    {
    	needsCommit = true;
    	val = (int)inRange(0,99999);
    	tle.setRevNum(val);
    }
    
    public String getLine1()
    {
    	return tle.getLine1();
    }
    
    public String getLine2()
    {
    	return tle.getLine2();
    }
    
    protected double inRange(double val,double max)
    {
    	while(val<0)val+=max;
    	while(val > max) val -= max;
    	
    	return val;
    }
}
