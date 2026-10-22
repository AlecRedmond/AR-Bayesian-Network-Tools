package io.github.alecredmond.internal.method.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class CollectionToString {
  private CollectionToString() {
    /* This utility class should not be instantiated */
  }

  public static <T> String apply(T[] array) {
    if (array.length == 0) return "";
    return stringFromIterator(Arrays.stream(array).iterator());
  }

  private static <T> String stringFromIterator(Iterator<T> iterator) {
    StringBuilder sb = new StringBuilder();
    while (iterator.hasNext()) {
      sb.append(iterator.next().toString());
      if (iterator.hasNext()) sb.append(", ");
    }
    return sb.toString();
  }

  public static <T> String apply(Collection<T> collection) {
    if (collection.isEmpty()) return "";
    return stringFromIterator(collection.iterator());
  }
}
