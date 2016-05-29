package com.hackerrank;

import java.util.Scanner;

public class AlternatingCharacter {

public static void main(String[] args) {
String str= in.next();
int noDeletion=0;
char lastchar=str.charAt(0);
for(int i=1;i<str.length();i++){

if(str.charAt(i)==lastchar)

