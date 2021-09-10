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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.jna.ptr.DoubleByReference;

import afspc.astrostds.wrappers.JnaSgp4Prop;
import afspc.astrostds.wrappers.JnaTle;
import odutils.util.DateUtil;
import sgp4.TLE;

/**
 * Memory management on linked dll/so is tricky, so we need a separate generator to emit
 * ephemeris all at once from two lines.
 * 
 * @author aholinch
 *
 */
public class USSFSGP4 
{
	public static String sync = "mutex";
	
    public static CartesianState getCart(Date d, TLE tle)
    {
    	long t1 = d.getTime();
    	long t0 = tle.getEpoch().getTime();
    	
    	double dt = t1-t0;
    	dt /= 60000.0d;
    	
    	return getCart(dt,tle.getLine1(),tle.getLine2());
    }
    
    public static CartesianState getCart(Date d, String line1, String line2)
    {
    	TLE tle = new TLE(line1,line2);
    	return getCart(d,tle);
    }
    
    public static CartesianState getCart(double minSinceEpoch, String line1, String line2)
    {
    	CartesianState cart = null;
    	
    	long satKey = -1;
    	try
    	{
    		satKey = JnaTle.TleAddSatFrLines(line1,line2);
    		DoubleByReference ds50UTC = new DoubleByReference();
  		  
    		int errCode = JnaSgp4Prop.Sgp4InitSat(satKey);
    		if(errCode != 0)
    		{
    			System.err.println("USSFSGP4 errCode = " + errCode);
    		}
    		
    		double[] pos = new double[3];   // Position (km) in TEME of Epoch
    		double[] vel = new double[3];   // Velocity (km/s) in TEME of Epoch
    		double[] llh = new double[3];   // Latitude(deg), Longitude(deg), Height above Geoid (km)

  		  	// propagate the initialized TLE to the specified time in minutes since epoch
  		  	JnaSgp4Prop.Sgp4PropMse(satKey, minSinceEpoch, ds50UTC, pos, vel, llh); // see Sgp4Prop dll document  
  		  	
  		  	Date d1 = DateUtil.getDate(1950, 1, 1);
  		  	long t = d1.getTime();
  		  	t += (long)(86400.0d*1000.0d*(ds50UTC.getValue()-1.0));
  		  	
  		  	d1 = new java.sql.Timestamp(t);
  		  	cart = new CartesianState();
  		  	cart.epoch = d1;
  		  	cart.setRVec(pos);
  		  	cart.setVVec(vel);
  		  	
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(satKey != -1)
    		{
    			try{ 
    				JnaTle.TleRemoveSat(satKey);   // remove loaded TLE from memory
    			}catch(Exception ex){};
    			
    			try{ 
       			    JnaSgp4Prop.Sgp4RemoveSat(satKey);  // remove initialized TLE from memory
    			}catch(Exception ex){};  
    		}
    	}
    	
    	return cart;
    }
    
    public static List<CartesianState> getCarts(Date d1, Date d2, double tStepSec, TLE tle)
    {
    	long t1 = d1.getTime();
    	long t2 = d2.getTime();
    	long t0 = tle.getEpoch().getTime();
    	
    	double dt = t1-t0;
    	dt /= 60000.0d;
    	double dt2 = t2-t0;
    	dt2 /= 60000.0d;
    	
    	double tStepMin = tStepSec/60.0d;
    	long tStepMS = (long)(1000.0d*tStepSec);
    	
    	int size = (int)((dt2-dt)/tStepMin)+1;
    	List<CartesianState> carts = new ArrayList<CartesianState>(size);
    	long satKey = -1;
    	try
    	{
    		satKey = JnaTle.TleAddSatFrLines(tle.getLine1(),tle.getLine2());
    		DoubleByReference ds50UTC = new DoubleByReference();
  		  
    		int errCode = JnaSgp4Prop.Sgp4InitSat(satKey);
    		if(errCode != 0)
    		{
    			System.err.println("USSFSGP4 errCode = " + errCode);
    		}
    		
    		double[] pos = new double[3];   // Position (km) in TEME of Epoch
    		double[] vel = new double[3];   // Velocity (km/s) in TEME of Epoch
    		double[] llh = new double[3];   // Latitude(deg), Longitude(deg), Height above Geoid (km)

    		double minSinceEpoch = dt;
    		
    		long t = t1;
    		
    		CartesianState cart = null;
    		Date d = null;
    		while(minSinceEpoch < dt2)
    		{
	  		  	// propagate the initialized TLE to the specified time in minutes since epoch
	  		  	JnaSgp4Prop.Sgp4PropMse(satKey, minSinceEpoch, ds50UTC, pos, vel, llh); // see Sgp4Prop dll document  
	  		  	
	  		  	
	  		  	d = new java.sql.Timestamp(t);
	  		  	cart = new CartesianState();
	  		  	cart.epoch = d;
	  		  	cart.setRVec(pos);
	  		  	cart.setVVec(vel);
	  		  	carts.add(cart);
	  		  	
	  		  	minSinceEpoch += tStepMin;
	  		  	t += tStepMS;
    		}
    		
    		// ensure d2 is used exactly
    		minSinceEpoch = dt2;
    		JnaSgp4Prop.Sgp4PropMse(satKey, minSinceEpoch, ds50UTC, pos, vel, llh); 
  		  	
  		  	d = new java.sql.Timestamp(t2);
  		  	cart = new CartesianState();
  		  	cart.epoch = d;
  		  	cart.setRVec(pos);
  		  	cart.setVVec(vel);
  		  	carts.add(cart);
  		  	
  		  	minSinceEpoch += tStepMin;
  		  	t += tStepMS;
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(satKey != -1)
    		{
    			try{ 
    				JnaTle.TleRemoveSat(satKey);   // remove loaded TLE from memory
    			}catch(Exception ex){};
    			
    			try{ 
       			    JnaSgp4Prop.Sgp4RemoveSat(satKey);  // remove initialized TLE from memory
    			}catch(Exception ex){};  
    		}
    	}
    	
    	
    	return carts;
    }
    
    public static List<CartesianState> getCarts(Date d1, Date d2, double tStepSec, String line1, String line2)
    {
    	TLE tle = new TLE(line1,line2);
    	
    	return getCarts(d1,d2,tStepSec,tle);
    }

    public static List<CartesianState> getCarts(List<Double> minsSinceEpoch, TLE tle)
    {
    	long t0 = tle.getEpoch().getTime();
    	    	
    	int size = minsSinceEpoch.size();
    	double mse = 0;
    	
    	List<CartesianState> carts = new ArrayList<CartesianState>(size);
    	long satKey = -1;
    	synchronized(sync)
    	{
    	try
    	{
    		satKey = JnaTle.TleAddSatFrLines(tle.getLine1(),tle.getLine2());
    		DoubleByReference ds50UTC = new DoubleByReference();
  		  
    		int errCode = JnaSgp4Prop.Sgp4InitSat(satKey);
    		if(errCode != 0)
    		{
    			System.err.println("USSFSGP4 errCode = " + errCode);
    		}
    		
    		double[] pos = new double[3];   // Position (km) in TEME of Epoch
    		double[] vel = new double[3];   // Velocity (km/s) in TEME of Epoch
    		double[] llh = new double[3];   // Latitude(deg), Longitude(deg), Height above Geoid (km)

    		long t = 0;
    		
    		CartesianState cart = null;
    		Date d = null;
    		
    		for(int i=0; i<size; i++)
    		{
    			mse = minsSinceEpoch.get(i);
	  		  	// propagate the initialized TLE to the specified time in minutes since epoch
	  		  	JnaSgp4Prop.Sgp4PropMse(satKey, mse, ds50UTC, pos, vel, llh); // see Sgp4Prop dll document  
	  		  	
	  		  	t = (long)(mse*60.0d*1000.0d);
	  		  	t+=t0;
	  		  	d = new java.sql.Timestamp(t);
	  		  	cart = new CartesianState();
	  		  	cart.epoch = d;
	  		  	cart.setRVec(pos);
	  		  	cart.setVVec(vel);
	  		  	carts.add(cart);
	  		  	
    		}
    		
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(satKey != -1)
    		{
    			try{ 
    				JnaTle.TleRemoveSat(satKey);   // remove loaded TLE from memory
    			}catch(Exception ex){};
    			
    			try{ 
       			    JnaSgp4Prop.Sgp4RemoveSat(satKey);  // remove initialized TLE from memory
    			}catch(Exception ex){};  
    		}
    	}
    	}
    	
    	
    	return carts;
    }
}
