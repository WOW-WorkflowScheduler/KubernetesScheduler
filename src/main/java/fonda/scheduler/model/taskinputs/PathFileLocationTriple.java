package fonda.scheduler.model.taskinputs;

import fonda.scheduler.model.location.hierachy.LocationWrapper;
import fonda.scheduler.model.location.hierachy.RealFile;

import java.nio.file.Path;
import java.util.List;

public class PathFileLocationTriple implements Input {

    public final Path path;
    public final RealFile file;
    public final List<LocationWrapper> locations;

    public PathFileLocationTriple(Path path, RealFile file, List<LocationWrapper> locations) {
        this.path = path;
        this.file = file;
        this.locations = locations;
    }
}