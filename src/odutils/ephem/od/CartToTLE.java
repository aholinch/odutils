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

import odutils.ephem.CartesianState;
import odutils.ephem.EphemerisUtil;
import odutils.ephem.KeplerianState;
import odutils.ephem.MutableTLE;
import odutils.ephem.USSFSGP4;
import odutils.util.DateUtil;
import sgp4.TLE;

public class CartToTLE 
{
	/**
	 * Uses direct iteration to find the tle.
	 * 
	 * @param cart
	 * @return
	 */
    public static TLE cartToTLE(CartesianState cart, String objectID)
    {
    	return cartToTLE(cart,objectID,false);
    }
    
    public static TLE cartToTLE(CartesianState cart, String objectID, boolean useUSSFSGP4)
    {

    	TLE tleOut = null;
    	
    	KeplerianState kep = EphemerisUtil.cartToKep(cart);
    	KeplerianState kepNew = null;
    	KeplerianState kepOrig = kep.copy();
    	
    	MutableTLE mTle = new MutableTLE();
    	mTle.setObjectID(objectID);
    	double delt = 0;

    	// TODO implement a better convergence check
    	for(int i=0; i<30; i++)
    	{
	    	KeplerianState.setToTLE(kep, mTle);
	    	mTle.commit();
	    	//System.out.println(i+"\t"+mTle.getN());
	    	//System.out.println(mTle.getLine1());
	    	//System.out.println(mTle.getLine2());
	    	
	    	if(!useUSSFSGP4)
	    	{
	    		cart = mTle.getCart(0);
	    	}
	    	else
	    	{
	    		cart = USSFSGP4.getCart(0,mTle.getLine1(),mTle.getLine2());
	    	}
	    	
	    	kepNew = EphemerisUtil.cartToKep(cart);
	    	delt = Math.abs((kepOrig.meanMotion-kepNew.meanMotion)/kepOrig.meanMotion);
	    	if(delt < 1e-12) break;
	    	
	    	kep.meanMotion += kepOrig.meanMotion-kepNew.meanMotion;
	    	kep.incDeg += kepOrig.incDeg-kepNew.incDeg;
	    	kep.argpDeg += kepOrig.argpDeg-kepNew.argpDeg;
	    	kep.omegaDeg += kepOrig.omegaDeg-kepNew.omegaDeg;
	    	kep.ecc += kepOrig.ecc-kepNew.ecc;
	    	kep.maDeg += kepOrig.maDeg-kepNew.maDeg;
    	}
    	
    	tleOut = new TLE(mTle.getLine1(),mTle.getLine2());
   
    	return tleOut;
    }
	/**
	 * Uses direct iteration to find the tle.
	 * 
	 * @param cart
	 * @return
	 */
    public static TLE cartToXPTLE(CartesianState cart, String objectID)
    {
    	TLE tleOut = null;
    	
    	KeplerianState kep = EphemerisUtil.cartToKep(cart);
    	KeplerianState kepNew = null;
    	KeplerianState kepOrig = kep.copy();
    	CartesianState co = cart;
    	MutableTLE mTle = new MutableTLE();
    	mTle.setObjectID(objectID);
    	double delt = 0;
    	// TODO implement a better convergence check
    	for(int i=0; i<200; i++)
    	{
    		KeplerianState.setToTLE(kep, mTle);
	    	mTle.setElType(4);
	    	mTle.commit();
	    	
	    	//System.out.println(i+"\t"+mTle.getN());
	    	//System.out.println(mTle.getLine1());
	    	//System.out.println(mTle.getLine2());

	    	cart = USSFSGP4.getCart(cart.epoch, mTle);
	    	//System.out.println(co);
	    	//System.out.println(cart);
	    	kepNew = EphemerisUtil.cartToKep(cart);
	    	delt = Math.abs((kepOrig.meanMotion-kepNew.meanMotion)/kepOrig.meanMotion);
	    	if(delt < 1e-9) break;
	    	
	    	//System.out.println(delt);
	    	kep.meanMotion += kepOrig.meanMotion-kepNew.meanMotion;
	    	kep.incDeg += kepOrig.incDeg-kepNew.incDeg;
	    	kep.argpDeg += kepOrig.argpDeg-kepNew.argpDeg;
	    	kep.omegaDeg += kepOrig.omegaDeg-kepNew.omegaDeg;
	    	kep.ecc += kepOrig.ecc-kepNew.ecc;
	    	kep.maDeg += kepOrig.maDeg-kepNew.maDeg;
    	}
    	
    	tleOut = new TLE(mTle.getLine1(),mTle.getLine2());
   
    	return tleOut;
    }
    
    /**
     * Adjust the epoch time of the tle, keep the same bstar/bterm, nddot/agom.
     * 
     * @param tle
     * @param epoch
     * @return
     */
    public static TLE shiftEpoch(TLE tle, Date epoch)
    {
    	CartesianState cart = USSFSGP4.getCart(epoch, tle);
    	
    	int type = tle.getElType();
    	TLE newTLE = null;
    	if(type == 4)
    	{
    		newTLE = cartToXPTLE(cart,tle.getObjectID());
    	}
    	else
    	{
    		newTLE = cartToTLE(cart,tle.getObjectID());
    	}
    	
    	MutableTLE mTLE =new MutableTLE(newTLE.getLine1(),newTLE.getLine2());
    	
    	mTLE.setNDDot(tle.getNDDot());
    	mTLE.setBstar(tle.getBstar());
    	mTLE.commit();
    	
    	newTLE = new TLE(mTLE.getLine1(),mTLE.getLine2());
    	
    	return newTLE;
    }
    
	public static void main(String[] args) 
	{
		DateUtil.setDefaultTimeZone();
		
		String line1 = null;
		String line2 = null;
		
		line1 = "1 41085U XYXYX    19001.50315140 +.00000134 +00000-0 +89211-4 0  9990";
		line2 = "2 41085 098.8407 084.9556 0023046 143.2800 216.9959 14.16488812160040";
		
		TLE tle = new TLE(line1,line2);
		CartesianState cart = EphemerisUtil.getCart(tle, tle.getEpoch(), false);
		
		System.out.println(cart);
		String objectID = "99999";
		TLE tle2 = cartToTLE(cart, objectID);
		cart = EphemerisUtil.getCart(tle2, tle2.getEpoch(), false);
		
		System.out.println(cart);
		System.out.println(tle2.getLine1());
		System.out.println(tle2.getLine2());
		
		TLE tle2a = cartToTLE(cart, objectID, true);
		cart = EphemerisUtil.getCart(tle2a, tle2a.getEpoch(), false);
		
		System.out.println(cart);
		System.out.println(tle2a.getLine1());
		System.out.println(tle2a.getLine2());

		TLE tle3 = cartToXPTLE(cart,objectID);
		cart = EphemerisUtil.getCart(tle3, tle3.getEpoch(), false);
		
		System.out.println(cart);
		
		System.out.println(tle2.getLine1());
		System.out.println(tle2.getLine2());
		System.out.println(tle3.getLine1());
		System.out.println(tle3.getLine2());
		
		
		Date d2 = new Date(tle.getEpoch().getTime()+180000);
		TLE tle4 = shiftEpoch(tle,d2);
		System.out.println(tle4.getLine1());
		System.out.println(tle4.getLine2());
		
		System.exit(0);
	}

}
