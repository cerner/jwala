package com.cerner.jwala.web.controller;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.jvm.JvmControlService;
import com.cerner.jwala.service.webserver.WebServerCommandService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class CommandController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);

    private JvmControlService jvmControlService;

    private WebServerCommandService webServerCommandService;

    @Autowired
    public CommandController(JvmControlService aJvmControlService, WebServerCommandService aWebServerCommandService) {
        jvmControlService = aJvmControlService;
        webServerCommandService = aWebServerCommandService;
    }

    @RequestMapping(value = "/jvmCommand")
    public ModelAndView jvmCommand(HttpServletRequest request, HttpServletResponse response) {

        Identifier<Jvm> jvmIdentifier = getJvmIdParameter(request);
        ControlJvmRequest controlJvmRequest = getControlOperation(request, jvmIdentifier);

        CommandOutput commandOutput = jvmControlService.controlJvm(controlJvmRequest, User.getSystemUser());

        ModelAndView mv = new ModelAndView("cmd/textOutput");
        mv.addObject("stdErr", commandOutput.getStandardError());
        mv.addObject("stdOut", commandOutput.getStandardOutput());

        return mv;
    }

    @RequestMapping(value = "/webServerCommand")
    public void webServerCommand(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final Identifier<WebServer> id = new Identifier<>(request.getParameter("webServerId"));
        final String ERROR_MSG_PREFIX = "Error reading httpd.conf: ";
        response.setContentType("text/plain");
        try {
            final CommandOutput execData = webServerCommandService.getHttpdConf(id);
            if (execData.getReturnCode().wasSuccessful()) {
                response.getWriter().print(execData.getStandardOutput());
            } else {
                response.getWriter().print(ERROR_MSG_PREFIX + execData.getStandardError());
            }
        } catch (CommandFailureException cmdFailEx) {
            LOGGER.warn("Request Failure occurred", cmdFailEx);
            response.getWriter().print(ERROR_MSG_PREFIX + cmdFailEx.getMessage());
        }
    }

    protected ControlJvmRequest getControlOperation(HttpServletRequest request, Identifier<Jvm> jvmIdentifier) {
        String operation = request.getParameter("operation");
        JvmControlOperation theControlOperation = JvmControlOperation.convertFrom(operation);
        ControlJvmRequest jvmCommand = new ControlJvmRequest(jvmIdentifier, theControlOperation);
        return jvmCommand;
    }

    protected Identifier<Jvm> getJvmIdParameter(HttpServletRequest request) {
        String parameter = request.getParameter("jvmId");
        long id = Long.parseLong(parameter);
        return new Identifier<>(id);
    }

}