package com.kprojekt.slickArcanoid.game;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

/**
 * 
 */

public abstract class Bouncible
{
	public static enum BouncibleType
	{
		PADDLE, BRICK, ENVIRONMENT
	}

	public static class MyLine extends Line
	{
		private static final long serialVersionUID = 1L;

		public MyLine( float x1, float y1, float x2, float y2 )
		{
			super( x1, y1, x2, y2 );
		}

		public MyLine( MyLine mirror )
		{
			super( mirror.getX1(), mirror.getY1(), mirror.getX2(), mirror.getY2() );
		}

		private float getA()
		{
			return (this.getY2() - this.getY1()) / (this.getX2() - this.getX1());
		}

		private float getB()
		{
			return this.getY1() - getA() * this.getX1();
		}

		public boolean isLeftUpOf( Vector2f p ) throws SlickException
		{
			if( p == null )
			{
				throw new SlickException( "p can not be null" );
			}

			return p.y > getA() * p.x + getB();
		}

		/**
		 * creates a perpendicular line to self in the given x, y point (which has to be on the line)
		 */
		public MyLine paralel( float x )
		{
			float a = this.getA();
			float b = this.getB() + x;

			float x1 = this.getX1();
			float y1 = a * x1 + b;
			float x2 = this.getX2();
			float y2 = a * x2 + b;

			return new MyLine( x1, y1, x2, y2 );
		}

		public MyLine rotate( int degree )
		{
			float halfX = (this.getX1() + (this.getX2() - this.getX1()) / 2f);
			float halfY = (this.getY1() + (this.getY2() - this.getY1()) / 2f);
			return this.rotate( degree, new Vector2f( halfX, halfY ) );
		}

		/**
		 * Returns bounced line from mirror 
		 * @param g 
		 */
		public MyLine bounce( MyLine mirror ) throws SlickException
		{
			//1. get the intersection point
			Vector2f ip = intersect( mirror );
			if( ip != null )
			{
				//2. get closest point to R
				Vector2f rayStart = this.getStart();
				//3. get D point on another
				Vector2f normalThroughR = new Vector2f( 1, 1 );
				mirror.getClosestPoint( rayStart, normalThroughR );
				//4. get alpha 
				double a = rayStart.distance( normalThroughR );
				double b = rayStart.distance( ip );

				boolean overMirror = mirror.isLeftUpOf( rayStart );
				double alpha = Math.toDegrees( Math.asin( a / b ) );
				MyLine tmp = rotate( 180 - 2 * alpha, ip );
				boolean afterRotateOverMirror = mirror.isLeftUpOf( tmp.getEnd() );
				if( overMirror == afterRotateOverMirror )
					return tmp;

				tmp = rotate( 180 + 2 * alpha, ip );
				return tmp;
			}
			return null;
		}

		public MyLine bounce2( MyLine mirror, Graphics g )
		{
			Vector2f ip = intersect( mirror );
			if( ip != null )
			{
				if( g != null )
					g.drawOval( ip.x - 2, ip.y - 2, 4, 4 );

				Vector2f Rdir = new Vector2f( this.getStart().x - ip.x, this.getStart().y - ip.y ).normalise();

				Vector2f R = new Vector2f( Rdir.x * this.length() + ip.x, Rdir.y * this.length() + ip.y );
				if( g != null )
				{
					g.setColor( Color.white );
					g.drawOval( R.x - 13, R.y - 13, 26, 26 );
				}

				Vector2f D = new Vector2f( 1, 1 );
				new MyLine( mirror ).getClosestPoint( R, D );
				if( g != null )
				{
					g.setColor( Color.blue );
					g.drawOval( D.x - 12, D.y - 12, 24, 24 );
				}

				Vector2f toR = new Vector2f( R.x - D.x, R.y - D.y );
				if( g != null )
				{
					g.setColor( Color.yellow );
					g.drawLine( D.x, D.y, D.x + toR.x, D.y + toR.y );
				}

				Vector2f dPrim = new Vector2f( ip ).sub( D );
				Vector2f rPrim = new Vector2f( D.x + 2 * dPrim.x + toR.x, D.y + 2 * dPrim.y + toR.y );
				if( g != null )
				{
					g.setColor( Color.orange );
					g.setLineWidth( 2 );
					g.drawLine( ip.x, ip.y, rPrim.x, rPrim.y );
				}

				return new MyLine( ip.x, ip.y, rPrim.x, rPrim.y );
			}
			return null;
		}

		/**
		 * Rotates this line around ip point of degree degrees
		 */
		public MyLine rotate( double degree, Vector2f ip )
		{
			float[] temp = new float[4];
			float halfX = ip.getX();
			float halfY = ip.getY();

			createPoints();
			MyLine l = new MyLine( this.points[0], this.points[1], this.points[2], this.points[3] );
			Transform transform = Transform.createTranslateTransform( -halfX, -halfY );

			l.createPoints();
			transform.transform( l.points, 0, temp, 0, 2 );
			l = new MyLine( temp[0], temp[1], temp[2], temp[3] );

			l.createPoints();
			transform = Transform.createRotateTransform( (float)Math.toRadians( degree ) );
			transform.transform( l.points, 0, temp, 0, 2 );
			l = new MyLine( temp[0], temp[1], temp[2], temp[3] );

			l.createPoints();
			transform = Transform.createTranslateTransform( halfX, halfY );
			transform.transform( l.points, 0, temp, 0, 2 );
			l = new MyLine( temp[0], temp[1], temp[2], temp[3] );

			return l;
		}

	}

	private float imagePosX, imagePosY;
	private Image image;
	private Shape shape;

	public Bouncible( int posX, int posY, Image image )
	{
		this.imagePosX = posX;
		this.imagePosY = posY;
		this.image = image;
		this.shape = new Polygon( new float[] { posX, posY, posX + image.getWidth(), posY, posX + image.getWidth(),
				posY + image.getHeight(), posX, posY + image.getHeight() } );
	}

	public MyLine getLine( int i )
	{
		int p = i * 2;
		float[] points = this.shape.getPoints();

		return new MyLine( points[p], points[p + 1], points[p + 2], points[p + 3] );
	}

	public void rotate( int i )
	{
		this.image.rotate( i );

		this.shape = this.shape.transform( Transform.createTranslateTransform(
				-(this.imagePosX + this.image.getWidth() / 2), -(this.imagePosY + this.image.getHeight() / 2) ) );
		this.shape = this.shape.transform( Transform.createRotateTransform( (float)Math.toRadians( i ) ) );
		this.shape = this.shape.transform( Transform.createTranslateTransform( this.imagePosX + this.image.getWidth()
				/ 2, this.imagePosY + this.image.getHeight() / 2 ) );
	}

	public void move( float x, float y, GameContainer gc )
	{
		float newX = this.imagePosX + x;
		float newY = this.imagePosY + y;
		if( newX - this.image.getWidth() / 2 < 0 )
		{
			newX = this.image.getWidth() / 2;
		}
		if( newY + this.image.getWidth() / 2 > gc.getWidth() )
		{
			newY = gc.getWidth() - this.image.getWidth() / 2;
		}
		this.shape = this.shape.transform( Transform.createTranslateTransform( newX - imagePosX, newY - imagePosY ) );

		this.imagePosX = newX;
		this.imagePosY = newY;

	}

	public void setMiddleXY( float x, float y, GameContainer gc )
	{
		float movedX = x - this.imagePosX - this.image.getWidth() / 2;
		float movedY = y - this.imagePosY - this.image.getHeight() / 2;
		this.move( movedX, movedY, gc );
	}

	protected abstract BouncibleType whoAmI();

	public MyLine intersects( float x, float y, float dirX, float dirY )
	{
		Line ray = new Line( x, y, x + dirX, y + dirY );

		float[] points = this.shape.getPoints();
		float closestDist = Float.MAX_VALUE;
		MyLine closestLine = null;
		for( int i = 0; i < points.length; i += 2 )
		{
			float p1x = points[i];
			float p1y = points[i + 1];
			float p2x = points[(i + 2) % points.length];
			float p2y = points[(i + 3) % points.length];
			MyLine line = new MyLine( p1x, p1y, p2x, p2y );
			Vector2f intersectionPoint = new Vector2f();
			boolean intersects = ray.intersect( line, true, intersectionPoint );
			if( intersects )
			{
				float distTmp = line.distance( new Vector2f( x, y ) );
				if( distTmp < closestDist )
				{
					closestDist = distTmp;
					closestLine = line;
				}
			}
		}
		if( closestLine != null )
		{
			return closestLine;
		}
		return null;
	}

	public abstract void wasHit();

	public void render( Graphics g )
	{
		this.image.draw( this.imagePosX, this.imagePosY );
		for( int i = 0; i < 4; i++ )
		{
			float[] point = this.shape.getPoint( i );
			float[] point2 = this.shape.getPoint( (i + 1) % 4 );
			g.setColor( Color.white );
			g.drawLine( point[0], point[1], point2[0], point2[1] );
		}

	}

	public Vector2f getBouncedDirection( float middleX, float middleY, Vector2f direction )
	{
		// TODO @Krzysiek Auto-generated method stub
		return null;
	}
}

