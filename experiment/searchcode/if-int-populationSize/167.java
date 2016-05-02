package votorola.a.poll.district; // Copyright 2007-2009, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Votorola Software"), to deal in the Votorola Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicence, and/or sell copies of the Votorola Software, and to permit persons to whom the Votorola Software is furnished to do so, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Votorola Software. THE VOTOROLA SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE VOTOROLA SOFTWARE OR THE USE OR OTHER DEALINGS IN THE VOTOROLA SOFTWARE.

import java.io.*;
import javax.script.*;
import java.util.*;
import votorola.a.*;
import votorola.g.lang.*;
import votorola.g.script.*;


/** An electoral district.
  *
  *     @see <a href='../../../../../../s/manual.xht#district'
  *                           >../../../s/manual.xht#district</a>
  *     @see DivisionalStratum#DISTRICT
  */
public @ThreadSafe final class District implements DivisionalNode
{

    private static final long serialVersionUID = 0L;



    /** Constructs a District.
      *
      *     @param districtConfigurationPath the path to the startup configuration file
      *       (it will be converted to canonical form, if it is non-canonical)
      */
    public static District newDistrict( final Pollserver.Run run,
      final String districtConfigurationPath ) throws IOException, ScriptException
    {
        final JavaScriptIncluder s = new JavaScriptIncluder( new File( districtConfigurationPath ));
        final ConstructionContext cc = new ConstructionContext( run.pollserver(), s );
        s.invokeKnownFunction( "constructingDistrict", cc );
        return new District( run, cc );
    }



    private District( final Pollserver.Run run, final ConstructionContext cc )
      throws IOException, ScriptException
    {
        startupConfigurationFile = cc.startupConfigurationFile;
        divisionalScheme = run.scopePoll().init_ensureDivisionalScheme(
          cc.getSchemeConfigurationPath() );
        populationSize = cc.getPopulationSize();
        populationSizeExplanation = cc.getPopulationSizeExplanation();
        shortTitle = cc.getShortTitle();
        summaryDescription = cc.getSummaryDescription();
        title = cc.getTitle();
    }



   // ------------------------------------------------------------------------------------


    /** The divisional scheme that defines this district.
      *
      *     @see ConstructionContext#setSchemeConfigurationPath(String)
      */
    public DivisionalScheme divisionalScheme() { return divisionalScheme; }


        private final DivisionalScheme divisionalScheme;



    /** The population base of this district, if known.  This is an estimate of the total
      * number of people who are permanent residents of the district.  All residents are
      * included in this total, regardless of their eligibility to vote in polls.
      *
      *     @return estimated population size, or zero if unknown
      *
      *     @see ConstructionContext#setPopulationSize(long)
      */
    public long populationSize() { return populationSize; }


        private final long populationSize;



    /** An explanation of the population base, intended for the information of users.  For
      * example: "population of Ward 19 from 2008 census".
      *
      *     @see ConstructionContext#setPopulationSizeExplanation(String)
      */
    public String populationSizeExplanation() { return populationSizeExplanation; }


        private final String populationSizeExplanation;



    /** The startup configuration file for this district.  It is guaranteed to be in canonical
      * form.  The language is JavaScript.  There are restrictions on the {@linkplain
      * votorola.g.script.JavaScriptIncluder character encoding}.
      */
    public File startupConfigurationFile() { return startupConfigurationFile; }


        private final File startupConfigurationFile;



    /** A brief description of this district, in sentence form.
      * It is intended for display, for example, as an introductory paragraph.
      *
      *     @see ConstructionContext#setSummaryDescription(String)
      */
    public String summaryDescription() { return summaryDescription; }


        private final String summaryDescription;



    /** The proper name of this district, in wiki-style title case (leading letter only).
      *
      *     @see #shortTitle()
      *     @see ConstructionContext#setTitle(String,String)
      */
    public String title() { return title; }


        private final String title;



   // - O b j e c t ----------------------------------------------------------------------


    /** Returns true iff o is a district constructed from the same startup configuration file.
      */
    public @Override boolean equals( Object o )
    {
        if( o == null || !getClass().equals( o.getClass() )) return false;

        return name().equals( ((District)o).name() );
    }



    public @Override int hashCode() { return name().hashCode(); }



   // - D i v i s i o n a l - N o d e ----------------------------------------------------


    /** The canonical path of the startup configuration file, identifying this scheme uniquely
      * among all other schemes.
      *
      *     @see #startupConfigurationFile()
      */
    public String name() { return startupConfigurationFile.getPath(); }



    public List<DivisionalPollNode> divisionalChildren( Pollserver.Run run )
    {
        if( divisionalChildren == null ) // for a newly contructed or deserialized 'this'
        {
            divisionalChildren = run.scopePoll().districtDivisionalChildren( name() ); // non-atomic test/set, not crucial
        }
        return divisionalChildren;
    }


        private transient volatile List<DivisionalPollNode> divisionalChildren;



    public DivisionalStratum divisionalStratum() { return DivisionalStratum.DISTRICT; }



    /** @see #divisionalScheme()
      */
    public DivisionalNode divisionalSuperNode() { return divisionalScheme; }



    /** @see #title()
      * @see ConstructionContext#setTitle(String,String)
      */
    public String shortTitle() { return shortTitle; }


        private final String shortTitle;



   // ====================================================================================


    /** A context for configuring an {@linkplain District electoral district}.  The
      * district is configured by its {@linkplain District#startupConfigurationFile()
      * startup configuration file}, which contains a script (s) for that purpose.  During
      * construction of the district, an instance of this context (districtCC) is passed
      * to s, via s::constructingDistrict(districtCC).
      */
    public static @ThreadSafe final class ConstructionContext
    {


        private ConstructionContext( final Pollserver pollserver, final JavaScriptIncluder s )
          throws IOException
        {
            startupConfigurationFile = s.scriptFile();

            setSchemeConfigurationPath( pollserver.scopePoll().pollDirectory().getPath()
              + File.separator + "divisional-scheme.js" );
            summaryDescription = "This is a district for polls.  Further information "
              + "is unavailable, because the 'constructingDistrict' function "
              + "of script " + startupConfigurationFile + " "
              + "makes no call to 'setSummaryDescription'.";
        }



        private final File startupConfigurationFile;



       // --------------------------------------------------------------------------------


        /** @see District#populationSize()
          * @see #setPopulationSize(long)
          */
        public long getPopulationSize() { return populationSize; }


            private long populationSize; // default zero = unknown, so unlikely to divide by it


            /** Sets the population base of this district.  The default value is zero,
              * meaning unknown.
              *
              *     @see District#populationSize()
              */
            @ThreadRestricted("constructor") public void setPopulationSize( long populationSize )
            {
                this.populationSize = populationSize;
            }



        /** @see District#populationSizeExplanation()
          * @see #setPopulationSizeExplanation(String)
          */
        public String getPopulationSizeExplanation() { return populationSizeExplanation; }


            private String populationSizeExplanation = "population of district";


            /** Sets the explanation of the population base.  The default value is
              * "population of district".
              *
              *     @see District#populationSizeExplanation()
              */
              @ThreadRestricted("constructor")
            public void setPopulationSizeExplanation( String populationSizeExplanation )
            {
                this.populationSizeExplanation = populationSizeExplanation;
            }



        /** @see District#divisionalScheme()
          * @see #setSchemeConfigurationPath(String)
          */
        public String getSchemeConfigurationPath() { return schemeConfigurationPath; }


            private String schemeConfigurationPath;


            /** Sets the path to the startup configuration file of this district's scheme.  It
              * must not contain any shell variable, or tilde ~.  It will be converted to
              * canonical form, if it is non-canonical.  The default value is:
              * <p class='indent'>
              *     ~/votorola/{@linkplain votorola.a.poll.Poll.PollserverScope#pollDirectory() poll}/divisional-scheme.js
              *     </p>
              *
              *     @see District#divisionalScheme()
              *     @see DivisionalScheme#startupConfigurationFile()
              */
              @ThreadRestricted("constructor")
            public void setSchemeConfigurationPath( String schemeConfigurationPath )
              throws IOException
            {
                this.schemeConfigurationPath =
                  new File( schemeConfigurationPath ).getCanonicalPath();
            }



        /** @see District#shortTitle()
          * @see #setTitle(String,String)
          */
        public String getShortTitle() { return shortTitle; }


            private String shortTitle = "Test district";



        /** @see District#summaryDescription()
          * @see #setSummaryDescription(String)
          */
        public String getSummaryDescription() { return summaryDescription; }


            private String summaryDescription;


            /** Sets the summary description of the district.  The default value is a
              * placeholder with configuration instructions for the administrator.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see District#summaryDescription()
              */
              @ThreadRestricted("constructor")
            public void setSummaryDescription( String summaryDescription )
            {
                if( summaryDescription.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.summaryDescription = summaryDescription;
            }



        /** @see District#title()
          * @see #setTitle(String,String)
          */
        public String getTitle() { return title; }


            private String title = shortTitle;;


            /** Sets the title of the district.  The default value for both the long and
              * short titles is the placeholder "Test district".
              *
              *     @see District#title()
              *     @see District#shortTitle()
              */
              @ThreadRestricted("constructor")
            public void setTitle( String title, String shortTitle )
            {
                this.title = title;
                this.shortTitle = shortTitle;
            }



    }



}

