package com.siemens.cto.aem.request;

import com.siemens.cto.aem.common.exception.BadRequestException;

public interface Request {

    void validate() throws BadRequestException;

}
