package ai.ann.image.filter.filter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Float;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import ai.ann.NeuralNetwork;
import ai.ann.NeuralNetworkFactory;
import ai.ann.Property;
import ai.ann.image.filter.beans.ColorRangeBean;
import ai.ann.image.filter.beans.ShapeBean;
import ai.ann.image.trainer.ColorFactory;

import util.essential.VariableValueGetter;

/**
 * @author S.A.M.G.
 * This class is to implemented to use a neural network
 * to filter an image.
 */
public class ImageFilter {
	/*private class MiniVisor extends Frame{
		private ImageComponent image;
		private final int SCALE = 3;
		public MiniVisor(int w,int h){
			this.image = new ImageComponent(w*this.SCALE,h*this.SCALE);
			this.add(this.image);
			this.pack();
			this.setVisible(true);
		}
		public void setImage(Image image){
			this.image.setImage(image.getScaledInstance(this.image.getWidth()*SCALE, this.image.getHeight()*SCALE, Image.SCALE_AREA_AVERAGING));
		}
	}*/
	/**
	 * This is the value getter that allows to get the configuration variable values.
	 */
	private final VariableValueGetter valueGetter = new VariableValueGetter(Property.CONFIGURATION_FILE_NAME);
	/**
	 * This constant contains the neural network path, with which will be recovered to allows the image filtering.
	 * This is set in the properties file. 
	 */
	private final String NEURAL_NETWORK_PATH = this.valueGetter.getStringValue("neural_network_path");
	/**
	 * This constant is the default background color.
	 */
	private final int BACKGROUND = Color.BLACK.getRGB();
	/**
	 * This constant allows to scale the input image to perform the analysis, and turn it back to
	 * the original scale. 
	 */
	private final int SCALE_OF_ANALYSIS = 4;
	/**
	 * This constant allows to set the analysis detail during the filtering execution.
	 * This is set in the properties file.
	 */
	private final short DETAIL = Short.parseShort(this.valueGetter.getStringValue("execution_detail"));
	/**
	 * This constant is to set the threshold to recognice a given color.
	 * The value that is filtering by the threshold is the value returned from the neural network
	 * like an uncertainty value.
	 * This is set in the properties file.
	 */
	private final double THRESHOLD = Double.parseDouble(this.valueGetter.getStringValue("threshold"));
	/**
	 * This constant allows to set the width analysis frame.
	 * This is set in the properties file.
	 */
	private final short WIDTH_TASTE = Short.parseShort(this.valueGetter.getStringValue("taste_size"));
	/**
	 * This constant allows to set the height analysis frame.
	 * This is set in the properties file.
	 */
	private final short HEIGHT_TASTE = Short.parseShort(this.valueGetter.getStringValue("taste_size"));
	/**
	 * This constant allows to set the step analysis in the X axi.
	 * This is set in the properties file.
	 */
	private final short WIDTH_TASTE_AREA = Short.parseShort(this.valueGetter.getStringValue("execution_taste_area_size"));
	/**
	 * This constant allows to set the step analysis in the Y axi.
	 * This is set in the properties file.
	 */
	private final short HEIGHT_TASTE_AREA = Short.parseShort(this.valueGetter.getStringValue("execution_taste_area_size"));
	/**
	 * This constant allows to enable or disable the intensity representation on the result image.
	 * This is set in the properties file.
	 */
	private final boolean WITH_RECOGNITION_INTENSITY = this.valueGetter.getStringValue("with_recognition_intensity").equals("true");
	/**
	 * If this is able, allows to increase the frame step by using the uncertainty value given by the neural network.
	 * This is set in the properties file.
	 */
	private final boolean INTELIGENT_TASTE_INCREASE = this.valueGetter.getStringValue("inteligent_taste_area_increase").equals("true");
	/**
	 * This constant allows to enable or disable the real texture representation on the result image.
	 * result image
	 */
	private final boolean REPRESENTATION_WITH_REAL_TEXTURE = this.valueGetter.getStringValue("representation_with_real_texture").equals("true");
	/**
	 * This constant allows to set the output colors depending on the neural network output.
	 */
	private final Color[] ANS_COLORS = {Color.RED,Color.GREEN,Color.BLUE,Color.DARK_GRAY,Color.LIGHT_GRAY,Color.WHITE};
	private LinkedList colorList;
	//private int detail;
	/**
	 * This variable is the neural network used for image filtering.
	 */
	private NeuralNetwork neuralNetwork;
	/*
	private MiniVisor miniVisor;
	/**/
	
	//public ImageFilter(LinkedList colorList,int detail) throws IOException, ClassNotFoundException{
	/**
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public ImageFilter() throws IOException, ClassNotFoundException{
		this.colorList = new LinkedList();
		for (int i = 0; i < this.ANS_COLORS.length; i++) {
			this.colorList.add(this.ANS_COLORS[i]);
		}
		//this.detail = detail;
		this.neuralNetwork = NeuralNetworkFactory.getInstance(this.NEURAL_NETWORK_PATH);
		System.out.println("Threshold: "+this.THRESHOLD);
		/*
		miniVisor = new MiniVisor(this.WIDTH_TASTE_AREA,this.HEIGHT_TASTE_AREA);
		/**/
	}
	
	/**
	 * This method filters an input "image" by using the "selectedColors", and depending on
	 * what is the uncertainty value given for the neural network.
	 * @param image is the input image
	 * @param selectedColors is the selected colors to filter the image.
	 * @return filtered image
	 */
	public BufferedImage geFiltered(BufferedImage image,boolean[]selectedColors){
		/**/
		int w = (image.getWidth()/this.SCALE_OF_ANALYSIS),h=(image.getHeight()/this.SCALE_OF_ANALYSIS);
		BufferedImage 	tmp = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB),
						area;
		tmp.getGraphics().drawImage(image.getScaledInstance(w, h, Image.SCALE_FAST), 0, 0, null);
		image = tmp;
		/**/
		w = image.getWidth();h=image.getHeight();
		tmp = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		Graphics g = tmp.getGraphics();
		/**
			g.drawImage(image, 0, 0, null);
		/**/
		Object[] recognizedInformation;
		Color recognizedColor;
		int widthScale = this.WIDTH_TASTE,
			heightScale = this.HEIGHT_TASTE,
			widthStep  = this.WIDTH_TASTE_AREA,heightStep = this.HEIGHT_TASTE_AREA;
		/**System.out.println("W="+widthScale+"; H="+heightScale);/**/
		double recognitionRate,rate,rateAvg=0,maxRate=0,minRate=Double.MAX_VALUE;
		int counter,counterRateAvg=0,heightStepAvg;
		for(int y=0;y<(h-this.HEIGHT_TASTE_AREA);y+=heightStep){
			heightStepAvg = counter = 0;
			for(int x=0;x<(w-this.WIDTH_TASTE_AREA);x+=widthStep){
				area =  new BufferedImage(widthScale, heightScale,BufferedImage.TYPE_INT_RGB);
				area.getGraphics().drawImage(image.getSubimage(x, y, widthStep, heightStep).getScaledInstance(widthScale, heightScale, Image.SCALE_AREA_AVERAGING), 0, 0, null);
				/*
				this.miniVisor.setImage(area);
				/**/
				counter++;
				recognizedInformation = recognizeColor(area,this.ANS_COLORS,this.THRESHOLD);
				if(recognizedInformation!=null){
					recognitionRate = ((Double)recognizedInformation[1]).doubleValue();
					//Calculing rate average.
					rateAvg+=recognitionRate;
					counterRateAvg++;
					maxRate = (recognitionRate>maxRate)?recognitionRate:maxRate;
					minRate = (recognitionRate<minRate)?recognitionRate:minRate;
					if(this.INTELIGENT_TASTE_INCREASE){
						rate = ((
								((int)(this.WIDTH_TASTE_AREA*recognitionRate))>=1||
								((int)(this.HEIGHT_TASTE_AREA*recognitionRate))>=1)?
										recognitionRate:1);
						/**System.out.println("\tW="+widthStep+"; H="+heightStep+" -> rate="+rate+" = "+(this.WIDTH_TASTE_AREA*rate)+"|"+(this.HEIGHT_TASTE_AREA*rate));/**/
						widthStep  = (int) (this.WIDTH_TASTE_AREA*rate);
						heightStepAvg += (int) (this.HEIGHT_TASTE_AREA*rate);
					}else{
						widthStep  = this.WIDTH_TASTE_AREA;
						heightStepAvg += this.HEIGHT_TASTE_AREA;
					}
					recognizedColor = (Color)recognizedInformation[0];
					if(recognizedColor!=null&&this.isSelectedColor(recognizedColor,selectedColors)){
						if(this.WITH_RECOGNITION_INTENSITY){
							recognizedColor = new Color(((int)(recognizedColor.getRed()*recognitionRate)),
														((int)(recognizedColor.getGreen()*recognitionRate)),
														((int)(recognizedColor.getBlue()*recognitionRate)));
						}
						if(this.REPRESENTATION_WITH_REAL_TEXTURE){
							area =  new BufferedImage(this.WIDTH_TASTE_AREA, this.HEIGHT_TASTE_AREA,BufferedImage.TYPE_INT_RGB);
							area.getGraphics().drawImage(image.getSubimage(x, y, this.WIDTH_TASTE_AREA, this.HEIGHT_TASTE_AREA), 0, 0, null);
							g.drawImage(area, x, y, null);
						}else{
							g.setColor(recognizedColor);
							g.fillRect(x,y,this.DETAIL,this.DETAIL);
						}
					}
				}else{
					widthStep  = this.WIDTH_TASTE_AREA;
					heightStepAvg += this.WIDTH_TASTE_AREA;
				}
			}
			heightStep = heightStepAvg/counter;
		}
		/**System.out.println(	"* Threshold:\n"+
								"\t- Average = "+((counterRateAvg>0)?(rateAvg/counterRateAvg):0)+ ".\n" +
								"\t- Max = "+maxRate+ ".\n" +
								"\t- Min = "+minRate+ ".\n" +
								"\t- Sugested = "+((((rateAvg/counterRateAvg)+maxRate+minRate)/3)-minRate)+".");/**/
		/**/
		w = (image.getWidth()*this.SCALE_OF_ANALYSIS);h=(image.getHeight()*this.SCALE_OF_ANALYSIS);
		image = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		image.getGraphics().drawImage(tmp.getScaledInstance(image.getWidth()/*2*/, h, Image.SCALE_FAST), 0, 0, null);
		tmp = image;
		/**/
		return tmp;
	}
	
	/**
	 * This method allows to know whether the recognized color is one of the selected colors.
	 * @param recognizedColor is the current recognized color. 
	 * @param selectedColors is the set of posible recognized colors.
	 * @return whether is or not a selected color. (true = yes; false = not)
	 */
	private boolean isSelectedColor(Color recognizedColor,boolean[]selectedColors){
		for (int i = 0; (i < selectedColors.length) && (i < this.ANS_COLORS.length); i++) {
			if(selectedColors[i]&&recognizedColor==this.ANS_COLORS[i]){
				return true;
			}
		}
		return false;
	}
	
	//**************************************************************************************
	//**************************************************************************************
	/**
	 * 5: Wall. (true)
	 * 0: Free space. (false)
	 * File Structure:
	 * Entrada = [Raw],[Column]
	 * Salida = [Raw],[Column]
	 * 
	 * 					<Exit Here>
	 * 555555555555555555550055555555555555555
	 * иииииииииииииииииииииииииииииииииииииии
	 * иииииииииииииииииииииииииииииииииииииии
	 * иииииииииииииииииииииииииииииииииииииии
	 * иииииииииииииииииииииииииииииииииииииии
	 * иииииииииииииииииииииииииииииииииииииии
	 * 555555555555550000555555555555555555555
	 * 			<Enter Here>
	 * 
	 * Enter location: Left and Down.
	 * Exit location: Right and Up.
	 * 
	 * @param image is the image to analyze.
	 */
	private void generateMatrix(BufferedImage image){
		//final int SCALE_FACTOR = 10;
		/*BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(),BufferedImage.TYPE_INT_RGB);
			tmp.getGraphics().drawImage(image.getScaledInstance(tmp.getWidth(), tmp.getHeight(), BufferedImage.SCALE_FAST),0,0,null);*/
		BufferedImage tmp = image;
		final int black=Color.BLACK.getRGB();
		int color;
		LinkedList ans = new LinkedList();
		StringBuffer str;
		boolean[][]plane=new boolean[tmp.getHeight()-1][tmp.getWidth()-1];
		for (int y = 0; y < (tmp.getHeight()-1); y++) {
			str = new StringBuffer();
			for (int x = 0; x < (tmp.getWidth()-1); x++) {
				color = tmp.getRGB(x, y);
				plane[y][x] = (color!=black);
				str.append((plane[y][x])?"5":"0");
				
			}
			ans.add(str);
		}
		//final int LOOKING_LIMIT = 5, PERMISIVE_AVG = 50;
		final int LOOKING_LIMIT = 3, PERMISIVE_AVG = 10;
		boolean alreadyFound;
		int walls,total,avg;
		walls = total = 0;
		//Finding for up exit:
		alreadyFound = false;
		str = new StringBuffer();
		str.append("Salida = -1,-1");
		for (int i = 0; i < plane[0].length&&!alreadyFound; i++) {
			if(!plane[0][i]){
				walls = total = 0;
				for(int h=0;(h<LOOKING_LIMIT)&&(h<plane.length);h++){
					for(int k=i;(k<(i+LOOKING_LIMIT))&&(k<plane[0].length);k++){
						total++;
						walls = (plane[h][k])?walls+1:walls;
					}
				}
				avg = (walls/total)*100;
				if(avg<=PERMISIVE_AVG){
					str = new StringBuffer();
					str.append("Salida = 0,"+i);
					alreadyFound = true;
				}
			}
		}
		if(!alreadyFound){
			for (int i = 0; i < plane.length&&!alreadyFound; i++) {
				if(!plane[i][plane.length-1]){
					walls = total = 0;
					for(int h=i;(h<(i+LOOKING_LIMIT))&&(h<plane.length);h++){
						for(int k=(plane[0].length-LOOKING_LIMIT);k<plane[0].length;k++){
							total++;
							walls = (plane[h][k])?walls+1:walls;
						}
					}
					avg = (walls/total)*100;
					if(avg<=PERMISIVE_AVG){
						str = new StringBuffer();
						str.append("Salida = "+i+","+(plane[0].length-1));
						alreadyFound = true;
					}
				}
			}
		}
		ans.addFirst(str);
		//Finding for up entry:
		alreadyFound = false;
		str = new StringBuffer();
		str.append("Entrada = -1,-1");
		for (int i = 0; i < plane[0].length&&!alreadyFound; i++) {
			if(!plane[plane.length-1][i]){
				walls = total = 0;
				for(int h=(plane.length-LOOKING_LIMIT);(h<plane.length);h++){
					for(int k=i;(k<(i+LOOKING_LIMIT))&&(k<plane[0].length);k++){
						total++;
						walls = (plane[h][k])?walls+1:walls;
					}
				}
				avg = (walls/total)*100;
				if(avg<=PERMISIVE_AVG){
					str = new StringBuffer();
					str.append("Entrada = "+(plane.length-1)+","+i);
					alreadyFound = true;
				}
			}
		}
		if(!alreadyFound){
			for (int i = 0; i < plane.length&&!alreadyFound; i++) {
				if(!plane[i][0]){
					walls = total = 0;
					for(int h=i;(h<(i+LOOKING_LIMIT))&&(h<plane.length);h++){
						for(int k=0;(k<LOOKING_LIMIT)&&(k<plane[0].length);k++){
							total++;
							walls = (plane[h][k])?walls+1:walls;
						}
					}
					avg = (walls/total)*100;
					if(avg<=PERMISIVE_AVG){
						str = new StringBuffer();
						str.append("Entrada = "+i+",0");
						alreadyFound = true;
					}
				}
			}
		}
		ans.addFirst(str);
		//Save matrix in a file:
		try {
			File fd = new File("matrix_out.txt");
			FileOutputStream output = new FileOutputStream(fd);
			for (Iterator iter = ans.iterator(); iter.hasNext();) {
				String element = iter.next().toString();
				output.write(element.getBytes());
				output.write('\n');
			}
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//**************************************************************************************
	//**************************************************************************************
	
	/**
	 * This method sends the actual color frame to the neural network, and returns the recognized color
	 * and recognition its recognition rate value.
	 * @param image matrix pattern.
	 * @param ans if answer from neuralnetwork is over the theeshold, then return the current "ans" array color. 
	 * @param theeshold is the limit to recognice the "ans" color.
	 * @return Color and Recognition rate in an Object[].
	 */
	private Object[] recognizeColor(BufferedImage image,Color[]ans,double theeshold){
		if(image!=null&&image.getWidth()>0&&image.getHeight()>0){
			Color c;
			double[]tmp=new double[(image.getWidth()*image.getHeight()*((NeuralNetwork.WITH_ILUMINATION_CHANNEL)?(NeuralNetwork.INPUT_CHANNELS+1):NeuralNetwork.INPUT_CHANNELS))+NeuralNetwork.NUMBER_OF_OUTPUTS];
			for (int x = 0,h = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					c = new Color(image.getRGB(x, y));
					//TODO: Here is possible to apply the f(x) function.
					long[]transformedColor = ColorFactory.transformeTo(c.getRed(),c.getGreen(),c.getBlue());
					for (int i = 0; i < transformedColor.length; i++,h++) {
						tmp[h] = transformedColor[i];
					}
					/*tmp[h] = transformedColor[0];
					tmp[h+1] = transformedColor[1];
					tmp[h+2] = transformedColor[2];
					h*=3;*/
				}
			}
			c = new Color(this.BACKGROUND);
			double[] response = this.neuralNetwork.stimulate(tmp);
			//Ensuring both arrays have the same length to do the match next.
			double max = 0;
			if(response!=null&&response.length<=ans.length){
				int index = -1;
				for (int i = 0; i < response.length; i++) {
					if(response[i]>max){
						index = i;
						max = response[i];
					}
				}
				//Determining the answer color.
				//System.out.println("Index = "+index+"; Response = "+response[index]+"; Thesshold = "+theeshold);
				if((index>=0)&&(response[index]>=theeshold)){
					c = ans[index];
					//System.out.println("OK");
				}else{
					c = null;
				}
			}
			Object[]tmpAns = {c,new Double(max)};
			return tmpAns;
		}else{
			return null;
		}
	}
	/*private Color recognizeColor(Color[][] rgb,Color[]ans,double theeshold){
		if(rgb!=null&&rgb.length>0&&rgb[0].length>0){
			Color c;
			//System.out.println("Size = "+((rgb.length*rgb[0].length*3)+1));
			double[]tmp=new double[(rgb.length*rgb[0].length*3)+1];
			for (int i = 0,h = 0; i < rgb.length; i++) {
				for (int j = 0; j < rgb[i].length; j++) {
					c = rgb[i][j];
					tmp[h] = c.getRed();
					tmp[h+1] = c.getGreen();
					tmp[h+2] = c.getBlue();
					h*=3;
				}
			}
			c = null;
			double[] response = this.neuralNetwork.stimulate(tmp);
			//Ensuring both arrays have the same length to do the match next.
			if(response.length==ans.length){
				double max = 0;
				int index = -1;
				for (int i = 0; i < response.length; i++) {
					if(response[i]>max){
						index = i;
						max = response[i];
					}
				}
				//Determining the answer color.
				//System.out.println("Index = "+index+"; Response = "+response[index]+"; Thesshold = "+theeshold);
				if((index>=0)&&(response[index]>=theeshold)){
					c = ans[index];
					//System.out.println("OK");
				}
			}
			return c;
		}else{
			return null;
		}
	}*/
	/*private Color recognizeColor(int rgb){
		Color c1 = new Color(rgb), c2;
		ColorRangeBean tmp;
		int error,red1,red2,green1,green2,blue1,blue2;
		
		for(Iterator i=this.colorList.iterator();i.hasNext();){
			tmp = (ColorRangeBean)i.next();
			c2 = tmp.getColor();
			error = tmp.getError();
			red1 = c1.getRed(); red2 = c2.getRed();
			green1 = c1.getGreen(); green2 = c2.getGreen();
			blue1 = c1.getBlue(); blue2 = c2.getBlue();
			if(
				(red1>=Math.round(red2-error)&&red1<Math.round(red2+error))&&
				(green1>=Math.round(green2-error)&&green1<Math.round(green2+error))&&
				(blue1>=Math.round(blue2-error)&&blue1<Math.round(blue2+error))
			){
				return tmp.getRepresentativeColor();
			}
		}
		return null;
	}*/
	/**
	 * This method allows to recognice a polygon of a common color area.
	 * @param image is the current imageto analyze. 
	 * @return List of ShapeBean.
	 */
	public LinkedList/*of Rectangle2D*/ geShape(BufferedImage image){
		int w = image.getWidth(),h=image.getHeight();
		LinkedList ans = new LinkedList(),bounds = new LinkedList();
		//++++
		ColorRangeBean colorRange;
		int c1;
		ShapeBean tmpShapeBean;
		Shape tmpShape;
		Point2D.Float tmpPoint;
		//++++
		for(Iterator i=this.colorList.iterator();i.hasNext();){
			colorRange = (ColorRangeBean)i.next();
			c1 = colorRange.getRepresentativeColor().getRGB();
			for(int y=0;y<h;y+=this.DETAIL){
				for(int x=0;x<w;x+=this.DETAIL){
					if(image.getRGB(x,y)==c1){
						tmpPoint = new Point2D.Float(x,y);
						tmpShapeBean = this.getContainerShapeOf(bounds,tmpPoint,c1);
						if(tmpShapeBean==null){
							tmpShape = explore(image,c1,tmpPoint);
							if(tmpShape!=null){
								bounds.add(new ShapeBean(tmpShape,new Color(c1)));
								tmpShape = getPolygon(image,c1,tmpShape.getBounds2D());
								if(tmpShape!=null){
									//ans.add(new ShapeBean(tmpShape,new Color(c1)));
									ans.add(new ShapeBean(tmpShape,colorRange.getColor()));
								}
							}
						}else{
							x+=tmpShapeBean.getShape().getBounds().getWidth();
						}
					}
				}
			}
		}
		return ans;
	}
	/**
	 * This method finds a shape in the "shapeList" that contains the "point" and
	 * is painted of "color".
	 * @param shapeList is the shape list.
	 * @param point is the reference point to find the shape.
	 * @param color is the color of the finding shape.
	 * @return the shape which contains the "point" and is painted of "color".
	 */
	private ShapeBean getContainerShapeOf(LinkedList shapeList,Point2D.Float point,int color){
		ShapeBean ans = null;
		Shape tmpShape; int tmpColor;
		for(Iterator i=shapeList.iterator();i.hasNext();){
			ans = (ShapeBean)i.next();
			tmpShape = ans.getShape();
			tmpColor = ans.getRepresentativeColor().getRGB();
			if(tmpColor==color && tmpShape.contains(point)){
				return ans;
			}
		}
		return null;
	}
	/**
	 * This method allows to enclose a shape in the "image", in a point "p",
	 * and with the "target" color.
	 * @param image is the current explored image.
	 * @param target is the color target to explore.
	 * @param p is the starting exploration point. 
	 * @return the enclosed shape.
	 */
	private Shape explore(BufferedImage image, int target, Point2D.Float p){
		Stack stack = new Stack();
		int w=image.getWidth(),h=image.getHeight(),x,y,z,
		
		h_y=0,//High y.
		h_x=0,//High x.
		l_y=h,//Low y.
		l_x=w;//Low x.
		
		boolean [][] flag = new boolean [h][w];
		boolean existABound = false;
		for(int i=0;i<flag.length;i++){
			for(int j=0;j<flag[i].length;j++){
				flag[i][j]=false;
			}
		}
		x = (int) p.getX();
		y = (int) p.getY();
		if((y>=0&&y<h) && (x>=0&&x<w) && !flag[y][x] && image.getRGB(x,y)==target){
			h_y=((int)p.getY());//High y.
			h_x=((int)p.getX());//High x.
			l_y=((int)p.getY());//Low y.
			l_x=((int)p.getX());//Low x.
			stack.push(p);
		}
		while(!stack.empty()){
			p = (Float)stack.pop();
			x = ((int)p.getX());
			y = ((int)p.getY());
			
			h_y=(y>h_y)?y:h_y;
			h_x=(x>h_x)?x:h_x;
			l_y=(y<l_y)?y:l_y;
			l_x=(x<l_x)?x:l_x;
			
			existABound = flag[y][x] = true;
			
			z = y-1;
			if((z>=0&&z<h) && !flag[z][x] && image.getRGB(x,z)==target){
				stack.push(new Point2D.Float(x,z));
			}
			
			z = x+1;
			if((z>=0&&z<w) &&!flag[y][z] && image.getRGB(z,y)==target){
				stack.push(new Point2D.Float(z,y));
			}
			
			z = y+1;
			if((z>=0&&z<h) &&!flag[z][x] && image.getRGB(x,z)==target){
				stack.push(new Point2D.Float(x,z));
			}
			
			z = x-1;
			if((z>=0&&z<w) &&!flag[y][z] && image.getRGB(z,y)==target){
				stack.push(new Point2D.Float(z,y));
			}
		}
		return (existABound)?new Rectangle2D.Float(l_x,l_y,Math.abs(h_x-l_x),Math.abs(h_y-l_y)):null;
	}
	/**
	 * This function allows to localice a polygon points.
	 * @param image is the current analized image.
	 * @param target is the color where we are going to extract the polygon.
	 * @param bound is the limit area to the finding.
	 * @return the polygon shape.
	 */
	public Shape getPolygon(BufferedImage image, int target,Rectangle2D bound){
		Polygon polygon1 = null,polygon2 = null;
		Area area = null;
		LinkedList points = null;
		Point2D[]tmpPoint;
		
		int initialX = (int) bound.getX(),xLimit, 
			initialY = (int) bound.getY(),yLimit,
			x1,y1,x2,y2;
		boolean secondPoint;
		xLimit = (int) bound.getMaxX();//(int) (initialX+bound.getWidth());
		yLimit = (int) bound.getMaxY();//(int) (initialY+bound.getHeight());
		
		/**System.out.println("init(x="+initialX+",y="+initialY+"); final(x="+xLimit+",y="+yLimit+")");/**/
		
		//Anрlisis horizontal:
		points = new LinkedList();
		for(int y=initialY;y<yLimit;y+=this.DETAIL){
			secondPoint = false;
			x1 = y1 = -1;
			x2 = y2 = -1;
			for(int x=initialX;x<xLimit;x++){
				if(!secondPoint){
					if(image.getRGB(x,y)==target){
						x1 = x;
						y1 = y;
						secondPoint = true;
					}
				}else{
					if(image.getRGB(x,y)==target){
						x2 = x;
						y2 = y;
					}
				}
			}
			if(secondPoint&&(x2>-1)&&(y2>-1)){
				/**System.out.print("\n\t<adding1> ("+x1+","+y1+"); ("+x2+","+y2+")");/**/
				tmpPoint=new Point2D[2];
				tmpPoint[0] = new Point2D.Float(x1,y1);
				tmpPoint[1] = new Point2D.Float(x2,y2);
				points.add(tmpPoint);
			}
		}
		//Building polygon:
		polygon1 = buildPolygon(points);
		/**System.out.println();/**/
		if(polygon1!=null){
			//Anрlisis vertical:
			points = new LinkedList();
			for(int x=initialX;x<xLimit;x+=this.DETAIL){
				secondPoint = false;
				x1 = y1 = -1;
				x2 = y2 = -1;
				for(int y=initialY;y<yLimit;y++){
					if(!secondPoint){
						if(image.getRGB(x,y)==target){
							x1 = x;
							y1 = y;
							secondPoint = true;
						}
					}else{
						if(image.getRGB(x,y)==target){
							x2 = x;
							y2 = y;
						}
					}
				}
				if(secondPoint&&(x2>-1)&&(y2>-1)){
					tmpPoint=new Point2D[2];
					tmpPoint[0] = new Point2D.Float(x1,y1);
					tmpPoint[1] = new Point2D.Float(x2,y2);
					points.add(tmpPoint);
				}
			}
			polygon2 = buildPolygon(points);
			if(polygon2!=null){
				area = new Area(polygon1);
				Area tmpArea = new Area(polygon2);
				area.intersect(tmpArea);
			}
		}
		//return polygon;
		return area;
	}
	/**
	 * This methos allows to build a polygon by using the points list. 
	 * @param points is the list of points.
	 * @return a polygon.
	 */
	private Polygon buildPolygon(LinkedList points){
		Polygon polygon = null; 
		if(!points.isEmpty()){
			polygon = new Polygon();
			for(int i=0;i<points.size();i++){
				Point2D p = ((Point2D[])points.get(i))[0];
				polygon.addPoint((int)p.getX(),(int)p.getY());
			}
			for(int i=(points.size()-1);i>=0;i--){
				Point2D p = ((Point2D[])points.get(i))[1];
				polygon.addPoint((int)p.getX(),(int)p.getY());
			}
		}
		return polygon;
	}
	/**
	 * This method transforms a RGB color into a HSI one.
	 * @param r is the red channel.
	 * @param g is the green channel.
	 * @param b is the blue channel.
	 * @return HSI color.
	 */
	public int[] RGB_to_HSI(double r,double g,double b){
		double n_r,n_g,n_b,r_g,r_b,d;//Normalized r,g,b.
		double h,s,i;//Normalized r,g,b.
		d = (r+g+b);
		//System.out.println("\td: "+d);
		n_r = r/d;	n_g = g/d;	n_b = b/d;
		r_g = (n_r-n_g);
		r_b = (n_r-n_b);
		h = (int) Math.acos((0.5*(r_g+r_b))/Math.sqrt(Math.pow(r_g,2)+(r_b*(n_g-n_b))));
		if(n_b>n_g){
			h = (int) ((2*Math.PI)-h);
		}
		s = 1-(3*Math.min((int)n_r,Math.min((int)n_g,(int)n_b)));
		i = d/765;//765 = 3*255.
		//Conversion:
		h = (int) ((h*180)/Math.PI);
		h = h*255/360;
		s = s*100;
		s = s*255/100;
		i = i*255;
		int[]tmp = {(int)h,(int)s,(int)i};
		//System.out.println("> H: "+tmp[0]+"<"+h+">"+"; S: "+tmp[1]+"<"+s+">"+"; I: "+tmp[2]+"<"+i+">");
		return tmp;
	}
	/**
	 * This method transforms a HSI color into a RGB one.
	 * @param h is the hue channel.
	 * @param s is the saturation channel.
	 * @param i is the ilumination channel.
	 * @return RGB color.
	 */
	byte[] HSI_to_RGB (double h,double s,double i){
		double n_h,n_s,n_i,x,y,z,a1,a2;//n_h,n_s,n_i son valores normalizados de HSI.
		double r = 0,g = 0,b = 0;
		n_h = (h*Math.PI)/180;
		n_s = s/100;
		n_i = i/255;
		x = n_i*(1-n_s);
		a1 = ((2*Math.PI)/3);
		a2 = ((4*Math.PI)/3);
		if(n_h<a1){
			y = n_i*(1+((n_s*Math.cos(n_h))/Math.cos(Math.PI/(3-n_h))));
			z = (3*n_i)-(x+y);
			r = y;	g = z;	b = x;
		}else if((a1<=n_h) && (n_h<a2)){
			h = h - a1;
			y = n_i*(1+((n_s*Math.cos(n_h))/Math.cos(Math.PI/(3-n_h))));
			z = (3*n_i)-(x+y);
			r = x;	g = y;	b = z;
		}else if((a2<=n_h) && (n_h<(2*Math.PI))){
			h = h-a2;
			y = n_i*(1+((n_s*Math.cos(n_h))/Math.cos(Math.PI/(3-n_h))));
			z = (3*n_i)-(x+y);
			r = z;	g = x;	b = y;
		}
		r = r*255;	g = g*255;	b = b*255;
		byte[]tmp = {(byte) r,(byte) g,(byte) b};
		return tmp;
	}

	/**
	 * This method returns a HSI image.
	 * @param image is the image to transform.
	 * @param w is the width of the image.
	 * @param h is the heigt of the image.
	 * @return HSI transformed image.
	 */
	public Image getHSIImage(Image image, int w, int h) {
		BufferedImage tmp = new BufferedImage(w,h, BufferedImage.TYPE_INT_RGB);
		tmp.getGraphics().drawImage(image,0,0,null);
		int width = tmp.getWidth(), height = tmp.getHeight();
		Color c;
		int[]hsi;
		//byte[]rgb;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				c = new Color(tmp.getRGB(i, j));
				hsi = this.RGB_to_HSI(c.getRed(), c.getGreen(), c.getBlue());
				tmp.setRGB(i,j,new Color(hsi[0], hsi[1], hsi[2]).getRGB());
				//rgb = this.HSI_to_RGB(hsi[0], hsi[1], hsi[2]);
				//tmp.setRGB(i,j,new Color(rgb[0], rgb[1], rgb[2]).getRGB());
			}
		}
		return tmp;
	}
	//Main test:
	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[]args) throws IOException, ClassNotFoundException{
		/*ImageFilter filter = new ImageFilter(null,0);
		int[]tmp = filter.RGB_to_HSI(0, 100, 0);
		System.out.println("H: "+tmp[0]+"; S: "+tmp[1]+"; I: "+tmp[2]);*/
	}
}

