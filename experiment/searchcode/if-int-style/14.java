package com.ej.enums;

public class Styles {

public static int STYLE_BOLD = 1 << 0; // 1
public static int STYLE_UNDERLINE = 1 << 1; // 2
public static int STYLE_WIDE = 1 << 3; // 8

public static void apply(int styles) {
if ((styles &amp; STYLE_BOLD) == STYLE_BOLD) {

