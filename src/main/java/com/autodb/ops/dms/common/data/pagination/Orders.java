package com.autodb.ops.dms.common.data.pagination;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 排序模型
 *
 * @author dongjs
 * @since 2013-1-21
 */
public class Orders implements Serializable {
    private static final long serialVersionUID = 6669355737027817625L;

    public static final String DEFAULT_NAME = "__DEFAULT__";

    /**
     * 排序模型条目
     */
    private List<OrderItem> orders = new ArrayList<OrderItem>();

    public Orders() {
    }

    /**
     * 建立针对一个字段的排序模型，默认降序
     *
     * @param name 字段名称
     */
    public Orders(String name) {
        orders.add(new OrderItem(name));
    }

    /**
     * 建立排序模型
     *
     * @param order 排序方式
     *              false:降序
     *              true:升序
     */
    public Orders(boolean order) {
        orders.add(new OrderItem(DEFAULT_NAME, order));
    }

    /**
     * 建立针对一个字段的排序模型
     *
     * @param name  字段名称
     * @param order 排序方式
     *              false:降序
     *              true:升序
     */
    public Orders(String name, boolean order) {
        orders.add(new OrderItem(name, order));
    }

    /**
     * 添加针对一个字段的排序模型，默认降序
     *
     * @param name 字段名称
     */
    public Orders addOrder(String name) {
        orders.add(new OrderItem(name));
        return this;
    }

    /**
     * 添加针对一个字段的排序模型
     *
     * @param name  字段名称
     * @param order 排序方式
     *              false:降序
     *              true:升序
     */
    public Orders addOrder(String name, boolean order) {
        orders.add(new OrderItem(name, order));
        return this;
    }


    /**
     * @return the orders
     */
    public List<OrderItem> getOrders() {
        return orders;
    }

    /**
     * 排序模型条目
     *
     * @author xiegang
     * @since 2011-12-23
     */
    public class OrderItem implements Serializable {
        private static final long serialVersionUID = -5579367181519701180L;

        /**
         * 默认降序
         */
        public OrderItem(String name) {
            super();
            this.name = name;
        }

        public OrderItem(String name, boolean order) {
            super();
            this.name = name;
            this.order = order;
        }

        /**
         * 排序字段的名称
         */
        private String name;

        /**
         * 标识倒序还是升序 false：降序
         */
        private boolean order;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the order
         */
        public boolean isOrder() {
            return order;
        }

        /**
         * @param order the order to set
         */
        public void setOrder(boolean order) {
            this.order = order;
        }
    }
}
