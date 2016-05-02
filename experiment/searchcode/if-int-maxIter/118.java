/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hpccsystems.pentaho.job.eclmlbuildclassify;

import java.util.ArrayList;
import java.util.List;
import org.hpccsystems.javaecl.ML_BuildClassify;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.compatibility.Value;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;
import org.hpccsystems.ecljobentrybase.*;
/**
 *
 * @author ChambersJ
 */
public class ECLML_BuildClassify extends ECLJobEntry{//extends JobEntryBase implements Cloneable, JobEntryInterface {
    
    //private String jobName;
    private String name = "";

    private String recordsetName = "";
    

    private String dependentVar = ""; // 1
    private String independentVar = ""; // 2
    private String classifyType = "";
    private String learnType = "";
    
    private String ridge = "0.00001";
    private String epsilon = "0.000000001";
    private String maxIter = "200";
    
    private String passes = "5";
    private String alpha = "0.1";


    public String getDependentVar() {
        return dependentVar;
    }

    public void setDependentVar(String dependentVar) {
        this.dependentVar = dependentVar;
    }

    public String getIndependentVar() {
        return independentVar;
    }

    public void setIndependentVar(String independentVar) {
        this.independentVar = independentVar;
    }

    public String getRecordsetName() {
        return recordsetName;
    }

    public void setRecordsetName(String recordsetName) {
        this.recordsetName = recordsetName;
    }

    public String getClassifyType() {
        return classifyType;
    }

    public void setClassifyType(String classifyType) {
        this.classifyType = classifyType;
    }

    public String getLearnType() {
        return learnType;
    }

    public void setLearnType(String learnType) {
        this.learnType = learnType;
    }
        


    public String getRidge() {
		return ridge;
	}

	public void setRidge(String ridge) {
		this.ridge = ridge;
	}

	public String getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(String epsilon) {
		this.epsilon = epsilon;
	}

	public String getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(String maxIter) {
		this.maxIter = maxIter;
	}

	public String getPasses() {
		return passes;
	}

	public void setPasses(String passes) {
		this.passes = passes;
	}

	public String getAlpha() {
		return alpha;
	}

	public void setAlpha(String alpha) {
		this.alpha = alpha;
	}
    
    
    

    @Override
    public Result execute(Result prevResult, int k) throws KettleException {
        
        Result result = prevResult;
        
        
    
    
        ML_BuildClassify direct = new ML_BuildClassify();
        direct.setName(this.getRecordsetName());
       

        direct.setRecordsetName(this.getRecordsetName());
        direct.setDependentVar(dependentVar);
        direct.setIndependentVar(independentVar);
        direct.setClassifyType(classifyType);
        direct.setLearnType(learnType);

        
        direct.setRidge(ridge);
        direct.setEpsilon(epsilon);
        direct.setMaxIter(maxIter);
        direct.setPasses(passes);
        direct.setAlpha(alpha);
        
        
        //private Text algType; //NaiveBayes, Logistic    
        //private Text dependentVar; // 1
        //private Text independentVar; // 2
        
        //ml.setIterations(this.getIterations());
        //ml.setThreshold(this.getThreshold());
        //ml.setThreshold(this.getThreshold());


        logBasic("{Iterate Job} Execute = " + direct.ecl());
        
        logBasic("{Iterate Job} Previous =" + result.getLogText());
        
        result.setResult(true);
        
        RowMetaAndData data = new RowMetaAndData();
        data.addValue("ecl", Value.VALUE_TYPE_STRING, direct.ecl());
        
        
        List list = result.getRows();
        list.add(data);
        String eclCode = "";
        
          
        if (list == null) {
            list = new ArrayList();
        } else {
            
            for (int i = 0; i < list.size(); i++) {
                RowMetaAndData rowData = (RowMetaAndData) list.get(i);
                String code = rowData.getString("ecl", null);
                if (code != null) {
                    eclCode += code;
                }
            }
            logBasic("{Iterate Job} ECL Code =" + eclCode);
        }
        
        result.setRows(list);
        
        
        return result;
    }

    @Override
    public void loadXML(Node node, List<DatabaseMeta> list, List<SlaveServer> list1, Repository rpstr) throws KettleXMLException {
        try {
            super.loadXML(node, list, list1);
        
          if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"recordset_name")) != null)
            this.setRecordsetName(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"recordset_name")));

             
          if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"dependentVar")) != null)
            this.setDependentVar(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"dependentVar")));
          
          if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"independentVar")) != null)
            this.setIndependentVar(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"independentVar")));
          
          //classifyType
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"classifyType")) != null)
                this.setClassifyType(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"classifyType")));
            
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"learnType")) != null)
                this.setLearnType(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"learnType")));
            
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"alpha")) != null)
                this.setAlpha(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"alpha")));
            
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"passes")) != null)
                this.setPasses(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"passes")));
            
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"ridge")) != null)
                this.setRidge(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"ridge")));
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"epsilon")) != null)
                this.setEpsilon(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"epsilon")));
            if(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"maxIter")) != null)
                this.setMaxIter(XMLHandler.getNodeValue(XMLHandler.getSubNode(node,"maxIter")));
            
        } catch (Exception e) {
            throw new KettleXMLException("ECL Distribute Job Plugin Unable to read step info from XML node", e);
        }

    }

    public String getXML() {
        String retval = "";
        
        retval += super.getXML();
      
        retval += "             <recordset_name eclIsDef=\"true\" eclType=\"recordset\"><![CDATA["+this.recordsetName+"]]></recordset_name>"+Const.CR;
                
        
        retval += "             <dependentVar><![CDATA["+this.dependentVar+"]]></dependentVar>"+Const.CR;
        retval += "             <independentVar><![CDATA["+this.independentVar+"]]></independentVar>"+Const.CR;
        //classifyType
        retval += "             <classifyType><![CDATA["+this.classifyType+"]]></classifyType>"+Const.CR;
        retval += "             <learnType><![CDATA["+this.learnType+"]]></learnType>"+Const.CR;
        retval += "             <alpha><![CDATA["+this.alpha+"]]></alpha>"+Const.CR;
        retval += "             <passes><![CDATA["+this.passes+"]]></passes>"+Const.CR;
        
        retval += "             <ridge><![CDATA["+this.ridge+"]]></ridge>"+Const.CR;
        retval += "             <epsilon><![CDATA["+this.epsilon+"]]></epsilon>"+Const.CR;
        retval += "             <maxIter><![CDATA["+this.maxIter+"]]></maxIter>"+Const.CR;
        return retval;

    }

    public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
            throws KettleException {
        try {

  
            if(rep.getStepAttributeString(id_jobentry, "recordset_name") != null)
                recordsetName = rep.getStepAttributeString(id_jobentry, "recordset_name");
            
            
            
            if(rep.getStepAttributeString(id_jobentry, "dependentVar") != null)
                dependentVar = rep.getStepAttributeString(id_jobentry, "dependentVar");
            
            if(rep.getStepAttributeString(id_jobentry, "independentVar") != null)
                independentVar = rep.getStepAttributeString(id_jobentry, "independentVar");
            
            //classifyType
            if(rep.getStepAttributeString(id_jobentry, "classifyType") != null)
                classifyType = rep.getStepAttributeString(id_jobentry, "classifyType");
            
             if(rep.getStepAttributeString(id_jobentry, "learnType") != null)
                learnType = rep.getStepAttributeString(id_jobentry, "learnType");
             
             if(rep.getStepAttributeString(id_jobentry, "alpha") != null)
                 alpha = rep.getStepAttributeString(id_jobentry, "alpha");
             if(rep.getStepAttributeString(id_jobentry, "passes") != null)
                 passes = rep.getStepAttributeString(id_jobentry, "passes");
             
             if(rep.getStepAttributeString(id_jobentry, "epsilon") != null)
                 epsilon = rep.getStepAttributeString(id_jobentry, "epsilon");
             if(rep.getStepAttributeString(id_jobentry, "ridge") != null)
                 ridge = rep.getStepAttributeString(id_jobentry, "ridge");
             if(rep.getStepAttributeString(id_jobentry, "maxIter") != null)
                 maxIter = rep.getStepAttributeString(id_jobentry, "maxIter");
            
        } catch (Exception e) {
            throw new KettleException("Unexpected Exception", e);
        }
    }

    public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
        try {

            rep.saveStepAttribute(id_job, getObjectId(), "recordset_name", recordsetName);
            
            //private Text algType; //NaiveBayes, Logistic    
        //private Text dependentVar; // 1
        //private Text independentVar; // 2
            
            
            rep.saveStepAttribute(id_job, getObjectId(), "dependentVar", dependentVar);
            rep.saveStepAttribute(id_job, getObjectId(), "independentVar", independentVar);
           //classifyType
            rep.saveStepAttribute(id_job, getObjectId(), "classifyType", classifyType);
            rep.saveStepAttribute(id_job, getObjectId(), "learnType", learnType);
            
            rep.saveStepAttribute(id_job, getObjectId(), "alpha", alpha);
            rep.saveStepAttribute(id_job, getObjectId(), "passes", passes);
            
            rep.saveStepAttribute(id_job, getObjectId(), "ridge", ridge);
            rep.saveStepAttribute(id_job, getObjectId(), "epsilon", epsilon);
            rep.saveStepAttribute(id_job, getObjectId(), "maxIter", maxIter);
            
        } catch (Exception e) {
            throw new KettleException("Unable to save info into repository" + id_job, e);
        }
    }

    public boolean evaluates() {
        return true;
    }

    public boolean isUnconditional() {
        return true;
    }
}

