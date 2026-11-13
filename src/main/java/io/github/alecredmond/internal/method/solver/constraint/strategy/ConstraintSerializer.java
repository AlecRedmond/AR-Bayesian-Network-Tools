package io.github.alecredmond.internal.method.solver.constraint.strategy;

import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.serialization.SerializationData;

public interface ConstraintSerializer<
    T extends ProbabilityConstraint, S extends SerializedProbabilityConstraint> {
  S serialize(T constraint);

  T deSerializeAndValidate(
      SerializedProbabilityConstraint serialized,
      ConstraintValidator<T, ?> validator,
      SerializationData serializationData);

  T deSerialize(S serialized, SerializationData serializationData);

  S safeCast(SerializedProbabilityConstraint serialized);
}
