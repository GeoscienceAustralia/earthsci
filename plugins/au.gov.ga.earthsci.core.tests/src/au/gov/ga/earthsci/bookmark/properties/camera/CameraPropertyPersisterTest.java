/*******************************************************************************
 * Copyright 2012 Geoscience Australia
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
package au.gov.ga.earthsci.bookmark.properties.camera;

import static org.junit.Assert.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.util.WWXML;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.common.util.XmlUtil;

/**
 * Unit tests for the {@link CameraPropertyPersister} class
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CameraPropertyPersisterTest
{
	
	private CameraPropertyPersister classUnderTest = new CameraPropertyPersister();
	
	private Mockery mockContext;

	@Before
	public void setup()
	{
		mockContext = new Mockery();
	}
	
	@Test
	public void testGetSupportedTypes()
	{
		assertArrayEquals(new String[] {CameraProperty.TYPE}, classUnderTest.getSupportedTypes());
	}
	
	@Test
	public void testExportToXMLWithNullProperty()
	{
		IBookmarkProperty property = null;
		Element propertyElement = createPropertyElement();
		
		classUnderTest.exportToXML(property, propertyElement);
		
		assertFalse(propertyElement.hasChildNodes());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testExportToXMLWithNullElement()
	{
		IBookmarkProperty property = createCameraProperty(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Element propertyElement = null;
		
		classUnderTest.exportToXML(property, propertyElement);
	}
	
	@Test
	public void testExportToXMLWithNonNull()
	{
		IBookmarkProperty property = createCameraProperty(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		Element propertyElement = createPropertyElement();
		
		classUnderTest.exportToXML(property, propertyElement);
		
		assertTrue(propertyElement.hasChildNodes());
		
		Element[] children = XmlUtil.getElements(propertyElement);
		assertEquals(1, children.length);
		assertEquals("camera", children[0].getNodeName());
		
		Element cameraElement = children[0];
		children = XmlUtil.getElements(cameraElement);
		
		assertEquals(3, children.length);
		assertEquals("eyePosition", children[0].getNodeName());
		assertEquals("lookatPosition", children[1].getNodeName());
		assertEquals("upVector", children[2].getNodeName());
		
		assertPositionElementCorrect(XmlUtil.getChildElementByTagName(0, "position", children[0]), 1, 2, 3, "degrees");
		assertPositionElementCorrect(XmlUtil.getChildElementByTagName(0, "position", children[1]), 4, 5, 6, "degrees");
		assertVec4ElementCorrect(XmlUtil.getChildElementByTagName(0, "vector", children[2]), 7, 8, 9, 10);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testExportToXMLWithNonCameraProperty()
	{
		final IBookmarkProperty property = mockContext.mock(IBookmarkProperty.class);
		Element propertyElement = createPropertyElement();
		
		mockContext.checking(new Expectations() {{{
			allowing(property).getType(); will(returnValue("not.a.camera.property"));
		}}});
		
		classUnderTest.exportToXML(property, propertyElement);
	}
	
	@Test
	public void testCreateFromXMLWithNullElement()
	{
		assertNull(classUnderTest.createFromXML(CameraProperty.TYPE, null));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateFromXMLWithNonCameraProperty()
	{
		classUnderTest.createFromXML("not.a.camera.property", null);
	}
	
	@Test
	public void testCreateFromXMLWithValidXML()
	{
		Element propertyElement = createCameraPropertyElement(true, true, true);
		
		IBookmarkProperty result = classUnderTest.createFromXML(CameraProperty.TYPE, propertyElement);
		assertNotNull(result);
		assertTrue(result instanceof CameraProperty);
		
		CameraProperty cameraProperty = (CameraProperty)result;
		assertNotNull(cameraProperty.getEyePosition());
		assertPositionCorrect(cameraProperty.getEyePosition(), 1, 2, 3);
		
		assertNotNull(cameraProperty.getLookatPosition());
		assertPositionCorrect(cameraProperty.getLookatPosition(), 4, 5, 6);

		assertNotNull(cameraProperty.getUpVector());
		assertVec4Correct(cameraProperty.getUpVector(), 7, 8, 9, 10);
	}

	@Test
	public void testCreateFromXMLMissingEyePosition()
	{
		Element propertyElement = createCameraPropertyElement(false, true, true);
		
		assertNull(classUnderTest.createFromXML(CameraProperty.TYPE, propertyElement));
	}
	
	@Test
	public void testCreateFromXMLMissingLookatPosition()
	{
		Element propertyElement = createCameraPropertyElement(true, false, true);
		
		assertNull(classUnderTest.createFromXML(CameraProperty.TYPE, propertyElement));
	}
	
	@Test
	public void testCreateFromXMLMissingUpVector()
	{
		Element propertyElement = createCameraPropertyElement(true, true, false);
		
		assertNull(classUnderTest.createFromXML(CameraProperty.TYPE, propertyElement));
	}
	
	private Element createCameraPropertyElement(boolean addEyePosition, boolean addLookatPosition, boolean addUpVector)
	{
		Element propertyElement = createPropertyElement();
		propertyElement.setAttribute("propertyType", CameraProperty.TYPE);
		
		Element cameraElement = WWXML.appendElement(propertyElement, "camera");
		
		if (addEyePosition)
		{
			Element eyePositionElement = WWXML.appendElement(cameraElement, "eyePosition");
			Element positionElement = WWXML.appendElement(eyePositionElement, "position");
			positionElement.setAttribute("latitude", "1");
			positionElement.setAttribute("longitude", "2");
			positionElement.setAttribute("elevation", "3");
			positionElement.setAttribute("units", "degrees");
		}
		
		if (addLookatPosition)
		{
			Element lookatPositionElement = WWXML.appendElement(cameraElement, "lookatPosition");
			Element positionElement = WWXML.appendElement(lookatPositionElement, "position");
			positionElement.setAttribute("latitude", "4");
			positionElement.setAttribute("longitude", "5");
			positionElement.setAttribute("elevation", "6");
			positionElement.setAttribute("units", "degrees");
		}
		
		if (addUpVector)
		{
			Element upVectorElement = WWXML.appendElement(cameraElement, "upVector");
			Element vectorElement = WWXML.appendElement(upVectorElement, "vector");
			vectorElement.setAttribute("x", "7");
			vectorElement.setAttribute("y", "8");
			vectorElement.setAttribute("z", "9");
			vectorElement.setAttribute("w", "10");
		}
		
		return propertyElement;
	}
	
	private void assertPositionElementCorrect(Element positionEement, double lat, double lon, double el, String units)
	{
		assertEquals(lat, XmlUtil.getDouble(positionEement, "@latitude", -1), 0.001);
		assertEquals(lon, XmlUtil.getDouble(positionEement, "@longitude", -1), 0.001);
		assertEquals(el, XmlUtil.getDouble(positionEement, "@elevation", -1), 0.001);
		assertEquals(units, XmlUtil.getText(positionEement, "@units"));
	}
	
	private void assertPositionCorrect(Position p, double lat, double lon, double el)
	{
		assertEquals(lat, p.latitude.degrees, 0.001);
		assertEquals(lon, p.longitude.degrees, 0.001);
		assertEquals(el, p.elevation, 0.001);
	}
	
	private void assertVec4ElementCorrect(Element vec4Element, double w, double x, double y, double z)
	{
		assertEquals(x, XmlUtil.getDouble(vec4Element, "@x", -1), 0.001);
		assertEquals(y, XmlUtil.getDouble(vec4Element, "@y", -1), 0.001);
		assertEquals(z, XmlUtil.getDouble(vec4Element, "@z", -1), 0.001);
		assertEquals(w, XmlUtil.getDouble(vec4Element, "@w", -1), 0.001);
	}
	
	private void assertVec4Correct(Vec4 v, double x, double y, double z, double w)
	{
		assertEquals(w, v.w, 0.001);
		assertEquals(x, v.x, 0.001);
		assertEquals(y, v.y, 0.001);
		assertEquals(z, v.z, 0.001);
	}
	
	private Element createPropertyElement()
	{
		Document d = WWXML.createDocumentBuilder(false).newDocument();
		return d.createElement("property");
	}
	
	private CameraProperty createCameraProperty(double eyeLat, double eyeLon, double eyeEl, 
												double lookatLat, double lookatLon, double lookatEl, 
												double upw, double upx, double upy, double upz)
	{
		CameraProperty p = new CameraProperty(Position.fromDegrees(eyeLat, eyeLon, eyeEl), 
											  Position.fromDegrees(lookatLat, lookatLon, lookatEl), 
											  new Vec4(upx, upy, upz, upw));
		return p;
	}
}
