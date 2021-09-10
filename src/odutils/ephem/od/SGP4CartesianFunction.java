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

import odutils.ephem.CartesianState;
import odutils.ephem.EphemerisUtil;
import odutils.ephem.MutableTLE;
import sgp4.TLE;

import java.util.List;

public class SGP4CartesianFunction implements MultivariateVectorFunction
{
	protected List<CartesianState> carts = null;
	
	protected TLE tleInit = null;
	protected String line1 = null;
	protected String line2 = null;
	
	protected boolean fitBStar = false;
	
	public SGP4CartesianFunction()
	{
		
	}
	
	public void setFitBStar(boolean flag)
	{
		fitBStar = flag;
	}
	
	/**
	 * If the cartesian states are not TEME, specify that the conversion is necessary.
	 * 
	 * @param list
	 * @param convertToTEME
	 */
	public void setCarts(List<CartesianState> list, boolean convertToTEME)
	{
		carts = list;
	}
	
	public void setInitialGuess(TLE tle)
	{
		line1 = tle.getLine1();
		line2 = tle.getLine2();
		tleInit = new TLE(line1,line2);
	}
	
	public void setInitialGuessToLastCart()
	{
		setInitialGuess(carts.get(carts.size()-1));
	}
	
	public void setInitialGuessToFirstCart()
	{
		setInitialGuess(carts.get(0));
	}
	
	public void setInitialGuess(CartesianState cart)
	{
		TLE tle = CartToTLE.cartToTLE(cart,"99999");
		setInitialGuess(tle);
	}
	
	public double[] getInitParams()
	{
		double tmp[] = EphemerisUtil.getEquinoctal(tleInit);
		
		if(fitBStar)
		{
			double out[] = new double[7];
			System.arraycopy(tmp, 0, out, 0, 6);
			out[6]=tleInit.getBstar();
			tmp = out;
		}
		
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
		int size = carts.size();
		double allzeros[] = new double[size];
		return allzeros;
	}
	
	@Override
	public double[] value(double[] params) throws IllegalArgumentException 
	{
		TLE tle = paramsToTLE(params);
		
		int size = carts.size();
		
		double out[] = new double[size];
		CartesianState cs1 = null;
		CartesianState cs2 = new CartesianState();
		double rv[][] = null;
		
		for(int i=0; i<size; i++)
		{
			cs1 = carts.get(i);
			rv = tle.getRV(cs1.epoch);
			cs2.setRVec(rv[0]);
			cs2.setVVec(rv[1]);
			
			out[i]=cs1.getDist(cs2); // residual is the position error
		}
		
		return out;
	}
	
	protected TLE paramsToTLE(double params[])
	{
		MutableTLE tle = null;
		
		double vals[] = EphemerisUtil.getCOEFromEquinoctal(params);

		tle = new MutableTLE(line1,line2);
		
		//i, Om, ecc, w, M, n
		tle.setIncDeg(vals[0]);
		tle.setRaanDeg(vals[1]);
		tle.setEcc(vals[2]);
		tle.setArgpDeg(vals[3]);
		tle.setMaDeg(vals[4]);
		tle.setN(vals[5]);
		
		if(fitBStar && params.length>6)
		{
			tle.setBstar(params[6]);
			//System.out.println("bstar\t"+params[6]);
			//System.out.println(tle.getLine1());
		}
		tle.commit();
		
		return tle;
	}
	
	public static class PVal implements ParameterValidator
	{

		@Override
		public RealVector validate(RealVector params) 
		{
			
			RealVector out = params.copy();
			
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
