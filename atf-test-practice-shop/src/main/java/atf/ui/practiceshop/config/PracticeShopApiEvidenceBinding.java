package atf.ui.practiceshop.config;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Typed, fail-closed binding between a Practice Shop journey and its API
 * checkpoints. It keeps selection out of the network journal and out of the
 * renderer, while retaining an explicit YAML seam for future journeys.
 */
public record PracticeShopApiEvidenceBinding(Set<PracticeShopApiCheckpoint> checkpoints) {
    public PracticeShopApiEvidenceBinding {
        Objects.requireNonNull(checkpoints, "checkpoints");
        checkpoints = checkpoints.isEmpty()
                ? EnumSet.noneOf(PracticeShopApiCheckpoint.class)
                : EnumSet.copyOf(checkpoints);
        checkpoints = Set.copyOf(checkpoints);
    }

    public static PracticeShopApiEvidenceBinding all() {
        return new PracticeShopApiEvidenceBinding(EnumSet.allOf(PracticeShopApiCheckpoint.class));
    }

    public static PracticeShopApiEvidenceBinding none() {
        return new PracticeShopApiEvidenceBinding(EnumSet.noneOf(PracticeShopApiCheckpoint.class));
    }

    /** Missing YAML means all checkpoints for UI_API, but none for UI_ONLY. */
    public static PracticeShopApiEvidenceBinding forProfile(
            PracticeShopValidationProfile profile, Collection<PracticeShopApiCheckpoint> configured) {
        Objects.requireNonNull(profile, "profile");
        if (!profile.apiEvidenceRequired()) return none();
        return configured == null ? all() : new PracticeShopApiEvidenceBinding(Set.copyOf(configured));
    }

    public static PracticeShopApiEvidenceBinding fromCsv(
            PracticeShopValidationProfile profile, String csv) {
        if (csv == null || csv.isBlank()) return forProfile(profile, null);
        Set<PracticeShopApiCheckpoint> checkpoints = java.util.Arrays.stream(csv.split(","))
                .map(PracticeShopApiCheckpoint::from)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PracticeShopApiCheckpoint.class)));
        return forProfile(profile, checkpoints);
    }

    public boolean includes(PracticeShopApiCheckpoint checkpoint) {
        return checkpoints.contains(Objects.requireNonNull(checkpoint, "checkpoint"));
    }

    public String displayValue() {
        return checkpoints.stream().map(PracticeShopApiCheckpoint::key)
                .sorted().collect(Collectors.joining(", "));
    }
}
