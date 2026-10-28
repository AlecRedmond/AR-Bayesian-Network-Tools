package io.github.alecredmond.internal.serialization.structure;

import io.github.alecredmond.exceptions.ConstraintValidationException;
import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.export.network.serialized.SerializedBayesianNetwork;
import io.github.alecredmond.internal.application.network.BayesianNetworkData;
import io.github.alecredmond.internal.method.constraints.ConstraintRegistry;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.network.validator.NetworkConstraintValidator;
import io.github.alecredmond.internal.serialization.SerializationData;
import java.util.*;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProbabilityConstraintSerializer {
  private final ConstraintRegistry registry = new ConstraintRegistry();

  public List<SerializedProbabilityConstraint> serializeAll(BayesianNetworkData data) {
    new NetworkConstraintValidator().validateData(data);
    List<SerializedProbabilityConstraint> list = new ArrayList<>();
    data.getConstraints().stream().map(this::serializeConstraint).forEach(list::add);
    return Collections.unmodifiableList(list);
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

  public void deserialize(SerializedBayesianNetwork sbn, SerializationData data) {
    List<SerializedProbabilityConstraint> serializedConstraints = sbn.serializedConstraints();
    Set<ProbabilityConstraint> constraints = data.getNetworkData().getConstraints();
    serializedConstraints.stream()
        .map(serialized -> deSerializeConstraint(serialized, data))
        .forEach(constraints::add);
    new NetworkConstraintValidator().validateData(data.getNetworkData());
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
