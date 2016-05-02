package com.bmc.arsys.demo.javadriver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import com.bmc.arsys.api.ARException;
import com.bmc.arsys.api.ARServerUser;
import com.bmc.arsys.api.ByteListValue;
import com.bmc.arsys.api.Constants;
import com.bmc.arsys.api.Container;
import com.bmc.arsys.api.DataType;
import com.bmc.arsys.api.DisplayInstanceMap;
import com.bmc.arsys.api.Field;
import com.bmc.arsys.api.Image;
import com.bmc.arsys.api.ImageCriteria;
import com.bmc.arsys.api.ImageData;
import com.bmc.arsys.api.PropertyMap;
import com.bmc.arsys.api.Reference;
import com.bmc.arsys.api.ReferenceType;
import com.bmc.arsys.api.Value;
import com.bmc.arsys.api.View;
import com.bmc.arsys.api.ViewCriteria;
import com.bmc.arsys.api.ViewDisplayPropertyMap;

public class ImageExtractor extends JavaDriver {
    static OutputWriter outputWriter = new OutputWriter();
    ARServerUser arServerUser;
    int totalImages;
    int totalDistinctImages;
    long memorySaving;
    long totalDistinctImageSize;
    ArrayList<String> schemaNames;
    HashMap<Long, String> imageMap; 
    Checksum checksumEngine;
    long curImageChecksum;
    int imageChunkSize;


    public ImageExtractor() {
        initThreadControlBlockPtr();
        schemaNames = new ArrayList<String>();
        imageMap = new HashMap<Long, String>();
        totalImages = 0;
        totalDistinctImages = 0;
        checksumEngine = new Adler32();
        curImageChecksum = 0;
        memorySaving = 0;
        totalDistinctImageSize = 0;
        String chunkStr = System.getProperty("ImageChunkSize");
        if (chunkStr == null) /* use the default image chunk size */
            imageChunkSize = 20;
        else {
            Integer myInt = new Integer(chunkStr);
            imageChunkSize = myInt.intValue();
        }
    }
    
    public long getTotalImageSize() {
        return totalDistinctImageSize;
    }
    public long getMemorySaving() {
        return memorySaving;
    }
    public int getTotalImageCount() {
        return totalImages;
    }
    
    public int getDistinctImageCount() {
        return totalDistinctImages;
    }
    
    public int getSchemaCount() {
        return schemaNames.size();
    }
    
    long calculateChecksum(byte[] inByte) {
        //Prepare checksum engine for next byte array by calling reset()
        checksumEngine.reset();
        checksumEngine.update(inByte, 0, inByte.length);
        return checksumEngine.getValue();
    }

    void populateImageList() throws ARException {
        //Get all the images on server 
        outputWriter.printHeader("", "Using image chunk value : " + imageChunkSize + ".", "\n");
        List <String> imageNames = null;
        imageNames = arServerUser.getListImage();
        if (imageNames == null)
            return;
        int imagesInServer = imageNames.size();
        outputWriter.printHeader("", "There are  " + imagesInServer + " images in the system.", "\n");
        int numCompleteChunks = imagesInServer/imageChunkSize;
        int partialChunksize = imagesInServer%imageChunkSize;
        int curIndex = 0;
        for (int i=0; i<numCompleteChunks; ++i) {
            /* Process the complete chunk of existing images */
            processExistingImages(imageNames.subList(curIndex, (curIndex+imageChunkSize)));
            curIndex += imageChunkSize;
        }
        /* Process the complete chunk of existing images */
        processExistingImages(imageNames.subList(curIndex, (curIndex+partialChunksize)));
    }

    private void processExistingImages(List<String> imageNames) throws ARException {
        List <Image> imageList = null;
        /* Get the complete chunk of images */
        ImageCriteria criteria = new ImageCriteria();
        criteria.setRetrieveAll(true);
        imageList = arServerUser.getListImageObjects(imageNames, criteria);
        if (imageList == null) 
            return;
        Checksum checksumEngine = new Adler32();
        //process each image
        for (int i = 0; i < imageList.size(); ++i) {
            Image curImage = imageList.get(i);
            byte[] bytes = curImage.getImageData().getValue();
            totalDistinctImageSize += bytes.length;
            //Compute Adler-32 checksum

            checksumEngine.update(bytes, 0, bytes.length);
            long checksum = checksumEngine.getValue();
            imageMap.put(new Long(checksum), curImage.getName());
            //	Prepare checksum engine for a different byte array by calling reset()
            checksumEngine.reset();
        }
    }

    void readSchemaNames(String fileName, boolean useApplication) throws ARException, FileNotFoundException, IOException {
        if ((fileName == null) || (fileName.length() == 0)) {
            //Get all the forms on server 
            schemaNames = (ArrayList<String>) arServerUser.getListForm((long) 0);
        } else {
            if (useApplication)
                populateSchemaNamesUsingApplicationNames(fileName);
            else
                populateSchemaNamesFromFile(fileName);
        }
        return;
    }

    private void populateSchemaNamesFromFile(String fileName) throws FileNotFoundException, IOException {
        JavaDriver.getThreadControlBlockPtr().setCurrentInputFile(fileName);
        String inputLine = null;
        BufferedReader currentInputFile = JavaDriver.getThreadControlBlockPtr().getCurrentInputFile();
        inputLine = currentInputFile.readLine();
        while (inputLine != null) {
            /* Remove any trailing white spaces */
            int index = 0;
            for (index = inputLine.length() - 1; index >= 0; index--) {
                if ((inputLine.charAt(index) != ' ') || (inputLine.charAt(index) != '\t')
                        || (inputLine.charAt(index) != '\n')) {
                    break;
                }
            }

            /* Process if it is a comment */
            if ((inputLine.length() >= 1) && (inputLine.charAt(0) == '#')) {
                continue;
            }
            schemaNames.add((String) inputLine);
            inputLine = currentInputFile.readLine();
        }
        JavaDriver.getThreadControlBlockPtr().closeCurrentInputFile();
    }
    
    private void populateSchemaNamesUsingApplicationNames(String fileName) throws ARException,
                                                            FileNotFoundException, IOException {
        JavaDriver.getThreadControlBlockPtr().setCurrentInputFile(fileName);
        String inputLine = null;
        BufferedReader currentInputFile = JavaDriver.getThreadControlBlockPtr().getCurrentInputFile();
        inputLine = currentInputFile.readLine();
        while (inputLine != null) {
            /* Remove any trailing white spaces */
            int index = 0;
            for (index = inputLine.length() - 1; index >= 0; index--) {
                if ((inputLine.charAt(index) != ' ') || (inputLine.charAt(index) != '\t')
                        || (inputLine.charAt(index) != '\n')) {
                    break;
                }
            }

            /* Process if it is a comment */
            if ((inputLine.length() >= 1) && (inputLine.charAt(0) == '#')) {
                continue;
            }
            
            Container application = arServerUser.getContainer(inputLine);
            List<Reference> references = application.getReferences();
            for(int i = 0; i < references.size(); i++)
            {
                if(references.get(i).getReferenceType() == ReferenceType.APPLICATION_FORMS)
                {
                    // next reference is a form, write it to the file
                    i++;
                    if (references.get(i).getReferenceType() == ReferenceType.SCHEMA)
                        schemaNames.add(references.get(i).getName());
                }
            }
            inputLine = currentInputFile.readLine();
        }
        JavaDriver.getThreadControlBlockPtr().closeCurrentInputFile();
    }

    boolean isUniqueImageObject(byte[] imageContent) {
        curImageChecksum = calculateChecksum(imageContent);
        if (imageMap.get(new Long(curImageChecksum)) == null) {
            totalDistinctImageSize += imageContent.length;
            return true;
        } else {
            memorySaving += imageContent.length;
            return false;
        }
    }

    void processNewImageObject(String imageName, byte[] imageContent) throws ARException {
        
        Image newImage = new Image(imageName);
        ImageData imageData = new ImageData(imageContent);
        newImage.setImageData(imageData);
        newImage.setType("jpg");
        newImage.setDescription("Extracted by Image Utility version 1.0");
        newImage.setOwner(arServerUser.getUser());
        arServerUser.createImage(newImage);
    }

    void processSchemaList() {
        String curSchema = null;
        for (int i = 0; i < schemaNames.size(); ++i) {
            curSchema = schemaNames.get(i);
            if (curSchema != null) {
                outputWriter.printHeader("", "Currently processing Form " + curSchema + "...", "\n");
                try {
                    /* Process all the views associated with the current schema */
                    processViews(curSchema);
                    /* Process all the fields associated with the current schema */
                    processFields(curSchema);
                } catch (ARException ex) {
                    outputWriter.printHeader("", "Error while processing schema " + curSchema + ".", "\n"); 
                    outputWriter.printHeader("", "Error message " + ex.getMessage() + ".", "\n");
                }
            }
        }
        return;
    }

    private void processViews(String schemaName) throws ARException {
        //Get all the views associated with the schema
        ViewCriteria criteria = new ViewCriteria();
        criteria.setPropertiesToRetrieve(ViewCriteria.PROPERTY_LIST);
        List<View> viewList = arServerUser.getListViewObjects(schemaName, (long) 0, criteria);
        if (viewList == null)
            return;
        for (int vuiIndex = 0; vuiIndex < viewList.size(); ++vuiIndex) {
            View currentView = viewList.get(vuiIndex);
            if (currentView == null)
                continue;
            PropertyMap dispProp = currentView.getDisplayProperties();
            if (dispProp == null)
                continue;
            String containerName = schemaName + "_" + currentView.getName();
            boolean propMapChanged = processPropertyMap(containerName, dispProp);
            if (propMapChanged == true) {
                currentView.setDisplayProperties((ViewDisplayPropertyMap)dispProp);
                arServerUser.setView(currentView);
            }
        }
    }
    
    private boolean processPropertyMap(String containerName, PropertyMap propMap) throws ARException {
        boolean didPropMapChange = false;
        for (Integer mapKey : propMap.keySet()) {
            Value curValue = propMap.get(mapKey.intValue());
            DataType type = curValue.getDataType();
            if (type == null)
                continue;
            if (type.toInt() == Constants.AR_DATA_TYPE_BYTES) {
                /* Found an Image */
                String imageName = null;
                ByteListValue byteListValue = (ByteListValue) curValue.getValue();
                byte[] imageContent = byteListValue.getValue();
                if (isUniqueImageObject(imageContent) == true) {
                    totalDistinctImages++;
                    /* Guarantee the uniqueness of image name */
                    imageName = containerName + "_" + curImageChecksum;
                    /* char ':' is not allowed in filenames. We should not use these characters in image names */
                    String finalImage = imageName.replace(':', '_');
                    processNewImageObject(finalImage, imageContent);
                    imageMap.put(new Long(curImageChecksum), finalImage);
                    imageName = finalImage; 
                } else {
                    /* get the image name from image List */
                    imageName = imageMap.get(new Long(curImageChecksum));
                }
                if (imageName != null) {
                    /* update the image content with image name */
                    Value newValue = new Value(imageName, DataType.CHAR);
                    propMap.put(mapKey, newValue);
                    didPropMapChange = true;
                    totalImages++;
                }
            }
        }
        return didPropMapChange;
    }

    private void processFields(String schemaName) throws ARException {
        //Get all the fields associated with the schema
        List<Field> fieldList = null;
        try {
            fieldList = arServerUser.getListFieldObjects(schemaName);
        } catch (java.lang.IllegalArgumentException ex) {
            System.out.println( "Error while processing fields on the form " + schemaName);
            return;
        }
        if (fieldList == null)
            return;
        String containerName = null;
        for (int fieldIndex = 0; fieldIndex < fieldList.size(); ++fieldIndex) {
            boolean propMapChanged = false;
            Field currentField = fieldList.get(fieldIndex);
            if (currentField == null)
                continue;
            containerName = schemaName + "_" + currentField.getName();
            DisplayInstanceMap diMap = currentField.getDisplayInstance();
            if (diMap == null)
                continue;
            PropertyMap diProp = null;
            for (Integer diMapKey : diMap.keySet()) {
                diProp = diMap.get(diMapKey.intValue());
                if (diProp == null)
                    continue;
                propMapChanged = processPropertyMap(containerName, diProp);
            }
            /* Update the display instance with new value for current Field*/
            if (propMapChanged == true) {
                currentField.setDisplayInstance(diMap);
                arServerUser.setField(currentField);
            }
        }
    }

    void verifyImages()
    {
        outputWriter.printHeader("", "\nVerifying the extracted image name references, Please wait...." , "\n");
      
        for (int i=0; i < schemaNames.size(); ++i) {
            try{
            outputWriter.printHeader("", "\nVerifying the image name references in schema : " + schemaNames.get(i) ,  "");
            //Get all the views associated with the schema
            ViewCriteria criteria = new ViewCriteria();
            criteria.setPropertiesToRetrieve(ViewCriteria.PROPERTY_LIST);
            List<View> viewList = arServerUser.getListViewObjects(schemaNames.get(i),
                                                                      (long) 0, 
                                                                      criteria);
            for (int vuiIndex=0; vuiIndex < viewList.size(); ++ vuiIndex) {
                View currentView = viewList.get(vuiIndex);
                ViewDisplayPropertyMap dispProp = currentView.getDisplayProperties();
                for (Integer j : dispProp.keySet()) {
                    int prop = j.intValue();
                    Value curValue = dispProp.get(prop);
                    if (prop == Constants.AR_DPROP_IMAGE ||
                        prop == Constants.AR_DPROP_PUSH_BUTTON_IMAGE ||
                        prop == Constants.AR_DPROP_DETAIL_PANE_IMAGE ||
                        prop == Constants.AR_DPROP_TITLE_BAR_ICON_IMAGE) { 
                        if (curValue.getDataType().toInt() == Constants.AR_DATA_TYPE_CHAR){
                            /* Found an Image name reference */
                            String imageName = curValue.getValue().toString();
                            if (isImagePresentInServer(imageName) == false)
                                outputWriter.printHeader("", "\nImage : " + imageName , " is associated with View : " + currentView.getName() + " couldn't not be verified");
                        }
                    }
                }
            }
            
            //Get all the fields associated with the schema
            List<Field> fieldList = arServerUser.getListFieldObjects(schemaNames.get(i));
            for (int fieldIndex=0; fieldIndex < fieldList.size(); ++ fieldIndex) {
                
                Field currentField = fieldList.get(fieldIndex);
                if (currentField == null)
                    continue;
                DisplayInstanceMap diMap = currentField.getDisplayInstance();
                if (diMap == null)
                    continue;
                PropertyMap diProp = null;
                for (Integer diMapKey : diMap.keySet()) {
                    diProp = diMap.get(diMapKey.intValue());
                    if (diProp == null)
                        continue;
                    for (Integer mapKey : diProp.keySet()) {
                        Value curValue = diProp.get(mapKey.intValue());
                        DataType type = curValue.getDataType();
                        if (type == null)
                            continue;
                        if (mapKey.intValue() == Constants.AR_DPROP_IMAGE ||
                            mapKey.intValue() == Constants.AR_DPROP_PUSH_BUTTON_IMAGE ||
                            mapKey.intValue() == Constants.AR_DPROP_DETAIL_PANE_IMAGE ||
                            mapKey.intValue() == Constants.AR_DPROP_TITLE_BAR_ICON_IMAGE) {         
                            if (curValue.getDataType().toInt() == Constants.AR_DATA_TYPE_CHAR){
                                /* Found an Image reference */
                                String imageName = curValue.getValue().toString();
                                if (isImagePresentInServer(imageName) == false)
                                    outputWriter.printHeader("", "\nImage : " + imageName , " is associated with Field : " + currentField.getName() + " couldn't not be verified");
                            }
                        }
                    }
                }
            }         
            } catch (Exception e){
                outputWriter.printString("\nError : " + e.toString() + "verifying schema " + schemaNames.get(i));
            }
        }
        outputWriter.printHeader("\n", "\nCompleted verification of extracted images." , "\n");
        
    }
   
    boolean isImagePresentInServer(String imageName) throws ARException {
        List <Image> imageList = null;
        List <String> imageNames = new ArrayList<String>();
        if (imageName.length() == 0)
            return false;
        imageNames.add(imageName);
        /* Get the image from server */
        ImageCriteria criteria = new ImageCriteria();
        criteria.setRetrieveAll(true);
        try {
            imageList = arServerUser.getListImageObjects(imageNames, criteria);
            if (imageList == null) {
                outputWriter.printHeader("\n", "\nCouldn't find image " + imageName + " in server." , "\n");
                return false;
            }
        } catch (ARException e) {
            outputWriter.printHeader("\n", "\nError" + e.toString() +" getting image " + imageName + " from server." , "\n");
            return false;
        }
        //process retrieved image, there will be only one in the list
        for (int i = 0; i < imageList.size(); ++i) {
            Image curImage = imageList.get(i);
            if (curImage.getImageData().getValue().length ==0){
                outputWriter.printHeader("\n", "\nImage " + imageName + " contains zero byte." , "\n");
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        ImageExtractor imageExtractor = null;
        boolean useApplication = true;
        try {  
            
            imageExtractor = new ImageExtractor();
            JavaDriver.getThreadControlBlockPtr().setPrimaryThread(true);
            JavaDriver.getThreadControlBlockPtr().setCurrentInputToStdIn();
            JavaDriver.getThreadControlBlockPtr().setOutputToStdOut();
            String serverName = InputReader.getString("Server Host:", "localhost");
            int serverPort = InputReader.getInt("Server Port:", 0);
            String userName = InputReader.getString("User Name:", "Demo");
            String password = InputReader.getString("Password:", "");
            String locale = null;
            InputReader.setNullPromptOption(true);
            String useAppStr = InputReader.getString("Would you like to use application names(Yes/No)?", "Yes");
            if (useAppStr.equals("Yes") || useAppStr.equals("yes"))
                useApplication = true;
            else
                useApplication = false;
                
            String fileName = null;
            if (useApplication == true)
                fileName = InputReader.getString("Input File containing the application names :", "");
            else
                fileName = InputReader.getString("Input File containing the form names :", "");
            imageExtractor.arServerUser = new ARServerUser(userName, password, locale, serverName, serverPort);
            imageExtractor.arServerUser.login();
            Calendar now = Calendar.getInstance();
            long startTime = now.getTimeInMillis();
            imageExtractor.populateImageList();
            imageExtractor.readSchemaNames(fileName, useApplication);
            if (useApplication == true)
                outputWriter.printHeader("", "Processing " + imageExtractor.getSchemaCount() + " forms belonging to specified application(s).", "\n");
            else
                outputWriter.printHeader("", "Processing " + imageExtractor.getSchemaCount() + " Forms.", "\n");
            imageExtractor.processSchemaList();    
            imageExtractor.verifyImages();
            now = Calendar.getInstance();
            long endTime = now.getTimeInMillis();
            long elapsedTime = (endTime - startTime)/1000;
            outputWriter.printHeader("", "\n" , "\n");
            outputWriter.printHeader("", "Current Run Report         \n" , "\n");
            outputWriter.printHeader("", "==========================================", "\n");
            outputWriter.printHeader("", "Total Images                 : " + imageExtractor.getTotalImageCount(), "\n");
            outputWriter.printHeader("", "Distinct Images              : " + imageExtractor.getDistinctImageCount(), "\n");
            //outputWriter.printHeader("", "Total Image size ( In bytes) : " + imageExtractor.getTotalImageSize(), "\n");
            outputWriter.printHeader("", "Total Saving     ( In bytes) : " + imageExtractor.getMemorySaving(), "\n");
            outputWriter.printHeader("", "Processing Completed in " + elapsedTime + " seconds.", "\n");
            outputWriter.printHeader("", "==========================================", "\n");
        } catch (Exception e) {
            outputWriter.printString("Error in executing the command\n");
            e.printStackTrace();
        } finally {
            try {
                imageExtractor.arServerUser.logout();
             
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 System.exit(0); 
             }
        }
    }
}

