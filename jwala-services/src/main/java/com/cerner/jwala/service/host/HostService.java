package com.cerner.jwala.service.host;

/**
 * Host Services
 * Created by Arvindo Kinny on 12/13/2016.
 */
public interface HostService {
    /**
     *
     * @param hostName
     * @return  Return uname output Example: Linux, CYGWIN_NT-6.3, etc.
     */
    public static String UNAME_LINUX = "Linux";
    public static String UNAME_CYGWIN = "CYGWIN";
    public static String UNAME_UNIX = "Unix";
    public String getUName(String hostName);
}
