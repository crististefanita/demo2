package atf.api.tests.governance.changes;

import java.time.LocalDate;
import java.util.Set;

record TestChangeRequest(String id, String status, String base, LocalDate expires,
                         String reason, String proof, Set<String> paths,
                         boolean allowDelete, String source) {
    TestChangeRequest {
        paths = Set.copyOf(paths);
    }
}
