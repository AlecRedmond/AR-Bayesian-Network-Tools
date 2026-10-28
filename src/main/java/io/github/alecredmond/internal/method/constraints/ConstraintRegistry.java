package io.github.alecredmond.internal.method.constraints;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintSerializer;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintStrategy;
import io.github.alecredmond.internal.method.constraints.strategy.ConstraintValidator;
import java.util.*;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConstraintRegistry {
  private final Map<ConstraintType, ConstraintStrategy<?>> registryMap = buildStrategyMap();

  public <P extends ProbabilityConstraint> ConstraintValidator<P, ?> getValidator(P constraint) {
    return Optional.ofNullable(getStrategy(constraint))
        .map(ConstraintStrategy::getConstraintValidator)
        .orElse(null);
  }

  public <T extends ProbabilityConstraint> ConstraintStrategy<T> getStrategy(
      @NonNull T constraint) {
    return getTypedStrategy(getConstraintType(constraint));
  }

  @SuppressWarnings("unchecked")
  public <T extends ProbabilityConstraint> ConstraintStrategy<T> getTypedStrategy(
      ConstraintType type) {
    try {
      return (ConstraintStrategy<T>) registryMap.get(type);
    } catch (ClassCastException e) {
      log.error("Error casting Constraint Strategy for type '{}'", type);
      return null;
    }
  }

  private <P extends ProbabilityConstraint> ConstraintType getConstraintType(P constraint) {
    return registryMap.entrySet().stream()
        .filter(entry -> entry.getValue().constraintIsInstance(constraint))
        .findAny()
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  public Stream<ConstraintValidator<?, ?>> streamValidators() {
    return registryMap.values().stream().map(ConstraintStrategy::getConstraintValidator);
  }

  @SuppressWarnings("unchecked")
  public <V extends P, P extends ProbabilityConstraint>
      ConstraintSerializer<ProbabilityConstraint, ?> getSerializer(V constraint) {
    return (ConstraintSerializer<ProbabilityConstraint, ?>)
        Optional.ofNullable(getConstraintType(constraint))
            .map(registryMap::get)
            .map(ConstraintStrategy::getConstraintSerializer)
            .orElse(null);
  }

  public <S extends SerializedProbabilityConstraint, T extends ProbabilityConstraint>
      ConstraintStrategy<T> getStrategy(@NonNull S serialized) {
    return getTypedStrategy(getConstraintType(serialized));
  }

  private <S extends SerializedProbabilityConstraint> ConstraintType getConstraintType(
      S serialized) {
    return registryMap.entrySet().stream()
        .filter(entry -> entry.getValue().serializedIsInstance(serialized))
        .findAny()
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  private Map<ConstraintType, ConstraintStrategy<?>> buildStrategyMap() {
    Map<ConstraintType, ConstraintStrategy<?>> map = new EnumMap<>(ConstraintType.class);
    Arrays.stream(ConstraintType.values()).forEach(v -> map.put(v, v.getStrategySupplier().get()));
    return map;
  }
}
