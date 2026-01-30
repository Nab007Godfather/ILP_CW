package uk.ac.ed.acp.cw2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Represents a geographic region defined by a closed polygon.
public class Region {

    @NotNull(message = "Region name cannot be null")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "Vertices cannot be null")
    @NotEmpty(message = "Vertices cannot be empty")
    @Valid
    @JsonProperty("vertices")
    private List<LngLat> vertices;

    public Region() {
    }

    public Region(String name, List<LngLat> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LngLat> getVertices() {
        return vertices;
    }

    public void setVertices(List<LngLat> vertices) {
        this.vertices = vertices;
    }

    // Checking if the region forms a closed polygon
    // only if the first vertex equals last vertex
    public boolean isClosed() {
        if (vertices == null || vertices.size() < 4) {
            return false;
        }

        LngLat first = vertices.get(0);
        LngLat last = vertices.get(vertices.size() - 1);

        return first.getLng().equals(last.getLng()) &&
                first.getLat().equals(last.getLat());
    }

    // Validating the region data
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (vertices == null || vertices.isEmpty()) {
            return false;
        }

        // Checking if all vertices are valid
        for (LngLat vertex : vertices) {
            if (!vertex.isValid()) {
                return false;
            }
        }

        // Checking if region is closed
        return isClosed();
    }

    @Override
    public String toString() {
        return String.format("Region{name='%s', vertices=%d}", name,
                vertices != null ? vertices.size() : 0);
    }
}
