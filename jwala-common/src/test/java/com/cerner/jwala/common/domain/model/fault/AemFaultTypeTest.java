package com.cerner.jwala.common.domain.model.fault;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class AemFaultTypeTest {

    @Test
    public void testUniquenessOfMessageCodes() {

        final Set<String> messageCodes = new HashSet<>();
        final Set<FaultType> duplicates = EnumSet.noneOf(FaultType.class);

        for (final FaultType faultType : FaultType.values()) {
            final String messageCode = faultType.getMessageCode().toLowerCase(Locale.US);
            if (messageCodes.contains(messageCode)) {
                duplicates.add(faultType);
            } else {
                messageCodes.add(messageCode);
            }
        }

        assertEquals(Collections.emptySet(),
                     duplicates);
    }
}
