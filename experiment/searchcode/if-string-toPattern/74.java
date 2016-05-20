package lb2.com.br.core;

import lb2.com.br.model.DataLine;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bernardovale
 * Date: 14/06/13
 * Time: 17:15
 */public class Engine {

    private List<String> realData = new ArrayList<String>();
    private List<DataLine> dtLine = new ArrayList<DataLine>();
    private String data;

    public Engine(String data) {
        this.data = toPattern(data);
    }

    public List<DataLine> extractUseful(){
        String [] linesArray = data.split("\n");
        //First remove the garbage lines. Init on line 1 to remove the pattern line of database insert
        //like CODIGO:8:BANCO
        for(int i=2; i<linesArray.length;i++){
            //Split for blank space and add to another array just the
            //real information
            String [] lineColumns = linesArray[i].split(" ");
            for(String column : lineColumns){
                if(!column.equals("")){//Just if its not garbage
                    realData.add(column);
                }
            }
            clearTempAndPopulate();
        }
        return dtLine;
    }

    /**
     * Populate real data if, and only if,
     * the temporary array was not empty
     */
    private void clearTempAndPopulate(){
        if(realData.size()!=0 && realData.size() != 1){ //Check if this is not a blank line
            dtLine.add(new DataLine(realData));//If not add a new Dataline
        }
        realData.clear();//Clear this shit
    }

    private String toPattern(String data){
        //American pattern for numbers
        data = data.replaceAll(",",".");
        //Windows to Linux
        data = data.replaceAll("\r\n","\n");
        //TAB to Space
        data = data.replaceAll("\t"," ");
        return data;
    }
}

