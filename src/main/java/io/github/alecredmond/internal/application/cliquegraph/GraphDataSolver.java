package io.github.alecredmond.internal.application.cliquegraph;

import io.github.alecredmond.export.inference.direct.Clique;
import io.github.alecredmond.internal.method.solver.constraint.strategy.ConstraintSolver;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GraphDataSolver extends GraphData {
  protected Map<Clique, List<ConstraintSolver>> solversPerClique;
}
