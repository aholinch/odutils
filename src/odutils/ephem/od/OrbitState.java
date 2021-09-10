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
import java.util.Map;

import odutils.ephem.CartesianState;
import odutils.ephem.KeplerianState;
import sgp4.TLE;

public class OrbitState 
{
    public static final int TYPE_TLE        = 1;
    public static final int TYPE_CARTESIAN  = 2;
    public static final int TYPE_KEPLERIAN  = 3;
    
    protected TLE tle = null;
    protected CartesianState cart = null;
    protected KeplerianState kep = null;
    protected Date epoch = null;
    
    protected double covariance[][] = null;
    
    protected Map<String,Double> params = null;
    
    protected int type = 0;
    protected int numIters = 0;
    protected double rms = 0;
    
    public OrbitState()
    {
    	
    }
    
    public double getRMS()
    {
    	return rms;
    }
    
    public int getNumIters()
    {
    	return numIters;
    }
    
    public TLE getTLE()
    {
    	return tle;
    }
}
