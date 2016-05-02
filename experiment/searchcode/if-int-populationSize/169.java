package votorola.a.poll.district; // Copyright 2008-2009, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Votorola Software"), to deal in the Votorola Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicence, and/or sell copies of the Votorola Software, and to permit persons to whom the Votorola Software is furnished to do so, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Votorola Software. THE VOTOROLA SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE VOTOROLA SOFTWARE OR THE USE OR OTHER DEALINGS IN THE VOTOROLA SOFTWARE.

import java.io.*;
import java.util.*;
import javax.script.*;
import votorola.a.*;
import votorola.g.lang.*;
import votorola.g.script.*;


/** A divisional scheme.
  *
  *     @see <a href='../../../../../../s/manual.xht#divisional-scheme'
  *                           >../../../s/manual.xht#divisional-scheme</a>
  *     @see DivisionalStratum#SCHEME
  */
public @ThreadSafe final class DivisionalScheme implements DivisionalNode
{

    private static final long serialVersionUID = 1L;



    /** Constructs a DivisionalScheme.
      *
      *     @param schemeConfigurationPath the path to the startup configuration file
      *       (it will be converted to canonical form, if it is non-canonical)
      */
    public static DivisionalScheme newDivisionalScheme( final String schemeConfigurationPath )
      throws IOException, ScriptException
    {
        final JavaScriptIncluder s = new JavaScriptIncluder( new File( schemeConfigurationPath ));
        final ConstructionContext cc = new ConstructionContext( s );
        s.invokeKnownFunction( "constructingDivisionalScheme", cc );
        return new DivisionalScheme( cc );
    }



    private DivisionalScheme( final ConstructionContext cc ) throws IOException
    {
        startupConfigurationFile = cc.startupConfigurationFile;
        isAbstractSingleton = cc.isAbstractSingleton();
        shortTitle = cc.getShortTitle();
        summaryDescription = cc.getSummaryDescription();
        title = cc.getTitle();
    }



   // ------------------------------------------------------------------------------------


    /** Answers whether or not this scheme is an abstract singleton.  An abstract
      * singleton is not a real scheme, and it normally defines only a single district.
      * When it defines a single district, it will usually be hidden or styled differently
      * in the user interface.  (When it does not, it will treated as any other scheme.)
      *
      *     @see ConstructionContext#setAbstractSingleton(boolean)
      */
    public final boolean isAbstractSingleton() { return isAbstractSingleton; };


        private final boolean isAbstractSingleton;



    /** The population base of this scheme, if known.  This is simply the sum of
      * populations across all districts of the scheme.
      *
      *     @return sum of district populations, or zero if the population
      *       is unknown for any district
      *
      *     @see District#populationSize()
      */
    public long populationSize( final Pollserver.Run run )
    {
        ensureTransients( run );
        return populationSize;
    }


        private transient volatile Long populationSize; // instead of primitive 'long', which cannot be set atomically



    /** The startup configuration file for this scheme.  It is guaranteed to be in canonical form.
      * The language is JavaScript.  There are restrictions on the {@linkplain
      * votorola.g.script.JavaScriptIncluder character encoding}.
      */
    public File startupConfigurationFile() { return startupConfigurationFile; }


        private final File startupConfigurationFile;



    /** A brief description of this scheme, in sentence form.
      * It is intended for display, for example, as an introductory paragraph.
      *
      *     @see ConstructionContext#setSummaryDescription(String)
      */
    public String summaryDescription() { return summaryDescription; }


        private final String summaryDescription;



    /** The proper name of this scheme, in wiki-style title case (leading letter only).
      *
      *     @see #shortTitle()
      *     @see ConstructionContext#setTitle(String,String)
      */
    public String title() { return title; }


        private final String title;



   // - O b j e c t ----------------------------------------------------------------------


    /** Returns true iff o is a scheme constructed from the same startup configuration file.
      */
    public @Override boolean equals( Object o )
    {
        if( o == null || !getClass().equals( o.getClass() )) return false;

        return name().equals( ((DivisionalScheme)o).name() );
    }



    public @Override int hashCode() { return name().hashCode(); }



   // - D i v i s i o n a l - N o d e ----------------------------------------------------


    /** The canonical path of the startup configuration file, identifying this scheme uniquely
      * among all other schemes.
      *
      *     @see #startupConfigurationFile()
      */
    public String name() { return startupConfigurationFile.getPath(); }



    public List<District> divisionalChildren( final Pollserver.Run run )
    {
        ensureTransients( run );
        return divisionalChildren;
    }


        private transient volatile List<District> divisionalChildren;



    public DivisionalStratum divisionalStratum() { return DivisionalStratum.SCHEME; }



    public DivisionalNode divisionalSuperNode() { return null; }



    /** @see #title()
      * @see ConstructionContext#setTitle(String,String)
      */
    public String shortTitle() { return shortTitle; }


        private final String shortTitle;



   // ====================================================================================


    /** A context for configuring a {@linkplain DivisionalScheme divisional scheme}.  The
      * scheme is configured by its {@linkplain DivisionalScheme#startupConfigurationFile()
      * startup configuration file}, which contains a script (s) for that purpose.  During
      * construction of the scheme, an instance of this context (schemeCC) is passed
      * to s, via s::constructingDivisionalScheme(schemeCC).
      */
    public static @ThreadSafe final class ConstructionContext
    {


        private ConstructionContext( final JavaScriptIncluder s )
        {
            startupConfigurationFile = s.scriptFile().getAbsoluteFile();
            summaryDescription = "This is a divisional scheme for districts.  Further information "
              + "is unavailable, because the 'constructingDivisionalScheme' function "
              + "of script " + startupConfigurationFile + " "
              + "makes no call to 'setSummaryDescription'.";
        }



        private final File startupConfigurationFile;



       // --------------------------------------------------------------------------------


        /** @see DivisionalScheme#isAbstractSingleton()
          * @see #setAbstractSingleton(boolean)
          */
        public final boolean isAbstractSingleton() { return isAbstractSingleton; };


            private boolean isAbstractSingleton;


            /** Sets whether or not the scheme is an abstract singleton.
              *
              *     @see DivisionalScheme#isAbstractSingleton()
              */
            public final void setAbstractSingleton( boolean b ) { isAbstractSingleton = b; }



        /** @see DivisionalScheme#shortTitle()
          * @see #setTitle(String,String)
          */
        public String getShortTitle() { return shortTitle; }


            private String shortTitle = "Test scheme";



        /** @see DivisionalScheme#summaryDescription()
          * @see #setSummaryDescription(String)
          */
        public String getSummaryDescription() { return summaryDescription; }


            private String summaryDescription;


            /** Sets the summary description of the scheme.  The default value is a
              * placeholder with configuration instructions for the administrator.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see DivisionalScheme#summaryDescription()
              */
              @ThreadRestricted("constructor")
            public void setSummaryDescription( String summaryDescription )
            {
                if( summaryDescription.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.summaryDescription = summaryDescription;
            }



        /** @see DivisionalScheme#title()
          * @see #setTitle(String,String)
          */
        public String getTitle() { return title; }


            private String title = shortTitle;;


            /** Sets the title of the district.  The default value for both the long and
              * short titles is the placeholder "Test scheme".
              *
              *     @see DivisionalScheme#title()
              *     @see DivisionalScheme#shortTitle()
              */
              @ThreadRestricted("constructor")
            public void setTitle( String title, String shortTitle )
            {
                this.title = title;
                this.shortTitle = shortTitle;
            }


    }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    /** Lazilly checks transient fields and ensures they are initialized, if this instance
      * is newly contructed or deserialized.
      */
    private void ensureTransients( final Pollserver.Run run )
    {
        if( divisionalChildren != null ) return; // non-atomic test/set, not crucial

        divisionalChildren = run.scopePoll().divisionalSchemeChildren( name() );
        long n = 0;
        for( final District district: divisionalChildren )
        {
            final long nDistict = district.populationSize();
            if( nDistict == 0 )
            {
                n = 0; // unknown, as one of districts is unknown
                break;
            }

            n += nDistict;
        }
        populationSize = n;
    }



}

