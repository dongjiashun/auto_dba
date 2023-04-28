package com.autodb.ops.dms.common.data.pagination;

import java.io.Serializable;
import java.util.List;

/**
 * Pagination model Orders model and data list
 *
 * @param <T> data list type
 * @author dongjs
 * @since 2013-1-22
 */
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -2390789109957405179L;

    public Pagination pagination;

    public Orders orders;

    public String search;

    public List<T> data;

    public Object header;

    public Page() {
        super();
    }

    public Page(Pagination pagination) {
        super();
        this.pagination = pagination;
    }

    public Page(Pagination pagination, Orders orders) {
        super();
        this.pagination = pagination;
        this.orders = orders;
    }

    public Page(Pagination pagination, Orders orders, String search) {
        this(pagination, orders);
        this.search = search;
    }

    public Page(List<T> data, Pagination pagination, Orders orders) {
        super();
        this.pagination = pagination;
        this.orders = orders;
        this.data = data;
    }

    public Page(List<T> data, Pagination pagination, Orders orders, String search) {
        this(data, pagination, orders);
        this.search = search;
    }

    /**
     * @return the pagination
     */
    public Pagination getPagination() {
        return pagination;
    }

    /**
     * @return the orders
     */
    public Orders getOrders() {
        return orders;
    }

    /**
     * @return the data
     */
    public List<T> getData() {
        return data;
    }

    public Object getHeader() {
        return header;
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    /**
     * @param orders the orders to set
     */
    public Page<T> setOrders(Orders orders) {
        this.orders = orders;
        return this;
    }

    /**
     * @param pagination the pagination to set
     */
    public Page<T> setPagination(Pagination pagination) {
        this.pagination = pagination;
        return this;
    }

    /**
     * @param data the data to set
     */
    public Page<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
