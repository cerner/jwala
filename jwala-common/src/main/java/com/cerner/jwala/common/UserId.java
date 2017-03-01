package com.cerner.jwala.common;

import com.cerner.jwala.common.domain.model.user.User;

/**
 * Holds {@link User} in a ThreadLocal.
 *
 * Created by Jedd Cuison on 12/29/2015.
 */
public class UserId {

    public static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(final String id) {
        THREAD_LOCAL.set(id);
    }

    public static void unset() {
        THREAD_LOCAL.remove();
    }

    public static String get() {
        return THREAD_LOCAL.get();
    }

}
