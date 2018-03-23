package dummy;

//CUSTOM ignore Box3D_F64

/**
 * Code which is to be converted into F32.  Note that text in comments is converted too.  double, _F64
 *
 * @author Peter Abeles
 */
public class DummyCode_F64 {
    public static double acuteAngle( double vx_a, double vy_a,
                                     double vx_b, double vy_b ) {
        double r_a = Math.sqrt( vx_a * vx_a + vy_a * vy_a );
        double r_b = Math.sqrt( vx_b * vx_b + vy_b * vy_b );

        return Math.acos( ( vx_a * vx_b + vy_a * vy_b ) / ( r_a * r_b ) );
    }

    public void encode(Sphere3D_F64 sphere, /**/double[] param) {
        param[0] = sphere.x;
        param[1] = sphere.y;
        param[2] = sphere.z;
        param[3] = sphere.radius;
    }

    public static class Sphere3D_F64 {
        public double x,y,z;
        public double radius;
    }

    public static class Box3D_F64 {
        public double x,y,z;
        public double width,height,length;
    }
}
