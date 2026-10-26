package io.github.alecredmond.networkscenario;

import io.github.alecredmond.networkscenario.scenarios.*;
import java.util.function.Supplier;
import lombok.Getter;

@Getter
public enum NetworkScenarioType {
  AH_NETWORK(AHNetwork::new),
  ASIA_NETWORK(AsiaNetwork::new),
  CAR_TRIMS(CarTrimNetwork::new),
  DIAMOND_NET(DiamondNetwork::new),
  LOPSIDED(LopsidedNetwork::new),
  RAIN_NETWORK(RainNetwork::new),
  SIMPLE_LINEAR(SimpleLinearNetwork::new),
  WEATHER_NETWORK(WeatherNetwork::new);

  private final Supplier<NetworkScenario> supplier;

  NetworkScenarioType(Supplier<NetworkScenario> supplier) {
    this.supplier = supplier;
  }

  public NetworkScenario get() {
    return supplier.get();
  }
}
