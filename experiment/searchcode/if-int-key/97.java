package wsl.licence;

/**
 * Title:        Mobile Data Now
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      WAP Solutions Ltd
 * @author Paul Lupton
 * @version 1.0
 */

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

public class Store
{
    private static final String LICENCE_KEY_ALIAS = "mdnlicencekey";
    private static final String ACTIVATION_KEY_ALIAS = "mdnactivationkey";
    private static final char[] PASSWORD = "nobody'sbusiness".toCharArray();
    private boolean _loadOK = false;
    private File _file;
    private Hashtable _keys = new Hashtable(89);
    private Hashtable _licences = new Hashtable(89);
    private String _registeredEmailAddress = null;
    private Boolean _publicGroup = null;
    private int _availablePublicMessages = 0;
    private int _installationReferenceNumber = 0;
    private static final int NUMBER_ADJUSTMENT = 983456764;
    private static final int AVAILABLE_MESSAGES_LENGTH = 9;
    private static final int INSTALLATION_REFERENCE_NUMBER_LENGTH = 4;

    public class InvalidStoreException extends RuntimeException
    {
        public InvalidStoreException(String message)
        {
            super(message);
        }
    }

    public class InvalidKeyException extends RuntimeException
    {
        public InvalidKeyException(String message)
        {
            super(message);
        }
    }

    public Store() throws Exception
    {
    }

    /**
     * Load the keystore from a file.  File must exist.
     */
    public void load() throws Exception
    {
        try
        {
            if (_file == null || !_file.exists())
                create();

            BufferedReader reader = new BufferedReader(new FileReader(_file));
            String line = null;

            while (null != (line = reader.readLine()))
            {
                if (line == null || line.length() < LicenceKey.ENCODED_LENGTH)
                   throw new InvalidKeyException("Licence key does not exist.");

                String l = line.substring(0, LicenceKey.ENCODED_LENGTH);
                LicenceKey licence = new LicenceKey(l);
                setLicenceKey(licence);

                ActivationKey activation = null;
                int keyLength = LicenceKey.ENCODED_LENGTH + ActivationKey.ENCODED_LENGTH;
                if (line.length() >= keyLength)
                {
                    String a = line.substring(LicenceKey.ENCODED_LENGTH, keyLength);
                    activation = new ActivationKey(a, l);
                    setActivationKey(licence, activation);
                    
                    int wholeLength = line.length();
                    if (wholeLength > keyLength){
                    	String strPublicGroup = line.substring(keyLength, keyLength + 1);
                    	if (strPublicGroup.equalsIgnoreCase("1"))
                    		_publicGroup = new Boolean("true");
                    	else
                    		_publicGroup = new Boolean("false");
                    	
                    	String strInstallationRef = line.substring(keyLength + 1, keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH);
                    	_installationReferenceNumber = getUnadjustInstallationReferenceNumber(strInstallationRef);
                    	
                    	if (line.length() > keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH + AVAILABLE_MESSAGES_LENGTH){
                        	String strAvailableMessages = line.substring(keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH, keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH + AVAILABLE_MESSAGES_LENGTH);
                        	_availablePublicMessages = getUnadjustPublicMessages(strAvailableMessages);                    		
                    	}

                    	if (line.length() > keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH + AVAILABLE_MESSAGES_LENGTH){
                    		_registeredEmailAddress = line.substring(keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH + AVAILABLE_MESSAGES_LENGTH);
                    	}else{
                    		_registeredEmailAddress = line.substring(keyLength + 1 + INSTALLATION_REFERENCE_NUMBER_LENGTH);
                    	}
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new InvalidStoreException("Unable to load store \n" + e.getMessage());
        }
    }
    
    

    /**
     * Store the keystore to a file.
     */
    public void store() throws Exception
    {
        Enumeration elementEnum = _licences.elements();

        if (_file == null)
            create();

        FileWriter writer = new FileWriter(_file);

        while (elementEnum.hasMoreElements())
        {
            LicenceKey l = (LicenceKey)elementEnum.nextElement();
            ActivationKey a = (ActivationKey)_keys.get(l);

            writer.write(l.toString());
            if (a != null)
               writer.write(a.toString());
            
            //This should not be null
            if (_publicGroup.equals(new Boolean("true")))
            	writer.write("1");
            else
            	writer.write("0");
            
            String strInstallationRef = getAdjustInstallationReferenceNumber(_installationReferenceNumber);
            writer.write(strInstallationRef);
            
            String availableMessages = getAdjustPublicMessages(getAvailablePublicMessages());
            writer.write(availableMessages);
            
            if (_registeredEmailAddress != null){
            	writer.write(_registeredEmailAddress);
            }
        }

        writer.close();
    }

    /**
     * Creates a new keystore file.
     */
    public void create() throws Exception
    {
        if (_file != null && _file.exists())
           _file.delete();

        _file = new File(getFileNamePath());
        _file.createNewFile();
    }

    /**
     * Returns the licence key that matches the product code.
     * Can be null if the key is invalid, or the store hasn't been loaded.
     */
    public LicenceKey getLicenceKey(int productCode)
    {
        LicenceKey l = (LicenceKey)_licences.get(new Integer(productCode));
        return l;
    }

    /**
     * Add/update a licence key to the store.
     * Throws an InvalidKeyException if null is passed or the key is invalid (not verified)
     */
    public void setLicenceKey(LicenceKey key) throws InvalidKeyException
    {
        if (key == null)
           throw new InvalidKeyException("Licence key cannot be set to null");

        if (!key.verifyChecksum())
           throw new InvalidKeyException("Licence key checksum does not verify");

        _licences.put(new Integer(key.getProductCode()), key);
    }

    /**
     * Returns the activation key associated with the LicenceKey.
     * Can be null if the key has not been created, is invalid, or the store hasn't been loaded.
     */
    public ActivationKey getActivationKey(LicenceKey lKey)
    {
        return (ActivationKey)_keys.get(lKey);
    }

    /**
     * Add/update an activation key tot the store.
     * Throws an InvalidKeyException if null is passed or the key is invalid (not verified)
     */
    public void setActivationKey(LicenceKey lKey, ActivationKey aKey) throws InvalidKeyException
    {
        if (lKey == null)
           throw new InvalidKeyException("Licence key cannot be set to null");

        if (aKey == null)
           throw new InvalidKeyException("Activation key cannot be set to null");

        if (!aKey.verifyChecksum())
           throw new InvalidKeyException("Activation key checksum does not verify");

        setLicenceKey(lKey);
        _keys.put(lKey, aKey);
    }

    public void setRegisteredEmailAddress(String registeredEmailAddress) throws InvalidKeyException
    {
        if (registeredEmailAddress == null)
           throw new InvalidKeyException("Registered Email Address cannot be set to null");

        _registeredEmailAddress = registeredEmailAddress;
    }    
    public String getRegisteredEmailAddress() throws InvalidKeyException
    {
        return _registeredEmailAddress;
    }     
    /**
     * Returns an enumeration of all of the licence keys in the store
     */
    public Enumeration getLicenceKeys()
    {
        return _licences.keys();
    }

    /**
     * Returns the filename and path for the store
     */
    protected  String getFileNamePath()
    {
        String sep = System.getProperty("file.separator", "/");
        String path = System.getProperty("user.home", ".");

        String filename = path + sep + "sec.dat";

        return filename;
    }

	public Boolean getPublicGroup() {
		return _publicGroup;
	}

	public void setPublicGroup(Boolean group) {
		_publicGroup = group;
	}

	public int getAvailablePublicMessages() {
		return _availablePublicMessages;
	}

	public void setAvailablePublicMessages(int publicMessages) {
		_availablePublicMessages = publicMessages;
	}
	private String getAdjustPublicMessages(int intMessages){
		String adjustNumber = null;
		
		if (intMessages < 0 || intMessages > NUMBER_ADJUSTMENT){
			adjustNumber = String.valueOf(NUMBER_ADJUSTMENT); 
		}else{
			int minusNum = NUMBER_ADJUSTMENT - intMessages;
			String firstAdjust = String.valueOf(minusNum);
			int len = firstAdjust.length();
			int addedZeroNum = AVAILABLE_MESSAGES_LENGTH - len;
			for (int i = 0; i < addedZeroNum; i++){
				firstAdjust = "0" + firstAdjust;
			}
			adjustNumber = firstAdjust;
		}
		
		return adjustNumber;
	}
	
	private int getUnadjustPublicMessages (String strMessages){
		int availableMessages = 0;
		String strAdjustNum = strMessages;
		if (!strMessages.isEmpty()){
			int i = 0;
			while ( i < strMessages.length()){
				char charAtI = strMessages.charAt(i);
				String strAtI = String.valueOf(charAtI);
				if (strAtI.equals("0")){
					strAdjustNum = strMessages.substring(i+1);
					i++;
				}else{
					break;
				}
			}
			
		}
		
		int adjustNum = NUMBER_ADJUSTMENT;
		try {
			adjustNum = Integer.parseInt(strAdjustNum);
		} catch (NumberFormatException e) {
			adjustNum = NUMBER_ADJUSTMENT;
		}
		availableMessages = NUMBER_ADJUSTMENT - adjustNum;
		
		return availableMessages;
	}

	public int getInstallationReferenceNumber() {
		return _installationReferenceNumber;
	}

	public void setInstallationReferenceNumber(int referenceNumber) {
		_installationReferenceNumber = referenceNumber;
	}
	private String getAdjustInstallationReferenceNumber(int referenceNumber){
		String adjustNumber = null;
		
		if (referenceNumber < 0 || referenceNumber > NUMBER_ADJUSTMENT){
			adjustNumber = "0000"; 
		}else{
			String firstAdjust = String.valueOf(referenceNumber);
			int len = firstAdjust.length();
			int addedZeroNum = INSTALLATION_REFERENCE_NUMBER_LENGTH - len;
			for (int i = 0; i < addedZeroNum; i++){
				firstAdjust = "0" + firstAdjust;
			}
			adjustNumber = firstAdjust;
		}
		
		return adjustNumber;
	}
	private int getUnadjustInstallationReferenceNumber (String strReferenceNumber){
		String strAdjustNum = strReferenceNumber;
		if (!strReferenceNumber.isEmpty()){
			int i = 0;
			while ( i < strReferenceNumber.length()){
				char charAtI = strReferenceNumber.charAt(i);
				String strAtI = String.valueOf(charAtI);
				if (strAtI.equals("0")){
					strAdjustNum = strReferenceNumber.substring(i+1);
					i++;
				}else{
					break;
				}
			}
			
		}
		
		int adjustNum = 0;
		try {
			adjustNum = Integer.parseInt(strAdjustNum);
		} catch (NumberFormatException e) {
			adjustNum = 0;
		}
		return adjustNum;
	}	
}

