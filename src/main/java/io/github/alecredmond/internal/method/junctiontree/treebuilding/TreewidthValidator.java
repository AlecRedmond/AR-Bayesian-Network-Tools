package io.github.alecredmond.internal.method.junctiontree.treebuilding;

import io.github.alecredmond.exceptions.TreewidthException;
import io.github.alecredmond.export.cartesianvector.CartesianVariable;
import io.github.alecredmond.export.node.Node;
import io.github.alecredmond.internal.application.junctiontree.JunctionTreeData;
import java.util.Collection;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreewidthValidator {
  private static final double LOG_INT_MAX = Math.log(Integer.MAX_VALUE);

  public void verifyClique(Set<Node> nodes, JunctionTreeData jtd) {
    verifyCliques(Set.of(nodes), jtd);
  }

  public void verifyCliques(Collection<Set<Node>> nodeSets, JunctionTreeData jtd) {
    verifyTreeWidth(
        nodeSets.stream().mapToDouble(TreewidthValidator::getLogCardinality).max().orElseThrow(),
        jtd);
  }

  private void verifyTreeWidth(double logMaxTableRank, JunctionTreeData jtd) {
    double equivalentTreeWidth = logMaxTableRank / Math.log(2);
    jtd.setEquivalentTreeWidth(equivalentTreeWidth);
    if (logMaxTableRank <= LOG_INT_MAX) return;
    throw new TreewidthException(
        "Equivalent Treewidth (2^%.2f) would require an array longer than 2^31 - 1!"
            .formatted(equivalentTreeWidth));
  }

  private static <N extends CartesianVariable> double getLogCardinality(Collection<N> nodes) {
    return nodes.stream().mapToInt(n -> n.getStates().size()).mapToDouble(Math::log).sum();
  }

  public static <N extends CartesianVariable> boolean validateVectorLength(Collection<N> nodes) {
    return getLogCardinality(nodes) <= LOG_INT_MAX;
  }
}
