package au.gov.ga.earthsci.model.geometry;

import java.util.Set;

import au.gov.ga.earthsci.core.util.IDescribed;
import au.gov.ga.earthsci.core.util.IIdentifiable;
import au.gov.ga.earthsci.core.util.INamed;
import au.gov.ga.earthsci.core.util.IPropertyChangeBean;
import au.gov.ga.earthsci.model.bounds.IBoundingVolume;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;

/**
 * Represents a single geometry within a model (e.g. a single surface, volume, pointset etc.)
 * <p/>
 * Instances have a globally unique ID that allows them to be identified within a single application
 * session.
 * <p/>
 * All model geometries implement the {@link IPropertyChangeBean} interface, allowing listeners
 * to be registered to detect changes in geometry state etc. Note that implementations may be
 * immutable and thus will not necessarily issue any events. Subclasses should provide documentation
 * as to what events they publish. 
 * <p/>
 * <b>Events</b>
 * <dl>
 * 	<dt>{@value #RENDERER_EVENT_NAME}</dt><dd>Issued when the renderer associated with this geometry is changed</dd>
 *  <dt>{@value #DATA_EVENT_NAME}</dt><dd>Issued when the set of {@link IModelData} associated with this geometry is changed</dd>
 *  <dt>{@value #BOUNDING_VOLUME_EVENT_NAME}</dt><dd>Issued when the bounding volume for this geometry changes</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface IModelGeometry extends IIdentifiable, INamed, IDescribed, IPropertyChangeBean
{
	
	String RENDERER_EVENT_NAME = "renderer"; //$NON-NLS-1$
	String DATA_EVENT_NAME = "data"; //$NON-NLS-1$
	String BOUNDING_VOLUME_EVENT_NAME = "boundingVolume"; //$NON-NLS-1$
	
	/**
	 * Return the renderer associated with this geometry, if there is one.
	 * 
	 * @return The renderer associated with this geometry, if there is one.
	 */
	IModelGeometryRenderer getRenderer();
	
	/**
	 * Set the renderer associated with this geometry.
	 * 
	 * @param renderer The renderer to associate with this geometry.
	 */
	void setRenderer(IModelGeometryRenderer renderer);
	
	/**
	 * Return the {@link IModelData} instance associated with this geometry that has
	 * the given ID, if one exists.
	 * <p/>
	 * Note that in many cases it is more convenient to access specific data from getter methods
	 * provided by subclasses if they are available.
	 *  
	 * @param id The ID of the data to retrieve
	 * 
	 * @return The model data with the given ID, or <code>null</code> if one cannot be found.
	 */
	IModelData getDataById(String id);
	
	/**
	 * Return the {@link IModelData} instance associated with this geometry under the given key,
	 * if one exists. 
	 * <p/>
	 * Note that in many cases it is more convenient to access specific data from getter methods
	 * provided by subclasses if they are available.
	 * 
	 * @param key The key to the data to be retrieved
	 * 
	 * @return The model data stored under the given key, or <code>null</code> if none is
	 * available
	 */
	IModelData getDataByKey(String key);
	
	/**
	 * Return the keys under which model data is stored on this geometry
	 * 
	 * @return The set of keys under which model data is available on this geometry
	 */
	Set<String> getDataKeys();
	
	/**
	 * Return the bounding volume for this geometry
	 * 
	 * @return The bounding volume for this geometry
	 */
	IBoundingVolume getBoundingVolume();
}
