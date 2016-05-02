package mobi.neuerung.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mobi.neuerung.BGThread.Splash0Thread;
import mobi.neuerung.ImageScale.ImageUtil;
import mobi.neuerung.MADP.Home;
import mobi.neuerung.MADP.MyUIApplication;
import mobi.neuerung.MADP.R;
import mobi.neuerung.Parser.AdvertisementParser;
import mobi.neuerung.Wrapper.ElementWrapper;
import mobi.neuerung.Wrapper.ScreenPartWrapper;

import org.apache.http.util.ByteArrayBuffer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ViewFlipper;



public class UtilAdvertisementSwitcher {
	//	static ImageView adv;
	static ViewFlipper flipper;
	static int heightofview=0;
	int switcherTimer=4000;
	ElementWrapper _objEW;
	// *** Set AdvertisementBar ****
	public void UniversalAdvertisementSwitcher(final ScreenPartWrapper _objScreenPartWrapper, String Section , final Context context)
	{
		_objEW = ((Home)context)._objUtil.getElementWrapperFromDB(_objScreenPartWrapper.ScreenName,Section);


		try {
			if(_objEW.switcherTimer.trim().length()>0)
			{
				switcherTimer = Integer.parseInt(_objEW.switcherTimer.trim());
			}
		} catch (Exception e) {
			// TODO: handle exception
			switcherTimer=4000;
		}

		AdvertisementSwitcherParser(context);


		String url=MyUIApplication.EMGUrl+"GetAdvertisements.aspx?ClientCode="+MyUIApplication.ClientCode+"&EventCode="+MyUIApplication.EventCode+"&Page=Inner%20Pages&Resolution=2";

		if(MyUIApplication.isInternetAvailable(context))
		{
			new Splash0Thread(context, "DownLoadAdvertisementXmlInSdCard", url).execute();
		}
		else
		{
			CallParser(context);
		}



		if(MyUIApplication.Copy)
		{
			if(Section.equals("Top"))
			{
				double widthLLTopCopy=Double.parseDouble(_objScreenPartWrapper.TopWidth);
				double heightLLTopCopy=Double.parseDouble(_objScreenPartWrapper.TopHeight);
				double widthLLTopCopyPer=widthLLTopCopy/100;
				double heightLLTopCopyPer=heightLLTopCopy/100;

				heightofview = (int)(heightLLTopCopyPer*((Home)context).deviceHeight);

				ViewGroup.LayoutParams params_LLMiddleCopy = ((Home)context).LLTopCopy.getLayoutParams();
				params_LLMiddleCopy.height=(int)(heightLLTopCopyPer*((Home)context).deviceHeight);
				params_LLMiddleCopy.width=(int)(widthLLTopCopyPer*((Home)context).deviceWidth);
				((Home)context).LLTopCopy.setLayoutParams(params_LLMiddleCopy);

				if(_objScreenPartWrapper.TopbgImage.length()>0)
				{
					if(MyUIApplication.IsSdCardImageAvailable(_objScreenPartWrapper.TopbgImage))
					{
						String topbarfilePath=_objScreenPartWrapper.TopbgImage;
						ImageUtil.setBgFromSDCardNotCache(((Home)context).LLTopCopy, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
					}
					else 
					{
						String topbarfilePath="images/"+_objScreenPartWrapper.TopbgImage;
						ImageUtil.setBackgroundBgFromAssetsNotCache(((Home)context).LLTopCopy, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
					}
				}
				else if(_objScreenPartWrapper.Topbgcolor.length()>0)
				{
					((Home)context).LLTopCopy.setBackgroundColor(Color.parseColor(_objScreenPartWrapper.Topbgcolor));
				}

				try
				{

					//					adv= new ImageView(context);
					flipper= new ViewFlipper(context);
					((Home)context).LLTopCopy.addView(flipper);



					//					ViewGroup.LayoutParams params_adv = adv.getLayoutParams();
					//					params_adv = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, 
					//							FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
					//					params_adv.height=(int)(heightadv);
					//					params_adv.width=new ImageScaling().ImageScale(heightadv, ImagePathinSDCard);
					//					adv.setLayoutParams(params_adv);

					if(_objEW.Bitmap.length()>0)
					{
						if(MyUIApplication.IsSdCardImageAvailable(_objEW.Bitmap))
						{
							String topbarfilePath=_objEW.Bitmap;
							ImageUtil.setBgFromSDCardNotCache(flipper, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
						}
						else 
						{
							String topbarfilePath="images/"+_objEW.Bitmap;
							ImageUtil.setBackgroundBgFromAssetsNotCache(flipper, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
						}
					}

				}
				catch (Exception e) 
				{
					System.out.println("Exception in Setting home Page Top Bar"+e);
				}
			}
			else if(Section.equals("Middle"))
			{
				double widthLLTopCopy=Double.parseDouble(_objScreenPartWrapper.MidWidth);
				double heightLLTopCopy=Double.parseDouble(_objScreenPartWrapper.MidHeight);
				double widthLLTopCopyPer=widthLLTopCopy/100;
				double heightLLTopCopyPer=heightLLTopCopy/100;

				heightofview = (int)(heightLLTopCopyPer*((Home)context).deviceHeight);

				ViewGroup.LayoutParams params_LLMiddleCopy = ((Home)context).LLMiddleCopy.getLayoutParams();
				params_LLMiddleCopy.height=(int)(heightLLTopCopyPer*((Home)context).deviceHeight);
				params_LLMiddleCopy.width=(int)(widthLLTopCopyPer*((Home)context).deviceWidth);
				((Home)context).LLMiddleCopy.setLayoutParams(params_LLMiddleCopy);

				if(_objScreenPartWrapper.MidbgImage.length()>0)
				{
					String topbarfilePath="images/"+_objScreenPartWrapper.MidbgImage;
					//				Drawable d = Drawable.createFromStream(context.getAssets().open(topbarfilePath), null);
					//				((Home)context).LLTopCopy.setBackgroundDrawable(d);
					ImageUtil.setBackgroundBgFromAssetsNotCache(((Home)context).LLMiddleCopy, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
				}
				else if(_objScreenPartWrapper.Midbgcolor.length()>0)
				{
					((Home)context).LLMiddleCopy.setBackgroundColor(Color.parseColor(_objScreenPartWrapper.Midbgcolor));
				}

				try
				{
					//					adv= new ImageView(context);
					flipper= new ViewFlipper(context);
					((Home)context).LLMiddleCopy.addView(flipper);

					if(_objEW.Bitmap.length()>0)
					{
						if(MyUIApplication.IsSdCardImageAvailable(_objEW.Bitmap))
						{
							String topbarfilePath=_objEW.Bitmap;
							ImageUtil.setBgFromSDCardNotCache(flipper, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
						}
						else 
						{
							String topbarfilePath="images/"+_objEW.Bitmap;
							ImageUtil.setBackgroundBgFromAssetsNotCache(flipper, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
						}
					}
				}
				catch (Exception e) 
				{
					System.out.println("Exception in Setting home Page Top Bar"+e);
				}
			}
			else if(Section.equals("Bottom"))
			{
				double widthLLTopCopy=Double.parseDouble(_objScreenPartWrapper.BottomWidth);
				double heightLLTopCopy=Double.parseDouble(_objScreenPartWrapper.BottomHeight);
				double widthLLTopCopyPer=widthLLTopCopy/100;
				double heightLLTopCopyPer=heightLLTopCopy/100;

				heightofview = (int)(heightLLTopCopyPer*((Home)context).deviceHeight);

				ViewGroup.LayoutParams params_LLMiddleCopy = ((Home)context).LLBottomCopy.getLayoutParams();
				params_LLMiddleCopy.height=(int)(heightLLTopCopyPer*((Home)context).deviceHeight);
				params_LLMiddleCopy.width=(int)(widthLLTopCopyPer*((Home)context).deviceWidth);
				((Home)context).LLBottomCopy.setLayoutParams(params_LLMiddleCopy);

				if(_objScreenPartWrapper.BottombgImage.length()>0)
				{
					String topbarfilePath="images/"+_objScreenPartWrapper.BottombgImage;
					//				Drawable d = Drawable.createFromStream(context.getAssets().open(topbarfilePath), null);
					//				((Home)context).LLTopCopy.setBackgroundDrawable(d);
					ImageUtil.setBackgroundBgFromAssetsNotCache(((Home)context).LLBottomCopy, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
				}
				else if(_objScreenPartWrapper.Bottombgcolor.length()>0)
				{
					((Home)context).LLBottomCopy.setBackgroundColor(Color.parseColor(_objScreenPartWrapper.Bottombgcolor));
				}

				try
				{
					//					adv= new ImageView(context);
					flipper= new ViewFlipper(context);
					((Home)context).LLBottomCopy.addView(flipper);

					if(_objEW.Bitmap.length()>0)
					{
						if(MyUIApplication.IsSdCardImageAvailable(_objEW.Bitmap))
						{
							String topbarfilePath=_objEW.Bitmap;
							ImageUtil.setBgFromSDCardNotCache(flipper, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
						}
						else 
						{
							String topbarfilePath="images/"+_objEW.Bitmap;
							ImageUtil.setBackgroundBgFromAssetsNotCache(flipper, context,topbarfilePath,(int)(heightLLTopCopyPer*((Home)context).deviceHeight),(int)(widthLLTopCopyPer*((Home)context).deviceWidth));
						}
					}					
				}
				catch (Exception e) 
				{
					System.out.println("Exception in Setting home Page Top Bar"+e);
				}
			}


			if(_objScreenPartWrapper.TopLink.length()>0)
			{
				((Home)context).LLTopCopy.setOnClickListener( new OnClickListener()
				{
					public void onClick(View v)
					{
						String str=_objScreenPartWrapper.TopLink.trim();

						if(_objEW.OnClick.length()>0)
						{
							if(((Home)context).LLLayout.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}
							if(((Home)context).LLLayoutCopy.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}

							String screenNumber=_objEW.OnClick.substring(12);

							((Home)context).OpenHtmlPage(screenNumber,_objScreenPartWrapper.TopLink);
							MyUIApplication.StateMachine.add(screenNumber);

							int i=MyUIApplication.StateMachine.size();
							System.out.println("State Machine Size >>>"+i);
						}
						else
						{
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(str));
							context.startActivity(myWebLink);
						}
					}
				});
			}
			if(_objScreenPartWrapper.MidLink.length()>0)
			{
				((Home)context).LLMiddleCopy.setOnClickListener( new OnClickListener()
				{
					public void onClick(View v)
					{
						String str=_objScreenPartWrapper.MidLink.trim();

						if(_objEW.OnClick.length()>0)
						{
							if(((Home)context).LLLayout.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}
							if(((Home)context).LLLayoutCopy.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}

							String screenNumber=_objEW.OnClick.substring(12);

							((Home)context).OpenHtmlPage(screenNumber,_objScreenPartWrapper.MidLink);
							MyUIApplication.StateMachine.add(screenNumber);

							int i=MyUIApplication.StateMachine.size();
							System.out.println("State Machine Size >>>"+i);
						}
						else
						{
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(str));
							context.startActivity(myWebLink);
						}
					}
				});
			}
			if(_objScreenPartWrapper.BottomLink.length()>0)
			{
				((Home)context).LLBottomCopy.setOnClickListener( new OnClickListener()
				{
					public void onClick(View v)
					{
						String str=_objScreenPartWrapper.BottomLink.trim();

						if(_objEW.OnClick.length()>0)
						{
							if(((Home)context).LLLayout.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}
							if(((Home)context).LLLayoutCopy.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}

							String screenNumber=_objEW.OnClick.substring(12);

							((Home)context).OpenHtmlPage(screenNumber,_objScreenPartWrapper.BottomLink);
							MyUIApplication.StateMachine.add(screenNumber);

							int i=MyUIApplication.StateMachine.size();
							System.out.println("State Machine Size >>>"+i);
						}
						else
						{
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(str));
							context.startActivity(myWebLink);
						}
					}
				});
			}

		}
		else if(!MyUIApplication.Copy)
		{


			if(Section.equals("Top"))
			{
				double widthLLTop=Double.parseDouble(_objScreenPartWrapper.TopWidth);
				double heightLLTop=Double.parseDouble(_objScreenPartWrapper.TopHeight);
				double widthLLTopPer=widthLLTop/100;
				double heightLLTopPer=heightLLTop/100;

				heightofview = (int)(heightLLTopPer*((Home)context).deviceHeight);

				ViewGroup.LayoutParams params_LLMiddle = ((Home)context).LLTop.getLayoutParams();
				params_LLMiddle.height=(int)(heightLLTopPer*((Home)context).deviceHeight);
				params_LLMiddle.width=(int)(widthLLTopPer*((Home)context).deviceWidth);
				((Home)context).LLTop.setLayoutParams(params_LLMiddle);

				if(_objScreenPartWrapper.TopbgImage.length()>0)
				{
					String topbarfilePath="images/"+_objScreenPartWrapper.TopbgImage;
					//				Drawable d = Drawable.createFromStream(context.getAssets().open(topbarfilePath), null);
					//				((Home)context).LLTopCopy.setBackgroundDrawable(d);
					ImageUtil.setBackgroundBgFromAssetsNotCache(((Home)context).LLTop, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
				}
				else if(_objScreenPartWrapper.Topbgcolor.length()>0)
				{
					((Home)context).LLTop.setBackgroundColor(Color.parseColor(_objScreenPartWrapper.Topbgcolor));
				}


				try
				{

					//					adv= new ImageView(context);
					flipper= new ViewFlipper(context);
					((Home)context).LLTop.addView(flipper);


					if(_objEW.Bitmap.length()>0)
					{
						if(MyUIApplication.IsSdCardImageAvailable(_objEW.Bitmap))
						{
							String topbarfilePath=_objEW.Bitmap;
							ImageUtil.setBgFromSDCardNotCache(flipper, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
						}
						else 
						{
							String topbarfilePath="images/"+_objEW.Bitmap;
							ImageUtil.setBackgroundBgFromAssetsNotCache(flipper, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
						}
					}
				}
				catch (Exception e) 
				{
					System.out.println("Exception in Setting home Page Top Bar"+e);
				}
			}
			else if(Section.equals("Middle"))
			{
				double widthLLTop=Double.parseDouble(_objScreenPartWrapper.MidWidth);
				double heightLLTop=Double.parseDouble(_objScreenPartWrapper.MidHeight);
				double widthLLTopPer=widthLLTop/100;
				double heightLLTopPer=heightLLTop/100;

				heightofview = (int)(heightLLTopPer*((Home)context).deviceHeight);

				ViewGroup.LayoutParams params_LLMiddle = ((Home)context).LLMiddle.getLayoutParams();
				params_LLMiddle.height=(int)(heightLLTopPer*((Home)context).deviceHeight);
				params_LLMiddle.width=(int)(widthLLTopPer*((Home)context).deviceWidth);
				((Home)context).LLMiddle.setLayoutParams(params_LLMiddle);

				if(_objScreenPartWrapper.MidbgImage.length()>0)
				{
					String topbarfilePath="images/"+_objScreenPartWrapper.MidbgImage;
					//				Drawable d = Drawable.createFromStream(context.getAssets().open(topbarfilePath), null);
					//				((Home)context).LLTopCopy.setBackgroundDrawable(d);
					ImageUtil.setBackgroundBgFromAssetsNotCache(((Home)context).LLMiddle, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
				}
				else if(_objScreenPartWrapper.Midbgcolor.length()>0)
				{
					((Home)context).LLMiddle.setBackgroundColor(MyUIApplication.GetColor(_objScreenPartWrapper.Midbgcolor));
				}

				try
				{

					//					adv= new ImageView(context);
					flipper= new ViewFlipper(context);
					((Home)context).LLMiddle.addView(flipper);

					if(_objEW.Bitmap.length()>0)
					{
						if(MyUIApplication.IsSdCardImageAvailable(_objEW.Bitmap))
						{
							String topbarfilePath=_objEW.Bitmap;
							ImageUtil.setBgFromSDCardNotCache(flipper, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
						}
						else 
						{
							String topbarfilePath="images/"+_objEW.Bitmap;
							ImageUtil.setBackgroundBgFromAssetsNotCache(flipper, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
						}
					}

				}
				catch (Exception e) 
				{
					System.out.println("Exception in Setting home Page Top Bar"+e);
				}
			}
			else if(Section.equals("Bottom"))
			{
				double widthLLTop=Double.parseDouble(_objScreenPartWrapper.BottomWidth);
				double heightLLTop=Double.parseDouble(_objScreenPartWrapper.BottomHeight);
				double widthLLTopPer=widthLLTop/100;
				double heightLLTopPer=heightLLTop/100;

				heightofview = (int)(heightLLTopPer*((Home)context).deviceHeight);

				ViewGroup.LayoutParams params_LLMiddle = ((Home)context).LLBottom.getLayoutParams();
				params_LLMiddle.height=(int)(heightLLTopPer*((Home)context).deviceHeight);
				params_LLMiddle.width=(int)(widthLLTopPer*((Home)context).deviceWidth);
				((Home)context).LLBottom.setLayoutParams(params_LLMiddle);

				if(_objScreenPartWrapper.Bottombgcolor.length()>0)
				{
					String topbarfilePath="images/"+_objScreenPartWrapper.BottombgImage;
					//				Drawable d = Drawable.createFromStream(context.getAssets().open(topbarfilePath), null);
					//				((Home)context).LLTopCopy.setBackgroundDrawable(d);
					ImageUtil.setBackgroundBgFromAssetsNotCache(((Home)context).LLBottom, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
				}
				else if(_objScreenPartWrapper.Bottombgcolor.length()>0)
				{
					((Home)context).LLBottom.setBackgroundColor(Color.parseColor(_objScreenPartWrapper.Bottombgcolor));
				}


				try
				{

					//					adv= new ImageView(context);
					flipper= new ViewFlipper(context);
					((Home)context).LLBottom.addView(flipper);


					if(_objEW.Bitmap.length()>0)
					{
						if(MyUIApplication.IsSdCardImageAvailable(_objEW.Bitmap))
						{
							String topbarfilePath=_objEW.Bitmap;
							ImageUtil.setBgFromSDCardNotCache(flipper, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
						}
						else 
						{
							String topbarfilePath="images/"+_objEW.Bitmap;
							ImageUtil.setBackgroundBgFromAssetsNotCache(flipper, context,topbarfilePath,(int)(heightLLTopPer*((Home)context).deviceHeight),(int)(widthLLTopPer*((Home)context).deviceWidth));
						}
					}

				}
				catch (Exception e) 
				{
					System.out.println("Exception in Setting home Page Top Bar"+e);
				}
			}


			if(_objScreenPartWrapper.TopLink.length()>0)
			{
				((Home)context).LLTop.setOnClickListener( new OnClickListener()
				{
					public void onClick(View v)
					{
						String str=_objScreenPartWrapper.TopLink.trim();
						if(_objEW.OnClick.length()>0)
						{
							if(((Home)context).LLLayout.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}
							if(((Home)context).LLLayoutCopy.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}

							String screenNumber=_objEW.OnClick.substring(12);

							((Home)context).OpenHtmlPage(screenNumber,_objScreenPartWrapper.TopLink);
							MyUIApplication.StateMachine.add(screenNumber);

							int i=MyUIApplication.StateMachine.size();
							System.out.println("State Machine Size >>>"+i);
						}
						else
						{
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(str));
							context.startActivity(myWebLink);
						}
					}
				});
			}
			if(_objScreenPartWrapper.MidLink.length()>0)
			{
				((Home)context).LLMiddle.setOnClickListener( new OnClickListener()
				{
					public void onClick(View v)
					{
						String str=_objScreenPartWrapper.MidLink.trim();
						if(_objEW.OnClick.length()>0)
						{
							if(((Home)context).LLLayout.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}
							if(((Home)context).LLLayoutCopy.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}

							String screenNumber=_objEW.OnClick.substring(12);

							((Home)context).OpenHtmlPage(screenNumber,_objScreenPartWrapper.MidLink);
							MyUIApplication.StateMachine.add(screenNumber);

							int i=MyUIApplication.StateMachine.size();
							System.out.println("State Machine Size >>>"+i);
						}
						else
						{
							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(str));
							context.startActivity(myWebLink);
						}
					}
				});
			}
			if(_objScreenPartWrapper.BottomLink.length()>0)
			{
				((Home)context).LLBottom.setOnClickListener( new OnClickListener()
				{
					public void onClick(View v)
					{
						String str=_objScreenPartWrapper.BottomLink.trim();
						if(_objEW.OnClick.length()>0)
						{
							if(((Home)context).LLLayout.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}
							if(((Home)context).LLLayoutCopy.getVisibility()==View.VISIBLE)
							{
								((Home)context).LLLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_left));
								((Home)context).LLLayoutCopy.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));
							}

							String screenNumber=_objEW.OnClick.substring(12);

							((Home)context).OpenHtmlPage(screenNumber,_objScreenPartWrapper.BottomLink);
							MyUIApplication.StateMachine.add(screenNumber);

							int i=MyUIApplication.StateMachine.size();
							System.out.println("State Machine Size >>>"+i);
						}
						else
						{

							Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
							myWebLink.setData(Uri.parse(str));
							context.startActivity(myWebLink);
						}
					}
				});
			}


		}
		try {
			setImageInAdver(context);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		try {
			String XmlPathofAdver = "/"+MyUIApplication.mainDirectoryName+"/"+ MyUIApplication.ClientCode+"/"+MyUIApplication.EventCode+"/xmls"+"/"+"AdvertisementSwitcher.xml";
			File f = new File(Environment.getExternalStorageDirectory() +XmlPathofAdver);
			boolean b = f.exists();

			if(b)
			{
				flipper.setBackgroundDrawable(null);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}


	}
	public String imagesName = "";
	public String imagesUrls = "";

	public void DownloadFromUrl(String DownloadUrl, String fileName) {

		try {

			File dir = MyUIApplication.SDCardPath("/"+MyUIApplication.mainDirectoryName+"/"+ MyUIApplication.ClientCode+"/"+MyUIApplication.EventCode+"/xmls");

			URL url = new URL(DownloadUrl); //you can write here any link
			File file = new File(dir, fileName);

			long startTime = System.currentTimeMillis();
			Log.d("DownloadManager", "download begining");
			Log.d("DownloadManager", "download url:" + url);
			Log.d("DownloadManager", "downloaded file name:" + fileName);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}


			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.flush();
			fos.close();
			Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

			// Activity For Splash0 To Splash.java


		} catch (IOException e) {
			Log.d("DownloadManager", "Error: " + e);
		}
	}

	public void CallParser(Context context )
	{
		//		new BadgeBGThread(context, "AdvertisementSwitcher", "").execute();

		AdvertisementSwitcherParser(context);
	}

	public void AdvertisementSwitcherParser(Context context)
	{
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			AdvertisementParser myXMLHandler = new AdvertisementParser(context,"AutoSwitcher");   
			xr.setContentHandler(myXMLHandler);

			String XmlPathofAdver = "/"+MyUIApplication.mainDirectoryName+"/"+ MyUIApplication.ClientCode+"/"+MyUIApplication.EventCode+"/xmls"+"/"+"AdvertisementSwitcher.xml";

			File f = new File(Environment.getExternalStorageDirectory() +XmlPathofAdver);
			boolean b = f.exists();

			if(b)
			{
				URL sourceUrl = new URL("file://" + Environment.getExternalStorageDirectory() +XmlPathofAdver);
				xr.parse(new InputSource(sourceUrl.openStream()));
			}
			else
			{
			}
		} 
		catch (Exception e) 
		{
			System.out.println("XML Pasing Excpetion in Advertisement Switcher Parser= " + e);
		}
	}
	//	public Handler handler = new Handler();
	//	public Runnable runnable;
	public void setImageInAdver(final Context context)
	{
		System.out.println(imagesName+imagesUrls);
		final String []images=imagesName.split(",");

		for (int i = 0; i < images.length; i++) {

			ImageView im = new ImageView(context);
			im.setAdjustViewBounds(true);
			if(_objEW.ImageMode != null && _objEW.ImageMode.trim().equalsIgnoreCase("crop"))
			{
				im.setScaleType(ScaleType.FIT_XY);
			}
			flipper.addView(im);
			im.getLayoutParams().height = LayoutParams.FILL_PARENT;
			im.getLayoutParams().width = LayoutParams.FILL_PARENT;
			//			String strName = "/EventsMobilePlatform/"+MyUIApplication.ClientCode+"/"+MyUIApplication.EventCode+"/"+"UNZIP/ImagePacket/"+images[i];
			//			String imageInSD = Environment.getExternalStorageDirectory().getAbsolutePath() +strName;
			//			Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
			//			if(bitmap !=null)
			//			im.setImageBitmap(bitmap);
			Bitmap bmp=	ImageUtil.setBgFromSDCardNotCache(im, context,images[i],heightofview,((Home)context).deviceWidth);

			if(bmp != null)
			{
				//			float increasingfact = 1.15f;
				//			
				//			int width = (int)(increasingfact* bmp.getWidth());
				//			int height = (int)(increasingfact* bmp.getHeight());
				//			
				//			Bitmap bmp1 = Bitmap.createScaledBitmap(bmp, width,height,false);
				im.setImageBitmap(bmp);
				im.setBackgroundDrawable(null);
			}

		}

		flipper.setAutoStart(true);
		flipper.setFlipInterval(switcherTimer);
		flipper.startFlipping();

		//		if(images.length>0)
		//		{
		//
		//			//							final String[] aa =_objEW.Bitmap.split(",");
		//			final String[] aa =images;
		//
		//			_objCountDownTimer = new  CountDownTimer(2000, 500) {
		//
		//				public void onTick(long millisUntilFinished)
		//				{
		//					String strName = "/EventsMobilePlatform/"+MyUIApplication.ClientCode+"/"+MyUIApplication.EventCode+"/"+"UNZIP/ImagePacket/"+images[i];
		//					
		//					String imageInSD = Environment.getExternalStorageDirectory().getAbsolutePath() +strName;
		//					Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
		//
		////					float bitWgt =bitmap.getWidth();
		////					float bitHgt =bitmap.getHeight();
		//
		////					int newWidth = (int) ((heightofview*bitWgt)/bitHgt);
		//					
		////					ImageUtil.setBgFromSDCardNotCache(adv, context,imageArray[i],heightofview,newWidth);
		//					ImageUtil.setBgFromSDCardNotCache(adv, context,images[i],heightofview,((Home)context).deviceWidth);
		//					
		//					
		//					FrameLayout.LayoutParams adv_param = (FrameLayout.LayoutParams)adv.getLayoutParams();
		//					adv_param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL);
		//					adv_param.height = heightofview;
		////					adv_param.width = newWidth;
		//					adv.setLayoutParams(adv_param);
		//				}
		//				public void onFinish() {
		//					if(i<aa.length-1)
		//					{
		//						i++;
		//					}
		//					else
		//					{
		//						i=0;
		//					}
		//					_objCountDownTimer.start();
		//				}
		//			}.start();
		//
		//			
		//		}


		//
		//		
		//		runnable = new Runnable() {
		//			int i=0;
		//			public void run() {
		//				
		//				try {
		//					String strName = "/EventsMobilePlatform/"+MyUIApplication.ClientCode+"/"+MyUIApplication.EventCode+"/"+"UNZIP/ImagePacket/"+imageArray[i];
		//					
		//					String imageInSD = Environment.getExternalStorageDirectory().getAbsolutePath() +strName;
		//					Bitmap bitmap = BitmapFactory.decodeFile(imageInSD);
		//
		////					float bitWgt =bitmap.getWidth();
		////					float bitHgt =bitmap.getHeight();
		//
		////					int newWidth = (int) ((heightofview*bitWgt)/bitHgt);
		//					
		////					ImageUtil.setBgFromSDCardNotCache(adv, context,imageArray[i],heightofview,newWidth);
		//					ImageUtil.setBgFromSDCardNotCache(adv, context,imageArray[i],heightofview,((Home)context).deviceWidth);
		//					
		//					
		//					FrameLayout.LayoutParams adv_param = (FrameLayout.LayoutParams)adv.getLayoutParams();
		//					adv_param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL);
		//					adv_param.height = heightofview;
		////					adv_param.width = newWidth;
		//					adv.setLayoutParams(adv_param);
		//					
		//					i++;
		//					if(i>imageArray.length-1)
		//					{
		//						i=0;    
		//					}
		//					handler.postDelayed(this, 2000);  //for interval...
		//				} catch (Exception e) {
		//					// TODO: handle exception
		//				}
		//				
		//			}
		//		};
		//		
		//		handler.postDelayed(runnable, 2000); //for initial delay..

	}

	//	CountDownTimer _objCountDownTimer;
	//	int i=0;
}

