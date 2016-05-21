if (!(o instanceof Complex)) {
 *
 * Math is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
return false;
}
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
final Complex complex = (Complex) o;
if (Float.compare(complex.x, x) != 0) {
return false;
}
if (Float.compare(complex.y, y) != 0) {
return false;

