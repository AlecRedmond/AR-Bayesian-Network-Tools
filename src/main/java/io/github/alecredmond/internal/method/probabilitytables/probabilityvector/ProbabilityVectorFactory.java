package io.github.alecredmond.internal.method.probabilitytables.probabilityvector;

import io.github.alecredmond.exceptions.ProbabilityVectorFactoryException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.method.junctiontree.treebuilding.TreewidthValidator;
import io.github.alecredmond.internal.method.node.NodeUtils;
import java.util.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class ProbabilityVectorFactory {

  public ProbabilityVector build(List<Node> nodes) {
    validateVectorLength(nodes);
    Node[] nodeArray = nodes.toArray(Node[]::new);
    int[] numberOfStates = new int[nodeArray.length];
    int[] strideLengths = new int[nodeArray.length];
    double[] probabilities = fillDataAndBuildProbs(numberOfStates, strideLengths, nodeArray);
    return new ProbabilityVector(
        nodeArray,
        buildStateArrays(nodeArray),
        numberOfStates,
        strideLengths,
        probabilities,
        NodeUtils.buildNodeIndexMap(nodeArray));
  }

  private void validateVectorLength(List<Node> nodes) {
    List<Node> emptyStates = nodes.stream().filter(node -> node.getNodeStates().isEmpty()).toList();
    if (!emptyStates.isEmpty()) {
      throw new ProbabilityVectorFactoryException(
          "Attempted to create a vector using nodes [%s], which have no NodeStates!"
              .formatted(NodeUtils.formatNodesToString(emptyStates)));
    }
    if (!TreewidthValidator.validateVectorLength(nodes)) {
      throw new ProbabilityVectorFactoryException(
          "Attempted to create a Probability Vector that would exceed 2^31 - 1 entries with nodes: [%s]"
              .formatted(NodeUtils.formatNodesToString(nodes)));
    }
  }

  private double[] fillDataAndBuildProbs(int[] numberOfStates, int[] strideLengths, Node[] nodes) {
    int strideLength = 1;
    for (int i = nodes.length - 1; i >= 0; i--) {
      int stateCount = nodes[i].getNodeStates().size();
      numberOfStates[i] = stateCount;
      strideLengths[i] = strideLength;
      strideLength *= stateCount;
    }
    double[] probabilities = new double[strideLength];
    Arrays.fill(probabilities, 1.0);
    return probabilities;
  }

  public static NodeState[][] buildStateArrays(Node[] nodesArray) {
    NodeState[][] arrays = new NodeState[nodesArray.length][];
    for (int i = 0; i < nodesArray.length; i++) {
      arrays[i] = nodesArray[i].getNodeStates().toArray(NodeState[]::new);
    }
    return arrays;
  }
}
