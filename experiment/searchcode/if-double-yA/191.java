import java.util.*;

class problem1
{
   public static void main (String [] args)
   {
      Scanner sc = new Scanner (System.in);
      int counter = 1;
      while (sc.hasNext())
      {
         int stop = sc.nextInt();
         int xArray[] = new int [stop];
         int yArray[] = new int [stop];
         for (int i = 0; i < stop; i++)
         {
            xArray[i] = (int)(sc.nextDouble()*100);
            yArray[i] = (int)(sc.nextDouble()*100);
         }
         double xAverage=average(xArray);
         double yAverage=average(yArray);
         System.out.printf("Set %d\n   Geometric center = (%.1f, %.1f)\n", counter, xAverage, yAverage);
         System.out.printf("   Dead Zone size   = %.1f\n", deadZoneSize(xArray, yArray, (int)(xAverage*10000), (int)(yAverage*10000)));
         System.out.printf("   Mega Zone yield  = %.1f\n\n", megaZoneYield(xArray, yArray, (int)(xAverage*10000), (int)(yAverage*10000)));
         counter++;
      }
   }
   private static double average(int [] a)
   {
      int result = 0;
      for (int i = 0; i < a.length; i++)
         result+=a[i];
      return ((result*1.0)/a.length)/100.0;
   }
   private static double deadZoneSize(int[] xA, int[] yA, int a, int b)
   { 
      int smallestX = Integer.MAX_VALUE;
      int smallestY = Integer.MAX_VALUE;
      int smallest = Integer.MAX_VALUE;
      for (int i = 0; i < xA.length; i++)
      {
         int x = Math.abs(xA[i]*100-a);
         int y = Math.abs(yA[i]*100-b);
         int r2 = x+y;
         if (smallest >= r2){ smallest = r2; smallestX = x; smallestY = y; }
      }
      double x = smallestX/10000.0;
      double y = smallestY/10000.0;
      return 2.0*(Math.sqrt((x*x+y*y)));
   }
   private static double megaZoneYield(int[] xA, int[] yA, int a, int b)
   {
     
      int smallestX = Integer.MAX_VALUE, largestX = -1;
      int smallestY = Integer.MAX_VALUE, largestY = -1;
      for (int i = 0; i < xA.length; i++)
      {
         if( xA[i] < smallestX ) smallestX = xA[i];
         if( xA[i] > largestX ) largestX = xA[i];
         if( yA[i] < smallestY ) smallestY = yA[i];
         if( yA[i] > largestY ) largestY = yA[i];
      }
      int dx1, dx2;
      dx1 = Math.abs(a-smallestX*100); 
      dx2 = Math.abs(a-largestX*100);
      int xdist;
      if (dx1 > dx2) xdist = dx2;
      else xdist = dx1;

      int dy1, dy2;
      dy1 = Math.abs(b-smallestY*100); 
      dy2 = Math.abs(b-largestY*100);
      int ydist;
      if (dy1 > dy2) ydist = dy2;
      else ydist = dy1;

      double radius = -1.0;
      if (xdist > ydist) radius = ydist/1000.0;
      else radius = xdist/1000.0;
      int counter = 0;
      for (int i = 0; i < xA.length; i++)
      {
         int x = Math.abs(xA[i]*100-a)/1000;
         int y = Math.abs(yA[i]*100-b)/1000;
         int r2 = x*x+y*y;
         if (r2 < radius*radius){ counter++; }
      }
      return (counter*100)/(xA.length*1.0);
   }
}

