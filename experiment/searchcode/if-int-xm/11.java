package com.gna.bstrds.backgammon;

import org.apache.http.conn.routing.RouteInfo.LayerType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;



public class TavliGame extends View{
	private Checker[][] singlesquare = null ;
	Player Bplayer ;
	Player Wplayer ;
	private int max_height ;
	private int max_width ;
	public static Board b ;
	Thread t = null ;
	private int x ;
	int linesHorizontal [] ;
	int linesVertical [];
	boolean whoplays = true;
	private int y ;
	private Paint paintforlines ;
	private Paint background ; 
	private Paint paintcheckers ;
	private Paint paintfornumbers ;
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
			case 0 :
				invalidate();break ;
			default :
					break ;
			}
			super.handleMessage(msg);
		}
	};
	public TavliGame(Context context,int maxh , int maxw) {
		super(context);
		paintforlines = new Paint();
		this.paintforlines.setARGB(0,255,0,0);
		this.paintforlines.setAntiAlias(true);
		this.paintforlines.setStyle(Style.STROKE);
		this.paintforlines.setStrokeWidth(1);
		paintcheckers = new Paint();
		paintfornumbers = new Paint();
		this.paintfornumbers.setAntiAlias(true);
		this.paintfornumbers.setTextSize(30);
		this.paintfornumbers.setColor(Color.GREEN);
		max_height = maxh ;
		int keno = 0;
		max_width = maxw ;
		//-200 it was
		/*
		 * TODO
		 * this is where we set the text in the right
		 * max_width = maxw - 200 ,will give us 200 pixels to the right :D
		 * TODO
		 * TODO
		 */
		x = (int)(max_width / 24) ;
		y = x ;
		int xm = max_height ;
		int akro_panw = (int)(x/2-3);
		//single square orisma,me tis times twn draw ;)
		//x = 15 
		//y = 12 
		
		
		b = new Board();
		
		linesHorizontal = new int[13];
		linesVertical = new int[14];
		
		//TODO Setting lines
		//canvas.drawLine(0, (xm/2),  ym , (xm/2), paintforlines);
		//zwgrafisma tis orizontias grammis stin mesi
		linesHorizontal[7]=(int)(xm/2);
		
		for (int i = 0 ; i<6 ; i++ ){
			//canvas.drawLine(0, (x*i)+akro_panw, max_width, (x*i)+akro_panw, paintforlines);
			linesHorizontal[i]=(int)((x*i)+akro_panw);
		}
		//zwgrafisma twn panw orizontiwn
		for (int i = 0 ; i <6 ;i++){
			//canvas.drawLine(0, max_height - (x*i)-akro_panw,max_width , max_height - (x*i)-akro_panw,paintforlines);
			linesHorizontal[12-i]= (int)( max_height - (x*i)-akro_panw);
		}
		//zwgrafisma twn katw orizontiwn
		
		//canvas.drawLine(2.6F*x, 0, 2.6F*x, max_height, paintforlines);
		linesVertical[0]= (int)(2.6F*x);
		
		//zwgrafisma tis prwtis kathetis 
		int y = 1;
		for(float i=4.05F;i<=12;i+=1.5F){
			
			//canvas.drawLine(i*x, 0, i*x, max_height, paintforlines);
			linesVertical[y]=(int)(i*x) ;
			y++ ;
		}
		//zwgrafisma twn allwn 5 kathetwn
		
		//canvas.drawLine(max_width-(2.6F*x),0,max_width-(2.6F*x),max_height,paintforlines);
		//zwgrafisma tis prwtis apo ta deksia 
		linesVertical[13]=(int)(max_width-(2.6F*x/2));
		y= 1 ;
		for(float i=4.05F;i<=12;i+=1.5F){
			//canvas.drawLine(max_width-(i*x), 0, max_width-(i*x), max_height, paintforlines);
			linesVertical[13-y]=(int)(max_width-(i*x));
			y++;
		}
		//TODO End of setting lines
		singlesquare = new Checker[14][13];
		for(int j =0;j<13;j++)
		{
		for(int i = 0 ; i<14 ; i ++)
		{
			
				singlesquare[i][j]= new CheckerEmpty(linesHorizontal[j],linesVertical[i]);
			}
		}
		
		
	}
	public void setPlayers(Player Bplayer,Player Wplayer)
	{
		this.Bplayer = Bplayer ;
		this.Wplayer = Wplayer ;
	}
	public Player getPlayerB()
	{
		return Bplayer ;
	}
	public Player getPlayerW()
	{
		return Wplayer ;
	}
	
	
	public void play()
	{

		
		byte d1,d2;
			
		switch(b.getLastColPlayed()) {
			
			case Board.B:
				
				//System.out.println("White moves");
				
				
				Wplayer.roll();
				
				/* this means human player */
				
					
				//System.out.println("White rolled "+Wplayer.getD1()+" and "+Wplayer.getD2()+" .");
				d1 = this.Wplayer.getD1();
				d2 = this.Wplayer.getD2();

					
				b = new Board(Wplayer.MiniMax(b, d1, d2));
				
				b.setLastColPlayed(Board.W);

				
					
				
				
				
				break;
			
			case Board.W:
				
				System.out.println("Black moves");
				
				
				Bplayer.roll();
				
				d1 = this.Bplayer.getD1();
				d2 = this.Bplayer.getD2();
				
					
				
				//System.out.println("Black rolled "+d1+" and "+Bplayer.getD2()+" .");
					
				b = new Board(Bplayer.MiniMax(b, d1, d2));
					
				b.setLastColPlayed(Board.B);
				
					
				
				
				break;
			default:
				break;
		}
		
		handler.sendMessage(Message.obtain(handler, 0));
		
		System.out.println("Board value = "+b.getValue());

		
		
		
		
	}
	protected void onDraw(Canvas canvas ){
		//zwgrafisma twn plagiwn grammwn 
		//allazei to y ;
		//menei stathero to width ;
		int xm = max_height ;
		int ym = max_width ;
		int akro_panw = (int)(x/2-3);
		int akro_deksia ;
		
		Bitmap board = BitmapFactory.decodeResource(getResources(), R.drawable.tavli_resized);
		canvas.drawBitmap(board,null,new Rect(0,0,max_width,max_height), new Paint());
		
		
		canvas.drawLine(0, (xm/2),  ym , (xm/2), paintforlines);
		//zwgrafisma tis orizontias grammis stin mesi
		//linesHorizontal[7]=(int)(xm/2);
		
		for (int i = 0 ; i<6 ; i++ ){
			canvas.drawLine(0, (x*i)+akro_panw, max_width, (x*i)+akro_panw, paintforlines);
			//linesHorizontal[i]=(int)((x*i)+akro_panw);
		}
		//zwgrafisma twn panw orizontiwn
		for (int i = 0 ; i <6 ;i++){
			canvas.drawLine(0, max_height - (x*i)-akro_panw,max_width , max_height - (x*i)-akro_panw,paintforlines);
			//linesHorizontal[12-i]= (int)( max_height - (x*i)-akro_panw);
		}
		//zwgrafisma twn katw orizontiwn
		
		canvas.drawLine(2.6F*x, 0, 2.6F*x, max_height, paintforlines);
		linesVertical[0]= (int)(2.6F*x);
		
		//zwgrafisma tis prwtis kathetis 
		//int y = 0;
		for(float i=4.25F;i<=12;i+=1.5F){
			canvas.drawLine(i*x, 0, i*x, max_height, paintforlines);
			//linesVertical[y]=(int)(i*x) ;
			//y++ ;
		}
		//zwgrafisma twn allwn 5 kathetwn
		
		canvas.drawLine(max_width-(2.6F*x),0,max_width-(2.6F*x),max_height,paintforlines);
		//zwgrafisma tis prwtis apo ta deksia 
		linesVertical[13]=(int)(max_width-(2.6F*x));
		//y= 0 ;
		for(float i=4.25F;i<=12;i+=1.5F){
			canvas.drawLine(max_width-(i*x), 0, max_width-(i*x), max_height, paintforlines);
			//linesVertical[13-y]=(int)(max_width-(i*x));
			//y++;
		}
		//zwgrafisma twn allwn 5 kathetwn apo ta deksia 
		
		
		
		
		
		
		canvas.save();
		drawBoard(canvas,b);
		//DrawBoardTR(canvas,b);
		canvas.restore();
		
		
		super.onDraw(canvas);
		
	}
	public boolean onTouchEvent(MotionEvent event)
	{
		
		return super.onTouchEvent(event);
		
	}
	
	public void drawBoard(Canvas canvas,Board board){
		
		
		Position pos[] = new Position[28];
		Bitmap checker_color ;
		pos = board.getPositions();
		
		byte d1,d2;
		
		switch(board.getLastColPlayed()) {
			case Board.W : 
				
				d1 = Bplayer.getD1();
				d2 = Bplayer.getD2();
				
				canvas.drawText(Integer.toString(d1), linesVertical[6]+x/4, linesHorizontal[5]+x , paintfornumbers);	
				canvas.drawText(Integer.toString(d2), linesVertical[6]+x/4,linesHorizontal[7]-x/2 , paintfornumbers);
				canvas.drawText("kenny plays", linesVertical[2], linesHorizontal[5]+2*x, paintfornumbers);
				
				break;
				
			case Board.B :
				
				d1 = Wplayer.getD1();
				d2 = Wplayer.getD2();
				
				canvas.drawText(Integer.toString(d1), linesVertical[6]+x/4, linesHorizontal[5]+x , paintfornumbers);	
				canvas.drawText(Integer.toString(d2), linesVertical[6]+x/4,linesHorizontal[7]-x/2 , paintfornumbers);
				canvas.drawText("stan plays", linesVertical[2], linesHorizontal[5]+2*x, paintfornumbers);
		}
		
		for (int i = 1 ; i <25 ; i++){
			if(pos[i].getCol()!=Board.EMPTY){
					if(pos[i].getCol()==Board.B){
						checker_color = BitmapFactory.decodeResource(getResources(), R.drawable.p1);
					}
					else
					{
						checker_color = BitmapFactory.decodeResource(getResources(), R.drawable.p2);
					}
				byte numberofcheckers = pos[i].getNum();
				boolean outnumbered = false ;
				int extracheckers = 0 ;
				if(numberofcheckers>5){
					outnumbered = true ;
					extracheckers = numberofcheckers - 5 ;
					numberofcheckers = 5 ;
				}
				if(i<7)
				{
					int yaw = 13 -i ;
					//for(int j = 0 ; j<numberofcheckers;j++)
					for(int j= numberofcheckers-1 ; j >=0;j--)
					{
						canvas.save();
						canvas.drawBitmap(checker_color, null,new Rect(linesVertical[yaw],linesHorizontal[j],linesVertical[yaw+1],linesHorizontal[j+1]),paintcheckers);
						canvas.restore();
					}
					if(outnumbered){
						canvas.save();
						canvas.drawText(Integer.toString(extracheckers), linesVertical[13-i]+x/2,linesHorizontal[5]+x , paintfornumbers);
						canvas.restore();
					}
				}
				else if (i<13)
				{
					int yaw = 12-i ;
					//for(int j = 0 ; j<numberofcheckers;j++)
					for(int j = numberofcheckers - 1 ; j >= 0 ; j--)
					{
						canvas.save();
						canvas.drawBitmap(checker_color, null,new Rect(linesVertical[yaw],linesHorizontal[j],linesVertical[yaw+1],linesHorizontal[j+1]),paintcheckers);
						canvas.restore();
					}
					if(outnumbered){
						canvas.save();
						canvas.drawText(Integer.toString(extracheckers), linesVertical[12-i]+x/2,linesHorizontal[5]+x , paintfornumbers);
						canvas.restore();
					}
				}
						
				else if(i<19)
				{
					//TODO na dwsw kapoio sovaro onoma sto kapoios
					int yaw = i - 13 ;
					for(int j=0 ; j<numberofcheckers ; j++)
					{
						canvas.save();
						canvas.drawBitmap(checker_color, null,new Rect(linesVertical[yaw],linesHorizontal[11-j],linesVertical[yaw+1],linesHorizontal[12-j]),paintcheckers);
						canvas.restore();
						//canvas.drawBitmap(checker_color, null,new Rect(linesVertical[kapoios],linesHorizontal[11-j],linesVertical[kapoios+1],linesHorizontal[12-j]),new Paint());
					}
					if(outnumbered){
						//TODO
						canvas.save();
						canvas.drawText(Integer.toString(extracheckers), linesVertical[yaw]+x/2,linesHorizontal[7]-x , paintfornumbers);
						canvas.restore();
					}
					
				}
				else if(i<25)
				{
					int yaw = i -12 ;
					for(int j = 0 ; j<numberofcheckers;j++)
					{
						canvas.save();
						canvas.drawBitmap(checker_color, null,new Rect(linesVertical[yaw],linesHorizontal[11-j],linesVertical[yaw+1],linesHorizontal[12-j]),paintcheckers);
						canvas.restore();
					}
					if(outnumbered){
						//TODO
						canvas.save();
						canvas.drawText(Integer.toString(extracheckers), linesVertical[yaw]+x/2,linesHorizontal[7]-x , paintfornumbers);
						canvas.restore();
					}
				}	
			}
		}	
		
		/* displays eaten white pills */
		if(pos[0].getNum()>0) {
			checker_color = BitmapFactory.decodeResource(getResources(), R.drawable.p2);
			canvas.drawBitmap(checker_color, null,new Rect(linesVertical[0]-2*x-2,linesHorizontal[0],linesVertical[1]-2*x-2,linesHorizontal[1]),paintcheckers);
			canvas.drawText(Integer.toString(pos[0].getNum()), linesVertical[0]-1.5F*x-2,linesHorizontal[2], paintfornumbers);
		}
		
		/* displays eaten black pills */
		if(pos[25].getNum()>0) {
			checker_color = BitmapFactory.decodeResource(getResources(), R.drawable.p1);
			canvas.drawBitmap(checker_color, null,new Rect(linesVertical[0]-2*x-2,linesHorizontal[11],linesVertical[1]-2*x-2,linesHorizontal[12]),paintcheckers);
			canvas.drawText(Integer.toString(pos[25].getNum()), linesVertical[0]-1.5F*x-2,linesHorizontal[11]-x/3, paintfornumbers);
		}

		if(pos[27].getNum()>0) {
			checker_color = BitmapFactory.decodeResource(getResources(), R.drawable.p1);
			canvas.drawBitmap(checker_color, null,new Rect(linesVertical[12]+2*x+4,linesHorizontal[0],linesVertical[13]+2*x+4,linesHorizontal[1]),paintcheckers);
			canvas.drawText(Integer.toString(pos[27].getNum()), linesVertical[13]+x,linesHorizontal[2], paintfornumbers);
		}
		
		if(pos[26].getNum()>0) {
			checker_color = BitmapFactory.decodeResource(getResources(), R.drawable.p2);
			canvas.drawBitmap(checker_color, null,new Rect(linesVertical[12]+2*x+4,linesHorizontal[11],linesVertical[13]+2*x+4,linesHorizontal[12]),paintcheckers);
			canvas.drawText(Integer.toString(pos[26].getNum()), linesVertical[13]+x,linesHorizontal[11]-x/3, paintfornumbers);
		}
		
	}
}

