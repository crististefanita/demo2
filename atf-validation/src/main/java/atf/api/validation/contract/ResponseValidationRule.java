package atf.api.validation.contract;
import atf.api.core.http.response.ApiResponse;
import atf.api.validation.evaluation.ValidationTable;

@FunctionalInterface
public interface ResponseValidationRule {
    void evaluate(ApiResponse response, ValidationTable table);

    default String name() {
        return getClass().getSimpleName();
    }
}
