/* $Id: PGPKeyRingKeyStore.java,v 1.2 2005/03/13 17:46:38 woudt Exp $
 *
 * Copyright (C) 1999-2005 The Cryptix Foundation Limited.
 * All rights reserved.
 * 
 * Use, modification, copying and distribution of this software is subject 
 * the terms and conditions of the Cryptix General Licence. You should have 
 * received a copy of the Cryptix General License along with this library; 
 * if not, you can download a copy from http://www.cryptix.org/ .
 */

package cryptix.openpgp.provider;


import cryptix.openpgp.PGPCertificate;
import cryptix.openpgp.PGPKeyBundle;
import cryptix.openpgp.PGPPrivateKey;
import cryptix.openpgp.PGPPublicKey;

import cryptix.pki.ExtendedKeyStoreSpi;
import cryptix.pki.KeyBundle;
import cryptix.pki.KeyBundleException;
import cryptix.pki.KeyID;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;


/**
 * KeyStore implementation of a keyring.
 *
 * @author  Edwin Woudt <edwin@cryptix.org>
 * @author  Ingo Luetkebohle
 * @version $Revision: 1.2 $
 */
public class PGPKeyRingKeyStore extends ExtendedKeyStoreSpi {


// Instance vars
// ..........................................................................


    private Vector bundles;
    private HashMap bundleAliases;
    private boolean loaded = false;


// Constants
// ..........................................................................


    private static final char[] hexDigits = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };


// Basic methods
// ..........................................................................


    /**
     * Returns a PGPPrivateKey object for the KeyBundle identified by the given 
     * alias.
     */
    public Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        if (! bundleAliases.containsKey(alias)) return null;
    
        PGPKeyBundle bundle = (PGPKeyBundle)bundleAliases.get(alias);
        PGPPublicKey mainkey = (PGPPublicKey)bundle.getPublicKeys().next();
        
        return bundle.getPrivateKey(mainkey, password);
        
    }
    
    
    /**
     * A certificate chain for a KeyBundle is defined here as the combination
     * of all certificates on that KeyBundle.
     *
     * This definition is only relevant for this class. Other classes (e.g.
     * the java.secuyrity.cert.CertPath and related classes) will have 
     * different definitions.
     */
    public Certificate[] engineGetCertificateChain(String alias)
    {
        if (! bundleAliases.containsKey(alias)) return null;
    
        PGPKeyBundle bundle = (PGPKeyBundle)bundleAliases.get(alias);

        Iterator it = bundle.getCertificates();
        int size = 0;
        while (it.hasNext())  size++;

        Certificate[] result = new Certificate[size];

        it = bundle.getCertificates();
        int i=0;
        while (it.hasNext())  result[i++] = (Certificate)it.next();

        return result;
    }
    
    
    /**
     * We do not support returning individual certificates. Always returns null.
     */
    public Certificate engineGetCertificate(String alias)
    {
        return null;
    }
    
    
    /**
     * Unsupported yet
     */
    public Date engineGetCreationDate(String alias)
    {
        throw new RuntimeException("NYI");
    }

    
    /**
     * Aliases are not supported. We calculate on ourselves (based on the keyID)
     * so a 'null' value is expected for the alias. We do accept (i.e. not
     * throw and Exception) if the given alias is the same as the one we
     * calculated, but we do throw an Exception if the alias is incorrect.
     */
    public void engineSetKeyEntry(String alias, Key key, char[] password,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        try {
        
            if (! (key instanceof PGPPrivateKey)) {
                throw new KeyStoreException("PGPPrivateKey expected.");
            }

            PGPKeyBundleImpl bundle = new PGPKeyBundleImpl();
            for (int i=0; i<chain.length; i++) {
                bundle.addCertificate(chain[i]);
            }
            bundle.addPrivateKey((PGPPrivateKey)key, chain[0].getPublicKey(), 
                                 password, new SecureRandom());
        
            KeyID id = PGPKeyIDFactory.convert(key);
            byte[] keyid = id.getBytes(4);
        
            char[] buf = new char[10];
            buf[0] = '0';
            buf[1] = 'x';
            buf[2] = hexDigits[(keyid[0] >>> 4) & 0x0F];
            buf[3] = hexDigits[ keyid[0]        & 0x0F];
            buf[4] = hexDigits[(keyid[1] >>> 4) & 0x0F];
            buf[5] = hexDigits[ keyid[1]        & 0x0F];
            buf[6] = hexDigits[(keyid[2] >>> 4) & 0x0F];
            buf[7] = hexDigits[ keyid[2]        & 0x0F];
            buf[8] = hexDigits[(keyid[3] >>> 4) & 0x0F];
            buf[9] = hexDigits[ keyid[3]        & 0x0F];
            String newalias = new String(buf);
            
            if ((alias != null) && (! newalias.equals(alias))) {
                throw new KeyStoreException(
                    "This keystore does not support storing user-supplied "+
                    "aliases. Use null as the alias and let the keystore "+
                    "think of an alias.");
            }
            
            bundles.add(bundle);
            bundleAliases.put(newalias, bundle);

        } catch (InvalidKeyException ike) {
            throw new KeyStoreException(""+ike);
        } catch (KeyBundleException kbe) {
            throw new KeyStoreException(""+kbe);
        }
    }

    
    /**
     * Not supported yet
     */
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
        throws KeyStoreException
    {
        throw new RuntimeException("NYI");
    }

    
    /**
     * Aliases are not supported. In fact, even though the certificate will
     * be added, there is not way to get the individual certificate back,
     * except trough the KeyBundle or the getCertificateChain method.
     */
    public void engineSetCertificateEntry(String alias, Certificate cert)
        throws KeyStoreException
    {
        try {

            if (! (cert instanceof PGPCertificate)) {
                throw new KeyStoreException(
                    "Supplied certificate is not a PGPCertificate.");
            }
            if (alias != null) {
                throw new KeyStoreException(
                    "This keystore does not support storing user-supplied "+
                    "aliases. Use null as the alias and let the keystore "+
                    "think of an alias.");
            }
            
            KeyID id = PGPKeyIDFactory.convert(cert.getPublicKey());
            byte[] keyid = id.getBytes(4);
        
            char[] buf = new char[10];
            buf[0] = '0';
            buf[1] = 'x';
            buf[2] = hexDigits[(keyid[0] >>> 4) & 0x0F];
            buf[3] = hexDigits[ keyid[0]        & 0x0F];
            buf[4] = hexDigits[(keyid[1] >>> 4) & 0x0F];
            buf[5] = hexDigits[ keyid[1]        & 0x0F];
            buf[6] = hexDigits[(keyid[2] >>> 4) & 0x0F];
            buf[7] = hexDigits[ keyid[2]        & 0x0F];
            buf[8] = hexDigits[(keyid[3] >>> 4) & 0x0F];
            buf[9] = hexDigits[ keyid[3]        & 0x0F];
            String keyalias = new String(buf);
            
            PGPKeyBundleImpl bundle = 
                (PGPKeyBundleImpl)bundleAliases.get(keyalias);

            if (bundle == null) {
                bundle = new PGPKeyBundleImpl();
                bundles.add(bundle);
                bundleAliases.put(keyalias, bundle);
            }
            
            bundle.addCertificate(cert);

        } catch (InvalidKeyException ike) {
            throw new KeyStoreException(""+ike);
        } catch (KeyBundleException kbe) {
            throw new KeyStoreException(""+kbe);
        }
    }

    
    public void engineDeleteEntry(String alias)
        throws KeyStoreException    
    {
        Object x = bundleAliases.remove(alias);
        if (x != null) bundles.remove(x);
    }

    
    public Enumeration engineAliases()
    {
        return new IteratorEnumeration(bundleAliases.keySet().iterator());
    }


    public boolean engineContainsAlias(String alias)
    {
        return bundleAliases.containsKey(alias);
    }
    

    public int engineSize()
    {
        return bundles.size();
    }

    
    public boolean engineIsKeyEntry(String alias)    
    {
        if (bundleAliases.containsKey(alias)) {
            PGPKeyBundleImpl bundle = 
                (PGPKeyBundleImpl)bundleAliases.get(alias);
            return bundle.containsPrivateKey();
        } else {
            return false;
        }
    }

    
    /**
     * Individual certificate entries are not supported. Always returns false;
     */
    public boolean engineIsCertificateEntry(String alias)
    {
        return false;
    }

    
    /**
     * Individual certificate entries are not supported. Always returns null;
     */
    public String engineGetCertificateAlias(Certificate cert)
    {
        return null;
    }

    
    /**
     * Password based integrity protection is not supported.
     */
    public void engineStore(OutputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        if (password != null) {
            throw new NoSuchAlgorithmException(
                "Password based integrity protection is not supported.");
        }

        try {
        
            Iterator it = bundles.iterator();
            while (it.hasNext()) {
                stream.write(((PGPKeyBundle)it.next()).getEncoded());
            }
            
        } catch (KeyBundleException kbe) {
            throw new CertificateException(
                "Error while writing keyring - "+kbe);
        }
    }

    
    /**
     * Password based integrity protection is not supported.
     */
    public void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        if (password != null) {
            throw new NoSuchAlgorithmException(
                "Password based integrity protection is not supported.");
        }

        try {
        
            if (stream == null) {
                // create empty store
                bundles = new Vector();
                bundleAliases = new HashMap();
            } else {

                bundles = (Vector)PGPKeyBundleFactory.helper(stream, false);
                bundleAliases = new HashMap();

                Iterator it = bundles.iterator();
                while (it.hasNext()) {

                    PGPKeyBundle bundle = (PGPKeyBundle)it.next();

                    KeyID id =
                    PGPKeyIDFactory.convert((Key)bundle.getPublicKeys().next());

                    byte[] keyid = id.getBytes(4);

                    char[] buf = new char[10];
                    buf[0] = '0';
                    buf[1] = 'x';
                    buf[2] = hexDigits[(keyid[0] >>> 4) & 0x0F];
                    buf[3] = hexDigits[ keyid[0]        & 0x0F];
                    buf[4] = hexDigits[(keyid[1] >>> 4) & 0x0F];
                    buf[5] = hexDigits[ keyid[1]        & 0x0F];
                    buf[6] = hexDigits[(keyid[2] >>> 4) & 0x0F];
                    buf[7] = hexDigits[ keyid[2]        & 0x0F];
                    buf[8] = hexDigits[(keyid[3] >>> 4) & 0x0F];
                    buf[9] = hexDigits[ keyid[3]        & 0x0F];
                    String keyalias = new String(buf);

                    bundleAliases.put(keyalias, bundle);

                }
            }

        } catch (KeyBundleException kbe) {
            throw new CertificateException(
                "Error while reading keyring - "+kbe);
        } catch (InvalidKeyException ike) {
            throw new CertificateException(
                "Error while reading keyring - "+ike);
        }
        
        loaded = true;
    }
            


// Extended methods
// ..........................................................................


    /**
     * Returns the keybundle identified by the given alias.
     *
     * @param alias the alias for the entry to return
     * @returns the keybundle entry identified by alias or null if the
     *          given alias does not identify a keybundle entry
     * @throws KeyStoreException if the keystore has not been initialized 
     *         (loaded) yet
     */
    public KeyBundle engineGetKeyBundle(String alias)
        throws KeyStoreException
    {
        if (! loaded) throw new KeyStoreException("Not yet loaded.");
        return (KeyBundle)bundleAliases.get(alias);
    }
    

    /**
     * Returns if the entry identified by alias is a keybundle entry.
     *
     * @param alias the alias for the entry to check
     * @returns true if the entry identified by alias is a keybundle entry,
     *          false otherwise
     * @throws KeyStoreException if the keystore has not been initialized 
     *         (loaded) yet
     */
    public boolean engineIsKeyBundleEntry(String alias)
        throws KeyStoreException
    {
        if (! loaded) throw new KeyStoreException("Not yet loaded.");
        return bundleAliases.containsKey(alias);
    }
    

    /**
     * Stores the given keybundle in this store.
     *
     * @param bundle the bundle to store
     * @returns the alias under which the bundle is stored
     * @throws KeyStoreException if the keystore has not been initialized 
     *         (loaded) yet
     */
    public String engineSetKeyBundleEntry(KeyBundle bundle)
        throws KeyStoreException
    {
        if (! loaded) throw new KeyStoreException("Not yet loaded.");
        try {

            if (! (bundle instanceof PGPKeyBundle)) {
                throw new KeyStoreException(
                    "Supplied certificate is not a PGPCertificate.");
            }
            
            KeyID id = 
                PGPKeyIDFactory.convert((Key)bundle.getPublicKeys().next());
            byte[] keyid = id.getBytes(4);
        
            char[] buf = new char[10];
            buf[0] = '0';
            buf[1] = 'x';
            buf[2] = hexDigits[(keyid[0] >>> 4) & 0x0F];
            buf[3] = hexDigits[ keyid[0]        & 0x0F];
            buf[4] = hexDigits[(keyid[1] >>> 4) & 0x0F];
            buf[5] = hexDigits[ keyid[1]        & 0x0F];
            buf[6] = hexDigits[(keyid[2] >>> 4) & 0x0F];
            buf[7] = hexDigits[ keyid[2]        & 0x0F];
            buf[8] = hexDigits[(keyid[3] >>> 4) & 0x0F];
            buf[9] = hexDigits[ keyid[3]        & 0x0F];
            String keyalias = new String(buf);
            
            bundles.add(bundle);
            bundleAliases.put(keyalias, bundle);
            
            return keyalias;

        } catch (InvalidKeyException ike) {
            throw new KeyStoreException(""+ike);
        }
    }



// Inner classes
// ..........................................................................


    /**
     * An Enumeration wrapper for an Iterator.
     */
    private class IteratorEnumeration implements Enumeration {
    
        private Iterator parent;
    
        public IteratorEnumeration(Iterator parent) {
            this.parent = parent;
        }
        
        public boolean hasMoreElements() { 
            return parent.hasNext(); 
        }
        
        public Object nextElement() {
            return parent.next();
        }
        
    }


}

