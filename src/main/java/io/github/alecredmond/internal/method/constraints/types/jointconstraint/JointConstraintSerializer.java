package io.github.alecredmond.internal.method.constraints.types.jointconstraint;

import io.github.alecredmond.export.constraints.JointProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedJointProbabilityConstraint;
import io.github.alecredmond.export.constraints.serialized.SerializedProbabilityConstraint;
import io.github.alecredmond.internal.method.constraints.base.ConstraintSerializerBase;
import io.github.alecredmond.internal.serialization.SerializationData;
import io.github.alecredmond.internal.serialization.SerializerUtils;
import java.util.ArrayList;

public class JointConstraintSerializer
    extends ConstraintSerializerBase<
        JointProbabilityConstraint, SerializedJointProbabilityConstraint> {

  @Override
  public SerializedJointProbabilityConstraint serialize(JointProbabilityConstraint constraint) {
    return new SerializedJointProbabilityConstraint(
        SerializerUtils.serializeNodeStates(constraint.getEventStates()),
        SerializerUtils.serializeNodeStates(constraint.getConditionStates()),
        constraint.getProbability());
  }

  @Override
  public JointProbabilityConstraint deSerialize(
      SerializedJointProbabilityConstraint serialized, SerializationData serializationData) {
    return new JointProbabilityConstraint(
        SerializerUtils.deSerializeNodeStates(
            serialized.eventIds(), ArrayList::new, serializationData),
        SerializerUtils.deSerializeNodeStates(
            serialized.conditionIds(), ArrayList::new, serializationData),
        serialized.probability());
  }

  @Override
  public SerializedJointProbabilityConstraint safeCast(SerializedProbabilityConstraint serialized) {
    return serialized instanceof SerializedJointProbabilityConstraint cast ? cast : null;
  }
}
