import java.util.*;
import java.io.*;

public class AMBIENCE //for discrete phenotypes.
{
	int INVALID_VAL = -99;//to denote missing data.
        static boolean[] attribute_status = null;//whether each attribute is discrete(true) or continuous(false);
        boolean SHOW_NEG_INTERACTIONS = false;        
        HashMap VAR_MAP;
        HashMap Rev_MAP;
        int data_type = -1;//1=discrete,2=continuous.
        double kwiithreshold = 0.001;

	public AMBIENCE() throws Exception
	{
            //for discrete trait.
            VAR_MAP = new HashMap();
            Rev_MAP = new HashMap();
	}
        
	public double entropy(int[][] D) throws Exception
	{
		int marker = INVALID_VAL;
		//marker indicates invalid value.
		int m = D.length;
		int n = D[0].length;
		HashMap H = new HashMap();

		//convert each row to a string and count the frequency of each row.
		int nvalid = 0;
		for(int i=0;i<m;i++)
		{
			String row = "";
			boolean inv = false;
			for(int j=0;j<n;j++)
			{
				if(D[i][j]==marker)
				{
					inv = true;
					break;
				}
				if(row.compareTo("")==0)
					row = ""+D[i][j];
				else
				 	row = row + "|" + D[i][j];
			}
			if(inv==false)
			{
				Integer cnt = (Integer)H.get(row);
				int c = -1;
				if(cnt!=null)
					c = cnt.intValue()+1;
				else
					c = 1;
				H.put(row,new Integer(c));
				nvalid++;
			}
		}
		double ent = 0;
		Iterator I = H.keySet().iterator();
		while(I.hasNext())
		{
			String row = (String)I.next();
			int v = ((Integer)H.get(row)).intValue();
			double p = (double)v/(double)nvalid;
			ent += p*(Math.log(p)/Math.log(2));
		}
		return -ent;
	}

	public int[][] readData_discrete(String fname) throws Exception
	{
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
            String line="";
            int i=0;
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
            System.out.println("#samples = "+m);
            System.out.println("#variables = "+n);

            in = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
            in.readLine(); //skip header line.

            int[][] D = new int[m][n];

            while((line=in.readLine())!=null)
            {
                StringTokenizer st = new StringTokenizer(line,",\t ");
                int j=0;
                while(st.hasMoreTokens())
                {
                    String t = st.nextToken().trim();
                    int v = Integer.valueOf(t).intValue();
                    D[i][j] = v;
                    j++;
                }
                i++;
            }
            in.close();
            return D;
	}

        String get_name(String s) throws Exception
        {
            String x = "";
            StringTokenizer st = new StringTokenizer(s,"|");
            while(st.hasMoreTokens())
            {
                Integer snp = Integer.valueOf(st.nextToken());
                String name = (String)VAR_MAP.get(snp);
                x += name+",";
            }
            return x;
        }

        String get_id(String s) throws Exception
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
       
        static void nchoosek(Vector V,int k,Vector result,Vector out) throws Exception
        {
                if(result==null)
                        result = new Vector();
                if(k==0)
                {
                        Vector R = new Vector(result);
                        out.add(R);
                        //for(int i=0;i<result.size();i++)
                        //	System.out.print(result.get(i));
                        //System.out.println();
                        return;
                }

                Vector nV = new Vector(V);
                int n = nV.size();
                for(int i=0;i<n;i++)
                {
                        int v = Integer.valueOf((String)nV.remove(0));                    
                        result.add(""+v);
                        nchoosek(nV,k-1,result,out);
                        result.remove(""+v);
                }
        }                

	public void print_comb(Vector V) throws Exception
	{
		System.out.print("comb = ");
		for(int i=0;i<V.size();i++)
		{
			System.out.print(V.get(i) +" ");
		}
		System.out.println();
	}

	int[][] get_col_data(int[][] D,Vector col_arr) throws Exception
	{
		//get column from data D as given in col_arr.
		int m = D.length;
		int n = col_arr.size();
		int[][] X = new int[m][n];

		for(int j=0;j<n;j++)
		{			
                        int col = Integer.valueOf((String)col_arr.get(j));
			for(int i=0;i<m;i++)
			{
				X[i][j] = D[i][col];
			}
		}
		return X;
	}

	int[][] get_col_data_pure(int[][] D,Vector col_arr) throws Exception
	{
		//get column from data D as given in col_arr, get only rows with no invalid values.
		int m = D.length;
		int n = col_arr.size();
		Vector X = new Vector();

		for(int i=0;i<m;i++)
		{
			boolean inv = false;
			int[] row = new int[n];
			for(int j=0;j<n;j++)
			{				
                                int col = Integer.valueOf((String)col_arr.get(j));
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

		int[][] Y = new int[X.size()][n];
		for(int i=0;i<X.size();i++)
		{
			Y[i] = (int[])X.get(i);
		}
		//System.out.println(Y.length+" pure rows out of "+D.length);
		return Y;
	}

	void printdata(int x,int[][] X) throws Exception
	{
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("t"+x+".dat")));
		for(int i=0;i<X.length;i++)
		{
			for(int j=0;j<X[0].length;j++)
			{
				out.write(X[i][j]+" ");
			}
			out.write("\n");
		}
		out.close();
	}

	public double Two_wii(int[][] D,Vector B)throws Exception
	{
		//The disease column is not added in B.
		Vector A = new Vector(B);
		int N = D[0].length;
		A.add(""+(N-1));
		int[][] Dpure = get_col_data_pure(D,A);//get pure data containing only these columns.
                //System.out.println(Dpure.length);
                if(Dpure.length<=0)
                    return -99;

		Vector W1 = new Vector();
		Vector W2 = new Vector();
		for(int i=0;i<A.size()-1;i++)
		{
                    W1.add(""+i);//all except last column.
		}
		W2.add(""+(A.size()-1));//only last column.

		int[][] X1 = get_col_data(Dpure,W1);
		int[][] X2 = get_col_data(Dpure,W2);

		double pai = entropy(X1) + entropy(X2) - entropy(Dpure);
		return pai;
	}

	public double tic(int[][] D,Vector col_arr) throws Exception
	{
		//The disease column is added in col_arr.
		int[][] Dpure = get_col_data_pure(D,col_arr);//get pure data containing only these columns.
                if(Dpure.length<=0)
                    return -99;

		int K = col_arr.size();
		double tic = 0;

		for(int k=0;k<K;k++)
		{
			Vector snp = new Vector();
			snp.add(""+k);
			int[][] X = get_col_data(Dpure,snp);
			double E = entropy(X);
			tic = tic + E;
		}

		double E = entropy(Dpure);
		tic = tic - E;

		//System.out.println("tic="+tic);
		return tic;
	}

	public double kwii(int[][] D,Vector col_array) throws Exception
	{            
		//The disease column is added in col_arr.
		int[][] Dpure = get_col_data_pure(D,col_array);//get pure data containing only these columns.
                if(Dpure.length<=0)
                    return -99;
               
		Vector col_arr = new Vector();
		for(int i=0;i<col_array.size();i++)
		{
			col_arr.add(""+i);//all columns.
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
				int[][] X = get_col_data(Dpure,snp_comb);
				double E = entropy(X);                                
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

	public String create_id(String comb,int snp)
	{
		//create a unique hash value for this combination and snp.
		StringTokenizer st = new StringTokenizer(comb,"|");
		Vector V = new Vector();
		while(st.hasMoreTokens())
		{
			Integer v = Integer.valueOf(st.nextToken());
			V.add(""+v);
		}
		String id = "";
		boolean added = false;
		for(int i=0;i<V.size();i++)
		{			
                        int v = Integer.valueOf((String)V.get(i));
			if(added==false)
			{
				if(v<snp)
					id = id + v + "|";
				else if(v==snp)
				{
					//do nothing. no repetition is allowed.
				}
				else
				{
					id = id + snp + "|" + v + "|";
					added = true;
				}
			}
			else
			{
				id = id + v + "|";
			}
		}
		if(added==false)
			id = id + snp + "|";
		return id;
	}

	public Vector convert(String comb)
	{
		StringTokenizer st = new StringTokenizer(comb,"|");
		Vector V = new Vector();
		while(st.hasMoreTokens())
		{
			Integer v = Integer.valueOf(st.nextToken());
			V.add(""+v);
		}
		return V;
	}

	public String convert(Vector comb)
	{
		String s = "";
		for(int i=0;i<comb.size();i++)
		{			
                        int x = Integer.valueOf((String)comb.get(i));
			s += x + "|";
		}
		return s;
	}

	public void print(HashMap H) throws Exception
	{
		Iterator I = H.keySet().iterator();
		while(I.hasNext())
		{
			String row = (String)I.next();
			Integer cnt = (Integer)H.get(row);
			System.out.println(row+" -> "+cnt);
		}
	}

	void compare(double d1,double d2) throws Exception
	{
		String s1 = (new Double(d1)).toString();
		String s2 = (new Double(d1)).toString();

		s1 = s1+"000000000000";
		s2 = s2+"000000000000";

		s1 = s1.substring(0,8);
		s2 = s2.substring(0,8);

		if(s1.compareTo(s2)!=0)
		{
			System.out.println("Wrong "+s1+","+s2);
			System.exit(0);
		}
	}

        //for discrete trait.
   //public void addTo(String parent,String child,int snp,HashMap PAI_MAP,HashMap ALL_KWII,int[][] Data,double twowii)throws Exception
   public void addTo(LIST L,HashMap PAI_MAP,HashMap ALL_KWII,int[][] Data)throws Exception
   {
        Iterator I = L.PQ.iterator();
        while(I.hasNext())
        {
            Entry e = (Entry)I.next();
            Combination child_comb = e.comb;
            String child = child_comb.comb_str;
            String parent = child_comb.parent;
            int snp = child_comb.last_to_enter;
            //System.out.println("Generating subsets for child "+child+" with parent "+parent+"     last = "+snp);
            int N = Data[0].length;
            Vector subsetcombs = (Vector)PAI_MAP.get(parent);
            Vector new_subsetcombs = new Vector(subsetcombs);

            //generate new combinations.
            String new_comb_str = snp+"|"+(N-1)+"|";
            //calculate the kwii for this new_comb.
            Combination new_comb = new Combination(new_comb_str);
            double kwii = kwii(Data,new_comb.comb_vec);
            new_comb.kwii = kwii;
            new_subsetcombs.add(new_comb);

            ALL_KWII.put(new_comb_str,new Double(new_comb.kwii));
            //System.out.println("      - "+new_comb_str+" "+kwii);
            int sz = subsetcombs.size()*2 + 1;

            for(int i=0;i<subsetcombs.size();i++)
            {
                Combination comb = (Combination)subsetcombs.get(i);
                new_comb_str = create_id(comb.comb_str,snp);
                //calculate the kwii for this new_comb.
                new_comb = new Combination(new_comb_str);
                kwii = kwii(Data,new_comb.comb_vec);
                new_comb.kwii = kwii;
                new_subsetcombs.add(new_comb);
                //System.out.println("      - "+new_comb_str+" "+kwii);
                if(SHOW_NEG_INTERACTIONS)//add all interactions.
                {
                    if(new_comb.kwii>=kwiithreshold)
                        ALL_KWII.put(new_comb.comb_str,new Double(new_comb.kwii));
                }
                else if(new_comb.kwii >= 0) //add only the +ve interactions.
                {
                    if(new_comb.kwii>=kwiithreshold)
                        ALL_KWII.put(new_comb.comb_str,new Double(new_comb.kwii));
                }
            }
            PAI_MAP.put(child,new_subsetcombs);
        }
        System.out.println("ALL_KWII = "+ALL_KWII.size()+" PAI_MAP = "+PAI_MAP.size()); 
   }

        //for discrete trait.
	public void search(int[][] Data,int THETA,int SEARCH_ITER,String fnameout) throws Exception
	{
            //to retrieve top K groups of snps.
            int N = Data[0].length; //----last column is disease status.
            int M = Data.length;    //----no. of samples.

            HashMap PAI_MAP = new HashMap();
            HashMap ALL_KWII = new HashMap();

            HashMap collection  = new HashMap(); //<combination, index> combination
            LIST L = new LIST(THETA);

            System.out.println("\nIteration 0 with "+(N-1)+" steps");
            long ts = System.currentTimeMillis();

            //1st step, calculate pairwise mutual information of each snp and phenotype.
            for(int snp1=0;snp1<N-1;snp1++)
            {
                Combination comb = new Combination(snp1+"|");//Do not add the disease column, it's implicit.
                comb.last_to_enter = snp1;
                double two_wii = Two_wii(Data,comb.comb_vec);
                comb.kwii = comb.pai = two_wii;
                L.add(comb);
                Vector subsetcombs = new Vector();
                subsetcombs.add(new Combination(comb.comb_str+(N-1)+"|"));
                PAI_MAP.put(comb.comb_str,subsetcombs);
                if(SHOW_NEG_INTERACTIONS)//add all interactions.                
                {
                    if(comb.kwii>=kwiithreshold)
                        ALL_KWII.put(comb.comb_str+(N-1)+"|",new Double(comb.kwii));
                }
                else
                {
                    if(comb.kwii>=kwiithreshold)
                        ALL_KWII.put(comb.comb_str+(N-1)+"|",new Double(comb.kwii));
                }
            }
            collection = L.copy();
            L.clear();

            long tf = System.currentTimeMillis();
            System.out.println(collection.size()+" elements in buffer");
            System.out.println("Time taken = "+  (((double)(tf-ts))/1000));

            //System.out.println(ALL_KWII.size());

            for (int iter=1;iter<SEARCH_ITER;iter++)
            {
                ts = System.currentTimeMillis();
                System.out.println("\nIteration : "+iter+" with "+collection.size()*(N-1)+" steps");
                //for each chain of collection do.
                int R = collection.size()*(N-1);
                Iterator I = collection.keySet().iterator();
                //collect K top combinations in L in this iteration.
                L = new LIST(THETA);//infinite buffer when THETA = -1.
                float step = 1;
                int mark = 0;
                while(I.hasNext())
                {
                    String comb = (String)I.next();//get an entry from collection.
                    //for each snp do.
                    for(int snp=0;snp<N-1;snp++)
                    {
                        step = step + 1;
                        String new_comb = create_id(comb,snp);
                        if(collection.containsKey(new_comb)==false && new_comb.compareTo(comb)!=0)
                        {
                            Combination C = new Combination(new_comb);
                            C.last_to_enter = snp;
                            C.parent = comb;
                            double two_wii = Two_wii(Data,C.comb_vec);
                            C.pai = two_wii;
                            L.add(C);
                            //addTo(comb,new_comb,snp,PAI_MAP,ALL_KWII,Data,two_wii);
                        }
                        float f = 100*step/R;
                        if(f >= mark)
                        {
                            int progress = (int)Math.floor(f);
                            System.out.print(" "+progress+"% ");
                            mark = mark+10;
                        }
                    }
            	}//end while.

                //for only those combinations present in L, compute kwii of all possible subsets.
                addTo(L,PAI_MAP,ALL_KWII,Data);

            	System.out.println();
            	collection.clear();
            	collection = L.copy();
            	System.out.println(collection.size()+" elements in buffer");
            	L.clear();
            	System.gc();
            	tf = System.currentTimeMillis();
            	System.out.println("Time taken = "+  (((double)(tf-ts))/1000));
            }//end for.
            System.out.println("Finally ALL_KWII = "+ALL_KWII.size()+" PAI_MAP = "+PAI_MAP.size()); 

            for(int ncomb=1;ncomb<=SEARCH_ITER;ncomb++)
            {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fnameout+"_"+ncomb+".dat")));
                out.write("Comb \t Kwii \t Pai \n");
                
            	Iterator I = ALL_KWII.keySet().iterator();
            	while(I.hasNext())
            	{
                    String comb = (String)I.next();
                    Vector colarr1 = convert1(comb,N-1); //combination without the disease phenotype.
                    Combination C1 = new Combination(colarr1);
                    if(colarr1.size()==ncomb)
                    {
                        //System.out.println("Writing comb "+comb);
                        double kwii = ((Double)ALL_KWII.get(comb)).doubleValue();
                        Combination C = new Combination(comb);
                        //double tic = tic(Data,C.comb_vec);
                        double pai = Two_wii(Data,C1.comb_vec);
                        String name = get_name(C.comb_str);
                        out.write(name+"\t"+kwii+"\t"+pai+"\n");
                    }
                }
                out.close();
            }
            
	}

	public Vector convert1(String comb,int n)
	{
		//convert comb to a vector, but leave out the snp n.
		StringTokenizer st = new StringTokenizer(comb,"|");
		Vector V = new Vector();
		boolean found = false;
		while(st.hasMoreTokens())
		{
			Integer v = Integer.valueOf(st.nextToken());
			if(v.intValue()!=n)
				V.add(""+v.intValue());
			else
				found = true;
		}
		if(found==false)
		{
			System.out.println(n+" not found in "+comb);
			System.exit(0);
		}
		return V;
	}
}

