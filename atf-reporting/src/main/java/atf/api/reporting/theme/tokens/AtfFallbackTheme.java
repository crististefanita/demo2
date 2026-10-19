package atf.api.reporting.theme.tokens;

import atf.api.reporting.asset.ReportAssetComposer;

/** Stable facade for the renderer-neutral standalone fallback stylesheet. */
public final class AtfFallbackTheme {
    private AtfFallbackTheme() {
    }

    public static String css() {
        return ReportAssetComposer.fallbackCss();
    }
}
