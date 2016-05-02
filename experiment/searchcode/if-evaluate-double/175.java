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


public class UDFToShort extends UDF {

  private static Log LOG = LogFactory.getLog(UDFToByte.class.getName());

  public UDFToShort() {
  }

  /**
   * Convert from void to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The void value to convert
   * @return Short
   */
  public Short evaluate(Void i)  {
    return null;
  }

  /**
   * Convert from boolean to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The boolean value to convert
   * @return Short
   */
  public Short evaluate(Boolean i)  {
    if (i == null) {
      return null;
    } else {
      return i.booleanValue() ? (short)1 : (short)0;
    }
  }

  /**
   * Convert from byte to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The byte value to convert
   * @return Short
   */
  public Short evaluate(Byte i)  {
    if (i == null) {
      return null;
    } else {
      return Short.valueOf(i.shortValue());
    }
  }

  /**
   * Convert from integer to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The integer value to convert
   * @return Short
   */
  public Short evaluate(Integer i)  {
    if (i == null) {
      return null;
    } else {
      return Short.valueOf(i.shortValue());
    }
  }

  /**
   * Convert from long to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The long value to convert
   * @return Short
   */
  public Short evaluate(Long i)  {
    if (i == null) {
      return null;
    } else {
      return Short.valueOf(i.shortValue());
    }
  }
  
  /**
   * Convert from float to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The float value to convert
   * @return Short
   */
  public Short evaluate(Float i)  {
    if (i == null) {
      return null;
    } else {
      return Short.valueOf(i.shortValue());
    }
  }
  
  /**
   * Convert from double to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The double value to convert
   * @return Short
   */
  public Short evaluate(Double i)  {
    if (i == null) {
      return null;
    } else {
      return Short.valueOf(i.shortValue());
    }
  }
  
  /**
   * Convert from string to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The string value to convert
   * @return Short
   */
  public Short evaluate(String i)  {
    if (i == null) {
      return null;
    } else {
      try {
        return Short.valueOf(i);
      } catch (NumberFormatException e) {
        // MySQL returns 0 if the string is not a well-formed numeric value.
        // return Byte.valueOf(0);
        // But we decided to return NULL instead, which is more conservative.
        return null;
      }
    }
  }
  
  /**
   * Convert from date to a short. This is called for CAST(... AS SMALLINT)
   *
   * @param i The date value to convert
   * @return Short
   */
  public Short evaluate(java.sql.Date i)  {
    if (i == null) {
      return null;
    } else {
        return Long.valueOf(i.getTime()).shortValue();
    }
  }  
}

