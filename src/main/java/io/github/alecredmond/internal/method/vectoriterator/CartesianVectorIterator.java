package io.github.alecredmond.internal.method.vectoriterator;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.application.vectoriterator.positionlocker.PositionLock;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerController;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.UpdateStateArrayLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes.ResetLogicUtils;
import java.util.function.ObjIntConsumer;
import lombok.Getter;

public class CartesianVectorIterator<
    N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>> {
  @Getter protected final OdometerController<N, S, T> controller;

  @SafeVarargs
  public CartesianVectorIterator(
      T odometer,
      UpdateStateArrayLogic stateUpdateLogic,
      CartesianIteratorLogic<N, S, T>... logicArgs) {
    this.controller =
        new OdometerController<>(
            odometer,
            ResetLogicUtils.supplyUpdateLogic(stateUpdateLogic),
            CartesianIteratorLogic.sort(logicArgs));
    controller.reset();
  }

  public void iterate(ObjIntConsumer<T> indexConsumer, PositionLock<N, S, T> positionLock) {
    iterate(
        controller.getOdometer(),
        indexConsumer,
        controller.getUpdateConsumer(),
        positionLock.getInitializer());
  }

  /**
   * This method exists to solve the problem of stepping iteratively through known combinations of
   * states while accessing associated array index of the vector's probability table.
   *
   * <p>Both the Vector Odometer (which can be read for the current state values) and the
   * probability index (the position in the vector's probability array of the combination) can be
   * iterated through and processed sequentially with a BiConsumer, while locking specific NodeState
   * values in place.
   *
   * <p>It achieves this by advancing the odometer's state index array, starting from the fastest
   * (rightmost) unlocked position and carrying left for every overflow encountered. An overflow
   * that carries to the leftmost position represents an overflow/reset of the odometer as a whole
   * and therefore the end of the iterator cycle.
   *
   * <p>Simultaneously, the index is incremented by the size of the base stride (1 if the fastest
   * moving position (fp) is the rightmost, stepMultiplier[fp] otherwise). When an overflow leads
   * into a locked position (lp), the index is increased by
   *
   * <p><code>
   * (numberOfStates[lp] - 1) * nodeMultiplier[lp])</code>
   *
   * <p>which represents a stride over the other states in the position.
   *
   * <p>There is the option for a second consumer which is run at the end of each update. This may
   * be used e.g. for updating the NodeState[] array to be in-line with the given int[]
   * StateIndexes, but is unused in most cases to reduce compute time per iteration.
   *
   * @param odometer a {@link VectorOdometer} to be iterated through
   * @param indexConsumer a consumer supplied with both the odometer and the current probability
   *     array index at the beginning of each iteration cycle.
   * @param updateConsumer a consumer supplied with the odometer and the next array index at the end
   *     of each iteration cycle. Useful for updating the Odometer state array to conform to the
   *     state indexes.
   * @param initializer the initializer object that contains the pre-calculated starting index,
   *     states, and whether the iterator is to fire only once.
   */
  protected void iterate(
      T odometer,
      ObjIntConsumer<T> indexConsumer,
      ObjIntConsumer<T> updateConsumer,
      OdometerInitializer initializer) {
    int currentIndex = initializer.getInitialIndex();

    if (initializer.isFireOnlyOnce()) {
      indexConsumer.accept(odometer, currentIndex);
      return;
    }

    /* The rightmost Node in the odometer which isn't locked... */
    int fastestPosition = initializer.getFastestPosition();
    /* ... And the probability index stride to address its next state, while preserving all other states. */
    int baseStride = initializer.getBaseStride();
    int[] numberOfStates = odometer.getNumberOfStates();
    int[] strideIfLocked = odometer.getStrideOverValues();
    int[] stateIndexes = odometer.getStatePositions();
    boolean[] positionLocked = initializer.getLockedPositions();
    boolean overflow = false;

    /* While the overflow has not carried fully left... */
    while (!overflow) {
      /* Accept the active consumer and stride to the next probability index... */
      indexConsumer.accept(odometer, currentIndex);
      currentIndex += baseStride;
      /* Then, from the rightmost unlocked Node... */
      for (int position = fastestPosition; position >= 0; position--) {
        /* And striding over any locked Node... */
        if (positionLocked[position]) {
          currentIndex += strideIfLocked[position];
          continue;
        }
        /* Increment the current Node's state index... */
        overflow = ++stateIndexes[position] >= numberOfStates[position];
        /* Stopping if it's within bounds... */
        if (!overflow) {
          break;
        }
        /* Or carrying left if it overflows... */
        stateIndexes[position] = 0;
      }
      /* And notify the update consumer of the new state and probability indexes. */
      updateConsumer.accept(odometer, currentIndex);
    }
  }

  public void iterateOuter(Runnable runnable, PositionLock<N, S, T> positionLock) {
    iterate(
        controller.getOdometer(),
        (o, i) -> runnable.run(),
        controller.getUpdateConsumer(),
        positionLock.getInitializer());
  }

  public void reset() {
    controller.reset();
  }

  public T getOdometer() {
    return controller.getOdometer();
  }
}
