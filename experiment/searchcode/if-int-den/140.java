/* Controller Final */


/* author akis */
/* Log Akis 1/1/2011 */
/* prosthesa tis getNumberOfClasses
/* kai tis getNumberOfMethods 
/* kapois na ta symplirwsei stin sxediasi */

/* auti tin stigmi doulevw tin getDependencies */
/* na ftiaxw to getActiveWorkspaceCollectionOfPackages */
/* getActiveClassCollectionOfMethods */


/* Log Akis 6/1/2011 */
/* simplirwsa tis getActive* .. */
/* mpore$ na xreiasoun listes me ta epilegmena */
/* paketa, klaseis, methodous */
/* ftiaxnw tis doSearch... sinartiseis parakalw na */
/* tis deite, kai kapoios na ftiaxeis tis synartiseis tou */
/* Workspace */
/* logika oloklirwsa kai tin getDepencencies */
/* menei to debug kai na synenoithoume an  */
/* tha diaxwrisoume tin sysxetisi apo tin exartisi */


/* Log Akis 13/1/2011 */
/* ekana debug ton Controller */
/* allaksa ta orismata twn getToken apo int
se antikeimena gia na exw perasma me anafora */
/* mpainw se atermono loop, na paw sto simeio pou exei $*/

/*log akis 22/1/2011 */
/* ftiaxnw tis addMethod addClass... */
/* ftiaxnw tis searchMethod */
/* na peiraxw kai to theWorkspace.java */
/* prepei na symplirwsw me antistoixes searchXDependency */ 
/* oupou X=Method,Class
/* na teleiwsw to atermono loop */

/* log akis 23/1/2011 */
/* na thimamai oti o constructor kataxwreitai san methodos */
/* xm.... dystyxws apo tin ylopoiisi mia klasi ginetai na periexei mia klasi */
/*prepei stin theClass na yparxei mia collectionOfClasses episis */

/* log akis 24/1/2011 */
/* dystyxws den exw dwsei tin dynatotita ena */
/* paketo na periexei kai alla paketa, na to ftiaxw... */
/* parola auta den perousiazetai tetoia periptwsi sto */
/* paron ergaleio */
/* omws parousiazetai klasi mesa se klasi ... */

/* log akis 25/1/2011 */
/* yposthrizw pleon mia klasi mesa se alli */
/* an yparxei provlima logw megalyterou vathous */
/* tha to epanexetasw... */

/* log akis 26/1/2011 */
/*fainetai na exw swsti leitourgia */
/* menei na to elegxw.... */
/* prosthetw to gui .... */

/* log akis 30/1/2011 */
/* thelw na apofygw na epanaprosthetw paketo sta selected */
/* opote na valw mia search */
/* na valw me diplo click na deixnei tis klaseis mesa se klasi */
/* na ftiaxw to printToOutput */

/* exw mia metavliti ston controller expandingClasses pou */
/* thelw otan einai true na fainontai oi ypoklaseis ton klasewn */
/* ypenthimizw pws den exw epitrepsei enfwliasmo sta paketa kai  */
/* episis stis klaseis exw epitrepsei mono ena epipedo enfwliasmou */
/* gia tin wra ... */


import java.io.*;
import java.util.*;


public class Controller{
	/* kapoios na prosthesei tis ypoloipes */
	theParser parser;
	String retString;
	thePackage activePackage;
	theMethod  activeMethod;
	theClass activeClass;
	theWorkspace activeWorkspace;
	BufferedReader in;
	boolean expandingClasses;

	ArrayList<theClass> collectionOfClasses;
	ArrayList<theMethod> collectionOfMethods;
	ArrayList<theWorkspace> collectionOfWorkspaces;


	/* den xerw an xreiazontai ola auta */
	ArrayList<theClass> selectedClasses;
	ArrayList<thePackage> selectedPackages;
	ArrayList<theMethod> selectedMethods;
	
	/* main gia na dw oti paizei */

	public Controller(){
		expandingClasses=false;
		collectionOfClasses = new ArrayList<theClass>();
		collectionOfMethods = new ArrayList<theMethod>();
		collectionOfWorkspaces = new ArrayList<theWorkspace>();
		
		selectedClasses = new ArrayList<theClass>();
		selectedPackages = new ArrayList<thePackage>();
		selectedMethods = new ArrayList<theMethod>();
	}
	public static void main(String[] args){
		Controller theController = new Controller();
		theController.openFile("./input.txt");
	}

	ArrayList<theMethod> getActiveClassCollectionOfMethods(){
		return activeClass.getCollectionOfMethods();
	}

	ArrayList<thePackage> getActiveWorkspaceCollectionOfPackages(){
		return activeWorkspace.getCollectionOfPackages();
	}
	
	ArrayList<theClass> getActivePackageCollectionOfClasses(){
		return activePackage.getCollectionOfClasses();
	}
		

	// ypothetw oti exei proigithei klisei tis openFile;
	public void getData(){
		DataToken token=new DataToken();
		parser=new theParser(in);
		retString=parser.getTokenData(token);


		while(true){	
			System.out.println("get Data read: " +retString);
			System.out.println("");

			if(retString.equals("secondHalf"))
				break;

			else 	if(token.getValue()==0)
				packageHandler(token);
			else if(token.getValue()==4)
				classHandler(token);
			
			
			else{
				System.out.println("Error package expected!!!!");
				System.out.println("value="+token.getValue());
				System.exit(-1);
			}
		//	System.out.println("retString-> "+retString);
		}
		System.out.println("getting data finished ...");
	}

	/* synartisi pou kaleitai otan vrethei paketo sto arxeio
		eisodou */
	public void packageHandler(DataToken token){
		
			if((activePackage=doSearchPackage(retString))==null){
				activePackage = new thePackage(retString);
				System.out.println("packageHandler willing to add: "+retString);
				activeWorkspace.addPackage(activePackage);
			}
			else
				System.out.println("Package " +retString+ " already in");
			retString = parser.getTokenData(token);

			/* tsekarisma gia termatismo tu arxeiou */
			if(retString.equals("secondHalf"))
				return;

			System.out.println("value= "+token.getValue());
			if(token.getValue() == 0) packageHandler(token);
			else if (token.getValue() ==1)classHandler(token); 
			else 	System.out.println("Error class or package expected!!!!");

	}

	/* kaleitai otan vrethei klasi sto arxeio eisodou */

	public void classHandler(DataToken token){
		
		if(token.getValue()==1 && (activeClass=doSearchClass(retString))==null){
			activeClass = new theClass(retString);
			System.out.println("classHandler willing to add: "+retString);
			activePackage.addClass(activeClass);
			addToCollectionOfClasses(activeClass);
		}

		 
		else if(token.getValue()==4){ /* class in class*/
			theClass nc =new theClass(retString);
			System.out.println("classHandler Class in Class adding: "+retString);
			activeClass.addClass(nc);
			addToCollectionOfClasses(nc);
			}

		else	System.out.println("Class " +retString+ " already in");
		
		retString = parser.getTokenData(token);
		/* tsekarisma gia termatismo tu arxeiou */
			if(retString.equals("secondHalf"))
				return;

	if( token.getValue() ==1 || token.getValue()==4) classHandler(token); //another class
		else if(token.getValue() == 2)methodHandler(token);
		else System.out.println("Error! class or method excpected!!!!!");
	}

	/* kaleitai otan vrethei methodos sto arxeio eisodou */
	public void methodHandler(DataToken token){
		theClass innerClass;
		
		System.out.println("classInClass: " +token.getClassInClassName());
		System.out.println("class: " +token.getClassName());
		if((token.getClassInClassName())!=null){
			//an exoume eswteriki klasi pou theloume na gemisoume
			System.out.println("methodHandler willing to add: "+retString);
			System.out.println("Adding in inner class....");
			activeMethod = new theMethod(retString);
			innerClass=activeClass.searchClass(token.getClassInClassName());
			activeMethod= new theMethod(token.getMethodName());
			innerClass.addMethod(activeMethod);	
			addToCollectionOfMethods(activeMethod);
			
		}
		else	if((activeMethod=doSearchMethod(retString))==null){
			activeMethod = new theMethod(retString);
			System.out.println("adding in normal class ");
			System.out.println("methodHandler willing to add: "+retString);
			addToCollectionOfMethods(activeMethod);
			activeClass.addMethod(activeMethod);
		}
		else
			System.out.println("Method " +retString+ " already in");
		retString = parser.getTokenData(token);
		/* tsekarisma gia termatismo tu arxeiou */
			if(retString.equals("secondHalf"))
				return;

	if(token.getValue() == 2)methodHandler(token);
		else if( token.getValue() ==1) classHandler(token); //another class
		else ;
}
	

	/* kaleitai sto deutero miso tou arxeiou eisodu prokeimenou
		na eisagei sto systima tis exartiseis twn klasewn kai methodwn */

	
	public void getDependencies(){
			theMethod dependedMethod;
			theClass dependedClass;
			thePackage dependedPackage;

			DependencyToken token = new DependencyToken();

			theMethod onMethod;
			theClass onClass;
			thePackage onPackage;


			retString=parser.getTokenDependency(token); 
			while(true){
				if(retString.equals("")) //end of file
					break; 
				
				if(retString.equals("depends")){
					//edw analoga me to an className==methodName exw constructor
					//kai mporw na entopisw sysxetisi se sxesi me apli
					//exartisi
					/* oi doSearchX psaxoun sta energa X ex orismou */
					dependedPackage = doSearchPackage(token.getPackageName());
					System.out.println("Depended package-> "+dependedPackage);

					setActivePackage(dependedPackage);
					dependedClass = doSearchClass(token.getClassName());
					System.out.println("Depended class-> "+dependedClass);
					setActiveClass(dependedClass);

					if(token.getClassInClassName()==null){
						dependedMethod = doSearchMethod(token.getMethodName());
						System.out.println("not class in class ");
					}
					else{ //class in Class
						System.out.println("class  in class: "+token.getClassInClassName());
						dependedClass =dependedClass.searchClass(token.getClassInClassName());
						dependedMethod = dependedClass.searchMethod(token.getMethodName());
						System.out.println("Depended class in class-> "+dependedClass);
					}
					System.out.println("Depended Method-> "+dependedMethod);
					setActiveMethod(dependedMethod);
					retString=parser.getTokenDependency(token); 

					while(retString.equals("on")){
	 
						onPackage = doSearchPackage(token.getPackageName());
						setActivePackage(onPackage);

						onClass = doSearchClass(token.getClassName());
						setActiveClass(onClass);

						if(token.getClassInClassName()==null)
							onMethod = doSearchMethod(token.getMethodName());
						else{ //class in Class
							onClass =onClass.searchClass(token.getClassInClassName());
							onMethod = onClass.searchMethod(token.getMethodName());
						}
						setActiveMethod(onMethod);
					
						dependedClass.addClassDependency(onClass);
						/* se periptwsi pou den einai constructor */
						/* na to thimithw.... */
						dependedMethod.addMethodDependency(onMethod);
						retString=parser.getTokenDependency(token); 
					}
				//	else{ System.out.println("Dependency not well formed ");
				//			System.exit(-1);
				//	}
	
				}
			}
						System.out.println("Finished getting dependencies...");
	}


	
	/* peritti synartisi den xreiastike ylopoiisi */
	/* kathws  oi leitourgeies tis kalyptwntai apo alles synartiseis */
  
	public void setParser(){
	}

	/* anoigei to arxeio me to onoma theFilename 
	 kai tautoxrona trexei ton parser panw sto arxeio	
	 prosoxi!! thelw to theFilename na perilamvanei 
	 to plires monopati tou arxeiou */
	

	public void openFile(String filename){
		int wNum;
			try {
			in = new BufferedReader(new FileReader(filename) );
		}catch (Exception ex) {
			System.out.println("File error!!");
		 }

		wNum=getCollectionOfWorkspaces().size();
		activeWorkspace = doCreateNewWorkspace("Workspace "+wNum);
	
		getData();
		System.out.println("finished getting data");
		System.out.println("NEW WORKSPACE CREATED!!!!!!");
		System.out.println("__________"+activeWorkspace.getName()+"_________");
			
		activeWorkspace.projectPackages();
	
		System.out.println("_______________________________");
		
		getDependencies();
		System.out.println("finished getting depencdencies");
		try{
			in.close(); //kleinei tin eisodo
		}catch(Exception ex){System.out.println("Cannot close file");
			System.exit(-1);
		}
	}

	public void addToCollectionOfClasses(theClass aClass){
		collectionOfClasses.add(aClass);
	}
	
	/* prosthetei stin collectionOfWorkspaces */
	public void addToCollectionOfWorkspaces(theWorkspace aWorkspace){
		collectionOfWorkspaces.add(aWorkspace);
	}

	public void removeFromCollectionOfClasses(theClass aClass){
		collectionOfClasses.remove(aClass);
		
	}
	public void removeFromCollectionOfMethods(theMethod aMethod){
		collectionOfMethods.remove(aMethod);
	}

	public ArrayList<theWorkspace> getCollectionOfWorkspaces(){
		return collectionOfWorkspaces;
	}
	public void removeFromCollectionOfWorkspaces(theWorkspace aWorkspace){
		System.out.println("removing Workspace " +aWorkspace);
		collectionOfWorkspaces.remove(aWorkspace);
	}

	public ArrayList<theClass> getCollectionOfClasses(){
		return collectionOfClasses;
	}

	public ArrayList<theMethod> getCollectionOfMethods(){
		return collectionOfMethods;
	}

	
	public void addToCollectionOfMethods(theMethod aMethod){
		collectionOfMethods.add(aMethod);
	}

	
	public void setActiveWorkspace(theWorkspace aWorkspace){
		activeWorkspace=aWorkspace;
	}

	public void removeWorkspace(theWorkspace aWorkspace){
		
		System.out.println("removing Workspace: "+aWorkspace);
		collectionOfWorkspaces.remove(aWorkspace);
	}
		

	public theWorkspace getActiveWorkspace(){
		return activeWorkspace;
	}
	
	public thePackage getActivePackage(){
		return activePackage;
	}

	public theClass getActiveClass(){
		return activeClass;
	}
	public void setActivePackage(thePackage aPackage){
		activePackage = aPackage;
	
	}

	public void setActiveClass(theClass aClass){
		activeClass= aClass;
	}
	
	public void setActiveMethod(theMethod aMethod){
		activeMethod = aMethod;
	}

	public void setExpandingClasses(boolean ans){
		expandingClasses=ans;
	}

	public boolean getExpandingClasses(){
		return expandingClasses;
	}


	/* dimiourgei neo workspace , ton topothetei stin lista me tous
	workspace kai ton kanei active */

	 public theWorkspace doCreateNewWorkspace(String workspaceName){
		theWorkspace aWorkspace;
		aWorkspace = new theWorkspace(workspaceName);
		addToCollectionOfWorkspaces(aWorkspace);
		setActiveWorkspace(aWorkspace);
	
		return aWorkspace;
	 }

	public ArrayList<thePackage> getSelectedPackages(){
		return selectedPackages;
	}
	
	public ArrayList<theClass> getSelectedClasses(){
		return selectedClasses;
	}

	public ArrayList<theMethod> getSelectedMethods(){
		return selectedMethods;
	}

	public void addToSelectedClasses(theClass aClass){
		selectedClasses.add(aClass);
	}

	public void addToSelectedMethods(theMethod aMethod){
		selectedMethods.add(aMethod);
	}

	public void addToSelectedPackages(thePackage aPackage){
		selectedPackages.add(aPackage);
	}
  //den xreiazontai ta pairnw mazika apo to gui

	/* setarei mazika */
/*
	public void setSelectedMethods(ArrayList<theMethod> methodList){
		selectedMethods=methodList;
	}

	public void setSelectedPackages(ArrayList<thePackage> packageList){
		selectedPackages=packageList;
	}

	public void setSelectedClasses(ArrayList<theMethod> classesList){
		selectedClasses=classesList;
	}


*/
	public void clearSelectedPackages(){
		selectedPackages.clear();
	}

	public void clearSelectedClasses(){
		selectedClasses.clear();
	}

	public void clearSelectedMethods(){
		selectedMethods.clear();
			
	}
		
	
		
	/* oi anazitiseis ginontai ex orismou sta active dedomena */
	public thePackage doSearchPackage(String packageName){
			return activeWorkspace.searchPackage(packageName);
	}

	/* tis thelw Amesa!!! Akis 1/1/2011 */
	public theClass doSearchClass( String className){
			return activePackage.searchClass(className);
	}

	public theMethod doSearchMethod(String methodName){
			return activeClass.searchMethod(methodName);
	}


	/* mexri edw */


	/* prosthesa aftes tis synartiseis pou xreiazontai
	kai mallon tis xesasame */
	public int getNumberOfClasses(){
		return 0;
	}
	
	public int getNumberOfMethods(){
		return 0;
	}
	

	
}


