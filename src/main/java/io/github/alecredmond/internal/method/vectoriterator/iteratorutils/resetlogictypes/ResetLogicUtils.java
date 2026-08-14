package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ResetLogicUtils {
  private ResetLogicUtils() {}

  public static Function<Node, boolean[]> updateEvidenceArrayFunction(
      Set<Node> requestNodes, Set<NodeState> requestStates) {
    return node -> {
      if (!requestNodes.contains(node)) {
        return new boolean[0];
      }
      List<NodeState> states = node.getNodeStates();
      boolean[] isEvidence = new boolean[states.size()];
      IntStream.range(0, states.size())
          .filter(y -> requestStates.contains(states.get(y)))
          .forEach(y -> isEvidence[y] = true);
      return isEvidence;
    };
  }

  public static <T extends VectorOdometer> boolean[] preBuildEvidenceCheckArray(
      VectorIterator<T> iterator) {
    T vectorOdometer = iterator.getController().getOdometer();
    int[] stateIndexes = vectorOdometer.getStateIndexes();
    boolean[][] stateIsEvent = vectorOdometer.getNodeStateEvidenceArray();
    List<Boolean> bools = new ArrayList<>();
    iterator.iterateOuter(() -> bools.add(checkIsEvidence(stateIndexes, stateIsEvent)));
    boolean[] bArray = new boolean[bools.size()];
    IntStream.range(0, bools.size()).forEach(i -> bArray[i] = bools.get(i));
    return bArray;
  }

  public static boolean checkIsEvidence(int[] stateIndexes, boolean[][] stateIsEvent) {
    for (int x = 0; x < stateIsEvent.length; x++) {
      if (stateIsEvent[x].length == 0) continue;
      if (!stateIsEvent[x][stateIndexes[x]]) return false;
    }
    return true;
  }

  public static Function<Node, NodeState> initializeToFirstNodeStates() {
    return node -> node.getNodeStates().getFirst();
  }
}
