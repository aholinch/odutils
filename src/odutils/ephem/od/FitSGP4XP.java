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

import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresBuilder;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresOptimizer;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LevenbergMarquardtOptimizer;

import odutils.ephem.CartesianState;
import odutils.ephem.EphemerisUtil;
import odutils.ephem.MutableTLE;
import sgp4.TLE;

public class FitSGP4XP 
{
	/**
	 * Fit an SGP4XP tle to the provided SGP4 tles.
	 * 
	 * @param tles
	 * @param epoch
	 * @return
	 */
	public static TLE fitSGP4XP(List<TLE> tles, Date epoch, boolean fitBTerm, boolean fitAGOM)
	{

		// Generate cartesians for the tles
		// one rev before epoch
		int numTle = tles.size();
		List<CartesianState> carts = new ArrayList<CartesianState>(numTle*100);
		List<CartesianState> tmpCarts = null;

		double mm = 0;
		double per = 0;
		Date d1 = null;
		Date d2 = null;
		long t = 0;
		TLE tle = null;

		for(int i=0; i<numTle; i++)
		{
			tle = tles.get(i);
			d2 = tle.getEpoch();
			t = d2.getTime();
			mm = tle.getN();
			per = 1440.0d/mm+1;
			t = t - (long)(per*60.0d*1000.0d);
			d1 = new java.sql.Timestamp(t);

			tmpCarts = EphemerisUtil.getCarts(tle, d1, d2, 60.0d, false);
			carts.addAll(tmpCarts);
		}

		return fitSGP4XP(carts,true,epoch,fitBTerm,fitAGOM);
	}

	/**
	 * Fit an SGP4XP tle to the provided SGP4 tles.
	 * 
	 * @param tles
	 * @param epoch
	 * @return
	 */
	public static TLE fitSGP4XP(List<CartesianState> carts, boolean isTEME, Date epoch, boolean fitBTerm, boolean fitAGOM)
	{
		return fitSGP4XP(carts,isTEME,epoch,fitBTerm,fitAGOM,null,null,true);
	}
	
	public static TLE fitSGP4XP(List<CartesianState> carts, boolean isTEME, Date epoch, boolean fitBTerm, boolean fitAGOM, TLE initTLE, double da[], boolean do2Stage)
	{
		if(da == null)da = new double[2];

		TLE tleOut = null;
		TLE tleInit = null;
		
		boolean setBstar = false;
		double bstar = 0;
		double agom = 0;
		if(!initTLE.getEpoch().equals(epoch))
		{
			bstar = initTLE.getBstar();
			agom = initTLE.getNDDot();
			CartesianState cs = EphemerisUtil.getCart(initTLE, epoch, false);
			initTLE = CartToTLE.cartToXPTLE(cs,"99999");
			setBstar = true;
		}

		MutableTLE mtle = new MutableTLE(initTLE.getLine1(),initTLE.getLine2());
		mtle.setObjectID("99999");
		mtle.setElType(0);
		if(setBstar) 
		{
			mtle.setBstar(bstar);
			mtle.setNDDot(agom);
		}

		mtle.setElType(4);
		mtle.commit();
		
		tleInit = new TLE(mtle.getLine1(),mtle.getLine2());
		System.err.println("\n\ntleInit");
		System.err.println(tleInit.getLine1());
		System.err.println(tleInit.getLine2());

		// Perform Fit
		SGP4XPCartesianFunction fit = new SGP4XPCartesianFunction();
		fit.setCarts(carts, !isTEME);

		fit.setInitialGuess(tleInit);
		
		if(do2Stage)
		{
			// fit without them, regardless
			fit.setFitBTerm(false);
			fit.setFitAGOM(false);
		}
		else
		{
			fit.setFitBTerm(fitBTerm);
			fit.setFitAGOM(fitAGOM);
		}
		
		double initParams[] = fit.getInitParams();

		FiniteDifferenceJacobian fdj = new FiniteDifferenceJacobian(fit,fit.getDeltas(),fit.getPercs());

		LeastSquaresProblem problem = new LeastSquaresBuilder().
				start(initParams).
				model(fdj).
				parameterValidator(new SGP4XPCartesianFunction.PVal()).
				target(fit.getTarget()).
				lazyEvaluation(false).
				maxEvaluations(1000).
				maxIterations(1000).
				build();

		LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().withParameterRelativeTolerance(2e-9).optimize(problem);
		//LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);

		System.out.println("RMS: "           + optimum.getRMS());
		//System.out.println("evaluations: "   + optimum.getEvaluations());
		//System.out.println("iterations: "    + optimum.getIterations());
		double ov[] = optimum.getPoint().toArray();
		da[0]=optimum.getRMS();

		tleOut = fit.paramsToTLE(ov);


		if(do2Stage && (fitBTerm || fitAGOM))
		{
			fit.setFitBTerm(fitBTerm);
			fit.setFitAGOM(fitAGOM);
			fit.setInitialGuess(tleOut);

			initParams = fit.getInitParams();

			fdj = new FiniteDifferenceJacobian(fit,fit.getDeltas(),fit.getPercs());

			problem = new LeastSquaresBuilder().
					start(initParams).
					model(fdj).
					parameterValidator(new SGP4XPCartesianFunction.PVal()).
					target(fit.getTarget()).
					lazyEvaluation(false).
					maxEvaluations(1000).
					maxIterations(1000).
					build();

			optimum = new LevenbergMarquardtOptimizer().optimize(problem);

			System.out.println("RMS: "           + optimum.getRMS());
			System.out.println("evaluations: "   + optimum.getEvaluations());
			System.out.println("iterations: "    + optimum.getIterations());
			ov = optimum.getPoint().toArray();
			da[0]=optimum.getRMS();
			tleOut = fit.paramsToTLE(ov);				
		}
		
		return tleOut;
	}    
}
