StringDomainSetting stringDomainSetting = getDomainSettingFromCache(settingName, domainName);
if (stringDomainSetting != null) {
stringDomainSetting = getDomainSettingFromDAO(settingName, domainName);
if (stringDomainSetting != null) {

