//Copyright (C) 2010  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.functional.BinaryFunction;
import org.nuclos.common2.functional.FunctionalUtils;

/**
 * "UsageCriteria" consisting of module and process. This class is immutable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class UsageCriteria implements Serializable, Comparable<UsageCriteria> {

	private static final long serialVersionUID = -7357563362566436100L;

	private static final Logger log = Logger.getLogger(UsageCriteria.class);

	private final UID entityUID;
	private final UID processUID;
	private final UID statusUID;
	private final String sCustom;
	
	public UsageCriteria(UID entityUID, UID processUID, UID statusUID, String sCustom) {
		this.entityUID = entityUID;
		this.processUID = processUID;
		this.statusUID = statusUID;
		this.sCustom = sCustom;
	}
/*
	//TODO MULTINUCLET use 
	 * getEntityUID
	 * getProcessUID
	 * getStatusUID
	 * ...
	 * 
	public Integer getModuleId() {
		return iModuleId;
	}
	

	public Integer getProcessId() {
		return iProcessId;
	}

	public Integer getStatusId() {
		return iStatusId;
	}
 */
	
	public UID getEntityUID() {
		return this.entityUID;
	}
	
	public UID getStatusUID() {
		return this.statusUID;
	}
	
	public UID getProcessUID() {
		return this.processUID;
	}

	public String getCustom() {
		return sCustom;
	}

	@Override
	public boolean equals(Object o) {
		final boolean result;
		if (this == o) {
			result = true;
		}
		else if (!(o instanceof UsageCriteria)) {
			result = false;
		}
		else {
			final UsageCriteria that = (UsageCriteria) o;
			result = LangUtils.equals(this.getEntityUID(), that.getEntityUID())
					&& LangUtils.equals(this.getProcessUID(), that.getProcessUID())
							&& LangUtils.equals(this.getStatusUID(), that.getStatusUID())
								&& LangUtils.equals(this.getCustom(), that.getCustom());
		}
		return result;
	}

	@Override
	public int hashCode() {
		return LangUtils.hashCode(getEntityUID())
				^ LangUtils.hashCode(getProcessUID())
				^ LangUtils.hashCode(getStatusUID())
				^ LangUtils.hashCode(getCustom());
	}

	@Override
	public String toString() {
		return "(ModuleId: " + getEntityUID() + ", ProcessId: " + getProcessUID() + ", StatusId: " + getStatusUID() + ", Custom: " + getCustom() + ")";
	}

	/**
	 * imposes a partial order on UsageCriteria. Note that not all quintuples are comparable.
	 * For a pair of non-comparable quintuples, this method returns false.
	 * @param that
	 * @return Is <code>this <= that</code>?
	 * @postcondition !this.isComparableTo(that) --> !result
	 * @see #isComparableTo(UsageCriteria)
	 * @see #compareTo(Object)
	 */
	public boolean isLessOrEqual(UsageCriteria that) {
		final boolean result;

		if (this == that) {
			result = true;
		}
		else if (!this.isComparableTo(that)) {
			result = false;
		}
		else {
			result = (this.asBinary() <= that.asBinary());
		}
		assert this.isComparableTo(that) || !result;
		return result;
	}

	/**
	 * @param that
	 * @return Is this comparable to that?
	 * @precondition that != null
	 */
	public boolean isComparableTo(UsageCriteria that) {
		if (that == null) {
			throw new NullArgumentException("that");
		}
		return isComparable(this.getEntityUID(), that.getEntityUID())
				&& isComparable(this.getProcessUID(), that.getProcessUID())
				&& isComparable(this.getStatusUID(), that.getStatusUID())
				&& isComparable(this.getCustom(), that.getCustom());
	}

	/**
	 * tries to compare this UsageCriteria to another. Note that not all quintuples are comparable.
	 * @param that
	 * @return
	 * @throws NuclosFatalException if <code>this</code> is not comparable to <code>o</code>.
	 */
	@Override
	public int compareTo(UsageCriteria that) {
		final int result;
		if (this.equals(that)) {
			result = 0;
		}
		else {
			if (!this.isComparableTo(that)) {
				throw new NuclosFatalException("The given usage criteria " + this + " and " + that + " are not comparable.");
			}
			result = this.isLessOrEqual(that) ? -1 : 1;
		}
		return result;
	}

	private int asBinary() {
		//TODO MULTINUCLET 
		return ((binary(this.getEntityUID()) << 3) | (binary(this.getProcessUID()) << 2) | (binary(this.getStatusUID()) << 1 | (binary(this.getCustom()))));
	}

	private static int binary(UID uid) {
		return uid == null ? 0 : 1;
	}
	
	private static int binary(String s) {
		return s == null ? 0 : 1;
	}

	private static boolean isComparable(UID uid1, UID uid2) {
		return uid1 == null || uid2 == null || uid1.equals(uid2);
	}
	
	private static boolean isComparable(String s1, String s2) {
		return s1 == null || s2 == null || s1.equals(s2);
	}

	/**
	 * Note that not all quintuples are comparable.
	 * @param q1
	 * @param q2
	 * @return the minimum of q1 and q2
	 * @throws NuclosFatalException if <code>q1</code> is not comparable to <code>q2</code>.
	 */
	public static UsageCriteria min(UsageCriteria q1, UsageCriteria q2) {
		return (q1.compareTo(q2) <= 0) ? q1 : q2;
	}

	/**
	 * Note that not all quintuples are comparable.
	 * @param q1
	 * @param q2
	 * @return the maximum of q1 and q2
	 * @throws NuclosFatalException if <code>q1</code> is not comparable to <code>q2</code>.
	 */
	public static UsageCriteria max(UsageCriteria q1, UsageCriteria q2) {
		return (q1.compareTo(q2) >= 0) ? q1 : q2;
	}

	/**
	 * @param collUsageCriteria
	 * @param usagecriteria
	 * @return the maximum usagecriteria contained in collUsageCriteria that is less or equal to the given usagecriteria.
	 */
	public static UsageCriteria getBestMatchingUsageCriteria(Collection<UsageCriteria> collUsageCriteria, UsageCriteria usagecriteria) {
		UsageCriteria result = null;
		for (UsageCriteria uc : collUsageCriteria) {
			if (uc.isMatchFor(usagecriteria)) {
				log.debug("uc: " + uc + " - usagecriteria: " + usagecriteria);
				assert result == null || uc.isComparableTo(result);
				result = (result == null) ? uc : max(result, uc);
			}
		}
		return result;
	}

	/**
	 * @param that
	 * @return this.equals(getGreatestCommonUsageCriteria(this, that)
	 */
	public boolean isMatchFor(UsageCriteria that) {
		return this.equals(getGreatestCommonUsageCriteria(this, that));
	}

	/**
	 * @param collusagecriteria Collection<UsageCriteria>
	 * @return the greatest common quintuple in the given Collection
	 * @precondition CollectionUtils.isNonEmpty(collusagecriteria)
	 */
	public static UsageCriteria getGreatestCommonUsageCriteria(Collection<UsageCriteria> collusagecriteria) {
		if (!CollectionUtils.isNonEmpty(collusagecriteria)) {
			throw new IllegalArgumentException("collusagecriteria");
		}

		return FunctionalUtils.foldl1(new GreatestCommonUsageCriteria(), collusagecriteria);
	}

	/**
	 * @param q1
	 * @param q2
	 * @return the greatest common factor in terms of quintuples. This is the greatest common factor for each single element.
	 * @postcondition result.isLessOrEqual(q1) && result.isLessOrEqual(q2)
	 */
	public static UsageCriteria getGreatestCommonUsageCriteria(UsageCriteria q1, UsageCriteria q2) {
		final UsageCriteria result = new UsageCriteria(gcf(q1.getEntityUID(), q2.getEntityUID()),
				gcf(q1.getProcessUID(), q2.getProcessUID()), gcf(q1.getStatusUID(), q2.getStatusUID()), gcf(q1.getCustom(), q2.getCustom()));
		assert result.isLessOrEqual(q1) && result.isLessOrEqual(q2);
		return result;
	}

	/**
	 * @param uid1
	 * @param uid2
	 * @return the "greatest common factor"
	 * @postcondition (uid1 == null || uid2 == null) --> result == null
	 * @todo Strengthen postcondition:  (uid1 == null || uid2 == null || uid1.intValue() != uid2.intValue()) --> result == null
	 * @postcondition LangUtils.equals(uid1, uid2) --> LangUtils.equals(result, uid1)
	 */
	private static UID gcf(UID uid1, UID uid2) {
		final UID result = (LangUtils.equals(uid1, uid2) ? uid1 : null);

		assert !(uid1 == null || uid2 == null) || result == null;
		assert !LangUtils.equals(uid1, uid2) || LangUtils.equals(result, uid1);

		return result;
	}
	
	private static String gcf(String s1, String s2) {
		final String result = (LangUtils.equals(s1, s2) ? s1 : null);
		return result;
	}

	private static class GreatestCommonUsageCriteria implements BinaryFunction<UsageCriteria, UsageCriteria, UsageCriteria, RuntimeException> {
		@Override
		public UsageCriteria execute(UsageCriteria q1, UsageCriteria q2) {
			return getGreatestCommonUsageCriteria(q1, q2);
		}
	}

}	// class UsageCriteria

