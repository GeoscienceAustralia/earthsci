#version 110

// Fragment shader for the BasicRenderer
//
// @author Michael de Hoog (michael.dehoog@ga.gov.au)
// @author James Navin (james.navin@ga.gov.au)
 
#ifdef GL_ES
precision highp float;
#endif

#define PI 3.14159

void main(void)
{
	gl_FragColor = gl_Color;
}