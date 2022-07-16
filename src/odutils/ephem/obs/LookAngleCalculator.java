package odutils.ephem.obs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.frames.Transform;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

import odutils.ephem.CartesianSource;
import odutils.ephem.CartesianState;
import odutils.ephem.Ephemerides;
import odutils.ephem.EphemerisUtil;
import odutils.ephem.OrekitUtils;
import sgp4.TLE;

public class LookAngleCalculator 
{
	static
	{
		OrekitUtils.checkInit();
	}

	public static List<LookAngleState> computeLookAngles(double latDeg, double lonDeg, double altM, Ephemerides eph, Date d1, Date d2, double tStepSec, String frame)
	{
		long t1 = d1.getTime();
		long t2 = d2.getTime();
		long tstep = (long)(1000.0d*tStepSec);
		int est = (int)((t2-t1)/tstep)+1;
		
		List<CartesianState> carts = new ArrayList<CartesianState>(est);
		Date d = null;
		while(t1<=t2)
		{
			d = new java.sql.Timestamp(t1);
			carts.add(eph.getCartesian(d));
			
			t1+=tstep;
		}
		return computeLookAngles(latDeg,lonDeg,altM,carts,frame);
	}

	public static LookAngleState computeLookAngle(double latDeg, double lonDeg, double altM, CartesianState cart, String frame)
	{
		List<LookAngleState> las = null;
		List<CartesianState> carts = new ArrayList<CartesianState>();
		carts.add(cart);
		las = computeLookAngles(latDeg,lonDeg,altM,carts,frame);
		
		return las.get(0);
	}
	
	public static List<LookAngleState> computeLookAngles(double latDeg, double lonDeg, double altM, CartesianSource src, Date d1, Date d2, double tStepSec, String frame)
	{
		long t1 = d1.getTime();
		long t2 = d2.getTime();
		long tstep = (long)(1000.0d*tStepSec);
		int est = (int)((t2-t1)/tstep)+1;
		
		List<CartesianState> carts = new ArrayList<CartesianState>(est);
		Date d = null;
		while(t1<=t2)
		{
			d = new java.sql.Timestamp(t1);
			carts.add(src.getCartesian(d));
			
			t1+=tstep;
		}
		return computeLookAngles(latDeg,lonDeg,altM,carts,frame);
	}
	
	public static List<LookAngleState> computeLookAngles(double latDeg, double lonDeg, double altM, TLE tle, Date d1, Date d2, double tStepSec)
	{
		List<CartesianState> carts = EphemerisUtil.getCarts(tle, d1, d2, tStepSec, false);
		
		return computeLookAngles(latDeg,lonDeg,altM,carts,"teme");
	}
	
	/**
	 * Compute az/el, ra/dec, range/rangeRate for the given cartesianstates relative to the specific location.  The frame specifies if the carts are J2000 (EME2000), ECEF, or TEME.
	 * 
	 * @param latDeg
	 * @param lonDeg
	 * @param altM
	 * @param carts
	 * @param frame
	 * @return
	 */
	public static List<LookAngleState> computeLookAngles(double latDeg, double lonDeg, double altM, List<CartesianState> carts, String frame)
	{
		int size = carts.size();
		List<LookAngleState> angles = new ArrayList<LookAngleState>(size);

		Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		Frame eme2000 = FramesFactory.getEME2000();

		BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING,
				itrf);

		GeodeticPoint loc = new GeodeticPoint(Math.toRadians(latDeg),Math.toRadians(lonDeg),altM);
		TopocentricFrame locFrame = new TopocentricFrame(earth, loc, "topo");

		
		// establish cartesian frame
		Frame cartFrame = null;
		if(frame == null) frame = "teme";
		frame = frame.toLowerCase();

		if(frame.equals("j2000") || frame.equals("eme2000"))
		{
			cartFrame = FramesFactory.getEME2000();
		}
		else if(frame.equals("teme"))
		{
			cartFrame = FramesFactory.getTEME();
		}
		else if(frame.equals("ecef") || frame.equals("itrf"))
		{
			cartFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		}
		
		// We can simply get the position and velocity of spacecraft in station frame at any time
	    PVCoordinates pvCart   = null;
	    PVCoordinates pvStationFrame = null;
	    Transform trans = null;
	    
	    Date epoch = null;
	    AbsoluteDate adEpoch = null;
	    
	    CartesianState cart = null;
	    LookAngleState las = null;
	    double doppler = 0;
	    Vector3D pos = null;
	    Vector3D zeroVel = new Vector3D(0,0,0);
	    PVCoordinates pvLocECEF = null;
	    PVCoordinates pvLocEME2000 = null;
	    PVCoordinates pvCartEME2000 = null;
    	pos = earth.transform(loc);
    	pvLocECEF = new PVCoordinates(pos,zeroVel);

    	Vector3D p = null;
    	Vector3D v = null;
    	
	    for(int i=0; i<size; i++)
	    {
	    	cart = carts.get(i);
	    	epoch = cart.epoch;
	    	adEpoch = OrekitUtils.toAD(epoch);
	    	
	    	pvCart = OrekitUtils.toPV(cart);
	    	trans = cartFrame.getTransformTo(locFrame,adEpoch);
	    	
	    	pvStationFrame = trans.transformPVCoordinates(pvCart);
	    	
	    	pos = pvStationFrame.getPosition();
	    	
	    	las = new LookAngleState();
	    	las.epoch = new java.sql.Timestamp(epoch.getTime());
	    	las.rangeKM = pos.getNorm()/1000.0d; // to km
	    	las.latDeg = latDeg;
	    	las.lonDeg = lonDeg;
	    	las.altM = altM;
	    	
	    	doppler = Vector3D.dotProduct(pos, pvStationFrame.getVelocity()) / pos.getNorm();

	    	las.rangeRateKMPerSec = doppler/1000.0d; // to km/s
	    	
	    	las.azDeg = Math.toDegrees(pos.getAlpha());
	    	las.elDeg = Math.toDegrees(pos.getDelta());

	    	
	    	angles.add(las);
	    	
	    	// do ra/dec
	    	pvCartEME2000 = cartFrame.getTransformTo(eme2000, adEpoch).transformPVCoordinates(pvCart);
	    	pvLocEME2000 = itrf.getTransformTo(eme2000, adEpoch).transformPVCoordinates(pvLocECEF);
	    	
	    	p = pvCartEME2000.getPosition().subtract(pvLocEME2000.getPosition());
	    	las.setRaDeg(Math.toDegrees(p.getAlpha()));
	    	las.decDeg = Math.toDegrees(p.getDelta());
	    }

		return angles;
	}

	/**
	 * Save on transform costs if cartesianstates are all the same time.
	 * 
	 * @param latDeg
	 * @param lonDeg
	 * @param altM
	 * @param carts
	 * @param frame
	 * @param epoch
	 * @return
	 */
	public static List<LookAngleState> computeLookAnglesSameTime(double latDeg, double lonDeg, double altM, List<CartesianState> carts, String frame, Date epoch)
	{
		int size = carts.size();
		List<LookAngleState> angles = new ArrayList<LookAngleState>(size);

		Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		Frame eme2000 = FramesFactory.getEME2000();

		BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING,
				itrf);

		GeodeticPoint loc = new GeodeticPoint(Math.toRadians(latDeg),Math.toRadians(lonDeg),altM);
		TopocentricFrame locFrame = new TopocentricFrame(earth, loc, "topo");

		
		// establish cartesian frame
		Frame cartFrame = null;
		if(frame == null) frame = "teme";
		frame = frame.toLowerCase();

		if(frame.equals("j2000") || frame.equals("eme2000"))
		{
			cartFrame = FramesFactory.getEME2000();
		}
		else if(frame.equals("teme"))
		{
			cartFrame = FramesFactory.getTEME();
		}
		else if(frame.equals("ecef") || frame.equals("itrf"))
		{
			cartFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		}
		
		// We can simply get the position and velocity of spacecraft in station frame at any time
	    PVCoordinates pvCart   = null;
	    PVCoordinates pvStationFrame = null;
	    Transform trans = null;
	    Transform transCart = null;
	    Transform transLoc = null;
	    
	    AbsoluteDate adEpoch = OrekitUtils.toAD(epoch);
    	
	    
	    CartesianState cart = null;
	    LookAngleState las = null;
	    double doppler = 0;
	    Vector3D pos = null;
	    Vector3D zeroVel = new Vector3D(0,0,0);
	    PVCoordinates pvLocECEF = null;
	    PVCoordinates pvLocEME2000 = null;
	    PVCoordinates pvCartEME2000 = null;
    	pos = earth.transform(loc);
    	pvLocECEF = new PVCoordinates(pos,zeroVel);

    	Vector3D p = null;
    	Vector3D v = null;

    	trans = cartFrame.getTransformTo(locFrame,adEpoch);
    	transCart = cartFrame.getTransformTo(eme2000, adEpoch);
    	transLoc = itrf.getTransformTo(eme2000, adEpoch);

	    for(int i=0; i<size; i++)
	    {
	    	cart = carts.get(i);
	    	epoch = cart.epoch;
	    	
	    	pvCart = OrekitUtils.toPV(cart);
	    	
	    	pvStationFrame = trans.transformPVCoordinates(pvCart);
	    	
	    	pos = pvStationFrame.getPosition();
	    	
	    	las = new LookAngleState();
	    	las.epoch = new java.sql.Timestamp(epoch.getTime());
	    	las.rangeKM = pos.getNorm()/1000.0d; // to km
	    	las.latDeg = latDeg;
	    	las.lonDeg = lonDeg;
	    	las.altM = altM;
	    	
	    	doppler = Vector3D.dotProduct(pos, pvStationFrame.getVelocity()) / pos.getNorm();

	    	las.rangeRateKMPerSec = doppler/1000.0d; // to km/s
	    	
	    	las.azDeg = Math.toDegrees(pos.getAlpha());
	    	las.elDeg = Math.toDegrees(pos.getDelta());
	    	
	    	angles.add(las);
	    	
	    	// do ra/dec
	    	pvCartEME2000 = transCart.transformPVCoordinates(pvCart);
	    	pvLocEME2000 = transLoc.transformPVCoordinates(pvLocECEF);
	    	
	    	p = pvCartEME2000.getPosition().subtract(pvLocEME2000.getPosition());
	    	las.setRaDeg(Math.toDegrees(p.getAlpha()));
	    	las.decDeg = Math.toDegrees(p.getDelta());
	    }
	    		
		return angles;
	}

	/**
	 * Compute the transits over the given location.
	 * 
	 * @param latDeg
	 * @param lonDeg
	 * @param altM
	 * @param tle
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static List<Transit> computeTransits(double latDeg, double lonDeg, double altM, TLE tle, Date d1, Date d2, double minElDeg)
	{
		List<CartesianState> carts = EphemerisUtil.getCarts(tle, d1, d2, 30.0d, false);

		return computeTransits(latDeg,lonDeg,altM,carts,"teme",d1,d2,minElDeg);
	}
	
	/**
	 * Compute the transits over the given location.
	 * 
	 * @param latDeg
	 * @param lonDeg
	 * @param altM
	 * @param carts
	 * @param frame
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static List<Transit> computeTransits(double latDeg, double lonDeg, double altM, List<CartesianState> carts, String frame, Date d1, Date d2, double minElDeg)
	{
		List<Transit> list = new ArrayList<Transit>();
		
		Ephemerides eph = new Ephemerides(carts,"transits");
		
		List<LookAngleState> angles = computeLookAngles(latDeg, lonDeg, altM, carts, frame);
		List<LookAngleState> angles2 = null;
		
		LookAngleState la1 = null;
		LookAngleState la2 = null;
		LookAngleState laSpecial = null;
		double mult = 0;
		
		la2 = angles.get(0);
		int size = angles.size();
		
		Transit currentTrans = null;
		
		if((la2.elDeg-minElDeg) >= 0)
		{
			currentTrans = new Transit();
			currentTrans.setAOSAngles(la2);
		}
		
		for(int i=1; i<size; i++)
		{
			la1 = la2;
			
			la2 = angles.get(i);
			
			mult = (la1.elDeg-minElDeg)*(la2.elDeg-minElDeg);
			
			if(mult > 0)
			{
				// see if the rate had a sign change
				//mult = la1.elDegPerSec * la2.elDegPerSec;
			}
			
			if(mult <= 0)
			{
				laSpecial = findElRoot(latDeg,lonDeg,altM,la1.epoch,la2.epoch,eph,frame,minElDeg);

				if(currentTrans == null)
				{
					// we found a start point
					currentTrans = new Transit();
					
					currentTrans.setAOSAngles(laSpecial);
				}
				else
				{
					// we found an end
					currentTrans.setLOSAngles(laSpecial);
					list.add(currentTrans);
					
					if(currentTrans.getTCAAngles() == null)
					{
						//System.err.println("oh no, tca is null");
						laSpecial = findTCA(latDeg,lonDeg,altM,currentTrans.getStartDate(),currentTrans.getStopDate(),eph,frame);
						if(laSpecial == null)
						{
							laSpecial = currentTrans.getAOSAngles();
							if(currentTrans.getLOSAngles().rangeKM<laSpecial.rangeKM)
							{
								laSpecial = currentTrans.getLOSAngles();
							}
						}
						currentTrans.setTCAAngles(laSpecial);
					}
					
					currentTrans = null;
				}
				//System.out.println(la1.epoch+"\t"+la1.elDeg + "\t" + la2.epoch + "\t"+la2.elDeg + "\t"+la1.elDegPerSec+"\t"+la2.elDegPerSec);
			}
			else if(currentTrans != null)
			{
				mult = la1.rangeRateKMPerSec*la2.rangeRateKMPerSec;
				if(mult <= 0)
				{
					laSpecial = findTCA(latDeg,lonDeg,altM,la1.epoch,la2.epoch,eph,frame);

					currentTrans.setTCAAngles(laSpecial);
				}
			}
		}
		
		if(currentTrans != null)
		{
			// we found an end
			currentTrans.setLOSAngles(la2);
			list.add(currentTrans);
			
			currentTrans = null;
		}
		
		return list;
	}

	public static double[] computeGeoCoords(CartesianState cart, String frame)
	{
		List<CartesianState> carts = new ArrayList<CartesianState>();
		carts.add(cart);
		List<double[]> vals = computeGeoCoords(carts,frame);
		
		return vals.get(0);
	}
	
	public static List<double[]> computeGeoCoords(List<CartesianState> carts, String frame)
	{
		int size = carts.size();
		List<double[]> out = new ArrayList<double[]>(size);

		Frame itrf = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		Frame eme2000 = FramesFactory.getEME2000();

		BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
				Constants.WGS84_EARTH_FLATTENING,
				itrf);
		
		// establish cartesian frame
		Frame cartFrame = null;
		if(frame == null) frame = "teme";
		frame = frame.toLowerCase();

		if(frame.equals("j2000") || frame.equals("eme2000"))
		{
			cartFrame = FramesFactory.getEME2000();
		}
		else if(frame.equals("teme"))
		{
			cartFrame = FramesFactory.getTEME();
		}
		else if(frame.equals("ecef") || frame.equals("itrf"))
		{
			cartFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
		}
		
    	CartesianState cart = null;
    	Date epoch = null;
    	AbsoluteDate adEpoch = null;
	    PVCoordinates pvCart   = null;
	    GeodeticPoint geo = null;
	    for(int i=0; i<size; i++)
	    {
	    	cart = carts.get(i);
	    	epoch = cart.epoch;
	    	adEpoch = OrekitUtils.toAD(epoch);
	    	pvCart = OrekitUtils.toPV(cart);

	    	geo = earth.transform(pvCart.getPosition(), cartFrame, adEpoch);
	    	
	    	out.add(new double[]{geo.getLatitude(),geo.getLongitude(),geo.getAltitude()});
	    }

		return out;
	}
	
	/**
	 * Group the list of angles by the same location.
	 * 
	 * @param list
	 * @return
	 */
	public static List<List<LookAngleState>> groupByLocation(List<LookAngleState> list)
	{
		Map<String,List<LookAngleState>> m = new HashMap<String,List<LookAngleState>>();
		String key = null;
		LookAngleState la = null;
		int size = list.size();
		List<LookAngleState> tmp = null;
		
		for(int i=0; i<size; i++)
		{
			la = list.get(i);
			key = la.latDeg+"_"+la.lonDeg+"_"+la.altM;
			tmp = m.get(key);
			if(tmp == null)
			{
				tmp = new ArrayList<LookAngleState>(size);
				m.put(key, tmp);
			}
			tmp.add(la);
		}

		return new ArrayList<List<LookAngleState>>(m.values());
	}

	protected static LookAngleState findElRoot(double latDeg, double lonDeg, double altM, Date d1, Date d2, Ephemerides eph, String frame, double minElDeg)
	{
		boolean digDeep = true;
		double tStepSec = 1;
		
		double diff = d2.getTime()-d1.getTime();
		if(diff > 1000)
		{
			tStepSec = 1;
		}
		else 
		{
			tStepSec = diff/1000/20;
		}
		
		if(diff <= 50)
		{
			digDeep = false;
		}
		
		//System.err.println(digDeep + "\t" + tStepSec + "\t" + diff);
				
		List<LookAngleState> list = computeLookAngles(latDeg,lonDeg,altM,eph,d1,d2,tStepSec,frame);
		
		LookAngleState la1 = null;
		LookAngleState la2 = null;
		LookAngleState laSpecial = null;

		double mult = 0;
		
		la2 = list.get(0);
		int size = list.size();
		
		for(int i=1; i<size; i++)
		{
			la1 = la2;
			
			la2 = list.get(i);
			
			mult = (la1.elDeg-minElDeg)*(la2.elDeg-minElDeg);
	
			if(mult <= 0)
			{
				if(digDeep)
				{
					laSpecial = findElRoot(latDeg, lonDeg, altM, la1.epoch,la2.epoch, eph, frame, minElDeg);
				}
				else
				{
					laSpecial = la2;
				}
				break;
				
			}
		}
		
		return laSpecial;
	}
	
	protected static LookAngleState findTCA(double latDeg, double lonDeg, double altM, Date d1, Date d2, Ephemerides eph, String frame)
	{
		
		boolean digDeep = true;
		double tStepSec = 1;
		
		double diff = d2.getTime()-d1.getTime();
		if(diff > 1000)
		{
			tStepSec = 1;
		}
		else 
		{
			tStepSec = diff/1000/20;
		}
		
		if(diff <= 50)
		{
			digDeep = false;
		}
		
		List<LookAngleState> list = computeLookAngles(latDeg,lonDeg,altM,eph,d1,d2,tStepSec,frame);
		
		LookAngleState la1 = null;
		LookAngleState la2 = null;
		LookAngleState laSpecial = null;

		double mult = 0;
		
		la2 = list.get(0);
		int size = list.size();
		
		for(int i=1; i<size; i++)
		{
			la1 = la2;
			
			la2 = list.get(i);
			
			mult = la1.rangeRateKMPerSec*la2.rangeRateKMPerSec;
	
			if(mult <= 0)
			{
				if(digDeep)
				{
					laSpecial = findTCA(latDeg, lonDeg, altM, la1.epoch,la2.epoch, eph, frame);
				}
				else
				{
					laSpecial = la2;
				}
				break;
			}
		}
		
		return laSpecial;
	}
	
	protected static void dumpAngles(double latDeg, double lonDeg, double altM, Date d1, Date d2, Ephemerides eph, String frame)
	{
		
		boolean digDeep = true;
		double tStepSec = 1;
		
		double diff = d2.getTime()-d1.getTime();
		if(diff > 1000)
		{
			tStepSec = 1;
		}
		else 
		{
			tStepSec = diff/1000/20;
		}
		
		if(diff <= 50)
		{
			digDeep = false;
		}
		
		List<LookAngleState> list = computeLookAngles(latDeg,lonDeg,altM,eph,d1,d2,tStepSec,frame);
		
		LookAngleState la1 = null;

		double mult = 0;
		
		int size = list.size();
		
		for(int i=0; i<size; i++)
		{
			la1 = list.get(i);
			System.out.println(la1.epoch+"\t"+la1.elDeg+"\t"+la1.rangeKM+"\t"+la1.rangeRateKMPerSec);
		}
		
	}

}
