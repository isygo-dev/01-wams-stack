package eu.isygoit.model;


/**
 * The interface Linked file.
 */
public interface ILinkedFile extends IFileEntity {

    /**
     * Gets crc 16.
     *
     * @return the crc 16
     */
    public Integer getCrc16();

    /**
     * Sets crc 16.
     *
     * @param crc16 the crc 16
     */
    public void setCrc16(Integer crc16);

    /**
     * Gets crc 32.
     *
     * @return the crc 32
     */
    public Integer getCrc32();

    /**
     * Sets crc 32.
     *
     * @param crc32 the crc 32
     */
    public void setCrc32(Integer crc32);

    /**
     * Gets size.
     *
     * @return the size
     */
    public Long getSize();

    /**
     * Sets size.
     *
     * @param size the size
     */
    public void setSize(Long size);

    /**
     * Gets version.
     *
     * @return the version
     */
    public Long getVersion();

    /**
     * Sets version.
     *
     * @param version the version
     */
    public void setVersion(Long version);

    /**
     * Gets mimetype.
     *
     * @return the mimetype
     */
    public String getMimetype();

    /**
     * Sets mimetype.
     *
     * @param mimetype the mimetype
     */
    public void setMimetype(String mimetype);
}
