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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import odutils.util.InterpUtil;

public class Ephemerides implements CartesianSource
{
    public String id;
    public long t1 = 0;
    public long t2 = 0;
    
    public double minR = 0;
    public double maxR = 0;

    public double minV = 0;
    public double maxV = 0;
    
    public double ts[] = null;
    public double rxs[] = null;
    public double rys[] = null;
    public double rzs[] = null;
    public double vxs[] = null;
    public double vys[] = null;
    public double vzs[] = null;
    
    protected String frame = null;
    
    public Ephemerides(List<CartesianState> carts, String id)
    {
    	this.id = id;
    	buildArrays(carts);
    }
    
    public void setFrame(String f)
    {
    	frame = f;
    }
    
    public String getFrame()
    {
    	return frame;
    }
    
    protected void buildArrays(List<CartesianState> carts)
    {
    	Collections.sort(carts);
    	
    	int size = carts.size();
    	
    	t1 = carts.get(0).getEpoch().getTime();
    	t2 = carts.get(size-1).getEpoch().getTime();
    	
    	ts = new double[size];
    	rxs = new double[size];
    	rys = new double[size];
    	rzs = new double[size];
    	vxs = new double[size];
    	vys = new double[size];
    	vzs = new double[size];
    	
    	CartesianState cart = null;
    	long t = 0;
    	double dt = 0;
    	double milli2Sec = 1.0d/1000.0;
    	
    	minR = Double.MAX_VALUE;
    	maxR = -10000000;
    	minV = Double.MAX_VALUE;
    	maxV = -10000000;
    	double mag = 0;
    	for(int i=0; i<size; i++)
    	{
    		cart = carts.get(i);
    		t = cart.getEpoch().getTime();
    		dt = t-t1;
    		dt *= milli2Sec;
    		ts[i]=dt;
    		rxs[i]=cart.rx;
    		rys[i]=cart.ry;
    		rzs[i]=cart.rz;
    		vxs[i]=cart.vx;
    		vys[i]=cart.vy;
    		vzs[i]=cart.vz;
    		
    		mag = cart.getRmag();
    		if(mag < minR)
    		{
    			minR = mag;
    		}
    		if(mag > maxR)
    		{
    			maxR = mag;
    		}
    		
    		mag = cart.getVmag();
    		if(mag < minV)
    		{
    			minV = mag;
    		}
    		if(mag > maxV)
    		{
    			maxV = mag;
    		}
    	}
    	
    	carts = null;
    }
    
    public long getTimeMS(int ind)
    {
    	double t = ts[ind]*1000.0;
    	t+= t1;
    	
    	return (long)t;
    }
    
    /**
     * Returns cartesianstate for given date.  Will be null if outside of provided dates.
     * 
     * @param d
     * @return
     */
    public CartesianState getCartesian(Date d)
    {
    	long t = d.getTime();
    	if(t<t1 || t > t2)
    	{
    		return null;
    	}
    	
    	CartesianState cs = new CartesianState();
    	cs.setEpoch(new java.sql.Timestamp(t));
    	
    	double dt = t-t1;
    	dt = dt/1000.0d;
    	
    	double vals[][] = InterpUtil.hermiteWFD3D(ts, rxs, rys, rzs, vxs, vys, vzs, 6, dt);
    	
    	double rv[] = vals[0];
    	double vv[] = vals[1];
    	
    	cs.setRVec(rv);
    	cs.setVVec(vv);
    	
    	return cs;
    }
    
    public double[][] getRV(long t)
    {
    	if(t<t1 || t > t2)
    	{
    		return null;
    	}
    	
    	
    	double dt = t-t1;
    	dt = dt/1000.0d;
    	
    	double vals[][] = InterpUtil.hermiteWFD3D(ts, rxs, rys, rzs, vxs, vys, vzs, 6, dt);
    	
    	return vals;
    }

    public boolean supportsUpdates()
    {
    	return false;
    }
    
    public void setFromEquinoctal(double params[])
    {
    	// nothing to do
    }
    
    
    public void setFromVector(double params[])
    {
    	// nothing to do
    }

	@Override
	public void setMeanAnomaly(double val) {
		// nothing to do
		
	}

	@Override
	public List<CartesianState> getCartesians(Date d1, Date d2, double tStepSec)
	{
    	double dt = d2.getTime()-d1.getTime();
    	dt = dt/(tStepSec*1000.0);
    	
    	int size = (int)(dt+1);
    	
    	List<CartesianState> carts = new ArrayList<CartesianState>(size);
    	
    	CartesianState cart = null;
    	Date d = null;
    	long t = 0;
    	
    	long tstep = (long)(1000.0d*tStepSec);
    	
    	long t2 = d2.getTime();
    	t = d1.getTime();
    	double rv[][] = null;
    	
   
		while(t < t2)
		{
			d = new java.sql.Timestamp(t);
			cart = getCartesian(d);
			carts.add(cart);
			
			t+=tstep;
		}
		
		// ensure last date is included regardless of steps
		d = new java.sql.Timestamp(t2);
		cart = getCartesian(d);
		carts.add(cart);

		return carts;
	}

}
