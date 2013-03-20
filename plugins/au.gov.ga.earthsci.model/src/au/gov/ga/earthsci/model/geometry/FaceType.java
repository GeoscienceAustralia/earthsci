package au.gov.ga.earthsci.model.geometry;

/**
 * An enumeration of possible face type primitives
 * <p/>
 * These definitions reflect primitives supported by this geometry, and are defined to 
 * reflect the OpenGL standard primitive types.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public enum FaceType
{
	/** 
	 * Sets of three vertices used to form triangles
	 * {@code [v0,v1,v2][v3,v4,v5][v6,v7,v8]} etc. 
	 */
	TRIANGLES,
	
	/** 
	 * Linked strip of triangles 
	 * {@code [v0,v1,v2][v2,v1,v3][v2,v3,v4]} etc. 
	 */
	TRIANGLE_STRIP,
	
	/**
	 * A fan centred around the first vertex
	 * {@code [v0,v1,v2][v0,v2,v3][v0,v3,v4]} etc.
	 */
	TRIANGLE_FAN,
	
	/**
	 * Sets of four vertices used form a quadrilateral
	 * {@code [v0,v1,v2,v3][v4,v5,v6,v7][v8,v9,v10,v11]} etc.
	 */
	QUADS,
	
	/**
	 * A linked set of quadrilaterals
	 *  {@code [v0,v1,v2,v3][v2,v3,v5,v4][v4,v5,v6,v7]} etc.
	 */
	QUAD_STRIP,
}
