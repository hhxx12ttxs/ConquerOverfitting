package org.apache.lucene.analysis.hi;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.lucene.analysis.util.StemmerUtil.*;

/**
 * Light Stemmer for Hindi.
 * <p>
 * Implements the algorithm specified in:
 * <i>A Lightweight Stemmer for Hindi</i>
 * Ananthakrishnan Ramanathan and Durgesh D Rao.
 * http://computing.open.ac.uk/Sites/EACLSouthAsia/Papers/p6-Ramanathan.pdf
 * </p>
 */
public class HindiStemmer {
  public int stem(char buffer[], int len) {
    // 5
    if ((len > 6) && (endsWith(buffer, len, "?????")
        || endsWith(buffer, len, "?????")
        || endsWith(buffer, len, "?????")
        || endsWith(buffer, len, "?????")
        || endsWith(buffer, len, "?????")
        || endsWith(buffer, len, "?????")
        || endsWith(buffer, len, "?????")
      ))
      return len - 5;
    
    // 4
    if ((len > 5) && (endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        || endsWith(buffer, len, "????")
        ))
      return len - 4;
    
    // 3
    if ((len > 4) && (endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        || endsWith(buffer, len, "???")
        ))
      return len - 3;
    
    // 2
    if ((len > 3) && (endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        || endsWith(buffer, len, "??")
        ))
      return len - 2;
    
    // 1
    if ((len > 2) && (endsWith(buffer, len, "?")
        || endsWith(buffer, len, "?")
        || endsWith(buffer, len, "?")
        || endsWith(buffer, len, "?")
        || endsWith(buffer, len, "?")
        || endsWith(buffer, len, "?")
        || endsWith(buffer, len, "?")
       ))
      return len - 1;
    return len;
  }
}

