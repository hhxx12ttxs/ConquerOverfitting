/**
 * Copyright (C) 2012 J.W.Marsden <jmarsden@plural.cc>
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cc.plural.math.statistic;

import java.lang.reflect.Array;

/**
 * Standard Deviation Utility
 *
 * @author j.w.marsden@gmail.com
 */
public class StandardDeviation {

    public static Double evaluate(Double[] data) {
        return StandardDeviation.evaluate(data, true);
    }

    public static Double evaluate(Double[] data, boolean partialPopulation) {
        if(data==null) {
            throw new NullPointerException();
        }
        int length = Array.getLength(data);
        if(length < 2) {
            throw new RuntimeException("More than two values are required to calculate a standard deviation");
        }
        double mean = Mean.evaluate(data);
        double sumDifferenceSquared = 0;
        for(int i=0;i<length;i++) {
            sumDifferenceSquared += Math.pow(data[i]-mean,2);
        }
        return Math.sqrt(sumDifferenceSquared/((partialPopulation) ? (length-1) : (length)));
    }
}
