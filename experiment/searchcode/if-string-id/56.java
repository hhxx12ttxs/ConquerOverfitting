package com.plywet.imeta.repository;

public class StringObjectId implements ObjectId, Comparable<StringObjectId> {
this.id = id;
}

public StringObjectId(ObjectId objectId) {
if (objectId instanceof StringObjectId) {
this.id = ((StringObjectId)objectId).id;

