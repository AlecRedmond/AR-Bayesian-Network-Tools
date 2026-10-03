package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.updatelogictypes;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerUpdateLogic;
import java.util.function.ObjIntConsumer;
import java.util.stream.IntStream;

public interface OdometerUpdateWriteStatesToArray extends OdometerUpdateLogic<VectorOdometer> {
  default ObjIntConsumer<VectorOdometer> update() {
    return (o, i) -> {
      NodeState[][] stateArrays = o.getStateArrays();
      NodeState[] states = o.getStates();
      int[] stateIndexes = o.getStatePositions();
      IntStream.range(0, states.length)
          .forEach(
              x -> {
                int y = stateIndexes[x];
                states[x] = stateArrays[x][y];
              });
    };
  }
}
