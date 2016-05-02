/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shoganai.sph;

/**
 *
 * @author kosuke
 */
public class Solver {

    ParticleSet ps;
    final float gravity = -9.81f;
    final float K = 1.2f;//1.2f; //1.5f; //8.314472 *300  #gas constant * 300K * mol -> m^3 Pa
    final float V = 1.0e-3f*1000;//1.0e-3f; //1.5f;
    private float dt;
    private float kernelRadius;

    public static float poly6(float h2, float r2) {
        if (h2 < r2) {
            return 0.0f;
        }
        final float h2_r2 = h2 - r2;
        return h2_r2 * h2_r2 * h2_r2;
    }

    public static float coeffPoly6(float h) {
        return (float) (4.0 / (Math.PI * (Math.pow(h, 8))));//9
        //return (float) (315.0 / (64.0 * Math.PI * (Math.pow(h, 9))));//9
    }

    public static float spiky(float h_r) {
        if(h_r< 0 ) return 0.0f;
        return h_r * h_r * h_r;
    }

    public static float coeffSpiky(float h) {
        return (float) (10.0 / (Math.PI * (Math.pow(h, 5))));
        //return (float) (-3.0 * 15.0 / (Math.PI * (Math.pow(h, 6))));
    }

    public static float gradSpiky(float h_r) {
        //if(h_r< 0 ) return 0.0f;
        return h_r * h_r;
    }

    public static float coeffGradSpiky(float h) {
        return (float) (-30.0 / (Math.PI * (Math.pow(h, 5))));
        //return (float) (-3.0 * 15.0 / (Math.PI * (Math.pow(h, 6))));
    }

    public static float coeffViscosity(float h) {
        return  (float) (60.0 / (Math.PI * (Math.pow(h, 5))));
        //return (float) (30.0 / (Math.PI * (Math.pow(h, 5))));
        //return (float) (45.0 / (Math.PI * (Math.pow(h, 6))));
    }

    public void setParticleSet(ParticleSet ps) {
        this.ps = ps;
        this.kernelRadius = ps.getRadius() * 4.1f;
    }

    public void setDt(float dt) {
        this.dt = dt;
    }

    public void update() {
        //clear();
        //boundary();
        density();
        force();
        boundary();
        move();
    }

    private void density() {
        final int length = ps.getLength();
        final float[] mass = ps.getMass();
        final float[] pos = ps.getPosition();
        final float[] den = ps.getDensity();
        final float[] pre = ps.getPressure();
        final float rho0 = ps.getRestDensity();
        final float h = kernelRadius;
        final float h2 = h * h;
        final float coeff = coeffPoly6(h);
        //final float coeff = coeffSpiky(h);
        final float m = mass[0];

        for (int i = 0; i < length; ++i) {
            final float xi = pos[i * 2];
            final float yi = pos[i * 2 + 1];
            float rho = 0.0f;
            for (int j = 0; j < length; ++j) {
                if (i == j) {
                    continue;
                }
                float xx = xi - pos[j * 2];
                float yy = yi - pos[j * 2 + 1];
                if(xx == Float.NaN || yy == Float.NaN)
                    continue;
                rho += poly6(h2, xx * xx + yy * yy);
                //rho += spiky((float) (h - Math.hypot(xx, yy)));
            }
            rho *= coeff * m;
            den[i] = rho ;//+ rho0 ;
            pre[i] = K * (rho - (rho0));
            //System.out.println(String.format("d:%4.4f, p:%4.4f | K:%4.4f, d0:%4.4f, m:%4.4f", rho, pre[i], K, rho0, m));
        }
    }

    private void force() {
        final int length = ps.getLength();
        final float[] mass = ps.getMass();
        final float[] pos = ps.getPosition();
        final float[] vel = ps.getVelocity();
        final float[] den = ps.getDensity();
        final float[] pre = ps.getPressure();
        final float[] acc = ps.getAcc();
        final float h = kernelRadius;
        final float coeffSpiky = coeffGradSpiky(h);
        final float coeffV = coeffViscosity(h) * V;
        final float m = mass[0];

        for (int i = 0; i < length; ++i) {
            final float pi = pre[i];
            final float d = den[i];
            final float xi = pos[i * 2];
            final float yi = pos[i * 2 + 1];
            final float vxi = vel[i * 2];
            final float vyi = vel[i * 2 + 1];
            float fx = 0.0f, fy = 0.0f;
            for (int j = 0; j < length; ++j) {
                if (i == j) {
                    continue;
                }
                final float xx = xi - pos[j * 2];
                final float yy = yi - pos[j * 2 + 1];
                if(xx == Float.NaN || yy == Float.NaN)
                    continue;
                final float r = (float) Math.hypot(xx, yy);
                if (h < r) {
                    continue;
                }
                final float vxx = vel[j * 2] - vxi;
                final float vyy = vel[j * 2 + 1] - vyi;
                final float dj = den[j];
                final float h_r = h - r;
                float a = -0.5f * (pi + pre[j]) * gradSpiky(h_r) * coeffSpiky;
                float b = h_r * coeffV;
                a /= r;
                fx += (a * xx + b * vxx) / dj;
                fy += (a * yy + b * vyy) / dj;
            }

            if(Math.abs(d)>1.0){
                acc[i * 2] = m*fx / d;
                acc[i * 2 + 1] = (m*fy + gravity*this.ps.getRestDensity())/d ;
            }else{
                acc[i * 2] = 0;
                acc[i * 2 + 1] = gravity;
            }
        }
    }

    private void move() {
        final int length = ps.getLength();
        final float t = this.dt;
        final float[] acc = ps.getAcc();
        float[] vel = ps.getVelocity();
        float[] pos = ps.getPosition();
        for (int i = 0; i < length; ++i) {
            vel[i * 2] += acc[i*2]*t*0.5f ;
            vel[i * 2 + 1] += acc[i*2+1]*t*0.5f;
            pos[i * 2] += vel[i * 2] *t;
            pos[i * 2 + 1] += vel[i * 2 + 1]*t;
            vel[i * 2] += acc[i*2]*t*0.5f ;
            vel[i * 2 + 1] += acc[i*2+1]*t*0.5f;
        }
    }
/*
    private void boundary() {
        final int length = ps.getLength();
        final float r = ps.getRadius();
        final float[] acc = ps.getAcc();
        float[] pos = ps.getPosition();
        float[] box = {0.0f, 0.0f, 1.0f, 1.0f};

        for (int i = 0; i < length; ++i) {
            final float x = pos[i * 2];
            final float y = pos[i * 2 + 1];

            if (x - r < box[0] && acc[i * 2] < 0){
                acc[i * 2] *= -0.9f;
            }else if(box[2] < (x + r) && acc[i * 2] > 0) {
                acc[i * 2] *= -0.9f;
            }

            if (y - r < box[1] && acc[i * 2 +1] < 0){
                acc[i * 2 + 1] *= -0.9f;            
            }else if(box[3] < (y + r) && acc[i * 2 + 1] > 0) {
                acc[i * 2 + 1] *= -0.9f;
            }
        }
    }
*/

    private void boundary() {
        final float t = this.dt;
        final int length = ps.getLength();
        final float r = ps.getRadius();
        float[] vel = ps.getVelocity();
        float[] pos = ps.getPosition();
        float[] box = {0.0f, 0.0f, .5f, .5f};

        for (int i = 0; i < length; ++i) {
            final float x = pos[i * 2];
            final float y = pos[i * 2 + 1];

            if (x - r < box[0]){
                vel[i * 2] *= -0.99f;
                pos[i * 2] = box[0] + r;
            }
            else if(box[2] < (x + r)){
                vel[i * 2] *= -0.99f;
                pos[i * 2] = box[2] - r;
            }

            if (y - r < box[1]){
                vel[i * 2 + 1] *= -0.99f;
                pos[i * 2 + 1] = box[1] + r;
            }else if(box[3] < (y + r)) {
                vel[i * 2 + 1] *= -0.99f;
                pos[i * 2 + 1] = box[3] - r;
            }
        }
    }
    
    private void clear() {
        final int length = ps.getLength();
        final float[] acc = ps.getAcc();
        for (int i = 0; i < length; ++i) {
            acc[i * 2] = .0f;
            acc[i * 2 + 1] = .0f;
        }
    }
}

