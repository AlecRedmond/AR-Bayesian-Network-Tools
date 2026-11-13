package io.github.alecredmond.export.solver.constraint.serialized;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.JointProbabilityConstraint;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link Serializable} representation of a {@link JointProbabilityConstraint}. {@code
 * SerializedProbabilityConstraint} records are flattened to contain only the identifiers of the
 * relevant {@link NodeState}s and the constraint's probability. Compatible with File I/O
 * operations.
 *
 * @param eventIds the identifiers of the measured {@link NodeState}s.
 * @param conditionIds the identifiers of the conditioning {@link NodeState}s.
 * @param probability the conditional joint probability of the measured states.
 * @see JointProbabilityConstraint
 * @author Alec Redmond
 */
public record SerializedJointProbabilityConstraint(
    List<Serializable> eventIds, List<Serializable> conditionIds, double probability)
    implements SerializedProbabilityConstraint {}
