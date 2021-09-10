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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ODTaskRunner 
{
    private static final String sync = "mutex";
    
    private static final Logger logger = Logger.getLogger(ODTaskRunner.class.getName());
    
    private static ODTaskRunner instance = null;
    
    private Map<String,ODTask> idToTask = null;
    private ExecutorService executorService = null;
    
    private ODTaskRunner()
    {
    	idToTask = new HashMap<String,ODTask>();
    	executorService = Executors.newFixedThreadPool(10);
    }
    
    public static ODTaskRunner getInstance()
    {
    	if(instance == null)
    	{
    		synchronized(sync)
    		{
    			// yes, check again
    			if(instance == null)
    			{
    				instance = new ODTaskRunner();
    			}
    		}
    	}
    	
    	return instance;
    }
    
    public ODTask getTask(String taskID)
    {
    	return idToTask.get(taskID);
    }
    
    public String getTaskStatus(String taskID)
    {
    	String status = null;
    	
    	ODTask task = idToTask.get(taskID);
    	if(task != null)
    	{
    		status = task.getStatus();
    	}
    	else
    	{
    		logger.info("No task for id " + String.valueOf(taskID));
    	}
    	
    	return status;
    }
    
    public String submitTask(ODTask task)
    {
    	if(task == null)
    	{
    		logger.info("Null task submitted");
    		return null;
    	}
    	
    	// generate id
    	String id = genID();
    	logger.info("Task " + id + " submitted");
    	idToTask.put(id, task);
    	
    	executorService.execute(task);
    	
    	return id;
    }
    
    protected String genID()
    {
    	String str = String.valueOf(System.currentTimeMillis());
    	
    	try
    	{
    		str = UUID.randomUUID().toString();
    		str = str.replace('_', ' ');
    		str = str.replace('-', ' ');
    		str = str.replaceAll(" ", "");
    	}
    	catch(Exception ex)
    	{
    		logger.log(Level.WARNING,"Error generating uuid",ex);
    	}
    	
    	return str;
    }
}
