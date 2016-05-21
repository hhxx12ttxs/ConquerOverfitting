  /**
   * Determines if a simple ID is present.
   *
   * @return true if the key is not a complex ID and at least one field was annotated with @Id.
   */
  public boolean isSimpleIdPresent() {
    return !isComplexKey() && !idPropertyMap.isEmpty();
   * @return true if complex, false otherwise
      throw new IllegalArgumentException(\"Primary Key Class cannot be null\");
    this.pkClazz = pkClazz;
  }
  /**
   * Determines if the key is complex (IdClass, Embedded, etc) or a simple one field type.
   *
    if (null == this.pkClazz) {
   */

