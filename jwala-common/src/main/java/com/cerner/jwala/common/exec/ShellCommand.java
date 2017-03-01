package com.cerner.jwala.common.exec;

public class ShellCommand extends ExecCommand {

    public ShellCommand(final String... theCommandFragments) {
        super(theCommandFragments);
        this.runInShell = true;
    }

}
