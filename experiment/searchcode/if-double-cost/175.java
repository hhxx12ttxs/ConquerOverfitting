package compartit;
import java.util.*;
/**
 * Classe que implementa una solució del QAP, mitjançant el
 * Branch & Bound algorithm.
 * 
 * @author David Casas
 * Thanks to Nathan Brixius for his QAP explanations.
 */
public class BB extends SolucionadorQAP{
    
    //problema inicial
    private QAP init_qap;
    
    //creadores per retro-compatibilitat
    public BB(CalcularAfinitats a, CalcularDistancies d)
    {
        super(a, d);
    }
    public BB(double[][] aff, double[][] dist){
        super(aff, dist);
    }

 
     /**
     *
     * @param afinitats Matriu d'afinitats (fluxos).
     * @param distancies Matriu de distàncies.
     * @return Retorna el vector d'assignacions "v", on v[i] = j vol dir 
     * que l'objecte 'i' te assignat el lloc 'j'.
     */
    @Override
    public int[] calcularAssignacions(double[][] afinitats, double[][] distancies)
    {
        //inicialization
        init_qap = new QAP(distancies, afinitats);
        //variables for best-so-far storing
        //we start assigning the solucion v[i]=i and its cost as best-so-far
        int[] assign = new int[afinitats.length];
        for (int i= 0; i<assign.length; ++i) assign[i] = i;
        double cost = init_qap.costOf(assign); 
        //first node
        int[] v = new int[assign.length];
        for (int i = 0; i<v.length; ++i) v[i] = -1;
        //insertion of first node in queue
        PriorityQueue<Node> s = new PriorityQueue<>();
        s.add(new Node(init_qap, v));
        while(s.size() != 0){
            Node n = s.poll();
            if(n.fita < cost){
                //if subproblem is small enough (dim < 4) backtrack it
                if (n.isAlmostSolved()){
                    int[][] whatsleft = n.whatsLeft();
                    ArrayList<int[]> ss = permutations(whatsleft[1]);
                    for(int[] p : ss){
                        int[] current_asign = n.currassign.clone();
                        for(int i = 0; i<whatsleft[0].length; ++i){
                            current_asign[whatsleft[0][i]] = p[i];
                        }
                        double newcost = init_qap.costOf(current_asign);
                        if( newcost < cost){
                            assign = current_asign.clone();
                            cost = newcost;
                        }
                    }
                    
                } else  {
                    Node[] xx = n.branch();
                    for(Node sn : xx){
                        if(sn.fita < cost ) s.add(sn);
                    }
                }
            }
        }
        return assign;
    }
    
    //Retorna una llista amb les permutacions de 'a'
    private ArrayList<int[]> permutations(int[] a)
    {
        ArrayList<int[]> ret = new ArrayList<>();
        permutation(a, 0, ret);
        return ret;
    }

    private void permutation(int[] a, int pos, ArrayList<int[]> list)
    {
        if(a.length - pos == 1)
            list.add(a.clone());
        else
            for(int i = pos; i < a.length; i++){
                swap(a, pos, i);
                permutation(a, pos+1, list);
                swap(a, pos, i);
            }
    }

    private void swap(int[] arr, int i, int j)
    {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
   
    //Retorna la decisio de com seguir l'arbre:
    //(fixant un lloc o un objecte i quin)
    private Branch branching(Node n)
    {
        return coreBranching(n, n.qap.cost);
    }
    
//    protected static Branch strongBranching(Node n)
//    {
//        double[][] U = new double[n.size()-1][n.size()-1];
//        for(int i = 0; i<n.size(); ++i){
//            for(int j = 0; j<n.size(); ++j){
//                QAP q = n.qap.reduced(i, j);
//                U[i][j] = GLB.calcularFita(q.freq, q.dist) - n.fita;
//            }
//        }
//        return coreBranching(n, U);
//    }
    
   /**
    *
    * @param n Node actual de l'espai de solucions.
    * @return Branca per la que es seguirà.
    */
   private Branch coreBranching(Node n, double[][] CostMatrix)
   {
       double[] rowSum = new double[CostMatrix.length];
       double[] colSum = new double[CostMatrix.length];
       for (int i = 0; i < CostMatrix.length; i++) {
           for (int j = 0; j < CostMatrix.length; j++) {
               rowSum[i] += CostMatrix[i][j];
               colSum[j] += CostMatrix[i][j];
           }
       }
       int rowind = 0, colind = 0;
       double rowbest = rowSum[0], colbest = colSum[0];
       for (int i=1; i<CostMatrix.length; ++i){
           if(rowbest < rowSum[i]){
               rowbest = rowSum[i];
               rowind = i;
           }
           if(colbest < colSum[i]){
               colbest = colSum[i];
               colind = i;
           }
       }

       if(rowbest > colbest){
           return new Branch(true, rowind);
       }
       else{
           return new Branch(false, colind);
       } 
   }
    
    protected class Node implements Comparable<Node>{
        protected QAP qap;
        protected double fita;
        protected int[] currassign;
        
        public int size()
        {
            return qap.size();
        }
        public Node(QAP q, int[] v){
            qap = q;
            currassign = v;
            fita = GLB.calcularFita(this.qap.freq, this.qap.dist, new double[size()][size()]) + this.qap.shift;
        }
        
        @Override
        public int compareTo(Node n)
        {
            if (this.fita > n.fita) return -1;
            else if (this.fita == n.fita) return 0;
            else return 1;
        }
        public Node[] branch()
        { 
            Branch b = branching(this);
            Node[] res = new Node[qap.size()];
            if (b.isRowBranch)
                for(int i=0; i<qap.size(); ++i){
                    res[i] = new Node(qap.reduced(b.index,i), 
                            newAssign(currassign,b.index,i));
                }
            else
                for(int i=0; i<qap.size(); ++i){
                    res[i] = new Node(qap.reduced(i,b.index), 
                            newAssign(currassign,i,b.index));
                }
            return res;
        }
        public Boolean isAlmostSolved()
        {
            return (qap.size()<=3);
        }
        public int[][] whatsLeft()
        {
            int[] llocs = new int[qap.size()], objs = new int[qap.size()];
            int[] checklist = new int[currassign.length];
            int k = 0;
            for(int i = 0; i<currassign.length; ++i){
                if(currassign[i]==-1){
                    objs[k] = i;
                    k++;
                }
                else{
                    checklist[currassign[i]] = 1;
                }
            }
            k=0;
            for(int i = 0; i<currassign.length; ++i){
                if(checklist[i]!=1){
                    llocs[k] = i;
                    k++;
                }
            }
            int[][] res = {objs,llocs};
            return res;
        }
        
        private int[] newAssign(int[] v, int i, int j)
        {
            int[] res = v.clone();
            int[] checklist = new int[v.length];
            for(int k = 0; k<v.length; ++k){
                if(v[k]!=-1){
                    if(k<=i) ++i;
                    checklist[v[k]] = 1;
                }
            }
            for (int k=0; k<v.length; ++k){
                if(checklist[k] == 1 && k<=j) ++j;
            }
            res[i]=j;
            return res;
        }
    }
    
   protected class Branch{
        protected Boolean isRowBranch;
        protected int index;
        public Branch(Boolean b, int i)
        {
            isRowBranch = b;
            index = i;
        }
    }
    
}

