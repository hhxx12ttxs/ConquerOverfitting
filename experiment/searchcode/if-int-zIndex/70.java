private ZIndexSorter() {

}

public static ZIndexSorter getInstance() {
if (INSTANCE == null) {
this.sort(pEntities, this.mZIndexComparator);
}

public void sort(final IEntity[] pEntities, final int pStart, final int pEnd) {

