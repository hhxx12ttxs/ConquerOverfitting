long intversion = ServletRequestUtils.getLongParameter(request,
&quot;intversion&quot;, 0);
try {
com.edarong.fileupload.model.File dbFile = null;
if (UPLOAD_STORAGE == UPLOAD_STORAGE_LOCAL) {
dbFile = this.handleUpload(file, request, invoicetype,
invoiceno, intversion, user);
} else if (UPLOAD_STORAGE == UPLOAD_STORAGE_FASTDFS) {
dbFile = this.uploadByFastdfs(file, invoicetype, invoiceno,

