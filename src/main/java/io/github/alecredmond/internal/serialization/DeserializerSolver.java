package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.exceptions.NetworkStructureException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.serialization.SerializedNode;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.BayesSolver;
import java.util.Optional;

public class DeserializerSolver implements BayesSolver {
  private final SerializedBayesianNetwork sbn;

  public DeserializerSolver(SerializedBayesianNetwork sbn) {
    this.sbn = sbn;
  }

  @Override
  public Optional<BayesianNetwork> solveNetwork(BayesianNetwork bayesianNetwork) {
    SerializedNode[] serializedNodes = sbn.serializedNodes();
    NetworkTable[] networkTables = bayesianNetwork.getNetworkTables();
    final int len = serializedNodes.length;
    for (int i = 0; i < len; i++) {
      SerializedNode serializedNode = serializedNodes[i];
      NetworkTable networkTable = networkTables[i];
      assertNodeIsTheSame(serializedNode, networkTable.getNetworkNode());
      double[] src = serializedNode.cptProbabilities();
      double[] dest = networkTable.getProbabilities();
      System.arraycopy(src, 0, dest, 0, src.length);
    }
    return Optional.of(bayesianNetwork);
  }

  private void assertNodeIsTheSame(SerializedNode serializedNode, Node networkNode) {
    if (!serializedNode.id().equals(networkNode.getId())
        || !serializedNode.name().equals(networkNode.getName())) {
      throw new NetworkStructureException("Deserialization failed: nodes out of order!");
    }
  }
}
