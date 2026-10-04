package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.PositionLock;
import io.github.alecredmond.internal.method.vectoriterator.VectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResetLogicUtils {
  private ResetLogicUtils() {}

  public static <
          N extends CartesianVariable,
          S extends CartesianState,
          T extends CartesianOdometer<N, S, ?>>
      ObjIntConsumer<T> supplyUpdateLogic(UpdateStateArrayLogic type) {
    return switch (type) {
      case NO_UPDATE -> (t, i) -> {};
      case WRITE_STATES_TO_ARRAY -> writeStatesToArray();
    };
  }

  public static <
          N extends CartesianVariable,
          S extends CartesianState,
          T extends CartesianOdometer<N, S, ?>>
      ObjIntConsumer<T> writeStatesToArray() {
    return (o, i) -> {
      S[][] stateArrays = o.getStateArrays();
      S[] states = o.getStates();
      int[] stateIndexes = o.getStatePositions();
      IntStream.range(0, states.length)
          .forEach(
              x -> {
                int y = stateIndexes[x];
                states[x] = stateArrays[x][y];
              });
    };
  }

  public static Function<Node, boolean[]> updateEvidenceArrayFunction(
      Set<Node> requestNodes, Set<NodeState> requestStates) {
    return node -> {
      if (!requestNodes.contains(node)) {
        return new boolean[0];
      }
      List<NodeState> states = node.getStates();
      boolean[] isEvidence = new boolean[states.size()];
      IntStream.range(0, states.size())
          .filter(y -> requestStates.contains(states.get(y)))
          .forEach(y -> isEvidence[y] = true);
      return isEvidence;
    };
  }

  public static Function<Node, Set<NodeState>> createEvidenceFunction(
      Set<Node> requestNodes, Set<NodeState> requestStates) {
    return node -> {
      if (!requestNodes.contains(node)) return new HashSet<>();
      return requestStates.stream()
          .filter(s -> s.getNode().equals(node))
          .collect(Collectors.toSet());
    };
  }

  public static boolean[] preBuildEvidenceCheckArray(VectorIterator iterator) {
    PositionLock<Node, NodeState, VectorOdometer> conditionLock = iterator.getOuterLocks();
    BooleanSupplier test = iterator.getEvidenceChecker().getTest();
    List<Boolean> bools = new ArrayList<>();
    iterator.iterateOuter(() -> bools.add(test.getAsBoolean()), conditionLock);
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
    return node -> node.getStates().getFirst();
  }
}
