package io.github.alecredmond.internal.method.junctiontree;

import io.github.alecredmond.internal.application.junctiontree.Clique;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import io.github.alecredmond.internal.application.junctiontree.Separator;
import java.util.*;
import java.util.function.Function;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MessagePasser {
  private final JunctionTreeData data;

  public void collectMessages(Clique collectTo) {
    runMessagePassers(collectTo, data.getCollectionRuns(), this::buildCollectionRuns);
  }

  private void runMessagePassers(
      Clique origin, Runnable[][] nestedArray, Function<Clique, Runnable[]> buildIfNull) {
    Runnable[] toRun = nestedArray[origin.getCliqueIndex()];
    if (toRun == null) {
      toRun = buildIfNull.apply(origin);
      nestedArray[origin.getCliqueIndex()] = toRun;
    }
    Arrays.stream(toRun).forEach(Runnable::run);
  }

  private Runnable[] buildCollectionRuns(Clique startClique) {
    RunBuilder rb = (clique, nextClique, separator) -> () -> separator.passMessageFrom(nextClique);
    List<Runnable> collectionRuns = buildRuns(startClique, rb);
    return collectionRuns.reversed().toArray(Runnable[]::new);
  }

  private List<Runnable> buildRuns(Clique startClique, RunBuilder runBuilder) {
    Queue<Clique> queue = new ArrayDeque<>();
    Set<Clique> visited = new HashSet<>();
    List<Runnable> runs = new ArrayList<>();
    queue.add(startClique);
    while (!queue.isEmpty()) {
      Clique clique = queue.poll();
      if(!visited.add(clique)) continue;
      clique
          .getSeparatorMap()
          .forEach(
              (nextClique, separator) -> {
                if (visited.contains(nextClique)) return;
                runs.add(runBuilder.apply(clique, nextClique, separator));
                queue.add(nextClique);
              });
    }
    return runs;
  }

  public void distributeMessages(Clique distributeFrom) {
    runMessagePassers(distributeFrom, data.getDistributionRuns(), this::buildDistributionRuns);
  }

  private Runnable[] buildDistributionRuns(Clique startClique) {
    RunBuilder rb = (clique, nextClique, separator) -> () -> separator.passMessageFrom(clique);
    List<Runnable> distributionRuns = buildRuns(startClique, rb);
    return distributionRuns.toArray(Runnable[]::new);
  }

  @FunctionalInterface
  interface RunBuilder {
    Runnable apply(Clique clique, Clique nextClique, Separator separator);
  }
}
