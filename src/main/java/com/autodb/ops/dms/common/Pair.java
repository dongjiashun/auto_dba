package com.autodb.ops.dms.common;

/**
 * 一对不可变的对象
 *
 * @param <L>
 * @param <R>
 * @author dongjs
 * @since 2013-3-27
 */
public class Pair<L, R> {
    private final L left;
    private final R right;

    private volatile String toStringResult;

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        final Pair pair = (Pair) o;
        return !(left != null ? !left.equals(pair.left) : pair.left != null)
                && !(right != null ? !right.equals(pair.right) : pair.right != null);
    }

    @Override
    public int hashCode() {
        int result = left != null ? left.hashCode() : 0;
        result = 31 * result + (right != null ? right.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (toStringResult == null) {
            toStringResult = "Pair{" + "left=" + left + ", right=" + right + '}';
        }
        return toStringResult;
    }
}