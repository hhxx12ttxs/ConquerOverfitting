package utils;

public class HashFunctions {

public static int hash(byte[] bytes, int k) {
int len = bytes.length;
for (int i = 0; i < len; i++) {
if ((i &amp; 1) == 0) {
hash ^= ((hash << 7) ^ bytes[i] ^ (hash >> 3));

