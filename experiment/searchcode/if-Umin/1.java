/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.politaktiv.captcha;

import java.util.Random;

class RandomUtil {
    int getRandomUnsignedInt(int uMin, int uMax) {
	if (uMin < 0)
	    uMin = 0;
	if (uMax < 0)
	    uMax = 0;
	int iDiff = uMax - uMin;
	if (iDiff <= 0) {
	    uMax = uMin + 1;
	    iDiff = 1;
	}
	Random oRand = new Random();
	return uMin + oRand.nextInt(iDiff + 1);
    }
}

