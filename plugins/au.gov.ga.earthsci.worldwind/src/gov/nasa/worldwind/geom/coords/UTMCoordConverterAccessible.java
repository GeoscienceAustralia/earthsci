package gov.nasa.worldwind.geom.coords;

import gov.nasa.worldwind.globes.Globe;

/**
 * This class exists to make the UTMCoordConverter class accessible (it is
 * currently package-private).
 * 
 * @author Michael de Hoog
 */
public class UTMCoordConverterAccessible extends UTMCoordConverter
{
	public UTMCoordConverterAccessible(Globe globe)
	{
		super(globe);
	}

	public UTMCoordConverterAccessible(double a, double f)
	{
		super(a, f);
	}
}
