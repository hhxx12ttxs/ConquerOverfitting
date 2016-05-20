import java.util.*;
import java.io.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;
import cern.colt.matrix.doublealgo.*;

public class EntropyNormal
{
    static double INVALID_VAL = 0;
    static boolean[] attribute_status = null;
    static HashMap Kwii_MAP = new HashMap();
    static HashMap Pai_MAP = new HashMap();
    
    static void init(double inv,boolean[] att_stat)
    {
        INVALID_VAL = inv;
        attribute_status = att_stat;
    }

    static double entropy_P(double[][] D) throws Exception
    {
        return entropy_discrete(D);
    }

    static double entropy(double[][] D,Vector discrete_columns,Vector continuous_columns) throws Exception
    {
        if(discrete_columns.size()>0 && continuous_columns.size()>0)
        {
            return entropy_mixed(D,discrete_columns,continuous_columns);
        }
        else if(discrete_columns.size()>0)
        {
            return entropy_discrete(D);
        }
        else
        {
            return entropy_continuous(D);
        }
    }

    static void print(double[][]d)
    {
        for(int i=0;i<d.length;i++)
        {
            for(int j=0;j<d[0].length;j++)
            {
                System.out.print(d[i][j]+"\t");
            }
            System.out.println();
        }
    }

    static double[][] convert(Vector pool)throws Exception
    {
        if(pool.size()<=0) return null;
        String row = (String)pool.get(0);
        StringTokenizer st = new StringTokenizer(row,":");
        int N = st.countTokens();
        double[][] M = new double[pool.size()][N];
        for(int i=0;i<pool.size();i++)
        {
                row = (String)pool.get(i);
                st = new StringTokenizer(row,":");
                int j = 0;
                while(st.hasMoreTokens())
                {
                        M[i][j] = (Double.valueOf(st.nextToken().trim())).doubleValue();
                        j++;
                }
        }
        return M;
    }

    static double entropy_mixed(double[][] D,Vector discrete_columns,Vector continuous_columns) throws Exception
    {
            //We assume the continuous columns to be independent and normally distributed => together multivariate normal.
            //System.out.println("entropy_mixed: discrete columns = "+discrete_columns);
            //System.out.println("entropy_mixed: continuous columns = "+continuous_columns);

            double[][] D_discrete = get_col_data(D,discrete_columns);
            double[][] D_continuous = get_col_data(D,continuous_columns);

            double marker = INVALID_VAL;
            //marker indicates invalid value.
            int m = D_discrete.length;
            int n = D_discrete[0].length;
            int t = D_continuous[0].length;
            HashMap H = new HashMap();

            //convert each row to a string and count the frequency of each row.
            int nvalid = 0;
            for(int i=0;i<m;i++)
            {
                    String row = "";
                    boolean invalid = false;
                    for(int j=0;j<n;j++)
                    {
                            if(((int)D_discrete[i][j])==marker)
                            {
                                    invalid = true;
                                    break;
                            }
                            if(row.compareTo("")==0)
                                    row = ""+((int)D_discrete[i][j]);
                            else
                                    row = row + "|" + ((int)D_discrete[i][j]);
                    }
                    if(invalid==false)
                    {
                            Vector cont_pool = (Vector)H.get(row); //cont_pool is the pool of values corresponding to this discrete value.
                                                                    //each value is a multivariate string holding again the values for the continuous variables.
                            if(cont_pool==null)
                                    cont_pool = new Vector();

                            String vals = new String("");
                            for(int k=0;k<t;k++)
                            {
                                    vals += D_continuous[i][k] + ":";
                                    if(D_continuous[i][k]==marker)
                                    {
                                            invalid=true;
                                            break;
                                    }
                            }
                            if(invalid==false)
                            {
                                    cont_pool.add(vals);
                                    H.put(row,cont_pool);
                                    nvalid++;
                            }
                    }
            }

            //now calculate the entropy.
            double ent_discrete = 0;
            double ent_continuous = 0;
            Iterator I = H.keySet().iterator();
            while(I.hasNext())
            {
                String row = (String)I.next();
                Vector cont_pool = (Vector)H.get(row);
                double p = (double)cont_pool.size()/(double)nvalid;
                //System.out.println("entropy_mixed:["+row+"] "+cont_pool.size()+"->"+p1);
                double a=0;
                double b=0;
                double e = 0;
                e = calc_cont_pool_entropy(cont_pool,nvalid);
                a = p*Math.log(p);
                b = p*e;
                ent_discrete -= a; //H(discrete).
                ent_continuous += b; //H(continuous|discrete).
            }
            double ent = ent_discrete + ent_continuous;
            return ent;
    }

    static double entropy_discrete(double[][] D_discrete) throws Exception
    {
            //System.out.println("entropy_discrete");
            double marker = INVALID_VAL;
            //marker indicates invalid value.
            int m = D_discrete.length;
            int n = D_discrete[0].length;
            HashMap H = new HashMap();

            //convert each row to a string and count the frequency of each row.
            int nvalid = 0;
            for(int i=0;i<m;i++)
            {
                    String row = "";
                    boolean invalid = false;
                    for(int j=0;j<n;j++)
                    {
                            if(((int)D_discrete[i][j])==marker)
                            {
                                    invalid = true;
                                    break;
                            }
                            if(row.compareTo("")==0)
                                    row = ""+((int)D_discrete[i][j]);
                            else
                                    row = row + "|" + ((int)D_discrete[i][j]);
                    }
                    if(invalid==false)
                    {
                            Integer count = (Integer)H.get(row); //cont_pool is the pool of values corresponding to this discrete value.
                                                                    //each value is a multivariate string holding again the values for the continuous variables.
                            if(count==null)
                                    count = new Integer(0);
                            int c = count.intValue();
                            c++;
                            H.put(row,new Integer(c));
                            nvalid++;

                    }
            }
            //now calculate the entropy.
            double ent_discrete = 0;
            Iterator I = H.keySet().iterator();
            while(I.hasNext())
            {
                    String row = (String)I.next();
                    Integer count = (Integer)H.get(row);
                    //System.out.println(row+" "+count);
                    double p1 = (double)count.intValue()/(double)nvalid;
                    ent_discrete -= p1*Math.log(p1); //H(discrete).
            }
            //System.out.println("H(X)="+(ent_discrete)+"\n\n");
            return ent_discrete;
    }

    static double entropy_continuous(double[][] D_continuous) throws Exception
    {
            //System.out.println("entropy_continuous");
            double marker = INVALID_VAL;
            //marker indicates invalid value.
            int m = D_continuous.length;
            int n = D_continuous[0].length;
            int nvalid = 0;
            Vector cont_pool = new Vector();

            for(int i=0;i<m;i++)
            {
                boolean invalid = false;
                String vals = new String("");
                for(int j=0;j<n;j++)
                {
                    vals += D_continuous[i][j] + ":";
                    if(D_continuous[i][j]==marker)
                    {
                        invalid=true;
                        break;
                    }
                }
                if(invalid==false)
                {
                    cont_pool.add(vals);
                    nvalid++;
                }
            }
            //now calculate the entropy.
            double e = calc_cont_pool_entropy(cont_pool,nvalid);
            return e;
    }

    static double calc_cont_pool_entropy(Vector pool,int nSamples) throws Exception
    {
            //calculate the entropy of this pool (2D matrix of data), assume each column is a variable.
            //convert this to a 2D matrix.
            //System.out.println(pool);
            if(pool.size()<=0) return 0;
            String row = (String)pool.get(0);
            StringTokenizer st = new StringTokenizer(row,":");
            int N = st.countTokens();
            double[][] M = new double[pool.size()][N];
            for(int i=0;i<pool.size();i++)
            {
                    row = (String)pool.get(i);
                    st = new StringTokenizer(row,":");
                    int j = 0;
                    while(st.hasMoreTokens())
                    {
                            M[i][j] = (Double.valueOf(st.nextToken().trim())).doubleValue();
                            j++;
                    }
            }
            DenseDoubleMatrix2D DM = new DenseDoubleMatrix2D(M);
            DoubleMatrix2D covM = Statistic.covariance(DM);

            int n = pool.size();
            cern.jet.math.Functions F = cern.jet.math.Functions.functions;
            //multiply each element of covM by n/(n-1).
            covM = covM.assign(F.mult((double)n/(n-1)));

            Algebra A = new Algebra();
            double determinant = A.det(covM);

            double entropy = Math.log(Math.pow(2*Math.PI*Math.exp(1),((double)N)/2.0)*Math.sqrt(Math.abs(determinant)));

            if(Double.isNaN(entropy) || entropy==Double.NEGATIVE_INFINITY || entropy==Double.POSITIVE_INFINITY)
            {
                //covariance matrix is singular, estimate entropy assuming a uniform distribution.
                //System.out.println("Warning:covariance matrix is singular,assuming uniform distribution");
                double p = ((double)pool.size())/nSamples;
                entropy = -p*Math.log(p);
                //System.out.println("ent="+entropy);
            }
            return entropy;
    }

    static double[][] get_col_data(double[][] D,Vector col_arr) throws Exception
    {
            //get column from data D as given in col_arr.
            int m = D.length;
            int n = col_arr.size();
            double[][] X = new double[m][n];

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
    
    static double[][] get_col_data_pure(double[][] D,Vector col_arr) throws Exception
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
        double[][] Y = new double[X.size()][n];
        for(int i=0;i<X.size();i++)
        {
            Y[i] = (double[])X.get(i);
        }
        return Y;
    }
    
    static double[][] get_col_data(double[][] D,Vector col_arr,Vector disc_cols,Vector cont_cols,boolean[] status) throws Exception
    {
        //get column from data D as given in col_arr.
        int m = D.length;
        int n = col_arr.size();
        double[][] X = new double[m][n];

        for(int j=0;j<n;j++)
        {            
            int col = Integer.valueOf((String)col_arr.get(j));
            boolean stat = status[col];//discrete or continuous ?
            for(int i=0;i<m;i++)
            {
                    X[i][j] = D[i][col];
            }
            if(stat==true)
                    disc_cols.add(""+j);
            else
                    cont_cols.add(""+j);
        }
        return X;
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

    static double kwii(double[][] D,Combination C) throws Exception
    {
        Combination G = null;
        if((G = (Combination)Kwii_MAP.get(C.comb_str))!=null)
        {
            return G.kwii;
        }        
        //The disease column is added in col_arr.        
        Vector col_array = C.comb_vec;
        double[][] Dpure = get_col_data_pure(D,col_array);//get pure data containing only these columns.
        Vector col_arr = new Vector();
        boolean[] status = new boolean[col_array.size()];
        for(int i=0;i<col_array.size();i++)
        {            
            int col_org = Integer.valueOf((String)col_array.get(i));
            col_arr.add(""+i);//all columns.
            status[i] = attribute_status[col_org];
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
                double E = entropy(X,disc_cols,cont_cols);
                S = S + E;
            }

            //do term = S * (-1)^(K-k).
            double term = S;
            if((K-k)%2==1)
                    term = -S;

            kwii += term;
        }
        kwii = -kwii;
        C.kwii = kwii;
        Kwii_MAP.put(C.comb_str,C);
        return kwii;
    }

    static double Two_wii(double[][] D,Combination C)throws Exception
    {
        Combination G = null;
        if((G = (Combination)Pai_MAP.get(C.comb_str))!=null)
        {
            return G.pai;
        }
        
        Vector A = new Vector(C.comb_vec);
        //The disease column is not added in A.
        int N = D[0].length;
        A.add(""+(N-1));
        double[][] Dpure = get_col_data_pure(D,A);//get pure data containing only these columns.

        boolean[] status = new boolean[A.size()];
        Vector disc_cols = new Vector();
        Vector cont_cols = new Vector();
        for(int i=0;i<A.size();i++)
        {            
            int col_org = Integer.valueOf((String)A.get(i));
            status[i] = attribute_status[col_org];
            if(status[i]==true)
                disc_cols.add(""+i);
            else
                cont_cols.add(""+i);
        }

        Vector W1 = new Vector();
        Vector W2 = new Vector();
        for(int i=0;i<A.size()-1;i++)
        {
            W1.add(""+i);//all except last column.
        }
        W2.add(""+(A.size()-1));//only last column.

        Vector disc_cols_X1 = new Vector();
        Vector cont_cols_X1 = new Vector();
        double[][] X1 = get_col_data(Dpure,W1,disc_cols_X1,cont_cols_X1,status);
        double e1 = entropy(X1,disc_cols_X1,cont_cols_X1);

        //entropy of the phenotype.
        Vector disc_cols_X2 = new Vector();
        Vector cont_cols_X2 = new Vector();
        double[][] X2 = get_col_data(Dpure,W2,disc_cols_X2,cont_cols_X2,status);
        double e2 = entropy(X2,disc_cols_X2,cont_cols_X2);

        double e3 = entropy(Dpure,disc_cols,cont_cols);
        double pai = e1 + e2 - e3;
        C.pai = pai;
        Pai_MAP.put(C.comb_str,C);
        return pai;
    }    
}

