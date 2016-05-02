/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terramagnet.pager;

import java.util.List;

/**
 * 用来封装分页查询结果的模型.
 * <p>提供了生成sql语句的能力. </p>
 * <p>可以选择启用分页或不启用分页：{@link #setPageAvailable(boolean) }</p>
 *
 * @see #wrapSql(java.lang.String) 生成sql语句
 * @see #setResult(int, java.util.List) 设置查询结果
 * @author LEE
 */
public abstract class JdbcPagerResult<E> implements Pager {
    //------------------------------------------------------------------------

    private boolean paginated = true;
    private int pageSize;
    private int totalSize;
    private int currentPage;
    private List<E> result;

    /**
     * 设置查询结果.
     *
     * 一般由DAO层在执行数据库查询后调用，可以将数据保存到分页器的结果中。
     *
     * @param totalSize 查询结果不分页时的总数据量（若该分页器工作在不分页模式下，此参数无效。）
     * @param result 某一页的数据（若该分页器工作在不分页模式下，此参数即为全部查询结果。）
     * @see #setPageAvailable(boolean)
     */
    public void setResult(int totalSize, List<E> result) {
        this.totalSize = totalSize;
        this.result = result;
    }

    @Override
    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * 查询结果.
     *
     * <p>当处于分页模式时（默认）返回{@link #getCurrentPage() 当前页}的数据；<br/>当处于不分页模式时返回全部查询结果</p>
     *
     * @return 查询结果
     * @see #setPageAvailable(boolean)
     * @see #setResult(int, java.util.List)
     */
    public List<E> getResult() {
        return result;
    }

    /**
     * 若不分页返回的结果集大小 <p><strong>当处于不分页模式下时返回-1</strong></p>
     *
     * @return 若不分页返回的结果集大小
     */
    @Override
    public int getTotalSize() {
        return paginated ? totalSize : -1;
    }

    /**
     * 设置当前页码. 当分页器不处于分页模式时，此方法是无效的。
     *
     * @param pageIndex 当前页
     */
    public void setCurrentPage(int pageIndex) {
        if (currentPage > 1) {
            this.currentPage = pageIndex;
        }
    }

    /**
     * 返回当前页码 <p><strong>当处于不分页模式下时返回-1</strong></p>
     *
     * @return 若不分页返回当前页码
     */
    @Override
    public int getCurrentPage() {
        return paginated ? currentPage : -1;
    }

    /**
     * 设置每页的数据量. 当分页器不处于分页模式时，此方法是无效的。
     *
     * @param size 每页显示的数据量
     */
    public void setPageSize(int size) {
        this.pageSize = size;
    }

    /**
     * 每页的数据量. <strong>当处于不分页模式下时返回-1</strong>
     *
     * @return 若不分页返回每页的数据量
     */
    @Override
    public int getPageSize() {
        return paginated ? pageSize : -1;
    }

    @Override
    public int getFirstIndex() {
        return (getCurrentPage() - 1) * getPageSize();
    }

    /**
     * 该分页器的结果是否带有分页信息 <p>即该分页器是否工作在分页模式，默认返回<code>true</code>。</p>
     *
     * @return
     * <ul><li><code>true</code>——表示其结果是按照分页信息进行分页的</li><li><code>false</code>——表示其结果不带有分页信息</li></ul>
     * @see #setPageAvailable(boolean) 设置分页器的工作模式
     */
    public boolean isResultPaginated() {
        return paginated;
    }

    /**
     * 设置分页器的工作模式
     *
     * @param pageAvailable <code>true</code>分页； <code>false</code>不分页
     */
    protected void setPageAvailable(boolean pageAvailable) {
        paginated = pageAvailable;
    }

    /**
     * 返回JSON字符串以方便浏览器段生成分页信息
     * <p><strong>当处于不分页模式下时返回<code>null</code></strong></p>
     * <p>当处于分页模式下时返回的JSON数据包含了源数据总数（totalSize）、每页数据量（pageSize）以及当前页码（currentPage）信息。</p>
     *
     * @return JSON字符串<strong>（不包含数据结果集）</strong>
     */
    @Override
    public String toJson() {
        if (paginated) {
            return "{totalSize:" + totalSize + ",pageSize:" + pageSize + ",currentPage:" + currentPage + "}";
        } else {
            return null;
        }
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

    /**
     * 包装<code>SQL</code>查询语句. <p>根据不同的数据库生成最有效率的分页查询语句</p>
     *
     * @param selectSql <code>SQL</code>查询语句
     * @return 分页查询语句<strong>（当处于不分页模式下时直接返回<code>selectSql</code>）</strong>
     */
    public abstract String wrapSql(String selectSql);

    /**
     * 包装{@code SQL}查询语句. <p>根据不同的数据库生成最有效率的分页查询语句</p>
     * <p>直接在参数上进行修改，然会返回该参数。当处于不分页模式下时直接返回{@code selectSql}。</p>
     *
     * @param selectSql <code>SQL</code>查询语句
     * @return 分页后查询语句
     */
    public abstract StringBuilder wrapSql(StringBuilder selectSql);
}

