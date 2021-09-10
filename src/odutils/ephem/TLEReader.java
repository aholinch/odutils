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
package odutils.ephem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sgp4.TLE;

public class TLEReader 
{
	public static Map<String,List<TLE>> mapByID(List<TLE> tles)
	{
		int size = tles.size();
		Map<String,List<TLE>> m = new HashMap<String,List<TLE>>();
		int ns = Math.min(100, size/100);
		
		TLE tle = null;
		String id = null;
		List<TLE> tmp = null;
		for(int i=0; i<size; i++)
		{
			tle = tles.get(i);
			id = tle.getObjectID();
			tmp = m.get(id);
			if(tmp == null)
			{
				tmp = new ArrayList<TLE>(ns);
				m.put(id, tmp);
			}
			tmp.add(tle);
		}
		
		return m;
	}
	
    public static List<TLE> readTLEs(String file)
    {
    	File f = new File(file);
    	double fsize = f.length();
    	fsize = fsize/(140); // divide by two lines
    	
    	List<TLE> tles = new ArrayList<TLE>((int)fsize);
    	
    	FileReader fr = null;
    	BufferedReader br = null;
    	TLE tle = null;
    	try
    	{
    		fr = new FileReader(file);
    		br = new BufferedReader(fr);
    		
    		String line1 = null;
    		String line2 = null;
    		
    		line2 = br.readLine();
    		while(line2 != null)
    		{
    			line1 = line2;
    			line2 = br.readLine();
    			if(line2 != null)
    			{
    				if(line1.startsWith("1 ") && line2.startsWith("2 "))
    				{
    					if(line1.substring(1,7).equals(line2.substring(1,7)))
    					{
    						tle = new TLE(line1,line2);
    						tles.add(tle);
    					}
    				}
    			}
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	finally
    	{
    		if(fr != null)try {fr.close();}catch(Exception ex) {}
    		if(br != null)try {br.close();}catch(Exception ex) {}
    	}
    	return tles;
    }
    
    public static List<TLE> cleanDuplicates(List<TLE> tles)
    {
    	if(tles == null || tles.size()<2) return tles;
    	int size = tles.size();
    	List<TLE> out = new ArrayList<TLE>(size);
    	
    	TLE tlePrev = null;
    	TLE tle = null;
    	
    	tle = tles.get(0);
    	out.add(tle);
    	
    	for(int i=1; i<size; i++)
    	{
    		tlePrev = tle;
    		tle = tles.get(i);
    		if(!tle.getEpoch().equals(tlePrev.getEpoch()))
    		{
    			out.add(tle);
    		}
    	}
    	
    	return out;
    }
}
