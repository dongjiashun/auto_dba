package com.autodb.ops.dms.common.data.pagination;

import java.io.Serializable;

/**
 * 分页模型,需要设置每一页的数据量和总数据量后即可使用<br/>
 * 每一页的数据量可以设置也可以使用默认值
 *
 * @author dongjs
 * @since 2013-1-21
 */
public class Pagination implements Serializable {
    private static final long serialVersionUID = -8343504471038714748L;

    /** 默认每一页的数据量 **/
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大一页数据量 **/
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 数据其实偏移位置
     */
    private int start;

    /**
     * 每一页的数据量
     */
    private int pageSize;

    /**
     * 总数据数量
     */
    private int rowCount;

    /**
     * 当前页数
     */
    private int currentPage;

    /**
     * 总页数
     */
    private int totalPage;

    public Pagination() {
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.start = 0;
        this.totalPage = 1;
        this.currentPage = 1;
    }

    public int getStart() {
        return this.start;
    }

    /**
     * 获取当前页数,过滤小于1的情况
     */
    public int getCurrentPage() {
        if (this.currentPage < 1) {
            this.currentPage = 1;
        }
        // int totalPage = getTotalPage();
        // if (this.currentPage > totalPage) {
        //     this.currentPage = totalPage;
        // }
        return this.currentPage;
    }

    /**
     * 每一页数据量
     */
    public int getPageSize() {
        if (this.pageSize < 1) {
            this.pageSize = DEFAULT_PAGE_SIZE;
        } else if (this.pageSize > MAX_PAGE_SIZE) {
            this.pageSize = MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /**
     * 总数据条数
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * 总页数
     */
    public int getTotalPage() {
        totalPage = (this.rowCount - this.start) / this.getPageSize()
                + ((this.rowCount - this.start) % this.getPageSize() == 0 ? 0 : 1);
        totalPage = totalPage < 1 ? 1 : totalPage;

        return this.totalPage;
    }

    /**
     * 获取当前页数据库的偏移值
     */
    public int getOffset() {
        return this.start + (this.getCurrentPage() - 1) * this.getPageSize();
    }

    /**
     * 获取当前页数据库的limit值
     */
    public int getLimit() {
        return this.getPageSize();
    }

    /**
     * @return the hasNext
     */
    public boolean hasNext() {
        return this.getCurrentPage() < this.getTotalPage();
    }

    /**
     * @return the hasPrev
     */
    public boolean hasPrev() {
        return this.getCurrentPage() > 1;
    }

    // setting...

    /**
     * 设置初始偏移
     */
    public Pagination setStart(int start) {
        this.start = start;
        return this;
    }

    /**
     * 设置当前页数
     */
    public Pagination setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        return this;
    }

    /**
     * 设置总数据量的时候，初始化总页数，如果每页数据量没有设定就取默认值10
     */
    public Pagination setRowCount(int rowCount) {
        this.rowCount = rowCount;
        return this;
    }

    /**
     * 每一页数据量
     */
    public Pagination setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pagination{");
        sb.append("start=").append(start);
        sb.append(", pageSize=").append(pageSize);
        sb.append(", rowCount=").append(rowCount);
        sb.append(", currentPage=").append(currentPage);
        sb.append(", totalPage=").append(totalPage);
        sb.append('}');
        return sb.toString();
    }
}
