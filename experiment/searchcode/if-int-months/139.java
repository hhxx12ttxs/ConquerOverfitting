package com.marketdata.marvin.midtier.domain.rate.backfill;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;

import com.marketdata.marvin.midtier.domain.sourcing.Source;
import com.marketdata.marvin.midtier.framework.util.PropertiesUtil;

public class BackfillInfo implements Serializable {

	/** Link to generator to use */
	public String generatorName;

	/** A ref to an uploaded file if any (expected for generator such as FILE) */
	public String attachedFilename;

	/** Filename as uploaded by client for {@link #attachedFilename} (Kept so messages make sense) */
	public String origFilename;

	/** If backfill relates to a source, attach to this att */
	private Source source;

	/** If backfill relates to a source - this is populated for the runtime temp cached version (so source not loaded too) */
	private transient Long sourceId;

	/** Years for backfill */
	public int years;

	/** Months for backfill */
	public int months;

	/** Days for backfill */
	public int days;

	/** encoded {@link #properties} - for simpler persistence not requiring EAV tables */
	private String propertiesStr;

	/** key value pairs for input to the Generator */
	private Map<String, String> properties;

	public void setSource(final Source source) {
		this.source = source;
		this.sourceId = (source == null) ? null : source.getId();
	}

	public Source getSource() {
		return source;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(final Long sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * This method is only for reading - only modify via the {@link #addProperty(String, String)} and
	 * {@link #removeProperty(String)} methods
	 * @return
	 */
	public Map<String, String> getProperties() {

		if (this.properties == null) {
			if (this.propertiesStr == null) {
				this.properties = new HashMap<String, String>();
			} else {
				decodePropertiesStr();
			}
		}

		return this.properties;
	}

	protected void encodePropertiesStr() {
		this.propertiesStr = PropertiesUtil.createPropertiesString(this.getProperties());
	}

	protected void decodePropertiesStr() {
		this.properties = PropertiesUtil.getPropertiesFromString(this.propertiesStr);
	}

	public void setPropertiesStr(final String propertiesStr) {
		this.propertiesStr = propertiesStr;
	}

	public String getPropertiesStr() {

		if (propertiesStr == null) {
			encodePropertiesStr();
		}

		return propertiesStr;
	}

	public String addProperty(final String propertyName, final String value) {
		try {
			return this.getProperties().put(propertyName, value);
		} finally {
			propertiesStr = null;
		}
	}

	public String removeProperty(final String propertyName) {
		try {
			return this.getProperties().remove(propertyName);
		} finally {
			propertiesStr = null;
		}
	}

	public String getProperty(final String propertyName) {
		return this.getProperties().get(propertyName);
	}

	public Date getEarliestDate(final Date endDate) {

		Date initialDate = endDate;

		// figure out date to go back to:
		if (years > 0) {
			initialDate = DateUtils.addYears(initialDate, 0 - years);
		}

		if (months > 0) {
			initialDate = DateUtils.addMonths(initialDate, 0 - months);
		}

		if (days > 0) {
			initialDate = DateUtils.addDays(initialDate, 0 - days);
		}

		return initialDate;
	}

	public static BackfillInfo findBackfillInfo(final List<? extends BackfillInfo> backfillInfos, final Long sourceId) {

		if (backfillInfos == null || backfillInfos.size() == 0) {
			return null;
		}

		for (final BackfillInfo bfi : backfillInfos) {
			// either a bfi with no source, or source/sourceId match
			if ((sourceId == null && (bfi.source == null && bfi.sourceId == null))
					|| (sourceId != null && ((bfi.sourceId != null && sourceId.equals(bfi.sourceId)) || (bfi.source != null && sourceId
							.equals(bfi.source.getId()))))) {
				return bfi;
			}
		}

		return null;
	}

	public static BackfillInfo finRatedBackfillInfo(final List<? extends BackfillInfo> backfillInfos) {

		if (backfillInfos == null || backfillInfos.size() == 0) {
			return null;
		}

		for (final BackfillInfo bfi : backfillInfos) {
			// either a bfi with no source
			if (bfi.source == null && bfi.sourceId == null) {
				return bfi;
			}
		}

		return null;
	}

	public static BackfillInfo findBackfillInfo(final List<? extends BackfillInfo> backfillInfos, final Source source) {

		if (backfillInfos == null || backfillInfos.size() == 0) {
			return null;
		}

		for (final BackfillInfo bfi : backfillInfos) {
			if ((source == null && (bfi.source == null && bfi.sourceId == null))
					|| (source != null && ((source.getId() != null && bfi.sourceId != null && source.getId().equals(bfi.sourceId)) || (source
							.getLabel() != null && bfi.source != null && source.getLabel().equals(bfi.source.getLabel()))))) {
				return bfi;
			}
		}

		return null;
	}

}

