package eu.isygoit.enums;

/**
 * The interface Enum storage.
 */
public interface IEnumStorage {

    /**
     * The constant STR_ENUM_SIZE.
     */
    int STR_ENUM_SIZE = 16;

    /**
     * The enum Types.
     */
    enum Types implements IEnum {
        /**
         * Minio storage types.
         */
        MINIO_STORAGE("MinIO"),
        /**
         * Lakefs storage types.
         */
        LAKEFS_STORAGE("LakeFS"),
        /**
         * Ceph storage types.
         */
        CEPH_STORAGE("Ceph"),
        /**
         * Openio storage types.
         */
        OPENIO_STORAGE("OpenIO");

        private final String meaning;

        Types(String meaning) {
            this.meaning = meaning;
        }

        public String meaning() {
            return meaning;
        }
    }
}
