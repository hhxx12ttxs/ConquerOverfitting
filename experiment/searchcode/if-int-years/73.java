yearsZb.add(0.1);
chZbs.put(CompanyType.DLGS, yearsZb);
}

public Double getYszb(int year, Company comp){
if (ysZbs.containsKey(comp.getType())){
public Double getChzb(int year, Company comp){
if (chZbs.containsKey(comp.getType())){
if ((year - baseYear) < chZbs.get(comp.getType()).size()){

