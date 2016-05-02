/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.duke.archives.erprocessor.metadata.mets;

import edu.duke.archives.erprocessor.metadata.QualifiedMetadata;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Seth Shaw
 */
public class EAD {

    public boolean display_items = true;
    public boolean include_all_metadata = true;
//    private int date_granularity = 1;
    private METS mets;
    public static final int YEAR = 1; //Default
    public static final int YEAR_MONTH = 2;
    public static final int YEAR_MONTH_DAY = 3;
    private static final DecimalFormat levelDF = new DecimalFormat("00");
    private static final DecimalFormat fileCountDF =
            new DecimalFormat("###,###,###,###");
    private static final DecimalFormat fileSizeDF =
            new DecimalFormat("#,##0.##");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
    private static SimpleDateFormat parseDateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private static SimpleDateFormat normalDateFormat =
            new SimpleDateFormat("yyyy");

    public String convert2EAD(METS mets) {
        this.mets = mets;
        //for the Collection structMap
        EADLevel myself = new EADLevel(new Element("ead_pieces"));
        Enumeration children = mets.getStructMap(METS.COLLECTION_STRUCTMAP).children();
        while(children.hasMoreElements()) {
//        for (Object series : mets.getStructMap(METS.COLLECTION_STRUCTMAP).element.getChildren("div",
//                Namespace.getNamespace("mets", "http://www.loc.gov/METS/"))) {
            Object series = children.nextElement();
            if (!(series instanceof MetsNode)) {
                continue;
            }
            EADLevel childLevel = recurseDir((MetsNode) series, 1);
            myself.level.addContent(childLevel.level);
        }
        Format format = Format.getPrettyFormat();
        XMLOutputter outputter = new XMLOutputter(format);
        return outputter.outputString(myself.level);
    }

    private synchronized EADLevel recurseDir(MetsNode node, int cLevel) {
        EADLevel elementLevel = new EADLevel(cLevel);
        Element did = new Element("did");
        elementLevel.level.addContent(did);

        QualifiedMetadata scopeQM =
                QualifiedMetadata.getQM(node.getQualifiedMetadata(),
                "description", "");
        if (scopeQM != null && !scopeQM.getValue().equalsIgnoreCase("")) {
            Element p = new Element("p");
            p.setText(scopeQM.getValue());
            Element scopecontent = new Element("scopecontent");
            scopecontent.addContent(p);
            elementLevel.level.addContent(scopecontent);
        }
        
        QualifiedMetadata restrictionQM =
                QualifiedMetadata.getQM(node.getQualifiedMetadata(),
                "restriction", "");
        if (restrictionQM != null && !restrictionQM.getValue().equalsIgnoreCase("")) {
            Element p = new Element("p");
            p.setText("RESTRICTION: "+restrictionQM.getValue());
            Element accessRestriction = new Element("accessrestrict");
            accessRestriction.addContent(p);
            elementLevel.level.addContent(accessRestriction);
        }
        
        String title = "Unknown";

        title = node.toString();
        if (node instanceof MetsFile) {
            
            elementLevel.totalSize = ((MetsFile) node).getSize();

            //Parse Date
            try {
                elementLevel.earliest =
                        parseDateFormat.parse(((MetsFile)node).getLastModified());
                elementLevel.latest = elementLevel.earliest;
            } catch (ParseException ex) {
                System.err.println("SimpleDateFormat (" +
                        parseDateFormat.toPattern() +
                        ") could not parse: " + ((MetsFile)node).getLastModified());
//                        ex.printStackTrace();
            }
            elementLevel.fileCount = 1;
        } else {
            Enumeration children = node.children();
            while(children.hasMoreElements()){
                Object child = children.nextElement();
                if (!(child instanceof MetsNode)) {
                    continue;
                }
                EADLevel childLevel = recurseDir((MetsNode) child, cLevel + 1);
                elementLevel.level.addContent(childLevel.level);
                // Add to the totals
                elementLevel.fileCount += childLevel.fileCount;
                if (childLevel.totalSize > 0) {
                    elementLevel.totalSize += childLevel.totalSize;
                }
                //Check Dates for earliest and latest
                if (childLevel.earliest != null) {
                    if (elementLevel.earliest == null ||
                            (elementLevel.earliest.after(childLevel.earliest))) {
                        elementLevel.earliest = childLevel.earliest;
                    }
                } //Else don't bother
                if (childLevel.latest != null) {
                    if (elementLevel.latest == null ||
                            (elementLevel.latest.before(childLevel.latest))) {
                        elementLevel.latest = childLevel.latest;
                    }
                } //Else don't bother
            }

        }
  
        if (include_all_metadata) {
            Element all_metadata = new Element("additional_metadata");
            for(QualifiedMetadata qm : node.getQualifiedMetadata()){
                Element item = new Element(qm.getElement());
                if(!qm.getQualifier().equalsIgnoreCase("")){
                    item.setAttribute("qualifier", qm.getQualifier());
                }
                item.setText(qm.getValue());
                all_metadata.addContent(item);
            }
            
        }
        //Alternate title
        String value = QualifiedMetadata.getQM(node.getQualifiedMetadata(),
                "title", "alternate").getValue();
        if (value != null && !value.equalsIgnoreCase("")) {
            title = value;
        }
        //Unit title / date
        Element unitTitle = new Element("unittitle");
        unitTitle.addContent(title);
        String unitdateStr = "";
        String unitdateNormalStr = "";
        if (elementLevel.earliest != null) {
            unitdateStr = dateFormat.format(elementLevel.earliest);
            unitdateNormalStr = normalDateFormat.format(elementLevel.earliest);
            if (elementLevel.latest != null &&
                    !(elementLevel.earliest.equals(elementLevel.latest))
                    &&!(unitdateStr.equalsIgnoreCase(dateFormat.format(elementLevel.latest)))) {
                unitdateStr += " - "+dateFormat.format(elementLevel.latest);
                unitdateNormalStr += "/"+normalDateFormat.format(elementLevel.latest);
                
            }
        } else if (elementLevel.latest != null) {
            if (elementLevel.earliest == null ||
                    !(elementLevel.earliest.equals(elementLevel.latest))) {
                unitdateStr += dateFormat.format(elementLevel.latest);
                unitdateNormalStr +=
                        normalDateFormat.format(elementLevel.latest);
            }
        }
        if (!unitdateStr.equalsIgnoreCase("")) {
            Element unitDate = new Element("unitdate");
            unitDate.setText(unitdateStr);
            if (!unitdateNormalStr.equalsIgnoreCase("")) {
                unitDate.setAttribute("normal", unitdateNormalStr);
            }
            unitDate.setAttribute("type", "inclusive");
            unitTitle.addContent(", ");
            unitTitle.addContent(unitDate);
        }
        did.addContent(unitTitle);

        Element extent = new Element("extent");
        String extentStr = "";
        if (elementLevel.fileCount > 1) {
            extentStr = fileCountDF.format(elementLevel.fileCount) +
                    " electronic files";
        }
        if (elementLevel.totalSize > 0) {
            if (elementLevel.fileCount > 1) {
                extentStr += " , ";
            }
            extentStr += prettySize(elementLevel.totalSize);
        }
        if (extentStr.length() > 0) {
            extent.setText("(" + extentStr + ")");
            Element physdesc = new Element("physdesc");
            physdesc.addContent(extent);
            did.addContent(physdesc);
        }

        return elementLevel;
    }

    /* 
     * EADLevel is a wrapper to store aggregated information before
     * it is finally formated and written to the EAD
     */
    private class EADLevel {

        long totalSize = 0;
        long fileCount = 0;
        Date latest;
        Date earliest;
        Element level;

        public EADLevel(Element level) {
            this.level = level;
        }

        public EADLevel(int level) {
            this.level = new Element("c" + levelDF.format(level));
        }
    }

    static private String prettySize(long size) {
        String prettySize = "";
        String[] measures = {"B", "KB", "MB", "GB", "TB", "EB", "ZB", "YB"};

        int power = measures.length - 1;
        //Cycle each measure starting with the smallest
        for (int i = 0; i < measures.length; i++) {
            //Test for best fit 
            if ((size / (Math.pow(1024, i))) < 1024) {
                power = i;
                break;
            }
        }

        Double newSize = (size / (Math.pow(1024, power)));
        prettySize = fileSizeDF.format(newSize) + " " + measures[power];
        return prettySize;
    }

    public void setDateGranularity(int granularity) {
        if (granularity == YEAR) {
            dateFormat = new SimpleDateFormat("yyyy");
            normalDateFormat = dateFormat;
        } else if (granularity == YEAR_MONTH) {
            dateFormat = new SimpleDateFormat("yyyy MMMMMMM");
            normalDateFormat = new SimpleDateFormat("yyyyMM");
        } else if (granularity == YEAR_MONTH_DAY) {
            dateFormat = new SimpleDateFormat("yyyy MMMMMMM d");
            normalDateFormat = new SimpleDateFormat("yyyyMMdd");
        }
    }
}

