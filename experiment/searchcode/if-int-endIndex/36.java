private long endindex;

public PageIndex(long startindex, long endindex) {
this.startindex = startindex;
this.endindex = endindex;
}

public static PageIndex getPageIndex(long viewpagecount, int currentPage,
long totalpage) {

