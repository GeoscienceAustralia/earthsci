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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class WorldWindViewState
{
	//constants
	protected final static Vec4 INITIAL_FORWARD = Vec4.UNIT_Z;
	protected final static Vec4 NEGATIVE_INITIAL_FORWARD = INITIAL_FORWARD.multiply3(-1);
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
	protected Angle lastWorldRoll;

	protected void clearCachedValues()
	{
		lastEye = null;
		lastUp = null;
		lastForward = null;
		lastSide = null;
		lastRotationMatrix = null;
		lastRoll = null;
		lastWorldRoll = null;
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
		this.rotation = rotation;
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
			lastForward = INITIAL_FORWARD.transformBy3(rotation);
		}
		return lastForward;
	}

	public Vec4 getUp()
	{
		if (lastUp == null)
		{
			lastUp = Vec4.UNIT_Y.transformBy3(rotation);
		}
		return lastUp;
	}

	public Vec4 getSide()
	{
		if (lastSide == null)
		{
			lastSide = Vec4.UNIT_X.transformBy3(rotation);
		}
		return lastSide;
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
		Angle worldRoll = getWorldRoll();
		Vec4 newForwardUnnormalized = center.subtract3(eye);
		Vec4 newForward = newForwardUnnormalized.normalize3();
		double dot = INITIAL_FORWARD.dot3(newForward);
		Quaternion newRotation = dot >= 1.0 ? Quaternion.IDENTITY :
				dot <= -1.0 ? Quaternion.fromAxisAngle(Angle.POS180, Vec4.UNIT_Y) :
						Quaternion.fromAxisAngle(Angle.fromRadians(Math.acos(dot)), INITIAL_FORWARD.cross3(newForward));
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
		if (lastWorldRoll == null)
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
			lastWorldRoll = dot >= 1.0 ? Angle.ZERO : dot <= -1.0 ? Angle.POS180 :
					Angle.fromRadians(Math.signum(side.dot3(relativeUp)) * Math.acos(dot));
		}
		return lastWorldRoll;
	}

	public void setWorldRoll(Angle angle)
	{
		Angle current = getWorldRoll();
		Angle delta = angle.subtract(current);
		setRoll(getRoll().add(delta));
	}

	public void setCenterConstantEye(Vec4 center)
	{
		Vec4 eye = getEye();
		setCenter(center);
		setEye(eye);
	}

	public Matrix getTransform()
	{
		Vec4 u = getUp();
		Vec4 f = getForward();
		Vec4 s = getSide();
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
}
