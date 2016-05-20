/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
package ru.adv.util.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import ru.adv.util.ErrorCodeException;

/**
 * It resizes images
 * @author vic
 *
 */
public class ImageResizer implements InitializingBean {

	private String outputDirectory;

	public ImageResizer() {
	}

	public ImageResizer(String tmpDirectory) {
		super();
		this.outputDirectory = tmpDirectory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(getOutputDirectory(), "Property outputDirectory is not set");
		// check for directory
		File tmpFile = new File(getOutputDirectory()); 
		Assert.isTrue( tmpFile.isDirectory() , "File "+tmpFile+" is not directory");
		Assert.isTrue( tmpFile.canWrite() , "Directory "+tmpFile+" is not writable");
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}
	public void setOutputDirectory(String tmpDirectory) {
		this.outputDirectory = tmpDirectory;
	}

	/**
	 * 
	 * If image need to be resized, it creates new resized Image as temporary file in directory {@link #getOutputDirectory()} 
	 * 
	 * @param srcImage image to resize
	 * @param width
	 * @param height
	 * @return srcImage if file is not changed
	 */
	public File resize(File srcImage, int newWidth, int newHeight) throws IOException {
		Dimension dim = getImageSize(srcImage.getAbsolutePath());
		Scales scale = calculateScalesTo(dim.width, dim.height, newWidth, newHeight);
		if (scale.isResizingNeed()) { 
			File resizedImage = createTempFile(srcImage, StringUtils.getFilenameExtension(srcImage.getAbsolutePath()) );
			createScaledImage(srcImage,	resizedImage,	scale);
			return resizedImage;
		}
		return srcImage; // image file is not changed
	}
	
	/**
	 * Resize image file to dstFile if it's need 
	 * @param srcImage image to resize
	 * @param dstFile file path to resized image if it will be created
	 * @param newWidth
	 * @param newHeight
	 * @return true if resized file was created
	 * @throws IOException
	 */
	public static boolean resize(File srcImage, String dstFile, int newWidth, int newHeight) throws IOException {
		Dimension dim = getImageSize(srcImage.getAbsolutePath());
		Scales scale = calculateScalesTo(dim.width, dim.height, newWidth, newHeight);
		if (scale.isResizingNeed()) { 
			File resizedImage = new File(dstFile);
			createScaledImage(srcImage,	resizedImage,	scale);
			return true;
		}
		return false; // image file is not changed
	}
	

	public static Dimension getImageSize(String filename) throws IOException {
		FileInputStream in = new FileInputStream(filename);
		ImageInputStream iis = ImageIO.createImageInputStream(in);
		ImageReader imageReader = ImageIO.getImageReaders( iis ).next();
		if (imageReader==null) {
			throw new ErrorCodeException("ImageReader is not found for file: "+ filename);
		}
		try {
			imageReader.setInput(iis);
			return new Dimension(imageReader.getWidth(0),imageReader.getHeight(0));
		} finally {
			imageReader.dispose();
			iis.close();
		}
	}

	//~ Private methoths ================================================================

	private static final float ONE = 1.0F;
	private static final float ZERO = 0.0F;


	public static Scales calculateScalesTo(long originalWidth, long originalHeight, long newWidth, long newHeight) {

		Scales s = new Scales();

		if (originalWidth > 0) {
			s.xScale = (float)newWidth / (float)originalWidth;
		}
		if (originalHeight > 0) {
			s.yScale = (float)newHeight / (float)originalHeight;
		}

		if (s.xScale != ZERO && s.yScale != ZERO) {
			double scale = s.xScale < s.yScale ? s.xScale : s.yScale;
			s.xScale = scale;
			s.yScale = scale;
		} else {
			s.xScale = s.xScale == ZERO ? ONE : s.xScale;
			s.yScale = s.yScale == ZERO ? ONE : s.yScale;
		}

		s.xScale = s.xScale > ONE ? ONE : s.xScale;
		s.yScale = s.yScale > ONE ? ONE : s.yScale;
		return s;

	}

	private static void createScaledImage(File srcFile, File dstFile, Scales scale) throws IOException {
		BufferedImage resizedImage = null;
		FileInputStream in = new FileInputStream(srcFile);
		ImageInputStream iis = ImageIO.createImageInputStream(in);
		ImageReader imageReader = ImageIO.getImageReaders( iis ).next();
		if (imageReader==null) {
			throw new ErrorCodeException("ImageReader is not found for file: "+ srcFile);
		}
		try {
			imageReader.setInput(iis);
			BufferedImage buffImage = imageReader.read(0);
			AffineTransform tx = new AffineTransform();
			tx.scale(scale.xScale, scale.yScale);
			int type = buffImage.getType()==0 ? BufferedImage.TYPE_INT_ARGB_PRE : buffImage.getType(); 
			resizedImage = new BufferedImage( 
					(int)Math.round( buffImage.getWidth()  * scale.xScale ),
					(int)Math.round( buffImage.getHeight() * scale.yScale ),
					type
			) ;
			final Graphics2D g = resizedImage.createGraphics();
			AffineTransformOp aop = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);
			g.drawImage(buffImage, aop, 0, 0);
			g.dispose();
		} finally {
			imageReader.dispose();
			iis.close();
		}
		Assert.notNull(resizedImage, "Cannot resize image");
		encode(resizedImage, srcFile, dstFile);
	}
	
	private static void encode(BufferedImage img, File srcFile, File dstFile) throws IOException {
		String ext = StringUtils.getFilenameExtension(srcFile.getName());
		ImageWriter imageWriter = ImageIO.getImageWritersBySuffix(ext).next();
		Assert.notNull(imageWriter, "Can't create ImageWriter for "+srcFile);
		ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(dstFile));
		imageWriter.setOutput(ios);
		imageWriter.write( img );
		imageWriter.dispose();
		ios.close();
	}

	private File createTempFile(File srcFile, String fileExt) throws IOException {
		String name = StringUtils.stripFilenameExtension(srcFile.getName()).trim();
		if (name.length()<3) {
			name+="_img";
		}
		return File.createTempFile(name, "."+fileExt, new File(getOutputDirectory()));
    }
	
	private static class Scales {
		double xScale = ZERO;
		double yScale = ZERO;
		boolean isResizingNeed() {
			return xScale!=ONE || yScale!=ONE; 
		}
	}
      
}

