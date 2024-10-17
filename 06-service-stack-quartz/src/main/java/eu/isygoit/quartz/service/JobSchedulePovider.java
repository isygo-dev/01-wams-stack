package eu.isygoit.quartz.service;

import lombok.extern.slf4j.Slf4j;

/**
 * The type Job schedule povider.
 */
@Slf4j
public class JobSchedulePovider {
    /**
     * The constant startDelay.
     */
    public static int startDelay = 3;

    /**
     * Gets start delay.
     *
     * @return the start delay
     */
    public static synchronized int getStartDelay() {
        log.info("StartDelay/Provided delay :{}", startDelay);
        return startDelay++;
    }
}
