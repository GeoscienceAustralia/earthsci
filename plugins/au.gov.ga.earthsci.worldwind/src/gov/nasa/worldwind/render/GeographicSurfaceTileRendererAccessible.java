package gov.nasa.worldwind.render;

/**
 * {@link GeographicSurfaceTileRenderer} subclass that provides access to the
 * {@link gov.nasa.worldwind.render.SurfaceTileRenderer.Transform} class
 * (providing a public constructor, and getters for its package-private fields).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GeographicSurfaceTileRendererAccessible extends GeographicSurfaceTileRenderer
{
	protected static class Transform extends SurfaceTileRenderer.Transform
	{
		public Transform()
		{
		}

		public double getHScale()
		{
			return HScale;
		}

		public double getVScale()
		{
			return VScale;
		}

		public double getHShift()
		{
			return HShift;
		}

		public double getVShift()
		{
			return VShift;
		}

		public double getRotationDegrees()
		{
			return rotationDegrees;
		}
	}
}
