package scalalabEdit;

import java.util.logging.Level;
import java.util.logging.Logger;
import scalaExec.Interpreter.GlobalValues;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;

import javax.help.*;
import scalaExec.gui.JFontChooser;
import scalaExec.scalaLab.EditorPaneHTMLHelp;
import scalaExec.scalaLab.StreamGobbler;


public class scalalabEditor extends JPanel  {
    static boolean formattedText = true;  // toggles formatted text, prevents formatting for large documents
    static boolean executedTextStyledExist = false;   // executed parts of the document, marked differently, exist
    private  static  boolean  documentIsStyled = false;    // document has already styled
    public   static  int thresholdSizeForFormatDocument =  2000;  // vaoid formatting of documents with greater size than this
    private File  currentFile;  // the descriptor of the currently edited file
    private String currentFileName;   // the full path name
    private String currentWorkingDirectory;  // the directory of the current file
    private String editedFileNameString;   // used for editor's frame title
    private Vector recentFiles;  // keeps the full names of the recent files
    private EditorKeyMouseHandler keyHandler;
    private JFrame editorFrame;  // the editor's main frame
    private static ResourceBundle resources;
    public static boolean documentEditsPendable = false;  // used to protect the user from accidentically exiting without saving changes
    public static String commandLineArguments = "";  // command line arguments to pass when executing programs

    public  int  linecnt = 1;  // the current line count
    public  int columnCnt = 1; // the current column count
    private  String recentsFileName;  // the full name of the file that keeps the list of the recent files
    public  JTextPane  textArea;    // the text area that keeps the edited text
    private Hashtable commands; 
    private JMenuBar menubar;   // the editor's menu bar
    private JToolBar toolbar;     // the editor's toolbar
    private JPanel  status;    // the editor's status line
    
    public int locPrevExecutedText;   // keeps the location of the previously executed text in buffer
 
    private JMenu recentFilesMenu;  // created dynamically to keep the recent files list

    public  JLabel  currentLineNumberLabel = new JLabel("Line: 1");
    public  JLabel  currentColumnNumberLabel = new JLabel("Column: 1");
    
      JPopupMenu  editorPopup = new JPopupMenu();
      JMenuItem cutJMenuItem = new JMenuItem(new DefaultEditorKit.CutAction());
      JMenuItem copyJMenuItem = new JMenuItem(new editorCopyAction());
      JMenuItem pasteJMenuItem = new JMenuItem( new editorPasteAction());
      JMenuItem undoJMenuItem = new JMenuItem(new UndoAction());
      JMenuItem redoJMenuItem = new JMenuItem(new RedoAction());
      JMenuItem executeJMenuItem = new JMenuItem(new ExecuteAction());
      JMenuItem executeWholeTextJMenuItem = new JMenuItem(new executeWholeTextAction());
      JMenuItem executeWholeTextUpToCursorJMenuItem =  new JMenuItem(new executeWholeTextUpToCursorAction());
      JMenuItem executeFollowingTextFromPrevPositionJMenuItem = new JMenuItem(new executeFollowingTextUpToCursorAction());
      JMenuItem resetFormattingJMenuItem = new JMenuItem(new resetFormattingAction());
      JMenuItem textColorDocumentJMenuItem = new JMenuItem(new textColoringDocumentAction());
      JMenuItem compileExecuteScalaJMenuItem = new JMenuItem(new CompileExecuteActionScala());
      JMenuItem compileExecuteJavaJMenuItem = new JMenuItem(new CompileExecuteActionJava());
      JMenuItem compileExecuteExternalJavaJMenuItem = new JMenuItem(new CompileExecuteExternalActionJava());
      JMenuItem configEditorFontsJMenuItem = new JMenuItem(new EditorFontAdjustAction());
      JMenuItem setCommandLineArgumentsJMenuItem = new JMenuItem(new EditorCommandLineArgsAction());

      public  SimpleAttributeSet  red, green, blue;
      JTabbedPane messages;
    static final String helpsetName = "IdeHelp";
    static final String helpsetLabel = "scalaLab-IDE - Help";

    // define some styles to distinguish different types of identifiers
    static int defaultFontSize = 14;
    static int definedVarsFontSize = 16;
    static int bracesSize = 16;
    static Font     reservedWordFont = new Font("Arial", Font.BOLD, defaultFontSize);
    static Color    reservedWordColor =  Color.BLUE;
    static Font     classWordFont = new Font("TimesNewRoman", Font.BOLD, defaultFontSize);
    static Color    classWordColor =  Color.RED;
    static Font     typeWordFont = new Font("TimesNewRoman", Font.BOLD, defaultFontSize);
    static Color    typeWordColor =  Color.GRAY;;
    static Font     basicFunctionsFont = new Font("TimesNewRoman", Font.BOLD+Font.ITALIC, defaultFontSize);
    static Color   basicFunctionsColor =  Color.GREEN;
    static Font     bracesFont = new Font("Arial", Font.BOLD, bracesSize);
    static Color    bracesColor = new Color(255,40, 40);
    static Font     workspaceVarsFont = new Font("Arial", Font.BOLD, definedVarsFontSize);
    static Color    workspaceVarsColor = Color.CYAN;
    static Font     defaultFont = new Font("Arial", Font.PLAIN, defaultFontSize);
    static Color    defaultColor = Color.BLACK;
    
    
    static TreeSet<String>  ReservedWords = new TreeSet<String>() ;
    static TreeSet<String>  ClassReservedWord = new TreeSet<String>();
    static TreeSet<String> TypeWords = new TreeSet<String>();
    static TreeSet<String> basicFunctions = new TreeSet<String>();
    static TreeSet<String> braces = new TreeSet<String>();
    static public  TreeSet<String>  definedWorkspaceVariables = new TreeSet<String>();

    private class editorPasteAction extends DefaultEditorKit.PasteAction {  
       editorPasteAction() {
          super();
     }
   }
   
    private class editorCopyAction extends DefaultEditorKit.CopyAction {  
       editorCopyAction() {
          super();
       }
   }
   
    // construct the tables with the identifiers that the editor highlights
    static void initHighlightedTextMap() {
        braces.clear(); braces.add("{"); braces.add("}");
        
        ClassReservedWord.clear();
        ClassReservedWord.add("AbstractButton");   
        ClassReservedWord.add("Action");   
        
        ClassReservedWord.add("ActionListener");  ClassReservedWord.add("ActionEvent");
        ClassReservedWord.add("ArrayList");       
        ClassReservedWord.add("Component");    ClassReservedWord.add("Container");   
        ClassReservedWord.add("Dialog");   
        
        
         ClassReservedWord.add("Ellipse2D");   
        
        ClassReservedWord.add("Exception");   ClassReservedWord.add("IOException");  ClassReservedWord.add("EOFException");
        ClassReservedWord.add("Error");  ClassReservedWord.add("RuntimeException");
        
        ClassReservedWord.add("Event");
        ClassReservedWord.add("FlowLayout");   
        ClassReservedWord.add("Font");
        ClassReservedWord.add("Frame");   
        ClassReservedWord.add("GridLayout");   
        ClassReservedWord.add("GridBagLayout");   
        
        
        
        ClassReservedWord.add("Graphics2D");
        ClassReservedWord.add("JButton"); 
        ClassReservedWord.add("JComboBox");   
        ClassReservedWord.add("JComponent");  
        ClassReservedWord.add("JCheckBox");   ClassReservedWord.add("JCheckBoxMenuItem");  
        ClassReservedWord.add("JToggleButton");   
        ClassReservedWord.add("JFormattedTextField");   
        ClassReservedWord.add("JFrame");                   
        ClassReservedWord.add("JLabel");   
        ClassReservedWord.add("JMenu"); 
        ClassReservedWord.add("JMenuItem"); 
        ClassReservedWord.add("JMenuBar"); 
        
        ClassReservedWord.add("JOptionPane");   
        ClassReservedWord.add("JPanel");   ClassReservedWord.add("JPopupMenu");  
        ClassReservedWord.add("JScrollPane");   
        ClassReservedWord.add("JSlider");    
        ClassReservedWord.add("JTable");  ClassReservedWord.add("JTextArea");   
        ClassReservedWord.add("JTextComponent");
        ClassReservedWord.add("JTextField");   
        
        
        ClassReservedWord.add("JTree"); 
        ClassReservedWord.add("MenuListener");  
        ClassReservedWord.add("MouseEvent");  
        
        ClassReservedWord.add("Object");   
        ClassReservedWord.add("ParseException");
        ClassReservedWord.add("Point2D");    ClassReservedWord.add("String");  
        ClassReservedWord.add("StringBuilder");
        ClassReservedWord.add("System");   
        ClassReservedWord.add("Thread");   ClassReservedWord.add("Timer");  
        ClassReservedWord.add("Window");   
        ClassReservedWord.add("Vector");  ClassReservedWord.add("Hashtable");
        ClassReservedWord.add("InputStream");  ClassReservedWord.add("OutputStream");
        ClassReservedWord.add("FileInputStream");  ClassReservedWord.add("FileOutputStream");
        ClassReservedWord.add("DataInputStream");  ClassReservedWord.add("BufferedInputStream");
        ClassReservedWord.add("FileInputStream");  ClassReservedWord.add("DataOutputStream");
        ClassReservedWord.add("BufferedOutputStream");  ClassReservedWord.add("FileOutputStream");
        ClassReservedWord.add("ObjectInputStream");   ClassReservedWord.add("ObjectOutputStream");
        ClassReservedWord.add("Serializable");   ClassReservedWord.add("ObjectInput");
        ClassReservedWord.add("ObjectOutput"); 
        
        ClassReservedWord.add("Reader");   ClassReservedWord.add("Writer");
        ClassReservedWord.add("FileReader");   ClassReservedWord.add("FileWriter");
        ClassReservedWord.add("BufferedReader");  ClassReservedWord.add("PrintWriter");
        ClassReservedWord.add("Scanner");     ClassReservedWord.add("PrintStream");
        ClassReservedWord.add("Console"); 
        
        
        
        
        
        ReservedWords.clear();
           // Scala reserved words
        ReservedWords.add("abstract");   ReservedWords.add("do"); ReservedWords.add("finally");
        ReservedWords.add("import");  ReservedWords.add("object");  ReservedWords.add("return");
        ReservedWords.add("trait");  ReservedWords.add("trait");   ReservedWords.add("var");
     //   ReservedWords.add("_"); ReservedWords.add(":"); ReservedWords.add("=");
      //  ReservedWords.add("=>");  ReservedWords.add("<-");  ReservedWords.add("<:");
      //  ReservedWords.add("<%");  ReservedWords.add(">:");        ReservedWords.add("#");
      //  ReservedWords.add("@");
        ReservedWords.add("case");  ReservedWords.add("else");  ReservedWords.add("for");
        ReservedWords.add("lazy");  ReservedWords.add("override");   ReservedWords.add("sealed");
        ReservedWords.add("try");  ReservedWords.add("while");  ReservedWords.add("catch");
        ReservedWords.add("extends");  ReservedWords.add("forSome");  ReservedWords.add("match");
        ReservedWords.add("package");  ReservedWords.add("super");  ReservedWords.add("true"); 
        ReservedWords.add("with"); ReservedWords.add("class");  ReservedWords.add("false");
        ReservedWords.add("if");  ReservedWords.add("new");  ReservedWords.add("private"); 
        ReservedWords.add("this");  ReservedWords.add("type");  ReservedWords.add("yield");
        ReservedWords.add("def");  ReservedWords.add("final"); ReservedWords.add("implicit");
        ReservedWords.add("null");  ReservedWords.add("protected"); ReservedWords.add("throw");
        ReservedWords.add("val");  ReservedWords.add("to"); ReservedWords.add("public");
        
             // Java reserved words
        ReservedWords.add("switch");  ReservedWords.add("break");  ReservedWords.add("continue");
        ReservedWords.add("static");   ReservedWords.add("enum");   ReservedWords.add("interface");
        ReservedWords.add("throws");  
        
        
        
        
        TypeWords.clear();
        TypeWords.add("Int"); TypeWords.add("Float");   TypeWords.add("Decimal");  
        TypeWords.add("Double"); TypeWords.add("Char");  TypeWords.add("BigDecimal");
        TypeWords.add("int"); TypeWords.add("float");
        TypeWords.add("double"); TypeWords.add("char");
        TypeWords.add("Short"); TypeWords.add("short");
        TypeWords.add("Unit");  TypeWords.add("void");

        basicFunctions.clear();
        basicFunctions.add("toString");       basicFunctions.add("clone");               basicFunctions.add("equals");
        basicFunctions.add("getClass");      basicFunctions.add("hashCode");       basicFunctions.add("wait");
        basicFunctions.add("notify");           basicFunctions.add("notifyAll");
        basicFunctions.add("print");            basicFunctions.add("println");              basicFunctions.add("console");
        basicFunctions.add("format");         basicFunctions.add("inc");
        basicFunctions.add("logspace");     basicFunctions.add("linspace");             basicFunctions.add("fft");
        basicFunctions.add("plot");              basicFunctions.add("jplot");                basicFunctions.add("title"); 
        basicFunctions.add("jtitle");           basicFunctions.add("label");                basicFunctions.add("jlabel");
                
        basicFunctions.add("subplot");              basicFunctions.add("jsubplot");                  
        basicFunctions.add("subplot2D");             basicFunctions.add("jsubplot");                  
        
        basicFunctions.add("main");
        basicFunctions.add("figure");           basicFunctions.add("jfigure");              basicFunctions.add("sin");
        basicFunctions.add("cos");              basicFunctions.add("tan");                  basicFunctions.add("abs"); 
        basicFunctions.add("tanh");             basicFunctions.add("cosh");                 basicFunctions.add("sinh");
        basicFunctions.add("acos");             basicFunctions.add("asin");                 basicFunctions.add("atan"); 
        basicFunctions.add("exp");              basicFunctions.add("pow");                  basicFunctions.add("log"); 
        basicFunctions.add("log2");             basicFunctions.add("log10");                basicFunctions.add("ceil"); 
        basicFunctions.add("floor");             basicFunctions.add("round");                  basicFunctions.add("sqrt"); 
        basicFunctions.add("toDegrees");   basicFunctions.add("diag");                  basicFunctions.add("diag0");
        basicFunctions.add("eye");              basicFunctions.add("eye0");                 basicFunctions.add("Eye");
        basicFunctions.add("ones");            basicFunctions.add("ones0");               basicFunctions.add("zeros");
        basicFunctions.add("zeros0");         basicFunctions.add("fill");                      basicFunctions.add("rand0");
        basicFunctions.add("rand");            basicFunctions.add("Fill");                     basicFunctions.add("Ones");
        basicFunctions.add("Zeros");          basicFunctions.add("randt");                  basicFunctions.add("Rand");
        basicFunctions.add("vrand");          basicFunctions.add("vones");                 basicFunctions.add("vzeros");
        basicFunctions.add("vfill");              basicFunctions.add("vFill");                    basicFunctions.add("dot");
        basicFunctions.add("mean");          basicFunctions.add("sum");                     basicFunctions.add("size");
        basicFunctions.add("length");         basicFunctions.add("prod");                   basicFunctions.add("max");
        basicFunctions.add("min");              basicFunctions.add("sumR");                 basicFunctions.add("meanR");
        basicFunctions.add("prodR");         basicFunctions.add("minR");                    basicFunctions.add("maxR");
        basicFunctions.add("T");                basicFunctions.add("trans");                    basicFunctions.add("transpose");
        basicFunctions.add("resample");    basicFunctions.add("reshape");               basicFunctions.add("corr");
        basicFunctions.add("correlation");  basicFunctions.add("Var");                      basicFunctions.add("variance");
        basicFunctions.add("std");              basicFunctions.add("stddeciation");         basicFunctions.add("cov");
        basicFunctions.add("covariance");  basicFunctions.add("randomNormal");    basicFunctions.add("randomUniform");
        basicFunctions.add("CholeskyL");   basicFunctions.add("Cholesky_SPD");    basicFunctions.add("Cholesky_solve");
        basicFunctions.add("LU_L");           basicFunctions.add("L");                          basicFunctions.add("LU_U");
        basicFunctions.add("U");                basicFunctions.add("LU_det");                   basicFunctions.add("det");
        basicFunctions.add("LU_solve");     basicFunctions.add("QR_H");                  basicFunctions.add("QR_Q");
        basicFunctions.add("Q");                basicFunctions.add("QR_R");                  basicFunctions.add("R");
        basicFunctions.add("QR_solve");    basicFunctions.add("solve");                  basicFunctions.add("Singular_cond");
        basicFunctions.add("cond");             basicFunctions.add("Singular_S");       basicFunctions.add("S");
        basicFunctions.add("Singular_values");  basicFunctions.add("Singular_V");  basicFunctions.add("Singular_norm2");
        basicFunctions.add("Singular_rank");   basicFunctions.add("rank");             basicFunctions.add("trace");
        basicFunctions.add("eigV");             basicFunctions.add("V");                       basicFunctions.add("eigD");
        basicFunctions.add("D");                 basicFunctions.add("norm");                  basicFunctions.add("cfft");
        basicFunctions.add("absFFT");       basicFunctions.add("cfftInverse");          basicFunctions.add("getRealValues");
        basicFunctions.add("inv");               basicFunctions.add("svdRank");             basicFunctions.add("svd");
        basicFunctions.add("psdInv");           basicFunctions.add("toMat");                basicFunctions.add("toMatrix");
        basicFunctions.add("lu");               basicFunctions.add("LU");                       basicFunctions.add("QR");
        basicFunctions.add("lsqsol");        basicFunctions.add("subm");                    basicFunctions.add("submr");
        basicFunctions.add("submc");       basicFunctions.add("diag");                      basicFunctions.add("diag0");
        basicFunctions.add("Diag");         basicFunctions.add("norm1");                    basicFunctions.add("norm2");
        basicFunctions.add("normF");        basicFunctions.add("norm");
        basicFunctions.add("read");           basicFunctions.add("write");                      basicFunctions.add("close");
        basicFunctions.add("flush");
        
        basicFunctions.add("hasNext");   basicFunctions.add("nextDouble");
        basicFunctions.add("first");  basicFunctions.add("next"); 
        basicFunctions.add("useDelimiter");    basicFunctions.add("useLocale");
        basicFunctions.add("writeDouble");  basicFunctions.add("writeInt");    basicFunctions.add("writeUTF");
        
        
        
        
    }
    
    
// marks the word without altering permanently its style attributes
public  void  markWord(String rwText) {
    String text  = textArea.getText();
    int loc = text.indexOf(rwText);
    if (loc != -1) {
      int strLocStart = loc;
      int strLocEnd = strLocStart+rwText.length();
      textArea.setSelectionStart(strLocStart);
      textArea.setSelectionEnd(strLocEnd);
      textArea.setSelectionColor(Color.RED);
    }
}

// styles the text specified by rwText, by detecting it in the text pane component jtp, and by using the specified font and color
public  void  styleWord(String rwText, JTextPane jtp, Font font,  Color  color) {
 if (formattedText)  {   
    String text  = jtp.getText();
    int loc = text.indexOf(rwText);
    int strLocStart=-1;
    int strLen=-1;
    if (loc != -1) {    // word to color exists
      strLocStart = loc;
      strLen = rwText.length()-1;
    
    MutableAttributeSet attrs = jtp.getInputAttributes();
    
    StyleConstants.setFontFamily(attrs, font.getFamily());
    StyleConstants.setFontSize(attrs, font.getSize());
    StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
    StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
    StyleConstants.setForeground(attrs, color);
    StyleConstants.setBackground(attrs, Color.WHITE);
        
    StyledDocument doc = jtp.getStyledDocument();
    
    doc.setCharacterAttributes(strLocStart, strLen, attrs, true);
    }
 }
}

public void styleExecutedText(JTextPane jtp, int startloc, int endloc) {
     MutableAttributeSet attrs = jtp.getInputAttributes();
    
    StyleConstants.setBackground(attrs, Color.lightGray);
    StyledDocument doc = jtp.getStyledDocument();
    doc.setCharacterAttributes(startloc, endloc-startloc, attrs, true);
}

// styles the text of the range start to end,  in the text pane component jtp, and by using the specified font and color
public    void  styleWord(int start, int len, JTextPane jtp, Font font,  Color  color) {
  if (formattedText)  {
    MutableAttributeSet attrs = jtp.getInputAttributes();
    
    StyleConstants.setFontFamily(attrs, font.getFamily());
    StyleConstants.setFontSize(attrs, font.getSize());
    StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
    StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);
    StyleConstants.setForeground(attrs, color);
    StyleConstants.setBackground(attrs, Color.WHITE);
    
    StyledDocument doc = jtp.getStyledDocument();
    doc.setCharacterAttributes(start, len, attrs, true);
  }
}

// set the default style to all the text of the document
public void setDefaultStyle(JTextPane jtp) {
    if (formattedText)  {
          // prepare the dafault attributes
    MutableAttributeSet attrs = jtp.getInputAttributes();
    StyleConstants.setForeground(attrs, defaultColor);
    StyleConstants.setFontFamily(attrs, "Arial");
    StyleConstants.setBackground(attrs, Color.WHITE);
    StyledDocument doc = jtp.getStyledDocument();
    doc.setCharacterAttributes(0, doc.getLength(), attrs, true);
    }
}

// style the substring specified by the range from start to end, according to its type
public void  styleText(JTextPane  jtp, int start, int len)  {
    if (formattedText)  {
    String text = jtp.getText();
    
    int end = start+len; 
    String identifier = text.substring(start, end);           // extract the String subrange
 if (ReservedWords.contains(identifier))  {
            styleWord(start, end, jtp, reservedWordFont, reservedWordColor);
        }
    else {
            if (basicFunctions.contains(identifier))  {
        styleWord(start, end, jtp, basicFunctionsFont, basicFunctionsColor);
            }
            else  {
        if (ClassReservedWord.contains(identifier)) {
            styleWord(start, end, jtp, classWordFont, classWordColor);
        }  else {
        if (TypeWords.contains(identifier))  {
            styleWord(start, end, jtp, typeWordFont, typeWordColor);
    }
        else {
            if (braces.contains(identifier)) {
             styleWord(start, end, jtp, bracesFont, bracesColor);
            }
        else  {
                if (definedWorkspaceVariables.contains(identifier))  {
                    styleWord(start, end, jtp, workspaceVarsFont, workspaceVarsColor);
                }
                else 
            styleWord(start, end, jtp, defaultFont, defaultColor);
        }
     }
        }
            }
      }
    }
}



// style the whole document
public void  styleAllText(JTextPane  jtp)  {
       if (formattedText)  {
    setDefaultStyle(jtp);
    String text = jtp.getText();
    
    int currpos=0;
    StringTokenizer  strTok = new StringTokenizer(text, "  \t\n\r\b\f;[]():.=+-*/&%,");
    while (strTok.hasMoreTokens())  {
        String identifier = strTok.nextToken();
        int start = text.indexOf(identifier, currpos);
        int end = start+identifier.length()-1;
        currpos = end;
      if (ReservedWords.contains(identifier))  {
            styleWord(start, end, jtp, reservedWordFont, reservedWordColor);
        }
    else {
            if (basicFunctions.contains(identifier))  {
        styleWord(start, end, jtp, basicFunctionsFont, basicFunctionsColor);
            }
            else  {
        if (ClassReservedWord.contains(identifier)) {
            styleWord(start, end, jtp, classWordFont, classWordColor);
        }
            else  {

        if (TypeWords.contains(identifier))  {
            styleWord(start, end, jtp, typeWordFont, typeWordColor);
    }
        else {
            if (braces.contains(identifier)) {
             styleWord(start, end, jtp, bracesFont, bracesColor);
            }
        else  {
                if (definedWorkspaceVariables.contains(identifier))  {
                    styleWord(start, end, jtp, workspaceVarsFont, workspaceVarsColor);
                }
                else 
            styleWord(start, end, jtp, defaultFont, defaultColor);
        }
     }

            }
      
     }   // while
    }
    }
    }  // documenStyled = false
    
}


// style the whole document
public void  styleLastIdText(JTextPane  jtp)  {
       if (formattedText)  {
    //setDefaultStyle(jtp);
    String text = jtp.getText();
    int  endOfTextPos = text.length()-1;
    int caretPos = jtp.getCaretPosition();
    if (endOfTextPos == caretPos)  {  // editing at the end of document, so format last word
        int lastLinePos = endOfTextPos;
        int textLen = lastLinePos+1;
        while (lastLinePos>0) {
            char lastChar = text.charAt(lastLinePos);
            if (lastChar == '\n' || lastChar == ' ' )  break;
            lastLinePos--;
    }
    
        String lastText = text.substring(lastLinePos, textLen);   // extract last text
        
        int currpos=0;
        StringTokenizer  strTok = new StringTokenizer(lastText, "  \t\n\r\b\f;[]():.=+-*/&%,");
    while (strTok.hasMoreTokens())  {
            String identifier = strTok.nextToken();
            int start = lastLinePos+lastText.indexOf(identifier, currpos);
            int end = start+identifier.length();
            currpos = end;
      if (ReservedWords.contains(identifier))  {
            styleWord(start, end, jtp, reservedWordFont, reservedWordColor);
        }
    else {
            if (basicFunctions.contains(identifier))  {
        styleWord(start, end, jtp, basicFunctionsFont, basicFunctionsColor);
            }
            else  {
        if (ClassReservedWord.contains(identifier)) {
            styleWord(start, end, jtp, classWordFont, classWordColor);
        }
            else  {

        if (TypeWords.contains(identifier))  {
            styleWord(start, end, jtp, typeWordFont, typeWordColor);
    }
        else {
            if (braces.contains(identifier)) {
             styleWord(start, end, jtp, bracesFont, bracesColor);
            }
        else  {
                if (definedWorkspaceVariables.contains(identifier))  {
                    styleWord(start, end, jtp, workspaceVarsFont, workspaceVarsColor);
                }
                else 
            styleWord(start, end, jtp, defaultFont, defaultColor);
        }
     }
            }
            }
      }
    }
     }   // editing at the end of document
    else   // if we edit in the document, we reformat all the document
        styleAllText(jtp);
        
    }
}


       private class MouseAdapterForEditor  extends  MouseAdapter {

           public void mousePressed(MouseEvent e) {
               if (e.isPopupTrigger()){
               editorPopup.show((Component) e.getSource(), e.getX(), e.getY());
             }
           }

        public void mouseReleased(MouseEvent e) {
           if (e.isPopupTrigger()){
                 editorPopup.show((Component) e.getSource(), e.getX(), e.getY());
             }

          }
       }



/**  A frame with components for search/replace. */
class  SearchReplaceAllFrame extends JFrame
{
   public static final int DEFAULT_WIDTH = 200;
   public static final int DEFAULT_HEIGHT = 200;
   private JTextField from;
   private JTextField to;

   public SearchReplaceAllFrame()
   {

      setTitle("Search / Replace All");
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      JPanel panel = new JPanel();
         // add button, text fields and labels
      JButton replaceButton = new JButton("Search / Replace All");
      panel.add(replaceButton);
      replaceButton.addActionListener(new ReplaceAllAction());

      from = new JTextField(15);
      panel.add(from);
      panel.add(new JLabel("with"));

      to = new JTextField(15);
      panel.add(to);
      add(panel);
   }

   /**      The action listener for the replace button.   */
   private class ReplaceAllAction implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
      {
          try {
         String f = from.getText();
         String t = to.getText();
         String  currentText = textArea.getText().replace(f, t);
         textArea.setText(currentText);
      }

          catch (Exception ex) { System.out.println("Exception in Search/Replace"); ex.printStackTrace(); }
      }
   }
}

class  GoToLineFrame  extends JFrame {
   public static final int DEFAULT_WIDTH = 200;
   public static final int DEFAULT_HEIGHT = 200;
   private JTextField lineNumberItem;
   private int strLocStart = 0;   // the location of the searched string
   private int strLocEnd = 0;
   private int docLen = -1;   // current document length
   private boolean firstInstance = true;

   public GoToLineFrame()
   {
      setTitle("Go To Line ");
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      JPanel panel = new JPanel();

      // add button, text fields and labels
      JButton lineNumberButton = new JButton("Line Number");
      JButton goButton = new JButton("Go");
      panel.add(goButton);
      goButton.addActionListener(new GoToLineAction());

      JButton exitButton = new JButton("Exit");
      panel.add(lineNumberButton);
      lineNumberItem= new JTextField(15);
      panel.add(lineNumberItem);
      panel.add(exitButton);
      exitButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             dispose();
          }
      });



      add(panel);
   }


   
/**       The action listener for the Search button.   */
   private class GoToLineAction implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
      {

         String searchTxt = lineNumberItem.getText();
         int lineNo = Integer.parseInt(searchTxt);
         String editedText = textArea.getText();
         int pos = 0;
         int txtLen = editedText.length();
         int fromIndex = 1;
         int cntLines = 1;
         int linePos=0;
         while (pos < txtLen) {
             int idx = editedText.indexOf('\n', fromIndex);
             if (idx==-1)  break;
             cntLines++;
             if (cntLines == lineNo)  {   // requested line number found
                 linePos = idx;
                 break;
             }
             fromIndex = idx+1;
         }

             textArea.setCaretPosition(linePos);
             textArea.setFocusable(true);

             GlobalValues.myEdit.linecnt = lineNo;
             GlobalValues.myEdit.currentLineNumberLabel.setText("Line: "+GlobalValues.myEdit.linecnt);
             GlobalValues.myEdit.currentColumnNumberLabel.setText("Column: "+GlobalValues.myEdit.columnCnt);
             

              }
       }

}





class  SearchFrame  extends JFrame {
   public static final int DEFAULT_WIDTH = 200;
   public static final int DEFAULT_HEIGHT = 200;
   private JTextField searchItem;
   private int strLocStart = 0;   // the location of the searched string
   private int strLocEnd = 0;
   private int docLen = -1;   // current document length
   private boolean firstInstance = true;

   public SearchFrame()
   {
      setTitle("Search ");
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      JPanel panel = new JPanel();

      // add button, text fields and labels
      JButton searchButton = new JButton("Search");
      JButton exitButton = new JButton("Exit");
      panel.add(searchButton);
      searchItem= new JTextField(15);
      panel.add(searchItem);
      panel.add(exitButton);
      exitButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             dispose();
          }
      });

      searchButton.addActionListener(new SearchAction());


      add(panel);
   }

/**       The action listener for the Search button.   */
   private class SearchAction implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
      {
          try {
         String searchTxt = searchItem.getText();
         docLen = searchTxt.length();
         String editedText = textArea.getText();
         int docLen = editedText.length();
         int posText = editedText.indexOf(searchTxt, strLocStart);
         if (posText != -1)   {
             textArea.setCaretPosition(posText);
             textArea.setFocusable(true);
             strLocStart  =posText;
             strLocEnd = posText+searchTxt.length();
             //textArea.setSelectionEnd(docLen);
             textArea.setSelectionStart(strLocStart);
             textArea.setSelectionEnd(strLocEnd);
             textArea.setSelectionColor(Color.RED);
             strLocStart=strLocEnd+1;
            }

         //textArea.ingetText().replace(f, t);
         //textArea.setText(currentText);
      }

          catch (Exception ex) { System.out.println("Exception in Search/Replace"); ex.printStackTrace(); }
      }
   }
}




class  SearchReplaceFrame  extends JFrame {
   public static final int DEFAULT_WIDTH = 200;
   public static final int DEFAULT_HEIGHT = 200;
   private JTextField searchItem;
   private int strLocStart = 0;   // the location of the searched string
   private int strLocEnd = 0;
   private int docLen = -1;   // current document length
   private boolean firstInstance = true;

   public SearchReplaceFrame()
   {
      setTitle("Search ");
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      JPanel panel = new JPanel();

      // add button, text fields and labels
      JButton searchButton = new JButton("Search");
      JButton exitButton = new JButton("Exit");
      panel.add(searchButton);
      searchItem= new JTextField(15);
      panel.add(searchItem);
      panel.add(exitButton);
      exitButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             dispose();
          }
      });

      searchButton.addActionListener(new SearchReplaceAction());


      add(panel);
   }

/**       The action listener for the Search button.   */
   private class SearchReplaceAction implements ActionListener
   {
      public void actionPerformed(ActionEvent event)
      {
          try {
         String searchTxt = searchItem.getText();
         docLen = searchTxt.length();
         String editedText = textArea.getText();
         int posText = editedText.indexOf(searchTxt, strLocStart);
         if (posText != -1)   {
             textArea.setCaretPosition(posText);
             textArea.setFocusable(true);
             strLocStart  =posText;
             strLocEnd = posText+searchTxt.length();
             textArea.setSelectionStart(strLocStart);
             textArea.setSelectionEnd(strLocEnd);
             textArea.setSelectionColor(Color.RED);
             strLocStart=strLocEnd+1;
            }

         //textArea.ingetText().replace(f, t);
         //textArea.setText(currentText);
      }

          catch (Exception ex) { System.out.println("Exception in Search/Replace"); ex.printStackTrace(); }
      }
   }
}

    static {
        try {
            resources = ResourceBundle.getBundle("scalalabEdit.resources.scalalabEditor",  Locale.getDefault());
        } catch (MissingResourceException mre) {
            System.err.println("resources/scalalabEditor.properties not found");
               }
    }


     /** closes the editor windows */
    protected class AppCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
   if (documentEditsPendable)  {
            int userSelection = JOptionPane.showOptionDialog(null, "Document pendable edits exist. Proceed anyway? ",  "Exit Editor?",
                     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
            if (userSelection == JOptionPane.OK_OPTION)    {  // user approves loss of new editings
                saveRecentFiles();   // save the recent file list
                GlobalValues.myEdit = null;   // no editor instance running in scalaLab
                editorFrame.dispose();
          }
    }   // documentEditsPendable
        else   // no pendable edits
        {
             saveRecentFiles();
             GlobalValues.myEdit = null;
             editorFrame.dispose();
     }
   }
 }

    // update the recent files menu with the items taken from recentFiles
    private void updateRecentFilesMenu()
    {
        recentFilesMenu.removeAll();  // clear previous menu items
        for (int k=0; k<recentFiles.size(); k++)  {
            final String  recentFileName = (String)recentFiles.elementAt(k);
            JMenuItem  recentFileMenuItem = new JMenuItem(recentFileName);
            recentFileMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
             scalalabEdit(recentFileName);   // reload the recent file in editor
                }
            });
            recentFilesMenu.add(recentFileMenuItem);
         }
      }

    // save to disk the list of recent files
    private void saveRecentFiles() {  // the file that keeps the recent files list is kept in GlobalValues.scalaLabRecentFilesList
                                                                  // at the same directory as the scalaLab.jar executable, i.e. GlobalValues.jarFilePath
        int idx = GlobalValues.jarFilePath.lastIndexOf(File.separatorChar);
         //create streams
         try {
            FileOutputStream output = new FileOutputStream(recentsFileName);

            //create writer stream
           OutputStreamWriter  recentsWriter= new OutputStreamWriter(output);
            int  fileCnt=0;  // restrict the maximum number of recent files

           for (int k=0; k<recentFiles.size(); k++) {
                String currentRecentFile = (String)recentFiles.elementAt(k)+"\n";
                recentsWriter.write(currentRecentFile, 0, currentRecentFile.length());
                if (fileCnt++ == GlobalValues.maxNumberOfRecentFiles)  break;
            }
            recentsWriter.close();
            output.close();
    }
        catch(java.io.IOException except)
        {
            System.out.println("IO exception in saveRecentFiles");
            System.out.println(except.getMessage());
            except.printStackTrace();
        }
    }

  // load the recent files list from the disk updating also the menu
    private void loadRecentFiles() {
        int idx = GlobalValues.jarFilePath.lastIndexOf(File.separatorChar);
         // create streams
         try {

         FileInputStream input = new FileInputStream(recentsFileName);

            //create reader stream
           BufferedReader  recentsReader= new BufferedReader(new InputStreamReader(input));

          recentFiles.clear();    // clear the Vector of recent files
          String currentLine;     // refill it from disk
          while ((currentLine = recentsReader.readLine()) != null)
              if (recentFiles.indexOf(currentLine) == -1)    // file not already in list
                recentFiles.add(currentLine);

            recentsReader.close();
            input.close();
           updateRecentFilesMenu();   // update the recent files menu

         }
        catch(java.io.IOException except)
        {
            System.out.println("IO exception in readRecentsFiles. File: "+recentsFileName+"  not found");
        }
           }



    public void paintChildren(Graphics g) {
        super.paintChildren(g);
    }

          // this is the main method for editing a file with scalaLab's editor'
   public void  scalalabEdit(String fileName) {  // fileName should contain the full path to the file

       String title = editorFrame.getTitle();
       int indxfFile = title.indexOf(title);
       if (indxfFile != -1)
         title = title.substring(0, indxfFile);

       editorFrame.setTitle(title + " file: "+fileName);
       currentFileName = fileName;
   if (fileName != null) {  // load the contents of the file at the editor's textArea for editing
       int idxDir = fileName.indexOf(File.separatorChar);
       if (idxDir != -1)      // a directory specified, get the directory part of the file and set the current editor's directory'
            currentWorkingDirectory = fileName.substring(0, idxDir);
          try {
      File file = new File(fileName);
      currentFile = file;  // keep "current" file
      FileReader fr = new FileReader(file);
      BufferedReader in = new BufferedReader(fr);
      int MaxBufLen = (int) file.length();
      char [] cbuf = new char[MaxBufLen];
      in.read(cbuf, 0, MaxBufLen);
      String textIn = String.valueOf(cbuf);
      textArea.setText(textIn);
      textArea.setCaretColor(Color.BLUE);
      textArea.setFont(GlobalValues.scalalabMainFrame.scalalabConsole.getFont());
      if (recentFiles.indexOf(fileName) == -1)
         recentFiles.add(fileName);
      textArea.setCaretPosition(0);
   
      styleAllText(textArea);   // style all text
          }
    catch (Exception e) {
      System.out.println("Cannot read file "+fileName);
    }
  }
      
}


     /**
     * Create a JButton out of a resource name
     */
    private JButton createButton(String name) {
	java.net.URL url = this.getClass().getResource(name);
	ImageIcon icon = new ImageIcon(url);
	return new JButton(icon);
    }


     private JButton addButton(JToolBar toolbar, String img, String tipKey) {
	JButton button = createButton(img);
	if (tipKey != null) {
	    try {
		String tipText =
		    resources.getString("toolbar."+tipKey+".tip");
		button.setToolTipText(tipText);
	    } catch (Exception ex) {
		System.err.println("Could not find a resource for "+tipKey);
	    }
	}
	toolbar.add(button);
	return button;
    }

     public static  int getColumnAtCaret(JTextComponent component)

{

int caretPosition = component.getCaretPosition();

Element root = component.getDocument().getDefaultRootElement();

int line = root.getElementIndex( caretPosition );

int lineStart = root.getElement( line ).getStartOffset();

return caretPosition - lineStart + 1;

}

/*

** Return the current line number at the Caret position.

*/

public static int getLineAtCaret(JTextComponent component)

{

int caretPosition = component.getCaretPosition();

Element root = component.getDocument().getDefaultRootElement();

return root.getElementIndex( caretPosition ) + 1;

}
    


    public scalalabEditor(int editFileType) {
	super(true);

        initHighlightedTextMap();    // init the map of Scala reserved words
        
        // create some styled text for red, green, and blue colors
        red = new SimpleAttributeSet();
        StyleConstants.setForeground(red, Color.red);
        StyleConstants.setBold(red, true);
        green = new SimpleAttributeSet();
        StyleConstants.setForeground(green, Color.green);
        StyleConstants.setBold(green, true);
        blue = new SimpleAttributeSet();
        StyleConstants.setForeground(blue, Color.blue);
        StyleConstants.setBold(blue, true);
        
        compileExecuteJavaJMenuItem.setEnabled(true);
        
        currentWorkingDirectory = GlobalValues.DirHavingFile;
        if (currentWorkingDirectory==null)
            currentWorkingDirectory = GlobalValues.jarFilePath;

        setBorder(BorderFactory.createEtchedBorder());
	setLayout(new BorderLayout());

	// create the embedded JTextComponent
      if (textArea ==  null) {
           textArea = createEditor();  // create and initialize the TextArea editor component
	// Add this as a listener for undoable edits.
	   textArea.getDocument().addUndoableEditListener(undoHandler);

           int len = textArea.getDocument().getLength();
           if (len > thresholdSizeForFormatDocument)   // for large documents we avoid styled formatting since it is constly in terms of resources
               formattedText = false;
           
           textArea.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent event) {
            
        try {
            GlobalValues.myEdit.linecnt = getLineAtCaret((JTextComponent)event.getSource());
            GlobalValues.myEdit.columnCnt =  getColumnAtCaret((JTextComponent)event.getSource());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
            }
        });

        linecnt = getLineAtCaret((JTextComponent)textArea);
        columnCnt = getColumnAtCaret((JTextComponent)textArea);
        
    	// install the command table
	commands = new Hashtable();
	Action[] actions = getActions();
	for (int i = 0; i < actions.length; i++) {
	    Action a = actions[i];
	    commands.put(a.getValue(Action.NAME), a);
	}

	JScrollPane scroller = new JScrollPane();
	JViewport port = scroller.getViewport();
	port.add(textArea);
	try {
	    String vpFlag =resources.getString("ViewportBackingStore");
	    Boolean bs = Boolean.valueOf(vpFlag);
	    port.setBackingStoreEnabled(bs.booleanValue());
	} catch (MissingResourceException mre) {
	    // just use the viewport default
	}

	JPanel panel = new JPanel();
	panel.setLayout(new BorderLayout());
	// TODO:  handle properly the toolbar
         JPanel toolbarPanel = new JPanel();
         toolbarPanel.add(createToolbar(), BorderLayout.WEST);

         panel.add("North", toolbarPanel);

	panel.add("Center", scroller);
        status = createStatusbar();
        status.setLayout(new BorderLayout());

	/*messages=new JTabbedPane();
	JTextArea newtext = new JTextArea();
	messages.addTab("Build", newtext);
	newtext = new JTextArea();
	messages.addTab("Debug", newtext);
	newtext = new JTextArea();
	messages.addTab("String Search", newtext);

	// Find out when we are selected.  When doing so...

	messages.setSelectedIndex(0);
	messages.setDoubleBuffered(true);

        status.add(messages, BorderLayout.NORTH);
*/
        panel.add("South", status);
        add(panel);
  }


       GlobalValues.myEdit = this;
        Font consoleFont = GlobalValues.scalalabMainFrame.scalalabConsole.getFont();
        int consoleFontSize = consoleFont.getSize();
        // if a reasonable font size was defined for the Console use it for the editor also
    if (consoleFontSize >= 8  && consoleFontSize <= 22)  { 
        String  consoleFontName = consoleFont.getFontName();
        reservedWordFont = new Font(consoleFontName, Font.BOLD, consoleFontSize);
        typeWordFont = new Font(consoleFontName, Font.BOLD, consoleFontSize);
        basicFunctionsFont = new Font(consoleFontName, Font.BOLD+Font.ITALIC, consoleFontSize);
        bracesFont = new Font(consoleFontName, Font.BOLD, consoleFontSize);
        workspaceVarsFont = new Font(consoleFontName, Font.BOLD, consoleFontSize+2);
        defaultFont = new Font(consoleFontName, Font.PLAIN, consoleFontSize);
    }
        if (editFileType == GlobalValues.sigProcessingType)  {
             String expression  =  GlobalValues.basicImportsScala +"\n"+GlobalValues.scalaPlusImports+"\n\n";

          textArea.setText(expression);
          textArea.setCaretPosition(expression.length());

        }
        else if (editFileType == GlobalValues.javaClassFile) {
            String expression = "public class yourNameForClass { \n"+
                    "public static void main(String [] args) { \n \n \n } \n} \n";
            textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
          else if (editFileType == GlobalValues.javaClassFileUsingScalalab) {
                  String expression  =  GlobalValues.basicImportsJava +"\n\n";


               expression +=
             "\n \n public class yourNameForClass { \n"+
                    "public static void main(String [] args) { \n \n \n } \n} \n";
            textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
   else if (editFileType == GlobalValues.javaClassFileUsingScalalabBioJava) {
                  String expression  =  GlobalValues.basicImportsJava+"\n\n"+ EditorImports.bioJavaImportsJava+"\n\n";

               expression +=
             "\n \n public class yourNameForClass { \n"+
                    "public static void main(String [] args) { \n \n \n } \n} \n";
            textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
  else if (editFileType == GlobalValues.javaExternalClassFileUsingScalalab) {
                  String expression  =  GlobalValues.basicImportsScala +"\n"+GlobalValues.scalaPlusImports+"\n\n";

               expression +=
             "\n \n public class yourNameForClass { \n"+
                    "public static void main(String [] args) { \n \n \n } \n} \n";
            textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
          else if (editFileType == GlobalValues.scalaClassSingletonObject) {
            String expression = "object  SSO    {    // change \"SSO\" to the name of your Scala singleton object \n"+
                    "def main(args: Array[String]) { \n \n \n } \n} \n";
            textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
        else if (editFileType == GlobalValues.scalaClassSingletonObjectWithCompanionClass) {
            String expression = "class  SSO {    // change \"SSO\" to the name of your Scala class with Singleton object \n }\n"+
                    "object  SSO     {    // change \"SSO\" to the name of your Scala class with Singleton object \n"+
                    "def main(args: Array[String]) { \n \n \n } \n} \n" +
                    "\n\n // create objects with:\n"+
                    "// myScalaObj  = new SSO$()\n"+
                    "//from Java  refer to fields of object SSO as: \n"+
                    "//e.g. if var df = 200.3, is a  field value,   SSO$.MODULE$.df \n"+
                    "// or myScalaObj.df \n";
            textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
          else if (editFileType == GlobalValues.scalaClassFileUsingScalalab) {
                  String expression  =  GlobalValues.basicImportsScala +"\n"+GlobalValues.scalaPlusImports+"\n\n";

               expression +=
                       "object  SSO     {     // change \"SSO\" to the name of your Scala singleton object \n"+
                    "def main(args: Array[String]) { \n \n \n } \n} \n" +
                    "\n\n // from Java refer to the object's variables with:\n"+
                    "// SSO$.MODULE$\n"+
                    "//e.g. if var df = 200.3, is a  field value, refer as:  SSO$.MODULE$.df";
             textArea.setText(expression);
            textArea.setCaretPosition(expression.length());
        }
          else if (editFileType == GlobalValues.scalaClassFileUsingScalalabBioJava) {
                  String expression  =  GlobalValues.basicImportsScala+"\n\n"+ EditorImports.bioJavaImportsScala+"\n\n";

               expression +=
                       "object  SSO      {    // change \"SSO\" to the name of your Scala singleton object \n"+
                    "def main(args: Array[String]) { \n \n \n } \n} \n" +
                    "\n\n // from Java refer to the object's variables with:\n"+
                    "// SSO$.MODULE$\n"+
                    "//e.g. if var df = 200.3, is a  field value, refer as:  SSO$.MODULE$.df";
             textArea.setText(expression);
            textArea.setCaretPosition(expression.length());

          }


       createEditorMainFrame();  // create the main frame that holds the editor's text frame'

        int idx = GlobalValues.jarFilePath.lastIndexOf(File.separatorChar);
        if (idx != -1)
          recentsFileName =  GlobalValues.jarFilePath.substring(0, idx)+File.separatorChar+GlobalValues.scalaLabRecentFilesList;
        else
          recentsFileName = "";
        recentFiles = new Vector();

        loadRecentFiles();

       
        textArea.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                    FocusEvent fe = new FocusEvent(textArea, FocusEvent.FOCUS_GAINED);
                     textArea.dispatchEvent(fe);
            }

            public void focusLost(FocusEvent e) {
                FocusEvent fe = new FocusEvent(textArea, FocusEvent.FOCUS_GAINED);
                textArea.dispatchEvent(fe);
            }
        });

      cutJMenuItem.setText(cutJMenuItem.getText()+ " (CTRL-X)");
      editorPopup.add(cutJMenuItem);
      copyJMenuItem.setText(copyJMenuItem.getText()+ " (CTRL-C)");
      editorPopup.add(copyJMenuItem);
      pasteJMenuItem.setText(pasteJMenuItem.getText()+ " (CTRL-V)");
      editorPopup.add(pasteJMenuItem);
      editorPopup.add(undoJMenuItem);

      editorPopup.add(redoJMenuItem);
      editorPopup.add(compileExecuteScalaJMenuItem);
       editorPopup.add(compileExecuteJavaJMenuItem);
      editorPopup.add(compileExecuteExternalJavaJMenuItem);
      editorPopup.add(resetFormattingJMenuItem);
      editorPopup.add(textColorDocumentJMenuItem);
      editorPopup.add(executeWholeTextJMenuItem);
      editorPopup.add(executeFollowingTextFromPrevPositionJMenuItem);
      editorPopup.add(executeWholeTextUpToCursorJMenuItem);
      editorPopup.add(executeFollowingTextFromPrevPosition);
      
  //    editorPopup.add(compileExecuteNScalaJMenuItem);
  //    editorPopup.add(interpretNScalaJMenuItem);
      editorPopup.add(executeJMenuItem);
      editorPopup.add(configEditorFontsJMenuItem);
      editorPopup.add(setCommandLineArgumentsJMenuItem);

      textArea.add(editorPopup);

      textArea.addMouseListener(new MouseAdapterForEditor());   // handles right mouse clicks

        // construct an explicit focus event in order to display the cursor at the input console
        FocusEvent fe = new FocusEvent(textArea, FocusEvent.FOCUS_GAINED);
        textArea.dispatchEvent(fe);
        
        
   }

    // creates and initializes the editor's main frame'
    public void createEditorMainFrame() {
        try {

        editorFrame = new JFrame();

        
        editorFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (currentFileName != null)
           editedFileNameString = resources.getString("Title")+currentFileName;
        else
          editedFileNameString = resources.getString("Title");
        editorFrame.setTitle(editedFileNameString);
	editorFrame.setBackground(Color.lightGray);
	editorFrame.getContentPane().setLayout(new BorderLayout());
        editorFrame.getContentPane().add("Center", GlobalValues.myEdit);  // add the text component
        editorFrame.setJMenuBar(this.createMenubar());
	editorFrame.addWindowListener(new AppCloser());
	editorFrame.pack();
        editorFrame.setSize(GlobalValues.figFrameSizeX, GlobalValues.figFrameSizeY);
        editorFrame.setVisible(true);
        
        } catch (Throwable t) {
            System.out.println("uncaught exception: " + t);
            t.printStackTrace();
        }
    }

    /**
     * Fetch the list of actions supported by this
     * editor.  It is implemented to return the list
     * of actions supported by the embedded JTextComponent
     * augmented with the actions defined locally.
     */
    public Action[] getActions() {
	return TextAction.augmentList(textArea.getActions(), defaultActions);
    }

    // creates and initializes the text area installing also a key listenener
    protected JTextPane createEditor() {
      	StyleContext sc = new StyleContext();
	DefaultStyledDocument doc = new DefaultStyledDocument(sc);
	textArea = new JTextPane(doc);

        textArea.setDragEnabled(true);
	textArea.setFont(GlobalValues.scalalabMainFrame.scalalabConsole.getFont());
        keyHandler = new EditorKeyMouseHandler();

        textArea.addKeyListener(keyHandler);
        textArea.addMouseListener(keyHandler);

        return textArea;
    }

 
    /**
     * Find the hosting frame, for the file-chooser dialog.
     */
    protected Frame getFrame() {
	for (Container p = getParent(); p != null; p = p.getParent()) {
	    if (p instanceof Frame) {
		return (Frame) p;
	    }
	}
	return null;
    }


    protected Action getAction(String cmd) {
	return (Action) commands.get(cmd);
    }

    protected String getResourceString(String nm) {
	String str;
	try {
	    str = resources.getString(nm);
	} catch (MissingResourceException mre) {
	    str = null;
	}
	return str;
    }

    protected URL getResource(String key) {
	String name = getResourceString(key);
	if (name != null) {
	    URL url = this.getClass().getResource(name);
	    return url;
	}
	return null;
    }

    protected Container getToolbar() {
	return toolbar;
    }

    protected JMenuBar getMenubar() {
	return menubar;
    }

    /**
     * Create a status bar
     */
    protected JPanel createStatusbar() {
	// need to do something reasonable here
	status = new StatusBar();
	return status;
    }

    /**
     * Resets the undo manager.
     */
    protected void resetUndoManager() {
	undo.discardAllEdits();
	undoAction.update();
	redoAction.update();
    }

    /**
     * Create the toolbar.  By default this reads the
     * resource file for the definition of the toolbar.
     */
    private Component createToolbar() {
	toolbar = new JToolBar();
	String[] toolKeys = tokenize(getResourceString("toolbar"));
	for (int i = 0; i < toolKeys.length; i++) {
	    if (toolKeys[i].equals("-")) {
		toolbar.add(Box.createHorizontalStrut(5));
	    } else {
		toolbar.add(createTool(toolKeys[i]));
	    }
	}
	toolbar.add(Box.createHorizontalGlue());
        toolbar.addSeparator();
	toolbar.add(currentLineNumberLabel);
        toolbar.addSeparator();
	toolbar.add(currentColumnNumberLabel);

        toolbar.addSeparator();

	addButton(toolbar, "resources/open.gif", "open");
	addButton(toolbar, "resources/save.gif", "save");
	toolbar.addSeparator();
	addButton(toolbar, "resources/start.gif", "start");
	addButton(toolbar, "resources/break.gif", "stop");
	addButton(toolbar, "resources/setbreak.gif", "setbreak");
	addButton(toolbar, "resources/resume.gif", "resume");
	addButton(toolbar, "resources/goto.gif", "goto");
	addButton(toolbar, "resources/goend.gif", "goend");
	addButton(toolbar, "resources/skip.gif", "skip");
	toolbar.addSeparator();
	addButton(toolbar, "resources/down.gif", "down");
	addButton(toolbar, "resources/up.gif", "up");
	toolbar.addSeparator();
	JButton helpbutton= addButton(toolbar, "resources/help.gif", "help");
	//helpbutton.addActionListener(new CSH.DisplayHelpAfterTracking(mainHB));

	return toolbar;
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected Component createTool(String key) {
	return createToolbarButton(key);
    }

    /**
     * Create a button to go inside of the toolbar.  By default this
     * will load an image resource.  The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param key The key in the resource file to serve as the basis
     *  of lookups.
     */
    protected JButton createToolbarButton(String key) {
	URL url = getResource(key + imageSuffix);
        JButton b = new JButton(new ImageIcon(url)) {
            public float getAlignmentY() { return 0.5f; }
	};
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1,1,1,1));

	String astr = getResourceString(key + actionSuffix);
	if (astr == null) {
	    astr = key;
	}
	Action a = getAction(astr);
	if (a != null) {
	    b.setActionCommand(astr);
	    b.addActionListener(a);
	} else {
	    b.setEnabled(false);
	}

	String tip = getResourceString(key + tipSuffix);
	if (tip != null) {
	    b.setToolTipText(tip);
	}

        return b;
    }

    /**
     * Take the given string and chop it up into a series
     * of strings on whitespace boundaries.  This is useful
     * for trying to get an array of strings out of the
     * resource file.
     */
    protected String[] tokenize(String input) {
	Vector v = new Vector();
	StringTokenizer t = new StringTokenizer(input);
	String cmd[];

	while (t.hasMoreTokens())
	    v.addElement(t.nextToken());
	cmd = new String[v.size()];
	for (int i = 0; i < cmd.length; i++)
	    cmd[i] = (String) v.elementAt(i);

	return cmd;
    }

    protected JMenuBar createMenubar() {
        JMenuBar editorJMenuBar = new JMenuBar();
        editorJMenuBar.setFont(new Font("Arial", Font.BOLD, 12));
        JMenu FileMenu = new JMenu("File");
        FileMenu.setMnemonic('F');

        JMenuItem newJMenuItem = new JMenuItem(new NewAction());
        //newJMenuItem.setIcon(new ImageIcon(editImage));

        JMenuItem openJMenuItem = new JMenuItem(new OpenAction());

        openJMenuItem.setMnemonic('O');
        openJMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));

        JMenuItem saveJMenuItem = new JMenuItem(new SaveAction());
        saveJMenuItem.setMnemonic('S');
        saveJMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));

        JMenuItem saveAsMenuItem = new JMenuItem(new SaveAsAction());
        JMenuItem exitJMenuItem = new JMenuItem(new ExitAction());
        FileMenu.add(newJMenuItem);
        FileMenu.add(openJMenuItem);
        FileMenu.add(saveJMenuItem);
        FileMenu.add(saveAsMenuItem);
        FileMenu.add(exitJMenuItem);
        exitJMenuItem.setMnemonic('X');
        exitJMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));


        JMenu editMenu = new JMenu("Edit");
        JMenuItem searchJMenuItem = new JMenuItem(new SearchAction());
        searchJMenuItem.setMnemonic('F');
        searchJMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl F"));

        JMenuItem searchReplaceAllJMenuItem = new JMenuItem(new SearchReplaceAllAction());
        JMenuItem searchReplaceJMenuItem  = new JMenuItem(new SearchReplaceAction());
        searchReplaceJMenuItem.setMnemonic('H');
        searchReplaceJMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl H"));

        JMenuItem gotoLineJMenuItem = new JMenuItem(new GoToLineAction());
        gotoLineJMenuItem.setMnemonic('L');
        gotoLineJMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));


        editMenu.add(cutJMenuItem);
        editMenu.add(copyJMenuItem);
        editMenu.add(pasteJMenuItem);
        editMenu.add(undoJMenuItem);
        editMenu.add(redoJMenuItem);
        editMenu.add(searchJMenuItem);
        editMenu.add(searchReplaceAllJMenuItem);
        editMenu.add(searchReplaceJMenuItem);
        editMenu.add(gotoLineJMenuItem);

        recentFilesMenu = new JMenu("Recent Files");
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem  editorhelpMenuItem = new JMenuItem("Editor Help");
        

        e
