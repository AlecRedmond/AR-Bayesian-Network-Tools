package io.github.alecredmond.internal.method.vectoriterator.misciterators;

import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.ProbabilityVector;
import io.github.alecredmond.export.probabilitytables.tableentry.TableEntry;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.ProbabilityIteratorTemplate;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StandardCartesianIterator;
import io.github.alecredmond.internal.method.vectoriterator.standardtemplate.StateUpdateBase;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public class TableEntryGenerator extends StateUpdateBase<Node, NodeState>
    implements ProbabilityIteratorTemplate {
  private final StandardCartesianIterator it;
  private final double[] probs;

  public TableEntryGenerator(ProbabilityVector vector) {
    super(vector, NodeState[]::new);
    this.it = StandardCartesianIterator.create(this, vector);
    this.probs = vector.getProbabilities();
  }

  public TableEntry[] buildEntries() {
    final int l = states.length;
    final TableEntry[] entries = new TableEntry[probs.length];
    it.iterateInner(i -> entries[i] = new TableEntry(Arrays.copyOf(states, l), i, probs[i]));
    return entries;
  }

  @Override
  public Function<Node, NodeState> initialStatePositionSetter() {
    return node -> node.getStates().getFirst();
  }

  @Override
  public Predicate<Node> checkLockOuter() {
    return node -> true;
  }

  @Override
  public Predicate<Node> checkLockInner() {
    return node -> false;
  }

  @Override
  public Function<Node, boolean[]> updateEvidenceArrays() {
    return node -> new boolean[0];
  }
}
