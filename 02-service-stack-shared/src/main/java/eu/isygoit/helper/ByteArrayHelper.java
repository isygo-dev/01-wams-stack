/*
 * Copyright (c) 2015, nvbt-SERVICE. All rights reserved.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * The type Byte array helper.
 */
@Slf4j
public final class ByteArrayHelper {

    private static final int SUFFIX_HEX_BYTE = 0xf;

    /**
     * Byte array to hex string string.
     *
     * @param bytesData the bytes data
     * @return the string
     */
    public static String byteArrayToHexString(byte[] bytesData) {
        if (bytesData == null) {
            return "";
        }
        int len = bytesData.length;
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < len; i++) {
            data.append(Integer.toHexString((bytesData[i] >> 4) & SUFFIX_HEX_BYTE));
            data.append(Integer.toHexString(bytesData[i] & SUFFIX_HEX_BYTE));
        }
        return data.toString().toUpperCase();
    }

    /**
     * Checksum mapped bytes long.
     *
     * @param bytes the bytes
     * @return the long
     * @throws IOException the io exception
     */
    public static long checksumMappedBytes(byte[] bytes) throws IOException {
        Checksum checksum = new CRC32();
        checksum.update(bytes, 0, bytes.length);
        long checksumValue = checksum.getValue();
        log.debug("checksumMappedFile :CRC checksum for byte[] is: {}" + checksumValue);
        return checksumValue;
    }

    /**
     * Hex string to byte array byte [ ].
     *
     * @param hexStringData the hex string data
     * @return the byte [ ]
     */
    public static byte[] hexStringToByteArray(String hexStringData) {
        if (hexStringData == null) {
            return null;
        }
        int hexStringDataLength = hexStringData.length();
        byte[] data = new byte[hexStringDataLength / 2];
        for (int i = 0; i < hexStringDataLength; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexStringData.charAt(i), 16) << 4) + Character.digit(hexStringData.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * To byte array byte [ ].
     *
     * @param obj the obj
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] toByteArray(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        }
    }

    /**
     * To object object.
     *
     * @param bytes the bytes
     * @return the object
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
}
