package tests.core_low;

import static org.junit.Assert.assertTrue;

import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.mplify.crypto.CompressionStatus;
import com.mplify.crypto.EncryptionStatus;
import com.mplify.crypto.RepackOrder;
import com.mplify.crypto.SymmetricKey;
import com.mplify.crypto.SymmetricKeyInfo;
import com.mplify.crypto_impl.SymmetricKeyImpl;
import com.mplify.crypto_impl.SymmetricKeyImpl.KEY_SIZE;
import com.mplify.junit.HelperForTestCases;
import com.mplify.junit.TestStarter;
import com.mplify.stringmangling.CompressBehaviour;
import com.mplify.stringmangling.EncryptBehaviour;
import com.mplify.stringmangling.PackData;
import com.mplify.stringmangling.PackException;
import com.mplify.stringmangling.PackResult;
import com.mplify.stringmangling.PackUtils;
import com.mplify.stringmangling.UnpackException;
/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2008, M-PLIFY S.A.
 *                     21, rue Glesener
 *                     L-1631 LUXEMBOURG
 *
 * All rights reserved.
 *******************************************************************************
 *******************************************************************************
 * Test "packing" (compression, encryption, encoding) of strings.
 ******************************************************************************/

//@SuppressWarnings("static-method")
public class TestCaseStringPacking extends TestStarter {

    private final static String CLASS = TestCaseStringPacking.class.getName();

    private static void stringLoop(Logger logger, SymmetricKey symKey, CompressBehaviour cb) throws PackException, UnpackException {
        for (int i = 0; i < 10000; i+=20) {
            long start = System.currentTimeMillis();
            String input = HelperForTestCases.createRandomString(i);
            RepackOrder ro;
            if (symKey == null) {
                ro = new RepackOrder(cb);
            }
            else {
                // use index 0 always
                ro = new RepackOrder(cb,EncryptBehaviour.DO_ENCRYPT,new SymmetricKeyInfo(0, symKey));
            }
            PackResult mr = PackUtils.pack(input, ro);
            long middle = System.currentTimeMillis();
            assertTrue("Compression must have been applied if asked", cb != CompressBehaviour.ALWAYS || mr.getCompressionStatus() != CompressionStatus.UNCOMPRESSED);
            assertTrue("Encryption must have been applied if asked", symKey == null || mr.getEncryptionStatus() != EncryptionStatus.UNENCRYPTED);
            String output = mr.getDataString();
            CompressionStatus cs = mr.getCompressionStatus();
            PackData packData = new PackData(mr.getDataString(), mr.getCompressionStatus(), mr.getEncryptionStatus());
            String reinput = PackUtils.unpack(packData, symKey);
            long end = System.currentTimeMillis();
            long pack_ms = middle - start;
            long unpack_ms = end - middle;
            // only print seriously long runs
            if (pack_ms + unpack_ms > 5) {
                logger.info("Tested with string length " + input.length() + ": " + pack_ms + " ms needed to pack, " + unpack_ms + " ms needed to unpack");
            }
            HelperForTestCases.compareString(input, reinput, logger);
        }
    }

    @Test
    public void testPackingWithCryptoAndCompression() throws Exception {
        Logger logger = LoggerFactory.getLogger(CLASS + ".testPackingWithCryptoAndCompression");
        // Default Java Provider only knows 128-bit AES
        SecretKeySpec sks = SymmetricKeyImpl.newKey(SymmetricKeyImpl.createAESKeyGenerator(KEY_SIZE.KEY_128));
        SymmetricKey crypto = new SymmetricKeyImpl(sks);
        boolean alwaysCompress;
        stringLoop(logger, crypto, CompressBehaviour.ALWAYS);
    }

    @Test
    public void testPackingWithoutCryptoAndVoluntaryCompression() throws Exception {
        Logger logger = LoggerFactory.getLogger(CLASS + ".testPackingWithoutCryptoAndVoluntaryCompression");
        boolean alwaysCompress;
        stringLoop(logger, null, CompressBehaviour.ONLY_IF_SMALLER);
    }

    @Test
    public void testPackingWithoutCryptoAndCompression() throws Exception {
        Logger logger = LoggerFactory.getLogger(CLASS + ".testPackingWithoutCryptoAndCompression");
        boolean alwaysCompress;
        stringLoop(logger, null, CompressBehaviour.ALWAYS);
    }

    @Test
    public void testPackingWithCryptoAndVoluntaryCompression() throws Exception {
        Logger logger = LoggerFactory.getLogger(CLASS + ".testPackingWithCryptoAndVoluntaryCompression");
        // Default Java Provider only knows 128-bit AES
        SecretKeySpec sks = SymmetricKeyImpl.newKey(SymmetricKeyImpl.createAESKeyGenerator(KEY_SIZE.KEY_128));
        SymmetricKey crypto = new SymmetricKeyImpl(sks);
        boolean alwaysCompress;
        stringLoop(logger, crypto, CompressBehaviour.ONLY_IF_SMALLER);
    }

}

