Dimension mapDimension = map.size();
if (rebuildNeeded) {
lastMapSize = Dimension.clone(mapDimension);
recount(iTerminal, mapDimension, terminalDimension);
}

if (lastMapSize == null || !lastMapSize.equals(mapDimension) || lastTerminalSize == null || !lastTerminalSize.equals(terminalDimension)) {

