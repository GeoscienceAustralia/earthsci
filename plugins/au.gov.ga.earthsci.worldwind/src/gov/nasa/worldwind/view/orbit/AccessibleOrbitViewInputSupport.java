package gov.nasa.worldwind.view.orbit;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

public class AccessibleOrbitViewInputSupport extends OrbitViewInputSupport
{
	public static class AccessibleOrbitViewState extends OrbitViewState
	{
		public AccessibleOrbitViewState(Position center, Angle heading, Angle pitch, double zoom)
		{
			super(center, heading, pitch, zoom);
		}
	}

	public static AccessibleOrbitViewState computeOrbitViewState(Globe globe, Vec4 eyePoint, Vec4 centerPoint, Vec4 up)
	{
		OrbitViewState ovs = OrbitViewInputSupport.computeOrbitViewState(globe, eyePoint, centerPoint, up);
		return new AccessibleOrbitViewState(ovs.getCenterPosition(), ovs.getHeading(), ovs.getPitch(), ovs.getZoom());
	}

	public static AccessibleOrbitViewState computeOrbitViewState(Globe globe, Matrix modelTransform, Vec4 centerPoint)
	{
		OrbitViewState ovs = OrbitViewInputSupport.computeOrbitViewState(globe, modelTransform, centerPoint);
		return new AccessibleOrbitViewState(ovs.getCenterPosition(), ovs.getHeading(), ovs.getPitch(), ovs.getZoom());
	}
}
