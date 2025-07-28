package eu.isygoit.service;

import eu.isygoit.annotation.InjectCodeGen;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.FileImageTenantService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.AppNextCode;
import eu.isygoit.model.imagefile.ResumeEntity;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectCodeGen(value = NextCodeService.class)
@InjectRepository(value = ResumeRepository.class)
public class ResumeService extends FileImageTenantService<Long, ResumeEntity, ResumeRepository> {

    @Override
    protected String getUploadDirectory() {
        return "/resume";
    }

    @Override
    public AppNextCode initCodeGenerator() {
        return AppNextCode.builder()
                .tenant(TenantConstants.DEFAULT_TENANT_NAME)
                .entity(ResumeEntity.class.getSimpleName())
                .attribute(ComSchemaColumnConstantName.C_CODE)
                .prefix("RES")
                .valueLength(6L)
                .codeValue(1L)
                .increment(1)
                .build();
    }
}