package eu.isygoit.service;

import eu.isygoit.model.ITLEntity;


/**
 * The interface Time line listener service.
 */
public interface ITimeLineListenerService {


    /**
     * Perform post persist tl.
     *
     * @param entity the entity
     */
    void performPostPersistTL(ITLEntity entity);

    /**
     * Perform post remove tl.
     *
     * @param entity the entity
     */
    void performPostRemoveTL(ITLEntity entity);

    /**
     * Perform post update tl.
     *
     * @param entity the entity
     */
    void performPostUpdateTL(ITLEntity entity);

}
