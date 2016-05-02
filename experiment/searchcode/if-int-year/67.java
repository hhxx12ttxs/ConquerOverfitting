package taseanalyzer.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import taseanalyzer.model.entities.EntityManager;

/**
 *
 * All TASE files have the same header and footer.
 * The header and footer provide the meta-data of the file: its date and type (ID).
 * Parsing of the header is done to validate that the file type parsed is correct.
 * This class provides generic functions to parse the header and footer of a
 * TASE file.
 * 
 * RECORD TYPE 01: HEADER
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |FIELD|         FIELD NAME        | LENGTH | PICTURE |                    REMARKS                   |
 * | NO. |                           |        |         |                                              |
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  1  |RECORD TYPE                |    2   |9(2)     |VALUE = 01                                    |0-2
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  2  |FILLER                     |    4   |9(4)     |ZEROES                                        |2-6
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  3  |T.A.S.E. FILE ID           |    2   |9(2)     |VALUE = 20                                    |6-8
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  4  |DATE                       |    6   |9(6)     |YYMMDD                                        |8-14
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  5  |VERSION                    |    2   |9(2)     |                                              |14-16
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  6  |FILLER                     |   52   |X(52)    |ZEROES                                        |16-68
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  7  |T.A.S.E. FILE ID (4 CHRS)  |    4   |9(4)     |VALUE = 0020                                  |68-72
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 * |  8  |FILLER                     |    8   |X(8)     |ZEROES                                        |72-80
 * +-----+---------------------------+--------+---------+----------------------------------------------+
 *  
 * @author vainolo
 */
public abstract class TASEFileParser {

    private static final Logger logger = Logger.getLogger(TASEFileParser.class.getName());

    private static TASE0020Parser tase0020Parser = new TASE0020Parser();
    private static TASE0023Parser tase0023Parser = new TASE0023Parser();
    private static TASE0024Parser tase0024Parser = new TASE0024Parser();
    private static TASE0028Parser tase0028Parser = new TASE0028Parser();
    private static TASE0030Parser tase0030Parser = new TASE0030Parser();
    private static TASE0035Parser tase0035Parser = new TASE0035Parser();
    private static TASE0036Parser tase0036Parser = new TASE0036Parser();
    private static TASE0068Parser tase0068Parser = new TASE0068Parser();
    private static TASE0803Parser tase0803Parser = new TASE0803Parser();
    private static TASEFileParser tase0164Parser = new TASE0164Parser();

    private static Calendar calendar;

    protected static EntityManager em = EntityManager.INSTANCE;

    private String fileId;
    private Date date;

    protected BufferedReader reader;
    protected List createdEntities = new ArrayList();

    /**
     * Parse a date in format YYYYMMDD.
     * @param dateString date in YYYYMMDD format.
     * @return the date representation of the provided string.
     */
    public static Date parseDate4CharYear(String dateString) {
        int year = Integer.parseInt(dateString.substring(0, 4));
        int month = Integer.parseInt(dateString.substring(4, 6));
        int day = Integer.parseInt(dateString.substring(6, 8));
        return getDate(year, month, day);
    }

    /**
     * Parse a date in format YYMMDD. If YY less than 70, assumes 20YY,
     * else assumes 19YY.
     * @param dateString date in YYMMDD format.
     * @return the date representation of the provided string.
     */
    public static Date parseDate2CharYear(String dateString) {
        int year = Integer.parseInt(dateString.substring(0,2));
        if (year < 70)
            year += 2000;
        else
            year += 1900;
        int month = Integer.parseInt(dateString.substring(2,4));
        int day = Integer.parseInt(dateString.substring(4,6));
        return getDate(year, month,day);

    }

    /**
     * Get a Date instance from a year, month and day.
     * The time of day is set to 00:00:00.
     * @param year four digit year.
     * @param month current month of the year (january=1)
     * @param day day in the month.
     * @return day instance matching the desired date and time 00:00:00.
     */
    private static Date getDate(int year, int month, int day) {
        if(calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.set(year, month-1, day);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * Parse the header of the TASEFile. All TASE files have the same header
     * and it can be used to validate that the file type matches the parser.
     * @throws IOException
     */
    private void parseHeader() throws IOException {
        String line = reader.readLine();
        String recordType = line.substring(0, 2);
        if (!recordType.equals("01")) {
            logger.severe("Expected file header line. Read "+line);
            throw new IllegalStateException("Expected file header line. Read " + line);
        }
        fileId = line.substring(68, 72);
        if(!fileId.equals(getParserId())) {
            logger.severe("Expecting file id "+getParserId()+" found "+fileId+".");
            throw new IllegalStateException("Expecting file id "+getParserId()+" found "+fileId+".");
        }
        int year = Integer.parseInt(line.substring(8, 10));
        if(year > 50)
            year += 1900;
        else
            year += 2000;
        int month = Integer.parseInt(line.substring(10, 12));
        int day = Integer.parseInt(line.substring(12, 14));
        date = getDate(year, month, day);
//TODO: remove        fileId2 = line.substring(68, 72);
    }

    /**
     * Parse the file trailer. Currently does nothing.
     */
    private void parseTrailer() {}

    /**
     * The specific parser for each file, implemented in the specific parsers.
     * @throws IOException
     */
    protected abstract void parseSpecific() throws IOException;

    /**
     * Reset parser state, clearing local storage of created entities.
     */
    public void reset() {
        if(calendar != null)
            calendar.clear();
        if(createdEntities != null)
            createdEntities.clear();
    }

    /**
     * Set the reader from which the file information is read.
     * @param reader a buffer reader containing the file information. This reader
     * should be located at the beginning of the file.
     */
    public void setReader(BufferedReader reader) {
        date = null;
        this.reader = reader;
    }

    /**
     * Parse a TASEFile. This function wraps the required steps to parse a
     * file, including parsing of header and footer.
     * @throws IOException
     */
    public void parse() throws IOException {
        parseHeader();
        parseSpecific();
        parseTrailer();
        reader.close();
    }

    /**
     * Get the date of the TASEFile. This is the date written in the file's header.
     * @return the date contained in the file's header.
     */
    public Date getTASEFileDate() {
        return date;
    }
    
    /**
     * Get the file id that was parsed from the file's header.
     * @return file id from the file's header.
     */
    private String getFileId() { return fileId; }

    /**
     * Get all the entities that were created by the parser.
     * @return List of entities created by the parser. 
     */
    public List getCreatedEntities() { return createdEntities; }

    /**
     * Return an instance of a TASEFileParser parser for the given
     * file type, which is deduced from the file's name.
     * @param filename the name of the file which will be parsed by the parser.
     * @return A TASEFileParser for the given file type, deduced from the filename.
     */
    public static TASEFileParser getParser(String filename) {
        if (filename.startsWith("0164")) {
            return tase0164Parser;
        } else if(filename.startsWith("0020")) {
            return tase0020Parser;
        } else if(filename.startsWith("0023")) {
            return tase0023Parser;
        } else if(filename.startsWith("0024")) {
            return tase0024Parser;
        } else if(filename.startsWith("0803")) {
            return tase0803Parser;
        }
        // TASE0028 not used yet;
        // TASE0030 not used yet;
        // TASE0035 not used yet;
        // TASE0036 not used yet;
        // TASE0064 not used yet.
        // TASE0068 not used yet.
        // TASE0161 not used yet.
        return null;
    }

    /**
     * The function must return the four number id of the current parser. For example,
     * a TASE0020 parser should return 0020. This function is used to validate
     * the file that is being parsed.
     * @return two digit ID of the parser instance.
     */
    protected abstract String getParserId();
}


