package au.gov.ga.earthsci.worldwind.common.sun;

import gov.nasa.worldwind.geom.LatLon;

import java.util.Calendar;

/**
 * Compute sun position for a given date/time and longitude/latitude.
 * 
 * This is a simple Java port of the "PSA" solar positioning algorithm, as
 * documented in:
 * 
 * Blanco-Muriel et al.: Computing the Solar Vector. Solar Energy Vol 70 No 5 pp
 * 431-441. http://dx.doi.org/10.1016/S0038-092X(00)00156-0
 * 
 * According to the paper, "The algorithm allows .. the true solar vector to be
 * determined with an accuracy of 0.5 minutes of arc for the period 1999–2015."
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SunCalculator
{
	public static LatLon subsolarPoint(Calendar time)
	{
		// Main variables
		double elapsedJulianDays;
		double decimalHours;
		double eclipticLongitude;
		double eclipticObliquity;
		double rightAscension, declination;

		// Calculate difference in days between the current Julian Day 
		// and JD 2451545.0, which is noon 1 January 2000 Universal Time
		{
			// Calculate time of the day in UT decimal hours
			decimalHours = time.get(Calendar.HOUR_OF_DAY) +
					(time.get(Calendar.MINUTE) + time.get(Calendar.SECOND) / 60.0) / 60.0;
			// Calculate current Julian Day
			long aux1 = (time.get(Calendar.MONTH) - 14) / 12;
			long aux2 = (1461 * (time.get(Calendar.YEAR) + 4800 + aux1)) / 4
					+ (367 * (time.get(Calendar.MONTH) - 2 - 12 * aux1)) / 12
					- (3 * ((time.get(Calendar.YEAR) + 4900 + aux1) / 100)) / 4
					+ time.get(Calendar.DAY_OF_MONTH) - 32075;
			double julianDate = (aux2) - 0.5 + decimalHours / 24.0;
			// Calculate difference between current Julian Day and JD 2451545.0 
			elapsedJulianDays = julianDate - 2451545.0;
		}

		// Calculate ecliptic coordinates (ecliptic longitude and obliquity of the 
		// ecliptic in radians but without limiting the angle to be less than 2*Pi 
		// (i.e., the result may be greater than 2*Pi)
		{
			double omega = 2.1429 - 0.0010394594 * elapsedJulianDays;
			double meanLongitude = 4.8950630 + 0.017202791698 * elapsedJulianDays; // Radians
			double meanAnomaly = 6.2400600 + 0.0172019699 * elapsedJulianDays;
			eclipticLongitude = meanLongitude + 0.03341607 * Math.sin(meanAnomaly) +
					0.00034894 * Math.sin(2 * meanAnomaly) - 0.0001134 - 0.0000203 * Math.sin(omega);
			eclipticObliquity = 0.4090928 - 6.2140e-9 * elapsedJulianDays + 0.0000396 * Math.cos(omega);
		}

		// Calculate celestial coordinates ( right ascension and declination ) in radians 
		// but without limiting the angle to be less than 2*Pi (i.e., the result may be 
		// greater than 2*Pi)
		{
			double sin_EclipticLongitude = Math.sin(eclipticLongitude);
			double dY = Math.cos(eclipticObliquity) * sin_EclipticLongitude;
			double dX = Math.cos(eclipticLongitude);
			rightAscension = Math.atan2(dY, dX);
			declination = Math.asin(Math.sin(eclipticObliquity) * sin_EclipticLongitude);
		}

		double TWO_PI = Math.PI * 2.0;
		double greenwichMeanSiderealTime = 6.6974243242 + 0.0657098283 * elapsedJulianDays + decimalHours;
		double longitude = rightAscension - (Math.toRadians(greenwichMeanSiderealTime * 15.0) % TWO_PI) + Math.PI;

		// Normalize longitude
		while (longitude > Math.PI)
		{
			longitude -= TWO_PI;
		}
		while (longitude <= -Math.PI)
		{
			longitude += TWO_PI;
		}

		return LatLon.fromRadians(declination, longitude);
	}
}
