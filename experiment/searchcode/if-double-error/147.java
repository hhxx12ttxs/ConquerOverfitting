package eu.gloria.website.liferay.portlets.experiment.services;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.gloria.gs.services.teleoperation.ccd.client.CCDTeleoperationException_Exception;
import eu.gloria.gs.services.teleoperation.ccd.handlers.CCDTeleoperation;
import eu.gloria.gs.services.teleoperation.rts.TeleoperationManager;
import eu.gloria.gs.services.teleoperation.scam.client.SCamTeleoperationException_Exception;
import eu.gloria.gs.services.teleoperation.scam.handlers.SCamTeleoperation;

public class WebcamServices {

	private static Log _log = LogFactory.getLog(WebcamServices.class);
	private static ResourceBundle rb =  ResourceBundle.getBundle("content.webcamera.Language");
	
	protected CCDTeleoperation ccd;
	protected SCamTeleoperation scam;

	private DecimalFormat doubleFormat = new DecimalFormat();
	static Object sync = new Object();
	
	private Integer timeSec = 10;
	
	public WebcamServices() 
	throws Exception {
		try {
//			ccd = TeleoperationManager.getReference().getCCDTeleoperation();
//			scam = TeleoperationManager.getReference().getSCamTeleoperation();
			
			doubleFormat.setMaximumFractionDigits(4);
			doubleFormat.setMinimumFractionDigits(0);
			doubleFormat.setMaximumIntegerDigits(4);
			doubleFormat.setMinimumIntegerDigits(1);	
			doubleFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
			
			this.getService();
			
		} catch (Exception e) {
			_log.error(e.getMessage());
			throw new Exception();
		}
	};
	
    protected void getService()
    throws Exception {
    	synchronized(sync)
		{
    		if (ccd == null) {
	    		try {
	    			ccd = TeleoperationManager.getReference().getCCDTeleoperation();
	    		} catch (Exception e) {
	    			_log.error(e.getMessage());
	    			throw new Exception();
	    		}
    		}
    		
    		if (scam == null) {
	    		try {
	    			scam = TeleoperationManager.getReference().getSCamTeleoperation();
	    		} catch (Exception e) {
	    			_log.error(e.getMessage());
	    			throw new Exception();
	    		}
    		}
		}
    }
	
	/*
	 *  GET and SET OPERATIONS
	 */
    public void setParams(final PortletPreferences prefs,
    				final String brightness, 
    				final String gain, 
    				final String exposure, 
    				final String contrast) 
	throws Exception {
    	ExecutorService exec = Executors.newFixedThreadPool(1);
    	exec.execute(new Runnable() {
    		public void run() {
    			String prefsTelescope 	= (String) prefs.getValue("telescope", rb.getString("label-none"));
            	String prefsCcd 		= (String) prefs.getValue("ccd", rb.getString("label-none"));
            	String prefsSurv 		= (String) prefs.getValue("surv", rb.getString("label-none"));
    			
            	String prefsBrightness 	= (String) prefs.getValue("brightness_value", "error");
        		String prefsGain 		= (String) prefs.getValue("gain_value", "error");       				
        		String prefsExposure 	= (String) prefs.getValue("expT_value", "error");
        		String prefsContrast 	= (String) prefs.getValue("contrast_value", "error");	

        		String ThreadBrightness = brightness;
        		String ThreadGain 		= gain;
        		String ThreadExposure	= exposure;
        		String ThreadContrast	= contrast;
        		
        		/* CHECK LIMITS */
        		try {
    				if (Double.parseDouble(brightness) < 0 )	{ ThreadBrightness="0";}
    				if (Double.parseDouble(brightness) > 63 ) 	{ ThreadBrightness="63";}
    			} catch (Exception e) {
    				ThreadBrightness = "error";
    				_log.error(e.getMessage());
    			}
        		
    			try {
    				if (Double.parseDouble(gain) < 260 ) 	{ ThreadGain="260";}
    				if (Double.parseDouble(gain) > 1023 ) 	{ ThreadGain="1023";}
    			} catch (Exception e) {
    				ThreadGain = "error";
    				_log.error(e.getMessage());
    			}
    			
    			try {	
    				if (Double.parseDouble(exposure) < 0.0001 ) { ThreadExposure="0.0001";}
    				if (Double.parseDouble(exposure) > 3600 ) 	{ ThreadExposure="3600";}
    			} catch (Exception e) {
    				ThreadExposure = "error";
    				_log.error(e.getMessage());
    			}
    			
    			try {
    				if (Double.parseDouble(contrast) < 0 ) 		{ ThreadContrast="0";}
    				if (Double.parseDouble(contrast) > 100 ) 	{ ThreadContrast="100";}
    			} catch (Exception e) {
    				ThreadContrast = "error";
    				_log.error(e.getMessage());
    			}
    			
    			/* SETS */
    			if (!prefsTelescope.equalsIgnoreCase(rb.getString("label-none"))) {
    				if (!prefsCcd.equalsIgnoreCase(rb.getString("label-none"))) {
    					if(!prefsBrightness.equalsIgnoreCase(ThreadBrightness) && 
    					    !ThreadBrightness.equalsIgnoreCase("error")) {
    						try {
    							setCCDBrightness(prefsTelescope, prefsCcd, ThreadBrightness);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    					if(!prefsGain.equalsIgnoreCase(ThreadGain) && 
        				    !ThreadGain.equalsIgnoreCase("error")) {
    						try {
    							setCCDGain(prefsTelescope, prefsCcd, ThreadGain);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    					if(!prefsExposure.equalsIgnoreCase(ThreadExposure) && 
        				    !ThreadExposure.equalsIgnoreCase("error")) {
    						try {
    							setCCDExposureTime(prefsTelescope, prefsCcd, ThreadExposure);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}

    					if(!prefsContrast.equalsIgnoreCase(ThreadContrast) && 
        				   !ThreadContrast.equalsIgnoreCase("error")) {
    						try {
    							setCCDContrast(prefsTelescope, prefsCcd, ThreadContrast);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    				} // end-if-camera
    				
    				if (!prefsSurv.equalsIgnoreCase(rb.getString("label-none"))) {
    					if(!prefsBrightness.equalsIgnoreCase(ThreadBrightness) && 
        					!ThreadBrightness.equalsIgnoreCase("error")) {
    						try {
    							setSCamBrightness(prefsTelescope, prefsSurv, ThreadBrightness);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    					if(!prefsGain.equalsIgnoreCase(ThreadGain) && 
        					!ThreadGain.equalsIgnoreCase("error")) {
    						try {
    							setSCamGain(prefsTelescope, prefsSurv, ThreadGain);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    					if(!prefsExposure.equalsIgnoreCase(ThreadExposure) && 
    						!ThreadExposure.equalsIgnoreCase("error")) {
    						try {
    							setSCamExposureTime(prefsTelescope, prefsExposure, ThreadExposure);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    					if(!prefsContrast.equalsIgnoreCase(ThreadContrast) && 
        					!ThreadContrast.equalsIgnoreCase("error")) {
    						try {
    							setSCamContrast(prefsTelescope, prefsSurv, ThreadContrast);
    						} catch (Exception e) {
    							_log.error(e.getMessage());
    						}
    					}
    				} // end-if-camera
    			} // end-if-telescope
    			
    			try {
					prefs.setValue("brightness_value", ThreadBrightness);
					prefs.setValue("gain_value", ThreadGain);
					prefs.setValue("expT_value", ThreadExposure);
					prefs.setValue("contrast_value", ThreadContrast);
					prefs.store();
				} catch (ReadOnlyException e) {
					_log.error("Preferences: ReadOnlyException");
				} catch (ValidatorException e) {
					_log.error("Preferences: ValidatorException");
				} catch (IOException e) {
					_log.error("Preferences: IOException");
				}	
    			
    		} // end-run
    	}); // end-exec
	 
		exec.shutdown();
		try {
			boolean b = exec.awaitTermination(timeSec, TimeUnit.SECONDS);
			if (!b) {
				_log.error("Timer has expired");
    			exec.shutdown();
    			throw new Exception();
    		}
		} catch (InterruptedException e) {
			_log.info(e.getMessage());
			throw new Exception();
		}
    }
    
    public void getParams(final PortletPreferences prefs) 
	throws Exception {
    	ExecutorService exec = Executors.newFixedThreadPool(1);
    	exec.execute(new Runnable() {
    		String prefsTelescope 	= (String) prefs.getValue("telescope", rb.getString("label-none"));
        	String prefsCcd 		= (String) prefs.getValue("ccd", rb.getString("label-none"));
        	String prefsSurv 		= (String) prefs.getValue("surv", rb.getString("label-none"));
    		 
        	String prefsBrightness 	= (String) prefs.getValue("showBrightness", "0");
        	String prefsGain 		= (String) prefs.getValue("showGain","0");
        	String prefsExposure 	= (String) prefs.getValue("showExposure","0");
        	String prefsContrast 	= (String) prefs.getValue("showContrast","0");
//        	String prefsContinous = (String) prefs.getValue("showContinousMode","0");
        	
        	String brightness = null, gain = null, exposure = null, contrast = null, url = null;	
    		
    		public void run() {
    			if (!prefsTelescope.equalsIgnoreCase(rb.getString("label-none"))) {
    	    		if (!prefsCcd.equalsIgnoreCase(rb.getString("label-none"))) {
    	    			_log.info("GET CCD PARAMETERS telescope= "+prefsTelescope+"  camera= "+ prefsCcd);
    	    			
    	    			/* BRIGHTNESS */
    	    			try {
    						if (prefsBrightness.equalsIgnoreCase("1"))
    							brightness = getCCDBrightness(prefsTelescope, prefsCcd);
    						else
    							brightness = "error";
    					} catch (Exception e) {
    						brightness = "error";
    					} 
    					/* GAIN */
    					try {
    						if (prefsGain.equalsIgnoreCase("1"))
    							gain = getCCDGain(prefsTelescope, prefsCcd);
    						else
    							gain = "error";	
    					} catch (Exception e) {
    						gain = "error";
    					}
    					/* EXPOSURE */
    					try {
    						if (prefsExposure.equalsIgnoreCase("1"))
    							exposure = getCCDExposureTime(prefsTelescope, prefsCcd);
    						else
    							exposure = "error";	
    					} catch (Exception e) {
    						exposure = "error";
    					}
    					/* CONTRAST */
    					try {
    						if (prefsContrast.equalsIgnoreCase("1"))
    							contrast = getCCDExposureTime(prefsTelescope, prefsCcd);
    						else
    							contrast = "error";	
    					} catch (Exception e) {
    						contrast = "error";
    					}
    					/* URL */
    					try {
    						url = getCCDUrlImage(prefsTelescope, prefsCcd);
    					} catch (Exception e) {
    						url = "None";
    					}
    					
    	    		} // end-if
    	    		
    	    		if (!prefsSurv.equalsIgnoreCase(rb.getString("label-none"))) {
    	    			_log.info("GET SURVILLANCE PARAMETERS telescope= "+prefsTelescope+"  camera= "+ prefsSurv);

    	    			/* BRIGHTNESS */
    	    			try {
    						if (prefsBrightness.equalsIgnoreCase("1"))
    							brightness = getSCamBrightness(prefsTelescope, prefsSurv);
    						else
    							brightness = "error";	
    					} catch (Exception e) {
    						brightness = "error";
    					} 
    					/* GAIN */
    					try {
    						if (prefsGain.equalsIgnoreCase("1"))
    							gain = getSCamGain(prefsTelescope, prefsSurv);
    						else
    							gain = "error";
    					} catch (Exception e) {
    						gain = "error";
    					}
    					/* EXPOSURE */
    					try {
    						if (prefsExposure.equalsIgnoreCase("1"))
    							exposure = getSCamExposureTime(prefsTelescope, prefsSurv);
    						else
    							exposure = "error";
    					} catch (Exception e) {
    						exposure = "error";
    					}
    					/* CONTRAST */
    					try {
    						if (prefsContrast.equalsIgnoreCase("1"))
    							contrast = getSCamContrast(prefsTelescope, prefsSurv);
    						else
    							contrast = "error";
    					} catch (Exception e) {
    						contrast = "error";
    					}
    					/* URL */
    					try {
    						url = getSCamUrlImage(prefsTelescope, prefsSurv);
    					} catch (Exception e) {
    						url = "None";
    					}
    	    		} // end-if
    	    		
    	    		try {
    					prefs.setValue("brightness_value", brightness);
    					prefs.setValue("gain_value", gain);
    					prefs.setValue("expT_value", exposure);
    					prefs.setValue("contrast_value", contrast);
    					prefs.setValue("url", url);
    					prefs.store();
    				} catch (ReadOnlyException e) {
    					_log.error("Preferences: ReadOnlyException");
    				} catch (ValidatorException e) {
    					_log.error("Preferences: ValidatorException");
    				} catch (IOException e) {
    					_log.error("Preferences: IOException");
    				}	
    	    		
    	    	} // end-if
	            	
    		} // end-run
    	}); // end-exec
		 
    	exec.shutdown();
    	try {
    		boolean b = exec.awaitTermination(timeSec, TimeUnit.SECONDS);
    		_log.info("All done: " + b);
    		if (!b) {
    			_log.error("Timer has expired");
    			exec.shutdown();
    			throw new Exception();
    		}
    	} catch (InterruptedException e) {
    		_log.info(e.getMessage());
    		throw new Exception();
    	}
	 }
    
	
	/*
	 *  CCD OPERATIONS
	 */
	public void setCCDBrightness (String telescope, String ccdCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\" \""+value+"\"  setCCDBrightness");
		
		try {
			ccd.setBrightness(telescope,ccdCamera, Long.parseLong(value));
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getCCDBrightness (String telescope, String ccdCamera)
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\"  getCCDBrightness");
		
		try {
			return doubleFormat.format(ccd.getBrightness(telescope, ccdCamera)).replaceAll(",", "");
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public void setCCDGain (String telescope, String ccdCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\" \""+value+"\"  setCCDGain");
		
		try {
			ccd.setGain(telescope,ccdCamera, Long.parseLong(value));
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getCCDGain (String telescope, String ccdCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\"  getCCDGain");
		
		try {
			return doubleFormat.format(ccd.getGain(telescope, ccdCamera)).replaceAll(",", "");
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public void setCCDExposureTime (String telescope, String ccdCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\" \""+value+"\"  setCCDExposureTime");
		
		try {
			ccd.setExposureTime(telescope,ccdCamera, Double.parseDouble(value));
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getCCDExposureTime (String telescope, String ccdCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\"  getCCDExposureTime");
		
		try {
			return doubleFormat.format(ccd.getExposureTime(telescope, ccdCamera)).replaceAll(",", "");
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public void setCCDContrast (String telescope, String ccdCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\" \""+value+"\"  setCCDContrast");
		
		try {
			ccd.setContrast(telescope,ccdCamera, Long.parseLong(value));
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getCCDContrast (String telescope, String ccdCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\"  getCCDContrast");
		
		try {
			return doubleFormat.format(ccd.getContrast(telescope, ccdCamera)).replaceAll(",", "");
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getCCDUrlImage (final String telescope, final String ccdCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\"   getCCDUrlImage");
			
		try {
			try {
				ccd.stopContinueMode(telescope, ccdCamera);						
			} catch (CCDTeleoperationException_Exception e) {
				_log.error(e.getMessage());
			}
				
			String imageId = ccd.startContinueMode(telescope, ccdCamera);
			String URL = "None";
			
			Boolean available = false;
			while (!available) {
				try {
					_log.info("ID= "+imageId);
					URL=ccd.getImageURL(telescope, ccdCamera, imageId);
					available = true;
				} catch (CCDTeleoperationException_Exception e) {
					_log.error(e.getMessage());
					if (e.getMessage().contains("not yet available"))								
						Thread.sleep(1000);
					else 
						throw new Exception();
				} // end-try
			} // end-while
			return URL;
		} catch (CCDTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			throw new Exception();
		}
	}
	
		
	String imagetaken = "None";
	Boolean varTaken = false;
	public String getCCDImage (final String telescope, 
								final String ccdCamera, 
								final String exposure) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+ccdCamera+"\" \""+exposure+"\"  getCCDImage");
		String idImagen = "None";
		
		ExecutorService exec = Executors.newFixedThreadPool(1);
		exec.execute(new Runnable() {
			public void run() {
				try {
					while (!varTaken) {
						varTaken = true;
						Double time = Double.parseDouble(exposure);
						_log.info("Running in: " + Thread.currentThread());

						ccd.setExposureTime(telescope, ccdCamera, time);
						
						_log.info("Start Exposure \""+ time+"\"");
						String imageId = ccd.startExposure(telescope, ccdCamera);
						
						if (time < 1) time = 1.0;
						
						Thread.sleep((long) (time*1000));						
						
						Boolean available = false;
						while (!available) {
							try {
								_log.info("ID= "+imageId);
								imagetaken=ccd.getImageURL(telescope, ccdCamera, imageId);
								available = true;
							} catch (CCDTeleoperationException_Exception e) {
								_log.error(e.getMessage());
								if (e.getMessage().contains("not yet available"))								
									Thread.sleep(1000);
								else 
									available = true;
							} // end-try
						} // end-while	
					} // end-while
				} catch (CCDTeleoperationException_Exception e) {
					_log.error(e.getMessage());
					imagetaken = "None";
				} catch (Exception e) {
					_log.error(e.getMessage());
					imagetaken = "None";
				} // end-try
			} // end-run	
		});
		
		exec.shutdown();
		
		try {
            boolean b = exec.awaitTermination(60, TimeUnit.SECONDS);
    		_log.info("All done: " + b);
            idImagen = imagetaken;
    		varTaken = false;
    		if (!b) {
    			_log.error("Timer has expired");
    			exec.shutdown();
    			throw new Exception();
    		}
    		return idImagen;
		} catch (InterruptedException e) {
			_log.info(e.getMessage());
			varTaken = false;
			throw new Exception();
		}
	}
	
	
	/*
	 *  SURVEILLANCE OPERATIONS
	 */
	
	public void setSCamBrightness (String telescope, String sCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\" \""+value+"\"  setSCamBrightness");
		
		try {
			scam.setBrightness(telescope,sCamera, Long.parseLong(value));
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getSCamBrightness (String telescope, String sCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\"  getSCamBrightness");
		
		try {
			return doubleFormat.format(scam.getBrightness(telescope, sCamera)).replaceAll(",", "");
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public void setSCamGain (String telescope, String sCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\" \""+value+"\"  setSCamGain");
		
		try {
			scam.setGain(telescope,sCamera, Long.parseLong(value));
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getSCamGain (String telescope, String sCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\"  getSCamGain");
		
		try {
			return doubleFormat.format(scam.getGain(telescope, sCamera)).replaceAll(",", "");
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public void setSCamExposureTime (String telescope, String sCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\" \""+value+"\"  setSCamExposureTime");
		
		try {
			scam.setExposureTime(telescope,sCamera, Double.parseDouble(value));
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getSCamExposureTime (String telescope, String sCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\"  getSCamExposureTime");
		
		try {
			return doubleFormat.format(scam.getExposureTime(telescope, sCamera)).replaceAll(",", "");
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public void setSCamContrast (String telescope, String sCamera, String value) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\" \""+value+"\"  setSCamContrast");
		
		try {
			scam.setContrast(telescope,sCamera, Long.parseLong(value));
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getSCamContrast (String telescope, String sCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\"  getSCamContrast");
		
		try {
			return doubleFormat.format(scam.getContrast(telescope, sCamera)).replaceAll(",", "");
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		} catch (Exception e) {
			_log.error(e.getMessage());
			errorInstance();
			throw new Exception();
		}
	}
	
	public String getSCamUrlImage (final String telescope, final String sCamera) 
	throws Exception {
		_log.info("\""+telescope+"\" \""+sCamera+"\"   getSCamUrlImage");
			
		try {
			return scam.getImageURL(telescope, sCamera);
		} catch (SCamTeleoperationException_Exception e) {
			_log.error(e.getMessage());
			throw new Exception();
		}
	}
	
	private void errorInstance() {
		_log.error("There was an error");
		synchronized(sync)
		{
			try {
				ccd = TeleoperationManager.getReference().getCCDTeleoperation();
				scam = TeleoperationManager.getReference().getSCamTeleoperation();
			} catch (Exception e) {
				_log.error(e.getMessage());
			}
		}
	}
}

