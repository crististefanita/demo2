package atf.ui.autoex.support;

import java.util.List;

/** Separates explicit setup exchanges from later browser-causal network evidence. */
public final class AutoExApiTrafficWindow {
    private int causalStart;

    public void markBaseline(List<?> alreadyObserved) {
        causalStart = alreadyObserved.size();
    }

    public <T> List<T> causalSince(List<T> observations) {
        return observations.subList(Math.min(causalStart, observations.size()), observations.size());
    }
}
