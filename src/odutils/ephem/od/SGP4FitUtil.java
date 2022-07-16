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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import odutils.ephem.CartesianState;
import odutils.ephem.MutableTLE;

import sgp4.TLE;

public class SGP4FitUtil 
{
	public static final double RMS_EPS = 1e-5;
	
	public static double[] simpleNewTerms(double val, int nt, double fact)
	{
		double out[] = new double[nt];
		
		double max = val*fact;
		double min = val/fact;
		
		double inc = (max-min)/(nt-1);
		if(1<0)
		//if(fact < 1.5*nt)
		{
			val = max;
			for(int i=0; i<nt; i++)
			{
				out[i]=val;
				val-=inc;
			}
		}
		else
		{
			// do log
			max = Math.log10(max);
			min = Math.log10(min);
			inc = (max-min)/(nt-1);
			val = max;
			for(int i=0; i<nt; i++)
			{
				out[i]=val;
				val-=inc;
			}

			for(int i=0; i<nt; i++)
			{
				out[i]=Math.pow(10, out[i]);
			}
		}
		
		return out;
	}
	
	public static double[] newTerms(double val, double termsIn[], int nt, boolean doLog)
	{
		double out[]= new double[nt];
		
		int ind = -1;
		double diff = 0;
		int size = termsIn.length;
		double minDiff = Double.MAX_VALUE;
		for(int i=0; i<size; i++)
		{
			diff = Math.abs(val-termsIn[i]);
			if(diff < minDiff)
			{
				ind = i;
				minDiff = diff;
			}
		}
		
		int minInd = ind-2;
		int maxInd = ind+2;
		if(minInd <0)minInd = 0;
		if(maxInd > size-1)maxInd = size-1;
		
		double val1 = termsIn[minInd];
		double val2 = termsIn[maxInd];
		
		if(doLog)
		{
			if(val2 == 0)
			{
				val1 = Math.log10(val1);			
				val2 = -10;
			}
			else
			{
				val1 = Math.log10(val1);
				val2 = Math.log10(val2);
			}
			
			diff = (val2-val1)/(nt-1.0);
			double nv = val1;
			
			val1 = termsIn[minInd];
			val2 = termsIn[maxInd];
			
			for(int i=0; i<nt; i++)
			{
				out[i]=Math.pow(10.0d,nv);
				System.out.println(out[i]);
				nv+=diff;
			}
		}
		else
		{
			diff = (val2-val1)/(nt-1.0);
			double nv = val1;
			
			
			for(int i=0; i<nt; i++)
			{
				out[i]=nv;
				System.out.println(out[i]);
				nv+=diff;
			}
			
		}
		out[0]=val1;
		out[nt-1]=val2;
		System.out.println("New terms range between " + out[0] + " and " + out[nt-1]);
		
		return out;
	}
	
	//public static final double[] WIDERANGE = {2,1,0.5,0.2,0.1,0.05,0.02,0.01,0.005,0.002,0.001,0.0005,0.0002,0.0001,0.00001,0};
	public static final double[] WIDERANGE = {1,0.5,0.2,0.1,0.05,0.02,0.01,0.005,0.002,0.001,0.0005,0.0002,0.0001,0.00001,0};
	public static final double[] SMALLERRANGE = {0.01,0.005,0.002,0.001,0.0005,0.0002,0.0001,0.00001,0};
	
	
	public static TLE minRMSForTerms(TLE initTLE, List<CartesianState> carts, boolean isTEME, boolean doBstar)
	{
		double bterms[]={2,1,0.5,0.2,0.1,0.05,0.02,0.01,0.005,0.002,0.001,0.0005,0.0002,0.0001,0.00001,0};
		return minRMSForTerms(initTLE,carts,isTEME,doBstar,bterms,true, new double[3]);
	}
	
	public static TLE minRMSForTerms(TLE initTLE, List<CartesianState> carts, boolean isTEME, boolean doBstar, double bterms[],boolean doXP, double da[])
	{
		TLE tleOut = null;		
		
		double minRMS = Double.MAX_VALUE;
		double rms = 0;
		
		TLE testTLE = null;
		
		if(da == null) da = new double[2];
		
		boolean hasZero = false;
		double zeroRMS = 0;
		TLE zeroTLE = null;
		
		double vals[][]=new double[bterms.length][2];
		
		double RMSVAL0 = 0;
		
		for(int i=0; i<bterms.length; i++)
		{
			MutableTLE mTLE = new MutableTLE(initTLE.getLine1(),initTLE.getLine2());
			if(doBstar)
			{
				mTLE.setBstar(bterms[i]);
			}
			else
			{
				mTLE.setNDDot(bterms[i]);
			}
			mTLE.commit();
			testTLE = mTLE;
			if(doXP)
			{
				testTLE = FitSGP4XP.fitSGP4XP(carts, isTEME,testTLE.getEpoch(), false, false,testTLE,da, false);
			}
			else
			{
				testTLE = FitSGP4.fitSGP4(carts, isTEME,testTLE.getEpoch(), false, testTLE,da, false);
			}
			rms = da[0];
			if(i==0)
			{
				RMSVAL0=rms;
			}
			if(rms<minRMS)
			{
				tleOut = testTLE;
				minRMS = rms;
			}
			//System.out.println(bterms[i]+"\t"+rms);
			vals[i][0]=bterms[i];
			vals[i][1]=rms;
			
			if(bterms[i]==0)
			{
				hasZero = true;
				zeroRMS = rms;
				zeroTLE = testTLE;
			}
			
			if(i>3)
			{
				if(rms > 3*RMSVAL0)
				{
					// let's stop if we have big error
					for(int j=i; j<vals.length; j++)
					{
						vals[j][0]=bterms[j];
						vals[j][1]=rms;
					}
					break;
				}
			}
		}
		
		for(int i=0; i<vals.length; i++)
		{
			System.out.println(vals[i][0]+"\t"+vals[i][1]);
		}
		
		if(hasZero)
		{
			double delt = Math.abs(minRMS-zeroRMS)/minRMS;
			if(delt < RMS_EPS)
			{
				tleOut = zeroTLE;
				
				System.err.println("Reseting term to 0\t"+minRMS+"\t"+zeroRMS+"\t"+delt);
			}
		}
		da[0]=minRMS;
		System.out.println("minRMS\t"+minRMS + "\t" + tleOut.getNDDot()+"\t"+tleOut.getBstar());
		

		//try{Thread.sleep(100000);}catch(Exception ex){};
		return tleOut;
	}

	public static TLE minRMSForTermsXPThreads(TLE initTLE, List<CartesianState> carts, boolean isTEME, boolean doBstar, double bterms[],
			boolean doXP, double da[])
	{
		TLE tleOut = null;		
		
		double minRMS = Double.MAX_VALUE;
		double rms = 0;
		
		TLE testTLE = null;
		
		if(da == null)da = new double[2];
		
		boolean hasZero = false;
		double zeroRMS = 0;
		TLE zeroTLE = null;
		
		double vals[][]=new double[bterms.length][2];
		
		if(doXP)
		{
			ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
			List<RMSTermWork> workers = RMSTermWork.buildWork(carts, isTEME, initTLE, bterms, doBstar);
			for(int i=0; i<workers.size(); i++)
			{
				executor.execute(workers.get(i));
			}
			
			
			for(int i=0; i<100; i++)
			{
				System.err.println(executor.getTaskCount()+"\t"+executor.getCompletedTaskCount()+"\t"+executor.getActiveCount());
				try {Thread.sleep(1000);}catch(Exception ex) {}
				
				if(executor.getCompletedTaskCount() == executor.getTaskCount())
				{
					executor.shutdown();
					break;
				}
			}
			
			for(int i=0; i<workers.size(); i++)
			{
				RMSTermWork w = workers.get(i);
				System.out.println(w.term + "\t" + w.rms + "\t" + w.fitTLE);
				rms = w.rms;
				testTLE = w.fitTLE;
				if(rms<minRMS)
				{
					tleOut = testTLE;
					minRMS = rms;
				}
				//System.out.println(bterms[i]+"\t"+rms);
				vals[i][0]=w.term;
				vals[i][1]=rms;
				
				if(w.term==0)
				{
					hasZero = true;
					zeroRMS = rms;
					zeroTLE = testTLE;
				}
			}
		}
		else
		{
			for(int i=0; i<bterms.length; i++)
			{
				MutableTLE mTLE = new MutableTLE(initTLE.getLine1(),initTLE.getLine2());
				if(doBstar)
				{
					mTLE.setBstar(bterms[i]);
				}
				else
				{
					mTLE.setNDDot(bterms[i]);
				}
				mTLE.commit();
				testTLE = mTLE;
				
				testTLE = FitSGP4.fitSGP4(carts, isTEME,testTLE.getEpoch(), false, testTLE,da, false);
				
				rms = da[0];
				if(rms<minRMS)
				{
					tleOut = testTLE;
					minRMS = rms;
				}
				//System.out.println(bterms[i]+"\t"+rms);
				vals[i][0]=bterms[i];
				vals[i][1]=rms;
				
				if(bterms[i]==0)
				{
					hasZero = true;
					zeroRMS = rms;
					zeroTLE = testTLE;
				}
			}
		}
		
		for(int i=0; i<vals.length; i++)
		{
			System.out.println(vals[i][0]+"\t"+vals[i][1]);
		}
		
		if(hasZero)
		{
			double delt = Math.abs(minRMS-zeroRMS)/minRMS;
			if(delt < RMS_EPS)
			{
				tleOut = zeroTLE;
				
				System.err.println("Reseting term to 0\t"+minRMS+"\t"+zeroRMS+"\t"+delt);
			}
		}
		
		if(tleOut == null) tleOut = initTLE;
		System.out.println("minRMS\t"+minRMS + "\t" + tleOut.getNDDot()+"\t"+tleOut.getBstar());
		da[0]=minRMS;
		//try{Thread.sleep(100000);}catch(Exception ex){};
		return tleOut;
	}

	public static class RMSTermWork implements Runnable
	{
		public List<CartesianState> carts;
		public TLE testTLE;
		public boolean isTEME;
		public double rms = Double.MAX_VALUE;
		public double term = 0;
		public TLE fitTLE;
		public boolean finished = false;
		
		public RMSTermWork()
		{
			
		}
		
		public void run()
		{
			double da[] = new double[3];
			fitTLE = FitSGP4XP.fitSGP4XP(carts, isTEME,testTLE.getEpoch(), false, false,testTLE,da, false);
			rms = da[0];
			finished = true;
		}
		
		public static List<RMSTermWork> buildWork(List<CartesianState> carts, boolean isTEME, TLE initTLE, double terms[], boolean doBstar)
		{
			int nt = terms.length;
			List<RMSTermWork> out = new ArrayList<RMSTermWork>(nt);
			RMSTermWork work = null;
			
			for(int i=0; i<nt; i++)
			{
				MutableTLE mTLE = new MutableTLE(initTLE.getLine1(),initTLE.getLine2());
				if(doBstar)
				{
					mTLE.setBstar(terms[i]);
				}
				else
				{
					mTLE.setNDDot(terms[i]);
				}
				mTLE.commit();
	
				work = new RMSTermWork();
				work.carts = carts;
				work.testTLE = mTLE;
				work.term = terms[i];
				work.isTEME = isTEME;
				out.add(work);
			}
			
			return out;
		}
	}
}
