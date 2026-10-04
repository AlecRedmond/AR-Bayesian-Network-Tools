package io.github.alecredmond.internal.method.vectoriterator.iteratorutils;

import io.github.alecredmond.export.cartesianvector.CartesianState;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.internal.application.vectoriterator.CartesianIteratorLogic;
import io.github.alecredmond.internal.application.vectoriterator.CartesianOdometer;
import java.util.List;
import java.util.function.ObjIntConsumer;
import lombok.Data;

@Data
public class OdometerController<
    N extends CartesianVariable, S extends CartesianState, T extends CartesianOdometer<N, S, ?>> {
  private T odometer;
  private List<CartesianIteratorLogic<N, S, T>> resetLogic;
  private ObjIntConsumer<T> updateConsumer;

  public OdometerController(
      T odometer,
      ObjIntConsumer<T> updateConsumer,
      List<CartesianIteratorLogic<N, S, T>> resetLogic) {
    this.odometer = odometer;
    this.updateConsumer = updateConsumer;
    this.resetLogic = resetLogic;
  }

  public void reset() {
    resetLogic.forEach(CartesianIteratorLogic::reset);
  }
}
