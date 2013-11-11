#version 110

// Vertex shader for the BasicRenderer
//
// Takes vertices in geographic decimal degrees coordinates and projects them into 
// cartesian coordinates
// 
// @author Michael de Hoog (michael.dehoog@ga.gov.au)
// @author James Navin (james.navin@ga.gov.au)
 
uniform float opacity; // Opacity in range [0,1] 

uniform float ve; //vertical exaggeration
uniform float zNodata; //nodata mask encoded in Z values

varying float mask;

#include /au/gov/ga/earthsci/model/core/shader/common/GeodeticToCartesianProjection.glsl
 
void main(void)
{	
	// Mask will be 0 where Z == zNodata; 1 everywhere else.
	mask = 1.0 - step(zNodata, gl_Vertex.z) * step(gl_Vertex.z, zNodata); 
	
	//project the geodetic coordinates to cartesian space
	vec3 geodetic = vec3(radians(gl_Vertex.xy), ve * gl_Vertex.z);
	vec3 cartesian = geodeticToCartesian(geodetic);

	gl_FrontColor = vec4(gl_Color.rgb, gl_Color.a * opacity);

	//output the vertex position
	gl_Position = gl_ModelViewProjectionMatrix * vec4(cartesian, 1.0);
}