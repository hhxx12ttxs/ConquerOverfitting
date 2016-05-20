package zinger.wbj.server;

import com.google.common.base.*;
import com.google.common.collect.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.*;
import javax.servlet.http.*;

import zinger.wbj.client.*;
import zinger.wbj.db.*;
import zinger.wbj.lookup.*;

public class TallyPoolServlet extends HttpServlet
{
	public static final String LOCATION_PARAM = "location";
	public static final String DAY_OF_WEEK_PARAM = "day";
	
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException
	{
		final String location = URLDecoder.decode(request.getParameter(LOCATION_PARAM));
		final int dayOfWeek = Integer.parseInt(request.getParameter(DAY_OF_WEEK_PARAM));
		
		final StateManager stateManager = StateManagerFactory.INSTANCE.getStateManager();
		final Lookup lookup = LookupManager.INSTANCE.getLookup();
		
		final Map<String, Number> bets = stateManager.getBetsByLocation(location);
		final int observed;
		try
		{
			observed = lookup.get(location);
		}
		catch(final NotFoundException ex)
		{
			throw new ServletException("Could not find day's high", ex);
		}
		
		final SetMultimap<String, String> userPools = HashMultimap.create();
		for(final String user : bets.keySet())
		{
			try
			{
				for(final String pool : stateManager.getUserPools(user))
					userPools.put(pool, user);
			}
			catch(final NotFoundException ex)
			{
			}
		}
		
		for(final String pool : userPools.keySet())
		{
			this.tallyPool(
				location,
				pool,
				userPools.get(pool),
				bets,
				observed,
				dayOfWeek,
				stateManager
			);
		}
	}
	
	protected void tallyPool(
		final String location, 
		final String pool, 
		final Set<String> users, 
		final Map<String, Number> bets,
		final int observedHigh,
		final int dayOfWeek,
		final StateManager stateManager
	)
	{
		final Set<String> winners = new HashSet<String>();
		int winningBet = 0;
		for(final String user : users)
		{
			final int bet = bets.get(user).intValue();
			if(bet > observedHigh)
				continue;
			if(winners.isEmpty())
			{
				winners.add(user);
				winningBet = bet;
			}
			else if(bet > winningBet)
			{
				winners.clear();
				winners.add(user);
				winningBet = bet;
			}
			else if(bet == winningBet)
				winners.add(user);
		}
		
		// if everyone lost (overshot) or won (bet the same winning score), there's no need to calculate a tally
		if(winners.isEmpty() || winners.size() == users.size())
			return;
			
		final float winningTally = ((float)(users.size() - winners.size())) / (float)winners.size();
		final Session mailSession = Session.getDefaultInstance(new Properties());
		
		for(final String user : users)
		{
			final float userTally = winners.contains(user) ? winningTally : -1F;
			stateManager.addUserTally(user, location, pool, dayOfWeek, userTally);
			try
			{
				this.sendPoolTallyNotification(
					dayOfWeek,
					user,
					location,
					pool,
					stateManager.getUserTally(user, pool)/*userTally*/,
					mailSession,
					stateManager
				);
			}
			catch(final MessagingException ex)
			{
				ex.printStackTrace();
			}
			catch(final UnsupportedEncodingException ex)
			{
				ex.printStackTrace();
			}
			catch(final NotFoundException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	protected void sendPoolTallyNotification(
		final int dayOfWeek,
		final String user,
		final String location,
		final String pool,
		final float userTally,
		final Session mailSession,
		final StateManager stateManager
	) throws MessagingException, UnsupportedEncodingException, NotFoundException
	{
		final Locale locale = Locale.getDefault();
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		final String dayLabel = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale);
		final Message message = new MimeMessage(mailSession);
		message.setSentDate(new Date());
		message.setFrom(new InternetAddress(DayRollServlet.FROM_EMAIL, DayRollServlet.FROM_NAME));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(stateManager.getUserEmail(user), user));
		message.setSubject(String.format("Your %s tally from %s", pool, location));
		message.setText(String.format(
			"Dear %s,\n" +
			"Your week's tally as of %s from pool %s in %s is %.2f",
			user,
			dayLabel,
			pool,
			location,
			userTally
		));
		Transport.send(message);
	}
}

