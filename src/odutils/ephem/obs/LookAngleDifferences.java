package odutils.ephem.obs;

import java.util.ArrayList;
import java.util.List;

import odutils.ephem.CartesianSource;
import odutils.ephem.CartesianState;

public class LookAngleDifferences 
{
	protected CartesianSource cartSource = null;
	
    public LookAngleDifferences()
    {
    	
    }
    
    public LookAngleDifferences(CartesianSource src)
    {
    	cartSource = src;
    }
    
    public void setCartesianSource(CartesianSource src)
    {
    	cartSource = src;
    }
    
    public CartesianSource getCartesianSource()
    {
    	return cartSource;
    }
    
    public double rmseMultiLoc(List<List<LookAngleState>> list, DifferenceFunction func)
    {
    	double sum = 0;
    	double w = 0;
    	double totw = 0;
    	double tmp = 0;
    	
    	int size = list.size();
    	List<LookAngleState> las = null;
    	
    	for(int i=0; i<size; i++)
    	{
    		las = list.get(i);
    		tmp = rmse(las,func);
    		w = las.size();
    		totw+=w;
    		sum+= w*tmp*tmp;
    	}
    	sum = sum/totw;
    	sum = Math.sqrt(sum);
    	return sum;
    }
    
    public double rmse(List<LookAngleState> las, DifferenceFunction func)
    {
    	double sum = 0;
    	
    	double diffs[] = differences(las,func);
    	
    	int len = diffs.length;
    	double tmp = 0;
    	for(int i=0; i<len; i++)
    	{
    		tmp = diffs[i];
    		sum += tmp*tmp;
    	}
    	sum = sum / ((double)len);
    	sum = Math.sqrt(sum);
    	return sum;
    }
    
    /**
     * This method is useful for orbit determination that may want to weight deltas differently.
     * 
     * @param las
     * @param func
     * @return
     */
    public double[] differences(List<LookAngleState> las, DifferenceFunction func)
    {
    	int size = las.size();
    	double diffs[] = new double[size];
    	
    	CartesianState cart = null;
    	List<CartesianState> carts = new ArrayList<CartesianState>();
    	LookAngleState la1 = null;
    	LookAngleState la2 = null;
    	List<LookAngleState> las2 = null;
    	for(int i=0; i<size; i++)
    	{
    		la1 = las.get(i);
    		cart = cartSource.getCartesian(la1.epoch);
    		carts.clear();
    		carts.add(cart);
    		las2 = LookAngleCalculator.computeLookAngles(la1.latDeg, la1.lonDeg, la1.altM, carts, cartSource.getFrame());
    		la2 = las2.get(0);
    		
    		diffs[i]=func.diff(la1, la2);
    	}
    	
    	return diffs;
    }
    
    public static interface DifferenceFunction
    {
    	public double diff(LookAngleState la1, LookAngleState la2);
    }
    
    /**
     * Computes angular separation between az/el values
     */
    public static class AzElDifference implements DifferenceFunction
    {
    	public double diff(LookAngleState la1, LookAngleState la2)
    	{
    		return la1.getAzElSeparationDeg(la2);
    	}
    }
    
    /**
     * Computes angular separation between ra/dec values
     */
    public static class RaDecDifference implements DifferenceFunction
    {
    	public double diff(LookAngleState la1, LookAngleState la2)
    	{
    		return la1.getRaDecSeparationDeg(la2);
    	}
    }    
    
    /**
     * Computes difference in range values
     */
    public static class RangeDifference implements DifferenceFunction
    {
    	public double diff(LookAngleState la1, LookAngleState la2)
    	{
    		return la1.getRangeKM()-la2.getRangeKM();
    	}
    }
    
    /**
     * Computes difference in range-rate values
     */
    public static class RangeRateDifference implements DifferenceFunction
    {
    	public double wrongSignWeight;
    	
    	public RangeRateDifference(double weight)
    	{
    		this.wrongSignWeight = weight;
    	}
    	
    	public double diff(LookAngleState la1, LookAngleState la2)
    	{
    		double diff =  la1.getRangeRateKMPerSec()-la2.getRangeRateKMPerSec();

    		if(la1.getRangeRateKMPerSec()*la2.getRangeRateKMPerSec()<0)
    		{
    			diff *= this.wrongSignWeight;
    		}
    		
    		return diff;
    	}
    }

    public static class RangeRateDifferenceAboveHorizon implements DifferenceFunction
    {
    	public double diff(LookAngleState la1, LookAngleState la2)
    	{
    		double diff = la1.getRangeRateKMPerSec()-la2.getRangeRateKMPerSec();
    		if(la1.elDeg<0 || la2.elDeg<0)diff*=1.1;
    		return diff;
    	}
    }

}
