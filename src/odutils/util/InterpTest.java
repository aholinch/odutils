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

public class InterpTest 
{
    public static class SinFunc implements Func
    {
    	public double[][] calc(double x)
    	{
    		double out[][] = new double[1][3];
    		
    		// function
    		out[0][0] = Math.sin(x);
    		// first derivative
    		out[0][1] = Math.cos(x);
    		// second derivative
    		out[0][2] = -Math.sin(x);
    		
    		return out;
    	}
    }
    
    public static class PolyFunc implements Func
    {
    	public double[][] calc(double x)
    	{
    		double out[][] = new double[1][3];
    		
    		// function
    		out[0][0] = 1.5*x-0.5*x*x+3.0/8.0*x*x*x*x*x;
    		// first derivative
    		out[0][1] = 1.5-x+15.0/8.0*x*x*x*x;
    		// second derivative
    		out[0][2] = -1+60.0/8.0*x*x*x;
    		
    		return out;
    	}
    }
    
    public static void testFunc(Func f, double minx, double maxx)
    {
    	double h = maxx-minx;
    	
    	int cnt = 20;

    	h = h/((double)cnt);
    	
    	for(int qq = 0; qq<5; qq++)
    	{
    	double dx = 0.2*h;
    	double x = minx;
    	double tx = 0;
    	double t = 0;
    	double vals1[][] = null;
    	double vals2[][] = null;
    	double fv = 0;
    	double iv = 0;
    	
    	double p1 = 0;
    	double p2 = 0;
    	double v1 = 0;
    	double v2 = 0;
    	double a1 = 0;
    	double a2 = 0;
    	double err = 0;
    	double toterr = 0;
    	
    	for(int i=0; i<cnt; i++)
    	{
    		vals1 = f.calc(x);
    		vals2 = f.calc(x+h);
    		
    		p1 = vals1[0][0];
    		v1 = vals1[0][1];
    		a1 = vals1[0][2];
    		p2 = vals2[0][0];
    		v2 = vals2[0][1];
    		a2 = vals2[0][2];
    		
    		for(int j=0; j<6; j++)
    		{
    			t = j*0.2;
    			tx = t*h+x;
    			fv = f.calc(tx)[0][0];
    			iv = InterpUtil.hermiteCubicInterp(t, 1, p1, p2, v1, v2);
    			//iv = InterpUtil.hermiteQuinticInterp(t, h, p1, p2, v1, v2, a1, a2);
    			err = fv-iv;
    			err = err*err;
    			toterr +=err;
    			System.out.println(tx + "\t" + t + "\t" +fv + "\t" + iv + "\t" + err);
    		}
    		
    		x+=h;
    	}
    	
    	System.out.println(h+"\t"+Math.sqrt(toterr/(cnt*4))+"\n\n");

    	h*=0.5;
    	cnt*=2;
    	}
    }
    
    public static void testFunc2(Func f, double minx, double maxx)
    {
    	int size = 41;
    	
    	double h = (maxx-minx)/((double)(size-1));
    	
    	double x[] = new double[size];
    	double y[] = new double[size];
    	double z[] = new double[size];
    	
    	double x0 = 0;
    	double vals[][] = null;
    	
    	for(int i=0; i<size; i++)
    	{
    		x[i]=x0;
    		vals = f.calc(x0);
    		y[i]=vals[0][0];
    		z[i]=vals[0][1];
    		x0+=h;
    	}
    	
    	h *= 0.1d;
    	size = 10*(size-1)+1;
    	double out[] = null;
    	x0 = 0;
    	for(int i=0; i<size; i++)
    	{
    		vals = f.calc(x0);
    		out = InterpUtil.hermiteWFD(x, y, z, 4, x0);
    		System.out.println(i + "\t" + x0 + "\t" + vals[0][0] + "\t" + out[0] + "\t" + vals[0][1] + "\t" + out[1] + "\t"+out[2]);
    		x0+=h;
    	}
    }
    
    public static void main(String args[])
    {
    	Func f = null;
    	
    	f = new SinFunc();
    		
    	f = new PolyFunc();
    	
    	testFunc2(f,0,Math.PI);
    	//testFunc(new PolyFunc(),0,Math.PI);
    	System.exit(0);
    }
}
