package io.github.alecredmond.documentation;

import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.inference.direct.InferenceEngine;
import io.github.alecredmond.export.inference.direct.JunctionTreeAlgorithm;
import io.github.alecredmond.export.inference.indirect.LikelihoodWeightingSampler;
import io.github.alecredmond.export.inference.indirect.MonteCarloSampler;
import io.github.alecredmond.export.inference.indirect.SampleCollection;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.network.cptdefinition.OrderedCptDefinition;
import io.github.alecredmond.export.node.BinaryNode;
import io.github.alecredmond.export.node.BinaryStateOrder;
import io.github.alecredmond.export.node.DefaultBinaryNode;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.solver.DirectCptWriter;
import io.github.alecredmond.export.solver.IPFPJunctionTreeSolver;
import io.github.alecredmond.export.solver.MultiStageSolver;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ReadMeQuickStartTest {

  @Test
  void testQuickStart_shouldOutputExpected() {
    // ----- Create the nodes / Define the structure
    BinaryStateOrder stateOrder = BinaryStateOrder.TRUE_FIRST;
    BinaryNode rain = new DefaultBinaryNode("RAIN", stateOrder);
    BinaryNode sprinkler = new DefaultBinaryNode("SPRINKLER", List.of(rain), stateOrder);
    BinaryNode wetGrass = new DefaultBinaryNode("WET GRASS", List.of(rain, sprinkler), stateOrder);

    // ----- Create a network builder
    BayesianNetworkBuilder builder =
        BayesianNetwork.builder()
            .setNetworkName("RAIN, SPRINKLER, WET GRASS")
            .setNodes(rain, sprinkler, wetGrass);

    // ----- Constrain the network
    builder
        .addConstraint(rain.trueState(), 0.2)
        .addConstraint(sprinkler.trueState(), rain.trueState(), 0.01)
        .addConstraint(sprinkler.trueState(), rain.falseState(), 0.4)
        .addConstraint(wetGrass.trueState(), List.of(rain.trueState(), sprinkler.trueState()), 0.99)
        .addConstraint(wetGrass.trueState(), List.of(rain.trueState(), sprinkler.falseState()), 0.8)
        .addConstraint(wetGrass.trueState(), List.of(rain.falseState(), sprinkler.trueState()), 0.9)
        .addConstraint(wetGrass.trueState(), List.of(rain.falseState(), sprinkler.falseState()), 0);

    // ----- Solve the network
    // Directly infer the CPT entries
    Optional<BayesianNetwork> optNetwork = builder.build(new DirectCptWriter());

    assertTrue(optNetwork.isPresent());

    // Perform an IPFP run
    optNetwork = builder.build(new IPFPJunctionTreeSolver());

    assertTrue(optNetwork.isPresent());

    // Attempt direct inference, or run an IPFP solver if that fails
    optNetwork = builder.build(new MultiStageSolver());

    assertTrue(optNetwork.isPresent());

    BayesianNetwork network = optNetwork.get();

    // ----- Analyse the CPTs
    NetworkTable sprinklerCPT = network.getNetworkTableByNodeName("SPRINKLER");
    double pSprinklerFalseGivenRain =
        sprinklerCPT.getProbability(sprinkler.falseState(), rain.trueState());
    System.out.printf("P(!S|R) = %.2f%n", pSprinklerFalseGivenRain);

    assertEquals(0.99, pSprinklerFalseGivenRain, 1e-3);

    network.printNetwork();

    // ----- Perform direct inference
    InferenceEngine engine = new JunctionTreeAlgorithm(network);
    engine.setObserved(wetGrass.trueState());
    double posteriorRainTrue = engine.getPosteriorProbability(rain.trueState());
    System.out.printf("P(R|W) = %.3f%n", posteriorRainTrue);

    assertEquals(0.358, posteriorRainTrue, 1e-3);

    engine.printObserved();

    // ----- Create random samples
    MonteCarloSampler sampler = new LikelihoodWeightingSampler(network);
    sampler.setObserved(engine.getCurrentObservations());
    SampleCollection sc = sampler.generateSamples(1000);
    int samplesWithRainTrue = sc.countSamplesIncludingStates(rain.trueState());
    System.out.println(samplesWithRainTrue);

    assertEquals(0.358, samplesWithRainTrue / 1000.0, 1e-1);
  }

  @Test
  void buildTablesWithCptDefs() {
    // ----- Create the nodes / Define the structure
    BinaryStateOrder stateOrder = BinaryStateOrder.TRUE_FIRST;
    BinaryNode rain = new DefaultBinaryNode("RAIN", stateOrder);
    BinaryNode sprinkler = new DefaultBinaryNode("SPRINKLER", List.of(rain), stateOrder);
    BinaryNode wetGrass = new DefaultBinaryNode("WET GRASS", List.of(rain, sprinkler), stateOrder);

    // ----- Create a network builder
    BayesianNetworkBuilder builder =
        BayesianNetwork.builder()
            .setNetworkName("RAIN, SPRINKLER, WET GRASS")
            .setNodes(rain, sprinkler, wetGrass);

    double[] rainCpt = new double[] {0.2, 0.8};
    double[] sprinklerCpt = new double[] {0.01, 0.99, 0.4, 0.6};
    double[] wetGrassCpt = new double[] {0.99, 0.01, 0.8, 0.2, 0.9, 0.1, 0.0, 1.0};
    builder
        .defineCPT(new OrderedCptDefinition(rainCpt, rain))
        .defineCPT(new OrderedCptDefinition(sprinklerCpt, rain, sprinkler))
        .defineCPT(new OrderedCptDefinition(wetGrassCpt, rain, sprinkler, wetGrass));

    // ----- Solve the network
    // Directly infer the CPT entries
    Optional<BayesianNetwork> optNetwork = builder.build(new DirectCptWriter());

    assertTrue(optNetwork.isPresent());

    // Perform an IPFP run
    optNetwork = builder.build(new IPFPJunctionTreeSolver());

    assertTrue(optNetwork.isPresent());

    // Attempt direct inference, or run an IPFP solver if that fails
    optNetwork = builder.build(new MultiStageSolver());

    assertTrue(optNetwork.isPresent());

    BayesianNetwork network = optNetwork.get();

    // ----- Analyse the CPTs
    NetworkTable sprinklerCPT = network.getNetworkTableByNodeName("SPRINKLER");
    double pSprinklerFalseGivenRain =
        sprinklerCPT.getProbability(sprinkler.falseState(), rain.trueState());
    System.out.printf("P(!S|R) = %.2f%n", pSprinklerFalseGivenRain);

    assertEquals(0.99, pSprinklerFalseGivenRain, 1e-3);

    network.printNetwork();

    // ----- Perform direct inference
    InferenceEngine engine = new JunctionTreeAlgorithm(network);
    engine.setObserved(wetGrass.trueState());
    double posteriorRainTrue = engine.getPosteriorProbability(rain.trueState());
    System.out.printf("P(R|W) = %.3f%n", posteriorRainTrue);

    assertEquals(0.358, posteriorRainTrue, 1e-3);

    engine.printObserved();

    // ----- Create random samples
    MonteCarloSampler sampler = new LikelihoodWeightingSampler(network);
    sampler.setObserved(engine.getCurrentObservations());
    SampleCollection sc = sampler.generateSamples(1000);
    int samplesWithRainTrue = sc.countSamplesIncludingStates(rain.trueState());
    System.out.println(samplesWithRainTrue);

    assertEquals(0.358, samplesWithRainTrue / 1000.0, 1e-1);
  }
}
