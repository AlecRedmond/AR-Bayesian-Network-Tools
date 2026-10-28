package io.github.alecredmond.internal.application.network;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.io.Serializable;
import java.util.*;
import lombok.Data;

/**
 * Container for data used in a {@link BayesianNetwork}.
 *
 * <p>This includes the {@link NetworkTable}s, {@link ProbabilityConstraint}s, and maps linking all
 * declared identifiers to their {@link Node} and {@link NodeState} objects.
 *
 * <p><strong>WARNING:</strong> Modifying this data directly may cause undefined behaviour! The
 * methods provided in {@link BayesianNetwork} should be used to add nodes, modify the network's
 * structure, add constraints, and perform other actions that would modify this data.
 *
 * @see BayesianNetwork
 * @author Alec Redmond
 */
@Data
public class BayesianNetworkData {
  private final String networkName;
  private final Node[] nodes;
  private final NodeState[][] states;
  private final NetworkTable[] networkTables;
  // Indexers
  private final Map<Serializable, Integer> nodeIndexFromId;
  private final Map<String, Integer> nodeIndexFromName;
  private final Map<Serializable, NodeState> nodeStateIDsMap;
  // Constraints
  private final Set<ProbabilityConstraint> constraints;

  public BayesianNetworkData(
      String networkName, Node[] nodes, Set<ProbabilityConstraint> constraints) {
    this.networkName = networkName;
    this.nodes = nodes;
    this.constraints = new LinkedHashSet<>(constraints);
    this.states = new NodeState[nodes.length][];
    this.networkTables = new NetworkTable[nodes.length];
    this.nodeIndexFromId = new HashMap<>();
    this.nodeStateIDsMap = new HashMap<>();
    this.nodeIndexFromName = new HashMap<>();
  }
}
