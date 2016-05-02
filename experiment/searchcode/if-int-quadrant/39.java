package com.suijten.bordermaker;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.jpeg.JpegSegmentReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import com.jhlabs.image.BoxBlurFilter;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.ShadowFilter;
import com.mortennobel.imagescaling.MultiStepRescaleOp;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import com.suijten.bordermaker.BorderMakerBean.OutputFormat;
import com.suijten.bordermaker.BorderMakerBean.Overwrite;
import com.suijten.bordermaker.BorderMakerBean.ScaleMethode;
import com.suijten.bordermaker.BorderMakerBean.ScaleQuality;
import com.suijten.bordermaker.BorderMakerBorderBean.BlendMode;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class BorderMakerProcessor implements Runnable, ClipboardOwner {
	private static final JComponent MEDIA_TRACKER_COMPONENT = new JLabel();
	private BorderMakerListener l = null;
	private BorderMakerBean b = null;
	private OverwriteHandler overwriteHandler = new OverwriteHandler(this);

	private Format dateFormat;
	private boolean paused = false;
	private boolean stopped = false;
	private boolean askForTitle = true;
//	private int totalFiles;
//	private float progress;
	private String title = "";
	private ArrayList<DestinationHandler> destinationHandlers;
	private ExecutorService exec;
	
	public void stopProcessing() {
		stopped = true;
		exec.shutdownNow();
		for (DestinationHandler destinationHandler : destinationHandlers) {
			if(destinationHandler.isEnabled()) {
				destinationHandler.interrupt();
			}
		}
	}
	
	public BorderMakerProcessor(BorderMakerBean b, BorderMakerListener l) {
		if(l == null) {
			l = new BorderMakerListener() {
				@Override
				public void uploadedSubmitted(Upload upload) {
				}
				
				@Override
				public void uploaded(Upload upload, long length) {
				}
				
				@Override
				public void uploadStopped(Upload upload) {
				}
				
				@Override
				public void uploadStarted(Upload upload) {
				}
				
				@Override
				public void stopProcessing() {
				}
				
				@Override
				public void startProcessing(int count) {
				}
				
				@Override
				public void progress() {
				}
				
				@Override
				public void fileProcessing(File source, String destination, int count, ImageIcon thumbnail) {
				}
				
				@Override
				public void fileProcessed(File source, String destination, long processingTime, int count) {
				}
				
				@Override
				public void awaitingUploads() {
				}
			};
		}
		
		this.l = l;
		this.b = b;
		destinationHandlers = new ArrayList<DestinationHandler>();
		destinationHandlers.add(new FTPDestinationHandler(b, l, overwriteHandler));
		
		dateFormat = BorderMaker.getDateFormat(b.getDefaultDateFormat(), b.getLocale());
	}
	
	private void checkThread() throws Exception {
		if(stopped) {
			throw new InterruptedException();
		}
		
		while(paused) {
			if(stopped) {
				throw new InterruptedException();
			}
			Thread.sleep(200);
		}
	}
	
	public static Metadata readMetadata(File file) throws JpegProcessingException {
		try {
			JpegSegmentReader segmentReader = new JpegSegmentReader(file);
			byte[] exifSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APP1);
			byte[] iptcSegment = segmentReader.readSegment(JpegSegmentReader.SEGMENT_APPD);
			Metadata metadata = new Metadata();
			new ExifReader(exifSegment).extract(metadata);
			new IptcReader(iptcSegment).extract(metadata);
			return metadata;
		} catch (Exception e) {
			return null;
		}
	}
	
	public void run() {
//		progress = 0;
		List<File> tempFiles = new ArrayList<File>();
		try {
			exec = Executors.newSingleThreadExecutor();
			List<File> sourceFiles = new Vector<File>();
			getSourceFiles(sourceFiles, b);
			
			//Sort by date
			Collections.sort(sourceFiles, new Comparator<File>() {
				public int compare(File o1, File o2) {
					return new Long(o1.lastModified()).compareTo(o2.lastModified());
				}
			}); 
			
//			totalFiles = sourceFiles.size();
			if(l != null) {
				l.startProcessing(sourceFiles.size());
			}

			for (DestinationHandler destinationHandler : destinationHandlers) {
				if(destinationHandler.isEnabled()) {
					destinationHandler.begin();
				}
			}
			
			for (int i = 0; i < sourceFiles.size(); i++) {
				int counter = i;
				checkThread();
				
				ImageIcon thumbnail = null;
	
//				if(l != null) {
//					l.subProgress(0);
//				}
				
				File sourceFile = (File) sourceFiles.get(i);
				
				Metadata metadata = BorderMakerProcessor.readMetadata(sourceFile);
				ExifDirectory exifDirectory = metadata == null ? null : (ExifDirectory) metadata.getDirectory(ExifDirectory.class);
				
				File destinationFile = null;
				boolean doProcessing = false;
				String destinationFilename = BorderMaker.processString(b.getFileName(), sourceFile.getName(), metadata, dateFormat, null, counter, sourceFiles.size(), b);
				
				final Destination destination = getDestination(sourceFile, destinationFilename, b);
				
				if(b.isDestinationFolderReallyEnabled()) {
					destinationFile = getDestinationFile(destination, b.getDestinationDirectory());
		
					if(destinationFile.exists()) {
						if(b.getDestinationFolderOverwrite() == Overwrite.ASK) {
							doProcessing = overwriteHandler.overwrite(ImageFactory.BROWSE, BorderMaker.getMessage("folder"), destinationFile.getAbsolutePath(), "file");
						} else {
							doProcessing = b.getDestinationFolderOverwrite() == Overwrite.ALWAYS;
						}
						
						checkThread();
					} else {
						doProcessing = true;
					}
				}
				
				List<DestinationHandler> enabledDestinationHandlers = new ArrayList<DestinationHandler>(destinationHandlers.size());
				for (DestinationHandler destinationHandler : destinationHandlers) {
					if(destinationHandler.isHandlerEnabledFor(destination)) {
						enabledDestinationHandlers.add(destinationHandler);
						doProcessing = true;
					}
				}
				
				if(doProcessing) {
					if(l != null) {
						ImageIcon image = null;
						try {
							image = BorderMaker.getThumbnail(sourceFile, 200, exifDirectory, b);
						} catch (Exception e) {
						}
						l.fileProcessing(sourceFile, destinationFile == null ? destination.toString() : SemanticaUtil.abbreviateFileName(destinationFile.getAbsolutePath(), 100), i, image);
					}
		
					BufferedImage image = processFile(metadata, sourceFile, counter, sourceFiles.size(), false);
					if(image != null) {
						// Save the file.
						if(destinationFile == null) {
							destinationFile = File.createTempFile("bordermaker", ".tmp");
							tempFiles.add(destinationFile);
						} else if(!destinationFile.getParentFile().exists()) {
							destinationFile.getParentFile().mkdirs();
						}
						
						saveAsImage(null, sourceFile, destinationFile, image, b);
						image.flush();
						image = null;
						
						for (final DestinationHandler destinationHandler : enabledDestinationHandlers) {
							final Upload upload = new Upload();
							upload.setSource(destinationFile);
							upload.setDestination(destination);
							upload.setDestinationName(destinationHandler.getDestinationName(destination));
							upload.setSize(destinationFile.length());
							upload.setDestinationHandler(destinationHandler);
							
							if(l != null) {
								l.uploadedSubmitted(upload);
							}
							
							exec.submit(new Runnable() {
								@Override
								public void run() {
									try {
										destinationHandler.write(upload);
									} catch (Exception e) {
										BorderMaker.showException(e);
									}
								}
							});
						}
					} else {
						if(l != null) {
							l.progress();
						}
					}
				} else {
					if(l != null) {
						l.progress();
					}
				}
				
				if(l != null) {
					l.fileProcessed(sourceFile, destination.toString(), 0, Math.min(i, sourceFiles.size() - 2));
				}
			}

			if(l != null) {
				l.awaitingUploads();
			}
			exec.shutdown();
			exec.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		} catch (RejectedExecutionException e) {
		} catch (Exception e) {
			if(!(e.getCause() instanceof InterruptedException) && !(e.getCause() instanceof RejectedExecutionException)) {
				throw new RuntimeException(e);
			}
		} finally {
			try {
				for (DestinationHandler destinationHandler : destinationHandlers) {
					try {
						if(destinationHandler.isEnabled()) {
							destinationHandler.end();
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			
			try {
				for (File file : tempFiles) {
					try {
						if(file != null && file.isFile()) {
							if(!file.delete()) {
								file.deleteOnExit();
							}
						}
					} catch (Exception e) {}
				}
			} catch (Exception e) {}
			
			if(l != null) {
				l.stopProcessing();
			}
			
			try {
				exec.shutdownNow();
			} catch (Exception e2) {
			}
		}
	}	
	
//	private float calculateDummyProgress() {
//		return (float)((float)100 / 1 / (float)totalFiles);
//	}
//	
//	private float calculateProgress(int subProgress) {
//		return calculateProgress(b, totalFiles, subProgress);
//	}
//	
//	private float calculateSubProgress(int subProgress) {
//		return calculateSubProgress(b, subProgress);
//	}
//	
//	private static float calculateProgress(BorderMakerBean b, int totalFiles, int subProgress) {
//		int subProcesses = 1;
//		if(b.isResize()) {
//			subProcesses++;
//		}
//		if(b.isUsm()) {
//			subProcesses++;
//		}
//		return (float)((float)subProgress / (float)subProcesses / (float)totalFiles);
//	}
//	
//	private static float calculateSubProgress(BorderMakerBean b, int subProgress) {
//		int subProcesses = 1;
//		if(b.isResize()) {
//			subProcesses++;
//		}
//		if(b.isUsm()) {
//			subProcesses++;
//		}
//		return (float)((float)subProgress / (float)subProcesses);
//	}

	public BufferedImage processFile(File sourceFile, boolean preview) throws Exception {
//		totalFiles = 1;
		if(l != null) {
			l.startProcessing(1);
		}
		
		BufferedImage image = null;
		try {
			Metadata metadata = null;
			if(sourceFile.getName().toLowerCase().endsWith(".jpg")) {
				metadata = BorderMakerProcessor.readMetadata(sourceFile);
			}
			image = processFile(metadata, sourceFile, 0, 1, preview);
		} finally {
			if(l != null) {
				l.stopProcessing();
			}
		}
		
		return image;
	}
	
	private BufferedImage processFile(Metadata metadata, File sourceFile, int counter, int total, boolean preview) throws Exception {
//		float subProgress = 0;
		// Get the source image.
		Image sourceImage = null;
		File tmpFile = null;
		Graphics2D g = null;
		BufferedImage image = null;
		try {
			sourceImage = loadImage(sourceFile, b);
			
//			progress += calculateProgress(100);
//			subProgress += calculateSubProgress(100);
//			if(l != null) {
//				l.progress();
//				l.subProgress(subProgress);
//			}
			

			int imageAvg = getImageAvg(b.getMaximumWidth(), b.getMaximumHeight(), sourceImage.getWidth(null), sourceImage.getHeight(null), b.getScaleMethode());
			BorderMakerImageResult r = paintImage(null, sourceImage, b.getBorders(), b.getTitles(), b.getMaximumWidth(), b.getMaximumHeight(), metadata, b, l, preview ? ScaleQuality.NearestNeighbor : b.getScaleQuality(), b.getScaleMethode(), b.isResize(), b.isUsm(), total, false, imageAvg);
			image = processFile(metadata, sourceFile, r, counter, total, imageAvg, preview);
			if(l != null) {
				l.progress();
			}
		} finally {
			if(g != null) {
				g.dispose();
			}
			if(sourceImage != null) {
				sourceImage.flush();
			} 
			if(tmpFile != null) {
				try {
					tmpFile.deleteOnExit();
					tmpFile.delete();
				} catch (Exception e) {}
			}
			sourceImage = null;
		}
		return image;
	}
	
	private BufferedImage processFile(Metadata metadata, File sourceFile, BorderMakerImageResult r, int counter, int total, int imageAvg, boolean preview) throws Exception {
//		float subProgress = 0;
		// Get the source image.
		Image sourceImage = null;
		File tmpFile = null;
		Graphics2D g = null;
		BufferedImage image = null;
		try {
			int imageWidth = r.getImageSize().width;
			int imageHeight = r.getImageSize().height;
			g = r.getGraphics();
			image = r.getImage();
			Color averageColor = r.getAverageColor();
//			progress = r.getProgress();
//			subProgress = r.getSubProgress();
			
			if(askForTitle) {
				boolean containsTitles = false;
				for (BorderMakerTitleBean t : b.getTitles()) {
					if(t.getText().contains("%t")) {
						containsTitles = true;
						break;
					}
				}
				
				if(containsTitles) {
					TitleDialog titleDialog = new TitleDialog(BorderMaker.mainFrame, b, sourceFile);
					BorderMaker.processWindowPositions(titleDialog);
					titleDialog.setTitle(BorderMaker.getMessage("imageTitle"));
					titleDialog.setVisible(true);
					
					title = titleDialog.getImageTitle();
					if(titleDialog.getCancelClicked()) {
						throw new InterruptedException();
					}
					if(titleDialog.getSkipClicked()) {
						return null;
					}
					if(titleDialog.getDontAsk() == true) {
						askForTitle = false;
					}
				}
			}
			
			checkThread();
			
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			checkThread();
			
			paintBorders(image, g, b.getBorders(), imageWidth, imageHeight, averageColor, imageAvg, preview, b);
			
			checkThread();
			
			paintImages(g, b.getImages(), imageWidth, imageHeight);
			
			checkThread();

			Composite composite = g.getComposite();
			
			paintTitles(g, image, b.getTitles(), imageWidth, imageHeight, averageColor, sourceFile.getName(), metadata, dateFormat, title, counter, total, b, imageAvg);

			g.setComposite(composite);
			
			System.gc();
		} finally {
			if(g != null) {
				g.dispose();
			}
			if(tmpFile != null) {
				try {
					tmpFile.deleteOnExit();
					tmpFile.delete();
				} catch (Exception e) {}
			}
		}
		return image;
	}

	public static int getRotation(Metadata metadata) throws Exception {
		int imageRotate = 0;
		if(metadata != null) {
			String rotate = "1";
			try	{
				ExifDirectory exifDirectory = (ExifDirectory) metadata.getDirectory(ExifDirectory.class);
				rotate = exifDirectory.getString(ExifDirectory.TAG_ORIENTATION);
				if(rotate == null) {
					rotate = "1";
				}
			} catch (Exception e){}
			
			if (!rotate.equals("1") && (rotate.equals("6") || rotate.equals("8"))) {
				if (rotate.equals("6"))	{
					imageRotate = 90;
				}
				if (rotate.equals("8"))	{
					imageRotate = -90;
				}
			}
		}
		
		return imageRotate;
	}
	
	public static int getImageAvg(int pmaxWidth, int pmaxHeight, int orgSourceImageWidth, int orgSourceImageHeight, ScaleMethode sm) {
		int imageAvg = 0;
		{
			if(sm != ScaleMethode.CONSTRAIN_PROPORTIONS) {
				imageAvg = (pmaxWidth + pmaxHeight) / 2;
			} else {
				Dimension dim = getProportionalDimension(orgSourceImageWidth, orgSourceImageHeight, pmaxWidth, pmaxHeight);
				imageAvg = (dim.width + dim.height) / 2;
			}
		}	
		return imageAvg;
	}
	
	public static BorderMakerImageResult paintImage(Graphics2D g, Image sourceImage, List<BorderMakerBorderBean> borderBeans, List<BorderMakerTitleBean> titleBeans, int pmaxWidth, int pmaxHeight, Metadata metadata, BorderMakerBean b, BorderMakerListener l, ScaleQuality sq, ScaleMethode sm, boolean resize, boolean usm, int totalFiles, boolean stretch, int imageAvg) throws Exception {
		int totalBorderTopWidth = 0;
		int totalBorderRightWidth = 0;
		int totalBorderBottomWidth = 0;
		int totalBorderLeftWidth = 0;
		int orgSourceImageWidth = sourceImage.getWidth(null);
		int orgSourceImageHeight = sourceImage.getHeight(null);
		
		Image scaledImage = null;
		Image sharpenedImage = null;
		BufferedImage rotatedImage = null;
		Image paintImage = null;
		BufferedImage image = null;
		
		boolean autoColor = false;
		
		for (int titleCount = 0; titleCount < titleBeans.size(); titleCount++) {
			BorderMakerTitleBean bean = (BorderMakerTitleBean) titleBeans.get(titleCount);
			if(bean.isAutoColor()) {
				autoColor = true;
				break;
			}
		}
		
		boolean foundTransparent = false;
		for (BorderMakerBorderBean bean : borderBeans) {
			if(isTransparent(bean)) {
				foundTransparent = true;
			}
			
			if(!foundTransparent) {
				totalBorderTopWidth += bean.getTopWidth(b, imageAvg);
				totalBorderRightWidth += bean.getRightWidth(b, imageAvg);
				totalBorderBottomWidth += bean.getBottomWidth(b, imageAvg);
				totalBorderLeftWidth += bean.getLeftWidth(b, imageAvg);
			}
			
			if(bean.isAutoColor()) {
				autoColor = true;
			}
		}

		int imageRotate = 0;
		if(b.isAutoRotate() && metadata != null) {
			imageRotate = getRotation(metadata);
		}

		int maxHeight = -1; 
		int maxWidth = -1; 
		
		if(resize) {
			if(!stretch) {
				maxHeight = Math.min(pmaxHeight, orgSourceImageHeight + (totalBorderTopWidth + totalBorderBottomWidth)); 
				maxWidth = Math.min(pmaxWidth, orgSourceImageWidth + (totalBorderRightWidth + totalBorderLeftWidth));
			} else {
				maxHeight = pmaxHeight;
				maxWidth = pmaxWidth;
			}
		} else {
			maxHeight = orgSourceImageHeight + (totalBorderTopWidth + totalBorderBottomWidth);
			maxWidth = orgSourceImageWidth + (totalBorderRightWidth + totalBorderLeftWidth);
		}

		int imageWidth = maxWidth - (totalBorderRightWidth + totalBorderLeftWidth);
		int imageHeight = maxHeight - (totalBorderTopWidth + totalBorderBottomWidth);
		
		imageWidth = Math.max(1, imageWidth);
		imageHeight = Math.max(1, imageHeight);
		
		if(imageRotate != 0) {
			int tmpWidth = imageWidth;
			imageWidth = imageHeight;
			imageHeight = tmpWidth;
		}

		int cropX = 0;
		int cropY = 0;
		
		int cropWidth = imageWidth;
		int cropHeight = imageHeight;
		
		int orgImageWidth = imageWidth;
		int orgImageHeight = imageHeight;

		int csourceImageWidth = orgSourceImageWidth;
		int csourceImageHeight = orgSourceImageHeight;
		
//		boolean cropImageWidth = orgSourceImageWidth >= orgSourceImageHeight;
		boolean cropImageWidth = false;
		
		int sourceImageWidth = orgSourceImageWidth;
		int sourceImageHeight = orgSourceImageHeight;

		if(sm == ScaleMethode.CROP) {
			double thumbRatio = (double) imageWidth / (double) imageHeight;
			double imageRatio = (double) csourceImageWidth / (double) csourceImageHeight;

			cropImageWidth = thumbRatio < imageRatio;
			if(cropImageWidth) {
				imageWidth = Integer.MAX_VALUE;
			} else {
				imageHeight = Integer.MAX_VALUE;
			}
			
		}
		
		if(sm != ScaleMethode.SQUEEZE) {
			double thumbRatio = (double) imageWidth / (double) imageHeight;
			double imageRatio = (double) sourceImageWidth / (double) sourceImageHeight;
			if (thumbRatio < imageRatio) {
				imageHeight = (int) (imageWidth / imageRatio);
			} else {
				imageWidth = (int) (imageHeight * imageRatio);
			}
		}
		
		if(sm == ScaleMethode.CROP) {
			if(imageRotate != 0) {
				if(cropImageWidth) {
					cropX = (int) Math.ceil(Math.max(Math.min((double)imageWidth - (double)orgImageWidth, (double)imageWidth) / 2d, 0d));
					cropHeight = (int) Math.ceil(Math.min((double)orgImageHeight + (double)cropY, (double)imageHeight));
				} else {
					cropWidth = (int) Math.ceil(Math.min((double)orgImageWidth + (double)cropX, (double)imageWidth));
					cropY = (int) Math.ceil(Math.max(Math.min((double)imageHeight - (double)orgImageHeight, (double)imageHeight) / 2d, 0d));
				}
			} else {
				if(cropImageWidth) {
					cropX = (int) Math.ceil(Math.max(Math.min((double)imageWidth - (double)orgImageWidth, (double)imageWidth) / 2d, 0d));
					cropWidth = (int) Math.ceil(Math.min((double)orgImageWidth + (double)cropX, (double)imageWidth));
				} else {
					cropY = (int) Math.ceil(Math.max(Math.min((double)imageHeight - (double)orgImageHeight, (double)imageHeight) / 2d, 0d));
					cropHeight = (int) Math.ceil(Math.min((double)orgImageHeight + (double)cropY, (double)imageHeight));
				}
			}
		}

		imageWidth = Math.max(1, imageWidth);
		imageHeight = Math.max(1, imageHeight);
		
		scaledImage = sourceImage;
		if(resize && (stretch || ((sourceImageWidth > imageWidth) || (sourceImageHeight > imageHeight)))) {
			scaledImage = getScaledInstanceThijs((BufferedImage) sourceImage, imageWidth, imageHeight, sq);
//			progress += calculateProgress(b, totalFiles, 100);
//			subProgress += calculateSubProgress(b, 100);
//			if(l != null) {
//				l.progress();
//				l.subProgress(subProgress);
//			}

			
			if(sourceImage != null) {
				sourceImage.flush();
				sourceImage = null;
			}
		}

		if(sm == ScaleMethode.CROP && (cropHeight + cropWidth + cropX + cropY) != 0) {
			scaledImage = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(scaledImage.getSource(),
			        new CropImageFilter(cropX, cropY, cropWidth, cropHeight)));
			MediaTracker tracker = new MediaTracker(MEDIA_TRACKER_COMPONENT);
			tracker.addImage(scaledImage, 1);
			tracker.waitForAll();
			
			if(cropImageWidth) {
				imageWidth -= cropX  + (imageWidth - cropWidth);
			} else {
				imageHeight -= cropY + (imageHeight - cropHeight);
			}
			
			if(imageHeight <= 0) {
				imageHeight = 1;
			}
			
			if(imageWidth <= 0) {
				imageWidth = 1;
			}
		}
		
		sharpenedImage = scaledImage;
		if(usm) {
			UnsharpMaskProc unsharpmaskproc = new UnsharpMaskProc(scaledImage);
			unsharpmaskproc.setAmount((float) ((float) b.getUsmAmount() / 100f));
			unsharpmaskproc.setSigma((float) (b.getUsmRadius()));
			unsharpmaskproc.run();
			
			sharpenedImage = unsharpmaskproc.getImage();
			unsharpmaskproc = null;

			MediaTracker tracker = new MediaTracker(MEDIA_TRACKER_COMPONENT);
			tracker.addImage(sharpenedImage, 1);
			tracker.waitForAll();
			
//			progress += calculateProgress(b, totalFiles, 100);
//			subProgress += calculateSubProgress(b, 100);
//			if(l != null) {
//				l.progress();
//				l.subProgress(subProgress);
//			}
		}

		
		paintImage = sharpenedImage;
		// If we have to rotate switch width and height.
		if(imageRotate != 0) {
			rotatedImage = rotate(sharpenedImage, imageRotate);
			paintImage = rotatedImage;
			imageWidth = rotatedImage.getWidth();
			imageHeight = rotatedImage.getHeight();
		}

	    int newImageWidth = imageWidth + totalBorderRightWidth + totalBorderLeftWidth;
	    int newImageHeight = imageHeight + totalBorderTopWidth + totalBorderBottomWidth;
	    
	    if(g == null) {
	    	image = new BufferedImage(newImageWidth, newImageHeight, BufferedImage.TYPE_INT_ARGB);
	    	g = image.createGraphics();
	    } else {
	    	image = toBufferedImage(paintImage);
	    }

	    Color averageColor = null;
	    if(autoColor) {
	    	averageColor = getAverageColor(paintImage);
	    }
	    
		g.drawImage(paintImage, totalBorderLeftWidth, totalBorderTopWidth, null);
		
		if(rotatedImage != null) {
			rotatedImage.flush();
		}
		
		if(sharpenedImage != null) {
			sharpenedImage.flush();
		}

		if(scaledImage != null) {
			scaledImage.flush();
		}

		rotatedImage = null;
		sharpenedImage = null;
		scaledImage = null;
		paintImage = null;
		
		BorderMakerImageResult r = new BorderMakerImageResult();
		r.setAutoColor(autoColor);
		r.setAverageColor(averageColor);
		r.setImage(image);
		r.setImageSize(new Dimension(newImageWidth, newImageHeight));
		r.setGraphics(g);
//		r.setProgress(progress);
//		r.setSubProgress(subProgress);
		return r;
	}
	
	private static BufferedImage filter(BufferedImageOp op, Image img) {
		BufferedImage src = toBufferedImage(img);
		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		op.filter(src, dest);
		return dest;
	}
	
	private static void filterPart(BufferedImageOp op, Graphics2D g, BufferedImage src, Area area) {
		g.setClip(area);
		g.drawImage(src, op, 0, 0);
		g.setClip(null);
	}
	
	public static Color getAverageColor(Image image) throws Exception {
		//Calculate average Color
		Rectangle imageBounds = new Rectangle(0,0,image.getWidth(null), image.getHeight(null));
		int[] imagePixels = new int[imageBounds.width * imageBounds.height];
		PixelGrabber imagePixelGrabber = new PixelGrabber(image, imageBounds.x, imageBounds.y, imageBounds.width, imageBounds.height, imagePixels, 0, imageBounds.width);
		imagePixelGrabber.grabPixels();
		if ((imagePixelGrabber.getStatus() & ImageObserver.ABORT) != 0) {
			throw new Exception("image fetch aborted or errored");
		}
		
		double averageImageR = 0;
		double averageImageG = 0;
		double averageImageB = 0;
		Color averageColor = null;
		int pixelCounter = 0;
		for (int yPixels = 0; yPixels < imageBounds.height; yPixels++) {
			for (int xPixels = 0; xPixels < imageBounds.width; xPixels++) {
				int pixel = imagePixels[yPixels * imageBounds.width + xPixels];
				int imageR = (pixel >> 16) & 0xff;
				int imageG = (pixel >>  8) & 0xff;
				int imageB = (pixel      ) & 0xff;

				int averageImageColor = (imageR + imageG + imageB) / 3;
				if(averageImageColor > 50 && averageImageColor < 200) {
					averageImageR += imageR;
					averageImageG += imageG;
					averageImageB += imageB;
					pixelCounter++;
				}
			}
		}
		
		averageImageR = Math.round(averageImageR / pixelCounter);
		averageImageG = Math.round(averageImageG / pixelCounter);
		averageImageB = Math.round(averageImageB / pixelCounter);

		float[] hsb = Color.RGBtoHSB((int) averageImageR, (int) averageImageG, (int) averageImageB, null);
		int saturatedColor = Color.HSBtoRGB(hsb[0], Math.min(hsb[1] + 0.1f, 1f), Math.min(hsb[2] + 0.3f, 1f));
		averageColor = new Color(saturatedColor);
		
		return averageColor;
	}
	
	public static void paintTitles(Graphics2D g2, Image image, List<BorderMakerTitleBean> titles, int imageWidth, int imageHeight, Color averageColor, String orgFilename, Metadata metadata, Format dateFormat, String fileTitle, int counter, int total, BorderMakerBean borderMakerBean, int imageAvg) throws Exception {
		// Add the titles.
		for (int titleCount = 0; titleCount < titles.size(); titleCount++) {
			BorderMakerTitleBean bean = (BorderMakerTitleBean) titles.get(titleCount);

			String text = bean.getText();
			try {
				text = BorderMaker.processString(bean.getText(), orgFilename, metadata, dateFormat, fileTitle, counter, total, borderMakerBean);
			} catch (Exception e) {}
			
			if(text == null || text.trim().equals("")) {
				continue;
			}

			Font font = new Font(bean.getFont(), bean.getFontStyle(), bean.getPointSize(borderMakerBean, imageAvg));

			ShadowFilter fi = null;
			Point p = new Point();
			if(bean.isShadow()) {
				fi = new ShadowFilter();
				fi.setAngle(-45f);
				fi.setOpacity((float) bean.getShadowOpacity() / 100f);
				fi.setRadius(bean.getShadowRadius());
				fi.setDistance(bean.getShadowDistance());
				fi.setShadowColor(bean.getShadowColor() == null ? Color.BLACK.getRGB() : bean.getShadowColor().getRGB());

				p.x = bean.getShadowRadius() + bean.getShadowDistance();
				p.y = p.x;
			}
	    	
			Rectangle bounds = g2.getFontMetrics(font).getStringBounds(text, g2).getBounds();

			AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(bean.getRotate()));

			Rectangle transformedBounds = transform.createTransformedShape(bounds).getBounds();

			BufferedImage im = new BufferedImage(transformedBounds.width + (p.x * 2), transformedBounds.height + (p.y * 2), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) im.getGraphics();
			g.translate(p.x, p.y);
			if(bean.isAntiAlias()) {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			} else {
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
			}
			
			Point point = translatePoint(bean.getXOffset(borderMakerBean, imageAvg), bean.getYOffset(borderMakerBean, imageAvg), bean.getGravity(), new Dimension(transformedBounds.width, transformedBounds.height), imageWidth, imageHeight);

			//Rectangle textRext = new Rectangle(point.x, point.y, transformedBounds.width, transformedBounds.height);
			
			Color fillColor = bean.getFillColor();

			if(bean.isAutoColor() && averageColor != null) {
				fillColor = averageColor;
			}
			
			g.translate(0 - transformedBounds.x, 0 - transformedBounds.y);
		    g.transform(transform);
		    g.setFont(font);
		    
			g.setColor(fillColor);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((float) bean.getFillTransparency()) / 100f));

	    	g.drawString(text, 0, 0);
	    	if(bean.isShadow()) {
	    		g2.drawImage(fi == null ? im : fi.filter(im, null), point.x - p.x, point.y - p.y, null);
	    	} else {
	    		g2.drawImage(im, point.x - p.x, point.y - p.y, null);
	    	}
		}
	}

	public static void paintImages(Graphics2D g, Iterable<BorderMakerImageBean> images, int imageWidth, int imageHeight) throws IOException {
		for (BorderMakerImageBean bean : images) {
			if(bean.getImage() != null && bean.getImage().exists()) {
				BufferedImage overlayImage = ImageIO.read(bean.getImage());
			    
				Point point = translatePoint(bean.getXOffset(), bean.getYOffset(), bean.getGravity(), new Dimension(overlayImage.getWidth(null), overlayImage.getHeight(null)), imageWidth, imageHeight);

				Point p = new Point();
				ShadowFilter fi = null;
				BufferedImage im = null;
				if(bean.isShadow()) {
					fi = new ShadowFilter();
					fi.setAngle(-45f);
					fi.setOpacity((float) bean.getShadowOpacity() / 100f);
					fi.setRadius(bean.getShadowRadius());
					fi.setDistance(bean.getShadowDistance());
					fi.setShadowColor(bean.getShadowColor() == null ? Color.BLACK.getRGB() : bean.getShadowColor().getRGB());

					p.x = bean.getShadowRadius() + bean.getShadowDistance();
					p.y = p.x;

					BufferedImage om = new BufferedImage(overlayImage.getWidth() + (p.x * 2), overlayImage.getHeight() + (p.y * 2), BufferedImage.TYPE_INT_ARGB);
					om.getGraphics().drawImage(overlayImage, p.x, p.y, overlayImage.getWidth(), overlayImage.getHeight(), null);
					overlayImage = om;
				}
		    	
				g.drawImage(fi == null ? overlayImage : fi.filter(overlayImage, im), point.x - p.x, point.y - p.y, null);
			}
		}
	}

	public static Image loadImage(File sourceFile, BorderMakerBean b) throws ImageFormatException, IOException {
		return BorderMaker.loadImage(sourceFile, b.isOptimizedImageLoading(), b.getMaximumWidth(), b.getMaximumHeight());
//		Image sourceImage = null;
//		FileInputStream fis = null;
//		try {
//			// load file from disk using Sun's JPEGIMageDecoder
//			fis = new FileInputStream(sourceFile);
//			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(fis);
//			sourceImage = decoder.decodeAsBufferedImage();
//		} finally {
//			try {
//				fis.close();
//			} catch (Exception e) {}
//		}
//
//		return sourceImage;
	}
	
	public static boolean isTransparent(BorderMakerBorderBean bmbb) {
		return bmbb.getFillTransparency() < 100 || bmbb.getComposite() != BlendMode.NORMAL;
	}
	
	public static void paintBorders(BufferedImage image, Graphics2D g, List<BorderMakerBorderBean> borders, int imageWidth, int imageHeight, Color averageColor, int imageAvg, boolean preview, BorderMakerBean bb) {
		// Add the borders
		float bordersLeft = 0;
		float bordersTop = 0;
		float bordersRight = imageWidth;
		float bordersBottom = imageHeight;
		int blur = bb.getBlur();
		
		Image blurred = null;
		if(blur > 0) {
			for (BorderMakerBorderBean bmbb : borders) {
				if(isTransparent(bmbb)) {
					BufferedImageOp fi = null;
					if(preview) {
						BoxBlurFilter gf = new BoxBlurFilter();
						gf.setRadius(blur / 2);
						fi = gf;
					} else {
						GaussianFilter gf = new GaussianFilter();
						gf.setRadius(blur);
						fi = gf;
					}
					
					blurred = filter(fi, image);
					break;
				}
			}
		}
		
		ArrayList<BorderPaint> paint = new ArrayList<BorderPaint>();

		float comp = 0f;
		float rcomp = 0f;
		Area prevArea = null;
		for (int i = 0; i < borders.size(); i++) {
			BorderMakerBorderBean bmbb = borders.get(i);

			comp = 0f;
			rcomp = 0f;
			if(i < borders.size() - 1) {
				BorderMakerBorderBean nbmbb = borders.get(i + 1);
				comp = (float) nbmbb.getFillTransparency() * 0.005f;
				rcomp = 0f;
			}
			
			bordersLeft += bmbb.getLeftWidth(bb, imageAvg);
			bordersRight -= bmbb.getRightWidth(bb, imageAvg);
			bordersTop += bmbb.getTopWidth(bb, imageAvg);
			bordersBottom -= bmbb.getBottomWidth(bb, imageAvg);
			
			Color color = bmbb.getColor();

			if(bmbb.isAutoColor() && averageColor != null) {
				color = averageColor;
			}
			
			Area transparent = null; 
			Area substractFromCopy = null; 
			if(bmbb.getRounded(bb, imageAvg) > 0) {
				transparent = new Area(new RoundRectangle2D.Float(bordersLeft + comp, bordersTop + comp, bordersRight - bordersLeft - (comp * 2f), bordersBottom - bordersTop - (comp * 2f), bmbb.getRounded(bb, imageAvg) + rcomp, bmbb.getRounded(bb, imageAvg) + rcomp));
				substractFromCopy = new Area(new RoundRectangle2D.Double(bordersLeft, bordersTop, bordersRight - bordersLeft, bordersBottom - bordersTop, bmbb.getRounded(bb, imageAvg), bmbb.getRounded(bb, imageAvg)));
			} else {
				transparent = new Area(new Rectangle2D.Double(bordersLeft, bordersTop, bordersRight - bordersLeft, bordersBottom - bordersTop));
				substractFromCopy = transparent;
			}
			Area blurrect = new Area(new Rectangle2D.Double(0, 0, imageWidth, imageHeight));
			Area rect = new Area(new Rectangle2D.Double(0, 0, imageWidth, imageHeight));
			Area rectCopy = new Area(new Rectangle2D.Double(0, 0, imageWidth, imageHeight));
			
			rect.subtract(transparent);
			blurrect.subtract(transparent);
			
			if(prevArea != null) {
				rect.subtract(prevArea);
			}

			rectCopy.subtract(substractFromCopy);
			
			prevArea = rectCopy;
			
			BorderPaint p = new BorderPaint();
			p.paint = rect;
			
			p.composite = bmbb.getComposite().getComposite(((float) bmbb.getFillTransparency()) / 100f);
			p.color = color;
			paint.add(p);
		}

		if(blurred != null) {
			if(true) { //Soft clipping
				// Create a translucent intermediate image in which we can perform
				// the soft clipping
				GraphicsConfiguration gc = g.getDeviceConfiguration();
//				int width = blurrect.getBounds().width;
//				int height = blurrect.getBounds().height;
				int width = image.getWidth();
				int height = image.getHeight();
				
//				if(width <= 0 && height <= 0) {
//					continue;
//				}

				BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
				Graphics2D g2 = img.createGraphics();

				// Clear the image so all pixels have zero alpha
				g2.setComposite(AlphaComposite.Clear);
				g2.fill(prevArea);

				// Render our clip shape into the image.  Note that we enable
				// antialiasing to achieve the soft clipping effect.  Try
				// commenting out the line that enables antialiasing, and
				// you will see that you end up with the usual hard clipping.
				g2.setComposite(AlphaComposite.Src);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(Color.WHITE);
				g2.fill(prevArea);

				// Here's the trick... We use SrcAtop, which effectively uses the
				// alpha value as a coverage value for each pixel stored in the
				// destination.  For the areas outside our clip shape, the destination
				// alpha will be zero, so nothing is rendered in those areas.  For
				// the areas inside our clip shape, the destination alpha will be fully
				// opaque, so the full color is rendered.  At the edges, the original
				// antialiasing is carried over to give us the desired soft clipping
				// effect.
				g2.setComposite(AlphaComposite.SrcAtop);
				g2.drawImage(blurred, 0, 0, null);
				g2.dispose();

				g.drawImage(img, 0, 0, null);
			} else {
				g.setClip(prevArea);
				g.drawImage(blurred, 0, 0, null);
				g.setClip(null);
			}
		}

		Collections.reverse(paint);
		
		for (BorderPaint p : paint) {
//			ShadowFilter sf = new ShadowFilter();
//			sf.setDistance(10);
//			sf.setRadius(5);
//			sf.setAngle(45);
//			sf.setShadowOnly(true);
//
//			BufferedImage img = g.getDeviceConfiguration().createCompatibleImage(image.getWidth(), image.getHeight(), Transparency.TRANSLUCENT);
//			Graphics2D graphics2d = (Graphics2D) img.getGraphics();
//			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			graphics2d.setComposite(p.composite);
//			graphics2d.setColor(p.color);
//			graphics2d.fill(p.paint);
//			g.drawImage(sf.filter(img, null), 0, 0, null);
//			graphics2d.dispose();

			Composite prevcomp = g.getComposite();
			g.setComposite(p.composite);
			g.setColor(p.color);
			g.fill(p.paint);
			g.setComposite(prevcomp);
		}
	}	
	
	public static Dimension getProportionalDimension(int imageWidth, int imageHeight, int pmaxWidth, int pmaxHeight) {
		int thumbWidth = pmaxWidth;
		int thumbHeight = pmaxHeight;
		double thumbRatio = (double)thumbWidth / (double)thumbHeight;
		double imageRatio = (double)imageWidth / (double)imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int)(thumbWidth / imageRatio);
		} else {
			thumbWidth = (int)(thumbHeight * imageRatio);
		}
		
		return new Dimension(thumbWidth, thumbHeight);
	}
	
	public static Image getScaledInstanceQuick(Image image, int pmaxWidth, int pmaxHeight) throws InterruptedException {
		Dimension size = getProportionalDimension(image.getWidth(null), image.getHeight(null), pmaxWidth, pmaxHeight);
	    // draw original image to thumbnail image object and
	    // scale it to the new size on-the-fly
	    BufferedImage thumbImage = new BufferedImage(size.width, 
	    		size.height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = thumbImage.createGraphics();
	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	    graphics2D.drawImage(image, 0, 0, size.width, size.height, null);

		return thumbImage;
	}
	
	public static Image getScaledInstanceThijs(BufferedImage img, int targetWidth, int targetHeight, ScaleQuality q) throws InterruptedException {
		if(q == ScaleQuality.MultiStepRescale) {
			MultiStepRescaleOp resampleOp = new MultiStepRescaleOp(targetWidth, targetHeight);
			return resampleOp.filter(img, null);
		} else if (q == ScaleQuality.NearestNeighbor) {
			int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
			BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
			g2.dispose();
			return tmp;
		} else {
			ResampleOp resampleOp = new ResampleOp(targetWidth, targetHeight);
			switch (q) {
			case Bell:
				resampleOp.setFilter(ResampleFilters.getBellFilter());
				break;
			case BiCubic:
				resampleOp.setFilter(ResampleFilters.getBiCubicFilter());
				break;
			case Box:
				resampleOp.setFilter(ResampleFilters.getBoxFilter());
				break;
			case BSpline:
				resampleOp.setFilter(ResampleFilters.getBSplineFilter());
				break;
			case Hermite:
				resampleOp.setFilter(ResampleFilters.getHermiteFilter());
				break;
			case Lanczos3:
				resampleOp.setFilter(ResampleFilters.getLanczos3Filter());
				break;
			case Mitchell:
				resampleOp.setFilter(ResampleFilters.getMitchellFilter());
				break;
			case Triangle:
				resampleOp.setFilter(ResampleFilters.getTriangleFilter());
				break;
			}
			return resampleOp.filter(img, null);
		}
//		
//		int quality = 30;
//		switch (q) {
//			case WORST:
//				quality = 0;
//				break;
//			case FINE:
//				return getScaledInstance(img, targetWidth, targetHeight);
//			case GOOD:
//				quality = 30;
//				break;
//			case BETTER:
//				quality = 70;
//				break;
//			case BEST:
//				quality = 100;
//				break;
//		}
//		
//		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
//		BufferedImage ret = (BufferedImage) img;
//		// Use multi-step technique: start with original size, then
//		// scale down in multiple passes with drawImage()
//		// until the target size is reached
//		int imageWidth = img.getWidth();
//		int imageHeight = img.getHeight();
//
//		double qualityDouble = (double) quality / 100;
//		
////		int firstPassWidth = targetWidth + ((imageWidth - targetWidth) / 2);
////		int firstPassHeight = targetHeight + ((imageHeight - targetHeight) / 2);
//		int firstPassWidth = targetWidth + (int) (((double) (imageWidth - targetWidth)) * qualityDouble);
//		int firstPassHeight = targetHeight + (int) (((double) (imageHeight - targetHeight)) * qualityDouble);
//
////		System.out.printf("%d x %d%n", firstPassWidth, firstPassHeight);
//
//		Image retValue = img;
//		if(quality >= 0 && quality < 100) {
//			BufferedImage tmp = new BufferedImage(firstPassWidth, firstPassHeight, type);
//			Graphics2D g2 = tmp.createGraphics();
//			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, quality == 0 ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR: RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//			g2.drawImage(ret, 0, 0, firstPassWidth, firstPassHeight, null);
//			g2.dispose();
//			
//			retValue = tmp;
//		}
//		
//		if(quality > 0) {
//			retValue = getScaledInstance2((BufferedImage) retValue, targetWidth, targetHeight);
//		}
//		
//		return retValue;
	}

    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
	public static Image getScaledInstance(BufferedImage img, int targetWidth, int targetHeight) {
		boolean higherQuality = true;
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w, h;
		if (higherQuality) {
			// Use multi-step technique: start with original size, then
			// scale down in multiple passes with drawImage()
			// until the target size is reached
			w = img.getWidth();
			h = img.getHeight();
		} else {
			// Use one-step technique: scale directly from original
			// size to target size with a single drawImage() call
			w = targetWidth;
			h = targetHeight;
		}

		do {
			if (higherQuality && w > targetWidth) {
				w /= 2;
				if (w < targetWidth) {
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight) {
				h /= 2;
				if (h < targetHeight) {
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}
    
//	public static PixelImage getScaledInstanceJUI(PixelImage jiuImage, int targetWidth, int targetHeight) throws Exception {
//		Resample resample = new Resample();
//		resample.setInputImage(jiuImage);
//		resample.setSize(targetWidth, targetHeight);
//		resample.setFilter(Resample.FILTER_TYPE_MITCHELL);
//		resample.process();
//		return resample.getOutputImage();
//	}
	
    public static Image getScaledInstance2(BufferedImage img, int targetWidth, int targetHeight) throws InterruptedException {
		Image scaledImage = img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING);
		MediaTracker tracker = new MediaTracker(MEDIA_TRACKER_COMPONENT);
		tracker.addImage(scaledImage, 1);
		tracker.waitForAll();
		return scaledImage;
	}
    
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
    
        // This code ensures that all the pixels in the image are loaded
		MediaTracker tracker = new MediaTracker(MEDIA_TRACKER_COMPONENT);
		tracker.addImage(image, 1);
		try {
			tracker.waitForAll();
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
		
    
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        boolean hasAlpha = false;
    
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }
    
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
    
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
    
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
    
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
    
        return bimage;
    }
    
    private void getSourceFiles(List<File> sourceFiles, BorderMakerBean bean) throws Exception {
    	if(bean.getSourceDirectory() == null && bean.getExampleFile() != null && bean.getExampleFile().isFile()) {
			sourceFiles.add(bean.getExampleFile());
			return;
    	}
    	
    	getSourceFiles(sourceFiles, bean.getSourceDirectory(), bean);
    }
    
	private void getSourceFiles(List<File> sourceFiles, File sourceDir, BorderMakerBean bean) throws Exception {
		if(sourceDir == null) {
			return;
		}
		
		File[] files = sourceDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return 	pathname.getName().toLowerCase().endsWith(".jpg") || 
						pathname.getName().toLowerCase().endsWith(".jpeg") || 
						pathname.isDirectory();
			}
		});
		
		if(files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if(file.isDirectory()) {
					if(bean.isRecurseFolders()) {
						getSourceFiles(sourceFiles, file, bean);
					}
				} else {
					sourceFiles.add(file);
				}
			}
		}
	}
	
	private Destination getDestination(File srcFile, String fileName, BorderMakerBean bean)  throws Exception {
		if(bean.isFlattenFolders() || !bean.isRecurseFolders() || bean.getSourceDirectory() == null) {
			return new Destination(null, fileName);
		} else {
			String srcDir = normalizePath(srcFile.getParentFile().getCanonicalPath());
			String refDir = normalizePath(bean.getSourceDirectory().getCanonicalPath());
			if(srcDir.equals(refDir)) {
				return new Destination(null, fileName);
			} else if(srcDir.startsWith(refDir)) {
				String compute = normalizePath(srcDir.substring(srcDir.indexOf(refDir) + refDir.length()));
				String[] path = StringUtils.split(compute, '/');
				return new Destination(path, fileName);
			} else {
				throw new RuntimeException("Couldn't compute destination for file in folder \"" + srcDir + "\" with base folder \"" + refDir + "\"");
			}
		}
	}
	
	private String normalizePath(String path) {
		path = path.replace('\\', '/');
		path = StringUtils.strip(path, "/");
		return path;
	}
	
	private File getDestinationFile(Destination destination, File baseDir)  throws Exception {
		return new File(baseDir, destination.toString());
	}
	
	public static BufferedImage rotate(Image image, int degrees) {
		// adjust the angle that was passed so it's between 0 and 360 degrees
		double positiveDegrees = (degrees % 360) + ((degrees < 0) ? 360 : 0);
		double degreesMod90 = positiveDegrees % 90;
		double radians = Math.toRadians(positiveDegrees);
		double radiansMod90 = Math.toRadians(degreesMod90);
		
		// don't bother with any of the rest of this if we're not really rotating
		if (positiveDegrees == 0)
			return null;
		
		// figure out which quadrant we're in (we'll want to know this later)
		int quadrant = 0;
		if (positiveDegrees < 90)
			quadrant = 1;
		else if ((positiveDegrees >= 90) && (positiveDegrees < 180))
			quadrant = 2;
		else if ((positiveDegrees >= 180) && (positiveDegrees < 270))
			quadrant = 3;
		else if (positiveDegrees >= 270)
			quadrant = 4;
		
		// get the height and width of the rotated image (you can also do this
		// by applying a rotational AffineTransform to the image and calling
		// getWidth and getHeight against the transform, but this should be a
		// faster calculation)
		int height = image.getHeight(null);
		int width = image.getWidth(null);
		double side1 = (Math.sin(radiansMod90) * height) + (Math.cos(radiansMod90) * width);
		double side2 = (Math.cos(radiansMod90) * height) + (Math.sin(radiansMod90) * width);
		
		double h = 0;
		int newWidth = 0, newHeight = 0;
		if ((quadrant == 1) || (quadrant == 3)) {
			h = (Math.sin(radiansMod90) * height);
			newWidth = (int)side1;
			newHeight = (int)side2;
		} else {
			h = (Math.sin(radiansMod90) * width);
			newWidth = (int)side2;
			newHeight = (int)side1;
		}
		
		// figure out how much we need to shift the image around in order to
		// get the origin where we want it
		int shiftX = (int)(Math.cos(radians) * h) - ((quadrant == 3) || (quadrant == 4) ? width : 0);
		int shiftY = (int)(Math.sin(radians) * h) + ((quadrant == 2) || (quadrant == 3) ? height : 0);
		
		if(newHeight <= 0 || newWidth <= 0) {
			return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		
		// create a new BufferedImage of the appropriate height and width and
		// rotate the old image into it, using the shift values that we calculated
		// earlier in order to make sure the new origin is correct
		BufferedImage newbi = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = newbi.createGraphics();
		g2d.setBackground(Color.BLUE);
		g2d.clearRect(0, 0, newWidth, newHeight);
		g2d.rotate(radians);
		g2d.drawImage(image, shiftX, -shiftY, null);
		return newbi;
	}
	
	public static Point translatePoint(int left, int top, int gravity, Dimension d, int width, int height) {
		int centerX = left + (width / 2) - (d.width / 2);
		int centerY = top + (height / 2) - (d.height / 2);
		int right = width - left - d.width;
		int bottom = height - top - d.height;
		switch (gravity) {
			case BorderMakerTitleBean.GRAVITY_NORTHWEST: {
				return new Point(left, top);
			} case BorderMakerTitleBean.GRAVITY_NORTH: {
				return new Point(centerX, top);
			} case BorderMakerTitleBean.GRAVITY_NORTHEAST: {
				return new Point(right, top);
			} case BorderMakerTitleBean.GRAVITY_WEST: {
				return new Point(left, centerY);
			} case BorderMakerTitleBean.GRAVITY_CENTER: {
				return new Point(centerX, centerY);
			} case BorderMakerTitleBean.GRAVITY_EAST: {
				return new Point(right, centerY);
			} case BorderMakerTitleBean.GRAVITY_SOUTHWEST: {
				return new Point(left, bottom);
			} case BorderMakerTitleBean.GRAVITY_SOUTH: {
				return new Point(centerX, bottom);
			} case BorderMakerTitleBean.GRAVITY_SOUTHEAST: {
				return new Point(right, bottom);
			}
		}
		return null;
	}

    public static void copyExif(File source, File dest, BorderMakerBean b) throws Exception {
		IImageMetadata metadata = null;
		JpegImageMetadata jpegMetadata = null;
		TiffImageMetadata exif = null;
		OutputStream os = null;
		TiffOutputSet outputSet = new TiffOutputSet();

		// establish metadata
		metadata = Sanselan.getMetadata(source);

		// establish jpegMedatadata
		if (metadata != null) {
			jpegMetadata = (JpegImageMetadata) metadata;
		}

		// establish exif
		if (jpegMetadata != null) {
			exif = jpegMetadata.getExif();
		}

		// establish outputSet
		if (exif != null) {
			outputSet = exif.getOutputSet();
		}

		if (outputSet != null && b.isAutoRotate()) {
				outputSet.removeField(TiffConstants.EXIF_TAG_ORIENTATION);
		}

		File tmpFile = null;
		// create stream using temp file for dst
			
		

		// write/update EXIF metadata to output stream
		try {
			tmpFile = File.createTempFile("BorderMaker", ".tmp");
			os = new FileOutputStream(tmpFile);
			os = new BufferedOutputStream(os);
			new ExifRewriter().updateExifMetadataLossless(dest, os, outputSet);
			SemanticaUtil.copyFile(tmpFile, dest);
		} catch (ImageWriteException e) {
			if(!e.getLocalizedMessage().toLowerCase().startsWith("no directories")) {
				throw e;
			}
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {}
			}
			try {
				tmpFile.delete();
			} catch (Exception e) {
			}
		}
		
		
//		// write/update EXIF metadata to output stream
//		ExifRewriter w = null;
//		try {
//			tmpFile = File.createTempFile("BorderMaker", ".tmp");
//			os = new BufferedOutputStream(new FileOutputStream(tmpFile));
//			w = new ExifRewriter();
//			w.updateExifMetadataLossless(dest, os, outputSet);
//			SemanticaUtil.copyFile(tmpFile, dest);
//		} catch (ImageWriteException e) {
//			if(!e.getLocalizedMessage().toLowerCase().startsWith("no directories")) {
//				throw e;
//			}
//		} finally {
//			if (os != null) {
//				try {
//					os.close();
//				} catch (IOException e) {}
//			}
//			try {
//				tmpFile.delete();
//			} catch (Exception e) {}
//			
//			outputSet = null;
//		}
	}
    
    public static void copyLastModifiedDate(File source, File dest) throws Exception {
    	try {
    		dest.setLastModified(source.lastModified());
		} catch (Exception e) {}
    }
    
    public static void writeImage(OutputStream destination, BufferedImage image, BorderMakerBean b) throws Exception {
    	if(b.getOutputFormat() == OutputFormat.PNG) {
    		writePNG(destination, image, b);
    	} else {
    		writeJPEG(destination, image, b);
    	}
    }
    
    public static void writeJPEG(OutputStream destination, BufferedImage image, BorderMakerBean b) throws Exception {
    	if(image.getType() == BufferedImage.TYPE_INT_ARGB) {
    		BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    		rgb.getGraphics().drawImage(image, 0, 0, null);
    		image.flush();
    		image = rgb;
    	}
    	
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(destination);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
		int quality = b.getJpegQuality();
		if (quality == 100) { // no color subsampling, better quality
			param.setQuality(1f, true);
			param.setHorizontalSubsampling(1, 1);
			param.setHorizontalSubsampling(2, 1);
			param.setVerticalSubsampling(1, 1);
			param.setVerticalSubsampling(2, 1);
		} else {
			float q = quality == 99 ? 1f : (float) (quality / 100.0);
			param.setQuality(q, true);
		}
//		param.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
//		param.setXDensity(100);
//		param.setYDensity(100);
		encoder.encode(image, param);
    }
    
    private static void writePNG(OutputStream destination, BufferedImage image, BorderMakerBean b) throws Exception {
		ImageIO.write(image, "png", destination);
    }
    
	public static void saveAsImage(JDialog parent, File sourceFile, File destinationFile, BufferedImage image, BorderMakerBean b) throws Exception {
//		FileOutputStream fos = null;
//		BufferedOutputStream bos = null;
//		try {
//			fos = new FileOutputStream(destinationFile);
//			bos = new BufferedOutputStream(fos);
//			JpegEncoder enc = new JpegEncoder(image, b.getJpegQuality(), bos);
//			enc.Compress();
//		} finally {
//			try{fos.close();}catch(Exception e){};
//			try{bos.close();}catch(Exception e){};
//		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destinationFile);
			writeImage(fos, image, b);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {}
		}
		
		if(b.getOutputFormat() == OutputFormat.JPG) {
			if(b.isCopyExif()) {
				try {
					copyExif(sourceFile, destinationFile, b);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
		
		copyLastModifiedDate(sourceFile, destinationFile);
		/*
		if(b.isPostCommandEnabled()) {
			CommandErrorDialog errordlg = null;
			if(parent == null) {
				errordlg = new CommandErrorDialog(BorderMaker.mainFrame);
			} else {
				errordlg = new CommandErrorDialog(parent);
			}
			
			String command = b.getPostCommand();
			command = SemanticaUtil.replaceIgnoreCase(command, "${src_file}", sourceFile.getCanonicalPath());
			command = SemanticaUtil.replaceIgnoreCase(command, "${dest_file}", destinationFile.getCanonicalPath());

			try {
				Runtime runtime = Runtime.getRuntime();
				Process process = runtime.exec(command);
				BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream()));

				errordlg.appendTextGreen(command + "\n\n");
				
				String nextLine = null;
				while ((nextLine = error.readLine()) != null) {
					errordlg.appendTextRed(nextLine + "\n");
				}
				
				while ((nextLine = out.readLine()) != null) {
					errordlg.appendTextBlue(nextLine + "\n");
				}
	
				if(process.waitFor() != 0) {
					errordlg.pack();
					errordlg.setLocationRelativeTo(null);
					errordlg.go();
					if(errordlg.getClicked() == CommandErrorDialog.NO) {
						throw new InterruptedException();
					}
				}
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				errordlg.appendTextRed(e.getMessage() + "\n");
				errordlg.pack();
				errordlg.setLocationRelativeTo(BorderMaker.mainFrame);
				errordlg.go();
				if(errordlg.getClicked() == CommandErrorDialog.NO) {
					throw new InterruptedException();
				}
			}
		}
		 */
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void lostOwnership(Clipboard arg0, Transferable arg1) {
	}

	public String getTitle() {
		return title;
	}

	public boolean isAskForTitle() {
		return askForTitle;
	}

	public void setAskForTitle(boolean askForTitle) {
		this.askForTitle = askForTitle;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}

