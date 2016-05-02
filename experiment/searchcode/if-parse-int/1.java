import java.util.*;
import java.io.*;
import java.lang.*;
class Parse
{
    //private int n = 10;
    public static String[][] var;
    public int[] rand;
    private Random generator = new Random();
    private int index;
    private int[] choices = new int[4];
    public static String error = null;
    private String[] splitted;
    public static String[] flags;
    private File folder;

    public Parse(int n)
    {
        rand = new int[n];
        if(Quiz.os.equals("Linux"))
        {
            folder = new File("./flags");
        }else{
            folder = new File(Quiz.home + "\\flags");
        }

        File[] paths = folder.listFiles();
        String[] path = new String[paths.length];
        flags = new String[paths.length];
        for(int i=0;i<paths.length;++i)
        {
            path[i] = paths[i].toString();
            Parse.flags[i] = path[i];
        }
        var = new String[n][5];
        for(int i = 0; i < n; ++i)
        {
            index = generator.nextInt(paths.length);

            // making sure that the question we want to add was not added already
            while( Arrays.asList(rand).contains(index) )
            {
                index = generator.nextInt(paths.length);
            }

            if(Quiz.os.equals("Linux"))
                {
                    path[index] = path[index].replace("./flags/","");
                }
                else
                {
                    path[index] = path[index].replace(Quiz.home,"");
                    path[index] = path[index].replace("\\flags\\","");

                }
            path[index] = path[index].replace(".png","");
            rand[i] = index;
            var[i][4] = path[index];

            int correct = generator.nextInt(2000) % 4;
            splitted = var[i][4].split("_");
            for(int k = 0; k < splitted.length; ++k)
                {
                    if( var[i][correct] == null)
                    {
                        var[i][correct] = splitted[k];
                    }
                    else
                    {
                        var[i][correct] += splitted[k];
                    }
                    if(k < (splitted.length - 1) )
                    {
                        var[i][correct] += " ";
                    }
                }


            //generating the rest of the choices
            for(int j = 0; j < 4; ++j)
            {
                //if the current choice is the right one, skip
                if(j == correct)
                {
                    continue;
                }
                // else generate a random choice
                index = generator.nextInt(paths.length);
                while( Arrays.asList(choices).contains(index) )
                {
                    index = generator.nextInt(paths.length);
                }
                choices[j] = index;
                if(Quiz.os.equals("Linux"))
                {
                    path[index] = path[index].replace("./flags/","");
                }
                else
                {
                    path[index] = path[index].replace(Quiz.home,"");
                    path[index] = path[index].replace("\\flags\\","");

                }
                path[index] = path[index].replace(".png","");

                splitted = path[index].split("_");
                for(int k = 0; k < splitted.length; ++k)
                {
                    if( var[i][j] == null)
                    {
                        var[i][j] = splitted[k];
                    }
                    else
                    {
                        var[i][j] += splitted[k];
                    }
                    if(k < (splitted.length - 1) )
                    {
                        var[i][j] += " ";
                    }
                }
            }
        }
        //printV(10);
    }

    public void printV(int n)
    {
        // int n = 10;
        // Parse parse = new Parse(n);
        for( int i = 0; i < n; ++i)
        {
            for( int j = 0; j < 5; ++j)
            {
                System.out.print(var[i][j] +" | ");
            }
            System.out.println("");
            if( i < flags.length)
            {
                System.out.println(flags[i]);
            }
        }
    }

    public static void main(String[] args)
    {
        //long startTime = System.nanoTime();
        
        // long endTime = System.nanoTime();
        // long duration = endTime - startTime;
        // System.out.println(duration/1000000);
    }
}

