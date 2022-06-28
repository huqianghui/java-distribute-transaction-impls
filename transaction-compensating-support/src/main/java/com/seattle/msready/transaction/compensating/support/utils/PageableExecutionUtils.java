package com.seattle.msready.transaction.compensating.support.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class PageableExecutionUtils {

    private PageableExecutionUtils() {
    }

    /**
     * Constructs a {@link Page} based on the given {@code content}, {@link Pageable} and {@link TotalSupplier} applying
     * optimizations. The construction of {@link Page} omits a count query if the total can be determined based on the
     * result size and {@link Pageable}.
     *
     * @param content       must not be {@literal null}.
     * @param pageable      can be {@literal null}.
     * @param totalSupplier must not be {@literal null}.
     * @return the {@link Page}.
     */
    public static <T> Page<T> getPage(List<T> content, Pageable pageable, TotalSupplier totalSupplier) {

        if (pageable == null || pageable.getOffset() == 0) {

            if (pageable == null || pageable.getPageSize() > content.size()) {
                return new PageImpl<>(content, pageable, content.size());
            }

            return new PageImpl <T>(content, pageable, totalSupplier.get());
        }

        if (content.size() != 0 && pageable.getPageSize() > content.size()) {
            return new PageImpl <T>(content, pageable, pageable.getOffset() + content.size());
        }

        return new PageImpl <T>(content, pageable, totalSupplier.get());
    }

    /**
     * Supplies the total count for a particular query. Can be replaced with a Java 8 Supplier when upgrading to Java 8.
     *
     * @author Mark Paluch
     */
    public interface TotalSupplier {

        /**
         * @return the total count for a particular query.
         */
        long get();
    }
}
