package eclihx.ui.launch.handlers;

import static eclihx.core.util.language.CollectionUtils.array;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import eclihx.core.EclihxCore;
import eclihx.core.haxe.internal.HaxeOutputErrorsParser;
import eclihx.core.haxe.internal.ICompilerError;
import eclihx.core.haxe.internal.IHaxeOutputErrorsParser;
import eclihx.core.haxe.model.core.IHaxeProject;
import eclihx.core.util.OSUtil;
import eclihx.launching.HaxeLaunchDelegate.FinishLaunchInfo;
import eclihx.ui.PreferenceConstants;
import eclihx.ui.internal.ui.EclihxUIPlugin;
import eclihx.ui.utils.ConsoleViewHelper;

/**
 * Handlers the end of launch operation.
 */
public final class FinishLaunchHandler implements IStatusHandler {

	/**
	 * Makes paths in the output string relative to the project.
	 * 
	 * @param output the output string.
	 * @param haxeProject the project.
	 * @return new string with paths relative to project.
	 */
	private String makeRelativePaths(final String output,
			final IHaxeProject haxeProject) {

		final String projectPath = OSUtil
				.replaceToHaxeOutputSlashes(haxeProject.getProjectBase()
						.getLocation().toString() + File.pathSeparator);

		final int pathLength = projectPath.length();
		final String projectReplaceString = "";

		final StringBuilder outputString = new StringBuilder(output);

		int tempIndex = -1;
		while ((tempIndex = outputString.indexOf(projectPath)) != -1) {
			outputString.replace(tempIndex, tempIndex + pathLength,
					projectReplaceString);
		}

		return outputString.toString();
	}

	/**
	 * Method prints the output of the launching to console.
	 * 
	 * @param output string to show on the console view.
	 * @param haxeProject the project this launch was performed for.
	 * @throws PartInitException
	 */
	private void printOutputToConsole(final String output,
			final IHaxeProject haxeProject) throws PartInitException {

		final MessageConsole myConsole = ConsoleViewHelper.findConsole(
				"EclihxLaunchConsole");

		myConsole.clearConsole();
		final MessageConsoleStream out = myConsole.newMessageStream();
		out.println(makeRelativePaths(output, haxeProject));
	}

	/**
	 * Method refreshes the output folder of the given project.
	 * 
	 * @param haxeProject the project for refreshing.
	 * @throws CoreException
	 */
	private void refreshOutputFolder(final IHaxeProject haxeProject)
			throws CoreException {

		final IFolder outputFolder = haxeProject.getOutputFolder().getBaseFolder();

		outputFolder.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
	}

	/**
	 * Parses compile errors from the text output of the compiler.
	 * 
	 * @param output the string with the compiler output.
	 * @param haxeProject the builded haXe project.
	 * @param buildFileName name of the build file. 
	 * @return List of the compile errors.
	 * 
	 *         TODO 9 Are there exist warnings in haXe? Can't we process them.
	 *         TODO 4 Parsing of the errors should be moved to the launch level.
	 */
	private List<ICompilerError> getCompilerErrors(final String output,
			final IHaxeProject haxeProject, String buildFileName) {

		final IHaxeOutputErrorsParser errorsParser = new HaxeOutputErrorsParser();
		return errorsParser.parseErrors(makeRelativePaths(output, haxeProject), 
				makeRelativePaths(buildFileName, haxeProject));
	}

	/**
	 * Method deletes old markers and places markers from the last build.
	 * @param haxeProject
	 * @param compileErrors
	 * @throws CoreException
	 */
	private void updateErrorsMarkers(final IHaxeProject haxeProject, String buildFilePath,
			final List<ICompilerError> compileErrors) throws CoreException {

		haxeProject.getProjectBase().deleteMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_INFINITE);

		for (final ICompilerError error : compileErrors) {
			
			if (isAddedToLocalResource(error, haxeProject)) {
				
			} else if (isAddedToBuildFile(error, haxeProject, buildFilePath)) {
				
			} else {
				EclihxUIPlugin.getLogHelper().logError(
						"Can't find a resource for marker attachment: " 
						+ error.getFilePath() + " " + buildFilePath);
				
				throw new RuntimeException("Can't find a resource for marker attachment");
			}
		}
	}

	private boolean isAddedToBuildFile(ICompilerError error, 
			IHaxeProject haxeProject, String buildFilePath) throws CoreException {
		
		IPath buildPath = new Path(buildFilePath);
		if (buildPath.isAbsolute()) {
			buildPath = buildPath.makeRelativeTo(haxeProject.getProjectBase().getLocation());
		}
		
		final IFile buildFile = haxeProject.getProjectBase().getFile(buildPath);
		if (buildFile.exists()) {
			final IMarker market = buildFile.createMarker(IMarker.PROBLEM);
			market.setAttributes(array(IMarker.SEVERITY, IMarker.MESSAGE), 
					             array((Object) IMarker.SEVERITY_ERROR, error.toString()));
			return true;
		}
		
		return false;
	}

	private boolean isAddedToLocalResource(ICompilerError error, IHaxeProject haxeProject) throws CoreException {
		IPath filePath = new Path(error.getFilePath());
		if (filePath.isAbsolute()) {
			filePath = filePath.makeRelativeTo(haxeProject.getProjectBase().getLocation());
		}
		
		final IResource fileResource = haxeProject.getProjectBase().findMember(filePath);
		if (fileResource != null) {
			final IFile file = (IFile)fileResource;
			final IMarker marker = file.createMarker(IMarker.PROBLEM);

			marker.setAttributes(array(IMarker.SEVERITY, IMarker.MESSAGE,
					IMarker.LINE_NUMBER), array(
					(Object) IMarker.SEVERITY_ERROR, error.getMessage(),
					error.getLineNumber()));
			return true;
		}
		
		return false;
	}

	/**
	 * Show notification dialog about errors in the build. Method should be
	 * executed in the UI thread.
	 * 
	 * @param haxeProjectName the name of the project which was unsuccessfully
	 *        builded
	 */
	private void showErrorsDialog(final String haxeProjectName) {

		final EclihxUIPlugin uiPlugin = EclihxUIPlugin.getDefault();
		final IPreferenceStore store = uiPlugin.getPreferenceStore();
		final boolean alwaysShowProblemView = store
				.getBoolean(PreferenceConstants.HAXE_ALWAYS_OPEN_PROBLEM_VIEW_ON_ERRORS);

		IWorkbenchWindow window = uiPlugin.getWorkbench()
				.getActiveWorkbenchWindow();

		if (window == null) {
			EclihxUIPlugin.getLogHelper().logError(
					"Not a UI-thread or absence of active workbench window");
			return;
		}

		if (!alwaysShowProblemView) {
			final String errorMessage = String.format(
					"There were errors during building of %s project.\n\n"
							+ "Do you want to open Problems View?",
					haxeProjectName);

			final MessageDialogWithToggle errorDialog = MessageDialogWithToggle
					.openYesNoQuestion(
							window.getShell(),
							"haXe build errors",
							errorMessage,
							"Always open problem view after error build",
							false,
							EclihxUIPlugin.getDefault().getPreferenceStore(),
							PreferenceConstants.HAXE_ALWAYS_OPEN_PROBLEM_VIEW_ON_ERRORS);

			if (errorDialog.getReturnCode() == IDialogConstants.NO_ID) {
				// User don't want to see problem view so return and omit
				// end of this method with activating the view.
				return;
			}
			
			if (errorDialog.getToggleState()) {
				EclihxUIPlugin.getDefault().getPreferenceStore().setValue(
						PreferenceConstants.HAXE_ALWAYS_OPEN_PROBLEM_VIEW_ON_ERRORS, 
						true);
			}			
		}

		try {
			window.getActivePage().showView(IPageLayout.ID_PROBLEM_VIEW);
		} catch (final PartInitException e) {
			EclihxUIPlugin.getLogHelper().logError(e);
		}
	}

	/**
	 * Method for graphical notification about the haXe project launch result.
	 * This method is expected to be called from the UI-thread.
	 * 
	 * @param finishInfo Information about launching result.
	 */
	private void uiThreadFinishHandler(final FinishLaunchInfo finishInfo) {
		try {

			final IHaxeProject haxeProject = EclihxCore.getDefault()
					.getHaxeWorkspace().getHaxeProject(
							finishInfo.getProjectName());

			if (haxeProject == null) {
				EclihxUIPlugin.getLogHelper().logError(
						"Launching of non-haXe project");
				return;
			}

			refreshOutputFolder(haxeProject);
			printOutputToConsole(finishInfo.getOutput(), haxeProject);

			final List<ICompilerError> compileErrors = getCompilerErrors(
					finishInfo.getOutput(), haxeProject, finishInfo.getBuildFile());

			updateErrorsMarkers(haxeProject, finishInfo.getBuildFile(), compileErrors);

			if (!compileErrors.isEmpty()) {
				showErrorsDialog(haxeProject.getName());
			}

		} catch (final CoreException e) {
			EclihxUIPlugin.getLogHelper().logError(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime
	 * .IStatus, java.lang.Object)
	 */
	@Override
	public Object handleStatus(final IStatus status, final Object source)
			throws CoreException {

		// Move to UI thread with asynchronous call
		// TODO 2 This is very common code so it should be moved to a common
		// place.
		Display display;
		display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}

		display.asyncExec(new Runnable() {
			public void run() {
				uiThreadFinishHandler((FinishLaunchInfo) source);
			}
		});

		return null;
	}
}

