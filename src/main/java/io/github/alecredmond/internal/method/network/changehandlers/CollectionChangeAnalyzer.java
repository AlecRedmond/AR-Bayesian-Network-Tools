package io.github.alecredmond.internal.method.network.changehandlers;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class CollectionChangeAnalyzer<T> {
  private Collection<T> oldCollection;
  private Collection<T> newCollection;
  private Set<T> removed;
  private Set<T> added;
  private Set<T> common;
  private Set<T> dupesInNewCollection;
  private boolean collectionIdenticallyOrdered;
  private boolean collectionHasSameElements;

  public CollectionChangeAnalyzer(Collection<T> oldCollection, Collection<T> newCollection) {
    this.oldCollection = oldCollection;
    this.newCollection = newCollection;
    if (oldCollection.equals(newCollection)) {
      identicalCollectionLogic(newCollection);
    } else {
      changedCollectionLogic(oldCollection, newCollection);
    }
  }

  private void identicalCollectionLogic(Collection<T> newCollection) {
    this.added = Set.of();
    this.removed = Set.of();
    this.common = new HashSet<>(newCollection);
    this.dupesInNewCollection = buildDupesInNewCollection(newCollection);
    this.collectionIdenticallyOrdered = true;
    this.collectionHasSameElements = true;
  }

  private void changedCollectionLogic(Collection<T> oldCollection, Collection<T> newCollection) {
    this.removed = new HashSet<>(oldCollection);
    this.added = new HashSet<>(newCollection);
    this.common = removed.stream().filter(added::contains).collect(Collectors.toSet());
    removed.removeAll(common);
    added.removeAll(common);
    this.dupesInNewCollection = buildDupesInNewCollection(newCollection);
    this.collectionHasSameElements = removed.isEmpty() && added.isEmpty();
    this.collectionIdenticallyOrdered = checkIdenticalOrdering();
  }

  private static <T> Set<T> buildDupesInNewCollection(Collection<T> newCollection) {
    return newCollection.stream()
        .filter(t -> Collections.frequency(newCollection, t) > 1)
        .collect(Collectors.toSet());
  }

  private boolean checkIdenticalOrdering() {
    if (!collectionHasSameElements) return false;
    Iterator<T> oci = oldCollection.iterator();
    Iterator<T> nci = newCollection.iterator();
    while (oci.hasNext() && nci.hasNext()) {
      if (!oci.next().equals(nci.next())) return false;
    }
    return !nci.hasNext() && !oci.hasNext();
  }

  @SuppressWarnings("unchecked")
  public static <T> CollectionChangeAnalyzer<T> of(PropertyChangeEvent evt) {
    return new CollectionChangeAnalyzer<>(
        (Collection<T>) evt.getOldValue(), (Collection<T>) evt.getNewValue());
  }
}
