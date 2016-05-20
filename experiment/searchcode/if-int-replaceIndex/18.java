/*
 * WebLEAF, a Light wEb Application Framework to help you develop server side
 * web applications written in Java. Copyright (C) 2000 Universidad de las Islas
 * Baleares(UIB), Cra. Valldemossa, km 7.5 07071 Palma de Mallorca(Illes
 * Balears) Espa?a This software is the confidential intellectual property of
 * the UIB; it is copyrighted and licensed, not sold. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.leaf.impl;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.leaf.OperationException;
import org.leaf.XMLOperation;
import org.leaf.XMLOperationDescriptor;
@Slf4j
public class DynaXMLOperation extends XMLOperation
{
  // /////////////////////////////////////
  // Regarding the operation behaviour //
  // /////////////////////////////////////
  /**
   *
   */
  private static final long serialVersionUID = -2653679815099357891L;
  public static final String EXTRA_XML_SOURCES_TAG = "leaf_xml_source";
  public static final String REPLACE_XML_SOURCE_TAG = "REPLACE_XML_SOURCE";
  //
  @Override
  protected void _setParameters(XMLOperationDescriptor theDescriptor,
      HttpServletRequest request, ServletContext theContext)
      throws OperationException
  {
    String[] extraXMLSources = request
        .getParameterValues(EXTRA_XML_SOURCES_TAG);
    try
    {
      String[] tempXMLSources = theDescriptor.getXmlSources();
      String[] finalXMLSources = tempXMLSources;
      if (extraXMLSources != null && extraXMLSources.length > 0)
      {
        // First check to find the one that we have to replace
        int replaceIndex = -1;
        for (int i = 0; i < tempXMLSources.length; i++)
        {
          if (tempXMLSources[i].equals(REPLACE_XML_SOURCE_TAG))
          {
            replaceIndex = i;
          }
        }
        // If there's no one to replace then add it to the end
        if (replaceIndex == -1)
        {
          finalXMLSources = new String[tempXMLSources.length
              + extraXMLSources.length];
          replaceIndex = tempXMLSources.length;
        }
        else
        {
          finalXMLSources = new String[tempXMLSources.length
              + extraXMLSources.length - 1];
        }
        System.arraycopy(tempXMLSources, 0, finalXMLSources, 0, replaceIndex);
        System.arraycopy(extraXMLSources, replaceIndex - replaceIndex,
            finalXMLSources, replaceIndex, replaceIndex
                + extraXMLSources.length - replaceIndex);
        System.arraycopy(tempXMLSources, replaceIndex + extraXMLSources.length
            - (extraXMLSources.length - 1), finalXMLSources, replaceIndex
            + extraXMLSources.length, finalXMLSources.length - replaceIndex
            + extraXMLSources.length);
      }
      request.setAttribute(XMLOperation.XML_SOURCES_TAG, finalXMLSources);
    }
    catch (Exception e)
    {
      log.error("Error replacing XML source ", e);
    }
    // Just in case
    super._setParameters(theDescriptor, request, theContext);
  }
}

