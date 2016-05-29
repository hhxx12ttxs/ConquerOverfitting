int[] replaceIndexes = commandReplaceIndexMap.get(code.command);
if (replaceIndexes != null) {
for (int replaceIndex : replaceIndexes) {
replace(code, replaceIndex, holder);
}
}
}
}

private void replace(CodeChunk.Code code, int replaceIndex, PositionPlaceholder holder) {

