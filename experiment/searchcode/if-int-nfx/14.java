public static boolean isNewVersion(){
boolean is = false;
try {
if(Integer.parseInt(Downloader.readFileFtp(cfg.getValue(&quot;DEFAULT_MP&quot;))) > Integer.parseInt(Downloader.readLocalVer())){
File[] files = directory.listFiles();
if(null!=files){
for(int i=0; i<files.length; i++) {

