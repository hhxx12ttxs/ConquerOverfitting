package corrrespondence_analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import graph.*;

public class create_ca_table {

	public static void main(String[] args)
	{
		create_ca_table cca=new create_ca_table();
		cca.main();
	}

	public void main()
	{
		create_fgraph cfgh=new create_fgraph();
		create_communities ccm=new create_communities();
		try {
			fgraph fg=cfgh.create("C:\\cygwin\\home\\cse\\CAA\\snap-master\\examples\\cesna\\0.edges","C:\\cygwin\\home\\cse\\CAA\\snap-master\\examples\\cesna\\0.nodefeat");
			ArrayList<community> g=ccm.create_communities(fg, "C:\\cygwin\\home\\cse\\CAA\\snap-master\\examples\\cesna\\0.circles");
			ArrayList<community> p=ccm.create_communities(fg, "C:\\cygwin\\home\\cse\\CAA\\snap-master\\examples\\cesna\\cmtyvv.txt_n");
			//print_communities(g);
			//print_communities(p);
			System.out.println("Sizes: g: "+g.size()+" p: "+p.size());
			ca_table t1=new ca_table(g.size(),fg.get_number_of_features());
			ca_table t2=new ca_table(p.size(),fg.get_number_of_features());

			int c=0;
			for(community ct:g)
			{
				for(int i=0;i<ct.size();i++)
				{
					ArrayList<feature> tfeat=fg.get_features_of_node(i);
					for(feature tf:tfeat)
					{
						t1.increment(c, tf.index());
					}
				}
				c++;
			}
			c=0;
			for(community ct:p)
			{
				for(int i=0;i<ct.size();i++)
				{
					ArrayList<feature> tfeat=fg.get_features_of_node(i);
					for(feature tf:tfeat)
					{
						t2.increment(c, tf.index());
					}
				}
				c++;
			}

			ArrayList<Integer> top_feat=top_features_overall(10,t1,t2);

			String ca_ground_table="C:\\R\\0_ground.txt";
			String ca_predicted_table="C:\\R\\0_detected.txt";

			Random r=new Random();

			int rndm_file_n=r.nextInt();

			reshape_ca_table(t1,top_feat).write_to_file(ca_ground_table);
			reshape_ca_table(t2,top_feat).write_to_file(ca_predicted_table);

			System.out.println("Top: 1");



			String cmd1 =  "Rscript -e \"source('C:/R/print.txt');library(ca);tb=read.table('C:/R/0_ground.txt');cav=ca(tb);print(cav$rowcoord[,1:2]);print(cav$colcoord[,1:2])\"" ;


			Process pr;

			System.out.println("Top: 1.1");

			double[][] g_row_arr=null,g_col_arr=null;

			try {
				int cnt=0;
				pr = Runtime.getRuntime().exec(cmd1);
				pr.waitFor();
				BufferedReader reader = 
						new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String[] lines =read_lines(reader,g.size());			

				g_row_arr=string_to_double_array(lines);

				System.out.println("Top: 1.1.1");

				lines =read_lines(reader,top_feat.size());			

				g_col_arr=string_to_double_array(lines);

				System.out.println("Top: 1.1.2");

			} catch (Exception e) {
				e.printStackTrace();
			}



			String cmd2 =  "Rscript -e \"source('C:/R/print.txt');library(ca);tb=read.table('C:/R/0_detected.txt');cav=ca(tb);print(cav$rowcoord[,1:2]);print(cav$colcoord[,1:2])\"" ;


			double[][] p_row_arr=null,p_col_arr=null;

			try {
				int cnt=0;
				pr = Runtime.getRuntime().exec(cmd2);
				pr.waitFor();
				BufferedReader reader = 
						new BufferedReader(new InputStreamReader(pr.getInputStream()));

				String[] lines =read_lines(reader,p.size());			

				p_row_arr=string_to_double_array(lines);

				lines =read_lines(reader,top_feat.size());			

				p_col_arr=string_to_double_array(lines);


			} catch (Exception e) {
				e.printStackTrace();
			}
			
			/*print_table(g_row_arr,"G Row");
			print_table(g_col_arr,"G Col");
			print_table(p_row_arr,"P Row");
			print_table(p_col_arr,"P Col");*/

			ca_coordinates g_coord=new ca_coordinates(g_row_arr,g_col_arr);

			ca_coordinates p_coord=new ca_coordinates(p_row_arr,p_col_arr);

			community_matcher cm=new community_matcher(g,p);

			double[][] match_table=new double[p.size()][5];

			System.out.println("Sizes: g: "+g.size()+" p: "+p.size());

			for(int i=0;i<p.size();i++)
			{
				//System.out.println("\nChecking: idx "+i);
				match_table[i][0]=i;
				match_table[i][1]=cm.get_match_idx(p.get(i));
				//System.out.println("Filling ratio: ");
				match_table[i][2]=cm.match_ratio(g.get((int)match_table[i][1]),p.get(i));
				
				match_table[i][3]=spearmans_coorelation(g_coord,p_coord,(int)match_table[i][1],i);
				
				match_table[i][4]=eld_dist(t1.row_profile((int)match_table[i][1]),t2.row_profile(i));
			}

			print_table(match_table,"Matches");

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public String[] read_lines(BufferedReader reader,int n) throws IOException
	{
		String[] tmp=new String[n];
		for(int i=0;i<n;i++)
		{
			tmp[i]=reader.readLine();
		}
		return tmp;
	}

	private double d2(int[] a,int[] b)
	{
		if(a.length==b.length)
		{
			double d=0;
			for(int i=0;i<a.length;i++)
			{
				d+=Math.pow(a[i]-b[i], 2);
			}
			return d;
		}
		else
		{
			return 10000;
		}
	}
	
	public double spearmans_coorelation(ca_coordinates g_coord,ca_coordinates p_coord,int g_idx,int p_idx)
	{
		int[] a=g_coord.rank_array(g_idx);
		int[] b=p_coord.rank_array(p_idx);
		
		double d=d2(a,b);
		
		int n=a.length;
		return (1 - 6*(d/(Math.pow(n, 3)-n)));
	}
	
	public void print_array(int[] a,String name)
	{
		System.out.println("Printing array: "+name+"\n");
		for(int i=0;i<a.length;i++)
		{
			System.out.print(a[i]+"\t");
		}
		
	}
	
	public double eld_dist(double[] a,double[] b)
	{
		if(a.length==b.length)
		{
			double d=0;
			for(int i=0;i<a.length;i++)
			{
				d+=Math.pow(a[i]-b[i], 2);
			}
			return Math.sqrt(d);
		}
		else
		{
			return 10000;
		}
	}

	public void print_table(double[][] a,String name)
	{
		System.out.println("Printing table: "+name+"\n");
		for(int i=0;i<a.length;i++)
		{
			for(int j=0;j<a[0].length;j++)
			{
				System.out.print(a[i][j]+"\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	public ca_table reshape_ca_table(ca_table t,ArrayList<Integer> col)
	{
		ca_table tmp=new ca_table(t.rows(),col.size());
		for(int j=0;j<col.size();j++)
		{
			for(int i=0;i<t.rows();i++)
			{
				tmp.put(i, j, t.get(i, col.get(j)));
			}
		}
		return tmp;

	}

	public ArrayList<Integer> top_features_overall(int num,ca_table t1,ca_table t2)
	{
		int[] t1_totals=new int[t1.columns()];
		int[] t2_totals=new int[t1.columns()];
		int[] t1_totals_copy=new int[t1.columns()];
		int[] t2_totals_copy=new int[t1.columns()];

		for(int i=0;i<t1.columns();i++)
		{
			t1_totals[i]=t1.column_total(i);
			t1_totals_copy[i]=t1.column_total(i)*-1;
		}

		for(int i=0;i<t2.columns();i++)
		{
			t2_totals[i]=t2.column_total(i);
			t2_totals_copy[i]=t2.column_total(i)*-1;
		}

		Arrays.sort(t1_totals_copy);

		Arrays.sort(t2_totals_copy);

		HashMap<Integer,Integer> t1_map=new HashMap<Integer,Integer>();

		HashMap<Integer,Integer> t2_map=new HashMap<Integer,Integer>();

		for(int i=0;i<num;i++)
		{
			t1_map.put(t1_totals_copy[i]*-1, 1);
			t2_map.put(t2_totals_copy[i]*-1, 1);
		}

		ArrayList<Integer> t1_list=new ArrayList<Integer>();
		ArrayList<Integer> t2_list=new ArrayList<Integer>();

		int c=0;
		for(int i=0;i<t1_totals.length&&c<num;i++)
		{
			if(t1_map.containsKey(t1_totals[i]))
			{
				t1_list.add(i);
				c++;
			}
		}
		c=0;

		for(int i=0;i<t2_totals.length&&c<num;i++)
		{
			if(t2_map.containsKey(t2_totals[i]))
			{
				t2_list.add(i);
				c++;
			}
		}

		return intersection(t1_list,t2_list);
	}

	public ArrayList<Integer> top_features_community_percentage(ca_table t1,ca_table t2)
	{
		ArrayList<Integer> f_list_t1=new ArrayList<Integer>();
		ArrayList<Integer> f_list_t2=new ArrayList<Integer>();

		for(int i=0;i<t1.rows();i++)
		{
			int tmp_row_total=t1.row_total(i);
			for(int j=0;j<t1.columns();j++)
			{
				if(((double)t1.get(i, j)/tmp_row_total)>0.5)
				{
					if(!f_list_t1.contains(tmp_row_total))
						f_list_t1.add(t1.get(i, j));
				}
			}
		}

		for(int i=0;i<t2.rows();i++)
		{
			int tmp_row_total=t1.row_total(i);
			for(int j=0;j<t1.columns();j++)
			{
				if(((double)t2.get(i, j)/tmp_row_total)>0.5)
				{
					if(!f_list_t2.contains(tmp_row_total))
						f_list_t2.add(t2.get(i, j));
				}
			}
		}

		return union(f_list_t1,f_list_t2);
	}

	public ArrayList<Integer> intersection(ArrayList<Integer> l1,ArrayList<Integer> l2)
	{

		ArrayList<Integer> l3=new ArrayList<Integer>();

		for(int tmp:l1)
		{
			if(l2.contains(tmp))
			{
				l3.add(tmp);
			}
		}

		return l3;
	}

	public ArrayList<Integer>union(ArrayList<Integer> l1,ArrayList<Integer> l2)
	{
		ArrayList<Integer> l3=new ArrayList<Integer>();

		for(int tmp:l1)
		{
			if(!l3.contains(tmp))
			{
				l3.add(tmp);
			}
		}

		for(int tmp:l2)
		{
			if(!l3.contains(tmp))
			{
				l3.add(tmp);
			}
		}

		return l3;
	}

	public void print_communities(ArrayList<community> comm_list)
	{
		System.out.println("Printing: ");
		int c=1;
		for(community cm:comm_list)
		{
			System.out.println("No: "+c);
			for(int i=0;i<cm.size();i++)
			{
				System.out.print(cm.get_node(i).get_name()+"\t");
			}
			System.out.println();
			c++;
		}
	}

	public double[][] string_to_double_array(String[] lines)
	{
		double[][] temp=null;
		for(int i=0;i<lines.length;i++)
		{
			String[] part=lines[i].split(" ");
			int c=0;
			if(i==0)
			{
				c=0;
				for(int j=0;j<part.length;j++)
				{
					if(part[j].length()>0&&is_numeric(part[j]))
					{
						c++;
					}
				}
				temp=new double[lines.length][c];
				c=0;
			}
			
			for(int j=0;j<part.length;j++)
			{

				if(part[j].length()>0&&is_numeric(part[j]))
				{
					temp[i][c++]=Double.parseDouble(part[j]);
				}
			}

		}
		return temp;
	}

	public boolean is_numeric(String str)
	{
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(str, pos);
		return str.length() == pos.getIndex();
	}

}

//cfgh.create("C:\\cygwin\\home\\cse\\CAA\\snap-master\\examples\\cesna","C:\\cygwin\\home\\cse\\CAA\\snap-master\\examples\\cesna");

