/*
 * Copyright (C) 2012, The MINT Consortium (See COPYRIGHTS file for a list of copyright holders).
 * All rights reserved.
 *
 * This source code contains the intellectual property of its copyright holders, and is made
 * available under a license. If you do not know the terms of the license, please review it before
 * you read further.
 *
 * You can read LICENSES for detailed information about the license terms this source code file is
 * available under.
 *
 * Questions should be directed to legal@peakhealthcare.com
 *
 */

package org.oht.miami.msdtk.studymodel;

import org.oht.miami.msdtk.conversion.GpbUtil;
import org.oht.miami.msdtk.conversion.StudyProtos.DicomElementGpb;
import org.oht.miami.msdtk.conversion.StudyProtos.InstanceGpb;
import org.oht.miami.msdtk.conversion.StudyProtos.SeriesGpb;
import org.oht.miami.msdtk.deidentification.AttributeProfileActions;
import org.oht.miami.msdtk.deidentification.DeIdentifyUtil;
import org.oht.miami.msdtk.deidentification.DeIdentifyUtil.ActionCode;
import org.oht.miami.msdtk.deidentification.StudyProfileOptions;
import org.oht.miami.msdtk.util.DicomElementUtil;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.dcm4che2.util.UIDUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Mahmoud Ismail. June-2011, email: maismail@cs.jhu.edu This class is
 *         to model a Series portion of a DICOM study.
 */
public class Series implements java.io.Serializable {

    /**
     * Required by the Serializable interface
     */
    private static final long serialVersionUID = 1L;
    /* List of series level attributes. */
    private final ArrayList<DicomElement> seriesDicomElements;
    /* List of instances within the series */
    private final ArrayList<Instance> instancesList;
    private Study studyParent;

    /**
     * Constructor used when building the data model from a DICOM object
     * 
     * @param study
     *            the parent of the series
     */
    public Series(Study study) {
        if (study != null)
            studyParent = study;
        seriesDicomElements = new ArrayList<DicomElement>();
        instancesList = new ArrayList<Instance>();
    }

    /**
     * Constructor used when building the data model from Google Protocol
     * Buffers
     * 
     * @param study
     *            the parent of the series
     * @param gpbData
     *            the GPB object
     */
    public Series(Study study, SeriesGpb gpbData) {
        instancesList = new ArrayList<Instance>();
        seriesDicomElements = new ArrayList<DicomElement>();

        for (DicomElementGpb attrData : gpbData.getSeriesDicomElementsList()) {
            this.putAttribute(GpbUtil.gpbToDicomElement(attrData));
        }

        for (InstanceGpb instanceData : gpbData.getInstancesList()) {
            this.addInstance(new Instance(this, instanceData));
        }
    }

    /**
     * Add a Dicom object to the series without normalizing the attributes.
     * 
     * @param dcmObj
     *            the object to be added
     */
    public void addDicomObjectWithoutNormalization(final DicomObject dcmObj) {
        final Instance instance = new Instance(this);
        this.addInstance(instance);

        // Now, iterate through all items in the object and store each
        // appropriately.
        // This dispatches the Attribute storage to one of the study level,
        // series level
        // or instance-level Attributes sets.
        for (Iterator<DicomElement> dcmElementIter = dcmObj.iterator(0x00020000, 0xffffffff); dcmElementIter
                .hasNext();) {

            DicomElement dcmElement = dcmElementIter.next();
            final int tag = dcmElement.tag();

            /* Check if it is a study element */
            if (StudyTags.isStudyTag(tag)) {
                if (studyParent.getAttribute(tag) == null) {
                    studyParent.putAttribute(dcmElement);
                }
            }

            /* Check if it is a study element */
            else {
                if (SeriesTags.isSeriesTag(tag)) {
                    if (this.getAttribute(tag) == null) {
                        this.putAttribute(dcmElement);
                    }
                }
                /* For sure it is an instance dicom element. */
                else
                    instance.putAttribute(dcmElement);
            }
        }
    }

    /**
     * Gets a specific attribute from the series level attributes
     * 
     * @param tag
     *            the tag of the attribute
     * @return the attribute for the given tag.
     */
    public DicomElement getAttribute(final int tag) {
        for (int i = 0; i < seriesDicomElements.size(); i++) {
            DicomElement seriesDcm = seriesDicomElements.get(i);
            if (seriesDcm.tag() == tag)
                return seriesDcm;
        }
        return null;
    }

    /**
     * Searches for a specific attribute from the series.
     * 
     * @param tag
     *            the tag of the attribute
     * @return the attribute for the given tag.
     */
    public DicomElement findAttribute(final int tag) {
        DicomElement result = this.getAttribute(tag);
        if (result == null) {
            return studyParent.findAttribute(tag);
        }
        return result;
    }

    /**
     * puts an Attribute into the Series - attributes are unique per tag
     * 
     * @param attr
     *            the attribute to add to the series
     */
    public void putAttribute(final DicomElement attr) {
        if (attr == null) {
            return;
        }
        if (StudyTags.isStudyTag(attr.tag()) == true) {
            throw new IllegalArgumentException("The tag is not a Series Tag");
        }
        if (getAttribute(attr.tag()) == null) {
            /* Checks to make sure the tag is not a Study Tag */
            seriesDicomElements.add(attr);
        } else
            throw new IllegalArgumentException(String.format(
                    "Dicom element with tag 0x%x already exists.", attr.tag()));

    }

    /**
     * puts an Attribute into the Series according to its tag order. Attributes
     * are unique per tag
     * 
     * @param attr
     *            the attribute to add to the series
     */
    public void putAttributeInOrder(final DicomElement attr) {
        if (attr == null) {
            return;
        }

        int imin = 0;
        int imax = seriesDicomElements.size() - 1;
        while (imax >= imin) {
            int imid = (imax + imin) / 2;
            int target = seriesDicomElements.get(imid).tag();
            if (target < attr.tag()) {
                imin = imid + 1;
            } else if (target > attr.tag()) {
                imax = imid - 1;
            } else {
                throw new IllegalArgumentException(String.format(
                        "Dicom element with tag 0x%x already exists.", attr.tag()));
            }
        }
        seriesDicomElements.add(imin, attr);
    }

    /**
     * Adds an attribute to the end of the list of series-level attributes
     * without any extra checks. This method should be only used if the input is
     * a valid MS-DICOM object.
     * 
     * @param attribute
     *            The series-level attribute to be added.
     */
    void addAttribute(DicomElement attribute) {
        seriesDicomElements.add(attribute);
    }

    /**
     * Gets the <i> value </i> of a specific attribute in the series
     * 
     * @param tag
     *            the tag of the attribute
     * @return the value
     */
    public byte[] getValueForAttribute(final int tag) {
        DicomElement attr = this.getAttribute(tag);
        return attr != null ? attr.getBytes() : null;
    }

    /**
     * Gets the <i> value </i> of a specific attribute in the instance as a
     * String
     * 
     * @param tag
     *            the tag of the attribute
     * @return the value as a String
     */
    public String getValueForAttributeAsString(final int tag) {
        DicomElement attr = this.getAttribute(tag);
        return attr != null ? attr.getString(null, false) : null;
    }

    /**
     * removes the Attribute with the given tag from the Series
     * 
     * @param tag
     *            the tag of the attribute to remove
     */
    public DicomElement removeAttribute(final int tag) {
        DicomElement attr = this.getAttribute(tag);
        seriesDicomElements.remove(attr);
        return attr;
    }

    /**
     * removes the Attribute
     * 
     * @param attr
     *            the tag of the attribute to remove
     */
    public void removeAttribute(final DicomElement attr) {
        seriesDicomElements.remove(attr);
    }

    /**
     * Gets the parent of the series
     * 
     * @return the study parent
     */
    public Study getStudyParent() {
        return studyParent;
    }

    /**
     * Gets an iterator over all the series level attributes
     * 
     * @return an iterator of all Attributes in the Series
     */
    public Iterator<DicomElement> attributeIterator() {
        return seriesDicomElements.iterator();
    }

    /**
     * puts an Instance into the Series - instances are unique per xfer
     * 
     * @param inst
     *            the instance to add
     */
    public void addInstance(final Instance inst) {
        instancesList.add(inst);
    }

    /**
     * removes an Instance from the Series based on its SOP Instance UID
     * 
     * @param sopInstanceUID
     *            the SOP UID of the instance to remove
     */
    public void removeInstance(final String sopInstanceUID) {
        instancesList.remove(this.getInstance(sopInstanceUID));
    }

    /**
     * removes an Instance from the Series
     * 
     * @param inst
     *            the instance to remove
     */
    public void removeInstance(final Instance inst) {
        instancesList.remove(inst);
    }

    /**
     * gets an Instance from the Series based on its SOP Instance UID
     * 
     * @param sopInstanceUID
     *            the SOP UID of the instance
     */
    public Instance getInstance(final String sopInstanceUID) {
        if (sopInstanceUID == null) {
            return null;
        }
        for (Iterator<Instance> it = instancesList.iterator(); it.hasNext();) {
            Instance temp = it.next();
            if (sopInstanceUID.equals(temp.getSOPInstanceUID())) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Gets an iterator over all instances in the series
     * 
     * @return an iterator of all Instances in the Series
     */
    public Iterator<Instance> instanceIterator() {
        return instancesList.iterator();
    }

    /**
     * Get the 'seriesInstanceUID' attribute value.
     * 
     * @return value
     */
    public String getSeriesInstanceUID() {
        DicomElement attr = this.getAttribute(Tag.SeriesInstanceUID);
        if (attr != null)
            return attr.getString(null, true);
        return null;
    }

    /**
     * Gets the number of instances in the series that has SOPInstanceUID
     * 
     * @return the number of instances with SOPInstanceUID in this series
     */
    public int getInstanceCount() {
        int numberOfInstance = 0;
        for (Instance instance : instancesList)
            numberOfInstance += instance.getInstanceCount();
        return numberOfInstance;
    }

    /**
     * Gets the number of instances in the series list of instances.
     * 
     * @return the number of instances in the series list of instances.
     */
    public int getInstanceListSize() {
        return instancesList.size();
    }

    /**
     * Gets the number of frames in the series
     * 
     * @return the number of frames in this series Note: number of frames !=
     *         number of instances. Some instances may be composite and contain
     *         more than one frame.
     */
    public int getNumberOfFrames() {

        int numberOfFrames = 0;
        for (Iterator<Instance> it = instancesList.iterator(); it.hasNext();) {
            Instance temp = it.next();
            numberOfFrames += temp.getNumberOfFrames();
        }
        return numberOfFrames;
    }

    /**
     * Writes the series into GPB
     * 
     * @return the GPB object
     */
    public SeriesGpb writeToGPB() {
        SeriesGpb.Builder builder = SeriesGpb.newBuilder();

        for (Iterator<DicomElement> attr = attributeIterator(); attr.hasNext();) {
            builder.addSeriesDicomElements(GpbUtil.dicomElementToGPB(attr.next()));
        }

        for (Iterator<Instance> attr = instanceIterator(); attr.hasNext();) {
            builder.addInstances(attr.next().writeToGPB());
        }

        return builder.build();
    }

    /**
     * Add attribute to a series with normalization.
     * 
     * @param oldElement
     *            The attribute to be added to the previously stored list of
     *            instances.
     * @param newElement
     *            The attribute to be added to the newly added series.
     * @param currentInstance
     *            The newly added instance to the study.
     */
    public void addAttributeWithNormalization(final DicomElement oldElement,
            final DicomElement newElement, final Instance currentInstance) {
        if (this.getInstanceListSize() == 1) {
            this.putAttributeInOrder(newElement);
        } else {
            for (Instance instance : instancesList) {
                if (instance == currentInstance) {
                    instance.putAttribute(newElement);
                } else {
                    instance.putAttributeInOrder(oldElement);
                }
            }
        }
    }

    /*
     * Remove an attribute from the study level and insert it into all series
     * level attributes. Insert the different dicom element to the list of
     * attributes of the current instance
     */
    /**
     * Remove an attribute from the series list of attributes and insert it into
     * instance level attributes for all instances within the series Insert the
     * new dicom element to the list of attributes of the newly added instance.
     * 
     * @param oldElement
     *            The attribute to be added to the previously stored list of
     *            instances.
     * @param newElement
     *            The attribute to be added to the newly added instance.
     * @param differentInstance
     *            The newly added instance to the series.
     */
    void deNormalizeSeriesAttribute(final DicomElement oldElement, final DicomElement newElement,
            final Instance differentInstance) {
        for (Instance inst : this.instancesList) {
            if (inst == differentInstance) {
                inst.putAttribute(newElement);
            } else {
                inst.putAttributeInOrder(oldElement);
            }
        }
        this.seriesDicomElements.remove(oldElement);
    }

    /**
     * Gets the total number of dicom elements of this series
     * 
     * @return the total count
     */
    public int elementCount() {
        int count = 0;
        for (DicomElement elem : seriesDicomElements) {
            count += DicomElementUtil.getCount(elem);
        }
        for (Instance instance : instancesList) {
            count += instance.elementCount();
        }
        return count;
    }

    /**
     * Get the series attribute stored in the given index
     * 
     * @param index
     *            Integer determines the location of the required attribute in
     *            the series attribute list.
     * @return DicomElement The series attribute at the specified index, null if
     *         not found.
     */
    public DicomElement getAttributeAtIndexOf(final int index) {
        if (index < 0 || index >= seriesDicomElements.size())
            return null;
        return seriesDicomElements.get(index);
    }

    /**
     * Get number of series level attributes within the series
     * 
     * @return int, number of attributes
     */
    public int getNumberOfSeriesAttributes() {
        return seriesDicomElements.size();
    }

    /**
     * De-Identify an attribute in the series.
     * 
     * @param elem
     *            , the dicom element to be de-Identified
     * @param actions
     *            , Actions associated with the input dicom element as defined
     *            in the de-identification standard
     * @param options
     *            , an object contains the list of profile options set by the
     *            user for de-identification.
     * @return boolean, determines if the order of the list of attributes within
     *         the study has been changed in the function.
     */
    private boolean deIdentifyAttribute(final DicomElement elem, AttributeProfileActions actions,
            StudyProfileOptions options) {
        ActionCode action = DeIdentifyUtil.GetAction(actions, options);
        switch (action) {
        // Delete Action
        case X:
        case XD:
        case XZ:
        case XZD:
        case XZU:
            this.removeAttribute(elem);
            return true;

            // Change UID Action
        case U:
            DicomElement updatedElem = DeIdentifyUtil.changeUID(elem, UIDUtils.createUID());
            this.removeAttribute(elem);
            this.seriesDicomElements.add(updatedElem);
            return true;

        case Z:
        case ZD:
            DicomElement zeroElem = DeIdentifyUtil.apply_Z_Action(elem);
            this.removeAttribute(elem);
            this.seriesDicomElements.add(zeroElem);
            return true;

        case K:
            if (elem.vr() == VR.SQ) {
                DeIdentifyUtil.apply_K_Action(elem, actions, options, null);
                if (elem.countItems() == 0)
                    this.removeAttribute(elem);
            }
            break;
        // TODO to be modified
        case D:
        case C:
        default:
            break;
        }
        return false;
    }

    /**
     * DeIdentify the series. Remove, modify or keep the attributes as
     * determined by the de-Identification profile. If an attribute is not
     * listed in the de-identification profile, it is kept without any change.
     * 
     * @param pOptions
     *            : Determine which profile options are chosen.
     */
    public void deIdentifySeries(final StudyProfileOptions pOptions) {
        int size = seriesDicomElements.size();
        for (int i = 0; i < size; i++) {
            DicomElement elem = seriesDicomElements.get(i);

            /* remove private tags */
            if (TagUtils.isPrivateDataElement(elem.tag())) {
                this.removeAttribute(elem);
                i--;
                size--;
                continue;
            }
            AttributeProfileActions actions = DeIdentifyUtil.getAttributeActions(elem.tag());
            // Check if this attribute is in the list of attributes to be
            // de-identified.
            if (actions != null)
                if (deIdentifyAttribute(elem, actions, pOptions)) {
                    i--;
                    size--;
                }
        }
        for (Instance inst : instancesList) {
            inst.deIdentifyInstance(pOptions);
        }
    }

    public boolean equals(Series series) {
        if (series == null) {
            return false;
        }

        // Compares series level attributes.
        Iterator<DicomElement> seriesAttrIter = series.attributeIterator();
        for (DicomElement attr : seriesDicomElements) {
            if (attr.tag() == Tag.SOPInstanceUID || attr.tag() == Tag.MediaStorageSOPInstanceUID) {
                continue;
            }
            DicomElement tmp = seriesAttrIter.next();
            while (tmp.tag() == Tag.SOPInstanceUID || tmp.tag() == Tag.MediaStorageSOPInstanceUID) {
                tmp = seriesAttrIter.next();
            }
            if (!DicomElementUtil.compareDicomElements(attr, tmp, true)) {
                return false;
            }
        }

        // Compares instances within the series
        if (this.getInstanceListSize() != series.getInstanceListSize()) {
            return false;
        }
        Iterator<Instance> instanceIter = series.instanceIterator();
        for (Instance inst : instancesList) {
            Instance instanceSeries = series.getInstance(inst.getSOPInstanceUID());
            /*
             * Compares the instance with the corresponding instance in the
             * other series with the same SOPInstanceUID. If there is no
             * instance with the same SOPInstanceUID, try to compare the
             * instance with the instance that has the same order within the
             * series. Conversion from multi-frame to single-frame and vice
             * versa could cause a change in the SOPInstanceUID and it might not
             * be possible to use SOPInstanceUID to find the corresponding
             * instance.
             */
            if (!inst.equals(instanceSeries) && !inst.equals(instanceIter.next())) {
                return false;
            }
        }
        return true;
    }

}

