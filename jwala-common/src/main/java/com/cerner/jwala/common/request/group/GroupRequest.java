package com.cerner.jwala.common.request.group;

import com.cerner.jwala.common.request.Request;

public interface GroupRequest extends Request {
    
    Long getId();
    
    String getType();

}
