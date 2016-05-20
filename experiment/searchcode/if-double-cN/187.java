package stepmania.chart;

import java.util.ArrayList;

import stepmania.attack.Attack;
import stepmania.enums.StepsType;
import stepmania.enums.song.BPMDisplay;
import stepmania.enums.song.Difficulty;
import stepmania.enums.song.SongDisplay;
import stepmania.timing.TimingData;

public class Song
{
	/**
	 * Should this song always be displayed on the wheel,
	 * or never?
	 * 
	 * Note that this may have no effect depending on options.
	 */
	private SongDisplay display;
	
	/**
	 * How should the BPM be displayed publically?
	 */
	private BPMDisplay bpmDisplay;
	
	/**
	 * The specified minimum BPM.
	 */
	private double specifiedMinBPM;
	/**
	 * The specified maximum BPM.
	 */
	private double specifiedMaxBPM;
	
	/**
	 * Where does this song live?
	 * 
	 * This is not usually written out.
	 */
	private String songPath;
	
	/**
	 * What version is this chart?
	 */
	private double version;
	
	private String credit;
	
	private String origin;
	
	private String genre;
	
	/**
	 * What is the name of this song?
	 */
	private String mainTitle;
	/**
	 * What is the subtitle of this song?
	 */
	private String subTitle;
	/**
	 * Who made this song?
	 */
	private String artist;
	/**
	 * What is the transliterated title of this song?
	 */
	private String mainTitleTranslit;
	/**
	 * What is the transliterated subtitle of this song?
	 */
	private String subTitleTranslit;
	/**
	 * What is the transliterated artist?
	 */
	private String artistTranslit;
	
	private double musicLengthSeconds;
	
	private double sampleStartSeconds;
	
	private double sampleLengthSeconds;
	
	private TimingData timing;
	
	private String bannerPath;
	
	private String backgroundPath;
	
	private String lyricsPath;
	
	private String cdTitlePath;
	
	private String musicPath;
	
	private ArrayList<Step> steps;
	
	private ArrayList<Attack> attacks;
	
	private ArrayList<String> keysounds;
	
	/**
	 * What version are we currently on?
	 */
	private static final double CURRENT_VERSION = 0.80;
	
	public Song()
	{
		setDisplay(SongDisplay.ALWAYS);
		setVersion(CURRENT_VERSION);
		setBPMDisplay(BPMDisplay.DISPLAY_BPM_ACTUAL);
		setTiming(new TimingData());
		steps = new ArrayList<Step>();
		this.setAttacks(new ArrayList<Attack>());
		this.setKeysounds(new ArrayList<String>());
	}
	
	/**
	 * @return the mainTitle
	 */
	public String getMainTitle()
	{
		return mainTitle;
	}
	
	/**
	 * @param mainTitle the mainTitle to set
	 */
	public void setMainTitle(String mainTitle)
	{
		this.mainTitle = mainTitle;
	}
	
	/**
	 * @return the subTitle
	 */
	public String getSubTitle()
	{
		return subTitle;
	}
	
	/**
	 * @param subTitle the subTitle to set
	 */
	public void setSubTitle(String subTitle)
	{
		this.subTitle = subTitle;
	}
	
	/**
	 * @return the artist
	 */
	public String getArtist()
	{
		return artist;
	}
	
	/**
	 * @param artist the artist to set
	 */
	public void setArtist(String artist)
	{
		this.artist = artist;
	}
	
	/**
	 * @return the version
	 */
	public double getVersion()
	{
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(double version)
	{
		this.version = version;
	}
	
	/**
	 * @return the songPath
	 */
	public String getSongPath()
	{
		return songPath;
	}
	
	/**
	 * @param songPath the songPath to set
	 */
	public void setSongPath(String songPath)
	{
		this.songPath = songPath;
	}
	
	/**
	 * @return the display
	 */
	public SongDisplay getDisplay()
	{
		return display;
	}
	
	/**
	 * @param display the display to set
	 */
	public void setDisplay(SongDisplay display)
	{
		this.display = display;
	}
	
	/**
	 * @return the mainTitleTranslit
	 */
	public String getMainTitleTranslit()
	{
		if (mainTitleTranslit != null && mainTitleTranslit.length() > 0)
		{
			return mainTitleTranslit;
		}
		return mainTitle;
	}
	
	/**
	 * @param mainTitleTranslit the mainTitleTranslit to set
	 */
	public void setMainTitleTranslit(String mainTitleTranslit)
	{
		this.mainTitleTranslit = mainTitleTranslit;
	}
	
	/**
	 * @return the subTitleTranslit
	 */
	public String getSubTitleTranslit()
	{
		if (subTitleTranslit != null && subTitleTranslit.length() > 0)
		{
			return subTitleTranslit;
		}
		return subTitle;
	}
	
	/**
	 * @param subTitleTranslit the subTitleTranslit to set
	 */
	public void setSubTitleTranslit(String subTitleTranslit)
	{
		this.subTitleTranslit = subTitleTranslit;
	}
	
	/**
	 * @return the artistTranslit
	 */
	public String getArtistTranslit()
	{
		if (artistTranslit != null && artistTranslit.length() > 0)
		{
			return artistTranslit;
		}
		return artist;
	}
	
	/**
	 * @param artistTranslit the artistTranslit to set
	 */
	public void setArtistTranslit(String artistTranslit)
	{
		this.artistTranslit = artistTranslit;
	}
	
	/**
	 * @return the bpmDisplay
	 */
	public BPMDisplay getBPMDisplay()
	{
		return bpmDisplay;
	}
	
	/**
	 * @param bpmDisplay the bpmDisplay to set
	 */
	public void setBPMDisplay(BPMDisplay bpmDisplay)
	{
		this.bpmDisplay = bpmDisplay;
	}
	
	/**
	 * @return the specifiedMinBPM
	 */
	public double getSpecifiedMinBPM()
	{
		return specifiedMinBPM;
	}
	
	/**
	 * @param specifiedMinBPM the specifiedMinBPM to set
	 */
	public void setSpecifiedMinBPM(double specifiedMinBPM)
	{
		this.specifiedMinBPM = specifiedMinBPM;
	}
	
	/**
	 * @return the specifiedMaxBPM
	 */
	public double getSpecifiedMaxBPM()
	{
		return specifiedMaxBPM;
	}
	
	/**
	 * @param specifiedMaxBPM the specifiedMaxBPM to set
	 */
	public void setSpecifiedMaxBPM(double specifiedMaxBPM)
	{
		this.specifiedMaxBPM = specifiedMaxBPM;
	}
	
	/**
	 * @return the credit
	 */
	public String getCredit()
	{
		return credit;
	}
	
	/**
	 * @param credit the credit to set
	 */
	public void setCredit(String credit)
	{
		this.credit = credit;
	}
	
	/**
	 * @return the origin
	 */
	public String getOrigin()
	{
		return origin;
	}
	
	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin)
	{
		this.origin = origin;
	}
	
	/**
	 * @return the genre
	 */
	public String getGenre()
	{
		return genre;
	}
	
	/**
	 * @param genre the genre to set
	 */
	public void setGenre(String genre)
	{
		this.genre = genre;
	}
	
	/**
	 * @return the musicLengthSeconds
	 */
	public double getMusicLengthSeconds()
	{
		return musicLengthSeconds;
	}
	
	/**
	 * @param musicLengthSeconds the musicLengthSeconds to set
	 */
	public void setMusicLengthSeconds(double musicLengthSeconds)
	{
		this.musicLengthSeconds = musicLengthSeconds;
	}
	
	/**
	 * @return the sampleStartSeconds
	 */
	public double getSampleStartSeconds()
	{
		return sampleStartSeconds;
	}
	
	/**
	 * @param sampleStartSeconds the sampleStartSeconds to set
	 */
	public void setSampleStartSeconds(double sampleStartSeconds)
	{
		this.sampleStartSeconds = sampleStartSeconds;
	}
	
	/**
	 * @return the sampleLengthSeconds
	 */
	public double getSampleLengthSeconds()
	{
		return sampleLengthSeconds;
	}
	
	/**
	 * @param sampleLengthSeconds the sampleLengthSeconds to set
	 */
	public void setSampleLengthSeconds(double sampleLengthSeconds)
	{
		this.sampleLengthSeconds = sampleLengthSeconds;
	}
	
	/**
	 * @return the timing
	 */
	public TimingData getTiming()
	{
		return timing;
	}
	
	/**
	 * @param timing the timing to set
	 */
	public void setTiming(TimingData timing)
	{
		this.timing = timing;
	}
	
	/**
	 * @return the bannerPath
	 */
	public String getBannerPath()
	{
		return bannerPath;
	}
	
	/**
	 * @param bannerPath the bannerPath to set
	 */
	public void setBannerPath(String bannerPath)
	{
		this.bannerPath = bannerPath;
	}
	
	/**
	 * @return the backgroundPath
	 */
	public String getBackgroundPath()
	{
		return backgroundPath;
	}
	
	/**
	 * @param backgroundPath the backgroundPath to set
	 */
	public void setBackgroundPath(String backgroundPath)
	{
		this.backgroundPath = backgroundPath;
	}
	
	/**
	 * @return the lyricsPath
	 */
	public String getLyricsPath()
	{
		return lyricsPath;
	}
	
	/**
	 * @param lyricsPath the lyricsPath to set
	 */
	public void setLyricsPath(String lyricsPath)
	{
		this.lyricsPath = lyricsPath;
	}
	
	/**
	 * @return the cdTitlePath
	 */
	public String getCDTitlePath()
	{
		return cdTitlePath;
	}
	
	/**
	 * @param cdTitlePath the cdTitlePath to set
	 */
	public void setCDTitlePath(String cdTitlePath)
	{
		this.cdTitlePath = cdTitlePath;
	}
	
	/**
	 * @return the musicPath
	 */
	public String getMusicPath()
	{
		return musicPath;
	}
	
	/**
	 * @param musicPath the musicPath to set
	 */
	public void setMusicPath(String musicPath)
	{
		this.musicPath = musicPath;
	}
	
	/**
	 * @return the steps
	 */
	public ArrayList<Step> getSteps()
	{
		return steps;
	}
	
	public ArrayList<Step> getSteps(StepsType st)
	{
		ArrayList<Step> type = new ArrayList<Step>(5);
		for (Step s : steps)
		{
			if (s.getStepsType() == st)
			{
				type.add(s);
			}
		}
		return type;
	}
	
	/**
	 * @param steps the steps to set
	 */
	public void setSteps(ArrayList<Step> steps)
	{
		this.steps = steps;
	}
	
	public void addStep(Step step)
	{
		this.getSteps().add(step);
	}
	
	/**
	 * @return the attacks
	 */
	public ArrayList<Attack> getAttacks()
	{
		return attacks;
	}
	
	/**
	 * @param attacks the attacks to set
	 */
	public void setAttacks(ArrayList<Attack> attacks)
	{
		this.attacks = attacks;
	}
	
	public boolean hasSteps()
	{
		return !this.getSteps().isEmpty();
	}
	
	public static ArrayList<Step> GetSteps(final Song s,
			StepsType st, Difficulty dc, int meterLow,
			int meterHigh, String desc, String credit, int max)
			{
		if (max == 0) return null;
		ArrayList<Step> analyze = (st == null) ? s.getSteps() : s.getSteps(st);
		ArrayList<Step> ret = new ArrayList<Step>(max);
		for (Step p : analyze)
		{
			if (dc != null && dc != p.getDifficulty())
				continue;
			if (meterLow != -1 && meterLow > p.getMeter())
				continue;
			if (meterHigh != -1 && meterHigh < p.getMeter())
				continue;
			if (desc != null && desc.equals(p.getDescription()))
				continue;
			if (credit != null && credit.equals(p.getCredit()))
				continue;
			
			ret.add(p);
		}
		
		return ret;
	}
	
	
	public static Step GetOneSteps(final Song s)
	{
		return Song.GetOneSteps(s, null);
	}
	
	public static Step GetOneSteps(final Song s, StepsType st)
	{
		return Song.GetOneSteps(s, st, null);
	}
	
	public static Step GetOneSteps(final Song s,
			StepsType st, Difficulty dc)
	{
		return Song.GetOneSteps(s, st, dc, -1);
	}
	
	public static Step GetOneSteps(final Song s,
			StepsType st, Difficulty dc, int meterLow)
	{
		return Song.GetOneSteps(s, st, dc, meterLow, -1);
	}
	
	public static Step GetOneSteps(final Song s,
			StepsType st, Difficulty dc, int meterLow,
			int meterHigh)
	{
		return Song.GetOneSteps(s, st, dc, meterLow, meterHigh, "");
	}
	
	public static Step GetOneSteps(final Song s,
			StepsType st, Difficulty dc, int meterLow,
			int meterHigh, String desc)
	{
		return Song.GetOneSteps(s, st, dc, meterLow, meterHigh, desc, "");
	}
	
	public static Step GetOneSteps(final Song s,
			StepsType st, Difficulty dc, int meterLow,
			int meterHigh, String desc, String credit)
	{
		ArrayList<Step> gotten = Song.GetSteps(s, st, dc,
				meterLow, meterHigh, desc, credit, 1);
		return (gotten.size() > 0) ? gotten.get(0) : null;
	}
	
	public static boolean isDescriptionUnique(final Song s,
			StepsType st, String cn, Step exclude)
	{
		for (Step p : s.getSteps(st))
		{
			if (p.equals(exclude))
				continue;
			if (p.getDescription().equals(cn))
				return false;
		}
		return true;
	}
	
	public static boolean isChartNameUnique(final Song s,
			StepsType st, String cn, Step exclude)
	{
		for (Step p : s.getSteps(st))
		{
			if (p.equals(exclude))
				continue;
			if (p.getChartName().equals(cn))
				return false;
		}
		return true;
	}
	
	/**
	 * @return the keysounds
	 */
	public ArrayList<String> getKeysounds()
	{
		return keysounds;
	}
	
	/**
	 * @param keysounds the keysounds to set
	 */
	public void setKeysounds(ArrayList<String> keysounds)
	{
		this.keysounds = keysounds;
	}
}

