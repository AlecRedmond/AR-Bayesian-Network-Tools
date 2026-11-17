package io.github.alecredmond.export.probabilitytables.tableentry;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * An immutable representation of a single Conditional Probability Table (CPT) entry within a {@link
 * NetworkTable}.
 *
 * @author Alec Redmond
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CptEntry extends TableEntry {
  private final Set<NodeState> conditionStates;
  private final NodeState eventState;

  /**
   * All-args constructor for a {@code CptEntry}.
   *
   * @param conditionStates the conditioning {@link NodeState}s active on this entry.
   * @param eventState the measured {@link NodeState}.
   * @param states the measured states for this entry.
   * @param index the index of the entry in the probability {@code double[]} array of the associated
   *     {@link NetworkTable}.
   * @param probability the probability of {@code eventState}, conditional on {@code
   *     conditionStates}.
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
   * @return the condition {@link NodeState}s.
   */
  public Set<NodeState> conditionStates() {
    return conditionStates;
  }

  /**
   * Returns the event {@link NodeState} measured by this entry.
   *
   * @return the event {@link NodeState}
   */
  public NodeState eventState() {
    return eventState;
  }
}
