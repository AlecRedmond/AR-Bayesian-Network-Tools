package io.github.alecredmond.internal.method.vectoriterator;

import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.application.vectoriterator.VectorOdometer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerController;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerResetLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorutils.OdometerUpdateLogic;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import lombok.Getter;

@Getter
public class VectorIterator<T extends VectorOdometer> {
  protected OdometerController<T> controller;

  public VectorIterator(OdometerController<T> controller) {
    this.controller = controller;
    controller.reset();
  }

  public <R extends OdometerUpdateLogic<T> & OdometerResetLogic<T>> VectorIterator(
      T odometer, R logic) {
    this.controller = new OdometerController<>(odometer, logic, logic);
    controller.reset();
  }

  public <R extends OdometerUpdateLogic<T> & OdometerResetLogic<T>> VectorIterator(
      ProbabilityVector vector, R logic, Function<ProbabilityVector, T> constructor) {
    T odometer = constructor.apply(vector);
    this.controller = new OdometerController<>(odometer, logic, logic);
    controller.reset();
  }

  public void iterateInner(ObjIntConsumer<T> indexConsumer) {
    iterateInner(indexConsumer, controller.getUpdateConsumer());
  }

  protected void iterateInner(ObjIntConsumer<T> indexConsumer, ObjIntConsumer<T> updateConsumer) {
    iterate(controller.getOdometer(), indexConsumer, updateConsumer, controller.getInitInner());
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
    int[] strideIfLocked = initializer.getStrideIfLocked();
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

  public void reset() {
    controller.reset();
  }

  public void iterateOuter(Runnable runnable) {
    iterateOuter((o, i) -> runnable.run(), controller.getUpdateConsumer());
  }

  protected void iterateOuter(ObjIntConsumer<T> indexConsumer, ObjIntConsumer<T> updateConsumer) {
    iterate(controller.getOdometer(), indexConsumer, updateConsumer, controller.getInitOuter());
  }

  public void iterateOuter(ObjIntConsumer<T> indexConsumer) {
    iterateOuter(indexConsumer, controller.getUpdateConsumer());
  }
}
