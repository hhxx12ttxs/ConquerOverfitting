package com.runescape.client.util.node;

public class Node {

public long id;
public Node prev;
public Node next;

public Node() {
}

public final void unlink() {
if (next != null) {

