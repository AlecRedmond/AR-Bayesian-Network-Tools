package io.github.alecredmond.export.probabilitytables.tableentry;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A single conditional probability table (CPT) entry within a {@link NetworkTable}. In addition to
 * the data of a {@link TableEntry}, a {@code CptEntry} separates its event state from its condition
 * states.
 *
 * @author Alec Redmond
 * @see CptRow
 * @see TableEntry
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CptEntry extends TableEntry {
  private final Set<NodeState> conditionStates;
  private final NodeState eventState;

  /**
   * Constructs an entry from its condition states, event state, measured states, index, and
   * probability.
   *
   * @param conditionStates the conditioning {@link NodeState}s active on this entry
   * @param eventState the measured {@code NodeState}
   * @param states the measured states for this entry
   * @param index the index of the entry in the probability {@code double[]} array of the associated
   *     {@link NetworkTable}
   * @param probability the probability of {@code eventState}, conditional on {@code
   *     conditionStates}
   */
  public CptEntry(
      Set<NodeState> conditionStates,
      NodeState eventState,
      NodeState[] states,
      int index,
      double probability) {
    super(states, index, probability);
    this.conditionStates = conditionStates;
    this.eventState = eventState;
  }

  /**
   * Returns the set of conditioning {@link NodeState}s active on this entry.
   *
   * @return the condition states of this entry
   */
  public Set<NodeState> conditionStates() {
    return conditionStates;
  }

  /**
   * Returns the event {@link NodeState} measured by this entry.
   *
   * @return the event state of this entry
   */
  public NodeState eventState() {
    return eventState;
  }
}
