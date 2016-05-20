/*---------------------------------------------------------------------------*
* Simple test framework
*----------------------------------------------------------------------------*
* 16-Feb-2009 swg
* 
* An application to run tests
*---------------------------------------------------------------------------*/
package flipsky.test;

/** Framework to run tests.
 */
public class TestRunner extends TestGroup implements TestReporter {
  /** Success code for all tests passed */
  private static final int EXIT_SUCCESS = 0;
  
  /** Failure code for not all tests passed */
  private static final int EXIT_FAILURE = 1;
  
  /** Insertion marker for formatted strings */
  private static final String INSERTION_MARKER = "%s";
  
  //--- Instance variables
  private String m_indent = ""; // Current indent level
  private int    m_passed;      // Number of tests passed
  private int    m_total;       // Total number of tests
  
  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------
  
  /** Default constructor
   */
  public TestRunner() {
    super("All Tests");
    }
  
  //-------------------------------------------------------------------------
  // Test output generation
  //-------------------------------------------------------------------------
  
  /** Generate an output string
   * 
   * Output always goes to stderr so it can be picked up by the ANT build
   * process.
   */
  private void outputString(String output) {
    System.err.println(output);
    }
  
  /** Generate an output string with formatting
   * 
   * This method is used to place additional information in the output string
   * at specified locations. Because we can't use StringFormat on the J2ME
   * platform we use a simplified implementation of our own that is restricted
   * to placing insertion strings and only in order.
   */
  private void outputString(String format, String[] params) {
    StringBuffer buffer = new StringBuffer();
    // Scan though the format string looking for insertion points
    int pidx = 0, cidx = 0, nidx = 0;
    while(cidx<format.length()) {
      nidx = format.indexOf(INSERTION_MARKER, cidx);
      if(nidx>=0) {
        // Found one, append leading text
        buffer.append(format.substring(cidx, nidx));
        // Append the actual insertion string
        buffer.append((pidx<params.length)?params[pidx]:"*NULL*");
        // Adjust offsets
        pidx++;
        cidx = nidx + 2;
        }
      else {
        // No insertions, copy the rest of the string
        buffer.append(format.substring(cidx));
        cidx = format.length();
        }
      }
    // Now display it
    outputString(buffer.toString());
    }
  
  //-------------------------------------------------------------------------
  // Implementation of TestReporter
  //-------------------------------------------------------------------------
  
  /** Start running a group of tests
   */
  public void beginGroup(TestGroup group) {
    outputString("%sTEST: Beginning group '%s'", new String[] { m_indent, group.getName() });
    // Increase the indent
    m_indent = m_indent + "  ";
    }

  /** Start an individual test
   */
  public void beginTest(Test test) {
    outputString("%sTEST: Beginning test '%s'", new String[] { m_indent, test.getName() });
    // Increase the indent
    m_indent = m_indent + "  ";
    }

  /** End a group of tests
   */
  public void endGroup(TestGroup group, boolean result) {
    // Decrease the indent
    m_indent = m_indent.substring(0, m_indent.length() - 2);
    outputString("%sTEST: Group '%s' completed with %s", new String[] { m_indent, group.getName(), result?"success":"failure" });
    }

  /** End an individual test
   */
  public void endTest(Test test, boolean result) {
    // Decrease the indent
    m_indent = m_indent.substring(0, m_indent.length() - 2);
    // Notify the user
    outputString("%sTEST: Test '%s' completed with %s", new String[] { m_indent, test.getName(), result?"success":"failure" });
    // Update counters
    if(result)
      m_passed++;
    m_total++;
    }

  /** Report a single message
   */
  public void report(String message) {
    outputString("%s%s", new String[] { m_indent, message });
    }

  //-------------------------------------------------------------------------
  // Public API
  //-------------------------------------------------------------------------
  
  /** Run all tests
   * 
   * To use this you need to:
   *   1/ Create an instance of TestRunner
   *   2/ Add all your top level tests
   *   3/ Call run()
   */
  public int run() {
    outputString("---- Beginning unit tests");
    boolean result = perform(null, this);
    outputString("TEST: Tests complete - %s total, %s failure(s)", new String[] { Integer.toString(m_total), Integer.toString(m_total - m_passed) });
    outputString("---- Unit tests complete");
    return result?EXIT_SUCCESS:EXIT_FAILURE;
    }

  /** A simple 'main' implementation to test the framework.
   * 
   */
  public static int main(String[] args) {
    TestRunner runner = new TestRunner();
    return runner.run();
    }
  
  }

