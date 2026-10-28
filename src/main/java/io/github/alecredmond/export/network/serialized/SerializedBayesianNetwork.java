package io.github.alecredmond.export.network.serialized;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.node.serialized.SerializedNode;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.serialized.SerializedNetworkTable;
import java.io.Serializable;
import java.util.List;

/**
 * A {@link Serializable} snapshot of a {@link BayesianNetwork}. The serialization process creates a
 * deep copy of the network, replacing all {@link Node} and {@link NodeState} instances with their
 * serializable identifiers. Intended for use in File I/O operations and to save/load the state of
 * the network.
 *
 * @param networkName the name of the {@link BayesianNetwork}.
 * @param serializedNodes a list of serialized {@link Node}s from the network.
 * @param serializedCPTs a list of all serialized {@link NetworkTable}s from the network.
 * @param serializedConstraints a list of serialized {@link ProbabilityConstraint}s within the
 *     network.
 * @see SerializedNode
 * @see SerializedNetworkTable
 * @see SerializedProbabilityConstraint
 * @author Alec Redmond
 */
public record SerializedBayesianNetwork(
    String networkName,
    List<SerializedNode> serializedNodes,
    List<SerializedNetworkTable> serializedCPTs,
    List<SerializedProbabilityConstraint> serializedConstraints)
    implements Serializable {}
