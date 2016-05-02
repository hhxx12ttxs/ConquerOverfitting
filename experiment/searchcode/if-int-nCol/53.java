import java.util.*;
import java.io.*;
import cern.jet.random.*;
import cern.jet.random.engine.*;

public class AS159
{
    static int ierror = 0;
    static boolean key;
    static int seed = 1333877;
    static double[] fact = null;
    static int ntotal = 0;
    static MersenneTwister RR = null; 
    
    static int i4vec_sum ( int n, int[] a ) throws Exception
    {
        int sum = 0;
        for (int i = 0; i < n; i++ )
        {
            sum = sum + a[i];
        }
        return sum;        
    }
    
    static double r8_uniform_01 ()
    {
        /*
        int k;
        double r;
        k = seed / 127773;
        seed = 16807 * ( seed - k * 127773 ) - k * 2836;
        if ( seed < 0 )
        {
            seed = seed + 2147483647;
        }
        r = ( double ) ( seed ) * 4.656612875E-10;
        return r;
        */
        return RR.nextDouble(); 
    }
    
    static void rcont2 (int nrow, int ncol, int[] nrowt, int[] ncolt, int[] matrix) throws Exception
    {
        boolean done1 = false;
        boolean done2 = false;        
        int i = 0;
        int ia = 0;
        int iap = 0;
        int ib = 0;
        int ic = 0;
        int id = 0;
        int idp = 0;
        int ie = 0;
        int igp = 0;
        int ihp = 0;
        int ii = 0;
        int iip = 0;
        int j = 0;
        int jc = 0;
        int[] jwork = null;
        int l = 0;
        boolean lsm = false;
        boolean lsp = false;
        int m = 0;
        int nll = 0;
        int nlm = 0;
        int nlmp = 0;
        int nrowtl = 0;
        double r = 0;
        double sumprb = 0;
        double x = 0;
        double y = 0;
        ierror = 0;
        //
        //  On user's signal, set up the factorial table.
        //
        if (!key)
        {
            key = true;
            if (nrow <= 1)
            {
                ierror = 1;
                System.out.println("RCONT - Fatal error!\n Input number of rows is less than 2.\n");
                return;
            }
            if (ncol <= 1)
            {
                ierror = 2;
                System.out.println("RCONT - Fatal error!\n Input number of columns is less than 2.\n");
                return;
            }
            for (i = 0; i < nrow; i++ )
            {
                if ( nrowt[i] <= 0 )
                {
                    ierror = 3;
                    System.out.println("RCONT - Fatal error!\n An entry in the row sum vector is not positive.\n");
                    return;
                }
            }
            for ( j = 0; j < ncol; j++ )
            {
                if ( ncolt[j] <= 0 )
                {
                    ierror = 4;
                    System.out.println("RCONT - Fatal error!\n An entry in the column sum vector is not positive.\n");
                    return;
                }
            }

            if ( i4vec_sum ( ncol, ncolt ) != i4vec_sum ( nrow, nrowt ) )
            {
                ierror = 6;
                System.out.println("RCONT - Fatal error!\n The row and column sum vectors do not have the same sum.\n");          
                return;
            }

            ntotal = i4vec_sum ( ncol, ncolt );
            fact = new double[ntotal+1];
            //
            //  Calculate log-factorials.
            //            
            x = 0.0;
            fact[0] = 0.0;
            for ( i = 1; i <= ntotal; i++ )
            {
                x = x + Math.log ( ( double ) ( i ) );
                fact[i] = x;
            }
        }        
        //
        //  Construct a random matrix.
        //
        jwork = new int[ncol];
        for ( i = 0; i < ncol - 1; i++ )
        {
            jwork[i] = ncolt[i];
        }
        jc = ntotal;        
        for ( l = 0; l < nrow - 1; l++ )
        {
            nrowtl = nrowt[l];
            ia = nrowtl;
            ic = jc;
            jc = jc - nrowtl;
            for ( m = 0; m < ncol - 1; m++ )
            {
                id = jwork[m];
                ie = ic;
                ic = ic - id;
                ib = ie - ia;
                ii = ib - id;
                //
                //  Test for zero entries in matrix.
                //
                if ( ie == 0 )
                {
                    ia = 0;
                    for ( j = m; j < ncol; j++ )
                    {
                        matrix[l+j*nrow] = 0;
                    }
                    break;
                }
                //
                //  Generate a pseudo-random number.
                //
                //r = r8_uniform_01 ( seed );
                r = r8_uniform_01 ();
                //
                //  Compute the conditional expected value of MATRIX(L,M).
                //
                done1 = false;

                for ( ; ; )
                {
                    nlm = ( int ) ( ( double ) ( ia * id ) / ( double ) ( ie ) + 0.5 );
                    iap = ia + 1;
                    idp = id + 1;
                    igp = idp - nlm;
                    ihp = iap - nlm;
                    nlmp = nlm + 1;
                    iip = ii + nlmp;

                    x = Math.exp ( fact[iap-1] + fact[ib] + fact[ic] + fact[idp-1] - fact[ie] - fact[nlmp-1] - fact[igp-1] - fact[ihp-1] - fact[iip-1] );

                    if ( r <= x )
                    {
                      break;
                    }
                    sumprb = x;
                    y = x;
                    nll = nlm;
                    lsp = false;
                    lsm = false;
                    //
                    //  Increment entry in row L, column M.
                    //
                    while ( !lsp )
                    {
                        j = ( id - nlm ) * ( ia - nlm );
                        if ( j == 0 )
                        {
                            lsp = true;
                        }
                        else
                        {
                            nlm = nlm + 1;
                            x = x * ( double ) ( j ) / ( double ) ( nlm * ( ii + nlm ) );
                            sumprb = sumprb + x;
                            if ( r <= sumprb )
                            {
                                done1 = true;
                                break;
                            }
                        }
                        done2 = false;
                        while ( !lsm )
                        {
                            //
                            //  Decrement the entry in row L, column M.
                            //
                            j = nll * ( ii + nll );
                            if ( j == 0 )
                            {
                              lsm = true;
                              break;
                            }
                            nll = nll - 1;
                            y = y * ( double ) ( j ) / ( double ) ( ( id - nll ) * ( ia - nll ) );
                            sumprb = sumprb + y;
                            if ( r <= sumprb )
                            {
                                nlm = nll;
                                done2 = true;
                                break;
                            }
                            if ( !lsp )
                            {
                                break;
                            }
                        }  
                        if ( done2 )
                        {
                            break;
                        }
                    }
                    if ( done1 )
                    {
                        break;
                    }
                    if ( done2 )
                    {
                        break;
                    }
                    //r = r8_uniform_01 ( seed );
                    r = r8_uniform_01 ();
                    r = sumprb * r;
                }
                matrix[l+m*nrow] = nlm;
                ia = ia - nlm;
                jwork[m] = jwork[m] - nlm;
            }
            matrix[l+(ncol-1)*nrow] = ia;
        }
        //
        //  Compute the last row.
        //
        for ( j = 0; j < ncol - 1; j++ )
        {
            matrix[nrow-1+j*nrow] = jwork[j];
        }
        matrix[nrow-1+(ncol-1)*nrow] = ib - matrix[nrow-1+(ncol-2)*nrow];        
        return;        
    }

    /*
    public static void main(String args[]) throws Exception
    {
        int M = 5;
        int N = 5;
        int[] a = new int[M*N];
        int[] c = { 2, 2, 2, 2, 1 };
        int i;
        ierror = 0;
        key = false;
        int m = M;
        int n = N;
        int ntest = 10;
        int[] r = { 3, 2, 2, 1, 1 };
        seed = 123456789;
        rcont2 ( m, n, r, c, a);
        System.out.println(a[0]+"   "+a[1]+"   "+a[2]+"   "+a[3]+"   "+a[4]);
        System.out.println(a[5]+"   "+a[6]+"   "+a[7]+"   "+a[8]+"   "+a[9]);
        System.out.println(a[10]+"   "+a[11]+"   "+a[12]+"   "+a[13]+"   "+a[14]);
        System.out.println(a[15]+"   "+a[16]+"   "+a[17]+"   "+a[18]+"   "+a[19]);
        System.out.println(a[20]+"   "+a[21]+"   "+a[22]+"   "+a[23]+"   "+a[24]);   
        System.out.println("---------------");
        
        rcont2 ( m, n, r, c, a);
        System.out.println(a[0]+"   "+a[1]+"   "+a[2]+"   "+a[3]+"   "+a[4]);
        System.out.println(a[5]+"   "+a[6]+"   "+a[7]+"   "+a[8]+"   "+a[9]);
        System.out.println(a[10]+"   "+a[11]+"   "+a[12]+"   "+a[13]+"   "+a[14]);
        System.out.println(a[15]+"   "+a[16]+"   "+a[17]+"   "+a[18]+"   "+a[19]);
        System.out.println(a[20]+"   "+a[21]+"   "+a[22]+"   "+a[23]+"   "+a[24]);           
    }
    */

    public static void main(String args[]) throws Exception
    {
        //3  - 957 449  = 1406
        //2  - 1023 1373  = 2396
        //1  - 520 678  = 1198
        int M = 2;
        int N = 3;
        int[] a = new int[M*N];
        int[] c = { 1406, 2396, 1198 };
        ierror = 0;
        key = false;
        int m = M;
        int n = N;
        int ntest = 10;
        int[] r = { 2500 , 2500 };
        seed = 1234569;
        Date dt = new Date();
        long start = dt.getTime();
        for(int i=0;i<1000000;i++)
        {
            rcont2 ( m, n, r, c, a);    
        }
        System.out.println(" Done "+(dt.getTime()-start));
        System.out.println(a[0]+"   "+a[1]);
        System.out.println(a[2]+"   "+a[3]);
        System.out.println(a[4]+"   "+a[5]);        
    }
}

