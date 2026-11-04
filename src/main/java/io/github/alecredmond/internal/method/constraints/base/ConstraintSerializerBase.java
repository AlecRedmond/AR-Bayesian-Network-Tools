package io.github.alecredmond.internal.method.constraints.base;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintValidator;
import io.github.alecredmond.internal.method.constraints.strategy.ValidatedConstraint;
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
