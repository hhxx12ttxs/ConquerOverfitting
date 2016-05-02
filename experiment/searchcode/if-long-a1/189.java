/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.util;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;


public class BSPatchTest extends TestCase {

    // $ bsdiff file1 file2 diff-1-2.patch
    // $ hexdump -v -e '1/1 "0x%02x, "' diff-1-2.patch

    public void testBSPatch1() throws Exception {
        byte[] file1 = toSignedBytes(new short[] {
                0x62, 0x73, 0x64, 0x69, 0x66, 0x66, 0x20, 0x69, 0x73, 0x20,
                0x61, 0x20, 0x74, 0x6f, 0x6f, 0x6c, 0x20, 0x66, 0x6f, 0x72,
                0x20, 0x62, 0x75, 0x69, 0x6c, 0x64, 0x69, 0x6e, 0x67, 0x20,
                0x61, 0x6e, 0x64, 0x20, 0x61, 0x70, 0x70, 0x6c, 0x79, 0x69,
                0x6e, 0x67, 0x20, 0x70, 0x61, 0x74, 0x63, 0x68, 0x65, 0x73,
                0x20, 0x74, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61, 0x72, 0x79,
                0x20, 0x66, 0x69, 0x6c, 0x65, 0x73, 0x2e, 0x0a
            });

        byte[] file2 = toSignedBytes(new short[] {
                0x62, 0x73, 0x64, 0x69, 0x66, 0x66, 0x20, 0x61, 0x6e, 0x64,
                0x20, 0x62, 0x73, 0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x61,
                0x72, 0x65, 0x20, 0x74, 0x6f, 0x6f, 0x6c, 0x73, 0x20, 0x66,
                0x6f, 0x72, 0x20, 0x62, 0x75, 0x69, 0x6c, 0x64, 0x69, 0x6e,
                0x67, 0x20, 0x61, 0x6e, 0x64, 0x20, 0x61, 0x70, 0x70, 0x6c,
                0x79, 0x69, 0x6e, 0x67, 0x20, 0x70, 0x61, 0x74, 0x63, 0x68,
                0x65, 0x73, 0x20, 0x74, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61,
                0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65, 0x73, 0x2e, 0x0a,
            });

        byte[] patch = toSignedBytes(new short[] {
                0x42, 0x53, 0x44, 0x49, 0x46, 0x46, 0x34, 0x30, 0x35, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x27, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41, 0x59, 0x26,
                0x53, 0x59, 0x93, 0x0d, 0x6a, 0xae, 0x00, 0x00, 0x0c, 0x68,
                0x40, 0x58, 0xa8, 0x02, 0x00, 0x04, 0x00, 0x40, 0x00, 0x20,
                0x00, 0x21, 0x88, 0x19, 0x08, 0x32, 0x62, 0x1b, 0xde, 0xbc,
                0x24, 0x08, 0xe9, 0x45, 0x3c, 0x5d, 0xc9, 0x14, 0xe1, 0x42,
                0x42, 0x4c, 0x35, 0xaa, 0xb8, 0x42, 0x5a, 0x68, 0x39, 0x31,
                0x41, 0x59, 0x26, 0x53, 0x59, 0x05, 0xb6, 0xa3, 0x63, 0x00,
                0x00, 0x00, 0x48, 0x00, 0x40, 0x00, 0x00, 0x80, 0x20, 0x00,
                0x21, 0x00, 0x82, 0x83, 0x17, 0x72, 0x45, 0x38, 0x50, 0x90,
                0x05, 0xb6, 0xa3, 0x63, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41,
                0x59, 0x26, 0x53, 0x59, 0xdb, 0x41, 0x22, 0x6f, 0x00, 0x00,
                0x01, 0x91, 0x80, 0x40, 0x00, 0x3e, 0x45, 0xdc, 0x00, 0x20,
                0x00, 0x22, 0x9a, 0x19, 0x32, 0x7a, 0x7a, 0xa1, 0x00, 0x00,
                0x21, 0xe2, 0xf8, 0x98, 0x42, 0x13, 0x3c, 0xec, 0x35, 0x5f,
                0x17, 0x72, 0x45, 0x38, 0x50, 0x90, 0xdb, 0x41, 0x22, 0x6f
        });

        byte[] expected = file2;
        byte[] actual   = patchFile(file1, patch);

        assertEquals(toDiffString(expected, actual),
                Arrays.toString(expected), Arrays.toString(actual));
    }

    public void testBSPatch2() throws Exception {
        byte[] file1 = toSignedBytes(new short[] {
                0x62, 0x73, 0x64, 0x69, 0x66, 0x66, 0x20, 0x61, 0x6e, 0x64,
                0x20, 0x62, 0x73, 0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x61,
                0x72, 0x65, 0x20, 0x74, 0x6f, 0x6f, 0x6c, 0x73, 0x20, 0x66,
                0x6f, 0x72, 0x20, 0x62, 0x75, 0x69, 0x6c, 0x64, 0x69, 0x6e,
                0x67, 0x20, 0x61, 0x6e, 0x64, 0x20, 0x61, 0x70, 0x70, 0x6c,
                0x79, 0x69, 0x6e, 0x67, 0x20, 0x70, 0x61, 0x74, 0x63, 0x68,
                0x65, 0x73, 0x20, 0x74, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61,
                0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65, 0x73, 0x2e, 0x0a,
                0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
                0x2e, 0x64, 0x61, 0x65, 0x6d, 0x6f, 0x6e, 0x6f, 0x6c, 0x6f,
                0x67, 0x79, 0x2e, 0x6e, 0x65, 0x74, 0x2f, 0x62, 0x73, 0x64,
                0x69, 0x66, 0x66, 0x2f, 0x0a, 0x44, 0x65, 0x73, 0x63, 0x72,
                0x69, 0x70, 0x74, 0x69, 0x6f, 0x6e, 0x3a, 0x20, 0x67, 0x65,
                0x6e, 0x65, 0x72, 0x61, 0x74, 0x65, 0x2f, 0x61, 0x70, 0x70,
                0x6c, 0x79, 0x20, 0x61, 0x20, 0x70, 0x61, 0x74, 0x63, 0x68,
                0x20, 0x62, 0x65, 0x74, 0x77, 0x65, 0x65, 0x6e, 0x20, 0x74,
                0x77, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61, 0x72, 0x79, 0x20,
                0x66, 0x69, 0x6c, 0x65, 0x73, 0x2e, 0x0a
            });

        byte[] file2 = toSignedBytes(new short[] {
                0x62, 0x73, 0x64, 0x69, 0x66, 0x66, 0x20, 0x61, 0x6e, 0x64,
                0x20, 0x62, 0x73, 0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x61,
                0x72, 0x65, 0x20, 0x74, 0x6f, 0x6f, 0x6c, 0x73, 0x20, 0x66,
                0x6f, 0x72, 0x20, 0x62, 0x75, 0x69, 0x6c, 0x64, 0x69, 0x6e,
                0x67, 0x20, 0x61, 0x6e, 0x64, 0x20, 0x61, 0x70, 0x70, 0x6c,
                0x79, 0x69, 0x6e, 0x67, 0x20, 0x70, 0x61, 0x74, 0x63, 0x68,
                0x65, 0x73, 0x20, 0x74, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61,
                0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65, 0x73, 0x2e, 0x0a,
                0x44, 0x65, 0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x69, 0x6f,
                0x6e, 0x3a, 0x20, 0x67, 0x65, 0x6e, 0x65, 0x72, 0x61, 0x74,
                0x65, 0x2f, 0x61, 0x70, 0x70, 0x6c, 0x79, 0x20, 0x61, 0x20,
                0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x62, 0x65, 0x74, 0x77,
                0x65, 0x65, 0x6e, 0x20, 0x74, 0x77, 0x6f, 0x20, 0x62, 0x69,
                0x6e, 0x61, 0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65, 0x73,
                0x2e, 0x0a, 0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77,
                0x77, 0x77, 0x2e, 0x64, 0x61, 0x65, 0x6d, 0x6f, 0x6e, 0x6f,
                0x6c, 0x6f, 0x67, 0x79, 0x2e, 0x6e, 0x65, 0x74, 0x2f, 0x62,
                0x73, 0x64, 0x69, 0x66, 0x66, 0x2f, 0x0a, 0x42, 0x53, 0x44,
                0x20, 0x6c, 0x69, 0x63, 0x65, 0x6e, 0x73, 0x65, 0x2c, 0x20,
                0x43, 0x6f, 0x70, 0x79, 0x72, 0x69, 0x67, 0x68, 0x74, 0x20,
                0x32, 0x30, 0x30, 0x33, 0x2d, 0x32, 0x30, 0x30, 0x35, 0x20,
                0x43, 0x6f, 0x6c, 0x69, 0x6e, 0x20, 0x50, 0x65, 0x72, 0x63,
                0x69, 0x76, 0x61, 0x6c, 0x0a,
            });

        byte[] patch = toSignedBytes(new short[] {
                0x42, 0x53, 0x44, 0x49, 0x46, 0x46, 0x34, 0x30, 0x3e, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x27, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0xe1, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41, 0x59, 0x26,
                0x53, 0x59, 0x3f, 0xa6, 0x07, 0x42, 0x00, 0x00, 0x14, 0x5d,
                0x40, 0x58, 0x08, 0x08, 0x00, 0xc8, 0x02, 0x00, 0x00, 0xa0,
                0x00, 0x40, 0x00, 0x20, 0x00, 0x21, 0xa4, 0x69, 0x84, 0xfd,
                0x41, 0x03, 0x40, 0xd0, 0x22, 0xef, 0xe1, 0x49, 0x33, 0x02,
                0xce, 0x2e, 0xe6, 0x8b, 0xb9, 0x22, 0x9c, 0x28, 0x48, 0x1f,
                0xd3, 0x03, 0xa1, 0x00, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41,
                0x59, 0x26, 0x53, 0x59, 0x58, 0xc3, 0x04, 0xf0, 0x00, 0x00,
                0x00, 0x40, 0x10, 0x40, 0x00, 0x00, 0x02, 0x20, 0x00, 0x21,
                0x00, 0x82, 0x83, 0x17, 0x72, 0x45, 0x38, 0x50, 0x90, 0x58,
                0xc3, 0x04, 0xf0, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41, 0x59,
                0x26, 0x53, 0x59, 0x26, 0xc7, 0xbc, 0x09, 0x00, 0x00, 0x08,
                0x5f, 0x80, 0x00, 0x10, 0x40, 0x06, 0x5a, 0x00, 0x1c, 0x00,
                0x48, 0x00, 0x2a, 0xe5, 0xdd, 0x20, 0x20, 0x00, 0x31, 0x46,
                0x86, 0x80, 0x00, 0x00, 0x1a, 0xa6, 0x26, 0x40, 0xfd, 0x50,
                0x34, 0x79, 0x27, 0x92, 0x78, 0xda, 0x4d, 0x37, 0xa9, 0x20,
                0x8d, 0x8c, 0x41, 0x90, 0xea, 0x1c, 0x3a, 0xb3, 0xaa, 0x63,
                0x64, 0xa4, 0x27, 0x6d, 0x5b, 0x2a, 0xfc, 0x25, 0x1b, 0xab,
                0xd2, 0xff, 0x8b, 0xb9, 0x22, 0x9c, 0x28, 0x48, 0x13, 0x63,
                0xde, 0x04, 0x80
        });

        byte[] expected = file2;
        byte[] actual   = patchFile(file1, patch);

        assertEquals(toDiffString(expected, actual),
                Arrays.toString(expected), Arrays.toString(actual));
    }

    public void testBSPatch3() throws Exception {
        byte[] file1 = toSignedBytes(new short[] {
                0x68, 0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77,
                0x2e, 0x64, 0x61, 0x65, 0x6d, 0x6f, 0x6e, 0x6f, 0x6c, 0x6f,
                0x67, 0x79, 0x2e, 0x6e, 0x65, 0x74, 0x2f, 0x62, 0x73, 0x64,
                0x69, 0x66, 0x66, 0x2f, 0x0a, 0x42, 0x69, 0x6e, 0x61, 0x72,
                0x79, 0x20, 0x64, 0x69, 0x66, 0x66, 0x2f, 0x70, 0x61, 0x74,
                0x63, 0x68, 0x20, 0x75, 0x74, 0x69, 0x6c, 0x69, 0x74, 0x79,
                0x0a, 0x53, 0x48, 0x41, 0x31, 0x3a, 0x20, 0x37, 0x32, 0x63,
                0x35, 0x37, 0x34, 0x33, 0x34, 0x62, 0x64, 0x64, 0x34, 0x63,
                0x33, 0x38, 0x33, 0x63, 0x36, 0x39, 0x62, 0x62, 0x30, 0x66,
                0x61, 0x64, 0x34, 0x32, 0x33, 0x35, 0x37, 0x38, 0x35, 0x32,
                0x32, 0x63, 0x64, 0x30, 0x64, 0x33, 0x61, 0x0a, 0x53, 0x48,
                0x41, 0x32, 0x35, 0x36, 0x3a, 0x20, 0x61, 0x62, 0x62, 0x64,
                0x32, 0x32, 0x30, 0x39, 0x33, 0x38, 0x35, 0x65, 0x38, 0x65,
                0x38, 0x38, 0x30, 0x61, 0x64, 0x64, 0x30, 0x62, 0x37, 0x38,
                0x31, 0x37, 0x37, 0x38, 0x64, 0x65, 0x64, 0x34, 0x39, 0x65,
                0x31, 0x30, 0x61, 0x36, 0x66, 0x30, 0x63, 0x37, 0x39, 0x39,
                0x64, 0x33, 0x32, 0x36, 0x61, 0x36, 0x61, 0x65, 0x36, 0x37,
                0x30, 0x33, 0x36, 0x39, 0x36, 0x38, 0x66, 0x62, 0x31, 0x64,
                0x0a, 0x44, 0x65, 0x73, 0x63, 0x72, 0x69, 0x70, 0x74, 0x69,
                0x6f, 0x6e, 0x3a, 0x20, 0x67, 0x65, 0x6e, 0x65, 0x72, 0x61,
                0x74, 0x65, 0x2f, 0x61, 0x70, 0x70, 0x6c, 0x79, 0x20, 0x61,
                0x20, 0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x62, 0x65, 0x74,
                0x77, 0x65, 0x65, 0x6e, 0x20, 0x74, 0x77, 0x6f, 0x20, 0x62,
                0x69, 0x6e, 0x61, 0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65,
                0x73, 0x0a, 0x20, 0x62, 0x73, 0x64, 0x69, 0x66, 0x66, 0x20,
                0x61, 0x6e, 0x64, 0x20, 0x62, 0x73, 0x70, 0x61, 0x74, 0x63,
                0x68, 0x20, 0x61, 0x72, 0x65, 0x20, 0x74, 0x6f, 0x6f, 0x6c,
                0x73, 0x20, 0x66, 0x6f, 0x72, 0x20, 0x62, 0x75, 0x69, 0x6c,
                0x64, 0x69, 0x6e, 0x67, 0x20, 0x61, 0x6e, 0x64, 0x20, 0x61,
                0x70, 0x70, 0x6c, 0x79, 0x69, 0x6e, 0x67, 0x20, 0x70, 0x61,
                0x74, 0x63, 0x68, 0x65, 0x73, 0x20, 0x74, 0x6f, 0x20, 0x62,
                0x69, 0x6e, 0x61, 0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65,
                0x73, 0x2e, 0x0a
            });

        byte[] file2 = toSignedBytes(new short[] {
                0x42, 0x69, 0x6e, 0x61, 0x72, 0x79, 0x20, 0x64, 0x69, 0x66,
                0x66, 0x2f, 0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x75, 0x74,
                0x69, 0x6c, 0x69, 0x74, 0x79, 0x0a, 0x48, 0x6f, 0x6d, 0x65,
                0x70, 0x61, 0x67, 0x65, 0x3a, 0x20, 0x20, 0x20, 0x20, 0x68,
                0x74, 0x74, 0x70, 0x3a, 0x2f, 0x2f, 0x77, 0x77, 0x77, 0x2e,
                0x64, 0x61, 0x65, 0x6d, 0x6f, 0x6e, 0x6f, 0x6c, 0x6f, 0x67,
                0x79, 0x2e, 0x6e, 0x65, 0x74, 0x2f, 0x62, 0x73, 0x64, 0x69,
                0x66, 0x66, 0x2f, 0x0a, 0x53, 0x48, 0x41, 0x31, 0x3a, 0x20,
                0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x37, 0x32, 0x63,
                0x35, 0x37, 0x34, 0x33, 0x34, 0x62, 0x64, 0x64, 0x34, 0x63,
                0x33, 0x38, 0x33, 0x63, 0x36, 0x39, 0x62, 0x62, 0x30, 0x66,
                0x61, 0x64, 0x34, 0x32, 0x33, 0x35, 0x37, 0x38, 0x35, 0x32,
                0x32, 0x63, 0x64, 0x30, 0x64, 0x33, 0x61, 0x0a, 0x53, 0x48,
                0x41, 0x32, 0x35, 0x36, 0x3a, 0x20, 0x20, 0x20, 0x20, 0x20,
                0x20, 0x61, 0x62, 0x62, 0x64, 0x32, 0x32, 0x30, 0x39, 0x33,
                0x38, 0x35, 0x65, 0x38, 0x65, 0x38, 0x38, 0x30, 0x61, 0x64,
                0x64, 0x30, 0x62, 0x37, 0x38, 0x31, 0x37, 0x37, 0x38, 0x64,
                0x65, 0x64, 0x34, 0x39, 0x65, 0x31, 0x30, 0x61, 0x36, 0x66,
                0x30, 0x63, 0x37, 0x39, 0x39, 0x64, 0x33, 0x32, 0x36, 0x61,
                0x36, 0x61, 0x65, 0x36, 0x37, 0x30, 0x33, 0x36, 0x39, 0x36,
                0x38, 0x66, 0x62, 0x31, 0x64, 0x0a, 0x44, 0x65, 0x73, 0x63,
                0x72, 0x69, 0x70, 0x74, 0x69, 0x6f, 0x6e, 0x3a, 0x20, 0x67,
                0x65, 0x6e, 0x65, 0x72, 0x61, 0x74, 0x65, 0x2f, 0x61, 0x70,
                0x70, 0x6c, 0x79, 0x20, 0x61, 0x20, 0x70, 0x61, 0x74, 0x63,
                0x68, 0x20, 0x62, 0x65, 0x74, 0x77, 0x65, 0x65, 0x6e, 0x20,
                0x74, 0x77, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61, 0x72, 0x79,
                0x20, 0x66, 0x69, 0x6c, 0x65, 0x73, 0x0a, 0x20, 0x20, 0x20,
                0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
                0x62, 0x73, 0x64, 0x69, 0x66, 0x66, 0x20, 0x61, 0x6e, 0x64,
                0x20, 0x62, 0x73, 0x70, 0x61, 0x74, 0x63, 0x68, 0x20, 0x61,
                0x72, 0x65, 0x20, 0x74, 0x6f, 0x6f, 0x6c, 0x73, 0x20, 0x66,
                0x6f, 0x72, 0x20, 0x62, 0x75, 0x69, 0x6c, 0x64, 0x69, 0x6e,
                0x67, 0x20, 0x61, 0x6e, 0x64, 0x20, 0x61, 0x70, 0x70, 0x6c,
                0x79, 0x69, 0x6e, 0x67, 0x20, 0x70, 0x61, 0x74, 0x63, 0x68,
                0x65, 0x73, 0x20, 0x74, 0x6f, 0x20, 0x62, 0x69, 0x6e, 0x61,
                0x72, 0x79, 0x20, 0x66, 0x69, 0x6c, 0x65, 0x73, 0x2e, 0x0a,
            });

        byte[] patch = toSignedBytes(new short[] {
                0x42, 0x53, 0x44, 0x49, 0x46, 0x46, 0x34, 0x30, 0x48, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2b, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x68, 0x01, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41, 0x59, 0x26,
                0x53, 0x59, 0xea, 0x1c, 0x55, 0x4e, 0x00, 0x00, 0x07, 0xfa,
                0x40, 0x7c, 0x0e, 0x00, 0x10, 0x88, 0x00, 0x10, 0x02, 0x20,
                0x00, 0x40, 0x00, 0x20, 0x00, 0x21, 0x29, 0xa8, 0x00, 0x6d,
                0x42, 0x98, 0x00, 0x09, 0x9a, 0x99, 0xcc, 0xb7, 0x2b, 0xcd,
                0xf7, 0x1e, 0x00, 0x86, 0x22, 0x21, 0x09, 0x25, 0x14, 0xc5,
                0x0e, 0xd4, 0x61, 0xf1, 0x77, 0x24, 0x53, 0x85, 0x09, 0x0e,
                0xa1, 0xc5, 0x54, 0xe0, 0x42, 0x5a, 0x68, 0x39, 0x31, 0x41,
                0x59, 0x26, 0x53, 0x59, 0xb2, 0xea, 0xe3, 0xb5, 0x00, 0x00,
                0x00, 0xc8, 0x00, 0xc0, 0x00, 0x00, 0x02, 0x00, 0x08, 0x20,
                0x00, 0x21, 0x26, 0x41, 0x98, 0xa8, 0x0e, 0x2e, 0xe4, 0x8a,
                0x70, 0xa1, 0x21, 0x65, 0xd5, 0xc7, 0x6a, 0x42, 0x5a, 0x68,
                0x39, 0x31, 0x41, 0x59, 0x26, 0x53, 0x59, 0x99, 0x1b, 0x67,
                0xdb, 0x00, 0x00, 0x07, 0xff, 0x80, 0x40, 0x00, 0x10, 0x00,
                0x40, 0x00, 0x20, 0x10, 0x20, 0x40, 0x08, 0x00, 0x22, 0x82,
                0xc0, 0x00, 0x20, 0x00, 0x31, 0x00, 0x00, 0x06, 0x81, 0x33,
                0x50, 0xc3, 0x00, 0x20, 0x73, 0xb3, 0x44, 0x9c, 0xfd, 0xde,
                0x1f, 0x68, 0xbb, 0x92, 0x29, 0xc2, 0x84, 0x84, 0xc8, 0xdb,
                0x3e, 0xd8
        });

        byte[] expected = file2;
        byte[] actual   = patchFile(file1, patch);

        assertEquals(toDiffString(expected, actual),
                Arrays.toString(expected), Arrays.toString(actual));
    }

    private String toDiffString(byte[] a1, byte[] a2) {
        StringBuilder sb = new StringBuilder();
        int n1 = a1.length;
        int n2 = a2.length;
        boolean was_same = false;

        for (int i = 0; i < n1; i++) {
            boolean same = i > 1 &&
                           i+2 < n1 &&
                           i+2 < n2 &&
                           a1[i+0] == a2[i+0] &&
                           a1[i+1] == a2[i+1] &&
                           a1[i+2] == a2[i+2];
            if (!same) {
                if (i >= n2) {
                    sb.append(String.format("[%1$3d] %2$02x %2$c | -- -\n", i, a1[i]));
                } else {
                    sb.append(String.format("[%1$3d] %2$02x %2$c | %3$02x %3$c\n", i, a1[i], a2[i]));
                }
            } else if (!was_same) {
                sb.append(String.format("[%1$3d] ...\n", i));
            }
            was_same = same;
        }
        for (int i = n1; i < n2; i++) {
            sb.append(String.format("[%1$3d] -- - | %2$02x %2$c\n", i, a2[i]));
        }

        return sb.toString();
    }

    /**
     * Work around the lack of unsigned bytes in java by providing an initialization
     * array where each short is in the range 0..0xFF and converting it to signed bytes.
     *
     * unsigned byte:   0..127 => signed java byte:    0..127
     * unsigned byte: 128..255 => signed java byte: -128..-1
     *
     * unsigned to signed java: (unsigned - 256) if unsigned > 127
     * signed java to unsigned: (256 + signed) if signed < 0
     */
    private byte[] toSignedBytes(short[] s) {
        int n = s.length;
        byte[] b = new byte[n];
        for (int i = 0; i < n; i++) {
            short v = s[i];
            b[i] = v < 128 ? (byte)v : (byte)(v - 256);
        }
        return b;
    }

    @SuppressWarnings("unused")
    private byte toSigned(int unsigned) {
        return unsigned < 128 ? (byte)unsigned : (byte)(unsigned - 256);
    }

    private short toUnsigned(byte signed) {
        if (signed >= 0) {
            return signed;
        } else {
            return (short) ((short) 256 + signed);
        }
    }

    /**
     * Patches the binary "file1" using the bsdiff/bspatch "patch" data.
     * This implements bspatch.c in Java.
     *
     * Reference: http://www.daemonology.net/bsdiff/ <br/>
     * Based on bspatch.c as identified by <br/>
     * {@code $FreeBSD: src/usr.bin/bsdiff/bspatch/bspatch.c,v 1.1 2005/08/06 01:59:06 cperciva Exp $}
     * (BSD license, Copyright 2003-2005 Colin Percival)
     *
     * @param file1 The base file to be patched.
     * @param patch The binary patch to apply to base file.
     * @return A new byte array representing the patched file.
     * @throws PatchException when the patch header is invalid.
     * @throws IOException if the BZIP2 decoder fails.
     */
    private byte[] patchFile(byte[] file1, byte[] patch) throws PatchException, IOException {
        /*
        File format:
            0   8   "BSDIFF40"
            8   8   X
            16  8   Y
            24  8   sizeof(newfile)
            32  X   bzip2(control block)
            32+X    Y   bzip2(diff block)
            32+X+Y  ??? bzip2(extra block)
        with control block a set of triples (x,y,z) meaning "add x bytes
        from oldfile to x bytes from the diff block; copy y bytes from the
        extra block; seek forwards in oldfile by z bytes".
        */

        /* Read header */
        if (patch.length < 32) {
            throw new PatchException("Header.len < 32");
        }
        byte[] header = patch;

        /* Check for appropriate magic */
        if (header[0] != 'B' || header[1] != 'S' || header[2] != 'D' || header[3] != 'I' ||
            header[4] != 'F' || header[5] != 'F' || header[6] != '4' || header[7] != '0') {
            throw new PatchException("Invalid header signature");
        }

        /* Read lengths from header */
        long bzctrllen = offtin(header,  8);
        long bzdatalen = offtin(header, 16);
        long newsize   = offtin(header, 24);
        if (bzctrllen < 0 || bzdatalen < 0 || newsize < 0) {
            throw new PatchException("Invalid header lengths");
        }

        // Note: bspatch uses long lengths everywhere;
        // however new byte[] doesn't support that and we don't expect to
        // have 2GB+ file sizes to diff any time soon so let's
        // do a first implementation that only supports 2^32 sizes.

        /* Read embedded files using Apache Common Compress' BZIP2 */
        InputStream cpfbz2 = readBzip2Data(patch, 32, bzctrllen);
        InputStream dpfbz2 = readBzip2Data(patch, 32 + bzctrllen, bzdatalen);
        InputStream epfbz2 = readBzip2Data(patch, 32 + bzctrllen + bzdatalen, -1);

        int oldsize = file1.length;
        byte[] old = file1;

        byte[] _new = new byte[(int) newsize];

        long ctrl[] = new long[3];
        byte buf[] = new byte[8];
        long oldpos = 0;
        long newpos = 0;
        while (newpos < newsize) {
            long lenread;

            /* Read control data */
            for(int i = 0; i <= 2; i++) {
                lenread = BZ2_bzRead(cpfbz2, buf, 0, 8);
                if (lenread < 8) {
                    throw new PatchException("Failed to read control data") ;
                }
                ctrl[i] = offtin(buf, 0);
            };

            /* Sanity-check */
            if (newpos + ctrl[0] > newsize) {
                throw new PatchException("Sanity check failed") ;
            }

            /* Read diff string */
            lenread = BZ2_bzRead(dpfbz2, _new, newpos, ctrl[0]);
            if (lenread < ctrl[0]) {
                throw new PatchException("Failed to read diff data") ;
            }

            /* Add old data to diff string */
            for (int i = 0; i < ctrl[0]; i++) {
                if (oldpos + i >= 0 && oldpos + i < oldsize) {
                    _new[(int) (newpos + i)] += old[(int) (oldpos + i)];
                }
            }

            /* Adjust pointers */
            newpos += ctrl[0];
            oldpos += ctrl[0];

            /* Sanity-check */
            if (newpos + ctrl[1] > newsize) {
                throw new PatchException("Sanity check failed") ;
            }

            /* Read extra string */
            lenread = BZ2_bzRead(epfbz2, _new, newpos, ctrl[1]);
            if (lenread < ctrl[1]) {
                throw new PatchException("Failed to read extra data") ;
            }

            /* Adjust pointers */
            newpos += ctrl[1];
            oldpos += ctrl[2];
        }

        /* Clean up the bzip2 reads */
        cpfbz2.close();
        dpfbz2.close();
        epfbz2.close();

        /* Write the new file */
        // nop

        return _new;
    }

    private long offtin(byte[] header, int offset) {
        long y = 0;

        offset += 7;
        y = header[offset] & 0x7F;
        boolean sign = (header[offset] & 0x80) != 0;
        for (int i = 6; i >= 0; i--) {
            y = y * 256 + toUnsigned(header[--offset]);
        }

        if (sign) {
            y = -y;
        }

        return y;
    }

    /**
     * Decode a BZIP2 data block starting at the given offset.
     *
     * @param data The binary data of the file.
     * @param offset The index where the file begins
     * @param length The length to read. Use -1 to mean "up to the end".
     * @return A new decoded byte array.
     * @throws IOException when the BZIP2 decompression fails.
     */
    private InputStream readBzip2Data(byte[] data, long offset, long length) throws IOException {
        if (length == -1) {
            length = data.length - offset;
        }
        ByteArrayInputStream is = new ByteArrayInputStream(data, (int) offset, (int) length);
        BZip2CompressorInputStream bis = new BZip2CompressorInputStream(is);
        return bis;
    }

    /**
     * Reads the {@code length} next bytes from the bzip2 input stream.
     *
     * @param bzip2is The input stream to read from.
     * @param dest The destination buffer to fill.
     * @param length The length to read in bytes.
     * @return The number of bytes read.
     * @throws IOException If there's not enough data to read.
     */
    private long BZ2_bzRead(InputStream bzip2is, byte[] dest, long offset, long length)
            throws IOException {
        for (long i = 0; i < length; ) {
            int len = bzip2is.read(dest, (int) (offset + i), (int) (length - i));
            if (len == -1) {
                throw new IOException("Bzip2 EOF");
            }
            i += len;
        }
        return length;
    }


    @SuppressWarnings("serial")
    static class PatchException extends Exception {
        public PatchException() {
            super("Corrupt patch");
        }
        public PatchException(String msg) {
            super("Corrupt patch: " + msg);
        }
    }
}

