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
package odutils.ephem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import odutils.util.DateUtil;
import odutils.util.MathUtil;

import sgp4.TLE;

//https://celestrak.com/software/vallado-sw.asp

public class EphemerisUtil 
{
    public static final double eesqrd = 0.00669437999013;
    public static final double JD_19700101 = 2440587.500000;
    public static final long MJD_BASE_MS = -3506716800000l;
    
    public static final double J2 =  0.00108263;
    public static final double twopi = 2.0d*Math.PI;
    public static final double RE = 6378.137;    // km
	public static final double GEO_RADIUS = 42164.0; // km
    public final static double GM_Earth_km3 = 398600.4415; // 9


	public static List<CartesianState> genPreviousRev(TLE tle, double tstepSec, double nRevs)
	{
		long t2 = tle.getEpoch().getTime();
		double n = tle.getN()*0.95;
		n = 86400.0d/n*nRevs;
		int size = (int)(n/tstepSec);
		long t = (long)(t2-1000.0d*n);
		
		List<CartesianState> carts = new ArrayList<CartesianState>(size);
		
		long dt = (long)(tstepSec*1000.0d);
		
		while(t<=t2)
		{
			carts.add(getCart(tle,new java.sql.Timestamp(t),false));
			t+=dt;
		}
		
		if(t2>(t-dt))
		{
			carts.add(getCart(tle,new java.sql.Timestamp(t2),false));
		}
		
		return carts;
	}
	
	public static CartesianState findClosestToEpoch(List<CartesianState> carts, Date tgtEpoch)
	{
		double diff = 0;
		long t = tgtEpoch.getTime();
		CartesianState cart = null;
		CartesianState best = null;
		double bestDiff = Double.MAX_VALUE;
		int size = carts.size();
		
		for(int i=0; i<size; i++)
		{
			cart = carts.get(i);
			diff = Math.abs(t-cart.getEpoch().getTime());
			if(diff < bestDiff)
			{
				best = cart;
				bestDiff = diff;
			}
		}
		
		return best;
	}
	
	public static List<TLE> filterTLEs(List<TLE> tles, Date startDate, Date endDate)
	{
		int size = tles.size();
		List<TLE> out = new ArrayList<TLE>(size/2);
		
		long t1 = startDate.getTime();
		long t2 = endDate.getTime();
		long t = 0;
		TLE tle = null;
		
		for(int i=0; i<size; i++)
		{
			tle = tles.get(i);
			t = tle.getEpoch().getTime();
			if(t1<=t && t<=t2)
			{
				out.add(tle);
			}
		}
		
		return out;
	}
	
	public static List<CartesianState> filter(List<CartesianState> carts, Date startDate, Date endDate)
	{
		int size = carts.size();
		List<CartesianState> out = new ArrayList<CartesianState>(size/2);
		
		long t1 = startDate.getTime();
		long t2 = endDate.getTime();
		long t = 0;
		CartesianState cart = null;
		
		for(int i=0; i<size; i++)
		{
			cart = carts.get(i);
			t = cart.getEpoch().getTime();
			if(t1<=t && t<=t2)
			{
				out.add(cart);
			}
		}
		
		return out;
	}
	
    public static Ephemerides buildEphemerides(TLE tle, Date d1, Date d2, double tStepSec)
    {
    	List<CartesianState> carts = getCarts(tle,d1,d2,tStepSec,true);
    	
    	Ephemerides eph = new Ephemerides(carts,tle.getObjectID());
    	
    	return eph;
    }

    public static CartesianState getCart(TLE tle, Date d, boolean makej2k)
    {
		double rv[][] = tle.getRV(d);
		
		
		CartesianState cart = null;
		if(tle.getElType()<4)
		{
			cart = new CartesianState();
			cart.setEpoch(d);
			
			cart.setRVec(rv[0]);
			cart.setVVec(rv[1]);
		}
		else
		{
			cart = USSFSGP4.getCart(d, tle);
		}
		
		if(makej2k)
		{
		}
		
		return cart;
    }
    
    public static List<CartesianState> getCarts(TLE tle, Date d1, Date d2, double tStepSec, boolean makej2k)
    {
    	double dt = d2.getTime()-d1.getTime();
    	dt = dt/(tStepSec*1000.0);
    	
    	int size = (int)(dt+1);
    	
    	List<CartesianState> carts = new ArrayList<CartesianState>(size);
    	
    	CartesianState cart = null;
    	Date d = null;
    	long t = 0;
    	
    	long tstep = (long)(1000.0d*tStepSec);
    	
    	long t2 = d2.getTime();
    	t = d1.getTime();
    	double rv[][] = null;
    	
    	if(tle.getElType()<4)
    	{
			while(t < t2)
			{
				d = new java.sql.Timestamp(t);
				cart = new CartesianState();
				cart.setEpoch(d);
				
				rv = tle.getRV(d);
				
				cart.setRVec(rv[0]);
				cart.setVVec(rv[1]);
				carts.add(cart);
				
				t+=tstep;
			}
			
			// ensure last date is included regardless of steps
			d = new java.sql.Timestamp(t2);
			cart = new CartesianState();
			cart.setEpoch(d);
			
			rv = tle.getRV(d);
			
			cart.setRVec(rv[0]);
			cart.setVVec(rv[1]);
			carts.add(cart);
    	}
    	else
    	{
    		
    		carts = USSFSGP4.getCarts(d1, d2, tStepSec, tle);
    	}
		
		if(makej2k)
		{
		}
		
    	return carts;
    }
    
    
    
    /**
     * Compute RIC matrix
     * radial, in-track, cross-track
     * @param cs
     * @return
     */
    public static double[][] getRICMatrix(CartesianState cs)
    {
    	double out[][] = new double[3][3];
    	
    	double r[] = cs.getRVec();
    	double mag = 1.0d/MathUtil.mag(r);
    	
    	// set r
    	out[0][0]=mag*r[0];
    	out[0][1]=mag*r[1];
    	out[0][2]=mag*r[2];
    	
    	// get cross track
    	double c[] = MathUtil.cross(r, cs.getVVec());
    	mag = 1.0d/MathUtil.mag(c);
    	out[2][0]=mag*c[0];
    	out[2][1]=mag*c[1];
    	out[2][2]=mag*c[2];
    	
    	double i[] = MathUtil.cross(out[2], out[0]);
    	out[1][0]=i[0];
    	out[1][1]=i[1];
    	out[1][2]=i[2];
    	
    	return out;
    }
    
    public static List<TLE> parseTLEs(String file)
    {
    	List<TLE> tles = new ArrayList<TLE>(30000);
    	
    	BufferedReader br = null;
    	FileReader fr = null;
    	TLE tle = null;
    	try
    	{
    		fr = new FileReader(file);
    		br = new BufferedReader(fr);
    		
    		String line1 = null;
    		String line2 = null;
    		
    		line1 = br.readLine();
    		line2 = br.readLine();
    		while(line2 != null)
    		{
    			if(line1.startsWith("1 ") && line2.startsWith("2 "))
    			{
    				tle = new TLE(line1,line2);
    				tles.add(tle);
    			}
    			line1 = line2;
    			line2 = br.readLine();
    		}
    	}
    	catch(Exception ex)
    	{
    		br = null;
    	}
    	finally
    	{
    		if(fr != null)try{fr.close();}catch(Exception ex){}
    		if(br != null)try{br.close();}catch(Exception ex){}
    	}
    	
    	return tles;
    }

    /**
     * returns ke,he,L,pe,qe,n
     * 
     * @param tle
     * @return
     */
    public static double[] getEquinoctal(TLE tle)
    {
    	
    	double e = tle.getEcc();
    	double Om = tle.getRaanDeg();
    	double w = tle.getArgpDeg();
    	double M = tle.getMaDeg();
    	double i = tle.getIncDeg();
    	
    	Om = Math.toRadians(Om);
    	w = Math.toRadians(w);
    	M = Math.toRadians(M);
    	i = Math.toRadians(i);
    	
    	double ke = e*Math.cos(Om+w);
    	double he = e*Math.sin(Om+w);
    	double L = M+w+Om;
    	double pe = Math.tan(0.5*i)*Math.sin(Om);
    	double qe = Math.tan(0.5*i)*Math.cos(Om);
    	
    	
    	return new double[]{ke,he,L,pe,qe,tle.getN()};
    }

    public static double[] getEquinoctal(KeplerianState kep)
    {
    	double e = kep.ecc;
    	double Om = kep.omegaDeg;
    	double w = kep.argpDeg;
    	double M = kep.maDeg;
    	double i = kep.incDeg;
    	
    	Om = Math.toRadians(Om);
    	w = Math.toRadians(w);
    	M = Math.toRadians(M);
    	i = Math.toRadians(i);
    	
    	double ke = e*Math.cos(Om+w);
    	double he = e*Math.sin(Om+w);
    	double L = M+w+Om;
    	double pe = Math.tan(0.5*i)*Math.sin(Om);
    	double qe = Math.tan(0.5*i)*Math.cos(Om);
    	
    	
    	return new double[]{ke,he,L,pe,qe,kep.meanMotion};
    }

    /**
     * Return i, Om, ecc, w, M, n
     * 
     * @param vals
     * @return
     */
    public static double[] getCOEFromEquinoctal(double vals[])
    {
    	double ke = vals[0];
    	double he = vals[1];
    	double L = vals[2];
    	double pe = vals[3];
    	double qe = vals[4];
    	double n = vals[5];
    	
    	double i = 0;
    	double ecc = 0;
    	double Om = 0;
    	double w = 0;
    	double M = 0;
    	
    	ecc = Math.sqrt(he*he+ke*ke);
    	i = 2.0d*Math.atan(Math.sqrt(pe*pe+qe*qe));
    	Om = Math.atan2(pe,qe);
    	w = Math.atan2(he,ke)-Om;
    	M = L - Om - w;
    	
    	i = Math.toDegrees(i);
    	Om = Math.toDegrees(Om);
    	w = Math.toDegrees(w);
    	M = Math.toDegrees(M);
    	
    	if(i<0)i+=180;
    	if(Om<0)Om+=360;
    	if(w<0)w+=360;
    	if(M<0)M+=360;
    	
    	if(Om>360)Om-=360;
    	if(w>360)w-=360;
    	if(M>360)M-=360;
    	return new double[]{i,Om,ecc,w,M,n};
    }
    
    public static CartesianState kepToCart(KeplerianState kep)
    {
    	return kepToCart(kep,GM_Earth_km3);
    }
    
    public static CartesianState kepToCart(KeplerianState kep, double mu)
    {
    	CartesianState cart = new CartesianState();
        double temp, sinnu, cosnu, small;
        double[] rpqw = new double[3];
        double[] vpqw = new double[3];
        double[] tempvec = new double[3];

        small = 0.0000001;

        double p = kep.pKM;
        double ecc = kep.ecc;
        double argp = Math.toRadians(kep.argpDeg);
        double omega = Math.toRadians(kep.omegaDeg);
        double nu = Math.toRadians(kep.taDeg);
        double incl = Math.toRadians(kep.incDeg);
        double truelon = Math.toRadians(kep.truelonDeg);
        double arglat = Math.toRadians(kep.arglatDeg);
        double lonper = Math.toRadians(kep.lonperDeg);
        
        // --------------------  implementation   ----------------------
        //       determine what type of orbit is involved and set up the
        //       set up angles for the special cases.
        // -------------------------------------------------------------
        if (ecc < small)
        {
            // ----------------  circular equatorial  ------------------
            if ((incl < small) | (Math.abs(incl - Math.PI) < small))
            {
                argp = 0.0;
                omega = 0.0;
                nu = truelon;
            }
            else
            {
                // --------------  circular inclined  ------------------
                argp = 0.0;
                nu = arglat;
            }
        }
        else
        {
            // ---------------  elliptical equatorial  -----------------
            if ((incl < small) | (Math.abs(incl - Math.PI) < small))
            {
                argp = lonper;
                omega = 0.0;
            }
        }

        // ----------  form pqw position and velocity vectors ----------
        cosnu = Math.cos(nu);
        sinnu = Math.sin(nu);
        temp = p / (1.0 + ecc * cosnu);
        rpqw[0] = temp * cosnu;
        rpqw[1] = temp * sinnu;
        rpqw[2] = 0.0;
        if (Math.abs(p) < 0.00000001)
            p = 0.00000001;

        vpqw[0] = -sinnu * Math.sqrt(mu / p);
        vpqw[1] = (ecc + cosnu) * Math.sqrt(mu / p);
        vpqw[2] = 0.0;
		
		// ----------------  perform transformation to ijk  ------------
		tempvec = rot3(rpqw, -argp);
		tempvec = rot1(tempvec, -incl);
		double []r = rot3(tempvec, -omega);
		
		tempvec = rot3(vpqw, -argp);
		tempvec = rot1(tempvec, -incl);
		double []v = rot3(tempvec, -omega);
		
		cart.setRVec(r);
		cart.setVVec(v);
		cart.epoch = kep.epoch;
    	return cart;
    }
    
    /**
     * Simple J2 propagator
     * 
     * @param kep
     * @param d
     * @return
     */
    public static CartesianState getCartJ2(KeplerianState kep, Date d)
    {
    	KeplerianState k2 = kep.copy();
    	
    	double dt = (d.getTime()-kep.epoch.getTime())/1000.0d;
    	
    	double mu = GM_Earth_km3;
    	
    	//System.out.println(dt);
    	// update omega, argp, m based on j2
    	
    	double n = Math.sqrt(mu/(kep.smaKM*kep.smaKM*kep.smaKM));
    	
    	// RE what about other MUs
    	double j2op2 = n*1.5*RE*RE*J2/(kep.pKM*kep.pKM);
    	double dRaan = -j2op2*Math.cos(Math.toRadians(kep.incDeg));
    	
    	double tmp = Math.sin(Math.toRadians(kep.incDeg));
    	double dArgp = j2op2*(2.0 - 2.5*tmp*tmp);
    	
    	double omega = Math.toRadians(kep.omegaDeg);
    	double argp = Math.toRadians(kep.argpDeg);
    	double m = Math.toRadians(kep.maDeg);
    	
    	omega += dt*dRaan;
    	omega = mod2pi(omega);
    	argp += dt*dArgp;
    	argp = mod2pi(argp);
    	m += dt*n;
    	m = mod2pi(m);
    	
    	// update derived angles arglat, truelon, and nu
    	double lonper=mod2pi(omega+argp);
		
		double EA = MA2EA(k2.ecc, m);
		double nu = EA2TA(k2.ecc, EA);
		double arglat=mod2pi(nu+argp);
		double truelon = nu+argp+omega;
		
		k2.omegaDeg = Math.toDegrees(omega);
		k2.argpDeg = Math.toDegrees(argp);
		k2.maDeg = Math.toDegrees(m);
		k2.lonperDeg = Math.toDegrees(lonper);
		k2.eaDeg = Math.toDegrees(EA);
		k2.taDeg = Math.toDegrees(nu);
		k2.arglatDeg = Math.toDegrees(arglat);
		k2.truelonDeg = Math.toDegrees(truelon);
		
		/*
		System.out.println("dt\t"+dt);
		System.out.println("n\t"+n);
		System.out.println("omega\t"+kep.omega + "\t" + k2.omega);
		System.out.println("argp\t"+kep.argp + "\t" + k2.argp);
		System.out.println("m\t"+kep.m + "\t" + k2.m);
		System.out.println("nu\t"+kep.nu + "\t" + k2.nu);
		System.out.println("nu\t"+MA2EA(kep.ecc,kep.m) + "\t" + EA);
		*/
		k2.epoch = new Date(d.getTime());
    	return kepToCart(k2, mu);
    }

    public static KeplerianState cartToKep(CartesianState cart)
    {
    	return cartToKep(cart,null,GM_Earth_km3);
    }
    
    public static KeplerianState cartToKep(CartesianState cart, double mu)
    {
    	if(cart == null) return null;
    	
    	return cartToKep(cart,null,mu);
    }
    
    /**
     * Use the provided keplerianstate.
     * 
     * @param cart
     * @param kep
     * @param mu
     * @return
     */
    public static KeplerianState cartToKep(CartesianState cart, KeplerianState kep, double mu)
    {
    	if(cart == null) return null;
    	
    	if(kep == null) kep = new KeplerianState();
    	
    	kep.epoch = cart.epoch;
    	
      	double p=0;
        double a=0;
        double ecc=0;
        double incl=0;
        double omega=0;
        double argp=0;
        double nu=0;
        double m=0;
        double arglat=0;
        double truelon=0;
        double lonper=0;
        
    	double r[] = cart.getRVec();
    	double v[] = cart.getVVec();
    	
        double undefined, small, magr, magv, magn, sme,
        rdotv, infinite, temp, c1, hk, twopi, magh, halfpi;
		 double[] hbar = new double[3];
		 double[] ebar = new double[3];
		 double[] nbar = new double[3];
		 int i;
		 String typeorbit;
		
		 twopi = 2.0 * Math.PI;
		 halfpi = 0.5 * Math.PI;
		 small = 0.00000001;
		 undefined = 999999.1;
		 infinite = 999999.9;
		 m = 0.0;
		
		 // -------------------------  implementation   -----------------
		 magr = mag(r);
		 magv = mag(v);
		
		 // ------------------  find h n and e vectors   ----------------
		 hbar = cross(r, v);
		 magh = mag(hbar);
		 if (magh > small)
		 {
		     nbar[0] = -hbar[1];
		     nbar[1] = hbar[0];
		     nbar[2] = 0.0;
		     magn = mag(nbar);
		     c1 = magv * magv - mu / magr;
		     rdotv = dot(r, v);
		     for (i = 0; i <= 2; i++)
		     {
		         ebar[i] = (c1 * r[i] - rdotv * v[i]) / mu;
		     }
		     
		     ecc = mag(ebar);
		     
		     // ------------  find a e and semi-latus rectum   ----------
		     sme = (magv * magv * 0.5) - (mu / magr);
		     if (Math.abs(sme) > small)
		     {
		         a = -mu / (2.0 * sme);
		     }
		     else
		     {
		         a = infinite;
		     }
		     p = magh * magh / mu;
		
		     // -----------------  find inclination   -------------------
		     hk = hbar[2] / magh;
		     incl = Math.acos(hk);
		
		     // --------  determine type of orbit for later use  --------
		     // ------ elliptical, parabolic, hyperbolic inclined -------
		     typeorbit = "ei";
		     if (ecc < small)
		     {
		         // ----------------  circular equatorial ---------------
		         if ((incl < small) | (Math.abs(incl - Math.PI) < small))
		         {
		        	 typeorbit = "ce";
		         }
		         else
		         {
		        	 // --------------  circular inclined ---------------
		             typeorbit = "ci";
		         }
		     }
		     else
		     {
		         // - elliptical, parabolic, hyperbolic equatorial --
		         if ((incl < small) | (Math.abs(incl - Math.PI) < small))
		         {
		        	 typeorbit = "ee";
		         }
		     }
		
		     // ----------  find longitude of ascending node ------------
			if (magn > small)
			{
			    temp = nbar[0] / magn;
			    if (Math.abs(temp) > 1.0)
			    {
			    	temp = Math.signum(temp);
			    }
			    omega = Math.acos(temp);
			    if (nbar[1] < 0.0)
			    {
			    	omega = twopi - omega;
			    }
			}
			else
			{
			    omega = undefined;
			}
			
			// ---------------- find argument of perigee ---------------
			if (typeorbit.equals("ei"))
			{
			    argp = angle(nbar, ebar);
			    if (ebar[2] < 0.0)
			    {
			    	argp = twopi - argp;
			    }
			}
			else
			{
			    argp = undefined;
			}
			
			// ------------  find true anomaly at epoch    -------------
			if (typeorbit.charAt(0) == 'e')
			{
			    nu = angle(ebar, r);
			    if (rdotv < 0.0)
			    {
			    	nu = twopi - nu;
			    }
			}
			else
			{
			    nu = undefined;
			}
			
			// ----  find argument of latitude - circular inclined -----
			if (typeorbit.equals("ci"))
			{
			    arglat = angle(nbar, r);
			    if (r[2] < 0.0)
			    {
			    	arglat = twopi - arglat;
			    }
			    m = arglat;
			}
			else
		    {
				arglat = undefined;
		    }
			
			// -- find longitude of perigee - elliptical equatorial ----
			if ((ecc > small) && (typeorbit.equals("ee")))
			{
			    temp = ebar[0] / ecc;
			    if (Math.abs(temp) > 1.0)
			    {
			    	temp = Math.signum(temp);
			    }
			    lonper = Math.acos(temp);
			    if (ebar[1] < 0.0)
			    {
			    	lonper = twopi - lonper;
			    }
			    if (incl > halfpi)
			    {
			    	lonper = twopi - lonper;
			    }
			}
			else
			{			
			    lonper = undefined;
			}
			
			// -------- find true longitude - circular equatorial ------
			if ((magr > small) && (typeorbit.equals("ce")))
			{
			    temp = r[0] / magr;
			    if (Math.abs(temp) > 1.0)
			    {
			    	temp = Math.signum(temp);
			    }
			    truelon = Math.acos(temp);
			    if (r[1] < 0.0)
			    {
			    	truelon = twopi - truelon;
			    }
			    if (incl > halfpi)
			    {
			    	truelon = twopi - truelon;
			    }
			    m = truelon;
			}
			else
			{
				truelon = undefined;
			}
			// ------------ find mean anomaly for all orbits -----------
			if (typeorbit.charAt(0) == 'e')
			{
				double tv[] = newtonnu(ecc, nu);
				//double ea = tv[0];
				m = tv[1];
			}
		}
		else
		{
			p = undefined;
			a = undefined;
			ecc = undefined;
			incl = undefined;
			omega = undefined;
			argp = undefined;
			nu = undefined;
			m = undefined;
			arglat = undefined;
			truelon = undefined;
			lonper = undefined;
		}

		kep.pKM = p;
		kep.smaKM = a;
		kep.ecc = ecc;
		kep.incDeg = Math.toDegrees(incl);
		kep.omegaDeg = Math.toDegrees(omega);
		kep.argpDeg = Math.toDegrees(argp);
		kep.taDeg = Math.toDegrees(nu);
		kep.maDeg = Math.toDegrees(m);
		kep.arglatDeg = Math.toDegrees(arglat);
		kep.truelonDeg = Math.toDegrees(truelon);
		kep.lonperDeg = Math.toDegrees(lonper);
		double n = Math.sqrt(mu/(kep.smaKM*kep.smaKM*kep.smaKM));
    	kep.periodSec = twopi/n;
    	kep.meanMotion = 86400.0d*n/twopi;
    	
    	return kep;
    }
    
    public static double[] rot1(double[] vec, double xval)
	{
	    double c, s, temp;
	    double[] outvec = new double[3];
	
	    temp = vec[2];
	    c = Math.cos(xval);
	    s = Math.sin(xval);
	
	    outvec[2] = c * vec[2] - s * vec[1];
	    outvec[1] = c * vec[1] + s * temp;
	    outvec[0] = vec[0];
	
	    return outvec;
	}  // rot1  

    public static double[] rot2(double[] vec,double xval)
	{
	    double c, s, temp;
	    double[] outvec = new double[3];
	
	    temp = vec[2];
	    c = Math.cos(xval);
	    s = Math.sin(xval);
	
	    outvec[2] = c * vec[2] + s * vec[0];
	    outvec[0] = c * vec[0] - s * temp;
	    outvec[1] = vec[1];
	
	    return outvec;
	}   // rot2  

    public static double[] rot3(double[] vec,double xval)
	{
	    double c, s, temp;
	    double[] outvec = new double[3];
	
	    temp = vec[1];
	    c = Math.cos(xval);
	    s = Math.sin(xval);
	
	    outvec[1] = c * vec[1] - s * vec[0];
	    outvec[0] = c * vec[0] + s * temp;
	    outvec[2] = vec[2];
	
	    return outvec;
	}  // rot3 

    
    public static double mag(double v[])
    {
    	double mag = 0;
    	if(v == null) return mag;
    	
    	if(v.length == 3)
    	{
    		mag = v[0]*v[0]+v[1]*v[1]+v[2]*v[2];
    	}
    	else
    	{
    		int len = v.length;
    		for(int i=0; i<len; i++)
    		{
    			mag += (v[i]*v[i]);
    		}
    	}
    	
    	mag = Math.sqrt(mag);
    	
    	return mag;
    }
    
    public static double dot(double v1[], double v2[])
    {
    	double dot = 0;
    	if(v1.length == 3 && v2.length == 3)
    	{
    		dot = v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
    	}
    	else
    	{
    		int len = Math.min(v1.length, v2.length);
    		for(int i=0; i<len; i++)
    		{
    			dot += v1[i]*v2[i];
    		}
    	}
    	return dot;
    }
    
    public static double[] cross(double[] vec1, double[] vec2)
	{
	    double[] tempvec = new double[3];
	    tempvec[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
	    tempvec[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
	    tempvec[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
	    return tempvec;
	}  //  cross

    public static double angle(double[] vec1,double[] vec2)
	{
	    double small, undefined, magv1, magv2, temp;
	    small = 0.00000001;
	    undefined = 999999.1;
	
	    magv1 = mag(vec1);
	    magv2 = mag(vec2);
	
	    if (magv1 * magv2 > small * small)
	    {
	        temp = dot(vec1, vec2) / (magv1 * magv2);
	        if (Math.abs(temp) > 1.0)
	            temp = Math.signum(temp) * 1.0;
	        return Math.acos(temp);
	    }
	    else
	        return undefined;
	}  //  angle

    public static double[] newtonnu(double ecc, double nu)
	{
	    double small, sine, cose;
	    double e0, m;
	    
	    // ---------------------  implementation   ---------------------
	    e0 = 999999.9;
	    m = 999999.9;
	    small = 0.00000001;
	
	    // --------------------------- circular ------------------------
	    if (Math.abs(ecc) < small)
	    {
	        m = nu;
	        e0 = nu;
	    }
	    else
	    {
	        // ---------------------- elliptical -----------------------
	        if (ecc < 1.0 - small)
	        {
	            sine = (Math.sqrt(1.0 - ecc * ecc) * Math.sin(nu)) / (1.0 + ecc * Math.cos(nu));
	            cose = (ecc + Math.cos(nu)) / (1.0 + ecc * Math.cos(nu));
	            e0 = Math.atan2(sine, cose);
	            m = e0 - ecc * Math.sin(e0);
	        }
	        else
	        {
	            // -------------------- hyperbolic  --------------------
	            if (ecc > 1.0 + small)
	            {
	                if ((ecc > 1.0) && (Math.abs(nu) + 0.00001 < Math.PI - Math.acos(1.0 / ecc)))
	                {
	                    sine = (Math.sqrt(ecc * ecc - 1.0) * Math.sin(nu)) / (1.0 + ecc * Math.cos(nu));
	                    e0 = asinh(sine);
	                    m = ecc * Math.sinh(e0) - e0;
	                }
	            }
	            else
	            {
	                // ----------------- parabolic ---------------------
	                if (Math.abs(nu) < 168.0 * Math.PI / 180.0)
	                {
	                    e0 = Math.tan(nu * 0.5);
	                    m = e0 + (e0 * e0 * e0) / 3.0;
	                }
	            }
	        }
	    }
	    if (ecc < 1.0)
	    {
	        m = m - Math.floor(m / (2.0 * Math.PI)) * (2.0 * Math.PI);
	        if (m < 0.0)
	        {
	            m = m + 2.0 * Math.PI;
	        }
	        e0 = e0 - Math.floor(e0 / (2.0 * Math.PI)) * (2.0 * Math.PI);
	    }
	    
	    return new double[]{e0,m};
	} // newtonnu}
    
    public static double asinh(double xval)
    {
    	return Math.log(xval + Math.sqrt(xval * xval + 1.0));
    }  //  asinh

    
    public static double[] ijk2ll(double[] r)
    {
    	double latgc = 0;
    	double latgd =0;
    	double lon =0;
    	double hellp = 0;
    	
	    double twopi = 2.0 * Math.PI;
	    double small = 0.00000001;         // small value for tolerances
	    //double eesqrd = 0.006694385000;     // eccentricity of earth sqrd
	
	    int i;
	    double temp, decl, rtasc, olddelta, magr, sintemp, c, s;
	
	    c = 0.0;
	
	    // -------------------------  implementation   -----------------
	    magr = mag(r);
	
	    // ----------------- find longitude value  ---------------------
	    temp = Math.sqrt(r[0] * r[0] + r[1] * r[1]);
	    if (Math.abs(temp) < small)
	        rtasc = Math.signum(r[2]) * Math.PI * 0.5;
	    else
	        rtasc = Math.atan2(r[1], r[0]);
	
	    lon = rtasc;
	    if (Math.abs(lon) >= Math.PI)   // mod it ?
	        if (lon < 0.0)
	            lon = twopi + lon;
	        else
	            lon = lon - twopi;
	
	    decl = Math.asin(r[2] / magr);
	    latgd = decl;
	
	    // ------------- iterate to find geodetic latitude -------------
		i = 1;
		olddelta = latgd + 10.0;
		
		while ((Math.abs(olddelta - latgd) >= small) && (i < 10))
		{
		    olddelta = latgd;
		    sintemp = Math.sin(latgd);
		    c = RE / (Math.sqrt(1.0 - eesqrd * sintemp * sintemp));
		    latgd = Math.atan((r[2] + c * eesqrd * sintemp) / temp);
		    i = i + 1;
		}
		
		// Calculate height
		if (Math.PI * 0.5 - Math.abs(latgd) > Math.PI / 180.0)  // 1 deg
		    hellp = (temp / Math.cos(latgd)) - c;
		else
		{
		    s = c * (1.0 - eesqrd);
		    hellp = r[2] / Math.sin(latgd) - s;
		}
		
		latgc = gd2gc(latgd);
		
    	return new double[]{latgc,latgd,lon,hellp};

	} //  ijk2ll

    
    public static double gd2gc(double latgd)
	{
	
	    // -------------------------  implementation   -----------------
	    return Math.atan((1.0 - eesqrd) * Math.tan(latgd));	
	}  //  gd2gc

    
    public static void site(double latgd, double lon, double altKM, double rsecef[], double vsecef[])
    {
    	
	    double sinlat, cearth, rdel, rk;
	
	    // needed since assignments aren't at root level in procedure
	    if(rsecef == null)
	    rsecef = new double[] { 0.0, 0.0, 0.0 };
	    if(vsecef == null)
	    vsecef = new double[] { 0.0, 0.0, 0.0 };
	
	    /* ---------------------  initialize values   ------------------- */
	    sinlat = Math.sin(latgd);
	
	    /* -------  find rdel and rk components of site vector  --------- */
	    cearth = RE / Math.sqrt(1.0 - (eesqrd * sinlat * sinlat));
	    rdel = (cearth + altKM) * Math.cos(latgd);
	    rk = ((1.0 - eesqrd) * cearth + altKM) * sinlat;
	
	    /* ----------------  find site position vector  ----------------- */
	    rsecef[0] = rdel * Math.cos(lon);
	    rsecef[1] = rdel * Math.sin(lon);
	    rsecef[2] = rk;
	
	    /* ----------------  find site velocity vector  ----------------- */
	    vsecef[0] = 0.0;
	    vsecef[1] = 0.0;
	    vsecef[2] = 0.0;
    }
    
    // -----------------------------------------------------------------------------
    public static double[] matvecmult(double[][] mat, double[] vec)
    {
        int row, ktr;
        double[] vecout = new double[3];
        for (row = 0; row < 3; row++)
        {
            vecout[row] = 0.0;
            for (ktr = 0; ktr < 3; ktr++)
                vecout[row] = vecout[row] + mat[row][ ktr] * vec[ktr];
        }
        return vecout;
    } // matvecmult  


    // -----------------------------------------------------------------------------
    public static double[][] matmult(double[][] mat1, double[][] mat2, int mat1r, int mat1c, int mat2c)
    {
        int row, col, ktr;
        double[][] mat3 = new double[3][3];
        for (row = 0; row < mat1r; row++)
        {
            for (col = 0; col < mat2c; col++)
            {
                mat3[row][col] = 0.0;
                for (ktr = 0; ktr < mat1c; ktr++)
                    mat3[row][col] = mat3[row][ col] + mat1[row][ ktr] * mat2[ktr][col];
            }
        }
        return mat3;
    } // matmult  


    // -----------------------------------------------------------------------------
    // form the transponse of a 3x3 matrix
    public static double[][] mattrans(double[][] mat1)
    {
        int row, col;
        double[][] mat2 = new double[3][3];
        for (row = 0; row < 3; row++)
        {
            for (col = 0; col < 3; col++)
            {
                mat2[row][col] = mat1[col][row];
            }
        }
        return mat2;
    }  // mattrans 


    // -----------------------------------------------------------------------------
    public static double[] norm(double[] vec1)
    {
        double[] norm = new double[3];
        double magr;

        magr = mag(vec1);
        norm[0] = vec1[0] / magr;
        norm[1] = vec1[1] / magr;
        norm[2] = vec1[2] / magr;
        return norm;
    }  // norm 
    
    public static double date2mjd(Date d)
    {
    	long diff = d.getTime()-MJD_BASE_MS;
    	double dd = diff/86400.0d;
    	dd /= 1000.0d;
    	return dd;
    }
    
    public static Date mjd2date(double mjd)
    {
    	double dd = mjd*1000.0d*86400.0d;
    	long diff = (long)dd;
    	diff += MJD_BASE_MS;
    	return new java.sql.Timestamp(diff);
    }
    
    public static double jday(Date d)
    {
    	double t = d.getTime()/1000.0d;
    	t/=86400.0d;
    	
    	t+=JD_19700101;
    	
    	return t;
    }
    
    public static double[] jday(int year, int mon, int day, int hr, int minute, double sec)    
	{
	        double jd=0;
	        double jdFrac=0;
	
	    jd = 367.0 * year -
	         Math.floor((7 * (year + Math.floor((mon + 9) / 12.0))) * 0.25) +
	         Math.floor(275 * mon / 9.0) +
	         day + 1721013.5;  // use - 678987.0 to go to mjd directly
	    jdFrac = (sec + minute * 60.0 + hr * 3600.0) / 86400.0;
	
	    // check that the day and fractional day are correct
	    if (Math.abs(jdFrac) >= 1.0)
	    {
	        double dtt = Math.floor(jdFrac);
	        jd = jd + dtt;
	        jdFrac = jdFrac - dtt;
	    }
	
	    // - 0.5*Math.Sign(100.0*year + mon - 190002.5) + 0.5;
	    
	    return new double[]{jd,jdFrac};
	}  //  jday
    
    public static double gstime(Date d)
    {
    	double jd = jday(d);
    	return gstime(jd);
    }
    /* -----------------------------------------------------------------------------
    *
    *                           function gstime
    *
    *  this function finds the greenwich sidereal time (iau-82).
    *
    *  author        : david vallado                  719-573-2600    1 mar 2001
    *
    *  revisions
    *    vallado     - conversion to c#                              16 Nov 2011
    *    
    *  inputs          description                    range / units
    *    jdut1       - julian date in ut1             days from 4713 bc
    *
    *  outputs       :
    *    gstime      - greenwich sidereal time        0 to 2pi rad
    *
    *  locals        :
    *    temp        - temporary variable for doubles   rad
    *    tut1        - julian centuries from the
    *                  jan 1, 2000 12 h epoch (ut1)
    *
    *  coupling      :
    *    none
    *
    *  references    :
    *    vallado       2013, 188, eq 3-47
    * --------------------------------------------------------------------------- */
    public static double gstime( double jdut1)
    {
	    double twopi = 2.0 * Math.PI;
	    double deg2rad = Math.PI / 180.0;
	    double temp, tut1;
	
	    tut1 = (jdut1 - 2451545.0) / 36525.0;
	    temp = -6.2e-6 * tut1 * tut1 * tut1 + 0.093104 * tut1 * tut1 +
	            (876600.0 * 3600 + 8640184.812866) * tut1 + 67310.54841;  // sec
	    temp = (temp * deg2rad / 240.0 % twopi); //360/86400 = 1/240, to deg, to rad
	
	    // ------------------------ check quadrants ---------------------
	    if (temp < 0.0)
	        temp += twopi;
	
	    return temp;
    }  // gstime 


/* -----------------------------------------------------------------------------
*                           procedure lstime
*
*  this procedure finds the local sidereal time at a given location.
*
*  author        : david vallado                  719-573-2600    1 mar 2001
*
*  inputs          description                    range / units
*    lon         - site longitude (west -)        -2pi to 2pi rad
*    jdut1       - julian date in ut1             days from 4713 bc
*
*  outputs       :
*    lst         - local sidereal time            0.0 to 2pi rad
*    gst         - greenwich sidereal time        0.0 to 2pi rad
*
*  locals        :
*    none.
*
*  coupling      :
*    gstime        finds the greenwich sidereal time
*
*  references    :
*    vallado       2013, 188, eq 3-47, Alg 15
* --------------------------------------------------------------------------- */

    public static double[] lstime(double lon, double jdut1)
	{
	    double twopi = 2.0 * Math.PI;
	    double lst = 0;
	    double gst = 0;
	    
	    gst = gstime(jdut1);
	    lst = lon + gst;
	
	    /* ------------------------ check quadrants --------------------- */
	    lst = (lst % twopi);
	    if (lst < 0.0)
	        lst = lst + twopi;
	    
	    return new double[]{lst,gst};
	}  // lstime

    public static double[] addvec
    (
    double a1, double[] vec1,
    double a2, double[] vec2
    )
    {
	    double[] vec3 = new double[] { 0.0, 0.0, 0.0 };
	    int row;
	    //double[] tempvec = new double[3];
	
	    for (row = 0; row <= 2; row++)
	    {
	        vec3[row] = 0.0;
	        vec3[row] = a1 * vec1[row] + a2 * vec2[row];
	    }
	    
	    return vec3;
    } // addvec

    /**
     * return ra and dec in radians.
     * 
     * @param cs
     * @return
     */
    public static double[] toRADec(CartesianState cs)
    {
    	double rv[] = cs.getRVec();
    	return toRADec(rv[0],rv[1],rv[2]);
    }
    
    /**
     * return ra and dec in radians.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double[] toRADec(double x, double y, double z)
    {
    	double out[] = new double[2];
    	fillRADec(x,y,z,out);
    	return out;
    }
    
    public static void fillRADec(double x, double y, double z, double out[])
    {
    	double r = Math.sqrt(x*x+y*y+z*z);
    	// invert once
    	r = 1.0d/r;
    	
    	x*=r;
    	y*=r;
    	z*=r;    	
    	
    	double dec = Math.asin(z);
    	out[1]=dec;
    	
    	dec = Math.cos(dec);
    	x/=dec;
    	y/=dec;
    	
    	double ra = Math.atan2(y, x);
    	out[0]=ra;    	
    }
    
    public static double[] radec2azel(double raDeg, double decDeg, double latgcDeg, double lonDeg, Date date)
    {
    	double lst = lstime(Math.toRadians(lonDeg),jday(date))[0];
    	
    	return radec2azel(raDeg,decDeg,latgcDeg,lst);
    }

    public static double[] radec2azel(double raDeg, double decDeg, double latgcDeg, double lstRad)
    {
    	double lha = lstRad-Math.toRadians(raDeg);
    	double sinlat = Math.sin(Math.toRadians(latgcDeg));
    	double coslat = Math.cos(Math.toRadians(latgcDeg));
    	double sindec = Math.sin(Math.toRadians(decDeg));
    	double cosdec = Math.cos(Math.toRadians(decDeg));
    	
    	double coslha = Math.cos(lha);
    	double sinlha = Math.sin(lha);
    	
    	double sinel = sinlat*sindec+coslat*cosdec*coslha;
    	
    	double el = Math.asin(sinel);
    	double cosel = Math.cos(el);
    	
    	double sinaz = -sinlha*cosdec/cosel;
    	double cosaz = (sindec-sinel*sinlat)/(cosel*coslat);
    	
    	double az = Math.atan2(sinaz, cosaz);
    	
    	return new double[]{Math.toDegrees(az),Math.toDegrees(el)};
    }
    
    public static double[] azel2radec(double azDeg, double elDeg, double latgcDeg, double lstRad)
    {
    	double sinel = Math.sin(Math.toRadians(elDeg));
    	double cosel = Math.cos(Math.toRadians(elDeg));
    	double sinaz = Math.sin(Math.toRadians(azDeg));
    	double cosaz = Math.cos(Math.toRadians(azDeg));
    	
    	double sinlat = Math.sin(Math.toRadians(latgcDeg));
    	double coslat = Math.cos(Math.toRadians(latgcDeg));
    	
    	double sindec = sinel*sinlat+cosel*coslat*cosaz;
    	double dec = Math.asin(sindec);
    	double cosdec = Math.cos(dec);
    	
    	double sinlha = -sinaz*cosel*coslat/(cosdec*coslat);
    	double coslha = (sinel-sinlat*sindec)/(cosdec*coslat);
    	
    	double lha = Math.atan2(sinlha, coslha);
    	double ra = lstRad-lha;
    	
    	return new double[]{Math.toDegrees(ra),Math.toDegrees(dec)};
    }


    /*------------------------------------------------------------------------------
    *
    *                           procedure rv_razel
    *
    *  this procedure converts range, azimuth, and elevation and their rates with
    *    the geocentric equatorial (ecef) position and velocity vectors.  notice the
    *    value of small as it can affect rate term calculations. uses velocity
    *    vector to find the solution of Math.Singular cases.
    *
    *  author        : david vallado                  719-573-2600   22 jun 2002
    *
    *  inputs          description                    range / units
    *    recef       - ecef position vector           km
    *    vecef       - ecef velocity vector           km/s
    *    rsecef      - ecef site position vector      km
    *    latgd       - geodetic latitude              -Math.PI/2 to Math.PI/2 rad
    *    lon         - geodetic longitude             -2pi to Math.PI rad
    *    direct      -  direction to convert          eFrom  eTo
    *
    *  outputs       :
    *    rho         - satellite range from site      km
    *    az          - azimuth                        0.0 to 2pi rad
    *    el          - elevation                      -Math.PI/2 to Math.PI/2 rad
    *    drho        - range rate                     km/s
    *    daz         - azimuth rate                   rad/s
    *    del         - elevation rate                 rad/s
    *
    *  locals        :
    *    rhovecef    - ecef range vector from site    km
    *    drhovecef   - ecef velocity vector from site km/s
    *    rhosez      - sez range vector from site     km
    *    drhosez     - sez velocity vector from site  km
    *    tempvec     - temporary vector
    *    temp        - temporary extended value
    *    temp1       - temporary extended value
    *    i           - index
    *
    *  coupling      :
    *    mag         - magnitude of a vector
    *    addvec      - add two vectors
    *    rot3        - rotation about the 3rd axis
    *    rot2        - rotation about the 2nd axis
    *    Math.Atan2       - arc tangent function which also resloves quadrants
    *    dot         - dot product of two vectors
    *    rvsez_razel - find r and v from site in topocentric horizon (sez) system
    *    lncom2      - combine two vectors and constants
    *    arcsin      - arc Math.Sine function
    *    Math.Sign         - returns the sign of a variable
    *
    *  references    :
    *    vallado       2013, 265, alg 27
    -----------------------------------------------------------------------------*/

    public static double[]rv2razel(double[] recef, double[] vecef, double[] rsecef, double latgd, double lon)
    { 
        double halfpi = Math.PI / 2.0;
        double small = 0.0000001;

        double temp, temp1;
        double[] rhoecef = new double[3];
        double[] drhoecef = new double[3];
        double[] rhosez = new double[3];
        double[] drhosez = new double[3];
        double[] tempvec = new double[3];

        double az = 0;
        double el = 0;
        double rho = 0;
        double daz =0;
        double del = 0;
        double drho = 0;
        
        /* ------- find ecef range vector from site to satellite ----- */
        rhoecef = addvec(1.0, recef, -1.0, rsecef);
        drhoecef[0] = vecef[0];
        drhoecef[1] = vecef[1];
        drhoecef[2] = vecef[2];
        rho = mag(rhoecef);

        /* ------------ convert to sez for calculations ------------- */
        tempvec = rot3(rhoecef, lon);
        rhosez = rot2(tempvec, halfpi - latgd);
        tempvec = rot3(drhoecef, lon);
        drhosez = rot2(tempvec, halfpi - latgd);

        /* ------------ calculate azimuth and elevation ------------- */
        temp = Math.sqrt(rhosez[0] * rhosez[0] + rhosez[1] * rhosez[1]);
        if (Math.abs(rhosez[1]) < small)
        {
        	if (temp < small)
            {
                temp1 = Math.sqrt(drhosez[0] * drhosez[0] +
                    drhosez[1] * drhosez[1]);
                az = Math.atan2(drhosez[1] / temp1, -drhosez[0] / temp1);
            }
            else
            {   
            	if (rhosez[0] > 0.0)
            	{    az = Math.PI; }
                else
                { az = 0.0;}
            }
        }
        else
            az = Math.atan2(rhosez[1] / temp, -rhosez[0] / temp);

        if (temp < small)  // directly over the north pole
            el = Math.signum(rhosez[2]) * halfpi; // +- 90
        else
            el = Math.asin(rhosez[2] / mag(rhosez));

        /* ----- calculate range, azimuth and elevation rates ------- */
        drho = dot(rhosez, drhosez) / rho;
        if (Math.abs(temp * temp) > small)
            daz = (drhosez[0] * rhosez[1] - drhosez[1] * rhosez[0]) /
            (temp * temp);
        else
            daz = 0.0;

        if (Math.abs(temp) > 0.00000001)
            del = (drhosez[2] - drho * Math.sin(el)) / temp;
        else
            del = 0.0;
        
        return new double[]{az,el,rho,daz,del,drho};
    }  //  rv_razel


    public static List<CartesianState> getGEOCarts(double degStep)
    {
    	DateUtil.setDefaultTimeZone();
    	
    	double r = GEO_RADIUS;
    	int size = (int)(360.0d/degStep)+1;
    	List<CartesianState> carts = new ArrayList<CartesianState>(size);
    	
    	Date d = new Date();
    	double ang = 0.0d;
    	double dAng = Math.toRadians(degStep);
    	
    	double cosa = 0;
    	double sina = 0;
    	CartesianState cart = null;
    	
    	for(int i=0; i<size; i++)
    	{
    		cosa = Math.cos(ang);
    		sina = Math.sin(ang);
    	
    		cart = new CartesianState();
    		cart.epoch = d;
    		cart.setRVec(r*cosa, r*sina, 0);
    		carts.add(cart);
    		ang+=dAng;
    	}
    	
    	return carts;
    }
    
    
    /**
     * Compute starting guess for eccentric anomaly
     * http://alpheratz.net/dynamics/twobody/KeplerIterations_summary.pdf
     * 
     * @param ecc
     * @param Mrad
     * @return
     */
    /*
    public static double kepStart(double ecc, double Mrad)
    {
    	Mrad = mod2pi(Mrad);
    	
    	double t34=ecc*ecc;
    	double t35 = ecc*t34;
    	double t33 = Math.cos(Mrad);
    	
    	double E = t34+1.5*t33*t35;
    	E = E*t33+ecc-0.5*t35;
    	E = E*Math.sin(Mrad);
    	return E;
    }
    */
    /**
     * Iterative update to eccentric anomaly
     * http://alpheratz.net/dynamics/twobody/KeplerIterations_summary.pdf
     * 
     * @param ecc
     * @param Mrad
     * @param x
     * @return
     */
    /*
    public static double kepEps(double ecc, double Mrad, double x)
    {
    	double t1 = Math.cos(x);
    	double t2 = -1.0d+ecc*t1;
    	double t3 = Math.sin(x);
    	double t4 = ecc*t3;
    	double t5 = -x+t4+Mrad;
    	double t6 = 0.5*t5*t4/t2+t2;
    	t6 = t5/t6;
    	double E = t5/((0.5*t3-t1*t6/6.0d)*ecc*t6+t2);
    	return E;
    }
    */
    
    /*
    public static double MA2EA(double ecc, double Mrad)
    {
    	double tol = 1e-14;
    	Mrad = mod2pi(Mrad);
    	
    	double E0 = kepStart(ecc,Mrad);
    	double dE = tol+1;
    	double E = 0;
    	int count = 0;
    	while(Math.abs(dE) > tol)
    	{
    		dE=kepEps(ecc,Mrad,E0);
    		E += dE;
    		E0=E;
    		count++;
    		if(count > 100)
    		{
    			System.err.println("Failed to solve kepler's equation");
    			System.err.println(ecc + "\t"+Math.toDegrees(Mrad)+"\t"+Mrad +"\t"+E+"\t"+dE);
    			System.err.flush();
    			try {Thread.sleep(100);}catch(Exception ex) {};
    			break;
    		}
    		E = mod2pi(E);
    	}
    	return E;
    }
    */
    
    public static double MA2EA(double ecc, double Mrad)
    {
    	double tol = 1e-14;
    	Mrad = mod2pi(Mrad);
    	//double den = 1.0d-ecc;
    	//double den2 = den*den*den*den;
    	
    	//double E0 = Mrad/den-ecc/den2*Mrad*Mrad*Mrad/6.0d;
    	//E0=mod2pi(E0);
    	double E0=Mrad;
    	double dE = tol+1;
    	double E = 0;
    	int count = 0;
    	while(Math.abs(dE) > tol)
    	{
    		E = Mrad+ecc*Math.sin(E0);
    		dE = E-E0;
    		E = mod2pi(E);
    		E0=E;
    		count++;
    		if(count > 200) // give it a lot of iterations because we deal with some large eccentricity orbits
    		{
    			System.err.println("Failed to solve kepler's equation");
    			System.err.println(ecc + "\t"+Math.toDegrees(Mrad)+"\t"+Mrad +"\t"+E+"\t"+dE);
    			System.err.flush();
    			try {Thread.sleep(100);}catch(Exception ex) {};
    			break;
    		}
    	}
    	return E;
    }
    
    public static double mod2pi(double val)
    {
    	double tmp = Math.abs(val);
    	if(tmp>60)
    	{
    		int cnt = (int)(tmp/twopi);
    		tmp-=cnt*twopi;
    		if(val < 0) tmp*=-1.0d;
    		val = tmp;
    	}
    	else
    	{
    		while(val > twopi)val-=twopi;
    		while(val < 0)val+=twopi;
    	}
    	
    	return val;
    }

    public static double mod360(double val)
    {
    	double tmp = Math.abs(val);
    	if(tmp>3600)
    	{
    		int cnt = (int)(tmp/360.0d);
    		tmp-=cnt*360.0d;
    		if(val < 0) tmp*=-1.0d;
    		val = tmp;
    	}
    	else
    	{
    		while(val > 360.0d)val-=360.0d;
    		while(val < 0)val+=360.0d;
    	}
    	
    	return val;
    }

    public static double EA2TA(double ecc, double EArad)
    {
    	double ce = Math.cos(EArad);
    	double se = Math.sin(EArad);
    	
    	double b = 1.0d-ecc*ce;
    	
    	double sv = Math.sqrt(1.0d-ecc*ecc)*se;
    	double cv = ce-ecc;
    	
    	sv = sv/b;
    	cv = cv/b;
    	
    	double ta = Math.atan2(sv,cv);
    	ta = mod2pi(ta);
    	return ta;
    }
}
