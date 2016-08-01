package com.pokescanner.helper;

import java.util.AbstractList;
import java.util.List;

/**
 * Created by Brian on 7/31/2016.
 */
public class MyPartition {
    public static <T> List<List<T>> partition(List<T> list, int size) {

        if (list == null)
            throw new NullPointerException("'list' must not be null");
        if (!(size > 0))
            throw new IllegalArgumentException("'size' must be greater than 0");

        return new Partition<T>(list, size);
    }

    private static class Partition<T> extends AbstractList<List<T>> {

        final List<T> list;
        final int size;

        Partition(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(int index) {
            int listSize = size();
            if (listSize < 0)
                throw new IllegalArgumentException("negative size: " + listSize);
            if (index < 0)
                throw new IndexOutOfBoundsException("index " + index + " must not be negative");
            if (index >= listSize)
                throw new IndexOutOfBoundsException("index " + index + " must be less than size " + listSize);
            int start = index * size;
            int end = Math.min(start + size, list.size());
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return (list.size() + size - 1) / size;
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }
    }


}
