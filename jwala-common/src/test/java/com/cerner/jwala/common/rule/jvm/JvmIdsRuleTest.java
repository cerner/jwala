package com.cerner.jwala.common.rule.jvm;

import org.junit.Test;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.jvm.JvmIdsRule;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JvmIdsRuleTest {

    @Test
    public void testValidIds() {

        final Set<Identifier<Jvm>> jvmIds = new HashSet<>();
        final int numberToAdd = 10;

        for (int i = 0; i < numberToAdd; i++) {
            jvmIds.add(new Identifier<Jvm>((long)i));
        }

        final JvmIdsRule rule = new JvmIdsRule(jvmIds);
        assertTrue(rule.isValid());
        rule.validate();
    }

    @Test
    public void testNoIdsAreOk() {

        final JvmIdsRule rule = new JvmIdsRule(Collections.<Identifier<Jvm>>emptySet());

        assertTrue(rule.isValid());
        rule.validate();
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidIds() {

        final Set<Identifier<Jvm>> jvmIds = new HashSet<>();

        jvmIds.add(null);
        jvmIds.add(new Identifier<Jvm>((Long)null));

        final JvmIdsRule rule = new JvmIdsRule(jvmIds);
        assertFalse(rule.isValid());
        rule.validate();
    }
}
