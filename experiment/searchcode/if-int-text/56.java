/**	$Id: WordReader.java,v 1.2 2002/11/13 22:48:00 tecris Exp $
 *
 * This class contains the main functionality for the Word file "reader". Much
 * of the code in this class is based on the Word 97 document file format. Only
 * works for non-complex files
 *
 */
package wsl.fw.msgserver.converters;

import org.apache.poi.hdf.extractor.util.*;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.io.Writer;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.hdf.extractor.TextPiece;
import org.apache.poi.hdf.extractor.Utils;

import org.apache.poi.util.LittleEndian;

public class WordReader
{
  /** byte buffer containing the main Document stream*/
  byte[] _header;

  /** document's text blocks*/
  BTreeSet _text = new BTreeSet();
  /** document's paragraphs*/
  BTreeSet _paragraphTable = new BTreeSet();

  /** starting position of text in main document stream*/
  int _fcMin;
  /** length of main document text stream*/
  int _ccpText;
  /** length of footnotes text*/

  /** OLE stuff*/
  private InputStream istream;
  /** OLE stuff*/
  private POIFSFileSystem filesystem;

  /**
   * Spits out the document text
   *
   * @param out The Writer to write the text to.
   * @throws IOException if there is a problem while reading from the file or
   *         writing out the text.
   */
  public void writeAllText(Writer out) throws IOException
  {
	int textStart = Utils.convertBytesToInt(_header, 0x18);
	int textEnd = Utils.convertBytesToInt(_header, 0x1c);
	ArrayList textPieces = findProperties(textStart, textEnd, _text.root);
	int size = textPieces.size();

	for(int x = 0; x < size; x++)
	{
	  TextPiece nextPiece = (TextPiece)textPieces.get(x);
	  int start = nextPiece.getStart();
	  int end = nextPiece.getEnd();
	  boolean unicode = nextPiece.usesUnicode();
	  int add = 1;

	  if(unicode)
	  {
		add = 2;
	  }
	  char ch;
	  for(int y = start; y < end; y += add)
	  {
		if(unicode)
		{
		  ch = (char)Utils.convertBytesToShort(_header, y);
		}
		else
		{
			if (_header[y]==13)
				ch = '\n';
			else
				ch = (char)_header[y];
		}
		out.write(ch);
	  }
	}
  }

  /**
   * Spits out the document text
   *
   */
  public String
  getAllText()
  {
	String docText;
	try
	{
		//get important stuff from the Header block and parse all the
		//data structures
		readFIB ();
		int textStart = Utils.convertBytesToInt(_header, 0x18);
		int textEnd = Utils.convertBytesToInt(_header, 0x1c);
		ArrayList textPieces = findProperties(textStart, textEnd, _text.root);
		int size = textPieces.size();
		char[] text = null;
		int charInt = 0;
		for(int x = 0; x < size; x++)
		{
		  TextPiece nextPiece = (TextPiece)textPieces.get(x);
		  int start = nextPiece.getStart();
		  int end = nextPiece.getEnd();
		  boolean unicode = nextPiece.usesUnicode();
		  int add = 1;
		  text = new char[end-start];
		  if(unicode)
		  {
			add = 2;
		  }
		  char ch;
		  int y;
		  for(y = start; y < end; y += add)
		  {
			if(unicode)
			{
			  ch = (char)Utils.convertBytesToShort(_header, y);
			}
			else
			{
				ch = (char)_header[y];
			}
			charInt = (int) ch;
			if ((charInt>= 33 && charInt <= 125))
			{
				text[y-start] = ch;
			} else if (charInt==13)
			{
				text[y-start] = '\n';
			} else
			{
				text[y-start] = ' ';
			}
		  }
		}
		docText = new String (text).trim ();
	} catch (Exception e)
	{
		docText = "Can't read MS Word document";
	}
	return docText;
  }

  /**
   * finds all chpx's that are between start and end
   */
  private ArrayList findProperties(int start, int end, BTreeSet.BTreeNode root)
  {
	ArrayList results = new ArrayList();
	BTreeSet.Entry[] entries = root.entries;

	for(int x = 0; x < entries.length; x++)
	{
	  if(entries[x] != null)
	  {
		BTreeSet.BTreeNode child = entries[x].child;
		PropertyNode xNode = (PropertyNode)entries[x].element;
		if(xNode != null)
		{
		  int xStart = xNode.getStart();
		  int xEnd = xNode.getEnd();
		  if(xStart < end)
		  {
			if(xStart >= start)
			{
			  if(child != null)
			  {
				ArrayList beforeItems = findProperties(start, end, child);
				results.addAll(beforeItems);
			  }
			  results.add(xNode);
			}
			else if(start < xEnd)
			{
			  results.add(xNode);
			  //break;
			}
		  }
		  else
		  {
			if(child != null)
			{
			  ArrayList beforeItems = findProperties(start, end, child);
			  results.addAll(beforeItems);
			}
			break;
		  }
		}
		else if(child != null)
		{
		  ArrayList afterItems = findProperties(start, end, child);
		  results.addAll(afterItems);
		}
	  }
	  else
	  {
		break;
	  }
	}
	return results;
  }

  /**
   * Constructs a Word document from an InputStream.
   * Parses the document and places
   * all the important stuff into data structures.
   *
   * @param fileName The name of the file to read.
   * @throws IOException if there is a problem while parsing the document.
   */
  public WordReader(InputStream inputStream) throws IOException
  {

		//do Ole stuff
		filesystem = new POIFSFileSystem(inputStream);

  }
  /**
   * Extracts the main document stream from the POI file then hands off to other
   * functions that parse other areas.
   *
   * @throws IOException
   */
  private void readFIB() throws IOException
  {
	  //get the main document stream
	  DocumentEntry headerProps =
		(DocumentEntry)filesystem.getRoot().getEntry("WordDocument");

	  //I call it the header but its also the main document stream
	  _header = new byte[headerProps.getSize()];
	  filesystem.createDocumentInputStream("WordDocument").read(_header);

	  //Get the information we need from the header
	  int info = LittleEndian.getShort(_header, 0xa);

	  _fcMin = LittleEndian.getInt(_header, 0x18);
	  _ccpText = LittleEndian.getInt(_header, 0x4c);

	  int charPLC = LittleEndian.getInt(_header, 0xfa);
	  int charPlcSize = LittleEndian.getInt(_header, 0xfe);
	  int parPLC = LittleEndian.getInt(_header, 0x102);
	  int parPlcSize = LittleEndian.getInt(_header, 0x106);
	  boolean useTable1 = (info & 0x200) != 0;

	  //process the text and formatting properties
	  processComplexFile(useTable1, charPLC, charPlcSize, parPLC, parPlcSize);
  }

  /**
   * Extracts the correct Table stream from the POI filesystem then hands off to
   * other functions to process text and formatting info. the name is based on
   * the fact that in Word 8(97) all text (not character or paragraph formatting)
   * is stored in complex format.
   *
   * @param useTable1 boolean that specifies if we should use table1 or table0
   * @param charTable offset in table stream of character property bin table
   * @param charPlcSize size of character property bin table
   * @param parTable offset in table stream of paragraph property bin table.
   * @param parPlcSize size of paragraph property bin table.
   * @return boolean indocating success of
   * @throws IOException
   */
  private void processComplexFile(boolean useTable1, int charTable,
									 int charPlcSize, int parTable, int parPlcSize) throws IOException
  {

	  //get the location of the piece table
	  int complexOffset = LittleEndian.getInt(_header, 0x1a2);

	  String tablename=null;
	  DocumentEntry tableEntry = null;
	  if(useTable1)
	  {
		  tablename="1Table";
	  }
	  else
	  {
		  tablename="0Table";
	  }
	  tableEntry = (DocumentEntry)filesystem.getRoot().getEntry(tablename);

	  //load the table stream into a buffer
	  int size = tableEntry.getSize();
	  byte[] tableStream = new byte[size];
	  filesystem.createDocumentInputStream(tablename).read(tableStream);

	  //parse out the text locations
	  findText(tableStream, complexOffset);
  }
  /**
   * Goes through the piece table and parses out the info regarding the text
   * blocks. For Word 97 and greater all text is stored in the "complex" way
   * because of unicode.
   *
   * @param tableStream buffer containing the main table stream.
   * @param beginning of the complex data.
   * @throws IOException
   */
  private void findText(byte[] tableStream, int complexOffset) throws IOException
  {
	//actual text
	int pos = complexOffset;
	//skips through the prms before we reach the piece table. These contain data
	//for actual fast saved files
	while(tableStream[pos] == 1)
	{
		pos++;
		int skip = LittleEndian.getShort(tableStream, pos);
		pos += 2 + skip;
	}
	if(tableStream[pos] != 2)
	{
		throw new IOException("corrupted Word file");
	}
	else
	{
		//parse out the text pieces
		int pieceTableSize = LittleEndian.getInt(tableStream, ++pos);
		pos += 4;
		int pieces = (pieceTableSize - 4) / 12;
		for (int x = 0; x < pieces; x++)
		{
			int filePos = LittleEndian.getInt(tableStream, pos + ((pieces + 1) * 4) + (x * 8) + 2);
			boolean unicode = false;
			if ((filePos & 0x40000000) == 0)
			{
				unicode = true;
			}
			else
			{
				unicode = false;
				filePos &= ~(0x40000000);//gives me FC in doc stream
				filePos /= 2;
			}
			int totLength = LittleEndian.getInt(tableStream, pos + (x + 1) * 4) -
							LittleEndian.getInt(tableStream, pos + (x * 4));

			TextPiece piece = new TextPiece(filePos, totLength, unicode);
			_text.add(piece);

		}
	}
  }
}

