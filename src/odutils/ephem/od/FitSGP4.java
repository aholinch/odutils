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

public class FitSGP4 
{

	/**
	 * Fit an SGP4XP tle to the provided SGP4 tles.
	 * 
	 * @param tles
	 * @param epoch
	 * @return
	 */
	public static TLE fitSGP4(List<CartesianState> carts, boolean isTEME, Date epoch, boolean fitBTerm)
	{
		return fitSGP4(carts,isTEME,epoch,fitBTerm,null,(double[])null,true);
	}
	
	public static TLE fitSGP4(List<CartesianState> carts, boolean isTEME, Date epoch, boolean fitBTerm, TLE initTLE, double da[], boolean do2Stage)
	{
		if(da == null)da = new double[2];

		TLE tleOut = null;
		TLE tleInit = null;

		boolean setBstar = false;
		double bstar = 0;
		
		if(!initTLE.getEpoch().equals(epoch))
		{
			bstar = initTLE.getBstar();
			CartesianState cs = EphemerisUtil.getCart(initTLE, epoch, false);
			initTLE = CartToTLE.cartToTLE(cs,"99999");
			setBstar = true;
		}

		MutableTLE mtle = new MutableTLE(initTLE.getLine1(),initTLE.getLine2());
		mtle.setObjectID("99999");
		mtle.setElType(0);
		if(setBstar)mtle.setBstar(bstar);
		mtle.commit();
		
		tleInit = new TLE(mtle.getLine1(),mtle.getLine2());
		System.out.println(tleInit.getLine1());
		System.out.println(tleInit.getLine2());
		

		// Perform Fit
		SGP4CartesianFunction fit = new SGP4CartesianFunction();
		fit.setCarts(carts, !isTEME);

		fit.setInitialGuess(tleInit);
		
		// fit without them, regardless
		if(do2Stage)
		{
			fit.setFitBStar(false);
		}
		else
		{
			fit.setFitBStar(fitBTerm);
		}
		
		double initParams[] = fit.getInitParams();

		FiniteDifferenceJacobian fdj = new FiniteDifferenceJacobian(fit,fit.getDeltas(),fit.getPercs());

		LeastSquaresProblem problem = new LeastSquaresBuilder().
				start(initParams).
				model(fdj).
				parameterValidator(new SGP4CartesianFunction.PVal()).
				target(fit.getTarget()).
				lazyEvaluation(false).
				maxEvaluations(4000).
				maxIterations(4000).
				build();

		LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().withParameterRelativeTolerance(2e-9).optimize(problem);

		System.out.println("RMS: "           + optimum.getRMS());
		System.out.println("evaluations: "   + optimum.getEvaluations());
		System.out.println("iterations: "    + optimum.getIterations());
		double ov[] = optimum.getPoint().toArray();
		da[0]=optimum.getRMS();

		tleOut = fit.paramsToTLE(ov);


		if(do2Stage && fitBTerm)
		{
			fit.setFitBStar(fitBTerm);
			fit.setInitialGuess(tleOut);

			initParams = fit.getInitParams();

			fdj = new FiniteDifferenceJacobian(fit,fit.getDeltas(),fit.getPercs());

			problem = new LeastSquaresBuilder().
					start(initParams).
					model(fdj).
					parameterValidator(new SGP4CartesianFunction.PVal()).
					target(fit.getTarget()).
					lazyEvaluation(false).
					maxEvaluations(1000).
					maxIterations(1000).
					build();

			optimum = new LevenbergMarquardtOptimizer().optimize(problem);

			System.out.println("RMS: "           + optimum.getRMS());
			//System.out.println("evaluations: "   + optimum.getEvaluations());
			//System.out.println("iterations: "    + optimum.getIterations());
			ov = optimum.getPoint().toArray();
			da[0]=optimum.getRMS();
			tleOut = fit.paramsToTLE(ov);				
		}
		
		return tleOut;
	}    
}
