/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.fastcodingtools.util;


import java.io.InputStream;
import java.io.Serializable;

import com.fastcodingtools.excecao.SerializacaoException;



/**
 * Classe utilit?ria com m?todos para serializar um objeto para string e
 * instancia-lo a partir desse string.
 *
 * @author macg
 *
 */
public class SerializationUtils {


	// private static final String ENCODING = "ISO-8859-1";
	private static final String	hexDigits	= "0123456789abcdef";


	public static byte[] serialize(Object object) {

		byte[] serialized = org.apache.commons.lang.SerializationUtils.serialize((Serializable) object);

		return serialized;
	}

	public static String serializeToHexa(Object object) {


		return byteArrayToHexString(serialize(object));
	}

	public static Object deserialize(InputStream inputStream) throws SerializacaoException {

		Object object = null;
		try {
			object = org.apache.commons.lang.SerializationUtils.deserialize(inputStream);
		} catch (Exception e) {
			throw new SerializacaoException(e);
		}

		return object;
	}	
	
	public static Object deserialize(byte[] serializedObject) throws SerializacaoException {

		Object object = null;
		try {
			object = org.apache.commons.lang.SerializationUtils.deserialize(serializedObject);
		} catch (Exception e) {
			throw new SerializacaoException(e);
		}

		return object;
	}


	public static Object deserializeFromHexa(String serializedObject) throws SerializacaoException {

		return deserialize(hexStringToByteArray(serializedObject));
	}

	/**
	 * Converte o array de bytes em uma representa??o hexadecimal.
	 *
	 * @param input -
	 *            O array de bytes a ser convertido.
	 * @return Uma String com a representa??o hexa do array
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer buf = new StringBuffer();

        for (byte aB : b) {
            int j = (aB) & 0xFF;
            buf.append(hexDigits.charAt(j / 16));
            buf.append(hexDigits.charAt(j % 16));
        }

        return buf.toString();
	}

	/**
	 * Converte uma String hexa no array de bytes correspondente.
	 *
	 * @param hexa -
	 *            A String hexa
	 * @return O vetor de bytes
	 * @throws IllegalArgumentException -
	 *             Caso a String n?o sej auma representa??o haxadecimal v?lida
	 */
	public static byte[] hexStringToByteArray(String hexa) {

		// verifica se a String possui uma quantidade par de elementos
		if (hexa.length() % 2 != 0) {
			throw new IllegalArgumentException("String hexa inv?lida");
		}

		byte[] b = new byte[hexa.length() / 2];

		for (int i = 0; i < hexa.length(); i  += 2) {
			b[i / 2] = (byte) ((hexDigits.indexOf(hexa.charAt(i)) << 4) | (hexDigits.indexOf(hexa.charAt(i + 1))));
		}
		return b;
	}
}

