package atf.ui.practiceshop.support;

import java.util.List;
import java.util.function.Predicate;

/** Pure selection policy for the ordered API exchanges captured by a UI session. */
public final class PracticeShopApiExchangeSelector {
    private PracticeShopApiExchangeSelector() { }

    public static PracticeShopApiNetworkJournal.Exchange latest(
            List<PracticeShopApiNetworkJournal.Exchange> exchanges,
            String method, String path, Predicate<PracticeShopApiNetworkJournal.Exchange> extra) {
        PracticeShopApiNetworkJournal.Exchange exchange = latestOptional(exchanges, method, path, extra);
        if (exchange != null) return exchange;
        throw new IllegalStateException("Missing API exchange: " + method + " " + path
                + "; captured=" + exchanges.stream()
                .map(PracticeShopApiNetworkJournal.Exchange::summary).toList());
    }

    public static PracticeShopApiNetworkJournal.Exchange latestOptional(
            List<PracticeShopApiNetworkJournal.Exchange> exchanges,
            String method, String path, Predicate<PracticeShopApiNetworkJournal.Exchange> extra) {
        for (int index = exchanges.size() - 1; index >= 0; index--) {
            PracticeShopApiNetworkJournal.Exchange exchange = exchanges.get(index);
            if (method.equalsIgnoreCase(exchange.method())
                    && exchange.path().contains(path) && extra.test(exchange)) {
                return exchange;
            }
        }
        return null;
    }

    public static String productIdFromPath(String selectedProductPath) {
        if (selectedProductPath == null || selectedProductPath.isBlank()) return null;
        String normalized = selectedProductPath.trim();
        int separator = normalized.lastIndexOf('/');
        if (separator < 0 || separator == normalized.length() - 1) return null;
        String id = normalized.substring(separator + 1).split("[?#]", 2)[0].trim();
        return id.isBlank() ? null : id;
    }
}
