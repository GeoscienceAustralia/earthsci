uniform sampler2D SunTexture;
uniform sampler2D DepthTexture;

void main()
{
	if(texture2D(DepthTexture, gl_TexCoord[0].st).r < 1.0)
	{
		gl_FragColor = vec4(vec3(0.0), 1.0);
	}
	else
	{
		gl_FragColor = vec4(texture2D(SunTexture, gl_TexCoord[0].st).rgb, 1.0);
	}
}
