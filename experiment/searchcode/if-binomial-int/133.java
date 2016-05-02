package FIT_9202_Machulskis.Knot;

import java.util.Vector;

/**
 * User: violetta
 * Date: 5/17/12
 * Time: 2:12 AM
 */
public class Spline implements SplineInterface
{
    Vector<Vector3> points;
    /*Bezier spline*/
    Spline(Vector<Vector3> points)
    {
        this.points = points;
    }

    public Vector3 valueAt(double param)
    {
        int size = points.size();

        Vector3 dest = new Vector3(0, 0, 0);

        for(int i = 0; i < size; i++)
        {
            double coef = binomial(size - 1, i) * Math.pow(param, i) * Math.pow(1 - param, size - 1 - i);

            dest = dest.add(points.get(i).mul(coef));
        }

        return dest;
    }

    long binomial(long n, long k)
    {
        if(k==0 || n==k)
        {
            return 1;
        }

        return binomial(n - 1, k - 1) + binomial(n - 1, k);
    }
}

