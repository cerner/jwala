package com.cerner.jwala.persistence.jpa.domain.constraint.validator;

import com.cerner.jwala.persistence.jpa.domain.constraint.ValidPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.File;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for {@link PathValidator}
 *
 * Created by Jedd Cuison on 12/13/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {PathValidatorTest.Config.class})
public class PathValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    public void testIsValid() throws Exception {
        final String existingFile = new File(this.getClass().getResource("/i-exists.txt").getPath()).getAbsolutePath();

        Set<ConstraintViolation<PathsWrapper>> constraintViolations =
                validator.validate(new PathsWrapper("c:/test", "c:/test/jdk.zip", existingFile));
        assertTrue(constraintViolations.isEmpty());

        constraintViolations = validator.validate(new PathsWrapper("c:\\test", "c:/test/jdk.jar", existingFile));
        assertTrue(constraintViolations.isEmpty());

        constraintViolations = validator.validate(new PathsWrapper("c:\\test/any", "c:\\jdk.war", existingFile));
        assertTrue(constraintViolations.isEmpty());

        constraintViolations = validator.validate(new PathsWrapper("c:\\test/any", "c:\\jdk", existingFile));
        assertTrue(constraintViolations.size() == 1);
        assertEquals("invalid dirAndFilename", constraintViolations.iterator().next().getMessage());

        constraintViolations = validator.validate(new PathsWrapper("c:\\test/any", "c:\\jdk.peanuts", existingFile));
        assertTrue(constraintViolations.size() == 1);
        assertEquals("invalid dirAndFilename", constraintViolations.iterator().next().getMessage());

        constraintViolations = validator.validate(new PathsWrapper("c::\\test/any", "c:\\jdk.zip", existingFile));
        assertTrue(constraintViolations.size() == 1);
        assertEquals("invalid dir", constraintViolations.iterator().next().getMessage());

        constraintViolations = validator.validate(new PathsWrapper("/unix/path", "/jdk.zip", existingFile));
        assertTrue(constraintViolations.isEmpty());

        constraintViolations = validator.validate(new PathsWrapper("/unix/path", "/jdk.zip", "/this/should/not/point/to/an/existing/file"));
        assertTrue(constraintViolations.size() == 1);
        assertEquals("invalid existingPath", constraintViolations.iterator().next().getMessage());
    }

    static class PathsWrapper {

        @ValidPath(message="invalid dir")
        public final String dir;

        @ValidPath(allowableFileExtensions = {"zip", "war", "jar"}, message="invalid dirAndFilename")
        public final String dirAndFilename;

        @ValidPath(checkIfExists = true, message = "invalid existingPath")
        public final String existingPath;

        public PathsWrapper(final String dir, final String dirAndFilename, final String existingPath) {
            this.dir = dir;
            this.dirAndFilename = dirAndFilename;
            this.existingPath = existingPath;
        }

    }

    @Configuration
    static class Config {

        @Bean
        public Validator getValidator() {
            return new LocalValidatorFactoryBean();
        }

    }

}