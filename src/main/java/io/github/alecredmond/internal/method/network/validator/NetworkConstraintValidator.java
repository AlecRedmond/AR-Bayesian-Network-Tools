package io.github.alecredmond.internal.method.network.validator;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ValidatedConstraint;
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
