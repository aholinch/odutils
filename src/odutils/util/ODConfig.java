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
package odutils.util;

import java.io.File;
import java.util.logging.Logger;

public class ODConfig 
{
	private static final Logger logger = Logger.getLogger(ODConfig.class.getName());
	
    private static ODConfig instance = null;
    
    private static final String sync = "mutex";
    
    protected String orekitDataDir = null;
    
    private ODConfig()
    {
    	// load from properties
    }
    
    public static ODConfig getInstance()
    {
    	if(instance == null)
    	{
    		synchronized(sync)
    		{
    			// yes check again
    			if(instance == null)
    			{
    				instance = new ODConfig();
    			}
    		}
    	}
    	return instance;
    }
    
    public String getOrekitDataDir()
    {
    	if(orekitDataDir == null)
    	{
    		File f = searchExistingDir("orekit-data","orekit-data-master","orekit_data","orekit.data");
    		if(f != null)
    		{
    			orekitDataDir = f.getAbsolutePath();
    		}
    	}
    	return orekitDataDir;
    }
    
    /**
     * Looks in env param, java define param, running dir, and then home for directory.
     * 
     * @param dirEnd
     * @param env
     * @param param
     * @return
     */
    protected File searchExistingDir(String name1, String name2, String env, String param)
    {
    	File f = null;
    	String path = null;
    	// check env first
    	if(env != null)
    	{
    		path = System.getenv(env);
    		if(path != null)
    		{
    			f = new File(path);
    		}
    	}
    	
    	if(f==null || !f.exists())
    	{
    		// check define param
    		if(param != null)
    		{
    			path = System.getProperty(param);
    			if(path != null)
    			{
    				f = new File(path);
    			}
    		}
    	}
    	
    	if(f == null || !f.exists())
    	{
    		// check home directory
    		final File home = new File(System.getProperty("user.home"));
    		f = new File(home,name1);
    		if(!f.exists() && name2 != null)
    		{
    			f = new File(home,name2);
    		}
    	}
    	
    	if(f == null || !f.exists())
    	{
    		// check current directory
    		f = new File(name1);
    		if(!f.exists() && name2 != null)
    		{
    			f = new File(name2);
    		}
    	}
    	
    	if(f == null || !f.exists())
    	{
    		logger.warning("Unable to find dir for " + String.valueOf(name1) + " after checking multiple places");
    	}
    	
    	return f;
    }
}
