package eu.isygoit.enums;

import java.util.Arrays;

/**
 * The interface Enum request.
 */
public interface IEnumRequest {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 6;
    /**
     * The constant GET.
     */
    String GET = "GET";
    /**
     * The constant PUT.
     */
    String PUT = "PUT";
    /**
     * The constant POST.
     */
    String POST = "POST";
    /**
     * The constant DELETE.
     */
    String DELETE = "DELETE";
    /**
     * The constant PATCH.
     */
    String PATCH = "PATCH";

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Get types.
         */
        GET(IEnumRequest.GET, GetMapping.class, "READ"),
        /**
         * Post types.
         */
        POST(IEnumRequest.POST, PostMapping.class, "WRITE"),
        /**
         * Put types.
         */
        PUT(IEnumRequest.PUT, PutMapping.class, "WRITE"),
        /**
         * Delete types.
         */
        DELETE(IEnumRequest.DELETE, DeleteMapping.class, "DELETE"),

        /**
         * Patch types.
         */
        PATCH(IEnumRequest.PATCH, PatchMapping.class, "WRITE");

        private final String meaning;
        private final Class<?> request;
        private final String action;

        Types(String meaning, Class<?> request, String action) {
            this.meaning = meaning;
            this.request = request;
            this.action = action;
        }

        /**
         * Value of types.
         *
         * @param request the request
         * @return the types
         */
        public static Types valueOf(Class<?> request) {
            return Arrays.stream(Types.values()).filter(types -> request.isAssignableFrom(types.request)).findAny().orElse(null);
        }

        public String meaning() {
            return meaning;
        }

        /**
         * Action string.
         *
         * @return the string
         */
        public String action() {
            return action;
        }
    }
}
