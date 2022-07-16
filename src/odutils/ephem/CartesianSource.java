package odutils.ephem;

import java.util.Date;
import java.util.List;

public interface CartesianSource 
{
    public CartesianState getCartesian(Date d);
    public List<CartesianState> getCartesians(Date d1, Date d2, double tStepSec);
    
    public String getFrame();
    
    public boolean supportsUpdates();
    
    public void setFromEquinoctal(double params[]);
    public void setFromVector(double params[]);
    
    public void setMeanAnomaly(double val);
}
