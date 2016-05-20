package supersecretproject.Instances;

import info.jeppes.ZoneCore.ZonePlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import supersecretproject.Loader;
import supersecretproject.SSPAPI;

public abstract class InstanceStageLoader extends InstanceStarter{
    private enum LoadStageProcess{
        FILE,
        PACKAGE
    }
    private ArrayList<Stage> stages = new ArrayList();
    private boolean stagesLoaded = false;
    
    //next 2 variables are used to reload without specifying which method to use
    private LoadStageProcess loadProcessUsed = null;
    private String directory = null;
    
    
    public ArrayList<Stage> getStages() {
        return stages;
    }
    public Stage getStage(int stage){
        return stages.get(stage);
    }
    public void setStages(ArrayList<Stage> stages) {
        this.stages = stages;
    }
    
    public boolean isStagesLoaded() {
        return stagesLoaded;
    }
    
    public void reloadStages(){
        if(stagesLoaded){
            switch(loadProcessUsed){
                case FILE:
                    loadStagesFromClassFiles(directory);
                    break;
                case PACKAGE:
                    loadStagesFromPackage();
                    break;
            }
        }
    }
    
    public void loadStagesFromClassFiles(String directory){
        if(stagesLoaded){
            return;
        }
        stages.clear();
        ArrayList<Object> javaObjectsFromDirectory = Loader.getJavaObjectsFromDirectory(directory);
        
        for(Object obj : javaObjectsFromDirectory){
            if(obj instanceof Stage){
                stages.add((Stage)obj);
            }
        }
        this.directory = directory;
        stagesLoaded = true;
        loadProcessUsed = LoadStageProcess.FILE;
    }
    
    public void loadStagesFromPackage(){
        loadStagesFromPackage(getClass().getPackage().getName()+".Stages");
    }
    public void loadStagesFromPackage(String packageName){
        if(stagesLoaded){
            return;
        }
        stages.clear();
        Class[] classesInPackage = SSPAPI.getPlugin().getClassesInPackage(packageName,null);
        for(Class commandClass : classesInPackage){
            try {
                Object obj = commandClass.newInstance();
                if(obj instanceof Stage){
                    stages.add((Stage) obj);
                }
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ZonePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        stagesLoaded = true;
        loadProcessUsed = LoadStageProcess.PACKAGE;
    }
}

