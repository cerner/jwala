package com.cerner.jwala.common;

import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.NameLengthRule;
import org.junit.Test;

/**
 * Created by SB053052 on 4/7/2017.
 */
public class NameLengthRuleTest {

    @Test
    public void testProperLengthName() {
        String name = "group1";
        NameLengthRule rule = new NameLengthRule(name);
        rule.validate();
    }


    @Test(expected = BadRequestException.class)
    public void testLongName() {
        String veryLongString = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        NameLengthRule rule = new NameLengthRule(veryLongString);
        rule.validate();
    }

}
