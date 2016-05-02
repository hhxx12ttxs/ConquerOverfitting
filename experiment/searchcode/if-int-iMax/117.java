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
import org.oht.miami.msdtk.conversion.StudyProtos.FramesGpb;
import org.oht.miami.msdtk.conversion.StudyProtos.InstanceGpb;
import org.oht.miami.msdtk.deidentification.AttributeProfileActions;
import org.oht.miami.msdtk.deidentification.DeIdentifyUtil;
import org.oht.miami.msdtk.deidentification.DeIdentifyUtil.ActionCode;
import org.oht.miami.msdtk.deidentification.StudyProfileOptions;
import org.oht.miami.msdtk.util.DicomElementUtil;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SimpleDicomElement;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.dcm4che2.util.UIDUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class Instance implements java.io.Serializable {

    /**
     * Required by the Serializable interface.
     */
    private static final long serialVersionUID = 1L;
    private final ArrayList<DicomElement> instanceDicomElements;
    private final Series seriesParent;
    private ArrayList<Instance> childFrames = null;
    private final String deIdentifiedSopInstanceUID = UIDUtils.createUID();

    /**
     * Constructor used usually when the model is build using Dicom object
     * 
     * @param parent
     *            the series parent of the instance
     */
    public Instance(final Series parent) {
        instanceDicomElements = new ArrayList<DicomElement>();
        seriesParent = parent;
    }

    /**
     * Constructor used when the model is build using google protocol buffer
     * object
     * 
     * @param parent
     *            the series parent of the instance
     * @param gpbData
     *            the GPB object to convert into the isntance
     */
    public Instance(final Series parent, final InstanceGpb gpbData) {
        seriesParent = parent;
        instanceDicomElements = new ArrayList<DicomElement>();
        /* Add attributes */
        for (DicomElementGpb attrData : gpbData.getInstanceDicomElementsList()) {
            this.putAttribute(GpbUtil.gpbToDicomElement(attrData));
        }
        /* Add child frames if the instance represent a multiframe instance */
        if (gpbData.hasChildFrames()) {
            this.addChildInstances(gpbData.getChildFrames());
        }
    }

    /**
     * Gets a specific attribute from the instance
     * 
     * @param tag
     *            the tag of the attribute
     * @return the attribute for the given tag (in hex)
     */
    public DicomElement getAttribute(final int tag) {
        for (int i = 0; i < instanceDicomElements.size(); i++) {
            DicomElement studyDcm = instanceDicomElements.get(i);
            if (studyDcm.tag() == tag) {
                return studyDcm;
            }
        }
        return null;
    }

    /**
     * Searches for a specific attribute in the instance without searching in
     * child frames if any.
     * 
     * @param tag
     *            the tag of the attribute
     * @return the attribute for the given tag.
     */
    public DicomElement findAttribute(final int tag) {
        DicomElement result = this.getAttribute(tag);
        if (result == null) {
            return seriesParent.findAttribute(tag);
        }
        return result;
    }

    /**
     * Puts an Attribute into the Instance - attributes are unique per tag
     * 
     * @param attr
     *            the attribute to add
     */
    public void putAttribute(final DicomElement attr) {
        if (attr == null) {
            return;
        }
        if (StudyTags.isStudyTag(attr.tag()) == true || SeriesTags.isSeriesTag(attr.tag()) == true) {
            throw new IllegalArgumentException("The tag is not an Instance Tag");
        }
        if (getAttribute(attr.tag()) != null) {
            throw new IllegalArgumentException(String.format(
                    "Dicom element with tag 0x%x already exists. " + attr.getString(null, false)
                            + "original value " + getAttribute(attr.tag()), attr.tag()));
        }
        if (attr.tag() == Tag.PerFrameFunctionalGroupsSequence) {
            addChildInstances(attr);
        } else {
            instanceDicomElements.add(attr);
        }
    }

    /**
     * Puts an Attribute into the instance according to its tag order.
     * Attributes are unique per tag
     * 
     * @param attr
     *            the attribute to add to the series
     */
    public void putAttributeInOrder(final DicomElement attr) {
        if (attr == null) {
            return;
        }
        if (StudyTags.isStudyTag(attr.tag()) == true || SeriesTags.isSeriesTag(attr.tag()) == true) {
            throw new IllegalArgumentException("The tag is not an Instance Tag");
        }

        int imin = 0;
        int imax = instanceDicomElements.size() - 1;
        while (imax >= imin) {
            int imid = (imax + imin) / 2;
            int target = instanceDicomElements.get(imid).tag();
            if (target < attr.tag()) {
                imin = imid + 1;
            } else if (target > attr.tag()) {
                imax = imid - 1;
            } else {
                throw new IllegalArgumentException(String.format(
                        "Dicom element with tag 0x%x already exists.", attr.tag()));
            }
        }
        instanceDicomElements.add(imin, attr);
    }

    /**
     * Adds an attribute to the end of the list of instance-level attributes
     * without any extra checks. This method should be only used if the input is
     * a valid MS-DICOM object.
     * 
     * @param attribute
     *            The instance-level attribute to be added.
     */
    void addAttribute(DicomElement attribute) {
        instanceDicomElements.add(attribute);
    }

    /**
     * Get the instance attribute stored in the given index
     * 
     * @param index
     *            Integer determines the location of the required attribute in
     *            the instance attribute list.
     * @return DicomElement The instance attribute at the specified index, null
     *         if not found.
     */
    public DicomElement getAttributeAtIndexOf(final int index) {
        return instanceDicomElements.get(index);
    }

    /**
     * Gets the <i> value </i> of a specific attribute in the instance
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
     * Removes the Attribute with the given tag from the Series
     * 
     * @param tag
     *            the tag of the attribute
     */
    public DicomElement removeAttribute(final int tag) {
        DicomElement attr = this.getAttribute(tag);
        instanceDicomElements.remove(attr);
        return attr;
    }

    /**
     * Removes the Attribute
     * 
     * @param attr
     *            the tag of the attribute to remove
     */
    public void removeAttribute(final DicomElement attr) {
        instanceDicomElements.remove(attr);
    }

    /**
     * Gets the parent of the instance
     * 
     * @return the parent
     */
    public Series getSeriesParent() {
        return seriesParent;
    }

    /**
     * Gets an iterator over all the instance level attributes
     * 
     * @return an iterator of all Attributes in the instance
     */
    public Iterator<DicomElement> attributeIterator() {
        return instanceDicomElements.iterator();
    }

    /**
     * Get the 'sopInstanceUID' attribute value.
     * 
     * @return value
     */
    public String getSOPInstanceUID() {
        DicomElement attr = this.getAttribute(Tag.SOPInstanceUID);
        return (attr != null) ? attr.getString(null, false) : null;
    }

    /**
     * Gets the 'transferSyntaxUID' attribute value.
     * 
     * @return value
     */
    public String getTransferSyntaxUID() {
        DicomElement attr = this.findAttribute(Tag.TransferSyntaxUID);
        return attr != null ? attr.getString(null, false) : null;
    }

    /**
     * Return number of frames per instance
     * 
     * @return the number of frames
     */
    public int getNumberOfFrames() {
        /*
         * Get number of frames if does not exist, number of frames = 1
         */
        int temp = 0;
        if (childFrames != null) {
            for (Instance child : childFrames) {
                temp += child.getNumberOfFrames();
            }
        }
        // TODO(maismail) checks if it is necessary to do this condition
        return (temp == 0) ? 1 : temp;
    }

    /**
     * Write instance to GPB
     * 
     * @return the GPB object
     */
    public InstanceGpb writeToGPB() {
        InstanceGpb.Builder builder = InstanceGpb.newBuilder();
        for (Iterator<DicomElement> attr = attributeIterator(); attr.hasNext();) {
            builder.addInstanceDicomElements(GpbUtil.dicomElementToGPB(attr.next()));
        }
        if (childFrames != null) {
            FramesGpb.Builder frameBuilder = FramesGpb.newBuilder();
            for (Instance frame : childFrames) {
                frameBuilder.addFrames(frame.writeToGPB());
            }
            builder.setChildFrames(frameBuilder.build());
        }
        return builder.build();
    }

    /**
     * Add the child frames to the instance. The input is a sequence dicom
     * element with tag = PerFrameFunctionalGroupsSequence tag
     * 
     * @param elem
     *            Sequence dicom element that holds a list of frames.
     */
    private void addChildInstances(final DicomElement elem) {
        // Allocate memory for the child frames
        this.childFrames = new ArrayList<Instance>();
        // Ensure that the input dicom element is a sequence dicom element.
        if (elem.vr() != VR.SQ) {
            return;
        }
        if (seriesParent.getStudyParent().isNormalized()) {
            // Adds all attributes to the parent instance excep for the
            // sopinstanceUID if exists
            Instance frame = new Instance(this.seriesParent);
            childFrames.add(frame);
            DicomObject currentFrame = elem.getDicomObject(0);
            /* Add Attributes */
            for (Iterator<DicomElement> iter = currentFrame.iterator(0x00020000, 0xffffffff); iter
                    .hasNext();) {
                DicomElement frameDcmElement = iter.next();
                if (frameDcmElement.vr() == VR.BD || frameDcmElement.tag() == Tag.SOPInstanceUID
                        || frameDcmElement.tag() == Tag.MediaStorageSOPInstanceUID) {
                    frame.putAttribute(frameDcmElement);
                } else {
                    this.putAttributeInOrder(frameDcmElement);
                }
            }
            // Normalize for the remaining children
            // dump the data of the child dicom objects to the model
            for (int i = 1; i < elem.countItems(); i++) {
                frame = new Instance(this.seriesParent);
                childFrames.add(frame);
                currentFrame = elem.getDicomObject(i);
                /* Add Attributes */
                for (Iterator<DicomElement> iter = currentFrame.iterator(0x00020000, 0xffffffff); iter
                        .hasNext();) {
                    DicomElement frameDcmElement = iter.next();
                    if (frameDcmElement.vr() == VR.BD
                            || frameDcmElement.tag() == Tag.SOPInstanceUID) {
                        frame.putAttribute(frameDcmElement);
                        continue;
                    }
                    DicomElement instDcmElement = this.getAttribute(frameDcmElement.tag());
                    if (instDcmElement == null) {
                        frame.putAttribute(frameDcmElement);
                    } else {
                        if (instDcmElement.vr() != frameDcmElement.vr()) {
                            throw new RuntimeException("VR's do not match");
                        }
                        if (!DicomElementUtil.compareDicomElements(frameDcmElement, instDcmElement,
                                false)) {
                            deNormalizeInstanceAttribute(instDcmElement, frameDcmElement, frame);
                        }
                    }
                }
            }
        } else {
            /* dump the data of the child dicom objects to the model */
            for (int i = 0; i < elem.countItems(); i++) {
                Instance frame = new Instance(this.seriesParent);
                childFrames.add(frame);
                DicomObject currentFrame = elem.getDicomObject(i);
                /* Add Attributes */
                for (Iterator<DicomElement> iter = currentFrame.iterator(0x00020000, 0xffffffff); iter
                        .hasNext();) {
                    frame.putAttribute(iter.next());
                }
            }
        }
    }

    /**
     * Adds the child frames to the instance. The input is in GPB format.
     * 
     * @param frames
     *            FramesGPB object
     */
    private void addChildInstances(final FramesGpb frames) {
        // Allocate memory for the child frames
        this.childFrames = new ArrayList<Instance>();
        /* dump the data of the GPB child frame to the model */
        for (InstanceGpb frame : frames.getFramesList()) {
            Instance inst = new Instance(this.seriesParent);
            /* Add attributes */
            for (DicomElementGpb attrData : frame.getInstanceDicomElementsList()) {
                inst.putAttribute(GpbUtil.gpbToDicomElement(attrData));
            }
            childFrames.add(inst);
        }
    }

    /**
     * Removes an attribute from the instance level and insert it into all child
     * frame level attributes
     * 
     * @param oldElement
     *            The attribute to be added to the previously stored list of
     *            instances.
     * @param newElement
     *            The attribute to be added to the newly added series.
     * @param currentInstance
     *            The newly added instance to the study.
     */
    void deNormalizeInstanceAttribute(final DicomElement oldElement, final DicomElement newElement,
            final Instance differentInstance) {
        // Add the attribute to all serieses and then remove
        // from the study
        for (Iterator<Instance> it = childFrames.iterator(); it.hasNext();) {
            Instance frame = it.next();
            if (frame == differentInstance) {
                frame.putAttribute(newElement);
            } else {
                frame.putAttributeInOrder(oldElement);
            }
        }
        this.instanceDicomElements.remove(oldElement);
    }

    /**
     * @return The list of child frames within the instances, if exists
     */
    public ArrayList<Instance> getChildrenFrames() {
        return childFrames;
    }

    /**
     * @return whether the instance has child frames or not.
     */
    public boolean hasChildFrames() {
        return (childFrames == null) ? false : true;
    }

    /**
     * Creates a Frame and adds it to this Instance.
     * 
     * @return The newly added Frame.
     */
    public Instance addFrame() {
        if (childFrames == null) {
            childFrames = new ArrayList<Instance>();
        }
        Instance frame = new Instance(seriesParent);
        childFrames.add(frame);
        return frame;
    }

    /**
     * Gets the total number of dicom elements of this instance (and child
     * frames if exist)
     * 
     * @return the total count
     */
    public int elementCount() {
        int count = 0;
        for (DicomElement elem : instanceDicomElements) {
            count += DicomElementUtil.getCount(elem);
        }
        if (childFrames != null) {
            for (Instance child : childFrames) {
                count += child.elementCount();
            }
        }
        return count;
    }

    /**
     * Gets the number of instances. An instance has to have a SOPInstanceUID to
     * be counted.
     * 
     * @return the number of instances.
     */
    public int getInstanceCount() {
        // Returns one, if the instance has SOPInstanceUID
        /*
         * int numberOfInstance = 0; if (childFrames == null) { if
         * (getAttribute(Tag.SOPInstanceUID) != null) { numberOfInstance = 1; }
         * } else { for (Instance frame : childFrames) { numberOfInstance +=
         * frame.getInstanceCount(); } } return numberOfInstance;
         */
        return 1;
    }

    /** Ensures that the sopInstanceUID matches the media storage instance UID */
    public void updateMediaStorage() {
        DicomElement oldMediaStorage = this.getAttribute(Tag.MediaStorageSOPInstanceUID);
        if (oldMediaStorage != null) {
            DicomElement newMediaStorage = new SimpleDicomElement(oldMediaStorage.tag(),
                    oldMediaStorage.vr(), oldMediaStorage.bigEndian(), this.getSOPInstanceUID()
                            .getBytes(), null);
            this.removeAttribute(oldMediaStorage);
            this.putAttributeInOrder(newMediaStorage);
        }
    }

    /**
     * De-Identify an attribute in the instance.
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
            if (elem.tag() == Tag.SOPInstanceUID || elem.tag() == Tag.MediaStorageSOPInstanceUID) {
                DicomElement updatedElem = DeIdentifyUtil.changeUID(elem,
                        deIdentifiedSopInstanceUID);
                this.removeAttribute(elem);
                this.instanceDicomElements.add(updatedElem);
                // Both parent instance and child instances have sopInstanceUID.
                // This is why we have to do scan the child frames
            } else {
                DicomElement updatedElem = DeIdentifyUtil.changeUID(elem, UIDUtils.createUID());
                this.removeAttribute(elem);
                this.instanceDicomElements.add(updatedElem);
            }
            return true;
        case Z:
        case ZD:
            DicomElement zeroElem = DeIdentifyUtil.apply_Z_Action(elem);
            this.removeAttribute(elem);
            this.instanceDicomElements.add(zeroElem);
            return true;

        case K:
            if (elem.vr() == VR.SQ) {
                DeIdentifyUtil.apply_K_Action(elem, actions, options, null);
                if (elem.countItems() == 0) {
                    this.removeAttribute(elem);
                }
            }
            break;
        case D:
        case C:
        default:
            break;
        }
        return false;
    }

    /**
     * DeIdentify the instance. Remove, modify or keep the attributes as
     * determined by the de-Identification profile. If an attribute is not
     * listed in the de-identification profile, it is kept without any change.
     * 
     * @param pOptions
     *            : Determine which profile options are chosen.
     */
    public void deIdentifyInstance(final StudyProfileOptions pOptions) {
        int size = instanceDicomElements.size();
        for (int i = 0; i < size; i++) {
            DicomElement elem = instanceDicomElements.get(i);
            // remove private tags
            if (TagUtils.isPrivateDataElement(elem.tag())) {
                this.removeAttribute(elem);
                i--;
                size--;
                continue;
            }
            AttributeProfileActions actions = DeIdentifyUtil.getAttributeActions(elem.tag());
            // Check if this attribute is in the list of attributes to be
            // de-identified.
            if (actions != null) {
                if (deIdentifyAttribute(elem, actions, pOptions)) {
                    i--;
                    size--;
                }
            }
        }
        if (this.childFrames != null) {
            for (Instance inst : childFrames) {
                inst.deIdentifyInstance(pOptions);
            }
        }
    }

    public boolean equals(Instance inst) {
        if (inst == null) {
            return false;
        }

        // Compares instance level attributes.
        Iterator<DicomElement> instAttrIter = inst.attributeIterator();
        for (DicomElement attr : instanceDicomElements) {
            if (attr.tag() == Tag.SOPInstanceUID || attr.tag() == Tag.MediaStorageSOPInstanceUID) {
                continue;
            }
            DicomElement tmp = instAttrIter.next();
            while (tmp.tag() == Tag.SOPInstanceUID || tmp.tag() == Tag.MediaStorageSOPInstanceUID) {
                tmp = instAttrIter.next();
            }
            if (!DicomElementUtil.compareDicomElements(attr, tmp, true)) {
                return false;
            }
        }

        // Compares child frames if exist
        if (this.childFrames != null) {
            if (inst.getChildrenFrames().size() != childFrames.size()) {
                return false;
            }
            Iterator<Instance> instChildFramesIter = inst.getChildrenFrames().iterator();
            for (Instance child : childFrames) {
                if (!child.equals(instChildFramesIter.next())) {
                    return false;
                }
            }
        }

        // (TODO) Compares the bids

        // (TODO) Compares the binary Items.
        return true;
    }

}

