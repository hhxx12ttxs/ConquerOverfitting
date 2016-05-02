package parselink;

import java.io.FileInputStream;
import java.util.List;
import java.util.Stack;

import main.MainWindow;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import sdt.ActionInformation;
import sdt.ConditionInformation;
import tts.StateForTTS;
import tts.TransitionForTTS;
import variableinformation.FODInformation;
import variableinformation.FSMInformation;
import variableinformation.GenericInformation;
import variableinformation.InputInformation;
import variableinformation.OutputInformation;
import variableinformation.SDTInformation;
import variableinformation.TTSInformation;
import fod.TransitionForFOD;
import fsm.StateForFSM;
import fsm.TransitionForFSM;

public class XmlParsing {
	MainWindow mainWindow;
	Stack<Object> xmlStack = new Stack<Object>();           //xml?? ??? stack
    Stack<String> stateStack = new Stack<String>();         //state? stack

    FODInformation fodInformation = new FODInformation();
    
    public int nodeCount=0;
    
    public XmlParsing(MainWindow mainWindow)
    {
        this.mainWindow = mainWindow;
        
    }
    public FODInformation LoadNuSCRXml(String filePath)
    {
    	Document document = null;

    	try{
    		document = new SAXBuilder().build(new FileInputStream(filePath));
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    	}

    	//root element ??
    	Element element = document.getRootElement();    
    	
    	//root element? ?? ??
    	System.out.println("Root Element Name :"+element.getName()+"\n");
    	
    	//child element? ??? ????
    	List childElementList = element.getChildren();

    	//root FOD??
        CreateRootFOD((Element)childElementList.get(0));    	
    	
        HandlingElement(element);
        return this.fodInformation;
    }
    
    public void HandlingElement(Element element)
    {
    	List childElementList = element.getChildren();
    	if(childElementList.size()==0)
    		return;
    	for(int i=0;i<childElementList.size();i++)
    	{
    		Element childElement =(Element)childElementList.get(i);
    		String elementName = ((Element)childElementList.get(i)).getName();
    		System.out.println("Child element Name:"+elementName);
    		
    		if(elementName=="FOD")
    		{ CreateFOD(childElement); }
    		else if(elementName=="SDT")
    		{ CreateSDT(childElement); }
    		else if(elementName=="condition")
    		{ CreateCondition(childElement); }
    		else if(elementName=="action")
    		{ CreateAction(childElement); }
    		else if(elementName=="FSM")
    		{ CreateFSM(childElement); }
    		else if(elementName=="TTS")
    		{ CreateTTS(childElement); }
    		else if(elementName=="states")
    		{ CreateStates(childElement); }
    		else if(elementName=="transitions")
    		{ CreateTransition(childElement); }
    		else if(elementName=="input")
    		{ CreateInput(childElement); }
    		else if(elementName=="output")
    		{ CreateOutput(childElement); }
    		//else if(elementName=="nodes")
    		//{}
    		//else if(elementName=="gml")
    		//{}
    		if(((Element)childElementList.get(i)).getChildren().size()!=0)
    			HandlingElement((Element)childElementList.get(i));
    		System.out.println();
    	}
    }
    
    public void CreateRootFOD(Element element)
    {
    	nodeCount++;
    	String[] ss;
    	ss = element.getAttribute("clock").getValue().toString().split(";");
    	for(String s:ss)
    		this.fodInformation.localClockVarialbles.add(s); //???? 
    	
    	ss = element.getAttribute("constants").getValue().toString().split(";");
    	for(String s:ss)
    		this.fodInformation.constants.add(s); 
    	
    	ss = element.getAttribute("description").getValue().toString().split(";");
    	for(String s:ss)
    		this.fodInformation.description.add(s); 
    	
    	this.fodInformation.id=element.getAttribute("id").toString();
    	
    	ss = element.getAttribute("memoVar").getValue().toString().split(";");
    	for(String s:ss)
    		this.fodInformation.momoriazbleVariableofExternalinputs.add(s); 
    	
    	this.fodInformation.name = element.getAttribute("name").getValue().toString();
    	
    	//(6) nodecount
    	
    	this.fodInformation.previousStateVariable = element.getAttribute("prevStateVar").getValue().toString();
    	
    	ss = element.getAttribute("templateNum").getValue().toString().split(";");
    	for(String s:ss)
    		this.fodInformation.templeteNumber.add(s); 
    	
    	//(9) transitioncount    	        
        xmlStack.push(fodInformation);
    }
    
    public void CreateFOD(Element element)
    {
    	nodeCount++;
    	Object o = xmlStack.peek();
        FODInformation parent = (FODInformation)o;            //fod? ??? ??fod??.
        FODInformation fodInformation = new FODInformation();
        String[] ss;
    	ss = element.getAttribute("clock").getValue().toString().split(";");
    	for(String s:ss)
    		fodInformation.localClockVarialbles.add(s); //???? 
    	
    	ss = element.getAttribute("constants").getValue().toString().split(";");
    	for(String s:ss)
    		fodInformation.constants.add(s); 
    	
    	ss = element.getAttribute("description").getValue().toString().split(";");
    	for(String s:ss)
    		fodInformation.description.add(s); 
    	
    	fodInformation.id=element.getAttribute("id").getValue().toString();
    	
    	ss = element.getAttribute("memoVar").getValue().toString().split(";");
    	for(String s:ss)
    		fodInformation.momoriazbleVariableofExternalinputs.add(s); 
    	
    	fodInformation.name = element.getAttribute("name").getValue().toString();
    	
    	//(6) nodecount
    	
    	fodInformation.previousStateVariable = element.getAttribute("prevStateVar").getValue().toString();
    	
    	ss = element.getAttribute("templateNum").getValue().toString().split(";");
    	for(String s:ss)
    		fodInformation.templeteNumber.add(s); 
    	
    	//(9) transitioncount    

        parent.fods.add(fodInformation);
        //xmlStack.pop();
        xmlStack.push(fodInformation);
    }
    
    public void CreateSDT(Element element)
    {
    	nodeCount++;
        Object o = xmlStack.peek();
        FODInformation parent = (FODInformation)o;            //sdt? ??? ?? fod??.                
        SDTInformation sdtInformation = new SDTInformation();
        String[] ss;
        ss = element.getAttribute("clock").getValue().toString().split(";");
        for(String s:ss)
        	sdtInformation.localClockVarialbles.add(s);    //????
        
        ss = element.getAttribute("constants").getValue().toString().split(";");
        for(String s:ss)
        	sdtInformation.constants.add(s);
        
        ss = element.getAttribute("description").getValue().toString().split(";");
        for(String s:ss)
        	sdtInformation.description.add(s);
        
        
        sdtInformation.id = element.getAttribute("id").getValue().toString();
        
        ss = element.getAttribute("memoVar").getValue().toString().split(";");
        for(String s:ss)
        	sdtInformation.momoriazbleVariableofExternalinputs.add(s);
        
        
        sdtInformation.name = element.getAttribute("name").getValue().toString();
                
        sdtInformation.previousStateVariable = element.getAttribute("prevStateVar").getValue().toString();
        
        ss = element.getAttribute("templateNum").getValue().toString().split(";");
        for(String s:ss)
        	sdtInformation.templeteNumber.add(s);
        //sdtInformation.yindex = element.GetAttribute(8);

        parent.sdts.add(sdtInformation);
        xmlStack.push(sdtInformation);
    }

    public void CreateCondition(Element element)    //for SDT
    {
    	
        Object o = xmlStack.peek();
        SDTInformation sdtInformation = (SDTInformation)o;

        int nCol = Integer.parseInt(element.getAttribute("nCol").getValue().toString());
        int nRow = Integer.parseInt(element.getAttribute("nRow").getValue().toString());        
        int conditionCount = -1;

        List childElementList = element.getChildren();
        
        for(int i=0;i<childElementList.size();i++)
        {
        	String s = ((Element)childElementList.get(i)).getAttribute("col").getValue().toString();
        	if (s.equals("0"))
            {     //??? condition??
                ConditionInformation conditionInformation = new ConditionInformation();
                conditionInformation.condition = ((Element)childElementList.get(i)).getAttribute("value").getValue().toString();
                if (conditionInformation.condition != "")
                {
                    sdtInformation.conditions.add(conditionInformation);
                    conditionCount++;                           
                }
                else
                {
                    for (int j = 0; j < nCol-1; j++)
                    {                        
                        i++;
                    }                        
                }
            }
            else
            {       //???? ??? condition? content??
                ((ConditionInformation)(sdtInformation.conditions.get(conditionCount))).contents.add
                (((Element)childElementList.get(i)).getAttribute("value").getValue().toString());
            }                    
        }        
    }
    
    public void CreateAction(Element element)
    {
        Object o = xmlStack.peek();
        SDTInformation sdtInformation = (SDTInformation)o;


        int nCol = Integer.parseInt(element.getAttribute("nCol").getValue().toString());
        int nRow = Integer.parseInt(element.getAttribute("nRow").getValue().toString());                
        int actionCount = -1;

        List childElementList = element.getChildren();
        
        for(int i=0;i<childElementList.size();i++)
        {  
        	String s = ((Element)childElementList.get(i)).getAttribute("col").getValue().toString();
            if (s.equals("0"))
            {     //??? action??
                ActionInformation actionInformation = new ActionInformation();                    
                actionInformation.action = ((Element)childElementList.get(i)).getAttribute("value").getValue().toString();
                if(actionInformation.action!="")
                {
                    sdtInformation.actions.add(actionInformation);
                    actionCount++;
                }
            }
            else
            {       //???? ??? action? content??
            	((ActionInformation)(sdtInformation.actions.get(actionCount))).contents.add
                (((Element)childElementList.get(i)).getAttribute("value").getValue().toString());
            }            
        }
        
        xmlStack.pop();
    }  
    
    public void CreateFSM(Element element)
    {
    	nodeCount++;
    	Object o = xmlStack.peek();
        FODInformation parent = (FODInformation)o;            //sdt? ??? ?? fod??.                
        FSMInformation fsmInformation = new FSMInformation();
        String[] ss;
        ss = element.getAttribute("clock").getValue().toString().split(";");
        for(String s:ss)
        	fsmInformation.localClockVarialbles.add(s);    //????
        
        ss = element.getAttribute("constants").getValue().toString().split(";");
        for(String s:ss)
        	fsmInformation.constants.add(s);
        
        ss = element.getAttribute("description").getValue().toString().split(";");
        for(String s:ss)
        	fsmInformation.description.add(s);
        
        
        fsmInformation.id = element.getAttribute("id").getValue().toString();
        
        ss = element.getAttribute("memoVar").getValue().toString().split(";");
        for(String s:ss)
        	fsmInformation.momoriazbleVariableofExternalinputs.add(s);
        
        fsmInformation.initReference = element.getAttribute("initialRef").getValue().toString();
        
        fsmInformation.name = element.getAttribute("name").getValue().toString();
                
        fsmInformation.previousStateVariable = element.getAttribute("prevStateVar").getValue().toString();
        
        ss = element.getAttribute("templateNum").getValue().toString().split(";");
        for(String s:ss)
        	fsmInformation.templeteNumber.add(s);
        //sdtInformation.yindex = element.GetAttribute(8);
        
        parent.fsms.add(fsmInformation);
        xmlStack.push(fsmInformation);
    }

    public void CreateTTS(Element element)
    {
    	nodeCount++;
    	Object o = xmlStack.peek();
        FODInformation parent = (FODInformation)o;            //sdt? ??? ?? fod??.                
        TTSInformation ttsInformation = new TTSInformation();
        String[] ss;
        ss = element.getAttribute("clock").getValue().toString().split(";");
        for(String s:ss)
        	ttsInformation.localClockVarialbles.add(s);    //????
        
        ss = element.getAttribute("constants").getValue().toString().split(";");
        for(String s:ss)
        	ttsInformation.constants.add(s);
        
        ss = element.getAttribute("description").getValue().toString().split(";");
        for(String s:ss)
        	ttsInformation.description.add(s);        
        
        ttsInformation.id = element.getAttribute("id").getValue().toString();
        
        ss = element.getAttribute("memoVar").getValue().toString().split(";");
        for(String s:ss)
        	ttsInformation.momoriazbleVariableofExternalinputs.add(s);
        
        ttsInformation.initReference = element.getAttribute("initialRef").getValue().toString();
        
        ttsInformation.name = element.getAttribute("name").getValue().toString();
                
        ttsInformation.previousStateVariable = element.getAttribute("prevStateVar").getValue().toString();
        
        ss = element.getAttribute("templateNum").getValue().toString().split(";");
        for(String s:ss)
        	ttsInformation.templeteNumber.add(s);
        //sdtInformation.yindex = element.GetAttribute(8);
        
        parent.ttss.add(ttsInformation);
        xmlStack.push(ttsInformation);
    }

    public void CreateStates(Element element)
    {    	
    	List childElementList = element.getChildren();
    	
        if (xmlStack.peek() instanceof FSMInformation)
        {
            FSMInformation fsmInformation = (FSMInformation)xmlStack.peek();
            
            for(int i=0;i<childElementList.size();i++)
            {
            	StateForFSM state = new StateForFSM();
            	state.id = (((Element)childElementList.get(i)).getAttribute("id").getValue().toString());
            	state.name = (((Element)childElementList.get(i)).getAttribute("name").getValue().toString());
            	if(state.name.charAt(state.name.length()-1)=='_')
            		continue;
            	fsmInformation.states.add(state);
            }            
        }

        else if (xmlStack.peek() instanceof TTSInformation)
        {
            TTSInformation ttsInformation = (TTSInformation)xmlStack.peek();
            for(int i=0;i<childElementList.size();i++)
            {
            	StateForTTS state = new StateForTTS();
            	state.id = (((Element)childElementList.get(i)).getAttribute("id").getValue().toString());
            	state.name = (((Element)childElementList.get(i)).getAttribute("name").getValue().toString());
            	ttsInformation.states.add(state);
            }             
        }
    }

    public void CreateTransition(Element element)
    {
       nodeCount++;
    	// Element && states ?? element ??
    	List childElementList = element.getChildren();
    	
    	if (xmlStack.peek() instanceof FSMInformation)
        {
            FSMInformation fsmInformation = (FSMInformation)xmlStack.peek();
            for(int i=0;i<childElementList.size();i++)
            {
            	TransitionForFSM transition = new TransitionForFSM();
            	transition.id = (((Element)childElementList.get(i)).getAttribute("id").getValue().toString());
            	List subChildElementList =((Element)childElementList.get(i)).getChildren();
            	
            	transition.sourceid = ((Element)subChildElementList.get(0)).getAttribute("refId").getValue().toString();
            	transition.sourcename = ((Element)subChildElementList.get(0)).getAttribute("refName").getValue().toString();
            	
            	transition.targetid = ((Element)subChildElementList.get(1)).getAttribute("refId").getValue().toString();
                transition.targetname = ((Element)subChildElementList.get(1)).getAttribute("refName").getValue().toString();
                
                transition.condition = ((Element)subChildElementList.get(2)).getValue().toString().replace("\n", "");
                
                transition.assignment = ((Element)subChildElementList.get(3)).getValue().toString().replace("\n", "");
                
                fsmInformation.transitions.add(transition);
            }           
        }

        else if (xmlStack.peek() instanceof TTSInformation)
        {
            TTSInformation ttsInformation = (TTSInformation)xmlStack.peek();
            for(int i=0;i<childElementList.size();i++)
            {                                    
                TransitionForTTS transition = new TransitionForTTS();                                                
                transition.id = (((Element)childElementList.get(i)).getAttribute("id").getValue().toString());
            	List subChildElementList =((Element)childElementList.get(i)).getChildren();
            	
            	transition.sourceid = ((Element)subChildElementList.get(0)).getAttribute("refId").getValue().toString();
            	transition.sourcename = ((Element)subChildElementList.get(0)).getAttribute("refName").getValue().toString();
            	
            	transition.targetid = ((Element)subChildElementList.get(1)).getAttribute("refId").getValue().toString();
                transition.targetname = ((Element)subChildElementList.get(1)).getAttribute("refName").getValue().toString();
                
                
                if(subChildElementList.size()==4)
                {
                	transition.condition = ((Element)subChildElementList.get(2)).getValue().toString();                    
                    transition.assignment = ((Element)subChildElementList.get(3)).getValue().toString();
                }
                else if(subChildElementList.size()==5) 
                {
                	transition.condition = "["+((Element)subChildElementList.get(2)).getAttribute("start").getValue().toString()+",";
                    transition.condition += ((Element)subChildElementList.get(2)).getAttribute("end").getValue().toString()+"]";
                	
                	transition.condition += "("+((Element)subChildElementList.get(3)).getValue().toString()+")";                    
                    transition.assignment = ((Element)subChildElementList.get(4)).getValue().toString().replace("\n", "");;
                }                
                
                ttsInformation.transitions.add(transition);                
            }
        }
    	
        else if (xmlStack.peek() instanceof FODInformation)
        {
        	FODInformation fodInformation  = (FODInformation)xmlStack.peek();
        	for(int i=0;i<childElementList.size();i++)
            {
        		TransitionForFOD transition = new TransitionForFOD();
        		transition.sourceid = transition.id = (((Element)childElementList.get(i)).getAttribute("id").getValue().toString());
        		
        		List subChildelementList = ((Element)childElementList.get(i)).getChildren();
        		
        		transition.sourceid = ((Element)subChildelementList.get(0)).getAttribute("refId").getValue().toString();
            	transition.sourcename = ((Element)subChildelementList.get(0)).getAttribute("refName").getValue().toString();
            	
            	transition.targetid = ((Element)subChildelementList.get(1)).getAttribute("refId").getValue().toString();
                transition.targetname = ((Element)subChildelementList.get(1)).getAttribute("refName").getValue().toString();     
                
                fodInformation.transitions.add(transition);
            }
        }
    	
    	xmlStack.pop();
    }
    
    public void CreateInput(Element element)
    {
        Object o = xmlStack.peek();
        GenericInformation genericInformation = (GenericInformation)o;
        InputInformation inputInformation = new InputInformation();
        inputInformation.id = (element.getAttribute("id").getValue().toString());
        inputInformation.name = (element.getAttribute("name").getValue().toString());        
        genericInformation.inputs.add(inputInformation);
    }
    
    public void CreateOutput(Element element)
    {
        Object o = xmlStack.peek();
        GenericInformation genericInformation = (GenericInformation)o;
        OutputInformation inputInformation = new OutputInformation();
        inputInformation.id = (element.getAttribute("id").getValue().toString());
        inputInformation.name = (element.getAttribute("name").getValue().toString());        
        genericInformation.outputs.add(inputInformation);        
    }
}

