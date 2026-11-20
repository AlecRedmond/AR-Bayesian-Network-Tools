package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.internal.application.network.BayesianNetworkData;

public interface NetworkValidator {
  void validateData(BayesianNetworkData networkData);
}
