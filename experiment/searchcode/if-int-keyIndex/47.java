package chordSpeaker;

/** 特定の音 */
public enum Key {
C {
@Override
public int keyIndex() {
public int keyIndex(int oct) {
int index = this.keyIndex() + oct * 12;

if (index < 0) {
throw new IllegalArgumentException(&quot;octが小さすぎます&quot;);

