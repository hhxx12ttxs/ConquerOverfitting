* @param listKey
* @param idKey
* @param list
* @return
*/
public static Model getValue(String listKey,Object idKey ,List<? extends Model> list){

for(Model m :list){

if(m.get(listKey).equals(idKey))return m;

}


return null;

}



}

