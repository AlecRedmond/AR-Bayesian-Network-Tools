package io.github.alecredmond.export.solver.constraint.serialized;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.ConditionalConstraint;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link Serializable} representation of a {@link ConditionalConstraint}. {@code
 * SerializedProbabilityConstraint} records are flattened to contain only the identifiers of the
 * relevant {@link NodeState}s and the constraint's probability. Compatible with File I/O
 * operations.
 *
 * @param eventStateId the identifier of the measured {@link NodeState}.
 * @param conditionStateIds the identifier of the conditioning {@link NodeState}s.
 * @param probability the conditional probability of the measured state.
 * @see ConditionalConstraint
 * @author Alec Redmond
 */
public record SerializedConditionalConstraint(
    Serializable eventStateId, List<Serializable> conditionStateIds, double probability)
    implements SerializedProbabilityConstraint {}
