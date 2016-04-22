/*******************************************************************************
 * Copyright (c) 2007 Cisco Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    E. Dillon (Cisco Systems, Inc.) - reformat for Code Open-Sourcing
 *******************************************************************************/
package org.eclipse.tigerstripe.workbench.internal.core.model;

import org.eclipse.tigerstripe.workbench.model.deprecated_.ossj.IEventDescriptorEntry;

/**
 * An entry in the descriptor, represented by its label and primitive type
 * 
 * @author Eric Dillon
 * 
 */
public class EventDescriptorEntry implements IEventDescriptorEntry {

	public final static int LABEL_COLUMN_INDEX = 0;
	public final static int TYPE_COLUMN_INDEX = 1;

	private boolean isCustom;
	private String label;
	private String primitiveType;

	public EventDescriptorEntry(EventDescriptorEntry entry) {
		this.label = entry.getLabel();
		this.primitiveType = entry.getPrimitiveType();
		this.isCustom = false;
	}

	public EventDescriptorEntry(String label, String primitiveType) {
		this.label = label;
		this.primitiveType = primitiveType;
		this.isCustom = false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventDescriptorEntry) {
			EventDescriptorEntry other = (EventDescriptorEntry) obj;
			return other.getLabel() != null
					&& other.getLabel().equals(this.label);
		}
		return false;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPrimitiveType() {
		return primitiveType;
	}

	public void setPrimitiveType(String primitiveType) {
		this.primitiveType = primitiveType;
	}

	public boolean isCustom() {
		return this.isCustom;
	}

	public void setCustom(boolean isCustom) {
		this.isCustom = isCustom;
	}
}

