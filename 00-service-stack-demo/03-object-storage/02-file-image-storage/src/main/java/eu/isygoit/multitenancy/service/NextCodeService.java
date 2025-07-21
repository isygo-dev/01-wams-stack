package eu.isygoit.multitenancy.service;

import eu.isygoit.multitenancy.repository.AppNextCodeRepository;
import eu.isygoit.repository.NextCodeRepository;
import eu.isygoit.service.AbstractCodeGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Next code service.
 */
@Service
@Transactional
public class NextCodeService extends AbstractCodeGeneratorService<eu.isygoit.multitenancy.model.AppNextCode> {

    @Autowired
    private AppNextCodeRepository nextCodeRepository;

    @Override
    public NextCodeRepository nextCodeRepository() {
        return nextCodeRepository;
    }
}