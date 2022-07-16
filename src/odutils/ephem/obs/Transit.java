package odutils.ephem.obs;

import java.util.Date;

public class Transit 
{
	public Date startDate;
	public Date stopDate;
	public Date tcaDate;
	
	public LookAngleState losAngles;
	public LookAngleState aosAngles;
	public LookAngleState tcaAngles;
	
    public Transit()
    {
    	
    }
    
    public Date getStartDate()
    {
    	return startDate;
    }
    
    public void setStartDate(Date d)
    {
    	startDate = d;
    }
    
    public Date getStopDate()
    {
    	return stopDate;
    }
    
    public void setStopDate(Date d)
    {
    	stopDate = d;
    }
    
    public Date getTCADate()
    {
    	return tcaDate;
    }
    
    public void setTCADate(Date d)
    {
    	tcaDate = d;
    }
    
    public LookAngleState getAOSAngles()
    {
    	return aosAngles;
    }
    
    public void setAOSAngles(LookAngleState la)
    {
    	aosAngles = la;
    	if(la != null)
    	{
    		startDate = la.epoch;
    	}
    }
    
    public LookAngleState getLOSAngles()
    {
    	return losAngles;
    }
    
    public void setLOSAngles(LookAngleState la)
    {
    	losAngles = la;
    	if(la != null)
    	{
    		stopDate = la.epoch;
    	}
    }
    
    public LookAngleState getTCAAngles()
    {
    	return tcaAngles;
    }
    
    public void setTCAAngles(LookAngleState la)
    {
    	tcaAngles = la;
    	if(la != null)
    	{
    		tcaDate = la.epoch;
    	}
    }
}
