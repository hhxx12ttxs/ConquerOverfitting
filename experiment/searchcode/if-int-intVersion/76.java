if (version != null) {
int intVersion = 0;
try {
intVersion = Integer.parseInt(version);
PackageInfo info = pm.getPackageInfo(getActivity().getPackageName(), 0);
int currentVC = info.versionCode;
if (intVersion > currentVC) {

