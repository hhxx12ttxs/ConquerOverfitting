File dir = new File(directory);
this.fileList = dir.listFiles(validFileFilter);

//if we want to recurse, check subfolders for more documents
if (this.recurse) {
this.recurseDirectories(dir.listFiles(directoryFilter));

