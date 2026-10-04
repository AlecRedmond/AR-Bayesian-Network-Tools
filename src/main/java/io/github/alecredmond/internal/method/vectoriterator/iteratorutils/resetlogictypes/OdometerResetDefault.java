package io.github.alecredmond.internal.method.vectoriterator.iteratorutils.resetlogictypes;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardVectorIteratorTemplate;

import java.util.function.Function;

public interface OdometerResetDefault extends StandardVectorIteratorTemplate {

    @Override
    default CartesianIteratorLogic.ResetLogicType getResetLogicType() {
        return CartesianIteratorLogic.ResetLogicType.VARIABLE;
    }

    default Function<Node, boolean[]> evidenceChecker() {
    return node -> null;
  }
}
