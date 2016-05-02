/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin Burger <m@rtin-burger.de> patch for #93810 and #93901
 *******************************************************************************/
package org.eclipse.core.patch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.patch.core.IHunk;
import org.eclipse.core.patch.core.IHunkFilter;
import org.eclipse.core.patch.core.PatchConfiguration;
import org.eclipse.core.patch.core.Utilities;
import org.eclipse.core.patch.core.internal.DiffProject;
import org.eclipse.core.patch.core.internal.FileDiffResult;
import org.eclipse.core.patch.core.internal.FilePatch;
import org.eclipse.core.patch.core.internal.Hunk;
import org.eclipse.core.patch.core.internal.LineReader;
import org.eclipse.core.patch.core.internal.PatchReader;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IPath;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.Path;
import org.eclipse.core.resources.internal.FileUtil;
import org.eclipse.core.runtime.Assert;

/**
 * A Patcher 
 * - knows how to parse various patch file formats into some in-memory structure,
 * - holds onto the parsed data and the options to use when applying the patches,
 * - knows how to apply the patches to files and folders.
 */
public class Patcher implements IHunkFilter {

	static protected final String REJECT_FILE_EXTENSION= ".rej"; //$NON-NLS-1$

	static protected final String MARKER_TYPE= "org.eclipse.compare.rejectedPatchMarker"; //$NON-NLS-1$

	/**
	 * Property used to associate a patcher with a {@link PatchConfiguration}
	 */
	public static final String PROP_PATCHER = "org.eclipse.compare.patcher"; //$NON-NLS-1$
	
	public interface IFileValidator {
		boolean validateResources(IFile[] array);
	}

	// diff formats
	//	private static final int CONTEXT= 0;
	//	private static final int ED= 1;
	//	private static final int NORMAL= 2;
	//	private static final int UNIFIED= 3;
	
	private FilePatch[] fDiffs;
	private IResource fTarget;
	// patch options
	private Set disabledElements = new HashSet();
	private Map diffResults = new HashMap();
	private final Map contentCache = new HashMap();
	private Set<Hunk> mergedHunks = new HashSet<Hunk>();

	private final PatchConfiguration configuration;
	private boolean fGenerateRejectFile = false;
	
	public Patcher() {
		configuration = new PatchConfiguration();
		configuration.setProperty(PROP_PATCHER, this);
		configuration.addHunkFilter(this);
	}
	
	/*
	 * Returns an array of Diffs after a sucessfull call to <code>parse</code>.
	 * If <code>parse</code> hasn't been called returns <code>null</code>.
	 */
	public FilePatch[] getDiffs() {
		if (fDiffs == null)
			return new FilePatch[0];
		return fDiffs;
	}
	
	public IPath getPath(FilePatch diff) {
		return diff.getStrippedPath(getStripPrefixSegments(), isReversed());
	}

	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	public boolean setStripPrefixSegments(int strip) {
		if (strip != getConfiguration().getPrefixSegmentStripCount()) {
			getConfiguration().setPrefixSegmentStripCount(strip);
			return true;
		}
		return false;
	}
	
	int getStripPrefixSegments() {
		return getConfiguration().getPrefixSegmentStripCount();
	}
	
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	public boolean setFuzz(int fuzz) {
		if (fuzz != getConfiguration().getFuzz()) {
			getConfiguration().setFuzz(fuzz);
			return true;
		}
		return false;
	}
	
	public int getFuzz(){
		return getConfiguration().getFuzz();
	}
		
	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	public boolean setIgnoreWhitespace(boolean ignoreWhitespace) {
		if (ignoreWhitespace != getConfiguration().isIgnoreWhitespace()) {
			getConfiguration().setIgnoreWhitespace(ignoreWhitespace);
			return true;
		}
		return false;
	}
	
	public boolean isIgnoreWhitespace() {
		return getConfiguration().isIgnoreWhitespace();
	}
	
	public boolean isGenerateRejectFile() {
		return fGenerateRejectFile;
	}

	public void setGenerateRejectFile(boolean generateRejectFile) {
		fGenerateRejectFile = generateRejectFile;
	}
	
	//---- parsing patch files

	public void parse(IStorage storage) throws IOException, RuntimeException {
		BufferedReader reader = Utilities.createReader(storage);
		try {
			parse(reader);
		} finally {
			FileUtil.safeClose(reader);
		}
	}
	
	public void parse(BufferedReader reader) throws IOException {
		PatchReader patchReader = new PatchReader() {
			protected FilePatch createFileDiff(IPath oldPath, long oldDate, IPath newPath, long newDate) {
				return new FilePatch(oldPath, oldDate, newPath, newDate);
			}
		};
		patchReader.parse(reader);
		patchParsed(patchReader);
	}

	protected void patchParsed(PatchReader patchReader) {
		fDiffs = patchReader.getDiffs();
	}
	
	public void countLines() {
		FilePatch[] fileDiffs = getDiffs();
		for (int i = 0; i < fileDiffs.length; i++) {
			int addedLines = 0;
			int removedLines = 0;
			FilePatch fileDiff = fileDiffs[i];
			for (int j = 0; j < fileDiff.getHunkCount(); j++) {
				IHunk hunk = fileDiff.getHunks()[j];
				String[] lines = ((Hunk) hunk).getLines();
				for (int k = 0; k < lines.length; k++) {
					char c = lines[k].charAt(0);
					switch (c) {
					case '+':
						addedLines++;
						continue;
					case '-':
						removedLines++;
						continue;
					}
				}
			}
			fileDiff.setAddedLines(addedLines);
			fileDiff.setRemovedLines(removedLines);
		}
	}
	
	//---- applying a patch file
	public void applyAll() throws RuntimeException {
		applyAll(new IFileValidator() {
			public boolean validateResources(IFile[] array) {
				return Utilities.validateResources(array);
			}
		});
	}
	
	
	public void applyAll(IFileValidator validator) throws RuntimeException {
		int i;
		
		IFile singleFile = null;	// file to be patched
		IContainer container = null;
		if (fTarget instanceof IContainer)
			container = (IContainer) fTarget;
		else if (fTarget instanceof IFile) {
			singleFile = (IFile) fTarget;
			container = singleFile.getParent();
		} else {
			Assert.isTrue(false);
		}
		
		// get all files to be modified in order to call validateEdit
		List<IFile> list = new ArrayList<IFile>();
		if (singleFile != null)
			list.add(singleFile);
		else {
			for (i = 0; i < fDiffs.length; i++) {
				FilePatch diff = fDiffs[i];
				if (isEnabled(diff)) {
					switch (diff.getDiffType(isReversed())) {
					case FilePatch.CHANGE:
						list.add(createPath(container, getPath(diff)));
						break;
					}
				}
			}
		}
		if (! validator.validateResources((IFile[])list.toArray(new IFile[list.size()]))) {
			return;
		}
		
		for (i = 0; i < fDiffs.length; i++) {
			FilePatch diff = fDiffs[i];
			if (isEnabled(diff)) {
				
				IPath path = getPath(diff);
				IFile file = singleFile != null
								? singleFile
								: createPath(container, path);
					
				List failed = new ArrayList();
				
				int type = diff.getDiffType(isReversed());
				switch (type) {
				case FilePatch.ADDITION:
					// patch it and collect rejected hunks
					List result = apply(diff, file, true, failed);
					if (result != null)
						store(LineReader.createString(isPreserveLineDelimeters(), result), file);
					break;
				case FilePatch.DELETION:
//					file.delete(true, true, new SubProgressMonitor(pm, workTicks));
					file.delete();
					break;
				case FilePatch.CHANGE:
					// patch it and collect rejected hunks
					result = apply(diff, file, false, failed);
					if (result != null)
						store(LineReader.createString(isPreserveLineDelimeters(), result), file);
					break;
				}

				if (isGenerateRejectFile() && failed.size() > 0) {
					IPath pp = getRejectFilePath(path);
					file = createPath(container, pp);
					if (file != null) {
						store(getRejected(failed), file);
						// TODO do we need markers here? probably not.
//						try {
//							IMarker marker= file.createMarker(MARKER_TYPE);
//							marker.setAttribute(IMarker.MESSAGE, Messages.Patcher_1);	
//							marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
//						} catch (RuntimeException ex) {
//							// NeedWork
//						}
					}
				}
			}
		}
	}

	private IPath getRejectFilePath(IPath path) {
		IPath pp = null;
		if (path.segmentCount() > 1) {
			pp = path.removeLastSegments(1);
			pp = pp.append(path.lastSegment() + REJECT_FILE_EXTENSION);
		} else {
			pp = new Path(path.lastSegment() + REJECT_FILE_EXTENSION);
		}
		return pp;
	}
	
	List<String> apply(FilePatch diff, IFile file, boolean create, List<IHunk> failedHunks) {
		FileDiffResult result = getDiffResult(diff);
		// original content, but doesn't work. fixed.
//		List<String> lines = LineReader.load(file, create);
//		result.patch(lines);
		result.refresh(Utilities.getReaderCreator(file));
		failedHunks.addAll(result.getFailedHunks());
		if (hasCachedContents(diff)) {
			// Used the cached contents since they would have been provided by the user
			return getCachedLines(diff);
		} else if (!result.hasMatches()) {
			// Return null if there were no matches
			return null;
		}
		return result.getLines();
	}
	
	/*
	 * Converts the string into bytes and stores them in the given file.
	 */
	protected void store(String contents, IFile file) throws RuntimeException {
		byte[] bytes = null;
		try {
			String charset = Utilities.getCharset(file);
			if (charset != null)
				bytes = contents.getBytes(charset);
		} catch (UnsupportedEncodingException x) {
			// will use default encoding after check
		}
		if (bytes == null)
			bytes = contents.getBytes();
		
		store(bytes,file);
	}

	protected void store(byte[] bytes, IFile file) throws RuntimeException {
		InputStream is = new ByteArrayInputStream(bytes);
		try {
			if (file.exists()) {
				file.setContents(is);
			} else {
				file.create(is);
			}
		} finally {
			FileUtil.safeClose(is);
		}
	}

	public boolean isPreserveLineDelimeters() {
		return true;
	}

	public static String getRejected(List<Hunk> failedHunks) {
		if (failedHunks.size() <= 0)
			return null;
		
		String lineSeparator= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer sb= new StringBuffer();
		for (Hunk hunk : failedHunks) {
			sb.append(hunk.getRejectedDescription());
			sb.append(lineSeparator);
			sb.append(hunk.getContent());
		}
		return sb.toString();
	}
	
	/*
	 * Ensures that a file with the given path exists in
	 * the given container. Folder are created as necessary.
	 */
	protected IFile createPath(IContainer container, IPath path) throws RuntimeException {
		if (path.segmentCount() > 1) {
			IContainer childContainer;
			// we don't have workspace roots anymore, so this is not necessary
//			if (container instanceof IWorkspaceRoot) {
//				IProject project = ((IWorkspaceRoot)container).getProject(path.segment(0));
//				if (!project.exists())
//					project.create(null);
//				if (!project.isOpen())
//					project.open(null);
//				childContainer = project;
//			} else {
				IFolder folder= container.getFolder(path.uptoSegment(1));
				if (!folder.exists())
					folder.create();
				childContainer = folder;
//			}
			return createPath(childContainer, path.removeFirstSegments(1));
		}
		// a leaf
		return container.getFile(path);
	}

	public IResource getTarget() {
		return fTarget;
	}

	public void setTarget(IResource target) {
		fTarget= target;
	}
	

	public IFile getTargetFile(FilePatch diff) {
		IPath path = diff.getStrippedPath(getStripPrefixSegments(), isReversed());
		return existsInTarget(path);
	}
	
	/**
	 * Iterates through all of the resources contained in the Patch Wizard target
	 * and looks to for a match to the passed in file 
	 * @param path
	 * @return IFile which matches the passed in path or null if none found
	 */
	public IFile existsInTarget(IPath path) {
		if (fTarget instanceof IFile) { // special case
			IFile file = (IFile) fTarget;
			if (matches(file.getFullPath(), path))
				return file;
		} else if (fTarget instanceof IContainer) {
			IContainer c = (IContainer) fTarget;
			if (c.exists(path))
				return c.getFile(path);
		}
		return null;
	}

	/**
	 * Returns true if path completely matches the end of fullpath
	 * @param fullpath 
	 * @param path 
	 * @return true if path matches, false otherwise
	 */
	private boolean matches(IPath fullpath, IPath path) {
		for (IPath p = fullpath; path.segmentCount()<=p.segmentCount(); p= p.removeFirstSegments(1)) {
			if (p.equals(path))
				return true;
		}
		return false;
	}

	public int calculatePrefixSegmentCount() {
		//Update prefix count - go through all of the diffs and find the smallest
		//path segment contained in all diffs.
		int length = 99;
		if (fDiffs != null)
			for (int i = 0; i < fDiffs.length; i++) {
				FilePatch diff = fDiffs[i];
				length = Math.min(length, diff.segmentCount());
			}
		return length;
	}
	
	public void addDiff(FilePatch newDiff){
		FilePatch[] temp = new FilePatch[fDiffs.length + 1];
		System.arraycopy(fDiffs, 0, temp, 0, fDiffs.length);
		temp[fDiffs.length] = newDiff;
		fDiffs = temp;
	}
	
	public void removeDiff(FilePatch diffToRemove){
		FilePatch[] temp = new FilePatch[fDiffs.length - 1];
		int counter = 0;
		for (int i = 0; i < fDiffs.length; i++) {
			if (fDiffs[i] != diffToRemove){
				temp[counter++] = fDiffs[i];
			}
		}
		fDiffs = temp;
	}
	
	public void setEnabled(Object element, boolean enabled) {
		if (element instanceof DiffProject) 
			setEnabledProject((DiffProject) element, enabled);
		if (element instanceof FilePatch) 
			setEnabledFile((FilePatch)element, enabled);
		if (element instanceof Hunk) 
			setEnabledHunk((Hunk) element, enabled);
	}
	
	private void setEnabledProject(DiffProject projectDiff, boolean enabled) {
		FilePatch[] diffFiles = projectDiff.getFileDiffs();
		for (int i = 0; i < diffFiles.length; i++) {
			setEnabledFile(diffFiles[i], enabled);
		}
	}
	
	private void setEnabledFile(FilePatch fileDiff, boolean enabled) {
		IHunk[] hunks = fileDiff.getHunks();
		for (int i = 0; i < hunks.length; i++) {
			setEnabledHunk((Hunk) hunks[i], enabled);
		}
	}

	private void setEnabledHunk(Hunk hunk, boolean enabled) {
		if (enabled) {
			disabledElements.remove(hunk);
			FilePatch file = hunk.getParent();
			disabledElements.remove(file);
			DiffProject project = file.getProject();
			if (project != null)
				disabledElements.remove(project);
		} else {
			disabledElements.add(hunk);
			FilePatch file = hunk.getParent();
			if (disabledElements.containsAll(Arrays.asList(file.getHunks()))) {
				disabledElements.add(file);
				DiffProject project = file.getProject();
				if (project != null
						&& disabledElements.containsAll(Arrays.asList(project
								.getFileDiffs())))
					disabledElements.add(project);
			}
		}
	}

	public boolean isEnabled(Object element) {
		if (disabledElements.contains(element)) 
			return false;
		Object parent = getElementParent(element);
		if (parent == null)
			return true;
		return isEnabled(parent);
	}

	protected Object getElementParent(Object element) {
		if (element instanceof Hunk) {
			Hunk hunk = (Hunk) element;
			return hunk.getParent();
		}
		return null;
	}
	
	/**
	 * Calculate the fuzz factor that will allow the most hunks to be matched.
	 * @param monitor a progress monitor
	 * @return the fuzz factor or <code>-1</code> if no hunks could be matched
	 */
	public int guessFuzzFactor() {
		FilePatch[] diffs = getDiffs();
		if (diffs == null || diffs.length <= 0)
			return -1;
		int fuzz = -1;
		for (int i = 0; i < diffs.length; i++) {
			FilePatch d = diffs[i];
			IFile file = getTargetFile(d);
			if (file != null && file.exists()) {
				List lines = LineReader.load(file, false);
				FileDiffResult result = getDiffResult(d);
				int f = result.calculateFuzz(lines);
				if (f > fuzz)
					fuzz = f;
			}
		}
		return fuzz;
	}
	
	public void refresh() {
		diffResults.clear();
		refresh(getDiffs());
	}
	
	public void refresh(FilePatch[] diffs) {
		for (int i = 0; i < diffs.length; i++) {
			FilePatch diff = diffs[i];
			FileDiffResult result = getDiffResult(diff);
			((BatchFileDiffResult)result).refresh();
		}
	}
	
	public FileDiffResult getDiffResult(FilePatch diff) {
		FileDiffResult result = (FileDiffResult)diffResults.get(diff);
		if (result == null) {
			result = new BatchFileDiffResult(diff, getConfiguration());
			diffResults.put(diff, result);
		}
		return result;
	}

	public PatchConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Return the project that contains this diff or <code>null</code>
	 * if the patch is not a workspace patch.
	 * @param diff the diff
	 * @return the project that contains the diff
	 */
	public DiffProject getProject(FilePatch diff) {
		return diff.getProject();
	}

	/*
	 * Returns <code>true</code> if new value differs from old.
	 */
	public boolean setReversed(boolean reverse) {
		if (getConfiguration().isReversed() != reverse) {
			getConfiguration().setReversed(reverse);
			refresh();
			return true;
		}
		return false;
	}
	
	public boolean isReversed() {
		return getConfiguration().isReversed();
	}
	
	/**
	 * Cache the contents for the given file diff. These contents
	 * will be used for the diff when the patch is applied. When the
	 * patch is applied, it is assumed that the provided contents 
	 * already have all relevant hunks applied.
	 * @param diff the file diff
	 * @param contents the contents for the file diff
	 */
	public void cacheContents(FilePatch diff, byte[] contents) {
		contentCache.put(diff, contents);
	}
	
	/**
	 * Return whether contents have been cached for the 
	 * given file diff.
	 * @param diff the file diff
	 * @return whether contents have been cached for the file diff
	 * @see #cacheContents(FilePatch, byte[])
	 */
	public boolean hasCachedContents(FilePatch diff) {
		return contentCache.containsKey(diff);
	}

	/**
	 * Return the content lines that are cached for the given 
	 * file diff.
	 * @param diff the file diff
	 * @return the content lines that are cached for the file diff
	 */
	public List getCachedLines(FilePatch diff) {
		byte[] contents = (byte[])contentCache.get(diff);
		if (contents != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(contents)));
			return LineReader.readLines(reader);
		}
		return null;
	}

	/**
	 * Return the contents that are cached for the given diff or
	 * <code>null</code> if there is no contents cached.
	 * @param diff the diff
	 * @return the contents that are cached for the given diff or
	 * <code>null</code>
	 */
	public byte[] getCachedContents(FilePatch diff) {
		return (byte[])contentCache.get(diff);
	}
	
	/**
	 * Return whether the patcher has any cached contents.
	 * @return whether the patcher has any cached contents
	 */
	public boolean hasCachedContents() {
		return !contentCache.isEmpty();
	}

	/**
	 * Clear any cached contents.
	 */
	public void clearCachedContents() {
		contentCache.clear();
		mergedHunks.clear();
	}
	
	public void setProperty(String key, Object value) {
		getConfiguration().setProperty(key, value);
	}
	
	public Object getProperty(String key) {
		return getConfiguration().getProperty(key);
	}

	public boolean isManuallyMerged(Hunk hunk) {
		return mergedHunks.contains(hunk);
	}

	public void setManuallyMerged(Hunk hunk, boolean merged) {
		if (merged)
			mergedHunks.add(hunk);
		else 
			mergedHunks.remove(hunk);
	}

	public static Patcher getPatcher(PatchConfiguration configuration) {
		return (Patcher)configuration.getProperty(PROP_PATCHER);
	}
	
	public boolean hasRejects() {
		for (Iterator iterator = diffResults.values().iterator(); iterator.hasNext();) {
			FileDiffResult result = (FileDiffResult) iterator.next();
			if (result.hasRejects())
				return true;
		}
		return false;
	}

	public boolean select(IHunk hunk) {
		return isEnabled(hunk);
	}
	
	@Override
	public String toString() {
		StringBuilder bldr = new StringBuilder();
		bldr.append("target { ").append((fTarget != null) ? fTarget.toString() : "null").append(" }\n");
		bldr.append("diffs {\n");
		for (FilePatch diff : fDiffs) {
			bldr.append("\t").append(diff).append("\n");
		}
		bldr.append("}\n");
		return bldr.toString();
	}
}

