/*
 * Copyright 2007 Future Earth, info@future-earth.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package eu.future.earth.gwt.client.utils;

public class General {
  public General() {
    super();
  }

  /**
   * Method Check's if the int Value is even or uneven.
   * @param number - the integer to check.
   * @return boolean - true if even false when uneven
   */
  public static boolean even(int number) {
    boolean result = false;
    if ( (number & 1) != 1) {
      result = true;
    }
    return result;
  }

  /**
   * Method Check's The nuber of time in the value.
   * @param number - the integer to check.
   * @param times - The number is has to be time to be a whole number when devided by.
   * @return int - The number of times this base value fit's
   */
  public static int timesInValue(int value, int base) {
    int count = 0;
    while (value > base) {
      value = value - base;
      count++;
    }
    return count;
  }

  /**
   * Method Check's The number of time in the value. SNaps to the nearest value
   * @param number - the integer to check.
   * @param times - The number is has to be time to be a whole number when devided by.
   * @return int - The number of times this base value fit's
   */
  public static int timesInValueSnap(int value, int base) {
    int count = 0;
    while (value > base) {
      value = value - base;
      count++;
    }
    if ( ( (value * 2) / base) > 0) {
      count++;
    }
    return count;
  }

}

