package io.github.alecredmond.export.probabilitytables;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;

/**
 * A {@link ProbabilityTable} which can be deep-copied.
 *
 * @param <T> the type of the {@code CloneableTable}
 * @see NetworkTable
 * @see ObservedTable
 */
public interface CloneableTable<T extends CloneableTable<T>> extends ProbabilityTable {
  /**
   * Builds a copy of this table. All data in the table is deep-copied, except the {@link Node} and
   * {@link NodeState} values, which maintain the original references.
   *
   * @return a copy of the current table.
   */
  T copyTable();
}
