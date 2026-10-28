package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import java.io.Serializable;
import java.util.Map;
import lombok.Data;

@Data
public class SerializationData {
  private final BayesianNetworkData networkData;
  private final Map<Serializable, Node> nodeIdMap;
  private final Map<Serializable, NodeState> nodeStateIdMap;

  public SerializationData() {
    this.networkData = null;
    this.nodeIdMap = null; // TODO - FIX THIS
    this.nodeStateIdMap = null;
  }
}
