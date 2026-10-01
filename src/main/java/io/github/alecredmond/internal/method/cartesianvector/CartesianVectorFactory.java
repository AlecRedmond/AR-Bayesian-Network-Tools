package io.github.alecredmond.internal.method.cartesianvector;

import io.github.alecredmond.exceptions.ProbabilityVectorFactoryException;
import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.method.junctiontree.treebuilding.TreewidthValidator;
import io.github.alecredmond.internal.method.utils.CollectionToString;
import java.util.List;
import java.util.function.IntFunction;

public abstract class CartesianVectorFactory<
    N extends CartesianVariable, S extends CartesianState, R extends CartesianVector<N, S>> {

  public R build(List<N> nodes) {
    assertNodesSizeNotZero(nodes);
    validateVectorLength(nodes);

    N[] nodeArray = nodes.toArray(nodeArraySupplier());
    S[][] stateArrays = state2DArraySupplier().apply(nodeArray.length);
    int[] numberOfStates = new int[nodeArray.length];
    int[] strideLengths = new int[nodeArray.length];
    int rank = fillDataAndReturnRank(numberOfStates, strideLengths, nodeArray,stateArrays);

    return instanceSpecificBuildLogic(nodeArray, stateArrays, numberOfStates, strideLengths, rank);
  }

  private void assertNodesSizeNotZero(List<N> nodes) {
    if (nodes.isEmpty()) {
      throw new ProbabilityVectorFactoryException("Node list was empty");
    }
  }

  private void validateVectorLength(List<N> nodes) {
    List<N> emptyStates = nodes.stream().filter(node -> node.getStates().isEmpty()).toList();
    if (!emptyStates.isEmpty()) {
      throw new ProbabilityVectorFactoryException(
          "Attempted to create a vector using nodes [%s], which have no NodeStates!"
              .formatted(CollectionToString.apply(emptyStates)));
    }
    if (!TreewidthValidator.validateVectorLength(nodes)) {
      throw new ProbabilityVectorFactoryException(
          "Attempted to create a Probability Vector that would exceed 2^31 - 1 entries with nodes: [%s]"
              .formatted(CollectionToString.apply(nodes)));
    }
  }

  protected abstract IntFunction<N[]> nodeArraySupplier();

  protected abstract IntFunction<S[][]> state2DArraySupplier();

    private int fillDataAndReturnRank(
      int[] numberOfStates, int[] strideLengths, N[] nodes, S[][] stateArray) {
    IntFunction<S[]> stateArraySupplier = stateArraySupplier();
    int strideLength = 1;
    for (int i = nodes.length - 1; i >= 0; i--) {
      S[] states = nodes[i].getStates().toArray(stateArraySupplier);
      stateArray[i] = states;
      int stateCount = states.length;
      numberOfStates[i] = stateCount;
      strideLengths[i] = strideLength;
      strideLength *= stateCount;
    }
    return strideLength;
  }

  protected abstract R instanceSpecificBuildLogic(
      N[] nodeArray, S[][] stateArrays, int[] numberOfStates, int[] strideLengths, int rank);

  protected abstract IntFunction<S[]> stateArraySupplier();
}
