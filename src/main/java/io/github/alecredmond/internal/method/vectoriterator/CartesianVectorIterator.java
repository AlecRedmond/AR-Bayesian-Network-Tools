package io.github.alecredmond.internal.method.vectoriterator;

import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.OdometerInitializer;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.CartesianIteratorLogic;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.PositionLock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import lombok.Getter;

public class CartesianVectorIterator {
  @Getter protected final CartesianOdometer odometer;
  protected final Consumer<CartesianOdometer> updateConsumer;
  protected final CartesianIteratorLogic[] resetLogic;

  public CartesianVectorIterator(
      CartesianOdometer odometer,
      Consumer<CartesianOdometer> updateConsumer,
      CartesianIteratorLogic... logicArgs) {
    this.odometer = odometer;
    this.updateConsumer = updateConsumer;
    this.resetLogic = logicArgs;
    reset();
  }

  public void reset() {
    for (CartesianIteratorLogic logic : resetLogic) {
      logic.reset();
    }
    updateConsumer.accept(odometer);
  }

  public void iterate(IntConsumer indexConsumer, PositionLock positionLock) {
    iterate(odometer, indexConsumer, updateConsumer, positionLock.getInitializer());
  }

  /**
   * This method exists to solve the problem of stepping iteratively through known combinations of
   * states while accessing associated array index of the vector's probability table.
   *
   * <p>The flat index of the Cartesian product can be iterated through and processed sequentially
   * with an IntConsumer, while locking specific Cartesian State values in place.
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
   * @param odometer a {@link CartesianOdometer} to be iterated through
   * @param indexConsumer a consumer supplied the current probability array index at the beginning
   *     of each iteration cycle.
   * @param updateConsumer a consumer supplied with the odometer at the end of each iteration cycle.
   *     This is typically used to write the newly calculated state positions {@code int[]} to state
   *     arrays {@code CartesianState[]}.
   * @param initializer the initializer object that contains the pre-calculated starting index,
   *     state positions, and whether the iterator is to fire only once.
   */
  protected void iterate(
      CartesianOdometer odometer,
      IntConsumer indexConsumer,
      Consumer<CartesianOdometer> updateConsumer,
      OdometerInitializer initializer) {
    int currentIndex = initializer.getInitialIndex();

    /* Fire only once, if all positions are locked */
    if (initializer.isFireOnlyOnce()) {
      indexConsumer.accept(currentIndex);
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
      indexConsumer.accept(currentIndex);
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
      /* And notify the update consumer of the new state positions. */
      updateConsumer.accept(odometer);
    }
  }

  public int[] cacheIndexes(PositionLock positionLock) {
    List<Integer> indexes = new ArrayList<>();
    iterate(odometer, indexes::add, updateConsumer, positionLock.getInitializer());
    return indexes.stream().mapToInt(Integer::intValue).toArray();
  }

  public void iterateOuter(Runnable runnable, PositionLock positionLock) {
    iterate(odometer, i -> runnable.run(), updateConsumer, positionLock.getInitializer());
  }
}
