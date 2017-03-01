package com.cerner.jwala.service.resource;

import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;

import java.util.List;

/**
 * Outlines what a concrete resource handler should look like and what it can do.
 * This abstract class is also the corner stone for implementing a chain or responsibility pattern.
 *
 * Created by Jedd Cuison on 7/21/2016
 */
public abstract class ResourceHandler {

    protected ResourceDao resourceDao;
    protected ResourceHandler successor;

    public abstract ConfigTemplate fetchResource(ResourceIdentifier resourceIdentifier);
    public abstract CreateResourceResponseWrapper createResource(ResourceIdentifier resourceIdentifier,
                                                                 ResourceTemplateMetaData metaData,
                                                                 String data);
    public abstract void deleteResource(ResourceIdentifier resourceIdentifier);

    protected abstract boolean canHandle(ResourceIdentifier resourceIdentifier);

    public abstract String updateResourceMetaData(ResourceIdentifier resourceIdentifier, String resourceName, String metaData);

    public abstract Object getSelectedValue(ResourceIdentifier resourceIdentifier);

    public abstract List<String> getResourceNames(ResourceIdentifier resourceIdentifier);
}
