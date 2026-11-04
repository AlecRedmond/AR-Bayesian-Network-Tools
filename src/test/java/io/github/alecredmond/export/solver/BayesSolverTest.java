package io.github.alecredmond.export.solver;

import static io.github.alecredmond.TestConfigs.DOUBLE_EQUALITY;
import static org.junit.jupiter.api.Assertions.*;

import io.github.alecredmond.export.constraints.ProbabilityConstraint;
import io.github.alecredmond.export.network.BayesianNetwork;
import io.github.alecredmond.export.network.BayesianNetworkBuilder;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.probabilitytables.ConditionalTable;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import io.github.alecredmond.export.probabilitytables.RootNodeTable;
import io.github.alecredmond.export.probabilitytables.cptentry.CptEntry;
import io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter.ConditionalTableConstraintSorter;
import io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter.CptConstraintSorter;
import io.github.alecredmond.internal.method.solver.cptmapper.constraintsorter.RootConstraintSorter;
import io.github.alecredmond.networkscenario.NetworkScenario;
import io.github.alecredmond.networkscenario.NetworkScenarioType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;

@Slf4j
class BayesSolverTest {

  public static Stream<Arguments> streamScenarios() {
    return Arrays.stream(NetworkScenarioType.values())
        .map(NetworkScenarioType::get)
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource("streamScenarios")
  void testSolvesTwiceWithSameResult(NetworkScenario scenario) {
    BayesianNetworkBuilder builder = scenario.getBuilder();
    List<Node> orderedNodes = Arrays.stream(scenario.getOrderedNodes()).toList();
    List<TableAnalyser> jtaCptEntries =
        getTableAnalysers(orderedNodes, builder, new IPFPJunctionTreeSolver());
    List<TableAnalyser> staCptEntries =
        getTableAnalysers(orderedNodes, builder, new IPFPSingleTableSolver());
    if (scenario.getDefaultSolver() instanceof DirectCptSolver directCptSolver) {
      List<TableAnalyser> directSolveCptEntries =
          getTableAnalysers(orderedNodes, builder, directCptSolver);
      assertListsEqual(directSolveCptEntries, jtaCptEntries);
      assertListsEqual(directSolveCptEntries, staCptEntries);
    }
    assertListsEqual(jtaCptEntries, staCptEntries);
  }

  private static List<TableAnalyser> getTableAnalysers(
      List<Node> orderedNodes, BayesianNetworkBuilder builder, BayesSolver solver) {
    BayesianNetwork network = builder.build(solver).orElseThrow();
    Set<ProbabilityConstraint> constraints = network.getConstraints();
    return orderedNodes.stream()
        .map(network::getNetworkTable)
        .map(table -> buildTableAnalyser(table, constraints))
        .toList();
  }

  private void assertListsEqual(List<TableAnalyser> analysers1, List<TableAnalyser> analysers2) {
    int bound = analysers1.size();
    for (int i = 0; i < bound; i++) {
      checkPerTable(analysers1.get(i), analysers2.get(i));
    }
  }

  private static TableAnalyser buildTableAnalyser(
      NetworkTable table, Set<ProbabilityConstraint> constraints) {
    List<CptEntry> cptEntries = table.getCptEntries();
    List<ProbabilityConstraint> tableConstraints;
    switch (table) {
      case ConditionalTable ct ->
          tableConstraints =
              getTableConstraints(ct, constraints, ConditionalTableConstraintSorter::new);
      case RootNodeTable rnt ->
          tableConstraints = getTableConstraints(rnt, constraints, RootConstraintSorter::new);
      default -> throw new IllegalStateException("Unexpected value: " + table);
    }
    return new TableAnalyser(cptEntries, tableConstraints);
  }

  private static void checkPerTable(TableAnalyser ta1, TableAnalyser ta2) {
    boolean allcorrect = true;
    List<CptEntry> entries1 = ta1.cptEntries;
    List<CptEntry> entries2 = ta2.cptEntries;
    Queue<ProbabilityConstraint> it = new ArrayDeque<>(ta1.constraints);
    final int size = entries1.size();
    for (int i = 0; i < size; i++) {
      CptEntry e1 = entries1.get(i);
      CptEntry e2 = entries2.get(i);
      assertEquals(e1.eventState(), e2.eventState());
      assertEquals(e1.conditionStates(), e2.conditionStates());
      allcorrect &= solverMethodsEqual(e1, e2, constraintMatchingEntry(it, e1));
    }
    assertTrue(allcorrect);
  }

  private static <T extends NetworkTable> List<ProbabilityConstraint> getTableConstraints(
      T table,
      Set<ProbabilityConstraint> allConstraints,
      Function<T, CptConstraintSorter<? extends ProbabilityConstraint, T>> sorterConstructor) {
    return sorterConstructor.apply(table).sortConstraints(allConstraints).stream()
        .map(ProbabilityConstraint.class::cast)
        .toList();
  }

  private static boolean solverMethodsEqual(
      CptEntry e1, CptEntry e2, ProbabilityConstraint constraint) {
    boolean allcorrect = true;
    try {
      Optional.ofNullable(constraint)
          .ifPresent(
              c -> {
                assertEquals(e1.probability(), c.getProbability(), DOUBLE_EQUALITY);
                assertEquals(e2.probability(), c.getProbability(), DOUBLE_EQUALITY);
              });
    } catch (AssertionFailedError e) {
      allcorrect = false;
      System.out.printf("DIVERGE FROM CONSTRAINT%n%s%n%s%n", e1, e2);
    }
    return allcorrect;
  }

  private static ProbabilityConstraint constraintMatchingEntry(
      Queue<ProbabilityConstraint> queue, CptEntry e1) {
    if (queue.isEmpty()) {
      return null;
    }
    return Optional.of(queue.peek())
        .filter(c -> c.getEventStates().contains(e1.eventState()))
        .filter(c -> c.getConditionStates().containsAll(e1.conditionStates()))
        .map(c -> queue.poll())
        .orElse(null);
  }

  record TableAnalyser(List<CptEntry> cptEntries, List<ProbabilityConstraint> constraints) {}
}
