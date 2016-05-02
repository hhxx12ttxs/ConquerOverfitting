package FileScript;

import Actions.*;
import Exceptions.*;
import Filters.Filter;
import Filters.Filters;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MyFileScript {
    
    private static final String BASIC_FILTERS_DELEIMITER = " ";
    private static final String BASIC_FILTER_PARTS_DELIMITER = "%";
    private static final String FILTER_NEGATION_TOKEN = "NOT";
    private static final String ACTION_PARTS_DELIMITER = "%";
    private static final String POSITIVE_PARAMETER = "YES";
    private static final String NEGATIVE_PARAMETER = "NO";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            //check for various errors:
            if (args.length < 2){
                throw new WrongUsageException();
            }
            File sourceDir = new File(args[0]);
            if (!sourceDir.isDirectory()){
                throw new IOException("Invalid source directory.");
            }
            File commandsFile = new File(args[1]);
            if (!(commandsFile.isFile() || commandsFile.canRead())){
                throw new IOException("Commands file not found or cannot be read.");
            }
            //get all the files under the directory tree:
            List<File> files = getFilesTree(sourceDir);
            List<Section> sections = prepareSections(commandsFile);
            //go over each of the sections and do the actions on the filtered files.
            for (Section section : sections){
                //first, sort the files list according to the sections's order.
                sortFiles(files,section.getOrder());
                //for each of the files, check if it should be filtered and
                //perform all the actions on it:
                for (File file : files){
                    if (isPassesFilters(section,file)){
                        doActions(section,file);
                    }
                }
            }
        }
        catch (WrongUsageException e){
            System.out.println("Usage:\tjava oop.ex1.filescript.MyFileScript sourceDir commandFile");
            System.out.println("Where:\tsourceDir\tThe path of the source directory.");
            System.out.println("\tcommandFile\tThe path to the script file which contains the commands.");
        }
        catch (Exception e){
            System.out.println("ERROR");
            System.out.println("\t" + e.toString());
        }
    }
    
    /**
     * This method recursively seeks all files (not directories) under the
     * directory tree.
     * @param dir The root directory.
     * @return List of files.
     * @throws FileNotFoundException In case 'dir' is not a directory.
     */
    private static List<File> getFilesTree(File dir) throws FileNotFoundException{
        //check that 'dir' represents a valid directory:
        if (!dir.isDirectory()){
            throw new FileNotFoundException("Cannot find specified directory.");
        }
        //list of all files and directories under 'dir':
        List<File> filesAndDirs = Arrays.asList( dir.listFiles() );
        //list of just the files (and not directories) under 'dir':
        List<File> justFiles = new LinkedList<>();
        //go over each of the sub-files or sub-directories and build the list
        //of files which will be returned by the function:
        for (File file : filesAndDirs){
            if (file.isDirectory()){
                //if it's a directory, recursively seek under it:
                justFiles.addAll( getFilesTree(file) );
            }
            else{
                //if it's a files - add it to the files list:
                justFiles.add(file);
            }
        }
        return justFiles;
    }
    
    /**
     * Parse the script file and build all the sections defined in it.
     * @param file The script file to parse.
     * @return The list of section objects defined in the script file.
     * @throws FileNotFoundException In case the script file is not found.
     * @throws IOException In case of an IO error.
     */
    private static List<Section> prepareSections(File file)
            throws FileNotFoundException, IOException, InvalidFirstLineException, BadSubsectionException{
        List<Section> sections = new LinkedList<>();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line;
        List<String> fileContents = new LinkedList();
        List<Integer> filterHeadersIndices = new LinkedList();
        List<Integer> actionHeadersIndices = new LinkedList();
        List<Integer> orderHeadersIndices = new LinkedList();
        //keep reading lines until no more lines to read:
        while ( (line = br.readLine()) != null ){
            fileContents.add(line);
            //if the current line corresponds to the filters header, it means a
            //new section is starting. so save the index of this line.
            if (line.equals(LineTypes.filter_header.toString())){
                filterHeadersIndices.add(fileContents.size() - 1);
            }
            else if (line.equals(LineTypes.action_header.toString())){
                filterHeadersIndices.add(fileContents.size() - 1);
            }
            else if (line.equals(LineTypes.order_header.toString())){
                filterHeadersIndices.add(fileContents.size() - 1);
            }
        }
        //the first line in the file must be a filters header, otherwise it's
        //an invalid file:
        if (filterHeadersIndices.isEmpty() || filterHeadersIndices.get(0) != 0)
            throw new InvalidFirstLineException("First line in commands file must be a filters header (" + LineTypes.filter_header + ").");
        //the number of filter headers must be equal to the number of action headers,
        //otherwise, there is an invalid section:
        if (filterHeadersIndices.size() != actionHeadersIndices.size())
            throw new BadSubsectionException("Commands file contains an incorrect number of actions subsections.");
        //now parse each of the sections:
        for (int i = 0 ; i < filterHeadersIndices.size() ; i++){
            //retrieve the bounds of this section in the fileContents list:
            int sectionStart = filterHeadersIndices.get(i);
            int sectionEnd;
            try{
                sectionEnd = filterHeadersIndices.get(i + 1);
            }
            catch (IndexOutOfBoundsException e){
                sectionEnd = fileContents.size();
            }
            for (int j = sectionStart + 1 ; j < sectionEnd ; j++){
                //we start from sectionStart+1 because the first line of the section
                //is obviously the filters header line.
                
            }
        }
        return sections;
    }
    
    /**
     * This method sorts the list of files according to the specified order.
     * @param files The list of files to sort.
     * @param order The order according to which to sort the list of files.
     */
    private static void sortFiles(List<File> files, Orders order){
        Comparator comparator;
        switch (order){
            default:
            case abs: //sort by path.
                comparator = new Comparator() {
                    @Override
                    public int compare(Object file1, Object file2) {
                        try {
                            String file1Path = ((File) file1).getCanonicalPath();
                            String file2Path = ((File) file2).getCanonicalPath();
                            return file1Path.compareTo(file2Path);
                        }
                        catch (IOException ex) {
                            //'getCanonicalPath()' can throw an IO exception,
                            //so we need to catch it.
                            //comparison falied due to an IO error:
                            return 0;
                        }
                    }
                };
                break;
            case file: //sort by file name
                comparator = new Comparator() {
                    @Override
                    public int compare(Object file1, Object file2) {
                        String file1Name = ((File) file1).getName();
                        String file2Name = ((File) file2).getName();
                        int result = file1Name.compareTo(file2Name);
                        //if file names are equal, compare by path:
                        if (result == 0){
                            try{
                                String file1Path = ((File) file1).getCanonicalPath();
                                String file2Path = ((File) file2).getCanonicalPath();
                                result = file1Path.compareTo(file2Path);
                            }
                            catch (IOException ex) {
                                //'getCanonicalPath()' can throw an IO exception,
                                //so we need to catch it.
                                //comparison falied due to an IO error:
                                result = 0;
                            }
                        }
                        return result;
                    }
                };
                break;
            case mod: //sort by modification date.
                comparator = new Comparator() {
                    @Override
                    public int compare(Object file1, Object file2) {
                        Calendar file1Date = new GregorianCalendar();
                        file1Date.setTimeInMillis(((File) file1).lastModified());
                        Calendar file2Date = new GregorianCalendar();
                        file1Date.setTimeInMillis(((File) file2).lastModified());
                        //discard information about hours, minutes and seconds
                        //and only compare by dates:
                        file1Date.clear(Calendar.HOUR);
                        file1Date.clear(Calendar.MINUTE);
                        file1Date.clear(Calendar.SECOND);
                        file1Date.clear(Calendar.MILLISECOND);
                        file2Date.clear(Calendar.HOUR);
                        file2Date.clear(Calendar.MINUTE);
                        file2Date.clear(Calendar.SECOND);
                        file2Date.clear(Calendar.MILLISECOND);
                        int result = file1Date.compareTo(file2Date);
                        //if file dates are equal, compare by path:
                        if (result == 0){
                            try{
                                String file1Path = ((File) file1).getCanonicalPath();
                                String file2Path = ((File) file2).getCanonicalPath();
                                result = file1Path.compareTo(file2Path);
                            }
                            catch (IOException ex) {
                                //'getCanonicalPath()' can throw an IO exception,
                                //so we need to catch it.
                                //comparison falied due to an IO error:
                                result = 0;
                            }
                        }
                        return result;
                    }
                };
                break;
            case size: //sort by file size
                comparator = new Comparator() {
                    @Override
                    public int compare(Object file1, Object file2) {
                        Long file1Size = ((File) file1).length();
                        Long file2Size = ((File) file2).length();
                        int result = file1Size.compareTo(file1Size);
                        //if file sizes are equal, compare by path:
                        if (result == 0){
                            try{
                                String file1Path = ((File) file1).getCanonicalPath();
                                String file2Path = ((File) file2).getCanonicalPath();
                                result = file1Path.compareTo(file2Path);
                            }
                            catch (IOException ex) {
                                //'getCanonicalPath()' can throw an IO exception,
                                //so we need to catch it.
                                //comparison falied due to an IO error:
                                result = 0;
                            }
                        }
                        return result;
                    }
                };
                break;
        }
        //now that we have the appropriate comparator, use it to sort the list:
        Collections.sort(files, comparator);
    }
    
    /**
     * This method identifies the complex filter which is represented by a String.
     * @param line The String which represents the complex filter.
     * @return The complex filter corresponding to the String - which is represented
     *         by a list of basic filters.
     */
    private List<Filter> parseFilterLine(String line) throws InvalidFilterException{
        List<Filter> result = new LinkedList();
        //basic filters inside the string are seperated by a delimiter:
        //(defined in FILTERS_IN_LINE_DELEIMITER).
        String[] basicFilters = line.split(BASIC_FILTERS_DELEIMITER);
        for (String basicFilter : basicFilters){
            String[] filterParts = basicFilter.split(BASIC_FILTER_PARTS_DELIMITER);
            //the basic filter should have at least two parts and no more than three.
            if (filterParts.length < 2)
                throw new InvalidFilterException("Invalid filter encountered, not enough parts. (" + basicFilter + ").");
            if (filterParts.length > 3)
                throw new InvalidFilterException("Invalid filter encountered, too many parts. (" + basicFilter + ").");
            Filters filterType;
            try{
                //try to match the filter type:
                filterType = Filters.valueOf(filterParts[0]);
            }
            catch (IllegalArgumentException e){
                //no such filter exists.
                throw new InvalidFilterException("Invalid filter type encountered (" + filterParts[0] + ").");
            }
            //determine if the filter should be created in negation mode:
            boolean negate = false;
            if (filterParts.length == 3){
                if (filterParts[2].equals(FILTER_NEGATION_TOKEN)){
                    negate = true;
                }
                else{
                    throw new InvalidFilterException("Invalid thirs filter part encountered (" + filterParts[2] + ").");
                }
            }
            //now we have the filter type and the negation mode.
            //all that is left is to create the appropriate filter object:
            switch (filterType){
                case before:
                    break;
                case after:
                    break;
                case greater_than:
                    break;
                case smaller_than:
                    break;
                case file:
                    break;
                case prefix:
                    break;
                case suffix:
                    break;
                case writable:
                    break;
                case executable:
                    break;
                case hidden:
                    break;
            }
        }
        return result;
    }
    
    /**
     * This method identifies the action which is represented by a String.
     * @param line The String which represents the action.
     * @return The action object corresponding to the String.
     */
    private Action parseActionLine(String line) throws InvalidActionException, BadParameterException{
        String[] actionParts = line.split(ACTION_PARTS_DELIMITER);
        //the action should have at most two parts.
        if (actionParts.length > 2)
            throw new InvalidActionException("Invalid action encountered, too many parts. (" + line + ").");
        Actions actionType;
        try{
            //try to match the action type:
            actionType = Actions.valueOf(actionParts[0]);
        }
        catch (IllegalArgumentException e){
            //no such action exists.
            throw new InvalidActionException("Invalid action type encountered (" + actionParts[0] + ").");
        }
        //now we have the action type.
        //all that is left is to create the appropriate action object:
        switch (actionType){
            case print_data:
                //this action should not recieve a paramter:
                if (actionParts.length != 1)
                    throw new BadParameterException("No paramteres required for '" + actionParts[0] + "' action.");
                return new PrintData();
            case print_name:
                //this action should not recieve a paramter:
                if (actionParts.length != 1)
                    throw new BadParameterException("No paramteres required for '" + actionParts[0] + "' action.");
                return new PrintName();
            case copy:
                //this action should recieve a paramter:
                if (actionParts.length != 2)
                    throw new BadParameterException("Missing paramtere for '" + actionParts[0] + "' action.");
                //define the target directory for the copy operation:
                File target = new File(actionParts[1]);
                //the target musn't be absolute path:
                if (target.isAbsolute())
                    throw new BadParameterException("Absolute target path cannot be used for this action. Please use relative path.");
                return new Copy(target);
            case exec:
                //this action should recieve a paramter:
                if (actionParts.length != 2)
                    throw new BadParameterException("Missing parameter for '" + actionParts[0] + "' action.");
                boolean exec;
                switch (actionParts[1]) {
                    case POSITIVE_PARAMETER:
                        exec = true;
                        break;
                    case NEGATIVE_PARAMETER:
                        exec = false;
                        break;
                    default:
                        throw new BadParameterException("Invalid parameter (" + actionParts[1] + ") for '" + actionParts[0] + "' action.");
                }
                return new Exec(exec);
            case write:
                //this action should recieve a paramter:
                if (actionParts.length != 2)
                    throw new BadParameterException("Missing parameter for '" + actionParts[0] + "' action.");
                boolean write;
                switch (actionParts[1]) {
                    case POSITIVE_PARAMETER:
                        write = true;
                        break;
                    case NEGATIVE_PARAMETER:
                        write = false;
                        break;
                    default:
                        throw new BadParameterException("Invalid parameter (" + actionParts[1] + ") for '" + actionParts[0] + "' action.");
                }
                return new Write(write);
            case last_mod:
                //this action should recieve a paramter:
                if (actionParts.length != 2)
                    throw new BadParameterException("Missing parameter for '" + actionParts[0] + "' action.");
                DateFormat dateFormatter;
                Date date;
                try {
                    dateFormatter = new SimpleDateFormat(DATE_PATTERN);
                    date = dateFormatter.parse(actionParts[1]);
                }
                catch (ParseException ex) {
                    throw new BadParameterException("Invalid date parameter (" + actionParts[1] + ") for '" +
                            actionParts[0] + "' action. Date format should be '" + DATE_PATTERN + "'");
                }
                return new LastMod(date);
            default:
                //we should never get here, but just to stop the IDE from nagging:
                throw new InvalidActionException("Invalid action. Unknown error.");
        }
    }
    
    /**
     * This method checks the file against all the filters of the section.
     * @param section The section which has the filters.
     * @param file The file that should be checked.
     * @return True is the file passes the filters, false otherwise.
     * @throws NotPermittedException In case access to the file is denied.
     */
    private static boolean isPassesFilters(Section section, File file) throws NotPermittedException{
        //start back from the start of filters list:
        section.resetFiltersList();
        //iterate over all the filters:
        while (section.hasNextComplexFilter()){
            try{
                //advance to the next complex filter:
                section.nextComplexFilter();
                boolean result = false; //result of the current complex filter.
                while (section.hasNextBasicFilter()){
                    try{
                        if (section.getNextBasicFilter().accept(file)){
                            result = true;
                        }
                    }
                    catch (NoMoreBasicFiltersException | NoMoreComplexFiltersException e){
                        //do nothing. since we allways check with 'hasNext', we should never get here.
                    }
                }
                //if just one of the complex filters doesn't pass the file, then
                //the file should not be accepted at all:
                if (!result) return false;
            }
            catch (NoMoreComplexFiltersException e){
                //do nothing. since we allways check with 'hasNext', we should never get here.
            }
        }
        //if we reached here, it means the file passed all the complex filters,
        //so it should be accepted:
        return true;
    }
    
    /**
     * This method executes all the actions of the specified section on the
     * specified file.
     * @param section The section which has the actions.
     * @param file The file to execute the actions on.
     */
    private static void doActions(Section section, File file)
            throws ActionFailureException, BadCopyDestinationException,
                   NotPermittedException, IOException, BadParameterException{
        //start back from the start of actions list:
        section.resetActionsList();
        //iterate over all the actions:
        while (section.hasNextAction()){
            try{
                //advance to the next action:
                section.getNextAction().doAction(file);
            }
            catch (NoMoreActionsException e){
                //do nothing. since we allways check with 'hasNext', we should never get here.
            }
        }
    }
}

