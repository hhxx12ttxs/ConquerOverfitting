return entity.getZIndex() - entity2.getZIndex();
}
};
}

public static ZIndexSorter getInstance() {
if (ZIndexSorter.INSTANCE == null) {
this.sort(list, this.mZIndexComparator);
}

public void sort(final List<IEntity> list, final int n, final int n2) {

