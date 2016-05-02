// Copyright (C) 2007-2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.enterprise.connector.filenet3;

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SpiConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.filenet.wcm.api.ObjectFactory;
import com.filenet.wcm.api.Search;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

public class FileTraversalManagerTest extends TestCase {
	Connector connector = null;

	Session sess = null;

	FileDocumentList fdl = null;

	FileTraversalManager qtm = null;

	protected void setUp() throws Exception {
		connector = new FileConnector();
		((FileConnector) connector).setUsername(FnConnection.userName);
		((FileConnector) connector).setPassword(FnConnection.password);
		((FileConnector) connector).setObject_store(FnConnection.objectStoreName);
		((FileConnector) connector).setWorkplace_display_url(FnConnection.displayUrl);
		((FileConnector) connector).setObject_factory(FnConnection.objectFactory);
		((FileConnector) connector).setPath_to_WcmApiConfig(FnConnection.pathToWcmApiConfig);
		((FileConnector) connector).setAdditional_where_clause(FnConnection.additionalWhereClause);
		((FileConnector) connector).setIs_public("false");
		sess = (FileSession) connector.login();
		qtm = (FileTraversalManager) sess.getTraversalManager();
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.startTraversal()'
	 */
	public void testStartTraversal() throws RepositoryException {
		qtm.setBatchHint(50);
		DocumentList set = this.qtm.startTraversal();
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(14, counter);
	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.resumeTraversal(String)'
	 */
	public void testResumeTraversal() throws RepositoryException {
		qtm.setBatchHint(50);
		DocumentList set = this.qtm.resumeTraversal(FnConnection.checkpoint2);
		assertNotNull(set);
		int counter = 0;
		com.google.enterprise.connector.spi.Document doc = null;
		doc = set.nextDocument();
		while (doc != null) {
			doc = set.nextDocument();
			counter++;
		}
		assertEquals(13, counter);

	}

	/*
	 * Test method for
	 * 'com.google.enterprise.connector.file.FileQueryTraversalManager.setBatchHint(int)'
	 */
	public void testSetBatchHint() throws RepositoryException {
		this.qtm.setBatchHint(10);
		DocumentList set = this.qtm.startTraversal();
		int counter = 0;
		while (set.nextDocument() != null) {
			counter++;
		}
		assertEquals(10, counter);
	}

	public void testFetchAndVerifyValueForCheckpoint()
			throws RepositoryException {

		/*
		 * public FileDocument(String docId, String timeStamp, IObjectStore
		 * objectStore, boolean isPublic, String displayUrl, HashSet
		 * included_meta, HashSet excluded_meta, SpiConstants.ActionType action)
		 */

		FileDocument pm = new FileDocument(FnConnection.docId,
				FnConnection.date, ((FileSession) sess).getObjectStore(),
				false, FnConnection.displayUrl, FnConnection.included_meta,
				FnConnection.excluded_meta, SpiConstants.ActionType.ADD);
		fdl = (FileDocumentList) qtm.startTraversal();
		String result = fdl.fetchAndVerifyValueForCheckpoint(pm, SpiConstants.PROPNAME_DOCID).nextValue().toString();
		assertEquals(FnConnection.docId, result);
	}

	public void testExtractDocidFromCheckpoint() {
		/*
		 * String checkPoint = "{\"uuid\":\"" + FnConnection.docVsId +
		 * "\",\"lastModified\":\"" + FnConnection.date + "\"}";
		 */
		String checkPoint = FnConnection.checkpoint;
		String uuid = null;
		JSONObject jo = null;
		try {
			jo = new JSONObject(checkPoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: " + checkPoint);
		}

		uuid = qtm.extractDocidFromCheckpoint(jo, checkPoint, FnConnection.PARAM_UUID);
		assertNotNull(uuid);
		assertEquals(FnConnection.docVsId, uuid);

	}

	public void testExtractNativeDateFromCheckpoint() {

		JSONObject jo = null;
		String modifDate = null;

		try {
			jo = new JSONObject(FnConnection.checkpoint);
		} catch (JSONException e) {
			throw new IllegalArgumentException(
					"checkPoint string does not parse as JSON: "
							+ FnConnection.checkpoint);
		}

		modifDate = qtm.extractNativeDateFromCheckpoint(jo, FnConnection.checkpoint, FnConnection.PARAM_DATE_LASTMODIFIED);
		assertNotNull(modifDate);
		assertEquals(FnConnection.dateForResume, modifDate);

	}

	public void testMakeCheckpointQueryString() throws RepositoryException {
		String uuid = FnConnection.docId;
		String statement = "";
		try {
			statement = qtm.makeCheckpointQueryString(uuid, FnConnection.date, FnConnection.PARAM_UUID);
		} catch (RepositoryException re) {
			re.printStackTrace();
		}

		assertNotNull(statement);
		assertEquals(FnConnection.DM_CHECKPOINT_QUERY_STRING, statement);
	}

	/**
	 * Compares the LastModified Date stored in Database of a given document
	 * with the LastModified Date stored in Checkpoint. If both the dates are
	 * equal then it proves that date stored in Checkpoint is correct and is
	 * stored in UTC timezone.
	 */
	public void testCompareDBTimeWithCheckPointTime() {
		String dbDate = getDBDate(FnConnection.objectStoreName, FnConnection.userName, FnConnection.password, FnConnection.docId3);
		assertEquals(FnConnection.checkpointDate, dbDate);
	}

	/**
	 * Retrieves the LastModified Date of a particular Document, stored in
	 * database.
	 * 
	 * @param objectStoreName - Name of the ObjectStore where the document is
	 *            stored.
	 * @param userName - Username to get session to retrieve the document
	 * @param password - Password of the corresponding user
	 * @param docId - Dcoument ID of the taarget document
	 * @return - Returns the exact LastModified Date of the document, which is
	 *         stored in Database.
	 */
	private String getDBDate(String objectStoreName, String userName,
			String password, String docId) {
		com.filenet.wcm.api.Session sess = ObjectFactory.getSession("com.google.enterprise.connector.filenet3.FileTraversalManagerTest", com.filenet.wcm.api.Session.DEFAULT, userName, password);
		StringBuffer query = new StringBuffer(
				"<?xml version=\"1.0\" ?><request>");
		query.append("<objectstores mergeoption=\"none\"><objectstore id=\"");
		query.append(objectStoreName);
		query.append("\"/></objectstores>");
		query.append("<querystatement>SELECT Id,DateLastModified FROM Document Where Id=");
		query.append(docId);
		query.append("</querystatement>");
		query.append("<options maxrecords='100' objectasid=\"false\"/></request>");

		Search search = ObjectFactory.getSearch(sess);
		Document document = stringToDom(search.executeXML(query.toString()));

		if (document != null) {
			return document.getElementsByTagName("z:row").item(0).getAttributes().item(0).getNodeValue();
		} else {
			return null;
		}

	}

	/**
	 * To get the Document out of the XML source passed as a string.
	 * 
	 * @param xmlSource - XML source to get the Document out of it.
	 * @return - Document object for the XML source.
	 */
	private Document stringToDom(String xmlSource) {
		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlSource)));
		} catch (Exception e) {
			return null;
		}
	}

}

