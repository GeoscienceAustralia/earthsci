package au.gov.ga.earthsci.model.geometry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.text.Position;

import au.gov.ga.earthsci.core.util.AbstractPropertyChangeBean;
import au.gov.ga.earthsci.core.util.Validate;
import au.gov.ga.earthsci.model.data.IModelData;
import au.gov.ga.earthsci.model.render.IModelGeometryRenderer;

/**
 * A basic implementation of the {@link IMeshGeometry} interface that also implements
 * {@link IVertexColouredGeometry} and allows colour information to be stored at each vertex.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicColouredMeshGeometry extends AbstractPropertyChangeBean implements IMeshGeometry, IVertexColouredGeometry
{

	private final String id;
	private String name;
	private String description;
	
	private IModelData<Position> vertices;
	
	private IModelData<Float> normals;
	
	private IModelData<Float> colours;
	private ColourType colourType;
	
	private IModelData<Integer> edges;
	private FaceType faceType;

	private IModelGeometryRenderer renderer;
	
	private final Map<String, IModelData<?>> dataById = new ConcurrentHashMap<String, IModelData<?>>();
	private final Map<String, IModelData<?>> dataByKey = new ConcurrentHashMap<String, IModelData<?>>();
	
	public BasicColouredMeshGeometry(String id, String name, String description)
	{
		Validate.notBlank(id, "An ID is required"); //$NON-NLS-1$
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	@Override
	public String getId()
	{
		return id;
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getDescription()
	{
		return description;
	}
	
	@Override
	public IModelData<Position> getVertices()
	{
		return vertices;
	}
	
	public void setVertices(IModelData<Position> vertices)
	{
		firePropertyChange(VERTICES_EVENT_NAME, this.vertices, this.vertices = vertices);
	}

	@Override
	public boolean hasVertices()
	{
		return vertices != null;
	}
	
	@Override
	public IModelGeometryRenderer getRenderer()
	{
		return renderer;
	}

	@Override
	public void setRenderer(IModelGeometryRenderer renderer)
	{
		firePropertyChange(RENDERER_EVENT_NAME, this.renderer, this.renderer = renderer);
	}

	@Override
	public IModelData<?> getDataById(String id)
	{
		return dataById.get(id);
	}

	@Override
	public IModelData<?> getDataByKey(String key)
	{
		return dataByKey.get(key);
	}

	@Override
	public Set<String> getDataKeys()
	{
		return dataByKey.keySet();
	}

	@Override
	public IModelData<Float> getVertexColour()
	{
		return colours;
	}
	
	public void setVertexColour(IModelData<Float> colours)
	{
		firePropertyChange(VERTEX_COLOUR_EVENT_NAME, this.colours, this.colours = colours);
	}

	@Override
	public boolean hasVertexColour()
	{
		return colours != null;
	}

	@Override
	public ColourType getColourType()
	{
		return colourType;
	}

	@Override
	public IModelData<Integer> getEdgeIndices()
	{
		return edges;
	}
	
	@Override
	public boolean hasEdgeIndices()
	{
		return edges != null;
	}
	
	@Override
	public FaceType getFaceType()
	{
		return faceType;
	}

	@Override
	public IModelData<Float> getNormals()
	{
		return normals;
	}

	@Override
	public boolean hasNormals()
	{
		return normals != null;
	}

}
