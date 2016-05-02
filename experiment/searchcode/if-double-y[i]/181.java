//#preprocessor

/* 
 * TestApp.java
 * 
 *  Rebuild, 2004-2011
 * Confidential and proprietary
 */
package littlecms.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import littlecms.internal.helper.Stream;
import littlecms.internal.helper.Utility;
import littlecms.internal.helper.VirtualPointer;
import littlecms.internal.lcms2.cmsCIELCh;
import littlecms.internal.lcms2.cmsCIELab;
import littlecms.internal.lcms2.cmsCIEXYZ;
import littlecms.internal.lcms2.cmsCIEXYZTRIPLE;
import littlecms.internal.lcms2.cmsCIExyY;
import littlecms.internal.lcms2.cmsCIExyYTRIPLE;
import littlecms.internal.lcms2.cmsContext;
import littlecms.internal.lcms2.cmsDICTentry;
import littlecms.internal.lcms2.cmsHANDLE;
import littlecms.internal.lcms2.cmsHPROFILE;
import littlecms.internal.lcms2.cmsHTRANSFORM;
import littlecms.internal.lcms2.cmsICCData;
import littlecms.internal.lcms2.cmsICCMeasurementConditions;
import littlecms.internal.lcms2.cmsICCViewingConditions;
import littlecms.internal.lcms2.cmsLogErrorHandlerFunction;
import littlecms.internal.lcms2.cmsMLU;
import littlecms.internal.lcms2.cmsNAMEDCOLORLIST;
import littlecms.internal.lcms2.cmsPipeline;
import littlecms.internal.lcms2.cmsProfileID;
import littlecms.internal.lcms2.cmsSAMPLER16;
import littlecms.internal.lcms2.cmsSEQ;
import littlecms.internal.lcms2.cmsScreening;
import littlecms.internal.lcms2.cmsStage;
import littlecms.internal.lcms2.cmsToneCurve;
import littlecms.internal.lcms2.cmsUcrBg;
import littlecms.internal.lcms2_internal._cmsTRANSFORM;
import littlecms.internal.lcms2_plugin.cmsFormatter;
import littlecms.internal.lcms2_plugin.cmsInterpParams;
import littlecms.internal.lcms2_plugin.cmsPluginMemHandler;
import littlecms.internal.lcms2_plugin.cmsPluginMemHandler.pluginFreePtr;
import littlecms.internal.lcms2_plugin.cmsPluginMemHandler.pluginMallocPtr;
import littlecms.internal.lcms2_plugin.cmsPluginMemHandler.pluginReallocPtr;

//#ifndef BlackBerrySDK4.5.0
import net.rim.device.api.util.MathUtilities;
//#endif
//#ifndef BlackBerrySDK4.6.1 | BlackBerrySDK4.6.0 | BlackBerrySDK4.5.0 | BlackBerrySDK4.2.1 | BlackBerrySDK4.2.0
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.util.Arrays;
import net.rim.device.api.system.Characters;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public final class TestApp extends UiApplication
{
	private static final String FILE_PREFIX = "file:///store/appdata/";
	//private static final String FILE_PREFIX = "file:///SDCard/BlackBerry/documents/";
	
	private static final String RES_PREFIX = "/littlecms/internal/res/";
	
	public static void main(String[] args)
	{
		TestApp.app = new TestApp();
		TestApp.app.runScreenStuff();
		TestApp.app.enterEventDispatcher();
	}
	
	private void runScreenStuff()
	{
		TestApp.screen = new TestScreen();
		this.pushScreen(TestApp.screen);
	}
	
	private static class ListOut extends OutputStream
	{
		//private StringBuffer buf;
		private int len;
		private ObjectListField list;
		
		public ListOut(ObjectListField list)
		{
			this.list = list;
			this.len = 0;
			//this.buf = new StringBuffer();
		}
		
		public void write(int b) throws IOException
		{
			write(new byte[]{(byte)b});
		}
		
		/*
		public void write(byte[] b, int off, int len) throws IOException
		{
			for(int i = off; i < len; i++)
			{
				if(b[i] != '\n')
				{
					buf.append((char)b[i]);
				}
				else
				{
					final ObjectListField listF = this.list;
					if(buf.length() > 1)
					{
						final String bufF = buf.toString();
						TestApp.app.invokeLater(new Runnable()
						{
							public void run()
							{
								listF.insert(0, bufF);
							}
						});
						buf.setLength(0);
					}
					else
					{
						TestApp.app.invokeLater(new Runnable()
						{
							public void run()
							{
								listF.insert(0);
							}
						});
					}
				}
			}
		}
		*/
		
		public void write(byte[] b, int off, int len) throws IOException
		{
			final ObjectListField listF = this.list;
			StringBuffer buf = new StringBuffer();
			int tLen = 0;
			for(int i = off; i < len; i++)
			{
				switch(b[i])
				{
					case '\n':
						if(buf.length() > 0)
						{
							final String bufF = buf.toString();
							this.len += tLen;
							buf.setLength(0);
							if(this.len != tLen)
							{
								TestApp.app.invokeAndWait(new Runnable()
								{
									public void run()
									{
										listF.delete(0);
										listF.insert(0, bufF);
									}
								});
							}
							else
							{
								TestApp.app.invokeAndWait(new Runnable()
								{
									public void run()
									{
										listF.insert(0, bufF);
									}
								});
							}
						}
						if(this.len > 0)
						{
							this.len = 0;
							tLen = 0;
						}
						else
						{
							TestApp.app.invokeAndWait(new Runnable()
							{
								public void run()
								{
									listF.insert(0);
								}
							});
						}
						break;
					case '\r':
						if(this.len > 0)
						{
							/*
							TestApp.app.invokeAndWait(new Runnable()
							{
								public void run()
								{
									listF.delete(0);
								}
							});
							*/
							//TODO
						}
						break;
					default:
						if(this.len > 0)
						{
							buf.append(listF.get(listF, 0));
						}
						buf.append((char)b[i]);
						tLen++;
						break;
				}
			}
			if(buf.length() > 0)
			{
				final String bufF = buf.toString();
				this.len += tLen;
				if(this.len != tLen)
				{
					TestApp.app.invokeAndWait(new Runnable()
					{
						public void run()
						{
							listF.delete(0);
							listF.insert(0, bufF);
						}
					});
				}
				else
				{
					TestApp.app.invokeAndWait(new Runnable()
					{
						public void run()
						{
							listF.insert(0, bufF);
						}
					});
				}
			}
		}
		
		public void close() throws IOException
		{
			//this.buf = null;
			super.close();
		}
	}
	
	//Now for the ported code
	
	// A single check. Returns 1 if success, 0 if failed
	private static interface TestFn
	{
		public int run();
	}
	
	// A parametric Tone curve test function
	private static interface dblfnptr
	{
		public float run(float x, final double[] Params);
	}
	
	// Some globals to keep track of error
	private static final int TEXT_ERROR_BUFFER_SIZE = 4096;
	
	private static char[] ReasonToFailBuffer = new char[TEXT_ERROR_BUFFER_SIZE];
	private static char[] SubTestBuffer = new char[TEXT_ERROR_BUFFER_SIZE];
	private static int TotalTests = 0, TotalFail = 0;
	private static boolean TrappedError;
	private static int SimultaneousErrors;
	private static PrintStream print;
	private static TestScreen screen;
	private static TestApp app;
	
	// Die, a fatal unexpected error is detected!
	private static void Die(final String Reason)
	{
		Utility.fprintf(print, "\n\nArrrgggg!!: %s!\n\n", new Object[]{Reason});
		print.flush();
	    //System.exit(1);
		TestApp.app.invokeLater(new Runnable()
		{
			public void run()
			{
				screen.close(); //Do this instead of System.exit(1);
			}
		});
	}
	
	// Memory management replacement -----------------------------------------------------------------------------
	
	// This is just a simple plug-in for malloc, free and realloc to keep track of memory allocated,
	// maximum requested as a single block and maximum allocated at a given time. Results are printed at the end
	private static int SingleHit, MaxAllocated=0, TotalMemory=0;
	
	// I'm hidding the size before the block. This is a well-known technique and probably the blocks coming from
	// malloc are built in a way similar to that, but I do on my own to be portable.
	private static class _cmsMemoryBlock
	{
		public static int SIZE_OF_MEM_HEADER = (4 * 3) + 4;
		
		//Not really used but here anyway
		public int KeepSize;
		public cmsContext WhoAllocated;
		public long HiSparc;
	}
	
	// This is a fake thread descriptor used to check thread integrity. 
	// Basically it returns a different threadID each time it is called.
	// Then the memory management replacement functions does check if each
	// free() is being called with same ContextID used on malloc()
	private static int n = 1;
	
	private static class cmsTestContext implements cmsContext
	{
		public int value;
		
		public cmsTestContext(int value)
		{
			this.value = value;
		}
		
		public String toString()
		{
			StringBuffer buf = new StringBuffer();
			Utility.sprintf(buf, "%#.8x", new Object[]{new Integer(this.value)});
			buf.deleteCharAt(buf.length() - 1);
			return buf.toString();
		}
	}
	
	private static cmsContext DbgThread()
	{
		return new cmsTestContext(n++);
	}
	
	// The allocate routine
	private static final pluginMallocPtr DebugMalloc = new pluginMallocPtr()
	{
		public VirtualPointer run(cmsContext ContextID, int size)
		{
			if (size <= 0)
			{
		       Die("malloc requested with zero bytes");
		    }
			
		    TotalMemory += size;
		    
		    if (TotalMemory > MaxAllocated)
		    {
		    	MaxAllocated = TotalMemory;
		    }
		    
		    if (size > SingleHit)
		    {
		    	SingleHit = size;
		    }
		    
		    VirtualPointer blk = new VirtualPointer(size + _cmsMemoryBlock.SIZE_OF_MEM_HEADER);
		    VirtualPointer.TypeProcessor proc = blk.getProcessor();
		    
		    proc.write(size, true);
		    proc.write(0, true);
		    if(ContextID == null)
		    {
		    	ContextID = new cmsTestContext(0);
		    }
		    proc.write(((cmsTestContext)ContextID).value, true);
		    
		    return blk;
		}
	};
	
	// The free routine
	private static final pluginFreePtr DebugFree = new pluginFreePtr()
	{
		public void run(cmsContext ContextID, VirtualPointer Ptr)
		{
			VirtualPointer blk;
		    
		    if (Ptr == null)
		    {
		        Die("NULL free (which is a no-op in C, but may be an clue of something going wrong)");
		    }
		    
		    blk = new VirtualPointer(Ptr);
		    blk.setPosition(0);
		    VirtualPointer.TypeProcessor proc = blk.getProcessor();
		    TotalMemory -= proc.readInt32(true);
		    
		    proc.readInt32(true);
		    
		    if(ContextID == null)
		    {
		    	ContextID = new cmsTestContext(0);
		    }
		    if (proc.readInt32(true) != ((cmsTestContext)ContextID).value)
		    {
		        Die("Trying to free memory allocated by a different thread");
		    }
		    
		    blk.free();
		}
	};
	
	// Reallocate, just a malloc, a copy and a free in this case.
	private static final pluginReallocPtr DebugRealloc = new pluginReallocPtr()
	{
		public VirtualPointer run(cmsContext ContextID, VirtualPointer Ptr, int NewSize)
		{
			VirtualPointer blk;
			VirtualPointer NewPtr;
		    int max_sz;
		    
		    if(ContextID == null)
		    {
		    	ContextID = new cmsTestContext(0);
		    }
		    NewPtr = DebugMalloc.run(ContextID, NewSize);
		    if (Ptr == null)
		    {
		    	return NewPtr;
		    }
		    
		    blk = new VirtualPointer(Ptr);
		    blk.movePosition(-_cmsMemoryBlock.SIZE_OF_MEM_HEADER);
		    VirtualPointer.TypeProcessor proc = blk.getProcessor();
		    max_sz = proc.readInt32() > NewSize ? NewSize : proc.readInt32();
		    NewPtr.writeRaw(Ptr, 0, max_sz);
		    DebugFree.run(ContextID, Ptr);
		    
		    return NewPtr;
		}
	};
	
	// Let's know the totals
	private static void DebugMemPrintTotals()
	{
	    Utility.fprintf(print, "[Memory statistics]\n", null);
	    Utility.fprintf(print, "Allocated = %d MaxAlloc = %d Single block hit = %d\n", new Object[]{new Integer(TotalMemory), new Integer(MaxAllocated), new Integer(SingleHit)});
	}
	
	// Here we go with the plug-in declaration
	private static final cmsPluginMemHandler DebugMemHandler;
	
	// Utils  -------------------------------------------------------------------------------------
	
	private static final cmsLogErrorHandlerFunction FatalErrorQuit = new cmsLogErrorHandlerFunction()
	{
		public void run(cmsContext ContextID, int ErrorCode, String Text)
		{
			Die(Text);
		}
	};
	
	// Print a dot for gauging
	private static void Dot()
	{
		Utility.fprintf(System.out, ".", null);
		print.flush();
	}
	
	// Keep track of the reason to fail
	private static void Fail(final String frm, Object[] args)
	{
	    Utility.vsprintf(ReasonToFailBuffer, frm, args);
	}
	
	// Keep track of subtest
	private static void SubTest(final String frm, Object[] args)
	{
	    Dot();
	    Utility.vsprintf(SubTestBuffer, frm, args);
	}
	
	// Memory string
	private static String MemStr(int size)
	{
		StringBuffer Buffer = new StringBuffer(1024);
		
	    if (size > 1024*1024)
	    {
	        Utility.sprintf(Buffer, "%g Mb", new Object[]{new Double(size / (1024.0*1024.0))});
	    }
	    else
	    {
	        if (size > 1024)
	        {
	        	Utility.sprintf(Buffer, "%g Kb", new Object[]{new Double(size / 1024.0)});
	        }
	        else
	        {
	        	Utility.sprintf(Buffer, "%g bytes", new Object[]{new Double(size)});
	        }
	    }
	    return Buffer.toString();
	}
	
	// The check framework
	private static void Check(final String Title, TestFn Fn)
	{
	    Utility.fprintf(print, "Checking %s ...", new Object[]{Title});
	    print.flush();
	    
	    ReasonToFailBuffer[0] = 0;
	    SubTestBuffer[0] = 0;
	    TrappedError = false;
	    SimultaneousErrors = 0;
	    TotalTests++;
	    
	    if (Fn.run() != 0 && !TrappedError)
	    {
	        // It is a good place to check memory
	        if (TotalMemory > 0)
	        {
	        	Utility.fprintf(print, "Ok, but %s are left!\n", new Object[]{MemStr(TotalMemory)});
	        }
	        else
	        {
	        	Utility.fprintf(print, "Ok.\n", null);
	        }
	    }
	    else
	    {
	    	Utility.fprintf(print, "FAIL!\n", null);
	        
	        if (SubTestBuffer[0] != 0)
	        {
	        	Utility.fprintf(print, "%s: [%s]\n\t%s\n", new Object[]{Title, SubTestBuffer, ReasonToFailBuffer});
	        }
	        else
	        {
	        	Utility.fprintf(print, "%s:\n\t%s\n", new Object[]{Title, ReasonToFailBuffer});
	        }
	        
	        if (SimultaneousErrors > 1)
	        {
	        	Utility.fprintf(print, "\tMore than one (%d) errors were reported\n", new Object[]{new Integer(SimultaneousErrors)}); 
	        }
	        
	        TotalFail++;
	    }   
	    print.flush();
	}
	
	// Dump a tone curve, for easy diagnostic
	private static void DumpToneCurve(cmsToneCurve gamma, final String FileName)
	{
	    cmsHANDLE hIT8;
	    int i;
	    
	    hIT8 = lcms2.cmsIT8Alloc(gamma.InterpParams.ContextID);
	    
	    lcms2.cmsIT8SetPropertyDbl(hIT8, "NUMBER_OF_FIELDS", 2);
	    lcms2.cmsIT8SetPropertyDbl(hIT8, "NUMBER_OF_SETS", gamma.nEntries);
	    
	    lcms2.cmsIT8SetDataFormat(hIT8, 0, "SAMPLE_ID");
	    lcms2.cmsIT8SetDataFormat(hIT8, 1, "VALUE");
	    
	    for (i=0; i < gamma.nEntries; i++)
	    {
	    	StringBuffer Val = new StringBuffer(30);
	        
	        Utility.sprintf(Val, "%d", new Object[]{new Integer(i)});
	        lcms2.cmsIT8SetDataRowCol(hIT8, i, 0, Val.toString());
	        Val.setLength(0);
	        Utility.sprintf(Val, "0x%x", new Object[]{new Short(gamma.Table16[i])});
	        lcms2.cmsIT8SetDataRowCol(hIT8, i, 1, Val.toString());
	    }
	    
	    lcms2.cmsIT8SaveToFile(hIT8, FileName);
	    lcms2.cmsIT8Free(hIT8);
	}
	
	// -------------------------------------------------------------------------------------------------


	// Used to perform several checks. 
	// The space used is a clone of a well-known commercial 
	// color space which I will name "Above RGB"
	private static cmsHPROFILE Create_AboveRGB()
	{
	    cmsToneCurve[] Curve = new cmsToneCurve[3];
	    cmsHPROFILE hProfile;
	    cmsCIExyY D65 = new cmsCIExyY();
	    cmsCIExyYTRIPLE Primaries = new cmsCIExyYTRIPLE(new double[]{
	    		0.64, 0.33, 1,
	    		0.21, 0.71, 1,
	    		0.15, 0.06, 1
	    });
	    
	    Curve[0] = Curve[1] = Curve[2] = lcms2.cmsBuildGamma(DbgThread(), 2.19921875);
	    
	    lcms2.cmsWhitePointFromTemp(D65, 6504);
	    hProfile = lcms2.cmsCreateRGBProfileTHR(DbgThread(), D65, Primaries, Curve);
	    lcms2.cmsFreeToneCurve(Curve[0]);
	    
	    return hProfile;
	}
	
	// A gamma-2.2 gray space
	private static cmsHPROFILE Create_Gray22()
	{
	    cmsHPROFILE hProfile;
	    cmsToneCurve Curve = lcms2.cmsBuildGamma(DbgThread(), 2.2);
	    if (Curve == null)
	    {
	    	return null;
	    }
	    
	    hProfile = lcms2.cmsCreateGrayProfileTHR(DbgThread(), lcms2.cmsD50_xyY, Curve);
	    lcms2.cmsFreeToneCurve(Curve);
	    
	    return hProfile;
	}
	
	private static cmsHPROFILE Create_GrayLab()
	{
	    cmsHPROFILE hProfile;
	    cmsToneCurve Curve = lcms2.cmsBuildGamma(DbgThread(), 1.0);
	    if (Curve == null)
	    {
	    	return null;
	    }
	    
	    hProfile = lcms2.cmsCreateGrayProfileTHR(DbgThread(), lcms2.cmsD50_xyY, Curve);
	    lcms2.cmsFreeToneCurve(Curve);
	    
	    lcms2.cmsSetPCS(hProfile, lcms2.cmsSigLabData);
	    return hProfile;
	}
	
	// A CMYK devicelink that adds gamma 3.0 to each channel
	private static cmsHPROFILE Create_CMYK_DeviceLink()
	{
	    cmsHPROFILE hProfile;
	    cmsToneCurve[] Tab = new cmsToneCurve[4];
	    cmsToneCurve Curve = lcms2.cmsBuildGamma(DbgThread(), 3.0);
	    if (Curve == null)
	    {
	    	return null;
	    }
	    
	    Tab[0] = Curve;
	    Tab[1] = Curve;
	    Tab[2] = Curve;
	    Tab[3] = Curve;
	    
	    hProfile = lcms2.cmsCreateLinearizationDeviceLinkTHR(DbgThread(), lcms2.cmsSigCmykData, Tab);
	    if (hProfile == null)
	    {
	    	return null;
	    }
	    
	    lcms2.cmsFreeToneCurve(Curve);
	    
	    return hProfile;
	}
	
	// Create a fake CMYK profile, without any other requeriment that being coarse CMYK. 
	// DONT USE THIS PROFILE FOR ANYTHING, IT IS USELESS BUT FOR TESTING PURPOSES.
	private static class FakeCMYKParams
	{
		public cmsHTRANSFORM hLab2sRGB;
		public cmsHTRANSFORM sRGB2Lab;
		public cmsHTRANSFORM hIlimit;
	}
	
	private static double Clip(double v)
	{
	    if (v < 0)
	    {
	    	return 0;
	    }
	    if (v > 1)
	    {
	    	return 1;
	    }
	    
	    return v;
	}
	
	private static final cmsSAMPLER16 ForwardSampler = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			FakeCMYKParams p = (FakeCMYKParams)Cargo;
		    double[] rgb = new double[3], cmyk = new double[4];
		    double c, m, y, k;
		    
		    lcms2.cmsDoTransform(p.hLab2sRGB, In, rgb, 1);
		    
		    c = 1 - rgb[0];
		    m = 1 - rgb[1];
		    y = 1 - rgb[2];
		    
		    k = (c < m ? Math.min(c, y) : Math.min(m, y));
		    
		    // NONSENSE WARNING!: I'm doing this just because this is a test 
		    // profile that may have ink limit up to 400%. There is no UCR here
		    // so the profile is basically useless for anything but testing.
		    
		    cmyk[0] = c;
		    cmyk[1] = m;
		    cmyk[2] = y;
		    cmyk[3] = k;
		    
		    lcms2.cmsDoTransform(p.hIlimit, cmyk, Out, 1);
		    
		    return 1;
		}
	};
	
	private static final cmsSAMPLER16 ReverseSampler = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			FakeCMYKParams p = (FakeCMYKParams)Cargo;
		    double c, m, y, k;
		    double[] rgb = new double[3];
		    
		    c = (In[0] & 0xFFFF) / 65535.0;
		    m = (In[1] & 0xFFFF) / 65535.0;
		    y = (In[2] & 0xFFFF) / 65535.0;
		    k = (In[3] & 0xFFFF) / 65535.0;
		    
		    if (k == 0)
		    {
		        rgb[0] = Clip(1 - c);
		        rgb[1] = Clip(1 - m);
		        rgb[2] = Clip(1 - y);
		    }
		    else
		    {
		        if (k == 1)
		        {
		            rgb[0] = rgb[1] = rgb[2] = 0;
		        }
		        else
		        {
		            rgb[0] = Clip((1 - c) * (1 - k));
		            rgb[1] = Clip((1 - m) * (1 - k));
		            rgb[2] = Clip((1 - y) * (1 - k));       
		        }
		    }
		    
		    lcms2.cmsDoTransform(p.sRGB2Lab, rgb, Out, 1);
		    return 1;
		}
	};
	
	private static cmsHPROFILE CreateFakeCMYK(double InkLimit, boolean lUseAboveRGB)
	{
	    cmsHPROFILE hICC;
	    cmsPipeline AToB0, BToA0;
	    cmsStage CLUT;
	    cmsContext ContextID;
	    FakeCMYKParams p = new FakeCMYKParams();
	    cmsHPROFILE hLab, hsRGB, hLimit;
	    int cmykfrm;
	    
	    if (lUseAboveRGB)
	    {
	    	hsRGB = Create_AboveRGB();
	    }
	    else
	    {
	    	hsRGB = lcms2.cmsCreate_sRGBProfile();
	    }
	    
	    hLab   = lcms2.cmsCreateLab4Profile(null);
	    hLimit = lcms2.cmsCreateInkLimitingDeviceLink(lcms2.cmsSigCmykData, InkLimit);
	    
	    cmykfrm = lcms2.FLOAT_SH(1) | lcms2.BYTES_SH(0)|lcms2.CHANNELS_SH(4);
	    p.hLab2sRGB = lcms2.cmsCreateTransform(hLab,  lcms2.TYPE_Lab_16,  hsRGB, lcms2.TYPE_RGB_DBL, lcms2.INTENT_PERCEPTUAL, lcms2.cmsFLAGS_NOOPTIMIZE|lcms2.cmsFLAGS_NOCACHE);
	    p.sRGB2Lab  = lcms2.cmsCreateTransform(hsRGB, lcms2.TYPE_RGB_DBL, hLab,  lcms2.TYPE_Lab_16,  lcms2.INTENT_PERCEPTUAL, lcms2.cmsFLAGS_NOOPTIMIZE|lcms2.cmsFLAGS_NOCACHE);
	    p.hIlimit   = lcms2.cmsCreateTransform(hLimit, cmykfrm, null, lcms2.TYPE_CMYK_16, lcms2.INTENT_PERCEPTUAL, lcms2.cmsFLAGS_NOOPTIMIZE|lcms2.cmsFLAGS_NOCACHE);
	    
	    lcms2.cmsCloseProfile(hLab); lcms2.cmsCloseProfile(hsRGB); lcms2.cmsCloseProfile(hLimit);
	    
	    ContextID = DbgThread();
	    hICC = lcms2.cmsCreateProfilePlaceholder(ContextID);
	    if (hICC == null)
	    {
	    	return null;
	    }
	    
	    lcms2.cmsSetProfileVersion(hICC, 4.2);
	    
	    lcms2.cmsSetDeviceClass(hICC, lcms2.cmsSigOutputClass);
	    lcms2.cmsSetColorSpace(hICC,  lcms2.cmsSigCmykData);
	    lcms2.cmsSetPCS(hICC,         lcms2.cmsSigLabData);
	    
	    BToA0 = lcms2.cmsPipelineAlloc(ContextID, 3, 4);
	    if (BToA0 == null)
	    {
	    	return null;
	    }
	    CLUT = lcms2.cmsStageAllocCLut16bit(ContextID, 17, 3, 4, null);
	    if (CLUT == null)
	    {
	    	return null;
	    }
	    if (!lcms2.cmsStageSampleCLut16bit(CLUT, ForwardSampler, p, 0))
	    {
	    	return null;
	    }
	    
	    lcms2.cmsPipelineInsertStage(BToA0, lcms2.cmsAT_BEGIN, lcms2_internal._cmsStageAllocIdentityCurves(ContextID, 3)); 
	    lcms2.cmsPipelineInsertStage(BToA0, lcms2.cmsAT_END, CLUT);
	    lcms2.cmsPipelineInsertStage(BToA0, lcms2.cmsAT_END, lcms2_internal._cmsStageAllocIdentityCurves(ContextID, 4));
	    
	    if (!lcms2.cmsWriteTag(hICC, lcms2.cmsSigBToA0Tag, BToA0))
	    {
	    	return null;
	    }
	    lcms2.cmsPipelineFree(BToA0);
	    
	    AToB0 = lcms2.cmsPipelineAlloc(ContextID, 4, 3);
	    if (AToB0 == null)
	    {
	    	return null;
	    }
	    CLUT = lcms2.cmsStageAllocCLut16bit(ContextID, 17, 4, 3, null);
	    if (CLUT == null)
	    {
	    	return null;
	    }
	    if (!lcms2.cmsStageSampleCLut16bit(CLUT, ReverseSampler, p, 0))
	    {
	    	return null;
	    }
	    
	    lcms2.cmsPipelineInsertStage(AToB0, lcms2.cmsAT_BEGIN, lcms2_internal._cmsStageAllocIdentityCurves(ContextID, 4)); 
	    lcms2.cmsPipelineInsertStage(AToB0, lcms2.cmsAT_END, CLUT);
	    lcms2.cmsPipelineInsertStage(AToB0, lcms2.cmsAT_END, lcms2_internal._cmsStageAllocIdentityCurves(ContextID, 3));
	    
	    if (!lcms2.cmsWriteTag(hICC, lcms2.cmsSigAToB0Tag, AToB0))
	    {
	    	return null;
	    }
	    lcms2.cmsPipelineFree(AToB0);
	    
	    lcms2.cmsDeleteTransform(p.hLab2sRGB);
	    lcms2.cmsDeleteTransform(p.sRGB2Lab);
	    lcms2.cmsDeleteTransform(p.hIlimit);
	    
	    lcms2.cmsLinkTag(hICC, lcms2.cmsSigAToB1Tag, lcms2.cmsSigAToB0Tag);
	    lcms2.cmsLinkTag(hICC, lcms2.cmsSigAToB2Tag, lcms2.cmsSigAToB0Tag);
	    lcms2.cmsLinkTag(hICC, lcms2.cmsSigBToA1Tag, lcms2.cmsSigBToA0Tag);
	    lcms2.cmsLinkTag(hICC, lcms2.cmsSigBToA2Tag, lcms2.cmsSigBToA0Tag);
	    
	    return hICC;    
	}
	
	// Does create several profiles for latter use------------------------------------------------------------------------------------------------
	
	private static int OneVirtual(cmsHPROFILE h, final String SubTestTxt, final String FileName)
	{
	    SubTest(SubTestTxt, null);
	    if (h == null)
	    {
	    	return 0;
	    }
	    
	    if (!lcms2.cmsSaveProfileToFile(h, FILE_PREFIX + FileName))
	    {
	    	return 0;
	    }
	    lcms2.cmsCloseProfile(h);
	    
	    h = lcms2.cmsOpenProfileFromFile(FILE_PREFIX + FileName, "r");
	    if (h == null)
	    {
	    	return 0;
	    }
	    
	    // Do some test....
	    
	    //XXX Temp
	    
	    lcms2.cmsCloseProfile(h);
	    
	    return 1;
	}
	
	// This test checks the ability of lcms2 to save its built-ins as valid profiles. 
	// It does not check the functionality of such profiles
	private static final TestFn CreateTestProfiles = new TestFn()
	{
		public int run()
		{
			cmsHPROFILE h;
			
			//XXX Takes 6.24 minutes to complete, speed up
		    h = lcms2.cmsCreate_sRGBProfileTHR(DbgThread());
		    if (OneVirtual(h, "sRGB profile", "sRGBlcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = Create_AboveRGB();
		    if (OneVirtual(h, "aRGB profile", "aRGBlcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = Create_Gray22();
		    if (OneVirtual(h, "Gray profile", "graylcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = Create_GrayLab();
		    if (OneVirtual(h, "Gray Lab profile", "glablcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = Create_CMYK_DeviceLink();
		    if (OneVirtual(h, "Linearization profile", "linlcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // -------
		    h = lcms2.cmsCreateInkLimitingDeviceLinkTHR(DbgThread(), lcms2.cmsSigCmykData, 150);
		    if (h == null)
		    {
		    	return 0;
		    }
		    if (OneVirtual(h, "Ink-limiting profile", "limitlcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ------
		    
		    h = lcms2.cmsCreateLab2ProfileTHR(DbgThread(), null);
		    if (OneVirtual(h, "Lab 2 identity profile", "labv2lcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = lcms2.cmsCreateLab4ProfileTHR(DbgThread(), null);
		    if (OneVirtual(h, "Lab 4 identity profile", "labv4lcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = lcms2.cmsCreateXYZProfileTHR(DbgThread());
		    if (OneVirtual(h, "XYZ identity profile", "xyzlcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ----
		    
		    h = lcms2.cmsCreateNULLProfileTHR(DbgThread());
		    if (OneVirtual(h, "NULL profile", "nullcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ---
		    
		    h = lcms2.cmsCreateBCHSWabstractProfileTHR(DbgThread(), 17, 0, 0, 0, 0, 5000, 6000);
		    if (OneVirtual(h, "BCHS profile", "bchslcms2.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    // ---
		    
		    h = CreateFakeCMYK(300, false);
		    if (OneVirtual(h, "Fake CMYK profile", "lcms2cmyk.icc") == 0)
		    {
		    	return 0;
		    }
		    
		    //Now create the temporary, built-in profiles
		    if(fileOperation("bad.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    if(fileOperation("test1.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    if(fileOperation("test2.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    if(fileOperation("test3.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    if(fileOperation("toosmall.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    if(fileOperation("test4.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    if(fileOperation("test5.icc", createTemp) != 0)
		    {
		    	return 0;
		    }
		    
		    return 1;
		}
	};
	
	private interface FileOp
	{
		public void run(String oFile, FileConnection file) throws IOException;
	}
	
	private static final FileOp remove = new FileOp()
	{
		public void run(String oFile, FileConnection file) throws IOException
		{
			if(file.exists())
			{
				file.delete();
			}
		}
	};
	
	private static final FileOp createTemp = new FileOp()
	{
		public void run(String oFile, FileConnection file) throws IOException
		{
			if(file.exists())
			{
				file.truncate(0L);
			}
			else
			{
				file.create();
			}
			byte[] buffer = new byte[4096];
    		OutputStream out = file.openOutputStream();
    		InputStream in = this.getClass().getResourceAsStream(RES_PREFIX + oFile);
    		int count = in.read(buffer, 0, 4096);
    		while(count > 0)
    		{
    			out.write(buffer, 0, count);
    			count = in.read(buffer, 0, 4096);
    		}
    		in.close();
    		out.close();
		}
	};
	
	private static int fileOperation(final String file, FileOp fop)
	{
		FileConnection ifile = null;
		boolean exp = false;
		try
		{
			ifile = (FileConnection)Connector.open(FILE_PREFIX + file, Connector.READ_WRITE);
			fop.run(file, ifile);
		}
		catch(Exception e)
		{
			exp = true;
		}
		finally
		{
			if(ifile != null)
			{
				try
				{
					ifile.close();
				}
				catch(Exception e)
				{
				}
			}
		}
		return exp ? 1 : 0;
	}
	
	private static void RemoveTestProfiles()
	{
	    fileOperation("sRGBlcms2.icc", remove);
	    fileOperation("aRGBlcms2.icc", remove);
	    fileOperation("graylcms2.icc", remove);
	    fileOperation("linlcms2.icc", remove);
	    fileOperation("limitlcms2.icc", remove);
	    fileOperation("labv2lcms2.icc", remove);
	    fileOperation("labv4lcms2.icc", remove);
	    fileOperation("xyzlcms2.icc", remove);
	    fileOperation("nullcms2.icc", remove);
	    fileOperation("bchslcms2.icc", remove);
	    fileOperation("lcms2cmyk.icc", remove);
	    fileOperation("glablcms2.icc", remove);
	    
	    //Also remove the temporary, built-in profiles
	    fileOperation("bad.icc", remove);
	    fileOperation("test1.icc", remove);
	    fileOperation("test2.icc", remove);
	    fileOperation("test3.icc", remove);
	    fileOperation("toosmall.icc", remove);
	    fileOperation("test4.icc", remove);
	    fileOperation("test5.icc", remove);
	}
	
	// -------------------------------------------------------------------------------------------------
	
	// Are we little or big endian?  From Harbison&Steele.
	private static final TestFn CheckEndianess = new TestFn()
	{
		public int run()
		{
			boolean BigEndian, IsOk;
			VirtualPointer u = new VirtualPointer(8);
			u.getProcessor().write(1L);
			
		    BigEndian = u.readRaw(7) == 1;
		    
//#ifdef CMS_USE_BIG_ENDIAN
		    IsOk = BigEndian;
//#else
		    IsOk = !BigEndian;
//#endif
		    
		    if (!IsOk)
		    {
		        Fail("\nOOOPPSS! You have CMS_USE_BIG_ENDIAN toggle misconfigured!\n\n" + 
		            "Please, edit lcms2.h and %s the CMS_USE_BIG_ENDIAN toggle.\n", new Object[]{BigEndian? "uncomment" : "comment"});
		        return 0;
		    }
		    
		    return 1;
		}
	};
	
	// Check quick floor
	private static final TestFn CheckQuickFloor = new TestFn()
	{
		public int run()
		{
			if ((lcms2_internal._cmsQuickFloor(1.234) != 1) ||
		        (lcms2_internal._cmsQuickFloor(32767.234) != 32767) ||
		        (lcms2_internal._cmsQuickFloor(-1.234) != -2) ||
		        (lcms2_internal._cmsQuickFloor(-32767.1) != -32768))
			{
				Fail("\nOOOPPSS! _cmsQuickFloor() does not work as expected in your machine!\n\n" +
						"Please, edit lcms.h and uncomment the CMS_DONT_USE_FAST_FLOOR toggle.\n", null);
				return 0;
		    }
			
		    return 1;
		}
	};
	
	// Quick floor restricted to word
	private static final TestFn CheckQuickFloorWord = new TestFn()
	{
		public int run()
		{
			int i;
			
		    for (i=0; i < 65535; i++)
		    {
		        if ((lcms2_internal._cmsQuickFloorWord(i + 0.1234) & 0xFFFF) != i)
		        {
		            Fail("\nOOOPPSS! _cmsQuickFloorWord() does not work as expected in your machine!\n\n" +
		                "Please, edit lcms.h and uncomment the CMS_DONT_USE_FAST_FLOOR toggle.\n", null);
		            return 0;
		        }
		    }
		    
		    return 1;
		}
	};
	
	// -------------------------------------------------------------------------------------------------
	
	// Precision stuff. 
	
	// On 15.16 fixed point, this is the maximum we can obtain. Remember ICC profiles have storage limits on this number 
	private static final double FIXED_PRECISION_15_16 = (1.0 / 65535.0);
	
	// On 8.8 fixed point, that is the max we can obtain.
	private static final double FIXED_PRECISION_8_8 = (1.0 / 255.0);
	
	// On cmsFloat32Number type, this is the precision we expect
	private static final double FLOAT_PRECISSION = (0.00001);
	
	private static double MaxErr;
	private static double AllowedErr = FIXED_PRECISION_15_16;
	
	private static boolean IsGoodVal(final String title, double in, double out, double max)
	{
	    double Err = Math.abs(in - out);
	    
	    if (Err > MaxErr)
	    {
	    	MaxErr = Err;
	    }
	    
	    if ((Err > max ))
	    {
	    	Fail("(%s): Must be %f, But is %f ", new Object[]{title, new Double(in), new Double(out)});
	    	return false;
	    }
	    
	    return true;
	}
	
	private static boolean IsGoodFixed15_16(final String title, double in, double out)
	{   
	    return IsGoodVal(title, in, out, FIXED_PRECISION_15_16);
	}
	
	private static boolean IsGoodFixed8_8(final String title, double in, double out)
	{
	    return IsGoodVal(title, in, out, FIXED_PRECISION_8_8);
	}
	
	private static boolean IsGoodWord(final String title, short in, short out)
	{
	    if ((Math.abs(in - out) > 0 ))
	    {
	    	Fail("(%s): Must be %x, But is %x ", new Object[]{title, new Short(in), new Short(out)});
	        return false;
	    }
	    
	    return true;
	}
	
	private static boolean IsGoodWordPrec(final String title, short in, short out, short maxErr)
	{
	    if ((Math.abs(in - out) > maxErr ))
	    {
	    	Fail("(%s): Must be %x, But is %x ", new Object[]{title, new Short(in), new Short(out)});
	        return false;
	    }
	    
	    return true;
	}
	
	// Fixed point ----------------------------------------------------------------------------------------------
	
	private static int TestSingleFixed15_16(double d)
	{
	    int f = lcms2_internal._cmsDoubleTo15Fixed16(d);
	    double RoundTrip = lcms2_internal._cms15Fixed16toDouble(f);
	    double Error     = Math.abs(d - RoundTrip);
	    
	    return ( Error <= FIXED_PRECISION_15_16) ? 1 : 0;
	}
	
	private static final TestFn CheckFixedPoint15_16 = new TestFn()
	{
		public int run()
		{
			if (TestSingleFixed15_16(1.0) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(2.0) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(1.23456) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(0.99999) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(0.1234567890123456789099999) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(-1.0) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(-2.0) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(-1.23456) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(-1.1234567890123456789099999) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(+32767.1234567890123456789099999) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed15_16(-32767.1234567890123456789099999) == 0)
			{
				return 0;
			}
		    return 1;
		}
	};
	
	private static int TestSingleFixed8_8(double d)
	{
	    short f = lcms2_internal._cmsDoubleTo8Fixed8(d);
	    double RoundTrip = lcms2_internal._cms8Fixed8toDouble(f);
	    double Error     = Math.abs(d - RoundTrip);
	    
	    return ( Error <= FIXED_PRECISION_8_8) ? 1 : 0;
	}
	
	private static final TestFn CheckFixedPoint8_8 = new TestFn()
	{
		public int run()
		{
			if (TestSingleFixed8_8(1.0) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed8_8(2.0) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed8_8(1.23456) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed8_8(0.99999) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed8_8(0.1234567890123456789099999) == 0)
			{
				return 0;
			}
		    if (TestSingleFixed8_8(+255.1234567890123456789099999) == 0)
			{
				return 0;
			}
		    
		    return 1;
		}
	};
	
	// Linear interpolation -----------------------------------------------------------------------------------------------
	
	// Since prime factors of 65535 (FFFF) are,
	//
	//	            0xFFFF = 3 * 5 * 17 * 257
	//
	// I test tables of 2, 4, 6, and 18 points, that will be exact.
	
	private static void BuildTable(int n, short[] Tab, boolean Descending)
	{
	    int i;
	    
	    for (i=0; i < n; i++) {
	        double v = (65535.0 * i ) / (n-1);
	        
	        Tab[Descending ? (n - i - 1) : i ] = (short)Math.floor(v + 0.5);
	    }
	}
	
	// A single function that does check 1D interpolation
	// nNodesToCheck = number on nodes to check
	// Down = Create decreasing tables
	// Reverse = Check reverse interpolation
	// max_err = max allowed error 
	
	private static int Check1D(int nNodesToCheck, boolean Down, int max_err)
	{
	    int i;
	    short[] in = new short[1], out = new short[1];
	    cmsInterpParams p;
	    short[] Tab;
	    
	    Tab = new short[nNodesToCheck];
	    
	    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), nNodesToCheck, 1, 1, Tab, lcms2_plugin.CMS_LERP_FLAGS_16BITS);
	    if (p == null)
	    {
	    	return 0;
	    }
	    
	    BuildTable(nNodesToCheck, Tab, Down);
	    
	    for (i=0; i <= 0xffff; i++)
	    {
	        in[0] = (short)i;
	        out[0] = 0;
	        
	        p.Interpolation.get16().run(in, out, p);
	        
	        if (Down)
	        {
	        	out[0] = (short)(0xffff - (out[0] & 0xFFFF));
	        }
	        
	        if (Math.abs((out[0] & 0xFFFF) - (in[0] & 0xFFFF)) > max_err)
	        {
	            Fail("(%dp): Must be %x, But is %x : ", new Object[]{new Integer(nNodesToCheck), new Short(in[0]), new Short(out[0])});
	            lcms2_internal._cmsFreeInterpParams(p);
	            return 0;
	        }
	    }
	    
	    lcms2_internal._cmsFreeInterpParams(p);
	    return 1;
	}
	
	private static final TestFn Check1DLERP2 = new TestFn()
	{
		public int run()
		{
			return Check1D(2, false, 0);
		}
	};
	
	private static final TestFn Check1DLERP3 = new TestFn()
	{
		public int run()
		{
			return Check1D(3, false, 1);
		}
	};
	
	private static final TestFn Check1DLERP4 = new TestFn()
	{
		public int run()
		{
			return Check1D(4, false, 0);
		}
	};
	
	private static final TestFn Check1DLERP6 = new TestFn()
	{
		public int run()
		{
			return Check1D(6, false, 0);
		}
	};
	
	private static final TestFn Check1DLERP18 = new TestFn()
	{
		public int run()
		{
			return Check1D(18, false, 0);
		}
	};
	
	private static final TestFn Check1DLERP2Down = new TestFn()
	{
		public int run()
		{
			return Check1D(2, true, 0);
		}
	};
	
	private static final TestFn Check1DLERP3Down = new TestFn()
	{
		public int run()
		{
			return Check1D(3, true, 1);
		}
	};
	
	private static final TestFn Check1DLERP6Down = new TestFn()
	{
		public int run()
		{
			return Check1D(6, true, 0);
		}
	};
	
	private static final TestFn Check1DLERP18Down = new TestFn()
	{
		public int run()
		{
			return Check1D(18, true, 0);
		}
	};
	
	private static final TestFn ExhaustiveCheck1DLERP = new TestFn()
	{
		public int run()
		{
			int j;
		    
		    Utility.fprintf(print, "\n", null);
		    for (j=10; j <= 4096; j++)
		    {
		        if ((j % 10) == 0)
		        {
		        	Utility.fprintf(print, "%d    \r", new Object[]{new Integer(j)});
		        }
		        
		        if (Check1D(j, false, 1) == 0)
		        {
		        	return 0;    
		        }
		    }
		    
		    Utility.fprintf(print, "\rResult is ", null);
		    return 1;
		}
	};
	
	private static final TestFn ExhaustiveCheck1DLERPDown = new TestFn()
	{
		public int run()
		{
			int j;
		    
			Utility.fprintf(print, "\n", null);
		    for (j=10; j <= 4096; j++)
		    {
		        if ((j % 10) == 0)
		        {
		        	Utility.fprintf(print, "%d    \r", new Object[]{new Integer(j)});
		        }
		        
		        if (Check1D(j, true, 1) == 0)
		        {
		        	return 0; 
		        }
		    }
		    
		    Utility.fprintf(print, "\rResult is ", null);
		    return 1;
		}
	};
	
	// 3D interpolation -------------------------------------------------------------------------------------------------
	
	private static final TestFn Check3DinterpolationFloatTetrahedral = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int i;
		    float[] In = new float[3], Out = new float[3];
		    float[] FloatTable = { //R     G    B
		        0,    0,   0,     // B=0,G=0,R=0
		        0,    0,  .25f,   // B=1,G=0,R=0
		        
		        0,   .5f,    0,   // B=0,G=1,R=0
		        0,   .5f,  .25f,  // B=1,G=1,R=0
		        
		        1,    0,    0,    // B=0,G=0,R=1
		        1,    0,  .25f,   // B=1,G=0,R=1
		        
		        1,    .5f,   0,   // B=0,G=1,R=1
		        1,    .5f,  .25f  // B=1,G=1,R=1
		    };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, FloatTable, lcms2_plugin.CMS_LERP_FLAGS_FLOAT);
		    
		    MaxErr = 0.0;
		    for (i=0; i < 0xffff; i++)
		    {
		    	In[0] = In[1] = In[2] = (i / 65535.0F);
		    	
		        p.Interpolation.getFloat().run(In, Out, p);
		        
		        if (!IsGoodFixed15_16("Channel 1", Out[0], In[0]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodFixed15_16("Channel 2", Out[1], In[1] / 2.F))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodFixed15_16("Channel 3", Out[2], In[2] / 4.F))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn Check3DinterpolationFloatTrilinear = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int i;
		    float[] In = new float[3], Out = new float[3];
		    float[] FloatTable = { //R     G    B
		        0,    0,   0,     // B=0,G=0,R=0
		        0,    0,  .25f,   // B=1,G=0,R=0
		        
		        0,   .5f,    0,   // B=0,G=1,R=0
		        0,   .5f,  .25f,  // B=1,G=1,R=0
		        
		        1,    0,    0,    // B=0,G=0,R=1
		        1,    0,  .25f,   // B=1,G=0,R=1
		        
		        1,    .5f,   0,   // B=0,G=1,R=1
		        1,    .5f,  .25f  // B=1,G=1,R=1
		    };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, FloatTable, lcms2_plugin.CMS_LERP_FLAGS_FLOAT|lcms2_plugin.CMS_LERP_FLAGS_TRILINEAR);
		    
		    MaxErr = 0.0;
		    for (i=0; i < 0xffff; i++)
		    {
		    	In[0] = In[1] = In[2] = (i / 65535.0F);
		    	
		        p.Interpolation.getFloat().run(In, Out, p);
		        
		        if (!IsGoodFixed15_16("Channel 1", Out[0], In[0]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodFixed15_16("Channel 2", Out[1], In[1] / 2.F))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodFixed15_16("Channel 3", Out[2], In[2] / 4.F))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn Check3DinterpolationTetrahedral16 = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int i;
		    short[] In = new short[3], Out = new short[3];
		    short[] Table = { 
		            0,    0,   0,     
		            0,    0,   (short)0xffff,    
		            
		            0,    (short)0xffff,    0,   
		            0,    (short)0xffff,    (short)0xffff,  
		            
		            (short)0xffff,    0,    0,    
		            (short)0xffff,    0,    (short)0xffff,   
		            
		            (short)0xffff,    (short)0xffff,   0,    
		            (short)0xffff,    (short)0xffff,   (short)0xffff    
		        };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, Table, lcms2_plugin.CMS_LERP_FLAGS_16BITS);
		    
		    MaxErr = 0.0;
		    for (i=0; i < 0xffff; i++)
		    {
		    	In[0] = In[1] = In[2] = (short)i;
		    	
		        p.Interpolation.get16().run(In, Out, p);
		        
		        if (!IsGoodWord("Channel 1", Out[0], In[0]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodWord("Channel 2", Out[1], In[1]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodWord("Channel 3", Out[2], In[2]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn Check3DinterpolationTrilinear16 = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int i;
		    short[] In = new short[3], Out = new short[3];
		    short[] Table = { 
		            0,    0,   0,     
		            0,    0,   (short)0xffff,    
		            
		            0,    (short)0xffff,    0,   
		            0,    (short)0xffff,    (short)0xffff,  
		            
		            (short)0xffff,    0,    0,    
		            (short)0xffff,    0,    (short)0xffff,   
		            
		            (short)0xffff,    (short)0xffff,   0,    
		            (short)0xffff,    (short)0xffff,   (short)0xffff    
		        };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, Table, lcms2_plugin.CMS_LERP_FLAGS_TRILINEAR);
		    
		    MaxErr = 0.0;
		    for (i=0; i < 0xffff; i++)
		    {
		    	In[0] = In[1] = In[2] = (short)i;
		    	
		        p.Interpolation.get16().run(In, Out, p);
		        
		        if (!IsGoodWord("Channel 1", Out[0], In[0]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodWord("Channel 2", Out[1], In[1]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		        if (!IsGoodWord("Channel 3", Out[2], In[2]))
		        {
		        	lcms2_internal._cmsFreeInterpParams(p);
				    return 0;
		        }
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn ExaustiveCheck3DinterpolationFloatTetrahedral = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int r, g, b;
		    float[] In = new float[3], Out = new float[3];
		    float[] FloatTable = { //R     G    B
		        0,    0,   0,     // B=0,G=0,R=0
		        0,    0,  .25f,   // B=1,G=0,R=0
		        
		        0,   .5f,    0,   // B=0,G=1,R=0
		        0,   .5f,  .25f,  // B=1,G=1,R=0
		        
		        1,    0,    0,    // B=0,G=0,R=1
		        1,    0,  .25f,   // B=1,G=0,R=1
		        
		        1,    .5f,   0,   // B=0,G=1,R=1
		        1,    .5f,  .25f  // B=1,G=1,R=1
		    };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, FloatTable, lcms2_plugin.CMS_LERP_FLAGS_FLOAT);
		    
		    MaxErr = 0.0;
		    for (r=0; r < 0xff; r++)
		    {
		    	for (g=0; g < 0xff; g++)
		    	{
		            for (b=0; b < 0xff; b++) 
			        {
			            In[0] = r / 255.0F;
			            In[1] = g / 255.0F;
			            In[2] = b / 255.0F;
			            
				        p.Interpolation.getFloat().run(In, Out, p);
				        
				        if (!IsGoodFixed15_16("Channel 1", Out[0], In[0]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodFixed15_16("Channel 2", Out[1], In[1] / 2.F))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodFixed15_16("Channel 3", Out[2], In[2] / 4.F))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
			        }
		    	}
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn ExaustiveCheck3DinterpolationFloatTrilinear = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int r, g, b;
		    float[] In = new float[3], Out = new float[3];
		    float[] FloatTable = { //R     G    B
		        0,    0,   0,     // B=0,G=0,R=0
		        0,    0,  .25f,   // B=1,G=0,R=0
		        
		        0,   .5f,    0,   // B=0,G=1,R=0
		        0,   .5f,  .25f,  // B=1,G=1,R=0
		        
		        1,    0,    0,    // B=0,G=0,R=1
		        1,    0,  .25f,   // B=1,G=0,R=1
		        
		        1,    .5f,   0,   // B=0,G=1,R=1
		        1,    .5f,  .25f  // B=1,G=1,R=1
		    };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, FloatTable, lcms2_plugin.CMS_LERP_FLAGS_FLOAT|lcms2_plugin.CMS_LERP_FLAGS_TRILINEAR);
		    
		    MaxErr = 0.0;
		    for (r=0; r < 0xff; r++)
		    {
		    	for (g=0; g < 0xff; g++)
		    	{
		            for (b=0; b < 0xff; b++) 
			        {
			            In[0] = r / 255.0F;
			            In[1] = g / 255.0F;
			            In[2] = b / 255.0F;
			            
				        p.Interpolation.getFloat().run(In, Out, p);
				        
				        if (!IsGoodFixed15_16("Channel 1", Out[0], In[0]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodFixed15_16("Channel 2", Out[1], In[1] / 2.F))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodFixed15_16("Channel 3", Out[2], In[2] / 4.F))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
			        }
		    	}
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn ExhaustiveCheck3DinterpolationTetrahedral16 = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int r, g, b;
		    short[] In = new short[3], Out = new short[3];
		    short[] Table = { 
		            0,    0,   0,     
		            0,    0,   (short)0xffff,    
		            
		            0,    (short)0xffff,    0,   
		            0,    (short)0xffff,    (short)0xffff,  
		            
		            (short)0xffff,    0,    0,    
		            (short)0xffff,    0,    (short)0xffff,   
		            
		            (short)0xffff,    (short)0xffff,   0,    
		            (short)0xffff,    (short)0xffff,   (short)0xffff    
		        };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, Table, lcms2_plugin.CMS_LERP_FLAGS_16BITS);
		    
		    MaxErr = 0.0;
		    for (r=0; r < 0xff; r++)
		    {
		        for (g=0; g < 0xff; g++)
		        {
		            for (b=0; b < 0xff; b++)
				    {
		            	In[0] = (short)r;
		                In[1] = (short)g;
		                In[2] = (short)b;
				    	
				        p.Interpolation.get16().run(In, Out, p);
				        
				        if (!IsGoodWord("Channel 1", Out[0], In[0]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodWord("Channel 2", Out[1], In[1]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodWord("Channel 3", Out[2], In[2]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				    }
		        }
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	private static final TestFn ExhaustiveCheck3DinterpolationTrilinear16 = new TestFn()
	{
		public int run()
		{
			cmsInterpParams p;
		    int r, g, b;
		    short[] In = new short[3], Out = new short[3];
		    short[] Table = { 
		            0,    0,   0,     
		            0,    0,   (short)0xffff,    
		            
		            0,    (short)0xffff,    0,   
		            0,    (short)0xffff,    (short)0xffff,  
		            
		            (short)0xffff,    0,    0,    
		            (short)0xffff,    0,    (short)0xffff,   
		            
		            (short)0xffff,    (short)0xffff,   0,    
		            (short)0xffff,    (short)0xffff,   (short)0xffff    
		        };
		    
		    p = lcms2_internal._cmsComputeInterpParams(DbgThread(), 2, 3, 3, Table, lcms2_plugin.CMS_LERP_FLAGS_TRILINEAR);
		    
		    MaxErr = 0.0;
		    for (r=0; r < 0xff; r++)
		    {
		        for (g=0; g < 0xff; g++)
		        {
		            for (b=0; b < 0xff; b++)
				    {
		            	In[0] = (short)r;
		                In[1] = (short)g;
		                In[2] = (short)b;
				    	
				        p.Interpolation.get16().run(In, Out, p);
				        
				        if (!IsGoodWord("Channel 1", Out[0], In[0]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodWord("Channel 2", Out[1], In[1]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				        if (!IsGoodWord("Channel 3", Out[2], In[2]))
				        {
				        	lcms2_internal._cmsFreeInterpParams(p);
						    return 0;
				        }
				    }
		        }
		    }
		    
		    if (MaxErr > 0)
		    {
		    	Utility.fprintf(print, "|Err|<%lf ", new Object[]{new Double(MaxErr)});
		    }
		    lcms2_internal._cmsFreeInterpParams(p);
		    return 1;
		}
	};
	
	// Check reverse interpolation on LUTS. This is right now exclusively used by K preservation algorithm
	private static final TestFn CheckReverseInterpolation3x3 = new TestFn()
	{
		public int run()
		{
			cmsPipeline Lut;
			cmsStage clut;
			float[] Target = new float[3], Result = new float[3], Hint = new float[3];
			float err, max;
			int i;
			short[] Table = new short[]{ 
			        0,    0,   0,										// 0 0 0  
			        0,    0,   (short)0xffff,							// 0 0 1  
			        
			        0,    (short)0xffff,    0,							// 0 1 0  
			        0,    (short)0xffff,    (short)0xffff,				// 0 1 1  
			        
			        (short)0xffff,    0,    0,							// 1 0 0  
			        (short)0xffff,    0,    (short)0xffff,				// 1 0 1  
			        
			        (short)0xffff,    (short)0xffff,   0,				// 1 1 0  
			        (short)0xffff,    (short)0xffff,   (short)0xffff,	// 1 1 1
			};
			
			Lut = lcms2.cmsPipelineAlloc(DbgThread(), 3, 3);
			
			clut = lcms2.cmsStageAllocCLut16bit(DbgThread(), 2, 3, 3, Table);
			lcms2.cmsPipelineInsertStage(Lut, lcms2.cmsAT_BEGIN, clut);
			
			Target[0] = 0; Target[1] = 0; Target[2] = 0;
			Hint[0] = 0; Hint[1] = 0; Hint[2] = 0;
			lcms2.cmsPipelineEvalReverseFloat(Target, Result, null, Lut);
			if (Result[0] != 0 || Result[1] != 0 || Result[2] != 0)
			{
				Fail("Reverse interpolation didn't find zero", null);
				return 0;
			}
			
			// Transverse identity
			max = 0;
			for (i=0; i <= 100; i++)
			{
				float in = i / 100.0F;
				
				Target[0] = in; Target[1] = 0; Target[2] = 0;
				lcms2.cmsPipelineEvalReverseFloat(Target, Result, Hint, Lut);
				
				err = Math.abs(in - Result[0]);
				if (err > max)
				{
					max = err;
				}
				
				System.arraycopy(Result, 0, Hint, 0, Hint.length);
			}
			
			lcms2.cmsPipelineFree(Lut);
			return (max <= FLOAT_PRECISSION) ? 1 : 0;
		}
	};
	
	private static final TestFn CheckReverseInterpolation4x3 = new TestFn()
	{
		public int run()
		{
			cmsPipeline Lut;
			cmsStage clut;
			float[] Target = new float[4], Result = new float[4], Hint = new float[4];
			float err, max;
			int i;
			
			// 4 -> 3, output gets 3 first channels copied
			short[] Table = new short[]{ 
			        0,         0,         0,							//  0 0 0 0   = ( 0, 0, 0)
			        0,         0,         0,							//  0 0 0 1   = ( 0, 0, 0)
			        
			        0,         0,         (short)0xffff,				//  0 0 1 0   = ( 0, 0, 1)
			        0,         0,         (short)0xffff,				//  0 0 1 1   = ( 0, 0, 1)
			        
			        0,         (short)0xffff,    0,						//  0 1 0 0   = ( 0, 1, 0)
			        0,         (short)0xffff,    0,						//  0 1 0 1   = ( 0, 1, 0)
			        
			        0,         (short)0xffff,    (short)0xffff,			//  0 1 1 0    = ( 0, 1, 1)
			        0,         (short)0xffff,    (short)0xffff,			//  0 1 1 1    = ( 0, 1, 1)
			        
			        (short)0xffff,    0,         0,						//  1 0 0 0    = ( 1, 0, 0)
			        (short)0xffff,    0,         0,						//  1 0 0 1    = ( 1, 0, 0)
			        
			        (short)0xffff,    0,         (short)0xffff,			//  1 0 1 0    = ( 1, 0, 1)
			        (short)0xffff,    0,         (short)0xffff,			//  1 0 1 1    = ( 1, 0, 1)
			        
			        (short)0xffff,    (short)0xffff,    0,				//  1 1 0 0    = ( 1, 1, 0)
			        (short)0xffff,    (short)0xffff,    0,				//  1 1 0 1    = ( 1, 1, 0)
			        
			        (short)0xffff,    (short)0xffff,    (short)0xffff,	//  1 1 1 0    = ( 1, 1, 1)
			        (short)0xffff,    (short)0xffff,    (short)0xffff,	//  1 1 1 1    = ( 1, 1, 1)
			};
			
			Lut = lcms2.cmsPipelineAlloc(DbgThread(), 4, 3);
			
			clut = lcms2.cmsStageAllocCLut16bit(DbgThread(), 2, 4, 3, Table);
			lcms2.cmsPipelineInsertStage(Lut, lcms2.cmsAT_BEGIN, clut);
			
			// Check if the LUT is behaving as expected
			SubTest("4->3 feasibility", null);
			for (i=0; i <= 100; i++)
			{
				Target[0] = i / 100.0F;
				Target[1] = Target[0];
				Target[2] = 0;
				Target[3] = 12;
				
				lcms2.cmsPipelineEvalFloat(Target, Result, Lut);
				
				if (!IsGoodFixed15_16("0", Target[0], Result[0]))
				{
					return 0;
				}
				if (!IsGoodFixed15_16("1", Target[1], Result[1]))
				{
					return 0;
				}
				if (!IsGoodFixed15_16("2", Target[2], Result[2]))
				{
					return 0;
				}
			}
			
			SubTest("4->3 zero", null);
			Target[0] = 0;
			Target[1] = 0;
			Target[2] = 0;
			
			// This one holds the fixed K
			Target[3] = 0;
			
			// This is our hint (which is a big lie in this case)
			Hint[0] = 0.1F; Hint[1] = 0.1F; Hint[2] = 0.1F;
			
			lcms2.cmsPipelineEvalReverseFloat(Target, Result, Hint, Lut);
			
			if (Result[0] != 0 || Result[1] != 0 || Result[2] != 0 || Result[3] != 0)
			{
				Fail("Reverse interpolation didn't find zero", null);
				return 0;
			}
			
			SubTest("4->3 find CMY", null);
			max = 0;
			for (i=0; i <= 100; i++)
			{
				float in = i / 100.0F;
				
				Target[0] = in; Target[1] = 0; Target[2] = 0;
				lcms2.cmsPipelineEvalReverseFloat(Target, Result, Hint, Lut);
				
				err = Math.abs(in - Result[0]);
				if (err > max)
				{
					max = err;
				}
				
				System.arraycopy(Result, 0, Hint, 0, Hint.length);
			}
			
			lcms2.cmsPipelineFree(Lut);
			return (max <= FLOAT_PRECISSION) ? 1 : 0;
		}
	};
	
	// Check all interpolation.
	
	private static short Fn8D1(short a1, short a2, short a3, short a4, short a5, short a6, short a7, short a8, int m)
	{
	    return (short)((((a1 & 0xFFFF) + (a2 & 0xFFFF) + (a3 & 0xFFFF) + (a4 & 0xFFFF) + (a5 & 0xFFFF) + (a6 & 0xFFFF) + (a7 & 0xFFFF) + (a8 & 0xFFFF)) & ((1L << 32) - 1)) / m);
	}
	
	private static short Fn8D2(short a1, short a2, short a3, short a4, short a5, short a6, short a7, short a8, int m)
	{
	    return (short)((((a1 & 0xFFFF) + 3 * (a2 & 0xFFFF) + 3* (a3 & 0xFFFF) + (a4 & 0xFFFF) + (a5 & 0xFFFF) + (a6 & 0xFFFF) + (a7 & 0xFFFF) + (a8 & 0xFFFF)) & ((1L << 32) - 1)) / ((m + 4) & ((1L << 32) - 1)));
	}
	
	private static short Fn8D3(short a1, short a2, short a3, short a4, short a5, short a6, short a7, short a8, int m)
	{
	    return (short)(((3 * (a1 & 0xFFFF) + 2 * (a2 & 0xFFFF) + 3 * (a3 & 0xFFFF) + (a4 & 0xFFFF) + (a5 & 0xFFFF) + (a6 & 0xFFFF) + (a7 & 0xFFFF) + (a8 & 0xFFFF)) & ((1L << 32) - 1)) / ((m + 5) & ((1L << 32) - 1)));
	}
	
	private static final short SHORT_ZERO = 0;
	
	private static final cmsSAMPLER16 Sampler3D = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			Out[0] = Fn8D1(In[0], In[1], In[2], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 3);
		    Out[1] = Fn8D2(In[0], In[1], In[2], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 3);
		    Out[2] = Fn8D3(In[0], In[1], In[2], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 3);
		    
		    return 1;
		}
	};
	
	private static final cmsSAMPLER16 Sampler4D = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			Out[0] = Fn8D1(In[0], In[1], In[2], In[3], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 4);
		    Out[1] = Fn8D2(In[0], In[1], In[2], In[3], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 4);
		    Out[2] = Fn8D3(In[0], In[1], In[2], In[3], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 4);
		    
		    return 1;
		}
	};
	
	private static final cmsSAMPLER16 Sampler5D = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			Out[0] = Fn8D1(In[0], In[1], In[2], In[3], In[4], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 5);
		    Out[1] = Fn8D2(In[0], In[1], In[2], In[3], In[4], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 5);
		    Out[2] = Fn8D3(In[0], In[1], In[2], In[3], In[4], SHORT_ZERO, SHORT_ZERO, SHORT_ZERO, 5);
		    
		    return 1;
		}
	};
	
	private static final cmsSAMPLER16 Sampler6D = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			Out[0] = Fn8D1(In[0], In[1], In[2], In[3], In[4], In[5], SHORT_ZERO, SHORT_ZERO, 6);
		    Out[1] = Fn8D2(In[0], In[1], In[2], In[3], In[4], In[5], SHORT_ZERO, SHORT_ZERO, 6);
		    Out[2] = Fn8D3(In[0], In[1], In[2], In[3], In[4], In[5], SHORT_ZERO, SHORT_ZERO, 6);
		    
		    return 1;
		}
	};
	
	private static final cmsSAMPLER16 Sampler7D = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			Out[0] = Fn8D1(In[0], In[1], In[2], In[3], In[4], In[5], In[6], SHORT_ZERO, 7);
		    Out[1] = Fn8D2(In[0], In[1], In[2], In[3], In[4], In[5], In[6], SHORT_ZERO, 7);
		    Out[2] = Fn8D3(In[0], In[1], In[2], In[3], In[4], In[5], In[6], SHORT_ZERO, 7);
		    
		    return 1;
		}
	};
	
	private static final cmsSAMPLER16 Sampler8D = new cmsSAMPLER16()
	{
		public int run(short[] In, short[] Out, Object Cargo)
		{
			Out[0] = Fn8D1(In[0], In[1], In[2], In[3], In[4], In[5], In[6], In[7], 8);
		    Out[1] = Fn8D2(In[0], In[1], In[2], In[3], In[4], In[5], In[6], In[7], 8);
		    Out[2] = Fn8D3(In[0], In[1]
