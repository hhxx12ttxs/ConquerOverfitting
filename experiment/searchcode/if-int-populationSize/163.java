package votorola.a.register; // Copyright 2008-2009, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Votorola Software"), to deal in the Votorola Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicence, and/or sell copies of the Votorola Software, and to permit persons to whom the Votorola Software is furnished to do so, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Votorola Software. THE VOTOROLA SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE VOTOROLA SOFTWARE OR THE USE OR OTHER DEALINGS IN THE VOTOROLA SOFTWARE.

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.*;
import java.sql.*;
import javax.script.*;
import votorola._.*;
import votorola.a.*;
import votorola.g.locale.*;
import votorola.a.register.trust.*;
import votorola.a.response.*;
import votorola.g.lang.*;
import votorola.g.logging.*;
import votorola.g.script.*;
import votorola.g.sql.*;
import votorola.g.util.*;


/** A voter register.
  *
  *     @see <a href='../../../../../s/manual.xht#voter-register'
  *                           >../../s/manual.xht#voter-register</a>
  */
  @ThreadRestricted("holds lock()")
public final class Register extends VoterService.UIdentified implements InputStore
{

    // cf. a/poll/Poll


    /** Constructs a Register.
      *
      *     @param s the compiled startup configuration script
      */
    public static @ThreadSafe Register newRegister( final Pollserver.Run run,
      final JavaScriptIncluder s ) throws IOException, ScriptException, SQLException
    {
        final ConstructionContext cc = new ConstructionContext( run.pollserver(), s );
        s.invokeKnownFunction( "contructingRegister", cc );
        cc.verifyWellFormed();
        return new Register( run, cc );
    }



    private Register( final Pollserver.Run run, final ConstructionContext cc )
      throws IOException, ScriptException, SQLException
    {
        super( run, cc );

        ArrayList<CommandResponder> responderList = new ArrayList<CommandResponder>();
          responderList.add( new CR_Doubt( Register.this ));
        responderList.add( new CR_Undoubt( Register.this ));
          responderList.add( new CR_Set( Register.this ));
        responderList.add( new CR_Unset( Register.this ));
          responderList.add( new CR_Trust( Register.this ));
        responderList.add( new CR_Untrust( Register.this ));
        init( responderList );

        constructionContext = null; // done with it, free the memory
    }



    private ConstructionContext cc() { return (ConstructionContext)constructionContext; } // nulled after init



   // ````````````````````````````````````````````````````````````````````````````````````
   // Initialized early, for use in other initializers.


    private final File startupConfigurationFile = cc().startupConfigurationFile();



   // ------------------------------------------------------------------------------------


    /** A brief description of the allowable content of this register's
      * {@linkplain Registration#getLink() link attribute}, in sentence form.
      *
      *     @see ConstructionContext#setAttributeDescription_link(String)
      */
    public @ThreadSafe String attributeDescription_link() { return attributeDescription_link; }


        private final String attributeDescription_link = cc().getAttributeDescription_link();


        /** An example of the allowable content for this register's
          * {@linkplain Registration#getLink() link attribute}.
          *
          *     @see ConstructionContext#setAttributeExample_link(String)
          */
        public @ThreadSafe String attributeExample_link() { return attributeExample_link; }


            private final String attributeExample_link = cc().getAttributeExample_link();



    /** A brief description of the allowable content of this register's
      * {@linkplain Registration#getName() name attribute}, in sentence form.
      *
      *     @see ConstructionContext#setAttributeDescription_name(String)
      */
    public @ThreadSafe String attributeDescription_name() { return attributeDescription_name; }


        private final String attributeDescription_name = cc().getAttributeDescription_name();


        /** An example of the allowable content for this register's
          * {@linkplain Registration#getName() name attribute}.
          *
          *     @see ConstructionContext#setAttributeExample_name(String)
          */
        public @ThreadSafe String attributeExample_name() { return attributeExample_name; }


            private final String attributeExample_name = cc().getAttributeExample_name();



    /** A brief description of the allowable content of this register's
      * {@linkplain Registration#getNote() note attribute}, in sentence form.
      *
      *     @see ConstructionContext#setAttributeDescription_note(String)
      */
    public @ThreadSafe String attributeDescription_note() { return attributeDescription_note; }


        private final String attributeDescription_note = cc().getAttributeDescription_note();


        /** An example of the allowable content for this register's
          * {@linkplain Registration#getNote() note attribute}.
          *
          *     @see ConstructionContext#setAttributeExample_note(String)
          */
        public @ThreadSafe String attributeExample_note() { return attributeExample_note; }


            private final String attributeExample_note = cc().getAttributeExample_note();



    /** A brief description of the allowable content of this register's
      * {@linkplain Registration#getResidence() residence attribute}, in sentence form.
      *
      *     @see ConstructionContext#setAttributeDescription_residence(String)
      */
    public @ThreadSafe String attributeDescription_residence() { return attributeDescription_residence; }


        private final String attributeDescription_residence = cc().getAttributeDescription_residence();


        /** An example of the allowable content for this register's
          * {@linkplain Registration#getResidence() residence attribute}.
          *
          *     @see ConstructionContext#setAttributeExample_residence(String)
          */
        public @ThreadSafe String attributeExample_residence() { return attributeExample_residence; }


            private final String attributeExample_residence = cc().getAttributeExample_residence();



    /** The relational store of doubt signals, backing this register's doubt signaling
      * network.  It is a table named "doubt", stored in the output database.
      *
      *     @see votorola.a.Pollserver.Run#outputDatabase()
      */
    public @ThreadSafe DoubtSignal.Table doubtTable() { return doubtTable; } // not present in super-registers, when those are implemented


        private final DoubtSignal.Table doubtTable =
          new DoubtSignal.Table( pollserverRun.outputDatabase() );



    /** The geocoding method, for conversion of addresses to cartographic coordinates
      * in the {@linkplain #runtimeConfigurationScript() runtime configuration script}.
      *
      *     @return geocoding method, or null if none is used
      *
      *     @see ConstructionContext#setGeocodingMethodGoogle(String)
      */
    public @ThreadSafe Geocode.GoogleGeocoding geocodingMethod() { return geocodingMethod; }


        private final Geocode.GoogleGeocoding geocodingMethod = cc().getGeocodingMethod();



    /** Retrieves a voter's list node from a compiled voter list.  This is a convenience
      * method.
      *
      *     @param list the voter list to use; or null, to lookup the currently
      *       {@linkplain #listToReport() reported list} (which involves locking overhead
      *       in threaded runs)
      *
      *     @return ListNodeC or ListNodeIC
      *       per {@linkplain ListNodeC.Table#getOrCreate(String) getOrCreate}(voterEmail);
      *       or, if lookup of the list itself fails, a ListNode0 with default values
      */
    public @ThreadSafe ListNode getListNode( VoterList list, final String voterEmail )
      throws IOException, SQLException
    {
        if( list == null )
        {
            lock().lock();
            try { list = listToReport(); }
            finally { lock().unlock(); }
        }
        final ListNode listNode;
        if( list == null ) listNode = new ListNode0( voterEmail );
        else listNode = list.listNodeTable().getOrCreate( voterEmail );
        return listNode;
    }



    /** The summary description for newly compiled voter lists.
      *
      *     @see VoterList#summaryDescription()
      *     @see ConstructionContext#setListSummaryDescription(String)
      */
    public @ThreadSafe String listSummaryDescription() { return listSummaryDescription; }


        private final String listSummaryDescription = cc().getListSummaryDescription();



    /** The current voter list to report, if any.
      *
      *     @return voter list, or null if none to report
      *
      *     @see #readyToReportLink()
      */
    public VoterList listToReport() throws IOException, SQLException
    {
        assert lock.isHeldByCurrentThread();
        ReadyDirectory readyDirectory = null; // so far
        if( readyToReportLink.exists() )
        {
            readyDirectory = new ReadyDirectory( readyToReportLink.getCanonicalPath() );
        }

        if( readyDirectory == null )
        {
            if( listToReport != null )
            {
                logger.info( readyToReportLink + ": link is lost, stopping report: " + listToReport.readyDirectory() );
                listToReport = null;
            }
            return listToReport;
        }

        if( !readyDirectory.isMounted() )
        {
            logger.warning( readyToReportLink + ": list not mounted: " + readyDirectory );
            listToReport = null;
            return listToReport;
        }

        if( listToReport == null || !listToReport.isObjectReadFromSerialFile( readyDirectory ))
        {
            logger.info( "starting new list report: " + readyDirectory );
            listToReport = VoterList.readObjectFromSerialFile( readyDirectory );
            final Database d = pollserverRun.outputDatabase();
            listToReport.init( new ListNodeC.Table( readyDirectory, d ),
              new Neighbourhood.Table( readyDirectory, d ),
              new TrustEdge.Table( readyDirectory, d ));
        }

        return listToReport;
    }


        private VoterList listToReport; // lazilly set/reset through listToReport()



    /** The population base, if known.  This is an estimate of the number of people who
      * eligible to register for voting in one or more polls.  Depending on the runtime
      * configuration of the voter list, the population base might be equal to the
      * population of the site region, or it might be something less.
      *
      *     @return estimated population base, or zero if unknown
      *
      *     @see ConstructionContext#setPopulationSize(long)
      *     @see ListIndexingContext
      */
    public @ThreadSafe long populationSize() { return populationSize; }


        private final long populationSize = cc().getPopulationSize();



    /** An explanation of the population base, intended for the information of users.  For
      * example: "population of city from 2008 census".
      *
      *     @see ConstructionContext#setPopulationSizeExplanation(String)
      */
    public @ThreadSafe String populationSizeExplanation() { return populationSizeExplanation; }


        private final String populationSizeExplanation = cc().getPopulationSizeExplanation();



    /** The minimum level of trust that a registrant must have, in order to pre-register
      * another registrant.
      *
      *     @see Registration#isWriteableAll()
      *     @see ConstructionContext#setPreRegistrationTrustLevel(int)
      */
    public @ThreadSafe int preRegistrationTrustLevel() { return preRegistrationTrustLevel; }


        private final int preRegistrationTrustLevel = cc().getPreRegistrationTrustLevel();



    /** The configured list of primary trust edges, for the neighbourhood trust network.
      *
      *     @return unmodifiable list of edges
      *
      *     @see ConstructionContext#addPrimaryTrust(String,int)
      */
    java.util.List<TrustEdge.Primary> primaryTrustList() { return primaryTrustList; }


        private final ArrayListU<TrustEdge.Primary> primaryTrustList =
            new ArrayListU<TrustEdge.Primary>( cc().getPrimaryTrustArray() );



    /** The symbolic link to the ready directory of the current voter list to report, if
      * any.
      *
      *     @return abstract path (never null) of symbolic link
      *
      *     @see #listToReport()
      *     @see ReadyDirectory#readyToReportLink(Pollserver)
      */
    public @ThreadSafe File readyToReportLink() { return readyToReportLink; }


        private final File readyToReportLink = ReadyDirectory.readyToReportLink(
          pollserverRun.pollserver() );



    /** The runtime configuration file for the register.  The language is JavaScript.
      * There are restrictions on the {@linkplain votorola.g.script.JavaScriptIncluder
      * character encoding}.
      *
      *     @see <a href='../../../../../a/register/register-run.js'
      *                                            >register-run.js (default example script)</a>
      *     @see <a href='../../../../../s/manual.xht#register-run.js'
      *                                >../manual.xht#register-run.js</a>
      */
      @Warning( "thread restricted object" )
    public JavaScriptIncluder runtimeConfigurationScript()
    {
        assert lock.isHeldByCurrentThread(); // this method is safe, but not the object
        return runtimeConfigurationScript;
    }


        private final JavaScriptIncluder runtimeConfigurationScript = new JavaScriptIncluder(
          new File( serviceDirectory(), "register-run.js" ));



   // - V o t e r - S e r v i c e --------------------------------------------------------


    public @Override Exception dispatch( final String[] argArray,
      final CommandResponder.Session commandSession )
    {
        voterInputTable.database().logAndClearWarnings();
        try{ return super.dispatch( argArray, commandSession ); }
        finally{ voterInputTable.database().logAndClearWarnings(); }
    }



    /** @see <a href='../../../../../s/manual.xht#register.js'
      *                            >../manual.xht#register.js</a>
      */
    public @ThreadSafe @Override File startupConfigurationFile()
    {
        return startupConfigurationFile;
    }



    /** @see ConstructionContext#setSummaryDescription(String)
      */
    public @ThreadSafe @Override String summaryDescription() { return summaryDescription; }


        private final String summaryDescription = cc().getSummaryDescription();



    /** Title of this voter register, in wiki-style title case (leading letter only).
      *
      *     @see ConstructionContext#setTitle(String)
      */
    public @ThreadSafe @Override String title() { return title; }


        private final String title = cc().getTitle();



   // - I n p u t - S t o r e ------------------------------------------------------------


    public @ThreadSafe @Override InputTable voterInputTable() { return voterInputTable; }


        private final InputTable voterInputTable = new InputTable( Register.this );
        {
            voterInputTable.init();
        }



   // ====================================================================================


    /** A context for configuring a {@linkplain Register register}.  Each register is
      * configured by its {@linkplain Register#startupConfigurationFile startup configuration file},
      * which contains a script (s) for that purpose.  During construction of the
      * register, an instance of this context (regCC) is passed to s, via
      * s::contructingRegister(regCC).
      */
    public static @ThreadSafe final class ConstructionContext
      extends VoterService.UIdentified.ConstructionContext
    {


        private ConstructionContext( final Pollserver pollserver, final JavaScriptIncluder s )
        {
            super( s.scriptFile().getParentFile().getName(), s );

            setListSummaryDescription( "This is the current voter list, "
              + "organized by neighbourhoods.  "
              + "Further information is unavailable, because the 'contructingRegister' function "
              + "of script " + startupConfigurationFile() + " "
              + "makes no call to 'setListSummaryDescription'." );
            setSummaryDescription( "This is the voter register.  "
              + "Further information is unavailable, because the 'contructingRegister' function "
              + "of script " + startupConfigurationFile() + " "
              + "makes no call to 'setSummaryDescription'." );

            final BundleFormatter bun = new BundleFormatter(
              ResourceBundle.getBundle( "votorola.a.locale.A", Locale.getDefault() ));
            setAttributeDescription_link( bun.l( "a.register.Register.attributeDescription_link" ));
                setAttributeExample_link( bun.l( "a.register.Register.attributeExample_link" ));
            setAttributeDescription_name( bun.l( "a.register.Register.attributeDescription_name" ));
                setAttributeExample_name( bun.l( "a.register.Register.attributeExample_name" ));
            setAttributeDescription_note( bun.l( "a.register.Register.attributeDescription_note" ));
                setAttributeExample_note( bun.l( "a.register.Register.attributeExample_note" ));
            setAttributeDescription_residence( bun.l( "a.register.Register.attributeDescription_residence" ));
                setAttributeExample_residence( bun.l( "a.register.Register.attributeExample_residence" ));
        }



       // --------------------------------------------------------------------------------


        /** @see Register#populationSize()
          * @see #setPopulationSize(long)
          */
        public long getPopulationSize() { return populationSize; }


            private long populationSize;


            /** Sets the estimated size of the population.  The default value is zero,
              * meaning unknown.
              *
              *     @see Register#populationSize()
              */
              @ThreadRestricted("constructor")
            public void setPopulationSize( long populationSize )
            {
                this.populationSize = populationSize;
            }



        /** @see Register#populationSizeExplanation()
          * @see #setPopulationSizeExplanation(String)
          */
        public String getPopulationSizeExplanation() { return populationSizeExplanation; }


            private String populationSizeExplanation = "number of potential registrants";


            /** Sets the population size explanation.  The default value is
              * "number of potential registrants".
              *
              *     @see Register#populationSizeExplanation()
              */
              @ThreadRestricted("constructor")
            public void setPopulationSizeExplanation( String populationSizeExplanation )
            {
                this.populationSizeExplanation = populationSizeExplanation;
            }



        /** @see Register#attributeDescription_link()
          * @see #setAttributeDescription_link(String)
          */
        public String getAttributeDescription_link() { return attributeDescription_link; }


            private String attributeDescription_link;


            /** Sets the description of the link attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeDescription_link()
              */
                @ThreadRestricted("constructor")
            public void setAttributeDescription_link( String attributeDescription_link )
            {
                if( attributeDescription_link.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeDescription_link = attributeDescription_link;
            }


            /** @see Register#attributeExample_link()
              * @see #setAttributeExample_link(String)
              */
            public String getAttributeExample_link() { return attributeExample_link; }


            private String attributeExample_link;


            /** Sets the description of the link attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeExample_link()
              */
                @ThreadRestricted("constructor")
            public void setAttributeExample_link( String attributeExample_link )
            {
                if( attributeExample_link.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeExample_link = attributeExample_link;
            }



        /** @see Register#attributeDescription_name()
          * @see #setAttributeDescription_name(String)
          */
        public String getAttributeDescription_name() { return attributeDescription_name; }


            private String attributeDescription_name;


            /** Sets the description of the name attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeDescription_name()
              */
                @ThreadRestricted("constructor")
            public void setAttributeDescription_name( String attributeDescription_name )
            {
                if( attributeDescription_name.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeDescription_name = attributeDescription_name;
            }


            /** @see Register#attributeExample_name()
              * @see #setAttributeExample_name(String)
              */
            public String getAttributeExample_name() { return attributeExample_name; }


            private String attributeExample_name;


            /** Sets the description of the name attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeExample_name()
              */
                @ThreadRestricted("constructor")
            public void setAttributeExample_name( String attributeExample_name )
            {
                if( attributeExample_name.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeExample_name = attributeExample_name;
            }



        /** @see Register#attributeDescription_note()
          * @see #setAttributeDescription_note(String)
          */
        public String getAttributeDescription_note() { return attributeDescription_note; }


            private String attributeDescription_note;


            /** Sets the description of the note attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeDescription_note()
              */
                @ThreadRestricted("constructor")
            public void setAttributeDescription_note( String attributeDescription_note )
            {
                if( attributeDescription_note.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeDescription_note = attributeDescription_note;
            }


            /** @see Register#attributeExample_note()
              * @see #setAttributeExample_note(String)
              */
            public String getAttributeExample_note() { return attributeExample_note; }


            private String attributeExample_note;


            /** Sets the description of the note attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeExample_note()
              */
                @ThreadRestricted("constructor")
            public void setAttributeExample_note( String attributeExample_note )
            {
                if( attributeExample_note.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeExample_note = attributeExample_note;
            }



        /** @see Register#attributeDescription_residence()
          * @see #setAttributeDescription_residence(String)
          */
        public String getAttributeDescription_residence() { return attributeDescription_residence; }


            private String attributeDescription_residence;


            /** Sets the description of the residence attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeDescription_residence()
              */
                @ThreadRestricted("constructor")
            public void setAttributeDescription_residence( String attributeDescription_residence )
            {
                if( attributeDescription_residence.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeDescription_residence = attributeDescription_residence;
            }


            /** @see Register#attributeExample_residence()
              * @see #setAttributeExample_residence(String)
              */
            public String getAttributeExample_residence() { return attributeExample_residence; }


            private String attributeExample_residence;


            /** Sets the description of the residence attribute.
              * The default value depends on the server's locale.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#attributeExample_residence()
              */
                @ThreadRestricted("constructor")
            public void setAttributeExample_residence( String attributeExample_residence )
            {
                if( attributeExample_residence.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.attributeExample_residence = attributeExample_residence;
            }



        /** @see Register#geocodingMethod()
          * @see #setGeocodingMethodGoogle(String)
          */
        public Geocode.GoogleGeocoding getGeocodingMethod() { return geocodingMethod; }


            private Geocode.GoogleGeocoding geocodingMethod;


            /** Sets the geocoding method of the register
              * to {@linkplain votorola.a.Geocode.GoogleGeocoding GoogleGeocoding}.
              *
              *     @param key per {@linkplain votorola.a.Geocode.GoogleGeocoding#key key}()
              *
              *     @see Register#geocodingMethod()
              */
                @ThreadRestricted("constructor")
            public void setGeocodingMethodGoogle( String key )
            {
                geocodingMethod = new Geocode.GoogleGeocoding( key );
            }



        /** @see VoterList#summaryDescription()
          * @see #setListSummaryDescription(String)
          */
        public @Deprecated String getListSummaryDescription() { return listSummaryDescription; }


            private String listSummaryDescription;


            /** Sets the summary description for newly compiled voter lists.  The default
              * value is a placeholder with configuration instructions for the
              * administrator.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see VoterList#summaryDescription()
              */
                @Deprecated @ThreadRestricted("constructor")
            public void setListSummaryDescription( String listSummaryDescription )
            {
                if( listSummaryDescription.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.listSummaryDescription = listSummaryDescription;
            }



        /** @see Register#preRegistrationTrustLevel()
          * @see #setPreRegistrationTrustLevel(int)
          */
        public int getPreRegistrationTrustLevel() { return preRegistrationTrustLevel; }


            private int preRegistrationTrustLevel;


            /** Sets the pre-registration trust level.  The default value is zero.
              *
              *     @see Register#preRegistrationTrustLevel()
              */
                @ThreadRestricted("constructor")
            public void setPreRegistrationTrustLevel( int preRegistrationTrustLevel )
            {
                this.preRegistrationTrustLevel = preRegistrationTrustLevel;
            }



        /** @see Register#primaryTrustList()
          * @see #addPrimaryTrust(String,int)
          */
        public TrustEdge.Primary[] getPrimaryTrustArray()
        {
            return primaryTrustList.toArray( new TrustEdge.Primary[primaryTrustList.size()] );
        }


            private final ArrayList<TrustEdge.Primary> primaryTrustList
              = new ArrayList<TrustEdge.Primary>();


            /** Adds primary trust edges to the neighbourhood network.
              * By default, there are no primary trust edges (and therefore
              * no trust in the network).
              *
              *     @param voter1Email per
              *       TrustEdge.Primary.{@linkplain votorola.a.register.trust.TrustEdge.Primary#voter1Email voter1Email}()
              *     @param edgeCount per
              *       TrustEdge.Primary.{@linkplain votorola.a.register.trust.TrustEdge.Primary#edgeCount edgeCount}()
              *
              *     @see Register#primaryTrustList()
              */
                @ThreadRestricted("constructor")
            public void addPrimaryTrust( String voter1Email, int edgeCount )
            {
                primaryTrustList.add( new TrustEdge.Primary( voter1Email, edgeCount ));
            }



        /** @see Register#summaryDescription()
          * @see #setSummaryDescription(String)
          */
        public String getSummaryDescription() { return summaryDescription; }


            private String summaryDescription;


            /** Sets the summary description of the register.  The default value is a
              * placeholder with configuration instructions for the administrator.
              *
              *     @throws IllegalArgumentException if the description contains
              *       any newline characters, because they might render inconsistently
              *       across different types of user interface
              *     @see Register#summaryDescription()
              */
                @ThreadRestricted("constructor")
            public void setSummaryDescription( String summaryDescription )
            {
                if( summaryDescription.indexOf('\n') >= 0 ) throw new IllegalArgumentException( "argument contains a newline character" );

                this.summaryDescription = summaryDescription;
            }



        /** @see Register#title()
          * @see #setTitle(String)
          */
        public String getTitle() { return title; }


            private String title = "Voter register";


            /** Sets the title of the register.  The default value is "Voter register".
              *
              *     @see Register#title()
              */
                @ThreadRestricted("constructor")
            public void setTitle( String title ) { this.title = title; }


    }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private static final Logger logger = LoggerX.i( Register.class );



}

