package com.truyen.utils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import com.google.gdata.client.Query;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaByteArraySource;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.GphotoEntry;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.AuthenticationException;
import com.truyen.persistence.entity.LoaiLog;
import com.truyen.service.LoaiLogService;
import com.truyen.service.LogOtherService;

/**
 * Up anh len picasa
 * http://truyenz.com
 * @author Taanzaza
 */
public class PicasawebAlbumUtil {
	
	@Autowired
	private LoaiLogService loaiLogService;
	
	@Autowired
	private LogOtherService logOtherService;
	
	private LoaiLog loaiLog = loaiLogService.findByProperty("nameUnsigned","upload image picasa");
	
	private static final String API_PREFIX = "https://picasaweb.google.com/data/feed/api/user/";
	
	public static String uploadImageFromUrl(byte[] imageBytes) throws Exception{
		String rs = "";
		PicasawebService myService = new PicasawebService("zaza");
		try {
			myService.setUserCredentials("truyenz.com@gmail.com", "11062010");
			//khai bao url album picasa dua theo user va albumid cua picasa. album avatar1
			URL albumPostUrl = new URL("https://picasaweb.google.com/data/feed/api/user/103555358382977699974/albumid/5789504264010875409");
			PhotoEntry myPhoto = new PhotoEntry();
			//dat ten cho tam anh khi up len picasa
			myPhoto.setTitle(new PlainTextConstruct("avatar-truyenzdotcom"));
			//chuyen doi anh thanh .jpg
			MediaByteArraySource myMedia = new MediaByteArraySource(imageBytes, "image/jpeg");
			myPhoto.setMediaSource(myMedia);
			//tien hanh upload anh len picasa
			PhotoEntry returnedPhoto = myService.insert(albumPostUrl, myPhoto);
			String newSrc = returnedPhoto.getHtmlLink().getHref();
			if (returnedPhoto.getMediaContents().size() > 0) {
				newSrc = returnedPhoto.getMediaContents().get(0).getUrl();
				int endNewSrc = newSrc.lastIndexOf("/");
				String urlGooglePicasa = newSrc.substring(0, endNewSrc);
				urlGooglePicasa = urlGooglePicasa.replace("https://lh3.googleusercontent.com", "http://1.bp.blogspot.com")
				.replace("https://lh4.googleusercontent.com", "http://2.bp.blogspot.com")
				.replace("https://lh5.googleusercontent.com", "http://3.bp.blogspot.com")
				.replace("https://lh6.googleusercontent.com", "http://4.bp.blogspot.com")
				.replace("lh3.ggpht.com", "1.bp.blogspot.com")
				.replace("lh4.ggpht.com", "2.bp.blogspot.com")
				.replace("lh5.ggpht.com", "3.bp.blogspot.com")
				.replace("lh6.ggpht.com", "4.bp.blogspot.com");
				rs = urlGooglePicasa;
			}//ket thuc if (returnedPhoto.getMediaContents().size() > 0) {
		} catch (AuthenticationException e1) {
//			logOtherService.writeLog("ERROR - K?t n?i v?i picasa","K?t n?i v?i picasa b? l?i",loaiLog,true);
			e1.printStackTrace();
			return rs;
		}//ket thuc try catch
		return rs;
	}
	
	public static String uploadImagePicasa(String linkImage,String gmail, String pwd, String urlAlbum) throws Exception{
		String rs = "";
		PicasawebService myService = new PicasawebService("zaza");
		try {
			linkImage = linkImage.replace(" ", "%20");
			URL fileURL = new URL(linkImage);
			URLConnection urlConn = fileURL.openConnection();
		    urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
		    InputStream inputStream = urlConn.getInputStream();
			byte[] imageBytes = IOUtils.toByteArray(inputStream);
			myService.setUserCredentials(gmail, pwd);
			//khai bao url album picasa dua theo user va albumid cua picasa. album avatar1
			URL albumPostUrl = new URL(urlAlbum);
			PhotoEntry myPhoto = new PhotoEntry();
			//dat ten cho tam anh khi up len picasa
			myPhoto.setTitle(new PlainTextConstruct("avatar-truyenzdotcom"));
			//chuyen doi anh thanh .jpg
			MediaByteArraySource myMedia = new MediaByteArraySource(imageBytes, "image/jpeg");
			myPhoto.setMediaSource(myMedia);
			//tien hanh upload anh len picasa
			PhotoEntry returnedPhoto = myService.insert(albumPostUrl, myPhoto);
			String newSrc = returnedPhoto.getHtmlLink().getHref();
			if (returnedPhoto.getMediaContents().size() > 0) {
				newSrc = returnedPhoto.getMediaContents().get(0).getUrl();
				int endNewSrc = newSrc.lastIndexOf("/");
				String urlGooglePicasa = newSrc.substring(0, endNewSrc);
				urlGooglePicasa = urlGooglePicasa.replace("https://lh3.googleusercontent.com", "http://1.bp.blogspot.com")
				.replace("https://lh4.googleusercontent.com", "http://2.bp.blogspot.com")
				.replace("https://lh5.googleusercontent.com", "http://3.bp.blogspot.com")
				.replace("https://lh6.googleusercontent.com", "http://4.bp.blogspot.com")
				.replace("lh3.ggpht.com", "1.bp.blogspot.com")
				.replace("lh4.ggpht.com", "2.bp.blogspot.com")
				.replace("lh5.ggpht.com", "3.bp.blogspot.com")
				.replace("lh6.ggpht.com", "4.bp.blogspot.com");
				rs = urlGooglePicasa;
			}//ket thuc if (returnedPhoto.getMediaContents().size() > 0) {
		} catch (AuthenticationException e1) {
//			logOtherService.writeLog("ERROR - K?t n?i v?i picasa","K?t n?i v?i picasa b? l?i",loaiLog,true);
			e1.printStackTrace();
			return rs;
		}//ket thuc try catch
		return rs;
	}
	
	public static String uploadImagePicasa(String linkImage,PicasawebService myService, String urlAlbum) throws Exception{
		String rs = "";
		try {
//			Thread.sleep(5000);
			linkImage = linkImage.replace(" ", "%20");
			URL fileURL = new URL(linkImage);
			URLConnection urlConn = fileURL.openConnection();
		    urlConn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:16.0) Gecko/20100101 Firefox/16.0");
		    InputStream inputStream = urlConn.getInputStream();
			byte[] imageBytes = IOUtils.toByteArray(inputStream);
			//khai bao url album picasa dua theo user va albumid cua picasa. album avatar1
			URL albumPostUrl = new URL(urlAlbum);
			PhotoEntry myPhoto = new PhotoEntry();
			//dat ten cho tam anh khi up len picasa
			myPhoto.setTitle(new PlainTextConstruct("avatar-truyenzdotcom"));
			//chuyen doi anh thanh .jpg
			MediaByteArraySource myMedia = new MediaByteArraySource(imageBytes, "image/jpeg");
			myPhoto.setMediaSource(myMedia);
			//tien hanh upload anh len picasa
			PhotoEntry returnedPhoto = myService.insert(albumPostUrl, myPhoto);
			String newSrc = returnedPhoto.getHtmlLink().getHref();
			if (returnedPhoto.getMediaContents().size() > 0) {
				newSrc = returnedPhoto.getMediaContents().get(0).getUrl();
				int endNewSrc = newSrc.lastIndexOf("/");
				String urlGooglePicasa = newSrc.substring(0, endNewSrc);
				urlGooglePicasa = urlGooglePicasa.replace("https://lh3.googleusercontent.com", "http://1.bp.blogspot.com")
				.replace("https://lh4.googleusercontent.com", "http://2.bp.blogspot.com")
				.replace("https://lh5.googleusercontent.com", "http://3.bp.blogspot.com")
				.replace("https://lh6.googleusercontent.com", "http://4.bp.blogspot.com")
				.replace("lh3.ggpht.com", "1.bp.blogspot.com")
				.replace("lh4.ggpht.com", "2.bp.blogspot.com")
				.replace("lh5.ggpht.com", "3.bp.blogspot.com")
				.replace("lh6.ggpht.com", "4.bp.blogspot.com");
				rs = urlGooglePicasa;
			}//ket thuc if (returnedPhoto.getMediaContents().size() > 0) {
		} catch (AuthenticationException e1) {
//			logOtherService.writeLog("ERROR - K?t n?i v?i picasa","K?t n?i v?i picasa b? l?i",loaiLog,true);
			e1.printStackTrace();
			return rs;
		}//ket thuc try catch
		return rs;
	}
	
	@Async
	public static void delAlbum(PicasawebService myService, String urlAlbum) throws Exception{
		try {
			// Retrieve only the ETag and location attributes for the album to be updated.
			Query patchQuery = new Query(new URL("https://picasaweb.google.com/100656358956806762835/NamiSunshineWallpaper?authkey=Gv1sRgCNzppqn4uNmf6QE"));
			AlbumEntry partialEntry = myService.getEntry(patchQuery.getUrl(), AlbumEntry.class);
			//xoa album
			partialEntry.delete();
		} catch (AuthenticationException e1) {
//			logOtherService.writeLog("ERROR - K?t n?i v?i picasa","K?t n?i v?i picasa b? l?i",loaiLog,true);
			e1.printStackTrace();
		}//ket thuc try catch
	}
	
	public static List<AlbumEntry> getLstAlbum(String gmail, String pwd) throws Exception{
		PicasawebService myService = new PicasawebService("zaza");
		try {
			myService.setUserCredentials(gmail, pwd);
//			URL feedUrl = new URL("https://picasaweb.google.com/data/feed/api/user/default?kind=album");
//			String urlAlbum = API_PREFIX + gmail;
//			UserFeed myUserFeed = myService.getFeed(urlAlbum, UserFeed.class);
//			for (AlbumEntry myAlbum : myUserFeed.getAlbumEntries()) {
//			    System.out.println(myAlbum.getTitle().getPlainText());
//			}
//            for (GphotoEntry myAlbum : myUserFeed.getEntries()) {
//            	System.out.println(myAlbum.getTitle().getPlainText());
//            }
//            List<GphotoEntry> entries = myUserFeed.getEntries();
//            for (GphotoEntry entry : entries) {
//              GphotoEntry adapted = entry.getAdaptedEntry();
//              if (adapted instanceof AlbumEntry) {
//                albums.add((AlbumEntry) adapted);
//              }
//            }
            PicasawebClient picasaClient = new PicasawebClient(myService);
            List<AlbumEntry> albums = picasaClient.getAlbums(gmail);
            return albums;
		} catch (AuthenticationException e1) {
			e1.printStackTrace();
		}//ket thuc try catch
		return null;
	}
	
}

