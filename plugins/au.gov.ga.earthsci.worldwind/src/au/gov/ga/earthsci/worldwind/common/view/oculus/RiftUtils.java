package au.gov.ga.earthsci.worldwind.common.view.oculus;

import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Quaternion;
import gov.nasa.worldwind.geom.Vec4;

import com.oculusvr.capi.OvrMatrix4f;
import com.oculusvr.capi.OvrQuaternionf;
import com.oculusvr.capi.OvrVector3f;

/**
 * Utility class used for converting types between the JOVR library and World
 * Wind.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RiftUtils
{
	public static Vec4 toVec4(OvrVector3f v)
	{
		return new Vec4(v.x, v.y, v.z);
	}

	public static Quaternion toQuaternion(OvrQuaternionf q)
	{
		return new Quaternion(q.x, q.y, q.z, q.w);
	}

	public static Matrix toMatrix(OvrMatrix4f m)
	{
		return new Matrix(m.M[0], m.M[1], m.M[2], m.M[3], m.M[4], m.M[5], m.M[6], m.M[7], m.M[8], m.M[9], m.M[10],
				m.M[11], m.M[12], m.M[13], m.M[14], m.M[15]);
	}
}
