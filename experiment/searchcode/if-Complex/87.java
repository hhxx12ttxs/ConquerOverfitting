}else{
if(devicePolicyManager.getPasswordQuality(demoDeviceAdmin) != DevicePolicyManager.PASSWORD_QUALITY_COMPLEX){
comply_fac6=false;
}else{
if(devicePolicyManager.getPasswordQuality(demoDeviceAdmin) == DevicePolicyManager.PASSWORD_QUALITY_COMPLEX){
comply_fac6=false;
complex = (String) jobj.get(\"allowSimple\").toString();
if (complex.equals(\"true\")) {
b_complex=true;
if (!jobj.isNull(\"minComplexChars\")
&& jobj.get(\"minComplexChars\") != null) {
if (!b_complex) {
if(IS_ENFORCE){

