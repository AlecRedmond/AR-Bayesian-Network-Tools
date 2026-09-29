package io.github.alecredmond.internal.method.network.prevalidationlogic;

import io.github.alecredmond.internal.method.network.BayesianNetworkImpl;

public interface PreValidationLogic {
  void runLogic(BayesianNetworkImpl network);
}
