log.info(&quot;save: stringDomainSetting=&quot;+stringDomainSetting);
if (stringDomainSetting instanceof StringDomainSetting) {
if (findStringDomainSetting(stringDomainSetting.getName(), stringDomainSetting.getSettingDomain().getName()) == null)
getHibernateTemplate().save(stringDomainSetting);
}
if (stringDomainSetting instanceof BooleanDomainSetting) {

