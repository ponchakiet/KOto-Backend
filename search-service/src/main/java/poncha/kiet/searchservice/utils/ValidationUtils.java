package poncha.kiet.searchservice.utils;

public class ValidationUtils {
    public static boolean isNullOrEmpty(String field) {
        return field == null || field.trim().isEmpty();
    }
}
