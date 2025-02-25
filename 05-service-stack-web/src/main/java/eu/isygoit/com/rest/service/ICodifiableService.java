package eu.isygoit.com.rest.service;

import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.RemoteNextCodeServiceNotDefinedException;
import eu.isygoit.model.IIdEntity;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.IRemoteNextCodeService;
import eu.isygoit.service.nextCode.INextCodeService;

import java.io.Serializable;

/**
 * The interface Codifiable service.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface ICodifiableService<I extends Serializable, T extends IIdEntity>
        extends ICrudServiceMethod<I, T> {

    /**
     * Init code generator next code model.
     *
     * @return the next code model
     */
    NextCodeModel initCodeGenerator();

    /**
     * Gets next code.
     *
     * @return the next code
     */
    String getNextCode();

    /**
     * Next code service next code service.
     *
     * @return the next code service
     * @throws NextCodeServiceNotDefinedException the next code service not defined exception
     */
    INextCodeService<NextCodeModel> nextCodeService() throws NextCodeServiceNotDefinedException;

    /**
     * Remote next code service remote next code service.
     *
     * @return the remote next code service
     * @throws RemoteNextCodeServiceNotDefinedException the remote next code service not defined exception
     */
    IRemoteNextCodeService remoteNextCodeService() throws RemoteNextCodeServiceNotDefinedException;

    /**
     * Gets next code key.
     *
     * @param initNextCode the init next code
     * @return the next code key
     */
    String getNextCodeKey(NextCodeModel initNextCode);
}
