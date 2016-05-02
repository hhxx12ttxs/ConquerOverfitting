/*******************************************************************************
 * Copyright 2011 AKRA GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.akra.idocit.common.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@link List} of {@link Parameter}s. This object represents the collection of input,
 * output or exception parameters of an {@link Operation}.
 * 
 * @author Dirk Meier-Eickhoff
 * @since 0.0.1
 * @version 0.0.1
 * 
 */
public abstract class Parameters extends SignatureElement
{
	/**
	 * {@link List} of {@link Parameter}s.
	 */
	private List<Parameter> parameters;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this SignatureElement.
	 * @param category
	 *            The category of this element.
	 * @param numerus
	 *            The numerus
	 */
	public Parameters(SignatureElement parent, String category, Numerus numerus,
			boolean hasPublicAccessibleAttributes)
	{
		super(parent, category, numerus, false);
		parameters = Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int size()
	{
		int size = 0;
		for (Parameter param : parameters)
		{
			size += param.size() + 1;
		}
		return size;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see de.akra.idocit.structure.SignatureElement#copy(de.akra.idocit.structure.SignatureElement)
	 */
	@Override
	public final SignatureElement copy(SignatureElement parent)
			throws IllegalArgumentException
	{
		SignatureElement newElem = super.copy(parent);
		checkIfAssignable(Parameters.class, newElem);

		Parameters paramList = (Parameters) newElem;

		for (Parameter param : this.parameters)
		{
			paramList.addParameter((Parameter) param.copy(paramList));
		}
		return paramList;
	}

	/**
	 * Adds the {@link Documentation} <code>documentation</code> to this object if, '
	 * <code>parameterPath</code> + [{@link Delimiters#pathDelimiter} +]
	 * {@link SignatureElement#getQualifiedIdentifier()}' equals
	 * {@link Documentation#getSignatureElementIdentifier()} of <code>documentation</code>
	 * . If it is not equal, the search is continued in {@link Parameters#parameters}.
	 * 
	 * @param delimiters
	 *            The {@link Delimiters} for path building.
	 * @param documentation
	 *            The documentation that should be added to a {@link Parameters} or
	 *            {@link Parameter}.
	 * @return True, if matching element found.
	 */
	public boolean addMatchingDocumentation(Delimiters delimiters,
			Documentation documentation)
	{
		// if there is no assigned signature element, the documentation is not assignable.
		if (documentation.getSignatureElementIdentifier() == null)
		{
			return false;
		}

		boolean itemFound = false;

		String parameterPath = super.getQualifiedIdentifier();

		// if searched Parameter found, add the documentation
		if (documentation.getSignatureElementIdentifier()
				.equals(parameterPath.toString()))
		{
			super.addDocpart(documentation);
			itemFound = true;
		}
		else
		{
			// if not found, then continue search in all sub Parameters
			Iterator<Parameter> it = parameters.iterator();
			while (it.hasNext() && !itemFound)
			{
				Parameter p = it.next();
				itemFound = p.addMatchingDocumentation(delimiters, documentation,
						parameterPath);
			}
		}
		return itemFound;
	}

	/**
	 * Adds a {@link Parameter} to {@link Parameters#parameters}.
	 * 
	 * @param parameter
	 *            An {@link Parameter}.
	 */
	public void addParameter(Parameter parameter)
	{
		if (parameter != null)
		{
			if (parameters == Collections.EMPTY_LIST)
			{
				parameters = new ArrayList<Parameter>(DEFAULT_ARRAY_SIZE);
			}
			this.parameters.add(parameter);
		}
	}

	/**
	 * @return the parameters
	 */
	public List<Parameter> getParameters()
	{
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (!(obj instanceof Parameters))
		{
			return false;
		}

		Parameters other = (Parameters) obj;
		if (parameters == null)
		{
			if (other.parameters != null)
			{
				return false;
			}
		}
		else if (!parameters.equals(other.parameters))
		{
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Parameters [parameters=");
		builder.append(parameters);
		builder.append(", toString()=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

}

