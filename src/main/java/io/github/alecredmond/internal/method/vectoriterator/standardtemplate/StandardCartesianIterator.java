package io.github.alecredmond.internal.method.vectoriterator.standardtemplate;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.cartesianvector.CartesianVector;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import io.github.alecredmond.internal.method.vectoriterator.CartesianVectorIterator;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.EvidenceTest;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.InitialPositionSetter;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.PositionLock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import lombok.Getter;

@Getter
public class StandardCartesianIterator extends CartesianVectorIterator {
  private final PositionLock outerLocks;
  private final PositionLock innerLocks;
  private final EvidenceTest evidenceTest;

  public StandardCartesianIterator(
      CartesianOdometer odometer,
      Consumer<CartesianOdometer> updateConsumer,
      InitialPositionSetter setter,
      PositionLock outerLocks,
      PositionLock innerLocks,
      EvidenceTest evidenceTest) {
    super(odometer, updateConsumer, setter, outerLocks, innerLocks, evidenceTest);
    this.outerLocks = outerLocks;
    this.innerLocks = innerLocks;
    this.evidenceTest = evidenceTest;
  }

  public static <N extends CartesianVariable, S extends CartesianState>
      StandardCartesianIterator create(
          CartesianIteratorTemplate<N, S> template, CartesianVector<N, S> vector) {
    CartesianOdometer odometer = template.createOdometer().apply(vector);
    return new StandardCartesianIterator(
        odometer,
        template.stateUpdateFunction(),
        InitialPositionSetter.create(vector, odometer, template.initialStatePositionSetter()),
        PositionLock.create(vector, odometer, template.checkLockOuter()),
        PositionLock.create(vector, odometer, template.checkLockInner()),
        EvidenceTest.create(vector, odometer, template.updateEvidenceArrays()));
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

  public BooleanSupplier getIsEvidenceCheck() {
    return evidenceTest.getTest();
  }

  public boolean[] preBuildEvidenceArray() {
    BooleanSupplier test = evidenceTest.getTest();
    List<Boolean> booleans = new ArrayList<>();
    iterateOuter(() -> booleans.add(test.getAsBoolean()));
    boolean[] bArray = new boolean[booleans.size()];
    IntStream.range(0, booleans.size()).forEach(i -> bArray[i] = booleans.get(i));
    return bArray;
  }
}
