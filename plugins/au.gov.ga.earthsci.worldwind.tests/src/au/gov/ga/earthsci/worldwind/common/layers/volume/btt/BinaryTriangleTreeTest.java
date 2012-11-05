package au.gov.ga.earthsci.worldwind.common.layers.volume.btt;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import au.gov.ga.earthsci.worldwind.common.render.fastshape.FastShape;

public class BinaryTriangleTreeTest
{
	@Test
	public void testSimplification() throws IOException
	{
		ByteBuffer byteBuffer = WWIO.readURLContentToBuffer(this.getClass().getResource("elevations.bil"));

		// Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, AVKey.INT16);
		bufferParams.setValue(AVKey.BYTE_ORDER, AVKey.LITTLE_ENDIAN);
		BufferWrapper wrapper = BufferWrapper.wrap(byteBuffer, bufferParams);

		int width = 150;
		int height = 150;
		List<Position> positions = new ArrayList<Position>(width * height);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				positions.add(Position.fromDegrees(y, x, wrapper.getDouble(y * width + x)));
			}
		}

		BinaryTriangleTree btt = new BinaryTriangleTree(positions, width, height);
		FastShape shape = btt.buildMeshFromCenter(1, new Rectangle(29, 29, 119, 119));

		BufferedImage image = shapeToImage(shape, width, height);
		BufferedImage reference = ImageIO.read(this.getClass().getResourceAsStream("reference.png"));

		Assert.assertTrue(areImagesEqual(image, reference));
	}

	protected BufferedImage shapeToImage(FastShape shape, int width, int height)
	{
		int[] indices = shape.getIndices();
		List<Position> posi = shape.getPositions();

		int s = (width - 1) * 8 + 1;
		BufferedImage image = new BufferedImage(s, s, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, s, s);
		g.setColor(Color.black);
		for (int i = 0; i < indices.length; i += 3)
		{
			Position left = posi.get(indices[i + 0]);
			Position apex = posi.get(indices[i + 1]);
			Position right = posi.get(indices[i + 2]);
			g.drawLine((int) (left.longitude.degrees * (s - 1) / (width - 1)),
					(int) (left.latitude.degrees * (s - 1) / (height - 1)),
					(int) (apex.longitude.degrees * (s - 1) / (width - 1)),
					(int) (apex.latitude.degrees * (s - 1) / (height - 1)));
			g.drawLine((int) (left.longitude.degrees * (s - 1) / (width - 1)),
					(int) (left.latitude.degrees * (s - 1) / (height - 1)),
					(int) (right.longitude.degrees * (s - 1) / (width - 1)),
					(int) (right.latitude.degrees * (s - 1) / (height - 1)));
			g.drawLine((int) (right.longitude.degrees * (s - 1) / (width - 1)),
					(int) (right.latitude.degrees * (s - 1) / (height - 1)),
					(int) (apex.longitude.degrees * (s - 1) / (width - 1)),
					(int) (apex.latitude.degrees * (s - 1) / (height - 1)));
		}
		g.dispose();

		return image;
	}

	protected boolean areImagesEqual(BufferedImage i1, BufferedImage i2)
	{
		if (i1.getWidth() != i2.getWidth() || i1.getHeight() != i2.getHeight())
		{
			return false;
		}

		for (int y = 0; y < i1.getHeight(); y++)
		{
			for (int x = 0; x < i1.getWidth(); x++)
			{
				if (i1.getRGB(x, y) != i2.getRGB(x, y))
				{
					return false;
				}
			}
		}

		return true;
	}

	/*public static void main(String[] args) throws IOException
	{
		Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				if (e instanceof StackOverflowError)
				{
					System.err.println(e);
				}
				else
				{
					e.printStackTrace();
				}
			}
		});

		
		

		ImageIO.write(image, "JPG", new File("output.jpg"));
	}*/
}
