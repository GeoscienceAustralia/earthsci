/*******************************************************************************
 * Copyright 2014 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.core.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class ViewState
{
	//constants
	protected final static double EPSILON = 1.0e-9;

	//state
	protected Vec4 center = Vec4.ZERO;
	protected Quaternion rotation = Quaternion.IDENTITY;
	protected double distance = 1.0;

	//cached derived values
	protected Vec4 lastEye;
	protected Vec4 lastUp;
	protected Vec4 lastForward;
	protected Vec4 lastSide;
	protected Matrix lastRotationMatrix;
	protected Angle lastRoll;
	protected Angle lastHeading;
	protected Angle lastPitch;

	protected void clearCachedValues()
	{
		lastEye = null;
		lastUp = null;
		lastForward = null;
		lastSide = null;
		lastRotationMatrix = null;
		lastRoll = null;
		lastHeading = null;
		lastPitch = null;
	}

	public Vec4 getCenter()
	{
		return center;
	}

	public void setCenter(Vec4 center)
	{
		this.center = center;
		clearCachedValues();
	}

	public Quaternion getRotation()
	{
		return rotation;
	}

	public void setRotation(Quaternion rotation)
	{
		this.rotation = rotation.normalize();
		clearCachedValues();
	}

	public double getDistance()
	{
		return distance;
	}

	public void setDistance(double distance)
	{
		this.distance = distance;
		clearCachedValues();
	}

	public Vec4 getForward()
	{
		if (lastForward == null)
		{
			lastForward = lastSide != null && lastUp != null ?
					lastSide.cross3(lastUp) :
					Vec4.UNIT_Z.transformBy3(rotation);
		}
		return lastForward;
	}

	public Vec4 getUp()
	{
		if (lastUp == null)
		{
			lastUp = lastForward != null && lastSide != null ?
					lastForward.cross3(lastSide) :
					Vec4.UNIT_Y.transformBy3(rotation);
		}
		return lastUp;
	}

	public Vec4 getSide()
	{
		if (lastSide == null)
		{
			lastSide = lastUp != null && lastForward != null ?
					lastUp.cross3(lastForward) :
					Vec4.UNIT_X.transformBy3(rotation);
		}
		return lastSide;
	}

	public Matrix getTransform()
	{
		//gluLookAt defines s as (f x u), but getSide returns (u x f), so negate it during matrix creation
		Vec4 s = getSide();
		Vec4 u = getUp();
		Vec4 f = getForward();
		Vec4 eye = getEye();
		Matrix mAxes = new Matrix(
				-s.x, -s.y, -s.z, 0.0,
				u.x, u.y, u.z, 0.0,
				-f.x, -f.y, -f.z, 0.0,
				0.0, 0.0, 0.0, 1.0);
		Matrix mEye = Matrix.fromTranslation(-eye.x, -eye.y, -eye.z);
		Matrix result = mAxes.multiply(mEye);
		return result;
	}

	public Matrix getRotationMatrix()
	{
		if (lastRotationMatrix == null)
		{
			lastRotationMatrix = Matrix.fromQuaternion(rotation);
		}
		return lastRotationMatrix;
	}

	public Vec4 getEye()
	{
		if (lastEye == null)
		{
			Vec4 forward = getForward();
			lastEye = center.add3(forward.multiply3(-distance));
		}
		return lastEye;
	}

	public void setEye(Vec4 eye)
	{
		//get the world roll (quasi heading) prior to the eye position change
		Angle worldRoll = getWorldRoll();

		//change the rotation and distance to move the eye to the new position
		Vec4 newForwardUnnormalized = center.subtract3(eye);
		Vec4 newForward = newForwardUnnormalized.normalize3();
		double dot = Vec4.UNIT_Z.dot3(newForward);
		Quaternion newRotation = dot >= 1.0 ? Quaternion.IDENTITY :
				dot <= -1.0 ? Quaternion.fromAxisAngle(Angle.POS180, Vec4.UNIT_Y) :
						Quaternion.fromAxisAngle(Angle.fromRadians(Math.acos(dot)), Vec4.UNIT_Z.cross3(newForward));
		setRotation(newRotation);
		setDistance(newForwardUnnormalized.getLength3());

		setWorldRoll(worldRoll);
	}

	public Angle getRoll()
	{
		if (lastRoll == null)
		{
			lastRoll = getRotationMatrix().getRotationZ();
		}
		return lastRoll;
	}

	public void setRoll(Angle angle)
	{
		Angle current = getRoll();
		Angle delta = angle.subtract(current);
		Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, delta);
		setRotation(rotation.multiply(q));
	}

	public Angle getWorldRoll()
	{
		if (lastHeading == null)
		{
			//get forward and side vectors
			Vec4 forward = getForward();
			Vec4 side = getSide();
			//find the rotation that rotates the forward vector to lie on the X-Z plane
			Vec4 axis = forward.cross3(Vec4.UNIT_Y);
			Angle angle = Angle.fromRadians(Math.asin(Vec4.UNIT_Y.dot3(forward)));
			Quaternion upRotation = Quaternion.fromAxisAngle(angle, axis);
			//rotate the global up vector (UNIT_Y) by the calculated rotation so that it is perpendicular to the forward vector
			Vec4 relativeUp = Vec4.UNIT_Y.transformBy3(upRotation).normalize3();
			//find the side vector that lies on the X-Z plane that is perpendicular to the rotated up vector and the forward vector
			Vec4 relativeSide = relativeUp.cross3(forward);
			//return the angle between the actual side vector and the X-Z plane side vector
			double dot = side.dot3(relativeSide);
			lastHeading = dot >= 1.0 ? Angle.ZERO : dot <= -1.0 ? Angle.POS180 :
					Angle.fromRadians(Math.signum(side.dot3(relativeUp)) * Math.acos(dot));
		}
		return lastHeading;
	}

	public void setWorldRoll(Angle angle)
	{
		Angle current = getWorldRoll();
		Angle delta = angle.subtract(current);
		setRoll(getRoll().add(delta));
	}

	public Angle getPitch()
	{
		if (lastPitch == null)
		{
			if (center.getLengthSquared3() == 0)
			{
				//pitch doesn't make sense when centered at the origin:
				lastPitch = Angle.ZERO;
			}
			else
			{
				Vec4 forward = getForward();
				Vec4 centerNormalized = center.normalize3().multiply3(-1);
				double dot = forward.dot3(centerNormalized);
				lastPitch = dot >= 1.0 ? Angle.ZERO : dot <= -1.0 ? Angle.POS180 : Angle.fromRadians(Math.acos(dot));
			}
		}
		return lastPitch;
	}

	public void setPitch(Angle pitch)
	{
		if (center.getLengthSquared3() == 0)
		{
			//can't pitch around zero
			//TODO should we change eye position instead?
			return;
		}

		Angle current = getPitch();
		Angle delta = pitch.subtract(current);
		Quaternion q = Quaternion.fromRotationXYZ(delta, Angle.ZERO, Angle.ZERO);
		setRotation(rotation.multiply(q));
	}

	public void setCenterConstantEye(Vec4 center)
	{
		Vec4 eye = getEye();
		setCenter(center);
		setEye(eye);
	}

	/*public static void main(String[] args)
	{
		WorldWindViewState state = new WorldWindViewState();
		System.out.println("Center = " + v4ts(state.getCenter()));
		System.out.println("Eye = " + v4ts(state.getEye()));
		System.out.println("Forward = " + v4ts(state.getForward()));
		System.out.println("Roll = " + state.getRoll());
		state.setEye(new Vec4(-0.5, -0.7, -0.9));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
		state.setEye(new Vec4(0.5, 0.7, 0.9));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
		state.setEye(new Vec4(0.7, 0.9, 0.5));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
		state.setEye(new Vec4(1.0, 0.0, 0.0));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
		state.setEye(new Vec4(0.0, 0.0, 1.0));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
		state.setEye(new Vec4(0.0, 0.0, -1.0));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());

		System.out.println();
		state.setEye(new Vec4(0.5, 0.7, 0.9));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
		state.setRoll(Angle.fromDegrees(-45));
		System.out.println("New eye = " + v4ts(state.getEye()) + ", roll = " + state.getRoll());
	}

	public static String v4ts(Vec4 v)
	{
		DecimalFormat df = new DecimalFormat("0.0########");
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		return "(" + df.format(v.x) + ", " + df.format(v.y) + ", " + df.format(v.z) + ", " + df.format(v.w) + ")";
	}*/
}
