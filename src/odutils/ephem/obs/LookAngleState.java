package odutils.ephem.obs;

import java.util.Date;

public class LookAngleState 
{
    public Date epoch;
    
    public double elDeg;
    public double azDeg;
    public double elDegPerSec;
    public double azDegPerSec;
    
    public double raDeg;
    public double decDeg;
    public double raDegPerSec;
    public double decDegPerSec;
    
    public double rangeKM;
    public double rangeRateKMPerSec;
    
    public double latDeg;
    public double lonDeg;
    public double altM;
    
    public LookAngleState()
    {
    	
    }
    
    public void setRaDeg(double deg)
    {
    	while(deg < 0) deg += 360.0d;
    	while(deg > 360) deg -= 360.0d;
    	
    	raDeg = deg;
    }
    
    public void setRaHour(double hr)
    {
    	setRaDeg(hr*15.0);
    }
    
    public double getRaDeg()
    {
    	return raDeg;
    }
    
    public double getRaHour()
    {
    	return raDeg/15.0d;
    }
    
    public void setRangeKM(double num)
    {
    	rangeKM = num;
    }
    
    public double getRangeKM()
    {
    	return rangeKM;
    }
    
    public void setRangeRateKMPerSec(double num)
    {
    	rangeRateKMPerSec = num;
    }
    
    public double getRangeRateKMPerSec()
    {
    	return rangeRateKMPerSec;
    }
    
    public double getAzElSeparationDeg(LookAngleState la)
    {
    	return getAngularSeparation(azDeg,elDeg,la.azDeg,la.elDeg);
    }
    
    public double getRaDecSeparationDeg(LookAngleState la)
    {
    	return getAngularSeparation(getRaDeg(),decDeg,la.getRaDeg(),la.decDeg);
    }
    
	/**
	 * Calculate angular separation in degrees
	 * 
	 * @param ra1Deg
	 * @param dec1Deg
	 * @param ra2Deg
	 * @param dec2Deg
	 * @return
	 */
    public static double getAngularSeparation(double ra1Deg, double dec1Deg, double ra2Deg, double dec2Deg)
    {
    	double diff1 = dec1Deg-dec2Deg;
    	double diff2 = Math.abs(ra1Deg-ra2Deg);
    	if(diff2 > 180.0d)
    	{
    		diff2 = 360.0d-diff2;
    	}
    	
    	diff1 = Math.toRadians(diff1);
    	diff2 = Math.toRadians(diff2);
    	
    	double dec1 = Math.toRadians(dec1Deg);
    	double dec2Rad = Math.toRadians(dec2Deg);
    	
    	double h = haversine(diff1)+Math.cos(dec2Rad)*Math.cos(dec1)*haversine(diff2);
    	
    	double dist = 2.0d*Math.asin(Math.sqrt(h));
    	dist = Math.toDegrees(dist);
    	return dist;
    }
    
    public static double haversine(double t)
    {
    	double sin = Math.sin(t*0.5d);
    	return sin*sin;
    }
}
