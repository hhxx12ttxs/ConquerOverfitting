* Created by Adam Bobowski on 5/16/2015.
*
* Guess what&#39;s that!
*/
public class Range {
private int lower;
private int upper;

public Range(int lower, int upper) {
if(lower > upper) {
throw new IllegalArgumentException();

