package org.meri.matasano.utils;

public class Ascii {

public boolean isCookieCharacter(byte ch) {
return isEnglishCharacter(ch) || ch == &#39;=&#39;;
}

public boolean isUrlCharacter(byte ch) {

