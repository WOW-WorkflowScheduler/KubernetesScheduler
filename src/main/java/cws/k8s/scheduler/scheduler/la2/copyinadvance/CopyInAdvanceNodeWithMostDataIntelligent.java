package cws.k8s.scheduler.scheduler.la2.copyinadvance;

import cws.k8s.scheduler.model.NodeWithAlloc;
import cws.k8s.scheduler.model.Task;
import cws.k8s.scheduler.model.location.NodeLocation;
import cws.k8s.scheduler.scheduler.filealignment.InputAlignment;
import cws.k8s.scheduler.scheduler.la2.CreateCopyTasks;
import cws.k8s.scheduler.scheduler.la2.TaskStat;
import cws.k8s.scheduler.util.NodeTaskFilesAlignment;
import cws.k8s.scheduler.util.SortedList;
import cws.k8s.scheduler.util.TaskStats;
import cws.k8s.scheduler.util.copying.CurrentlyCopying;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CopyInAdvanceNodeWithMostDataIntelligent extends CopyInAdvance {

    /**
     * Try to distribute the tasks evenly on the nodes. More important tasks can be on x more nodes. (x = TASKS_READY_FACTOR)
     */
    private final int TASKS_READY_FACTOR = 1;

    public CopyInAdvanceNodeWithMostDataIntelligent(
            CurrentlyCopying currentlyCopying,
            InputAlignment inputAlignment,
            int copySameTaskInParallel ) {
        super( currentlyCopying, inputAlignment, copySameTaskInParallel );
    }


    /**
     * Do not filter maxHeldCopyTaskReady, that could lead to starving if another node has resources.
     */
    public void createAlignmentForTasksWithEnoughCapacity(
            final List<NodeTaskFilesAlignment> nodeTaskFilesAlignments,
            final TaskStats taskStats,
            final CurrentlyCopying planedToCopy,
            final List<NodeWithAlloc> allNodes,
            final int maxCopyingTaskPerNode,
            final int maxHeldCopyTaskReady,
            final Map<NodeLocation, Integer> currentlyCopyingTasksOnNode,
            int prio
    ) {
        final SortedList<TaskStat> stats = new SortedList<>( taskStats.getTaskStats() );
        removeTasksThatAreCopiedMoreThanXTimeCurrently( stats, copySameTaskInParallel );

        int readyOnNodes = 0;
        //Outer loop to only process tasks that are not yet ready on enough nodes
        while( !stats.isEmpty() ) {
            LinkedList<TaskStat> tasksReadyOnMoreNodes = new LinkedList<>();
            while( !stats.isEmpty() ) {
                final TaskStat poll = stats.poll();

                if ( poll.dataOnNodes() + TASKS_READY_FACTOR > readyOnNodes ) {
                    tasksReadyOnMoreNodes.add( poll );
                    continue;
                }

                final TaskStat.NodeAndStatWrapper bestStats = poll.getBestStats();
                final Task task = poll.getTask();
                final NodeWithAlloc node = bestStats.getNode();

                final boolean cannotAdd;

                //Check if the node has still enough resources to run the task
                if ( currentlyCopyingTasksOnNode.getOrDefault( node.getNodeLocation(), 0 ) < maxCopyingTaskPerNode ) {
                    if ( createFileAlignment( planedToCopy, nodeTaskFilesAlignments, currentlyCopyingTasksOnNode, poll, task, node, prio ) ) {
                        cannotAdd = false;
                        log.info( "Start copy task with {} missing bytes", poll.getBestStats().getTaskNodeStats().getSizeRemaining() );
                    } else {
                        cannotAdd = true;
                    }
                } else {
                    cannotAdd = true;
                }
                //if not enough resources or too many tasks are running, mark next node as to compare and add again into the list
                if ( cannotAdd && poll.increaseIndexToCompare() ) {
                    //Only re-add if still other opportunities exist
                    stats.add( poll );
                }
            }
            stats.addAll( tasksReadyOnMoreNodes );
            readyOnNodes++;
        }
    }

}
