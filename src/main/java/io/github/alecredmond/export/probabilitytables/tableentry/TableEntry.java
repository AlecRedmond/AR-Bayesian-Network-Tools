package io.github.alecredmond.export.probabilitytables.tableentry;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An individual entry in a {@link ProbabilityTable}. A {@code TableEntry} contains the measured
 * {@link NodeState} values, the index of the entry, and the probability value measured.
 *
 * @author Alec Redmond
 * @see CptEntry
 * @see ProbabilityTable
 */
@EqualsAndHashCode
@ToString
public class TableEntry {
  private final NodeState[] states;
  private final int index;
  private final double probability;

  /**
   * Constructs an entry from its measured states, index, and probability.
   *
   * @param states the measured states for this entry
   * @param index the index of the entry in the probability {@code double[]} array of the associated
   *     {@link ProbabilityTable}
   * @param probability the probability of the measured states
   */
  public TableEntry(NodeState[] states, int index, double probability) {
    this.states = states;
    this.index = index;
    this.probability = probability;
  }

  /**
   * Returns the measured {@link NodeState}s for this entry.
   *
   * @return the {@code NodeState}s measured in this entry
   */
  public NodeState[] states() {
    return states;
  }

  /**
   * Returns the index of this {@code TableEntry}. This is equal to the index of the probability in
   * the probability array of the measured {@link ProbabilityTable}.
   *
   * @return the index of this entry
   */
  public int index() {
    return index;
  }

  /**
   * Returns the probability of the measured {@link NodeState}s.
   *
   * @return the probability of this {@code TableEntry}
   */
  public double probability() {
    return probability;
  }
}
