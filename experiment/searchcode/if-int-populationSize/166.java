package votorola.a.poll; // Copyright 2007-2009, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Votorola Software"), to deal in the Votorola Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicence, and/or sell copies of the Votorola Software, and to permit persons to whom the Votorola Software is furnished to do so, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Votorola Software. THE VOTOROLA SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE VOTOROLA SOFTWARE OR THE USE OR OTHER DEALINGS IN THE VOTOROLA SOFTWARE.

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import java.sql.SQLException;
import javax.script.*;
import votorola._.*;
import votorola.a.*;
import votorola.g.locale.*;
import votorola.a.poll.district.*;
import votorola.a.register.*;
import votorola.a.response.*;
import votorola.a.voter.*;
import votorola.g.io.*;
import votorola.g.lang.*;
import votorola.g.logging.LoggerX;
import votorola.g.script.*;
import votorola.g.sql.*;
import votorola.g.util.*;


/** A poll provided as a voter service.  A poll is a collection of votes that are stored
  * in a public repository, from which vote counts are generated.  The name of the poll is
  * guaranteed to end with {@linkplain #P_SUFFIX P_SUFFIX}.
  *
  *     @see <a href='../../../../../s/manual.xht#Polls'
  *                           >../../s/manual.xht#Polls</a>
  */
  @ThreadRestricted("holds lock()")
public final class Poll extends VoterService.UIdentified implements InputStore
{

    // cf. a/register/Register


    private Poll( final Pollserver.Run run, final ConstructionContext cc )
      throws IOException, ScriptException, SQLException
    {
        super( run, cc );
        {
            ArrayList<CommandResponder> responderList = new ArrayList<CommandResponder>();
            responderList.add( new CR_Unvote( Poll.this ));
            responderList.add( new CR_Vote( Poll.this ));
            init( responderList );

            constructionContext = null; // done with it, free the memory
        }

        leaderTryReconfigure();
          // Eagerly init leader-configured fields, in order to avoid null pointers from
          // uninitialized values.  Can't do it lazily, because test/set logic is
          // non-atomic/non-blocking (for speed).
        leaderTryReconfigure_isNeededA.set( false );
    }



    private ConstructionContext cc() { return (ConstructionContext)constructionContext; } // nulled after init



   // ````````````````````````````````````````````````````````````````````````````````````
   // Initialized early, for use in other initializers.


    private final File startupConfigurationFile = cc().startupConfigurationFile();



   // ------------------------------------------------------------------------------------


    /** The current count to report, if any.
      *
      *     @return count, or null if none to report
      */
    public Count countToReport() throws IOException, SQLException
    {
        assert lock.isHeldByCurrentThread();
        ReadyDirectory readyDirectory = null; // so far
        if( readyToReportLink.exists() )
        {
            readyDirectory = new ReadyDirectory( readyToReportLink.getCanonicalPath() );
        }

        if( readyDirectory == null )
        {
            if( countToReport != null )
            {
                logger.info( readyToReportLink + ": link is lost, stopping report: " + countToReport.readyDirectory() );
                countToReport = null;
            }
            return countToReport;
        }

        if( !readyDirectory.isMounted() )
        {
            logger.warning( readyToReportLink + ": count not mounted: " + readyDirectory );
            countToReport = null;
            return countToReport;
        }

        if( countToReport == null || !countToReport.isObjectReadFromSerialFile( readyDirectory ))
        {
            logger.info( "starting new count report: " + readyDirectory + " (" + name + ")" );
            try
            {
                countToReport = Count.readObjectFromSerialFile( name, readyDirectory );
            }
            catch( FileNotFoundException x )
            {
                logger.warning( "excluded from count: " + readyDirectory + " (" + name + ")" );
                countToReport = null;
                return countToReport;
            }

            countToReport.init( new CountTable( readyDirectory, pollserverRun.outputDatabase() ));
            leaderTryReconfigure_isNeededA.set( true );
        }

        return countToReport;
    }


        private Count countToReport; // lazilly set/reset through countToReport()



    /** The divisional node for this poll.
      */
    public DivisionalPollNode divisionalNode() { return divisionalNode; }


        private final DivisionalPollNode divisionalNode =
          new DivisionalPollNode( pollserverRun, cc() );



    /** The issue of this poll.  The issue is leader configured.
      *
      *     @see <a href='../../../../../s/manual.xht#leader-config'
      *                                >../manual.xht#leader-config</a>
      *     @see <a href='../../../../../a/poll/Leader.xsd'>Leader.xsd (Poll/issue)</a>
      */
    public @ThreadSafe Issue issue()
    {
        if( leaderTryReconfigure_isNeededA.getAndSet( false )) leaderTryReconfigure();

        return issue;
    }


        private volatile Issue issue;


        private @ThreadSafe void setIssue( final LeaderConfiguration leaderConfigOrNull )
        {
            issue = Issue.forLeaderConfiguration( leaderConfigOrNull );
        }



    /** Either constructs an event to record the change that occured between oldVote and
      * newVote; or returns null, if no significant change occured.
      */
    public ActivityEvent newChangeEventOrNull( Vote oldVote, Vote newVote )
    {
        assert oldVote.voterEmail().equals( newVote.voterEmail() );
        ActivityEvent event = null;
        if( newVote.getCandidateEmail() == null )
        {
            if( oldVote.getCandidateEmail() != null )
            {
                event = new Vote.WithdrawalEvent( Poll.this, oldVote );
            }
        }
        else if( !newVote.getCandidateEmail().equals( oldVote.getCandidateEmail() ))
        {
            event = new Vote.CastEvent( Poll.this, newVote );
        }
        return event;
    }



    /** The standard suffix for all poll names.
      *
      *     @see #name()
      *     @see #pName()
      */
    public static final String P_SUFFIX = "-p";



    /** The p-name of this poll.  The p-name is the name without the P_SUFFIX.  It is
      * unique among all polls of the pollserver, and it never changes.
      *
      * <p>As a rule, the p-name is used only for aesthetic reasons, and only in the
      * end-user interface.  The administrative interface and internal code references
      * always use the full service name.</p>
      *
      *     @see #name()
      *     @see #pName(String)
      */
    public @ThreadSafe String pName() { return pName( name ); }



    /** The p-name of a poll.  The p-name is the name without the P_SUFFIX.  It is
      * unique among all polls of the pollserver, and it never changes.
      *
      * <p>As a rule, the p-name is used only for aesthetic reasons, and only in the
      * end-user interface.  The administrative interface and internal code references
      * always use the full service name.</p>
      *
      *     @param name the full service name of the poll, which is assumed to end
      *       with the P_SUFFIX
      *     @return the name trucated by the length of P_SUFFIX
      *
      *     @see #name()
      *     @see #pName()
      */
    public @ThreadSafe static String pName( final String name )
    {
        assert name.endsWith( P_SUFFIX );
        return name.substring( 0, name.length() - P_SUFFIX.length() );
    }



    /** The population base of this poll, if known.  This an estimate of the number of
      * people who are eligible to vote in the poll.  Depending on the runtime
      * configuration of the poll, the population base might be equal to the population of
      * the surrounding district, or it might be something less.
      *
      *     @return estimated population base, or zero if unknown
      *
      *     @see ConstructionContext#setPopulationSize(long)
      *     @see District#populationSize()
      */
    public @ThreadSafe long populationSize() { return populationSize; }


        private final long populationSize = cc().getPopulationSize();



    /** An explanation of the population base, intended for the information of users.  For
      * example: "population of Ward 19 from 2008 census", or "neighbourhood of Trinity
      * Bellwoods from 2008 census".
      *
      *     @see ConstructionContext#setPopulationSizeExplanation(String)
      */
    public @ThreadSafe String populationSizeExplanation() { return populationSizeExplanation; }


        private final String populationSizeExplanation = cc().getPopulationSizeExplanation();



    /** The runtime configuration file for the register.  The language is JavaScript.
      * There are restrictions on the {@linkplain votorola.g.script.JavaScriptIncluder
      * character encoding}.
      *
      *     @see <a href='../../../../../a/poll/poll-run.js'
      *                                        >poll-run.js (default example script)</a>
      *     @see <a href='../../../../../s/manual.xht#poll-run.js'
      *                                >../manual.xht#poll-run.js</a>
      */
      @Warning( "thread restricted object" )
    JavaScriptIncluder runtimeConfigurationScript()
    {
        assert lock.isHeldByCurrentThread(); // this method is safe, but not the object
        return runtimeConfigurationScript;
    }


        private final JavaScriptIncluder runtimeConfigurationScript =
          new JavaScriptIncluder( new File( serviceDirectory(), "poll-run.js" ));



    /** The standard base identifier for RDF applications, in URI format.
      */
    static final String SEMANTIC_BASE_ID = "http://zelea.com/project/votorola/a/poll/";



   // - V o t e r - S e r v i c e --------------------------------------------------------


    public @Override Exception dispatch( final String[] argArray,
      final CommandResponder.Session commandSession )
    {
        voterInputTable.database().logAndClearWarnings();
        try{ return super.dispatch( argArray, commandSession ); }
        finally{ voterInputTable.database().logAndClearWarnings(); }
    }



    /** @see <a href='../../../../../s/manual.xht#poll.js'
      *                            >../manual.xht#poll.js</a>
      */
    public @ThreadSafe @Override File startupConfigurationFile()
    {
        return startupConfigurationFile;
    }



    /** A brief description of this poll, up to a few sentences in length.  The
      * description is leader configured.  The default value is the empty string "".
      *
      *     @see #summaryDescription(BundleFormatter)
      *     @see <a href='../../../../../s/manual.xht#leader-config'
      *                                >../manual.xht#leader-config</a>
      *     @see <a href='../../../../../a/poll/Leader.xsd'>Leader.xsd (Poll/summaryDescription)</a>
      */
    public @ThreadSafe @Override String summaryDescription()
    {
        if( leaderTryReconfigure_isNeededA.getAndSet( false )) leaderTryReconfigure();

        return summaryDescription;
    }


        /** Returns either the summary description; or, if there is none, a placeholder
          * with localized configuration instructions.
          */
        public @ThreadSafe String summaryDescription( final BundleFormatter bun )
        {
            final String s = summaryDescription();
            return s.length() > 0? s:
              bun.l( "a.poll.Poll.summaryDescription-default" );
        }


        private volatile String summaryDescription;


        private @ThreadSafe void setSummaryDescription(
          final LeaderConfiguration leaderConfigOrNull )
        {
            String newValue = "";
            if( leaderConfigOrNull != null )
            {
                final Resource i = leaderConfigOrNull.individual();
                final Property p = i.getModel().createProperty(
                  SEMANTIC_BASE_ID, "#summaryDescription" );
                final Statement s = i.getProperty( p ); // using basic RDF queries, but see also setIssue()
                if( s != null )
                {
                    newValue = StringBuilderX.collapseAndTrim(
                      new StringBuilder(s.getString()) ).toString();
                }
            }

            summaryDescription = newValue;
        }



    /** Title of this poll, in wiki-style title case (leading letter only).  The title is
      * leader configured.
      *
      *     @see <a href='../../../../../s/manual.xht#leader-config'
      *                                >../manual.xht#leader-config</a>
      *     @see <a href='../../../../../a/poll/Leader.xsd'>Leader.xsd (Poll/title)</a>
      */
    public @ThreadSafe @Override String title()
    {
        if( leaderTryReconfigure_isNeededA.getAndSet( false )) leaderTryReconfigure();

        return title;
    }


        private volatile String title;


        private @ThreadSafe void setTitle( final LeaderConfiguration leaderConfigOrNull )
        {
            String newValue = "";
            if( leaderConfigOrNull != null )
            {
                final Resource i = leaderConfigOrNull.individual();
                final Property p = i.getModel().createProperty( SEMANTIC_BASE_ID, "#title" );
                final Statement s = i.getProperty( p ); // using basic RDF queries, but see also setIssue()
                if( s != null )
                {
                    newValue = StringBuilderX.collapseAndTrim(
                      new StringBuilder(s.getString()) ).toString();
                }
            }

            if( newValue.length() == 0 ) newValue = name;
            title = newValue;
        }



   // - I n p u t - S t o r e ------------------------------------------------------------


    public @ThreadSafe @Override InputTable voterInputTable() { return voterInputTable; }


        private final InputTable voterInputTable = new InputTable( Poll.this );
        {
            voterInputTable.init();
        }



   // ====================================================================================


    /** A context for configuring a {@linkplain Poll poll}.  Each poll is configured by
      * its {@linkplain Poll#startupConfigurationFile startup configuration file}, which contains a
      * script (s) for that purpose.  During construction of the poll, an instance of this
      * context (pollCC) is passed to s, via s::contructingPoll(pollCC).
      */
    public static @ThreadSafe final class ConstructionContext
      extends VoterService.UIdentified.ConstructionContext
    {


        /** Constructs the complete configuration of the poll, and runs sanity tests on
          * it.
          *
          *     @param name the pollserver name
          *     @param s the compiled startup configuration script
          */
        private static ConstructionContext configure( final Pollserver pollserver,
          final JavaScriptIncluder s ) throws IOException, ScriptException
        {
            final ConstructionContext cc = new ConstructionContext( pollserver, s );
            s.invokeKnownFunction( "contructingPoll", cc );
            cc.verifyWellFormed();
            return cc;
        }



        private ConstructionContext( final Pollserver pollserver, final JavaScriptIncluder s )
          throws IOException
        {
            super( s.scriptFile().getParentFile().getName(), s );
            final String pollPath = pollserver.scopePoll().pollDirectory().getPath();
            setDistrictConfigurationPath( pollPath + File.separator + "district.js" );
        }



       // --------------------------------------------------------------------------------


        /** The canonical path of the district startup configuration file.
          *
          *     @see DivisionalPollNode#district()
          *     @see #setDistrictConfigurationPath(String)
          */
        public String getDistrictConfigurationPath() { return districtConfigurationPath; }


            private String districtConfigurationPath;


            /** Sets the path to the startup configuration file of this poll's district.  It must
              * not contain any shell variable, or tilde ~.  It will be converted to
              * canonical form, if it is non-canonical.  The default value is:
              * <p class='indent'>
              *     ~/votorola/{@linkplain PollserverScope#pollDirectory() poll}/district.js
              *     </p>
              *
              *     @see District#startupConfigurationFile()
              *     @see DivisionalPollNode#district()
              */
              @ThreadRestricted("constructor")
            public void setDistrictConfigurationPath( String districtConfigurationPath )
              throws IOException
            {
                this.districtConfigurationPath =
                  new File( districtConfigurationPath ).getCanonicalPath();
            }



        /** @see Poll#populationSize()
          * @see #setPopulationSize(long)
          */
        public long getPopulationSize() { return populationSize; }


            private long populationSize; // default zero = unknown, so unlikely to divide by it


            /** Sets the population base of this poll.  The default value is zero,
              * meaning unknown.
              *
              *     @see Poll#populationSize()
              */
            @ThreadRestricted("constructor") public void setPopulationSize( long populationSize )
            {
                this.populationSize = populationSize;
            }



        /** @see Poll#populationSizeExplanation()
          * @see #setPopulationSizeExplanation(String)
          */
        public String getPopulationSizeExplanation() { return populationSizeExplanation; }


            private String populationSizeExplanation = "number of potential voters";


            /** Sets the explanation of the population base.  The default value is "number
              * of potential voters".
              *
              *     @see Poll#populationSizeExplanation()
              */
              @ThreadRestricted("constructor")
            public void setPopulationSizeExplanation( String populationSizeExplanation )
            {
                this.populationSizeExplanation = populationSizeExplanation;
            }


    }



   // ====================================================================================


    /** The leader configuration of a poll.
      *
      *     @see <a href='../../../../../a/poll/Leader.xsd'>Leader.xsd (Poll)</a>
      */
    static @ThreadRestricted("constructor") final class LeaderConfiguration
    {

        private LeaderConfiguration( File _file, Individual _individual )
        {
            file = _file;
            individual = _individual;
            constructionThread = Thread.currentThread();
        }


        private final Thread constructionThread;


        /** The configuration file.
          */
        File file() { return file; }


            private final File file;


        /** The instance of Poll, as parsed from the configuration file.
          */
        Individual individual()
        {
            assert Thread.currentThread().equals( constructionThread );
            return individual;
        }


            private final Individual individual;


    }



   // ====================================================================================


    /** API for all polls within the scope of a pollserver.
      *
      *     @see Pollserver#scopePoll()
      */
    public static @ThreadSafe final class PollserverScope
    {


        /** Constructs a PollserverScope.
          */
        public @Warning( "non-API" ) PollserverScope( final Pollserver _pollserver )
        {
            pollserver = _pollserver;
            servicesDirectory = new File( pollserver.votorolaDirectory(),
               "poll" + File.separatorChar + "services" );
        }



        private final Pollserver pollserver;



       // --------------------------------------------------------------------------------


        /** The base poll directory.  It is located at:
          *
          * <p class='indent'>~/{@linkplain Pollserver#votorolaDirectory()
          * votorola}/poll</p>
          */
        public File pollDirectory() { return servicesDirectory.getParentFile(); }



        /** Iterates over the service directories of all polls.
          */
        public IterableIteratorA<File> serviceDirectories()
        {
            return new IterableIteratorA<File>()
            {
                private final File[] directoryArray =
                  FileX.listFilesNoNull( servicesDirectory, FileFilterX.DIR );

                private int d;

               // - I t e r a t o r ------------------------------------------------------

                public boolean hasNext() { return d < directoryArray.length; }

                public File next()
                {
                    if( d >= directoryArray.length ) throw new NoSuchElementException();
                    return directoryArray[d++];
                }

                public void remove() { throw new UnsupportedOperationException(); }
            };
        }



        /** The parent of all poll service directories (In future, multiple service
          * directories might be configured.  But for now, there's just the one.)  It is
          * located at:
          *
          * <p class='indent'>~/{@linkplain Pollserver#votorolaDirectory()
          * votorola}/poll/services</p>
          */
        public File servicesDirectory() { return servicesDirectory; }


            private final File servicesDirectory;



       //  = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =


        /** API for all polls within the scope of a pollserver run.
          *
          *     @see votorola.a.Pollserver.Run#scopePoll()
          */
        public @ThreadSafe final class Run
        {


            /** Constructs a Run.
              */
            public @Warning( "non-API" ) Run( final Pollserver.Run _pollserverRun )
            {
                pollserverRun = _pollserverRun;
            }



            /** @see votorola.a.Pollserver.Run#init_done()
              */
            public @Warning( "non-API" ) void init_done()
            {

              // Classify all polls by division.
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                {
                    final Collection<DivisionalScheme> sList = divisionalSchemeMap.values();
                    final DivisionalScheme[] sArray = new DivisionalScheme[sList.size()];
                    sList.toArray( sArray );
                    Arrays.sort( sArray, DivisionalNode.ShortTitleComparator.i() );
                    divisionalSchemes = new ArrayListU<DivisionalScheme>( sArray );
                }
                final District[] dArray = newDistrictArray();
                s: for( final DivisionalScheme s: divisionalSchemes )
                {
                    final ArrayList<District> sChildren = new ArrayList<District>();
                    d: for( final District d: dArray )
                    {
                        if( !d.divisionalScheme().equals( s )) continue d;

                        final ArrayList<DivisionalPollNode> dChildren =
                          new ArrayList<DivisionalPollNode>();
                        e: for( final VoterService service: pollserverRun.newVoterServiceArray() )
                        {
                            if( !( service instanceof Poll )) continue e;

                            final DivisionalPollNode e = ((Poll)service).divisionalNode();
                            if( !e.district().equals( d )) continue e;

                            dChildren.add( e );
                        }

                        final DivisionalPollNode[] dChildArray =
                          new DivisionalPollNode[dChildren.size()];
                        dChildren.toArray( dChildArray );
                        Arrays.sort( dChildArray, DivisionalNode.ShortTitleComparator.i() );
                        districtDivisionalChildrenMap.put( d.startupConfigurationFile().getPath(),
                          new ArrayListU<DivisionalPollNode>( dChildArray ));

                        sChildren.add( d );
                    }

                    final District[] sChildArray = new District[sChildren.size()];
                    sChildren.toArray( sChildArray );
                    Arrays.sort( sChildArray, DivisionalNode.ShortTitleComparator.i() );
                    divisionalSchemeChildrenMap.put( s.startupConfigurationFile().getPath(),
                      new ArrayListU<District>( sChildArray ));
                }
            }



            /** @see votorola.a.Pollserver.Run#init_ensureAllVoterServices()
              */
            public @Warning( "non-API" ) void init_ensureAllVoterServices()
              throws IOException, ScriptException, SQLException
            {
                for( final File serviceDirectory: serviceDirectories() )
                {
                    if( !serviceDirectory.isDirectory() ) continue;

                    final File startupConfigurationFile = new File( serviceDirectory, "poll.js" );
                    try
                    {
                        pollserverRun.init_ensureVoterService(
                          startupConfigurationFile, Poll.class );
                    }
                    catch( NoSuchServiceException x )
                    {
                        logger.config( "skipping poll service directory '" + serviceDirectory + "' because of: " + x );
                    }
                }
            }



            /** Returns an electoral district, creating it if necessary, and storing it
              * for later retrieval.
              *
              *     @see #district(String)
              */
              @ThreadRestricted("constructor") // so no worry about deadlock involving run and service locks held during atomic get/create/put of new district
            public District init_ensureDistrict( final String name )
              throws IOException, ScriptException
            {
                assert Thread.currentThread().equals( pollserverRun.constructionThreadA.get() );
                District d = district( name );
                if( d != null ) return d;

                d = District.newDistrict( pollserverRun, name );
                assert name.equals( d.name() ) :"path used as key, is canonical";
                districtMap.put( name, d );
                return d;
            }



            /** Returns a divisional scheme, creating it if necessary, and storing it for later
              * retrieval.
              *
              *     @see #divisionalScheme(String)
              */
              @ThreadRestricted("constructor") // so no worry about deadlock involving run and service locks held during atomic get/create/put of new scheme
            public DivisionalScheme init_ensureDivisionalScheme( final String name )
              throws IOException, ScriptException
            {
                assert Thread.currentThread().equals( pollserverRun.constructionThreadA.get() );
                DivisionalScheme s = divisionalScheme( name );
                if( s != null ) return s;

                s = DivisionalScheme.newDivisionalScheme( name );
                assert name.equals( s.name() ) :"path used as key, is canonical";
                divisionalSchemeMap.put( name, s );
                return s;
            }



            /** Returns a poll, creating it if necessary, and storing it for later
              * retrieval.  This is just a convenience wrapper for {@linkplain
              * votorola.a.Pollserver.Run#init_ensureVoterService(File,Class)
              * init_ensureVoterService}(startupConfigurationFile,serviceClass).
              */
              @ThreadRestricted("constructor") // so no worry about deadlock involving run and service locks held during atomic get/create/put of new service
            public Poll init_ensurePoll( final String name )
              throws IOException, ScriptException, SQLException
            {
                final File startupConfigurationFile = new File(
                  servicesDirectory, name + File.separatorChar + "poll.js" );
                return pollserverRun.init_ensureVoterService( startupConfigurationFile, Poll.class );
            }



           // ----------------------------------------------------------------------------


            /** Looks up an electoral district that is active in this run.
              *
              *     @see #init_ensureDistrict(String)
              *     @see #newDistrictArray()
              *     @see #divisionalSchemeChildren(String)
              */
            public synchronized District district( String name )
            {
                return districtMap.get( name );
            }

                  @ThreadRestricted("holds Run.this")
                private final HashMap<String,District> districtMap = new HashMap<String,District>();



            /** Looks up all divisional children (poll nodes) of a district.
              *
              *     @param name the name of the district
              *     @return unmodifiable list of nodes
              *
              *     @see votorola.a.Pollserver.Run#voterService(String)
              *     @throws IllegalStateException if pollserverRun.init_done() not already called
              */
            public List<DivisionalPollNode> districtDivisionalChildren( String name )
            {
                if( pollserverRun.constructionThreadA.get() != null ) throw new IllegalStateException();

                return districtDivisionalChildrenMap.get( name );
            }

                  @ThreadRestricted("readers") // writing restricted to init by constructor thread
                private final HashMap<String,List<DivisionalPollNode>> districtDivisionalChildrenMap =
                  new HashMap<String,List<DivisionalPollNode>>();



            /** Looks up all electoral districts that are active in this run.
              *
              *     @return newly created array of districts
              *
              *     @see #district(String)
              */
            public District[] newDistrictArray()
            {
                Collection<District> values;
                synchronized( Run.this ) { values = districtMap.values(); }
                return values.toArray( new District[values.size()] );
            }



            /** Constructs a Poll.
              *
              *     @param s the compiled startup configuration script
              */
            public Poll newPoll( JavaScriptIncluder s )
              throws IOException, ScriptException, SQLException
            {
                final ConstructionContext cc =
                  ConstructionContext.configure( pollserverRun.pollserver(), s );
                return new Poll( pollserverRun, cc );
            }



            /** The pollserver run.
              */
            public Pollserver.Run pollserverRun() { return pollserverRun; }


                private final Pollserver.Run pollserverRun;



            /** Looks up a divisional scheme that is active in this run.
              *
              *     @see #divisionalSchemes()
              *     @see #init_ensureDivisionalScheme(String)
              */
            public synchronized DivisionalScheme divisionalScheme( String name )
            {
                return divisionalSchemeMap.get( name );
            }

                  @ThreadRestricted("holds Run.this")
                private final HashMap<String,DivisionalScheme> divisionalSchemeMap =
                  new HashMap<String,DivisionalScheme>();



            /** Looks up all children (districts) of a divisional scheme.
              *
              *     @param name the name of the scheme
              *     @return unmodifiable list of districts
              *
              *     @see #district(String)
              *     @throws IllegalStateException if init_done() not already called
              */
            public List<District> divisionalSchemeChildren( String name )
            {
                if( pollserverRun.constructionThreadA.get() != null ) throw new IllegalStateException();

                return divisionalSchemeChildrenMap.get( name );
            }

                  @ThreadRestricted("readers") // writing restricted to init by constructor thread
                private final HashMap<String,List<District>> divisionalSchemeChildrenMap =
                  new HashMap<String,List<District>>();



            /** The list of all divisional schemes that are active in this run.
              *
              *     @return unmodifiable list of schemes
              *
              *     @see #divisionalScheme(String)
              *     @throws IllegalStateException if init_done() not already called
              */
            public List<DivisionalScheme> divisionalSchemes()
            {
                if( pollserverRun.constructionThreadA.get() != null ) throw new IllegalStateException();

                return divisionalSchemes;
            }

                private volatile ArrayListU<DivisionalScheme> divisionalSchemes;



        }
    }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    /** Loads and applies new leader configuration.  Checks the timestamp of the
      * configuration file, and skips it if already loaded.
      */
    private @ThreadSafe void leaderTryReconfigure()
    {
        LeaderConfiguration leaderConfigOrNull =
          readNewLeaderConfiguration( leaderTryReconfigure_lastTimestampA );
        if( leaderConfigOrNull == null )
        {
            leaderConfigOrNull = readNewLeaderConfiguration(
              leaderTryReconfigure_lastTimestampOverrideA );
        }
        if( leaderConfigOrNull == null ) return;

        setIssue( leaderConfigOrNull );
        setSummaryDescription( leaderConfigOrNull );
        setTitle( leaderConfigOrNull );
    }


        private AtomicBoolean leaderTryReconfigure_isNeededA = new AtomicBoolean( true );

        private AtomicLong leaderTryReconfigure_lastTimestampA =
          new AtomicLong( Long.MIN_VALUE );

        private AtomicLong leaderTryReconfigure_lastTimestampOverrideA =
          new AtomicLong( Long.MIN_VALUE );



    private static final Logger logger = LoggerX.i( Poll.class );



    /** @return new leader configuration; or null, if there is none
      */
    private @ThreadSafe LeaderConfiguration readNewLeaderConfiguration(
      final AtomicLong lastTimestampA )
    {
        final File file = new File( serviceDirectory(), "leader"
          + (lastTimestampA==leaderTryReconfigure_lastTimestampOverrideA? "-override": "")
          + ".rdf" );
        if( !file.isFile() ) return null;

        final long newTimestamp = file.lastModified();
        final long oldTimestamp = lastTimestampA.getAndSet( newTimestamp );
        if( newTimestamp == oldTimestamp ) return null;

        final OntModel semantic;
     // semanticBase.enterCriticalSection( /*read-only*/true );
     // try
     // {
     //     semantic = ModelFactory.createOntologyModel(
     //       semanticBase.getSpecification(), semanticBase );
     // }
     // finally { semanticBase.leaveCriticalSection(); }
     //// OPT.  Very slow, even without locking, at least on first use.  Slows command line.  So:
        semantic = ModelFactory.createOntologyModel( semanticBaseSpec );
        semantic.getDocumentManager().setProcessImports( false ); // Imports explicitly read in semanticBase_init().  See also Poll/owl:Ontology/owl:imports in http://zelea.com/project/votorola/a/poll/_/leader-owl.rdf
        semanticBase_init( semantic );

     // semantic.getDocumentManager().setProcessImports( false ); // Imports explicitly read in semanticBase.  See also Poll/owl:Ontology/owl:imports in http://zelea.com/project/votorola/a/poll/_/leader-owl.rdf
     //// "
        semantic.read( file.toURI().toString() );
        final Individual i = semantic.getIndividual( uid );
        if( i == null )
        {
            logger.warning( "not about individual '" + uid + "': " + file );
            return null;
        }

        if( !i.hasOntClass( SEMANTIC_BASE_ID + "#Poll" )) // Not that it matters much.  And even changing the name of the leader.rdf doc element, e.g. to XPoll, only adds XPoll as a class, without removing Poll.  Somehow it deduces that it's a Poll.  (But not by caching.)
        {
            logger.warning( "instance (document element) is not a Poll: " + file );
            return null;
        }

        return new LeaderConfiguration( file, i );
    }



    private final File readyToReportLink =
      ReadyDirectory.readyToReportLink( pollserverRun.pollserver() );



        private static final OntModelSpec semanticBaseSpec =
       // OntModelSpec.OWL_LITE_MEM_TRANS_INF;
       /// fails to resolve superclass hierarchy, as advertised
          OntModelSpec.OWL_LITE_MEM_RULES_INF;

 // private static final OntModel semanticBase = semanticBase_init(
 //     ModelFactory.createOntologyModel( semanticBaseSpec ));



    private static OntModel semanticBase_init( final OntModel semanticBase )
    {
        final FileManager fM = new FileManager( LocationMapper.get() );
        fM.addLocatorClassLoader( Poll.class.getClassLoader() ); // first locator, as OPT, since the semanticBase is actually being reloaded every time
        fM.addLocatorFile();
        fM.addLocatorURL();
        semanticBase.getDocumentManager().setFileManager( fM );
     // final FileManager fM = semanticBase.getDocumentManager().getFileManager();

        final String basePath = "file:votorola/a/poll/"; // for Model.read(), not tested, hopefully it uses FileManager, which knows this is a classloader path
        final InputStream in = fM.open( basePath + "Poll.rdf" ); // from classloader
        try
        {
            semanticBase.read( in, basePath );
        }
        finally{ InputStreamX.runClose( in ); }

        return semanticBase;
    }



}

