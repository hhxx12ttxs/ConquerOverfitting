package com.ironore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.ironore.config.ServerConfig;
import com.ironore.log.LogsManager;
import com.ironore.log.LogsManager.Logger;
import com.ironore.utils.IOUtils;
import com.ironore.utils.StringUtils;

/**
 * Hot deployment manager keeps an eye on the hot-deployment directory and 
 * trigger a hot deployment when there are changes.
 * 
 * <p/>
 * 
 * This is how it works :-
 * 
 * <p/>
 * 
 * Under typically '/deploy' directory (specified by <code>monitorDirectory</code>, 
 * this {@link HotDeploymentManager} will listen for file addition to it. Files 
 * that will be process must followed the following syntax 
 * 
 * <p/>
 * 
 * [context path without slash].undeploy
 * 
 * or
 * 
 * [context path without slash].deploy
 * 
 * <p/>
 * 
 * Eg.
 * 
 * "jroller.deploy" will cause the webapp under '/jroller' to be re-deployed (if exists).
 * <p/>
 * "ROOT.undeploy" will cause the webapp under '/' to be undeployed (if exists).
 * 
 * 
 * @author tmjee
 */
public class HotDeploymentManager implements Component {
	
	private Logger LOG = LogsManager.getLog(HotDeploymentManager.class);
	private Logger LOG_WORKER = LogsManager.getLog(HotDeploymentManager.InternalRunnable.class);
	
	private ServerContext serverContext;
	
	private volatile int count = 1;
	private URL monitoringDirectory;
	
	private WatchKey monitoringWatchKey;
	private Map<Path, VirtualHostDirKey> virtualHostDirKeys;
	
	private ScheduledExecutorService scheduledExecutors;
	
	public HotDeploymentManager(URL monitoringDirectory) {
		this.monitoringDirectory = monitoringDirectory;
	}

	@Override
	public void init(ServerContext serverContext) throws Exception {
		
		LOG.info("Initializing HotDeploymentManager ...");
		
		this.serverContext = serverContext;
		virtualHostDirKeys = new ConcurrentHashMap<Path, VirtualHostDirKey>();
		
		scheduledExecutors = Executors.newScheduledThreadPool(
				2, 
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, "HotDeploymentManager-Thread-"+count++);
						t.setDaemon(true);
						return t;
					}
				});
		
		final Path monitoringDirectoryPath = Paths.get(monitoringDirectory.toURI());
		boolean isMonitoringDirectoryPathDirectory = Files.isDirectory(monitoringDirectoryPath, LinkOption.NOFOLLOW_LINKS);
		if (isMonitoringDirectoryPathDirectory) {
			
			Files.walkFileTree(monitoringDirectoryPath, EnumSet.noneOf(FileVisitOption.class), 2, 
					new FileVisitor<Path>(){
						@Override
						public FileVisitResult preVisitDirectory(Path dir,
								BasicFileAttributes attrs) throws IOException {
							
							
							boolean isMonitoringDirectoryPath = Files.isSameFile(monitoringDirectoryPath, dir);
							
							if (isMonitoringDirectoryPath) {
								LOG.info("registering watch service on monitoring base directory ["+monitoringDirectory+"]");

								monitoringWatchKey = monitoringDirectoryPath.register(
										FileSystems.getDefault().newWatchService(), 
										new WatchEvent.Kind[]{
											StandardWatchEventKinds.ENTRY_CREATE, 
											StandardWatchEventKinds.ENTRY_DELETE, 
											StandardWatchEventKinds.ENTRY_MODIFY, 
											StandardWatchEventKinds.OVERFLOW
										});
							}
							else {
								Path relativizedPath = monitoringDirectoryPath.relativize(dir);
								String virtualHost = relativizedPath.getFileName().toString();
								
								LOG.info("registering watch service on virtual host directory ["+dir+"]");
								
								virtualHostDirKeys.put(dir, new VirtualHostDirKey(virtualHost, dir));
							}
							
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(Path file,
								BasicFileAttributes attrs) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(Path file,
								IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path dir,
								IOException exc) throws IOException {
							return FileVisitResult.CONTINUE;
						}
				
				});
			
			
			
			scheduledExecutors.scheduleWithFixedDelay(new InternalRunnable(monitoringDirectoryPath), 10000, 10000, TimeUnit.MILLISECONDS);
		}
		else {
			LOG.warn("Directory to be monitored ["+monitoringDirectory+"] is not a directory, hot deployment monitoring disabled");
		}
		
		LOG.info("Done initializing HotDeploymentManager");
	}

	@Override
	public void end(ServerContext serverContext) throws Exception {
		LOG.info("ending HotDeploymentManager ...");
		
		if (monitoringWatchKey != null) {
			if (monitoringWatchKey.isValid()) {
				LOG.info("cancelling monintoring watch on ["+monitoringDirectory+"]");
				monitoringWatchKey.cancel();
			}
			monitoringWatchKey = null;
		}
		
		LOG.info("HotDeploymentManager ended");
	}
	
	
	/**
	 * A holder that represents directory for a particular virtual host
	 * under which hot-deployment indicator files should be monitored
	 * 
	 * @author tmjee
	 */
	class VirtualHostDirKey {
		private String virtualHost;
		private Path dir;
		private WatchKey watchKey;
		
		public VirtualHostDirKey(String virtualHost, Path dir) throws IOException {
			this.virtualHost = virtualHost;
			this.dir = dir;
			watchKey = dir.register(
					FileSystems.getDefault().newWatchService(), 
					new WatchEvent.Kind[]{
						StandardWatchEventKinds.ENTRY_CREATE,
						StandardWatchEventKinds.ENTRY_DELETE, 
						StandardWatchEventKinds.ENTRY_MODIFY, 
						StandardWatchEventKinds.OVERFLOW
					});
		}
	}
	

	/**
	 * Internal {@link Runnable} that runs the job of monitoring directory for
	 * hot deployment and triggering {@link WebAppsManager} when hot deployment
	 * is detected.
	 * 
	 * @author tmjee
	 */
	class InternalRunnable implements Runnable {
		
		private Path monitoringDirectoryPath;
		
		InternalRunnable(Path monitoringDirectoryPath) {
			this.monitoringDirectoryPath = monitoringDirectoryPath;
		}

		@Override
		public void run() {
			
			LOG_WORKER.trace("start periodic hot-deployment indicator file scan");
			
			/*
			 * poll monitoringWatchKey first
			 */
			if (monitoringWatchKey != null && monitoringWatchKey.isValid()) {
				List<WatchEvent<?>> events = monitoringWatchKey.pollEvents();

				LOG_WORKER.trace("polling monitoringWatchKey, events size = "+events.size());
				
				try {
					for (WatchEvent event: events) {

						/*
						 * This will be the path of the file/directory (relative to the Path 
						 * registered monitoring)
						 */
						Path p = (Path) event.context();
						Path path = Paths.get(monitoringDirectoryPath.toUri()).resolve(p);
						
						LOG.trace("Detected movement on path ["+path+"] action detected ["+event.kind()+"]");
						
						if(StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
						
							if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {

								try {
									VirtualHostDirKey virtualHostDirKey = new VirtualHostDirKey(p.getFileName().toString(), path);
								
									LOG.trace("directory ["+path+"] created, virtual host deduced to be ["+virtualHostDirKey.virtualHost+"]");
								
									virtualHostDirKeys.put(path, virtualHostDirKey);
									
									LOG.trace("registered monitoring watch on virtual host ["+virtualHostDirKey.virtualHost+"] under directory ["+path+"] ");
									
								} catch (IOException e) {
									LOG.error("failed to register a watch on virtual host directory ["+path+"]", e);
								}
							}
							else { // not a directory? we can only allow dir creation here (that represent virtualhosts)
								try {
									LOG.trace("Path ["+path+"] is not a directory, deleting it as virtual host under deployment monitoring directory can only be directory representing virtual host");
									IOUtils.delete(path);
								} 
								catch (IOException e) {
									LOG.warn("error deleting bad file ["+path+"] created deploy directory, where only directories representing virtual hosts", e);
								}
							}
						}
						else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
							LOG.trace("virtual host deployment directory ["+path+"] deleted, unregistering watch monitoring for it");
							VirtualHostDirKey virtualHostDirKey = virtualHostDirKeys.remove(path);
							if (virtualHostDirKey != null) {
								LOG.trace("unregistered monitoring watch for virtual host ["+virtualHostDirKey.virtualHost+"] on path ["+virtualHostDirKey.dir+"]");
							}
						}
						else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
							LOG.trace("ignore virtual host path ["+path+"], do not know how to process it");
						}
						else if (StandardWatchEventKinds.OVERFLOW.equals(event.kind())) {
							LOG.trace("ignore virtual host path ["+path+"], do not know how to process it");
						}
					}
				}
				catch(Throwable t) {
					LOG.error(t.toString(), t);
				}
				finally {
					monitoringWatchKey.reset();
				}
			}
			
			/*
			 * Then VirtualHostDirKeys
			 */
			for (VirtualHostDirKey key : virtualHostDirKeys.values()) {
				
				LOG_WORKER.trace("scanning for virtualhost ["+key.virtualHost+"] in dir ["+key.dir+"]");
				
				if(key.watchKey != null && key.watchKey.isValid()) {

					List<WatchEvent<?>> events = key.watchKey.pollEvents();	
					
					LOG_WORKER.trace("polling virtualHostDirKey for virtualhost["+key.virtualHost+"], events size = "+events.size());
					
					try {
						for (WatchEvent event: events) {
							Path p = (Path) event.context();
							Path path = key.dir.resolve(p);
							
							LOG.trace("Detected hot-deployment indicator file ["+path+"] action detected ["+event.kind()+"]");
						
							if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
								
								deployOrUndeploy_file(key, p);
								
								// delete after use
								/*try {
									IOUtils.delete(path);
								} catch (IOException e) {
									LOG.warn("failed to delete ["+path+"]", e);
								}*/
							}
							else if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
								//LOG.debug("ignore hot-deployment indicator path ["+path+"], it has been manually deleted");
							}
							else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
								//LOG.debug("ignore hot-deployment indicator path ["+path+"], it has been modified, 'add' the file instead of 'touching' it to trigger intended action");
							}
							else if (StandardWatchEventKinds.OVERFLOW.equals(event.kind())) {
								//LOG.debug("ignore hot-deployment indicator path ["+path+"], do not know what to do with it");
							}
						}
					}
					catch(Throwable t) {
						LOG.error(t.toString(), t);
					}
					finally {
						key.watchKey.reset();
					}
				}
			}
			
			LOG_WORKER.trace("done periodic hot-deployment indicator file scan");
		}
	}
	

	/**
	 * Decides if it is a file type deploy/undeployment or directory type deploy/undeployment 
	 * and delegate accordingly to either 
	 * 
	 * {@link HotDeploymentManager#deployOrUndeploy_file(Path)}
	 * 
	 * and 
	 * 
	 * {@link HotDeploymentManager#deployOrUndeploy_dir(Path)}
	 * 
	 * accordingly.
	 * 
	 * @param monitoringDirectoryPath
	 * @param path
	 */
/*	protected void performDeployOrUndeployment(Path monitoringDirectoryPath, Path p) {
		Path path = Paths.get(monitoringDirectoryPath.toUri()).resolve(p);
		boolean isFile = Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
		
		LOG.info("detected creation of path ["+path+"] isFile=["+isFile+"]");
		if (isFile) {
			
			Path relativePath = monitoringDirectoryPath.relativize(path);
			
			if (relativePath.getNameCount() == 2) {
				String parentPathName = relativePath.getParent().getFileName().toString(); // this is the virtual host name
				if ("default".equals(parentPathName)) {
					
					 * if the hot-deployment indicator file under the root of the monitoring directory,
					 * in a sub directory called "default" 
					 * we treat it as if hot-deployment is to be done to the default ('*') virtual host
					 * WebAppsManager
					 
		 			deployOrUndeploy_file("*", path);
		 			return;
				}
				else {
					
					 * If the hot-deployment indicator file is in a directory under the root of the monitoring 
					 * directory that is NOT "default", then the directory will be the virtual host name 
					 * (or WebAppsManager that is handling this
					 * virtual host)
					 
		 			deployOrUndeploy_file(parentPathName, path);
		 			return;
				}
			}
			else {
				
				 * We do not recognize the location if it is neither of the above 2 cases
				 
				LOG.error("Improper location of hot deployment indicator file ["+path+"] with relativized path ["+relativePath+"], do not know what to do with it");
			}
		}
		else {
			
			 *	We do not know how to handle this hot-deployment indicator file 
			 
			LOG.error("Improper location or format of hot deployment indicator file ["+path+"], do not know what to do with it");
		}
	}
	*/

	/**
	 * File type deployment / undeployment
	 * 
	 * @param file
	 */
	protected void deployOrUndeploy_file(VirtualHostDirKey virtualHostKey, Path file) {
		Path fullFilePath = virtualHostKey.dir.resolve(file);
		Path fileNameAsPath = file.getFileName();
		
		if (fileNameAsPath != null) {
			
			String fileName = fileNameAsPath.toString();
			
			int indexOfDeployFileExtension = fileName.indexOf(".deploy");
			int indexOfUndeployFileExtension = fileName.indexOf(".undeploy");
			
			String fileContent = null;  // NOTE: content of the file is the location of the web app starting with file://...
			long fileSize = 0;
			
			// read hot-deployment indicator file's size
			try {
				fileSize = Files.size(fullFilePath);
			}
			catch(IOException e) {
				LOG.warn("unable to read size of hot-deployment indicator file ["+fullFilePath+"]", e);
			}
			
			// read hot-deployment indicator file's content only if it's size is valid.
			if (fileSize > 0) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ByteBuffer bb = ByteBuffer.allocate(1024);
					Future<Integer> f = AsynchronousFileChannel.open(fullFilePath, StandardOpenOption.READ).read(bb, 0);
			
					while(!f.isDone()) {
						f.get();
						bb.flip();
						while(bb.hasRemaining()) {
							baos.write(bb.get());
						}
						bb.compact();
					}
				
					fileContent = new String(baos.toByteArray()); 
				}
				catch(IOException | ExecutionException | InterruptedException e) {
					LOG.warn("unable to read content from hot-deployment indicator file ["+fullFilePath+"]", e);
				}
			}
			
			
			// delete file after use
			try {
				IOUtils.delete(fullFilePath);
			} catch (IOException e) {
				// TODO: we need to keep track of this file and not do further deployment to prevent recursive infinite re-deployment 
				// 		 A better way maybe ? currently we just refuse to do hot-deployment if this happens
				LOG.error("Failed to delete ["+fullFilePath+"] this is not good, will not continue with hot-deployment to abvid infinite cyclic re-deployment");
				return;
			}
			
			
			
			if (indexOfDeployFileExtension > 0) { 	// deployment
				
				String fileNameWithoutExtension = fileName.substring(0, indexOfDeployFileExtension);
				
				WebAppsManager webAppsManager = serverContext.getWebAppsManagerByVirtualHosts(virtualHostKey.virtualHost);
				if (webAppsManager == null) {
					LOG.warn("No WebAppsManager found to handle virtual host ["+virtualHostKey.virtualHost+"] for hot-deployment indicator file ["+file+"]");
					return;
				}
				
				String contextPath = calculateContextPath(fileNameWithoutExtension);
				
				if (fileContent != null) {

					fileContent = StringUtils.clean2(fileContent);
					
					String webAppLocation = null;
					String webAppTempWorkLocation = "${ironore.home}/temp/"+virtualHostKey.virtualHost+"/"+fileNameWithoutExtension;
					String webAppParentWebConfigLocation = "${ironore.home}/conf/default.xml";
					String webAppClassLoadingFileLocation = "${ironore.home}/conf/classloading.properties";
					
					StringTokenizer tok = new StringTokenizer(fileContent, System.getProperty("line.separator"));
					if(tok.hasMoreTokens()) {
						String tempWebAppLocation = StringUtils.clean(tok.nextToken());
						webAppLocation = tempWebAppLocation == null ? webAppLocation : tempWebAppLocation;
					}
					if (tok.hasMoreTokens()) {
						String tmpWebAppTempWorkLocation = StringUtils.clean(tok.nextToken());
						webAppTempWorkLocation = tmpWebAppTempWorkLocation == null ? webAppTempWorkLocation : tmpWebAppTempWorkLocation;
					}
					if (tok.hasMoreTokens()) {
						String tmpWebAppParentWebConfigLocation = StringUtils.clean(tok.nextToken());
						webAppParentWebConfigLocation = tmpWebAppParentWebConfigLocation == null ? webAppParentWebConfigLocation : tmpWebAppParentWebConfigLocation;
					}
					if (tok.hasMoreTokens()) {
						String tmpWebAppClassLoadingFileLocation = StringUtils.clean(tok.nextToken());
						webAppClassLoadingFileLocation = tmpWebAppClassLoadingFileLocation == null ? webAppClassLoadingFileLocation : tmpWebAppClassLoadingFileLocation;
					}
					
					
					try {
						
						URL webAppLocationURL = new URL(insertEnvVariables(webAppLocation));
						URL webAppTempWorkLocationURL = new URL(insertEnvVariables(webAppTempWorkLocation));
						URL webAppParentWebConfigLocationURL = new URL(insertEnvVariables(webAppParentWebConfigLocation));
						URL webAppClassLoadingFileLocationURL = new URL(insertEnvVariables(webAppClassLoadingFileLocation));
						
						LOG.debug("attempting hot-deployment of webApp with virtualHost=["+virtualHostKey.virtualHost+"] contextPath=["+contextPath
								+"]webAppLocationURL=["+webAppLocationURL+"] webAppTempWorkLocationURL=["+webAppTempWorkLocationURL
								+"] webAppParentWebConfigLocationURL=["+webAppParentWebConfigLocationURL+"] webAppClassLoadingFileLocationURL=["
								+webAppClassLoadingFileLocationURL+"]");

						WebApp webApp = new WebApp(
								contextPath, 
								webAppLocationURL, 
								webAppTempWorkLocationURL, 
								webAppParentWebConfigLocationURL, 
								webAppClassLoadingFileLocationURL);
						
						webAppsManager.hotDeployWebApp(contextPath, webApp);
					}
					catch(MalformedURLException e) {
						LOG.error("webapp location in file ["+fullFilePath+"] with content[\n"+fileContent+File.separator+"\n] is badly formed URL", e);
					}
				}
				else {
					webAppsManager.hotDeployWebApp(contextPath);
				}
			}
			else if (indexOfUndeployFileExtension > 0) { 	// undeployment
				String fileNameWithoutExtension = fileName.substring(0, indexOfUndeployFileExtension);
				String contextPath = calculateContextPath(fileNameWithoutExtension);
				
				LOG.debug("attempting hot-undeployment of webapp with virtualHost=["+virtualHostKey.virtualHost+"] with context path ["+contextPath+"]");
				
				WebAppsManager webAppsManager = serverContext.getWebAppsManagerByVirtualHosts(virtualHostKey.virtualHost);
				
				if (webAppsManager != null) {
					webAppsManager.undeployWebApp(calculateContextPath(fileNameWithoutExtension));
				}
				else {
					LOG.warn("No WebAppsManager found to handle virtual host ["+virtualHostKey.virtualHost+"] for hot-deployment indicator file ["+file+"]");
				}
			}
		}
	}
	

	/**
	 * Work out the context path based on file name without extension 
	 * <code>fileNameWithoutExtension</code>
	 * 
	 * Eg. 
	 * 
	 * "jroller.undeploy" will result in context path of '/jroller'
	 * <p/>
	 * "ROOT.deploy" will result in context path of '/'
	 * 
	 * @param fileNameWithoutExtension
	 * @return
	 */
	protected String calculateContextPath(String fileNameWithoutExtension) {
		String context = "/"+fileNameWithoutExtension;
		if ("ROOT".equalsIgnoreCase(fileNameWithoutExtension)) {
			context ="/";
		}
		return context;
	}
	
	
	protected String insertEnvVariables(String text) {
		return StringUtils.insertVariables(text, ServerConfig.envMap);
	}
	
	
}

