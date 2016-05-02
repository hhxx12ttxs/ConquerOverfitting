/********************************************************************************
 * Project.java   Aug 24, 2011
 *
 * Copyright (c) 2011 Comcrowd/ZenithSoft.
 * The information contained in this document is the exclusive property of
 * Comcrowd / Zenith Software Ltd.  This work is protected under copyright laws of given countries of
 * origin and international laws, treaties and/or conventions.
 * No part of this document may be reproduced or transmitted in any form or by any means,
 * electronic or mechanical including photocopying or by any informational storage or
 * retrieval system, unless as expressly permitted by Comcrowd / Zenith Software Ltd
 * 
 * Modification History :
 * Name				Date					Description
 * ----				----					-----------
 * gouthamr			Aug 24, 2011					Created
 *******************************************************************************/
package com.comcrowd.domain.project;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.comcrowd.domain.company.Company;
import com.comcrowd.domain.user.AppUser;
import com.comcrowd.exception.ComCrowdException;
import com.comcrowd.persistence.dao.CompanyDAO;
import com.comcrowd.persistence.dao.ProjectDAO;
import com.comcrowd.persistence.dao.UserDAO;
import com.google.appengine.api.datastore.Key;

/**
 * Project
 * 
 * @version $ Revision: 1.0 $
 * @author gouthamr
 */
@PersistenceCapable(detachable = "true")
public class Project implements Serializable
{

	

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4131677517631472977L;

	/**
	 * key
	 */
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	/**
	 * cost
	 */
	@Persistent
	private Double cost;

	/**
	 * details
	 */
	@Persistent
	private String details;

	/**
	 * endDate
	 */
	@Persistent
	private Date endDate;

	/**
	 * paymentTerms
	 */
	@Persistent
	private String paymentTerms;

	/**
	 * startDate
	 */
	@Persistent
	private Date startDate;

	/**
	 * state
	 */
	@Persistent
	private String state;

	/**
	 * title
	 */
	@Persistent
	private String title;

	/**
	 * feedBacks :Owned relationship can exists in the same entity store
	 */
	@Persistent
	private List<ProjectFeedBack> feedBacks;

	/**
	 * mileStones :Owned relationship can exists in the same entity store
	 */
	@Persistent(defaultFetchGroup = "true",mappedBy = "project")
	private List<MileStone> mileStones;

	/**
	 * proposalKey :nOwned relationship as this need not belong to the same
	 * entity store
	 */
	@Persistent
	private Key proposalKey;

	/**
	 * ratings :Owned relationship can exists in the same entity store
	 */
	@Persistent(defaultFetchGroup = "true",mappedBy = "project")
	private List<ProjectRating> ratings;

	/**
	 * companyKey : UnOwned relationship as this need not belong to the same
	 * entity store
	 */
	@Persistent
	private Key companyKey;

	/**
	 * ownerKey : UnOwned relationship as this need not belong to the same
	 * entity store
	 */
	@Persistent
	private Key ownerKey;

	/**
	 * Constructor :
	 */
	public Project()
	{
		super();
	}

	/**
	 * Constructor :
	 * 
	 * @param cost
	 * @param details
	 * @param endDate
	 * @param paymentTerms
	 * @param startDate
	 * @param state
	 * @param title
	 * @param feedBacks
	 * @param mileStones
	 * @param proposalKey
	 * @param ratings
	 * @param companyKey
	 * @param ownerKey
	 */
	public Project(Double cost, String details, Date endDate,
			String paymentTerms, Date startDate, String state, String title,
			List<ProjectFeedBack> feedBacks, List<MileStone> mileStones,
			Key proposalKey, List<ProjectRating> ratings, Key companyKey,
			Key ownerKey)
	{
		super();
		this.cost = cost;
		this.details = details;
		this.endDate = endDate;
		this.paymentTerms = paymentTerms;
		this.startDate = startDate;
		this.state = state;
		this.title = title;
		this.feedBacks = feedBacks;
		this.mileStones = mileStones;
		this.proposalKey = proposalKey;
		this.ratings = ratings;
		this.companyKey = companyKey;
		this.ownerKey = ownerKey;
	}

	// /////// Setter getter methods
	// //////////////////////////////////////////////////////

	/**
	 * getKey : gets key.
	 * 
	 * @return Returns the key.
	 */
	public Key getKey()
	{
		return key;
	}

	/**
	 * setKey : Sets the value to key
	 * 
	 * @param key
	 *            The key to set.
	 */
	public void setKey(Key key)
	{
		this.key = key;
	}

	/**
	 * getCost : gets cost.
	 * 
	 * @return Returns the cost.
	 */
	public Double getCost()
	{
		return cost;
	}

	/**
	 * setCost : Sets the value to cost
	 * 
	 * @param cost
	 *            The cost to set.
	 */
	public void setCost(Double cost)
	{
		this.cost = cost;
	}

	/**
	 * getDetails : gets details.
	 * 
	 * @return Returns the details.
	 */
	public String getDetails()
	{
		return details;
	}

	/**
	 * setDetails : Sets the value to details
	 * 
	 * @param details
	 *            The details to set.
	 */
	public void setDetails(String details)
	{
		this.details = details;
	}

	/**
	 * getEndDate : gets endDate.
	 * 
	 * @return Returns the endDate.
	 */
	public Date getEndDate()
	{
		return endDate;
	}

	/**
	 * setEndDate : Sets the value to endDate
	 * 
	 * @param endDate
	 *            The endDate to set.
	 */
	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	/**
	 * getPaymentTerms : gets paymentTerms.
	 * 
	 * @return Returns the paymentTerms.
	 */
	public String getPaymentTerms()
	{
		return paymentTerms;
	}

	/**
	 * setPaymentTerms : Sets the value to paymentTerms
	 * 
	 * @param paymentTerms
	 *            The paymentTerms to set.
	 */
	public void setPaymentTerms(String paymentTerms)
	{
		this.paymentTerms = paymentTerms;
	}

	/**
	 * getStartDate : gets startDate.
	 * 
	 * @return Returns the startDate.
	 */
	public Date getStartDate()
	{
		return startDate;
	}

	/**
	 * setStartDate : Sets the value to startDate
	 * 
	 * @param startDate
	 *            The startDate to set.
	 */
	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * getState : gets state.
	 * 
	 * @return Returns the state.
	 */
	public String getState()
	{
		return state;
	}

	/**
	 * setState : Sets the value to state
	 * 
	 * @param state
	 *            The state to set.
	 */
	public void setState(String state)
	{
		this.state = state;
	}

	/**
	 * getTitle : gets title.
	 * 
	 * @return Returns the title.
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * setTitle : Sets the value to title
	 * 
	 * @param title
	 *            The title to set.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * getFeedBacks : gets feedBacks.
	 * 
	 * @return Returns the feedBacks.
	 */
	public List<ProjectFeedBack> getFeedBacks()
	{
		return feedBacks;
	}

	/**
	 * setFeedBacks : Sets the value to feedBacks
	 * 
	 * @param feedBacks
	 *            The feedBacks to set.
	 */
	public void setFeedBacks(List<ProjectFeedBack> feedBacks)
	{
		this.feedBacks = feedBacks;
	}

	/**
	 * getMileStones : gets mileStones.
	 * 
	 * @return Returns the mileStones.
	 */
	public List<MileStone> getMileStones()
	{
		return mileStones;
	}

	/**
	 * setMileStones : Sets the value to mileStones
	 * 
	 * @param mileStones
	 *            The mileStones to set.
	 */
	public void setMileStones(List<MileStone> mileStones)
	{
		this.mileStones = mileStones;
	}

	/**
	 * getProposalKey : gets proposalKey.
	 * 
	 * @return Returns the proposalKey.
	 */
	public Key getProposalKey()
	{
		return proposalKey;
	}

	/**
	 * setProposalKey : Sets the value to proposalKey
	 * 
	 * @param proposalKey
	 *            The proposalKey to set.
	 */
	public void setProposalKey(Key proposalKey)
	{
		this.proposalKey = proposalKey;
	}

	/**
	 * getRatings : gets ratings.
	 * 
	 * @return Returns the ratings.
	 */
	public List<ProjectRating> getRatings()
	{
		return ratings;
	}

	/**
	 * setRatings : Sets the value to ratings
	 * 
	 * @param ratings
	 *            The ratings to set.
	 */
	public void setRatings(List<ProjectRating> ratings)
	{
		this.ratings = ratings;
	}

	/**
	 * toString : (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Project [key=" + key + ", title=" + title + ", cost=" + cost
				+ ", details=" + details + ", ownerKey=" + ownerKey
				+ ", companyKey=" + companyKey + ", startDate=" + startDate
				+ ", endDate=" + endDate + ", proposalKey=" + proposalKey
				+ ", paymentTerms=" + paymentTerms + ", state=" + state + "]";
	}

	/**
	 * getCompanyKey : gets companyKey.
	 * 
	 * @return Returns the companyKey.
	 */
	public Key getCompanyKey()
	{
		return companyKey;
	}

	/**
	 * setCompanyKey : Sets the value to companyKey
	 * 
	 * @param companyKey
	 *            The companyKey to set.
	 */
	public void setCompanyKey(Key companyKey)
	{
		this.companyKey = companyKey;
	}

	/**
	 * getOwnerKey : gets ownerKey.
	 * 
	 * @return Returns the ownerKey.
	 */
	public Key getOwnerKey()
	{
		return ownerKey;
	}

	/**
	 * setOwnerKey : Sets the value to ownerKey
	 * 
	 * @param ownerKey
	 *            The ownerKey to set.
	 */
	public void setOwnerKey(Key ownerKey)
	{
		this.ownerKey = ownerKey;
	}

	// /////// Operations //////////////////////////////////////////////////////

	/**
	 * getOwner :
	 * 
	 * @return
	 * @throws ComCrowdException
	 */
	public AppUser getOwner() throws ComCrowdException
	{
		AppUser user = null;
		if (this.ownerKey != null)
		{
			UserDAO dao = new UserDAO();
			user = dao.getUserByKey(this.ownerKey);
		}
		return user;
	}

	/**
	 * getCompany :
	 * 
	 * @return
	 * @throws ComCrowdException
	 */
	public Company getCompany() throws ComCrowdException
	{
		Company company = null;
		if (this.companyKey != null)
		{
			CompanyDAO dao = new CompanyDAO();
			company = dao.getCompanyByKey(this.companyKey);
		}
		return company;
	}

	/**
	 * getProposal :
	 * 
	 * @return
	 * @throws ComCrowdException
	 */
	public Proposal getProposal() throws ComCrowdException
	{
		Proposal proposal = null;
		if (this.proposalKey != null)
		{
			ProjectDAO dao = new ProjectDAO();
			proposal = dao.getProposalByKey(this.proposalKey);
		}
		return proposal;
	}

}

