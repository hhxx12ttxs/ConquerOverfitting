package com.mplify.crypto_impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mplify.checkers._check;
import com.mplify.crypto.KeyManager;
import com.mplify.crypto.KeyManagerConstructionException;
import com.mplify.crypto.SymmetricKey;
import com.mplify.crypto_impl.SymmetricKeyImpl.KEY_SIZE;
import com.mplify.helpers.ByteCoding;
import com.mplify.properties.PropertiesReader;
import com.mplify.properties.ReferencedProperties;
import com.mplify.properties.ReferencedStuff;
import com.mplify.properties.ReferencedStuffAsNeeded;
import com.mplify.resources.ResourceHelpers;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2011, M-PLIFY S.A.
 *                     21, rue Glesener
 *                     L-1631 LUXEMBOURG
 *
 * All rights reserved.
 *******************************************************************************
 *******************************************************************************
 * Implementation of a "Key Manager" which has a factory call to obtain
 * key material from a configurable file or resource.
 * 
 * 2011.06.16 - Created
 * 2011.12.29 - Added getKey(Integer) in addition to getKey(int)
 * 2012.12.28 - Throwing Error after _check.cannotHappen() to keep compiler 
 *              happy 
 ******************************************************************************/

public class KeyManagerImpl implements KeyManager {

    private final static String CLASS = KeyManagerImpl.class.getName();
    private final static Logger LOGGER_makeKeyManagerBasedOnStoredKeys = LoggerFactory.getLogger(CLASS + ".makeKeyManagerBasedOnStoredKeys");
    private final static Logger LOGGER_fromProperties_KeyManager = LoggerFactory.getLogger(CLASS + ".fromProperties_KeyManager");

    private final static Pattern commentPattern = Pattern.compile("^\\s*#");
    private final static Pattern key128BitPattern = Pattern.compile("^\\s*(\\d+)\\s*:\\s*([0-9A-Fa-f]{32})\\s*($|#)");
    private final static Pattern hashPattern = Pattern.compile("^\\s*(HASH|hash)\\s*:\\s*([0-9A-Fa-f]+)\\s*($|#)");

    private final Map<Integer, SymmetricKey> keyMaterial; // immutable
    private final byte[] keyMaterialHash; // SHA-1 hash computed over the (index, key) tuples
    private final ArrayList<Integer> allowed; // set of allowed indexes, not necessarily continguous
    private final Random rand = new Random(); // used in obtaining random indexes
    
    /**
     * Constructor takes map of keys, which is deep-copied
     */

    public KeyManagerImpl(Map<Integer, SymmetricKey> keyMaterial) {
        _check.notNull(keyMaterial,"key material map");
        this.keyMaterial = Collections.unmodifiableMap(new HashMap<Integer, SymmetricKey>(keyMaterial));
        this.keyMaterialHash = computeHashKeyOverMaterial();
        this.allowed = new ArrayList<Integer>(keyMaterial.keySet());
    }
    
    /**
     * Factory call: build a "KeyManager" based on nothing but the size of the key material array.
     * New keys will be generated.
     */

    @SuppressWarnings("boxing")
    public static KeyManager makeKeyManagerBasedOnNewKeys(int arraySize) throws NoSuchAlgorithmException {
        _check.isTrue(arraySize > 0, "The array size must be larger than 0 but is %s", arraySize);
        KeyGenerator keyGen = SymmetricKeyImpl.createAESKeyGenerator(KEY_SIZE.KEY_128);
        Map<Integer, SymmetricKey> keyMaterial = new HashMap<Integer, SymmetricKey>();
        for (int i = 0; i < arraySize; i++) {
            SecretKeySpec sks = SymmetricKeyImpl.newKey(keyGen);
            keyMaterial.put(i, new SymmetricKeyImpl(sks));
        }
        return new KeyManagerImpl(keyMaterial);
    }

    /**
     * Dump call: write the contents of "KeyManager" to a file, including the hash over the key material.
     * Throws on any problem. Callers should re-read to make double sure.
     */

    public void writeToFile(File file) throws IOException {
        _check.notNull(file,"file");
        _check.isFalse(file.exists(), "The file '%s' already exists - won't overwrite", file);
        Writer w = new OutputStreamWriter(new FileOutputStream(file));
        try {
            List<Integer> indexes = new ArrayList<Integer>(keyMaterial.keySet());
            Collections.sort(indexes);
            for (Integer x : indexes) {
                SymmetricKeyImpl sksi = (SymmetricKeyImpl) keyMaterial.get(x);
                String raw = ByteCoding.toHexString(sksi.getEncoded());
                w.write(x + ":" + raw + "\n");
            }
            w.write("HASH:" + ByteCoding.toHexString(keyMaterialHash) + "\n");
        } finally {
            w.close();
        }
    }

    /**
     * Factory call: build a "KeyManager" based on a reference to a resource or file.
     * KeyManagerConstructionException is raised if there is any problem with the key material,
     * it will have the actual problem's Exception as cause.
     * The file or resource:
     * 1) Must not be empty
     * 2) Should but need not have a continguous set of indexes
     * 3) Should but need not have a hash (only one is allowed) against which a hash created from the read-in keys can be checked
     */

    public static KeyManager makeKeyManagerBasedOnStoredKeys(ReferencedStuff refStuff) throws KeyManagerConstructionException {
        Logger logger = LOGGER_makeKeyManagerBasedOnStoredKeys;
        _check.notNull(refStuff,"referenced stuff");
        try {
            //
            // Fill this map and possibly the hash, too
            //
            Map<Integer, SymmetricKey> map = new HashMap<Integer, SymmetricKey>();
            byte[] hash = null;
            //
            // Get the stuff from disk or wherever
            //
            String text;
            String provenance;
            if (refStuff.file != null) {
                text = ResourceHelpers.slurpFile(refStuff.file, "UTF-8");
                provenance = "file '" + refStuff.file.getAbsolutePath() + "'";
            } else {
                assert refStuff.resource != null;
                text = ResourceHelpers.slurpResource(refStuff.resource, "UTF-8");
                provenance = "resource '" + refStuff.resource + "'";
            }
            //
            // Analyze line by line
            //
            LineNumberReader lnr = new LineNumberReader(new StringReader(text));
            try {
                String line;
                while ((line = lnr.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        // skip empty line
                        continue;
                    }
                    {
                        Matcher m = key128BitPattern.matcher(line);
                        if (m.matches()) {
                            Integer keyIndex = new Integer(m.group(1));
                            byte[] rawKey = ByteCoding.toByteArray(m.group(2));
                            _check.isTrue(!map.containsKey(keyIndex), "Clash on key index %s at line %s in %s", keyIndex, new Integer(lnr.getLineNumber()), provenance);
                            SecretKeySpec sks = SymmetricKeyImpl.newAES128Key(rawKey);
                            map.put(keyIndex, new SymmetricKeyImpl(sks));
                            continue;
                        }
                    }
                    {
                        Matcher m = commentPattern.matcher(line);
                        if (m.matches()) {
                            continue; // skip
                        }
                    }
                    {
                        Matcher m = hashPattern.matcher(line);
                        if (m.matches()) {
                            // One could consider hashing the keys read up to now...
                            _check.isTrue(hash == null, "Hash already set but encountered another one at line %s in %s", new Integer(lnr.getLineNumber()), provenance);
                            hash = ByteCoding.toByteArray(m.group(2));
                            continue;
                        }
                    }
                    _check.isTrue(false, "Unmatched line '%s' at line %s in %s", line, new Integer(lnr.getLineNumber()), provenance); // never returns
                }
            } finally {
                lnr.close();
            }
            //
            // We want to find at least one key!
            //
            _check.isFalse(map.isEmpty(), "There is no key material at all in %s", provenance);
            //
            // The map is now sucked into a "KeyManager". This also computes the hash
            //
            KeyManagerImpl keyManager = new KeyManagerImpl(map);
            //
            // If possible, compare hashes
            //
            if (hash != null) {
                byte[] actualHash = keyManager.getKeyMaterialHash();
                boolean hashCmp = ByteCoding.compareByteArrays(hash, actualHash);
                if (!hashCmp) {
                    _check.fail("The hash from %s (%s) and the hash computed over the key material (%s) don't match", provenance, ByteCoding.toHexString(hash), ByteCoding.toHexString(actualHash));
                }
                logger.info("Key material from " + provenance + " read and hash correspondance checked. Looking good!");
            } else {
                logger.warn("No hash given in " + provenance + " -- no guarantee as to key material!");
            }
            //
            // If we are here, we have won!!
            //
            return keyManager;
        } catch (Exception exe) {
            throw new KeyManagerConstructionException(exe);
        }
    }

    /**
     * Compute a hash value of the stored keys
     */

    @SuppressWarnings("boxing")
    private byte[] computeHashKeyOverMaterial() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (Exception exe) {
            _check.cannotHappen(exe);
            throw new Error(_check.NEVER_GET_HERE_BUT_NEEDED_FOR_KEEPING_COMPILER_HAPPY);
        }
        assert md != null;
        List<Integer> indexes = new ArrayList<Integer>(keyMaterial.keySet());
        Collections.sort(indexes);
        for (Integer x : indexes) {
            SymmetricKeyImpl sksi = (SymmetricKeyImpl) keyMaterial.get(x);
            md.update((byte) (x & 0xFF));
            md.update((byte) (x >> 8 & 0xFF));
            md.update((byte) (x >> 16 & 0xFF));
            md.update((byte) (x >> 24 & 0xFF));
            md.update(sksi.getEncoded());
        }
        return md.digest();
    }

    /**
     * Ask for a the Symmetric Key at index "keyIndex". Throws "CheckFailedException" if not exists.
     */

    @Override
    public SymmetricKey getKey(int keyIndex) {
        return getKey(Integer.valueOf(keyIndex));
    }

    /**
     * Ask for a the Symmetric Key at index "keyIndex". Throws "CheckFailedException" if not exists.
     */

    @Override
    public SymmetricKey getKey(Integer keyIndex) {
        _check.notNull(keyIndex,"key index");
        SymmetricKey res = this.keyMaterial.get(keyIndex);
        if (res == null) {
            _check.fail("There is no key with index " + keyIndex);
            throw new Error(_check.NEVER_GET_HERE_BUT_NEEDED_FOR_KEEPING_COMPILER_HAPPY);
        } else {
            return res;
        }        
    }

    /**
     * Ask for the hash over the key material. Returns a private, clones array to be used by the caller. 
     */

    public byte[] getKeyMaterialHash() {
        return this.keyMaterialHash.clone();
    }

    /**
     * Helper to get from props. Returns either null if no key manager is needed or a non-null
     * key manager with presumably good key material. May throw a certain number of exceptions 
     * due to problems encountered while building the KeyManager instance.
     */

    public static KeyManager fromProperties_KeyManager(PropertiesReader properties, String pname) throws KeyManagerConstructionException {
        Logger logger = LOGGER_fromProperties_KeyManager;
        ReferencedStuffAsNeeded rsan = ReferencedProperties.extractReferencedStuffAsNeeded(properties, pname);
        if (!rsan.use) {
            logger.info("Key manager not needed! Set property " + pname + " if you do need it!");
            return null; // don't use!
        }
        else {
            if (rsan.stuff == null) {
                _check.fail("Key manager is apparently needed according to property '%s', but no resource or file is indicated! Use ':file=' or ':resource=' appendage", pname);
            }
            assert rsan.stuff != null;
            //
            //  Building the KeyManager may raise a number of exception...
            //
            return KeyManagerImpl.makeKeyManagerBasedOnStoredKeys(rsan.stuff);
        }
    }

    /**
     * Get a random index of the allowed indexes
     */
    
    @Override
    public Integer randomKeyIndex() {
        synchronized (rand) { 
            return allowed.get(rand.nextInt(this.allowed.size()));
        }
    }

    /**
     * Check that key exists
     */

    @Override
    public boolean isAcceptableKeyIndex(int x) {
        return keyMaterial.containsKey(Integer.valueOf(x));
    }

}

