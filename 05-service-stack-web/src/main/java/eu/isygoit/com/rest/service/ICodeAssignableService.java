package eu.isygoit.com.rest.service;

import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.RemoteNextCodeServiceNotDefinedException;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.IRemoteNextCodeService;
import eu.isygoit.service.nextCode.ICodeGeneratorService;

import java.io.Serializable;
import java.util.Optional;

/**
 * The interface Code assignable api.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 */
public interface ICodeAssignableService<I extends Serializable, T extends IIdAssignable<I>> {

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
     * Next code api code generator api.
     *
     * @return the code generator api
     * @throws NextCodeServiceNotDefinedException the next code api not defined exception
     */
    ICodeGeneratorService<NextCodeModel> nextCodeService() throws NextCodeServiceNotDefinedException;

    /**
     * Remote next code api remote next code api.
     *
     * @return the remote next code api
     * @throws RemoteNextCodeServiceNotDefinedException the remote next code api not defined exception
     */
    IRemoteNextCodeService remoteNextCodeService() throws RemoteNextCodeServiceNotDefinedException;

    /**
     * Gets next code key.
     *
     * @param initNextCode the init next code
     * @return the next code key
     */
    String getNextCodeKey(NextCodeModel initNextCode);


    /**
     * Find by code optional.
     *
     * @param code the code
     * @return the optional
     */
    Optional<T> findByCode(String code);
}
