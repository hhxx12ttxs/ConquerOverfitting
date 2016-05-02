import java.io.File;
import java.io.IOException;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.AbstractID3v2;



public class MediaSorter {
	String title = "untitled";
    String album = "unalbumed";
    int year = 0;
    String artist = "unartisted";
	
	public MediaSorter(File file) {
		//filename a trier
        // create an MP3File object representing our chosen file
        
		getInfo(file);
		album = trimString(album);
		title = trimString(title);
		artist = trimString(artist);
		
		if (title.equals("untitled")){
			System.out.println("No ID3 found : " + file.toString());
			return;
		}
		
		//Artist
		File dir = null;
        if (year != 0){
        	dir = new File(file.getAbsoluteFile().getParent() + "/music/" + artist + "/"+ album + "-" + year);
        }else{
        	dir = new File(file.getAbsoluteFile().getParent() + "/music/" + artist + "/"+ album );
        }
        
        if (!dir.exists())
        	dir.mkdirs();
        
        File ne = new File(dir, title + ".mp3");
        if (ne.exists())
        	ne.delete();
        boolean success = file.renameTo(ne);
        if (success ){
        	System.out.println("-> SORTED : " + ne);
        }else{
        	System.out.println("-> ERROR SORTING : " + ne);
        }
		
	}

	private void getInfo(File file) {
		try {
			MP3File mp3file = new MP3File(file);
			if (mp3file.getID3v2Tag() != null){
				title = cleanString(mp3file.getID3v2Tag().getSongTitle());
				if (title.equals(""))
					title = cleanFrame(mp3file.getID3v2Tag(), "TT2");
				album = cleanString(mp3file.getID3v2Tag().getAlbumTitle());
				if (album.equals(""))
					album = cleanFrame(mp3file.getID3v2Tag(), "TAL");
				artist = cleanString(mp3file.getID3v2Tag().getLeadArtist());
				if (artist.equals(""))
					artist = cleanFrame(mp3file.getID3v2Tag(), "TP1");
				
				try{
					if (!mp3file.getID3v2Tag().getYearReleased().equals("")){
						year = Integer.parseInt(mp3file.getID3v2Tag().getYearReleased());
					}else if (!mp3file.getID3v2Tag().getFrame("TYE").getBody().toString().equals("")){
							year = Integer.parseInt(cleanFrame(mp3file.getID3v2Tag(), "TYE"));
					}
				}catch (Exception e) {
					System.out.println("Year error");
				}

			}else if (mp3file.getID3v1Tag() != null){
				title = mp3file.getID3v1Tag().getSongTitle();
				album = mp3file.getID3v1Tag().getAlbumTitle();
				artist = mp3file.getID3v1Tag().getLeadArtist();
				try{
					if (!mp3file.getID3v1Tag().getYearReleased().equals(""))
						year = Integer.parseInt(mp3file.getID3v1Tag().getYearReleased());
				}catch (Exception e) {
					System.out.println("Year error");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TagException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String cleanFrame(AbstractID3v2 frame, String param){
		String c = frame.getFrame(param).getBody().toString().substring("??TT2 : ".length());
		c = c.trim();
		return c;
	}
	
	public String cleanString(String c){
		String r = "";
		for(byte ch: c.trim().getBytes()){
			if (ch > 0){
				r += String.valueOf((char)ch);
			}
		}
		return r;
	}
	
	public String trimString(String c){
		c = c.trim().replace("/", "-");
		c = c.replace("\\", "-");
		c = c.replace("?", "");
		c = c.replace("*", "");
		c = c.replace("|", "");
		c = c.replace(":", "");
		return c;
	}
	
}

