StyleEntry styleEntry = (StyleEntry) i.next();
if (styleEntry.isFullyEnabled())
styleEntry.getStyle().paint(geom, viewport, g);
private StyleEntry getEntry(Style style)
{
int index = getEntryIndex(style);
if (index < 0) return null;

