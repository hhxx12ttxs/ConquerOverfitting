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
import org.oht.miami.msdtk.conversion.StudyProtos.SeriesGpb;
import org.oht.miami.msdtk.conversion.StudyProtos.StudyGpb;
import org.oht.miami.msdtk.deidentification.AttributeProfileActions;
import org.oht.miami.msdtk.deidentification.DeIdentifyUtil;
import org.oht.miami.msdtk.deidentification.DeIdentifyUtil.ActionCode;
import org.oht.miami.msdtk.deidentification.StudyProfileOptions;
import org.oht.miami.msdtk.store.BulkDataSet;
import org.oht.miami.msdtk.store.Store;
import org.oht.miami.msdtk.store.VersionType;
import org.oht.miami.msdtk.util.DicomElementUtil;
import org.oht.miami.msdtk.util.DicomObjectUtil;
import org.oht.miami.msdtk.util.DicomUID;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SimpleDicomElement;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.util.TagUtils;
import org.dcm4che2.util.UIDUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Study model class. It is the core of the MSD-toolkit. It stores the Dicom
 * study meta data and keep the binary data stored in a secondary storage
 * device. The study normalization is done using the one-pass algorithm.
 * 
 * @author maismail (maismail@cs.jhu.edu)
 */
public class Study implements java.io.Serializable {

    /**
     * Required by the Serializable interface.
     */
    private static final long serialVersionUID = -6786575031512671767L;

    /**
     * generated UID of the toolkit
     */
    public static final String IMPLEMENTATION_CLASS_UID = "2.25.173304553061931624249947962978008073687";

    /**
     * Implementation version name
     */
    public static final String IMPLEMENTATION_VERSION_NAME = "MSD 1.0.0";

    /**
     * Source AET for toolkit
     */
    public static final String SOURCE_AET = "MSD_TOOLKIT";

    /**
     * List of study level attributes.
     */
    protected final List<DicomElement> studyDicomElements;

    /**
     * List of series within the study
     */
    protected final List<Series> seriesList;

    /**
     * MSD-Store
     */
    protected final Store store;

    /**
     * Bulk Data Set
     */
    protected final BulkDataSet bulkDataSet;

    /**
     * Flag determine whether the study is normalized or not
     */
    private final boolean isNormalized;

    /**
     * The DICOM Study Instance UID for this study. All versions of the study
     * have the same StudyUID. This will become DE with
     * MediaStorageSOPInstanceUID [0002, 0002]
     */
    private final DicomUID studyInstanceUID;

    /**
     * Version type
     */
    private VersionType versionType;

    // Index of study and series attributes to be checked
    private int studyAttributeIndex = 0;
    private int seriesAttributeIndex = 0;

    /**
     * Constructs an empty study model with de-duplication.
     * 
     * @param store
     *            The MSD-Store for storing bulk data objects/files.
     * @param studyInstanceUID
     *            The Study Instance UID of this study.
     */
    public Study(Store store, DicomUID studyInstanceUID) {
        this(store, studyInstanceUID, true);
    }

    /**
     * Constructs an empty study model.
     * 
     * @param store
     *            The MSD-Store for storing bulk data objects/files.
     * @param studyInstanceUID
     *            The Study Instance UID of this study.
     * @param normalize
     *            Whether or not to perform de-duplication while building the
     *            model.
     */
    public Study(Store store, DicomUID studyInstanceUID, boolean normalize) {
        this.store = store;
        this.studyInstanceUID = studyInstanceUID;
        versionType = VersionType.New;
        bulkDataSet = new BulkDataSet(studyInstanceUID, store);
        studyDicomElements = new ArrayList<DicomElement>();
        seriesList = new ArrayList<Series>();
        isNormalized = normalize;
    }

    /**
     * Constructs an empty study model, which is then populated with attributes
     * from the given <code>DicomObject</code>.
     * 
     * @param store
     *            The MSD-Store for storing bulk data objects/files.
     * @param studyInstanceUID
     *            The Study Instance UID of this study.
     * @param normalize
     *            Whether or not to perform de-duplication while building the
     *            model.
     * @param dcmObj
     *            The <code>DicomObject</code> whose attributes will be loaded
     *            into the study model.
     */
    public Study(Store store, DicomUID studyInstanceUID, boolean normalize, DicomObject dcmObj) {
        this(store, studyInstanceUID, normalize);
        if (dcmObj != null) {
            addDicomObject(dcmObj);
        }
    }

    /**
     * Constructs a study model from the given Google Protobuf object.
     * 
     * @param store
     *            The MSD-Store for storing bulk data objects/files.
     * @param studyInstanceUID
     *            The Study Instance UID of this study.
     * @param gpbData
     *            The GPB object to build the model from.
     */
    @Deprecated
    public Study(Store store, DicomUID studyInstanceUID, StudyGpb gpbData) {
        this(store, studyInstanceUID, true);
        for (DicomElementGpb attrData : gpbData.getStudyDicomElementsList()) {
            putAttribute(GpbUtil.gpbToDicomElement(attrData));
        }
        for (SeriesGpb seriesData : gpbData.getSeriesList()) {
            Series series = new Series(this, seriesData);
            addSeries(series);
        }
    }

    /**
     * Adds a dcm4che2 Dicom object to the study model
     * 
     * @param dcmObj
     *            Dicom object to be added.
     */
    public synchronized void addDicomObject(final DicomObject dcmObj) {
        if (UID.MultiSeriesStudyStorage.equals(dcmObj.getString(Tag.MediaStorageSOPClassUID))) {
            DicomObjectUtil.removeFileMetaInformation(dcmObj);
            addMultiSeriesDicomObject(dcmObj);
        } else {
            DicomObjectUtil.removeFileMetaInformation(dcmObj);
            if (dcmObj.get(Tag.PerSeriesFunctionalGroupsSequence) != null) {
                addUnifiedMultiSeriesDicomObject(dcmObj);
            } else {
                if (isNormalized) {
                    addDicomObjectWithNormalization_tags(dcmObj);
                } else {
                    addDicomObjectWithoutNormalization(dcmObj);
                }
            }
        }
    }

    private void addMultiSeriesDicomObject(DicomObject multiSeriesDicomObject) {
        Iterator<DicomElement> studyAttributeIter = multiSeriesDicomObject.iterator(0x00020000,
                0xffffffff);
        while (studyAttributeIter.hasNext()) {
            DicomElement studyAttribute = studyAttributeIter.next();
            if (studyAttribute.tag() != Tag.PerSeriesFunctionalGroupsSequence) {
                studyDicomElements.add(studyAttribute);
            } else {
                for (int i = 0, n = studyAttribute.countItems(); i < n; i++) {
                    DicomObject seriesAttributes = studyAttribute.getDicomObject(i);
                    Series series = new Series(this);
                    addSeries(series);
                    Iterator<DicomElement> seriesAttributeIter = seriesAttributes.iterator();
                    while (seriesAttributeIter.hasNext()) {
                        DicomElement seriesAttribute = seriesAttributeIter.next();
                        if (seriesAttribute.tag() != Tag.PerInstanceFunctionalGroupsSequence) {
                            series.addAttribute(seriesAttribute);
                        } else {
                            for (int j = 0, m = seriesAttribute.countItems(); j < m; j++) {
                                DicomObject instanceAttributes = seriesAttribute.getDicomObject(j);
                                Instance instance = new Instance(series);
                                series.addInstance(instance);
                                Iterator<DicomElement> instanceAttributeIter = instanceAttributes
                                        .iterator();
                                while (instanceAttributeIter.hasNext()) {
                                    DicomElement instanceAttribute = instanceAttributeIter.next();
                                    if (instanceAttribute.tag() != Tag.PerFrameFunctionalGroupsSequence) {
                                        instance.addAttribute(instanceAttribute);
                                    } else {
                                        for (int k = 0, l = instanceAttribute.countItems(); k < l; k++) {
                                            DicomObject frameAttributes = instanceAttribute
                                                    .getDicomObject(k);
                                            Instance frame = instance.addFrame();
                                            Iterator<DicomElement> frameAttributeIter = frameAttributes
                                                    .iterator();
                                            while (frameAttributeIter.hasNext()) {
                                                DicomElement frameAttribute = frameAttributeIter
                                                        .next();
                                                frame.addAttribute(frameAttribute);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a Dicom object that represents multiseries to the study model.
     * 
     * @param dcmObj
     *            to be added. It has to contain
     *            StudyParameters.PerSeriesFunctionalGroupsSequene tag.
     */
    private void addUnifiedMultiSeriesDicomObject(DicomObject dcmObj) {
        final String studyInstanceUID = dcmObj.getString(Tag.StudyInstanceUID);
        /* Ensures that the instance has a Study instance UID */
        if (studyInstanceUID == null) {
            throw new RuntimeException(" -- missing study instance uid");
        }
        for (Iterator<DicomElement> studyDcmElementIter = dcmObj.iterator(0x00020000, 0xffffffff); studyDcmElementIter
                .hasNext();) {
            DicomElement studyDcmElement = studyDcmElementIter.next();
            final int studyTag = studyDcmElement.tag();
            // Adds study level attributes

            if (studyTag != Tag.PerSeriesFunctionalGroupsSequence) {
                this.putAttribute(studyDcmElement);
            } else {
                // Loop on different series
                for (int i = 0; i < studyDcmElement.countItems(); i++) {
                    DicomObject seriesObj = studyDcmElement.getDicomObject(i);
                    Series series = new Series(this);
                    this.addSeries(series);
                    // loop on the dicom elements of each series
                    final Instance instance = new Instance(series);
                    series.addInstance(instance);
                    for (Iterator<DicomElement> seriesDcmElementIter = seriesObj.iterator(
                            0x00020000, 0xffffffff); seriesDcmElementIter.hasNext();) {
                        DicomElement seriesDcmElement = seriesDcmElementIter.next();
                        // Adds Series level attribute
                        if (seriesDcmElement.tag() != Tag.PerFrameFunctionalGroupsSequence
                                && seriesDcmElement.tag() != Tag.PixelData) {
                            series.putAttribute(seriesDcmElement);
                        } else {
                            instance.putAttribute(seriesDcmElement);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return the versionType
     */
    public VersionType getVersionType() {
        return versionType;
    }

    /**
     * @param versionType
     *            the versionType to set
     */
    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    /**
     * Get bulk data set
     * 
     * @return bulk data set
     */
    public BulkDataSet getBulkDataSet() {
        return bulkDataSet;
    }

    /**
     * @see org.oht.miami.msdtk.store.BulkDataSet#appendValue(VR, byte[])
     */
    public BulkDataReference appendBulkDataValue(VR vr, byte[] value) throws IOException {
        return bulkDataSet.appendValue(vr, value);
    }

    /**
     * Finds the relevant series, or creates the series if it does not exist,
     * for the study and populates the attributes
     * 
     * @param dcmObj
     *            the object to build the model from
     */
    private void addDicomObjectWithoutNormalization(final DicomObject dcmObj) {
        final String dataStudyInstanceUID = dcmObj.getString(Tag.StudyInstanceUID);
        /* Ensures that the instance has a Study instance UID */
        if (dataStudyInstanceUID == null) {
            throw new RuntimeException(" -- missing study instance uid");
        }

        /* If this is not the first series in the study */
        if (seriesCount() > 0) {
            /*
             * Ensures the new element has the same Study instance UID,
             * otherwise throw exception
             */
            if (!this.getStudyInstanceUIDAsString().equals(dataStudyInstanceUID)) {
                throw new RuntimeException(" study instance uid (" + dataStudyInstanceUID
                        + ") does not match current study (" + this.getStudyInstanceUIDAsString()
                        + ')');
            }
        }
        /* Ensures that the instance has a Series instance UID */
        final String seriesInstanceUID = dcmObj.getString(Tag.SeriesInstanceUID);
        if (seriesInstanceUID == null) {
            throw new RuntimeException(" -- missing series instance uid");
        }
        final String sopInstanceUID = dcmObj.getString(Tag.SOPInstanceUID);

        /*
         * Search for the new series ID in the list of series that belongs to
         * the current study
         */
        Series series = this.getSeries(seriesInstanceUID);

        /*
         * Throws exception if there is an instance in the series with the same
         * sopinstanceUID as the input instance
         */
        if (series != null) {
            if (series.getInstance(sopInstanceUID) != null) {
                throw new RuntimeException(" -- SOPInstanceUID conflict."
                        + " Trying to insert a dicom object that already exists");
            }
        }
        /* If not found, this implies new series */
        if (series == null) {
            series = new Series(this);
            this.addSeries(series);
        }
        /* Adds the new file to the instance */
        series.addDicomObjectWithoutNormalization(dcmObj);
    }

    /**
     * Gets a specific attribute from the study
     * 
     * @param tag
     *            the tag of the attribute
     * @return the attribute for the given tag (in hex)
     */
    public DicomElement getAttribute(final int tag) {
        for (int i = 0; i < studyDicomElements.size(); i++) {
            DicomElement studyDcm = studyDicomElements.get(i);
            if (studyDcm.tag() == tag) {
                return studyDcm;
            }
        }
        return null;
    }

    /**
     * Searches for a specific attribute from the study
     * 
     * @param tag
     *            the tag of the attribute
     * @return the attribute for the given tag (in hex)
     */
    public DicomElement findAttribute(final int tag) {
        return this.getAttribute(tag);
    }

    /**
     * Puts an Attribute into the Series - attributes with a series have unique
     * tags
     * 
     * @param attr
     *            the attribute to add
     */
    public void putAttribute(final DicomElement attr) {
        if (getAttribute(attr.tag()) == null) {
            studyDicomElements.add(attr);
        } else {
            throw new IllegalArgumentException(String.format(
                    "Dicom element with tag 0x%x already exists.", attr.tag()));
        }
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
     * Removes the Attribute with the given tag from the Series
     * 
     * @param tag
     *            the tag of the attribute to remove
     */
    public DicomElement removeAttribute(final int tag) {
        DicomElement dcmElement = getAttribute(tag);
        studyDicomElements.remove(dcmElement);
        return dcmElement;
    }

    // TODO validate that this comment is correct (by JFP)
    /**
     * Removes the Attribute
     * 
     * @param attr
     *            the tag of the attribute to remove
     */
    public void removeAttribute(final DicomElement attr) {
        studyDicomElements.remove(attr);
    }

    /**
     * Gets an iterator over all the attributes in the study
     * 
     * @return an iterator of all Attributes in the Study
     */
    public Iterator<DicomElement> attributeIterator() {
        return studyDicomElements.iterator();
    }

    /**
     * Puts an Series into the Study - instances are unique per xfer
     * 
     * @param series
     *            the series to add to the study
     */
    public void addSeries(final Series series) {
        seriesList.add(series);
    }

    /**
     * Removes an series from the Study based on its UID
     * 
     * @param seriesUID
     *            the UID of the series to remove
     */
    public void removeSeries(final String seriesUID) {
        seriesList.remove(this.getSeries(seriesUID));
    }

    /**
     * Removes an series from the study
     * 
     * @param series
     *            the series to remove
     */
    public void removeSeries(Series series) {
        seriesList.remove(series);
    }

    /**
     * Gets a series within the study based on its UID
     * 
     * @param seriesUID
     *            the UID of the series
     * @return the series
     */
    public Series getSeries(final String seriesUID) {
        for (Iterator<Series> it = seriesList.iterator(); it.hasNext();) {
            Series temp = it.next();
            if (seriesUID.equals(temp.getSeriesInstanceUID())) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Gets an iterator over all the series in the study
     * 
     * @return an iterator of all series in the study
     */
    public Iterator<Series> seriesIterator() {
        return seriesList.iterator();
    }

    /**
     * Gets the number of series in the study
     * 
     * @return the number of series in the Study
     */
    public int seriesCount() {
        return seriesList.size();
    }

    /**
     * Gets the 'studyUID' attribute value String
     * 
     * @return value
     */
    public String getStudyInstanceUIDAsString() {
        return studyInstanceUID.toString();
    }

    public DicomUID getStudyInstanceUID() {
        return studyInstanceUID;
    }

    /**
     * Gets the patient name from the study
     * 
     * @return the name of the patient
     */
    public String getPatientName() {
        DicomElement attr = this.getAttribute(Tag.PatientName);
        return attr.getValueAsString(null, 0);
    }

    /**
     * Writes the study to GPB
     * 
     * @throws IOException
     */
    @Deprecated
    public void writeToGPB() throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(
                this.studyInstanceUID.toString() + ".gpb"));
        StudyGpb.Builder builder = StudyGpb.newBuilder();

        /* Writes the study attributes */
        for (Iterator<DicomElement> attr = attributeIterator(); attr.hasNext();) {
            builder.addStudyDicomElements(GpbUtil.dicomElementToGPB(attr.next()));
        }

        /* Write the data of each series */
        for (Iterator<Series> attr = seriesIterator(); attr.hasNext();) {
            builder.addSeries(attr.next().writeToGPB());
        }
        StudyGpb data = builder.build();
        data.writeTo(out);
        out.close();
    }

    /**
     * Scans all series. Searches for an instance using seriesInstanceUID
     * 
     * @param sopInstanceUID
     *            the UID of the instance
     * @return the instance
     */
    public Instance getInstance(String sopInstanceUID) {
        /*
         * Scans all series. Asks each series if it has this instanceUID. If yes
         * return instance else continue searching in the series list
         */
        for (Iterator<Series> it = seriesList.iterator(); it.hasNext();) {
            Instance inst = it.next().getInstance(sopInstanceUID);
            if (inst != null) {
                return inst;
            }
        }
        return null;
    }

    /**
     * Accumulates the total number of frames in the study
     * 
     * @return the total number of frames
     */
    public int frameCount() {
        int count = 0;
        for (Series series : seriesList) {
            for (Iterator<Instance> iter = series.instanceIterator(); iter.hasNext();) {
                count += iter.next().getNumberOfFrames();
            }
        }
        return count;
    }

    /**
     * Gets the total number of dicom elements of this study
     * 
     * @return the total count
     */
    public int elementCount() {
        int count = 0;
        for (DicomElement elem : studyDicomElements) {
            count += DicomElementUtil.getCount(elem);
        }
        for (Series series : seriesList) {
            count += series.elementCount();
        }
        return count;
    }

    /**
     * Writes the contents of the study to a file
     * 
     * @param filename
     *            the path of the file to write to
     * @throws IOException
     */
    public void dumpStudyContents(String filename) throws IOException {
        File file = new File(filename);
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);

        /* Writes study level attributes */
        fos.write("Study Attributes:\n".getBytes());
        for (DicomElement de : this.studyDicomElements) {
            writeAttribute(fos, de, "");
        }

        /* Writes series level attributes for each series within the study */
        fos.write("\nSeries Attributes:\n".getBytes());
        for (Series series : this.seriesList) {
            fos.write(("\n\tSeries " + series.getSeriesInstanceUID() + ":\n").getBytes());
            Iterator<DicomElement> seriesAttrIter = series.attributeIterator();
            while (seriesAttrIter.hasNext()) {
                writeAttribute(fos, seriesAttrIter.next(), "\t");
            }
            /* Writes instance level attributes for each instance */
            fos.write("\nInstance Attributes:\n".getBytes());
            Iterator<Instance> instIter = series.instanceIterator();
            while (instIter.hasNext()) {
                Instance instance = instIter.next();
                fos.write(("\n\t\tInstance " + instance.getSOPInstanceUID() + ":\n").getBytes());
                Iterator<DicomElement> instAttrIter = instance.attributeIterator();
                while (instAttrIter.hasNext()) {
                    DicomElement r = instAttrIter.next();
                    writeAttribute(fos, r, "\t\t");
                }

                if (instance.getChildrenFrames() != null) {
                    Iterator<Instance> childIter = instance.getChildrenFrames().iterator();
                    while (childIter.hasNext()) {
                        Instance frame = childIter.next();
                        fos.write(("\n\t\tFrame " + frame.getSOPInstanceUID() + ":\n").getBytes());
                        Iterator<DicomElement> frameAttrIter = frame.attributeIterator();
                        while (frameAttrIter.hasNext()) {
                            DicomElement r = frameAttrIter.next();
                            writeAttribute(fos, r, "\t\t");
                        }
                    }
                }
            }
        }
        fos.close();
    }

    /**
     * Helper method to write a single attribute to an output stream.
     * 
     * @param out
     *            the output stream to write the data to
     * @param elem
     *            the attribute to be written
     * @param padding
     *            the string (whitespace) to write before writing the attribute
     * @throws IOException
     */
    private void writeAttribute(OutputStream out, DicomElement elem, String padding)
            throws IOException {
        /* Writes the attribute */
        out.write((padding + elem.toString() + "\n").getBytes());

        /* If elem is a sequence element, write all children attributes */
        if (elem.hasDicomObjects()) {
            for (int i = 0; i < elem.countItems(); i++) {
                DicomObject obj = elem.getDicomObject(i);
                Iterator<DicomElement> iter = obj.iterator();
                while (iter.hasNext()) {
                    writeAttribute(out, iter.next(), padding + "\t");
                }
            }
        }
    }

    /**
     * Adds the first instance to an empty study.
     * 
     * @param dcmObj
     */
    private void addFirstDicomInstance(final DicomObject dcmObj) {
        /*
         * Special function to add the first instance to the study. most of the
         * attributes are added to the study level attributes
         */
        // Creates Series
        Series series = new Series(this);
        this.addSeries(series);

        // Creates instance
        final Instance instance = new Instance(series);
        series.addInstance(instance);

        for (Iterator<DicomElement> dcmElement = dcmObj.iterator(0x00020000, 0xffffffff); dcmElement
                .hasNext();) {
            DicomElement element = dcmElement.next();
            final int tag = element.tag();
            switch (tag) {
            case Tag.SeriesInstanceUID:
                series.putAttribute(element);
                break;

            case Tag.PerFrameFunctionalGroupsSequence:
            case Tag.SOPInstanceUID:
            case Tag.PixelData:
                instance.putAttribute(element);
                break;

            // Adds all attributes to the study level attributes
            default:
                this.putAttribute(element);
                break;
            }
        }
    }

    /**
     * Adds a Dicom object to the study model with normalization The
     * normalization is done using the UPDATED version of the 1-pass algorithm
     * 
     * @param dcmObj
     *            Dicom object to be added.
     */
    private void addDicomObjectWithNormalization_tags(final DicomObject dcmObj) {
        final String studyInstanceUID = dcmObj.getString(Tag.StudyInstanceUID);
        /* Ensures the instance has a Study instance UID */
        if (studyInstanceUID == null) {
            throw new RuntimeException(" -- missing study instance uid");
        }

        /* Ensures the instance has a Series instance UID */
        final String seriesInstanceUID = dcmObj.getString(Tag.SeriesInstanceUID);
        if (seriesInstanceUID == null) {
            throw new RuntimeException(" -- missing series instance uid");
        }

        /*
         * Adds all attributes to the list of study attributes if this is the
         * first instance in the study
         */
        if (seriesCount() == 0) {
            addFirstDicomInstance(dcmObj);
            return;
        }

        /*
         * Ensures the new element has the same Study instance UID, otherwise
         * throw exception
         */
        if (!this.getStudyInstanceUIDAsString().equals(studyInstanceUID)) {
            throw new RuntimeException(" study instance uid (" + studyInstanceUID
                    + ") does not match current study (" + this.getStudyInstanceUIDAsString() + ')');
        }

        /*
         * Searches for the new series ID in the list of series that belongs to
         * the current study If not found, this implies new series
         */
        Series series = this.getSeries(seriesInstanceUID);
        if (series == null) {
            series = new Series(this);
            this.addSeries(series);
        }
        final Instance instance = new Instance(series);
        series.addInstance(instance);

        // Now, iterate through all items in the object and store each
        // appropriately.
        // The attribute is stored in one of the study level, series level
        // or instance-level Attributes sets.
        Iterator<DicomElement> dcmElement = dcmObj.iterator(0x00020000, 0xffffffff);
        DicomElement elem = null;

        seriesAttributeIndex = 0;
        studyAttributeIndex = 0;
        /* Loop on all DicomElements */
        while (dcmElement.hasNext()) {
            // Reads the next DicomElement if there is any, break otherwise.
            elem = dcmElement.next();
            if (elem.tag() == Tag.PerFrameFunctionalGroupsSequence
                    || elem.tag() == Tag.SOPInstanceUID) {
                instance.putAttribute(elem);
            } else {
                insertDE(elem, series, instance);
            }
        }
    }

    private void insertDE(DicomElement elem, Series series, Instance instance) {

        /*
         * DicomElements with the lowest tag number that has not been scanned
         */
        DicomElement studyAttribute = getAttributeAtIndexOf(studyAttributeIndex);
        DicomElement seriesAttribute = series.getAttributeAtIndexOf(seriesAttributeIndex);

        if (studyAttribute != null) {
            if (elem.tag() == studyAttribute.tag()) {

                /*
                 * If the new attribute does not equal to the the attribute that
                 * has the same tag, fix up (remove it from the study list of
                 * attributes and add it to the all sereises and all instances.)
                 */
                if (!DicomElementUtil.compareDicomElements(studyAttribute, elem, false)) {

                    /*
                     * Before denormalizing this study attribute, ensure there
                     * is no DICOM Elements in series list of attributes with a
                     * tag number lower than the element to be normalized.
                     * otherwise, the newly inserted item in the series list of
                     * attributes will be scanned twice.
                     */
                    int next = seriesAttributeIndex;
                    while (seriesAttribute != null) {
                        if (seriesAttribute.tag() < elem.tag()) {
                            series.deNormalizeSeriesAttribute(seriesAttribute, null, instance);
                            next++;
                            seriesAttribute = series.getAttributeAtIndexOf(next);
                        } else
                            break;
                    }
                    deNormalizeStudyAttribute(studyAttribute, elem, series, instance);
                    // If the number of instances in the current series = 1,
                    // increases series attribute index because there is an
                    // attribute has
                    // been added to the series list of attributes.
                    if (series.getInstanceListSize() == 1)
                        seriesAttributeIndex++;
                } else {
                    // Increases the study attribute index if the new
                    // attribute has the same value as the study attribute
                    studyAttributeIndex++;
                }
                return;
            }

            if (elem.tag() > studyAttribute.tag()) {

                // De-normalizes study attributes with tag number < the new
                // attribute. Ensures there is no DICOM
                // attributes in series list of attributes with a tag number
                // lower than the element to be normalized.
                // otherwise, the newly inserted (de-normalized) item in the
                // series list of attributes will be scanned twice.
                int next = seriesAttributeIndex;
                while (seriesAttribute != null) {
                    if (seriesAttribute.tag() < elem.tag()) {
                        series.deNormalizeSeriesAttribute(seriesAttribute, null, instance);
                        next++;
                        seriesAttribute = series.getAttributeAtIndexOf(next);
                    } else
                        break;
                }
                deNormalizeStudyAttribute(studyAttribute, null, series, instance);
                insertDE(elem, series, instance);
                return;
            }
        }

        if (seriesAttribute != null) {
            if (elem.tag() == seriesAttribute.tag()) {

                // If the new attribute value does not equal to the the
                // attribute
                // that has the same tag, fix up (remove it from the series list
                // of attributes
                // and add it to all instances.)
                if (!DicomElementUtil.compareDicomElements(seriesAttribute, elem, false)) {
                    series.deNormalizeSeriesAttribute(seriesAttribute, elem, instance);
                } else {
                    seriesAttributeIndex++;
                }
                return;
            }

            if (elem.tag() > seriesAttribute.tag()) {
                // Denormalizes series Attribute
                series.deNormalizeSeriesAttribute(seriesAttribute, null, instance);
                insertDE(elem, series, instance);
                return;
            }
        }

        int tag = elem.tag();
        switch (tag) {
        case Tag.SeriesInstanceUID:
            series.putAttributeInOrder(elem);
            seriesAttributeIndex++;
            break;

        case Tag.PixelData:
            instance.putAttribute(elem);
            break;

        default:
            if (series.getInstanceListSize() == 1) {
                series.putAttributeInOrder(elem);
                seriesAttributeIndex++;
            } else {
                instance.putAttribute(elem);
            }
            break;
        }
    }

    /**
     * Remove an attribute from the study list of attributes and insert it into
     * series level attributes for all series within the study
     * 
     * @param oldElement
     *            The attribute to be added to the previously stored list of
     *            series.
     * @param newElement
     *            The attribute to be added to the newly added series.
     * @param differentSeries
     *            The newly added series that has a different attribute value
     * @param differentInstance
     *            The newly added instance to the study.
     */
    private void deNormalizeStudyAttribute(final DicomElement oldElement,
            final DicomElement newElement, final Series differentSeries,
            final Instance differentInstance) {
        // Special part to handle single series case.
        // If there is a single series, usually it has
        // only one attribute which is the seriesUID
        if (seriesCount() == 1) {
            // Does the de-normalization, adds the attribute to all
            // instances within the series and then remove it from the
            // the study attribute list.
            for (Iterator<Instance> ittr = differentSeries.instanceIterator(); ittr.hasNext();) {
                Instance inst = ittr.next();
                if (inst == differentInstance)
                    inst.putAttribute(newElement);
                else
                    inst.putAttributeInOrder(oldElement);
            }
        } else {
            // If more than one series, de-nomalize this attribute.
            // Add the attribute to all serieses and then remove
            // from the study
            for (Iterator<Series> it = seriesIterator(); it.hasNext();) {
                Series series = it.next();
                if (series == differentSeries) {
                    series.addAttributeWithNormalization(oldElement, newElement, differentInstance);
                } else {
                    series.putAttributeInOrder(oldElement);
                }
            }
        }
        this.removeAttribute(oldElement.tag());
    }

    /**
     * @return boolean shows whether the study is normalized or not.
     */
    public boolean isNormalized() {
        return isNormalized;
    }

    /**
     * Get the study attribute stored in the given index
     * 
     * @param index
     *            Integer determines the location of the required attribute in
     *            the series attribute list.
     * @return DicomElement The study attribute at the specified index, null if
     *         not found.
     */
    public DicomElement getAttributeAtIndexOf(final int index) {
        if (index < 0 || index >= studyDicomElements.size())
            return null;
        return studyDicomElements.get(index);
    }

    /**
     * De-Identify an attribute in the study.
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
    private boolean deIdentifyAttribute(DicomElement elem, AttributeProfileActions actions,
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
            this.studyDicomElements.add(updatedElem);
            return true;

        case Z:
        case ZD:
            DicomElement zeroElem = DeIdentifyUtil.apply_Z_Action(elem);
            this.removeAttribute(elem);
            this.studyDicomElements.add(zeroElem);
            return true;

        case K:
            if (elem.vr() == VR.SQ) {
                DeIdentifyUtil.apply_K_Action(elem, actions, options, null);
                if (elem.countItems() == 0)
                    this.removeAttribute(elem);
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
     * DeIdentify the study. Remove, modify or keep the attributes as determined
     * by the de-Identification profile. If an attribute is not listed in the
     * de-identification profile, it is kept without any change.
     * 
     * @param pOptions
     *            : Determine which profile options are chosen.
     */
    public void deIdentifyStudy(final StudyProfileOptions pOptions) {
        // Checks the patient identity attribute. If the identity has been
        // already removed, return

        DicomElement identity = this.getAttribute(Tag.PatientIdentityRemoved);
        if (identity != null) {
            if (identity.getValueAsString(null, 0).compareToIgnoreCase("yes") == 0) {
                return;
            }
        }

        int size = studyDicomElements.size();
        for (int i = 0; i < size; i++) {
            DicomElement elem = studyDicomElements.get(i);
            // remove private tags
            // TODO allow option to retain private tags
            if (TagUtils.isPrivateDataElement(elem.tag())) {
                this.removeAttribute(elem);
                i--;
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
            } else { // De-identify file meta data attributes

                if (elem.tag() == Tag.SourceApplicationEntityTitle) {
                    elem.setValue((byte[]) null);
                }

                else if (elem.tag() == Tag.PrivateInformationCreatorUID) {
                    elem.setValue((byte[]) null);
                }

                else if (elem.tag() == Tag.PrivateInformation) {
                    elem.setValue((byte[]) null);
                }
            }
        }

        String value = "";
        if (pOptions.isBasicProfile)
            value += DeIdentifyUtil.BasicApplicationConfidentialityProfile;
        if (pOptions.isCleanGraphOption)
            value += DeIdentifyUtil.CleanGraphicsOption;
        if (pOptions.isCleanStructContOption)
            value += DeIdentifyUtil.CleanStructuredContentOption;
        if (pOptions.isCleanDescOption)
            value += DeIdentifyUtil.CleanDescriptorsOption;
        if (pOptions.isRetainLongFullDatesOption)
            value += DeIdentifyUtil.RetainLongitudinalTemporalInformationWithFullDatesOption;
        if (pOptions.isRetainLongModifiedDatesOption)
            value += DeIdentifyUtil.RetainLongitudinalTemporalInformationWithModifiedDatesOption;
        if (pOptions.isRetainPatientCharsOption)
            value += DeIdentifyUtil.RetainPatientCharacteristicsOption;
        if (pOptions.isRetainDeviceIdentOption)
            value += DeIdentifyUtil.RetainDeviceIdentityOption;
        if (pOptions.isRetainUIDsOption)
            value += DeIdentifyUtil.RetainUIDsOption;
        if (pOptions.isRetainSafePrivateOption)
            value += DeIdentifyUtil.RetainSafePrivateOption;

        DicomElement newElement = new SimpleDicomElement(Tag.DeidentificationMethodCodeSequence,
                VR.LO, true, value.getBytes(), null);

        putInOrder(newElement);

        for (Series series : seriesList) {
            series.deIdentifySeries(pOptions);
        }

        DeIdentifyUtil.addDeIdentifyInfo(this, pOptions);
    }

    /**
     * Adds a dicom element in correct tag order
     * 
     * @param newElement
     *            The dicom element to be inserted
     * @return true if the element was inserted; false if the element was not
     *         inserted
     */
    private boolean putInOrder(DicomElement newElement) {
        int size = studyDicomElements.size();
        for (int i = 0; i < size; i++) {
            DicomElement elem = studyDicomElements.get(i);
            if (elem.tag() > newElement.tag()) {
                studyDicomElements.add(i, newElement);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Study)) {
            return false;
        }
        Study otherStudy = (Study) obj;

        // Compares study level attributes.
        Iterator<DicomElement> otherStudyAttrIter = otherStudy.attributeIterator();
        for (DicomElement attr : studyDicomElements) {
            int tag = attr.tag();
            if (tag == Tag.SOPInstanceUID || TagUtils.isFileMetaInfoElement(tag)
                    && tag != Tag.SourceApplicationEntityTitle) {
                continue;
            }
            DicomElement otherAttr = otherStudyAttrIter.next();
            int otherTag = otherAttr.tag();
            while (otherTag == Tag.SOPInstanceUID || TagUtils.isFileMetaInfoElement(otherTag)
                    && otherTag != Tag.SourceApplicationEntityTitle) {
                otherAttr = otherStudyAttrIter.next();
                otherTag = otherAttr.tag();
            }
            if (!DicomElementUtil.compareDicomElements(attr, otherAttr, true)) {
                return false;
            }
        }

        // Compares series within the study
        if (this.seriesCount() != otherStudy.seriesCount()) {
            return false;
        }
        for (Series series : seriesList) {
            Series otherSeries = otherStudy.getSeries(series.getSeriesInstanceUID());
            if (otherSeries == null) {
                return false;
            }
            if (!series.equals(otherSeries)) {
                return false;
            }
        }
        return true;
    }

}

