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
import odutils.ephem.USSFSGP4;

import sgp4.TLE;

import java.util.ArrayList;
import java.util.List;

public class SGP4XPCartesianFunction implements MultivariateVectorFunction
{
	protected List<CartesianState> carts = null;
	protected List<Double> mseList = null;
	
	protected TLE tleInit = null;
	protected String line1 = null;
	protected String line2 = null;
	
	protected boolean fitBTerm = false;
	protected boolean fitAGOM = false;
	
	public SGP4XPCartesianFunction()
	{
		
	}
	
	public void setFitBTerm(boolean flag)
	{
		fitBTerm = flag;
	}
	
	public void setFitAGOM(boolean flag)
	{
		fitAGOM = flag;
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
		
		fillMSEList();
	}
	
	public void fillMSEList()
	{
		int size = carts.size();
		long t0 = tleInit.getEpoch().getTime();
		double dt = 0;
		mseList = new ArrayList<Double>(size);
		
		for(int i=0; i<size; i++)
		{
			dt = carts.get(i).getEpoch().getTime()-t0;
			dt = dt/(60000.0d);
			mseList.add(dt);
		}
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
		TLE tle = CartToTLE.cartToXPTLE(cart,"99999");
		setInitialGuess(tle);
	}
	
	public double[] getInitParams()
	{
		double tmp[] = EphemerisUtil.getEquinoctal(tleInit);
		
		if(fitBTerm)
		{
			double out[] = new double[7];
			System.arraycopy(tmp, 0, out, 0, 6);
			out[6]=tleInit.getBstar();
			//out[6] = Math.sqrt(Math.abs(out[6]));
			tmp = out;
		}
		
		if(fitAGOM)
		{
			int len = tmp.length;
			double out[] = new double[len+1];
			System.arraycopy(tmp, 0, out, 0, len);
			out[len]=tleInit.getNDDot();
			//out[len]=Math.sqrt(Math.abs(out[len]));
			tmp = out;
		}
		
		return tmp;
	}
	
	public boolean[] getPercs()
	{
		return new boolean[] {false,false,false,false,false,false,true,true};
		//return new boolean[] {true,true,true,true,true,true,true,true};
	}

	public double[] getDeltas()
	{
		//return new double[]{0.0001,0.0001,0.0001,0.0001,0.0001,0.0001,2e-5,2e-5};
		//return new double[]{0.00001,0.00001,0.00001,0.00001,0.00001,0.0001,1e-6,1e-6};
		//return new double[]{0.00008,0.00008,0.00008,0.00008,0.00008,0.0001,3e-6,3e-6};
		//return new double[]{0.00008,0.00008,0.00008,0.00008,0.00008,0.0001,0.01,0.01};
		//return new double[]{0.0001,0.0001,0.0001,0.0001,0.0001,0.0001,0.05,0.05};
//		return new double[]{0.00005,0.00005,0.00005,0.00005,0.00005,0.00005,0.001,0.001};
		
		return new double[] {0.00001, 0.00001, 0.00001, 0.00001, 0.00001, 0.00001, 0.01, 0.01};
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
		
		String line1 = tle.getLine1();
		String line2 = tle.getLine2();
		
		if(mseList == null)
		{
			for(int i=0; i<size; i++)
			{
				cs1 = carts.get(i);
				
				cs2 = USSFSGP4.getCart(cs1.epoch, line1, line2);
				
				out[i]=cs1.getDist(cs2); // residual is the position error
			}
		}
		else
		{
			List<CartesianState> carts2 = USSFSGP4.getCarts(mseList, tle);
			for(int i=0; i<size; i++)
			{
				cs1 = carts.get(i);
				cs2 = carts2.get(i);
				out[i]=cs1.getDist(cs2);
				//System.out.println(out[i]+"\t"+cs1 + "\t" + cs2);
			}
		}
		
		return out;
	}
	
	protected TLE paramsToTLE(double params[])
	{
		MutableTLE tle = null;
		
		double vals[] = EphemerisUtil.getCOEFromEquinoctal(params);

		tle = new MutableTLE(line1,line2);
		tle.setElType(4);
		
		//i, Om, ecc, w, M, n
		tle.setIncDeg(vals[0]);
		tle.setRaanDeg(vals[1]);
		tle.setEcc(vals[2]);
		tle.setArgpDeg(vals[3]);
		tle.setMaDeg(vals[4]);
		tle.setN(vals[5]);
		
		double val = 0;
		if(fitBTerm && params.length>6)
		{
			//val = params[6]*params[6];
			//if(val > 1e3) val = 1e3;
			//tle.setBstar(val);
			//System.out.println("bstar\t"+params[6]);
			//System.out.println(tle.getLine1());
			tle.setBstar(params[6]);
		}
		if(fitAGOM && params.length>6)
		{
			if(fitBTerm)
			{
				val = params[7];
			}
			else
			{
				val = params[6];
			}
			//val = val*val;
			//if(val > 1e3) val = 1e3;
			tle.setNDDot(val);

		}
		
		//System.err.println("Test vals\t"+tle.getNDDot()+"\t"+tle.getBstar());
		
		//System.out.println("bstarnddot\t"+tle.getBstar()+"\t"+tle.getNDDot());
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
