/*
 * WPS_Setup.java
 *
 * Created on 2006/12/13, 20:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package DrawFormat;

import Common.MyProperties;
import MyClass.Segment;
import dimension.PointD;
import java.text.DecimalFormat;

/**
 *
 * @author T2
 */
public class WPS_Setup {

	String type = "";
	boolean setLim = true;
	boolean setLimX = false, setLimY = false;//setLim=zennbu wo awaseru
	boolean useMaxX = false, useMaxY = false;//useMax=zennbu wo max ni awaseru
	boolean single = false;
	boolean direct = false;
	boolean adjust = true;
	public PointD max = new PointD(0, 0);
	public PointD min = new PointD(0, 0);
	public PointD small = new PointD(0, 0);	///'small' for LogScale

	public PointD delMain = new PointD(0, 0);
	public PointD delSub = new PointD(0, 0);
	int nX = 3;
	int nY = 5;
	String scaleType = "LogLog";
	String xLabel = "";
	String yLabel = "";
	int qq = 1;  //  QualityQuantity dot sepalate qq

	boolean writeOBS = true;

	/** Creates a new instance of WPS_Setup */
	public WPS_Setup() {
		String[] properties = new String[20];
		properties[1] = (String) MyProperties.prop.get("WAVELET_LEVEL");
		properties[2] = (String) MyProperties.prop.get("X_MAX");
		properties[3] = (String) MyProperties.prop.get("X_MIN");
		properties[4] = (String) MyProperties.prop.get("Y_MAX");
		properties[5] = (String) MyProperties.prop.get("Y_MIN");
		properties[6] = (String) MyProperties.prop.get("X_LABEL");
		properties[7] = (String) MyProperties.prop.get("Y_LABEL");
		properties[8] = (String) MyProperties.prop.get("X_MAIN_TIP");
		properties[9] = (String) MyProperties.prop.get("X_SUB_TIP");
		properties[10] = (String) MyProperties.prop.get("Y_MAIN_TIP");
		properties[11] = (String) MyProperties.prop.get("Y_SUB_TIP");
		properties[12] = (String) MyProperties.prop.get("NX");
		properties[13] = (String) MyProperties.prop.get("NY");
		properties[14] = (String) MyProperties.prop.get("SINGLE");
		properties[15] = (String) MyProperties.prop.get("DIRECT");
		properties[16] = (String) MyProperties.prop.get("POINT_TYPE");
		properties[17] = (String) MyProperties.prop.get("WRITE_OBS");
		properties[18] = (String) MyProperties.prop.get("SCALE_TYPE");
		properties[19] = (String) MyProperties.prop.get("TYPE_NAME");
		if (properties[2].equals("") || properties[3].equals("")) {
			setLimX = true;
			useMaxX = true;
		} else {
			min.p[PointD.X] = Double.parseDouble(properties[3]);//

			max.p[PointD.X] = Double.parseDouble(properties[2]);//

		}
		if (properties[4].equals("") || properties[5].equals("")) {
			setLimY = true;
			useMaxY = true;
		} else {
			min.p[PointD.Y] = Double.parseDouble(properties[5]);//

			max.p[PointD.Y] = Double.parseDouble(properties[4]);//

		}
		xLabel = (String) properties[6];
		yLabel = (String) properties[7];
		if (!properties[8].equals("")) {
			delMain.p[PointD.X] = Double.parseDouble(properties[8]);
		}
		if (!properties[9].equals("")) {
			delSub.p[PointD.X] = Double.parseDouble(properties[9]);
		}
		if (!properties[10].equals("")) {
			delMain.p[PointD.Y] = Double.parseDouble(properties[10]);
		}
		if (!properties[11].equals("")) {
			delSub.p[PointD.Y] = Double.parseDouble(properties[11]);
		}
		if (!properties[12].equals("")) {
			nX = Integer.parseInt(properties[12]);
		}
		if (!properties[13].equals("")) {
			nY = Integer.parseInt(properties[13]);
		}
		if (!properties[14].equals("")) {
			single = Boolean.parseBoolean(properties[14]);
		}
		if (!properties[15].equals("")) {
			direct = Boolean.parseBoolean(properties[15]);
		}
		if (!properties[16].equals("")) {
			WritePostScript.pointType = Integer.parseInt(properties[16]);
		}
		if (!properties[17].equals("")) {
			writeOBS = Boolean.parseBoolean(properties[17]);
		}
		if (!properties[18].equals("")) {
			scaleType = (String) properties[18].trim();
		}
		type = properties[19];
	}

	public WPS_Setup(String type) {
		if (type.contains("TimeHistory") || type.contains("_rev")) {
			single = true;
			direct = true;
			WritePostScript.pointType = 0;
			writeOBS = true;
			nX = 1;
			nY = 6;
			scaleType = "LinerLiner";
			xLabel = "[s]";
			yLabel = "[gal]";
			setLimX = true;
			setLimY = true;
			useMaxX = true;
			useMaxY = true;

			min.p[PointD.Y] = -0.25;//

			max.p[PointD.Y] = 0.25;//

			useMaxY = false;
			delMain.p[PointD.Y] = 0.10;
			delSub.p[PointD.Y] = 0.05;
		} else if (type.contains("Orbit")) {
			adjust = false;
			Segment.setORT(1);
			WritePostScript.pointType = 0;
			nX = 2;
			nY = 2;
			scaleType = "LinerLiner";
			xLabel = "[gal]";
			yLabel = "[gal]";
			setLimX = false;
			setLimY = false;
			useMaxX = false;
			useMaxY = false;
		} else if (type.contains("Spectrum") || type.contains("_pow") || type.contains("PowSpec")) {
			single = true;
			direct = true;
			Segment.setORT(2);
			WritePostScript.pointType = 0;
			nX = 3;
			nY = 5;
			scaleType = "LogLog";
			xLabel = "[Hz]";
			yLabel = "[(gal*s)^2]";
			setLimX = true;
			setLimY = true;
			useMaxX = false;
			useMaxY = false;
			small = new PointD(0.1, 10e-9);
			max = new PointD(10, 10e-4);
//				small.p[PointD.Y]=10e-5;//jisinn you
//				max.p[PointD.Y]=10e2;//jisinn you
			min.p[PointD.X] = 0;
			small.p[PointD.X] = 1;
			max.p[PointD.X] = 15;

			delMain.p[PointD.X] = 1;
			delSub.p[PointD.X] = 1;
			delMain.p[PointD.Y] = 1;
			delSub.p[PointD.Y] = 1;
			if (type.contains("_rc")) {
				small.p[PointD.Y] = 10e-17;//

				max.p[PointD.Y] = 10e-12;//

			} else if (type.contains("_sr")) {
				small.p[PointD.Y] = 10e-17;//

				max.p[PointD.Y] = 10e-12;//

			}
			if (type.contains("_rc")) {
				yLabel = "[(rad*s)^2]";
			}
		} else if (type.contains("TF") || type.contains("_TransFanc")) {
//	    adjust=false;
			single = true;
			direct = true;
			Segment.setORT(2);
			WritePostScript.pointType = 0;
			writeOBS = true;
			nX = 3;
			nY = 5;
			scaleType = "LogLog";
			xLabel = "[Hz]";
			yLabel = "[rate]";
			setLimX = true;
			setLimY = true;
			useMaxX = false;
			useMaxY = false;
			min.p[PointD.X] = 0;
			small.p[PointD.X] = 1;
			max.p[PointD.X] = 15;
			small.p[PointD.Y] = 10e-2;//

			max.p[PointD.Y] = 10e1;//

			delMain.p[PointD.X] = 1;
			delSub.p[PointD.X] = 1;
			delMain.p[PointD.Y] = 1;
			delSub.p[PointD.Y] = 1;
		} else if (type.contains("Coh2") || type.contains("_coh")) {
			single = true;
			direct = true;
			Segment.setORT(2);
			WritePostScript.pointType = 0;
			nX = 3;
			nY = 5;
			scaleType = "LogLiner";
			xLabel = "[Hz]";
			yLabel = "[coh^2]";
			setLimX = true;
			setLimY = true;
			useMaxX = false;
			useMaxY = false;
			min.p[PointD.X] = 0;
			small.p[PointD.X] = 1;
			max.p[PointD.X] = 15;
			min.p[PointD.Y] = 0d;//

			max.p[PointD.Y] = 1.05d;//

			delMain.p[PointD.X] = 1;
			delSub.p[PointD.X] = 1;
			delMain.p[PointD.Y] = 0.2;
			delSub.p[PointD.Y] = 0.1;
		} else if (type.contains("phase") || type.contains("_ph") || type.contains("Phase")) {
			adjust = false;
			single = true;
			direct = true;
			Segment.setORT(2);
			WritePostScript.pointType = 1;
			writeOBS = true;
			nX = 3;
			nY = 5;
			scaleType = "LogLiner";
			xLabel = "[Hz]";
			yLabel = "[rad]";
			setLimX = true;
			setLimY = true;
			useMaxX = false;
			useMaxY = false;
			min.p[PointD.X] = 0;
			small.p[PointD.X] = 1;
			max.p[PointD.X] = 15;
			min.p[PointD.Y] = -3.3;//

			small.p[PointD.Y] = 0.1;
			max.p[PointD.Y] = 3.3;//

			delMain.p[PointD.X] = 1;
			delSub.p[PointD.X] = 1;
			delMain.p[PointD.Y] = 1;
			delSub.p[PointD.Y] = 0.5;
		} else if (type.contains("RPV") || type.contains("_rpv")) {
			adjust = false;
			single = false;
			direct = false;
			WritePostScript.pointType = 0;
			WritePostScript.dform = new DecimalFormat("#0.0");
			WritePostScript.VX = -20;
			writeOBS = true;
			nX = 2;
			nY = 3;
			scaleType = "LinerLiner";
			//scaleType="LogLiner";
			xLabel = "Frequency[Hz]";
			yLabel = "PhaseVelocity[km/s]";
			small.p[PointD.X] = 0.1;
			min.p[PointD.X] = 0;
			max.p[PointD.X] = 20;
			small.p[PointD.Y] = 0.1;
			min.p[PointD.Y] = 0;//

			max.p[PointD.Y] = 0.8;//

			delMain.p[PointD.X] = 5;
			delSub.p[PointD.X] = 1;
			delMain.p[PointD.Y] = 0.2;
			delSub.p[PointD.Y] = 0.1;
			setLimX = true;
			setLimY = true;
			useMaxX = false;
			useMaxY = false;
		}
		if (type.contains("_rc") || type.contains("_sr")) {
			nX = 2;
			nY = 4;
		}
		this.type = type;
	}

	public void setDPF(DrawPageFormat dpf) {
		DrawPageFormat.nX = nX;
		DrawPageFormat.nY = nY;
	}

	public void setDGF(DrawGraphFormat dgf) {
		if (useMaxX) {
			/*			double max = Math.max(Math.abs(dgf.max.p[PointD.X]), Math.abs(dgf.min.p[PointD.X]));
			max = Math.pow(10, (int) Math.log10(max));
			dgf.max.p[PointD.X] = max;
			dgf.min.p[PointD.X] = -max;
			dgf.delMain.p[PointD.X] = max.p[PointD.X];
			 */
			dgf.delSub.p[PointD.X] = dgf.delMain.p[PointD.X] / 5d;
			
		} else {
			if (setLimX) {
				dgf.max.p[PointD.X] = max.p[PointD.X];
				dgf.min.p[PointD.X] = min.p[PointD.X];
				dgf.small.p[PointD.X] = small.p[PointD.X];
				dgf.delMain.p[PointD.X] = delMain.p[PointD.X];
				dgf.delSub.p[PointD.X] = delSub.p[PointD.X];
			}
		}
		if (useMaxY) {
			double max = 1;
			if (scaleType.equals("LinerLog") || scaleType.equals("LogLog")) {
				max = Math.max(Math.abs(dgf.max.p[PointD.Y]), Math.abs(dgf.min.p[PointD.Y]));
				max = Math.pow(10, (int) Math.log10(max));
			} else if (scaleType.equals("LinerLiner") || scaleType.equals("LogLiner")) {
				double maxV = Math.max(Math.abs(dgf.max.p[PointD.Y]), Math.abs(dgf.min.p[PointD.Y]));
				max = Math.pow(10, (int) Math.log10(maxV) );
				if (max / maxV > 2) {
					max = max / 2;
				}
			}
			dgf.max.p[PointD.Y] = max;
			dgf.min.p[PointD.Y] = -max;
			dgf.delMain.p[PointD.Y] = max;
			dgf.delSub.p[PointD.Y] = dgf.max.p[PointD.Y] / 5d;
		} else {
			if (setLimY) {
				dgf.max.p[PointD.Y] = max.p[PointD.Y];
				dgf.min.p[PointD.Y] = min.p[PointD.Y];
				dgf.small.p[PointD.Y] = small.p[PointD.Y];
				dgf.delMain.p[PointD.Y] = delMain.p[PointD.Y];
				dgf.delSub.p[PointD.Y] = delSub.p[PointD.Y];
			}
		}
		if (setLim) {
			if (type.contains("Orbit")) {
				double max = Math.max(Math.max(dgf.max.p[PointD.X], dgf.max.p[PointD.Y]), Math.max(-dgf.min.p[PointD.X], -dgf.min.p[PointD.Y]));
				dgf.max.p[PointD.X] = max;
				dgf.min.p[PointD.X] = -dgf.max.p[PointD.X];
				dgf.max.p[PointD.Y] = dgf.max.p[PointD.X] * DrawPageFormat.mY / DrawPageFormat.mX;
				dgf.min.p[PointD.Y] = dgf.min.p[PointD.X] * DrawPageFormat.mY / DrawPageFormat.mX;

//				dgf.delMain.p[PointD.X]=dgf.max.p[PointD.X]/2;
//				dgf.delMain.p[PointD.Y]=dgf.delMain.p[PointD.X];

				dgf.delSub.p[PointD.X] = dgf.delMain.p[PointD.X] / 5;
				dgf.delSub.p[PointD.Y] = dgf.delSub.p[PointD.X];
			}
		}
		dgf.scaleType = scaleType;
		dgf.xLabel = xLabel;
		dgf.yLabel = yLabel;
	}

	public void setMaxX(double maxX) {
		max.p[PointD.X] = maxX;
	}

	public void setMinX(double minX) {
		min.p[PointD.X] = minX;
	}

	public void setSmallX(double smallX) {
		small.p[PointD.X] = smallX;
	}

	public void setDelMainX(double delMainX) {
		delMain.p[PointD.X] = delMainX;
	}

	public void setDelSubX(double delSubX) {
		delSub.p[PointD.X] = delSubX;
	}

	public void setSetLimX(boolean setLimX) {
		this.setLimX = setLimX;
	}

	public void setUseMaxX(boolean useMaxX) {
		this.useMaxX = useMaxX;
	}

	public void setScaleType(String scaleType) {
		this.scaleType = scaleType;
	}

	public void setLabelX(String xLabel) {
		this.xLabel = xLabel;
	}

	public void setNX(int nX) {
		this.nX = nX;
	}

	public void setMaxY(double maxY) {
		max.p[PointD.Y] = maxY;
	}

	public void setMinY(double minY) {
		min.p[PointD.Y] = minY;
	}

	public void setSmallY(double smallY) {
		small.p[PointD.Y] = smallY;
	}

	public void setDelMainY(double delMainY) {
		delMain.p[PointD.Y] = delMainY;
	}

	public void setDelSubY(double delSubY) {
		delSub.p[PointD.Y] = delSubY;
	}

	public void setSetLimY(boolean setLimY) {
		this.setLimY = setLimY;
	}

	public void setUseMaxY(boolean useMaxY) {
		this.useMaxY = useMaxY;
	}

	public void setLabelY(String xLabel) {
		this.xLabel = xLabel;
	}

	public void setNY(int nY) {
		this.nY = nY;
	}

	public boolean getSingle() {
		return single;
	}

	public boolean getDirect() {
		return direct;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}

	public void setDirect(boolean direct) {
		this.direct = direct;
	}
}

