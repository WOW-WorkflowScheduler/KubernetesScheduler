package fonda.scheduler.util.copying;

import fonda.scheduler.model.Task;
import fonda.scheduler.model.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CurrentlyCopyingOnNode {

    private final Map< String, CopySource> currentlyCopying = new HashMap<>();

    public void add( String path, Task task, Location location ) {
        synchronized ( this.currentlyCopying ) {
            if ( !this.currentlyCopying.containsKey( path ) ) {
                this.currentlyCopying.put( path, new CopySource( task, location ) );
            } else {
                throw new IllegalStateException( "Already copying " + path );
            }
        }
    }

    public boolean isCurrentlyCopying( String path ) {
        synchronized ( this.currentlyCopying ) {
            return this.currentlyCopying.containsKey( path );
        }
    }

    public CopySource getCopySource( String path ) {
        synchronized ( this.currentlyCopying ) {
            return this.currentlyCopying.get( path );
        }
    }

    void add ( CurrentlyCopyingOnNode currentlyCopying ) {
        synchronized ( this.currentlyCopying ) {
            this.currentlyCopying.putAll( currentlyCopying.currentlyCopying );
        }
    }

    void remove ( CurrentlyCopyingOnNode currentlyCopying ) {
        synchronized ( this.currentlyCopying ) {
            this.currentlyCopying.keySet().removeAll( currentlyCopying.currentlyCopying.keySet() );
        }
    }

    public Set<String> getAllFilesCurrentlyCopying() {
        synchronized ( this.currentlyCopying ) {
            return this.currentlyCopying.keySet();
        }
    }

    public boolean isEmpty() {
        synchronized ( this.currentlyCopying ) {
            return this.currentlyCopying.isEmpty();
        }
    }

    @Override
    public String toString() {
        return getAllFilesCurrentlyCopying().toString();
    }
}