package io.github.alecredmond.internal.method.network;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilderNode;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.probabilitytables.probabilityvector.ProbabilityVectorFactory;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.ConstraintBuilderIterator;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkInputBuilder {
  private final ProbabilityVectorFactory vectorFactory = new ProbabilityVectorFactory();
  private List<BayesianNetworkBuilderNode<?>> nodeInputs;
  private BayesianNetwork bayesianNetwork;

  public BayesianNetwork buildNetwork(
      String networkName, List<BayesianNetworkBuilderNode<?>> nodeInputs) {
    this.nodeInputs = nodeInputs;
    this.bayesianNetwork = BayesianNetwork.newNetwork(networkName);
    addAllNodes();
    parentNodes();
    createCptConstraints();
    return bayesianNetwork;
  }

  private void addAllNodes() {
    nodeInputs.forEach(
        nodeInput -> bayesianNetwork.addNewNode(nodeInput.getNodeId(), nodeInput.getStateIds()));
  }

  private void parentNodes() {
    nodeInputs.stream()
        .filter(ni -> ni.getParentNodeIds() != null)
        .forEach(ni -> bayesianNetwork.addParents(ni.getNodeId(), ni.getParentNodeIds()));
  }

  private void createCptConstraints() {
    nodeInputs.parallelStream()
        .filter(ni -> ni.getCptValues() != null)
        .map(this::buildConstraintBuilderIterator)
        .map(ConstraintBuilderIterator::buildConstraints)
        .flatMap(Collection::stream)
        .sequential()
        .forEach(bayesianNetwork.getNetworkData().getConstraints()::add);
  }

  private ConstraintBuilderIterator buildConstraintBuilderIterator(
      BayesianNetworkBuilderNode<?> nodeInput) {
    ProbabilityVector vector = buildCPTInputVector(nodeInput);
    Node event = bayesianNetwork.getNode(nodeInput.getNodeId());
    return new ConstraintBuilderIterator(event, vector);
  }

  private ProbabilityVector buildCPTInputVector(BayesianNetworkBuilderNode<?> nodeInput) {
    ProbabilityVector vector = vectorFactory.build(convertNodeOrderIdsToNodes(nodeInput));
    double[] cptValues = nodeInput.getCptValues();
    validateArrayLengths(cptValues, vector);
    System.arraycopy(cptValues, 0, vector.getProbabilities(), 0, cptValues.length);
    return vector;
  }

  private List<Node> convertNodeOrderIdsToNodes(BayesianNetworkBuilderNode<?> nodeInput) {
    return nodeInput.getCptNodeOrder().stream().map(bayesianNetwork::getNode).toList();
  }

  private static void validateArrayLengths(double[] cptValues, ProbabilityVector vector) {
    int vectorLength = vector.getProbabilities().length;
    if (cptValues.length != vectorLength) {
      throw new ConstraintValidationException(
          "CPT input for Nodes [%s] requires array length %d, but was length %d."
              .formatted(
                  CollectionToString.apply(vector.getOrderedNodes()),
                  vectorLength,
                  cptValues.length));
    }
  }
}
