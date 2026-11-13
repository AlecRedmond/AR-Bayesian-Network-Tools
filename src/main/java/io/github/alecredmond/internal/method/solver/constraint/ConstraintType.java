package io.github.alecredmond.internal.method.solver.constraint;

import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.solver.constraint.types.conditionalconstraint.ConditionalConstraintStrategy;
import io.github.alecredmond.internal.method.solver.constraint.types.jointconstraint.JointConstraintStrategy;
import io.github.alecredmond.internal.method.solver.constraint.types.marginalconstraint.MarginalConstraintStrategy;
import io.github.alecredmond.internal.method.solver.constraint.types.sumconstraint.SumConstraintStrategy;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ConstraintType {
  MARGINAL(MarginalConstraintStrategy::new),
  CONDITIONAL(ConditionalConstraintStrategy::new),
  JOINT(JointConstraintStrategy::new),
  SUM(SumConstraintStrategy::new);

  private final Supplier<ConstraintStrategy<?>> strategySupplier;

  ConstraintType(Supplier<ConstraintStrategy<?>> strategySupplier) {
    this.strategySupplier = strategySupplier;
  }
}
