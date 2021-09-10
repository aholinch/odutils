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

public abstract class AbstractODTask implements ODTask 
{
    protected int type = 0;
    protected int iter = 0;
    protected int maxIters = 20;
    protected double rms = 0;
    protected boolean isRunning = false;
    protected ObservationSet obsSet = null;
    protected Date epoch = null;
    protected OrbitState initState = null;
    protected OrbitState solvedState = null;
    protected String status = null;
    
    public AbstractODTask()
    {
    	
    }
    
    public void setStatus(String val)
    {
    	status = val;
    }
    
    public String getStatus()
    {
    	return status;
    }


	@Override
	public void setObservationSet(ObservationSet obs)
	{
		obsSet = obs;
	}

	@Override
	public ObservationSet getObservationSet()
	{
		return obsSet;
	}

	@Override
	public int getType() 
	{
		return type;
	}

	@Override
	public int getIteration() 
	{
		return iter;
	}

	@Override
	public void setMaxIterations(int num) 
	{
		maxIters = num;
	}

	@Override
	public int getMaxIterations() 
	{
		return maxIters;
	}

	@Override
	public double getRMS() 
	{
		return rms;
	}
	
	@Override
	public boolean isRunning() 
	{
		return isRunning;
	}

	@Override
	public OrbitState getSolvedState() 
	{
		return solvedState;
	}


	@Override
	public OrbitState getInitialState() 
	{
		return initState;
	}

	@Override
	public void setInitialState(OrbitState state) 
	{
		initState = state;
	}
	
	@Override
	public Date getEpoch() 
	{
		return epoch;
	}
	
	@Override
	public void setEpoch(Date date) 
	{
		epoch = date;
	}
}
