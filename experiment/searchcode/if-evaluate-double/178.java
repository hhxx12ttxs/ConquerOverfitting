/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.udf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.exec.UDF;


public class UDFToFloat extends UDF {

  private static Log LOG = LogFactory.getLog(UDFToFloat.class.getName());

  public UDFToFloat() {
  }

  /**
   * Convert from void to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The void value to convert
   * @return Float
   */
  public Float evaluate(Void i)  {
    return null;
  }

  /**
   * Convert from boolean to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The boolean value to convert
   * @return Float
   */
  public Float evaluate(Boolean i)  {
    if (i == null) {
      return null;
    } else {
      return i.booleanValue() ? (float)1.0 : (float)0.0;
    }
  }
  
  /**
   * Convert from byte to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The byte value to convert
   * @return Float
   */
  public Float evaluate(Byte i)  {
    if (i == null) {
      return null;
    } else {
      return Float.valueOf(i.floatValue());
    }
  }
  
  /**
   * Convert from short to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The short value to convert
   * @return Float
   */
  public Float evaluate(Short i)  {
    if (i == null) {
      return null;
    } else {
      return Float.valueOf(i.floatValue());
    }
  }
  
  /**
   * Convert from integer to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The integer value to convert
   * @return Float
   */
  public Float evaluate(Integer i)  {
    if (i == null) {
      return null;
    } else {
      return Float.valueOf(i.floatValue());
    }
  }
  
  /**
   * Convert from long to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The long value to convert
   * @return Float
   */
  public Float evaluate(Long i)  {
    if (i == null) {
      return null;
    } else {
      return Float.valueOf(i.floatValue());
    }
  }

  /**
   * Convert from double to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The double value to convert
   * @return Float
   */  
  public Float evaluate(Double i)  {
    if (i == null) {
      return null;
    } else {
      return Float.valueOf(i.floatValue());
    }
  }
  
  /**
   * Convert from string to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The string value to convert
   * @return Float
   */
  public Float evaluate(String i)  {
    if (i == null) {
      return null;
    } else {
      // MySQL returns 0 if the string is not a well-formed numeric value.
      // return Float.valueOf(0);
      // But we decided to return NULL instead, which is more conservative.
      return null;
    }
  }
  
  /**
   * Convert from date to a float. This is called for CAST(... AS FLOAT)
   *
   * @param i The date value to convert
   * @return Float
   */
  public Float evaluate(java.sql.Date i)  {
    if (i == null) {
      return null;
    } else {
        return Float.valueOf(i.getTime());
    }
  }  
}

