package juitar.gwrexpansions.client.render;

import org.joml.Vector4f;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/** Clip textured faces at the animated sigil plane; UVs stay continuous at the cut. */
public final class IgnitiumSoulSweep {
    private IgnitiumSoulSweep() {}

    public record Vertex(float x, float y, float z, float u, float v) {
        private Vertex lerp(Vertex other, float t) {
            return new Vertex(x + (other.x - x) * t, y + (other.y - y) * t,
                    z + (other.z - z) * t, u + (other.u - u) * t, v + (other.v - v) * t);
        }
    }

    public static Vector4f planeInRenderSpace(Matrix4f inverseRoot, float depth) {
        return new Vector4f(inverseRoot.m02(), inverseRoot.m12(), inverseRoot.m22(), inverseRoot.m32() - depth);
    }

    public static List<Vertex> clip(List<Vertex> polygon, Vector4f plane, boolean soul) {
        if (polygon.isEmpty()) return polygon;
        List<Vertex> result = new ArrayList<>(6);
        Vertex previous = polygon.get(polygon.size() - 1);
        float previousDistance = distance(previous, plane, soul);
        for (Vertex vertex : polygon) {
            float distance = distance(vertex, plane, soul);
            if ((previousDistance >= 0) != (distance >= 0))
                result.add(previous.lerp(vertex, previousDistance / (previousDistance - distance)));
            if (distance >= 0) result.add(vertex);
            previous = vertex;
            previousDistance = distance;
        }
        return result;
    }

    private static float distance(Vertex vertex, Vector4f plane, boolean soul) {
        float distance = plane.x * vertex.x + plane.y * vertex.y + plane.z * vertex.z + plane.w;
        return soul ? distance : -distance;
    }
}
