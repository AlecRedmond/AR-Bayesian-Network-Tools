package io.github.alecredmond.scenarios;

import static io.github.alecredmond.scenarios.NetworkScenarioBuilder.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum NetworkScenario {
  RAIN_NETWORK(buildRainNetwork()),
  AH_NETWORK(buildAhNetwork()),
  SIMPLE_LINEAR(buildSimpleLinearGraph()),
  DIAMOND_NET(buildDiamondNetwork()),
  WEATHER_NETWORK(buildWeatherNetwork()),
  CAR_TRIMS(buildCarTrimNetwork()),
  LOPSIDED(buildNetworkLopsided()),
  ASIA_NETWORK(buildAsiaNetwork());

  private final Supplier<BayesianNetwork> supplier;

  NetworkScenario(Supplier<BayesianNetwork> supplier) {
    this.supplier = supplier;
  }

  public BayesianNetwork get() {
    return supplier.get();
  }
}
