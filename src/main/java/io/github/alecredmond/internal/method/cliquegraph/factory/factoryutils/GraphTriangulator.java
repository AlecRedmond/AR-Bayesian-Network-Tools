package io.github.alecredmond.internal.method.cliquegraph.factory.factoryutils;

import java.util.*;

/**
 * General Minimal Triangulation algorithm. This class has been made generic for simpler unit tests.
 * Algorithm source:
 *
 * <p><i>Do Le Paul Minh, Truong Thi Thuy Duong, Bayesian networks: The minimal triangulations of a
 * graph, Theoretical Computer Science, Volume 795, 2019, Pages 1-8, ISSN 0304-3975.</i>
 *
 * <p>The algorithm has been annotated with numbers corresponding to the numbered steps in the GMT
 * section.
 */
public class GraphTriangulator<T> {
  public Map<T, List<T>> getFillInEdges(Map<T, ? extends Collection<T>> edges) {
    // 1.
    Map<T, Set<T>> graph = new HashMap<>();
    Map<T, Integer> labels = new HashMap<>();
    Map<T, List<T>> fillInEdges = new HashMap<>();

    edges.forEach(
        (t, tEdges) -> {
          graph.put(t, new HashSet<>(tEdges));
          labels.put(t, 0);
          fillInEdges.put(t, new ArrayList<>());
        });

    // 2.
    Set<T> unnumbered = new HashSet<>(graph.keySet());
    Map<T, Integer> eliminationOrder = new HashMap<>();
    int totalTs = edges.size();

    T startingT = selectFirstByLeastConnected(graph);
    unnumbered.remove(startingT);
    eliminationOrder.put(startingT, totalTs);
    updateNeighbourLabels(startingT, graph, labels);

    Queue<ComponentState<T>> activeComponents = new ArrayDeque<>();
    Set<T> initialComponent = new HashSet<>(unnumbered);
    Set<T> initialNeighbourhood = graph.get(startingT);
    activeComponents.add(new ComponentState<>(initialComponent, initialNeighbourhood));

    // 3.
    for (int orderNumber = totalTs; orderNumber > 1; orderNumber--) {
      // 3.a
      ComponentState<T> currentState = Optional.ofNullable(activeComponents.poll()).orElseThrow();
      Set<T> neighbourhood = currentState.neighbourhood;
      Set<T> component = currentState.component;
      T mu = neighbourhood.stream().max(Comparator.comparingInt(labels::get)).orElseThrow();

      // 3.b
      neighbourhood.stream()
          .filter(theta -> !theta.equals(mu))
          .filter(theta -> !graph.get(theta).contains(mu))
          .filter(theta -> hasQualifyingPath(theta, mu, component, graph, labels))
          .forEach(
              theta -> {
                graph.get(theta).add(mu);
                graph.get(mu).add(theta);
                fillInEdges.get(theta).add(mu);
                fillInEdges.get(mu).add(theta);
              });

      // 3.c
      unnumbered.remove(mu);
      eliminationOrder.put(mu, orderNumber - 1);

      // 3.d
      updateNeighbourLabels(mu, graph, labels);

      // 3.e
      component.remove(mu);
      for (Set<T> subComp : findConnectedComponents(component, graph)) {
        Set<T> subNeighbourhood = new HashSet<>();
        subComp.stream()
            .filter(t -> graph.get(t).stream().anyMatch(eliminationOrder::containsKey))
            .forEach(subNeighbourhood::add);
        if (subNeighbourhood.isEmpty()) continue;
        activeComponents.add(new ComponentState<>(subComp, subNeighbourhood));
      }
    }
    edges.keySet().stream().filter(t -> fillInEdges.get(t).isEmpty()).forEach(fillInEdges::remove);
    return fillInEdges;
  }

  private T selectFirstByLeastConnected(Map<T, Set<T>> graph) {
    return graph.entrySet().stream()
        .min(Comparator.comparingInt(e -> e.getValue().size()))
        .map(Map.Entry::getKey)
        .orElseThrow();
  }

  private void updateNeighbourLabels(T current, Map<T, Set<T>> graph, Map<T, Integer> labels) {
    graph.get(current).forEach(node -> labels.merge(node, 1, Integer::sum));
  }

  private boolean hasQualifyingPath(
      T start, T target, Set<T> component, Map<T, Set<T>> graph, Map<T, Integer> labels) {
    int maxAllowedLabel = labels.get(start) - 1;
    Set<T> visited = new HashSet<>();
    Queue<T> queue = new ArrayDeque<>();
    visited.add(start);
    queue.add(start);

    while (!queue.isEmpty()) {
      T current = queue.poll();
      for (T neighbour : graph.get(current)) {
        if (neighbour.equals(target)) return true;
        if (!component.contains(neighbour)
            || visited.contains(neighbour)
            || labels.get(neighbour) > maxAllowedLabel) {
          continue;
        }
        queue.add(neighbour);
        visited.add(neighbour);
      }
    }
    return false;
  }

  private List<Set<T>> findConnectedComponents(Set<T> component, Map<T, Set<T>> graph) {
    List<Set<T>> subComponents = new ArrayList<>();
    Set<T> unvisited = new HashSet<>(component);

    while (!unvisited.isEmpty()) {
      T start = unvisited.iterator().next();

      Set<T> currentComponent = new HashSet<>();
      Queue<T> queue = new ArrayDeque<>();

      queue.add(start);
      unvisited.remove(start);
      currentComponent.add(start);

      while (!queue.isEmpty()) {
        graph.get(queue.poll()).stream()
            .filter(unvisited::contains)
            .forEach(
                neighbour -> {
                  unvisited.remove(neighbour);
                  currentComponent.add(neighbour);
                  queue.add(neighbour);
                });
      }
      subComponents.add(currentComponent);
    }
    return subComponents;
  }

  private record ComponentState<T>(Set<T> component, Set<T> neighbourhood) {}
}
