package eu.isygoit.multitenancy.service;

import eu.isygoit.annotation.InjectCodeGen;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.ImageTenantService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.multitenancy.model.AppNextCode;
import eu.isygoit.multitenancy.model.UserEntity;
import eu.isygoit.multitenancy.repository.UserTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectCodeGen(value = NextCodeService.class)
@InjectRepository(value = UserTenantAssignableRepository.class)
public class UserService extends ImageTenantService<Long, UserEntity, UserTenantAssignableRepository> {

    @Override
    protected String getUploadDirectory() {
        return "/user/avatar";
    }

    @Override
    public AppNextCode initCodeGenerator() {
        return AppNextCode.builder()
                .tenant(TenantConstants.DEFAULT_TENANT_NAME)
                .entity(UserEntity.class.getSimpleName())
                .attribute(ComSchemaColumnConstantName.C_CODE)
                .prefix("ACT")
                .valueLength(6L)
                .value(1L)
                .increment(1)
                .build();
    }
}