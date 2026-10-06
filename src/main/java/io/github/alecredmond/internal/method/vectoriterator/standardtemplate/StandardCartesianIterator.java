package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.application.vectoriterator.IteratorFactoryData;
import io.github.alecredmond.internal.method.vectoriterator.CartesianVectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.InitialPositionSetter;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.OdometerEvidenceChecker;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.PositionLock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class StandardCartesianIterator<N extends CartesianVariable, S extends CartesianState>
    extends CartesianVectorIterator<N, S> {
  private final PositionLock<N, S> outerLocks;
  private final PositionLock<N, S> innerLocks;
  private final OdometerEvidenceChecker<N, S> evidenceChecker;

  public StandardCartesianIterator(IteratorFactoryData<N, S> data) {
    super(
        data.odometer(),
        data.updateConsumer(),
        data.setter(),
        data.outerLocks(),
        data.innerLocks(),
        data.evidenceChecker());
    this.outerLocks = data.outerLocks();
    this.innerLocks = data.innerLocks();
    this.evidenceChecker = data.evidenceChecker();
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
  StandardCartesianIterator<N, S> create(
          CartesianIteratorTemplate<N, S, V> template, V vector) {
    return new StandardCartesianIterator<>(createFactoryData(template, vector));
  }

  public static <
          N extends CartesianVariable, S extends CartesianState, V extends CartesianVector<N, S>>
      IteratorFactoryData<N, S> createFactoryData(
          CartesianIteratorTemplate<N, S, V> template, V vector) {
    CartesianOdometer<N, S> odometer = template.createOdometer().apply(vector);
    return new IteratorFactoryData<>(
        odometer,
        template.stateUpdateFunction(),
        new InitialPositionSetter<>(odometer, template.initialStatePositionSetter()),
        new PositionLock<>(odometer, template.checkLockOuter()),
        new PositionLock<>(odometer, template.checkLockInner()),
        new OdometerEvidenceChecker<>(odometer, template.updateEvidenceArrays()));
  }

  public void iterateOuter(IntConsumer indexConsumer) {
    super.iterate(indexConsumer, outerLocks);
  }

  public void iterateInner(IntConsumer indexConsumer) {
    super.iterate(indexConsumer, innerLocks);
  }

  public int[][] cacheIndexesOverOuterRuns() {
    List<int[]> innerArrays = new ArrayList<>();
    iterateOuter(() -> innerArrays.add(cacheIndexes(innerLocks)));
    return innerArrays.toArray(int[][]::new);
  }

  public void iterateOuter(Runnable runnable) {
    super.iterateOuter(runnable, outerLocks);
  }

  public int[] cacheInnerIndexes() {
    return super.cacheIndexes(innerLocks);
  }

  public BooleanSupplier getIsEvidenceCheck() {
    return evidenceChecker.getTest();
  }

  public boolean[] preBuildEvidenceArray() {
    BooleanSupplier test = evidenceChecker.getTest();
    List<Boolean> bools = new ArrayList<>();
    iterateOuter(() -> bools.add(test.getAsBoolean()));
    boolean[] bArray = new boolean[bools.size()];
    IntStream.range(0, bools.size()).forEach(i -> bArray[i] = bools.get(i));
    return bArray;
  }
}
