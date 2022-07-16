package odutils.ephem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class J2CartesianSource implements CartesianSource 
{
	protected KeplerianState kep = null;
	protected String frame = "eme2000";
	
	public J2CartesianSource(KeplerianState kep)
	{
		this.kep = kep;
	}
	
	public J2CartesianSource(CartesianState cart)
	{
		this.kep = EphemerisUtil.cartToKep(cart);
	}
	
	public void setCartesianState(CartesianState cart)
	{
		this.kep = EphemerisUtil.cartToKep(cart);
	}
	
	public KeplerianState getKep()
	{
		return kep;
	}
	
	@Override
	public CartesianState getCartesian(Date d) 
	{
		return EphemerisUtil.getCartJ2(kep, d);
	}

	public String getFrame()
	{
		return frame;
	}
	
    public boolean supportsUpdates()
    {
    	return true;
    }
    
    public void setFromEquinoctal(double params[])
    {
    	double vals[] = EphemerisUtil.getCOEFromEquinoctal(params);
    	
    	KeplerianState newKep = new KeplerianState();
    	newKep.epoch = kep.epoch;
    	newKep.incDeg = vals[0];
    	newKep.omegaDeg = vals[1];
    	newKep.ecc = vals[2];
    	newKep.argpDeg = vals[3];
    	newKep.maDeg = vals[4];
    	newKep.updateValsFromMeanMotion(vals[5]);
    	
    	this.kep = newKep;
    }
    
    public void setFromVector(double params[])
    {
    	CartesianState cart = new CartesianState();
    	cart.epoch = kep.epoch;
    	cart.setRVec(params[0],params[1],params[2]);
    	cart.vx = params[3];
    	cart.vy = params[4];
    	cart.vz = params[5];
    	setCartesianState(cart);
    }

	@Override
	public void setMeanAnomaly(double val) {
		kep.maDeg = val;
		if(kep.ecc == 0)
		{
			kep.taDeg = val;
		}
		else
		{
			double EA = EphemerisUtil.MA2EA(kep.ecc, Math.toRadians(val));
			double nu = EphemerisUtil.EA2TA(kep.ecc, EA);
			kep.taDeg = Math.toDegrees(nu);
		}
	}
	
	@Override
	public List<CartesianState> getCartesians(Date d1, Date d2, double tStepSec)
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
    	
   
		while(t < t2)
		{
			d = new java.sql.Timestamp(t);
			cart = getCartesian(d);
			carts.add(cart);
			
			t+=tstep;
		}
		
		// ensure last date is included regardless of steps
		d = new java.sql.Timestamp(t2);
		cart = getCartesian(d);
		carts.add(cart);

		return carts;
	}


}
