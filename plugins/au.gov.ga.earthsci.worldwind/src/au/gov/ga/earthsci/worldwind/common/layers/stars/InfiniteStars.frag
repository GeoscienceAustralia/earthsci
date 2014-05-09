uniform sampler2D tex;

void main (void)
{
	gl_FragColor = gl_Color * texture2D(tex, gl_TexCoord[0].st);
}
