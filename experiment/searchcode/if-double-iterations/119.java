package ir.webutils;

import java.util.*;
import java.net.*;
import java.io.*;
import java.math.*;
import java.text.*;
import ir.utilities.*;


public class PageRankSpider extends Spider {

    Graph graph = new Graph();
    
    TreeMap<String, HTMLPage> pageToURL = new TreeMap<String, HTMLPage>();
    
    HashSet<String> pageUrls = new HashSet<String>();
    
    Node[] nodes;
    
    double alpha = 0.15;
    
    double iterations = 50;
     

    public void go(String[] args) 
    {
        processArgs(args);
        doCrawl();
        makeGraph();
        calcPageRank();
        outputGraphToFile();
    }
    
    
    /**
       *    creates "pageRanks" file that contains html pge file to Page Rank mapping
       */
    public void outputGraphToFile()
    {   
        
        try {
          PrintWriter out = new PrintWriter(new FileWriter(new File(saveDir, "pageRanks")));
          String text;
          for (Map.Entry<String, HTMLPage> entry : pageToURL.entrySet())
          {
             String key = entry.getKey();
             HTMLPage value = entry.getValue();
             
             Node node = null; //= graph.getNode(value.getLink().toString());
             for (Node n : nodes)
             {
                if(n.toString().equals(value.getLink().toString()))
                {
                    node = n;
                    break;
                }
             }
             //System.out.println("output file: " + node.toString());
             NumberFormat formatter = new DecimalFormat("#0.0000000000");
             text = key + " " + formatter.format(new BigDecimal(node.getPageRank())).toString();
             out.println(text);
             
          }
          out.close();
        }
        catch (IOException e) {
          System.err.println("HTMLPage.write(): " + e);
        }
    }
    
    
    /**
       *    After doCrawl has finished this create a graph of the files in the
            TreeMap of indexed files using the HTMLPage stored in pageToURL
            as the value for the html page file name
       */
    public void makeGraph()
    {
        for (Map.Entry<String, HTMLPage> entry : pageToURL.entrySet())
        {
            String key = entry.getKey();
            HTMLPage value = entry.getValue(); 
            
            graph.addNode(value.getLink().toString()); 
            //System.out.println("adding node: "+ value.getLink().toString()); 
            List<Link> newLinks = getNewLinks(value);
            for (Link l : newLinks)
            {
                if (pageUrls.contains(l.toString()) && !(value.getLink().toString().equals(l.toString())))
                {
                    //System.out.println("adding edge: "+ value.getLink().toString() + " -> " + l.toString()); 
                    graph.addEdge(value.getLink().toString(), l.toString());
                }
            }    
        }   
    }
    
    
    /**
       *    goes through the graph of the indexed pages calculating the 
            Page Rank for each of the pages for 50 iterations
       */
    public void calcPageRank()
    {    
          nodes = graph.nodeArray();
          
          for (Node n : nodes)
          { 
                n.setE(alpha/nodes.length);
                n.setPageRank(1/nodes.length); 
          }
          
          for (int i = 0; i<50; i++)
          {
                double[] ranks = new double[nodes.length];
                int b = 0;
                for (Node n: nodes)
                {
                    ranks[b++] = computeNewR(n);
                }
                
                double c_sum = 0;
                for(int c = 0; c<ranks.length; c++)
                {
                    c_sum += ranks[c];
                } 
                double c = 1.0/c_sum;
                b = 0;
                for (Node n : nodes)
                {
                    n.setPageRank(c*ranks[b++]);
                }
          }
      
    }
    
    
    
    /**
       *    Calculates the new Page rank value for a given node
       */
    public double computeNewR(Node n){
            double rank = 0.0;
                
            List<Node> edgesIn = n.getEdgesIn();
            if (edgesIn.size() == 0)
            {
                rank = n.getE();
            }
            else
            {
                double sum = 0.0;
                for (Node in : edgesIn)
                {
                    sum += ((in.getPageRank()*1.0)/(in.getEdgesOut().size()*1.0));

                }
                rank = ((1.0-alpha)*sum) + n.getE();

            }                   
            return rank;
    }
    
    

    public void indexPage(HTMLPage page) {
    
        String fileName = "P" + MoreString.padWithZeros(count, (int) Math.floor(MoreMath.log(maxCount, 10)) + 1);
        page.write(saveDir, fileName);
        pageToURL.put(fileName+".html", page);
        pageUrls.add(page.getLink().toString());
        //System.out.println("adding page: " +page.getLink().toString() + "  file: "+ fileName);
        
    }
  
    
  
    public static void main(String args[]) 
    {
        new PageRankSpider().go(args);
    }

}

