package com.david.gibbslda;

import java.util.Random;
import java.util.Vector;

class GibbsLDA {

    private int K,V,iter;
    private float alpha, beta;
    private int data [][];
    private int zAssign [][];
    private int NWZ [][]; // row word, col z
    private int NMZ [][]; // row doc, col z 
    private int NZ []; // the count of words are assigned to z
    private int NM []; // the count of words in a doc
    private int M; // doc count

    public int [][] getZAssign () {
        return this.zAssign;
    }

    public int [][] getNWZ () {
        return this.NWZ;
    }

    public int [][] getNMZ () {
        return this.NMZ;
    }

    public int [] getNZ () {
        return this.NZ;
    }

    public int [] getNM () {
        return this.NM;
    }

    public GibbsLDA (int K, int V, float alpha, float beta, int iter, int data [][]) {
        this.K = K;
        this.V = V;
        this.alpha = alpha;
        this.beta = beta;
        this.iter = iter;
        this.data = data;
        
        this.M = data.length;
        int N = 0;
        Random random = new Random ();
        zAssign = new int [M][];

        //NWZ,NMZ,NZ,NM
        NWZ = new int [V][K];
        NMZ = new int [M][K];
        NZ = new int [K];
        NM = new int [M];
        
        for (int i=0;i<V;++i) {
            for (int j=0;j<K;++j) {
                NWZ [i][j] = 0;
            }
        }

        for (int i=0;i<M;++i) {
            for (int j=0;j<K;++j) {
                NMZ [i][j] = 0;
            }
        }
        
        for (int i=0;i<K;++i) {
            NZ [i] = 0;
        }

        for (int i=0;i<M;++i) {
            NM [i] = 0;
        }

        // Create zAssign and init it
        int z = 0;
        int wordId = 0;

        for (int m=0;m<M;++m) {
            N = data [m].length;
            zAssign [m] = new int [N]; 
            
            for (int n=0;n<N;++n) {
                wordId = data [m][n];
                z = random.nextInt (this.K);
                zAssign [m][n] = z;
                
                NWZ [wordId][z] += 1;
                NMZ [m][z] += 1;
                NZ [z] += 1;
            }
                
            NM [m] += N;
        }

    }

    public void sampling () {
        int z = 0;
        int N = 0;
        int wordId = 0;
        float probZ [] = new float [K];
        double probZ_ = 0.0;
        double probSum = 0.0; 
        Random random = new Random ();
       
        //Iter 
        for (int it=0;it<iter;++it) {
            System.out.println ("Iterate:" + it);
            //All Doc
            for (int m=0;m<M;++m) {
                //A Doc
                N = data [m].length;
                for (int n=0;n<N;++n) {
                    z = zAssign [m][n];
                    wordId = data [m][n];
                    
                    // -1
                    NWZ [wordId][z] -= 1;
                    NMZ [m][z] -= 1;
                    NZ [z] -= 1;

                    // Cal prob
                    for (int k=0;k<K;++k) {
                        probZ [k] = (NWZ [wordId][k] + beta) / (NZ [k] + V*beta) * (NMZ [m][k] + alpha);
                    }                    
                    
                    for (int k=1;k<K;++k) {
                        probZ [k] += probZ [k-1];
                    }
                    
                    probSum = probZ [K-1]; 
                    probZ_ = random.nextDouble () * probSum; // (0~1.0)*probSum
                    
                    int k = 0; 
                    for (k=0;k<K-1;++k) {
                        if (probZ [k] > probZ_) {
                            break;
                        }
                    }
                    
                    z = k; 
                    zAssign [m][n] = z;
                    
                    // +1
                    NWZ [wordId][z] += 1;
                    NMZ [m][z] += 1;
                    NZ [z] += 1;
                } //A Doc
            } //All Doc
        } //Iter
    } // Sampling

    public void samplingWithModel (Model model) {

        int mNWZ [][] = model.getNWZ (); 
        int mNMZ [][] = model.getNMZ ();
        int mNZ [] = model.getNZ ();
        int mNM [] = model.getNM ();

        int z = 0;
        int N = 0;
        int wordId = 0;
        float probZ [] = new float [K];
        double probZ_ = 0.0;
        double probSum = 0.0; 
        Random random = new Random ();
       
        //Iter 
        for (int it=0;it<iter;++it) {
            System.out.println ("Iterate:" + it);
            //All Doc
            for (int m=0;m<M;++m) {
                //A Doc
                N = data [m].length;
                for (int n=0;n<N;++n) {
                    z = zAssign [m][n];
                    wordId = data [m][n];
                    
                    // -1
                    NWZ [wordId][z] -= 1;
                    NMZ [m][z] -= 1;
                    NZ [z] -= 1;

                    // Cal prob
                    for (int k=0;k<K;++k) {
                        // Here is the only different with that of estimate sampling
                        probZ [k] = (NWZ [wordId][k] + mNWZ [wordId][k] + beta) / (NZ [k] + mNZ [k] +V*beta);
                        probZ [k] *= (NMZ [m][k] + alpha);
                    }                    
                    
                    for (int k=1;k<K;++k) {
                        probZ [k] += probZ [k-1];
                    }
                    
                    probSum = probZ [K-1]; 
                    probZ_ = random.nextDouble () * probSum; // (0~1.0)*probSum
                    
                    int k = 0; 
                    for (k=0;k<K-1;++k) {
                        if (probZ [k] > probZ_) {
                            break;
                        }
                    }
                    
                    z = k; 
                    zAssign [m][n] = z;
                    
                    // +1
                    NWZ [wordId][z] += 1;
                    NMZ [m][z] += 1;
                    NZ [z] += 1;
                } //A Doc
            } //All Doc
        } //Iter
    }
}

