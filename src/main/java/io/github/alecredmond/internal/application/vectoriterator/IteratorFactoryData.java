package io.github.alecredmond.internal.application.vectoriterator;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.InitialPositionSetter;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.OdometerEvidenceChecker;
import io.github.alecredmond.internal.method.vectoriterator.iteratorlogic.PositionLock;
import java.util.function.Consumer;

public record IteratorFactoryData<N extends CartesianVariable, S extends CartesianState>(
    CartesianOdometer<N, S> odometer,
    Consumer<CartesianOdometer<N, S>> updateConsumer,
    InitialPositionSetter<N, S> setter,
    PositionLock<N, S> outerLocks,
    PositionLock<N, S> innerLocks,
    OdometerEvidenceChecker<N, S> evidenceChecker) {}
