private ZIndexSorter() {

}

public static ZIndexSorter getInstance() {
if(INSTANCE == null) {
INSTANCE = new ZIndexSorter();
this.sort(pEntities, this.mZIndexComparator);
}

public void sort(final IEntity[] pEntities, final int pStart, final int pEnd) {
this.sort(pEntities, pStart, pEnd, this.mZIndexComparator);

