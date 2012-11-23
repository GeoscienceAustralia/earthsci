package au.gov.ga.earthsci.worldwind.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for the {@link Util} class
 */
public class UtilTest
{
	private static final double ALLOWABLE_DOUBLE_ERROR = 0.0001;

	// computeVec4FromString()

	@Test
	public void testComputeVec4FromStringWithNull() throws Exception
	{
		String vectorString = null;

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNull(result);
	}

	@Test
	public void testComputeVec4FromStringWithBlank() throws Exception
	{
		String vectorString = "";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNull(result);
	}

	@Test
	public void testComputeVec4FromStringWith3VecBracesWhitespace() throws Exception
	{
		String vectorString = "(1, 2, 3)";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNotNull(result);
		assertEquals(1, result.x, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(2, result.y, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(3, result.z, ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testComputeVec4FromStringWith4VecNoBracesNoWhitespace() throws Exception
	{
		String vectorString = "1,2,3,4";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNotNull(result);
		assertEquals(1, result.x, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(2, result.y, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(3, result.z, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(4, result.w, ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testComputeVec4FromStringWith4VecWhitespaceSeparators() throws Exception
	{
		String vectorString = "1 2 -3.3 0.04";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNotNull(result);
		assertEquals(1, result.x, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(2, result.y, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(-3.3, result.z, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(0.04, result.w, ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testComputeVec4FromStringWith2Vec() throws Exception
	{
		String vectorString = "(1, 2)";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNull(result);
	}

	@Test
	public void testComputeVec4FromStringWith5Vec() throws Exception
	{
		String vectorString = "(1, 2, 3, 4, 5)";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNull(result);
	}

	@Test
	public void testComputeVec4FromStringWithInvalidString() throws Exception
	{
		String vectorString = "(1, a, 3)";

		Vec4 result = Util.computeVec4FromString(vectorString);

		assertNull(result);
	}

	// clamp() number methods

	@Test
	public void testClampWithIntegerValueLessThanMin()
	{
		assertEquals(5, Util.clamp(4, 5, 10));
	}

	@Test
	public void testClampWithIntegerValueEqualMin()
	{
		assertEquals(5, Util.clamp(5, 5, 10));
	}

	@Test
	public void testClampWithIntegerValueInRange()
	{
		assertEquals(7, Util.clamp(7, 5, 10));
	}

	@Test
	public void testClampWithIntegerValueEqualMax()
	{
		assertEquals(10, Util.clamp(10, 5, 10));
	}

	@Test
	public void testClampWithIntegerValueGreaterThanMax()
	{
		assertEquals(10, Util.clamp(11, 5, 10));
	}

	@Test
	public void testClampWithIntegerValueMinEqualMax()
	{
		assertEquals(10, Util.clamp(11, 10, 10));
	}

	@Test
	public void testClampWithIntegerValueMinGreaterThanMax()
	{
		assertEquals(9, Util.clamp(7, 10, 9));
	}

	@Test
	public void testClampWithDoubleValueLessThanMin()
	{
		assertEquals(5d, Util.clamp(4.9d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testClampWithDoubleValueEqualMin()
	{
		assertEquals(5d, Util.clamp(5d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testClampWithDoubleValueInRange()
	{
		assertEquals(7d, Util.clamp(7d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testClampWithDoubleValueEqualMax()
	{
		assertEquals(10d, Util.clamp(10d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testClampWithDoubleValueGreaterThanMax()
	{
		assertEquals(10d, Util.clamp(10.1d, 5d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testClampWithDoubleValueMinEqualMax()
	{
		assertEquals(10d, Util.clamp(11d, 10d, 10d), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testClampWithDoubleValueMinGreaterThanMax()
	{
		assertEquals(9d, Util.clamp(7d, 10d, 9d), ALLOWABLE_DOUBLE_ERROR);
	}

	// clampLatLon()

	@Test
	public void testClampLatLonWithNullLatLon()
	{
		LatLon source = null;
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		LatLon result = Util.clampLatLon(source, extents);

		assertNull(result);
	}

	@Test
	public void testClampLatLonWithNullExtents()
	{
		LatLon source = new LatLon(Angle.NEG90, Angle.NEG90);
		Sector extents = null;

		LatLon result = Util.clampLatLon(source, extents);

		assertEquals(source, result);
	}

	@Test
	public void testClampLatLonWithSourceWithinExtents()
	{
		LatLon source = new LatLon(Angle.fromDegrees(-77.345), Angle.fromDegrees(12.56));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		LatLon result = Util.clampLatLon(source, extents);

		assertEquals(source, result);
	}

	@Test
	public void testClampLatLonWithSourceOutsideExtentsLat()
	{
		LatLon source = new LatLon(Angle.fromDegrees(-97.345), Angle.fromDegrees(12.56));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		LatLon result = Util.clampLatLon(source, extents);

		assertEquals(new LatLon(extents.getMinLatitude(), source.longitude), result);
	}

	@Test
	public void testClampLatLonWithSourceOutsideExtentsLon()
	{
		LatLon source = new LatLon(Angle.fromDegrees(-77.345), Angle.fromDegrees(112.56));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		LatLon result = Util.clampLatLon(source, extents);

		assertEquals(new LatLon(source.latitude, extents.getMaxLongitude()), result);
	}

	// clampSector()

	@Test
	public void testClampSectorWithNullSource()
	{
		Sector source = null;
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		Sector result = Util.clampSector(source, extents);

		assertEquals(null, result);
	}

	@Test
	public void testClampSectorWithNullExtents()
	{
		Sector source =
				new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(56.66), Angle.fromDegrees(1.01),
						Angle.fromDegrees(87.05));
		Sector extents = null;

		Sector result = Util.clampSector(source, extents);

		assertEquals(source, result);
	}

	@Test
	public void testClampSectorWithSourceWithinExtents()
	{
		Sector source =
				new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(56.66), Angle.fromDegrees(1.01),
						Angle.fromDegrees(87.05));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		Sector result = Util.clampSector(source, extents);

		assertEquals(source, result);
	}

	@Test
	public void testClampSectorWithSourceOverlapExtents()
	{
		Sector source =
				new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(96.66), Angle.fromDegrees(1.01),
						Angle.fromDegrees(97.05));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		Sector result = Util.clampSector(source, extents);

		Sector expected =
				new Sector(Angle.fromDegrees(-12.34), Angle.fromDegrees(90), Angle.fromDegrees(1.01),
						Angle.fromDegrees(90));

		assertEquals(expected, result);
	}

	@Test
	public void testClampSectorWithSourceOutsideExtents()
	{
		Sector source =
				new Sector(Angle.fromDegrees(91.34), Angle.fromDegrees(96.66), Angle.fromDegrees(91.01),
						Angle.fromDegrees(97.05));
		Sector extents = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG90, Angle.POS90);

		Sector result = Util.clampSector(source, extents);

		Sector expected =
				new Sector(Angle.fromDegrees(90), Angle.fromDegrees(90), Angle.fromDegrees(90), Angle.fromDegrees(90));

		assertEquals(expected, result);
	}

	// capitalizeFirstLetter()
	@Test
	public void testCapitalizeFirstLetterWithNull()
	{
		assertEquals(null, Util.capitalizeFirstLetter(null));
	}

	@Test
	public void testCapitalizeFirstLetterWithEmpty()
	{
		assertEquals("", Util.capitalizeFirstLetter(""));
	}

	@Test
	public void testCapitalizeFirstLetterWithBlank()
	{
		assertEquals("   ", Util.capitalizeFirstLetter("   "));
	}

	@Test
	public void testCapitalizeFirstLetterWithAlpha()
	{
		assertEquals("The Wonderful STRING", Util.capitalizeFirstLetter("the Wonderful STRING"));
	}

	@Test
	public void testCapitalizeFirstLetterWithNumeric()
	{
		assertEquals("123 456", Util.capitalizeFirstLetter("123 456"));
	}

	// isBlank()
	@Test
	public void testIsBlankWithNull()
	{
		assertEquals(true, Util.isBlank(null));
	}

	@Test
	public void testIsBlankWithEmpty()
	{
		assertEquals(true, Util.isBlank(""));
	}

	@Test
	public void testIsBlankWithWhitespaceOnly()
	{
		assertEquals(true, Util.isBlank(" \t\r"));
	}

	@Test
	public void testIsBlankWithMixed()
	{
		assertEquals(false, Util.isBlank(" \t\r  t"));
	}

	// isEmpty()
	@Test
	public void testIsEmptyWithNull()
	{
		Collection<Object> collection = null;

		assertEquals(true, Util.isEmpty(collection));
	}

	@Test
	public void testIsEmptyWithEmpty()
	{
		Collection<Object> collection = Collections.emptyList();

		assertEquals(true, Util.isEmpty(collection));
	}

	@Test
	public void testIsEmptyWithSingleElement()
	{
		Collection<Object> collection = Arrays.asList(new Object[] { "item" });

		assertEquals(false, Util.isEmpty(collection));
	}

	@Test
	public void testIsEmptyWithMap()
	{
		Map<Double, Integer> collection = new HashMap<Double, Integer>();

		assertEquals(true, Util.isEmpty(collection));
	}

	// paddedInt()

	@Test
	public void testPaddedIntWithIntLengthLessThanCharCount()
	{
		assertEquals("0012", Util.paddedInt(12, 4));
	}

	@Test
	public void testPaddedIntWithIntLengthEqualToCharCount()
	{
		assertEquals("1223", Util.paddedInt(1223, 4));
	}

	@Test
	public void testPaddedIntWithIntLengthGreaterThanCharCount()
	{
		assertEquals("12235", Util.paddedInt(12235, 4));
	}

	//randomString()
	@Test
	public void testRandomStringWithZeroLength()
	{
		String result = Util.randomString(0);

		assertEquals(0, result.length());
		assertEquals(0, result.replaceAll("[a-zA-Z]", "").length());
	}

	@Test
	public void testRandomStringWithNonZeroLength()
	{
		String result = Util.randomString(10);

		assertEquals(10, result.length());
		assertEquals(0, result.replaceAll("[a-zA-Z]", "").length());
	}

	//mixDouble()
	@Test
	public void testMixDouble5050()
	{
		assertEquals(30, Util.mixDouble(0.5, 10, 50), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testMixDouble2575()
	{
		assertEquals(20, Util.mixDouble(0.25, 10, 50), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testMixDouble7525()
	{
		assertEquals(40, Util.mixDouble(0.75, 10, 50), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testMixDoubleAmountLessThanZero()
	{
		assertEquals(10, Util.mixDouble(-1, 10, 50), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testMixDoubleAmountGreaterThanOne()
	{
		assertEquals(50, Util.mixDouble(2, 10, 50), ALLOWABLE_DOUBLE_ERROR);
	}

	// percentDouble()
	@Test
	public void testPercentDoubleLessThanMin()
	{
		assertEquals(0, Util.percentDouble(-1, 0.0, 100.0), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testPercentDoubleGreaterThanMax()
	{
		assertEquals(1.0, Util.percentDouble(101, 0.0, 100.0), ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testPercentDoubleInRange()
	{
		assertEquals(0.55, Util.percentDouble(55, 0.0, 100.0), ALLOWABLE_DOUBLE_ERROR);
	}

	// computeLatLonFromString()
	@Test
	public void testComputeLatLonFromStringWithNull()
	{
		assertEquals(null, Util.computeLatLonFromString(null));
	}

	@Test
	public void testComputeLatLonFromStringWithBlank()
	{
		assertEquals(null, Util.computeLatLonFromString(""));
	}

	@Test
	public void testComputeLatLonFromStringWithInvalid()
	{
		assertEquals(null, Util.computeLatLonFromString("abc"));
	}

	@Test
	public void testComputeLatLonFromStringWithValidSignedDecimalDegrees()
	{
		LatLon result = Util.computeLatLonFromString("-87.345, 123.45");
		LatLon expected = new LatLon(Angle.fromDegrees(-87.345), Angle.fromDegrees(123.45));
		assertEquals(expected, result);
	}

	@Test
	public void testComputeLatLonFromStringWithValidEWNSDecimalDegrees()
	{
		LatLon result = Util.computeLatLonFromString("87.345S, 123.45E");
		LatLon expected = new LatLon(Angle.fromDegrees(-87.345), Angle.fromDegrees(123.45));
		assertEquals(expected, result);
	}

	@Test
	public void testComputeLatLonFromStringWithInvalidDecimalDegrees()
	{
		LatLon result = Util.computeLatLonFromString("-90.345, 123.45");
		LatLon expected = null;
		assertEquals(expected, result);
	}

	@Test
	public void testComputeLatLonFromStringWithValidSignedDegMinSec()
	{
		LatLon result = Util.computeLatLonFromString("-87\u00B0 23' 33\", 123\u00B0 45' 56\"");
		LatLon expected = new LatLon(Angle.fromDegrees(-87.3925), Angle.fromDegrees(123.7656));

		assertEquals(expected.latitude.degrees, result.latitude.degrees, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(expected.longitude.degrees, result.longitude.degrees, ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testComputeLatLonFromStringWithValidEWNSDegMinSec()
	{
		LatLon result = Util.computeLatLonFromString("87\u00B0 23' 33\"S, 123\u00B0 45' 56\"E");
		LatLon expected = new LatLon(Angle.fromDegrees(-87.3925), Angle.fromDegrees(123.7656));

		assertEquals(expected.latitude.degrees, result.latitude.degrees, ALLOWABLE_DOUBLE_ERROR);
		assertEquals(expected.longitude.degrees, result.longitude.degrees, ALLOWABLE_DOUBLE_ERROR);
	}

	@Test
	public void testComputeLatLonFromStringWithInvalidDegMinSec()
	{
		LatLon result = Util.computeLatLonFromString("-90° 23' 33\", 123° 45' 56\"");

		assertNull(result);
	}

}
