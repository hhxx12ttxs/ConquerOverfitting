    if(ae == null) return returnComponent;
  public void setValue(Object o) {
    if(o instanceof Icon) {
      super.setIcon((Icon) o);
        (column == TableColumnController.SNIPE_OR_MAX ||
   * Paint a row prior to drawing the components on it.  There are four core
   * paths.  If complex backgrounds are enabled (my hackery from a while ago)
   * then they are rendered.  Otherwise, if it's not a Mac, then the compoent's
    if (ae.isSniped() &&
    column = table.convertColumnIndexToModel(column);
    if(value instanceof Icon) {
      setHorizontalAlignment(SwingConstants.CENTER);
    Object rowData = table.getValueAt(row, -1);
    if(rowData instanceof String) return returnComponent;
    AuctionEntry ae = (AuctionEntry)rowData;

