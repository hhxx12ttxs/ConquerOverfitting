/*******************************************************************************
 * Copyright (c) 2010 Gyรถrgy Orosz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gyรถrgy Orosz - initial API and implementation
 ******************************************************************************/
package org.erlide.wrangler.refactoring.codeinspection;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.erlide.core.erlang.ErlangCore;
import org.erlide.core.erlang.IErlElement;
import org.erlide.core.erlang.IErlFunctionClause;
import org.erlide.core.erlang.IErlModule;
import org.erlide.jinterface.rpc.RpcResult;
import org.erlide.wrangler.refactoring.backend.WranglerBackendManager;
import org.erlide.wrangler.refactoring.codeinspection.ui.InputDialogWithCheckbox;
import org.erlide.wrangler.refactoring.selection.IErlSelection;
import org.erlide.wrangler.refactoring.util.GlobalParameters;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangLong;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangRangeException;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

import erlang.FunctionRef;

/**
 * Handles commands, which needs only a listing UI, and inspects the code.
 * 
 * @author Gyorgy Orosz
 * 
 */
public class SimpleCodeInspectionHandler extends AbstractHandler implements
		IHandler {
	protected final class IntegerInputValidator implements IInputValidator {
		@Override
		public String isValid(String newText) {
			try {
				Integer.parseInt(newText);
				return null;
			} catch (Exception e) {
				return "Please type an integer!";
			}

		}
	}

	static protected final String LARGE_MODULES_VIEW_ID = "largemodules";
	static protected final String DEPENECIES_1_VIEW_ID = "dependencies1";
	static protected final String DEPENECIES_2_VIEW_ID = "dependencies2";
	static protected final String NON_TAIL_RECURSIVE_VIEW_ID = "nontailrecursive";
	static protected final String NOT_FLUSH_UNKNOWN_MESSAGES = "notflush";
	static protected final String NESTED_EXPRESSIONS = "nested";
	static protected final String LONG_FUNCTIONS = "longfunctions";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String actionId = event.getCommand().getId();
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor().setFocus();
		GlobalParameters.setSelection(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getSelection());

		IErlSelection wranglerSelection = GlobalParameters
				.getWranglerSelection();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();

		if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.largemodules")) {
			handleLargeModulesCall(wranglerSelection, shell);

		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.dependencies")) {
			handleDepenenciesCall(wranglerSelection, shell);
		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.nontailrecursive")) {
			handleNonTailRecursiveCall(wranglerSelection, shell);
		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.notflush")) {
			handleNotFlushUnknownMessages(wranglerSelection, shell);
		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.nestedif")) {
			handleNested(wranglerSelection, shell, "if");

		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.nestedcase")) {
			handleNested(wranglerSelection, shell, "case");

		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.nestedreceive")) {
			handleNested(wranglerSelection, shell, "receive");

		} else if (actionId
				.equals("org.erlide.wrangler.refactoring.codeinspection.longfunctions")) {
			handleLongFunctions(wranglerSelection, shell);
		}

		return event;
	}

	private void handleLongFunctions(IErlSelection wranglerSelection,
			Shell shell) {
		try {
			CodeInspectionViewsManager.hideView(LONG_FUNCTIONS);
			// call inputdialog
			InputDialogWithCheckbox dialog = new InputDialogWithCheckbox(shell,
					"Search for long functions", "Number of lines:",
					"Search in the project", "", new IntegerInputValidator());
			if (InputDialogWithCheckbox.OK == dialog.open()) {
				int linesVal = Integer.parseInt(dialog.getValue());
				boolean inProject = dialog.isCheckBoxChecked();
				RpcResult res = null;
				if (inProject) {
					res = WranglerBackendManager.getRefactoringBackend()
							.callInspection("long_functions_in_dirs_eclipse",
									"ixi", linesVal,
									wranglerSelection.getSearchPath(),
									GlobalParameters.getTabWidth());
				} else {
					res = WranglerBackendManager.getRefactoringBackend()
							.callInspection("long_functions_in_file_eclipse",
									"sixi", wranglerSelection.getFilePath(),
									linesVal,
									wranglerSelection.getSearchPath(),
									GlobalParameters.getTabWidth());
				}
				// handle rpc
				ArrayList<IErlElement> elements = processFunctionResult(shell,
						res);
				if (elements == null)
					return;
				// show result
				if (!elements.isEmpty()) {
					CodeInspectionViewsManager.showErlElements(
							"Long functions", elements, LONG_FUNCTIONS);
				} else {
					MessageDialog.openInformation(shell, "No result",
							"Could not found any function which is longer, than "
									+ linesVal + " lines.");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void handleNested(IErlSelection wranglerSelection, Shell shell,
			String type) {
		try {
			CodeInspectionViewsManager.hideView(NESTED_EXPRESSIONS + type);
			// call inputdialog
			InputDialogWithCheckbox dialog = new InputDialogWithCheckbox(shell,
					"Search for nested expression", "Nest level:",
					"Search in the project", "", new IntegerInputValidator());
			if (InputDialogWithCheckbox.OK == dialog.open()) {
				int nestedVal = Integer.parseInt(dialog.getValue());
				boolean inProject = dialog.isCheckBoxChecked();
				RpcResult res = null;
				if (inProject) {
					res = WranglerBackendManager.getRefactoringBackend()
							.callInspection("nested_exprs_in_dirs_eclipse",
									"iaxi", nestedVal, type,
									wranglerSelection.getSearchPath(),
									GlobalParameters.getTabWidth());
				} else {
					res = WranglerBackendManager.getRefactoringBackend()
							.callInspection("nested_exprs_in_file_eclipse",
									"siaxi", wranglerSelection.getFilePath(),
									nestedVal, type,
									wranglerSelection.getSearchPath(),
									GlobalParameters.getTabWidth());
				}
				// handle rpc
				ArrayList<IErlElement> elements = processFunctionResult(shell,
						res);
				if (elements == null)
					return;
				// show result
				if (!elements.isEmpty()) {
					CodeInspectionViewsManager.showErlElements("Nested " + type
							+ " expressions", elements, NESTED_EXPRESSIONS
							+ type);
				} else {
					MessageDialog
							.openInformation(shell, "No result",
									"Could not found any " + nestedVal
											+ " levels nested " + type
											+ " expression!");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void handleNotFlushUnknownMessages(IErlSelection wranglerSelection,
			Shell shell) {
		String inFile = "not_flush_unknown_messages_in_file_eclipse";
		String inProject = "not_flush_unknown_messages_in_dirs_eclipse";
		CodeInspectionViewsManager.hideView(NOT_FLUSH_UNKNOWN_MESSAGES);
		Boolean answer = MessageDialog.openQuestion(shell,
				"Find incomplete receive patterns",
				"Would you like to run the scan in the whole project?");
		try {
			RpcResult result = null;
			String function = "";
			if (answer) {
				function = inProject;
				result = WranglerBackendManager.getRefactoringBackend()
						.callInspection(function, "xi",
								wranglerSelection.getSearchPath(),
								GlobalParameters.getTabWidth());
			} else {
				function = inFile;
				result = WranglerBackendManager.getRefactoringBackend()
						.callInspection(function, "sxi",
								wranglerSelection.getFilePath(),
								wranglerSelection.getSearchPath(),
								GlobalParameters.getTabWidth());
			}

			ArrayList<IErlElement> elements = processFunctionResult(shell,
					result);

			if (elements == null)
				return;
			if (!elements.isEmpty()) {
				CodeInspectionViewsManager.showErlElements(
						"Incomplete receive patterns", elements,
						NOT_FLUSH_UNKNOWN_MESSAGES);
			} else {
				MessageDialog.openInformation(shell, "No result",
						"Could not found any incomplete receive patterns!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<IErlElement> processFunctionResult(Shell shell,
			RpcResult result) throws OtpErlangRangeException {
		ArrayList<IErlElement> elements = new ArrayList<IErlElement>();
		OtpErlangObject obj = result.getValue();
		OtpErlangTuple restuple = (OtpErlangTuple) obj;
		OtpErlangAtom resindicator = (OtpErlangAtom) restuple.elementAt(0);
		if (resindicator.atomValue().equals("ok")) {
			OtpErlangList erlangFunctionList = (OtpErlangList) restuple
					.elementAt(1);
			for (int i = 0; i < erlangFunctionList.arity(); ++i) {
				OtpErlangTuple fTuple = (OtpErlangTuple) erlangFunctionList
						.elementAt(i);
				IErlFunctionClause f = extractFunction(fTuple);
				elements.add(f);
			}
		} else {
			OtpErlangString s = (OtpErlangString) restuple.elementAt(1);
			MessageDialog.openError(shell, "Error", s.stringValue());
			return null;
		}
		return elements;
	}

	private void handleNonTailRecursiveCall(IErlSelection wranglerSelection,
			Shell shell) {
		CodeInspectionViewsManager.hideView(NON_TAIL_RECURSIVE_VIEW_ID);

		try {
			String inFile = "non_tail_recursive_servers_in_file_eclipse";
			String inProject = "non_tail_recursive_servers_in_dirs_eclipse";
			Boolean answer = MessageDialog.openQuestion(shell,
					"Find non tail recursive servers",
					"Would you like to run the scan in the whole project?");
			String function = "";
			RpcResult res = null;
			if (!answer) {
				function = inFile;
				res = WranglerBackendManager.getRefactoringBackend()
						.callInspection(function, "sxi",
								wranglerSelection.getFilePath(),
								wranglerSelection.getSearchPath(),
								GlobalParameters.getTabWidth());
			} else {
				function = inProject;
				res = WranglerBackendManager.getRefactoringBackend()
						.callInspection(function, "xi",
								wranglerSelection.getSearchPath(),
								GlobalParameters.getTabWidth());
			}

			ArrayList<IErlElement> elements = processFunctionResult(shell, res);

			if (elements == null)
				return;
			if (!elements.isEmpty()) {
				CodeInspectionViewsManager.showErlElements(
						"Non tail recursive servers", elements,
						NON_TAIL_RECURSIVE_VIEW_ID);
			} else {
				MessageDialog.openInformation(shell, "No result",
						"Could not found any non tail recursive server!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private IErlFunctionClause extractFunction(OtpErlangTuple fTuple)
			throws OtpErlangRangeException {
		IErlModule mod = extractModule(fTuple.elementAt(0));
		String function = ((OtpErlangAtom) fTuple.elementAt(1)).atomValue();
		int arity = ((OtpErlangLong) fTuple.elementAt(2)).intValue();
		IErlFunctionClause f = mod.getModel().findFunction(
				new FunctionRef(mod.getModuleName(), function, arity));
		return f;
	}

	private void handleDepenenciesCall(IErlSelection wranglerSelection,
			Shell shell) {
		// hiding the views
		CodeInspectionViewsManager.hideView(
				CodeInspectionViewsManager.CODE_INSPECTION_VIEW,
				DEPENECIES_1_VIEW_ID);
		CodeInspectionViewsManager.hideView(
				CodeInspectionViewsManager.CODE_INSPECTION_VIEW,
				DEPENECIES_2_VIEW_ID);

		// run the rpc
		try {
			RpcResult res = WranglerBackendManager.getRefactoringBackend()
					.callInspection("dependencies_of_a_module_eclipse", "sx",
							wranglerSelection.getFilePath(),
							wranglerSelection.getSearchPath());

			ArrayList<IErlElement> modules1 = new ArrayList<IErlElement>();
			ArrayList<IErlElement> modules2 = new ArrayList<IErlElement>();

			OtpErlangObject obj = res.getValue();
			OtpErlangTuple restuple = (OtpErlangTuple) obj;
			OtpErlangAtom resindicator = (OtpErlangAtom) restuple.elementAt(0);
			if (resindicator.atomValue().equals("ok")) {
				OtpErlangTuple listtuple = (OtpErlangTuple) restuple
						.elementAt(1);
				OtpErlangList modList1 = (OtpErlangList) listtuple.elementAt(0);
				OtpErlangList modList2 = (OtpErlangList) listtuple.elementAt(1);
				modules1 = createErlMOduleList(modList1);
				modules2 = createErlMOduleList(modList2);
			} else {
				OtpErlangString s = (OtpErlangString) restuple.elementAt(1);
				MessageDialog.openError(shell, "Error", s.stringValue());
				return;
			}

			if (!modules1.isEmpty())
				CodeInspectionViewsManager.showErlElements(
						"Modules which depends on "
								+ wranglerSelection.getErlElement().getModule()
										.getName(), modules1,
						DEPENECIES_1_VIEW_ID);
			if (!modules2.isEmpty())
				CodeInspectionViewsManager.showErlElements("Modules, on which "
						+ wranglerSelection.getErlElement().getModule()
								.getName() + " depends", modules2,
						DEPENECIES_2_VIEW_ID);
			else
				MessageDialog
						.openInformation(shell, "No result",
								"There is no large module with the specified parameter!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<IErlElement> createErlMOduleList(OtpErlangList modList) {
		ArrayList<IErlElement> modules = new ArrayList<IErlElement>();
		for (int i = 0; i < modList.arity(); ++i) {
			IErlModule m = extractModule(modList.elementAt(i));
			modules.add(m);
		}
		return modules;
	}

	private IErlModule extractModule(OtpErlangObject m) {
		String name = "";
		if (m instanceof OtpErlangString) {
			OtpErlangString element = (OtpErlangString) m;
			name = element.stringValue();
		} else if (m instanceof OtpErlangAtom) {
			OtpErlangAtom atom = (OtpErlangAtom) m;
			name = atom.atomValue();
		}
		String[] modNameParts = name.split("/");
		IErlModule mod = ErlangCore.getModel().findModule(
				modNameParts[modNameParts.length - 1]);
		return mod;
	}

	private void handleLargeModulesCall(IErlSelection wranglerSelection,
			Shell shell) {
		CodeInspectionViewsManager.hideView(
				CodeInspectionViewsManager.CODE_INSPECTION_VIEW,
				LARGE_MODULES_VIEW_ID);

		InputDialog dialog = new InputDialog(shell, "Lines of a large module",
				"Lines of a large module:", "", new IntegerInputValidator());
		int ret = dialog.open();
		if (ret == InputDialog.CANCEL)
			return;
		int lines = Integer.parseInt(dialog.getValue());
		RpcResult res = WranglerBackendManager.getRefactoringBackend()
				.callInspection("large_modules_eclipse", "ixi", lines,
						wranglerSelection.getSearchPath(),
						GlobalParameters.getTabWidth());

		ArrayList<IErlElement> modules = new ArrayList<IErlElement>();
		try {
			OtpErlangObject obj = res.getValue();
			OtpErlangTuple restuple = (OtpErlangTuple) obj;
			OtpErlangAtom resindicator = (OtpErlangAtom) restuple.elementAt(0);
			if (resindicator.atomValue().equals("ok")) {

				OtpErlangList modList = (OtpErlangList) restuple.elementAt(1);
				modules = createErlMOduleList(modList);
			} else {
				OtpErlangString s = (OtpErlangString) restuple.elementAt(1);
				MessageDialog.openError(shell, "Error", s.stringValue());
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!modules.isEmpty())
			CodeInspectionViewsManager.showErlElements("Large modules",
					modules, LARGE_MODULES_VIEW_ID);
		else
			MessageDialog.openInformation(shell, "No result",
					"There is no large module with the specified parameter!");
	}
}

