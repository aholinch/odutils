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

import java.util.Date;

import sgp4.TLE;

public class KeplerianState 
{
    public Date epoch;
    public double smaKM;
    public double pKM;
    public double periodSec;
    public double meanMotion;
    public double omegaDeg;
    public double incDeg;
    public double ecc;
    public double argpDeg;
    public double maDeg;
    public double taDeg;
    public double eaDeg;
    public double arglatDeg;
    public double truelonDeg;
    public double lonperDeg;
    
    public KeplerianState()
    {
    	
    }
    
    public KeplerianState(CartesianState cart)
    {
    	setFromCart(cart);
    }
    
    public void setFromCart(CartesianState cart)
    {
    	EphemerisUtil.cartToKep(cart, this, EphemerisUtil.GM_Earth_km3);
    }
    
    public CartesianState getCart()
    {
    	return EphemerisUtil.kepToCart(this);
    }
    
    public KeplerianState copy()
    {
    	KeplerianState kep = new KeplerianState();
    	kep.epoch = new java.sql.Timestamp(epoch.getTime());
    	kep.smaKM = smaKM;
    	kep.pKM = pKM;
    	kep.periodSec = periodSec;
    	kep.meanMotion = meanMotion;
    	kep.omegaDeg = omegaDeg;
    	kep.incDeg = incDeg;
    	kep.ecc = ecc;
    	kep.argpDeg = argpDeg;
    	kep.maDeg = maDeg;
    	kep.taDeg = taDeg;
    	kep.eaDeg = eaDeg;
    	kep.arglatDeg = arglatDeg;
    	kep.truelonDeg = truelonDeg;
    	kep.lonperDeg = lonperDeg;
    	return kep;
    }
    
    public void updateValsFromMeanMotion(double mmRevsPerDay)
    {
    	this.meanMotion = mmRevsPerDay;
    	this.periodSec = 86400.0d/this.meanMotion;
    	double tmp = EphemerisUtil.GM_Earth_km3/(4.0*Math.PI*Math.PI)*this.periodSec*this.periodSec;
    	this.smaKM = Math.pow(tmp, 1.0/3.0);
    	this.pKM=this.smaKM*(1.0-this.ecc*this.ecc);
    }
    
    public void updateValsFromSma(double smaKm)
    {
    	this.smaKM = smaKm;
    	this.pKM=this.smaKM*(1.0-this.ecc*this.ecc);
    	double tmp = smaKm*smaKm*smaKm/(EphemerisUtil.GM_Earth_km3/(4.0*Math.PI*Math.PI));
    	this.periodSec = Math.sqrt(tmp);
    	this.meanMotion = 86400.0d/this.periodSec;
    }
    
    public static void setToTLE(KeplerianState kep, TLE tle)
    {
    	tle.setN(kep.meanMotion);
    	tle.setArgpDeg(kep.argpDeg);
    	tle.setMaDeg(kep.maDeg);
    	tle.setIncDeg(kep.incDeg);
    	tle.setEcc(kep.ecc);
    	tle.setRaanDeg(kep.omegaDeg);
    	tle.setEpoch(kep.epoch);
    }
    
    public String toString()
    {
    	return String.valueOf(epoch)+","+smaKM+","+ecc+","+incDeg+","+omegaDeg+","+argpDeg+","+maDeg +","+meanMotion+","+periodSec;
    }
}
