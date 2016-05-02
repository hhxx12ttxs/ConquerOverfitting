package test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.*;
import java.io.*;
import java.util.*;

import static prclqz.core.Const.*;
public class Parser{
	
	// args?????
	public static final int CHENG_ZHEN = 0;
	public static final int NONG_CUN = 1;
	public static final int NONG_YE = 2;
	public static final int FEI_NONG = 3;
	
	// ???foxpro??????
	public static final int NC_NY = 61;
	public static final int NC_FN = 62;
	public static final int CZ_NY = 63;
	public static final int CZ_FN = 64;
	public static final int NC = 65;
	public static final int CZ = 66;
	public static final int NY = 67;
	public static final int FN = 68;
	public static final int ALL = 69;
	
//	Test path
//	private static final String PREFIX = "D:\\prclqz\\??????????\\";
	private static final String PREFIX = "D:\\DB\\data\\????????\\";
	
	private static final String INPUT_HEADER_PATH = "??\\???_zinv_";
//	private static final String INPUT_HEADER_PATH = "??\\???_";

	private static final String INPUT_DATA_PATH = "??\\???_zinv_";
//	private static final String INPUT_DATA_PATH = "??\\???_";
	
//	private static final String OUTPUT_DATA_PATH = "??\\???_";
//	private static final String OUTPUT_HEADER_PATH = "??\\???_";
	
	//????
	
	public static void main(String[] _args) throws Exception{
		int args[] = new int[4];
		args[NONG_CUN] = 1;
		args[CHENG_ZHEN] = 0;
		args[NONG_YE] = 1;
		args[FEI_NONG] = 0;
		parse(77, 2014, 28);
	}
	// args: ??(CHENG_ZHEN)???(NONG_CUN)???(NONG_YE)???(FEI_NONG)???
	// year: ??
	// dqx: ??
	/**
	 * ?????????????????,?prclqz???????,?:String:???,double[]:????MAX_AGE????,?????enum??
	 * !!!!?????????????prefix???????
	 * @param dn
	 * ????
	 * @param year
	 * @param dqx
	 * @return
	 * @throws Exception
	 */
	public static HashMap<String, double[]> parse (
			int dn,
			int year,
			int dqx
	)throws Exception{
		String postfix = Integer.toString(dn) + "_" + year + "_" + dqx + ".txt";
		File header_file = new File(PREFIX + INPUT_HEADER_PATH + postfix);
		File data_in_file = new File(PREFIX + INPUT_DATA_PATH + postfix);
		
		//???
		FileInputStream header = null;
		try{
			header = new FileInputStream(header_file);
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		//???
		FileInputStream data_in = null;
		try{
			data_in = new FileInputStream(data_in_file);
		}
		catch (FileNotFoundException e){
			e.printStackTrace();
		}
		BufferedReader header_br = new BufferedReader(new InputStreamReader(header, "GBK"));
		BufferedReader data_in_br = new BufferedReader(new InputStreamReader(data_in, "GBK"));
		
		ArrayList<String> header_arr = parseHeader(header_br);
		ArrayList<ArrayList<Double>> data_arr = parseData(data_in_br);
		HashMap<String, double[]> res = new HashMap<String, double[]>();

		//res???:String:???,double[],??????
		for(int i = 0; i != header_arr.size(); ++i){
			ArrayList<Double> tmp_db_arr = data_arr.get(i);
			double[] tmp_db = new double[MAX_AGE];
			for(int j = 0; j < MAX_AGE; ++j){
				if(j >= tmp_db_arr.size()){
					tmp_db[j] = 0.0;
				}
				else{
					tmp_db[j] = tmp_db_arr.get(j);
				}
			}
			res.put(header_arr.get(i), tmp_db);
			
		}	
		return res;
		
	}
		
	public static ArrayList<String> parseHeader(BufferedReader br) throws Exception{
		// ??7?
		for(int ignore_i = 0; ignore_i < 7; ++ignore_i ){
			br.readLine();
		}
		
		ArrayList<String> header = new ArrayList<String>();
		
		while(br.ready()){
			String curr_line = br.readLine();
			String[] tmp_str_list = curr_line.split("\\s+");
			//??????
			if(tmp_str_list.length <= 4) 
				break;
//			System.out.println(tmp_str_list[2].trim());
			header.add(tmp_str_list[2].trim());
		}
		
		return header;
	}
/**
 * ????????,???br???????????,?????
 * @param br
 * @return
 * @throws Exception
 */
	//DB?parseData
	public static ArrayList<ArrayList<Double>> parseData(BufferedReader br) 
			throws Exception{
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();
		boolean first = true;
		int count = 0;
		while(br.ready()){
			String curr_line = br.readLine();
			String[] tmp_str_list = curr_line.split(",");
			if(first){
				first = false;
				//?????
				for(int i = 0; i != tmp_str_list.length; ++i){
					res.add(new ArrayList<Double>());
				}
			}
			for(int i = 1; i != tmp_str_list.length; ++i){
				// ?????ArrayList?
				try{
					res.get(i - 1).add(Double.parseDouble(tmp_str_list[i]));
				}
				catch(NumberFormatException e){
					System.out.println("??????????????????0.0?????????");
					res.get(i - 1).add(0.0);
				}
			}
		}
		return res;
	}
	public static ArrayList<String> getHeader(String path)throws Exception{
		File file = new File(path);
		FileInputStream fs = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fs, "GBK"));
		
		for(int ignore_i = 0; ignore_i < 7; ++ignore_i ){
			br.readLine();
		}
		ArrayList<String> header = new ArrayList<String>();
		
		while(br.ready()){
			String curr_line = br.readLine();
			String[] tmp_str_list = curr_line.split("\\s+");
			//??????
			if(tmp_str_list.length <= 4) 
				break;
			header.add(tmp_str_list[2].trim());
		}
		return header;
	}

public static HashMap<String, double[]> parse_DS (
		int dn,
		int year,
		int dqx,
		int offset
)throws Exception{
	String postfix = Integer.toString(dn) + "_" + year + "_" + dqx + ".txt";
	File header_file = new File(PREFIX + INPUT_HEADER_PATH + postfix);
	File data_in_file = new File(PREFIX + INPUT_DATA_PATH + postfix);
	
	//???
	FileInputStream header = null;
	try{
		header = new FileInputStream(header_file);
	}
	catch (FileNotFoundException e){
		e.printStackTrace();
	}
	//???
	FileInputStream data_in = null;
	try{
		data_in = new FileInputStream(data_in_file);
	}
	catch (FileNotFoundException e){
		e.printStackTrace();
	}
	BufferedReader header_br = new BufferedReader(new InputStreamReader(header, "GBK"));
	BufferedReader data_in_br = new BufferedReader(new InputStreamReader(data_in, "GBK"));
	
	ArrayList<String> header_arr = parseHeader_DS(header_br,offset);
	ArrayList<ArrayList<Double>> data_arr = parseData_DS(data_in_br,offset);
	HashMap<String, double[]> res = new HashMap<String, double[]>();
	//res???:String:???,double[],??????
	for(int i = 0; i != header_arr.size(); ++i){
		ArrayList<Double> tmp_db_arr = data_arr.get(i);
		double[] tmp_db = new double[MAX_AGE];
		for(int j = 0; j != MAX_AGE; ++j){
			if(j >= tmp_db_arr.size()){
				tmp_db[j] = 0.0;
			}
			else{
				tmp_db[j] = tmp_db_arr.get(j);
			}
		}
		res.put(header_arr.get(i), tmp_db);
		
	}	
	return res;
	
}

	public static ArrayList<ArrayList<Double>> parseData_DS(BufferedReader br,int offset) 
			throws Exception{
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();
		boolean first = true;
		while(br.ready()){
			String curr_line = br.readLine();
			String[] tmp_str_list = curr_line.split(",");
			if(first){
				first = false;
				// ?offset???
				for(int i = offset; i != tmp_str_list.length; ++i){
					res.add(new ArrayList<Double>());
				}
			}
			for(int i = offset; i != tmp_str_list.length; ++i){
				// ?????ArrayList?
				res.get(i - offset).add(Double.parseDouble(tmp_str_list[i]));
			}
		}
		return res;
	}
	public static ArrayList<String> parseHeader_DS(BufferedReader br,int offset) throws Exception{
		// ??7?
		for(int ignore_i = 0; ignore_i < 6+offset; ++ignore_i ){
			br.readLine();
		}
		
		ArrayList<String> header = new ArrayList<String>();
		
		while(br.ready()){
			String curr_line = br.readLine();
			String[] tmp_str_list = curr_line.split("\\s+");
			//??????
			if(tmp_str_list.length <= 4) 
				break;
//			System.out.println(tmp_str_list[2].trim());
			header.add(tmp_str_list[2].trim());
		}
		
		return header;
	}
	
	
}


