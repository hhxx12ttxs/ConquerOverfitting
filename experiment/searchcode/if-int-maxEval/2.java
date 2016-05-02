import java.io.*;
import java.util.*;

public class POOArticle{
	public static final int MAXEVAL = 1024;
	private String id = null;
	private String arthor = null;
	private POOUser user = null;
	private String title = null;
	private String content = null;
	private String path = null;
	private int length = 0;
	private int evaluationCount = 0;
	

	private String [] evaluationMessages = null;

	public POOArticle(String path, POOUser user){

		
		String [] temp;
		this.user = user;
		this.path = path;
		temp = path.split("/");
		String [] splitedAttr = temp[temp.length-1].split("_");

		this.id = splitedAttr[0];
		if(id.length() == 1)id = "00" + id;
		else if(id.length() == 2)id = "0" + id;


		this.title = splitedAttr[1];
		this.arthor = splitedAttr[2];
		Scanner scan = null;
		
		
		try {
			scan = new Scanner(new File(this.path));
		}catch (FileNotFoundException e){
			System.err.println("File not found");


		}

		
		if(scan.hasNextLine())this.content = scan.nextLine();

		evaluationMessages = new String[MAXEVAL];
		String line = null;

		
		while(scan.hasNextLine()){
			line = scan.nextLine();
			evaluationMessages[length++] = line;
			String [] tokens = line.split(" ");
			if(tokens[0].equals("push")){
				evaluationCount++;
			}else if(tokens[0].equals("boo")){
				evaluationCount--;
			}
			
		}

		
		
	}

	public void push(String message){
		if(length + 1 >= MAXEVAL){
			System.out.println("The evaluation box has reached the maximum capacity");
			return;

		}
		try{
			FileWriter fw = new FileWriter(path,true); //the true will append the new data
		    fw.write("push " + user.getName() + ":" + message + "\n");//appends the string to the file
		    fw.close();
		}catch(IOException ioe){
		    System.err.println("IOException: " + ioe.getMessage());
		}
		
	}
	public void boo(String message){
		if(length + 1 >= MAXEVAL){
			System.out.println("The evaluation box has reached the maximum capacity");
			return;

		}
		try{
			FileWriter fw = new FileWriter(path,true); //the true will append the new data
		    fw.write("boo  " + user.getName() + ":" + message + "\n");//appends the string to the file
		    fw.close();
		}catch(IOException ioe){
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}
	public void arrow(String message){
		if(length + 1 >= MAXEVAL){
			System.out.println("The evaluation box has reached the maximum capacity");
			return;

		}
		try{
			FileWriter fw = new FileWriter(path,true); //the true will append the new data
		    fw.write("->   " + user.getName() + ":" + message + "\n");//appends the string to the file
		    fw.close();
		}catch(IOException ioe){
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public void show(){
		list();
		System.out.println("Content: " + (content == null ? "": content));
		System.out.println("***************Evaluation Message****************");

		for(int i = 0;i < length;i++){
			if(evaluationMessages[i] != null)System.out.println(evaluationMessages[i]);

		}
		System.out.println("");



	}

	public void list(){
		System.out.println("");
		System.out.println("Id: " + id);
		System.out.println("Title: " + title);
		System.out.println("Arthor: " + arthor);
		System.out.println("Evaluatoin Count: " + evaluationCount);



	}






}
