package eu.isygoit.multitenancy.service;

import eu.isygoit.annotation.InjectCodeGen;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.FileTenantService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.multitenancy.model.AppNextCode;
import eu.isygoit.multitenancy.model.ContractEntity;
import eu.isygoit.multitenancy.repository.ContractRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectCodeGen(value = NextCodeService.class)
@InjectRepository(value = ContractRepository.class)
public class ContractService extends FileTenantService<Long, ContractEntity, ContractRepository> {

    @Override
    protected String getUploadDirectory() {
        return "/contract";
    }

    @Override
    public AppNextCode initCodeGenerator() {
        return AppNextCode.builder()
                .tenant(TenantConstants.DEFAULT_TENANT_NAME)
                .entity(ContractEntity.class.getSimpleName())
                .attribute(ComSchemaColumnConstantName.C_CODE)
                .prefix("CTR")
                .valueLength(6L)
                .value(1L)
                .increment(1)
                .build();
    }
}