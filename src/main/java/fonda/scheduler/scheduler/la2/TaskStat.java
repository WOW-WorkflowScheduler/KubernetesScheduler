package fonda.scheduler.scheduler.la2;

import fonda.scheduler.model.NodeWithAlloc;
import fonda.scheduler.model.Task;
import fonda.scheduler.model.taskinputs.TaskInputs;
import fonda.scheduler.util.TaskNodeStats;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class TaskStat implements Comparable<TaskStat> {

    @Getter
    private final Task task;
    @Getter
    private final TaskInputs inputsOfTask;
    private final TaskStatComparator comparator;
    private final List<NodeAndStatWrapper> taskStats = new ArrayList<>();
    @Getter
    private int completeOnNodes = 0;
    @Getter
    private int copyingToNodes = 0;
    private boolean finished = false;
    private int indexToCompare = 0;

    /**
     * Call this to sort the taskStats list
     */
    public void finish() {
        finished = true;
        taskStats.sort( comparator.getComparator() );
    }

    /**
     * Increases the index to compare.
     * False if no other opportunity exists
     * @return
     */
    public boolean increaseIndexToCompare() {
        indexToCompare++;
        return indexToCompare < taskStats.size();
    }

    private final AtomicInteger canStartAfterCopying = new AtomicInteger( 0 );

    public void add( NodeWithAlloc node, TaskNodeStats taskNodeStats ) {
        assert !finished;
        this.taskStats.add( new NodeAndStatWrapper( node, taskNodeStats ) );
        if ( taskNodeStats.allOnNode() ) {
            this.completeOnNodes++;
        } else if ( taskNodeStats.allOnNodeOrCopying() ) {
            this.copyingToNodes++;
        }
    }

    /**
     * Mark this task as it is currently copying to a node with enough available resources.
     */
    public void canStartAfterCopying() {
        this.canStartAfterCopying.incrementAndGet();
    }

    public int getCanStartAfterCopying() {
        return this.canStartAfterCopying.get();
    }

    public NodeAndStatWrapper getBestStats() {
        assert finished;
        assert indexToCompare < taskStats.size();
        return taskStats.get( indexToCompare );
    }

    @Override
    public int compareTo( @NotNull TaskStat o ) {
        if ( indexToCompare >= taskStats.size() || o.indexToCompare >= o.taskStats.size() ) {
            throw new IllegalStateException( "Cannot compare tasks that have no stats" );
        }
        return comparator.compare( this, o );
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NodeAndStatWrapper {
        private final NodeWithAlloc node;
        private final TaskNodeStats taskNodeStats;
    }


}