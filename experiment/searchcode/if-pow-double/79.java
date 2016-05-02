package logic.main;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;

import tool.component.DrawingObserver;
import define.concept.Circle;
import define.concept.DialogData;
import define.concept.Polygon;
import define.concept.Rectangle;

public class MC_Model implements DrawingObserver
{
	private int m_FrameHeight;
	private int m_FrameWidth;
	private int m_DrawPanelHeight;
	private int m_DrawPanelWidth;
	private int mf_FrameHeight;
	private int mf_FrameWidth;
	private int mf_DrawPanelHeight;
	private int mf_DrawPanelWidth;
	
	private ArrayList<Rectangle> m_Rectangle;
	private ArrayList<Circle> m_Circle;

	private ArrayList<DialogData> m_DiaglogData;
	
	private ArrayList<Integer> m_ShapeStack;
	private int m_NowShape; // 1- rectangle, 2 - circle, 3 - polygon, 4 - eraser
	private Color m_NowColor;
	private int m_NowMode;
	
	private Image m_BackgroundImage;
	private int m_ImageWidth;
	private int m_ImageHeight;
	
	private int m_StartX,m_StartY,m_EndX,m_EndY;
	
	private int[][] m_Bitmap;
	
	//constructure
	public MC_Model()
	{
		m_FrameHeight = 500;
		m_FrameWidth = 500;
		m_DrawPanelHeight = 0;
		m_DrawPanelWidth = 0;
		mf_FrameHeight = 500;
		mf_FrameWidth = 500;
		mf_DrawPanelHeight = 0;
		mf_DrawPanelWidth = 0;
		
		m_Rectangle = new ArrayList<Rectangle>();
		m_Circle = new ArrayList<Circle>();
		
		m_DiaglogData = new ArrayList<DialogData>();
		
		m_ShapeStack = new ArrayList<Integer>();
		m_NowShape = 0;
		m_NowColor = Color.BLACK;
		m_NowMode = 1;
		
		m_BackgroundImage = null;
		m_ImageWidth = 0;
		m_ImageHeight = 0;
		
		m_StartX = 0;
		m_StartY = 0;
		m_EndX = 0;
		m_EndY = 0;
		
		m_Bitmap = new int[100][100];
	}
	
	
	//logic
	
	public void saveShape()
	{
		if(m_NowShape == 1)
		{
			Rectangle temp = new Rectangle();
			temp.setStart(m_StartX, m_StartY);
			temp.setEnd(m_EndX, m_EndY);
			temp.setColor(m_NowColor);
			m_Rectangle.add(temp);
			m_ShapeStack.add(m_NowShape);
		}
		else if(m_NowShape == 2)
		{
			Circle temp = new Circle();
			temp.setStart(m_StartX, m_StartY);
			temp.setEnd(m_EndX, m_EndY);
			temp.setColor(m_NowColor);
			m_Circle.add(temp);
			m_ShapeStack.add(m_NowShape);
		}
		else if(m_NowShape == 3)
		{
			
		}
	}
	
	public void pushShapeStack()
	{
		m_ShapeStack.add(m_NowShape);
	}
	
	public int popShapeStack()
	{
		if((m_ShapeStack.size()-1) >=0)
		{
			int temp = m_ShapeStack.get(m_ShapeStack.size()-1);
			m_ShapeStack.remove(m_ShapeStack.size()-1);
			return temp;
		}
		return -1;
	}
	
	public boolean isImageSet()
	{
		if(m_BackgroundImage == null)
		{
			return false;
		}
		return true;
	}
	
	public void removeShape(int shape)
	{
		if(shape == 1)
		{
			if((m_Rectangle.size()-1)>=0)m_Rectangle.remove(m_Rectangle.size()-1);
		}
		else if(shape == 2)
		{
			if((m_Circle.size() -1) >=0) m_Circle.remove(m_Circle.size()-1);
		}
		else if(shape == 3)
		{
			
		}
	}
	
	public void transEmptyToBitmap()
	{
		int start1 = (m_ImageWidth*100)/mf_DrawPanelWidth;
		int start2 = (m_ImageHeight*100)/mf_DrawPanelHeight;
		
		if(m_ImageHeight - mf_DrawPanelHeight == 0)
		{
			for(int i=0; i<100; i++)
			{
				for(int k=start1; k<100; k++)
				{
					m_Bitmap[i][k] = 1;
				}
			}
		}
		else if(m_ImageWidth - mf_DrawPanelWidth == 0)
		{
			for(int i=start2; i<100; i++)
			{
				for(int k=0; k<100; k++)
				{
					m_Bitmap[i][k] = 1;
				}
			}
		}
		else
		{
			//bug catcher part
		}
	}
	
	public void transCirToBitmap()
	{
		int[] start = new int[2];
		int[] end = new int[2];
		
		start[0] = m_StartX;
		start[1] = m_StartY;
		end[0] = m_EndX;
		end[1] = m_EndY;
		
		double PA = Math.pow(Math.abs(start[0] - end[0])/2.0, 2.0);
		double PB = Math.pow(Math.abs(start[1] - end[1])/2.0, 2.0);
			
		int[] origin = new int[2];
		origin[0] = (start[0] + end[0])/2;
		origin[1] = (start[1] + end[1])/2;
		
		if(start[0] > end[0])
		{
			int temp = start[0];
			start[0] = end[0];
			end[0] = temp;
		}
		
		if(start[1] > end[1])
		{
			int temp = start[1];
			start[1] = end[1];
			end[1] = temp;
		}
		
		for(int x = start[0]; x<=end[0]; x++)
		{
			int oneX;
			int[] twoY = new int[2];
			twoY[0] =  (int)(origin[1] +  Math.sqrt(PB*(1.0 - (Math.pow((double)(x-origin[0]), 2.0)/PA))));
			twoY[1] = (int)(origin[1] -  Math.sqrt(PB*(1.0 - (Math.pow((double)(x-origin[0]), 2.0)/PA))));
			twoY[0] = (twoY[0] * 100) / mf_DrawPanelHeight;
			twoY[1] = (twoY[1] * 100) / mf_DrawPanelHeight;
			oneX = (x * 100) / mf_DrawPanelWidth;
			
			for(int y = twoY[1]; y<twoY[0] & y<100; y++ )
			{
				m_Bitmap[y][oneX] = 1;
			}
		}
		
		m_StartX = 0;
		m_StartY = 0;
		m_EndX = 0;
		m_EndY = 0;
	}
	
	public void transAllCirToBitmap()
	{
		for(int count = 0; count <m_Circle.size(); count++)
		{
			int[] start = new int[2];
			int[] end = new int[2];
			
			start[0] = m_Circle.get(count).getSX();
			start[1] = m_Circle.get(count).getSY();
			end[0] = m_Circle.get(count).getEX();
			end[1] = m_Circle.get(count).getEY();
		
			double PA = Math.pow(Math.abs(start[0] - end[0])/2.0, 2.0);
			double PB = Math.pow(Math.abs(start[1] - end[1])/2.0, 2.0);
			
			int[] origin = new int[2];
			origin[0] = (start[0] + end[0])/2;
			origin[1] = (start[1] + end[1])/2;
		
			if(start[0] > end[0])
			{
				int temp = start[0];
				start[0] = end[0];
				end[0] = temp;
			}
		
			if(start[1] > end[1])
			{
				int temp = start[1];
				start[1] = end[1];
				end[1] = temp;
			}
		
			for(int x = start[0]; x<=end[0]; x++)
			{
				int oneX;
				int[] twoY = new int[2];
				twoY[0] =  (int)(origin[1] +  Math.sqrt(PB*(1.0 - (Math.pow((double)(x-origin[0]), 2.0)/PA))));
				twoY[1] = (int)(origin[1] - Math.sqrt(PB*(1.0 - (Math.pow((double)(x-origin[0]), 2.0)/PA))));
				twoY[0] = (twoY[0] * 100) / mf_DrawPanelHeight;
				twoY[1] = (twoY[1] * 100) / mf_DrawPanelHeight;
				oneX = (x * 100) / mf_DrawPanelWidth;
			
				for(int y = twoY[1]; y<twoY[0] & y<100; y++ )
				{
					m_Bitmap[y][oneX] = 1;
				}
			}
		}
	}
	
	public void transRecToBitmap()
	{
		int[] start = new int[2];
		int[] end = new int[2];

		start[0] = m_StartX;
		start[1] = m_StartY;
		end[0] = m_EndX;
		end[1] = m_EndY;
		
		int c_startX = (100*m_StartX)/mf_DrawPanelWidth;
		int c_startY = (100*m_StartY)/mf_DrawPanelHeight;
		int c_endX = (100*m_EndX)/mf_DrawPanelWidth;
		int c_endY = (100*m_EndY)/mf_DrawPanelHeight;
		
		if(c_startX>c_endX)
		{
			int temp = c_startX;
			c_startX = c_endX;
			c_endX = temp;
		}
		
		if(c_startY>c_endY)
		{
			int temp = c_startY;
			c_startY = c_endY;
			c_endY = temp;
		}
		
		for(int y=c_startY; y<=c_endY & y<100; y++)
		{
			for(int x=c_startX; x<=c_endX & x<100; x++)
			{
				m_Bitmap[y][x] = 1;
			}
		}
		
		m_StartX = 0;
		m_StartY = 0;
		m_EndX = 0;
		m_EndY = 0;
	}
	
	public void transAllRecToBitmap()
	{
		for(int count = 0; count<m_Rectangle.size(); count++)
		{
			int[] start = new int[2];
			int[] end = new int[2];

			start[0] = m_Rectangle.get(count).getSX();
			start[1] = m_Rectangle.get(count).getSY();
			end[0] = m_Rectangle.get(count).getEX();
			end[1] = m_Rectangle.get(count).getEY();
		
			int c_startX = (100*start[0])/mf_DrawPanelWidth;
			int c_startY = (100*start[1])/mf_DrawPanelHeight;
			int c_endX = (100*end[0])/mf_DrawPanelWidth;
			int c_endY = (100*end[1])/mf_DrawPanelHeight;
		
			if(c_startX>c_endX)
			{
				int temp = c_startX;
				c_startX = c_endX;
				c_endX = temp;
			}
		
			if(c_startY>c_endY)
			{
				int temp = c_startY;
				c_startY = c_endY;
				c_endY = temp;
			}
		
			for(int y=c_startY; y<=c_endY & y<100; y++)
			{
				for(int x=c_startX; x<=c_endX & x<100; x++)
				{
					m_Bitmap[y][x] = 1;
				}
			}
		}
	}
	
	public void clearBitmap()
	{
		m_Bitmap = new int[100][100];
	}
	
	public void addDialogData(String URL, String title, String context)
	{
		DialogData temp = new DialogData(URL, title, context);
		m_DiaglogData.add(temp);
	}
	
	public void addDialogData(DialogData dialogdata)
	{
		m_DiaglogData.add(dialogdata);
	}
	
	//setter
	
	public void setDialogImage(int index, Image image)
	{
		DialogData temp = m_DiaglogData.get(index);
		temp.setImage(image);
	}
	
	public void setDialogImageURL(int index, String URL)
	{
		DialogData temp = m_DiaglogData.get(index);
		temp.setImageURL(URL);
	}
	
	public void setDialogTitle(int index, String title)
	{
		DialogData temp = m_DiaglogData.get(index);
		temp.setTitle(title);
	}
	
	public void setDialogContext(int index, String context)
	{
		DialogData temp = m_DiaglogData.get(index);
		temp.setContext(context);
	}
	
	public void setMode(int mode)
	{
		m_NowMode = mode;
	}
	
	public void setStartXY(int x, int y)
	{
		if(m_DrawPanelWidth < x)
		{
			m_StartX = m_DrawPanelWidth;
		}
		else if(x <0)
		{
			m_StartX = 0;
		}
		else
		{
			m_StartX = x;
		}
		
		if(m_DrawPanelHeight < y)
		{
			m_StartY = m_DrawPanelHeight;
		}
		else if( y < 0)
		{
			m_StartY = 0;
		}
		else
		{
			m_StartY = y;
		}
	}
	
	public void setEndXY(int x, int y)
	{
		if(m_DrawPanelWidth < x)
		{
			m_EndX = m_DrawPanelWidth;
		}
		else if(x <0)
		{
			m_EndX = 0;
		}
		else
		{
			m_EndX = x;
		}
		
		if(m_DrawPanelHeight < y)
		{
			m_EndY = m_DrawPanelHeight;
		}
		else if( y < 0)
		{
			m_EndY = 0;
		}
		else
		{
			m_EndY = y;
		}
	}
	
	public void setLineColor(Color color)
	{
		m_NowColor = color;
	}
	
	public void setShape(int shape)
	{
		m_NowShape = shape;
	}
	
	public void setDrawingPanelSize(int width, int height)
	{
		m_DrawPanelHeight = height;
		m_DrawPanelWidth = width;
	}
	
	public void setImage(Image image)
	{
		m_BackgroundImage = image;
	}
	
	public void setImageSize(int width, int height)
	{
		m_ImageWidth = width;
		m_ImageHeight = height;
	}
	
	public void setFrameSize(int width, int height)
	{
		m_FrameHeight = height;
		m_FrameWidth = width;
	}
	
	public void setFakeDrawPanelSize(int width, int height)
	{
		mf_DrawPanelHeight = height;
		mf_DrawPanelWidth = width;
	}
	
	public void setFakeFrameSize(int width, int height)
	{
		mf_FrameWidth = width;
		mf_FrameHeight = height;
	}
	
	//getter
	public int getDialogDataSetSize()
	{
		return m_DiaglogData.size();
	}
	
	public ArrayList<DialogData> getDialogDataSet()
	{
		return m_DiaglogData;
	}
	
	public int getNowMode()
	{
		return m_NowMode;
	}
	
	public int getStartX()
	{
		return m_StartX;
	}
	
	public int getStartY()
	{
		return m_StartY;
	}
	
	public int getEndX()
	{
		return m_EndX;
	}
	
	public int getEndY()
	{
		return m_EndY;
	}
	
	public Color getNowLineColor()
	{
		return m_NowColor;
	}
	
	public int getNowShape()
	{
		return m_NowShape;
	}
	
	public int getDrawPanelHeight()
	{
		return m_DrawPanelHeight;
	}
	
	public int getDrawPanelWidth()
	{
		return m_DrawPanelWidth;
	}
	
	public Image getImage()
	{
		return m_BackgroundImage;
	}
	
	public int getImageWidth()
	{
		return m_ImageWidth;
	}
	
	public int getImageHeight()
	{
		return m_ImageHeight;
	}
	
	public int[][] getBitmap()
	{
		return m_Bitmap;
	}
	
	public int getFrameWidth()
	{
		return m_FrameWidth;
	}
	
	public int getFrameHeight()
	{
		return m_FrameHeight;
	}
	
	public int getFakeDrawPanelHeight()
	{
		return mf_DrawPanelHeight;
	}
	
	public int getFakeDrawPanelWidth()
	{
		return mf_DrawPanelWidth;
	}
	
	public int getFakeFrameWidth()
	{
		return mf_FrameWidth;
	}
	
	public int getFakeFrameHeight()
	{
		return mf_FrameHeight;
	}

	@Override
	public ArrayList<Rectangle> getRectangle() {
		return m_Rectangle;
	}

	@Override
	public ArrayList<Circle> getCircle() {
		return m_Circle;
	}

	@Override
	public ArrayList<Polygon> getPolygon() {
		
		return null;
	}
}

