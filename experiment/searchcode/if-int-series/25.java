package org.kaffeezusatz.episodemovelet.helper;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

public class Episode {
	
	private String series;
	private String title;
	
	private Integer season;
	private Integer episode;

	private File file;
	
	public Episode(final String series, final Integer season, final String title, final Integer episode) {
		this.season = season;
		this.episode = episode;
		
		this.series = series;
		this.title = title;
	}
	
	public Episode(final Integer season, final Integer episode) {
		this.season = season;
		this.episode = episode;
	}

	public Integer getSeason() {
		return season;
	}
	
	public void setSeason(final Integer season) {
		this.season = season;
	}

	public Integer getEpisode() {
		return episode;
	}
	
	public void setEpisode(final Integer episode) {
		this.episode = episode;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
	/**
	 * <p>Following place holder variables are available.<br/>
	 * <b>%series%, %title%, %season%, %episode%</b></p>
	 * 
	 * @param pattern
	 * @return
	 */
	public File getToFile(final File parent, final String pattern) {
		String file = toString(pattern);
		
		//add previous file extension
		file += "." + FilenameUtils.getExtension(getFile().getName());

		return new File(parent, file);
	}
	
	private static List<String> ignore;
	static {
		ignore = new ArrayList<String>();
		ignore.add("avi");
		ignore.add("hdtv");
		ignore.add("xvid");
		ignore.add("fqm");
		ignore.add("asap");
		ignore.add("river");
		ignore.add("fov");
		ignore.add("lol");
		ignore.add("vtv");
		ignore.add("dl");
		ignore.add("german");
		ignore.add("dvdrip");
		ignore.add("ws");
		ignore.add("tvr");
		ignore.add("ac3");
		ignore.add("mp4");
		ignore.add("torrent");
		ignore.add("pdtv");
		ignore.add("carat");
		ignore.add("tvs");
		ignore.add("ithd");
		ignore.add("ded");
		ignore.add("xv");
		ignore.add("eztv");
		ignore.add("x264");
		ignore.add("mkv");
	}
	
	private static DecimalFormat df = new DecimalFormat("00");
	private static String formatNumber(Integer i) {
		return df.format(i);
	}

	protected String toString(final String pattern) {
		String file = pattern;
		
		file = file.replace("%series%", getSeries());
		file = file.replace("%title%", getTitle());
		file = file.replace("%season%", formatNumber(getSeason()));
		file = file.replace("%episode%", formatNumber(getEpisode()));
		file = file.trim();
		
		return file;
	}
	
	public String toString() {
		return toString("%series%-%title%-%season%-%episode%");
	}

	public static Episode parse(File file) {
		if (file == null) {
			throw new IllegalArgumentException(file + " can't be null");
		}
		
		String name = EpisodeHelper.normalizeFileName(file);
		String[] parts = name.split(" ");
		
		List<String> series = new ArrayList<String>();
		List<String> title = new ArrayList<String>();
		String episodeString = null;

		for (String part : parts) {
			if (ignore.contains(part)) {
				if ((!series.isEmpty() || !title.isEmpty()) && episodeString != null) {
					break;
				} else {
					continue;
				}
			} else if (EpisodeHelper.isWord(part) || (part.length() < 3 && episodeString == null)) {
				if (series.size() == 0 || episodeString == null) {
					series.add(part);
				} else {
					title.add(part);
				}
			} else if (episodeString == null) {
				episodeString = part;
			}
		}

		final Episode episode = parseEpisodeString(episodeString);
		if (episode == null) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " can't be parsed");
		}
		
		if (series.size() == 0) {
			final File parent = file.getParentFile();
			if (parent.getName().contains(String.valueOf(episode.getSeason()))) {
				if (parent.getName().length() < 4) {
					series.add(parent.getParentFile().getName());
				}
			}
		}
		
		episode.setFile(file);
		episode.setSeries(EpisodeHelper.capitalizeWords(series));
		episode.setTitle(EpisodeHelper.capitalizeWords(title));
		
		return episode;
	}
	
	private static Episode parseEpisodeString(String string) {
		if (string == null) {
			return null;
		}
		string = string.toLowerCase();
		
		if (string.matches(".*[e|x]{1}[0-9]{1}")) {
			string = string.replaceAll("[e]", "e0");
			string = string.replaceAll("[x]", "x0");
		}
		
		string = string.replaceAll("[^\\d]", "");
		
		if (string.isEmpty()) {
			return null;
		}
		
		if (string.length() == 3 && !string.startsWith("0")) {
			string = "0" + string;
		} else if (string.length() == 2 && !string.contains("0")) {
			string = "0" + string.charAt(0) + "0" + string.charAt(1);
		}
		
		Episode result = null;

		Pattern pattern = Pattern.compile("([\\d]{2})([\\d]{2})");
		Matcher matcher = pattern.matcher(string);

		if (!matcher.find() || matcher.groupCount() != 2) {
			return null;
		}
			
		try {
			Integer season = Integer.parseInt(matcher.group(1));
			Integer episode = Integer.parseInt(matcher.group(2));
			
			result = new Episode(season, episode);
		} catch (NumberFormatException e) {
			return null;
		} catch (IllegalStateException e) {
			return null;
		}
		
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((episode == null) ? 0 : episode.hashCode());
		result = prime * result + ((season == null) ? 0 : season.hashCode());
		result = prime * result + ((series == null) ? 0 : series.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Episode)) {
			return false;
		}
		Episode other = (Episode) obj;
		if (episode == null) {
			if (other.episode != null) {
				return false;
			}
		} else if (!episode.equals(other.episode)) {
			return false;
		}
		if (season == null) {
			if (other.season != null) {
				return false;
			}
		} else if (!season.equals(other.season)) {
			return false;
		}
		if (series == null) {
			if (other.series != null) {
				return false;
			}
		} else if (!series.equals(other.series)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}
}
