    column = table.convertColumnIndexToModel(column);
    if(value instanceof Icon) {
      setHorizontalAlignment(SwingConstants.CENTER);
      boolean painted = false;
      if(JConfig.queryDisplayProperty(\"background.complex\", \"false\").equals(\"true\")) {
        drawComplexBackground(g);
    Object rowData = table.getValueAt(row, -1);
    if(rowData instanceof String) return returnComponent;
    AuctionEntry ae = (AuctionEntry)rowData;
  public void setValue(Object o) {
    if(o instanceof Icon) {
      super.setIcon((Icon) o);
   * Paint a row prior to drawing the components on it.  There are four core
   * paths.  If complex backgrounds are enabled (my hackery from a while ago)
   * then they are rendered.  Otherwise, if it's not a Mac, then the compoent's

