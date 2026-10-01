package io.github.alecredmond.export.cartesianvector;

import java.util.List;

public interface CartesianVariable {
    List<? extends CartesianState> getStates();
}
