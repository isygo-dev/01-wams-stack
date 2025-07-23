package eu.isygoit.multitenancy.service;

import eu.isygoit.annotation.InjectCodeGen;
import eu.isygoit.annotation.InjectLinkedFileRepository;
import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.com.rest.service.tenancy.MultiFileTenantService;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.model.schema.ComSchemaColumnConstantName;
import eu.isygoit.multitenancy.model.AppNextCode;
import eu.isygoit.multitenancy.model.imagefile.ResumeEntity;
import eu.isygoit.multitenancy.model.multifile.ResumeLinkedFile;
import eu.isygoit.multitenancy.repository.ResumeLinkedFileRepository;
import eu.isygoit.multitenancy.repository.ResumeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@InjectCodeGen(value = NextCodeService.class)
@InjectRepository(value = ResumeRepository.class)
@InjectLinkedFileRepository(value = ResumeLinkedFileRepository.class)
public class ResumeMultiFileService extends MultiFileTenantService<Long, ResumeEntity, ResumeLinkedFile, ResumeRepository,
        ResumeLinkedFileRepository> {


    @Override
    protected String getUploadDirectory() {
        return "/resume/files";
    }

    @Override
    public AppNextCode initCodeGenerator() {
        return AppNextCode.builder()
                .tenant(TenantConstants.DEFAULT_TENANT_NAME)
                .entity(ResumeLinkedFile.class.getSimpleName())
                .attribute(ComSchemaColumnConstantName.C_CODE)
                .prefix("RLF")
                .valueLength(6L)
                .codeValue(1L)
                .increment(1)
                .build();
    }
}
