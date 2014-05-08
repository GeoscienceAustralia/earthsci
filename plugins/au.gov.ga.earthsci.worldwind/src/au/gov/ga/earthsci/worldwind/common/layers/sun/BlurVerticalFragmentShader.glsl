uniform sampler2D Texture;
uniform int Width;
uniform float od;

void main()
{
	vec3 Color = vec3(0.0);
	int wp1 = Width + 1;
	float Sum = 0.0;
	
	for(int y = -Width; y <= Width; y++)
	{
		float width = (wp1 - abs(float(y)));
		Color += texture2D(Texture, gl_TexCoord[0].st + vec2(0.0, od * y)).rgb * width;
		Sum += width;
	}
	
	gl_FragColor = vec4(Color / Sum, 1.0);
}
