package eu.isygoit.controller.file;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedFileTenantController;
import eu.isygoit.dto.ContractDto;
import eu.isygoit.mapper.ContractMapper;
import eu.isygoit.model.file.ContractEntity;
import eu.isygoit.service.ContractService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = ContractMapper.class, minMapper = ContractMapper.class)
@InjectService(ContractService.class)
@RestController
@RequestMapping("/api/v1/contract")
public class ContractFileController extends MappedFileTenantController<Long, ContractEntity,
        ContractDto, ContractDto, ContractService> {
}
