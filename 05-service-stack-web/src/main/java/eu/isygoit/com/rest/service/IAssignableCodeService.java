package eu.isygoit.com.rest.service;

import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.RemoteNextCodeServiceNotDefinedException;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableId;
import eu.isygoit.model.extendable.NextCodeModel;
import eu.isygoit.service.IKmsCodeService;
import eu.isygoit.service.nextCode.ILocalCodeService;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * The interface Assignable code service.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 */
public interface IAssignableCodeService<I extends Serializable, E extends AssignableId & AssignableCode>
        extends ICrudServiceMethod<I, E> {

    /**
     * Find by code optional.
     *
     * @param code the code
     * @return the optional
     */
    Optional<E> findByCode(String code);

    /**
     * Find by code list.
     *
     * @param codeList the code list
     * @return the list
     */
    List<E> findByCode(List<String> codeList);

    /**
     * Create code generator optional.
     *
     * @return the optional
     */
    Optional<NextCodeModel> createCodeGenerator();

    /**
     * Gets next code.
     *
     * @return the next code
     */
    Optional<String> getNextCode();

    /**
     * Gets local code service.
     *
     * @return the local code service
     * @throws NextCodeServiceNotDefinedException the next code service not defined exception
     */
    ILocalCodeService<NextCodeModel> getLocalCodeService() throws NextCodeServiceNotDefinedException;

    /**
     * Gets kms code service.
     *
     * @return the kms code service
     * @throws RemoteNextCodeServiceNotDefinedException the remote next code service not defined exception
     */
    IKmsCodeService getKmsCodeService() throws RemoteNextCodeServiceNotDefinedException;

    /**
     * Gets next code key.
     *
     * @param initNextCode the init next code
     * @return the next code key
     */
    String getNextCodeKey(NextCodeModel initNextCode);
}
