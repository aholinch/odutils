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

/**
 * ODTasks can be run independently or submitted to the ODTaskRunner.
 * 
 * @author aholinch
 *
 */
public interface ODTask extends Runnable
{
	public static final int TYPE_SGP4        = 1;
	public static final int TYPE_SGP4_XP     = 2;
	public static final int TYPE_OREKIT_HP   = 3;
	
	public void setEpoch(Date epoch);
	public Date getEpoch();
	
	public void setInitialState(OrbitState state);
	public OrbitState getInitialState();
	
	public void setObservationSet(ObservationSet obs);
	public ObservationSet getObservationSet();
	public int getType();
	public int getIteration();
	public void setMaxIterations(int num);
	public int getMaxIterations();
	public double getRMS();
	
    public void run();
    
    public boolean isRunning();
    
    public void stopOD();
    
    public OrbitState getSolvedState();
    
    public void setStatus(String val);
    public String getStatus();
}
