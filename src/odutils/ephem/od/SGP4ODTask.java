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

public class SGP4ODTask extends AbstractODTask 
{
	protected boolean solveForBStar = true;
	protected boolean allowDownsample = true;
	protected double PREFERREDBTERMS[] = null;
	
    public SGP4ODTask()
    {
    	type = ODTask.TYPE_SGP4;
    }
    
    public void setSolveForBStar(boolean flag)
    {
    	solveForBStar = flag;
    }
    
    public boolean getSolveForBStar()
    {
    	return solveForBStar;
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
			List<CartesianState> carts = obsSet.getTEMECarts();
			
			if(epoch == null)
			{
				epoch = carts.get(carts.size()-1).getEpoch();
			}
			
			TLE initTLE = null;
			if(initState == null)
			{
				int step = InitialOD.estimateBestStep(carts,15);
				
				CartesianState cart = InitialOD.gibbs(carts.get(0), carts.get(step), carts.get(2*step), EphemerisUtil.GM_Earth_km3);
				
				initTLE = CartToTLE.cartToTLE(cart, "99999");
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
			
			double INITBTERMS[] = SGP4FitUtil.SMALLERRANGE;
			if(PREFERREDBTERMS != null)
			{
				INITBTERMS = PREFERREDBTERMS;
			}
			
			INITBTERMS = new double[] {0,
					1.00E-09,
					1.78E-09,
					3.16E-09,
					5.62E-09,
					1.00E-08,
					1.78E-08,
					3.16E-08,
					5.62E-08,
					1.00E-07,
					1.78E-07,
					3.16E-07,
					5.62E-07,
					1.00E-06,
					1.78E-06,
					3.16E-06,
					5.62E-06,
					1.00E-05,
					1.78E-05,
					3.16E-05,
					5.62E-05,
					1.00E-04,
					1.78E-04,
					3.16E-04,
					5.62E-04,
					1.00E-03,
					1.78E-03,
					3.16E-03,
					5.62E-03,
					1.00E-02,
					1.78E-02,
					3.16E-02,
					5.62E-02,
					1.00E-01,
					1.78E-01,
					3.16E-01,
					5.62E-01,
					1.00E+00,
					1.78E+00,
					3.16E+00,
					5.62E+00,
					1.00E+01,
					1.78E+01,
					3.16E+01,
					5.62E+01,
					1.00E+02,
					1.78E+02,
					3.16E+02,
					5.62E+02,
					1.00E+03,
					1.78E+03,
					3.16E+03,
					5.62E+03,
					1.00E+04,
					1.78E+04,
					3.16E+04,
					5.62E+04,
					1.00E+05,
					1.78E+05,
					3.16E+05,
					5.62E+05,
					1.00E+06,
					1.78E+06,
					3.16E+06,
					5.62E+06,
					1.00E+07,
					1.78E+07,
					3.16E+07,
					5.62E+07,
					1.00E+08,
					1.78E+08,
					3.16E+08,
					5.62E+08,
					1.00E+09,
					1.78E+09,
					3.16E+09
			};
			boolean doLog = true;
			
			TLE tle = null;
			if(carts.size()>1000 && allowDownsample)
			{
				// downsample
				initTLE = downsample(100,epoch,initTLE,carts,null);
				INITBTERMS = SGP4FitUtil.simpleNewTerms(initTLE.getBstar(), 21, 10.0);
				doLog = false;
				tle = initTLE;
			}
			else
			{
				tle = FitSGP4.fitSGP4(carts, isTEME, epoch, solveForBStar, initTLE, da, true);
			}

			// yes we will downsample again!
			if(carts.size()>50 && allowDownsample)
			{
				// downsample
				initTLE = downsample(carts.size()/3,epoch,tle,carts,INITBTERMS);
				INITBTERMS = SGP4FitUtil.simpleNewTerms(initTLE.getBstar(), 31, 3.0);
				doLog = false;
				tle = initTLE;
			}

			this.rms = da[0];
			
			OrbitState orbit = new OrbitState();
			orbit.tle = tle;
			orbit.rms = this.rms;
			this.solvedState = orbit;
			this.iter = 1;
			orbit.numIters = this.iter;
			
			double del = 0;
			INITBTERMS = new double[] {0,
					1.00E-09,
					1.78E-09,
					3.16E-09,
					5.62E-09,
					1.00E-08,
					1.78E-08,
					3.16E-08,
					5.62E-08,
					1.00E-07,
					1.78E-07,
					3.16E-07,
					5.62E-07,
					1.00E-06,
					1.78E-06,
					3.16E-06,
					5.62E-06,
					1.00E-05,
					1.78E-05,
					3.16E-05,
					5.62E-05,
					1.00E-04,
					1.78E-04,
					3.16E-04,
					5.62E-04,
					1.00E-03,
					1.78E-03,
					3.16E-03,
					5.62E-03,
					1.00E-02,
					1.78E-02,
					3.16E-02,
					5.62E-02,
					1.00E-01,
					1.78E-01,
					3.16E-01,
					5.62E-01,
					1.00E+00,
					1.78E+00,
					3.16E+00,
					5.62E+00,
					1.00E+01,
					1.78E+01,
					3.16E+01,
					5.62E+01,
					1.00E+02,
					1.78E+02,
					3.16E+02,
					5.62E+02,
					1.00E+03,
					1.78E+03,
					3.16E+03,
					5.62E+03,
					1.00E+04,
					1.78E+04,
					3.16E+04,
					5.62E+04,
					1.00E+05,
					1.78E+05,
					3.16E+05,
					5.62E+05,
					1.00E+06,
					1.78E+06,
					3.16E+06,
					5.62E+06,
					1.00E+07,
					1.78E+07,
					3.16E+07,
					5.62E+07,
					1.00E+08,
					1.78E+08,
					3.16E+08,
					5.62E+08,
					1.00E+09,
					1.78E+09,
					3.16E+09
			};
			// let's try to improve upon bstar fit
			if(solveForBStar)
			{
				TLE tleOut = orbit.tle;
				double BTERMS[] = INITBTERMS;
				double dda[] = new double[6];
				double minRMS = Double.MAX_VALUE;
				TLE tleMinRMS = null;
				
				for(int i=0; i<maxIters; i++)
				{
					tleOut = SGP4FitUtil.minRMSForTerms(tleOut,carts,isTEME,true,BTERMS,false,dda);
					if(dda[0]<minRMS)
					{
						minRMS = dda[0];
						tleMinRMS = tleOut;
					}
					
					
					BTERMS=SGP4FitUtil.newTerms(tleOut.getBstar(),BTERMS,9, doLog);
					BTERMS[BTERMS.length-1]*=1.01;

					if(i>1)doLog = false;
					
					this.iter = (i+2);
					this.rms = minRMS;
					
					del = Math.abs(BTERMS[0]-BTERMS[BTERMS.length-1])/BTERMS[0];
					System.err.println("\n\n"+i+"\t"+del+"\n\n");

					if(del<3e-5)
					{
						break;
					}
					
				}
				
				orbit = new OrbitState();
				orbit.tle = tleMinRMS;
				this.rms = minRMS;
				orbit.rms = minRMS;
				orbit.numIters = this.iter;
				this.solvedState = orbit;
			
				// see if fine tuning helps
				tle = FitSGP4.fitSGP4(carts, isTEME, epoch, solveForBStar, tleMinRMS, da, true);
				if(da[0]<minRMS)
				{
					this.rms=da[0];
					orbit.rms=this.rms;
					orbit.tle=tle;
					minRMS = da[0];
				}
			}
			
		}
		finally
		{
			isRunning = false;
		}
		
		System.err.println("MINRMS = " + this.rms);
	}

	protected TLE downsample(int maxCarts, Date epoch, TLE initTLE, List<CartesianState> carts, double useBTERMS[])
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
		
		SGP4ODTask odTask = new SGP4ODTask();
		odTask.allowDownsample=false;
		odTask.PREFERREDBTERMS=useBTERMS;
		odTask.setObservationSet(obs);
		odTask.setEpoch(epoch);
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
