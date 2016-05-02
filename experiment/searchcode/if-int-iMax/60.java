import java.util.* ;

public class SongBinarySearch {
	public SongBinarySearch() {
	
	}
	
	public Song binarySearch(ArrayList<Song> s, String tag, String key, int imin, int imax) {
		if (imax < imin){
			return null;
		} else {
			int imid = (imin + imax) / 2;
			if (s.get(imid).getSomething(tag).compareTo(key) > 0){
				//System.out.println(s.get(imid).getSomething(tag));
				return binarySearch(s, tag, key, imin, imid - 1);
			} else if (s.get(imid).getSomething(tag).compareTo(key) < 0) {
				//System.out.println(s.get(imid).getSomething(tag));
				return binarySearch(s, tag, key, imid + 1, imax);
			} else {
				return s.get(imid);
			}
		}
	}
}
