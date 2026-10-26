package io.github.alecredmond.internal.method.network.validator;

import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum ValidatorType {
  ID_VALIDATOR(NetworkIdValidator::new),
  STRUCTURE_VALIDATOR(NetworkStructureValidator::new),
  CONSTRAINT_VALIDATOR(NetworkConstraintValidator::new),
  STATE_POSITION_VALIDATOR(NodeStatePositionValidator::new);

  private final Supplier<NetworkValidator> validatorSupplier;

  ValidatorType(Supplier<NetworkValidator> validatorSupplier) {
    this.validatorSupplier = validatorSupplier;
  }
}
