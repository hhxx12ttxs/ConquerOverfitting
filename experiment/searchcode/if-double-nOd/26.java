nod.setRecTime(recTime);
}
nod.setNo(XmlUtil.getDouble(data, &quot;NOConct&quot;));
nod.setNo2(XmlUtil.getDouble(data, &quot;NO2Conct&quot;));
list.add(nod);
}
if (list.size() > 0) {
gatherNodDAO.batchInsert(list);
deviceRealDAO.saveMany(list);
}
}
}

