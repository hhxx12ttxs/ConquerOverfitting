// <a href=http://ssdl-linux.cs.technion.ac.il/wiki/index.php>SSDLPedia</a>
package il.ac.technion.cs.ssdl.utils;

import static il.ac.technion.cs.ssdl.strings.StringUtils.sprintf;
import static il.ac.technion.cs.ssdl.utils.Box.box;
import static org.junit.Assert.assertEquals;
import il.ac.technion.cs.ssdl.stereotypes.Utility;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Invariant;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Reachability;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Value.NonNull;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Value.Numerical.Negative;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Value.Numerical.NonNan;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Value.Numerical.NonNegative;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Value.Numerical.NonPositive;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Value.Numerical.Positive;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Variant.Initial;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Variant.Nondecreasing;
import il.ac.technion.cs.ssdl.utils.____.Bug.Assertion.Variant.Underflow;
import il.ac.technion.cs.ssdl.utils.____.Bug.Contract.Postcondition;
import il.ac.technion.cs.ssdl.utils.____.Bug.Contract.Precondition;

import java.util.Formatter;

import org.junit.Test;

/**
 * A simple implementation of design by contract services. Violations are
 * reported to <code>System.err</code>. Error descriptions are passed by a
 * <code>printf</code> like argument syntax. Services are often used with
 * <code><b>static import</b></code>.
 *
 * @author Yossi Gil (
 * @since 11/01/2006)
 */
@Utility public abstract class ____ {
  /**
   * The base of all exception classes thrown as a result of violations of
   * contracts, assertions, and the such. This class derives from
   * {@link RuntimeException} since errors of this sort are programming-, not
   * runtime- errors. Programming errors cannot be corrected at runtime, and
   * hence all errors of this class and its descendants should not be caught by
   * ordinary applications.
   *
   * @author Yossi Gil, the Technion.
   * @since 04/08/2008
   */
  public abstract static class Bug extends RuntimeException {
    /**
     * This is the root of all assertion related exceptions, including contract
     * violations.
     *
     * @author Yossi Gil, the Technion.
     * @since 04/08/2008
     */
    public abstract static class Assertion extends Bug {
      /**
       * Thrown in case a class invariant was violated.
       *
       * @author Yossi Gil, the Technion.
       * @since 04/08/2008
       */
      public static final class Invariant extends Assertion {
        private static final long serialVersionUID = 3613179176640712216L;

        /**
         * instantiate this class with a given textual description
         *
         * @param message
         *          a description of the exceptional situation
         */
        public Invariant(final String message) {
          super(message);
        }
      }

      /**
       * Thrown in case execution reached code that should never be executed
       *
       * @author Yossi Gil, the Technion.
       * @since 04/08/2008
       */
      public static final class Reachability extends Assertion {
        private static final long serialVersionUID = -1522565621962121759L;

        /**
         * instantiate this class with a given textual description
         *
         * @param message
         *          a description of the exceptional situation
         */
        public Reachability(final String message) {
          super(message);
        }
      }

      /**
       * Abstract base class of all exceptions thrown in case a value violated a
       * condition placed on it.
       *
       * @author Yossi Gil, the Technion.
       * @since 04/08/2008
       */
      public abstract static class Value extends Assertion {
        /**
         * Thrown in case a value was <code><b>null</b></code>, when it was
         * expected to be non-code><b>null</b></code>.
         *
         * @author Yossi Gil
         * @since 18/01/2008
         */
        public static final class NonNull extends Value {
          private static final long serialVersionUID = -6739260930609363921L;
          /**
           * instantiate this class with no textual description
           */
          public NonNull() {
            super();
          }
          /**
           * instantiate this class with a given textual description
           *
           * @param message
           *          a description of the exceptional situation
           */
          public NonNull(final String message) {
            super(message);
          }

          public NonNull(final String format, final Object... args) {
            super(format, args);
          }
        }
        /**
         * Abstract base class of exceptions thrown when a numerical value did
         * not satisfy conditions assumed on it.
         *
         * @author Yossi Gil, the Technion.
         * @since 04/08/2008
         */
        public abstract static class Numerical extends Value {
          /**
           * Thrown when a numerical value assumed to be negative, was not.
           *
           * @author Yossi Gil
           * @since 23/01/2008
           */
          public static final class Negative extends Numerical {
            static final String expected = "negative";

            private static final long serialVersionUID = 4550076451966877958L;
            public Negative(final double d) {
              this(d, "");
            }
            public Negative(final double d, final String message) {
              super(expected, d, message);
            }
            public Negative(final double d, final String format, final Object... args) {
              this(d, nprintf(format, args));
            }
            public Negative(final int n) {
              this(n, "");
            }
            public Negative(final int n, final String message) {
              super(expected, n, message);
            }

            public Negative(final int n, final String format, final Object... args) {
              this(n, nprintf(format, args));
            }
          }
          /**
           * Thrown when a numerical value assumed to be non-NaN, but it was not
           *
           * @author Yossi Gil
           * @since 23/01/2008
           */
          public static final class NonNan extends Numerical {
            static final String expected = "NonNan";

            private static final long serialVersionUID = -5312054045684211451L;
            public NonNan(final double d) {
              this(d, "");
            }
            public NonNan(final double d, final String message) {
              super(expected, d, message);
            }
            public NonNan(final double d, final String format, final Object... args) {
              this(d, nprintf(format, args));
            }
            public NonNan(final int n) {
              this(n, "");
            }
            public NonNan(final int n, final String message) {
              super(expected, n, message);
            }

            public NonNan(final int n, final String format, final Object... args) {
              this(n, nprintf(format, args));
            }
          }

          /**
           * Thrown when a numerical value assumed to be non-negative, was not.
           *
           * @author Yossi Gil
           * @since 23/01/2008
           */
          public static final class NonNegative extends Numerical {
            static final String expected = "nonnegative";

            private static final long serialVersionUID = 1L;
            public NonNegative(final double d) {
              this(d, "");
            }
            public NonNegative(final double d, final String message) {
              super("expected", d, message);
            }
            public NonNegative(final double d, final String format, final Object... args) {
              this(d, nprintf(format, args));
            }
            public NonNegative(final int n) {
              this(n, "");
            }
            public NonNegative(final int n, final String message) {
              super("expected", n, message);
            }

            public NonNegative(final int n, final String format, final Object... args) {
              this(n, nprintf(format, args));
            }
          }

          /**
           * @author Yossi Gil
           * @since 23/01/2008
           */
          public static final class NonPositive extends Numerical {
            static final String expected = "nonpositive";

            private static final long serialVersionUID = -8815781684971495019L;
            public NonPositive(final double d) {
              this(d, "");
            }
            public NonPositive(final double d, final String message) {
              super("nonpositive", d, message);
            }
            public NonPositive(final double d, final String format, final Object... args) {
              this(d, nprintf(format, args));
            }
            public NonPositive(final int n) {
              this(n, "");
            }
            public NonPositive(final int n, final String message) {
              super("nonpositive", n, message);
            }

            public NonPositive(final int n, final String format, final Object... args) {
              this(n, nprintf(format, args));
            }
          }

          /**
           * Thrown when a numerical value assumed to be positive, was not.
           *
           * @author Yossi Gil
           * @since 23/01/2008
           */
          public static final class Positive extends Numerical {
            static final String expected = "positive";

            private static final long serialVersionUID = -5312054045684211451L;
            public Positive(final double d) {
              this(d, "");
            }
            public Positive(final double d, final String message) {
              super(expected, d, message);
            }
            public Positive(final double d, final String format, final Object... args) {
              this(d, nprintf(format, args));
            }
            public Positive(final int n) {
              this(n, "");
            }
            public Positive(final int n, final String message) {
              super(expected, n, message);
            }

            public Positive(final int n, final String format, final Object... args) {
              this(n, nprintf(format, args));
            }
          }

          private static final long serialVersionUID = 6004150110997223579L;

          public Numerical(final String expected, final double d, final String message) {
            super(nprintf("Found %g while expecting a %s number.", d, expected) + message);
          }

          Numerical(final String expected, final int n, final String message) {
            super(nprintf("Found %g while expecting a %s integer.", n, expected) + message);
          }
        }
        private static final long serialVersionUID = -5932674935888850461L;

        /**
         * instantiate this class with no textual description
         */
        public Value() {
          super("");
        }

        /**
         * instantiate this class with a given textual description
         *
         * @param message
         *          a description of the exceptional situation
         */
        public Value(final String message) {
          super(message);
        }

        public Value(final String format, final Object... args) {
          super(nprintf(format, args));
        }
      }

      /**
       * Abstract base class of exceptions thrown when a loop variant failed.
       *
       * @author Yossi Gil, the Technion.
       * @since 04/08/2008
       */
      public abstract static class Variant extends Assertion {
        /**
         * Thrown when the initial value of a loop variant was negative.
         *
         * @author Yossi Gil, the Technion.
         * @since 04/08/2008
         */
        public static final class Initial extends Variant {
          private static final long serialVersionUID = -6719831484164226074L;

          public Initial(final int value) {
            super(nprintf("Initial variant value (%d) is negative", value));
          }
        }

        /**
         * Thrown if an iteration of a certain loop failed to decrease this
         * loop's variant.
         *
         * @author Yossi Gil, the Technion.
         * @since 04/08/2008
         */
        public static final class Nondecreasing extends Variant {
          private static final long serialVersionUID = 5006328864309542167L;

          public Nondecreasing(final int newValue, final int oldValue) {
            super(nprintf("New variant value (%d) should be less than previous value (%d)", newValue, oldValue));
          }
        }

        /**
         * Thrown if an iteration of a certain loop tried to make this loop's
         * variant negative.
         *
         * @author Yossi Gil, the Technion.
         * @since 04/08/2008
         */
        public static final class Underflow extends Variant {
          private static final long serialVersionUID = 8362864540539118946L;

          public Underflow(final int newValue) {
            super(nprintf("New variant value (%d) is negative", newValue));
          }
        }

        private static final long serialVersionUID = 5055379624378837959L;

        /**
         * instantiate this class with a given textual description
         *
         * @param message
         *          a description of the exceptional situation
         */
        public Variant(final String message) {
          super(message);
        }
      }

      private static final long serialVersionUID = -7893002781575729383L;

      /**
       * instantiate this class with a given textual description
       *
       * @param message
       *          a description of the exceptional situation
       */
      public Assertion(final String message) {
        super(message);
      }
    }
    /**
     * Abstract base class of contract (pre- and post-condition) violations
     *
     * @author Yossi Gil, the Technion.
     * @since 04/08/2008
     */
    public abstract static class Contract extends ____.Bug {
      /**
       * Thrown in case a post-condition was not satisfied
       *
       * @author Yossi Gil, the Technion.
       * @since 04/08/2008
       */
      public static final class Postcondition extends Contract {
        private static final long serialVersionUID = -3177288122092390767L;

        /**
         * instantiate this class with a given textual description
         *
         * @param message
         *          a description of the exceptional situation
         */
        public Postcondition(final String message) {
          super(message);
        }
      }

      /**
       * Thrown in case a pre-condition was not satisfied
       *
       * @author Yossi Gil, the Technion.
       * @since 04/08/2008
       */
      public static final class Precondition extends Contract {
        private static final long serialVersionUID = -5317027949287654746L;

        /**
         * instantiate this class with a given textual description
         *
         * @param message
         *          a description of the exceptional situation
         */
        public Precondition(final String message) {
          super(message);
        }
      }

      private static final long serialVersionUID = -8228321063991272253L;

      /**
       * @param message
       *          a description of the exceptional situation
       */
      protected Contract(final String message) {
        super(message);
      }
    }

    private static final long serialVersionUID = 8737036163937047206L;

    /**
     * convert an ordinary exception into this type.
     *
     * @param e
     *          the exception to convert
     */
    public Bug(final Exception e) {
      super(e);
    }

    /**
     * instantiate this class with a given textual description
     *
     * @param message
     *          a description of the exceptional situation
     */
    public Bug(final String message) {
      super(message);
    }
  }
  /**
   * An interface representing a class with an invariant.
   *
   * @author Yossi Gil
   * @since 11/04/2006
   */
  public static interface Invariantable {
    /**
     * This function represents the invariant of the implementing class. It
     * returns nothing. However, if the invariant is violated, a runtime
     * exception aborts execution.
     */
    void check();
  }
  @SuppressWarnings("static-method") public static class TEST {
    @Test public void ensure() {
      ____.ensure(true);
      try {
        ____.ensure(false);
      } catch (final Postcondition e) {
        assertEquals("", e.getMessage());
      }
      try {
        ____.ensure(false, "ensure");
      } catch (final Postcondition e) {
        assertEquals("ensure", e.getMessage());
      }
      try {
        ____.ensure(false, "ensure %s message %s", "this", "now");
      } catch (final Postcondition e) {
        assertEquals("ensure this message now", e.getMessage());
      }
    }
    @Test public void negative() {
      ____.negative(-1);
      ____.negative(-2);
      ____.negative(-0.3);
      try {
        ____.negative(0);
      } catch (final Negative e) {
        assertEquals("Found 0 while expecting a negative integer.", e.getMessage());
      }
      try {
        ____.negative(0.0);
      } catch (final Negative e) {
        assertEquals("Found 0.00000 while expecting a negative number.", e.getMessage());
      }
      try {
        ____.negative(-1);
      } catch (final Negative e) {
        assertEquals("Found -1 while expecting a negative integer.", e.getMessage());
      }
      try {
        ____.negative(-1.0);
      } catch (final Negative e) {
        assertEquals("Found -1.00000 while expecting a negative number.", e.getMessage());
      }
    }
    @Test public void nonnegative() {
      ____.nonnegative(1);
      ____.nonnegative(2);
      ____.nonnegative(0);
      ____.nonnegative(0.3);
      ____.nonnegative(0.0);
      try {
        ____.nonnegative(1);
      } catch (final NonNegative e) {
        assertEquals("Found -1 while expecting a negative integer.", e.getMessage());
      }
      try {
        ____.nonnegative(1.0);
      } catch (final NonNegative e) {
        assertEquals("Found -1.00000 while expecting a negative number.", e.getMessage());
      }
    }
    @Test public void nonnull() {
      ____.nonnull(new Object());
      try {
        ____.nonnull(null);
      } catch (final NonNull e) {
        assertEquals("", e.getMessage());
      }
      try {
        ____.nonnull(null, "nonnull");
      } catch (final NonNull e) {
        assertEquals("nonnull", e.getMessage());
      }
      try {
        ____.nonnull(null, "nonnull %s message %s", "this", "now");
      } catch (final NonNull e) {
        assertEquals("nonnull this message now", e.getMessage());
      }
    }
    @Test public void nonpositive() {
      ____.nonpositive(-1);
      ____.nonpositive(-2);
      ____.nonpositive(-0.3);
      ____.nonpositive(0);
      ____.nonpositive(0.0);
      try {
        ____.nonpositive(-1);
      } catch (final NonPositive e) {
        assertEquals("Found -1 while expecting a nonpositive integer.", e.getMessage());
      }
      try {
        ____.nonpositive(-1.0);
      } catch (final NonPositive e) {
        assertEquals("Found -1.00000 while expecting a nonpositive number.", e.getMessage());
      }
    }
    @Test public void positive() {
      ____.positive(1);
      ____.positive(2);
      ____.positive(0.3);
      try {
        ____.positive(0);
      } catch (final Positive e) {
        assertEquals("Found 0 while expecting a positive integer.", e.getMessage());
      }
      try {
        ____.positive(0.0);
      } catch (final Positive e) {
        assertEquals("Found 0.00000 while expecting a positive number.", e.getMessage());
      }
      try {
        ____.positive(-1);
      } catch (final Positive e) {
        assertEquals("Found -1 while expecting a positive integer.", e.getMessage());
      }
      try {
        ____.positive(-1.0);
      } catch (final Positive e) {
        assertEquals("Found -1.00000 while expecting a positive number.", e.getMessage());
      }
    }
    @Test public void require() {
      ____.require(true);
      try {
        ____.require(false);
      } catch (final Precondition e) {
        assertEquals("", e.getMessage());
      }
      try {
        ____.require(false, "requireMessage");
      } catch (final Precondition e) {
        assertEquals("requireMessage", e.getMessage());
      }
      try {
        ____.require(false, "require %s message %s", "this", "now");
      } catch (final Precondition e) {
        assertEquals("require this message now", e.getMessage());
      }
    }
    @Test(expected = ____.Bug.class) public void requireBug() {
      ____.require(false);
    }
    @Test(expected = Precondition.class) public void requirePrecondition() {
      ____.require(false);
    }
    @Test public void sure() {
      ____.sure(true);
      try {
        ____.sure(false);
      } catch (final Invariant e) {
        assertEquals("", e.getMessage());
      }
      try {
        ____.sure(false, "sure");
      } catch (final Invariant e) {
        assertEquals("sure", e.getMessage());
      }
      try {
        ____.sure(false, "sure %s message %s", "this", "now");
      } catch (final Invariant e) {
        assertEquals("sure this message now", e.getMessage());
      }
    }
    @Test public void unreachable() {
      try {
        ____.unreachable();
      } catch (final Reachability e) {
        assertEquals("", e.getMessage());
      }
      try {
        ____.unreachable("unreachable message");
      } catch (final Reachability e) {
        assertEquals("unreachable message", e.getMessage());
      }
      try {
        ____.unreachable("unreachable %s message %s", "this", "now");
      } catch (final Reachability e) {
        assertEquals("unreachable this message now", e.getMessage());
      }
    }
    @Test public void variant() {
      {
        final Variant v = new Variant(10);
        assertEquals(10, v.value());
        v.check(9);
        v.check(8);
        v.check(4);
        v.check(2);
        v.check(1);
        v.check(0);
        assertEquals(0, v.value());
      }
      try {
        unused(new Variant(-1));
      } catch (final Initial e) {
        assertEquals("Initial variant value (-1) is negative", e.getMessage());
      }
      try {
        final Variant v = new Variant(10);
        v.check(8);
        v.check(9);
      } catch (final Nondecreasing e) {
        assertEquals("New variant value (9) should be less than previous value (8)", e.getMessage());
      }
      try {
        final Variant v = new Variant(10);
        v.check(8);
        v.check(-2);
      } catch (final Underflow e) {
        assertEquals("New variant value (-2) is negative", e.getMessage());
      }
    }
  }
  /**
   * A class to emulate Eiffel's <code>variant</code> construct. To use, create
   * an object of this type, initializing it with the variant's first value ,
   * and then call function {@link #check(int)} successively.
   *
   * @author Yossi Gil
   * @since 05/06/2007
   */
  public static final class Variant {
    private int value;

    /**
     * Initialize a variant, with a specified value
     *
     * @param value
     *          a non-negative value
     * @throws Bug.Assertion.Variant.Initial
     *           in case initial value is negative
     */
    public Variant(final int value) throws Bug.Assertion.Variant.Initial {
      if (value < 0)
        throw new Assertion.Variant.Initial(value);
      this.value = value;
    }
    /**
     * reset the variant value to a new, smaller value value; abort if the new
     * value is negative or no lesser than the previous value.
     *
     * @param newValue
     *          the next value of this variant.
     * @throws Nondecreasing
     *           in case the variant's value did not decrease
     * @throws Underflow
     *           in case the variant's value went negative
     */
    public void check(final int newValue) throws Nondecreasing, Underflow {
      if (newValue >= value)
        throw new Nondecreasing(newValue, value);
      if (newValue < 0)
        throw new Underflow(newValue);
      value = newValue;
    }
    /**
     * inspect the variant's value.
     *
     * @return a non-negative integer which is the current value of this object
     */
    public int value() {
      return value;
    }
  }
  /**
   * A do nothing method to document the fact that a <code><b>long</b></code>
   * parameter, along with a optional list of {@link Object}s are not used by a
   * function, and to suppress the warning.
   *
   * @param _
   *          the unused parameter
   * @param __
   *          more unused parameters
   */
  public static void ___unused(final long _, final Object... __) {
    // empty
  }
  /**
   * A do nothing method to document the fact that some <code>Object</code>(s)
   * parameter(s) (or local variable(s)) are not used by a function. Calling
   * this method saves the caller the trouble of suppressing a "variable unused"
   * warnings on the argument(s).
   *
   * @param __
   *          the unused parameters
   */
  public static void ___unused(final Object... __) {
    unused(__);
  }
  /**
   * Exercise the {@link Invariantable#check()}
   *
   * @param v
   *          a Invariantable object whose invariant should be checked
   */
  public static void check(final Invariantable v) {
    v.check();
  }
  /**
   * A possibly non-returning method to be used for checking postconditions.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @throws Postcondition
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void ensure(final boolean condition) throws Postcondition {
    ensure(condition, "");
  }
  /**
   * A possibly non-returning method to be used for checking postconditions. If
   * the postcondition fails, then a user supplied message is associated with
   * the thrown exception.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Postcondition
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void ensure(final boolean condition, final String message) throws Postcondition {
    ensure(condition, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking postconditions. If
   * the postcondition fails, then a user supplied formatted message (generated
   * from <code>printf</code> like arguments) is associated with the thrown
   * exception.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Postcondition
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void ensure(final boolean condition, final String format, final Object... args) throws Postcondition {
    if (!condition)
      throw new Postcondition(nprintf(format, args));
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be negative.
   *
   * @param d
   *          a value which must be negative
   * @throws Negative
   *           in case <code>d</code> was nonnegative
   */
  public static void negative(final double d) throws Negative {
    negative(d, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be negative.
   *
   * @param d
   *          a value which must be negative
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Negative
   *           in case <code>d</code> was nonnegative
   */
  public static void negative(final double d, final String message) throws Negative {
    negative(d, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be negative.
   *
   * @param d
   *          a value which must be negative
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Negative
   *           in case <code>d</code> was nonnegative
   */
  public static void negative(final double d, final String format, final Object... args) throws Negative {
    if (d >= 0)
      throw new Negative(d, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be negative.
   *
   * @param n
   *          a value which must be negative
   * @throws Negative
   *           in case <code>n</code> was nonnegative
   */
  public static void negative(final int n) throws Negative {
    negative(n, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be negative.
   *
   * @param n
   *          a value which must be negative
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Negative
   *           in case <code>n</code> was nonnegative
   */
  public static void negative(final int n, final String message) throws Negative {
    negative(n, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be negative.
   *
   * @param n
   *          a value which must be negative
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Negative
   *           in case <code>n</code> was nonnegative
   */
  public static void negative(final int n, final String format, final Object... args) throws Negative {
    if (n >= 0)
      throw new Negative(n, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be non-NaN.
   *
   * @param d
   *          a value which must be not be NaN
   * @throws NonNan
   *           in case <code>d</code> was NaN
   */
  public static void nonNaN(final double d) throws NonNan {
    nonNaN(d, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be non-NaN.
   *
   * @param d
   *          a value which must be nonnegative
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws NonNan
   *           in case <code>d</code> was NaN
   */
  public static void nonNaN(final double d, final String message) throws NonNan {
    nonNaN(d, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be non-NaN.
   *
   * @param d
   *          a value which must be nonnegative
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws NonNan
   *           in case <code>d</code> was NaN
   */
  public static void nonNaN(final double d, final String format, final Object... args) throws NonNan {
    if (Double.isNaN(d))
      throw new NonNan(d, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking doubles which must
   * be non-NaN.
   *
   * @param ds
   *          a array which must not be NaN
   * @throws NonNan
   *           in case <code>n</code> was NaN
   */
  public static void nonNaN(final double[] ds) throws NonNan {
    for (final double d : ds)
      nonNaN(d);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param d
   *          a value which must be nonnegative
   * @throws NonNegative
   *           in case <code>d</code> was negative
   */
  public static void nonnegative(final double d) throws NonNegative {
    nonnegative(d, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param d
   *          a value which must be nonnegative
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws NonNegative
   *           in case <code>n</code> was negative
   */
  public static void nonnegative(final double d, final String message) throws NonNegative {
    nonnegative(d, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param d
   *          a value which must be nonnegative
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws NonNegative
   *           in case <code>d</code> was negative
   */
  public static void nonnegative(final double d, final String format, final Object... args) throws NonNegative {
    if (d < 0)
      throw new NonNegative(d, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking doubles which must
   * be nonnegative.
   *
   * @param ds
   *          a array which must be nonnegative
   * @throws NonNegative
   *           in case <code>n</code> was negative
   */
  public static void nonnegative(final double[] ds) throws NonNegative {
    for (final double d : ds)
      nonnegative(d);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param n
   *          a value which must be nonnegative
   * @throws NonNegative
   *           in case <code>n</code> was negative
   */
  public static void nonnegative(final int n) throws NonNegative {
    nonnegative(n, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param n
   *          a value which must be nonnegative
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws NonNegative
   *           in case <code>n</code> was negative
   */
  public static void nonnegative(final int n, final String message) throws NonNegative {
    nonnegative(n, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param n
   *          a value which must be nonnegative
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws NonNegative
   *           in case <code>n</code> was negative
   */
  public static void nonnegative(final int n, final String format, final Object... args) throws NonNegative {
    if (n < 0)
      throw new NonNegative(n, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonnegative.
   *
   * @param ns
   *          a array which must be nonnegative
   * @throws NonNegative
   *           in case <code>n</code> was negative
   */
  public static void nonnegative(final int[] ns) throws NonNegative {
    for (final int n : ns)
      nonnegative(n);
  }
  /**
   * A possibly non-returning method to be used for checking objects that should
   * never be <code><b>null</b></code>.
   *
   * @param o
   *          if <code><b>null</b></code>, program will halt.
   * @throws NonNull
   *           in case <code>o</code> was <code><b>null</b></code>
   */
  public static void nonnull(final Object o) throws NonNull {
    nonnull(o, "");
  }
  /**
   * A possibly non-returning method to be used for checking objects that should
   * never be <code><b>null</b></code>.
   *
   * @param o
   *          if <code><b>null</b></code>, program will halt.
   * @param message
   *          an error message to be associated with the failure
   * @throws NonNull
   *           in case <code>o</code> was <code><b>null</b></code>
   */
  public static void nonnull(final Object o, final String message) throws NonNull {
    nonnull(o, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking objects that should
   * never be <code><b>null</b></code>.
   *
   * @param o
   *          if <code><b>null</b></code>, program will halt.
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws NonNull
   *           in case <code>o</code> was <code><b>null</b></code>
   */
  public static void nonnull(final Object o, final String format, final Object... args) throws NonNull {
    if (o == null)
      throw new NonNull(format, args);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonpositive.
   *
   * @param d
   *          a value which must be nonpositive
   * @throws NonPositive
   *           in case <code>d</code> was positive
   */
  public static void nonpositive(final double d) throws NonPositive {
    nonpositive(d, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonpositive.
   *
   * @param d
   *          a value which must be nonpositive
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws NonPositive
   *           in case <code>d</code> was positive
   */
  public static void nonpositive(final double d, final String message) throws NonPositive {
    nonpositive(d, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonpositive.
   *
   * @param d
   *          a value which must be nonpositive
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws NonPositive
   *           in case <code>d</code> was positive
   */
  public static void nonpositive(final double d, final String format, final Object... args) throws NonPositive {
    if (d > 0)
      throw new NonPositive(d, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonpositive.
   *
   * @param n
   *          a value which must be positive
   * @throws NonPositive
   *           in case <code>n</code> was positive.
   */
  public static void nonpositive(final int n) throws NonPositive {
    nonpositive(n, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonpositive.
   *
   * @param n
   *          a value which must be nonpositive
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws NonPositive
   *           in case <code>n</code> was positive
   */
  public static void nonpositive(final int n, final String message) throws NonPositive {
    nonpositive(n, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be nonpositive.
   *
   * @param n
   *          a value which must be nonpositive
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws NonPositive
   *           in case <code>n</code> was positive
   */
  public static void nonpositive(final int n, final String format, final Object... args) throws NonPositive {
    if (n > 0)
      throw new NonPositive(n, format, args);
  }
  /**
   * A do nothing method to document the fact that nothing is done, to be used
   * typically for C style (Yuck) loops.
   */
  public static void nothing() {
    // empty
  }
  /**
   * A possibly non-returning method to be used for checking floating point
   * numbers which must be positive.
   *
   * @param d
   *          a value which must be positive
   * @throws Positive
   *           in case <code>d</code> was nonpositive
   */
  public static void positive(final double d) throws Positive {
    positive(d, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be positive.
   *
   * @param d
   *          a value which must be positive
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Positive
   *           in case <code>n</code> was nonpositive
   */
  public static void positive(final double d, final String message) throws Positive {
    positive(d, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be positive.
   *
   * @param d
   *          a value which must be positive
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Positive
   *           in case <code>d</code> was not positive
   */
  public static void positive(final double d, final String format, final Object... args) throws Positive {
    if (d <= 0)
      throw new Positive(d, format, args);
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be positive.
   *
   * @param n
   *          a value which must be positive
   * @throws Positive
   *           in case <code>n</code> was nonpositive
   */
  public static void positive(final int n) throws Positive {
    positive(n, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be positive.
   *
   * @param n
   *          if negative program will halt.
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Positive
   *           in case <code>n</code> was nonpositive
   */
  public static void positive(final int n, final String message) throws Positive {
    positive(n, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking integers which must
   * be positive.
   *
   * @param n
   *          a value which must be positive
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Positive
   *           in case <code>d</code> was nonpositive
   */
  public static void positive(final int n, final String format, final Object... args) throws Positive {
    if (n <= 0)
      throw new Positive(n, format, args);
  }

  /**
   * A possibly non-returning method to be used for checking preconditions.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @throws Precondition
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void require(final boolean condition) throws Precondition {
    require(condition, "");
  }
  /**
   * A possibly non-returning method to be used for checking preconditions. If
   * the precondition fails, then a user supplied message is associated with the
   * thrown exception.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Precondition
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void require(final boolean condition, final String message) throws Precondition {
    require(condition, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking preconditions. If
   * the precondition fails, then a user supplied formatted message (generated
   * from <code>printf</code> like arguments) is associated with the thrown
   * exception.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Precondition
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void require(final boolean condition, final String format, final Object... args) throws Precondition {
    if (!condition)
      throw new Precondition(nprintf(format, args));
  }
  /**
   * A possibly non-returning method to be used for checking assertions.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @throws Invariant
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void sure(final boolean condition) throws Invariant {
    sure(condition, "");
  }
  /**
   * A possibly non-returning method to be used for checking assertions. If the
   * postcondition fails, then a user supplied message is associated with the
   * thrown exception.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @param message
   *          text to be associated with the exception thrown in the case of an
   *          error.
   * @throws Invariant
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void sure(final boolean condition, final String message) throws Invariant {
    sure(condition, message, "");
  }
  /**
   * A possibly non-returning method to be used for checking assertions. If the
   * postcondition fails, then a user supplied formatted message (generated from
   * <code>printf</code> like arguments) is associated with the thrown
   * exception.
   *
   * @param condition
   *          if <code><b>false</b></code>, program will halt.
   * @param format
   *          format string to be associated with the exception thrown in the
   *          case of an error.
   * @param args
   *          <code>printf</code>-like arguments to be used with the format
   *          string.
   * @throws Invariant
   *           A {@link RuntimeException} to be thrown in the case
   *           <code>condition</code> was <code><b>false</b></code>
   */
  public static void sure(final boolean condition, final String format, final Object... args) throws Invariant {
    if (!condition)
      throw new Invariant(nprintf(format, args));
  }
  /**
   * A never-returning method indicating code sites with missing functionality
   *
   * @param args
   *          a list of strings in a <code>printf</code> like format describing
   *          the task to be done.
   */
  public static void todo(final String... args) {
    error("Feature unsupported. ", args);
  }
  /**
   * A never-returning method to be used in points of code which should never be
   * reached.
   *
   * @throws Reachability
   *           will always be thrown
   */
  public static void unreachable() throws Reachability {
    unreachable("");
  }
  /**
   * A never-returning method to be used in points of code which should never be
   * reached.
   *
   * @param message
   *          a string describing the violation
   * @throws Reachability
   *           will always be thrown
   */
  public static void unreachable(final String message) throws Reachability {
    unreachable(message, "");
  }
  public static void unreachable(final String format, final Object... args) throws Reachability {
    throw new Reachability(nprintf(format, args));
  }
  /**
   * A do nothing method to document the fact that a <code><b>double</b></code>
   * parameter, along with a optional list of {@link Object}s are not used by a
   * function, and to suppress the warning.
   *
   * @param _
   *          the unused parameter
   * @param __
   *          more unused parameters
   */
  public static void unused(final double _, final double... __) {
    // empty
  }
  /**
   * A do nothing method to document the fact that a <code><b>long</b></code>
   * parameter, along with a optional list of {@link Object}s are not used by a
   * function, and to suppress the warning.
   *
   * @param _
   *          the unused parameter
   * @param __
   *          more unused parameters
   */
  public static void unused(final int _, final int... __) {
    // empty
  }
  /**
   * A do nothing method to document the fact that a <code><b>long</b></code>
   * parameter, along with a optional list of {@link Object}s are not used by a
   * function, and to suppress the warning.
   *
   * @param _
   *          the unused parameter
   * @param __
   *          more unused parameters
   */
  public static void unused(final long _, final long... __) {
    // empty
  }
  /**
   * A do nothing method to document the fact that some <code>Object</code>(s)
   * parameter(s) (or local variable(s)) are not used by a function. Calling
   * this method saves the caller the trouble of suppressing a "variable unused"
   * warnings on the argument(s).
   *
   * @param __
   *          the unused parameters
   */
  public static void unused(final Object... __) {
    // Empty
  }
  public static void warn(final boolean condition, final String s) {
    if (condition)
      return;
    warn(s);
  }
  public static void warn(final String s) {
    System.out.println(s);
  }
  private static String buildMessage(final String kind, final String... args) {
    String $ = kind + " ";
    switch (args.length) {
      case 0:
        break;
      case 1:
        $ += args[0];
        break;
      default:
        final Object os[] = new Object[args.length - 1];
        for (int i = 1; i < args.length; i++)
          os[i - 1] = args[i];
        final Formatter f = new Formatter();
        f.format(args[0], os);
        $ += f.out();
    }
    return $;
  }
  private static void error(final String kind, final String... args) {
    final String s = buildMessage(kind, args);
    System.out.flush();
    System.err.flush();
    System.err.println(s);
    System.err.flush();
    new Exception().printStackTrace(System.err);
    STOP.stop(s);
  }
  /**
   * A possibly non returning method used in class implementation.
   *
   * @param cond
   *          If <code><b>false</b></code>, method will not return and print
   *          error message.
   * @param kind
   *          A string describing an error kind, e.g., pre-condition failure
   * @param args
   *          Additional strings describing an error kind in a
   *          <code>printf</code> format.
   */
  static void error(final boolean cond, final String kind, final String... args) {
    if (!cond)
      error(kind, args);
  }

  static String nprintf(final String format, final double d, final Object o) {
    return sprintf(format, box(d), o);
  }

  static String nprintf(final String format, final int n) {
    return sprintf(format, box(n));
  }

  static String nprintf(final String format, final int n1, final int n2) {
    return sprintf(format, box(n1), box(n2));
  }

  static String nprintf(final String format, final Object... args) {
    return format == null ? "" : args == null ? format : sprintf(format, args);
  }
}

