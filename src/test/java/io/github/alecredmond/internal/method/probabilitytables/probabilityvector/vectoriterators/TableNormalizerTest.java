package io.github.alecredmond.internal.method.probabilitytables.probabilityvector.vectoriterators;

import static io.github.alecredmond.export.method.network.NetworkScenario.*;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ProbabilityTable;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.internal.method.probabilitytables.tablebuilders.NetworkTableBuilder;
import io.github.alecredmond.internal.method.vectoriterator.misciterators.TableNormalizer;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TableNormalizerTest {
  static final List<BayesianNetwork> NETWORKS =
      List.of(FANTASY_GRAPH.get(), AH_NETWORK.get(), RAIN_NETWORK.get(), SIMPLE_LINEAR.get());
  TableNormalizer test;

  static Stream<Arguments> getNetworkTables() {
    return NETWORKS.stream()
        .flatMap(
            net ->
                net.getNetworkData().getNodeIDsMap().keySet().stream()
                    .map(s -> Arguments.of(net, s)));
  }

  @ParameterizedTest
  @MethodSource("getNetworkTables")
  void normalize(BayesianNetwork network, Serializable nodeId) {
    NetworkTableBuilder builder = new NetworkTableBuilder();
    Node node = network.getNode(nodeId);
    ProbabilityTable table = builder.buildTable(List.of(node), node.getParents());
    double[] probs = table.getVector().getProbabilities();
    assertTrue(Arrays.stream(probs).allMatch(p -> p == 1.0));

    test = new TableNormalizer(table);
    test.normalize();

    int blockSize = node.getStates().size();
    double average = 1.0 / blockSize;
    Arrays.stream(probs).forEach(p -> assertEquals(average, p, 1e-6));

    IntStream.range(0, blockSize).forEach(i -> probs[i] = 1);
    probs[0] = blockSize - 1;

    test.normalize();

    assertEquals(1, Arrays.stream(probs, 0, blockSize).sum(), 1e-6);
    assertEquals(0.5, probs[0], 1e-6);
    IntStream.range(1, blockSize).forEach(i -> assertEquals(0.5 / (blockSize - 1), probs[i], 1e-6));
    IntStream.range(blockSize, probs.length).forEach(i -> assertEquals(average, probs[i], 1e-6));
  }
}
