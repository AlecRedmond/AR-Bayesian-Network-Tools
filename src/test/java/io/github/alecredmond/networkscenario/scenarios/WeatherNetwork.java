package io.github.alecredmond.networkscenario.scenarios;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.solver.BayesSolver;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.networkscenario.templates.AbstractNetworkScenario;
import java.util.List;

public class WeatherNetwork extends AbstractNetworkScenario {
  // CLOUD_COVER
  //      |
  // PRECIPITATION
  public final Node cloudCover;
  public final Node precipitation;

  public WeatherNetwork() {
    cloudCover = defaultNode("CLOUD_COVER", List.of("CLEAR", "LIGHT", "HEAVY"));
    precipitation = defaultNode("PRECIPITATION", List.of("NONE", "RAIN", "SNOW"), cloudCover);

    addConstraint(state(cloudCover, "HEAVY"), 0.30);
    addConstraint(List.of(state(cloudCover, "CLEAR"), state(cloudCover, "LIGHT")), 0.70);
    addConstraint(state(precipitation, "NONE"), List.of(state(cloudCover, "CLEAR")), 1.0);
    addConstraint(List.of(state(cloudCover, "LIGHT"), state(precipitation, "RAIN")), 0.15);
    addConstraint(
        List.of(state(precipitation, "RAIN"), state(precipitation, "SNOW")),
        List.of(state(cloudCover, "HEAVY")),
        0.90);
  }

  @Override
  protected BayesSolver defaultSolver() {
    return new IPFPJunctionTreeSolver();
  }

  @Override
  protected String getNetworkName() {
    return "WEATHER_AND_PRECIP";
  }

  @Override
  protected Node[] definedNodes() {
    return new Node[] {cloudCover, precipitation};
  }
}
