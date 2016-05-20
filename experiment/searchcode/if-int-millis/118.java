/*
 * Copyright 2011-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.xd.integration.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jclouds.ec2.domain.RunningInstance;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.xd.integration.fixtures.Jobs;
import org.springframework.xd.integration.fixtures.Processors;
import org.springframework.xd.integration.fixtures.Sinks;
import org.springframework.xd.integration.fixtures.Sources;
import org.springframework.xd.integration.util.ConfigUtil;
import org.springframework.xd.integration.util.HadoopUtils;
import org.springframework.xd.integration.util.JobUtils;
import org.springframework.xd.integration.util.StreamUtils;
import org.springframework.xd.integration.util.XdEc2Validation;
import org.springframework.xd.integration.util.XdEnvironment;
import org.springframework.xd.rest.client.domain.ModuleMetadataResource;
import org.springframework.xd.test.fixtures.AbstractModuleFixture;
import org.springframework.xd.test.fixtures.LogSink;
import org.springframework.xd.test.fixtures.SimpleFileSink;

/**
 * Base Class for Spring XD Integration classes
 *
 * @author Glenn Renfro
 * @author David Turanski
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestConfig.class)
public abstract class AbstractIntegrationTest {

	private final static String STREAM_NAME = "ec2Test3";

	protected final static String JOB_NAME = "ec2Job3";

	protected final static String DEFAULT_XD_PORT = "9393";

	protected final static String XD_DELIMITER = " | ";

	public final static int WAIT_TIME = 10000;

	protected final static String XD_TAP_DELIMITER = " > ";


	@Autowired
	protected XdEnvironment xdEnvironment;

	@Autowired
	protected XdEc2Validation validation;

	protected URL adminServer;

	@Value("${xd_pause_time}")
	protected int pauseTime;

	@Value("${xd_run_on_ec2}")
	protected boolean isOnEc2;

	@Value("${aws_access_key:}")
	protected String awsAccessKey;

	@Value("${aws_secret_key:}")
	protected String awsSecretKey;

	@Value("${aws_region:}")
	protected String awsRegion;

	@Autowired
	protected Sources sources;

	@Autowired
	protected Sinks sinks;

	@Autowired
	protected Jobs jobs;

	@Autowired
	protected Processors processors;

	@Autowired
	protected ConfigUtil configUtil;

	@Autowired
	protected HadoopUtils hadoopUtil;

	private boolean initialized = false;

	/**
	 * Maps the containerID to the container dns.
	 */
	private Map<String, String> containers;


	/**
	 * Initializes the environment before the test. Also asserts that the admin server is up and at least one container is
	 * available.
	 *
	 */
	public void initializer() {
		if (!initialized) {
			adminServer = xdEnvironment.getAdminServerUrl();
			validation.verifyXDAdminReady(adminServer);
			containers = getAvailableContainers(adminServer);
			assertTrue("There must be at least one container", containers.size() > 0);
			initialized = true;
		}
	}

	/**
	 * Retrieves the containers that are recognized by the adminServer.  
	 * If the test is on EC2 the IPs of the containers will be set to the external IPs versus the default internal IPs.
	 * @param adminServer The adminserver to interrogate.
	 * @return A Map of container servers , that is keyed on the containerID assigned by the admin server.
	 */
	private Map<String, String> getAvailableContainers(URL adminServer) {
		Map<String, String> result = null;
		result = StreamUtils.getAvailableContainers(adminServer);
		//if ec2 replace local aws DNS with external DNS
		if (isOnEc2) {
			Map<String, String> metadataIpMap = getPrivateIpToPublicIP(StreamUtils.getEC2RunningInstances(
					awsAccessKey, awsSecretKey, awsRegion));
			Iterator<String> keysIter = result.keySet().iterator();
			while (keysIter.hasNext()) {
				String key = keysIter.next();
				String privateIP = result.get(key);
				Iterator<String> metadataIter = metadataIpMap.keySet().iterator();
				while (metadataIter.hasNext()) {
					String metadataPrivateIP = metadataIter.next();
					//AWS metadata suffixes its data with .ec2.internal or .compute-1.internal.  So we are finding
					//the metadata private ip that contains the internal id returned by the admin server. 
					if (metadataPrivateIP != null && metadataPrivateIP.contains(privateIP)) {
						result.put(key, metadataIpMap.get(metadataPrivateIP));
						break;
					}
				}
			}
		}
		return result;
	}

	private Map<String, String> getPrivateIpToPublicIP(List<RunningInstance> riList) {
		Map<String, String> privateIPMap = new HashMap<String, String>();
		Iterator<RunningInstance> runningInstanceIter = riList.iterator();
		while (runningInstanceIter.hasNext()) {
			RunningInstance ri = runningInstanceIter.next();
			privateIPMap.put(ri.getPrivateDnsName(), ri.getDnsName());
		}
		return privateIPMap;
	}

	/**
	 * Destroys the temporary directory.
	 */
	@AfterClass
	public static void tearDownAfterClass() {
		File file = new File(StreamUtils.TMP_DIR);
		if (file.exists()) {
			file.delete();
		}

	}

	/**
	 * Destroys all streams in the xd cluster and calls initializer.
	 */
	@Before
	public void setup() {
		initializer();
		StreamUtils.destroyAllStreams(adminServer);
		JobUtils.destroyAllJobs(adminServer);
		waitForXD();
	}

	/**
	 * Destroys all stream created in the test.
	 */
	@After
	public void tearDown() {
		StreamUtils.destroyAllStreams(adminServer);
		JobUtils.destroyAllJobs(adminServer);
		waitForXD();
	}


	/**
	 * Creates a stream on the XD cluster defined by the test's Artifact or Environment variables Uses STREAM_NAME as
	 * default stream name.
	 *
	 * @param stream the stream definition
	 */
	public void stream(String stream) {
		stream(STREAM_NAME, stream, WAIT_TIME);
	}

	/**
	 * Creates a stream on the XD cluster defined by the test's Artifact or Environment variables
	 *
	 * @param streamName the name of the stream
	 * @param stream the stream definition
	 * @param waitTime the time to wait for a stream to be deployed
	 */
	public void stream(String streamName, String stream, int waitTime) {
		Assert.hasText(streamName, "stream name can not be empty nor null");
		Assert.hasText(stream, "stream needs to be populated with a definition and can not be null");
		StreamUtils.stream(streamName, stream, adminServer);
		waitForXD();
		assertTrue("The stream did not deploy. ",
				waitForStreamDeployment(streamName, waitTime));
	}

	/**
	 * Creates a job on the XD cluster defined by the test's
	 * Artifact or Environment variables Uses JOB_NAME as default job name.
	 *
	 * @param job the job definition
	 */
	public void job(String job) {
		Assert.hasText(job, "job needs to be poopulated with a definition and can not be null");
		job(JOB_NAME, job, WAIT_TIME);
	}

	/**
	 * Creates a job on the XD cluster defined by the test's Artifact or Environment variables
	 *
	 * @param jobName the name of the job
	 * @param job the job definition
	 * @param waitTime the time to wait for a job to be deployed
	 */
	public void job(String jobName, String job, int waitTime) {
		Assert.hasText(jobName, "job name can not be empty nor null");
		Assert.hasText(job, "job needs to be populated with a definition and can not be null");
		JobUtils.job(jobName, job, adminServer);
		waitForXD();
		assertTrue("The job did not deploy. ",
				waitForJobDeployment(jobName, waitTime));

	}

	/**
	 * Launches a job with the test's JOB_NAME on the XD instance.
	 */
	public void jobLaunch() {
		jobLaunch(JOB_NAME);
	}

	/**
	 * Launches a job on the XD instance
	 *
	 * @param jobName The name of the job to be launched
	 */
	public void jobLaunch(String jobName) {
		JobUtils.launch(adminServer, jobName);
		waitForXD();
	}

	/**
	 * Creates a file in a source directory for file source base tests.
	 * @param sourceDir The directory to place the file
	 * @param fileName The name of the file where the data will be written
	 * @param data The data to be written to the file
	 */
	public void setupSourceDataFiles(String sourceDir, String fileName, String data) {
		setupDataFiles(getContainerHostForSource(), sourceDir, fileName, data);
	}

	/**
	 * Creates a file in a directory for file based tests.
	 * @param host The host machine that the data will be written 
	 * @param sourceDir The directory to place the file
	 * @param fileName The name of the file where the data will be written
	 * @param data The data to be written to the file
	 */
	public void setupDataFiles(String host, String sourceDir, String fileName, String data) {
		Assert.hasText(host, "host must not be empty nor null");
		Assert.hasText(fileName, "fileName must not be empty nor null");
		Assert.notNull(sourceDir, "sourceDir must not be null");
		Assert.notNull(data, "data must not be null");

		if (xdEnvironment.isOnEc2()) {
			StreamUtils.createDataFileOnRemote(xdEnvironment, host, sourceDir, fileName, data);
		}
		else {
			try {
				File file = new File(sourceDir + "/" + fileName);
				file.deleteOnExit();
				file.createNewFile();
				FileCopyUtils.copy(data.getBytes(), file);
			}
			catch (IOException ioe) {
				throw new IllegalStateException(ioe.getMessage(), ioe);
			}
		}
	}

	/**
	 * Appends data to the specified file wherever the source module for the stream is deployed.
	 * @param sourceDir The location of the file
	 * @param fileName The name of the file to be appended 
	 * @param dataToAppend The data to be appended to the file
	 */
	public void appendDataToSourceTestFile(String sourceDir, String fileName, String dataToAppend) {
		Assert.hasText(fileName, "fileName must not be empty nor null");
		Assert.notNull(sourceDir, "sourceDir must not be null");
		Assert.notNull(dataToAppend, "dataToAppend must not be null");

		if (xdEnvironment.isOnEc2()) {
			StreamUtils.appendToRemoteFile(xdEnvironment, getContainerHostForSource(), sourceDir, fileName,
					dataToAppend);
		}
		else {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(sourceDir + "/" + fileName, true)));
				out.println(dataToAppend);
				out.close();
			}
			catch (IOException ioe) {
				throw new IllegalStateException(ioe.getMessage(), ioe);
			}
			finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}

	/*
	* Launches a job on the XD instance
	*
	* @param jobName The name of the job to be launched
	* @param jobParameters the job parameters
	*/
	public void jobLaunch(String jobName, String jobParameters) {
		JobUtils.launch(adminServer, jobName, jobParameters);
		waitForXD();
	}


	/**
	 * Gets the URL of the container for the sink being tested.
	 *
	 * @return The URL that contains the sink.
	 */
	public URL getContainerUrlForSink() {
		Assert.hasText(STREAM_NAME, "stream name can not be empty nor null");
		// Assuming one container for now.
		return getContainerUrlForSink(STREAM_NAME);
	}

	/**
	 * Gets the URL of the container where the sink was deployed using default XD Port.
	 *
	 * @param streamName Used to find the container that contains the sink.
	 * @return The URL that contains the sink.
	 */
	public URL getContainerUrlForSink(String streamName) {
		return getContainerHostForURL(streamName, ModuleType.sink);
	}

	/**
	 * Gets the URL of the container where the processor was deployed
	 * @return The URL that contains the sink.
	 */

	public URL getContainerUrlForProcessor() {
		return getContainerUrlForProcessor(STREAM_NAME);
	}


	/**
	 * Gets the URL of the container where the processor was deployed
	 *
	 * @param streamName Used to find the container that contains the processor.
	 * @return The URL that contains the processor.
	 */
	public URL getContainerUrlForProcessor(String streamName) {

		return getContainerHostForURL(streamName, ModuleType.processor);
	}

	/**
	 * Gets the host of the container where the source was deployed
	 * @return The host that contains the source.
	 */
	public String getContainerHostForSource() {
		return getContainerHostForSource(STREAM_NAME);
	}

	/**
	 * Gets the host of the container where the source was deployed
	 *
	 * @param streamName Used to find the container that contains the source.
	 * @return The host that contains the source.
	 */
	public String getContainerHostForSource(String streamName) {
		return getContainerHostForModulePrefix(streamName, ModuleType.source);
	}

	/**
	 * Gets the host of the container where the job was deployed
	 *
	 * @param jobName Used to find the container that contains the job.
	 * @return The host that contains the job.
	 */
	public String getContainerHostForJob() {
		return getContainerHostForJob(JOB_NAME);
	}

	/**
	 * Gets the host of the container where the job was deployed
	 *
	 * @param jobName Used to find the container that contains the job.
	 * @return The host that contains the job.
	 */
	public String getContainerHostForJob(String jobName) {
		return getContainerHostForModulePrefix(jobName, ModuleType.job);
	}

	/**
	 * Gets the URL of the container where the module
	 *
	 * @param streamName Used construct the module id prefix.
	 * @return The URL that contains the module.
	 */
	public String getContainerHostForModulePrefix(String streamName, ModuleType moduleType) {
		Assert.hasText(streamName, "stream name can not be empty nor null");
		String moduleIdPrefix = streamName + "." + moduleType + ".";
		Iterator<ModuleMetadataResource> resourceIter = StreamUtils.getRuntimeModules(adminServer).iterator();
		ArrayList<String> containerIds = new ArrayList<String>();
		while (resourceIter.hasNext()) {
			ModuleMetadataResource resource = resourceIter.next();
			if (resource.getModuleId().startsWith(moduleIdPrefix)) {
				containerIds.add(resource.getContainerId());
			}
		}
		Assert.isTrue(
				containerIds.size() == 1,
				"Test require that module to be deployed to only one container. It was deployed to "
						+ containerIds.size() + " containers");
		return containers.get(containerIds.get(0));
	}

	/**
	 * Asserts that the expected number of messages were received by all modules in a stream.
	 *
	 */
	public void assertReceived(int msgCountExpected) {
		waitForXD();

		validation.assertReceived(StreamUtils.replacePort(
				getContainerUrlForSink(STREAM_NAME), xdEnvironment.getJmxPort()),
				STREAM_NAME, msgCountExpected);
	}

	/**
	 * Asserts that all channels of the module channel combination, processed the correct number of messages
	 * @param containerUrl the container that is hosting the module
	 * @param moduleName the name of the module jmx element to interrogate.
	 * @param channelName the name of the channel jmx element to interrogate
	 * @param msgCountExpected The number of messages this module and channel should have sent.
	 */
	public void assertReceived(URL containerUrl, String moduleName, String channelName, int msgCountExpected) {
		waitForXD();

		validation.assertReceived(StreamUtils.replacePort(
				containerUrl, xdEnvironment.getJmxPort()),
				STREAM_NAME, moduleName, channelName, msgCountExpected);
	}

	/**
	 * Asserts that the data stored by the file or log sink is what was expected.
	 *
	 * @param data The data expected in the file or log sink
	 * @param sinkInstance determines whether to look at the log or file for the result
	 */
	public void assertValid(String data, AbstractModuleFixture<?> sinkInstance) {
		Assert.hasText(data, "data can not be empty nor null");
		Assert.notNull(sinkInstance, "sinkInstance must not be null");
		if (sinkInstance.getClass().equals(SimpleFileSink.class)) {
			assertValidFile(data, getContainerUrlForSink(STREAM_NAME), STREAM_NAME);
		}
		if (sinkInstance.getClass().equals(LogSink.class)) {
			assertLogEntry(data, getContainerUrlForSink(STREAM_NAME));
		}

	}

	/**
	 * Asserts that the data stored by the file sink, whose name is based off the stream
	 * name, is what was expected.
	 *
	 * @param data The data expected in the file
	 */
	public void assertFileContains(String data) {
		assertFileContains(data, getContainerUrlForSink(STREAM_NAME), STREAM_NAME);
	}

	/**
	 * Asserts that the data stored by a file sink, whose name is based off the stream name,
	 * is what was expected.  The assertion is case insensitive.
	 *
	 * @param data The data expected in the file
	 */
	public void assertFileContainsIgnoreCase(String data) {
		assertFileContainsIgnoreCase(data, getContainerUrlForSink(STREAM_NAME), STREAM_NAME);
	}

	/**
	 * Undeploys the test stream
	 */
	public void undeployStream() {
		undeployStream(STREAM_NAME);
	}

	/**
	 * Undeploys the stream specified by the streamName
	 * @param streamName the name of the stream to undeploy.
	 */
	public void undeployStream(String streamName) {
		StreamUtils.undeployStream(adminServer, streamName);
	}

	/**
	 * Wait the "waitTime" for a stream to be deployed.
	 *
	 * @param waitTime the time in millis to wait.
	 * @return true if deployed else false.
	 */
	public boolean waitForStreamDeployment(int waitTime) {
		return waitForStreamDeployment(STREAM_NAME, waitTime);
	}

	/**
	 * Wait the "waitTime" for a stream to be deployed.
	 *
	 * @param streamName the name of stream to be evaluated.
	 * @param waitTime the time in millis to wait.
	 * @return true if deployed else false.
	 */
	public boolean waitForStreamDeployment(String streamName, int waitTime) {
		Assert.hasText(streamName, "streamName must not be empty nor null");
		return StreamUtils.waitForStreamDeployment(streamName, adminServer, waitTime);
	}


	/**
	 * Wait the "waitTime" for a job to be deployed.
	 *
	 * @param waitTime the time in millis to wait.
	 * @return true if deployed else false.
	 */
	public boolean waitForJobDeployment(int waitTime) {
		return waitForJobDeployment(JOB_NAME, waitTime);
	}

	/**
	 * Wait the "waitTime" for a job to be deployed.
	 *
	 * @param jobName the name of stream to be evaluated.
	 * @param waitTime the time in millis to wait.
	 * @return true if deployed else false.
	 */
	public boolean waitForJobDeployment(String jobName, int waitTime) {
		Assert.hasText(jobName, "jobName must not be empty nor null");
		return JobUtils.waitForJobDeployment(jobName, adminServer, waitTime);
	}


	/**
	 * Verifies that the content of file on HDFS is the same as the data.
	 * @param data The data expected in the file.
	 * @param path The path/filename of the file on hdfs.  
	 */
	public void assertValidHdfs(String data, String path) {
		validation.verifyHdfsTestContent(data, path);
	}

	/**
	 * Asserts that the data stored by the file sink is what was expected.  
	 *
	 * @param data The data expected in the file
	 * @param url The URL of the server that we will ssh into to get the data
	 * @param streamName the name of the stream, used to form the filename we are retrieving from the remote server
	 */
	private void assertFileContains(String data, URL url, String streamName)
	{
		Assert.hasText(data, "data can not be empty nor null");
		String fileName = XdEnvironment.RESULT_LOCATION + "/" + streamName
				+ ".out";
		waitForPath(pauseTime * 2000, fileName);
		validation.verifyContentContains(url, fileName, data);
	}

	/**
	 * Asserts that the data stored by a file sink, whose name is based off the stream name,
	 * is what was expected.  The assertion is case insensitive.
	 *
	 * @param data The data to validate the file content against
	 * @param url The URL of the server that we will ssh, to get the data
	 * @param streamName the name of the file we are retrieving from the remote server
	 */
	private void assertFileContainsIgnoreCase(String data, URL url, String streamName)
	{
		Assert.hasText(data, "data can not be empty nor null");
		String fileName = XdEnvironment.RESULT_LOCATION + "/" + streamName
				+ ".out";
		waitForPath(pauseTime * 2000, fileName);
		validation.verifyContentContainsIgnoreCase(url, fileName, data);
	}

	/**
	 * Asserts the file data to see if it matches what is expected.
	 *
	 * @param data The data to validate the file content against
	 * @param url The URL of the server that we will ssh, to get the data
	 * @param streamName the name of the file we are retrieving from the remote server
	 */
	private void assertValidFile(String data, URL url, String streamName)
	{
		String fileName = XdEnvironment.RESULT_LOCATION + "/" + streamName
				+ ".out";
		waitForPath(pauseTime * 2000, fileName);
		validation.verifyTestContent(url, fileName, data);
	}

	/**
	 * Waits up to the timeout for the resource to be written to filesystem.
	 *
	 * @param waitTime The number of millis to wait.
	 * @param path the path to the resource .
	 * @return false if the path was not present. True if it was present.
	 */
	public boolean waitForPath(int waitTime, String path) {
		long timeout = System.currentTimeMillis() + waitTime;
		File file = new File(path);
		boolean exists = file.exists();
		while (!exists && System.currentTimeMillis() < timeout) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e.getMessage(), e);
			}
			exists = file.exists();
		}
		return exists;
	}

	/**
	 * Asserts the log to see if the data specified is in the log.
	 *
	 * @param data The data to check if it is in the log file
	 * @param url The URL of the server we will ssh, to get the data.
	 */
	private void assertLogEntry(String data, URL url)
	{
		waitForXD();
		validation.verifyContentContains(url, xdEnvironment.getContainerLogLocation(), data);
	}

	protected void waitForXD() {
		waitForXD(pauseTime * 1000);
	}

	protected void waitForXD(int millis) {
		try {
			Thread.sleep(millis);
		}
		catch (Exception ex) {
			// ignore
		}

	}

	/**
	 * Finds the container URL where the module is deployed with the stream name & module type 
	 * @param streamName The name of the stream that the module is deployed
	 * @param moduleType The type of module that we are seeking
	 * @return the container url.
	 */
	private URL getContainerHostForURL(String streamName, ModuleType moduleType) {
		URL result = null;
		try {
			result = new URL("http://"
					+ getContainerHostForModulePrefix(streamName, moduleType) + ":" + DEFAULT_XD_PORT);
		}
		catch (MalformedURLException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return result;
	}

	/**
	 * Get the {@see XdEnvironment}
	 * @return the XdEnvironment
	 */
	public XdEnvironment getEnvironment() {
		return xdEnvironment;
	}

	public enum ModuleType {
		sink, source, processor, job
	}

}

