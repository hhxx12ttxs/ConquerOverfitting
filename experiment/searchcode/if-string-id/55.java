package n2hell.torrent;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import n2hell.config.RTorrentProcessConfig;
import n2hell.config.RtorrentBoxConfig;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

public interface Process {

	/**
	 * returns processId
	 * @return
	 */
	public abstract String getProcessId();

	/**
	 * rtorrent process pid
	 * 	returns rtorrent process pid
	 * @throws XmlRpcFault 
	 */
	public abstract long getPid() throws XmlRpcFault;

	/**
	 * returns file path separator char (/ for unix, \for windows) 
	 * @return
	 */
	public abstract char getFileSeparatoChar();

	/**
	 * erases torrent
	 * 
	 * @param id
	 *            hash of torrent
	 * @param deleteData
	 * 				delete torrent and data
	 * @throws XmlRpcFault 
	 */
	public abstract void erase(TorrentInfo torrent, Boolean deleteData)
			throws XmlRpcFault;

	/**
	 * stops torrent
	 * 
	 * @param id
	 *            id hash of torrent
	 * @throws XmlRpcFault
	 */
	public abstract void stop(TorrentInfo torrent) throws XmlRpcFault;

	/**
	 * starts torrent
	 * 
	 * @param id
	 *            id hash of torrent
	 * @throws XmlRpcFault
	 */
	public abstract void start(TorrentInfo torrent) throws XmlRpcFault;

	/**
	 * Returns torrent info by torrent hash
	 * 
	 * @param id -
	 *            torrent hash
	 * @return return {@link n2hell.torrent.TorrentInfo}
	 * @throws XmlRpcFault 
	 */
	public abstract TorrentInfo getTorrent(TorrentInfo ti) throws XmlRpcFault;

	/**
	 * Returns full torrent info by hash
	 * 
	 * @param id -
	 *            torrent hash
	 * @return return {@link n2hell.torrent.TorrentInfo}
	 * @throws XmlRpcFault 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings("unchecked")
	public abstract TorrentFullInfo getTorrentFull(TorrentInfo torrentInfo)
			throws XmlRpcFault;

	/**
	 * loads torrent by url
	 * 
	 * @param url
	 *            url of torrent
	 * @throws XmlRpcFault
	 */
	public abstract void load(String url, boolean start) throws XmlRpcFault;

	/**
	 * loads torrent by url
	 * 
	 * @param torrent
	 *            byte array of torrent file
	 * @param start
	 *            start torrent immediately
	 * @throws XmlRpcFault
	 */
	public abstract void load(byte[] torrent, boolean start, String tag,
			String directory) throws XmlRpcFault;

	/**
	 * Returns torrent updates for specified view
	 * 
	 * @param view
	 *            rtorrent view name
	 * @return array of {@link n2hell.torrent.TorrentInfo}
	 * @throws XmlRpcFault 
	 */
	public abstract TorrentInfo[] getTorrentsListUpdates() throws XmlRpcFault;

	/**
	 * Returns torrents list for specified view
	 * 
	 * @param view
	 *            rtorrent view name
	 * @return array of {@link n2hell.torrent.TorrentInfo}
	 * @throws XmlRpcFault 
	 */
	public abstract TorrentInfo[] getTorrentsList() throws XmlRpcFault;

	/**
	 * sets the torrent comment
	 * 
	 * @param id
	 *            hash of torrent
	 * @param comment
	 *            comment
	 * @throws XmlRpcFault
	 */
	public abstract void setComment(TorrentInfo torrent, String comment)
			throws XmlRpcFault;

	/**
	 * sets the torrent tag
	 * 
	 * @param id
	 *            hash of torrent
	 * @param tag
	 *            tag
	 * @throws XmlRpcFault
	 */
	public abstract void setTag(TorrentInfo torrent, String tag)
			throws XmlRpcFault;

	/**
	 * sets torrent directory
	 * 
	 * @param directory
	 * @throws XmlRpcException
	 */
	public abstract void setTorrentDirectory(String id, String directory)
			throws XmlRpcFault;

	/**
	 * sets the torrent specific file priority
	 * 
	 * @param id
	 *            hash of torrent
	 * @param fileIds
	 *            file id
	 * @param priority
	 *            priority {0,1,2}
	 * @throws XmlRpcException
	 */
	public abstract void setFilePriority(String id, Object[] fileIds,
			Integer priority) throws XmlRpcFault;

	/**
	 * shutdown rtorrent process
	 * 
	 * @throws XmlRpcFault
	 */
	public abstract void stopProcess() throws XmlRpcFault;

	/**
	 * checks if rtorrent running
	 * 
	 * @return boolean
	 */
	public abstract boolean isProcessStarted();

	/**
	 * gets global upload speed
	 * 
	 * @return
	 * @throws XmlRpcFault
	 */
	public abstract long getGlobalUpSpeed() throws XmlRpcFault;

	/**
	 * gets global download speed
	 * 
	 * @return
	 * @throws XmlRpcFault
	 */
	public abstract long getGlobalDownSpeed() throws XmlRpcFault;

	/**
	 * sets global upload speed
	 * 
	 * @param bytes
	 * @return
	 * @throws XmlRpcFault
	 */
	public abstract void setGlobalUpSpeed(int bytes) throws XmlRpcFault;

	/**
	 * sets global download speed
	 * 
	 * @param bytes
	 * @throws XmlRpcFault
	 */
	public abstract void setGlobalDownSpeed(int bytes) throws XmlRpcFault;

	public abstract RTorrentProcessConfig getProcessConfig()
			throws IOException, SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException;

	public abstract void setProcessConfig(RTorrentProcessConfig processConfig)
			throws XmlRpcFault, SecurityException, IllegalArgumentException,
			IOException, NoSuchMethodException, IllegalAccessException,
			InvocationTargetException;

	public abstract RtorrentBoxConfig getBoxConfig();
	
	public String getVersion() throws XmlRpcFault;

	public void setLive(boolean live);
	public boolean isLive();
}
