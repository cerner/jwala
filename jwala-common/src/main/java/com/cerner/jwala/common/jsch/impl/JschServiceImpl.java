package com.cerner.jwala.common.jsch.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.JschServiceException;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.ExitCodeNotAvailableException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Implements {@link JschService}
 * <p>
 * Created by JC043760 on 12/26/2016
 */
@Service
public class JschServiceImpl implements JschService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JschServiceImpl.class);
    private static final String CRLF = "\r\n";
    private static final int CHANNEL_CONNECT_TIMEOUT = 60000;
    private static final int CHANNEL_BORROW_LOOP_WAIT_TIME = 180000;
    private static final String EXIT_CODE_START_MARKER = "EXIT_CODE";
    private static final String EXIT_CODE_END_MARKER = "***";
    private static final int BYTE_CHUNK_SIZE = 1024;
    private static final int CHANNEL_EXEC_CLOSE_TIMEOUT = 300000;
    private static final String JSCH_CHANNEL_SHELL_READ_INPUT_SLEEP_DURATION = "jsch.channel.shell.read.input.sleep.duration";
    private static final String SHELL_READ_SLEEP_DEFAULT_VALUE = "250";
    private static final String JSCH_EXEC_READ_REMOTE_OUTPUT_LOOP_SLEEP_TIME = "jsch.exec.read.remote.output.loop.sleep.time";
    private static final String READ_LOOP_SLEEP_TIME_DEFAULT_VALUE = "100";

    @Autowired
    private JSch jsch;

    @Autowired
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    @Override
    public RemoteCommandReturnInfo runShellCommand(RemoteSystemConnection remoteSystemConnection, String command, long timeout) {
        final ChannelSessionKey channelSessionKey = new ChannelSessionKey(remoteSystemConnection, ChannelType.SHELL);
        LOGGER.debug("channel session key = {}", channelSessionKey);
        Channel channel = null;
        try {
            channel = getChannelShell(channelSessionKey);
            return runShellCommand(command, (ChannelShell) channel, timeout);
        } catch (final Exception e) {
            final String errMsg = MessageFormat.format("Failed to run the following command: {0}", command);
            LOGGER.error(errMsg, e);
            throw new JschServiceException(errMsg, e);
        } finally {
            if (channel != null) {
                channelPool.returnObject(channelSessionKey, channel);
                LOGGER.debug("channel {} returned", channel.getId());
            }
        }
    }

    /**
     * Runs a command in a shell
     *
     * @param command      the command to run
     * @param channelShell the channel where the command is sent for execution
     * @param timeout      the length of time in ms in which the method waits for a available byte(s) as a result of command
     * @return result of the command
     * @throws IOException
     */
    private RemoteCommandReturnInfo runShellCommand(final String command, final ChannelShell channelShell, final long timeout)
            throws IOException {
        final InputStream in = channelShell.getInputStream();
        final OutputStream out = channelShell.getOutputStream();

        LOGGER.debug("Executing command \"{}\"...", command);
        out.write(command.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        out.write("echo 'EXIT_CODE='$?***".getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        out.write("echo -n -e '\\xff'".getBytes(StandardCharsets.UTF_8));
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        out.flush();

        LOGGER.debug("Reading remote output ...");
        final String remoteOutput = readRemoteOutput(in, (char) 0xff, timeout);
        LOGGER.debug("****** output: start ******");
        LOGGER.debug(remoteOutput);
        LOGGER.debug("****** output: end ******");

        return new RemoteCommandReturnInfo(parseReturnCode(remoteOutput, command), remoteOutput, null);
    }

    @Override
    public RemoteCommandReturnInfo runExecCommand(RemoteSystemConnection remoteSystemConnection, String command, long timeout) {
        Session session = null;
        ChannelExec channel = null;
        try {
            // We can't keep the session and the channels open for type exec since we need the exit code and the
            // standard error e.g. thread dump uses this and requires the exit code and the standard error.
            LOGGER.debug("preparing session...");
            session = prepareSession(remoteSystemConnection);
            session.connect();
            LOGGER.debug("session connected");
            channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getChannelType());

            LOGGER.debug("Executing command \"{}\"...", command);
            channel.setCommand(command.getBytes(StandardCharsets.UTF_8));

            LOGGER.debug("channel {} connecting...", channel.getId());
            channel.connect(CHANNEL_CONNECT_TIMEOUT);
            LOGGER.debug("channel {} connected!", channel.getId());

            return getExecRemoteCommandReturnInfo(channel, timeout);
        } catch (final Exception e) {
            final String errMsg = MessageFormat.format("Failed to run the following command: {0}", command);
            LOGGER.error(errMsg, e);
            throw new JschServiceException(errMsg, e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
                LOGGER.debug("Channel {} disconnected!", channel.getId());
            }

            if (session != null && session.isConnected()) {
                session.disconnect();
                LOGGER.debug("session disconnected");
            }
        }
    }

    /**
     * Runs a command via jsch's exec channel.
     * Unlike the shell channel, an exec channel closes after an execution of a command.
     *
     * @param channelExec the channel where the command is sent for execution
     * @param timeout     the length of time in ms in which the method waits for a available byte(s) as a result of command
     * @return result of the command
     */
    private RemoteCommandReturnInfo getExecRemoteCommandReturnInfo(final ChannelExec channelExec, final long timeout)
            throws IOException, JSchException {

        final String output = readExecRemoteOutput(channelExec, timeout);
        LOGGER.debug("remote output = {}", output);

        String errorOutput = null;

        // wait for the channel to close before checking the exit status
        final long startTime = System.currentTimeMillis();
        while (!channelExec.isClosed()) {
            if ((System.currentTimeMillis() - startTime) > CHANNEL_EXEC_CLOSE_TIMEOUT) {
                errorOutput = MessageFormat.format("Wait for channel to close timeout! Timeout = {0} ms",
                        CHANNEL_EXEC_CLOSE_TIMEOUT);
                LOGGER.error(errorOutput);
                break;
            }
        }

        LOGGER.debug("Channel exec exit status = {}", channelExec.getExitStatus());

        if (channelExec.getExitStatus() != 0 && channelExec.getExitStatus() != -1) {
            errorOutput = readExecRemoteOutput(channelExec, timeout);
            LOGGER.debug("remote error output = {}", errorOutput);
        }

        return new RemoteCommandReturnInfo(channelExec.getExitStatus(), output, errorOutput);
    }

    /**
     * Read the remote output for the exec channel. This is the code provided on the jcraft site with a timeout added.
     *
     * @param channelExec the exec channel
     * @param timeout     the maximum period of time for reading the channel output
     * @return the output from the channel
     * @throws IOException for any issues encoutered when retrieving the input stream from the channel
     */
    private String readExecRemoteOutput(ChannelExec channelExec, long timeout) throws IOException {
        final BufferedInputStream bufIn = new BufferedInputStream(channelExec.getInputStream());
        final StringBuilder outputBuilder = new StringBuilder();

        final byte[] tmp = new byte[BYTE_CHUNK_SIZE];
        final long startTime = System.currentTimeMillis();
        final long readLoopSleepTime = Long.parseLong(ApplicationProperties.get(JSCH_EXEC_READ_REMOTE_OUTPUT_LOOP_SLEEP_TIME,
                                                                                READ_LOOP_SLEEP_TIME_DEFAULT_VALUE));

        while (true) {
            // read the stream
            while (bufIn.available() > 0) {
                int i = bufIn.read(tmp, 0, BYTE_CHUNK_SIZE);
                if (i < 0) {
                    break;
                }
                outputBuilder.append(new String(tmp, 0, i, StandardCharsets.UTF_8));
            }

            // check if the channel is closed
            if (channelExec.isClosed()) {
                // check for any more bytes on the input stream
                sleep(readLoopSleepTime);

                if (bufIn.available() > 0) {
                    continue;
                }

                LOGGER.debug("exit-status: {}", channelExec.getExitStatus());
                break;
            }

            // check timeout
            if ((System.currentTimeMillis() - startTime) > timeout) {
                LOGGER.warn("Remote exec output reading timeout!");
                break;
            }

            // If for some reason the channel is not getting closed, we should not hog CPU time with this loop hence
            // we sleep for a while
            sleep(readLoopSleepTime);
        }
        return outputBuilder.toString();
    }

    /**
     * Puts the current thread to sleep. If the sleep is interrupted, the error is caught and logged.
     * @param val the period of time in ms for the thread to sleep
     */
    private void sleep(final long val) {
        try {
            Thread.sleep(val);
        } catch (final InterruptedException ie) {
            LOGGER.error("Sleep interrupted while reading JSch exec remote output!", ie);
        }
    }

    /**
     * Reads data streamed from a remote connection
     *
     * @param remoteOutput  the inputstream where the remote connection will stream data to
     * @param dataEndMarker a marker which tells the method to stop reading from the inputstream. If this is null
     *                      then the method will try to read data from the input stream until read timeout is reached.
     * @param timeout       the length of time in which to wait for incoming data from the stream
     * @return the data streamed from the remote connection
     * @throws IOException thrown when reading bytes from input stream
     */
    private String readRemoteOutput(final InputStream remoteOutput, final Character dataEndMarker, final long timeout)
            throws IOException {

        if (null == dataEndMarker) {
            throw new JschServiceException("Expecting non-null end marker when reading remote output");
        }

        final int readInputSleepDuration = Integer.parseInt(ApplicationProperties.get(JSCH_CHANNEL_SHELL_READ_INPUT_SLEEP_DURATION,
                SHELL_READ_SLEEP_DEFAULT_VALUE));
        final BufferedInputStream buffIn = new BufferedInputStream(remoteOutput);
        final byte[] bytes = new byte[BYTE_CHUNK_SIZE];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        String result;
        LOGGER.debug("Default char encoding:" + Charset.defaultCharset());
        long startTime = System.currentTimeMillis();
        try {
            while (true) {
                if (buffIn.available() != 0) {
                    final int size = buffIn.read(bytes);

                    if (size > 0) {
                        LOGGER.debug("Read:" + size + " bytes");
                        out.write(bytes, 0, size);
                        LOGGER.debug("Bytes read as String: " + new String(bytes));
                    }

                    startTime = System.currentTimeMillis();

                    // Alternative method of checking if we got our 'EOF' character.
                    boolean stopReading = false;
                    for(int b=0;b<size;b++) {
                        if (bytes[b]==-1) {
                            LOGGER.debug("Read EOF byte '{}', stopping remote output reading...", bytes[size - 1]);
                            stopReading=true;
                        }
                    }

                    if (stopReading) {
                        break;
                    }

                    // need to verify this works on non-windows systems
                    // currently setting the encoding to UTF8, UTF16, ASCII, or ISO all fail this conditional
                    // e.g. new String(bytes, StandardCharsets.UTF-8)
                    // printing out Charset.defaultCharset() shows it's using windows-1252 on a Windows JVM
                    if (new String(bytes).indexOf(dataEndMarker) > -1) {
                        LOGGER.debug("Read EOF character '{}', stopping remote output reading...", bytes[size - 1]);
                        break;
                    }
                }

                if ((System.currentTimeMillis() - startTime) > timeout) {
                    LOGGER.warn("Remote output reading timeout!");
                    break;
                }

                try {
                    Thread.sleep(readInputSleepDuration);
                } catch (final InterruptedException e) {
                    final String errMsg =  "readRemoteOutput was interrupted while waiting for input from JSch channel!";
                    LOGGER.error(errMsg, e);
                    throw new JschServiceException(errMsg, e);
                }
            }
        } finally {
            result = out.toString(StandardCharsets.UTF_8.name());
            out.close();
        }

        return result;
    }

    /**
     * Parse the return code from the output string.
     *
     * @param outputStr the output string
     * @param command   the command string
     * @return {@link ExecReturnCode}
     */
    private int parseReturnCode(final String outputStr, final String command) {
        if (outputStr != null) {
            try {
                final String exitCodeStr = outputStr.substring(outputStr.lastIndexOf(EXIT_CODE_START_MARKER)
                        + EXIT_CODE_START_MARKER.length() + 1, outputStr.lastIndexOf(EXIT_CODE_END_MARKER));
                return Integer.parseInt(exitCodeStr);
            } catch (final IndexOutOfBoundsException e) {
                final String errorMsg = MessageFormat.format("Failed to parse output: {0}", outputStr);
                LOGGER.error(errorMsg, e);
                throw new JschServiceException(errorMsg, e);
            }
        }
        throw new ExitCodeNotAvailableException(command);
    }

    /**
     * Get a {@link ChannelShell}
     *
     * @param channelSessionKey the session key that identifies the channel
     * @return {@link ChannelShell}
     * @throws Exception thrown by borrowObject and invalidateObject
     */
    private ChannelShell getChannelShell(final ChannelSessionKey channelSessionKey) throws Exception {
        final long startTime = System.currentTimeMillis();
        Channel channel;
        do {
            LOGGER.debug("borrowing a channel...");
            channel = channelPool.borrowObject(channelSessionKey);
            if (channel != null) {
                LOGGER.debug("channel {} borrowed", channel.getId());
                if (!channel.isConnected()) {
                    try {
                        LOGGER.debug("channel {} connecting...", channel.getId());
                        channel.connect(CHANNEL_CONNECT_TIMEOUT);
                        LOGGER.debug("channel {} connected!", channel.getId());
                    } catch (final JSchException jsche) {
                        LOGGER.error("Borrowed channel {} connection failed! Invalidating the channel...",
                                channel.getId(), jsche);
                        channelPool.invalidateObject(channelSessionKey, channel);
                    }
                } else {
                    LOGGER.debug("Channel {} already connected!", channel.getId());
                }
            }

            if ((channel == null || !channel.isConnected()) && (System.currentTimeMillis() - startTime) > CHANNEL_BORROW_LOOP_WAIT_TIME) {
                final String errMsg = MessageFormat.format("Failed to get a channel within {0} ms! Aborting channel acquisition!",
                        CHANNEL_BORROW_LOOP_WAIT_TIME);
                LOGGER.error(errMsg);
                throw new JschServiceException(errMsg);
            }
        } while (channel == null || !channel.isConnected());
        return (ChannelShell) channel;
    }

    /**
     * Prepare the session by setting session properties
     *
     * @param remoteSystemConnection {@link RemoteSystemConnection}
     * @return {@link Session}
     * @throws JSchException
     */
    private Session prepareSession(final RemoteSystemConnection remoteSystemConnection) throws JSchException {
        final Session session = jsch.getSession(remoteSystemConnection.getUser(), remoteSystemConnection.getHost(),
                remoteSystemConnection.getPort());
        final char[] encryptedPassword = remoteSystemConnection.getEncryptedPassword();
        if (encryptedPassword != null) {
            session.setPassword(new DecryptPassword().decrypt(encryptedPassword));
            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("PreferredAuthentications", "password,gssapi-with-mic,publickey,keyboard-interactive");
        }
        return session;
    }

}
