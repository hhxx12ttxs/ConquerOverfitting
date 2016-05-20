package unimatica.FTPUtility;

import org.apache.commons.io.FileUtils;
import unimatica.FTPUtility.FtpDataTransfer.InvoiceQuadraturaFTP;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by oreste on 10/04/14.
 *
 * Classe che contiene i metodi per apporre la firma, criptazione, decriptazione e verifica firma ai file prodotti
 *
 */
public class FTPSign {

    private String signKeyPassword, privateKeyPassword, passwordSignXmlKey;
    private File publicKey, privateKey, signKey, verifySignKey, signXmlKey;


    public FTPSign(File publicKey, File privateKey, String privateKeyPassword, File signKey, String signKeyPassword, File verifySignKey, File signXmlKey, String passwordSignXmlKey) throws ClassNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.signKey = signKey;
        this.verifySignKey = verifySignKey;
        this.privateKeyPassword = privateKeyPassword;
        this.signKeyPassword = signKeyPassword;
        this.passwordSignXmlKey = passwordSignXmlKey;
        this.signXmlKey = signXmlKey;
    }

    public FTPSign(File publicKey, File privateKey, String privateKeyPassword, File signKey, String signKeyPassword, File verifySignKey) throws ClassNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.signKey = signKey;
        this.verifySignKey = verifySignKey;
        this.privateKeyPassword = privateKeyPassword;
        this.signKeyPassword = signKeyPassword;
    }

    /**
     *
     * return zip file if it is valid
     *
     * @param file
     * @return
     */
    private boolean isZipFile(File file) throws Exception {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            return true;
        } catch (Exception e) {

            if(zipFile!=null){
                zipFile.close();
                zipFile = null;
            }

            return false;
        }
    }

    /**
     *
     * return an array of bytes from a ArrayList of Bytes
     *
     * @param byteArrayList
     * @return
     */
    private byte[] toByteArray(ArrayList<Byte> byteArrayList){
        byte[] totlaArrByte = new byte[byteArrayList.size()];
        int index = 0;
        for(byte singleByte : byteArrayList){
            totlaArrByte[index++] = singleByte;
        }

        return totlaArrByte;
    }

    /**
     *
     * verify if the file sign matches against the one inside the signed file
     *
     * @param file
     * @return
     * @throws Exception
     */
    private boolean verifySignFile(File file) throws Exception {

        String fileName = file.getAbsolutePath();

        Process p = Runtime.getRuntime().exec("openssl smime -verify -in "+fileName+" -inform der -binary -out "+fileName.replace(".dec","")+" -CAfile "+verifySignKey.getAbsolutePath());

        p.waitFor();

        if(p.exitValue()==0){
            file.delete();
            copyRight(new File(fileName.replace(".dec","")),"DaSogei/noncriptati");
            return true;
        }

        return false;

    }

    /**
     *
     * returns a byte array from a ZipInputStream
     *
     * @param zipInputStream
     * @return
     * @throws Exception
     */
    private byte[] extractFileBytesInZip(ZipInputStream zipInputStream) throws Exception {
        int read;
        ArrayList<Byte> byteArrayList = new ArrayList<Byte>();

        while((read = zipInputStream.read())>0){
            byteArrayList.add((byte) read);
        }

        return toByteArray(byteArrayList);
    }

    /**
     *
     * extract a file zip inside another file zipa
     *
     * @param zipInputStream
     * @param name
     * @throws Exception
     */
    private void extractZipInsideZip(ZipInputStream zipInputStream, String name) throws Exception {
        BufferedInputStream is = new BufferedInputStream(zipInputStream);
        int currentByte;
        // establish buffer for writing file
        byte data[] = new byte[2048];

        // write the current file to disk
        FileOutputStream fos = new FileOutputStream(name);
        BufferedOutputStream dest = new BufferedOutputStream(fos,
                2048);

        // read and write until last byte is encountered
        while ((currentByte = is.read(data, 0, 2048)) != -1) {
            dest.write(data, 0, currentByte);
        }
        dest.flush();
        dest.close();
    }

    /**
     *
     * put a signing onto a file passed as an argument
     *
     * @param file
     * @return boolean
     */
    private boolean signFile(File file, boolean isXml) throws Exception {

        String signer = isXml ? signXmlKey.getAbsolutePath() : signKey.getAbsolutePath();
        String password = isXml ? passwordSignXmlKey : signKeyPassword;
        String fileName = file.getAbsolutePath();

        System.out.println("signer -> "+signer);
        System.out.println("password -> "+password);

        Process p = Runtime.getRuntime().exec("openssl smime -sign -in "+fileName+" -outform der -binary -nodetach -out "+fileName.concat(".sign")+" -signer "+signer+" -passin pass:"+password);

        p.waitFor();

        return p.exitValue()==0;
    }

    /**
     *
     * get the name of the xml which has the same name of the zip file
     *
     * @param file
     * @return
     * @throws Exception
     */
    public ZipEntry getSameNameZipEntryXML(File file) throws Exception{
        byte[] buff;
        FileWriter fileWriter;
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while(zipEntry!=null){
            if(!zipEntry.getName().equals(file.getName().replace(".zip",".xml"))){
                zipEntry = zipInputStream.getNextEntry();
            }else{
                break;
            }
        }

        if(zipEntry!=null){
            fileWriter = new FileWriter(new File(file.getPath().substring(0,file.getPath().lastIndexOf(File.separator))+"/"+zipEntry.getName()));
            buff = new byte[1024];

            while(zipInputStream.read(buff)>0){
                fileWriter.write(new String(buff,"UTF-8").trim());
            }

            fileWriter.close();
        }

        zipInputStream.close();
        //file.delete();
        return zipEntry;
    }

    /**
     *
     *
     * returns an array list of responses which could be parsed
     *
     * @param file
     * @param responsesArrayList
     * @return
     * @throws Exception
     */
    public ArrayList<FTPNotificationParser> parseResponsesZip(File file, ArrayList<FTPNotificationParser> responsesArrayList) throws Exception {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while(zipEntry!=null){
            if(zipEntry.getName().endsWith(".xml") && !zipEntry.getName().contains("FO")){
                responsesArrayList.add(new FTPNotificationParser(new String(extractFileBytesInZip(zipInputStream),"UTF-8").trim(),zipEntry.getName()));
            }else if(zipEntry.getName().endsWith(".zip")){
                extractZipInsideZip(zipInputStream,zipEntry.getName());
                parseResponsesZip(new File(zipEntry.getName()),responsesArrayList);
            }
            zipEntry = zipInputStream.getNextEntry();
        }

        zipInputStream.close();
        file.delete();

        return responsesArrayList;

    }

    /**
     *
     * makes a sign on an invoice file
     *
     * @param file
     * @return
     * @throws Exception
     */
    public boolean signXmlFile(File file) throws Exception {
        return signFile(file,true);
    }

    /**
     *
     * returns the number of files inside the zip
     *
     * @param file
     * @return
     * @throws Exception
     */
    public int getNumberOfFilesInZip(File file) throws Exception {
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        int count = 0;

        while(zipEntry!=null){
            zipEntry = zipInputStream.getNextEntry();
            count++;
        }

        zipInputStream.close();

        return count-1;
    }

    /**
     *
     * crypt a zip file with a .pem certificate
     *
     * @param invoiceFTP
     * @throws Exception
     * @return boolean
     */
    public boolean encrypt(InvoiceQuadraturaFTP invoiceFTP) throws Exception{
        File file = new File(invoiceFTP.getPath()+File.separator+invoiceFTP.getCompleteInvoiceName());

        return encrypt(file);

    }

    /**
     *
     * code to crypt a file with a .pem certificate
     *
     * @param file
     * @throws Exception
     * @return boolean
     */
    public boolean encrypt(File file) throws Exception{
        copyRight(file, "VersoSogei/noncriptati");
        if(signFile(file,false)){
            String fileName = file.getAbsolutePath();

            Process p = Runtime.getRuntime().exec("openssl smime -encrypt -in "+fileName.concat(".sign")+" -outform der -binary -des3 -out "+fileName+" "+publicKey.getAbsolutePath());

            p.waitFor();

            if(p.exitValue()==0){
                new File(fileName.concat(".sign")).delete();
                copyRight(file,"VersoSogei/criptati");
                return true;
            }

            return false;
        }

        return false;
    }

    /**
     *
     * decrypt a zip whit a .pem certificate
     *
     * @param invoiceFTP
     * @throws Exception
     * @return boolean
     */
    public boolean decrypt(InvoiceQuadraturaFTP invoiceFTP) throws Exception{
        File file = new File(invoiceFTP.getPath()+File.separator+invoiceFTP.getCompleteInvoiceName());

        return decrypt(file);
    }

    /**
     *
     * code to decrypt a file with a .pem certificate
     *
     * @param file
     * @throws Exception
     * @return boolean
     */
    public boolean decrypt(File file) throws Exception{

        copyRight(file,"DaSogei/criptati");

        String fileName = file.getAbsolutePath();

        Process p = Runtime.getRuntime().exec("openssl smime -decrypt -in "+fileName+" -inform der -binary -out "+fileName.concat(".dec")+" -recip "+privateKey.getAbsolutePath()+" -passin pass:"+privateKeyPassword);

        p.waitFor();

        return p.exitValue()==0 && verifySignFile(new File(fileName.concat(".dec")));
    }

    /**
     *
     * copy the files crypted and decrypted inside the properly dirs, it acts like a backup of what you downloaded or uploaded on Sogei
     *
     * @param file
     * @param lastPath
     * @throws IOException
     */
    private void copyRight(File file, String lastPath) throws IOException {

        FTPBackup ftpBackup = FTPBackup.readConfigFileBackup();
        if(ftpBackup!=null && ftpBackup.isActivated()){
            String path = ftpBackup.getPathBackup()==null ? "" : ftpBackup.getPathBackup();
            path = org.apache.commons.lang3.StringUtils.removeEnd(path,File.separator)+File.separator;

            File toSogeiCryptEI = new File(path+"SOGEI"+File.separator+"VersoSogei"+File.separator+"criptati"+File.separator+"EI");
            File toSogeiCryptFI = new File(path+"SOGEI"+File.separator+"VersoSogei"+File.separator+"criptati"+File.separator+"FI");
            File toSogeiNoCryptEI = new File(path+"SOGEI"+File.separator+"VersoSogei"+File.separator+"noncriptati"+File.separator+"EI");
            File toSogeiNoCryptFI = new File(path+"SOGEI"+File.separator+"VersoSogei"+File.separator+"noncriptati"+File.separator+"FI");
            File fromSogeiCryptEO = new File(path+"SOGEI"+File.separator+"DaSogei"+File.separator+"criptati"+File.separator+"EO");
            File fromSogeiCryptFO = new File(path+"SOGEI"+File.separator+"DaSogei"+File.separator+"criptati"+File.separator+"FO");
            File fromSogeiNoCryptFO = new File(path+"SOGEI"+File.separator+"DaSogei"+File.separator+"noncriptati"+File.separator+"FO");
            File fromSogeiNoCryptEO = new File(path+"SOGEI"+File.separator+"DaSogei"+File.separator+"noncriptati"+File.separator+"EO");


            if(!toSogeiCryptEI.exists()){
                toSogeiCryptEI.mkdirs();
            }
            if(!toSogeiCryptFI.exists()){
                toSogeiCryptFI.mkdirs();
            }
            if(!toSogeiNoCryptEI.exists()){
                toSogeiNoCryptEI.mkdirs();
            }
            if(!toSogeiNoCryptFI.exists()){
                toSogeiNoCryptFI.mkdirs();
            }
            if(!fromSogeiCryptEO.exists()){
                fromSogeiCryptEO.mkdirs();
            }
            if(!fromSogeiCryptFO.exists()){
                fromSogeiCryptFO.mkdirs();
            }
            if(!fromSogeiNoCryptEO.exists()){
                fromSogeiNoCryptEO.mkdirs();
            }
            if(!fromSogeiNoCryptFO.exists()){
                fromSogeiNoCryptFO.mkdirs();
            }

            String folderKind = file.getName().startsWith("FO") ? "FO" : file.getName().startsWith("EO") ? "EO" : file.getName().startsWith("FI") ? "FI" : "EI";

            FileUtils.copyFile(file, new File(path+"SOGEI"+File.separator + lastPath + File.separator + folderKind + File.separator + file.getName()));
        }

    }

}

