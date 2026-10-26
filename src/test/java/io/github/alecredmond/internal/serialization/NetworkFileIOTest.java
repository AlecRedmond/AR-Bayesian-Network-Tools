package io.github.alecredmond.internal.serialization;

import static io.github.alecredmond.TestConfigs.*;
import static io.github.alecredmond.networkscenario.NetworkScenarioType.*;
import static io.github.alecredmond.internal.method.utils.AppProperty.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.fileio.NetworkFileIO;
import io.github.alecredmond.internal.method.utils.PropertiesLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class NetworkFileIOTest {
  static final PropertiesLoader LOADER = new PropertiesLoader();
  static final String DIRECTORY = LOADER.loadDirectory(DIRECTORY_ROOT, DIRECTORY_SAVE);
  static final String EXTENSION = LOADER.loadString(EXTENSION_FILE_TYPE);
  static List<Supplier<BayesianNetwork>> networkSuppliers;
  NetworkFileIO test = new NetworkFileIO(new BayesianNetworkSerializer());

  static Stream<Arguments> provideNetworks() {
    return networkSuppliers.stream()
        .map(Supplier::get)
        .map(BayesianNetwork::solveNetwork)
        .map(Arguments::of);
  }

  @BeforeAll
  static void init() {
    networkSuppliers = new ArrayList<>();
    networkSuppliers.add(RAIN_NETWORK.getSupplier());
    networkSuppliers.add(AH_NETWORK.getSupplier());
    networkSuppliers.add(SIMPLE_LINEAR.getSupplier());
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void saveNetworkFileChooser(BayesianNetwork network) {
    if (!RUN_J_FILE_CHOOSER_TESTS || !SAVE_BINARIES) {
      assertTrue(true);
      return;
    }
    log.info("SAVE {}", network.getNetworkData().getNetworkName());
    assertTrue(test.saveNetwork(network));
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void loadNetworkFileChooser(BayesianNetwork network) {
    if (!RUN_J_FILE_CHOOSER_TESTS || !SAVE_BINARIES) {
      assertTrue(true);
      return;
    }
    log.info("LOAD NETWORK : {}", network.getNetworkData().getNetworkName());
    BayesianNetwork newNet = test.loadNetwork();
    assertEquals(network.getNetworkData(), newNet.getNetworkData());
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void saveNetwork(BayesianNetwork network) {
    if (!SAVE_BINARIES) {
      assertTrue(true);
      return;
    }
    String netName = network.getNetworkData().getNetworkName();
    String totalPath = DIRECTORY + netName;
    assertTrue(test.saveNetwork(network, totalPath));
    assertTrue(test.saveNetwork(network, new File(totalPath)));
  }

  @ParameterizedTest
  @MethodSource("provideNetworks")
  void loadNetwork(BayesianNetwork network) {
    if (!SAVE_BINARIES) {
      assertTrue(true);
      return;
    }
    String netName = network.getNetworkData().getNetworkName();
    String totalPath = DIRECTORY + netName + EXTENSION;
    BayesianNetwork loaded = test.loadNetwork(totalPath);
    assertEquals(network.getNetworkData(), loaded.getNetworkData());
  }
}
