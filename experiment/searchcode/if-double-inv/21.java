import java.util.*;
import java.io.*;
import cern.jet.random.*;
import cern.jet.random.engine.*;

public class Permutation
{
    double INVALID_VAL = -9999;//to denote missing data.
    static boolean[] attribute_status;//whether each attribute is discrete(true) or continuous(false);
    EntropyNormal EstEnt;
    static HashMap VAR_MAP; //<var id, var name> map.
    static HashMap Rev_MAP = new HashMap();  //<var name, var id> map.

    public Permutation(double inv) throws Exception
    {
        INVALID_VAL = inv;
        EstEnt.init(INVALID_VAL,attribute_status);
        VAR_MAP = new HashMap();
    }    
    
	public static double[][] readData_continuous(String fname) throws Exception
	{
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
            String line="";
            int m = 0;
            int n = 0;
            //populate VAR_MAP.
            {
                //first line should be the header.
                line = in.readLine();//read header line.
                StringTokenizer st = new StringTokenizer(line,",\t ");
                n = st.countTokens();//count the no. of snps.
                int k = 0;
                while(st.hasMoreTokens())
                {
                    String x = st.nextToken();
                    VAR_MAP.put(new Integer(k),x);
                    Rev_MAP.put(x,new Integer(k));
                    k++;
                }
            }
            //count the no. of samples.
            while((line=in.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line,",\t ");
                m++;
            }
            in.close();

            System.out.println(m);
            System.out.println(n);

            in = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
            in.readLine(); //skip header line.
            int i=0;

            double[][] D = new double[m][n];

            while((line=in.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line,",\t ");
                int j = 0;
                while(st.hasMoreTokens())
                {
                    String t = st.nextToken().trim();
                    double v = -1;
                    v = Double.valueOf(t).doubleValue();
                    D[i][j] = v;
                    j++;
                }
                i++;
            }
            in.close();
            attribute_status = new boolean[n];
            for(int col=0;col<D[0].length;col++)
            {
		attribute_status[col] = true;
            }
            attribute_status[n-1] = false;//Only phenotype is continuous.
            return D;
	}
    

    public void nchoosek(Vector V,int k,Vector result,Vector out) throws Exception
    {
        if(result==null)
                result = new Vector();
        if(k==0)
        {
            Vector R = new Vector(result);
            out.add(R);
            return;
        }

        Vector nV = new Vector(V);
        int n = nV.size();
        for(int i=0;i<n;i++)
        {
            int v = ((Integer)nV.remove(0)).intValue();
            result.add(new Integer(v));
            nchoosek(nV,k-1,result,out);
            result.remove(new Integer(v));
        }
    }

	double[][] get_col_data(double[][] D,Vector col_arr,Vector disc_cols,Vector cont_cols,boolean[] status) throws Exception
	{
		//get column from data D as given in col_arr.
		int m = D.length;
		int n = col_arr.size();
		double[][] X = new double[m][n];

		for(int j=0;j<n;j++)
		{
			int col = ((Integer)col_arr.get(j)).intValue();
            boolean stat = status[col];//discrete or continuous ?
			for(int i=0;i<m;i++)
			{
				X[i][j] = D[i][col];
			}
			if(stat==true)
				disc_cols.add(""+new Integer(j));
			else
				cont_cols.add(""+new Integer(j));
		}
		return X;
	}

	double[][] get_col_data(double[][] D,Vector col_arr) throws Exception
	{
		//get column from data D as given in col_arr.
		int m = D.length;
		int n = col_arr.size();
		double[][] X = new double[m][n];

		for(int j=0;j<n;j++)
		{
			int col = ((Integer)col_arr.get(j)).intValue();
                        boolean stat = attribute_status[col];//discrete or continuous ?
			for(int i=0;i<m;i++)
			{
				X[i][j] = D[i][col];
			}
		}
		return X;
	}

    double[][] get_col_data_pure(double[][] D,Vector col_arr) throws Exception
	{
		//get column from data D as given in col_arr, get only rows with no invalid values.
		int m = D.length;
		int n = col_arr.size();
		Vector X = new Vector();

		for(int i=0;i<m;i++)
		{
			boolean inv = false;
			double[] row = new double[n];
			for(int j=0;j<n;j++)
			{
				int col = Integer.valueOf((String)col_arr.get(j)).intValue();
				if(D[i][col]!=INVALID_VAL)
				{
					row[j] = D[i][col];
				}
				else
				{
					inv = true;
					break;
				}
			}
			if(inv==false)
				X.add(row);
		}

		double[][] Y = new double[X.size()][n];
		for(int i=0;i<X.size();i++)
		{
			Y[i] = (double[])X.get(i);
		}
		return Y;
	}

	public double kwii(double[][] D,Combination C,boolean[] comb_status) throws Exception
	{
            //The disease column is added in col_arr.
            //System.out.println(C.comb_vec);
            Vector col_array = C.comb_vec;
            double[][] Dpure = get_col_data_pure(D,col_array);//get pure data containing only these columns.
            Vector col_arr = new Vector();
            boolean[] status = new boolean[col_array.size()];
            for(int i=0;i<col_array.size();i++)
            {
                int col_org = Integer.valueOf((String)col_array.get(i)).intValue();
                col_arr.add(new Integer(i));//all columns.
                status[i] = comb_status[col_org];
            }

            int K = col_arr.size();
            double kwii = 0;

            for(int k=1;k<=K;k++)
            {
                Vector all_snp_combs = new Vector();
                nchoosek(col_arr,k,null,all_snp_combs);
                int snp_comb_count = all_snp_combs.size();
                double S = 0;
                for(int i=0;i<snp_comb_count;i++)
                {
                    Vector snp_comb = (Vector)all_snp_combs.get(i);
                    Vector disc_cols = new Vector();
                    Vector cont_cols = new Vector();
                    double[][] X = get_col_data(Dpure,snp_comb,disc_cols,cont_cols,status);
                    double E = EstEnt.entropy(X,disc_cols,cont_cols);
                    S = S + E;
                }
                //do term = S * (-1)^(K-k).
                double term = S;
                if((K-k)%2==1)
                        term = -S;
                kwii += term;
            }
            kwii = -kwii;
            return kwii;
	}
        
	public static double[][] permutation_sample(double[][] D,Combination C,int seed)throws Exception
	{
            //Obtain a permutation of the phenotype to get a sample from the data D.
            //D should have the phenotype variable as the last column.
            int m = D.length;
            int n = C.comb_vec.size();            

            //vectorize D.
            Vector V = new Vector(D.length);
            for(int r=0;r<m;r++)
            {
                V.add(D[r]);//store the rows of data D in V.
            }            
            //now sample WITH REPLACEMENT.
            double[][] Dsample = new double[m][n];
            MersenneTwister Mrand = new MersenneTwister(seed);
            for(int sample=0;sample<m;sample++)
            {
                double r = Mrand.raw();//between 0 and 1.
                int sampleNo = (int)Math.floor(r*V.size());
                double[] row = (double[])V.remove(sampleNo);//randomly remove a sample row from V.
                //double[] row = (double[])V.get(sample);
                for(int i=0;i<n-1;i++)
                {
                    int col = Integer.valueOf((String)C.comb_vec.get(i)).intValue(); //copy the row to Dsample.
                    Dsample[sample][i] = row[col];
                }
                int col = Integer.valueOf((String)C.comb_vec.get(n-1)).intValue();
                Dsample[sample][n-1] = D[sample][col]; //assign the corresponding status from original data D.
            }
            return Dsample;
	}
        
        static void print(double[][] D) throws Exception
        {
            for(int i=0;i<10;i++)
            {
                for(int j=0;j<D[0].length;j++)
                {
                    System.out.print(D[i][j]+" ");
                }
                System.out.println();
            }
        }

	double permutation(double[][] D,Combination C,int seed)throws Exception
	{           
            String s1 = "";
            boolean[] comb_status = new boolean[C.comb_vec.size()];
            for(int i=0;i<C.comb_vec.size();i++)
            {
                comb_status[i] = attribute_status[Integer.valueOf((String)C.comb_vec.get(i)).intValue()];
            }
            for(int i=0;i<C.comb_vec.size();i++)
            {
                s1 += i + "|";                    
            }
            Combination new_comb = new Combination(s1);            
            //do not forget to manage the attribute status vector.
            double[][] Xsample = permutation_sample(D,C,seed);//get one sample permuted data.
            return kwii(Xsample,new_comb,comb_status);		
	}

	public double run_permutation(double[][] D_org, Combination C, int B,Random R) throws Exception
	{
            double kwii_obs = C.kwii;
            double p_kwii = 0;            
            for(int i=0;i<B;i++)
            {
                double value = permutation(D_org,C,R.nextInt());
                //System.out.println(i+" KWII = "+kwii_obs+" perm = "+value);
                if(value>kwii_obs)p_kwii++;
            }
            p_kwii = p_kwii/B;
            return p_kwii;
	}
        
        
        public int index(String s) throws Exception
        {
            Iterator I = VAR_MAP.keySet().iterator();            
            while(I.hasNext())
            {
                Integer index = (Integer)I.next();
                String x = (String)VAR_MAP.get(index);
                if(x.compareTo(s)==0)
                {                    
                    return index;
                }                
            } 
            System.out.println("Cannot find "+s);
            System.exit(1);
            return -1;
        }
        
        public String convert(String comb) throws Exception
        {
            String x = "";
            StringTokenizer st = new StringTokenizer(comb,"\t, ");
            while(st.hasMoreTokens())
            {
                String s = st.nextToken();
                int index = index(s);
                x += index+"|";
            }
            return x;
        }
        
        static String get_id(String s) throws Exception
        {
            String x = "";
            StringTokenizer st = new StringTokenizer(s,"|,");
            while(st.hasMoreTokens())
            {
                String snp = st.nextToken().trim();
                int id = (Integer)Rev_MAP.get(snp);
                x += id+"|";
            }            
            return x;
        }                
        
 	public static void main(String args[])throws Exception
	{
            //Change your program parameters here.
            
            String dname = "Data.txt"; //the name of your data file that should contain gentotypes and last column phenotype separated by tab.
            String cname = "Combinations.txt"; //your combinations file.
            String oname = "Pvalues.txt"; //name of output file.
            int B = 1000; //No. of permutations.            
            
            //Do not change anything below here.
            Date dt = new Date();
            Random R = new Random(527454+dt.getTime());            
            Permutation P = new Permutation(-9999);            
            double[][] Data = readData_continuous(dname);                        
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(cname)));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(oname)));            
            String line="";
            in.readLine();//skip header.
            while((line=in.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line,"\t ");
                String comb = st.nextToken().trim();
                String comb1 = get_id(comb);
                System.out.println("Permutation for ["+comb+"] -> ("+comb1+")");
                double kwii_obs = Double.valueOf(st.nextToken().trim()).doubleValue();
                Combination C = new Combination(comb1);
                C.kwii = kwii_obs;
                double pvalue = P.run_permutation(Data,C,B,R);
                out.write(comb+"\t"+kwii_obs+"\t"+pvalue+"\n");
            }
            in.close();
            out.close();            
	}                
}


