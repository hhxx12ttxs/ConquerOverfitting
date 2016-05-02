package io.process.verostko.graphics;
import io.process.verostko.Calc;

/**
 * Immutable AffineTransformation class.
 *
 */
public class AffineTransformation implements Transform
{
	protected double[] values = {1, 0, 0, 1, 0, 0};
	protected AffineTransformation inverse = null;
	public final static AffineTransformation IDENTITY = new AffineTransformation();
	
	/**
	 * Creates an instance of AffineTransformation with an identity matrix as transformation matrix.
	 */
	public AffineTransformation()
	{
		this(new double[] {1, 0, 0, 1, 0, 0} );
	}
	
	/**
	 * Creates an instance of AffineTransform with the supplied transformation matrix.
	 * @param matrix
	 */
	public AffineTransformation(double[] m) 
	{	
		values = m;
	}

	/**
	 * Creates an instance of AffineTransformation that is a copy of the one given.
	 * @param tr The AffineTransformation that will be copied.
	 */
	public AffineTransformation(AffineTransformation tr)
	{
		values = tr.getElements();
	}
	
	/**
	 * Returns a derived rotated transformation.
	 * @param angle The angle by  which to rotate.
	 * @return This transformation concatenated by a rotation.
	 */
	public AffineTransformation rotate(double angle)
	{
		return concat(new AffineTransformation(new double[] {Math.cos(angle), -Math.sin(angle), Math.sin(angle), Math.cos(angle), 0, 0} ));
	}
	
	/**
	 * Returns a derived translated transformation.
	 * @param xt	The horizontal translation.
	 * @param yt	The vertical translation.
	 * @return	The derived translation.
	 */
	public AffineTransformation translate(double xt, double yt)
	{
		return concat(new AffineTransformation(new double[] {1, 0, 0, 1, xt, yt} ));
	}
	
	public AffineTransformation translate(Point p)
	{
		return translate(p.x, p.y);
	}
	
	/**
	 * Returns a derived scaled transformation.
	 * @param xs	The horizontal scale factor.
	 * @param ys	The vertical scale vector.
	 * @return	The derived transformation.
	 */
	public AffineTransformation scale(double xs, double ys)
	{
		return concat(new AffineTransformation(new double[] {xs, 0, 0, ys, 0, 0}));
	}
	
	/**
	 * Applies the transformation matrix on a point and returns the transformed version.
	 * @param p The original point.
	 * @return The transformed point.
	 */
	public Point transform(Point p) 
	{
		double[] v = getElements();
		double x = v[0]*p.getX()+v[2]*p.getY()+v[4];
		double y = v[1]*p.getX()+v[3]*p.getY()+v[5];
		
		return new Point(x, y);
	}
	
	/**
	 * Returns the scaling elements of the transformation as a Point.
	 * @return
	 */
	public Point getScale()
	{
		double[] v = getElements();
		return new Point(v[0], v[3]);
	}
	
	/**
	 * Returns the translating elements of the transformation as a Point.
	 * @return
	 */
	public Point getTranslate()
	{
		double[] v = getElements();
		return new Point(v[4], v[5]);
	}
	
	/**
	 * Returns the elements of the transformation matrix as a double array. Let the matrix be:
	 * <p><table>
	 *   <tr><td>m00</td><td> m01</td><td>m02</td></tr>
	 *   <tr><td>m10</td><td> m11</td><td>m12</td></tr>
	 *   <tr><td>0</td><td>0</td><td>1</td></td></tr>
	 * </table></p>
	 * 
	 * <p>The returned double array will then look like: { m00, m10, m01, m11, m02, m12 }</p>
	 * @return	The elements of the transformation matrix.
	 */
	public double[] getElements() 
	{
		return values;
	}
	
	protected double[][] matrix()
	{
		double[] e = getElements();
		return new double[][] {{e[0], e[2], e[4]}, {e[1], e[3], e[5] }, {0, 0, 1}};
	}
	
	/**
	 * Concatenates a transformation to this one and returns it.
	 * @param tr	The concatenated transformation.
	 */
	public AffineTransformation concat(AffineTransformation tr)
	{
		final double[][] c = Calc.matrixMultiply(matrix(), tr.matrix());
		
		return new AffineTransformation(new double[] { c[0][0], c[1][0], c[0][1], c[1][1], c[0][2], c[1][2] });
	}
	
	/** Pre-concatenates a transformation to this one and returns it.
	 * @param tr
	 * @return	The pre-concatenated transformation.
	 */
	public AffineTransformation preConcat(AffineTransformation tr)
	{
		final double[][] c = Calc.matrixMultiply(tr.matrix(), matrix());
		
		return new AffineTransformation(new double[] { c[0][0], c[1][0], c[0][1], c[1][1], c[0][2], c[1][2] });
	}
	
	/**
	 * Returns the concatenation of a transformation to this one. TODO elaborate.
	 * @param tr
	 * @return
	 */
	public AffineTransformation getConcatenation(AffineTransformation tr)
	{
		return tr.inverse().concat(tr); 
	}
	
	/**
	 * Returns the inverse transformation.
	 * @return	The inverse transformation.
	 */
	public AffineTransformation inverse()
	{
		if (inverse == null)
			inverse = new AffineTransformation(inverseValues());
		
		return inverse;
	}
	
//	public AffineTransformation scalarMultiply(double s)
//	{
//		return new AffineTransformation(matrix.scalarMultiply(s));
//	}
	
	public boolean equals(Object o)
	{
		try
		{
			final AffineTransformation tr = (AffineTransformation)o;
			
			final double[] trElements = tr.getElements();
			final double[] elements = tr.getElements();
			
			for (int i = 0; i < 6; i++)
				if (trElements[i] != elements[i])
					return false;
			
			return true;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}
	
	protected double[] inverseValues()
	{
		double[] v = getElements();
		
		final double a = v[0];
		final double b = v[2];
		final double c = v[1];
		final double d = v[3];
		
		double m = 1 / ((a*d)-(b*c));
		double[] i = new double[] {m*d, -m*c, -m*b, m*a};
		
		double x = -(i[0]*v[4] + i[2]*v[5]);
		double y = -(i[1]*v[4] + i[3]*v[5]);
		
		return new double[] {i[0], i[1], i[2], i[3], x, y };
	}
	
	public String toString()
	{
		return values[0] + ", " + values[2] + ", " +  values[4] + "\n"
			 + values[1] + ", " + values[3] + ", " +  values[5];
	}
	
	public boolean isTranslation()
	{
		return (values[0] == 1 && values[1] == 0 && values[2] == 0 && values[3] == 1);
	}
	
	public boolean isScale()
	{
		return (values[1] == 0 && values[2] == 0 && values[4] == 0 && values[5] == 0);
	}
	
	public boolean isIdentity()
	{
		return (values[1] == 0 && values[2] == 0 && values[4] == 0 && values[5] == 0 
				&& values [0] == 1 && values[3] == 1);
	}
}

