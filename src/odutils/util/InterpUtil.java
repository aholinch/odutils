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

/**
 * Hermite Interpolation taken from notes:
 * 
 * MA 323 Geometric Modelling Course Notes, Day 9, David L. Finn, 2004.
 *
 * t is the parameter between 0 and 1 in the interval.
 * dx is the size of the interval in the units used (meters, s, days, etc.)
 * 
 * @author aholinch
 *
 */
public class InterpUtil 
{
	public static double hermiteCubicInterp(double t, double dx, double p1, double p2, double v1, double v2)
	{
		return hermiteCubicInterp(t,dx,p1,p2,v1,v2,null);
	}

	public static double hermiteCubicInterp(double t, double dx, double p1, double p2, double v1, double v2, double w[])
	{
		double out = 0;

		w = hermiteCubicWeights(t,w);

		out = w[0]*p1+dx*(w[1]*v1+w[2]*v2)+w[3]*p2;

		return out;
	}

	public static double hermiteQuinticInterp(double t, double dx, double p1, double p2, double v1, double v2, double a1, double a2)
	{
		return hermiteQuinticInterp(t,dx,p1,p2,v1,v2,a1,a2,null);
	}

	public static double hermiteQuinticInterp(double t, double dx, double p1, double p2, double v1, double v2, double a1, double a2, double w[])
	{
		double out = 0;

		w = hermiteQuinticWeights(t,w);

		//out = w[0]*p1 + w[1]*v1 + w[2]*a1 + w[3]*a2 + w[4]*v2 + w[5]*p2;
		out = w[0]*p1 + dx*(w[1]*v1+ w[4]*v2) +  dx*dx*(w[2]*a1 + w[3]*a2) + w[5]*p2;

		return out;
	}

	public static double[] hermiteCubicWeights(double t)
	{
		return hermiteCubicWeights(t,new double[4]);
	}

	public static double[] hermiteCubicWeights(double t, double w[])
	{
		if(w == null) w = new double[4];

		double t2 = t*t;
		double t3 = t2*t;

		w[0] = 1.0 - 3.0*t2 + 2.0*t3;
		w[1] = t - 2.0*t2 + t3;
		w[2] = -t2 + t3;
		w[3] = 3.0*t2 - 2.0*t3;

		return w;
	}

	public static double[] hermiteQuinticWeights(double t)
	{
		return hermiteQuinticWeights(t,new double[6]);
	}

	public static double[] hermiteQuinticWeights(double t, double w[])
	{
		if(w == null) w = new double[6];

		double t2 = t*t;
		double t3 = t2*t;
		double t4 = t2*t2;
		double t5 = t3*t2;

		w[0] = 1.0 - 10.0*t3 + 15.0*t4 - 6.0*t5;
		w[1] = t - 6.0*t3 + 8.0*t4 - 3.0*t5;
		w[2] = 0.5*t2 - 1.5*t3 + 1.5*t4 - 0.5*t5;
		w[3] = 0.5*t3 - t4 + 0.5*t5;
		w[4] = -4.0*t3 + 7.0*t4 - 3.0*t5;
		w[5] = 10.0*t3 - 15.0*t4 + 6.0*t5;

		return w;
	}

	/**
	 * https://ilrs.gsfc.nasa.gov/docs/2020/cpf_2.00a.tgz
	 * hermite.c
		C  Author:   W. Gurtner
		C            Astronomical Institute
		C            University of Bern
		C
		C  Created:  June 2002
		C
		C  Modified: 25-JUL-2005 : Check boundaries
		C
	 **/ 
	public static double[] hermite (int ityp, double x[], double y[], double z[], int nval, double xp)
	{
		int i, i0, j, k, n;
		int debug= 0;
		double pj, sk, vi, ui;

		double yp = 0;
		double zp = 0;
		double ircode = 0;
		int nmax = x.length;
		n=nval-1;
		
		/* Look for given values to be used */ 
		if (xp < x[0] || xp > x[nmax-1])
		{
			ircode= 2;
			return new double[]{yp,zp,ircode};
		}

		/* Look for given value immediately preceeding interpolation argument */
		for (i=0; i<nmax; i++)
		{
			if (x[i] >= xp)
			{
				ircode= 0;
				//System.out.println(i + "\t" + x[i] + xp);
				break;
			}
		}

		/*  Start index in vectors x,y,z */
		i0=i-(n+1)/2;
		if (i0 < 0)
		{
			i0= 0; /* or 1? */
			ircode=1;
		}
		
		if ((i0+n) >= nmax)
		{
			i0= nmax- n-1;
			ircode=1;
		}

		if(i0 < 0)
		{
			return new double[]{yp,zp,ircode};
		}
		/* Lagrange formula for polynomial interpolation */ 
		if (Math.abs(ityp) == 1)
		{

			for (i=0; i<n+1; i++) /* i=0 or 1?? */
			{
				pj=1.e0;
				for (j=0; j<n+1; j++)
				{
					if (j != i) pj*=(xp-x[j+i0])/(x[i+i0]-x[j+i0]);
				}
				yp+=y[i+i0]*pj;
			}

			/* COMPUTE DERIVATIVE OF THE LAGRANGE POLYNOMIAL */
			if (ityp == -1)
			{
				for (i=0; i<n+1; i++) /* i=0 or 1?? */
				{
					sk=0.e0;
					for (k=0; k<n+1; k++) /* i=0 or 1?? */
					{
						if (k != i) 
						{
							pj=1.e0;
							for (j=0; j<n+1; j++) /* i=0 or 1?? */
							{
								if (j != i && j != k)
								{
									pj*=(xp-x[j+i0])/(x[i+i0]-x[j+i0]);
								}
							} 
							sk+=pj/(x[i+i0]-x[k+i0]);
						}
					}
					zp+=y[i+i0]*sk;
				}
			}


		/* HERMITE INTERPOLATION (ADDITIONAL USE OF THE DERIVATIVES) */ 
		} else
			if (ityp == 2)
			{ 
				/*printf("Vel:\n");*/
				for (i=0; i<n+1; i++)
				{
					sk=0.e0;
					for (k=0; k<n+1; k++)
					{
						if (k != i) sk+= 1.e0/(x[i+i0]-x[k+i0]);
					}
					vi= 1.e0-2.e0*(xp-x[i+i0])*sk;
					ui= xp-x[i+i0];

					pj=1.e0;
					for (j=0; j<n+1; j++)
					{
						if (j != i) pj*=(xp-x[j+i0])/(x[i+i0]-x[j+i0]);
					}

					yp+=(y[i+i0]*vi+z[i+i0]*ui)*pj*pj;
				}
			}

		return new double[]{yp,zp,ircode};
	}

	/**
	 * Returns the function and first derivative at the specified point.
	 * 
	 * https://ilrs.gsfc.nasa.gov/docs/2020/cpf_2.00a.tgz
	 * hermite.c
		C  Author:   W. Gurtner
		C            Astronomical Institute
		C            University of Bern
		C
		C  Created:  June 2002
		C
		C  Modified: 25-JUL-2005 : Check boundaries
		C
	 **/ 
	public static double[] hermiteWFD (double x[], double y[], double z[], int nval, double xp)
	{
		int i, i0, j, k, n;
		double pj, sk, vi, ui;

		double yp = 0;
		double zp = 0;
		double ircode = 0;
		int nmax = x.length;
		n=nval-1;

		/* Look for given values to be used */ 
		if (xp < x[0] || xp > x[nmax-1])
		{
			ircode= 2;
			return new double[]{yp,zp,ircode};
		}

		/* Look for given value immediately preceeding interpolation argument */
		for (i=0; i<nmax; i++)
		{
			if (x[i] >= xp)
			{
				ircode= 0;
				//System.out.println(i + "\t" + x[i] + xp);
				break;
			}
		}

		/*  Start index in vectors x,y,z */
		i0=i-(n+1)/2;
		if (i0 < 0)
		{
			i0= 0; /* or 1? */
			ircode=1;
		}

		if ((i0+n) >= nmax)
		{
			i0= nmax- n-1;
			ircode=1;
		}

		if(i0 < 0)
		{
			return new double[]{yp,zp,ircode};
		}
		
		for (i=0; i<n+1; i++)
		{
			sk=0.e0;

			for (k=0; k<n+1; k++)
			{
				if (k != i)
				{
					sk+= 1.e0/(x[i+i0]-x[k+i0]);
				}
			}

			vi= 1.e0-2.e0*(xp-x[i+i0])*sk;
			ui= xp-x[i+i0];

			pj=1.e0;
			
			for (j=0; j<n+1; j++)
			{
				if (j != i) 
				{
					pj*=(xp-x[j+i0])/(x[i+i0]-x[j+i0]);
				}
			}
			
			// lagrange interp of the first derivative
			zp+=z[i+i0]*pj;
			
			// hermite interp
			yp+=(y[i+i0]*vi+z[i+i0]*ui)*pj*pj;
		}

		return new double[]{yp,zp,ircode};
	}

	/**
	 * Returns the function and first derivative at the specified point.  Works
	 * for multidimensional functions.
	 * 
	 * https://ilrs.gsfc.nasa.gov/docs/2020/cpf_2.00a.tgz
	 * hermite.c
		C  Author:   W. Gurtner
		C            Astronomical Institute
		C            University of Bern
		C
		C  Created:  June 2002
		C
		C  Modified: 25-JUL-2005 : Check boundaries
		C
	 **/ 
	public static double[][] hermiteWFDMD (double x[], double y[][], double z[][], int nval, double xp)
	{
		int i, i0, j, k, n;
		double pj, sk, vi, ui;

		int ndim = y[0].length;
		double yp[] = new double[ndim];
		double zp[] = new double[ndim];
		double zvals[] = null;
		double yvals[] = null;
		
		double ircode = 0;
		int nmax = x.length;
		n=nval-1;

		// Java sets it to 0 but lets be clear about it
		for(i=0; i<ndim; i++)
		{
			yp[i]=0;
			zp[i]=0;
		}
		
		/* Look for given values to be used */ 
		if (xp < x[0] || xp > x[nmax-1])
		{
			ircode= 2;
			return new double[][]{yp,zp,new double[]{ircode}};
		}

		/* Look for given value immediately preceeding interpolation argument */
		for (i=0; i<nmax; i++)
		{
			if (x[i] >= xp)
			{
				ircode= 0;
				//System.out.println(i + "\t" + x[i] + xp);
				break;
			}
		}

		/*  Start index in vectors x,y,z */
		i0=i-(n+1)/2;
		if (i0 < 0)
		{
			i0= 0; /* or 1? */
			ircode=1;
		}

		if ((i0+n) >= nmax)
		{
			i0= nmax- n-1;
			ircode=1;
		}

		if(i0 < 0)
		{
			return new double[][]{yp,zp,new double[]{ircode}};
		}
		
		for (i=0; i<n+1; i++)
		{
			sk=0.e0;

			for (k=0; k<n+1; k++)
			{
				if (k != i)
				{
					sk+= 1.e0/(x[i+i0]-x[k+i0]);
				}
			}

			vi= 1.e0-2.e0*(xp-x[i+i0])*sk;
			ui= xp-x[i+i0];

			pj=1.e0;
			
			for (j=0; j<n+1; j++)
			{
				if (j != i) 
				{
					pj*=(xp-x[j+i0])/(x[i+i0]-x[j+i0]);
				}
			}
			
			zvals = z[i+i0];
			yvals = y[i+i0];
			for(int d=0; d<ndim; d++)
			{
				// lagrange interp of the first derivative
				//zp[d]+=z[i+i0][d]*pj;
				zp[d]+=zvals[d]*pj;
				
				// hermite interp
				//yp[d]+=(y[i+i0][d]*vi+z[i+i0][d]*ui)*pj*pj;
				yp[d]+=(yvals[d]*vi+zvals[d]*ui)*pj*pj;
			}
		}

		return new double[][]{yp,zp,new double[]{ircode}};
	}

	/**
	 * Returns the function and first derivative at the specified point.  Works
	 * for three simultaneous functions.
	 * 
	 * https://ilrs.gsfc.nasa.gov/docs/2020/cpf_2.00a.tgz
	 * hermite.c
		C  Author:   W. Gurtner
		C            Astronomical Institute
		C            University of Bern
		C
		C  Created:  June 2002
		C
		C  Modified: 25-JUL-2005 : Check boundaries
		C
	 **/ 
	public static double[][] hermiteWFD3D (double x[], double y1[], double y2[], double y3[], double z1[], double z2[], double z3[], int nval, double xp)
	{
		int i, i0, j, k, n;
		double pj, sk, vi, ui;

		double yp1 = 0;
		double yp2 = 0;
		double yp3 = 0;
		double zp1 = 0;
		double zp2 = 0;
		double zp3 = 0;
		
		double ircode = 0;
		int nmax = x.length;
		n=nval-1;

		
		/* Look for given values to be used */ 
		if (xp < x[0] || xp > x[nmax-1])
		{
			ircode= 2;
			return new double[][]{new double[]{yp1,yp2,yp3},new double[]{zp1,yp2,zp3},new double[]{ircode}};
		}

		/* Look for given value immediately preceeding interpolation argument */
		for (i=0; i<nmax; i++)
		{
			if (x[i] >= xp)
			{
				ircode= 0;
				//System.out.println(i + "\t" + x[i] + xp);
				break;
			}
		}

		/*  Start index in vectors x,y,z */
		i0=i-(n+1)/2;
		if (i0 < 0)
		{
			i0= 0; /* or 1? */
			ircode=1;
		}

		if ((i0+n) >= nmax)
		{
			i0= nmax- n-1;
			ircode=1;
		}

		if(i0 < 0)
		{
			return new double[][]{new double[]{yp1,yp2,yp3},new double[]{zp1,yp2,zp3},new double[]{ircode}};
		}
		
		for (i=0; i<n+1; i++)
		{
			sk=0.e0;

			for (k=0; k<n+1; k++)
			{
				if (k != i)
				{
					sk+= 1.e0/(x[i+i0]-x[k+i0]);
				}
			}

			vi= 1.e0-2.e0*(xp-x[i+i0])*sk;
			ui= xp-x[i+i0];

			pj=1.e0;
			
			for (j=0; j<n+1; j++)
			{
				if (j != i) 
				{
					pj*=(xp-x[j+i0])/(x[i+i0]-x[j+i0]);
				}
			}

			zp1 += z1[i+i0]*pj;
			zp2 += z2[i+i0]*pj;
			zp3 += z3[i+i0]*pj;
			
			yp1 += (y1[i+i0]*vi + z1[i+i0]*ui)*pj*pj;
			yp2 += (y2[i+i0]*vi + z2[i+i0]*ui)*pj*pj;
			yp3 += (y3[i+i0]*vi + z3[i+i0]*ui)*pj*pj;
		}

		return new double[][]{new double[]{yp1,yp2,yp3},new double[]{zp1,zp2,zp3},new double[]{ircode}};
	}

}
