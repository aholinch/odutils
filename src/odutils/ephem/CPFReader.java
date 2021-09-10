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

import java.util.ArrayList;
import java.util.List;

import odutils.util.FileUtil;

public class CPFReader 
{
    @SuppressWarnings({ "unchecked", "unused" })
	public static List<CartesianState> readCPF(String file)
    {
    	List<CartesianState> carts = null;
    	
    	List<String> lines = FileUtil.getLinesFromFile(file);
    	int size = lines.size();
    	carts = new ArrayList<CartesianState>(size);
    	
    	String line = null;
    	String id = null;
    	String intlid = null;
    	String sa[] = null;
    	
    	// read the headers
    	int nl = Math.min(10, size);
    	for(int i=0; i<nl; i++)
    	{
    		line = lines.get(i);
    		if(line.toLowerCase().startsWith("h2"))
    		{
    			line = line.replaceAll("  ", " ");
    			line = line.replaceAll("  ", " ");
    			line = line.replaceAll("  ", " ");
    			line = line.replaceAll("  ", " ");
    			sa = line.split(" ");
    			intlid = sa[1];
    			id = sa[3];
    			break;
    		}
    	}

    	//System.out.println(intlid+"\t"+id);
    	
    	//          1         2         3         4         5         6         7         8
    	//012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
    	//10 0 59215      0.000000  0      -4912774.270       4520982.403       1345867.408
    	//10 0 59215    180.000000  0      -4995945.953       4631787.077        -20851.315
    	//10 0 59215    360.000000  0      -4875882.444       4554087.058      -1386725.715
    	//10 0 59215    540.000000  0      -4562640.778       4286025.985      -2695981.054
    	//10 0 59215      0.00000  0 -13130672.696  13616218.171 -17073752.357
    	//10 0 59215    900.00000  0 -14911160.212  14464902.925 -14758024.492
    	//10 0 59215   1800.00000  0 -16353902.746  15305488.970 -12154782.966
    	    	
    	double mjd = 0;
    	double secs = 0;
    	double rx = 0;
    	double ry = 0;
    	double rz = 0;
    	//Date epoch = null;
    	CartesianState cart = null;
    	
    	double s2d = 1.0d/86400.0d;
    	double m2k = 1.0d/1000.0d;
    	
    	for(int i=0; i<size; i++)
    	{
    		line = lines.get(i);
    		if(line.startsWith("10"))
    		{
    			cart = new CartesianState();
    			
    			line = line.replaceAll("  "," ");
    			line = line.replaceAll("  "," ");
    			line = line.replaceAll("  "," ");
    			line = line.replaceAll("  "," ");
    			sa = line.split(" ");
    			mjd = Double.parseDouble(sa[2]);
    			secs = Double.parseDouble(sa[3]);
    			rx = Double.parseDouble(sa[5]);
    			ry = Double.parseDouble(sa[6]);
    			rz = Double.parseDouble(sa[7]);
    			/*
    			mjd = gd(line,5,10);
    			secs = gd(line,11,24);
    			rx = gd(line,28,45);
    			ry = gd(line,46,63);
    			rz = gd(line,64,81);
    			*/
    			mjd += secs*s2d;
    			rx*=m2k;
    			ry*=m2k;
    			rz*=m2k;
    			
    			cart.setEpoch(EphemerisUtil.mjd2date(mjd));
    			cart.setRVec(rx, ry, rz);
    			carts.add(cart);
    		}
    	}
    	return carts;
    }
    
    protected static double gd(String line, int i1, int i2)
    {
    	double num = 0;
    	try
    	{
    		String str = line.substring(i1,i2);
    		num = Double.parseDouble(str.trim());
    	}catch(Exception ex){} // quiet
    	return num;
    }
}
