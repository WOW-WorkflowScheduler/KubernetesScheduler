package fonda.scheduler.scheduler.la2.copystrategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fonda.scheduler.client.KubernetesClient;
import fonda.scheduler.scheduler.LocationAwareSchedulerV2;
import fonda.scheduler.util.CopyTask;
import fonda.scheduler.util.LogCopyTask;
import fonda.scheduler.util.NodeTaskFilesAlignment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ShellCopy implements CopyRunner {

    private final KubernetesClient client;
    private final LocationAwareSchedulerV2 scheduler;
    private final LogCopyTask logCopyTask;

    @Override
    public void startCopyTasks( final CopyTask copyTask, final NodeTaskFilesAlignment nodeTaskFilesAlignment ) {
        String[] command = new String[3];
        command[0] = "/bin/bash";
        command[1] = "-c";
        command[2] = "cd " + nodeTaskFilesAlignment.task.getWorkingDir() + " && ";
        final String json;
        try {
            json = new ObjectMapper()
                    .writeValueAsString( copyTask.getInputs() )
                    .replace( "\"", "\\\"" );
        } catch ( JsonProcessingException e ) {
            throw new RuntimeException( e );
        }
        command[2] += "/code/ftp.py false " + nodeTaskFilesAlignment.node.getName() + " \"" + json + "\"";
        String name = nodeTaskFilesAlignment.task.getConfig().getName() + "-copy-" + nodeTaskFilesAlignment.node.getName();
        log.info( "Starting {} to node {}", nodeTaskFilesAlignment.task.getConfig().getName(), nodeTaskFilesAlignment.node.getName() );
        logCopyTask.copy( nodeTaskFilesAlignment.task.getConfig().getName(), nodeTaskFilesAlignment.node.getName(), copyTask.getInputFiles().size(), "start" );
        client.execCommand( scheduler.getDaemonNameOnNode( copyTask.getNodeLocation().getIdentifier() ), scheduler.getNamespace(), command, new LaListener( copyTask, name, nodeTaskFilesAlignment, scheduler, logCopyTask ) );
    }

}