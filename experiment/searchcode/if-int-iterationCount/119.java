/*
 * QuestForever Everquest and Everquest II (TM) data browser for Android
 * Smartphones.
 * Copyright (C) 2010-2011 Gerard Krupa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package info.truewatch.android.questforever.db;

import info.truewatch.android.questforever.core.Config;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class Crypto {

    Cipher ecipher;
    Cipher dcipher;

    Crypto(String passPhrase) {

        // 8-bytes Salt
        byte[] salt = "QuestFor".getBytes();

        // Iteration count
        int iterationCount = 19;

        try {

            KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

            ecipher = Cipher.getInstance(key.getAlgorithm());
            dcipher = Cipher.getInstance(key.getAlgorithm());

            // Prepare the parameters to the cipthers
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        } catch (Exception e) {
        	Config.LOG.error("Construct error", e);
        }
    }


    public String encrypt(String str) {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");

            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            return new String(Base64.encodeBase64(enc));

        } catch (Exception e) {
        	Config.LOG.error("Encrypt error", e);
        }
        return null;
    }

    public String decrypt(String str) {

        try {

            // Decode base64 to get bytes
            byte[] dec = Base64.decodeBase64(str.getBytes());

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");

        } catch (Exception e) {
        	Config.LOG.error("Decrypt error", e);
        }
        return null;
    }
}


