package com.kudlaienko.parser.engine.xml.parser.enums;

public enum Bracket {
OPEN(&quot;<&quot;), CLOSE(&quot;>&quot;), AUTO_CLOSE(&quot;/>&quot;);
for (Bracket bracket : Bracket.values()) {
if (bracket.getValue().equals(value)) {

