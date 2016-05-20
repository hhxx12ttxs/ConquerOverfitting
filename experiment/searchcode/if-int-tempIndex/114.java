/*******************************************************************************
 * Copyright (c) 2005-2011 eBay Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.ebayopensource.vjet.eclipse.internal.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

/**
 * Contains information needed for running interpreter.
 */
public class VjoRunnerInfo {

	private static final String BUNDLE_NAME = "org.ebayopensource.vjet.rt.wrapper";
	private static final String s_className = "org.ebayopensource.vjo.runner.VjoRunner";
	private static final String[] s_classPath = buildClassPath();

	public static String[] getClassPath() {

		return s_classPath;
	}

	public static String getClassName() {
		return s_className;
	}

	/**
	 * Combines build path
	 * 
	 * @return combined build path.
	 */
	private static String[] buildClassPath() {
		List<String> classPath = new ArrayList<String>();
		Set<String> visitedBundles = new HashSet<String>();
		
		Bundle bundle = Platform.getBundle(BUNDLE_NAME);
		visitedBundles.add(BUNDLE_NAME);
		
		extractClassPath(bundle, classPath, visitedBundles);
		return classPath.toArray(new String[classPath.size()]);
	}
	
	@SuppressWarnings("unchecked")
	private static void extractClassPath(
			Bundle bundle, List<String> classPath, Set<String> visitedBundles) {

		String baseLoc = getLocation(bundle);
		Dictionary<String, String> dic = bundle.getHeaders();
		
		if(baseLoc.endsWith("jar")){
			classPath.add(baseLoc);
		}
		else {// DEV MODE ONLY CODE
			classPath.add(baseLoc + File.separatorChar + "bin");
			try {
				ManifestElement[] cp = ManifestElement.parseHeader(
					Constants.BUNDLE_CLASSPATH, dic.get(Constants.BUNDLE_CLASSPATH));

				for (int iter = 0; cp != null && iter < cp.length; iter++) {
					classPath.add(baseLoc + File.separatorChar + cp[iter].getValue());
				}
			} catch (BundleException be) {
				// TODO: log this error
				// skip this entries...
				be.printStackTrace();
			}
		}
		
		//search dependent bundle
		try {
			ManifestElement[] mfEls = ManifestElement.parseHeader(
					Constants.REQUIRE_BUNDLE, (String) dic
							.get(Constants.REQUIRE_BUNDLE));
			for (int iter = 0; mfEls != null && iter < mfEls.length; iter++) {
				String bundleName = mfEls[iter].getValue();
				if (!visitedBundles.contains(bundleName)) {
					visitedBundles.add(bundleName);
					extractClassPath(Platform.getBundle(bundleName), classPath, visitedBundles);
				}
			}
		} catch (BundleException be) {
			// TODO: log this error
			// skip this entries...
		}
		
	}

	private static String getLocation(Bundle bundle) {

		String location = bundle.getLocation();
		location = location.substring(location.indexOf("@") + 1);

		// add by patrick for 3.5 compatibility
		int tempIndex = location.indexOf("reference:file:");
		if (tempIndex > -1) {
			location = location.substring(location.indexOf("file:") + 5);
		}
		// end add
		
		if (!new File(location).isAbsolute()) {
			location = new File(Platform.getInstallLocation().getURL()
					.getFile(), location).getAbsolutePath();
		}

		return location;
	}
}
