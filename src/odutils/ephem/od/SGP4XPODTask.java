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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import odutils.ephem.CartesianState;
import odutils.ephem.EphemerisUtil;
import odutils.util.DateUtil;
import sgp4.TLE;

public class SGP4XPODTask extends AbstractODTask 
{
    protected boolean solveForBTerm = true;
    protected boolean solveForAGOM = true;
    protected boolean allowDownsample = true;
    protected double PREFERREDBTERMS[] = null;
    protected double PREFERREDAGOMS[] = null;
    
    public SGP4XPODTask()
    {
    	
    }
    
    public boolean getSolveForBTerm()
    {
    	return solveForBTerm;
    }
    
    public void setSolveForBTerm(boolean flag)
    {
    	solveForBTerm = flag;
    }
    
    public boolean getSolveForAGOM()
    {
    	return solveForAGOM;
    }
    
    public void setSolveForAGOM(boolean flag)
    {
    	solveForAGOM = flag;
    }
    
    protected boolean validateTerms(double terms[])
    {
    	boolean flag = true;
    	
    	if(terms[0]==terms[terms.length-1])
    	{
    		flag = false;
    	}
    	else
    	{
    		for(int i=0; i<terms.length; i++)
    		{
    			if(Double.isNaN(terms[i]))
    			{
    				flag = false;
    				break;
    			}
    			
    			if(Double.isInfinite(terms[i]))
    			{
    				flag = false;
    				break;
    			}
    		}
    	}
    	return flag;
    }
    
	@Override
	public void run() 
	{
		DateUtil.setDefaultTimeZone();
		isRunning= true;
		try
		{
			boolean isTEME = true;
			
			double da[] = new double[4];
			List<CartesianState> carts = obsSet.getCarts();
			
			if(epoch == null)
			{
				epoch = carts.get(carts.size()-1).getEpoch();
			}
			
			TLE initTLE = null;
			if(initState == null)
			{
				int step = InitialOD.estimateBestStep(carts,15);
				
				CartesianState cart = InitialOD.gibbs(carts.get(0), carts.get(step), carts.get(2*step), EphemerisUtil.GM_Earth_km3);
				
				initTLE = CartToTLE.cartToXPTLE(cart, "99999");
			}
			else
			{
				initTLE = initState.tle;
			}
			
			if(!initTLE.getEpoch().equals(epoch))
			{
				// shift epoch
				initTLE = CartToTLE.shiftEpoch(initTLE, epoch);
			}
			
			double INITBTERMS[] = SGP4FitUtil.WIDERANGE;
			double INITAGOMS[] = SGP4FitUtil.WIDERANGE;
			
			if(PREFERREDBTERMS != null)
			{
				INITBTERMS = PREFERREDBTERMS;
			}
			if(PREFERREDAGOMS != null)
			{
				INITAGOMS = PREFERREDAGOMS;
			}
			boolean doLog = true;

			TLE tle = null;

			int bothBZero = 0;
			int bothAZero = 0;

			if(carts.size()>1000 && allowDownsample)
			{
				// downsample
				initTLE = downsample(100,epoch,initTLE,carts,INITBTERMS,INITAGOMS);
				INITBTERMS = SGP4FitUtil.simpleNewTerms(initTLE.getBstar(), 15, 20.0);
				INITAGOMS = SGP4FitUtil.simpleNewTerms(initTLE.getNDDot(), 15, 20.0);
				doLog = false;
				tle = initTLE;
			}
			else
			{
				tle = FitSGP4XP.fitSGP4XP(carts, isTEME, epoch, false, false, initTLE, da, true);
			}

			// yes downsample again!
			if(carts.size()>100 && allowDownsample)
			{
				// downsample
				initTLE = downsample(carts.size()/3,epoch,initTLE,carts,INITBTERMS,INITAGOMS);
				INITBTERMS = SGP4FitUtil.simpleNewTerms(initTLE.getBstar(), 15, 3.0);
				INITAGOMS = SGP4FitUtil.simpleNewTerms(initTLE.getNDDot(), 15, 3.0);
				doLog = false;
				tle = initTLE;
				
				if(!validateTerms(INITAGOMS))
				{
					bothAZero++;
				}

				if(!validateTerms(INITBTERMS))
				{
					bothBZero++;
				}
			}

			this.rms = da[0];
			
			OrbitState orbit = new OrbitState();
			orbit.tle = tle;
			orbit.rms = this.rms;
			
			this.solvedState = orbit;
			
			// let's try to improve upon bstar fit
			if(solveForBTerm || solveForAGOM)
			{
				TLE tleOut = orbit.tle;
				double BTERMS[] = INITBTERMS;
				double AGOMS[] = INITAGOMS;
				double dda[] = new double[6];
				double minRMS = Double.MAX_VALUE;
				TLE tleMinRMS = null;
				double del = 0;
				double del2 = 0;
				
				
				for(int i=0; i<maxIters; i++)
				{
					if(solveForAGOM && bothAZero < 1)
					{
						tleOut = SGP4FitUtil.minRMSForTerms(tleOut,carts,isTEME,false,AGOMS,true,dda);
						if(dda[0]<minRMS)
						{
							minRMS = dda[0];
							tleMinRMS = tleOut;
						}
												
						AGOMS=SGP4FitUtil.newTerms(tleOut.getNDDot(),AGOMS,9, doLog);
						if(!validateTerms(AGOMS))
						{
							bothAZero++;
						}

					}

					if(solveForBTerm && bothBZero < 1)
					{
						tleOut = SGP4FitUtil.minRMSForTerms(tleOut,carts,isTEME,true,BTERMS,true,dda);
						if(dda[0]<minRMS)
						{
							minRMS = dda[0];
							tleMinRMS = tleOut;
						}
												
						BTERMS=SGP4FitUtil.newTerms(tleOut.getBstar(),BTERMS,9, doLog);
						if(!validateTerms(BTERMS))
						{
							bothBZero++;
						}
					}

					
					if(i>1) 
					{
						doLog = false;
					} 
					else if (i == 0)
					{
						// try srp again after updating BTerm one time on first iteration
						AGOMS = INITAGOMS;
					}
					
					this.iter = (i+2);
					this.rms = minRMS;
					
					del = Math.abs(BTERMS[0]-BTERMS[BTERMS.length-1])/BTERMS[0];
					del2 = Math.abs(AGOMS[0]-AGOMS[AGOMS.length-1])/AGOMS[0];
					System.err.println("\n\n"+i+"\t"+del+"\t"+del2+"\n\n");

					if(del<2e-5 || del2<2e-5)
					{
						break;
					}
				}
				
				orbit = new OrbitState();
				orbit.tle = tleMinRMS;
				this.rms = minRMS;
				orbit.rms = minRMS;
				
				tle = FitSGP4XP.fitSGP4XP(carts, isTEME, epoch, solveForBTerm, solveForAGOM, tleMinRMS, da, true);
				if(da[0]<minRMS)
				{
					this.rms = da[0];
					orbit.rms = da[0];
					orbit.tle = tle;
				}
				orbit.numIters = this.iter;
				this.solvedState = orbit;
				

			}

		}
		finally
		{
			isRunning = false;
		}
	}

	protected TLE downsample(int maxCarts, Date epoch, TLE initTLE, List<CartesianState> carts, double setBTERMS[], double setAGOMS[])
	{
		TLE out = null;
		List<CartesianState> newCarts = new ArrayList<CartesianState>(maxCarts);
		
		int step = maxCarts/3;
		int size = carts.size();
		int mid = (size-step)/2;
		for(int i=0; i<step; i++)
		{
			newCarts.add(carts.get(i));
			newCarts.add(carts.get(mid+i));
			newCarts.add(carts.get(size-1-i));
		}
	
		// we don't support weights yet, so add begin and end vectors a few times
		for(int i=0; i<step/5; i++)
		{
			newCarts.add(carts.get(i));
			newCarts.add(carts.get(size-1));
			newCarts.add(carts.get(size-1));
		}

		ObservationSet obs = new ObservationSet();
		obs.setCarts(newCarts);
		
		SGP4XPODTask odTask = new SGP4XPODTask();
		odTask.allowDownsample = false;
		odTask.PREFERREDAGOMS=setAGOMS;
		odTask.PREFERREDBTERMS=setBTERMS;
		odTask.solveForAGOM=this.solveForAGOM;
		odTask.solveForBTerm=this.solveForBTerm;
		odTask.setObservationSet(obs);
		odTask.setEpoch(epoch);
		odTask.setMaxIterations(this.maxIters/2);
		OrbitState init = new OrbitState();
		init.epoch=epoch;
		init.tle = initTLE;
		odTask.setInitialState(init);
		
		System.err.println("Nested run");
		odTask.run();
		out = odTask.getSolvedState().tle;
		System.err.println("Nested run over");
		System.err.println(out.getLine1());
		System.err.println(out.getLine2());
		
		
		return out;
	}
	@Override
	public void stopOD() {
		// TODO Auto-generated method stub

	}

}
