package eu.isygoit.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public class RepoHelper {

    private static final int DEFAULT_PAGE_SIZE = 100;

    public static Pageable resolvePageable(Integer limit, Integer nextToken, String sortCol) {
        if(!StringUtils.hasText(sortCol)){
            sortCol = "creationDate";
        }
        int pageSize = (limit != null && limit > 0)
                ? Math.min(limit, DEFAULT_PAGE_SIZE)
                : DEFAULT_PAGE_SIZE;

        int pageNum = (nextToken != null)
                ? nextToken
                : 0;

        Pageable pageable = PageRequest.of(
                pageNum,
                pageSize,
                Sort.by(sortCol).descending()
        );
        return pageable;
    }

    public static Pageable resolvePageable(Integer limit, String nextToken, String sortCol) {
        int pageNum = (nextToken != null)
                ? Integer.parseInt(nextToken)
                : 0;

        return resolvePageable(limit, pageNum, sortCol);
    }
}
