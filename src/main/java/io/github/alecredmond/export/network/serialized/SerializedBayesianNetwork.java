package io.github.alecredmond.export.network.serialized;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialization.SerializedNode;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import java.io.Serializable;

/**
 * A {@link Serializable} snapshot of a {@link BayesianNetwork}.
 *
 * <p>The serialization process creates a deep copy of the network, replacing all {@link Node} and
 * {@link NodeState} instances with their serializable identifiers. A snapshot is intended for file
 * I/O, to save and load the state of a network.
 *
 * @author Alec Redmond
 * @param networkName the name of the network
 * @param serializedNodes the serialized form of each {@code Node} in the network
 * @param serializedConstraints the serialized form of each {@link ProbabilityConstraint} in the
 *     network
 * @see BayesianNetwork#serializeNetwork()
 * @see BayesianNetwork#loadNetwork(SerializedBayesianNetwork)
 * @see SerializedNode
 * @see SerializedProbabilityConstraint
 */
public record SerializedBayesianNetwork(
    String networkName,
    SerializedNode[] serializedNodes,
    SerializedProbabilityConstraint[] serializedConstraints)
    implements Serializable {}
