package com.cerner.jwala.control.command.common;

/**
 * Created by Arvindo Kinny on 1/9/2017.
 */
public enum Command {
    CHANGE_FILE_MODE("chmod"),
    CHECK_FILE_EXISTS("test -e %s"),
    CREATE_DIR("if [ ! -e \"%s\" ]; then mkdir -p %s; fi;"),
    MOVE("mv %s %s"),
    SCP("scp");

    public String cmd;

    Command(String cmd){
        this.cmd=cmd;
    }

    public String get(){
        return this.cmd;
    }
}
