package com.cerner.jwala.service.jvm;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.exception.CommandFailureException;

public interface JvmControlService {

    CommandOutput controlJvm(final ControlJvmRequest controlJvmRequest, final User aUser);

    CommandOutput controlJvmSynchronously(ControlJvmRequest controlJvmRequest, long timeout, User user) throws InterruptedException;

    CommandOutput secureCopyFile(ControlJvmRequest secureCopyRequest, String sourcePath, String destPath, String userId, boolean overwrite) throws CommandFailureException;

    CommandOutput executeCreateDirectoryCommand(Jvm jvm, String dirAbsolutePath) throws CommandFailureException;

    CommandOutput executeChangeFileModeCommand(Jvm jvm, String modifiedPermissions, String targetAbsoluteDir, String targetFile) throws CommandFailureException;

    CommandOutput executeCheckFileExistsCommand(Jvm jvm, String filename) throws CommandFailureException;

    CommandOutput executeBackUpCommand(Jvm jvm, String filename) throws CommandFailureException;
}
