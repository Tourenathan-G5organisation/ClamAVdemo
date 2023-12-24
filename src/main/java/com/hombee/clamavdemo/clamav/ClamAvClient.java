package com.hombee.clamavdemo.clamav;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
class ClamAvClient {

    @Value("${clamav.host}")
    private String hostName;
    @Value("${clamav.port}")
    private int port;
    @Value("${clamav.timeout}")
    private int timeout;
    @Value("${clamav.stream.maxlength}")
    int streamMaxLength;

    private static final int PONG_REPLY_LEN = 4;

    Logger LOG = LogManager.getLogger();

    /**
     * Builder with value on the initialisation file
     */
    public ClamAvClient() {
       
    }

    /**
     * @param hostName The hostname of the server running clamav-daemon
     * @param port The port that clamav-daemon listens to(By default it might not listen to a port. Check your clamav configuration).
     * @param timeout zero means infinite timeout. Not a good idea, but will be accepted.
     */
    public ClamAvClient(String hostName, int port, int timeout)  {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout value does not make sense.");
        }
        this.hostName = hostName;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Run PING command to clamd to test it is responding.
     *
     * @return true if the server responded with proper ping reply.
     */
    public boolean ping() {
        try (Socket s = new Socket(hostName,port);
             OutputStream outs = s.getOutputStream()) {
            s.setSoTimeout(timeout);
            outs.write(asBytes("zPING\0"));
            outs.flush();
            byte[] b = new byte[PONG_REPLY_LEN];
            InputStream inputStream = s.getInputStream();
            int copyIndex = 0;
            int readResult;
            do {
                readResult = inputStream.read(b, copyIndex, Math.max(b.length - copyIndex, 0));
                copyIndex += readResult;
            } while (readResult > 0);
            LOG.info("Check scanner status: {}", asString(b));
            return Arrays.equals(b, asBytes("PONG"));
        } catch (IOException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Streams the given data to the server in chunks. The whole data is not kept in memory.
     * This method is preferred if you don't want to keep the data in memory, for instance by scanning a file on disk.
     * Since the parameter InputStream is not reset, you can not use the stream afterwards, as it will be left in an EOF-state.
     * If your goal is to scan some data, and then pass that data further, consider using {@link #scan(byte[]) scan(byte[] in)}.
     * <p>
     * Opens a socket and reads the reply. Parameter input stream is NOT closed.
     *
     * @param is data to scan. Not closed by this method!
     * @return server reply
     */
    public ClamAvScanStatus scan(InputStream is) {
        try (Socket s = new Socket(hostName,port);
             OutputStream outs = new BufferedOutputStream(s.getOutputStream())) {

            s.setSoTimeout(timeout);

            // handshake
            outs.write(asBytes("zINSTREAM\0"));
            outs.flush();
            byte[] chunk = new byte[streamMaxLength];

            try (InputStream clamIs = s.getInputStream()) {
                // send data
                int read = is.read(chunk);
                while (read >= 0) {
                    // The format of the chunk is: '<length><data>' where <length> is the size of the following data in bytes expressed as a 4 byte unsigned
                    // integer in network byte order and <data> is the actual chunk. Streaming is terminated by sending a zero-length chunk.
                    byte[] chunkSize = ByteBuffer.allocate(4).putInt(read).array();

                    outs.write(chunkSize);
                    outs.write(chunk, 0, read);
                    if (clamIs.available() > 0) {
                        // reply from server before scan command has been terminated.
                        byte[] reply = assertSizeLimit(readAll(clamIs));
                        throw new IOException("Scan aborted. Reply from server: " + asString(reply));
                    }
                    read = is.read(chunk);
                }

                // terminate scan
                outs.write(new byte[]{0,0,0,0});
                outs.flush();

                // read reply and return result
                return checkReply(readAll(clamIs));
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            return ClamAvScanStatus.FAILED;
        }
    }

    /**
     * Scans bytes for virus by passing the bytes to clamav
     *
     * @param in data to scan
     * @return server reply
     **/
    public ClamAvScanStatus scan(byte[] in) {
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(in);
            ClamAvScanStatus status = scan(bis);
            bis.close();
            return status;
        } catch (IOException exception){
            exception.printStackTrace();
            return ClamAvScanStatus.FAILED;
        }
    }

    /**
     * Interpret the result from a  ClamAV scan, and determine if the result means the data is clean
     *
     * @param reply The reply from the server after scanning
     * @return true if no virus was found according to the clamd reply message
     */
    public boolean isCleanReply(byte[] reply) {
        String r = asString(reply);
        return (r.contains("OK") && !r.contains("FOUND"));
    }


    private byte[] assertSizeLimit(byte[] reply) {
        String r = asString(reply);
        if (r.startsWith("INSTREAM size limit exceeded."))
            throw new RuntimeException("Clamd size limit exceeded. Full reply from server: " + r);
        return reply;
    }

    private ClamAvScanStatus checkReply(byte[] reply) {
        String r = asString(reply);
        LOG.info("File scan response: {}", r);
        if(r.contains("OK") && !r.contains("FOUND"))
            return ClamAvScanStatus.OK;
        return ClamAvScanStatus.VIRUS;
    }

    // byte conversion based on ASCII character set regardless of the current system locale
    private static byte[] asBytes(String s) {
        return s.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * Return the byte answer from clamav server to String using US ASCI
     *
     * @param reply server reply in byte array
     * @return server reply in string
     */
    private static String asString(byte[] reply){
        return new String(reply, StandardCharsets.US_ASCII);
    }

    // reads all available bytes from the stream
    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();

        byte[] buf = new byte[2000];
        int read;
        do {
            read = is.read(buf);
            tmp.write(buf, 0, read);
        } while ((read > 0) && (is.available() > 0));
        return tmp.toByteArray();
    }
}
