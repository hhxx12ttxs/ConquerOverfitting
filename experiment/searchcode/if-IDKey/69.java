public UrungrubuItem(CloudEntity ce) {
setUrungrubuCE(ce);

if (urungrubuCE.get(Urungrubu.PROP_IDKEY)!=null)
setIdkey(urungrubuCE.get(Urungrubu.PROP_IDKEY).toString());
if (urungrubuCE.get(Urungrubu.PROP_URUNGRUBU)!=null)

