package io.github.alecredmond.internal.serialization;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.export.solver.constraint.ProbabilityConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.ConstraintRegistry;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintStrategy;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProbabilityConstraintSerializer {
  private final ConstraintRegistry registry = new ConstraintRegistry();

  public SerializedProbabilityConstraint[] serialize(Set<ProbabilityConstraint> constraints) {
    return constraints.stream()
        .map(this::serializeConstraint)
        .toArray(SerializedProbabilityConstraint[]::new);
  }

  private <T extends ProbabilityConstraint> SerializedProbabilityConstraint serializeConstraint(
      T constraint) {
    return Optional.ofNullable(registry.getSerializer(constraint))
        .map(s -> s.serialize(constraint))
        .orElseThrow(supplySerializerNotFoundException(constraint));
  }

  private Supplier<ConstraintValidationException> supplySerializerNotFoundException(Object object) {
    return () ->
        new ConstraintValidationException(
            "Object %s mapped to no valid ConstraintType".formatted(object));
  }

  public Set<ProbabilityConstraint> deserialize(
      SerializedBayesianNetwork sbn, SerializationData data) {
    return Arrays.stream(sbn.serializedConstraints())
        .map(serialized -> deSerializeConstraint(serialized, data))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private <P extends ProbabilityConstraint> ProbabilityConstraint deSerializeConstraint(
      SerializedProbabilityConstraint serialized, SerializationData data) {
    ConstraintStrategy<P> strategy = registry.getStrategy(serialized);
    if (strategy == null) {
      throw supplySerializerNotFoundException(serialized).get();
    }
    ConstraintSerializer<P, ?> serializer = strategy.getConstraintSerializer();
    return serializer.deSerializeAndValidate(serialized, strategy.getConstraintValidator(), data);
  }
}
