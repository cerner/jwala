package com.cerner.jwala.service.group;

import com.cerner.jwala.common.domain.model.id.Identifier;

/**
 * Retrieve group state details (e.g. running JVM count and Web Sever count) and send the said data to a destination via
 * a messaging framework like JMS or Spring STOMP.
 *
 * Created by Jedd Cuison on 3/14/2016.
 */
public interface GroupStateNotificationService {

    /**
     * Retrieve the group state and send it to a topic.
     * @param id the id
     * @param aClass the class where the state belongs to.
     */
    void retrieveStateAndSend(Identifier id, Class aClass);

}
