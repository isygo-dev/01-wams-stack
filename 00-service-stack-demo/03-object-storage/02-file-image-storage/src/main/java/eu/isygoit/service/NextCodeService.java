package eu.isygoit.service;

import eu.isygoit.model.AppNextCode;
import eu.isygoit.repository.AppNextCodeRepository;
import eu.isygoit.repository.code.NextCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Next code service.
 */
@Service
@Transactional
public class NextCodeService extends AbstractCodeGeneratorService<AppNextCode> {

    @Autowired
    private AppNextCodeRepository nextCodeRepository;

    @Override
    public NextCodeRepository nextCodeRepository() {
        return nextCodeRepository;
    }
}