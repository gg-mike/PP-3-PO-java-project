package object.network;

import data.MovementComponent;

import java.util.HashMap;

public class Junction extends NetworkObject {
    protected HashMap<MovementComponent.Heading, String> tracks = new HashMap<>();

    public Junction(String data) { super(data); }

    public void addTrack(MovementComponent.Heading heading, String trackId) {
        tracks.put(heading, trackId);
    }

    public String getTrack(MovementComponent.Heading heading, boolean invertHeading) {
        if (invertHeading) {
            switch (heading) {
                case N -> { return tracks.get(MovementComponent.Heading.S); }
                case E -> { return tracks.get(MovementComponent.Heading.W); }
                case S -> { return tracks.get(MovementComponent.Heading.N); }
                case W -> { return tracks.get(MovementComponent.Heading.E); }
            }
        }
        return tracks.get(heading);
    }

    public HashMap<MovementComponent.Heading, String> getTracks() {
        return tracks;
    }
}
