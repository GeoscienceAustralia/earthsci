package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport.OrbitViewState;

public class OrientationBasicOrbitView extends LenientBasicOrbitView
{
	private boolean validModelCoordinates;
	private boolean attemptingSetOrientation;

	protected boolean trySetOrientation(Position eyePosition, Position centerPosition)
	{
		attemptingSetOrientation = true;
		setOrientation(eyePosition, centerPosition);
		attemptingSetOrientation = false;
		return validModelCoordinates;
	}

	@Override
	protected boolean validateModelCoordinates(OrbitViewState modelCoords)
	{
		validModelCoordinates = super.validateModelCoordinates(modelCoords);

		//if we are copying the view state, always return true, so an error is not thrown
		if (attemptingSetOrientation)
			return true;

		return validModelCoordinates;
	}
}
