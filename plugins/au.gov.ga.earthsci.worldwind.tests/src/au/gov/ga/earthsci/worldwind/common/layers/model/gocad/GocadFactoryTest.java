package au.gov.ga.earthsci.worldwind.common.layers.model.gocad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.media.opengl.GL;

import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;

public class GocadFactoryTest
{
	@Test
	public void testTSurf() throws IOException
	{
		URL url = this.getClass().getResource("tsurf.ts");
		InputStream is = url.openStream();
		GocadReaderParameters parameters = new GocadReaderParameters();
		List<FastShape> shapes = GocadFactory.read(is, url, parameters);
		assertEquals(1, shapes.size());
		assertEquals(3, shapes.get(0).getPositions().size());
	}

	@Test
	public void testPLine() throws IOException
	{
		URL url = this.getClass().getResource("pline.gp");
		InputStream is = url.openStream();
		GocadReaderParameters parameters = new GocadReaderParameters();
		List<FastShape> shapes = GocadFactory.read(is, url, parameters);
		assertEquals(1, shapes.size());
		assertEquals(2, shapes.get(0).getPositions().size());
	}

	@Test
	public void testVSet() throws IOException
	{
		URL url = this.getClass().getResource("vset.vs");
		InputStream is = url.openStream();
		GocadReaderParameters parameters = new GocadReaderParameters();
		List<FastShape> shapes = GocadFactory.read(is, url, parameters);

		assertEquals(1, shapes.size());

		FastShape shape = shapes.get(0);
		assertEquals(20, shape.getPositions().size());
		assertEquals(GL.GL_POINTS, shape.getMode());
		assertFalse(shape.isLighted());
		assertEquals(2.0, shape.getPointSize(), 0.001);
		assertEquals(new Color(0.098039f, 0.098039f, 0.439216f), shape.getColor());
		assertNull(shape.getColorBuffer());
	}

	@Test
	public void testVSetWithParameterOverrides() throws IOException
	{
		URL url = this.getClass().getResource("vset.vs");
		InputStream is = url.openStream();

		GocadReaderParameters parameters = new GocadReaderParameters();
		parameters.setColor(Color.RED);
		parameters.setPointSize(4.0);

		List<FastShape> shapes = GocadFactory.read(is, url, parameters);

		assertEquals(1, shapes.size());

		FastShape shape = shapes.get(0);
		assertEquals(20, shape.getPositions().size());
		assertEquals(GL.GL_POINTS, shape.getMode());
		assertFalse(shape.isLighted());
		assertEquals(4.0, shape.getPointSize(), 0.001);
		assertEquals(Color.RED, shape.getColor());
		assertNull(shape.getColorBuffer());
	}

	@Test
	public void testVSetWithColorMap() throws IOException
	{
		URL url = this.getClass().getResource("vset.vs");
		InputStream is = url.openStream();

		GocadReaderParameters parameters = new GocadReaderParameters();
		ColorMap colorMap = new ColorMap();
		colorMap.setValuesPercentages(true);
		colorMap.put(0d, Color.BLACK);
		colorMap.put(1d, Color.WHITE);
		parameters.setColorMap(colorMap);

		List<FastShape> shapes = GocadFactory.read(is, url, parameters);

		assertEquals(1, shapes.size());

		FastShape shape = shapes.get(0);
		assertNotNull(shape.getColorBuffer());
		assertEquals(20 * 4, shape.getColorBuffer().length);
	}
}
