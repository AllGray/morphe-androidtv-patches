package ajstrick81.morphe.extension.pluto17;

/**
 * Pluto TV 17 phone build: Compose's CornerBasedShape.createOutline throws "Corner size in Px can't be
 * negative(topStart = NaN, ...)" when a corner radius works out to NaN (seen in the tablet-width layout
 * of the series page). The patch passes the four computed corner sizes through here twice: right after they
 * are computed, and again just before the check (the scaling in between turns Infinity * 0 into NaN). A NaN,
 * negative or infinite radius becomes 0 (a square corner) instead of killing the app.
 */
public final class PlutoComposeFix {
    private PlutoComposeFix() {}

    /** Called with each corner size in px; NaN, negative and infinite values become 0. */
    public static float nanToZero(float px) {
        return (px >= 0f && px < Float.POSITIVE_INFINITY) ? px : 0f;
    }
}
