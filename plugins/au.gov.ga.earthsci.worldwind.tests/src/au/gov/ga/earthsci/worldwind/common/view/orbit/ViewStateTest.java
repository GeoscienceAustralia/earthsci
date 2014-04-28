/*******************************************************************************
 * Copyright 2014 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.worldwind.common.view.orbit;

import static org.junit.Assert.assertEquals;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.ZeroElevationModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class ViewStateTest
{
	private static final double GLOBE_RADIUS = 50;
	private static final double SQRTHALF = Math.sqrt(0.5);
	private static final double EPSILON = 1e-9;
	private static Globe globe;
	private IViewState classUnderTest;

	@BeforeClass
	public static void setupClass()
	{
		globe = new EllipsoidalGlobe(GLOBE_RADIUS, GLOBE_RADIUS, 0, new ZeroElevationModel());
	}

	@Before
	public void setup()
	{
		classUnderTest = new ViewState();
	}

	@Test
	public void testInitialState()
	{
		Matrix expected = Matrix.fromTranslation(0, 0, -GLOBE_RADIUS - classUnderTest.getZoom());
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);
	}

	@Test
	public void testHeading()
	{
		classUnderTest.setHeading(Angle.fromDegrees(90));
		Matrix expected = new Matrix(
				0.0, 1.0, 0.0, 0.0,
				1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertEquals(90, classUnderTest.getHeading().degrees, EPSILON);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		expected = new Matrix(
				-SQRTHALF, SQRTHALF, 0.0, 0.0,
				SQRTHALF, SQRTHALF, 0.0, 0.0,
				0.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertEquals(45, classUnderTest.getHeading().degrees, EPSILON);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);
	}

	@Test
	public void testPitch()
	{
		classUnderTest.setPitch(Angle.fromDegrees(90));
		Matrix expected = new Matrix(
				-1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 1.0, 0.0,
				0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertEquals(90, classUnderTest.getPitch().degrees, EPSILON);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setPitch(Angle.fromDegrees(45));
		expected = new Matrix(
				-1.0, 0.0, 0.0, 0.0,
				0.0, SQRTHALF, SQRTHALF, 0.0,
				0.0, SQRTHALF, -SQRTHALF, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertEquals(45, classUnderTest.getPitch().degrees, EPSILON);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);
	}

	@Test
	public void testRoll()
	{
		classUnderTest.setRoll(Angle.fromDegrees(90));
		Matrix expected = new Matrix(
				0.0, -1.0, 0.0, 0.0,
				-1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertEquals(90, classUnderTest.getRoll().degrees, EPSILON);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setRoll(Angle.fromDegrees(45));
		expected = new Matrix(
				-SQRTHALF, -SQRTHALF, 0.0, 0.0,
				-SQRTHALF, SQRTHALF, 0.0, 0.0,
				0.0, 0.0, -1.0, -0.0,
				0.0, 0.0, 0.0, 1.0);
		assertEquals(45, classUnderTest.getRoll().degrees, EPSILON);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);
	}

	@Test
	public void testZoom()
	{
		double zoomTest = 10;
		classUnderTest.setZoom(zoomTest);
		assertEquals(zoomTest, classUnderTest.getZoom(), EPSILON);
		Matrix expected = Matrix.fromTranslation(0, 0, -globe.getRadius() - zoomTest);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		Vec4 eyePoint = classUnderTest.getEyePoint(globe);
		Vec4 centerPoint = classUnderTest.getCenterPoint(globe);
		assertEquals(zoomTest, eyePoint.distanceTo3(centerPoint), EPSILON);
	}

	@Test
	public void testCenter()
	{
		Vec4 expected = new Vec4(0.0, 0.0, GLOBE_RADIUS, 1.0);
		assertVec4Equals(expected, classUnderTest.getCenterPoint(globe), EPSILON);

		double elevation = 40;
		Position expectedPosition = Position.fromDegrees(45, 45, elevation);
		classUnderTest.setCenter(expectedPosition);
		expected = new Vec4((GLOBE_RADIUS + elevation) * 0.5, (GLOBE_RADIUS + elevation) * SQRTHALF,
				(GLOBE_RADIUS + elevation) * 0.5, 1.0);
		assertPositionEquals(expectedPosition, classUnderTest.getCenter(), EPSILON);
		assertVec4Equals(expected, classUnderTest.getCenterPoint(globe), EPSILON);
	}

	@Test
	public void testEye()
	{
		Vec4 expected = new Vec4(0.0, 0.0, GLOBE_RADIUS + classUnderTest.getZoom(), 1.0);
		assertVec4Equals(expected, classUnderTest.getEyePoint(globe), EPSILON);

		//changing the heading with pitch == 0 shouldn't change the eye point
		classUnderTest.setHeading(Angle.fromDegrees(90));
		assertVec4Equals(expected, classUnderTest.getEyePoint(globe), EPSILON);

		classUnderTest.setCenter(Position.fromDegrees(45, 45));
		classUnderTest.setZoom(20);
		assertPositionEquals(Position.fromDegrees(45, 45, classUnderTest.getZoom()), classUnderTest.getEye(globe),
				EPSILON);
		expected = new Vec4((GLOBE_RADIUS + classUnderTest.getZoom()) * 0.5, (GLOBE_RADIUS + classUnderTest.getZoom())
				* SQRTHALF, (GLOBE_RADIUS + classUnderTest.getZoom()) * 0.5, 1.0);
		assertVec4Equals(expected, classUnderTest.getEyePoint(globe), EPSILON);

		classUnderTest.setCenter(Position.ZERO);
		classUnderTest.setPitch(Angle.fromDegrees(90));
		expected = new Vec4(-classUnderTest.getZoom(), 0.0, GLOBE_RADIUS, 1.0);
		assertVec4Equals(expected, classUnderTest.getEyePoint(globe), EPSILON);

		classUnderTest.setZoom(10);
		expected = new Vec4(-classUnderTest.getZoom(), 0.0, GLOBE_RADIUS, 1.0);
		assertVec4Equals(expected, classUnderTest.getEyePoint(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		expected =
				new Vec4(-classUnderTest.getZoom() * SQRTHALF, -classUnderTest.getZoom() * SQRTHALF, GLOBE_RADIUS, 1.0);
		assertVec4Equals(expected, classUnderTest.getEyePoint(globe), EPSILON);

		Position expectedEye = Position.fromDegrees(50, 50, 50);
		classUnderTest.setEye(expectedEye, globe);
		assertPositionEquals(expectedEye, classUnderTest.getEye(globe), EPSILON);

		Position expectedCenter = Position.fromDegrees(40, 40, 0);
		classUnderTest.setCenter(expectedCenter);
		classUnderTest.setEye(expectedEye, globe);
		assertPositionEquals(expectedCenter, classUnderTest.getCenter(), EPSILON);
		assertPositionEquals(expectedEye, classUnderTest.getEye(globe), EPSILON);

		expectedCenter = Position.fromDegrees(50, 50, 0);
		Angle previousHeading = classUnderTest.getHeading();
		classUnderTest.setCenter(expectedCenter);
		classUnderTest.setEye(expectedEye, globe);
		assertPositionEquals(expectedCenter, classUnderTest.getCenter(), EPSILON);
		assertPositionEquals(expectedEye, classUnderTest.getEye(globe), EPSILON);
		assertEquals(0, classUnderTest.getPitch().degrees, EPSILON);
		assertEquals(previousHeading.degrees, classUnderTest.getHeading().degrees, EPSILON);

		expectedCenter = Position.fromDegrees(50, 50, 50);
		expectedEye = Position.fromDegrees(40, 40, 20);
		classUnderTest.setCenter(expectedCenter);
		classUnderTest.setEye(expectedEye, globe);
		assertPositionEquals(expectedCenter, classUnderTest.getCenter(), EPSILON);
		assertPositionEquals(expectedEye, classUnderTest.getEye(globe), EPSILON);
		Assert.assertTrue(classUnderTest.getPitch().degrees > 90);
	}

	@Test
	public void testUp()
	{
		Vec4 expected = new Vec4(0.0, 1.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getUp(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(90));
		expected = new Vec4(1.0, 0.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getUp(globe), EPSILON);

		classUnderTest.setPitch(Angle.fromDegrees(90));
		expected = new Vec4(0.0, 0.0, 1.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getUp(globe), EPSILON);

		classUnderTest.setRoll(Angle.fromDegrees(90));
		expected = new Vec4(0.0, 1.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getUp(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		classUnderTest.setPitch(Angle.fromDegrees(45));
		classUnderTest.setRoll(Angle.ZERO);
		expected = new Vec4(0.5, 0.5, SQRTHALF, 1.0);
		assertVec4Equals(expected, classUnderTest.getUp(globe), EPSILON);
	}

	@Test
	public void testSide()
	{
		Vec4 expected = new Vec4(-1.0, 0.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getSide(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(90));
		expected = new Vec4(0.0, 1.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getSide(globe), EPSILON);

		classUnderTest.setPitch(Angle.fromDegrees(90));
		expected = new Vec4(0.0, 1.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getSide(globe), EPSILON);

		classUnderTest.setRoll(Angle.fromDegrees(90));
		expected = new Vec4(0.0, 0.0, -1.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getSide(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		classUnderTest.setPitch(Angle.fromDegrees(45));
		classUnderTest.setRoll(Angle.ZERO);
		expected = new Vec4(-SQRTHALF, SQRTHALF, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getSide(globe), EPSILON);
	}

	@Test
	public void testForward()
	{
		Vec4 expected = new Vec4(0.0, 0.0, -1.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getForward(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(90));
		expected = new Vec4(0.0, 0.0, -1.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getForward(globe), EPSILON);

		classUnderTest.setPitch(Angle.fromDegrees(90));
		expected = new Vec4(1.0, 0.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getForward(globe), EPSILON);

		classUnderTest.setRoll(Angle.fromDegrees(90));
		expected = new Vec4(1.0, 0.0, 0.0, 1.0);
		assertVec4Equals(expected, classUnderTest.getForward(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		classUnderTest.setPitch(Angle.fromDegrees(45));
		classUnderTest.setRoll(Angle.ZERO);
		expected = new Vec4(0.5, 0.5, -SQRTHALF, 1.0);
		assertVec4Equals(expected, classUnderTest.getForward(globe), EPSILON);
	}

	@Test
	public void testRotation()
	{
		Matrix expected = new Matrix(
				-1.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(90));
		expected = new Matrix(
				0.0, 1.0, 0.0, 0.0,
				1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, -1.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setPitch(Angle.fromDegrees(90));
		expected = new Matrix(
				0.0, 0.0, 1.0, 0.0,
				1.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setRoll(Angle.fromDegrees(90));
		expected = new Matrix(
				0.0, 0.0, 1.0, 0.0,
				0.0, 1.0, 0.0, 0.0,
				-1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		classUnderTest.setPitch(Angle.fromDegrees(45));
		classUnderTest.setRoll(Angle.ZERO);
		expected = new Matrix(
				-SQRTHALF, 0.5, 0.5, 0.0,
				SQRTHALF, 0.5, 0.5, 0.0,
				0.0, SQRTHALF, -SQRTHALF, 0.0,
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getRotation(globe), EPSILON);
	}

	@Test
	public void testTransform()
	{
		Matrix expected = new Matrix(
				1.0, 0.0, 0.0, 0.0,
				0.0, 1.0, 0.0, 0.0,
				0.0, 0.0, 1.0, -GLOBE_RADIUS - classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(90));
		expected = new Matrix(
				0.0, -1.0, 0.0, 0.0,
				1.0, 0.0, 0.0, 0.0,
				0.0, 0.0, 1.0, -GLOBE_RADIUS - classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setPitch(Angle.fromDegrees(90));
		expected = new Matrix(
				0.0, -1.0, 0.0, 0.0,
				0.0, 0.0, 1.0, -GLOBE_RADIUS,
				-1.0, 0.0, 0.0, -classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setRoll(Angle.fromDegrees(90));
		expected = new Matrix(
				0.0, 0.0, 1.0, -GLOBE_RADIUS,
				0.0, 1.0, 0.0, 0.0,
				-1.0, 0.0, 0.0, -classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setHeading(Angle.fromDegrees(45));
		classUnderTest.setPitch(Angle.fromDegrees(45));
		classUnderTest.setRoll(Angle.ZERO);
		expected = new Matrix(
				SQRTHALF, -SQRTHALF, 0.0, 0.0,
				0.5, 0.5, SQRTHALF, -SQRTHALF * GLOBE_RADIUS,
				-0.5, -0.5, SQRTHALF, -SQRTHALF * GLOBE_RADIUS - classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setZoom(50);
		expected = new Matrix(
				SQRTHALF, -SQRTHALF, 0.0, 0.0,
				0.5, 0.5, SQRTHALF, -SQRTHALF * GLOBE_RADIUS,
				-0.5, -0.5, SQRTHALF, -SQRTHALF * GLOBE_RADIUS - classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setCenter(Position.fromDegrees(45, 45));
		expected = new Matrix(
				(SQRTHALF + 1.0) * 0.5, -0.5, (SQRTHALF - 1.0) * 0.5, 0.0,
				SQRTHALF - 0.25, (SQRTHALF + 1.0) * 0.5, -0.25, -SQRTHALF * GLOBE_RADIUS,
				0.25, (1.0 - SQRTHALF) * 0.5, SQRTHALF + 0.25, -SQRTHALF * GLOBE_RADIUS - classUnderTest.getZoom(),
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);

		classUnderTest.setCenter(Position.fromDegrees(12, 34));
		classUnderTest.setHeading(Angle.fromDegrees(56));
		classUnderTest.setPitch(Angle.fromDegrees(78));
		classUnderTest.setRoll(Angle.fromDegrees(91));
		classUnderTest.setZoom(23);
		expected = new Matrix(
				0.654527728349612, 0.33119467416635756, 0.6796348583065297, -48.899931205822476,
				-0.5714882330999435, 0.8052636139982711, 0.15796110723449525, 0.8535514741830017,
				-0.4949693447576333, -0.49179324901251276, 0.7163412231443305, -33.39558454088797,
				0.0, 0.0, 0.0, 1.0);
		assertMatrixEquals(expected, classUnderTest.getTransform(globe), EPSILON);
	}

	private static void assertMatrixEquals(Matrix expected, Matrix actual, double delta)
	{
		try
		{
			assertEquals(expected.m11, actual.m11, delta);
			assertEquals(expected.m12, actual.m12, delta);
			assertEquals(expected.m13, actual.m13, delta);
			assertEquals(expected.m14, actual.m14, delta);
			assertEquals(expected.m21, actual.m21, delta);
			assertEquals(expected.m22, actual.m22, delta);
			assertEquals(expected.m23, actual.m23, delta);
			assertEquals(expected.m24, actual.m24, delta);
			assertEquals(expected.m31, actual.m31, delta);
			assertEquals(expected.m32, actual.m32, delta);
			assertEquals(expected.m33, actual.m33, delta);
			assertEquals(expected.m34, actual.m34, delta);
			assertEquals(expected.m41, actual.m41, delta);
			assertEquals(expected.m42, actual.m42, delta);
			assertEquals(expected.m43, actual.m43, delta);
			assertEquals(expected.m44, actual.m44, delta);
		}
		catch (AssertionError e)
		{
			assertEquals(expected, actual);
		}
	}

	private static void assertVec4Equals(Vec4 expected, Vec4 actual, double delta)
	{
		try
		{
			assertEquals(expected.x, actual.x, delta);
			assertEquals(expected.y, actual.y, delta);
			assertEquals(expected.z, actual.z, delta);
			assertEquals(expected.w, actual.w, delta);
		}
		catch (AssertionError e)
		{
			assertEquals(expected, actual);
		}
	}

	private static void assertPositionEquals(Position expected, Position actual, double delta)
	{
		try
		{
			assertEquals(expected.latitude.degrees, actual.latitude.degrees, delta);
			assertEquals(expected.longitude.degrees, actual.longitude.degrees, delta);
			assertEquals(expected.elevation, actual.elevation, delta);
		}
		catch (AssertionError e)
		{
			assertEquals(expected, actual);
		}
	}
}
