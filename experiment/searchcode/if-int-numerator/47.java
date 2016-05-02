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
package cc.plural.math;

public class Fraction {

    int numerator;
    int denominator;

    public Fraction(int n, int d) {
        numerator = n;
        denominator = d;
    }

    public double floatValue() {
        return ((float) numerator) / ((float) denominator);
    }

    public double doubleValue() {
        return ((double) numerator) / ((double) denominator);
    }

    public static Fraction add(Fraction a, Fraction b) {
        if (a.denominator != b.denominator) {
            int aTop = b.denominator * a.numerator;
            int bTop = a.denominator * b.numerator;
            return new Fraction(aTop + bTop, a.denominator * b.denominator);
        } else {
            return new Fraction(a.numerator + b.numerator, a.denominator);
        }
    }

    public static Fraction subtract(Fraction a, Fraction b) {
        if (a.denominator != b.denominator) {
            int aTop = b.denominator * a.numerator;
            int bTop = a.denominator * b.numerator;
            return new Fraction(aTop - bTop, a.denominator + b.denominator);
        } else {
            return new Fraction(a.numerator - b.numerator, a.denominator);
        }
    }

    public static Fraction multiply(Fraction a, Fraction b) {
        return new Fraction(a.numerator * b.numerator, a.denominator * b.denominator);
    }

    public static Fraction divide(Fraction a, Fraction b) {
        return new Fraction(a.numerator * b.denominator, a.denominator * b.numerator);
    }
}

