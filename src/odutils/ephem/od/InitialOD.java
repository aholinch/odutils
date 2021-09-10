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

import java.util.List;

import odutils.ephem.CartesianState;
import odutils.util.MathUtil;

public class InitialOD 
{
    public static CartesianState gibbs(CartesianState cart1, CartesianState cart2, CartesianState cart3, double mu)
    {
    	double r1[] = cart1.getRVec();
    	double r2[] = cart2.getRVec();
    	double r3[] = cart3.getRVec();
    	
    	double r1mag = cart1.getRmag();
    	double r2mag = cart2.getRmag();
    	double r3mag = cart3.getRmag();
    	
    	double r1xr2[] = MathUtil.cross(r1, r2);
    	double r2xr3[] = MathUtil.cross(r2, r3);
    	double r3xr1[] = MathUtil.cross(r3, r1);
    	
    	double D[] = new double[3];
    	double N[] = new double[3];
    	double S[] = new double[3];
    	
    	for(int i=0; i<3; i++)
    	{
    		D[i]=r1xr2[i]+r2xr3[i]+r3xr1[i];
    		N[i]=r1mag*r2xr3[i]+r2mag*r3xr1[i]+r3mag*r1xr2[i];
    		S[i]=(r2mag-r3mag)*r1[i]+(r3mag-r1mag)*r2[i]+(r1mag-r2mag)*r3[i];
    	}
    	
    	double B[] = MathUtil.cross(D, r2);
    	
    	double nmag = MathUtil.mag(N);
    	double dmag = MathUtil.mag(D);
    	
    	double c = Math.sqrt(mu/(nmag*dmag));
    	double invr2 = 1.0d/r2mag;
    	
    	double v[] = new double[3];
    	
    	for(int i=0; i<3; i++)
    	{
    		v[i]=invr2*c*B[i]+c*S[i];
    	}
    	
    	CartesianState cout = new CartesianState();
    	cout.setEpoch(cart2.getEpoch());
    	cout.setRVec(cart2.getRVec());
    	cout.setVVec(v);
    	return cout;
    }

	public static int estimateBestStep(List<CartesianState> carts, double numMins) 
	{
		if(carts == null || carts.size()<3) return -1;
		
		int size = carts.size();
		if(size <6) return 1;
		
		long t1 = carts.get(0).getEpoch().getTime();
		long t2 = carts.get(1).getEpoch().getTime();
		double dt = 0;
		
		dt = (t2-t1)/60000.0d;
		int step = 1;
		// ensure points are at least numMins minutes apart
		while(dt < numMins && step < size/2)
		{
			step++;
			t2 = carts.get(step).getEpoch().getTime();
			dt = (t2-t1)/60000.0d;		
		}
		
		if(2*step >= size)step = size/2-1;

		return step;
	}
}
