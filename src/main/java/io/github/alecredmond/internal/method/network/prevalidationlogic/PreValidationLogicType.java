package io.github.alecredmond.internal.method.network.prevalidationlogic;

import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;

import java.util.function.Supplier;

public enum PreValidationLogicType {
  ASSIGN_CPT_CONSTRAINTS(CptConstraintAssigner::new);

  private final Supplier<PreValidationLogic> logicSupplier;

  PreValidationLogicType(Supplier<PreValidationLogic> logicSupplier) {
    this.logicSupplier = logicSupplier;
  }

  public void runLogic(BayesianNetworkImpl network) {
    logicSupplier.get().runLogic(network);
  }
}
