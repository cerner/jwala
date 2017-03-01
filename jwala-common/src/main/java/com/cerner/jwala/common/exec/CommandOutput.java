package com.cerner.jwala.common.exec;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class CommandOutput implements Serializable {

    private final ExecReturnCode returnCode;
    private String standardOutput;
    private final String standardError;

    public CommandOutput(final ExecReturnCode theReturnCode,
                         final String theStandardOutput,
                         final String theStandardError) {
        returnCode = theReturnCode;
        standardOutput = theStandardOutput;
        standardError = theStandardError;
    }

    public ExecReturnCode getReturnCode() {
        return returnCode;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getStandardError() {
        return standardError;
    }

    public String standardErrorOrStandardOut() {
        if (isPresent(standardError)) {
            return standardError;
        } else {
            return standardOutput;
        }
    }

    private boolean isPresent(final String aString) {
        return aString != null && !"".equals(aString.trim());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CommandOutput rhs = (CommandOutput) obj;
        return new EqualsBuilder()
                .append(this.returnCode, rhs.returnCode)
                .append(this.standardOutput, rhs.standardOutput)
                .append(this.standardError, rhs.standardError)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(returnCode)
                .append(standardOutput)
                .append(standardError)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("returnCode", returnCode)
                .append("standardOutput", standardOutput)
                .append("standardError", standardError)
                .toString();
    }

    public void cleanStandardOutput() {
        standardOutput = standardOutput.replaceAll("\u001B\\[[;\\d]*m", "") // remove the ASCII color codes that come back from the shell
                .replaceAll("\u001B\\]0;~", "") // remove the ASCII color codes that come back from the shell
                .replaceAll("\\n", "NEWLINE")                               // remove the new line formatting
                .replaceAll("\\p{C}", "")                                   // remove all the hidden formatting characters that cause display issues in the browser
                .replaceAll("NEWLINE", "\\\n");                             // restore the new line formatting
    }

    public String extractMessageFromStandardOutput() {
        return standardOutput.replaceAll("\\n", "NEWLINE")                      // remove the new line formatting
                .replaceAll("^.*?NEWLINE\\$", "")                               // remove the shell prompt and the original command by taking out the first line
                .replaceAll("^.*\\s\\\".*\\\"\\s\\d+\\sNEWLINE", "")            // remove the original command again by looking for the second script parameter in quotes
                .replaceAll("[A-Za-z0-9]+@[A-Za-z0-9]+\\s~NEWLINE\\$", "")      // remove the user@host info
                .replaceAll("\\s\\sexitNEWLINElogout", "")                      // remove the remaining shell prompt info
                .replaceAll("NEWLINE", "\\\n");                                 // restore the new line formatting
    }

    public void cleanHeapDumpStandardOutput() {
        final String outputStartStr = "***heapdump-start***";
        int startIndex = standardOutput.lastIndexOf(outputStartStr);
        int endIndex = standardOutput.lastIndexOf("***heapdump-end***");
        if (endIndex > -1) {
            standardOutput = standardOutput.substring(startIndex + outputStartStr.length(), endIndex);
        } else {
            // for cases when heapdump-end is missing
            standardOutput = standardOutput.substring(startIndex + outputStartStr.length(), standardOutput.length());
        }
    }
}
