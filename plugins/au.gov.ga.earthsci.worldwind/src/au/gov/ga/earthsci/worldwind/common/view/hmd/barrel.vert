uniform mat4 Texm;

void main()
{
	//gl_TexCoord[0] = vec2(Texm * vec4(gl_MultiTexCoord0, 0.0, 1.0));
	gl_TexCoord[0] = gl_MultiTexCoord0;
	gl_Position = gl_Vertex;
}
