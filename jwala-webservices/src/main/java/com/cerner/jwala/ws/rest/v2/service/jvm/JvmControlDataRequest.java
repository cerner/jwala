package com.cerner.jwala.ws.rest.v2.service.jvm;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * POJO that wraps JVM related data
 *
 * Created by Jedd Cuison on 8/11/2016.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
// TODO: Complete this POJO...
public class JvmControlDataRequest {

    private String controlOperation;

    public String getControlOperation() {
        return controlOperation;
    }

    public void setControlOperation(String controlOperation) {
        this.controlOperation = controlOperation;
    }
}
