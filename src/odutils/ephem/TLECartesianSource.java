package odutils.ephem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sgp4.TLE;

public class TLECartesianSource implements CartesianSource 
{
	protected TLE tle = null;
	protected boolean makej2k = false;
	
	public TLECartesianSource(TLE tle)
	{
		this.tle = tle;
	}
	
	public void setMakeJ2k(boolean flag)
	{
		makej2k = flag;
	}
	
	public boolean getMakeJ2k()
	{
		return makej2k;
	}
	
	@Override
	public CartesianState getCartesian(Date d) 
	{
		return EphemerisUtil.getCart(tle, d, makej2k);
	}

	public String getFrame()
	{
		if(makej2k)
		{
			return "eme2000";
		}
		
		return "teme";
	}
	
    public boolean supportsUpdates()
    {
    	return true;
    }
    
    public void setFromEquinoctal(double params[])
    {
    	this.tle = paramsToTLE(params);
    }

    public void setFromVector(double params[])
    {
    	CartesianState cart = new CartesianState();
    	cart.epoch = tle.getEpoch();
    	cart.setRVec(params[0],params[1],params[2]);
    	cart.vx = params[3];
    	cart.vy = params[4];
    	cart.vz = params[5];
    	
    	throw new RuntimeException("not implemented");
    	//this.tle = EphemerisUtil.c
    }
    
    public TLE getTLE()
    {
    	return tle;
    }
    
	protected TLE paramsToTLE(double params[])
	{
		MutableTLE mtle = null;
		
		double vals[] = EphemerisUtil.getCOEFromEquinoctal(params);

		mtle = new MutableTLE(tle.getLine1(),tle.getLine2());
		
		//i, Om, ecc, w, M, n
		mtle.setIncDeg(vals[0]);
		mtle.setRaanDeg(vals[1]);
		mtle.setEcc(vals[2]);
		mtle.setArgpDeg(vals[3]);
		mtle.setMaDeg(vals[4]);
		mtle.setN(vals[5]);
		
		double val = 0;
		if(params.length>6)
		{
			mtle.setBstar(params[6]);
		}
		if(params.length>7)
		{
			mtle.setNDDot(params[7]);
		}
		
		//System.err.println("Test vals\t"+tle.getNDDot()+"\t"+tle.getBstar());
		
		//System.out.println("bstarnddot\t"+tle.getBstar()+"\t"+tle.getNDDot());
		mtle.commit();
		
		return mtle;
	}

	@Override
	public void setMeanAnomaly(double val) {
		MutableTLE mtle = null;
		
		mtle = new MutableTLE(tle.getLine1(),tle.getLine2());
		
		mtle.setMaDeg(val);
		this.tle = mtle;
	}
	
	@Override
	public List<CartesianState> getCartesians(Date d1, Date d2, double tStepSec)
	{
    	return EphemerisUtil.getCarts(tle, d1, d2, tStepSec, makej2k);
	}


}
