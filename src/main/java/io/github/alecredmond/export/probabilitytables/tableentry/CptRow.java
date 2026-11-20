package io.github.alecredmond.export.probabilitytables.tableentry;

import io.github.alecredmond.export.node.NodeState;
import io.github.alecredmond.export.probabilitytables.NetworkTable;
import java.util.List;
import java.util.Set;

/**
 * Groups every {@link CptEntry} that is evaluated under a single combination of parent condition
 * states within a {@link NetworkTable}.
 *
 * @author Alec Redmond
 * @param conditionStates a specific combination of conditioning {@link NodeState}s that applies to
 *     the entire row
 * @param rowEntries all CPT entries subject to the conditioning states
 * @param rowStartIndex the index of the first row entry in the probability {@code double[]} array
 *     of the associated {@code NetworkTable}
 * @see CptEntry
 */
public record CptRow(
    Set<NodeState> conditionStates, List<CptEntry> rowEntries, int rowStartIndex) {}
