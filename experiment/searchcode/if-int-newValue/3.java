package lsql;

import lsql.LUtilities;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BasicUnmodifiedTokenAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.*;

//Basic useage:
//java -jar LSql.jar -x
//>index C:\LSQLINDEX
//>info
//>SELECT field1 WHERE +field1:abc

//TODO: Make a function that creates header lines according to screen width
//^ jte: done 3-20-09
//TODO: Print a message if openReader cannot open index because of a bad path
//TODO: Improve parsing
//TODO: Improve performance.. ensure open/closed is used optimally
//TODO: Make LSql more object-oriented
//^ jte: done
//TODO: Make vertical display lign up nicer...
//TODO: GetFolderSize is not working.
//TODO: Log files

public class LSql {

    //publically available buffered reader
    //we leave it open.. is that ok?
    public BufferedReader br;

    //our terminal settings-variables
    private String index = null;
    private String file = null;
    private String command = null;
    private String export = null;
    private int top=0;
    private boolean display = false;
    private boolean show_help = false;
    private boolean interactive = false;
    private boolean horizontal = true;
    private boolean verbose = false;
	private boolean optimize = false;

    //a guess on the size of the command screen width
    //based on the default windows DOS
    public int screen_width = 77;

    //we
    LWrapper wrapper;
    static LParser parser; 

	public static void main(String args[]) {
		try {
			new LSql(args);
		} catch (Exception ex) {
			LUtilities.log("Failed with exception: " + ex);
			ex.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

	public LSql(String args[]) throws IOException, ParseException {
		if (args.length == 0) {
			printHelp();
			System.exit(1);
		}

        //open up the buffered reader
        br = new BufferedReader(new InputStreamReader(System.in));

        //create the parser... which will anayze all the commands
        parser = new LParser(this);

        //tell it to parse the command-line arguments
		parser.parseArguments(args);
	}


    /*
     * Enters the interactive mode
     */
    public void interactive() throws ParseException
    {
        //we will turn off certain LUtilities.logs in interactive mode, even if verbose is on
        //running_as_interactive = true;

        String xcommand = null;
        boolean run = true;

        LUtilities.log("Entering interactive mode.");
        if (getExport() != null)
            LUtilities.log("Register a Lucen index with: INDEX [pathtoindex]");
        
        try {
            while(run)
            {
                System.out.print("> ");
                xcommand = br.readLine();

                //take out unecessary characters
                xcommand.replaceAll("\"", "");
                xcommand.replaceAll("'", "");
                xcommand.replaceAll(";", "");

                if (xcommand.equals("exit") || xcommand.equals("quit"))
                {    run = false; break; }

                //will return true if it could not parse it, in other words
                //it is just a normal LSql command
                //if it finds a parsed command like top 4, it will return
                //false, we do not want to run the LSql parser
                boolean should_run_command = !parser.parseInteractiveCommand(xcommand);

                if (should_run_command) parser.parseCommand(xcommand);

            };
            br.close();
        } catch (IOException ioe) {
            System.out.println("IO error");
            System.exit(1);
        }
    }

    

	

	

    //handles verticle/horizontal views and exporting
    //all in one function
	public void doSelect(String[] editFields, String query) throws IOException, ParseException
    {
        LQuery lquery = new LQuery(wrapper, query);

        if (verbose) {
            LUtilities.log("Found " + lquery.getSize() + " hits using the Lucene query.");
        }

        //get all the fields if it is a *
        if (editFields[0].equals("*"))
            editFields = LUtilities.collectionToStringArray(wrapper.getFieldNames());

        //find the length of the longest field if in vertical mode
        int length = 0;
        if (!horizontal)
            //we add one because we want a buffer between the pipe sign
            //and the value
            length = LUtilities.findLongestStringLength(editFields) + 1;

        //what happens if we have too many fields to fit in the screen width?
        int mostFields = screen_width / 4;
        //we truncate the fields...
        if (editFields.length > mostFields)
        {
            editFields = LUtilities.truncateArray(editFields, mostFields - 1);
            LUtilities.log("Only " + mostFields + " fields will be displayes since there is not enough room on the terminal screen width. " +
                    "Use the -w option to expand the screen width.");
        }

        //divide screen width by how many fields we have
        //we add one because we are inserting a document column
        int horizontalColumn = (screen_width / (editFields.length + 1));

        //we deduct three because we are actually adding a PIPE and two spaces
        int horizontalColumnAdjusted = horizontalColumn-3;

        FileWriter fstream = null;
        BufferedWriter out = null;

        if (getExport() != null)
        {
            File f = new File(export);

            //ask a question if the file exists
            if (f.exists())
            {
                System.out.println();
                System.out.println("Would you like to overwrite the exising file: " + export + "?");
                System.out.print("Type yes to proceed: ");
                String answer = br.readLine();
                if (!answer.equals("yes"))
                {
                    setExport(null);
                    System.out.println("The output will not be exported.");
                }
            }

            if (getExport() != null)
            {
                fstream = new FileWriter(export);
                out = new BufferedWriter(fstream);
            }
        }

        for (int i = 0; i < lquery.getSize(); ++i) {

            //get the document
            int docId = lquery.getDocumentId(i);
            Document d = lquery.getDocument(i);

            //headers for horizontal mdoe
            if (horizontal && i==0)
            {
                System.out.println(LUtilities.strPad("", screen_width, "-"));

                System.out.print("| " + LUtilities.strPad("Document", horizontalColumnAdjusted + 1, " ").substring(0, horizontalColumnAdjusted) + " ");
                for (int x = 0; x < (editFields.length); x++)
                {
                    String nextField = editFields[x].trim();
                    System.out.print("| " + LUtilities.strPad(nextField, horizontalColumnAdjusted + 1, " ").substring(0, horizontalColumnAdjusted)  + " ");
                }

                //start a new line since we just printed the header
                System.out.println("|");

                System.out.println(LUtilities.strPad("", screen_width, "-"));
            }
            //headers for vertical mode
            if (!horizontal)
                System.out.println(LUtilities.strPad("-----[DOCUMENT " + docId + "]", screen_width, "-"));

            //headers for exporting
            if (getExport() != null && (out != null) && i==0)
            {
                out.write("Document, ");
                for (int x = 0; x < (editFields.length); x++)
                {
                    String nextField = editFields[x].trim();
                    if (getExport() != null && (out != null))
                    {
                        out.write(nextField + ", ");
                    }

                }
                out.write("\n");
            }

            //now we can actually print data
            for (int x = 0; x < (editFields.length); x++)
            {
                String nextField = editFields[x].trim();
                String value = d.get(nextField);
                if (value == null) value = "NULL";

                //horizontal mode
                if (horizontal)
                {
                    //print the document column
                    if (x == 0)
                    System.out.print("| " + LUtilities.strPad("DOC " + docId, horizontalColumnAdjusted + 1, " ").substring(0, horizontalColumnAdjusted)  + " ");

                    //print all other columns
                    System.out.print("| " + LUtilities.strPad(value, horizontalColumnAdjusted + 1, " ").substring(0, horizontalColumnAdjusted)  + " ");
                }
                //vertical mode
                else
                {
                    System.out.println(LUtilities.strPad(nextField.toUpperCase(), length, " ") + " | " + value);
                }
            
                //exporting
                if (getExport() != null && (out != null))
                {
                    //print the document column
                    if (x==0)
                        out.write("DOC " + docId + ", ");

                    //print all other columns
                    //TODO: Remove comma on last column
                    out.write(value.replace(",", "") + ", ");
                }
            }

            //we have to print a new line on horiztonal mode... since we have
            //not been printing a new line this whole time
            if (horizontal)
                System.out.println("|");

            //exporting
            if (getExport() != null && (out != null))
            {
                out.write("\n");
            }
            
            //stop if we have the -t option and we passed the limit
            if (i >= (top-1) && getTop() > 0) break;
        }

        LUtilities.log();

        //close the writer
        if (out != null)
        {
            out.close();

            if (getExport() != null)
                LUtilities.log("The output was successfully exported to: " + export);
        }

        setExport(null);
	}

    public void doInsert(String[] editFields, String query) throws IOException
    {
        Document doc = new Document();
        for (int i=0; i<editFields.length; i++)
        {
            String theField = editFields[i].split("=")[0];
            String newValue = editFields[i].split("=")[1];
            LUtilities.log(theField);
            LUtilities.log(newValue);
            Field newField = new Field(theField, newValue, Field.Store.YES, Field.Index.TOKENIZED);
            doc.add(newField);
        }
        wrapper.addDocument(doc);
        LUtilities.log("One new docoument was successfully added to the repository.");
    }

	public void doDelete(String[] editFields, String query) throws ParseException, IOException
    {
        //execute the search
        LQuery lquery = new LQuery(wrapper, query);

        //go through every document in the query
        for (int i = 0; i < lquery.getSize(); ++i) {
            //get the doc id
            int docId = lquery.getDocumentId(i);

            wrapper.addToDeleteList(docId);

            if (verbose) LUtilities.log("Document " + docId + " was successfully deleted.");

        }

        wrapper.deleteFromList();

        LUtilities.log("END DELETE");
        LUtilities.log(lquery.getSize() + " documents were updated.");

	}

	public void doAdd(String[] editFields, String query) throws IOException, ParseException {
        

        //execute the search
        LQuery lquery = new LQuery(wrapper, query);

        //go through every document in the query
        for (int i = 0; i < lquery.getSize(); ++i) {

            //get the doc id
            int docId = lquery.getDocumentId(i);

            //get the new Document
            Document doc = lquery.getDocument(i);

            List<Field> fields = doc.getFields();

            Document newDoc = new Document();

            //go through every field in the document
            for (Field f : fields) {
                newDoc.add(f);
            }

            //go through all the fields that were specified by the user:
            //ADD f1=alpha, f2=beta...
            //ADD f1:its:value
            for (int x = 0; x < editFields.length; x++) {
                //look for the field name by breaking it up
                String[] split = editFields[x].split("=");
                String theField = split[0].trim();
                String newValue = "";
                if (split.length > 1)
                    newValue = split[1].trim();

                Field newField = new Field(theField, newValue, Field.Store.YES, Field.Index.TOKENIZED);

                newDoc.add(newField);

                if (verbose) {
                    LUtilities.log("In docId: " + docId + " the field: " + theField + " has been added to the new value: " + newValue);
                }
            }


            wrapper.addToDeleteList(docId);
            wrapper.addDocument(newDoc);
        }

        wrapper.deleteFromList();

        LUtilities.log("END ADD");
        LUtilities.log(lquery.getSize() + " documents were updated.");
	}

	public void doUpdate(String[] editFields, String query) throws IOException, ParseException
    {
       //execute the search
        LQuery lquery = new LQuery(wrapper, query);

        //go through every document in the query
        for (int i = 0; i < lquery.getSize(); ++i) {

            //get the doc id
            int docId = lquery.getDocumentId(i);

            //get the new Document
            Document doc = lquery.getDocument(i);

            Document newDoc = new Document();
            List<Field> fields = doc.getFields();

            //go through every field in the document
            for (Field f : fields) {
                //go through all the fields in our:
                //UPDATE f1=alpha, f2=beta...
                for (int x = 0; x < editFields.length; x++) {
                    //look for the field name by breaking it up
                    String theField = editFields[x].split("=")[0].trim();

                    //if the two fields equal, we have a match and can update it
                    if (f.name().equals(theField)) {
                        String newValue = editFields[x].split("=")[1].trim();

                        //inserting an escape character
                        newValue.replaceAll(":", "/:");

                        f.setValue(newValue);
                        if (verbose) {
                            LUtilities.log("In docId: " + docId + " the field: " + theField + " updated successfully to the new value: " + newValue);
                        }
                    }
                }

                newDoc.add(f);
            }
 

            //remove and add the new documents
            wrapper.addToDeleteList(docId);
            wrapper.addDocument(newDoc);
        }

        wrapper.deleteFromList();

        LUtilities.log("END UPDATE");
        LUtilities.log(lquery.getSize() + " documents updated.");
	}

	public void doRemove(String[] editFields, String query) throws ParseException, IOException {

        //execute the search
        LQuery lquery = new LQuery(wrapper, query);

        //go through every document in the query
        for (int i = 0; i < lquery.getSize(); ++i) {

            //get the doc id
            int docId = lquery.getDocumentId(i);

            //get the new Document
            Document doc = lquery.getDocument(i);

            Document newDoc = new Document();
            List<Field> fields = doc.getFields();

            int fieldsRemoved = 0;

            //go through every field in the document
            for (Field f : fields)
            {
                boolean removeField = false;

                //go through all the fields in our:
                //REMOVE f1=alpha, f2=beta...
                for (int x = 0; x < editFields.length; x++) {
                    //look for the field name by breaking it up
                    String theField = editFields[x].split("=")[0].trim();

                    //if the two fields equal, we have a match and can update it
                    if (f.name().equals(theField))
                    {
                        removeField = true;
                        fieldsRemoved++;
                        if (verbose) {
                            LUtilities.log("In docId: " + docId + " the field: " + theField + " was successfully removed.");
                        }
                    }
                }

                //if we are not removing the field, add it
                if (!removeField)
                    newDoc.add(f);
            }

            

            //remove old and add new
            wrapper.addToDeleteList(docId);
            wrapper.addDocument(newDoc);
        }



        //delete the old documents
        wrapper.deleteFromList();

        LUtilities.log("END REMOVE");
        LUtilities.log(lquery.getSize() + " documents updated.");

	}

    /*
     * Our PRINT functions
     */

    public void printIndexVersion(boolean open) {
        if (open == false)
        {
            LUtilities.log("Index version: " + getLWrapper().getVersion());
        }
    }
    
    public void printLuceneVersion() {
        LUtilities.log("Using Lucene API version: " + LucenePackage.get().getSpecificationVersion() );
    }

    public void printInformation() throws IOException {
 
        Collection fieldNames = wrapper.getFieldNames();
        Iterator iterator = fieldNames.iterator();

        LUtilities.log("Number of documents in Lucene index: " + wrapper.numDocs());
        LUtilities.log("Lucene index version number: " + getLWrapper().getVersion());
        long fileSizeByte=LUtilities.getFolderSize(new File(index));
        LUtilities.log("Size of Lucene index: " + fileSizeByte + " bytes (" + LUtilities.bytesToMeg(fileSizeByte) + " MB)");
        LUtilities.log("Available fields in Lucene index: ");
        while (iterator.hasNext())
        {
            String field = iterator.next().toString();
            LUtilities.log(field, false);
        }
    }

    public void printHelp()
    {
        LUtilities.log("LSQL HELP", false);
        LUtilities.log();
        LUtilities.log("Useage: java -jar lsql.jar -i [path-to-index] -c [\"lucene-command\"]", false);
        LUtilities.log();
        LUtilities.log("Options", false);
        LUtilities.log();
        LUtilities.log("-i     Path to the Lucene index", false);
        LUtilities.log("-c     The LSql command to run", false);
        LUtilities.log("-f     Runs a list of LSql commands in the specified file", false);
        LUtilities.log("-v     Verbose output", false);
        LUtilities.log("-o     Optomize the Lucene index", false);
        LUtilities.log("-d     Display detailed information about he Lucene index", false);
        LUtilities.log("-t     Display only specified number of documents on a SELECT", false);
        LUtilities.log("-x     Interactive mode", false);
        LUtilities.log("-e     Export to file format to csv", false);
        LUtilities.log("-l     Display SELECT results as a list", false);
        LUtilities.log("-s     Change the screen width in the terminal", false);
        LUtilities.log("-help  Print the help message", false);
        LUtilities.log();
        LUtilities.log("Environment variables are supported by using $ in the lucene query.", false);
        LUtilities.log();
        LUtilities.log("LSql command syntax", false);
        LUtilities.log();
        LUtilities.log("SELECT/UPDATE/REMOVE/ADD/DELETE/INSERT f1=abc,f2=def WHERE [lucene-query]", false);
        LUtilities.log();
        LUtilities.log("SELECT     Displays the documents that match the Lucene query",false);
        LUtilities.log("UPDATE     Changes the values for specific fields in the documents",false);
        LUtilities.log("REMOVE     Removes a field from the documents",false);
        LUtilities.log("ADD        Adds a field to the documents",false);
        LUtilities.log("DELETE     Deletes the documents",false);
        LUtilities.log("INSERT     Inserts a document to the index",false);
        LUtilities.log("WHERE      The Lucene query that will fetch the documents from the index", false);
        LUtilities.log();
        LUtilities.log("Example LSql commands", false);
        LUtilities.log();
        LUtilities.log("SELECT field1 WHERE field2=abc", false);
        LUtilities.log("UPDATE field1=abc, field2=def WHERE field3=ghi", false);
        LUtilities.log("ADD field1=abc WHERE field2=abc", false);
        LUtilities.log("REMOVE field1 WHERE field1=abc", false);
        LUtilities.log("DELETE WHERE field1=abc", false);
        LUtilities.log("INSERT field1=abc,field2=def", false);
        LUtilities.log();
    }

    /*
     * Our GET/SET Functions
     */

    public LWrapper getLWrapper()
    {
        return wrapper;
    }

    public String getFile()
    {
        return file;
    }

    public String getIndex()
    {
        return index;
    }

    public String getCommand()
    {
        return command;
    }

    public String getExport()
    {
        return export;
    }

    public int getTop()
    {
        return top;
    }

    public boolean getDisplay()
    {
        return display;
    }

    public boolean getShowHelp()
    {
        return show_help;
    }

    public boolean getInteractive()
    {
        return interactive;
    }

    public boolean getHorizontal()
    {
        return horizontal;
    }

    public boolean getVerbose()
    {
        return verbose;
    }

    public boolean getOptimize()
    {
        return optimize;
    }

    public int getScreenWidth()
    {
        return screen_width;
    }

    public void setScreenWidth(int newValue)
    {
        screen_width = newValue;
    }

    public void setVerbose(boolean newValue)
    {
        verbose = newValue;
    }

    public void setOptimize(boolean newValue)
    {
        optimize = newValue;
    }

    public void setDisplay(boolean newValue)
    {
        display = newValue;
    }

    public void setShowHelp(boolean newValue)
    {
        show_help = newValue;
    }

    public void setInteractive(boolean newValue)
    {
        interactive = newValue;
    }

    public void setHorizontal(boolean newValue)
    {
        horizontal = newValue;
    }

    public void setIndex(String newIndex) throws IOException, ParseException
    {
        String newIndexTemp = LUtilities.insertEnvVariables(newIndex);
        index = newIndexTemp;

        wrapper = new LWrapper(index);
    }

    public void setCommand(String newCommand)
    {
        command = newCommand;
    }

    public void setFile(String newFile)
    {
        file = newFile;
    }

    public void setTop(int newTop)
    {
        top = newTop;
    }

    public void setExport(String newExport)
    {
        export = newExport;
    }

    
}

