if (other.map != null) {
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
public boolean equals(final Object obj) {
if (this == obj) {
return true;
if (map == null) {
 * * Redistributions of source code must retain the above copyright
return false;
}
if (complex == null) {
if (other.complex != null) {
return false;
}
} else if (!complex.equals(other.complex)) {
return false;

