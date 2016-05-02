package proxy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Util {
	public static final String getRegEx = "^get[\\s]+.+[\\s]+.+";
	public static final String lsRegEx = "^ls$";
	public static final String cdRegEx = "^cd[\\s]+.+";
	public static final String exitRegEx = "^exit$";

	public static final String putRegEx = "^put[\\s]+.+[\\s]+.+";
	public static final String statusRegEx = "^status$";
	public static final String partitionRegEx = "^partitions$";
	public static final String loadRegEx = "^load[\\s]+[0-9]+[\\s].+";
	public static final String recoverRegEx = "^recover[\\s]+[0-9]+";
	public static final String migrateRegEx = "^migrate[\\s]+.+[\\s].+";
	public static final String delRegEx = "^delete[\\s]+.+";
	public static final String clearRegEx = "^clear[\\s]+.+";
	public static final String listRegEx = "^list[\\s]+.+";
	public static final String splitBySpace = "[\\s]+(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

	public static String getPartFilename(String file, int partition){
		return file + "." + partition + ".part";
	}

	public static int split(String file, int size) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(file));
		BufferedWriter[] bw = new BufferedWriter[size];
		int counter,written,buffer;
		int parity = 0;

		//Open files
		for(counter= 0; counter < size; counter++){
			bw[counter] = new BufferedWriter(new FileWriter(getPartFilename(file,counter)));
		}

		//Write to files and parity file		
		counter = 1;
		for(buffer = br.read(); buffer != -1; buffer = br.read()){
			bw[counter].write(buffer);
			parity = parity ^ buffer;

			counter++;
			if(counter == bw.length){
				bw[0].write(parity);
				parity = 0;
				counter = 1;
			}
		}

		written = counter;

		for(counter= 0; counter < size; counter++){
			bw[counter].close();
		}
		br.close();

		return written;
	}

	//size is the number of partitions, count is the byte count of the file size
	public static void merge(String file, int size) throws IOException{
		FileReader[] fr = new FileReader[size];
		BufferedWriter bw = new BufferedWriter(new FileWriter(Proxy.dir + file));
		int counter,buffer;

		//Only get the data partitions
		for(counter= 1; counter < size; counter++){
			fr[counter] = new FileReader(getPartFilename(Proxy.dir + file,counter));
		}

		counter = 1;
		for(buffer = fr[counter].read(); buffer != -1; buffer = fr[counter].read()){
			bw.write(buffer);
			counter++;

			if(counter == fr.length){
				counter = 1;
			}
		}


		//Close stuff
		for(counter= 1; counter < size; counter++){
			fr[counter].close();
		}

		bw.close();

	}

	
	
	//Fixes file of broken partition
	public static void rebuild(String file, int partitionBroken, int fileSize, int partitionSize) throws IOException {
		int partLength;

		file = Proxy.dir + file;
		
		//Parity disk is broken
		if(partitionBroken == 0){
			partLength = fileSize/(partitionSize-1);
			if(fileSize % (partitionSize - 1) != 0){
				partLength++;
			}
		} else {
			partLength = fileSize/(partitionSize-1);
			if(fileSize % (partitionSize - 1) >= partitionBroken){
				partLength++;
			}
		}


		BufferedReader parts[];
		parts = new BufferedReader[partitionSize];

		//Writer for fixed file
		BufferedWriter result;
		result = new BufferedWriter(new FileWriter(new File(getPartFilename(file,partitionBroken))));

		for(int i = 0; i < parts.length; i++){
			if(i != partitionBroken){
				parts[i] = new BufferedReader(new FileReader(new File(getPartFilename(file,i))));
			}
		}

		for(int i = 0; i < partLength; i++){
			int value = 0;

			for(int j = 0; j < partitionSize; j++){
				if(j != partitionBroken){
					value = value^parts[j].read();
				}
			}
			
			result.write(value);

		}

		result.close();
		for(int i = 1; i < parts.length; i++){
			if(i != partitionBroken){
				parts[i].close();
			}
		}

	}

	public static Properties readConfig(String filename) throws IOException{
		Properties p = new Properties();
		InputStream in = new FileInputStream(new File(filename));
		p.load(in);
		in.close();

		return p;
	}
}

