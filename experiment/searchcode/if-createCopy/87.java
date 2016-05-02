package titech.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

import titech.action.Action;
import titech.util.ExtArrayList;

public class SnapContainer {
	
	private class Snap implements Comparable<Snap> {
		
		public long time = -1;
		public File snap = null;
		
		public Snap(long time) {
			this.time = time;
		}
		
		public Snap(long time, File snap) {
			this.time = time;
			this.snap = snap;
		}

		@Override
		public int compareTo(Snap rhs) {
			if (this.time < rhs.time) return -1;
			if (this.time == rhs.time) return 0;
			return 1;
		}
		
		public BufferedImage load() {
			BufferedImage ret = null;
			try {
				ret = ImageIO.read(snap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ret;
		}
	}
	
	private ExtArrayList<Snap> snaps = new ExtArrayList<Snap>();
	private BufferedImage cache = null;
	private int cacheIndex = -1;
	private int snapWidth = -1;
	private int snapHeight = -1;

	public SnapContainer(ArrayList<File> snapFiles) {
		for (File f : snapFiles) {
			long time = getTimeFromName(f.getName()) - Action.getBaseTime();
			snaps.add(new Snap(time, f));
		}
		Collections.sort(snaps);
		
		int firstCacheIndex = 0;
		cache = snaps.get(firstCacheIndex).load();
		cacheIndex = firstCacheIndex;
		
		snapWidth = cache.getWidth();
		snapHeight = cache.getHeight();
	}
	
	public BufferedImage getSnap(long time, int width, int height) {
		int i = snaps.lowerBoundDecl(new Snap(time));
		if (i == cacheIndex) {
			return createCopy(cache, width, height);
		} else {
			cache = snaps.get(i).load();
			cacheIndex = i;
			return createCopy(cache, width, height);
		}
	}
	
	public int getWidth() {
		return snapWidth;
	}
	
	public int getHeight() {
		return snapHeight;
	}
	
	private BufferedImage createCopy(BufferedImage image, int width, int height) {
		BufferedImage copy = new BufferedImage(
				snapWidth, snapHeight, BufferedImage.TYPE_INT_RGB);
		copy.getGraphics().drawImage(cache, 0, 0, width, height, null);
		return copy;
	}
	
	// For example, if "1353462063008.suffix", return "1353462063008".
	// This means millisecond time in UTC.
	private long getTimeFromName(String name) {
		return Long.parseLong(name.substring(0, name.indexOf(".")));
	}
}

