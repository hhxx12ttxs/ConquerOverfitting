package org.esgi.java.grabbergui.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.esgi.java.grabber.Grabber;
import org.esgi.java.grabber.Grabber.LimitMethod;
import org.esgi.java.grabber.GrabberListener;
import org.esgi.java.grabbergui.controller.ProjectDestructor;
import org.esgi.java.grabbergui.view.gui.lang.TR;

/**
 *
 * @author gwadaboug
 * @author Julien Massot
 */
public class ProjectGrabber implements GrabberListener{

	//---------------------------------------------------------------------------------------------
	// Static constants
	//---------------------------------------------------------------------------------------------
	public enum State{
		PROJECT_PAUSE,
		PROJECT_RUN,
		PROJECT_END,
		PROJECT_STOP,
		PROJECT_UNKNOWN
	}
	//---------------------------------------------------------------------------------------------

	//-------------------------------pool.pause()--------------------------------------------------------------
	// Private variables
	//---------------------------------------------------------------------------------------------

	private String name;
	private String url;
	private State status;
	private String downloaddir;
	private LimitMethod limitmethod;
	private int limit;
	private final LinkedList<String> recenturl = new LinkedList<String>();
	private ArrayList<FileGrabber> activefiles;
	private Grabber grabber;

	//---------------------------------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------------------------------
	/**
	 * Add project to download with the browse
	 */
	public ProjectGrabber() {
		this._initProjectGrabber("", "", State.PROJECT_PAUSE, "", LimitMethod.FILENUMBER, 500);
	}

	/**
	 *
	 * @param gs
	 */
	public ProjectGrabber(GrabberSerialize gs)
	{
		this.name = gs.getName();
		this.url  = gs.getUrl();
		this.status = State.valueOf(gs.getStatus());
		this.downloaddir = gs.getPath();

		this.limit = gs.getLimit();
		this.limitmethod = LimitMethod.valueOf(gs.getTypeLimit());

		this.grabber = new Grabber(name, this.downloaddir + "/" + this.name, limitmethod, limit);
		this.grabber.register(this);
		this.grabber.history = gs.getDownloaded();

		this.grabber.todownload = gs.getToDownload();

		this.grabber.addtodownload(this.url);
	}

	/**
	 *
	 * Add project to download by giving the project name and the project URL
	 * @param name
	 * @param url
	 */
	public ProjectGrabber(String name, String url) {
		this._initProjectGrabber(name, url, State.PROJECT_PAUSE, "", LimitMethod.FILENUMBER, 500);
	}
	/**
	 *
	 * Add project to download by giving the project name and the project URL
	 * @param name
	 * @param url
	 */
	public ProjectGrabber(String name, String url, String projectdir) {
		this._initProjectGrabber(name, url, State.PROJECT_PAUSE, projectdir, LimitMethod.UNLIMITED, 500);
	}
	/**
	 *
	 * @param name
	 * @param url
	 * @param status
	 */
	public ProjectGrabber(String name, String url, State status, String projectdir,
			LimitMethod limitmethod, int limit) {
		this._initProjectGrabber(name, url, status, projectdir, limitmethod, limit);
	}

	//---------------------------------------------------------------------------------------------
	// Getter and Setter
	//---------------------------------------------------------------------------------------------
	/**
	 *
	 * @return return the name of the project
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @return return the download directory
	 */
	public String getDownloaddir() {
		return downloaddir;
	}

	/**
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @return return the base url to grab
	 */
	public String getUrl() {
		return url;
	}

	/**
	 *
	 * @return a list of files not downloaded for this project
 	 */

	public int getdowloadedfiles() {
		return this.grabber.getFiles();
	}

	/**
	 *
	 * @return the list of files which are downloading
	 */
	public ArrayList<FileGrabber> getActiveFiles() {
		return activefiles;
	}

	/**
	 *
	 * @return a list of files not downloaded for this project
 	 */
	public List<String> getTodownload() {
		return grabber.getTodownload();
	}

	/**
	 *
	 * @return a list of files ever downloaded for this project
 	 */
	public HashMap<String, Date> getHistory() {
		return grabber.getHistory();
	}

	/**
	 * Return the status of the files download
	 * @return
	 */
	public State getStatus() {
		return status;
	}
	/**
	 * Return the status of the download files in string
	 * @return
	 */

	public String getStatusStr() {
		return state2string(status);
	}

	/**
	 * Return the Max thread
	 */
	public int getMaxThread() {
		return grabber.getMaxthread();
	}

	public void setMaxThread(int maxthread) {
		grabber.setMaxthread(maxthread);
	}

	/**
	 * This method permit to change the status of the file download
	 * @param status
	 */
	public void setStatus(State status) {
		if (status == State.PROJECT_PAUSE)
			this.grabber.pause();
		else if (status == State.PROJECT_RUN)
			this.grabber.start();
		else if (status == State.PROJECT_STOP) {
			this.grabber.pause();
		}
		this.status = status;
	}
	/**
	 * this method change the status of the files download
	 * @param status
	 * @return
	 */

	public String state2string(State status) {
		switch (status) {
		case PROJECT_PAUSE:
			return TR.toString("$PG_STATE_PAUSE");
		case PROJECT_RUN:
			return TR.toString("$PG_STATE_RUN");
		case PROJECT_END:
			return TR.toString("$PG_STATE_END");
		case PROJECT_STOP:
			return TR.toString("$PG_STATE_STOP");
		}
		return null;
	}
	/**
	 * this method show the percentage of the download progression
	 * @return
	 */

	public String getProgress() {
		if (this.limitmethod == LimitMethod.FILENUMBER)
			return ((getdowloadedfiles() * 100) / limit) + "%";
		else if (this.limitmethod == LimitMethod.SIZEDOWLOADED)
			return (int) ((getDowloadedsize() *100)/(limit*1024*1024)) + "%";
		return TR.toString("$PG_METHOD_NOLIMIT");
	}
	/**
	 * change the project status in project_pause
	 */

	public void pause() {
		setStatus(State.PROJECT_PAUSE);
		saveProject();
	}
	/**change the project status in project_stop
	 */

	public void stop() {
		setStatus(State.PROJECT_STOP);
		saveProject();
	}
	/**
	 * change the project status in project_run
	 */
	public void start() {
		setStatus(State.PROJECT_RUN);
	}

	/**
	 * change the project status in project_run
	 */
	public void delete() {
		new ProjectDestructor(this);
	}

	/**
	 * This method return the status of the file downloaded
	 * @param status
	 * @return
	 */

	public State string2state(String status) {
		if (status == TR.toString("$PG_STATE_PAUSE"))
			return State.PROJECT_PAUSE;
		else if (status == TR.toString("$PG_STATE_RUN"))
			return State.PROJECT_RUN;
		else if (status == TR.toString("$PG_STATE_END"))
			return State.PROJECT_END;
		else if (status == TR.toString("$PG_STATE_STOP"))
			return State.PROJECT_STOP;
		return State.PROJECT_UNKNOWN;
	}
	/**
	 *
	 */
	@Override
	public void addActiveFile(String url, int size) {
		FileGrabber fgrabber = new FileGrabber(this,url, size);
		fgrabber.setStatus(fgrabber.IN_DOWNLOADED);
		activefiles.add(fgrabber);
		addtorecent(url);
	}
	/**
	 *
	 */
	@Override
	public void remActiveFile(String url) {
		for ( FileGrabber fgrabber : activefiles)
			if (fgrabber.getUrl() == url) {
				activefiles.remove(fgrabber);
				return;
			}
	}

	/**
	 *
	 * @return return the size in octet downloaded for this project
	 */
	public long getDowloadedsize() {
		return this.grabber.getDownloaded();
	}


	/**
	 * Save the project.
	 * Save the current project to a file named
	 * {project name}.esgiGrabber.
	 */
	public boolean saveProject()
	{
		GrabberSerialize gs = new GrabberSerialize(this.name,
												   this.url,
												   this.downloaddir,
												   this.status.name(),
												   this.grabber.history,
												   this.grabber.todownload,
												   this.limit,
												   this.limitmethod.name());
		return gs.save();
	}

	 /**
	 * @return return the size in octet downloaded for this project
	 */
	public LinkedList<String> getRecentUrl() {
		return this.recenturl;
	}

	//---------------------------------------------------------------------------------------------
	// Private methods
	//---------------------------------------------------------------------------------------------
	private void addtorecent(String url) {
		recenturl.addFirst(url);
		if (recenturl.size() > 50)
			recenturl.removeLast();
	}
	/**
	 *
	 * Start the grabbage of the url
	 */
	private void _initProjectGrabber(String name, String url, State state,
			String downloadldir, LimitMethod limitmethod, int limit) {
		this.name   = name;
		this.url    = url;
		this.status = state;
		this.downloaddir = downloadldir;
		this.activefiles = new ArrayList<FileGrabber>();
		this.grabber = new Grabber(name, this.downloaddir + "/" + this.name, limitmethod, limit);
		this.grabber.register(this);
		this.grabber.addtodownload(url);
		this.limit = limit;
		this.limitmethod = limitmethod;
		if ( this.status == State.PROJECT_RUN )
			this.grabber.start();
	}

	@Override
	public void incDownloaded(String URL, int length) {
		for ( FileGrabber file : activefiles)
			if (file.getUrl() == url) {
				file.incDownloaded(length);
				return;
			}
	}
/**
 * this method stop the url grabbing
 */
	@Override
	public void grabberEnd(Grabber grabber) {
		// TODO Auto-generated method stub
		this.status = State.PROJECT_END;
	}
}

