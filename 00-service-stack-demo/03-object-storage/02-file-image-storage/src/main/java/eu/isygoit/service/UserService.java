package eu.isygoit.service;

import eu.isygoit.annotation.InjectCodeGen;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.ImageTenantService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.AppNextCode;
import eu.isygoit.model.image.UserEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectCodeGen(value = NextCodeService.class)
@InjectRepository(value = UserRepository.class)
public class UserService extends ImageTenantService<Long, UserEntity, UserRepository> {

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
                .prefix("USR")
                .valueLength(6L)
                .codeValue(1L)
                .increment(1)
                .build();
    }
}