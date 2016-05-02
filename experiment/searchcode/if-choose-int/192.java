/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.terramagnet.pager;

/**
 * 应用于Oracle数据库的分页查询器.
 *
 * @see #wrapSql(java.lang.String)
 * @author LEE
 */
public class OraclePager<E> extends JdbcPagerResult<E> {

    private static final String[] template = {"select * from (select row_.*, rownum rownum_ from (", ") row_ where rownum <= ", " ) where rownum_ >= "};

    /**
     * 创建分页器
     *
     * @param page 当前页（必须是自然数，否则认为该分页器将不执行分页功能。）
     * @param size 每页的数据量（若<code>page</code>不是自然数，该参数无效。）
     */
    public OraclePager(int page, int size) {
        if (page > 0) {
            setPageSize(size);
            setCurrentPage(page);
        } else {
            setPageAvailable(false);
        }
    }

    /**
     * 创建分页器 <p>使用{@link JdbcPagerResult#DEFAULT_PAGESIZE 默认分页大小}创建分页器</p>
     *
     * @param page 当前页（必须是自然数，否则认为该分页器将不执行分页功能。）
     */
    public OraclePager(int page) {
        if (page > 0) {
            setPageSize(DEFAULT_PAGESIZE);
            setCurrentPage(page);
        } else {
            setPageAvailable(false);
        }
    }

    /**
     * 包装SQL查询语句. <p>包装格式如下：
     * <pre>select * from (select row_.*, rownum rownum_ from ( <code style="color:#ff6666">${selectSql}</code>
     * ) row_ where rownum <= <code style="color:#ff6666">${max}</code> ) where
     * rownum_ >= <code style="color:#ff6666">${min}</code></pre></p>
     */
    @Override
    public String wrapSql(String selectSql) {
        if (isResultPaginated()) {
            int max = getCurrentPage() * getPageSize();
            int min = max - getPageSize() + 1;
            StringBuilder sb = new StringBuilder();
            sb.append(template[0]).append(selectSql).append(template[1]).append(max).append(template[2]).append(min);
            return sb.toString();
        } else {
            return selectSql;
        }
    }

    /**
     * {@inheritDoc }
     *
     * <p>按照{@code Oracle}数据库分页查询的语法来包装语句。</p>
     *
     * @see #wrapSql(java.lang.String) 以Oracle数据库分页查询的语法来包装SQL查询
     */
    @Override
    public StringBuilder wrapSql(StringBuilder selectSql) {
        if (isResultPaginated()) {
            int max = getCurrentPage() * getPageSize();
            int min = max - getPageSize() + 1;
            selectSql.insert(0, template[0]).append(template[1]).append(max).append(template[2]).append(min);
        }
        return selectSql;
    }
}

