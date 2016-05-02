package zom.dong.sourceconverter.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import zom.dong.sourceconverter.handler.GetSourceInfo;

public class SecondDialog extends TitleAreaDialog {

	private GetSourceInfo sourceInfo;
	private Text[] pathTextList;	//패스경로
	private Text[] fileTextList;	//파일이름
	private Button[] checkList;	
	private List<Integer> selectPath = new ArrayList<Integer>();
	

	public SecondDialog(Shell parentShell, GetSourceInfo sourceInfo) {
		super(parentShell);
		//this.checkMethod = checkMethod;
		this.sourceInfo = sourceInfo;
	}

	@Override
	public void create() {
		super.create();
		// Set the title
		setTitle("경로 생성");
		// Set the message
		setMessage("생성하거나 수정될 소스 경로를 맞쳐주십시오.\n Model 경로는 꼭 맞쳐주셔야 합니다.", IMessageProvider.INFORMATION);

	}
	

	@Override
	protected Control createDialogArea(Composite parent) {
		
		GridLayout layout = new GridLayout(4, false);
		parent.setLayout(layout);
		
		pathTextList = new Text[9];
		fileTextList = new Text[9];
		checkList = new Button[9];
		
		makeButtonText(parent, "Dao", sourceInfo.getDaoPath(), sourceInfo.getDaoFile());
		makeButtonText(parent, "DaoImpl", sourceInfo.getDaoImplPath(), sourceInfo.getDaoImplFile());
	
	
		makeButtonText(parent, "DaoTest", sourceInfo.getDaoTestPath(), sourceInfo.getDaoTestFile());
	
		makeButtonText(parent, "Service", sourceInfo.getServicePath(), sourceInfo.getServiceFile());
		makeButtonText(parent, "ServiceImpl", sourceInfo.getServiceImplPath(), sourceInfo.getServiceImplFile());
	
		makeButtonText(parent, "ServiceTest", sourceInfo.getServiceTestPath(), sourceInfo.getServiceTestFile());
	
		makeButtonText(parent, "Web", sourceInfo.getWebPath(), sourceInfo.getWebFile());
		
		makeButtonText(parent, "Model", sourceInfo.getModelPath(), " ");
		
		makeButtonText(parent, "Html", sourceInfo.getHtmlPath(), " ");
		
		return parent;
	}
	
	private int gInx = 0;
	
	private void makeButtonText(Composite parent, String title, String path, String fileName){
		
		Button buttons = new Button(parent, SWT.PUSH);
		buttons.setText(title);
		buttons.setData("id", gInx);
		buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		checkList[gInx] = new Button(parent, SWT.CHECK);
		checkList[gInx].setData("id", gInx);
		
		checkList[gInx].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button t = (Button)e.getSource();
				
				if(t.getSelection()){
					pathTextList[(Integer)t.getData("id")].setEnabled(true);
					fileTextList[(Integer)t.getData("id")].setEnabled(true);
				} else {
					pathTextList[(Integer)t.getData("id")].setEnabled(false);
					fileTextList[(Integer)t.getData("id")].setEnabled(false);
				}
				
				if((Integer)t.getData("id") == 8){
					fileTextList[(Integer)t.getData("id")].setEnabled(false);
				}
			}
		});
		
	    
		pathTextList[gInx] = new Text(parent, SWT.SINGLE | SWT.BORDER);
		pathTextList[gInx].setText(path);
		pathTextList[gInx].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pathTextList[gInx].setData("title", title);
		
		
		fileTextList[gInx] = new Text(parent, SWT.SINGLE | SWT.BORDER);
		fileTextList[gInx].setText(fileName);
		fileTextList[gInx].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileTextList[gInx].setData("title", title);
		
		
		if(gInx == 0 || gInx == 1 || gInx == 3 || gInx == 4 || gInx == 7){
			checkList[gInx].setSelection(true);
		} else {
			pathTextList[gInx].setEnabled(false);
			fileTextList[gInx].setEnabled(false);
		}
		
		if(gInx == 7){
			checkList[gInx].setEnabled(false);
			fileTextList[gInx].setEnabled(false);
		}
		
		buttons.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button t = (Button)e.getSource();
				
				openDirectoryDialog((Integer)t.getData("id"));
			        
			}
		});
		
		gInx++;
	}
	
	private void openDirectoryDialog(int id){
		
		DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.SAVE);
        fd.setText("폴더 저장");
        fd.setFilterPath(pathTextList[id].getText());
        String selected = fd.open();
        
        if(selected != null){
        	 pathTextList[id].setText(selected);
        }
       
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);
		// Create Add button
		// Own method as we need to overview the SelectionAdapter
		createOkButton(parent, OK, "NEXT", true);

		// Create Cancel button
		Button cancelButton = createButton(parent, CANCEL, "Cancel", false);
		// Add a SelectionListener
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	protected Button createOkButton(Composite parent, int id, String label, boolean defaultButton) {
		
		// increment the number of columns in the button bar
		((GridLayout) parent.getLayout()).numColumns++;
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(new Integer(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (isValidInput()) {
					okPressed();
				}
			}
		});
		
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		
		setButtonLayoutData(button);
		return button;
	}

	private boolean isValidInput() {
		boolean valid = true;
		/*
		if (firstNameText.getText().length() == 0) {
			setErrorMessage("Please maintain the first name");
			valid = false;
		}
		if (lastNameText.getText().length() == 0) {
			setErrorMessage("Please maintain the last name");
			valid = false;
		}
		*/
		return valid;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * TODO Javadoc주석작성
	 *
	 */
	private void saveInput() {
		
		//경로 저장
		for(Text text : pathTextList){
			if(text == null){continue;}
			
			String title = (String)text.getData("title");
			
			if(title.equals("Dao")){
				sourceInfo.setDaoPath(text.getText());
			} else if(title.equals("DaoImpl")){
				sourceInfo.setDaoImplPath(text.getText());
			} else if(title.equals("DaoTest")){
				sourceInfo.setDaoTestPath(text.getText());
			} else if(title.equals("Service")){
				sourceInfo.setServicePath(text.getText());
			} else if(title.equals("ServiceImpl")){
				sourceInfo.setServiceImplPath(text.getText());
			} else if(title.equals("ServiceTest")){
				sourceInfo.setServiceTestPath(text.getText());
			} else if(title.equals("Web")){
				sourceInfo.setWebPath(text.getText());
			} else if(title.equals("Model")){
				sourceInfo.setModelPath(text.getText());
			} else if(title.equals("Html")){
				sourceInfo.setHtmlPath(text.getText());
			}
			
		}
		
		//파일이름 저장
		for(Text text : fileTextList){
			if(text == null){continue;}
			String title = (String)text.getData("title");
			
			if(title.equals("Dao")){
				sourceInfo.setDaoFile(text.getText());
			} else if(title.equals("DaoImpl")){
				sourceInfo.setDaoImplFile(text.getText());
			} else if(title.equals("DaoTest")){
				sourceInfo.setDaoTestFile(text.getText());
			} else if(title.equals("Service")){
				sourceInfo.setServiceFile(text.getText());
			} else if(title.equals("ServiceImpl")){
				sourceInfo.setServiceImplFile(text.getText());
			} else if(title.equals("ServiceTest")){
				sourceInfo.setServiceTestFile(text.getText());
			} else if(title.equals("Web")){
				sourceInfo.setWebFile(text.getText());
			} 
			
		}
	}
	

	@Override
	protected void okPressed() {
		
		//체크된 목록 
		for(Button bc : checkList){
			if(bc.getSelection()){
				int data = (Integer)bc.getData("id");
				int sd = 0;
				if(data == 0 ||data == 1){
					sd = 1;
				} else if(data == 2 ){
					sd = 3;
				} else if(data == 3 || data == 4 ){
					sd = 2;
				} else if (data == 5){
					sd = 4;
				} else if (data == 6){
					sd = 5;
				} else if (data == 8){
					sd = 6;
				}
				
				selectPath.add(sd);
			}
		}
		
		saveInput();
		super.okPressed();
	}
	
	public List<Integer> getSelectList(){
		
		return selectPath;
	}
	
}


