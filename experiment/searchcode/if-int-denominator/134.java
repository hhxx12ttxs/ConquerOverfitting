import java.util.*;
import java.io.*;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.*;
import cern.colt.matrix.doublealgo.*;
import cern.jet.random.engine.*;
import cern.jet.random.*; 

class Pool
{
    int[] rowval; //value of the corresponding discrete row, can be null, in that case this pool is unconditional.
    String rowname = "";
    Vector data;
    DoubleMatrix2D sigma = null;
    DoubleMatrix2D inv_sigma = null;
    double det_sigma = 0;
    
    public Pool(String r)
    {
        data = new Vector();
        rowname = r;
        StringTokenizer st = new StringTokenizer(r,"\t, ");
        int i = 0;
        rowval = new int[st.countTokens()];
        while(st.hasMoreTokens())
        {
            String s = st.nextToken();
            rowval[i] = Integer.valueOf(s);
            i++;
        }
    }
    
    void set(DoubleMatrix2D sigma,double det,DoubleMatrix2D inv_sigma)
    {
        this.sigma = sigma;
        this.det_sigma = det_sigma;
        this.inv_sigma = inv_sigma;
    }
    
    static void print(double[] row) throws Exception
    {
        for(int i=0;i<row.length;i++)
        {
            System.out.print(row[i]);
        }
        System.out.println();
    }
    
    static void print(int[] row) throws Exception
    {
        for(int i=0;i<row.length;i++)
        {
            System.out.print(row[i]);
        }
        System.out.println();
    }    
    
    void calc() throws Exception
    {
        if(data.size()>5)
        {
            //System.out.print("Pool for row : ");
            //print(rowval);
            double[] row = (double[])data.get(0);
            //System.out.println("data->");
            //System.out.println("data size ->"+data.size());
            double[][] D = new double[data.size()][row.length];
            for(int i=0;i<D.length;i++)
            {
                D[i] = (double[])data.get(i);
                //print(D[i]);
            }
            Algebra A = new Algebra();
            sigma = Statistic.covariance(new DenseDoubleMatrix2D(D));
            if(EstimateParzen.use_only_diag==true)
            {
                //keep only the diagonal terms, all else set to 0.
                for(int i=0;i<sigma.rows();i++)
                    for(int j=0;j<sigma.columns();j++)
                        if(i!=j)
                            sigma.set(i,j,0.0);            
            }            
            det_sigma = A.det(sigma);
            inv_sigma = A.inverse(sigma);
        }
    }
    
    static Vector get_big_cov(double[][] D) throws Exception
    {
        Vector res = new Vector();
        Algebra A = new Algebra();        
        DoubleMatrix2D sigma = Statistic.covariance(new DenseDoubleMatrix2D(D));        
        if(EstimateParzen.use_only_diag==true)
        {
            //keep only the diagonal terms, all else set to 0.
            for(int i=0;i<sigma.rows();i++)
                for(int j=0;j<sigma.columns();j++)
                    if(i!=j)
                        sigma.set(i,j,0.0);            
        }
        double det_sigma = A.det(sigma);
        DoubleMatrix2D inv_sigma = A.inverse(sigma);
        res.add(sigma);
        res.add(new Double(det_sigma));
        res.add(inv_sigma);
        return res;
    }
}

public class EstimateParzen
{
    static int INVALID_VAL = -99;
    static HashMap Pai_MAP = new HashMap();//just stores the pai of each combination, not the entire combination.    
    static HashMap Kwii_MAP = new HashMap();//just stores the kwii of each combination, not the entire combination.    
    static boolean use_big_cov_matrix = false; //use the full covariance matrix of all samples.
    static boolean use_only_diag = false; //use only the diagonal terms of the above covariance matrix.    
    static int N = 0;
    
    static void init(boolean b1,boolean b2,int n,int inv)
    {
        N = n;
        use_big_cov_matrix = b1;
        use_only_diag = b2;        
        INVALID_VAL = inv;
    }
    
    static double phi(double[] x, double[] xi,double h, DoubleMatrix2D sigma, DoubleMatrix2D inv_sigma) throws Exception
    {
        Algebra A = new Algebra();
        double det_sigma = A.det(sigma);
        int dim = x.length;
        double coeff = Math.pow(h*h*2*Math.PI,((double)dim)/2.0) * Math.sqrt(Math.abs(det_sigma));
        double[][] diff = new double[dim][1];//dim x 1 column vector.
        for(int t=0;t<dim;t++) diff[t][0] = x[t] - xi[t];
        DenseDoubleMatrix2D V = new DenseDoubleMatrix2D(diff) ;//column vector.
        DoubleMatrix2D R = A.mult(A.transpose(V),A.mult(inv_sigma,V));
        double[][] X = R.toArray();
        if(X.length>1 || X[0].length>1)
            System.out.println("Some error");
        double v = Math.exp(-0.5*X[0][0]/(h*h))/coeff;
        return v;
    }

    static double conditional_probability(double[] x, Pool pool, double h) throws Exception
    {        
        //Evaluate p(x|d=rowval) 
        //The probability is to be evaluated at x.        
        double nc = pool.data.size();        
        double p = 0;
        if(nc>0)
        {
            for(int i=0;i<nc;i++)
            {
                double[] pool_row = (double[])pool.data.get(i);
                if(use_big_cov_matrix==false)
                {
                    if(pool.data.size()>5)
                        p += phi(x,pool_row,h,pool.sigma,pool.inv_sigma);                
                    else
                        p += ((double)pool.data.size())/N;
                }
                else
                    p += phi(x,pool_row,h,pool.sigma,pool.inv_sigma);                
            }
            p /= nc;
        }        
        return p;
    }
    
    static double conditional_probability1(double[] xi, Pool pool, double h, HashMap H) throws Exception
    {
        //System.out.println("Evaluating p(d|xi) : ");
        //Evaluate p(c|x).        
        double nc = pool.data.size();
        double numerator = 0;
        if(nc>0)
        {
            for(int j=0;j<nc;j++)
            {
                double[] pool_row = (double[])pool.data.get(j);
                double phi = 0;
                if(use_big_cov_matrix==false)
                {
                    if(pool.data.size()>5)
                        phi = phi(xi,pool_row,h,pool.sigma,pool.inv_sigma);                
                    else
                        phi = ((double)pool.data.size())/N;
                }
                else
                    phi = phi(xi,pool_row,h,pool.sigma,pool.inv_sigma);                
                numerator += phi;
            }            
        }
        //System.out.println("numerator = "+numerator);
        //Now calculate the denominator.
        double denominator = 0;
        Iterator I = H.keySet().iterator();
        while(I.hasNext())
        {
            String row = (String)I.next();            
            Pool cont_pool = (Pool)H.get(row);
            double nd = cont_pool.data.size();
            double sum = 0;
            if(nd>0)
            {
                for(int j=0;j<nd;j++)
                {
                    double[] pool_row = (double[])cont_pool.data.get(j);
                    if(use_big_cov_matrix==false)
                    {
                        if(cont_pool.data.size()>5)
                            sum += phi(xi,pool_row,h,cont_pool.sigma,cont_pool.inv_sigma);
                        else
                            sum += ((double)cont_pool.data.size())/N;
                    }
                    else
                        sum += phi(xi,pool_row,h,cont_pool.sigma,cont_pool.inv_sigma);
                }            
            }
            //System.out.println("pool = "+row+" has "+sum);
            denominator += sum;
        }
        //System.out.println("denominator = "+denominator);
        return (numerator/denominator);
    } 
    
    public static double mutual_info(double[][] Data,Vector discrete_cols,Vector continuous_cols) throws Exception
    {
        //To calculate I(X,C) where X is discrete and C is continuous.
        //I(X,C) = H(X) - H(X|C).
        int nvalid = 0;
        int md = discrete_cols.size();
        int mc = continuous_cols.size();
        int n = Data.length;
        HashMap H = new HashMap();
        
        double[][] Dcont = new double[n][mc]; //only continuous part of the Data.        
        //get the pools of continuous values for each genotype.        
        for(int i=0;i<n;i++)
        {
            String row = "";
            boolean invalid = false;
            for(int j=0;j<md;j++)
            {
                int discrete_col = Integer.valueOf((String)discrete_cols.get(j));
                if(((int)Data[i][discrete_col])==INVALID_VAL)
                {
                    invalid = true;
                    break;
                }
                //a valid row of discrete values.
                row += (int)Data[i][discrete_col]+",";
            }
            if(invalid==false)
            {
                Pool cont_pool = (Pool)H.get(row);     //cont_pool is the pool of values corresponding to this discrete value.
                                                       //each value is a multivariate string holding again the values for 
                                                       //the continuous variables.
                if(cont_pool==null) cont_pool = new Pool(row);
                double[] vals = new double[mc];
                for(int j=0;j<mc;j++)
                {
                    int continuous_col = Integer.valueOf((String)continuous_cols.get(j));                    
                    if(Data[i][continuous_col]==INVALID_VAL)
                    {
                        invalid=true;
                        break;
                    }
                    vals[j] = Data[i][continuous_col];
                    Dcont[i][j] = vals[j];
                }
                if(invalid==false)
                {
                    cont_pool.data.add(vals);
                    H.put(row,cont_pool);
                    nvalid++;
                }
            }
        }
        //Now H has a pool of continuous values for each discrete value of the discrete column.
        DoubleMatrix2D big_sigma = null;
        Double big_det_sigma = null;
        DoubleMatrix2D big_inv_sigma = null;
        if(use_big_cov_matrix==true)
        {
            Vector res = Pool.get_big_cov(Dcont);
            big_sigma = (DoubleMatrix2D)res.get(0);
            big_det_sigma = (Double)res.get(1);
            big_inv_sigma = (DoubleMatrix2D)res.get(2);
        }                        
        Iterator I = H.keySet().iterator();                        
        while(I.hasNext()) //for each d.
        {
            String row = (String)I.next();
            Pool cont_pool = (Pool)H.get(row);
            if(use_big_cov_matrix==false)
            {
                System.out.println("Pool size specific = "+cont_pool.data.size());
                cont_pool.calc();//calculate the matrices sigma, inverse_sigma etc.
            }
            else
            {
                //override.
                //System.out.println("Pool size big = "+Dcont.length);
                cont_pool.set(big_sigma,big_det_sigma,big_inv_sigma);
            }                        
        }
        //calculate H(P|X) and H(P) simultaneously
        double h = 1/Math.log(n);
        double H_P_given_X = 0;//Think of P as X and X as C.
        double H_P = 0;
        I = H.keySet().iterator();        
        while(I.hasNext()) //for each d.
        {
            String row = (String)I.next();
            Pool cont_pool = (Pool)H.get(row);            
            double sum = 0;
            double pd = ((double)cont_pool.data.size())/nvalid;
            System.out.println("Pool -> "+cont_pool.rowname+","+cont_pool.data.size()+","+nvalid);
            for(int i=0;i<n;i++) //for each xi in Dcont.
            {
                double[] xi = Dcont[i];
                double prob_d_given_xi = conditional_probability1(xi,cont_pool,h,H);                
                double g = 0;
                if(prob_d_given_xi==0)
                {
                    //Ignore this small probability.
                }
                else
                {
                    g = prob_d_given_xi*Math.log(prob_d_given_xi);                
                    System.out.println(i+" For "+xi[0]+" : Under pool = "+row+" : log ( "+prob_d_given_xi+" ) = "+g);
                    H_P_given_X += g;
                }
            }
            H_P += pd*Math.log(pd);
            //System.out.println("----------------");
        }
        H_P = -H_P;
        H_P_given_X = -H_P_given_X/n;
        System.out.println("---------------H(P|X) = "+H_P_given_X);
        System.out.println("---------------H(P) = "+H_P);
        System.out.println("---------------mui = "+(H_P-H_P_given_X));
        return H_P - H_P_given_X;
    }
    
    public static double triple_info(double[][] Data,Vector discrete_cols,int C) throws Exception
    {        
        //To calculate I(V1,V2,C) = I(V1V2,C) - I(V1,C) - I(V2,C).
        if(discrete_cols.size()==2)
        {            
            int V1 = Integer.valueOf((String)discrete_cols.get(0));
            int V2 = Integer.valueOf((String)discrete_cols.get(1));
            //System.out.println(X+","+D);
            //To calculate I(D,P,X) where D,P are each univariate discrete and X is univariate continuous.            
            //I(D,P,X) = I(DP,X) - I(D,X) - I(P,X).
            
            Vector d_cols = new Vector();d_cols.add(""+V1);d_cols.add(""+V2);
            Vector c_cols = new Vector();c_cols.add(""+C);

            double v1 = mutual_info(Data,d_cols,c_cols); //d_cols = V1,V2, c_cols = C, I(V1V2,C)            
            d_cols.remove(1);
            double v2 = mutual_info(Data,d_cols,c_cols); //d_cols = V1 c_cols = C, I(V1,C)            
            d_cols.remove(0);d_cols.add(""+V2);
            double v3 = mutual_info(Data,d_cols,c_cols); //d_cols = V2 c_cols = C, I(V2,C)            
            return v1 - v2 - v3;            
        }
        else
        {
            System.out.println("Cannot calculate triple info with "+discrete_cols+" and "+C);
            System.exit(0);
        }
        return 0;        
    }
    
    public static double tetra_info(double[][] Data,Vector discrete_cols,int C) throws Exception
    {
        System.out.println("KWII("+discrete_cols+","+C+") ->");
        //To calculate I(V1,V2,V3,C)      
        //First get pure data.
        if(discrete_cols.size()==3)
        {            
            int V1 = Integer.valueOf((String)discrete_cols.get(0));
            int V2 = Integer.valueOf((String)discrete_cols.get(1));            
            int V3 = Integer.valueOf((String)discrete_cols.get(2));            
            double sum = 0;
            
            //To evaluate I(V1,V2,V3,C).                        
            //I(V1V2V3,C).
            {                   
                Vector vd=new Vector();vd.add(""+V1);vd.add(""+V2);vd.add(""+V3);
                Vector vc = new Vector();vc.add(""+C);
                String str = Combination.convert(vd);                
                Double mui = (Double)Pai_MAP.get(str);
                double m = 0;
                if(mui!=null) 
                {
                    m = mui.doubleValue();
                    sum += m;
                }
                else
                {
                    m = mutual_info(Data,vd,vc);
                    Pai_MAP.put(str,new Double(m));
                    sum += m;
                }
                System.out.println("PAI("+vd+","+vc+") = "+m);
            }
            
            //I(V1,P).
            {
                Vector vd=new Vector();vd.add(""+V1);
                Vector vc = new Vector();vc.add(""+C);
                String str = V1+"|"+C+"|";
                Combination G = null;
                if((G = (Combination)Kwii_MAP.get(str))!=null)
                {
                    sum -= G.kwii;
                }
                else
                {
                    double m = mutual_info(Data,vd,vc);
                    G = new Combination(str);
                    G.kwii = m;
                    Kwii_MAP.put(str,G);
                    sum -= m;
                }
                System.out.println("KWII("+vd+","+vc+") = "+G.kwii);
            }

            //I(V2,P).
            {
                Vector vd=new Vector();vd.add(""+V2);
                Vector vc = new Vector();vc.add(""+C);
                String str = V2+"|"+C+"|";
                Combination G = null;
                if((G = (Combination)Kwii_MAP.get(str))!=null)
                {
                    sum -= G.kwii;
                }
                else
                {
                    double m = mutual_info(Data,vd,vc);
                    G = new Combination(str);
                    G.kwii = m;
                    Kwii_MAP.put(str,G);
                    sum -= m;
                }
                System.out.println("KWII("+vd+","+vc+") = "+G.kwii);
            }
            
            //I(V3,P).
            {
                Vector vd=new Vector();vd.add(""+V3);
                Vector vc = new Vector();vc.add(""+C);
                String str = V3+"|"+C+"|";
                Combination G = null;
                if((G = (Combination)Kwii_MAP.get(str))!=null)
                {
                    sum -= G.kwii;
                }
                else
                {
                    double m = mutual_info(Data,vd,vc);
                    G = new Combination(str);
                    G.kwii = m;
                    Kwii_MAP.put(str,G);
                    sum -= m;
                }
                System.out.println("KWII("+vd+","+vc+") = "+G.kwii);
            }                        
            
            
            //I(V1,V2,P).
            {
                Vector vd=new Vector();vd.add(""+V2);
                Vector vc = new Vector();vc.add(""+C);                                
                String str = "";
                if(V1<V2) str = V1+"|"+V2+"|"+C+"|";
                else str = V2+"|"+V1+"|"+C+"|";
                Combination G = null;
                if((G = (Combination)Kwii_MAP.get(str))!=null)
                {
                    sum -= G.kwii;
                }
                else
                {
                    double m = triple_info(Data,vd,C);
                    G = new Combination(str);
                    G.kwii = m;
                    Kwii_MAP.put(str,G);
                    sum -= m;                    
                }
                System.out.println("KWII("+vd+","+vc+") = "+G.kwii);
            }

            //I(V1,V3,P).
            {
                Vector vd=new Vector();vd.add(""+V3);
                Vector vc = new Vector();vc.add(""+C);                                
                String str = "";
                if(V1<V3) str = V1+"|"+V3+"|"+C+"|";
                else str = V3+"|"+V1+"|"+C+"|";
                Combination G = null;
                if((G = (Combination)Kwii_MAP.get(str))!=null)
                {
                    sum -= G.kwii;
                }
                else
                {
                    double m = triple_info(Data,vd,C);
                    G = new Combination(str);
                    G.kwii = m;
                    Kwii_MAP.put(str,G);
                    sum -= m;                    
                }
                System.out.println("KWII("+vd+","+vc+") = "+G.kwii);
            }

            //I(V2,V3,P).
            {
                Vector vd=new Vector();vd.add(""+V3);
                Vector vc = new Vector();vc.add(""+C);                                
                String str = "";
                if(V2<V3) str = V2+"|"+V3+"|"+C+"|";
                else str = V3+"|"+V2+"|"+C+"|";
                Combination G = null;
                if((G = (Combination)Kwii_MAP.get(str))!=null)
                {
                    sum -= G.kwii;
                }
                else
                {
                    double m = triple_info(Data,vd,C);
                    G = new Combination(str);
                    G.kwii = m;
                    Kwii_MAP.put(str,G);
                    sum -= m;                    
                }
                System.out.println("KWII("+vd+","+vc+") = "+G.kwii);
            }
            
            return sum;
        }
        else
        {
            System.out.println("Cannot calculate tetra info with "+discrete_cols+" and "+C);
            System.exit(0);
        }
        return 0;
    }
    
    static double[][] get_col_data_pure(double[][] D,Vector col_arr) throws Exception
    {
        //get all columns from data D with rows having no invalid values for the columns in col_arr.        
        int m = D.length;
        int n = col_arr.size();
        Vector X = new Vector();

        for(int i=0;i<m;i++)
        {
            boolean inv = false;
            for(int j=0;j<n;j++)
            {                
                int col = Integer.valueOf((String)col_arr.get(j));
                if(D[i][col]==INVALID_VAL)
                {
                    inv = true;
                    break;
                }
            }
            if(inv==false)
                X.add(D[i]);
        }

        double[][] Y = new double[X.size()][n];
        for(int i=0;i<X.size();i++)
        {
            Y[i] = (double[])X.get(i);
        }        
        return Y;
    }    
    
    public static double Two_wii(double[][] D,Combination Cmb) throws Exception
    {        
        Combination C = new Combination(Cmb.comb_str+(D[0].length-1)+"|");        
        Combination G = null;
        if((G = (Combination)Pai_MAP.get(C.comb_str))!=null)
        {
            return G.pai;
        }
        //Get pure data.
        double[][] Data = get_col_data_pure(D,C.comb_vec);//all columns of D are preserved.        
        if(C.comb_vec.size()==2)
        {
            int col0 = Integer.valueOf((String)C.comb_vec.get(0));
            int P = Integer.valueOf((String)C.comb_vec.get(1));
            Vector vd = new Vector();vd.add(""+col0);
            Vector vc = new Vector();vc.add(""+P);
            double pai = mutual_info(Data,vd,vc);
            C.pai = pai;
            Pai_MAP.put(C.comb_str,C);
            return pai;
        }
        else if(C.comb_vec.size()==3)
        {
            int col0 = Integer.valueOf((String)C.comb_vec.get(0));
            int col1 = Integer.valueOf((String)C.comb_vec.get(1));
            int P = Integer.valueOf((String)C.comb_vec.get(2));
            Vector vd = new Vector();vd.add(""+col0);vd.add(""+col1); 
            Vector vc = new Vector();vc.add(""+P);
            double pai = mutual_info(Data,vd,vc);
            C.pai = pai;
            Pai_MAP.put(C.comb_str,C);
            return pai;
        }
        else if(C.comb_vec.size()==4)
        {
            int col0 = Integer.valueOf((String)C.comb_vec.get(0));
            int col1 = Integer.valueOf((String)C.comb_vec.get(1));
            int col2 = Integer.valueOf((String)C.comb_vec.get(2));
            int P = Integer.valueOf((String)C.comb_vec.get(3));
            Vector vd = new Vector();vd.add(""+col0);vd.add(""+col1);vd.add(""+col2);
            Vector vc = new Vector();vc.add(""+P);
            double pai = mutual_info(Data,vd,vc);
            C.pai = pai;
            Pai_MAP.put(C.comb_str,C);
            return pai;            
        }
        else
        {
            System.out.println("Parzen Estimation : PAI for order "+(C.comb_vec.size()-1)+" not supported yet!!!");
        }
        return 0;        
    }    
    
    public static double kwii(double[][] D,Combination C) throws Exception
    {        
        Combination G = null;
        if((G = (Combination)Kwii_MAP.get(C.comb_str))!=null)
        {
            return G.kwii;
        }
        //Get pure data.
        double[][] Data = get_col_data_pure(D,C.comb_vec);//all columns of D are preserved.        
        if(C.comb_vec.size()==2)
        {
            int col0 = Integer.valueOf((String)C.comb_vec.get(0));
            int P = Integer.valueOf((String)C.comb_vec.get(1));
            Vector vd = new Vector();vd.add(""+col0);
            Vector vc = new Vector();vc.add(""+P);
            double kwii = mutual_info(Data,vd,vc);
            C.kwii = kwii;
            Kwii_MAP.put(C.comb_str,C);
            return kwii;
        }
        else if(C.comb_vec.size()==3)
        {
            int col0 = Integer.valueOf((String)C.comb_vec.get(0));
            int col1 = Integer.valueOf((String)C.comb_vec.get(1));
            int P = Integer.valueOf((String)C.comb_vec.get(2));
            Vector vd = new Vector();vd.add(""+col0);vd.add(""+col1);            
            double kwii = triple_info(Data,vd,P);
            C.kwii = kwii;
            Kwii_MAP.put(C.comb_str,C);
            return kwii;
        }
        else if(C.comb_vec.size()==4)
        {
            int col0 = Integer.valueOf((String)C.comb_vec.get(0));
            int col1 = Integer.valueOf((String)C.comb_vec.get(1));
            int col2 = Integer.valueOf((String)C.comb_vec.get(2));
            int P = Integer.valueOf((String)C.comb_vec.get(3));
            Vector vd = new Vector();vd.add(""+col0);vd.add(""+col1);vd.add(""+col2);            
            double kwii = tetra_info(Data,vd,P);
            C.kwii = kwii;
            Kwii_MAP.put(C.comb_str,C);
            return kwii;            
        }
        else
        {
            System.out.println("Parzen Estimation : KWII for order "+(C.comb_vec.size()-1)+" not supported yet!!!");
        }
        return 0;
    }    
}

