package object.network;

import component.MovementComponent;

import java.util.HashMap;

public class Junction extends NetworkObject {
    protected HashMap<MovementComponent.Heading, String> tracks = new HashMap<>();

    /**
     * Constructor
     * @param data json file string
     */
    public Junction(String data) { super(data); }

    /**
     * Add track which starts/ends on this junction
     * @param heading heading of the track
     * @param trackId id of the track
     */
    public void addTrack(MovementComponent.Heading heading, String trackId) {
        tracks.put(heading, trackId);
    }

    /**
     * Get track for specific heading
     * @param heading heading of the track
     * @param invertHeading true if heading should be inverted (N->S, W->E)
     * @return id of the track
     */
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

    /**
     * @return all tracks with corresponding headings
     */
    public HashMap<MovementComponent.Heading, String> getTracks() {
        return tracks;
    }
}
