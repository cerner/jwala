package com.cerner.jwala.persistence.jpa.domain.constraint.validator;

import com.cerner.jwala.persistence.jpa.domain.constraint.ValidPath;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * The path validator
 *
 * Created by JC043760 on 12/13/2016
 */
public class PathValidator implements ConstraintValidator<ValidPath, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathValidator.class);

    private List<String> allowableFileExtensions;
    private boolean checkIfExists;

    @Override
    public void initialize(final ValidPath constraintAnnotation) {
        allowableFileExtensions = Arrays.asList(constraintAnnotation.allowableFileExtensions());
        checkIfExists = constraintAnnotation.checkIfExists();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        try {
            if (StringUtils.isEmpty(value)) {
                return true; // do not validate empty value there are other constraints for that e.g. @NotNull, @Size etc...
            }

            final Path path = Paths.get(value);

            if (checkIfExists && !path.toFile().exists()) {
                return false;
            }

            final String fileExt = FilenameUtils.getExtension(value);
            return allowableFileExtensions.isEmpty() ||
                   !allowableFileExtensions.stream().filter(fileExt::equalsIgnoreCase).findFirst().orElse(StringUtils.EMPTY).isEmpty();

        } catch (final InvalidPathException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

}
