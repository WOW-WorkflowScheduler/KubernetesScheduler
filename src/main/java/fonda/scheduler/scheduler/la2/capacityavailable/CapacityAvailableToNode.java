package fonda.scheduler.scheduler.la2.capacityavailable;

import fonda.scheduler.model.NodeWithAlloc;
import fonda.scheduler.model.Requirements;
import fonda.scheduler.util.NodeTaskFilesAlignment;
import fonda.scheduler.util.TaskStats;
import fonda.scheduler.util.copying.CurrentlyCopying;

import java.util.List;
import java.util.Map;

public interface CapacityAvailableToNode {

    List<NodeTaskFilesAlignment> createAlignmentForTasksWithEnoughCapacity(
            final TaskStats taskStats,
            final CurrentlyCopying planedToCopy,
            final Map<NodeWithAlloc, Requirements> availableByNodes,
            final List<NodeWithAlloc> allNodes,
            final int maxCopyingTaskPerNode
    );

}