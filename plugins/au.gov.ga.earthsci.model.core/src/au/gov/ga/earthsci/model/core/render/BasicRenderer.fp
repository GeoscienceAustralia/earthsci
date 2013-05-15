#version 110

// Fragment shader for the BasicRenderer
//
// @author Michael de Hoog (michael.dehoog@ga.gov.au)
// @author James Navin (james.navin@ga.gov.au)
 
#ifdef GL_ES
precision highp float;
#endif

#define PI 3.14159

varying float mask;

void main(void)
{
	gl_FragColor = vec4(gl_Color.rgb, gl_Color.a * step(1.0f, mask));
}