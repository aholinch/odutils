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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;


import odutils.util.DateUtil;
import odutils.util.ODConfig;

public class OrekitUtils 
{
	static
	{
		initDataDir();
	}
	
    public static void initDataDir()
    {
    	
        // configure Orekit
        final File home       = new File(System.getProperty("user.home"));
        /*
        File orekitData = null;
        
        // check for environment variable
        String envPath = System.getenv("orekit_data");
        if(envPath != null && envPath.length()>0)
        {
        	orekitData = new File(envPath);
        }
        
        // check java property
        if(orekitData == null || !orekitData.exists())
        {
        	envPath = System.getProperty("orekit.data");
        	if(envPath != null && envPath.length()>0)
            {
            	orekitData = new File(envPath);
            }	
        }
        
        // guess local run dir
        if(orekitData == null || !orekitData.exists())
    	{
        	orekitData = new File("orekit-data");
    	}
        if(!orekitData.exists())
        {
        	orekitData = new File("orekit-data-master");
        }
        
        // check home directory
        if(!orekitData.exists())
        {
        	orekitData = new File(home,"orekit-data");
        }
        
        if(!orekitData.exists())
        {
        	orekitData = new File(home,"orekit-data-master");
        }
        */

    	String path = ODConfig.getInstance().getOrekitDataDir();
    	File orekitData = new File(path);
    	
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                              orekitData.getAbsolutePath());
            System.err.format(Locale.US, "dddYou need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                              "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                              home.getAbsolutePath());
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

    }
    
    public static CartesianState doTransform(Transform trans, CartesianState cart)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        
        x = cart.rx*1000.0d;
        y = cart.ry*1000.0d;
        z = cart.rz*1000.0d;
        
        Vector3D pvec = new Vector3D(x,y,z);
        
        x = cart.vx*1000.0d;
        y = cart.vy*1000.0d;
        z = cart.vz*1000.0d;
        
        Vector3D vvec = new Vector3D(x,y,z);
        PVCoordinates pv = new PVCoordinates(pvec,vvec);
        
        pv = trans.transformPVCoordinates(pv);
       
        CartesianState cartOut = new CartesianState();
        cartOut.epoch = cart.epoch;
        pvec = pv.getPosition();
        vvec = pv.getVelocity();
        cartOut.rx=pvec.getX()/1000.0d;
        cartOut.ry=pvec.getY()/1000.0d;
        cartOut.rz=pvec.getZ()/1000.0d;
        cartOut.vx=vvec.getX()/1000.0d;
        cartOut.vy=vvec.getY()/1000.0d;
        cartOut.vz=vvec.getZ()/1000.0d;
        
        return cartOut;

    }
    
    public static CartesianState ecef2teme(CartesianState cart)
    {
        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = toAD(cart.getEpoch());
        Transform ecfToTEME = null;
        ecfToTEME = itrf.getTransformTo(teme, epoch);
        
        return doTransform(ecfToTEME,cart);
    }

    
    public static List<CartesianState> ecef2teme(List<CartesianState> carts)
    {
        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = null;
        CartesianState cartNew = null;
        CartesianState cart = null;
        Transform ecfToTEME = null;
        
        int size = carts.size();
        
        List<CartesianState> cartsOut = new ArrayList<CartesianState>(size);
        for(int i=0; i<size; i++)
        {
        	cart = carts.get(i);
        	epoch = toAD(cart.getEpoch());
        	ecfToTEME = itrf.getTransformTo(teme, epoch);
        	cartNew = doTransform(ecfToTEME,cart);
        	cartsOut.add(cartNew);
        }
        
        return cartsOut;
    }

    public static CartesianState teme2ecef(CartesianState cart)
    {
        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = toAD(cart.getEpoch());
        Transform TEMEtoecef = null;
        TEMEtoecef = teme.getTransformTo(itrf, epoch);
        
        return doTransform(TEMEtoecef,cart);
    }

    
    public static List<CartesianState> teme2ecef(List<CartesianState> carts)
    {
        Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = null;
        CartesianState cartNew = null;
        CartesianState cart = null;
        Transform TEMEtoecef = null;
        
        int size = carts.size();
        
        List<CartesianState> cartsOut = new ArrayList<CartesianState>(size);
        for(int i=0; i<size; i++)
        {
        	cart = carts.get(i);
        	epoch = toAD(cart.getEpoch());
        	TEMEtoecef = teme.getTransformTo(itrf, epoch);
        	cartNew = doTransform(TEMEtoecef,cart);
        	cartsOut.add(cartNew);
        }
        
        return cartsOut;
    }

    public static CartesianState teme2EME2000(CartesianState cart)
    {
        Frame eme2000 = FramesFactory.getEME2000();
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = toAD(cart.getEpoch());
        Transform teme2EME2000 = null;
        teme2EME2000 = teme.getTransformTo(eme2000, epoch);
        
        return doTransform(teme2EME2000,cart);
    }
    
    public static List<CartesianState> teme2EME2000(List<CartesianState> carts)
    {
        Frame eme2000 = FramesFactory.getEME2000();
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = null;
        
        Transform teme2EME2000 = null;
        CartesianState cart = null;
        int size = carts.size();
        List<CartesianState> out = new ArrayList<CartesianState>(size);
        for(int i=0; i<size; i++)
        {
	        epoch = toAD(cart.getEpoch());
	        teme2EME2000 = teme.getTransformTo(eme2000, epoch);
	        
	        cart = doTransform(teme2EME2000,cart);
	        out.add(cart);
        }
        
        return out;
    }


    public static CartesianState EME20002teme(CartesianState cart)
    {
        Frame eme2000 = FramesFactory.getEME2000();
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = toAD(cart.getEpoch());
        Transform EME20002teme = null;
        EME20002teme = eme2000.getTransformTo(teme, epoch);
        
        return doTransform(EME20002teme,cart);
    }
    
    public static List<CartesianState> EME20002teme(List<CartesianState> carts)
    {
        Frame eme2000 = FramesFactory.getEME2000();
        Frame teme = FramesFactory.getTEME();

        AbsoluteDate epoch = null;
        
        Transform EME20002teme = null;
        CartesianState cart = null;
        int size = carts.size();
        List<CartesianState> out = new ArrayList<CartesianState>(size);
        for(int i=0; i<size; i++)
        {
	        epoch = toAD(cart.getEpoch());
	        EME20002teme = eme2000.getTransformTo(teme, epoch);
	        
	        cart = doTransform(EME20002teme,cart);
	        out.add(cart);
        }
        
        return out;
    }

    public static AbsoluteDate toAD(Date date)
    {
        DateUtil.setDefaultTimeZone();
        GregorianCalendar gc = new GregorianCalendar();
    	gc.setTime(date);
    	double sec = ((double)gc.get(Calendar.SECOND))+gc.get(Calendar.MILLISECOND)/1000.0d;
        AbsoluteDate epoch = new AbsoluteDate(gc.get(Calendar.YEAR),gc.get(Calendar.MONTH)+1,gc.get(Calendar.DAY_OF_MONTH), 
        		gc.get(Calendar.HOUR_OF_DAY),gc.get(Calendar.MINUTE),sec,
        		TimeScalesFactory.getUTC());
    	return epoch;
    }

}
