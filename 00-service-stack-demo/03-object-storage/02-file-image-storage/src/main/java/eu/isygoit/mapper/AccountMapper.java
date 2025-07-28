package eu.isygoit.mapper;

import eu.isygoit.dto.AccountDto;
import eu.isygoit.model.simple.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface AccountMapper extends EntityMapper<AccountEntity, AccountDto> {
}
