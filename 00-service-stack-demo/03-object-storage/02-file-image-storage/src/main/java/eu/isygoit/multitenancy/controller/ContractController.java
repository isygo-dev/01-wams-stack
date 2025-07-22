package eu.isygoit.multitenancy.controller;

import eu.isygoit.annotation.InjectMapper;
import eu.isygoit.annotation.InjectService;
import eu.isygoit.com.rest.controller.impl.tenancy.MappedCrudTenantController;
import eu.isygoit.multitenancy.dto.ContractDto;
import eu.isygoit.multitenancy.mapper.ContractMapper;
import eu.isygoit.multitenancy.model.ContractEntity;
import eu.isygoit.multitenancy.service.ContractService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8081")
@InjectMapper(mapper = ContractMapper.class, minMapper = ContractMapper.class)
@InjectService(ContractService.class)
@RestController
@RequestMapping("/api/v1/contract")
public class ContractController extends MappedCrudTenantController<Long, ContractEntity,
        ContractDto, ContractDto, ContractService> {
}
