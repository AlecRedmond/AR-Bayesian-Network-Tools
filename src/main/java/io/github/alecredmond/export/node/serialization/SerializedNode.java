package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.io.Serializable;

/**
 * A {@link Serializable} representation of a {@link Node}. The serialization process flattens the
 * {@link Node} to its constituent identifiers. Used in File I/O operations.
 *
 * @param id the identifier of the {@link Node}.
 * @param name the name of the {@link Node}
 * @param stateIds the identifiers of the {@link Node}'s {@link NodeState}s.
 * @param stateNames the names of the {@link Node}'s {@link NodeState}s.
 * @param parentIds the identifiers of the {@link Node}'s parents.
 * @param cptProbabilities the probability values from the {@link Node}'s {@link NetworkTable}
 * @param serializerId the unique identifier for the {@link NodeSerializer} which can deserialize
 *     this instance.
 * @see Node
 * @see SerializedBayesianNetwork
 * @author Alec Redmond
 */
public record SerializedNode(
    Serializable id,
    String name,
    Serializable[] stateIds,
    String[] stateNames,
    Serializable[] parentIds,
    double[] cptProbabilities,
    Serializable serializerId)
    implements Serializable {}
