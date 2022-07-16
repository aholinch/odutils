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
package odutils.ephem.od;

import org.hipparchus.analysis.MultivariateVectorFunction;
import org.hipparchus.linear.RealVector;
import org.hipparchus.optim.nonlinear.vector.leastsquares.ParameterValidator;

import odutils.ephem.CartesianSource;
import odutils.ephem.CartesianState;
import odutils.ephem.EphemerisUtil;
import odutils.ephem.KeplerianState;
import odutils.ephem.MutableTLE;
import odutils.ephem.obs.LookAngleCalculator;
import odutils.ephem.obs.LookAngleDifferences;
import odutils.ephem.obs.LookAngleState;
import sgp4.TLE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartesianSourceLookAngleFunction implements MultivariateVectorFunction
{
	protected List<LookAngleState> lookAngles = null;
	protected List<List<LookAngleState>> lookAnglesList = null;
	
	protected CartesianSource cartSource = null;
	
	protected LookAngleDifferences.DifferenceFunction func = null;
	
	protected LookAngleDifferences lookAngleDifferences = null;
	
	protected Date epoch = null;
	
	protected boolean fitCartesian = false;
	
	protected boolean allOneLocation = true;
	
	public CartesianSourceLookAngleFunction()
	{
		lookAngleDifferences = new LookAngleDifferences();
	}
	
	public void setFitCartesian(boolean flag)
	{
		fitCartesian = flag;
	}
	
	public void setEpoch(Date date)
	{
		epoch = date;
	}
	
	public void setCartesianSource(CartesianSource src)
	{
		cartSource = src;
		lookAngleDifferences.setCartesianSource(src);
	}
	
	public CartesianSource getCartesianSource()
	{
		return cartSource;
	}
	
	public void setLookAngleStates(List<LookAngleState> list)
	{
		lookAngles = list;
		
		this.lookAnglesList = LookAngleCalculator.groupByLocation(list);
		
		if(lookAnglesList.size() > 1)
		{
			this.allOneLocation = false;
		}
		else
		{
			this.allOneLocation = true;
			lookAnglesList = null;
		}
	}
	
	public void setDifferenceFunction(LookAngleDifferences.DifferenceFunction f)
	{
		func = f;
	}
	
	public double[] getInitParams()
	{
		CartesianState cart = cartSource.getCartesian(epoch);
		if(fitCartesian)
		{
			return new double[] {cart.rx,cart.ry,cart.rz,cart.vx,cart.vy,cart.vz};
		}
		KeplerianState kep = EphemerisUtil.cartToKep(cart);
		double tmp[] = EphemerisUtil.getEquinoctal(kep);

		return tmp;
	}
	
	public boolean[] getPercs()
	{
		return new boolean[] {false,false,false,false,false,false,true};
	}
	
	public double[] getDeltas()
	{

		return new double[]{0.0001,0.0001,0.0001,0.0001,0.0001,0.0001,0.01};
	}
	
	public double[] getTarget()
	{
		int size = lookAngles.size();
		double allzeros[] = new double[size];
		return allzeros;
	}
	
	@Override
	public double[] value(double[] params) throws IllegalArgumentException 
	{
		if(params.length==1)
		{
			cartSource.setMeanAnomaly(params[0]);
		}
		else
		{
			if(fitCartesian)
			{
				cartSource.setFromVector(params);
			}
			else
			{
				cartSource.setFromEquinoctal(params);
			}
		}
		double out[] = null;
		
		if(allOneLocation)
		{
			out = lookAngleDifferences.differences(lookAngles, func);
		}
		else
		{
			int size = lookAngles.size();
			out = new double[size];
			
			double tmp[] = null;
			int ind = 0;
			for(int i=0; i<lookAnglesList.size(); i++)
			{
				tmp = lookAngleDifferences.differences(lookAnglesList.get(i), func);
				System.arraycopy(tmp,0,out,ind,tmp.length);
				ind+=tmp.length;
			}
		}
		
		return out;
	}
	
	
	public static class PVal implements ParameterValidator
	{

		@Override
		public RealVector validate(RealVector params) 
		{
			RealVector out = params.copy();
			if(params.getEntry(5)>16.5)out.setEntry(5, 16.5);
			
			// constrain inclination
	    	double pe = params.getEntry(3);
	    	double qe = params.getEntry(4);
	    	
	    	double inc = 0;
	    	double Om = 0;
	    	
	    	Om = Math.atan2(pe,qe);

	    	inc = Math.toRadians(97.5);
	    	System.out.println("Setting inc to 27");
	    	pe = Math.tan(0.5*inc)*Math.sin(Om);
	    	qe = Math.tan(0.5*inc)*Math.cos(Om);

	    	out.setEntry(3, pe);
	    	out.setEntry(4, qe);

	    	
			int len = out.getDimension();
			
			if(len > 6)
			{
				for(int i=6; i<len; i++)
				{
					if(out.getEntry(i)<0)
					{
						out.setEntry(i, 0);
					}
					
					if(out.getEntry(i)>1e3)
					{
						out.setEntry(i, 1e3);
					}
				}
			}
			return out;
		}
	}

	public static class PValConstrained implements ParameterValidator
	{
		public boolean fixInc = false;
		public boolean fixRaan = false;
		public boolean fixEcc = false;
		public boolean fixMeanMotion = false;
		
		public double incRadMin = 0;
		public double incRadMax = 0;
		public double raanRadMin = 0;
		public double raanRadMax = 0;
		public double eccMin = 0;
		public double eccMax = 0;
		public double mmMin = 0;
		public double mmMax = 0;
		
		public PValConstrained()
		{
			
		}
		
		public void setMeanMotionTarget(double revsDay)
		{
			setMeanMotionTarget(revsDay,revsDay);
		}
		
		public void setMeanMotionTarget(double mmMin, double mmMax)
		{
			fixMeanMotion = true;
			this.mmMin = mmMin;
			this.mmMax = mmMax;
		}
		
		public void setIncTarget(double incDeg)
		{
			setIncTarget(incDeg,incDeg);
		}
		public void setIncTarget(double incDegMin, double incDegMax)
		{
			fixInc = true;
			incRadMin = Math.toRadians(incDegMin);
			incRadMax = Math.toRadians(incDegMax);
		}
		
		public void setRaanTarget(double raanDeg)
		{
			setRaanTarget(raanDeg,raanDeg);
		}
		
		public void setRaanTarget(double raanDegMin, double raanDegMax)
		{
			fixRaan = true;
			raanRadMin = Math.toRadians(raanDegMin);
			raanRadMax = Math.toRadians(raanDegMax);
		}
		
		public void setEccTarget(double ecc)
		{
			setEccTarget(ecc,ecc);
		}
		
		public void setEccTarget(double eMin, double eMax)
		{
			fixEcc = true;
			eccMin = eMin;
			eccMax = eMax;
		}
		
		@Override
		public RealVector validate(RealVector params) 
		{
			if(params.getDimension()==1)return params;
			
			RealVector out = params.copy();
			
			// this is the highest regardless of mm setting
			if(params.getEntry(5)>16.5)out.setEntry(5, 16.5);
			
			if(fixMeanMotion)
			{
				double mm = params.getEntry(5);
				if(mm<mmMin)
				{
					mm = mmMin;
				}
				else if(mm>mmMax)
				{
					mm = mmMax;
				}
				out.setEntry(5, mm);
			}
			
	    	if(fixInc || fixRaan)
	    	{
	    		// constrain inclination
		    	double pe = params.getEntry(3);
		    	double qe = params.getEntry(4);
		    	double inc = 0;
		    	double Om = 0;

	    		Om = Math.atan2(pe,qe);
	    		inc = 2.0d*Math.atan(Math.sqrt(pe*pe+qe*qe));

		    	if(fixRaan)
		    	{
		    		if(Om<raanRadMin)
		    		{
		    			Om = raanRadMin;
		    		}
		    		else if(Om>raanRadMax)
		    		{
		    			Om = raanRadMax;
		    		}
		    	}
		    	
		    	if(fixInc)
		    	{
		    		if(inc < incRadMin)
		    		{
		    			inc = incRadMin;
		    		}
		    		else if(inc > incRadMax)
		    		{
		    			inc = incRadMax;
		    		}
		    	}
		    	
		    	pe = Math.tan(0.5*inc)*Math.sin(Om);
		    	qe = Math.tan(0.5*inc)*Math.cos(Om);
	
		    	out.setEntry(3, pe);
		    	out.setEntry(4, qe);
			}
	    	
	    	
	    	if(fixEcc)
	    	{
	    		double ke = params.getEntry(0);
	    		double he = params.getEntry(1);
	    		double ecc = Math.sqrt(he*he+ke*ke);
	        	double ang = Math.atan2(he,ke);
	        	
	        	if(ecc < eccMin)
	        	{
	        		ecc = eccMin;
	        	}
	        	else if(ecc > eccMax)
	        	{
	        		ecc = eccMax;
	        	}
	        	
	        	ke = ecc*Math.cos(ang);
	        	he = ecc*Math.sin(ang);
	        	out.setEntry(0, ke);
	        	out.setEntry(1, he);
	    	}
	    	
			int len = out.getDimension();
			
			if(len > 6)
			{
				for(int i=6; i<len; i++)
				{
					if(out.getEntry(i)<0)
					{
						out.setEntry(i, 0);
					}
					
					if(out.getEntry(i)>1e3)
					{
						out.setEntry(i, 1e3);
					}
				}
			}
			return out;
		}
	}

}
