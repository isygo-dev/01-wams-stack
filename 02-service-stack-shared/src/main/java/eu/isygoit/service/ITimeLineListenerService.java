package eu.isygoit.service;

import eu.isygoit.model.Trackable;


/**
 * The interface Time line listener service.
 */
public interface ITimeLineListenerService {


    /**
     * Perform post persist tl.
     *
     * @param entity the entity
     */
    void performPostPersistTL(Trackable entity);

    /**
     * Perform post remove tl.
     *
     * @param entity the entity
     */
    void performPostRemoveTL(Trackable entity);

    /**
     * Perform post update tl.
     *
     * @param entity the entity
     */
    void performPostUpdateTL(Trackable entity);

}
