public boolean crearTbTipoAtributo(String cTipoAtributo, String dTipoAtributo, Date fIngreso){
if(daoTbTipoAtributo.daTbTipoAtributoById(cTipoAtributo)==null){
return true;
}
else
return false;
}

public boolean eliminarTbTipoAtributo(String cTipoAtributo){
if(daoTbTipoAtributo.daTbTipoAtributoById(cTipoAtributo)!=null){

