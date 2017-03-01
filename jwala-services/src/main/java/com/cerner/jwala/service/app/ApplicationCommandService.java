package com.cerner.jwala.service.app;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.app.ControlApplicationRequest;
import com.cerner.jwala.exception.CommandFailureException;

/**
 * An interface that defines application-centric external command tasks.
 * <p/>
 * Created by Jedd Cuison on 9/9/2015.
 */
public interface ApplicationCommandService {
    CommandOutput controlApplication(ControlApplicationRequest applicationRequest, Application app, String... params) throws CommandFailureException;
}
