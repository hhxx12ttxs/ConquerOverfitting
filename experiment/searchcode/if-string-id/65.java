package com.lob.id;

public abstract class LobId implements StringValued {
protected final String id;

protected LobId(final String id) {
this.id = id;
}

@Override
public String value() {

