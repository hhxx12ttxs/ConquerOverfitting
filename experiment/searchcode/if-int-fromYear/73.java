/**
 ******************************************************************************
 *
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 ******************************************************************************
 *
 * Project        Lister
 * File           AdvSearch.java
 * Description    Advanced Search Component
 * Created on     28th June 2001
 * Tab width      3
 *
 ******************************************************************************
 *
 * PVCS Maintained Data
 *
 * Revision       $Revision: 1.3 $
 * Modified on    $Date: 2007/01/11 04:55:05 $
 *
 * Log at EOF
 *
 ******************************************************************************
 */
package com.custom.library.advsearch;

import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.DfQuery;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.LocaleService;
import com.documentum.web.common.SessionState;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.IReturnListener;
import com.documentum.web.form.control.*;
import com.documentum.web.form.control.databound.ConfigResultSet;
import com.documentum.web.form.control.databound.DataDropDownList;



import com.documentum.web.form.query.LogicalOperator;
import com.documentum.web.form.query.Predicate;
import com.documentum.web.formext.component.Component;
import com.documentum.web.form.query.Expression;
import com.documentum.web.form.query.ParsedExpression;

import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.*;

import com.documentum.webcomponent.library.locator.ILocator;
import com.documentum.webcomponent.library.locator.LocatorItemResultSet;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.search.SearchWordTokenizer;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

/**
 * Advanced Search component
 */
public class AdvSearch extends Component implements IReturnListener
{
   ////////////////////////////////////////////////////////////////////////////////
   // Event handlers

   /**
    * Component initialization.
    *
    * @param   args     Contains the initialization parameters for the component.
    */
   public void onInit(ArgumentList args)
   {
      // call the super component to perform the setup work
      super.onInit(args);

      // set the initial visibility of the panels
      IConfigElement visibilityRoot = lookupElement(PANEL_VISIBILITY);
      Iterator iterVisibility = visibilityRoot.getChildElements();
      while (iterVisibility.hasNext())
      {
         IConfigElement panel = (IConfigElement)iterVisibility.next();
         String strName = panel.getDescendantValue(NAME);
         Boolean bCheck = panel.getDescendantValueAsBoolean(EXPANDED);
         boolean bExpanded = bCheck.booleanValue();
         if (strName.equals(PROPERTIES_PANEL))
         {
            Panel showPanel = (Panel)getControl(PROPERTIES_PANEL, Panel.class);
            Checkbox check = (Checkbox)getControl(strName + "check", Checkbox.class);
            // handle differently owing to nested panels
            if (bExpanded)
            {
               showPanel.setVisible(true);
               // find and show the first panel
               Panel property = (Panel)getControl(PANEL + "1", Panel.class);
               property.setVisible(true);
               // find and show the add property panel
               Panel addProperty = (Panel)getControl(ADD_PROPERTIES_PANEL, Panel.class);
               addProperty.setVisible(true);
               check.setValue(true);
            }
            else
            {
               showPanel.setVisible(false);
               // find and hide all sub-panels
               for (int i = 1; i < MAX_PANELS + 1; i++)
               {
                  Panel property = (Panel)getControl(PANEL + i, Panel.class);
                  property.setVisible(false);
               }
               // find and hide the add property panel
               Panel addProperty = (Panel)getControl(ADD_PROPERTIES_PANEL, Panel.class);
               addProperty.setVisible(false);
               check.setValue(false);
            }
         }
         else
         {
            Panel showPanel = (Panel)getControl(strName, Panel.class);
            Checkbox check = (Checkbox)getControl(strName + "check", Checkbox.class);
            if (bExpanded)
            {
               check.setValue(true);
               showPanel.setVisible(true);
            }
            else
            {
               check.setValue(false);
               showPanel.setVisible(false);
            }
         }
      }




      // get all the types and use them to populate the drop down
      IConfigElement element = lookupElement(SEARCH_TYPES);
      DataDropDownList list = (DataDropDownList)getControl(TYPELIST, DataDropDownList.class);
      ConfigResultSet typeSet = new ConfigResultSet(element, null, "name", "name", "id", null, "type");
      list.getDataProvider().setResultSet(typeSet, null);
      onTypeSelected(list, null);


      // now set up the extended options
      IConfigElement dateOptions = lookupElement(DATE_OPTIONS);
      m_currentDateOptions = new ConfigResultSet(dateOptions.getDescendantElement(DATE_CONDITIONS), null, "label", "date", null, "value", "value");
      m_currentDateTimes = new ConfigResultSet(dateOptions.getDescendantElement(ELAPSED_TIMES), null, "label", "time", null, "value", "value");
      IConfigElement sizeOptions = lookupElement(SIZE_OPTIONS);
      m_currentSizeOptions = new ConfigResultSet(sizeOptions.getDescendantElement(SIZE_CONDITIONS), null, null, "size", null, null, "size");

      // apply the resultsets to the controls
      DataDropDownList dateOptionList = (DataDropDownList)getControl(DATE_PARAMS, DataDropDownList.class);
      dateOptionList.getDataProvider().setResultSet(m_currentDateOptions, null);
      DataDropDownList dateTimeList = (DataDropDownList)getControl(TIME_PARAMS, DataDropDownList.class);
      dateTimeList.getDataProvider().setResultSet(m_currentDateTimes, null);
      DataDropDownList sizeOptionList = (DataDropDownList)getControl(SIZE_PARAMS, DataDropDownList.class);
      sizeOptionList.getDataProvider().setResultSet(m_currentSizeOptions, null);

      //Create instances of resettable controls for the clear button.
      setupControls();

      // get the folder path and pre-populate the location field
      Text location = (Text)getControl(LOCATION, Text.class);
      String strFolderPath = args.get("folderpath");
      if (strFolderPath != null)
      {
         location.setValue(strFolderPath);
      }

      // make consistent states among controls
      updateDateOptionControlStatus();
   }

   /**
    * Called when the user clicks on the date radio button
    *
    * @param control Radio control
    * @param args    Arguments
    */
   public void onClickDateRadio(Radio control, ArgumentList args)
   {
      updateDateOptionControlStatus();
   }

   /**
    * Set the current control state from the request.
    */
   public void updateStateFromRequest()
   {
      super.updateStateFromRequest();

      // make consistent states among controls
      updateDateOptionControlStatus();
   }

   /**
    * Handle the <b>onBrowse</b> event.  This launches the folder browser.<p>
    *
    * @param   control           The control that fired the <b>onBrowse</b> event.
    * @param   args              The arguments to the event.
    */
   public void onBrowse(Control control, ArgumentList args)
   {
      DataDropDownList  oTypeList   = (DataDropDownList)getControl(TYPELIST, DataDropDownList.class);
      String            strType     = oTypeList.getValue();

      // Need a special case if they're searching for categories or taxonomies
      if (!strType.equals(CATTYPE))
      {
         ArgumentList selectArgs = new ArgumentList();
         selectArgs.add("component", "allfolderlocator");
         selectArgs.add("multiselect", "true");
         selectArgs.add(args);
         setComponentNested("folderlocatorcontainer", selectArgs, getContext(), this);
      }
      else
      {
         ArgumentList selectArgs = new ArgumentList();
         selectArgs.add("component", "categorylocator");
         selectArgs.add("multiselect", "true");
         selectArgs.add(args);
         setComponentNested("categorylocatorcontainer", selectArgs, getContext(), this);
      }
   }

   /**
    * Return from the locator action.
    *
    * @param  form            the returning component
    * @param  map             the map of return values
    */
   public void onReturn(Form form, Map map)
   {
      if (map != null)
      {
         // updates the location
         String strLocation = "";
         Text location = (Text)getControl(LOCATION, Text.class);
         String strCurLocation = location.getValue();
         if (strCurLocation != null)
         {
            strLocation = strCurLocation.trim();
         }

         // add values from the locator
         LocatorItemResultSet setLocatorSelections = (LocatorItemResultSet)map.get(ILocator.LOCATORSELECTIONS);
         if (setLocatorSelections != null && setLocatorSelections.first() == true)
         {
            do
            {
               // add the value if the path is not in the current selection
               String strSelectionPath = setLocatorSelections.getObject("fullpath").toString();
               if (strSelectionPath != null &&
                  strLocation.equals(strSelectionPath) == false &&
                  strLocation.startsWith(strSelectionPath+';') == false &&
                  strLocation.endsWith(';' + strSelectionPath) == false &&
                  strLocation.indexOf(';' + strSelectionPath + ';') < 0)
               {
                  if (strLocation.length() > 0)
                  {
                     strLocation += ';';
                  }
                  strLocation += strSelectionPath;
               }

            } while (setLocatorSelections.next() == true);

            location.setValue(strLocation);
         }
      }
   }

   /**
    * Handle the <b>onRender</b> event.
    */
   public void onRender()
   {
      super.onRender();
   }

   /**
    * Handle the <b>onTypeSelected</b> event.
    *
    * @param   ddlist            The control that fired the <b>onTypeSelected</b> event.
    * @param   args              The arguments to the event.
    */
   public void onTypeSelected(DataDropDownList ddlist, ArgumentList args)
   {
      IConfigElement type = null, firstType = null;
      String strValue = ddlist.getValue();
      //System.out.println("type is " + strValue);

      // using this value interrogate the XML to find out the attributes we want to display
      IConfigElement element = lookupElement(SEARCH_TYPES);
      Iterator iter = element.getChildElements();

      // find the correct type
      while (iter.hasNext() == true)
      {
         IConfigElement elem = (IConfigElement)iter.next();
         String strTestValue = elem.getAttributeValue("id");

         // remember the first type which will be the default
         if (firstType == null)
         {
            firstType = elem;
         }

         if (strTestValue.equals(strValue))
         {
             type = elem;
             break;
         }
      }

      if (type == null)
      {
         type = firstType;
         ddlist.setValue(type.getAttributeValue("id"));
      }

      if (type != null)
      {
         IConfigElement attributes = type.getDescendantElement("attributes");
         String strInitialParam = ((IConfigElement)attributes.getChildElements().next()).getChildValue("docbase_attribute");

         // get all the parameter drop downs and tie in the new resultset
         for (int i = 1; i < MAX_PANELS + 1; i++)
         {
            m_currentParamSet[i] = new ConfigResultSet(attributes, null, "name", "name", null, "docbase_attribute", "docid");
            DataDropDownList list = (DataDropDownList)getControl(PARAM_LIST + i, DataDropDownList.class);
            list.getDataProvider().setResultSet(m_currentParamSet[i], null);
            // set initial value
            list.setValue(strInitialParam);
         }

         // clear all the properties except the first one
         for (int iPropPan = 2; iPropPan < MAX_PANELS + 1; iPropPan++)
         {
            Panel property = (Panel)getControl(PANEL + iPropPan, Panel.class);
            property.setVisible(false);
         }

         // enable the first property
         getControl(PANEL + "1", Panel.class).setVisible(true);
         getControl(ADD_PROPERTIES_PANEL, Panel.class).setVisible(true);
         onParamSelected((DataDropDownList)getControl(PARAM_LIST + '1', DataDropDownList.class), null);

         // should we disable the fulltext?
         IConfigElement cfgFullText = type.getDescendantElement("fulltextsearch");
         boolean fFullText = true;
         if (cfgFullText != null)
         {
            fFullText = cfgFullText.getValueAsBoolean().booleanValue();
         }

         // enable or hide fulltext search
         Panel panelText = (Panel)getControl(TEXT_PANEL, Panel.class);
         Checkbox checkText = (Checkbox)getControl("fulltextcheck", Checkbox.class);
         if (fFullText == true)
         {
            checkText.setEnabled(true);
         }
         else
         {
            checkText.setEnabled(false);
            checkText.setValue(false);
            panelText.setVisible(false);
         }

         // should we disable the size option?
         IConfigElement cfgSize = type.getDescendantElement("sizeoptionenabled");
         boolean fSize = true;
         if (cfgSize != null)
         {
            fSize = cfgSize.getValueAsBoolean().booleanValue();
         }

         // enable or disable sizeoption
         Panel panelSize = (Panel)getControl(SIZE_PANEL, Panel.class);
         Checkbox checkSize = (Checkbox)getControl("sizeoptionscheck", Checkbox.class);
         if (fSize == true)
         {
            checkSize.setEnabled(true);
         }
         else
         {
            checkSize.setEnabled(false);
            checkSize.setValue(false);
            panelSize.setVisible(false);
         }

         // should we disable the version option?
         IConfigElement cfgVersion = type.getDescendantElement("versionoptionenabled");
         boolean fVersion = true;
         if (cfgVersion != null)
         {
            fVersion = cfgVersion.getValueAsBoolean().booleanValue();
         }

         // enable or disable version option
         Checkbox checkVersion = (Checkbox)getControl(FIND_VERSION_CHECK, Checkbox.class);
         checkVersion.setEnabled(fVersion);
         if (fVersion == false)
         {
            checkVersion.setValue(false);
         }
         if ((ddlist.getValue()).equals("epa_case")){
          //System.out.println("this is search for control");
            IConfigElement citi_attributes = type.getDescendantElement("citizen_attributes");
            String strCitiInitialParam = ((IConfigElement)citi_attributes.getChildElements().next()).getChildValue("docbase_attribute");
            //System.out.println("initial param is " + strCitiInitialParam);
         // get all the parameter drop downs and tie in the new resultset
            for (int i = 1; i < MAX_PANELS + 1; i++)
            {
                m_currentCitiParamSet[i] = new ConfigResultSet(citi_attributes, null, "name", "name", null, "docbase_attribute", "docid");
                DataDropDownList list = (DataDropDownList)getControl(CITI_PARAM_LIST + i, DataDropDownList.class);
                list.getDataProvider().setResultSet(m_currentCitiParamSet[i], null);
            // set initial value
                list.setValue(strInitialParam);
            }

         // clear all the properties except the first one
            for (int iPropPan = 2; iPropPan < MAX_PANELS + 1; iPropPan++)
            {
                Panel property = (Panel)getControl(CITI_PANEL + iPropPan, Panel.class);
                property.setVisible(false);
            }

         // enable the first property
            getControl(CITI_PANEL + "1", Panel.class).setVisible(true);
            getControl(CITI_ADD_PROPERTIES_PANEL, Panel.class).setVisible(true);
            onCitiParamSelected((DataDropDownList)getControl(CITI_PARAM_LIST + '1', DataDropDownList.class), null);

          Panel control_citizen = (Panel)getControl("control_citizen",Panel.class);
          control_citizen.setVisible(true);
          Panel showPanel = (Panel)getControl(CITI_PROPERTIES_PANEL, Panel.class);
          Checkbox citizen_check = (Checkbox)getControl("citi_propertiescheck",Checkbox.class);
          boolean bExpanded = citizen_check.getValue();
          if (bExpanded){
             showPanel.setVisible(true);
               // find and show the first panel
             Panel property = (Panel)getControl(CITI_PANEL + "1", Panel.class);
             property.setVisible(true);
               // find and show the add property panel
             Panel addProperty = (Panel)getControl(CITI_ADD_PROPERTIES_PANEL, Panel.class);
             addProperty.setVisible(true);
             citizen_check.setValue(true);
           }
           else{
               showPanel.setVisible(false);
               // find and hide all sub-panels
               for (int i = 1; i < MAX_PANELS + 1; i++)
               {
                  Panel property = (Panel)getControl(CITI_PANEL + i, Panel.class);
                  property.setVisible(false);
               }
               // find and hide the add property panel
               Panel addProperty = (Panel)getControl(CITI_ADD_PROPERTIES_PANEL, Panel.class);
               addProperty.setVisible(false);
               citizen_check.setValue(false);
            }

            Panel control_assignee = (Panel)getControl("control_assignee", Panel.class);
		    control_assignee.setVisible(true);



        }
        else{
          Panel control_citizen = (Panel)getControl("control_citizen",Panel.class);
          control_citizen.setVisible(false);

		  Panel control_assignee = (Panel)getControl("control_assignee", Panel.class);
	      control_assignee.setVisible(false);


        }
      }
   }

   /**
    * Handle the <b>onParamSelected</b> event.
    *
    * @param   ddlist            The control that fired the <b>onParamSelected</b> event.
    * @param   args              The arguments to the event.
    */
   public void onParamSelected(DataDropDownList ddlist, ArgumentList args)
   {
      IConfigElement type = null;
      // need the type value
      DataDropDownList typeList = (DataDropDownList)getControl(TYPELIST, DataDropDownList.class);
      String strValue = typeList.getValue();
      String strAttribute = ddlist.getValue();

      // using this value interrogate the XML to find out the params we want to display
      IConfigElement element = lookupElement(SEARCH_TYPES);
      Iterator iter = element.getChildElements();

      // find the correct type
      boolean bFound = false;
      while ((iter.hasNext()) && (!bFound))
      {
         IConfigElement elem = (IConfigElement)iter.next();
         String strTestValue = elem.getAttributeValue("id");
         if (strTestValue.equals(strValue))
         {
             type = elem;
             bFound = true;
         }
      }
      // get the attribute type
      IConfigElement attribute = null;
      IConfigElement attributes = type.getDescendantElement("attributes");
      Iterator iter2 = attributes.getChildElements();

      // find the correct type
      while (iter2.hasNext() == true)
      {
         IConfigElement elem = (IConfigElement)iter2.next();
         String strTestValue = elem.getDescendantValue(DOCBASE_ATTRIBUTE);

         // use the first valid attribute if there is no match
         if (attribute == null)
         {
            attribute = elem;
         }

         if (strTestValue.equals(strAttribute))
         {
             attribute = elem;
            break;
         }
      }


      // now find the attribute type config
      IConfigElement attributeConditions = lookupElement("attribute_conditions");
      IConfigElement attributeType = attributeConditions.getDescendantElement(attribute.getDescendantValue("attribute_type"));

      // get the index of the condition list
      // note since we know the control names the substring values are hard coded
      String strIndex = ddlist.getName().substring(5,6);
      ConfigResultSet conditionSet = new ConfigResultSet(attributeType, null, null, "condition", null, null, "condition");

      // get the list
      DataDropDownList conditionList = (DataDropDownList)getControl(BOOLEAN_LIST + strIndex, DataDropDownList.class);
      conditionList.getDataProvider().setResultSet(conditionSet, null);
      Integer index = new Integer(strIndex);
      int idx = index.intValue();

      // store the resultset
      m_currentBooleanSet[idx] = conditionSet;

      // depending on the attribute type may need to swap the text for a dateinput
      String strDocbaseTypeName = type.getAttributeValue("id");
      String strDocbaseAttrName = attribute.getDescendantValue("docbase_attribute");
      Text text = (Text)getControl(SEARCH_STRING + strIndex, Text.class);
      DateInput date = (DateInput)getControl(DATE + strIndex, DateInput.class);
      try
      {
         IDfType iType = getDfSession().getType(strDocbaseTypeName);
         switch (iType.getTypeAttrDataType(strDocbaseAttrName))
         {
            case IDfType.DF_TIME:
               text.setVisible(false);
               date.setVisible(true);
               setYearRange(date);
               break;

            case IDfType.DF_BOOLEAN:
               text.setVisible(false);
               date.setVisible(false);
               break;

            default: // number and string
               text.setVisible(true);
               date.setVisible(false);
         }
      }
      catch(Exception dfe)
      {
         throw new WrapperRuntimeException(dfe);
      }
   }


   public void onCitiParamSelected(DataDropDownList ddlist, ArgumentList args)
   {
      IConfigElement type = null;
      // need the type value
      DataDropDownList typeList = (DataDropDownList)getControl(TYPELIST, DataDropDownList.class);
      String strValue = typeList.getValue();
      String strAttribute = ddlist.getValue();

      // using this value interrogate the XML to find out the params we want to display
      IConfigElement element = lookupElement(SEARCH_TYPES);
      Iterator iter = element.getChildElements();

      // find the correct type
      boolean bFound = false;
      while ((iter.hasNext()) && (!bFound))
      {
         IConfigElement elem = (IConfigElement)iter.next();
         String strTestValue = elem.getAttributeValue("id");
         if (strTestValue.equals(strValue))
         {
             type = elem;
             bFound = true;
         }
      }
      // get the attribute type
      IConfigElement attribute = null;
      IConfigElement attributes = type.getDescendantElement("citizen_attributes");
      Iterator iter2 = attributes.getChildElements();

      // find the correct type
      while (iter2.hasNext() == true)
      {
         IConfigElement elem = (IConfigElement)iter2.next();
         String strTestValue = elem.getDescendantValue(DOCBASE_ATTRIBUTE);

         // use the first valid attribute if there is no match
         if (attribute == null)
         {
            attribute = elem;
         }

         if (strTestValue.equals(strAttribute))
         {
             attribute = elem;
            break;
         }
      }


      // now find the attribute type config
      IConfigElement attributeConditions = lookupElement("attribute_conditions");
      IConfigElement attributeType = attributeConditions.getDescendantElement(attribute.getDescendantValue("attribute_type"));

      // get the index of the condition list
      // note since we know the control names the substring values are hard coded
      String strIndex = ddlist.getName().substring(10,11);
      ConfigResultSet conditionSet = new ConfigResultSet(attributeType, null, null, "condition", null, null, "condition");

      // get the list
      DataDropDownList conditionList = (DataDropDownList)getControl(CITI_BOOLEAN_LIST + strIndex, DataDropDownList.class);
      conditionList.getDataProvider().setResultSet(conditionSet, null);
      Integer index = new Integer(strIndex);
      int idx = index.intValue();

      // store the resultset
      m_currentCitiBooleanSet[idx] = conditionSet;

      // depending on the attribute type may need to swap the text for a dateinput
      String strDocbaseTypeName = "constituent";
      String strDocbaseAttrName = attribute.getDescendantValue("docbase_attribute");
      Text text = (Text)getControl(CITI_SEARCH_STRING + strIndex, Text.class);
      DateInput date = (DateInput)getControl(CITI_DATE + strIndex, DateInput.class);
      try
      {
         IDfType iType = getDfSession().getType(strDocbaseTypeName);
         switch (iType.getTypeAttrDataType(strDocbaseAttrName))
         {
            case IDfType.DF_TIME:
               text.setVisible(false);
               date.setVisible(true);
               setYearRange(date);
               break;

            case IDfType.DF_BOOLEAN:
               text.setVisible(false);
               date.setVisible(false);
               break;

            default: // number and string
               text.setVisible(true);
               date.setVisible(false);
         }
      }
      catch(Exception dfe)
      {
         throw new WrapperRuntimeException(dfe);
      }
   }
   /**
    * Handle <b>onSelectText</b> event.
    *
    * @param   checkbox          The control that fired the <b>onSelectText</b> event.
    * @param   args              The arguments to the event.
    */
   public void onSelectText(Checkbox checkbox, ArgumentList args)
   {
      Control control = getControl(TEXT_PANEL);
      Panel panel = (Panel)control;
      if (panel.isVisible() == false)
      {
         panel.setVisible(true);
      }
      else
      {
         panel.setVisible(false);
      }
   }

   /**
    * Handle <b>onSelectProperties</b> event.
    *
    * @param   checkbox          The control that fired the <b>onSelectProperties</b> event.
    * @param   args              The arguments to the event.
    */
   public void onSelectProperties(Checkbox checkbox, ArgumentList args)
   {
      Control control = getControl(PROPERTIES_PANEL, Panel.class);
      Panel panel = (Panel)control;
      // since there are nested panels in this panel, must make all hidden on deselect
      if (panel.isVisible() == false)
      {
         panel.setVisible(true);
         // find and show the first panel
         Panel property = (Panel)getControl(PANEL + "1", Panel.class);
         property.setVisible(true);
         // find and show the add property panel
         Panel addProperty = (Panel)getControl(ADD_PROPERTIES_PANEL, Panel.class);
         addProperty.setVisible(true);
      }
      else
      {
         panel.setVisible(false);
         // find and hide all sub-panels
         for (int i = 1; i < MAX_PANELS + 1; i++)
         {
            Panel property = (Panel)getControl(PANEL + i, Panel.class);
            property.setVisible(false);
         }
         // find and hide the add property panel
         Panel addProperty = (Panel)getControl(ADD_PROPERTIES_PANEL, Panel.class);
         addProperty.setVisible(false);
      }
   }


   public void onSelectCitiProperties(Checkbox checkbox, ArgumentList args)
   {

      Control control = getControl(CITI_PROPERTIES_PANEL, Panel.class);
      Panel panel = (Panel)control;
      // since there are nested panels in this panel, must make all hidden on deselect
      if (panel.isVisible() == false)
      {
         panel.setVisible(true);
         // find and show the first panel
         Panel property = (Panel)getControl(CITI_PANEL + "1", Panel.class);
         property.setVisible(true);
         // find and show the add property panel
         Panel addProperty = (Panel)getControl(CITI_ADD_PROPERTIES_PANEL, Panel.class);
         addProperty.setVisible(true);
      }
      else
      {
         panel.setVisible(false);
         // find and hide all sub-panels
         for (int i = 1; i < MAX_PANELS + 1; i++)
         {
            Panel property = (Panel)getControl(CITI_PANEL + i, Panel.class);
            property.setVisible(false);
         }
         // find and hide the add property panel
         Panel addProperty = (Panel)getControl(CITI_ADD_PROPERTIES_PANEL, Panel.class);
         addProperty.setVisible(false);
      }
   }
   /**
    * Handle <b>onSelectDate</b> event.
    *
    * @param   checkbox          The control that fired the <b>onSelectDate</b> event.
    * @param   args              The arguments to the event.
    */
   public void onSelectDate(Checkbox checkbox, ArgumentList args)
   {
      Control control = getControl(DATE_PANEL, Panel.class);
      Panel panel = (Panel)control;
      if (panel.isVisible() == false)
      {
         panel.setVisible(true);


         DateInput startDateInput = (DateInput)getControl(START_DATE, DateInput.class);
         setYearRange(startDateInput);

         DateInput endDateInput = (DateInput)getControl(END_DATE, DateInput.class);
         setYearRange(endDateInput);
      }
      else
      {
         panel.setVisible(false);
      }
   }

   /**
    * Handle <b>onSelectSize</b> event.
    *
    * @param   checkbox          The control that fired the <b>onSelectSize</b> event.
    * @param   args              The arguments to the event.
    */
   public void onSelectSize(Checkbox checkbox, ArgumentList args)
   {
      Control control = getControl(SIZE_PANEL, Panel.class);
      Panel panel = (Panel)control;
      if (panel.isVisible() == false)
      {
         panel.setVisible(true);
      }
      else
      {
         panel.setVisible(false);
      }
   }

   /**
    * Handle <b>onSelectAdvanced</b> event.
    *
    * @param   checkbox          The control that fired the <b>onSelectAdvanced</b> event.
    * @param   args              The arguments to the event.
    */
   public void onSelectAdvanced(Checkbox checkbox, ArgumentList args)
   {
      Control control = getControl(ADV_PANEL, Panel.class);
      Panel panel = (Panel)control;
      if (panel.isVisible() == false)
      {
         panel.setVisible(true);
      }
      else
      {
         panel.setVisible(false);
      }
   }

   /**
    * Handle <b>onCloseSearch</b> event.
    *
    * @param   control           The control that fired the <b>onCloseSearch</b> event.
    * @param   args              The arguments to the event.
    */
   public void onCloseSearch(Control control, ArgumentList args)
   {
      Form topform = getTopForm();
      if (topform instanceof Component)
      {
         ((Component)topform).setComponentReturn();
      }
      else
      {
         topform.setFormReturn();
      }
   }

   /**
    * Handle <b>onClickAdd</b> event.
    *
    * @param   button            The control that fired the <b>onClickAdd</b> event.
    * @param   args              The arguments to the event.
    */
   public void onClickAdd(Button button, ArgumentList args)
   {
      boolean bShown = false;
      for (int i = 2; i < MAX_PANELS + 1; i++)
      {
         if (!bShown)
         {
            Control control = getControl(PANEL + i, Panel.class);
            Panel panel = (Panel)control;
            if (panel.isVisible() == false)
            {
               panel.setVisible(true);
               bShown = true;

               // we need to sync the attribute name against type
               DataDropDownList list = (DataDropDownList)getControl(PARAM_LIST + i, DataDropDownList.class);
               onParamSelected(list, null);

               // disable the add button if all available panels have been shown
               if (i == MAX_PANELS)
               {
                  button.setEnabled(false);
               }
               else
               {
                  button.setEnabled(true);
               }
               // also need to get and hide the remove button on the previous panel
               if (i != 2)
               {
                  Button removeButton = (Button)getControl(REMOVE_BUTTON + (i - 1));
                  removeButton.setVisible(false);
               }
            }
         }
      }
   }

   public void onClickCitiAdd(Button button, ArgumentList args)
   {
      boolean bShown = false;
      for (int i = 2; i < MAX_PANELS + 1; i++)
      {
         if (!bShown)
         {
            Control control = getControl(CITI_PANEL + i, Panel.class);
            Panel panel = (Panel)control;
            if (panel.isVisible() == false)
            {
               panel.setVisible(true);
               bShown = true;

               // we need to sync the attribute name against type
               DataDropDownList list = (DataDropDownList)getControl(CITI_PARAM_LIST + i, DataDropDownList.class);
               onCitiParamSelected(list, null);

               // disable the add button if all available panels have been shown
               if (i == MAX_PANELS)
               {
                  button.setEnabled(false);
               }
               else
               {
                  button.setEnabled(true);
               }
               // also need to get and hide the remove button on the previous panel
               if (i != 2)
               {
                  Button removeButton = (Button)getControl(CITI_REMOVE_BUTTON + (i - 1));
                  removeButton.setVisible(false);
               }
            }
         }
      }
   }

   /**
    * Handle <b>onClickRemove</b> event.
    *
    * @param   button            The control that fired the <b>onClickRemove</b> event.
    * @param   args              The arguments to the event.
    */
   public void onClickRemove(Button button, ArgumentList args)
   {
      String index = args.get(INDEX);
      if (index != null)
      {
         // get the panel and hide it
         Control control = getControl(PANEL + index);
         Panel panel = (Panel)control;
         if (panel.isVisible() == true)
         {
            panel.setVisible(false);
         }
         // show the remove button on the previous panel
         if (index.charAt(0) != '2')
         {
            Integer integer = new Integer(index);
            int i = integer.intValue();
            Button removeButton = (Button)getControl(REMOVE_BUTTON + (i - 1));
            removeButton.setVisible(true);
            // re-enable the add button
            if (i == MAX_PANELS)
            {
               Control control3 = getControl(ADD_BUTTON);
               Button addButton = (Button)control3;
               addButton.setEnabled(true);
            }
         }
      }
   }


   public void onClickCitiRemove(Button button, ArgumentList args)
   {
      String index = args.get(INDEX);
      if (index != null)
      {
         // get the panel and hide it
         Control control = getControl(CITI_PANEL + index);
         Panel panel = (Panel)control;
         if (panel.isVisible() == true)
         {
            panel.setVisible(false);
         }
         // show the remove button on the previous panel
         if (index.charAt(0) != '2')
         {
            Integer integer = new Integer(index);
            int i = integer.intValue();
            Button removeButton = (Button)getControl(CITI_REMOVE_BUTTON + (i - 1));
            removeButton.setVisible(true);
            // re-enable the add button
            if (i == MAX_PANELS)
            {
               Control control3 = getControl(CITI_ADD_BUTTON);
               Button addButton = (Button)control3;
               addButton.setEnabled(true);
            }
         }
      }
   }

   /**
    * Handle <b>onClickSearch</b> event.
    *
    * @param   button            The control that fired the <b>onClickSearch</b> event.
    * @param   args              The arguments to the event.
    */
   public void onClickSearch(Button button, ArgumentList args)
   {
      if (getIsValid() == true)
      {
		 String strTextSearchPhrase = "";
         String citiQueryString = null;
         String qry = "";
         String tier = "";
         IDfCollection col = null;
         boolean bChecked = false;

          // doclist type
		 DataDropDownList typeList = (DataDropDownList)getControl(TYPELIST);
         String strDocbaseType = typeList.getValue();

         // save the control values in the users Session
         saveControls();

         // initialise the argument list
         ArgumentList list = new ArgumentList();

         // query object to build
         DocbaseQuery search = new DocbaseQuery();



         search.setFromClause(strDocbaseType);

         // attributes to select from

         if (strDocbaseType.equals("epa_case") || strDocbaseType.equals("epa_nctims"))
            buildEpaCaseSelectAttributes(search);
         else
            buildSelectAttributes(search);

         // query for location
         boolean bDoSearch = buildLocationQuery(search);

         boolean fDoTextSearch = false;
         boolean fTextSearch = false;

         if (bDoSearch == true)
         {

		    if (strDocbaseType.equals("epa_case")|| strDocbaseType.equals("epa_nctims"))
		    {
			   DocbaseQuery tmpSearch = new DocbaseQuery();

			   fTextSearch = buildTextQuery(tmpSearch);

			   if (fTextSearch == true)
			   {
				   String strStmt = tmpSearch.getStatement();
				   StringTokenizer st = new StringTokenizer(strStmt, "'");
				   String buffer = st.nextToken();
				   strTextSearchPhrase = st.nextToken();
			   }
                           if (strDocbaseType.equals("epa_case")){
                            citiQueryString = buildCitiPropertiesQuery(search, "constituent");
                           }
            // properties
            bDoSearch |= buildPropertiesQuery(search, strDocbaseType);

		    }
		    else
		    {
			   // text

			   fDoTextSearch = buildTextQuery(search);
			   bDoSearch |= fDoTextSearch;
            // properties
            bDoSearch |= buildPropertiesQuery(search, strDocbaseType);
                 // I remove this criteria from epa_case and epa_nctims since I am going to just
                 //select one attribute: r_object_id, so no need for those
                           if (fDoTextSearch == false)
                           {
                            //  remove duplicate rows when a repeating attribute is selected.
                            search.addSelectValue("lower(object_name)", "lowerobjname");
                            search.addOrderByAttribute("lowerobjname", search.SORT_ASC);
                            search.addOrderByAttribute("r_object_id", search.SORT_ASC);
                            }
		    }






            // dates
            bDoSearch |= buildDateOptionsQuery(search);

            // size
            bDoSearch |= buildSizeQuery(search, strDocbaseType);

            bDoSearch |= buildAdvancedOptionQuery(search, strDocbaseType);


         }


         // prepare the search and get the query
         if (bDoSearch)
         {
            search.prepare();
            String strStatement = search.getStatement();

            // initialise the results page

            if (strDocbaseType.equals("epa_case")||strDocbaseType.equals("epa_nctims")){

                if ( citiQueryString != null){
                    /* since there is no order by any more, no need for this section
                    int index1 = strStatement.indexOf("ORDER BY");
                    String subStr1 = strStatement.substring(0, index1);
                    String subStr2 = strStatement.substring(index1, strStatement.length());
                     */


                    int i = strStatement.indexOf("WHERE");
                    if (i != -1){
                        strStatement = strStatement + " AND " + citiQueryString;
                    }
                    else{
                        strStatement = strStatement + " WHERE " + citiQueryString;
                    }

                }

                    if (fTextSearch == true)
                    {

                    list.add("textSearchPhrase", strTextSearchPhrase);
                    list.add("fulltextsearch", "true");
                    }
                    else
                    {
                    //add max return limit to search
                    //strStatement = strStatement + " enable(return_top " + maxReturnCount + ")";
                    //System.out.println("statement is " +  strStatement);
                    list.add("textSearchPhrase", "");
                    list.add("fulltextsearch", "false");
                    }

            }
	    else if ((strDocbaseType.equals("dm_document")) && (fDoTextSearch == true))
		list.add("fulltextsearch", "true");
            else
                list.add("fulltextsearch", "false");
            DataDropDownList returnLimit = (DataDropDownList)getControl("return_limit", DataDropDownList.class);
            String limitValue = returnLimit.getValue();
            System.out.println("max return " + limitValue);
            list.add("returnLimit",limitValue);
            list.add("docbaseType", strDocbaseType);
            list.add("queryType", "dql");
            list.add("query", strStatement);

            doSearch(list, getContext());
         }
      }
   }

   /**
    * Handle <b>onClickSearch</b> event.
    *
    * @param   button            The control that fired the <b>onClickSearch</b> event.
    * @param   args              The arguments to the event.
    */
   public void onClickClear(Button button, ArgumentList args)
   {
      resetControls();
      updateDateOptionControlStatus();
   }

   /**
    * Really do the search. The implementation nests the basic search component to do the search.
    *
    * @param args       The parameters collected from the UI controls
    * @param context    The current context
    */
   protected void doSearch(ArgumentList args, Context context)
   {
      setComponentJump("search", args, context);
   }

   ////////////////////////////////////////////////////////////////////////////////
   // Private methods

   /**
    * Create and setup all the controls that are to be reset if the clear button should be
    * used, so they can be accessed by the event handler. Read initial values from the users
    * Session so we keep data from previous Advanced Search for the duration of the Session.
    */
   private void setupControls()
   {
      // these controls have to be created before hand as their creation may just be needed diring event handling
      // The form processor disable control creation during an event is being processed.
      for (int iPropPan = 1; iPropPan < MAX_PANELS + 1; iPropPan++)
      {
          getControl(SEARCH_STRING + iPropPan, Text.class);
          getControl(DATE + iPropPan, DateInput.class);
          //added for citizen properties
          getControl(CITI_SEARCH_STRING + iPropPan, Text.class);
          getControl(CITI_DATE + iPropPan, DateInput.class);
      }

      // Text controls
      Text txtContained = (Text)getControl(CONTAINS_ALL, Text.class);
      txtContained.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CONTAINS_ALL));

      Text txtExact     = (Text)getControl(EXACT_PHRASE, Text.class);
      txtExact.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + EXACT_PHRASE));

      Text txtAny       = (Text)getControl(CONTAINS_ANY, Text.class);
      txtAny.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CONTAINS_ANY));

      Text txtWithout   = (Text)getControl(DOES_NOT_CONTAIN, Text.class);
      txtWithout.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + DOES_NOT_CONTAIN));

      Checkbox chkCase  = (Checkbox)getControl(CASE_CHECK, Checkbox.class);
      Boolean bChkCase = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + CASE_CHECK);
      if (bChkCase != null)
      {
         chkCase.setValue(bChkCase.booleanValue());
      }


      // Properties controls
      Text txtLocation              = (Text)getControl(LOCATION, Text.class);
      String strPrevLocation = (String)SessionState.getAttribute(ADVANCED_SEARCH + LOCATION);
      // we don't test against empty location as it is set on "clear"
      // this will set the location as the context string when the component is first initialized.
      if (strPrevLocation != null)
      {
         txtLocation.setValue(strPrevLocation);
      }

      DataDropDownList ddlParam1    = (DataDropDownList)getControl(PARAM_1, DataDropDownList.class);
      ddlParam1.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + PARAM_1));

      DataDropDownList ddlBooleans1 = (DataDropDownList)getControl(BOOLEANS_1, DataDropDownList.class);
      ddlBooleans1.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + BOOLEANS_1));

      Text txtSearch1               = (Text)getControl(SEARCH_FOR_1, Text.class);
      txtSearch1.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SEARCH_FOR_1));

      DataDropDownList ddlParam2    = (DataDropDownList)getControl(PARAM_2, DataDropDownList.class);
      ddlParam2.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + PARAM_2));

      DataDropDownList ddlBooleans2 = (DataDropDownList)getControl(BOOLEANS_2, DataDropDownList.class);
      ddlBooleans2.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + BOOLEANS_2));

      Text txtSearch2               = (Text)getControl(SEARCH_FOR_2, Text.class);
      txtSearch2.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SEARCH_FOR_2));

      DataDropDownList ddlParam3    = (DataDropDownList)getControl(PARAM_3, DataDropDownList.class);
      ddlParam3.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + PARAM_3));

      DataDropDownList ddlBooleans3 = (DataDropDownList)getControl(BOOLEANS_3, DataDropDownList.class);
      ddlBooleans3.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + BOOLEANS_3));

      Text txtSearch3               = (Text)getControl(SEARCH_FOR_3, Text.class);
      txtSearch3.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SEARCH_FOR_3));

      DataDropDownList ddlParam4    = (DataDropDownList)getControl(PARAM_4, DataDropDownList.class);
      ddlParam4.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + PARAM_4));

      DataDropDownList ddlBooleans4 = (DataDropDownList)getControl(BOOLEANS_4, DataDropDownList.class);
      ddlBooleans4.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + BOOLEANS_4));

      Text txtSearch4               = (Text)getControl(SEARCH_FOR_4, Text.class);
      txtSearch4.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SEARCH_FOR_4));

      DataDropDownList ddlParam5    = (DataDropDownList)getControl(PARAM_5, DataDropDownList.class);
      ddlParam5.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + PARAM_5));

      DataDropDownList ddlBooleans5 = (DataDropDownList)getControl(BOOLEANS_5, DataDropDownList.class);
      ddlBooleans5.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + BOOLEANS_5));

      Text txtSearch5               = (Text)getControl(SEARCH_FOR_5, Text.class);
      txtSearch5.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SEARCH_FOR_5));

      //citizen properties
      DataDropDownList ddlCitiParam1    = (DataDropDownList)getControl(CITI_PARAM_1, DataDropDownList.class);
      ddlCitiParam1.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_PARAM_1));

      DataDropDownList ddlCitiBooleans1 = (DataDropDownList)getControl(CITI_BOOLEANS_1, DataDropDownList.class);
      ddlCitiBooleans1.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_1));

      Text txtCitiSearch1               = (Text)getControl(CITI_SEARCH_FOR_1, Text.class);
      txtCitiSearch1.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_1));

      DataDropDownList ddlCitiParam2    = (DataDropDownList)getControl(CITI_PARAM_2, DataDropDownList.class);
      ddlCitiParam2.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_PARAM_2));

      DataDropDownList ddlBCitiooleans2 = (DataDropDownList)getControl(CITI_BOOLEANS_2, DataDropDownList.class);
      ddlBCitiooleans2.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_2));

      Text txtCitiSearch2               = (Text)getControl(CITI_SEARCH_FOR_2, Text.class);
      txtCitiSearch2.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_2));

      DataDropDownList ddlCitiParam3    = (DataDropDownList)getControl(CITI_PARAM_3, DataDropDownList.class);
      ddlCitiParam3.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_PARAM_3));

      DataDropDownList ddlCitiBooleans3 = (DataDropDownList)getControl(CITI_BOOLEANS_3, DataDropDownList.class);
      ddlCitiBooleans3.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_3));

      Text txtCitiSearch3               = (Text)getControl(CITI_SEARCH_FOR_3, Text.class);
      txtCitiSearch3.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_3));

      DataDropDownList ddlCitiParam4    = (DataDropDownList)getControl(CITI_PARAM_4, DataDropDownList.class);
      ddlCitiParam4.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_PARAM_4));

      DataDropDownList ddlCitiBooleans4 = (DataDropDownList)getControl(CITI_BOOLEANS_4, DataDropDownList.class);
      ddlCitiBooleans4.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_4));

      Text txtCitiSearch4               = (Text)getControl(CITI_SEARCH_FOR_4, Text.class);
      txtCitiSearch4.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_4));

      DataDropDownList ddlCitiParam5    = (DataDropDownList)getControl(CITI_PARAM_5, DataDropDownList.class);
      ddlCitiParam5.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_PARAM_5));

      DataDropDownList ddlCitiBooleans5 = (DataDropDownList)getControl(CITI_BOOLEANS_5, DataDropDownList.class);
      ddlCitiBooleans5.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_5));

      Text txtCitiSearch5               = (Text)getControl(CITI_SEARCH_FOR_5, Text.class);
      txtCitiSearch5.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_5));

      // Date controls
      DataDropDownList ddlDateParams = (DataDropDownList)getControl(DATE_PARAMS, DataDropDownList.class);
      ddlDateParams.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + DATE_PARAMS));

      DataDropDownList ddlInTheLast  = (DataDropDownList)getControl(TIME_PARAMS, DataDropDownList.class);
      ddlInTheLast.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + TIME_PARAMS));

      DateInput datStartDate         = (DateInput)getControl(START_DATE, DateInput.class);
      datStartDate.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + START_DATE));

      DateInput datEndDate           = (DateInput)getControl(END_DATE, DateInput.class);
      datEndDate.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + END_DATE));

      Radio radioInTheLast = (Radio) getControl(DATEOPTIONS_INTHELAST_RADIO, Radio.class);
      Boolean bRadioLast = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + DATEOPTIONS_INTHELAST_RADIO);
      if (bRadioLast != null)
      {
         radioInTheLast.setValue(bRadioLast.booleanValue());
      }
      else
      {
		 // inthelastradio will be unchecked by default for CMS
         radioInTheLast.setValue(false);
      }


      // Size controls
      DataDropDownList ddlSizeBoolean = (DataDropDownList)getControl(SIZE_PARAMS, DataDropDownList.class);
      ddlSizeBoolean.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SIZE_PARAMS));

      Text txtSize                    = (Text)getControl(SIZE, Text.class);
      txtSize.setValue((String)SessionState.getAttribute(ADVANCED_SEARCH + SIZE));


      // Advanced controls
      Checkbox chkHdn = (Checkbox)getControl(FIND_HIDDEN_CHECK, Checkbox.class);
      Boolean bChkHdn = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + FIND_HIDDEN_CHECK);
      if (bChkHdn != null)
      {
         chkHdn.setValue(bChkHdn.booleanValue());
      }

      Checkbox chkVer = (Checkbox)getControl(FIND_VERSION_CHECK, Checkbox.class);
      Boolean bChkVer = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + FIND_VERSION_CHECK);
      if (bChkVer != null)
      {
         chkVer.setValue(bChkVer.booleanValue());
      }


      // Options visibility
      Checkbox chkDate = (Checkbox)getControl(DATE_CHECK, Checkbox.class);
      Boolean bChkDate = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + DATE_CHECK);
      if (bChkDate != null)
      {
         chkDate.setValue(bChkDate.booleanValue());
         getControl(DATE_PANEL, Panel.class).setVisible(bChkDate.booleanValue());
      }

      Checkbox chkSize = (Checkbox)getControl(SIZE_CHECK, Checkbox.class);
      Boolean bChkSize = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + SIZE_CHECK);
      if (bChkSize != null)
      {
         chkSize.setValue(bChkSize.booleanValue());
         getControl(SIZE_PANEL, Panel.class).setVisible(bChkSize.booleanValue());
      }

      Checkbox chkAdv = (Checkbox)getControl(ADV_CHECK, Checkbox.class);
      Boolean bChkAdv = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + ADV_CHECK);
      if (bChkAdv != null)
      {
         chkAdv.setValue(bChkAdv.booleanValue());
         getControl(ADV_PANEL, Panel.class).setVisible(bChkAdv.booleanValue());
      }

      Checkbox chkText = (Checkbox)getControl(TEXT_CHECK, Checkbox.class);
      Boolean bChkText = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + TEXT_CHECK);
      if (bChkText != null)
      {
         chkText.setValue(bChkText.booleanValue());
         getControl(TEXT_PANEL, Panel.class).setVisible(bChkText.booleanValue());
      }

      Checkbox chkProp = (Checkbox)getControl(PROPERTIES_CHECK, Checkbox.class);
      Boolean bChkProp = (Boolean)SessionState.getAttribute(ADVANCED_SEARCH + PROPERTIES_CHECK);
      if (bChkProp != null)
      {
         chkProp.setValue(bChkProp.booleanValue());
         getControl(PROPERTIES_PANEL, Panel.class).setVisible(bChkProp.booleanValue());
         if (bChkProp.booleanValue() == true)
         {
            // find and show the first Add Property panel
            Panel property = (Panel)getControl(PANEL + "1");
            property.setVisible(true);
            Panel addProperty = (Panel)getControl(ADD_PROPERTIES_PANEL);
            addProperty.setVisible(true);
         }
      }

      // Types drop-down
      String strPrevType = (String)SessionState.getAttribute(ADVANCED_SEARCH + TYPELIST);
      if ( strPrevType != null)
      {
          DataDropDownList types = (DataDropDownList)getControl(TYPELIST, DataDropDownList.class);
          types.setValue(strPrevType);
          onTypeSelected(types, null);
      }
   }

   /**
    * Reset all the controls.
    */
   private void resetControls()
   {
       // Text controls
      Text txtContained = (Text)getControl(CONTAINS_ALL);
      txtContained.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CONTAINS_ALL, "");

      Text txtExact     = (Text)getControl(EXACT_PHRASE);
      txtExact.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + EXACT_PHRASE, "");

      Text txtAny       = (Text)getControl(CONTAINS_ANY);
      txtAny.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CONTAINS_ANY, "");

      Text txtWithout   = (Text)getControl(DOES_NOT_CONTAIN);
      txtWithout.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + DOES_NOT_CONTAIN, "");

      Checkbox chkCase  = (Checkbox)getControl(CASE_CHECK);
      chkCase.setValue(false);
      saveSessionAttribute(ADVANCED_SEARCH + CASE_CHECK, Boolean.FALSE);


      // Properties controls
      Text txtLocation              = (Text)getControl(LOCATION);
      txtLocation.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + LOCATION, "");

      Text txtSearch1               = (Text)getControl(SEARCH_FOR_1);
      txtSearch1.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_1, "");

      Text txtSearch2               = (Text)getControl(SEARCH_FOR_2);
      txtSearch2.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_2, "");

      Text txtSearch3               = (Text)getControl(SEARCH_FOR_3);
      txtSearch3.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_3, "");

      Text txtSearch4               = (Text)getControl(SEARCH_FOR_4);
      txtSearch4.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_4, "");

      Text txtSearch5               = (Text)getControl(SEARCH_FOR_5);
      txtSearch5.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_5, "");

      //citizen properties

      Text txtCitiSearch1               = (Text)getControl(CITI_SEARCH_FOR_1);
      txtCitiSearch1.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_1, "");

      Text txtCitiSearch2               = (Text)getControl(CITI_SEARCH_FOR_2);
      txtCitiSearch2.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_2, "");

      Text txtCitiSearch3               = (Text)getControl(CITI_SEARCH_FOR_3);
      txtCitiSearch3.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_3, "");

      Text txtCitiSearch4               = (Text)getControl(CITI_SEARCH_FOR_4);
      txtCitiSearch4.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_4, "");

      Text txtCitiSearch5               = (Text)getControl(CITI_SEARCH_FOR_5);
      txtCitiSearch5.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_5, "");

      // Date controls
      DataDropDownList ddlDateParams = (DataDropDownList)getControl(DATE_PARAMS);
      ddlDateParams.setValue(null);
      saveSessionAttribute(ADVANCED_SEARCH + DATE_PARAMS, null);

      DataDropDownList ddlInTheLast  = (DataDropDownList)getControl(TIME_PARAMS);
      ddlInTheLast.setValue(null);
      saveSessionAttribute(ADVANCED_SEARCH + TIME_PARAMS, null);

      DateInput datStartDate         = (DateInput)getControl(START_DATE);
      datStartDate.clear();
      saveSessionAttribute(ADVANCED_SEARCH + START_DATE, null);

      DateInput datEndDate           = (DateInput)getControl(END_DATE);
      datEndDate.clear();
      saveSessionAttribute(ADVANCED_SEARCH + END_DATE, null);


      // For CMS, we will make this default to false.
      Radio radioInTheLast           = (Radio)getControl(DATEOPTIONS_INTHELAST_RADIO);
      radioInTheLast.setValue(false);
      saveSessionAttribute(ADVANCED_SEARCH + DATEOPTIONS_INTHELAST_RADIO, Boolean.TRUE);


      // Size controls
      DataDropDownList ddlSizeBoolean = (DataDropDownList)getControl(SIZE_PARAMS);
      ddlSizeBoolean.setValue(null);
      saveSessionAttribute(ADVANCED_SEARCH + SIZE_PARAMS, null);

      Text txtSize                    = (Text)getControl(SIZE);
      txtSize.setValue("");
      saveSessionAttribute(ADVANCED_SEARCH + SIZE, "");


      // Advanced controls
      Checkbox chkHdn = (Checkbox)getControl(FIND_HIDDEN_CHECK);
      chkHdn.setValue(false);
      saveSessionAttribute(ADVANCED_SEARCH + FIND_HIDDEN_CHECK, null);

      Checkbox chkVer = (Checkbox)getControl(FIND_VERSION_CHECK);
      chkVer.setValue(false);
      saveSessionAttribute(ADVANCED_SEARCH + FIND_VERSION_CHECK, null);
   }

   /**
    * Save a session attribute value.
    *
    * @param strParamName the name of the session variable
    * @param strValue the value
    */
   private void saveSessionAttribute(String strParamName, Object strValue)
   {
      // JRun doesn't like null values
      if (strValue == null)
      {
         SessionState.removeAttribute(strParamName);
      }
      else
      {
         SessionState.setAttribute(strParamName, strValue);
      }
   }

   /**
    * Save all the control values to the users Session.
    */
   private void saveControls()
   {

      // Text controls
      Text txtContained = (Text)getControl(CONTAINS_ALL);
      saveSessionAttribute(ADVANCED_SEARCH + CONTAINS_ALL, txtContained.getValue());

      Text txtExact     = (Text)getControl(EXACT_PHRASE);
      saveSessionAttribute(ADVANCED_SEARCH + EXACT_PHRASE, txtExact.getValue());

      Text txtAny       = (Text)getControl(CONTAINS_ANY);
      saveSessionAttribute(ADVANCED_SEARCH + CONTAINS_ANY, txtAny.getValue());

      Text txtWithout   = (Text)getControl(DOES_NOT_CONTAIN);
      saveSessionAttribute(ADVANCED_SEARCH + DOES_NOT_CONTAIN, txtWithout.getValue());

      Checkbox chkCase  = (Checkbox)getControl(CASE_CHECK);
      saveSessionAttribute(ADVANCED_SEARCH + CASE_CHECK, new Boolean(chkCase.getValue()));


      // Properties controls
      Text txtLocation              = (Text)getControl(LOCATION);
      saveSessionAttribute(ADVANCED_SEARCH + LOCATION, txtLocation.getValue());

      DataDropDownList ddlParam1    = (DataDropDownList)getControl(PARAM_1);
      saveSessionAttribute(ADVANCED_SEARCH + PARAM_1, ddlParam1.getValue());

      DataDropDownList ddlBooleans1 = (DataDropDownList)getControl(BOOLEANS_1);
      saveSessionAttribute(ADVANCED_SEARCH + BOOLEANS_1, ddlBooleans1.getValue());

      Text txtSearch1               = (Text)getControl(SEARCH_FOR_1);
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_1, txtSearch1.getValue());

      DataDropDownList ddlParam2    = (DataDropDownList)getControl(PARAM_2);
      saveSessionAttribute(ADVANCED_SEARCH + PARAM_2, ddlParam2.getValue());

      DataDropDownList ddlBooleans2 = (DataDropDownList)getControl(BOOLEANS_2);
      saveSessionAttribute(ADVANCED_SEARCH + BOOLEANS_2, ddlBooleans2.getValue());

      Text txtSearch2               = (Text)getControl(SEARCH_FOR_2);
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_2, txtSearch2.getValue());

      DataDropDownList ddlParam3    = (DataDropDownList)getControl(PARAM_3);
      saveSessionAttribute(ADVANCED_SEARCH + PARAM_3, ddlParam3.getValue());

      DataDropDownList ddlBooleans3 = (DataDropDownList)getControl(BOOLEANS_3);
      saveSessionAttribute(ADVANCED_SEARCH + BOOLEANS_3, ddlBooleans3.getValue());

      Text txtSearch3               = (Text)getControl(SEARCH_FOR_3);
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_3, txtSearch3.getValue());

      DataDropDownList ddlParam4    = (DataDropDownList)getControl(PARAM_4);
      saveSessionAttribute(ADVANCED_SEARCH + PARAM_4, ddlParam4.getValue());

      DataDropDownList ddlBooleans4 = (DataDropDownList)getControl(BOOLEANS_4);
      saveSessionAttribute(ADVANCED_SEARCH + BOOLEANS_4, ddlBooleans4.getValue());

      Text txtSearch4               = (Text)getControl(SEARCH_FOR_4);
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_4, txtSearch4.getValue());

      DataDropDownList ddlParam5    = (DataDropDownList)getControl(PARAM_5);
      saveSessionAttribute(ADVANCED_SEARCH + PARAM_5, ddlParam5.getValue());

      DataDropDownList ddlBooleans5 = (DataDropDownList)getControl(BOOLEANS_5);
      saveSessionAttribute(ADVANCED_SEARCH + BOOLEANS_5, ddlBooleans5.getValue());

      Text txtSearch5               = (Text)getControl(SEARCH_FOR_5);
      saveSessionAttribute(ADVANCED_SEARCH + SEARCH_FOR_5, txtSearch5.getValue());


      //citizen properties

      DataDropDownList ddlCitiParam1    = (DataDropDownList)getControl(CITI_PARAM_1);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_PARAM_1, ddlCitiParam1.getValue());

      DataDropDownList ddlCitiBooleans1 = (DataDropDownList)getControl(CITI_BOOLEANS_1);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_1, ddlCitiBooleans1.getValue());

      Text txtCitiSearch1               = (Text)getControl(CITI_SEARCH_FOR_1);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_1, txtCitiSearch1.getValue());

      DataDropDownList ddlCitiParam2    = (DataDropDownList)getControl(CITI_PARAM_2);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_PARAM_2, ddlCitiParam2.getValue());

      DataDropDownList ddlCitiBooleans2 = (DataDropDownList)getControl(CITI_BOOLEANS_2);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_2, ddlCitiBooleans2.getValue());

      Text txtCitiSearch2               = (Text)getControl(CITI_SEARCH_FOR_2);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_2, txtCitiSearch2.getValue());

      DataDropDownList ddlCitiParam3    = (DataDropDownList)getControl(CITI_PARAM_3);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_PARAM_3, ddlCitiParam3.getValue());

      DataDropDownList ddlCitiBooleans3 = (DataDropDownList)getControl(CITI_BOOLEANS_3);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_3, ddlCitiBooleans3.getValue());

      Text txtCitiSearch3               = (Text)getControl(CITI_SEARCH_FOR_3);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_3, txtCitiSearch3.getValue());

      DataDropDownList ddlCitiParam4    = (DataDropDownList)getControl(CITI_PARAM_4);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_PARAM_4, ddlCitiParam4.getValue());

      DataDropDownList ddlCitiBooleans4 = (DataDropDownList)getControl(CITI_BOOLEANS_4);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_4, ddlCitiBooleans4.getValue());

      Text txtCitiSearch4               = (Text)getControl(CITI_SEARCH_FOR_4);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_4, txtCitiSearch4.getValue());

      DataDropDownList ddlCitiParam5    = (DataDropDownList)getControl(CITI_PARAM_5);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_PARAM_5, ddlCitiParam5.getValue());

      DataDropDownList ddlCitiBooleans5 = (DataDropDownList)getControl(CITI_BOOLEANS_5);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_BOOLEANS_5, ddlCitiBooleans5.getValue());

      Text txtCitiSearch5               = (Text)getControl(CITI_SEARCH_FOR_5);
      saveSessionAttribute(ADVANCED_SEARCH + CITI_SEARCH_FOR_5, txtCitiSearch5.getValue());

      // Date controls
      DataDropDownList ddlDateParams = (DataDropDownList)getControl(DATE_PARAMS);
      saveSessionAttribute(ADVANCED_SEARCH + DATE_PARAMS, ddlDateParams.getValue());

      DataDropDownList ddlInTheLast  = (DataDropDownList)getControl(TIME_PARAMS);
      saveSessionAttribute(ADVANCED_SEARCH + TIME_PARAMS, ddlInTheLast.getValue());

      DateInput datStartDate         = (DateInput)getControl(START_DATE);
      saveSessionAttribute(ADVANCED_SEARCH + START_DATE, datStartDate.getValue());

      DateInput datEndDate           = (DateInput)getControl(END_DATE);
      saveSessionAttribute(ADVANCED_SEARCH + END_DATE, datEndDate.getValue());

      Radio radioInTheLast           = (Radio)getControl(DATEOPTIONS_INTHELAST_RADIO);
      saveSessionAttribute(ADVANCED_SEARCH + DATEOPTIONS_INTHELAST_RADIO, new Boolean(radioInTheLast.getValue()));


      // Size controls
      DataDropDownList ddlSizeBoolean = (DataDropDownList)getControl(SIZE_PARAMS);
      saveSessionAttribute(ADVANCED_SEARCH + SIZE_PARAMS, ddlSizeBoolean.getValue());

      Text txtSize                    = (Text)getControl(SIZE);
      saveSessionAttribute(ADVANCED_SEARCH + SIZE, txtSize.getValue());


      // Advanced controls
      Checkbox chkHdn = (Checkbox)getControl(FIND_HIDDEN_CHECK);
      saveSessionAttribute(ADVANCED_SEARCH + FIND_HIDDEN_CHECK, new Boolean(chkHdn.getValue()));

      Checkbox chkVer = (Checkbox)getControl(FIND_VERSION_CHECK);
      saveSessionAttribute(ADVANCED_SEARCH + FIND_VERSION_CHECK, new Boolean(chkVer.getValue()));


      // Options visibility
      Checkbox chkDate = (Checkbox)getControl(DATE_CHECK, Checkbox.class);
      saveSessionAttribute(ADVANCED_SEARCH + DATE_CHECK, new Boolean(chkDate.getValue()));

      Checkbox chkSize = (Checkbox)getControl(SIZE_CHECK, Checkbox.class);
      saveSessionAttribute(ADVANCED_SEARCH + SIZE_CHECK, new Boolean(chkSize.getValue()));

      Checkbox chkAdv = (Checkbox)getControl(ADV_CHECK, Checkbox.class);
      saveSessionAttribute(ADVANCED_SEARCH + ADV_CHECK, new Boolean(chkAdv.getValue()));

      Checkbox chkText = (Checkbox)getControl(TEXT_CHECK, Checkbox.class);
      saveSessionAttribute(ADVANCED_SEARCH + TEXT_CHECK, new Boolean(chkText.getValue()));

      Checkbox chkProp = (Checkbox)getControl(PROPERTIES_CHECK, Checkbox.class);
      saveSessionAttribute(ADVANCED_SEARCH + PROPERTIES_CHECK, new Boolean(chkProp.getValue()));


      // Types drop-down
      DataDropDownList types = (DataDropDownList)getControl(TYPELIST, DataDropDownList.class);
      saveSessionAttribute(ADVANCED_SEARCH + TYPELIST, types.getValue());
   }


   /**
    * Create a <b>Date</b> object from a control.

    * @param   oDateControl   A date input control
    * @return  The <b>Date</b> object, or <b>null</b> if one of the date fields is not set.
    */
   private Date createDate(DateInput oDateControl)
   {
      Date dateObj = null;

      int year = oDateControl.getYear();
      int month = oDateControl.getMonth();
      int day = oDateControl.getDay();

      if (day != -1 && month != -1 && year != -1)
      {
         Calendar calendar = Calendar.getInstance(LocaleService.getLocale());
         calendar.set(year, month - 1, day);
         dateObj = calendar.getTime();
      }

      return  dateObj;
   }


   /**
    * Update the date option control status.
    */
   private void updateDateOptionControlStatus()
   {
      Radio radioInTheLast = (Radio) getControl(DATEOPTIONS_INTHELAST_RADIO, Radio.class);
      Radio radioBetween = (Radio) getControl(DATEOPTIONS_BETWEEN_RADIO, Radio.class);

      DataDropDownList ddlInTheLast  = (DataDropDownList)getControl(TIME_PARAMS, DataDropDownList.class);
      DateInput datStartDate         = (DateInput)getControl(START_DATE, DateInput.class);
      DateInput datEndDate           = (DateInput)getControl(END_DATE, DateInput.class);
      if (radioInTheLast.getValue() == true)
      {
         // change the dropdown date options
         radioBetween.setValue(false);
         ddlInTheLast.setEnabled(true);
         datStartDate.setEnabled(false);
         datEndDate.setEnabled(false);
      }
      else
      {
         radioBetween.setValue(true);
         ddlInTheLast.setEnabled(false);
         datStartDate.setEnabled(true);
         datEndDate.setEnabled(true);
      }
   }



   private void buildEpaCaseSelectAttributes(DocbaseQuery searchQuery)
   {
	  // add internal attributes to select
	  for (int iCount=0; iCount<INTERNAL_EPA_CASE_ATTRIBUTES.length; iCount++)
	  {
		searchQuery.addSelectValue(INTERNAL_EPA_CASE_ATTRIBUTES[iCount], "");
	  }
   }

   /**
    * Build the attributes to be selected in the query
    *
    * @param searchQuery      The query object to bulid
    */
   private void buildSelectAttributes(DocbaseQuery searchQuery)
   {
      // add internal attributes to select
      for (int iCount=0; iCount<INTERNAL_ATTRIBUTES.length; iCount++)
      {
         searchQuery.addSelectValue(INTERNAL_ATTRIBUTES[iCount], "");
      }
   }

   /**
    * Build a query for the location.
    *
    * @param searchQuery    The query object to bulid
    * @return <b>true</b> if the locations are valid.
    */
   private boolean buildLocationQuery(DocbaseQuery searchQuery)
   {
      boolean fDoSearch = true;

      // get all the controls and their values
      Text location = (Text)getControl("location", Text.class);
      String strValue = location.getValue();

      // if the location field is set add the folder where clause
      if (strValue != null)
      {
         StringTokenizer allTokens = new StringTokenizer(strValue,";");
         Vector vecAll = new Vector();
         while (allTokens.hasMoreTokens())
         {
            String strToken = allTokens.nextToken().trim();

            // ignore root
            if (strToken.length() > 0 && strToken.equals("/") == false)
            {
               if (strToken.charAt(0) != '/')
               {
                  strToken = "/" + strToken;
               }

               // test the existentance, and give a warning if folder is inValid
               String strFolderId = null;
               try
               {
                  strFolderId = FolderUtil.getFolderId(strToken);
               }
               catch(Exception e) {}

               if (strFolderId != null && strFolderId.length() > 0)
               {
                  vecAll.add(strToken);
               }
               else
               {
                  MessageService.addDetailedMessage(this, "MSG_FOLDER_NOTEXIST", new String[]{strToken}, null, true);
                  fDoSearch = false;
               }
            }
         }

         // add to the query if there is any location
         if (fDoSearch == true && vecAll.size() > 0)
         {
            FolderExpression folderExpr = new FolderExpression(vecAll);
            folderExpr.setUseIDFunction(false);
            searchQuery.addWhereClause(LogicalOperator.AND, folderExpr);
         }
      }

      return fDoSearch;
   }

   /**
    * Build the query for "contains all words or phrase".
    *
    * @param searchQuery      The query object to bulid
    * @param bCaseSensitive   The case sensitive flag
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildTextContainsAllQuery(DocbaseQuery searchQuery, boolean bCaseSensitive)
   {
      boolean bDoSearch = false;

      Text containsAll = (Text)getControl(CONTAINS_ALL);
      Text exactPhrase = (Text)getControl(EXACT_PHRASE);

      // all words or phase
      Vector vecContainsAll = new Vector(10);
      String strContainsAll = containsAll.getValue();
      if (strContainsAll != null)
      {
         // each individual word is a token
         SearchWordTokenizer wordTokenizer = new SearchWordTokenizer(strContainsAll);
         while (wordTokenizer.hasMoreTokens() == true)
         {
            String strWord = wordTokenizer.nextToken();
            if (strWord.length() > 0)
            {
               vecContainsAll.add(strWord);
            }
         }
      }

      // phase
      String strPhases = exactPhrase.getValue();
      if (strPhases != null && strPhases.length() > 0)
      {
         vecContainsAll.add(strPhases);
      }

      if (vecContainsAll.size() > 0)
      {
         bDoSearch = true;
         VerityExpression all = new VerityExpression(vecContainsAll);
         all.setAnd(true);
         all.setCaseSensitive(bCaseSensitive);
         searchQuery.addVerityWhereClause(LogicalOperator.AND, all);
      }

      return bDoSearch;
   }

   /**
    * Build the query for "contains any".
    *
    * @param searchQuery      The query object to bulid
    * @param bCaseSensitive   The case sensitive flag
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildTextContainsAnyQuery(DocbaseQuery searchQuery, boolean bCaseSensitive)
   {
      boolean bDoSearch = false;
      Text containsAny = (Text)getControl(CONTAINS_ANY);

      // any words
      Vector vecContainsAny = new Vector(10);
      String strContainsAny = containsAny.getValue();
      if (strContainsAny != null)
      {
         // each individual word is a token
         SearchWordTokenizer wordTokenizer = new SearchWordTokenizer(strContainsAny);
         while (wordTokenizer.hasMoreTokens() == true)
         {
            String strWord = wordTokenizer.nextToken();
            if (strWord.length() > 0)
            {
               vecContainsAny.add(strWord);
            }
         }
      }

      if (vecContainsAny.size() > 0)
      {
         bDoSearch = true;
         VerityExpression any = new VerityExpression(vecContainsAny);
         any.setAccrue(true);
         any.setCaseSensitive(bCaseSensitive);
         searchQuery.addVerityWhereClause(LogicalOperator.AND, any);
      }

      return bDoSearch;
   }

   /**
    * Builds the query for "does not contain".
    *
    * @param searchQuery      The query object to bulid
    * @param bCaseSensitive   The case sensitive flag
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildTextNotContainsQuery(DocbaseQuery searchQuery, boolean bCaseSensitive)
   {
      boolean bDoSearch = false;
      Text doesNotContain = (Text)getControl(DOES_NOT_CONTAIN);

      // all words or phase
      Vector vecWords = new Vector(10);
      String strWords = doesNotContain.getValue();
      if (strWords != null)
      {
         // each individual word is a token
         SearchWordTokenizer wordTokenizer = new SearchWordTokenizer(strWords);
         while (wordTokenizer.hasMoreTokens() == true)
         {
            String strWord = wordTokenizer.nextToken();
            if (strWord.length() > 0)
            {
               vecWords.add(strWord);
            }
         }
      }

      if (vecWords.size() > 0)
      {
         bDoSearch = true;
         VerityExpression notContained = new VerityExpression(vecWords);
         notContained.setAnd(true);
         notContained.setNegate(true);
         notContained.setCaseSensitive(bCaseSensitive);
         searchQuery.addVerityWhereClause(LogicalOperator.AND, notContained);
      }

      return bDoSearch;
   }

   /**
    * Build the query for full-text search.
    *
    * @param searchQuery      The query object to bulid
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildTextQuery(DocbaseQuery searchQuery)
   {
      boolean bDoSearch = false;

      // if the full-text options are visible get the associated controls and build the query
      Panel textPanel = (Panel)getControl(TEXT_PANEL);
      if (textPanel.isVisible())
      {
         // if case sensitive search is set need to prepend a <CASE> to the verity query
         boolean bCaseSensitive = false;
         Checkbox caseCheck = (Checkbox)getControl(CASE_CHECK);
         if (caseCheck.getValue())
         {
            bCaseSensitive = true;
         }

         bDoSearch = bDoSearch | buildTextContainsAllQuery(searchQuery, bCaseSensitive);
         bDoSearch = bDoSearch | buildTextContainsAnyQuery(searchQuery, bCaseSensitive);
         bDoSearch = bDoSearch | buildTextNotContainsQuery(searchQuery, bCaseSensitive);
         if (bDoSearch == true)
         {
            // add score attribute so that we can rank the search results
            searchQuery.addSelectValue("score","");
         }
      }

      return bDoSearch;
   }

   /**
    * Build the query for properties.
    *
    * @param searchQuery      The query object to bulid
    * @param strDocbaseType   The docbase type for the query.
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildPropertiesQuery(DocbaseQuery searchQuery, String strDocbaseType)
   {
      boolean bDoSearch = false;

      // if the property panel is visible get its controls and add their values to the query
      Panel propertyPanel = (Panel)getControl(PROPERTIES_PANEL);
      if (propertyPanel.isVisible())
      {
         DataDropDownList[] params = new DataDropDownList[MAX_PANELS + 1];
         DataDropDownList[] booleans = new DataDropDownList[MAX_PANELS + 1];
         Text[] searchStrings = new Text[MAX_PANELS + 1];
         DateInput[] dateInputs = new DateInput[MAX_PANELS + 1];
         Panel[] panels = new Panel[MAX_PANELS + 1];
         for (int i = 1; i < MAX_PANELS + 1; i++)
         {
            params[i] = (DataDropDownList)getControl(PARAM_LIST + i);
            booleans[i] = (DataDropDownList)getControl(BOOLEAN_LIST + i);
            searchStrings[i] = (Text)getControl(SEARCH_STRING + i);
            dateInputs[i] = (DateInput)getControl(DATE + i);
            panels[i] = (Panel)getControl(PANEL + i);
         }

         // set all the where clauses
         for (int j = 1; j < MAX_PANELS + 1; j++)
         {
            if (panels[j].isVisible())
            {
               // check whether to build a time or a string expression
               if (searchStrings[j].isVisible())
               {
                  String strId = params[j].getValue();
                  String strValue = formatPropertyValue(strDocbaseType, strId, searchStrings[j].getValue());
                  if (strValue != null)
                  {
                     bDoSearch = true;
                     StringExpression exp = new StringExpression(null, new DocbaseAttribute(strId, strDocbaseType), new Predicate(booleans[j].getValue()), strValue);
                     searchQuery.addWhereClause(LogicalOperator.AND, exp);
                  }
               }
               else if (dateInputs[j].isVisible())
               {
                  if (dateInputs[j].isEnabled())
                  {
                     String strId = params[j].getValue();
                     Date  dateObj = createDate(dateInputs[j]);
                     if (dateObj != null)
                     {
                        bDoSearch = true;
                        //TimeExpression time = new TimeExpression(null, new DocbaseAttribute(strId, strDocbaseType), new Predicate(booleans[j].getValue()), dateObj, true);
                        String predicateString = (new Predicate(booleans[j].getValue())).getSymbol();
                        System.out.println("time string " + predicateString);
                        if (predicateString.equals("=")){
                            Expression time = getDateExpression(new DocbaseAttribute(strId, strDocbaseType),">=", dateObj);
                            searchQuery.addWhereClause(LogicalOperator.AND, time);
                            Date dateObj1 = new Date(86400000 + dateObj.getTime());
                            Expression time1 = getDateExpression(new DocbaseAttribute(strId, strDocbaseType),"<", dateObj1);
                            searchQuery.addWhereClause(LogicalOperator.AND, time1);
                        }
                        else{                          
                            Expression time = getDateExpression(new DocbaseAttribute(strId, strDocbaseType), (new Predicate(booleans[j].getValue())).getSymbol(), dateObj);
                            searchQuery.addWhereClause(LogicalOperator.AND, time);
                        }
                     }
                  }
               }
               else // boolean
               {
                  bDoSearch = true;
                  String strBooleanCondition = booleans[j].getValue();
                  String strId = params[j].getValue();
                  BooleanExpression boolExpression;
                  if (strBooleanCondition.equals(getString("MSG_IS_TRUE")) == true)
                  {
                     boolExpression = new BooleanExpression(null, strId, Predicate.OBJ_EQ, "true");
                  }
                  else
                  {
                     boolExpression = new BooleanExpression(null, strId, Predicate.OBJ_EQ, "false");
                  }
                  searchQuery.addWhereClause(LogicalOperator.AND, boolExpression);
               }
            }
         }
      }

      return bDoSearch;
   }
   /**
    * build the date time expression for date options query.
    *
    * - avoid using TimeExpression as the dql func DateFloor() doesn't work for far-east version of SQL server.
    * - applies to only ge or le operations.
    * @param dateAttribute         the date attribute
    * @param strPredicate          the predicate
    * @param dateValue           the value
    * @return an expression for the search clause
    */
   private Expression getDateExpression(DocbaseAttribute dateAttribute, String strPredicate, Date dateValue)
   {
      String strDatefloorPattern = "MM/dd/yyyy";

      StringBuffer bufExpression = new StringBuffer(256);
      if (dateAttribute.isRepeating())
      {
         bufExpression.append("any ");
      }
      bufExpression.append(dateAttribute.getAttribute());
      bufExpression.append(strPredicate);
      bufExpression.append(" DATE('");
      bufExpression.append(new SimpleDateFormat(strDatefloorPattern).format(dateValue));
      bufExpression.append("', '");
      bufExpression.append(strDatefloorPattern);
      bufExpression.append("')");

      return new ParsedExpression(bufExpression.toString());
   }

      /**
    * Build the query for properties.
    *
    * @param searchQuery      The query object to bulid
    * @param strDocbaseType   The docbase type for the query.
    * @return <b>true</b> if there is anything worth for a real query
    */
   private String buildCitiPropertiesQuery(DocbaseQuery searchQuery, String strDocbaseType)
   {
      boolean bDoSearch = false;
      String citiSearchString = "( any constituent in (";
      String constituentSearchString = ") or any real_constituent in (";
      DocbaseQuery citiQuery = new DocbaseQuery();
      citiQuery.setFromClause("constituent");
      citiQuery.addSelectValue("r_object_id","");

      // if the property panel is visible get its controls and add their values to the query
      Panel propertyPanel = (Panel)getControl(CITI_PROPERTIES_PANEL);
      if (propertyPanel.isVisible())
      {
         DataDropDownList[] params = new DataDropDownList[MAX_PANELS + 1];
         DataDropDownList[] booleans = new DataDropDownList[MAX_PANELS + 1];
         Text[] searchStrings = new Text[MAX_PANELS + 1];
         DateInput[] dateInputs = new DateInput[MAX_PANELS + 1];
         Panel[] panels = new Panel[MAX_PANELS + 1];
         for (int i = 1; i < MAX_PANELS + 1; i++)
         {
            params[i] = (DataDropDownList)getControl(CITI_PARAM_LIST + i);
            booleans[i] = (DataDropDownList)getControl(CITI_BOOLEAN_LIST + i);
            searchStrings[i] = (Text)getControl(CITI_SEARCH_STRING + i);
            dateInputs[i] = (DateInput)getControl(CITI_DATE + i);
            panels[i] = (Panel)getControl(CITI_PANEL + i);
         }

         // set all the where clauses
         for (int j = 1; j < MAX_PANELS + 1; j++)
         {
            if (panels[j].isVisible())
            {
               // check whether to build a time or a string expression
               if (searchStrings[j].isVisible())
               {
                  String strId = params[j].getValue();
                  String strInitValue = searchStrings[j].getValue();
                  String strValue = null;
                if (strInitValue != null)
               {
                  strValue = strInitValue.trim();
                  if (strValue.length() == 0)
                  {
                     strValue = null;
                  }
                }
                  //String strValue = formatPropertyValue(strDocbaseType, strId, searchStrings[j].getValue());
                  if (strValue != null)
                  {
                     bDoSearch = true;

                     StringExpression exp = new StringExpression(null, new DocbaseAttribute(strId, strDocbaseType), new Predicate(booleans[j].getValue()), strValue);
                     //searchQuery.addWhereClause(LogicalOperator.AND, exp);
                     citiQuery.addWhereClause(LogicalOperator.AND, exp);
                  }
               }

               //only text search for citizen, so ingnore other cases.
               /*
               else if (dateInputs[j].isVisible())
               {
                  if (dateInputs[j].isEnabled())
                  {
                     String strId = params[j].getValue();
                     Date  dateObj = createDate(dateInputs[j]);
                     if (dateObj != null)
                     {
                        bDoSearch = true;
                        TimeExpression time = new TimeExpression(null, new DocbaseAttribute(strId, strDocbaseType), new Predicate(booleans[j].getValue()), dateObj, true);
                        searchQuery.addWhereClause(LogicalOperator.AND, time);
                     }
                  }
               }
               else // boolean
               {
                  bDoSearch = true;
                  String strBooleanCondition = booleans[j].getValue();
                  String strId = params[j].getValue();
                  BooleanExpression boolExpression;
                  if (strBooleanCondition.equals(getString("MSG_IS_TRUE")) == true)
                  {
                     boolExpression = new BooleanExpression(null, strId, Predicate.OBJ_EQ, "true");
                  }
                  else
                  {
                     boolExpression = new BooleanExpression(null, strId, Predicate.OBJ_EQ, "false");
                  }
                  searchQuery.addWhereClause(LogicalOperator.AND, boolExpression);
               }
                */
            }
         }
      }
      citiQuery.prepare();
      citiSearchString = citiSearchString + citiQuery.getStatement() +  constituentSearchString + citiQuery.getStatement() + "))";
      //System.out.println("search string is " + citiSearchString);
      if (bDoSearch == true)
        return citiSearchString;
      else
        return null;
      //return bDoSearch;
   }
   /**
    * Build the query for date options.
    *
    * @param searchQuery      The query object to bulid
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildDateOptionsQuery(DocbaseQuery searchQuery)
   {
      boolean bDoSearch = false;
      // get the extended options (if required)
      Panel datePanel = (Panel)getControl(DATE_PANEL);
      if (datePanel.isVisible())
      {
         Radio radioInTheLast = (Radio) getControl(DATEOPTIONS_INTHELAST_RADIO, Radio.class);
         if (radioInTheLast.getValue() == true)
         {
            bDoSearch = buildInTheLastDateQuery(searchQuery);
         }
         else
         {
            bDoSearch = buildBetweenDatesQuery(searchQuery);
         }
      }

      return bDoSearch;
   }

   /**
    * build the query for date from 'in the last dates' control
    *
    * @param searchQuery      The query object to bulid
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildInTheLastDateQuery(DocbaseQuery searchQuery)
   {
      boolean bDoSearch = false;
      DataDropDownList timeElapsed = (DataDropDownList)getControl(TIME_PARAMS, DataDropDownList.class);
      String strValue = timeElapsed.getValue();
      if (strValue != null )
      {
         Locale locale = LocaleService.getLocale();
         Calendar calendar = Calendar.getInstance(locale);

         // roll back the date to the amount specified
         bDoSearch = true;
         if (strValue.equals("day") == true)
         {
            calendar.add(calendar.DATE, -1);
         }
         else if (strValue.equals("week") == true)
         {
            calendar.add(calendar.DATE, -7);
         }
         else if (strValue.equals("month") == true)
         {
            calendar.add(calendar.MONTH, -1);
         }
         else if (strValue.equals("year") == true)
         {
            calendar.add(calendar.YEAR, -1);
         }
         else
         {
            bDoSearch = false;
         }

         if (bDoSearch == true)
         {
           DataDropDownList dateAttribute = (DataDropDownList)getControl(DATE_PARAMS, DataDropDownList.class);

           Date date = calendar.getTime();
           TimeExpression startTime = new TimeExpression(null, dateAttribute.getValue(), new Predicate(">="), date, true);
           searchQuery.addWhereClause(LogicalOperator.AND, startTime);
         }
      }

      return bDoSearch;
   }

   /**
    * Build the query for date from 'date range' control
    *
    * @param searchQuery      The query object to bulid
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildBetweenDatesQuery(DocbaseQuery searchQuery)
   {
      boolean bDoSearch = false;

      // get date attribute for the query
      DataDropDownList dateAttribute = (DataDropDownList)getControl(DATE_PARAMS, DataDropDownList.class);
      String strDateAttr = dateAttribute.getValue();

      DateInput startDateInput = (DateInput)getControl(START_DATE, DateInput.class);
      DateInput endDateInput = (DateInput)getControl(END_DATE, DateInput.class);

      // date ranges
      Date startDate = createDate(startDateInput);
      Date endDate = createDate(endDateInput);
      TimeExpression startTimeExpression = null;
      TimeExpression endTimeExpression = null;

      // test the case where one date is null
      if (startDate == null)
      {
         if (endDate != null)
         {
            endTimeExpression = new TimeExpression(null, strDateAttr, new Predicate("<="), endDate, true);
         }
      }
      else if (endDate == null)
      {
         if (startDate != null)
         {
            startTimeExpression = new TimeExpression(null, strDateAttr, new Predicate(">="), startDate, true);
         }
      }
      else if (startDate.before(endDate) == true)  // swap ends if required
      {
         startTimeExpression = new TimeExpression(null, strDateAttr, new Predicate(">="), startDate, true);
         endTimeExpression = new TimeExpression(null, strDateAttr, new Predicate("<="), endDate, true);
      }
      else
      {
         startTimeExpression = new TimeExpression(null, strDateAttr, new Predicate(">="), endDate, true);
         endTimeExpression = new TimeExpression(null, strDateAttr, new Predicate("<="), startDate, true);
      }

      if (startTimeExpression != null)
      {
         bDoSearch = true;
         searchQuery.addWhereClause(LogicalOperator.AND, startTimeExpression);
      }

      if (endTimeExpression != null)
      {
         bDoSearch = true;
         searchQuery.addWhereClause(LogicalOperator.AND, endTimeExpression);
      }

      return bDoSearch;
   }

   /**
    * Build the query for size options.
    *
    * @param searchQuery      The query object to bulid
    * @param strDocbaseType   The docbase type for the query.
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildSizeQuery(DocbaseQuery searchQuery, String strDocbaseType)
   {
      boolean bDoSearch = false;
      Panel sizePanel = (Panel)getControl(SIZE_PANEL);
      if (sizePanel.isVisible())
      {
         DataDropDownList sizeBoolean = (DataDropDownList)getControl(SIZE_PARAMS);
         Text size = (Text)getControl(SIZE);
         String strSize = size.getValue();
         if (strSize != null && strSize.trim().length() != 0)
         {
            NumberFormat nf = NumberFormat.getInstance(LocaleService.getLocale());
            long lSize = 0;
            try
            {
               lSize = (long) (nf.parse(strSize).doubleValue() * 1024.0);
               StringExpression exp = new StringExpression(null, new DocbaseAttribute("r_content_size", strDocbaseType), new Predicate(sizeBoolean.getValue()), Long.toString(lSize));
               searchQuery.addWhereClause(LogicalOperator.AND, exp);
               bDoSearch = true;
            }
            catch(ParseException parseExcept)
            {
               MessageService.addDetailedMessage(this, "MSG_INVALID_SIZE", new String[]{strSize}, null, true);
            }
         }
      }

      return bDoSearch;
   }

   /**
    * Build the query for advanced options.
    *
    * @param searchQuery      The query object to bulid
    * @param strDocbaseType   The docbase type for the query.
    * @return <b>true</b> if there is anything worth for a real query
    */
   private boolean buildAdvancedOptionQuery(DocbaseQuery searchQuery, String strDocbaseType)
   {
      boolean bDoSearch = false;

      Panel advPanel = (Panel)getControl(ADV_PANEL);
      if (advPanel.isVisible())
      {
         Checkbox hiddenCheck = (Checkbox)getControl(FIND_HIDDEN_CHECK);
         if (hiddenCheck.getValue() == true)
         {
            BooleanExpression exp = new BooleanExpression(null, "a_is_hidden", new Predicate("="), "true");
            searchQuery.addWhereClause(LogicalOperator.AND, exp);
         }

         Checkbox versionCheck = (Checkbox)getControl(FIND_VERSION_CHECK);
         if (versionCheck.isEnabled() == true && versionCheck.getValue() == true)
         {
            searchQuery.setFromClause(strDocbaseType + " (all)");
         }
      }

      return bDoSearch;
   }

   /**
    * Set the from and to year range based on the configuration settings.
    *
    * @param dateCtrl   The date control
    */
   private void setYearRange(DateInput dateCtrl)
   {
      if (m_strDateCtrlFromYear == null)
      {
         // get the from year
         m_strDateCtrlFromYear = "1990";
         IConfigElement cfgFromYear = lookupElement("date_controls.fromyear");
         if (cfgFromYear != null)
         {
            String strThisFromYear = cfgFromYear.getValue().trim();
            if (strThisFromYear.length() > 0)
            {
               m_strDateCtrlFromYear = strThisFromYear;
            }
         }

         // config to year
         Calendar calendarToYear = Calendar.getInstance();
         IConfigElement cfgFutureYears = lookupElement("date_controls.minyearsfromnow");
         if (cfgFutureYears != null)
         {
            String strFutureYears = cfgFutureYears.getValue().trim();
            if (strFutureYears.length() > 0)
            {
               int nFutureYears = Integer.parseInt(strFutureYears);
               calendarToYear.roll(calendarToYear.YEAR, nFutureYears);
            }
         }
         m_strDateCtrlToYear = Integer.toString(calendarToYear.get(calendarToYear.YEAR));
      }

      dateCtrl.setFromYear(Integer.valueOf(m_strDateCtrlFromYear).intValue());
      dateCtrl.setToYear(Integer.valueOf(m_strDateCtrlToYear).intValue());
   }

   /**
    * Validate and format an input value.
    *
    * @param strDocbaseType      The search docbase type.
    * @param strAttributeName    The attribute name
    * @param strValue            The attribute value
    * @return The formatted string.
    */
   private String formatPropertyValue(String strDocbaseType, String strAttributeName, String strValue)
   {
      String strFormattedValue = null;
      try
      {
         IDfType iType = getDfSession().getType(strDocbaseType);
         switch (iType.getTypeAttrDataType(strAttributeName))
         {
            case IDfType.DF_ID:
            case IDfType.DF_STRING:
            case IDfType.DF_UNDEFINED:
               if (strValue != null)
               {
                  strFormattedValue = strValue.trim();
                  if (strFormattedValue.length() == 0)
                  {
                     strFormattedValue = null;
                  }
               }
               break;

            case IDfType.DF_TIME:
            case IDfType.DF_BOOLEAN:
               strFormattedValue = strValue;
               break;

            case IDfType.DF_INTEGER:
               NumberFormat nfInt = NumberFormat.getInstance(LocaleService.getLocale());
               try
               {
                  nfInt.setParseIntegerOnly(true);
                  int intValue = nfInt.parse(strValue).intValue();
                  strFormattedValue = Integer.toString(intValue);
               }
               catch(ParseException parseExcept)
               {
                  MessageService.addDetailedMessage(this, "MSG_INVALID_INTEGER", new String[]{strValue}, null, true);
               }
               break;

            default: // floating numbers which have different types depending dmserver versions
               NumberFormat nf = NumberFormat.getInstance(LocaleService.getLocale());
               try
               {
                  nf.setParseIntegerOnly(false);
                  double dblValue = nf.parse(strValue).doubleValue();
                  strFormattedValue = Double.toString(dblValue);
               }
               catch(ParseException parseExcept)
               {
                  MessageService.addDetailedMessage(this, "MSG_INVALID_NUMBER", new String[]{strValue}, null, true);
               }
               break;
         }
      }
      catch(Exception e)
      {
         throw new WrapperRuntimeException(e);
      }

      return strFormattedValue;
   }

   ////////////////////////////////////////////////////////////////////////////////
   // Private data

   private ConfigResultSet[] m_currentParamSet = new ConfigResultSet[6];
   private ConfigResultSet[] m_currentBooleanSet = new ConfigResultSet[6];
   private ConfigResultSet m_currentDateOptions = null;
   private ConfigResultSet m_currentDateTimes = null;
   private ConfigResultSet m_currentSizeOptions = null;

   final static private String ADVANCED_SEARCH = "_advsearch_";

   final static private String INDEX = "index";
   final static private String DATE_PANEL = "dateoptions";
   final static private String DATE_CHECK = "dateoptionscheck";
   final static private String SIZE_PANEL = "sizeoptions";
   final static private String SIZE_CHECK = "sizeoptionscheck";
   final static private String ADV_PANEL = "advoptions";
   final static private String ADV_CHECK = "advoptionscheck";
   final static private String ADD_BUTTON = "addproperty";
   final static private String REMOVE_BUTTON = "removeaction";
   final static private String SEARCH_TYPES = "search_types";
   final static private String PARAM_LIST = "param";
   final static private String BOOLEAN_LIST = "booleans";
   final static private String NAME = "name";
   final static private String DATE_OPTIONS = "date_options";
   final static private String SIZE_OPTIONS = "size_options";
   final static private String DATE_CONDITIONS = "date_conditions";
   final static private String ELAPSED_TIMES = "elapsed_times";
   final static private String DATEOPTIONS_INTHELAST_RADIO = "inthelastradio";
   final static private String DATEOPTIONS_BETWEEN_RADIO = "betweenradio";
   final static private String SIZE_CONDITIONS = "size_conditions";
   final static private String DATE_PARAMS = "dateparams";
   final static private String TIME_PARAMS = "inthelast";
   final static private String SIZE_PARAMS = "sizeboolean";
   final static private String SEARCH_STRING = "searchfor";
   final static private String PANEL = "set";
   final static private String DOCBASE_ATTRIBUTE = "docbase_attribute";
   final static private String SIZE = "size";
   final static private String CASE_CHECK = "casecheck";
   final static private String FIND_HIDDEN_CHECK = "findhiddencheck";
   final static private String FIND_VERSION_CHECK = "findversioncheck";
   final static private String TEXT_PANEL = "fulltext";
   final static private String TEXT_CHECK = "fulltextcheck";
   final static private String PROPERTIES_PANEL = "properties";
   final static private String PROPERTIES_CHECK = "propertiescheck";
   final static private String ADD_PROPERTIES_PANEL = "addproperties";
   final static private String CONTAINS_ALL = "containedwords";
   final static private String CONTAINS_ANY = "anywords";
   final static private String EXACT_PHRASE = "exactphrase";
   final static private String DOES_NOT_CONTAIN = "withoutwords";
   final static private String LOCATION = "location";
   final static private String PANEL_VISIBILITY = "panel_visibility";
   final static private String EXPANDED = "expanded";
   final static private String START_DATE = "between1";
   final static private String END_DATE = "between2";
   final static private String DATE = "date";
   final static private int MAX_PANELS = 5;
   final static private String TYPELIST = "typelist";
   final static private String CATTYPE = "dm_category";

   final static private String PARAM_1      = "param1";
   final static private String BOOLEANS_1   = "booleans1";
   final static private String SEARCH_FOR_1 = "searchfor1";
   final static private String PARAM_2      = "param2";
   final static private String BOOLEANS_2   = "booleans2";
   final static private String SEARCH_FOR_2 = "searchfor2";
   final static private String PARAM_3      = "param3";
   final static private String BOOLEANS_3   = "booleans3";
   final static private String SEARCH_FOR_3 = "searchfor3";
   final static private String PARAM_4      = "param4";
   final static private String BOOLEANS_4   = "booleans4";
   final static private String SEARCH_FOR_4 = "searchfor4";
   final static private String PARAM_5      = "param5";
   final static private String BOOLEANS_5   = "booleans5";
   final static private String SEARCH_FOR_5 = "searchfor5";

   final static private String CITI_PANEL = "citi_set";
   final static private String CITI_PROPERTIES_PANEL = "citi_properties";
   final static private String CITI_ADD_PROPERTIES_PANEL = "citi_addproperties";
   final static private String CITI_PARAM_LIST = "citi_param";
   final static private String CITI_BOOLEAN_LIST = "citi_booleans";
   final static private String CITI_SEARCH_STRING = "citi_searchfor";
   final static private String CITI_DATE = "citi_date";
   final static private String CITI_ADD_BUTTON = "citi_addproperty";
   final static private String CITI_REMOVE_BUTTON = "citi_removeaction";
   final static private String CITI_PARAM_1      = "citi_param1";
   final static private String CITI_BOOLEANS_1   = "citi_booleans1";
   final static private String CITI_SEARCH_FOR_1 = "citi_searchfor1";
   final static private String CITI_PARAM_2      = "citi_param2";
   final static private String CITI_BOOLEANS_2   = "citi_booleans2";
   final static private String CITI_SEARCH_FOR_2 = "citi_searchfor2";
   final static private String CITI_PARAM_3      = "citi_param3";
   final static private String CITI_BOOLEANS_3   = "citi_booleans3";
   final static private String CITI_SEARCH_FOR_3 = "citi_searchfor3";
   final static private String CITI_PARAM_4      = "citi_param4";
   final static private String CITI_BOOLEANS_4   = "citi_booleans4";
   final static private String CITI_SEARCH_FOR_4 = "citi_searchfor4";
   final static private String CITI_PARAM_5      = "citi_param5";
   final static private String CITI_BOOLEANS_5   = "citi_booleans5";
   final static private String CITI_SEARCH_FOR_5 = "citi_searchfor5";
   private ConfigResultSet[] m_currentCitiParamSet = new ConfigResultSet[6];
   private ConfigResultSet[] m_currentCitiBooleanSet = new ConfigResultSet[6];


   /** interal column attributes - always queried */
   final static private String[] INTERNAL_ATTRIBUTES = {
      "r_object_id", "object_name", "r_object_type", "r_lock_owner", "owner_name",
      "r_link_cnt", "r_is_virtual_doc", "r_content_size", "a_content_type", "i_is_reference"
   };

   /** interal column attributes - always queried */
   final static private String[] INTERNAL_EPA_CASE_ATTRIBUTES = {
         "r_object_id"
   };

   // date control year range
   private String m_strDateCtrlFromYear = null;
   private String m_strDateCtrlToYear = null;
   String maxReturnCount = "500";



} // end class AdvSearch


