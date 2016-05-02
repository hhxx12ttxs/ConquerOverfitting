package magicfive;
import java.util.*;
public class MagicFive {
    
    static Key INF = new Key(2000000);

    
    public static Key median(Key[] keys, int p, int r)
    {
        if(r >= keys.length) r = keys.length - 1;
        Key[] T = new Key[r - p + 1];
        for(int i = 0; i < T.length; i++)
        {
            T[i] = keys[i+p];
        }
        
        Arrays.sort(T, 0, T.length, new Key());
        
        for(int i = 0; i < T.length; i++) System.out.print(T[i].getNumber() + " ");
        System.out.println();
        return T[(r-p)/2];
    }
    
    public static Key select(Key[] keys, int p, int r, int k)
    {
        if(keys.length<=5)
        {
            
            Arrays.sort(keys, p, r+1, new Key());
            return keys[k];
        }
        else
        {
            int howMuch = (int)((r-p)/5)+1;
            
            Key[] medians = new Key[howMuch];
            for(int i=0;i<howMuch;i++)
            {
               medians[i] = median(keys, i*5, (i+1)*5-1);
               System.out.println(medians[i].getNumber()+" ");
            }
            
            Key med = select(medians, 0, medians.length-1, howMuch/2);
            
            
            List<Key> lower = new ArrayList<Key>();
            List<Key> equal = new ArrayList<Key>();
            List<Key> greater = new ArrayList<Key>();
            
            for(int i=0;i<keys.length-1;i++)
            {
                if(keys[i].getNumber() < med.getNumber())
                    lower.add(keys[i]);
                else if(keys[i].getNumber() == med.getNumber())
                    equal.add(keys[i]);
                else
                    greater.add(keys[i]);
            }
            
            if(k<=(lower.size()-1))
            {
                return select(lower.toArray(new Key[0]),0,lower.size()-1,k);
            }
            else if(k<=(lower.size()+equal.size()-2))
                return med;
            else
                return select(greater.toArray(new Key[0]), 0, greater.size()-1, k-lower.size()-equal.size()+2);
        }
   }
     

    public static void main(String[] args) {
        int size = 6;
        Key[] A = new Key[size];
        Random r = new Random();
        for(int i = 0; i < size; i++)
        {
            A[i] = new Key(r.nextInt(100));
            
        }
        System.out.println();
        
        for(int i = 0; i < size; i++)
        {
            System.out.print(A[i].getNumber()+" ");
        }
        System.out.println();
        
        Key temp = select(A, 0, size - 1, 0);
        System.out.println(temp.getNumber());
    }
}

