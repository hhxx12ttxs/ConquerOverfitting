public TbBranchschool(TbTown tbTown, TbSchool tbSchool, String braschName,
String braschMinName, String braschAddress, Double longitude,Double latitude,String braschPhone,
DataBaseDaoImpl daoImpl = new DataBaseDaoImpl();
List<TbUserinfo> list = daoImpl.getObjects(TbUserinfo.class, map);

if(list==null||list.isEmpty()||list.get(0)==null)

