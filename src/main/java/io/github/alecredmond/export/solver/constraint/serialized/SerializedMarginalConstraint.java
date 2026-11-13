package io.github.alecredmond.export.solver.constraint.serialized;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import java.io.Serializable;

/**
 * A {@link Serializable} representation of a {@link MarginalConstraint}. {@code
 * SerializedProbabilityConstraint} records are flattened to contain only the identifiers of the
 * relevant {@link NodeState}s and the constraint's probability. Compatible with File I/O
 * operations.
 *
 * @param eventStateId the identifier of the measured {@link NodeState}.
 * @param probability the marginal probability of the measured state.
 * @see MarginalConstraint
 * @author Alec Redmond
 */
public record SerializedMarginalConstraint(Serializable eventStateId, double probability)
    implements SerializedProbabilityConstraint {}
