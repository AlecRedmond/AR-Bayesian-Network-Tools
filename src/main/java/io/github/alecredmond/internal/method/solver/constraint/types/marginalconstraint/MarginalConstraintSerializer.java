package io.github.alecredmond.internal.method.solver.constraint.types.marginalconstraint;

import io.github.alecredmond.export.solver.constraint.MarginalConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedMarginalConstraint;
import io.github.alecredmond.export.solver.constraint.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.solver.constraint.base.ConstraintSerializerBase;
import io.github.alecredmond.internal.serialization.SerializationData;

public class MarginalConstraintSerializer
    extends ConstraintSerializerBase<MarginalConstraint, SerializedMarginalConstraint> {

  @Override
  public SerializedMarginalConstraint serialize(MarginalConstraint constraint) {
    return new SerializedMarginalConstraint(
        constraint.getEventState().getId(), constraint.getProbability());
  }

  @Override
  public MarginalConstraint deSerialize(
      SerializedMarginalConstraint serialized, SerializationData serializationData) {
    return new MarginalConstraint(
        serializationData.getNodeStateIdMap().get(serialized.eventStateId()),
        serialized.probability());
  }

  @Override
  public SerializedMarginalConstraint safeCast(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedMarginalConstraint cast ? cast : null;
  }
}
