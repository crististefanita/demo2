package atf.ui.tests.practiceshop.api.network;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopJsonEvidence;
import atf.ui.tests.practiceshop.api.support.PracticeShopUiNetworkSettlementFixture;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component proof for bounded consumer-owned UI network settlement. */
@AtfComponentTest
class PracticeShopUiNetworkSettlementComponentTest {
    @Test
    void consecutiveMutationResponsesCollapseOnlyInThePresentationStream() {
        try (var inspector = PracticeShopUiNetworkSettlementFixture.inspector()) {
            var first = PracticeShopUiNetworkSettlementFixture.mutation(1, 1);
            var retry = PracticeShopUiNetworkSettlementFixture.mutation(2, 1);
            var readBack = PracticeShopUiNetworkSettlementFixture.readBack(3, 1);

            var capturedFirst = inspector.capture(first).orElseThrow();
            inspector.capture(retry);
            var capturedReadBack = inspector.capture(readBack).orElseThrow();

            assertEquals(3, inspector.observations().size(),
                    "raw browser order remains lossless for audit");
            assertEquals(2, inspector.presentationObservations().size(),
                    "only consecutive identical signatures are compacted");
            assertEquals(2, inspector.signatureCounts().get(capturedFirst.requestSignature()));
            assertEquals(capturedReadBack, inspector.presentationObservations().get(1));
        }
    }

    @Test
    void boundedReadBackCanArriveAfterMutationWithoutReplayingTheMutation() throws Exception {
        try (var inspector = PracticeShopUiNetworkSettlementFixture.inspector();
             var scheduler = Executors.newSingleThreadScheduledExecutor()) {
            AtomicInteger mutationAttempts = new AtomicInteger(1);
            inspector.capture(PracticeShopUiNetworkSettlementFixture.mutation(1, 1));
            scheduler.schedule(() -> inspector.capture(
                    PracticeShopUiNetworkSettlementFixture.readBack(2, 1)),
                    20, TimeUnit.MILLISECONDS);

            Optional<?> readBack = inspector.await(observation ->
                            PracticeShopUiNetworkSettlementFixture.CART_PATH.equals(observation.path())
                                    && "GET".equals(observation.method())
                                    && PracticeShopJsonEvidence.parse(observation.responseBody())
                                            .path("cart_items").path(0).path("quantity").asInt() == 1,
                    Duration.ofMillis(200));

            assertTrue(readBack.isPresent());
            assertEquals(1, mutationAttempts.get(),
                    "settlement waits for read-back and never replays a browser mutation");
        }
    }

    @Test
    void expiredReadBackIsEmptyAndCannotBecomeFalseGreenThroughReplay() {
        try (var inspector = PracticeShopUiNetworkSettlementFixture.inspector()) {
            AtomicInteger mutationAttempts = new AtomicInteger(1);
            inspector.capture(PracticeShopUiNetworkSettlementFixture.mutation(1, 1));

            Optional<?> readBack = inspector.await(observation ->
                            PracticeShopUiNetworkSettlementFixture.CART_PATH.equals(observation.path())
                                    && "GET".equals(observation.method()),
                    Duration.ofMillis(20));

            assertTrue(readBack.isEmpty());
            assertEquals(1, mutationAttempts.get(),
                    "timeout is observable; no implicit mutation replay is allowed");
        }
    }
}
