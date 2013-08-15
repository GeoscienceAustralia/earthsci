// Contains function and uniforms for projecting geodetic coordinates to cartesian.
//
// Uniforms:
//   - radius : The radius of the globe
//   - es: The globe eccentricity squared
//   - ve: Current vertical exaggeration
//
// Functions:
//   - vec3 geodeticToCartesian(vec3 geodetic)

uniform float radius; //globe radius
uniform float es; //eccentricity squared

// Project the given geodetic coordinates [lon, lat, elevation] into 
// the correct cartesian coordinates [x, y, z].
//
// @param geodetic The geodetic coordinates to project [lon, lat, elevation]
//
// @return The projected coordinates [x, y, z]
vec3 geodeticToCartesian(vec3 geodetic)
{
	vec4 cosSinLatLon = vec4(cos(geodetic.y), sin(geodetic.y), cos(geodetic.x), sin(geodetic.x));

    float rpm = radius / sqrt(1.0 - es * cosSinLatLon.y * cosSinLatLon.y);
    float x = (rpm + geodetic.z) * cosSinLatLon.x * cosSinLatLon.w;
    float y = (rpm * (1.0 - es) + geodetic.z) * cosSinLatLon.y;
    float z = (rpm + geodetic.z) * cosSinLatLon.x * cosSinLatLon.z;
    return vec3(x, y, z);
}