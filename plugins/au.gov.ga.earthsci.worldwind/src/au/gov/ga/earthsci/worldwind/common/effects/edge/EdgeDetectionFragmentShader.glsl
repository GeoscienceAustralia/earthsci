uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
uniform float textureWidth;
uniform float textureHeight;

vec4 get_pixel(in vec2 coords, in float dx, in float dy)
{
	return texture2D(colorTexture, coords + vec2(dx, dy));
}

float Convolve(in float[9] kernel, in float[9] matrix, in float denom, in float offset)
{
	float res = 0.0;
	for (int i=0; i<9; i++)
	{
		res += kernel[i]*matrix[i];
	}
	return clamp(res/denom + offset,0.0,1.0);
}

float[9] GetData(in int channel)
{
	float dxtex = 1.0 / textureWidth;
	float dytex = 1.0 / textureHeight;
	float[9] mat;
	int k = -1;
	for (int i=-1; i<2; i++)
	{   
		for(int j=-1; j<2; j++)
		{    
			k++;    
			mat[k] = get_pixel(gl_TexCoord[0].xy,float(i)*dxtex, float(j)*dytex)[channel];
		}
	}
	return mat;
}

float[9] GetMean(in float[9] matr, in float[9] matg, in float[9] matb)
{
	float[9] mat;
	for (int i=0; i<9; i++)
	{
		mat[i] = (matr[i]+matg[i]+matb[i])/3.;
	}
	return mat;
}

void main()
{	
	float[9] kerEmboss = float[] ( 2.,  0.,  0.,
								   0., -1.,  0.,
								   0.,  0., -1. );

	float[9] kerSharpness = float[] ( -1., -1., -1.,
									  -1.,  9., -1.,
									  -1., -1., -1. );

	float[9] kerGausBlur = float[] ( 1., 2., 1.,
									 2., 4., 2.,
									 1., 2., 1. );

	float[9] kerEdgeDetect = float[] ( -1./8., -1./8., -1./8.,
                                       -1./8.,     1., -1./8.,
                                       -1./8., -1./8., -1./8. );

	float matr[9] = GetData(0);
	float matg[9] = GetData(1);
	float matb[9] = GetData(2);
	float mata[9] = GetMean(matr,matg,matb);

   // Sharpness kernel
   gl_FragColor = vec4(Convolve(kerSharpness,matr,1.,0.),
                       Convolve(kerSharpness,matg,1.,0.),
                       Convolve(kerSharpness,matb,1.,0.),1.0);

   // Gaussian blur kernel
   //gl_FragColor = vec4(Convolve(kerGausBlur,matr,16.,0.),
   //                    Convolve(kerGausBlur,matg,16.,0.),
   //                    Convolve(kerGausBlur,matb,16.,0.),1.0);

   // Edge Detection kernel
   //float edge = Convolve(kerEdgeDetect,mata,0.1,0.);
   //gl_FragColor = vec4(edge, edge, edge, edge);
   //gl_FragColor = vec4(1.0, 1.0, 1.0, edge);
   
   // Emboss kernel
	//gl_FragColor = vec4(Convolve(kerEmboss,mata,1.,1./2.),
	//					Convolve(kerEmboss,mata,1.,1./2.),
	//					Convolve(kerEmboss,mata,1.,1./2.), 1.0);
	
	float unnormalizedDepth = texture2D(depthTexture, gl_TexCoord[0].xy).r;
	gl_FragDepth = unnormalizedDepth;
}
