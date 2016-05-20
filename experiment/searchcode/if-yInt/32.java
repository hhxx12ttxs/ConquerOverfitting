package it.univpm.dii.marvinPID;

import org.opencv.core.Rect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/** Control of ER1 robot direction using dominant face position */
public class ER1Direction {
	
	/** Error for PID */
	private float	ErrorX;
	private float	ErrorY;
	private float	ErrorZ;

	private Paint	paint;
	private Paint	paintRECT;
	
	private int	width;
	private int	heigh;

	private float	ErrorMax_X;
	private float	ErrorMax_Y;
	private float	ErrorMax_Z;
	
	private float	coordX=-999.0f;
	private float	coordY=-999.0f;
	
	private int	Ydirection=0;
	private int	Xdirection=0;
	private int	Zdirection=0;

	/* data is the array where the error are stored and sensed to arduino */
	private byte data[]=new byte[16];
	
	private int SetPoint_onZ=130;

	/* msg is the array for convertinf 1 float to 4 byte */
	int msg[]=new int[4];	
	
	String output;
	
	String stringaX;
	String stringaY;
	String stringaZ;

	
	/**
	 * Initialize robot video frame limits for face detected position 
	 * @param width		int
	 * @param height	int
	 */
	public void init(int width, int height){
		this.heigh=height;
		this.width=width;
		output = "";

		paint=new Paint();
		paint.setColor(Color.RED);
		paint.setTextSize(20);
		
		paintRECT=new Paint();
		paintRECT.setColor(Color.RED);
		paintRECT.setStyle(Paint.Style.STROKE);

	}
	
	/**
	 * Updates robot video frame limits for face detected position 
	 * @param width		int
	 * @param height	int
	 */
	
	public void updateLimits(int width, int height){
		this.heigh=height;
		this.width=width;
	}

	/**
	* Elaborates dominant face position into frame and decides robot direction, due to previous set limits
	* @param a		{@link Rect}	face position
	*/

	public void calc(Rect a,float offsetx, float offsety){
		
		if (a!=null){
			output="PID";
			data[0]=0x0C;

			coordX= (float) ((a.tl().x)+(a.width/2))+offsetx;
			coordY= (float) ((a.tl().y)+(a.height/2))+offsety;

			ErrorX=(int) ((width/2)-(a.tl().x+(a.width/2)));
			ErrorY=(int) ((heigh/2)-(a.tl().y+(a.height/2)));

			ErrorZ=(int) ((SetPoint_onZ)-(a.height));


			//USED to decide the direction of the robot : 0-> UP 1->DOWN
			if(ErrorY > 0) Ydirection=0;
				else Ydirection=1;

			//USED to decide the direction of the robot : 1-> positive(LEFT) 0->negative(RIGHT)
			if(ErrorX > 0) Xdirection=1;
				else Xdirection=0;

			//USED to decide the direction of the robot : 0-> positive(FORWARD) 1->negative(BACKWARD)
			if(ErrorZ > 0) Zdirection=0; //
				else Zdirection=1;

			//Now i take the absolute values of errors
			ErrorY=Math.abs(ErrorY);
			ErrorX=Math.abs(ErrorX);
			ErrorZ=Math.abs(ErrorZ);

			// Limita l'errore sull'asse Z infatti il volto pu? diventare molto grosso se
			// esso ? vicino alla telecamera e quindi l'errore pu? crescere molto in
			// forward rispetto a backward cos? allora unifico i valori.
			if(Zdirection ==1 && ErrorZ >=15)
				ErrorZ=14;

			// Inserisco delle tolleranze su ogni asse
			if(ErrorY <=4 )
				ErrorY=0;
			else
				ErrorY=ErrorY-4; //else eliminate the acceptable zone

			if(ErrorX <=10 )
				ErrorX=0;
			else
				ErrorX=ErrorX-10;//else eliminate the acceptable zone

			if(ErrorZ <=5 )
				ErrorZ=0;
			else
				ErrorZ=ErrorZ-5;//else eliminate the acceptable zone

			//Normalize the errors: ErrorK/ErrorMax_K  con K={X,Y,Z}
			ErrorMax_X =  ((width/2+offsetx)/2);
			ErrorMax_Y =  ((width/2+offsetx)/2);
			ErrorMax_Z =  20;

			ErrorX=ErrorX/ErrorMax_X;
			ErrorY=ErrorY/ErrorMax_Y;
			ErrorZ=ErrorZ/ErrorMax_Z;

			/*trasfomate the FLOAT ErrorX,ErrorY,ErroZ into a 4 bytes array and put them in data[]*/

			// java method that with some bit's masks convert the float bits to bit and
			//display bit with a int -> see official reference api
			int Xint=Float.floatToIntBits(ErrorX);
			int Yint=Float.floatToIntBits(ErrorY);
			int Zint=Float.floatToIntBits(ErrorZ);

			//now i separate the int Xint into 4 bytes array
			msg[0] = ((Xint >> 24) & 0xFF) ;
			msg[1] = ((Xint >> 16) & 0xFF) ;
			msg[2] = ((Xint >> 8) & 0XFF);
			msg[3] = ((Xint & 0XFF));

			//copy into the data[] array, that will be sended to flex, Xdirection and 0-3 msg byte
			data[1]=(byte)Xdirection;
			data[2]=(byte)msg[0];
			data[3]=(byte)msg[1];
			data[4]=(byte)msg[2];
			data[5]=(byte)msg[3];

			//now i separate the int Yint into 4 bytes array
			msg[0] = ((Yint >> 24) & 0xFF) ;
			msg[1] = ((Yint >> 16) & 0xFF) ;
			msg[2] = ((Yint >> 8) & 0XFF);
			msg[3] = ((Yint & 0XFF));

			//copy into the data[] array, that will be sended to flex, Ydirection and 0-3 msg byte
			data[6]=(byte)Ydirection;
			data[7]=(byte)msg[0];
			data[8]=(byte)msg[1];
			data[9]=(byte)msg[2];
			data[10]=(byte)msg[3];

			//now i separate the int Yint into 4 bytes array
			msg[0] = ((Zint >> 24) & 0xFF) ;
			msg[1] = ((Zint >> 16) & 0xFF) ;
			msg[2] = ((Zint >> 8) & 0XFF);
			msg[3] = ((Zint & 0XFF));

			//copy into the data[] array, that will be sended to flex, Ydirection and 0-3 msg byte
			data[11]=(byte)Zdirection;
			data[12]=(byte)msg[0];
			data[13]=(byte)msg[1];
			data[14]=(byte)msg[2];
			data[15]=(byte)msg[3];

			//and then i send them by the function
			//in the try catch statement
			//FdActivity.mServer.send(data);

			// QUINDI QUELLO CHE HO ORA ? UN ARRAY DI 4 BYTE CHE RAPPRESENTANO IL FLOAT
			// A QUESTO PUNTO LO INVIO AD ARDUINO CHE LO PASSA COS? COM'? ALLA FLEX
			// SUCCESSIVAMENTE LA FLEX RICOMPATTER? QUESTI 4 BYTE IN UN FLOAT CON UN METODO
			// CHE PUOI VEDERE SUL CODICE DELLA FLEX
	
		} else {
			int j=0;
			output="PID STOPPED";
			data[0] = 0x0C;
			//put the signal of stop 
			data[1] = 0x0B;
			data[2] = 0x12;
			data[3] = 0x0B;
			//for the draw
			ErrorX=ErrorY=ErrorZ=0;
			coordX=coordY=-999.0f;
		}
    	try{
    		//structure of data[]: Control byte for Arduino[0] - X[1-5] - Y[6-11] - Z [12-16] 
    		FdActivity.mServer.send(data);
    		
		}catch(Exception e) { }
	}
	/**
	 * Paint graphics to canvas application holder
	 * @param canvas		{@link Canvas}	target bitmap
	 * @param offsetx		float			start x position
	 * @param offsety		float			start y position
	 */
	public void draw(Canvas canvas, float offsetx, float offsety) {

		Paint punto=new Paint();
		punto.setColor(Color.GREEN);
		punto.setStrokeWidth(10);
		
		//print a line parallel to the y-axis
		canvas.drawLine(width/2+offsetx, 0+offsety, width/2+offsetx, heigh+offsety, paint); 
		
		//print a line parallel to the x-axis
		canvas.drawLine(0+offsetx, heigh/2+offsety, width+offsetx, heigh/2+offsety, paint);

		//print the Acceptable zone where error = 0
		canvas.drawRect(width/2+offsetx-10, heigh/2+offsety-4, width/2+offsetx+10,  heigh/2+offsety+4, paintRECT);

		canvas.drawText(output+" "+FdView.faceCheck, 20 + offsetx, 90, paint);

		//print the point on Y
		if(coordX != -999.0f && coordY != -999.0f) {
			canvas.drawPoint(coordX, coordY, punto);

			canvas.drawText("ERROR on X:"+ ErrorX, 20 + offsetx, 200,paint);
			canvas.drawText("ERROR on Y:"+ ErrorY, 20 + offsetx, 220, paint);
			canvas.drawText("ERROR on Z:"+ ErrorZ, 20 + offsetx, 240, paint);

			if(Xdirection == 0)
				stringaX = "RIGHT";
			else
				stringaX = "LEFT";

			if(Ydirection == 0)
				stringaY = "UP";
			else
				stringaY = "DOWN";

			if(Zdirection == 0)
				stringaZ = " FORWARD";
			else
				stringaZ = "BACKWARD";

			canvas.drawText(stringaX, 240+ offsetx, 200,paint);
			canvas.drawText(stringaY, 240 + offsetx, 220, paint);
			canvas.drawText(stringaZ, 240 + offsetx, 240, paint);
		}
	}
}

