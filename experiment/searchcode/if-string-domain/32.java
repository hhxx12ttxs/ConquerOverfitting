package com.ipdev.architect;

public final class AppConfig {
public static enum Domain {
PROD,
TEST
}

static boolean initialized_;
static Domain domain_;

public static void setDomain(Domain domain) {

