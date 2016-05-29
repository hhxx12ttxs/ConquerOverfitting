String normalizePath2 = normalizePath(cookie);
if (!normalizePath.equals(normalizePath2)) {
if (normalizePath.startsWith(normalizePath2)) {
return -1;

