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
import org.hipparchus.linear.Array2DRowRealMatrix;
import org.hipparchus.linear.ArrayRealVector;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.linear.RealVector;
import org.hipparchus.optim.nonlinear.vector.leastsquares.MultivariateJacobianFunction;
import org.hipparchus.util.Pair;

/**
 * Provided a MultivariateVectorFunction, this class can compute the approximate Jacobian using finite differences.
 * 
 */
public class FiniteDifferenceJacobian implements MultivariateJacobianFunction
{
	protected MultivariateVectorFunction func = null;
	protected double h[] = null;
	protected boolean isPerc[] = null;
	
	public FiniteDifferenceJacobian(MultivariateVectorFunction f, double h[], boolean isPerc[])
	{
		func = f;
		this.h = h;
		this.isPerc = isPerc;
	}

	@Override
	public Pair<RealVector, RealMatrix> value(RealVector params) 
	{
		RealVector value = new ArrayRealVector(func.value(params.toArray()));
		
		int nr = value.getDimension();
		int size = params.getDimension();
		
		RealMatrix jacobian = new Array2DRowRealMatrix(nr, size);

		RealVector rvP2; // copy of params
		//double v[] = null;
		//double v2[] = null;
		double invH = 0;
        // compute the jacobian by perturbing the parameters slightly
        // then seeing how it affects the results.
		/*
        for( int i = 0; i < size; i++ ) {
        	
        	rvP2 = params.copy();
        	rvP2.setEntry(i, params.getEntry(i)+h[i]);
        	
        	v = func.value(rvP2.toArray());
        	//invH = 1.0d/h[i];
        	invH = 1.0d/(2.0d*h[i]);
        	rvP2.setEntry(i, params.getEntry(i)-h[i]);
        	
        	v2 = func.value(rvP2.toArray());
        	
        	for(int j=0; j<nr; j++)
        	{
//        		jacobian.setEntry(j, i, (v[j]-value.getEntry(j))*invH);
        		
        		jacobian.setEntry(j, i, (v[j]-v2[j])*invH);
        	}
        }
        */
		
		double vph[] = null;
		double vp2h[] = null;
		double vmh[] = null;
		double vm2h[] = null;
		double v[] = value.toArray();
		double hval = 0;
		double tv = 0;
		
        for( int i = 0; i < size; i++ ) {
        	hval = h[i];
        	if(isPerc[i])
        	{
        		tv = Math.abs(params.getEntry(i));
        		if(tv > 0)
        		{
        			//System.out.println(i + "\t" + params.getEntry(i) + "\t" + hval + "\t" + (hval*tv));
        			hval = tv*hval;
        		} // else use the percent as the value for h
        	}
        	rvP2 = params.copy();
        	rvP2.setEntry(i, params.getEntry(i)+hval);
        	vph = func.value(rvP2.toArray());
        	rvP2.setEntry(i, params.getEntry(i)+2*hval);
        	vp2h = func.value(rvP2.toArray());
        	
        	if(params.getEntry(i)!=0)
        	//if(1>0)
        	{
        	
	        	invH = 1.0d/(12.0d*h[i]);
	        	
	        	rvP2.setEntry(i, params.getEntry(i)-hval);
	        	vmh = func.value(rvP2.toArray());
	        	rvP2.setEntry(i, params.getEntry(i)-2*hval);
	        	vm2h = func.value(rvP2.toArray());
	        	
	        	for(int j=0; j<nr; j++)
	        	{
	        		
	        		jacobian.setEntry(j, i, (-vp2h[j]+8.0*vph[j]-8.0*vmh[j]+vm2h[j])*invH);
	        	}
        	}
        	else
        	{
        		// use forward difference approximation
        		invH = 1.0d/(2.0d*hval);
	        	for(int j=0; j<nr; j++)
	        	{	        		
	        		jacobian.setEntry(j, i,(-3.0d*v[j]+4.0d*vph[j]-vp2h[j])*invH);
	        	}
        		
        	}
        }
        
        //System.out.println(value);
        //System.out.println(jacobian);
		return new Pair<RealVector, RealMatrix>(value, jacobian);
	}	
}
