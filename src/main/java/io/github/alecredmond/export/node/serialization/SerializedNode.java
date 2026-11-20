package io.github.alecredmond.export.node.serialization;

import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A {@link Serializable} representation of a {@link Node}, used in file I/O.
 *
 * <p>Serialization flattens a node into the identifiers, names, and probability values needed to
 * reconstruct it. The states and parents of the node are recorded by identifier rather than by
 * reference.
 *
 * @author Alec Redmond
 * @param id the identifier of the node
 * @param name the name of the node
 * @param stateIds the identifiers of the {@link NodeState}s of the node
 * @param stateNames the names of the {@code NodeState}s of the node, in the same order as {@code
 *     stateIds}
 * @param parentIds the identifiers of the parents of the node
 * @param cptProbabilities the probability values from the {@link NetworkTable} of the node
 * @param serializerId the unique identifier of the {@link NodeSerializer} that can deserialize this
 *     instance
 * @see Node
 * @see SerializedBayesianNetwork
 */
public record SerializedNode(
    Serializable id,
    String name,
    Serializable[] stateIds,
    String[] stateNames,
    Serializable[] parentIds,
    double[] cptProbabilities,
    Serializable serializerId)
    implements Serializable {

  @Override
  public boolean equals(Object object) {
    if (!(object
        instanceof
        SerializedNode(
            Serializable id1,
            String name1,
            Serializable[] ids,
            String[] names,
            Serializable[] parentIds1,
            double[] probabilities,
            Serializable serializerId1))) return false;
    return Objects.equals(name, name1)
        && Objects.equals(id, id1)
        && Objects.deepEquals(stateNames, names)
        && Objects.deepEquals(stateIds, ids)
        && Objects.deepEquals(parentIds, parentIds1)
        && Objects.deepEquals(cptProbabilities, probabilities)
        && Objects.equals(serializerId, serializerId1);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        name,
        Arrays.hashCode(stateIds),
        Arrays.hashCode(stateNames),
        Arrays.hashCode(parentIds),
        Arrays.hashCode(cptProbabilities),
        serializerId);
  }
}
