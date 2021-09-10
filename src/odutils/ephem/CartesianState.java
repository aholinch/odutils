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

import java.util.Date;

public class CartesianState implements Comparable<CartesianState>
{
    public Date epoch;
    
    public double rx;
    public double ry;
    public double rz;
    
    public double vx;
    public double vy;
    public double vz;
    
    public CartesianState()
    {
    	
    }
    
    public void setEpoch(Date d)
    {
    	epoch = d;
    }
    
    public Date getEpoch()
    {
    	return epoch;
    }
    
    public double getRmag()
    {
    	double tmp = rx*rx+ry*ry+rz*rz;
    	return Math.sqrt(tmp);
    }

    public double getVmag()
    {
    	double tmp = vx*vx+vy*vy+vz*vz;
    	return Math.sqrt(tmp);
    }
    
    public CartesianState diff(CartesianState cs)
    {
    	CartesianState csout = new CartesianState();
    	if(epoch != null)
    	{
    		csout.epoch = new java.sql.Timestamp(epoch.getTime());
    	}
    	
    	csout.rx = rx-cs.rx;
    	csout.ry = ry-cs.ry;
    	csout.rz = rz-cs.rz;
    	csout.vx = vx-cs.vx;
    	csout.vy = vy-cs.vy;
    	csout.vz = vz-cs.vz;
    	
    	return csout;
    }
    
    public double getDist(CartesianState cs)
    {
    	return getDist(cs.rx,cs.ry,cs.rz);
    }
    
    public double getDist(double Rx, double Ry, double Rz)
    {
    	double tmp = 0;
    	double sum = 0;
    	
    	tmp = rx-Rx;
    	sum += tmp*tmp;
    	tmp = ry-Ry;
    	sum += tmp*tmp;
    	tmp = rz-Rz;
    	sum += tmp*tmp;
    	
    	return Math.sqrt(sum);
    }
    
    public double getDV(CartesianState cs)
    {
    	return getDV(cs.vx,cs.vy,cs.vz);
    }
    
    public double getDV(double Vx, double Vy, double Vz)
    {
    	double tmp = 0;
    	double sum = 0;
    	
    	tmp = vx-Vx;
    	sum += tmp*tmp;
    	tmp = vy-Vy;
    	sum += tmp*tmp;
    	tmp = vz-Vz;
    	sum += tmp*tmp;
    	
    	return Math.sqrt(sum);    	
    }
    
    public double[] getRVec()
    {
    	return new double[]{rx,ry,rz};    	
    }
    
    public double[] getVVec()
    {
    	return new double[]{vx,vy,vz};
    }
    
    public void setRVec(double r_x, double r_y, double r_z)
    {
    	rx = r_x;
    	ry = r_y;
    	rz = r_z;
    }
    
    public void setRVec(double r[])
    {
    	rx = r[0];
    	ry = r[1];
    	rz = r[2];
    }
    
    public void setVVec(double v[])
    {
    	vx = v[0];
    	vy = v[1];
    	vz = v[2];
    }

	@Override
	public int compareTo(CartesianState cs) 
	{
		int comp = epoch.compareTo(cs.epoch);
		
		if(comp == 0)
		{
			double r1 = getRmag();
			double r2 = cs.getRmag();
			
			if(r1<r2)
			{
				comp = -1;
			}
			else if(r1>r2)
			{
				comp = 1;
			}
		}
		
		return comp;
	}
    
	/**
	 * Returns answer in radians.
	 * 
	 * @param cs
	 * @return
	 */
	public double angleBetween(CartesianState cs)
	{
		return angleBetween(cs.rx,cs.ry,cs.rz);
	}
	
	/**
	 * Returns answer in radians.
	 * 
	 * @param Rx
	 * @param Ry
	 * @param Rz
	 * @return
	 */
	public double angleBetween(double Rx, double Ry, double Rz)
	{
		double r1 = getRmag();
		double r2 = Rx*Rx+Ry*Ry+Rz*Rz;
		r2 = Math.sqrt(r2);
		
		double tmp = rx*Rx+ry*Ry+rz*Rz;
		tmp = tmp/(r1*r2);
		
		tmp = Math.acos(tmp);
		
		return tmp;
	}
	
	public String toString()
	{
		return String.valueOf(epoch)+"\t"+rx+"\t"+ry+"\t"+rz+"\t"+vx+"\t"+vy+"\t"+vz;
	}
}
