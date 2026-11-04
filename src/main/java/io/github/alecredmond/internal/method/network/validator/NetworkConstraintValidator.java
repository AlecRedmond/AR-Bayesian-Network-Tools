package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NetworkConstraintValidator implements NetworkValidator {
  @Override
  public void validateData(BayesianNetworkData networkData) {
    for (ProbabilityConstraint constraint : networkData.getConstraints()) {
      if (!(constraint instanceof ValidatedConstraint<?>)) {
        throw new ConstraintValidationException(
            "Unvalidated Constraint %s present in the network!".formatted(constraint));
      }
    }
  }
}
