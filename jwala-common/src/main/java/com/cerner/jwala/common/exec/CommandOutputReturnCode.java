package com.cerner.jwala.common.exec;

/**
 * {@link CommandOutput} return code enum.
 *
 * Created by Jedd Cuison on 2/9/2016.
 */
public enum CommandOutputReturnCode {

    SUCCESS(0, "Successful"),
    FAILED(1, "Failed executing the command"),
    NO_SUCH_SERVICE_UNFILTERED(36, "No such service to control"), // when calling sc directly instead of from a Jwala script
    NO_SUCH_SERVICE(123, "No such service to control"),
    TIMED_OUT(124, "Command timed out"),
    ABNORMAL_SUCCESS(126, "Service already started/stopped"),
    NO_OP(127, "Command operation does not exist"),
    KILL(255, "Kill command executed"),
    UNKNOWN(99999, "Return code not defined")
    ;

    private final int retCode;
    private final String desc;

    CommandOutputReturnCode(final int retCode, final String desc) {
        this.retCode = retCode;
        this.desc = desc;
    }

    public int getCodeNumber() {
        return retCode;
    }

    public String getDesc() {
        return desc;
    }

    public static CommandOutputReturnCode fromReturnCode(final int retCode) {
        for (CommandOutputReturnCode commandOutputReturnCode: CommandOutputReturnCode.values()) {
            if (commandOutputReturnCode.retCode == retCode) {
                return commandOutputReturnCode;
            }
        }
        return UNKNOWN;
    }

}
