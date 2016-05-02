package org.terramagnet.pager;

/**
 * JavaBean实现的分页器. 用来封装分页信息。
 *
 * @author LEE
 */
public class DefaultPager implements Pager {

    private static DefaultPager instance;

    /**
     * 返回一个全局唯一的分页器实例.
     *
     * @return 全局唯一的实例
     */
    public static DefaultPager singleton() {
        if (instance == null) {
            instance = new DefaultPager();
        }
        return instance;
    }
    private int currentPage = 1;
    private int pageSize = DEFAULT_PAGESIZE;
    private int totalSize;

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage > 1) {
            this.currentPage = currentPage;
        }
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize > 1) {
            this.pageSize = pageSize;
        }
    }

    @Override
    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public int getFirstIndex() {
        return (currentPage - 1) * pageSize;
    }

    @Override
    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public String toJson() {
        return "{totalSize:" + totalSize + ",pageSize:" + pageSize + ",currentPage:" + currentPage + "}";
    }

    /**
     * {@inheritDoc }. 与{@link #toJson() }等价
     *
     * @return JSON字符串
     */
    @Override
    public String toString() {
        return toJson();
    }
}

