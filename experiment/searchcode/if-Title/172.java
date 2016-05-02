package MFS;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ibm.icu.util.StringTokenizer;

public class ReadFile {
	
	private MFS mfs_;
	private boolean readFinished_ = false;
	
		
	public ReadFile(String fileName) throws Exception{
		try{
			this.mfs_ = new MFS(fileName);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			String line = "";
			
			while( (line = br.readLine() ) !=null && !this.readFinished_){
				readData(line, br);
			}
			
			if( this.mfs_.getSurfacePoints() == null ){
				this.mfs_.setSurfacePoints();
			}

		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	private void readData(String title, BufferedReader br) throws Exception{
		title = title.toUpperCase();
		if(title.startsWith("MATERIAL")){
			title = readMaterial(br);
		}
		else if( title.startsWith("BOUNDARY")){
			
			title = readBoundary( br ).toUpperCase();
		}
	}
	
	private String readMaterial( BufferedReader br ) throws Exception{
		String line = br.readLine();
		
		StringTokenizer stk = new StringTokenizer(line);
			
		double E = Double.parseDouble(stk.nextToken());
		double nu = Double.parseDouble(stk.nextToken());
		
		this.mfs_.setE(E);
		this.mfs_.setNu(nu);
		
		return line;
		
	}
	
	private String readBoundary( BufferedReader br ) throws Exception{
		String line = br.readLine();
		
		while( !isTitleLine(line) ){
			StringTokenizer stk = new StringTokenizer(line);
			int no = Integer.parseInt( stk.nextToken() );
			
			double x = Double.parseDouble( stk.nextToken() );
			double y = Double.parseDouble( stk.nextToken() );
			
			char boundaryType = stk.nextToken().toCharArray()[0];
			
			double bcX = Double.parseDouble(stk.nextToken());
			double bcY = Double.parseDouble(stk.nextToken());
			
			double normalX = Double.parseDouble(stk.nextToken());
			double normalY = Double.parseDouble(stk.nextToken());
			
			BoundaryPoint boundaryPoint = new BoundaryPoint(no, x, y, boundaryType, bcX, bcY, normalX, normalY);
			
			this.mfs_.addBoundaryPoint(boundaryPoint);
			
			line = br.readLine();
			
		}
		
		return line;
		
	}
	
	private boolean isTitleLine(String line){
		line = line.toUpperCase();
		
		String[] titleList = {"MATERIAL", "BOUNDARY", "EOF"};
		
		for(String command : titleList){
			if( line.startsWith(command)){
				return true;
			}
		}
		
		return false;
	}
	
	public MFS getMFS(){
		return this.mfs_;
	}

}

