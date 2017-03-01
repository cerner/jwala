package com.cerner.jwala.common.domain.model.id;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.BadRequestException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IdentifierSetBuilder {

    private Collection<String> ids;

    public IdentifierSetBuilder(final Collection<String> someIds) {
        ids = someIds;
    }

    public <T> Set<Identifier<T>> build() {

        try {
            final Set<Identifier<T>> newIds = new HashSet<>(ids != null ? ids.size() : 0);

            if (ids == null) {
                return newIds;
            }

            for (final String id : ids) {
                newIds.add(new Identifier<T>(id));
            }

            return newIds;
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }
}
