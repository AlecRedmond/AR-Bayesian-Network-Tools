package io.github.alecredmond.internal.method.solver.constraint.base;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintValidator;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ValidatedConstraint;
import io.github.alecredmond.internal.serialization.SerializationData;
import java.util.Optional;
import java.util.Set;

public abstract class ConstraintSerializerBase<
        P extends ProbabilityConstraint, S extends SerializedProbabilityConstraint>
    implements ConstraintSerializer<P, S> {

  @Override
  public P deSerializeAndValidate(
      SerializedProbabilityConstraint serialized,
      ConstraintValidator<P, ?> validator,
      SerializationData serializationData) {
    Set<Node> nodeSet = serializationData.getNodeSet();
    Set<ProbabilityConstraint> constraints = serializationData.getConstraints();
    return Optional.ofNullable(safeCast(serialized))
        .map(s -> deSerialize(s, serializationData))
        .map(p -> validator.validateConstraint(nodeSet, constraints, p))
        .map(ValidatedConstraint::getConstraint)
        .filter(constraints::add)
        .orElseThrow(() -> supplyWrongTypeException(serialized));
  }

  protected ConstraintValidationException supplyWrongTypeException(
      SerializedProbabilityConstraint serialized) {
    return new ConstraintValidationException(
        "Serialized Constraint %s is invalid for Constraint Serializer %s"
            .formatted(serialized.getClass(), this.getClass()));
  }
}
