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
package au.gov.ga.earthsci.worldwind.common.render;

import javax.media.opengl.GL2;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.DrawContextImpl;
import gov.nasa.worldwind.render.GLRuntimeCapabilities;
import gov.nasa.worldwind.terrain.SectorGeometryList;

/**
 * Extension of {@link DrawContextImpl} that provides better wireframe elevation
 * rendering. Also stores the sector geometry for tiled layers that ignore
 * elevation (rendered on a flat surface).
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExtendedDrawContext extends DrawContextImpl
{
	protected boolean wireframe = false;
	protected SectorGeometryList flatSurfaceGeometry;
	protected SectorGeometryList oldSurfaceGeomtry;

	public ExtendedDrawContext()
	{
		geographicSurfaceTileRenderer.dispose();
		geographicSurfaceTileRenderer = new ExtendedSurfaceTileRenderer();
	}

	@Override
	public ExtendedSurfaceTileRenderer getGeographicSurfaceTileRenderer()
	{
		return (ExtendedSurfaceTileRenderer) super.getGeographicSurfaceTileRenderer();
	}

	/**
	 * @return Is wireframe enabled?
	 */
	public boolean isWireframe()
	{
		return wireframe;
	}

	/**
	 * Enable/disable wireframe.
	 * 
	 * @param wireframe
	 */
	public void setWireframe(boolean wireframe)
	{
		this.wireframe = wireframe;
	}

	/**
	 * Helper function to check if wireframe is enabled for the provided
	 * DrawContext.
	 * 
	 * @param dc
	 *            DrawContext to check
	 * @return If dc has wireframe enabled
	 */
	public static boolean isWireframe(DrawContext dc)
	{
		return dc instanceof ExtendedDrawContext && ((ExtendedDrawContext) dc).isWireframe();
	}

	/**
	 * Apply the correct OpenGL polygon mode for the provided dc's wireframe
	 * property value. Uses {@link GL#GL_FILL} by default, {@link GL#GL_LINE} if
	 * wireframe is enabled.
	 * 
	 * @param dc
	 */
	public static void applyWireframePolygonMode(DrawContext dc)
	{
		dc.getGL().glPolygonMode(GL2.GL_FRONT_AND_BACK, isWireframe(dc) ? GL2.GL_LINE : GL2.GL_FILL);
	}

	@Override
	public void setGLRuntimeCapabilities(GLRuntimeCapabilities capabilities)
	{
		super.setGLRuntimeCapabilities(capabilities);

		//it would be better to override the initialize method, but unfortunately (for some unknown reason) it's final

		if (this.flatSurfaceGeometry != null)
			this.flatSurfaceGeometry.clear();
		this.flatSurfaceGeometry = null;
	}

	/**
	 * @return The sector geometry for the unelevated globe, used for tiled
	 *         layers rendered onto a flat surface.
	 */
	public SectorGeometryList getFlatSurfaceGeometry()
	{
		return flatSurfaceGeometry;
	}

	/**
	 * Set the sector geometry for the unelevated globe. Called by the scene
	 * controller when the terrain is computed each frame.
	 * 
	 * @param flatSectorGeometry
	 */
	public void setFlatSurfaceGeometry(SectorGeometryList flatSectorGeometry)
	{
		this.flatSurfaceGeometry = flatSectorGeometry;
	}

	/**
	 * Switch this {@link DrawContext} to use the flat unelevated model.
	 */
	public void switchToFlatSurfaceGeometry()
	{
		oldSurfaceGeomtry = getSurfaceGeometry();
		setSurfaceGeometry(flatSurfaceGeometry);
	}

	/**
	 * Switch this {@link DrawContext} back to the standard elevation model.
	 */
	public void switchToStandardSurfaceGeometry()
	{
		setSurfaceGeometry(oldSurfaceGeomtry);
	}
}
