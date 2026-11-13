package io.github.alecredmond.internal.method.cliquegraph.factory.abstractfactory;

import io.github.alecredmond.export.inference.NodeObservation;
import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ObservedTable;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.internal.application.cliquegraph.CliqueData;
import io.github.alecredmond.internal.application.cliquegraph.GraphDataInference;
import io.github.alecredmond.internal.method.cliquegraph.InferenceGraph;
import io.github.alecredmond.internal.method.cliquegraph.externalwriters.InferenceExternalWriter;
import io.github.alecredmond.internal.method.cliquegraph.factory.InferenceGraphFactory;
import io.github.alecredmond.internal.method.cliquegraph.implementation.InferenceGraphImpl;
import io.github.alecredmond.internal.method.inference.ObservableUtils;
import io.github.alecredmond.internal.method.probabilitytables.tablefactory.ObservedTableFactory;
import java.util.Map;

public abstract class AbstractInferenceGraphFactory
    extends AbstractGraphFactoryBase<InferenceGraph, GraphDataInference>
    implements InferenceGraphFactory {

  public InferenceGraph build(BayesianNetwork network) {
    return super.build(network, new GraphDataInference());
  }

  @Override
  protected Map<Clique, CliqueData> commonBuildLogic() {
    buildObservedTables();
    return super.commonBuildLogic();
  }

  @Override
  protected InferenceGraphImpl constructCliqueGraph() {
    return new InferenceGraphImpl(cliqueGraphData, new InferenceExternalWriter(cliqueGraphData));
  }

  @Override
  protected String getAlgorithmType() {
    return "INFERENCE";
  }

  @Override
  protected ProbabilityTable[] getWriteBackTables() {
    return cliqueGraphData.getObservedTables();
  }

  private void buildObservedTables() {
    ObservedTableFactory builder = new ObservedTableFactory();
    BayesianNetwork network = cliqueGraphData.getNetwork();
    Node[] nodes = network.getNodes();
    ObservedTable[] observedTables = new ObservedTable[nodes.length];
    int length = nodes.length;
    for (int i = 0; i < length; i++) {
      observedTables[i] = builder.buildTable(nodes[i]);
    }
    cliqueGraphData.setObservedTables(observedTables);
    Map<Node, NodeObservation> defaultObservations =
        ObservableUtils.buildUnobservedNetwork(network);
    cliqueGraphData.setObservedEvidence(defaultObservations);
    cliqueGraphData.setUnobservedBackup(defaultObservations);
  }
}
