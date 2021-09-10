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
package odutils.util;

public class MathUtil 
{
	public static double[] mult(double v1[], double s)
	{
		int l = v1.length;
		for(int i=0; i<l; i++)v1[i]*=s;
		return v1;
	}
	
    public static double[] sub(double v1[], double v2[])
    {
    	int len = Math.min(v1.length, v2.length);
    	double v[] = new double[len];
    	for(int i=0; i<len; i++)
    	{
    		v[i]=v1[i]-v2[i];
    	}
    	return v;
    }
    
    /**
     * Return 2-norm magnitude.
     * 
     * @param v
     * @return
     */
    public static double mag(double v[])
    {
    	double sum = 0;
    	int l = v.length;
    	for(int i=0; i<l; i++)sum+=v[i]*v[i];
    	return Math.sqrt(sum);
    }
    
    public static double dot(double v1[], double v2[])
    {
    	double sum = 0;
    	int l = v1.length;
    	for(int i=0; i<l; i++)sum+= v1[i]*v2[i];
    	return sum;
    }
    
    /**
     * Assumes 3d.
     * 
     * @param v1
     * @param v2
     * @return
     */
    public static double[] cross(double v1[], double v2[])
    {
    	double out[] = new double[3];
    	
    	out[0]=v1[1]*v2[2]-v1[2]*v2[1];
    	out[1]=v1[2]*v2[0]-v1[0]*v2[2];
    	out[2]=v1[0]*v2[1]-v1[1]*v2[0];
    	
    	return out;
    }
}
