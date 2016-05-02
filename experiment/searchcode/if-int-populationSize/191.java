package votorola.a.poll.web; // Copyright 2009-2010, Michael Allan.  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Votorola Software"), to deal in the Votorola Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicence, and/or sell copies of the Votorola Software, and to permit persons to whom the Votorola Software is furnished to do so, subject to the following conditions: The preceding copyright notice and this permission notice shall be included in all copies or substantial portions of the Votorola Software. THE VOTOROLA SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE VOTOROLA SOFTWARE OR THE USE OR OTHER DEALINGS IN THE VOTOROLA SOFTWARE.

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Date; // over java.sql.Date
import javax.script.*;
import org.apache.wicket.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.basic.*;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.model.*;
import org.apache.wicket.protocol.http.*;
import votorola._.*;
import votorola.a.poll.*;
import votorola.a.poll.district.*;
import votorola.a.voter.*;
import votorola.a.web.*;
import votorola.a.web.authen.*;
import votorola.g.lang.*;
import votorola.g.locale.*;
import votorola.g.mail.*;
import votorola.g.servlet.wic.*;
import votorola.g.text.*;
import votorola.g.util.*;

import static votorola.a.register.Registration.NOBODY;
import static votorola.a.voter.IDPair.NOBODY_ID;


/** A navigable overview of a poll in cascading table form, complete with results
  * summaries.  The poll is specified by query parameter 'p'.  Query parameters for this
  * page are:
  *
  * <table class='definition' style='margin-left:1em'>
  *     <tr>
  *         <th class='key'>Key</th>
  *         <th>Value</th>
  *         <th>Default</th>
  *         </tr>
  *     <tr><td class='key'>clean</td>
  *
  *         <td>Whether to display the page cleanly.  A value of 'y' eliminates some of
  *         the more complicated navigation controls, while 'n' displays the page as
  *         usual.  This is just a temporary hack.  Soon the register and districts will
  *         be moved out of the pollserver, and the navigation controls with them.</td>
  *
  *         <td>'n'</td>
  *
  *         </tr>
  *     <tr><td class='key'>p</td>
  *
  *         <td>The poll.  It is specified by its {@linkplain Poll#pName() p-name}.</td>
  *
  *         <td>Null, specifying no particular poll.</td>
  *
  *         </tr>
  *     <tr><td class='key'>u</td>
  *
  *         <td>{@linkplain IDPair#username() Username} of the voter at the top of the
  *         vote path.  Incompatible with parameter 'v'; specify one or the other.</td>
  *
  *         <td>Null, specifying no particular voter.</td>
  *
  *         </tr>
  *     <tr><td class='key'>v</td>
  *
  *         <td>{@linkplain IDPair#email() Email address} of the voter at the top of the
  *         vote path.  Incompatible with parameter 'u'; specify one or the other.</td>
  *
  *         <td>Null, specifying no particular voter.</td>
  *
  *         </tr>
  *     <tr><td class='key'>vCor</td>
  *
  *         <td>Whether to correct the results for any vote shift of the voter's since the
  *         last reported count.  A value of 'y' corrects the results, while 'n' leaves
  *         them uncorrected.</td>
  *
  *         <td>'y'</td>
  *
  *         </tr>
  *     </table>
  *
  *     @see <a href='../../../../../../a/poll/web/WP_Pollspace.html'>WP_Pollspace.html</a>
  */
  @ThreadRestricted("wicket")
public final class WP_Pollspace extends DivisionalPage1 implements TabbedPage, VoterPage // public - bookmarkable page
{


    /** Constructs a WP_Pollspace.
      */
    public WP_Pollspace( final PageParameters parameters ) // public - bookmarkable page, must have default|PageParameter constructor
    {
        super( parameters );
        final VRequestCycle cycle = VRequestCycle.get();

        final Poll pollOrNull = WP_Poll.pollOrNullFor( parameters, cycle );
        final WP_Poll.DivisionalPathMPP divisionalPathMPP = // first, as it writes path to session
          new WP_Poll.DivisionalPathMPP( pollOrNull, WP_Pollspace.this, cycle );

        add( new WC_NavigationHead( "navHead", WP_Pollspace.this, cycle ));
        final String clean = getPageParameters().getString( "clean", "n" );
        if( "n".equals( clean ))
        {
            add( new WC_NavPile( "navPile", navTab(cycle), cycle ));
            add( new WC_DivisionalPathNavigator( "navPath", divisionalPathMPP,
              DivisionalStratum.POLL, cycle ));
        }
        else if( "y".equals( clean ))
        {
            add( newNullComponent( "navPile" ));
            add( newNullComponent( "navPath" ));
        }
        else
        {
            cycle.vSession().error( "improper value for page parameter 'clean': " + clean );
            throw new RestartResponseException( new WP_Message() );
        }
        divisionalPath = DivisionalPath1.newPath( divisionalPathMPP ); // immutable

        voterIDPair = VoterPage.X.idPairOrNobodyFor( parameters, cycle );
        cycle.vSession().scopeVoterNavigator().setLastIDPair( voterIDPair ); // in case user wanders off to a voter page
        try
        {
            init_content( pollOrNull, cycle );
        }
        catch( Exception x ) { throw VotorolaRuntimeException.castOrWrapped( x ); } // not much expected
    }



    private void init_content( final Poll pollOrNull, final VRequestCycle cycle )
      throws IOException, ScriptException, SQLException
    {

      // POLL AND TITLING
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        final BundleFormatter bunW = cycle.bunW();
        final Model titleModel = new Model();
        add( new Label( "title", titleModel ));
        if( pollOrNull == null )
        {
            add( newNullComponent( "contentPoll" ));
            titleModel.setObject( bunW.l( "a.poll.navPath.promptTitle" ) );
            return;
        }

        final Poll poll = pollOrNull;
        final Fragment yPoll = newBodyOnlyFragment( "contentPoll", "contentPollFrag",
          WP_Pollspace.this );
        add( yPoll );

        titleModel.setObject( poll.title() ); // so far
        final Model hModel = new Model( poll.title() );
        yPoll.add( new Label( "h", hModel ));

      // CANDIDATE NAVIGATION AND VOTING CONTROLS
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        final CorrectableCount count;
        {
            final Count c;
            poll.lock().lock();
            try { c = poll.countToReport(); }
            catch( IOException x ) { throw new RuntimeException( x ); } // not much expected
            finally { poll.lock().unlock(); }

            count = c == null? null: new CorrectableCount( c );
        }

        final BundleFormatter bunA = cycle.bunA();
        final String nobodyString = bunA.l( "a.poll.nobodyEmailPlaceholder" );
        final String userEmail = cycle.vSession().userEmail();
        final boolean isVotingEnabled = userEmail != null;
        currentVote = isVotingEnabled? new Vote( userEmail, poll.voterInputTable() ):
          new Vote( NOBODY.voterEmail() );
        newVote = currentVote.clone();
        {
            final IDPair candidate;
            if( voterEmail().equals( userEmail ))
            {
                final String email = currentVote.getCandidateEmail();
                candidate = email == null? NOBODY_ID: new IDPair( email, IDPair.toUsername(email),
                  /*isFromEmail*/voterIDPair.isFromEmail() ); // force it to the same form as specified for the voter
            }
            else if( NOBODY_ID.equalsEmail( voterIDPair )) candidate = NOBODY_ID;
            else candidate = voterIDPair;
            setNewCandidate( candidate ); // and thence newVote
        }

        final CandidateForm candidateForm = new CandidateForm();
        yPoll.add( candidateForm );
        {
            candidateForm.add( new Label( "hPollwiki",
              bunW.l( "a.poll.web.WP_Pollspace.hPollwiki" )).setRenderBodyOnly( true ));
            final Model m = new Model(
              poll.pollserverRun().pollserver().wikiLocation().toString() + "P/" + poll.pName() );
            candidateForm.add( new ExternalLink( "hPollwikiA", /*href*/m, /*label*/m ));
        }
        {
            final TextField field = new TextField( "otherUID" );
            candidateForm.add( field );

            field.setModel( new PropertyModel( WP_Pollspace.this, "newCandidate" )
            {
                public @Override Object getObject()
                {
                    Object o = super.getObject();
                    return IDPair.NOBODY_ID.equalsEmail(o)? null: o;
                }
                public @Override void setObject( final Object o )
                {
                    super.setObject( o == null? IDPair.NOBODY_ID: o );
                }
            });
            invalidStyled( field );
            IDPairConverter.setMaxLength_Type( field );

            if( isVotingEnabled ) candidateForm.add( newNullComponent( "loginLink" ));
            else
            {
                final WC_LoginLink link = new WC_LoginLink( "loginLink",
                  bunW.l( "a.poll.web.WP_Pollspace.login" ));
                candidateForm.add( link );

                field.setEnabled( count != null ); // no need of field, if all the buttons are disabled
            }
        }
        {
            final Button button = new Button( "go" );
            button.add( new SimpleAttributeModifier( "value",
              bunW.l( "a.poll.web.WP_Pollspace.candidateGo" )));
            candidateForm.add( button );

            button.setEnabled( count != null ); // no point in navigating anywhere, there is no view
        }
        {
            final Button button = new Button( "vote" );
            button.add( new SimpleAttributeModifier( "value",
              bunW.l( "a.poll.web.WP_Pollspace.candidateVote" )));
            button.setEnabled( isVotingEnabled );
            candidateForm.add( button );
        }
        {
            final Button button = new Button( "unvote" );
            button.add( new SimpleAttributeModifier( "value",
              bunW.l( "a.poll.web.WP_Pollspace.candidateUnvote" )));
            button.setEnabled( isVotingEnabled && currentVote.getCandidateEmail() != null );
            candidateForm.add( button );
        }

      // Feedback Messages
      // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        candidateForm.add( new WC_Feedback( "feedback" ));

      // COUNT
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        final Fragment yCount;
        final MarkupContainer candidateDetail;
        if( count == null )
        {
            yCount = new Fragment( "contentCount", "contentCountNullFrag",
              WP_Pollspace.this );
            yCount.add( new Label( "explanation", bunA.l( "a.poll.noResultsToReport" )));
            yPoll.add( yCount );

            if( isVotingEnabled )
            {
                candidateDetail = new Fragment( "candidateDetail", "candidateLyCnFrag",
                  WP_Pollspace.this );
                {
                    final String candidateEmail = currentVote.getCandidateEmail();
                    final Label label = new Label("candidate",
                      bunW.l( "a.poll.web.WP_Pollspace.candidateLyCn(1)",
                      candidateEmail == null? nobodyString:
                        IDPair.toUsername( candidateEmail )));
                    label.setRenderBodyOnly( true );
                    candidateDetail.add( label );
                }
            }
            else candidateDetail = newNullComponent( "candidateDetail" );
            candidateForm.add( candidateDetail );
            return;
        }

        yCount = new Fragment( "contentCount", "contentCountFrag", WP_Pollspace.this );
        yPoll.add( yCount );

        final CountTablePVC countTablePV = new CountTablePVC( count.countTable(), poll.name() );
        final boolean toCorrectResults; // true iff a correction is actually needed
        final CountNode specificPathNodeAtLastCount;
        final SpecificCrosspathBarFragment specificCrosspathBarFragOrNull;
        if( isVotingEnabled )
        {
            specificPathNodeAtLastCount = countTablePV.getOrCreate( userEmail );
            final boolean nodeAtLastCountIsImageAndCurrent = specificPathNodeAtLastCount.isImage()
              && specificPathNodeAtLastCount.getTime() > currentVote.getTime();
            final String candidateEmailOld = specificPathNodeAtLastCount.getCandidateEmail();
            final String candidateEmailNew = currentVote.getCandidateEmail();
            final AttributeAppender candidateLinkNewCrosspathStyler =
              newCandidateLinkCrosspathStyler();
            if( nodeAtLastCountIsImageAndCurrent
             || ObjectX.nullEquals( candidateEmailOld, candidateEmailNew ))
            {
                toCorrectResults = false;
                final String candidateEmail = candidateEmailOld; // rather than new, which may be stale
                candidateDetail = new Fragment( "candidateDetail", "candidateLyCySnFrag",
                  WP_Pollspace.this );
                {
                    final Label label = new Label( "candidate1",
                      bunW.l( "a.poll.web.WP_Pollspace.candidateLyCySn(1)",
                      candidateEmail == null? nobodyString: IDPair.toUsername( candidateEmail )));
                    label.setRenderBodyOnly( true );
                    candidateDetail.add( label );
                }
                addCandidateDetail( candidateDetail, "a.poll.web.WP_Pollspace.candidateLyCySn", 2,
                  candidateEmail, candidateLinkNewCrosspathStyler, nobodyString, cycle );
            }
            else
            {
                final String vCor = getPageParameters().getString( "vCor", "y" );
                if( "y".equals( vCor )) toCorrectResults = true;
                else if( "n".equals( vCor )) toCorrectResults = false;
                else
                {
                    cycle.vSession().error( "improper value for page parameter 'vCor': " + vCor );
                    throw new RestartResponseException( new WP_Message() );
                }

                final AttributeAppender candidateLinkOldCrosspathStyler =
                  newCandidateLinkCrosspathStyler();
                candidateLinkOldCrosspathStyler.setEnabled( !toCorrectResults );
                candidateLinkNewCrosspathStyler.setEnabled( toCorrectResults );

                candidateDetail = new Fragment( "candidateDetail", "candidateLyCySyFrag",
                  WP_Pollspace.this );
                addCandidateDetail( candidateDetail, "a.poll.web.WP_Pollspace.candidateLyCySy", 1,
                  candidateEmailOld, candidateLinkOldCrosspathStyler,
                  nobodyString, cycle );
                addCandidateDetail( candidateDetail, "a.poll.web.WP_Pollspace.candidateLyCySy", 3,
                  candidateEmailNew, candidateLinkNewCrosspathStyler,
                  nobodyString, cycle );
                {
                    final Label label = new Label( "vCor",
                      bunW.l( "a.poll.web.WP_Pollspace.vCor." + vCor ));
                    label.setRenderBodyOnly( true );
                    candidateDetail.add( label );
                }
                {
                    final PageParameters linkParameters = new PageParameters( getPageParameters() );
                    if( toCorrectResults ) linkParameters.put( "vCor", "n" );
                    else linkParameters.remove( "vCor" );

                    final BookmarkablePageLinkX link = new BookmarkablePageLinkX(
                      "aModifier", WP_Pollspace.class, linkParameters );
                    link.setBodyModel( bunW.l( "a.poll.web.WP_Pollspace.vCorUndo." + vCor ));
                    candidateDetail.add( link );
                }
            }
            specificCrosspathBarFragOrNull = new SpecificCrosspathBarFragment();
            candidateDetail.add( specificCrosspathBarFragOrNull );
        }
        else
        {
            toCorrectResults = false;
            specificPathNodeAtLastCount = null;
            candidateDetail = new Fragment( "candidateDetail", "candidateLnCyFrag",
              WP_Pollspace.this );
            specificCrosspathBarFragOrNull = null;
        }

        candidateForm.add( candidateDetail );
        {
            final Label countIDLabel = new Label( "countID",
              bunW.l( "a.poll.web.WP_Pollspace.candidateLnCy(1)",
                countTablePV.table().readyDirectory().toUIString( " " )));
            countIDLabel.setRenderBodyOnly( true );
            candidateDetail.add( countIDLabel );
        }

        final CountNode[] crosspath;
        {
            final CR_Vote.TracePair tP;
            if( toCorrectResults ) // show crosspath as current path
            {
                tP = new CR_Vote.TracePair( poll, count, currentVote, countTablePV );
                if( tP.traceProjected == null )
                {
                    crosspath = new CountNode[] {};
                    specificCrosspathNode = null;
                }
                else
                {
                    crosspath = tP.traceProjected;
                    specificCrosspathNode = crosspath[0];
                }
            }
            else // show crosspath as path at last count
            {
                tP = null;
                specificCrosspathNode = specificPathNodeAtLastCount;
                if( specificCrosspathNode == null ) crosspath = new CountNode[] {};
                else crosspath = specificCrosspathNode.trace();
            }
            countTablePV.setCorrecting( toCorrectResults, tP, count );
        }

        final CountNode crosspathEndNode;
        final boolean crosspathEndNodeIsCandidate;
        if( specificCrosspathNode == null )
        {
            crosspathEndNode = null;
            crosspathEndNodeIsCandidate = false;
        }
        else
        {
            crosspathEndNode = crosspath[crosspath.length - 1];
            crosspathEndNodeIsCandidate = crosspathEndNode.receiveCount() > 0;
        }

      // CASCADE MODEL
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
      // A cascade is constructed of multiple tiers, arranged along a vote path, from
      // upstream (view left) to downstream (right).  A vote path of length N nodes
      // corresponds to a cascade of either N tiers in depth, or N + 1 if there are
      // upstream voters off the path.
      //
        final ArrayList<Tier> tierList = new ArrayList<Tier>();

        final CountNode specificPathNode;
        final CountNode[] path;
        final CountNode pathEndNode;
        final boolean pathEndNodeIsCandidate;
        if( voterEmail().equals( NOBODY.voterEmail() ))
        {
            specificPathNode = null;
            path = new CountNode[] {};
            pathEndNode = null;
            pathEndNodeIsCandidate = false;
        }
        else
        {
            final CountNode origin;
            if( voterEmail().equals( userEmail ))
            {
                origin = specificCrosspathNode;
                path = crosspath;
            }
            else
            {
                origin = countTablePV.getOrCreate( voterEmail() );
                path = origin.trace();
            }

         // cycle.vSession().scopeVoterNavigator().setLastIDPair( voterIDPair ); // in case user wanders off to a voter page
         /// but even if it is nobody
            specificPathNode = origin;
            pathEndNode = path[path.length - 1];
            pathEndNodeIsCandidate = pathEndNode.receiveCount() > 0;

            titleModel.setObject( voterUsername() + "/" +  titleModel.getObject() );
        }

      // end tier, root and cyclic candidates
      // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
        {
            final CountNode pathNode = pathEndNodeIsCandidate? pathEndNode: null;
            final CountNode crosspathNode = crosspathEndNodeIsCandidate? crosspathEndNode: null;
            final Tier tier = new Tier( pathNode, crosspathNode,
              countTablePV.sublistEndCandidates(),
              /*candidateNode*/null, count );
            tierList.add( tier );
        }

        {
            Tier orphanTier = null;
            if( path.length != 0 )
            {
              // upstream tiers, voters
              // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                if( pathEndNodeIsCandidate )
                {
                    for( int p = path.length - 1; p >= 0; --p )
                    {
                        final CountNode candidateNode = path[p];
                        final CountNode pathNode; // of voter
                        if( p == 0 )
                        {
                            if( candidateNode.receiveCount() == 0 ) break; // top-most node has no voters

                            pathNode = null;
                        }
                        else pathNode = path[p - 1];

                        final int q = p + crosspath.length - path.length;
                        final CountNode crosspathNode;
                        if( q > 0 && crosspath[q].equals( candidateNode )) // if they share the same candidate
                        {
                            crosspathNode = crosspath[q - 1];
                        }
                        else crosspathNode = null;

                        final Tier tier = new Tier( pathNode, crosspathNode,
                          countTablePV.sublistCastersByCandidate( candidateNode.voterEmail() ),
                          candidateNode, count );
                        assert tier.properNodesList.size() > 0; // vote path length, or receive count, implies voters
                        tierList.add( 0, tier );
                    }
                }
                else // single non-voter/non-candidate orphan
                {
                    assert path.length == 1; // end node is non-candidate implies path length 1
                    final Tier tier = new Tier( /*pathNode*/pathEndNode, pathEndNode );
                    tierList.add( 0, tier );
                    orphanTier = tier;
                }
            }
        }

      // CASCADE VIEW
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
      // The view runs horizontally from the leftmost tier (upstream) to the rightmost
      // (downstream).  Voters/candidates are stacked vertically in each tier.  The layout
      // is a "cascading table".  It is similar to a "cascading list", except that each
      // stacked node has multiple vertical columns for its various properties, such as
      // vote counts, email address, and links.
      //
        final int tN = tierList.size();
        int maxRowCount = 0;
        for( int t = 0; t < tN; ++t  )
        {
            final Tier tier = tierList.get( t );
            final int rowCount = tier.rowCount();
            if( rowCount > maxRowCount ) maxRowCount = rowCount;
        }

        {
          // Header Row
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            final RepeatingView tierRepeating = new RepeatingView( "tierRepeat" );
            yCount.add( tierRepeating );

            for( int t = 0, tLast = tN - 1; t <= tLast; ++t  ) // left to right
            {
                final CountNode candidateNode;
                if( t != tLast )
                {
                    final Tier candidateTier = tierList.get( t + 1 );
                    candidateNode = candidateTier.pathNode;
                }
                else candidateNode = null;

                final Fragment y = newBodyOnlyFragment( tierRepeating.newChildId(), "headerRowFrag",
                  WP_Pollspace.this );
                tierRepeating.add( y );

              // receive count
              // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                {
                    final Label label = new Label( "receiveCount",
                      bunW.l( "a.poll.web.WP_Pollspace.th.receiveCount.short" ));
                    label.add( new SimpleAttributeModifier( "title",
                      bunW.l( "a.poll.web.WP_Pollspace.th.receiveCount" )));
                    y.add( label );
                }

              // voter email
              // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                {
                    final String key;
                    if( t == tLast ) key = "a.poll.web.WP_Pollspace.th.voterEmail.end";
                    else if( candidateNode == null )
                    {
                        key = "a.poll.web.WP_Pollspace.th.voterEmail.orphan";
                    }
                    else key = "a.poll.web.WP_Pollspace.th.voterEmail";
                    y.add( new Label( "voterEmail", bunW.l( key )));
                }

              // position
              // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                {
                    final WebMarkupContainer th = new WebMarkupContainer( "position" );
                    th.add( new SimpleAttributeModifier( "title",
                      bunW.l( "a.poll.web.WP_Pollspace.th.position" )));
                    y.add( th );
                }

              // hold count
              // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                {
                    final Label label = new Label( "holdCount",
                      bunW.l( "a.poll.web.WP_Pollspace.th.holdCount.short" ));
                    label.add( new SimpleAttributeModifier( "title",
                      bunW.l( "a.poll.web.WP_Pollspace.th.holdCount" )));
                    y.add( label );
                }

              // cast-carry count
              // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                {
                    final Label label = new Label( "castCarryCount",
                      bunW.l( "a.poll.web.WP_Pollspace.th.castCarryCount.short" ));
                    label.add( new SimpleAttributeModifier( "title",
                      bunW.l( "a.poll.web.WP_Pollspace.th.castCarryCount" )));
                    y.add( label );
                }

            }
        }

        final RepeatingView rowRepeating = new RepeatingView( "repeat" );
        yCount.add( rowRepeating );
        final SimpleDateFormat iso8601Formatter =
          new SimpleDateFormat( SimpleDateFormatX.ISO_8601_PATTERN );
        final Date date = new Date( 0L );
        for( int r = 0; r < maxRowCount; ++r ) // data rows, top to bottom
        {
            final WebMarkupContainer row = new WebMarkupContainer( rowRepeating.newChildId() );
            rowRepeating.add( row );

            final RepeatingView tierRepeating = new RepeatingView( "tierRepeat" );
            row.add( tierRepeating );
            for( int t = 0, tLast = tN - 1; t <= tLast; ++t  ) // left to right
            {
                final Tier tier = tierList.get( t );
                final int lastNodeRow = tier.lastNodeRow();

                final Tier candidateTier;
                final CountNode candidateNode;
                if( t != tLast )
                {
                    candidateTier = tierList.get( t + 1 );
                    candidateNode = candidateTier.pathNode;
                }
                else
                {
                    candidateTier = null;
                    candidateNode = null;
                }

                final Fragment y;

              // Footnote Row
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                if( r >= tier.footnoteRow )
                {
                    y = newBodyOnlyFragment( tierRepeating.newChildId(), "footnoteRowFrag",
                      WP_Pollspace.this );

                  // footnote
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    if( r == tier.footnoteRow )
                    {
                        final WebMarkupContainer td = new WebMarkupContainer( "footnote" );
                        td.add( new SimpleAttributeModifier( "rowspan",
                          Integer.toString( maxRowCount - r )));
                        tier.footnoteBuilder.td = td;
                        y.add( td );

                        if( t < tLast && candidateNode == null ) appendStyleClass( td, "orphan" );
                    }
                    else y.add( newNullComponent( "footnote" ));

                  // outflow
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    addOutflow( r, y, tier, candidateTier, cycle );
                }

              // Sum Row
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                else if( tier.sumRow != -1 && r > tier.sumRow ) // it must be the 2nd sum row
                {
                    y = newBodyOnlyFragment( tierRepeating.newChildId(), "sumRow2Frag",
                      WP_Pollspace.this );

                  // outflow
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    addOutflow( r, y, tier, candidateTier, cycle );
                }
                else if( r == tier.sumRow )
                {
                  // hold count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    if( t == tLast ) // end-candidate tier
                    {
                        y = newBodyOnlyFragment( tierRepeating.newChildId(), "sumHRowFrag",
                          WP_Pollspace.this );

                        y.add( new Label( "turnout",
                          bunW.l( "a.poll.web.WP_Pollspace.turnout" )).setRenderBodyOnly( true ));

                        final Fragment sup = new Fragment( "sup", "footnoteCallFrag",
                          WP_Pollspace.this );
                        final long nTurnout = count.holdCount();
                        final long nEligible = poll.populationSize();
                        final String footnoteBody;
                        if( nEligible > 0 )
                        {
                            footnoteBody = bunW.l(
                              "a.poll.web.WP_Pollspace.turnout_XHT(1Int,2Int,3Float,4)",
                              nTurnout, nEligible, nTurnout * 100d / nEligible,
                              poll.populationSizeExplanation() );
                        }
                        else footnoteBody = bunW.l( "a.poll.web.WP_Pollspace.turnout_XHT" );  // turnout cannot be calculated
                        final Footnote footnote = new Footnote( footnoteBody );
                        sup.add( footnote.newCallLink() );
                        tier.footnoteBuilder.append( footnote );
                        y.add( sup );

                        y.add( new Label( "holdCount", bunA.format( "%,d", nTurnout )));

                    }

                  // cast-carry count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    else // voter tier
                    {
                        y = newBodyOnlyFragment( tierRepeating.newChildId(), "sumCCRowFrag",
                          WP_Pollspace.this );
                        y.add( new Label( "castCarryCount",
                          bunA.format( "%,d", candidateNode.receiveCount() )));
                    }

                  // outflow
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    addOutflow( r, y, tier, candidateTier, cycle );

                }

              // Other Row
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                else if( r == tier.otherNodesRow )
                {
                    y = newBodyOnlyFragment( tierRepeating.newChildId(), "otherNodesRowFrag",
                      WP_Pollspace.this );

                  // voter email
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    final long holdCount = tier.otherNodesCumulate.holdCount;
                    y.add( new Label( "voterEmail", bunW.l(
                      t == tLast && holdCount == 0?
                        "a.poll.web.WP_Pollspace.otherNodes0": // edge case that needs special labelling to make sense
                        "a.poll.web.WP_Pollspace.otherNodes" )));

                  // hold count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    y.add( new Label( "holdCount", holdCount == -1?
                      "": // non-end-candidate tier, cumulative data not calculated
                      bunA.format( "%,d", holdCount )));

                  // cast-carry count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    final long castCarryCount = tier.otherNodesCumulate.castCarryCount;
                    y.add( new Label( "castCarryCount", castCarryCount == -1?
                      "": // end-candidate tier, cumulative data not calculated
                      bunA.format( "%,d", castCarryCount )));

                  // outflow
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    addOutflow( r, y, tier, /*node*/null, castCarryCount, candidateTier, cycle );

                }

              // Node Row
              // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                else
                {
                    y = newBodyOnlyFragment( tierRepeating.newChildId(), "nodeRowFrag",
                      WP_Pollspace.this );

                    final CountNode node = tier.getNode( r );
                    final String nodeUsername = IDPair.toUsername( node.voterEmail() );
                    final boolean isPathNode;
                    final boolean isSpecificPathNode;
                    if( r == tier.pathRow )
                    {
                        isPathNode = true;
                        isSpecificPathNode = node.equals( specificPathNode );
                    }
                    else
                    {
                        isPathNode = false;
                        isSpecificPathNode = false;
                    }

                  // inflow
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    {
                        final WebMarkupContainer td = new WebMarkupContainer( "inflow" );
                        y.add( td );

                        final Component img;
                        if( isPathNode && node.receiveCount() > 0 )
                        {
                            appendStyleClass( td, "f" ); // flow
                            appendStyleClass( td, "i" ); // in

                            img = new WebMarkupContainer( "img" );
                            final String imgName = isSpecificPathNode? "f-i": "f-i-path";
                            img.add( new SimpleAttributeModifier( "src",
                              cycle.getWebRequest().getHttpServletRequest().getContextPath()
                                + "/pollspace/" + imgName + ".png" ));
                        }
                        else img = newNullComponent( "img" );
                        td.add( img );
                    }

                  // receive count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    {
                        final Label label = new Label( "receiveCount",
                          bunA.format( "%,d", node.receiveCount() ));
                        y.add( label );
                    }

                  // position
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    {
                        final WebMarkupContainer td = new WebMarkupContainer( "symbol" );
                        y.add( td );

                        final Model model = new Model();
                        final ExternalLink link = new ExternalLink( "position", model );
                        String location = node.getLocation();
                        if( location == null )
                        {
                            location = Position.pageLocation( nodeUsername, poll );
                        }
                        else appendStyleClass( td, "ex" );

                        model.setObject( location );
                        td.add( link );
                    }

                  // voter email
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    final long castCarryCount = node.singleCastCount() + node.carryCount();
                    {
                        final boolean isOrphan = t < tLast && castCarryCount == 0;
                        final boolean toEnable = !isOrphan; // othewise click on node and it vanishes (too surprising)
                        final PageParameters linkParameters =
                          new PageParameters( getPageParameters() );
                        linkParameters.remove( "v" );
                        if( isSpecificPathNode ) // leftmost on path
                        {
                            if( path.length == 1 ) linkParameters.remove( "u" ); // clear single-node path to no path at all
                            else // else unroll the path, rightward
                            {
                                linkParameters.put( "u", IDPair.toUsername(
                                  node.getCandidateEmail() ));
                            }
                        }
                        else linkParameters.put( "u", nodeUsername ); // make it the specific node

                        final WebMarkupContainer td;
                        {
                            if( toEnable )
                            {
                                td = new BookmarkablePageLinkX( // JavaScript link
                                  "voterEmail", WP_Pollspace.class, linkParameters );
                                appendStyleClass( td, "k" ); // clickable
                            }
                            else td = new WebMarkupContainer( "voterEmail" );
                            y.add( td );
                        }
                        if( isPathNode ) appendStyleClass( td, "dpath" );

                        if( r == tier.crosspathRow ) appendStyleClass( td, "crosspath" );

                        final BookmarkablePageLinkX link = new BookmarkablePageLinkX( // ordinary link within and in addition to the JavaScript link
                          "a", WP_Pollspace.class, linkParameters );
                        link.setBodyModel( nodeUsername );
                        link.setEnabled( toEnable );
                        td.add( link );

                        if( isOrphan )
                        {
                            final Footnote footnote;
                            if( node.getBar() != null && node.getCandidateEmail() != null ) // non-voting nodes have "voterBarUnknown", because they are not checked for bars
                            {
                                if( node.equals( specificCrosspathNode ))
                                {
                                    specificCrosspathBarFragOrNull.init(
                                      tier.footnoteBuilder, cycle );
                                    footnote = specificCrosspathBarFragOrNull.footnote;
                                    if( footnote == null ) throw new NullPointerException(); // fail fast
                                }
                                else
                                {
                                    footnote = new Footnote( "<p>"
                                      + bunA.l( "a.poll.voteBar(1,2,3)", nodeUsername,
                                          IDPair.toUsername( node.getCandidateEmail() ),
                                          node.getBar() )
                                      + "</p>" );
                                    tier.footnoteBuilder.append( footnote );
                                }
                            }
                            else
                            {
                                footnote = new Footnote( bunW.l(
                                  "a.poll.web.WP_Pollspace.orphanVoter-non_XHT" ));
                                tier.footnoteBuilder.append( footnote );
                            }

                            final Fragment sup = new Fragment( "sup", "footnoteCallFrag",
                              WP_Pollspace.this );
                            sup.add( footnote.newCallLink() );
                            td.add( sup );
                        }
                        else td.add( newNullComponent( "sup" ));
                    }

                  // hold count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    y.add( new Label( "holdCount", bunA.format( "%,d", node.holdCount() )));

                  // cast-carry count
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    {
                        final WebMarkupContainer td = new WebMarkupContainer( "castCarryCount" );
                        y.add( td );

                        final StringBuilder b = new StringBuilder();
                        b.append( "javascript:alert( '" );
                        b.append( nodeUsername );
                        final long time = node.getTime();
                        if( time == 0L ) appendStyleClass( td, "timeless" );
                        else
                        {
                            date.setTime( time );
                            b.append( ", " );
                            b.append( iso8601Formatter.format( date ));
                        }
                        b.append( "' )" );
                        final ExternalLink link = new ExternalLink( "a",
                          /*href*/b.toString(), /*label*/bunA.format( "%,d", castCarryCount ));
                        td.add( link );
                    }

                  // outflow
                  // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
                    addOutflow( r, y, tier, node, castCarryCount, candidateTier, cycle );
                }
                tierRepeating.add( y );
            }

          // startCol and endCol (first row only)
          // ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` ` `
            if( r == 0 )
            {
                final SimpleAttributeModifier sAM =
                  new SimpleAttributeModifier( "rowspan", Integer.toString( maxRowCount ));
                {
                    final WebMarkupContainer td = new WebMarkupContainer( "startCol" );
                    td.add( sAM );
                    row.add( td );
                }
                {
                    final WebMarkupContainer td = new WebMarkupContainer( "endCol" );
                    td.add( sAM );
                    row.add( td );
                }
            }
            else
            {
                row.add( newNullComponent( "startCol" ));
                row.add( newNullComponent( "endCol" ));
            }
        }

      // FOOTNOTES
      // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
        if( specificCrosspathBarFragOrNull != null &&
          !specificCrosspathBarFragOrNull.initWasCalled ) // i.e. footnote not placed in an orphan tier
        {
            specificCrosspathBarFragOrNull.init( tierList.get(tN-1).footnoteBuilder, cycle ); // place it in the final tier
        }

        for( int f = 0, t = 0; t < tN; ++t  )
        {
            final Tier tier = tierList.get( t );
            final FootnoteBuilder fB = tier.footnoteBuilder;
            final MarkupContainer container;
            if( fB.list.size() == 0 ) container = newNullComponent( "container" );
            else
            {
                appendStyleClass( fB.td, "footnote" );
                container = new Fragment( "container", "footnoteTableFrag", WP_Pollspace.this );
                final RepeatingView repeating = new RepeatingView( "repeat" );
                container.add( repeating );

                final int fTN = fB.list.size();
                for( int fT = 0;; )
                {
                    final Footnote footnote = fB.list.get( fT );
                    final WebMarkupContainer y = new WebMarkupContainer( repeating.newChildId() );
                    repeating.add( y );

                    ++f;
                    final String fString = Integer.toString( f );
                    final ExternalLink noteLink =
                      new ExternalLink( "link", "#fc-" + fString, fString );
                    noteLink.add( new SimpleAttributeModifier( "id", "fn-" + fString ));
                    y.add( noteLink );

                    for( int c = footnote.callLinkList.size() - 1;; --c )
                    {
                        final ExternalLink callLink = footnote.callLinkList.get( c );
                        callLink.setModelObject( "#fn-" + fString );
                        callLink.getLabel().setObject( fString );
                        if( c == 0 )
                        {
                            callLink.add( new SimpleAttributeModifier( "id", "fc-" + fString )); // only the first call gets this
                            break;
                        }
                    }

                    y.add( new Label( "body", footnote.body ).setEscapeModelStrings( false ));
                    ++fT;
                    if( fT >= fTN )
                    {
                        appendStyleClass( y, "last" );
                        break;
                    }
                }
            }
            fB.td.add( container );
        }

    }



   // ------------------------------------------------------------------------------------


    /** Returns the query parameters for a particular pollspace page.  The voter is set as
      * the last fore-navigated to.
      *
      *     @see votorola.a.voter.WC_VoterNavigator.SessionScope#getLastIDPair()
      */
    public static PageParameters parameters( final String serviceName, final VRequestCycle cycle )
    {
        final PageParameters parameters = new PageParameters();
        parameters.put( "p", Poll.pName( serviceName ));
        VoterPage.X.setFrom( cycle.vSession().scopeVoterNavigator().getLastIDPair(), parameters );
        return parameters;
    }



    public @Warning("non-API") IDPair getNewCandidate() { return newCandidate; };


        private IDPair newCandidate;


        public @Warning("non-API") void setNewCandidate( IDPair newCandidate ) // public for sake of Wicket property models only
        {
            this.newCandidate = newCandidate;
            newVote.setCandidateEmail( NOBODY_ID.equalsEmail(newCandidate)? null:
              newCandidate.email() );
        };



   // - D i v i s i o n a l - P a g e ----------------------------------------------------


    public DivisionalPath divisionalPath() { return divisionalPath; }


        private final DivisionalPath divisionalPath;



   // - T a b b e d - P a g e ------------------------------------------------------------


    /** @see #NAV_TAB
      */
    public NavTab navTab( VRequestCycle cycle ) { return NAV_TAB; }



    /** The navigation tab that fetches the pollspace page (an instance of WP_Pollspace).
      */
    public static final NavTab NAV_TAB = new PollspaceTab();



   // - V o t e r - P a g e --------------------------------------------------------------


    public String voterEmail() { return voterIDPair.email(); }



    public IDPair voterIDPair() { return voterIDPair; }


        private final IDPair voterIDPair;



    public String voterUsername() { return voterIDPair.username(); }



//// P r i v a t e ///////////////////////////////////////////////////////////////////////


    private void addCandidateDetail( final MarkupContainer y, final String baseKey, int suffix,
      final String email, final AttributeAppender styler,
      final String nobodyString, final VRequestCycle cycle )
    {
        y.add( newCandidateDetailLabel( baseKey, suffix, cycle ));
        y.add( newCandidateDetailVLink( suffix, email, nobodyString ).add( styler ));
        ++suffix;
        y.add( newCandidateDetailLabel( baseKey, suffix, cycle ));
    }



    /** Adds outflow for a non-node row.
      */
    private void addOutflow( final int r, final WebMarkupContainer y, final Tier tier,
      final Tier candidateTier, final VRequestCycle cycle )
    {
        final WebMarkupContainer td = new WebMarkupContainer( "outflow" );
        y.add( td );

        Component img = null; // so far
        if( candidateTier != null )
        {
            int rCandidate = candidateTier.pathRow;
            if( r <= rCandidate )
            {
                appendStyleClass( td, "f" ); // flow
                appendStyleClass( td, "o" ); // out
                if( tier.pathNode != null ) appendStyleClass( td, "path" );

                if( r == rCandidate )
                {
                    img = new WebMarkupContainer( "img" );
                    final StringBuilder b = new StringBuilder();
                    b.append( cycle.getWebRequest().getHttpServletRequest().getContextPath() );
                    b.append( "/pollspace/f-DR-clear-bottom" );

                    if( tier.pathNode != null &&
                      ( r == tier.pathRow || r == rCandidate )) b.append( "-path" );
                    b.append( ".png" );
                    img.add( new SimpleAttributeModifier( "src", b.toString() ));
                }
                // else empty cell, showing only the background image
            }
        }
        if( img == null ) img = newNullComponent( "img" );

        td.add( img );
    }



    /** Adds outflow for a node row (proper, other, or external path).
      *
      *     @param node the count node for looking up any vote bar;
      *       it is null in the case of an "other" row, and in that case
      *       castCarryCount is non-zero, or the candidateTier is null
      */
    private void addOutflow( final int r, final WebMarkupContainer y,
      final Tier tier, final CountNode node, final long castCarryCount,
      final Tier candidateTier, final VRequestCycle cycle )
    {
        final WebMarkupContainer td = new WebMarkupContainer( "outflow" );
        y.add( td );

        final MarkupContainer img;
        if( candidateTier == null ) img = newNullComponent( "img" );
        else if( castCarryCount == 0 )
        {
            appendStyleClass( td, "f" ); // flow
            appendStyleClass( td, "o" ); // out
            appendStyleClass( td, "non" );

            final StringBuilder b = new StringBuilder();
            b.append( cycle.getWebRequest().getHttpServletRequest().getContextPath() );
            b.append( "/pollspace/f-non" );
            if( node.getBar() != null && node.getCandidateEmail() != null ) b.append( "-bar" ); // non-voting nodes have "voterBarUnknown", because they are not checked for bars

            b.append( ".png" );
            img = new WebMarkupContainer( "img" );
            img.add( new SimpleAttributeModifier( "src", b.toString() ));
        }
        else
        {
            int rCandidate = candidateTier.pathRow;
            appendStyleClass( td, "f" ); // flow
            appendStyleClass( td, "o" ); // out
            if( tier.pathNode != null )
            {
                if( r > tier.pathRow && r <= rCandidate
                 || r <= tier.pathRow && r > rCandidate ) appendStyleClass( td, "path" );
            }

            img = new WebMarkupContainer( "img" );
            final int lastNodeRow = tier.lastNodeRow();
            final StringBuilder b = new StringBuilder();
            b.append( cycle.getWebRequest().getHttpServletRequest().getContextPath() );
            b.append( "/pollspace/f-" );
            if( r == 0 )
            {
                appendStyleClass( td, "top" );
                if( r == rCandidate )
                {
                    if( r == lastNodeRow ) b.append( "R-single" );
                    else if( tier.pathNode == null ) b.append( "UR-top" ); // covers everything in this case, and so there are no other images
                    else if( r == tier.pathRow ) b.append( "R-top" );
                    else b.append( "UR-top" );
                }
                else b.append( "RD-top" );
            }
            else if( r < rCandidate )
            {
                if( r == tier.pathRow ) b.append( "RD-top" );
                else b.append( "RD" );  // special case
            }
            else if( r == lastNodeRow )
            {
                if( r == rCandidate ) b.append( "DR-bottom" );
                else b.append( "RU-bottom" );
            }
            else
            {
                if( r == rCandidate )
                {
                    if( tier.pathNode == null ) b.append( "R" ); // covers everything in this case, and so there are no other images
                    else if( r > tier.pathRow ) b.append( "DR" );
                    else if( r < tier.pathRow ) b.append( "UR" );
                    else b.append( 'R' );
                }
                else b.append( "RU" );
            }

            if( tier.pathNode != null &&
              ( r == tier.pathRow || r == rCandidate )) b.append( "-path" );
            b.append( ".png" );
            img.add( new SimpleAttributeModifier( "src", b.toString() ));
        }
        td.add( img );
    }



    /** @see #newVote
      */
    private Vote currentVote; // final after init



    private Label newCandidateDetailLabel( final String baseKey, final int suffix,
      final VRequestCycle cycle )
    {
        final Label label = new Label( "candidate" + suffix, cycle.bunW().l( baseKey + suffix ));
        label.setRenderBodyOnly( true );
        return label;
    }



    private BookmarkablePageLinkX newCandidateDetailVLink( final int suffix, final String email,
      final String nobodyString )
    {
        final PageParameters linkParameters = new PageParameters( getPageParameters() );
        linkParameters.remove( "v" );
        boolean toEnableLink = false; // so far
        final String username;
        if( email == null )
        {
            username = nobodyString;
            linkParameters.remove( "u" );
            toEnableLink = !voterEmail().equals( NOBODY.voterEmail() );
        }
        else if( email.equals( voterEmail() )) username = voterUsername();
        else
        {
            username = IDPair.toUsername( email );
            linkParameters.put( "u", username );
            toEnableLink = true;
        }

        final BookmarkablePageLinkX link =
          new BookmarkablePageLinkX( "a" + suffix, WP_Pollspace.class, linkParameters );
        link.setBodyModel( username );
        link.setEnabled( toEnableLink );
        return link;
    }



    private static AttributeAppender newCandidateLinkCrosspathStyler()
    {
        return new AttributeAppender( "class", new Model("crosspath"), " " );
    }



    /** @see #currentVote
      */
    private Vote newVote; // final after init, don't set candidate directly, but use setNewCandidate



    /** Removes the divisional path parameter (p), but does not remove the voter (v).
      *
      *     @return the same instance of paramaters
      */
    private static @ThreadSafe PageParameters removeDivisionalPath( final PageParameters p )
    {
        p.remove( "p" );
        return p;
    }



    private void setResponsePage( final VRequestCycle cycle )
    {
        final PageParameters newParameters = new PageParameters( getPageParameters() );
        VoterPage.X.setFrom( newCandidate, newParameters );
        cycle.setResponsePage( WP_Pollspace.class, newParameters );
    }



    private static @ThreadSafe void setResponsePage( final DivisionalPath divisionalPath,
      final PageParameters _parametersOrNull, final VRequestCycle cycle )
    {
        final PageParameters newParameters = removeDivisionalPath(
          PageParametersX.copyOrNew( _parametersOrNull ));
        final DivisionalPollNode n = divisionalPath.getPoll();
        if( n != null ) newParameters.put( "p", Poll.pName( n.name() ));

        cycle.setResponsePage( WP_Pollspace.class, newParameters );
    }



    private CountNode specificCrosspathNode; // or null, final after init



   // ====================================================================================


    private final class CandidateForm extends Form
    {

        private CandidateForm() { super( "candidate" ); }


        protected @Override void onSubmit()
        {
            super.onSubmit();
         // if( !isVotingEnabled ) throw new IllegalStateException(); // probably impossible
         //// no need, access is guarded in VoterInputTable

            final VRequestCycle cycle = VRequestCycle.get();
            final Component submitter = (Component)findSubmittingButton();

          // Go
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            if( submitter == null || "go".equals( submitter.getId() ))
            {
                super.onSubmit();
                WP_Pollspace.this.setResponsePage( VRequestCycle.get() );
                return;
            }

          // Vote or Unvote
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            if( cycle.vSession().getUser() == null ) throw new PageExpiredException( /*message*/null ); // fail fast // user has logged out, then navigated back to this page

            if( "unvote".equals( submitter.getId() )) setNewCandidate( NOBODY_ID ); // and thence newVote
            else assert "vote".equals( submitter.getId() );

            final PageParameters parameters = getPageParameters();
            final Poll poll = WP_Poll.pollOrNullFor( parameters, cycle );
            try
            {
                newVote.commit( poll.voterInputTable(), cycle.vSession(), /*toForce*/true );
                cycle.vApplication().scopeActivity().activityList().log(
                  poll.newChangeEventOrNull( currentVote, newVote ));
                WP_Pollspace.this.setResponsePage( cycle );
            }
            catch( Exception x ) { throw VotorolaRuntimeException.castOrWrapped( x ); } // not much expected
        }

    }



   // ====================================================================================


    private static @ThreadRestricted final class CorrectableCount extends Count
    {

        CorrectableCount( final Count count ) { super( count ); }


       // - C o u n t --------------------------------------------------------------------


        public long singleCastCount() { return super.singleCastCount() + singleCastCorrection; }


            private long singleCastCorrection;

    }



   // ====================================================================================


    /** Total counts across multiple count nodes.
      */
    private static final class CountCumulate implements Serializable
    {

        private static final long serialVersionUID = 0L;


        /** Total of cast-carry counts.  This is not calculated (is -1) for end
          * candidates.
          *
          * @see CountNode#singleCastCount()
          * @see CountNode#carryCount()
          */
        long castCarryCount;


        /** Total of hold counts.  This is not calculated (is -1) for non-end candidates.
          *
          * @see CountNode#holdCount()
          */
        long holdCount;


    }



   // ====================================================================================


    /** A cached view of a count table, restricted to a particular poll.
      */
    private static @ThreadRestricted final class CountTablePVC extends CR_Vote.CountTablePVC
    {

        public CountTablePVC( CountTable t, String serviceName ) { super( t, serviceName ); }


        private void correct( final ArrayList<CountNode> nodeList,
          final QueryConstraintTester tester, final Comparator<CountNode> sortComparator )
        {
            boolean toResort = false; // so far

          // Substitute changed nodes, remove any that no longer meet query contraints
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            {
                final ListIterator<CountNode> nodeListI = nodeList.listIterator();
                while( nodeListI.hasNext() )
                {
                    final CountNode node = nodeListI.next();
                    final CountNode currentNode = cache.get( node.voterEmail() );
                    if( currentNode == null ) continue;

                    if( tester.meetsConstraints( currentNode ))
                    {
                        nodeListI.set( currentNode ); // substitute latest
                        if( node.receiveCount() != currentNode.receiveCount() ) toResort = true;
                    }
                    else nodeListI.remove(); // no longer an end-candidate
                }
            }

          // Add changed nodes that now meet query contraints
          // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            for( final CountNode currentNode: cache.values() )
            {
                if( tester.meetsConstraints(currentNode) && !nodeList.contains( currentNode ))
                {
                    nodeList.add( currentNode );
                    toResort = true;
                }
            }

            if( toResort ) Collections.sort( nodeList, sortComparator );

         // final int nN = nodeList.size();
         // if( nN > Tier.MAX_PROPER_NODES ) nodeList.removeRange( Tier.MAX_PROPER_NODES, nN - 1 );
         /// method protected, so:
            for( int n = nodeList.size();; )
            {
                if( n <= Tier.MAX_PROPER_NODES ) break;

                --n;
                nodeList.remove( n );
            }

        }


        /** Buffering the length of each raw sublist, so correctional deletions cannot
          * reduce it below MAX_PROPER_NODES.
          */
        private static final int SUBLIST_BUFFER_SIZE = 2; // guess, overkill if 1 vote change can cause at most 1 deletion


        private int subListBufferSize() { return isCorrecting? SUBLIST_BUFFER_SIZE: 0; }


        /** @param tP tracePair, or null if there is none
          */
        void setCorrecting( final boolean toCorrectResults, final CR_Vote.TracePair tP,
          final CorrectableCount count )
        {
            if( isCorrecting != null ) throw new IllegalStateException();

            if( toCorrectResults && tP != null && tP.traceProjected != null )
            {
                isCorrecting = true;
                count.singleCastCorrection =
                  tP.traceProjected[0].singleCastCount() - tP.traceAtLastCount[0].singleCastCount();
            }
            else isCorrecting = false;
        }

            private Boolean isCorrecting;


        /** A facility to test whether a node meets the contraints of a query.
          */
        interface QueryConstraintTester{ public boolean meetsConstraints( CountNode n ); }


       // - C o u n t - T a b l e . P o l l - V i e w ------------------------------------


        public ArrayList<CountNode> sublistCastersByCandidate( final String candidateEmail )
          throws SQLException
        {
            final ArrayList<CountNode> nodeList = super.sublistCastersByCandidate(
              candidateEmail, 0, Tier.MAX_PROPER_NODES + subListBufferSize() );
            if( isCorrecting )
            {
                correct( nodeList, new QueryConstraintTester()
                  { public boolean meetsConstraints( final CountNode n )
                    { return n.isCast() && candidateEmail.equals( n.getCandidateEmail() ); }},
                  CountNode.RECEIVE_COUNT_COMPARATOR );
            }
            return nodeList;
        }


        public ArrayList<CountNode> sublistEndCandidates() throws SQLException
        {
            final ArrayList<CountNode> nodeList = super.sublistEndCandidates(
              0, Tier.MAX_PROPER_NODES + subListBufferSize() );
            if( isCorrecting )
            {
                correct( nodeList, new QueryConstraintTester()
                  { public boolean meetsConstraints( final CountNode n )
                    { return n.holdCount() > 0; }},
                  CountNode.RECEIVE_COUNT_COMPARATOR );
            }
            return nodeList;
        }

    }



   // ====================================================================================


    private static final class Footnote
    {

        Footnote( String _body ) { body = _body; }


        final String body;


        /** Constructs a new call link for this footnote, and adds it to the list.
          */
        ExternalLink newCallLink()
        {
            final ExternalLink link = new ExternalLink( "link", new Model(), new Model() ); // models set later, in init_content.FOOTNOTES
            callLinkList.add( link );
            return link;
        }


        final ArrayList<ExternalLink> callLinkList
          = new ArrayList<ExternalLink>( /*initial capacity*/2 );

    }



   // ====================================================================================


    private static final class FootnoteBuilder
    {


        void append( final Footnote footnote )
        {
            if( list.size() == 0 ) list = new ArrayList<Footnote>( /*init capacity*/4 );

            list.add( footnote );
        }


        /** The read-only list of footnotes.  To append footnotes to the list, use
          * append().
          */
        List<Footnote> list = Collections.emptyList();


        /** The footnote cell at the bottom of the tier, if the footnotes are placed in a
          * tier.  A single set of footnotes is placed outside of a tier, only when there
          * is no count.
          */
        WebMarkupContainer td;

    }



   // ====================================================================================


    private static @ThreadSafe final class PollspaceTab extends NavTab
    {

        public @Override Bookmark bookmark() { throw new UnsupportedOperationException(); }


        public @Override Class pageClass() { return WP_Pollspace.class; }


        public @Override RequestCycleRunner runner( VRequestCycle c ) { return navTab_runner; }


            private static final RequestCycleRunner navTab_runner = new RequestCycleRunner()
            {
                public void run( WebRequestCycle cW )
                {
                    final VRequestCycle cycle = (VRequestCycle)cW;
                    final IDPair voterIDPair =
                      cycle.vSession().scopeVoterNavigator().getLastIDPair();
                    PageParameters parameters = null;
                    if( !NOBODY_ID.equalsEmail( voterIDPair ))
                    {
                        parameters = new PageParameters();
                        VoterPage.X.setFrom( voterIDPair, parameters );
                    }
                    setResponsePage( cycle.vSession().scopeDivisionalPathMP().getLast(),
                      parameters, cycle );
                }
            };


        public String shortTitle( final VRequestCycle cycle )
        {
            return cycle.bunW().l( "a.poll.web.WP_Pollspace.tab.shortTitle" );
        }

    }



   // ====================================================================================


    private final class SpecificCrosspathBarFragment extends Fragment
    {

        SpecificCrosspathBarFragment()
        {
            super( "candidateBar", "candidateBarFrag", WP_Pollspace.this );
            {
                final IModel model = new AbstractReadOnlyModel()
                {
                    public Object getObject()
                    {
                     // if( specificCrosspathNode == null ) return null;
                        assert specificCrosspathNode != null; // else wasting time here:
                        return VRequestCycle.get().bunW().l( "a.poll.web.WP_Pollspace.candidateBar_XHT" );
                    }
                };
                final Label label = new Label( "bar", model )
                {
                    public @Override boolean isVisible() { return isBarred(); }
                };
                label.setEscapeModelStrings( false );
                label.setRenderBodyOnly( true );
                add( label );
            }
            setRenderBodyOnly( true );
        }


        transient Footnote footnote; // final after init


        void init( final FootnoteBuilder fB, final VRequestCycle cycle )
        {
            if( initWasCalled ) throw new IllegalStateException();

            initWasCalled = true;
            final MarkupContainer sup;
            if( !isBarred() ) sup = newNullComponent( "sup" );
            else
            {
                sup = new Fragment( "sup", "footnoteCallFrag", WP_Pollspace.this );
                footnote = new Footnote( "<p>"
                  + cycle.bunA().l( "a.poll.voteBar(1,2,3)",
                      IDPair.toUsername( specificCrosspathNode.voterEmail() ),
                      IDPair.toUsername( specificCrosspathNode.getCandidateEmail() ),
                      specificCrosspathNode.getBar() )
                  + "</p>" );
                sup.add( footnote.newCallLink() );
                fB.append( footnote );
            }
            add( sup );
        }


            boolean initWasCalled;


        private boolean isBarred()
        {
            return specificCrosspathNode != null && specificCrosspathNode.getBar() != null;
        }


    }



   // ====================================================================================


    /** A tier in a cascade.  There are three types of tier: upstream tier, end tier, and
      * orphan tier.  All are comprised of count nodes.  In an upstream tier, the nodes
      * represent co-voters, all of whom are voting for the same candidate.  In an end
      * tier, the nodes represent end candidates and/or cyclic candidates, all of whom are
      * voting for nobody, or for themselves.  Finally, an orphan tier has a single node
      * that represents a non-participant; neither a voter, nor an end-candidate.</p>
      *
      * <p>The rows of the tier are indexed from zero (top), and ordered as
      * follows:</p><ul>
      *
      *     <li>{@linkplain #properRow properRow} (first and subsequent)</li>
      *     <li>{@linkplain #otherNodesRow otherNodesRow} (if any)</li>
      *     <li>{@linkplain #pathRow pathRow} (if not a proper node)</li>
      *     <li>{@linkplain #crosspathRow crosspathRow} (if not a proper node)</li>
      *     <li>{@linkplain #sumRow sumRow}</li>
      *     <li>{@linkplain #footnoteRow footnoteRow} (first and subsequent)</li>
      *
      * </ul>
      */
    private static final class Tier
    {


        /** Constructs an orphan tier.
          */
        Tier( CountNode _pathNode, CountNode node )
        {
            this( _pathNode, null, new ArrayListU<CountNode>(new CountNode[] { node }),
              null, null );
        }


        /** Constructs a tier from an ArrayList.
          *
          *     @param candidateNode the count node of the candidate
          *       in the downstream tier, or null if this is an end-candididate
          *       or orphan-voter tier
          */
        Tier( CountNode pathNode, CountNode crosspathNode,
          ArrayList<CountNode> properNodesList, CountNode candidateNode, Count count )
        {
            this( pathNode, crosspathNode, new ArrayListU<CountNode>(
                properNodesList.toArray( new CountNode[properNodesList.size()] )),
              candidateNode, count );
        }


        /** @param count the count, or null if this is an orphan-voter tier
          */
        private Tier( CountNode _pathNode, CountNode _crosspathNode,
          ArrayListU<CountNode> _properNodesList, final CountNode candidateNode, final Count count )
        {
            pathNode = _pathNode;
            crosspathNode = _crosspathNode;
            properNodesList = _properNodesList;

            int r = 0;
            properRow = r; r += properNodesList.size();
            final int pathRowProper;
            final boolean pathNodeIsSeparate;
            if( pathNode == null )
            {
                pathRowProper = -1;
                pathNodeIsSeparate = false;
            }
            else
            {
                pathRowProper = properNodesList.indexOf( pathNode );
                pathNodeIsSeparate = pathRowProper == -1;
            }

            final int crosspathRowProper;
            final boolean crosspathNodeIsSeparate;
            if( crosspathNode == null )
            {
                crosspathRowProper = -1;
                crosspathNodeIsSeparate = false;
            }
            else if( crosspathNode.equals( pathNode ))
            {
                crosspathRowProper = pathRowProper;
                crosspathNodeIsSeparate = false;
            }
            else
            {
                crosspathRowProper = properNodesList.indexOf( crosspathNode );
                crosspathNodeIsSeparate = crosspathRowProper == -1;
            }

            {
                CountCumulate c;
                if( count == null ) c = null;
                else
                {
                    c = new CountCumulate();
                    if( candidateNode == null ) // end candidate tier
                    {
                        c.castCarryCount = -1;
                        c.holdCount = count.holdCount();
                        if( c.holdCount > 0 )
                        {
                            for( CountNode node: properNodesList ) c.holdCount -= node.holdCount();

                            if( pathNodeIsSeparate ) c.holdCount -= pathNode.holdCount();

                            if( crosspathNodeIsSeparate ) c.holdCount -= crosspathNode.holdCount();

                            if( c.holdCount == 0 ) c = null; // no other nodes
                        }
                        // else no votes are cast, so there'll be an others row with a zero count (just so we have something to show)
                    }
                    else // voter tier
                    {
                        c.castCarryCount = candidateNode.receiveCount();
                        c.holdCount = -1;
                        for( CountNode node: properNodesList )
                        {
                            --c.castCarryCount; // cast
                            c.castCarryCount -= node.carryCount(); // carry
                        }

                        if( pathNodeIsSeparate )
                        {
                            --c.castCarryCount; // cast
                            c.castCarryCount -= pathNode.carryCount(); // carry
                        }

                        if( crosspathNodeIsSeparate )
                        {
                            --c.castCarryCount; // cast
                            c.castCarryCount -= crosspathNode.carryCount(); // carry
                        }

                        if( c.castCarryCount == 0 ) c = null; // no other nodes
                    }
                }
                otherNodesCumulate = c;
            }

            otherNodesRow = otherNodesCumulate == null? -1: r++;
            pathRow = pathNodeIsSeparate? r++: pathRowProper;
            crosspathRow = crosspathNodeIsSeparate? r++: crosspathRowProper;
            if( count == null ) sumRow = -1; // none for orphan tier
            else
            {
                sumRow = r;
                r += 2; // 2 to match its double rowspan, which gives room for its borders etc., so it doesn't affect the layout of other tiers
            }
            footnoteRow = r;
        }


       // --------------------------------------------------------------------------------


        /** The row of the crosspath node, or -1 if there is no crosspath node.  The
          * crosspath node corresponds to the node on the user's own vote path, where it
          * crosses the tier.  The crosspath node may be in or out of the proper node
          * list, and equal or unequal to the path node.
          */
        final int crosspathRow;


            final CountNode crosspathNode;


        final FootnoteBuilder footnoteBuilder = new FootnoteBuilder();


        /** The first footnote row.
          */
        final int footnoteRow;


        /** Returns the node for the specified row, which is either a proper node
          * or a path node (or crosspath node) external to the proper list.
          */
        CountNode getNode( final int r )
        {
            final CountNode node;
            if( r == crosspathRow ) node = crosspathNode;
            else if( r == pathRow ) node = pathNode;
            else node = properNodesList.get( r );
            return node;
        }


        /** The row of the last node.  It is either a proper node, other node, or external
          * path or crosspath node.
          */
        int lastNodeRow()
        {
            final int followingRow =  sumRow == -1? footnoteRow: sumRow;
            return followingRow - 1;
        }


        /** The nominal maximum number of proper nodes for each tier.  Assuming the proper
          * node list is ordered by cast-carry or receive count, these are the nodes that
          * might usefully "belong" in the tier.  Others probably ought to relocate
          * themselves to an upstream tier where they can be more effective and visible.
          */
        static final int MAX_PROPER_NODES = 20;


        /** The row of the other-nodes cumulate, or -1 if there are no other nodes.
          */
        final int otherNodesRow;


            /** The cumulative count of the other nodes, not individually shown in the
              * view, or null if there are no other nodes.
              */
            final CountCumulate otherNodesCumulate;


        /** The row of the path node, or -1 if there is no path node.  The path node may
          * be in or out of the proper node list, and equal or unequal to the crosspath
          * node.
          */
        final int pathRow;


            final CountNode pathNode;


        /** The row of the first proper node.
          */
        final int properRow;


            /** An unmodifiable list of the proper nodes in this tier.
              *
              *     @see #MAX_PROPER_NODES
              */
            final List<CountNode> properNodesList;


        /** The total number of rows.
          */
        int rowCount() { return footnoteRow + 1; }


        /** The first of the two sum rows, or -1 if there are none.
          */
        final int sumRow;


    }



}

