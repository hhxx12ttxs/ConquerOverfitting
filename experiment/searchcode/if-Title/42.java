package zom.dong.sourceconverter.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.events.KeyListener;

import org.eclipse.swt.layout.GridData;

import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import org.eclipse.swt.widgets.Label;

import org.eclipse.swt.widgets.Text;

import zom.dong.sourceconverter.handler.GetSourceInfo;

public class FormPageOne extends WizardPage {

	private Composite container;
	
	private GetSourceInfo sourceInfo;
	private Text[] pathTextList;	//패스경로
	private Text[] fileTextList;	//파일이름
	private Button[] checkList;	
	private List<Integer> selectPath = new ArrayList<Integer>();

	public FormPageOne(GetSourceInfo sourceInfo) {

		super("formPageOne");

		setTitle("경로 생성");
		setDescription("생성하거나 수정될 소스 경로를 맞쳐주십시오.\n Model 경로는 꼭 맞쳐주셔야 합니다.");
		
		this.sourceInfo = sourceInfo;

	}

	public void createControl(Composite parent) {
		
		container = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(4, false);
		container.setLayout(layout);
		
		pathTextList = new Text[9];
		fileTextList = new Text[9];
		checkList = new Button[9];
		
		makeButtonText("Dao", sourceInfo.getDaoPath(), sourceInfo.getDaoFile());
		makeButtonText("DaoImpl", sourceInfo.getDaoImplPath(), sourceInfo.getDaoImplFile());
	
	
		makeButtonText("DaoTest", sourceInfo.getDaoTestPath(), sourceInfo.getDaoTestFile());
	
		makeButtonText("Service", sourceInfo.getServicePath(), sourceInfo.getServiceFile());
		makeButtonText("ServiceImpl", sourceInfo.getServiceImplPath(), sourceInfo.getServiceImplFile());
	
		makeButtonText("ServiceTest", sourceInfo.getServiceTestPath(), sourceInfo.getServiceTestFile());
	
		makeButtonText("Web", sourceInfo.getWebPath(), sourceInfo.getWebFile());
		
		makeButtonText( "Model", sourceInfo.getModelPath(), " ");
		
		makeButtonText("Html", sourceInfo.getHtmlPath(), " ");
		
		
		setControl(container);
		//setPageComplete(true);

	}

	
	
	private int gInx = 0;
	
	private void makeButtonText(String title, String path, String fileName){
		
		Button buttons = new Button(container, SWT.PUSH);
		buttons.setText(title);
		buttons.setData("id", gInx);
		buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		buttons.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button t = (Button)e.getSource();
				
				openDirectoryDialog((Integer)t.getData("id"));
			        
			}
		});
		
		checkList[gInx] = new Button(container, SWT.CHECK);
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
		
	    
		pathTextList[gInx] = new Text(container, SWT.SINGLE | SWT.BORDER);
		pathTextList[gInx].setText(path);
		pathTextList[gInx].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pathTextList[gInx].setData("title", title);
		pathTextList[gInx].setData("id", gInx);
		
		pathTextList[gInx].addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Button t = (Button)e.getSource();
				
				openDirectoryDialog((Integer)t.getData("id"));
			        
			}
		});
		
		fileTextList[gInx] = new Text(container, SWT.SINGLE | SWT.BORDER);
		fileTextList[gInx].setText(fileName);
		fileTextList[gInx].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fileTextList[gInx].setData("title", title);
		
		
		if(gInx == 0 || gInx == 1 || gInx == 3 || gInx == 4 || gInx == 7){
			checkList[gInx].setSelection(true);
		} else {
			pathTextList[gInx].setEnabled(false);
			fileTextList[gInx].setEnabled(false);
		}
		
		if(gInx == 7 || gInx == 0 || gInx == 1){
			checkList[gInx].setEnabled(false);
			fileTextList[gInx].setEnabled(false);
		}
		
		
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
	/*
	public IWizardPage getNextPage() {
		
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
		return super.getNextPage();
	}
	*/
	
	
	/**
	 * 경로 리스트 목록
	 * @return
	 */
	public void setSelectList(){
		
		//체크된 목록 
		for(Button bc : checkList){
			if(bc.getSelection()){
				int data = (Integer)bc.getData("id");
				/*
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
				*/
				
				selectPath.add(data);
			}
		}
		
	}
	
	
	public List<Integer> getSelectList(){
		return this.selectPath;
	}
	
	/**
	 * 리턴 경로 계산
	 */
	public void saveInput() {
		
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
	
	public GetSourceInfo getSourceInfo(){
		
		return sourceInfo;
	}
	
}

