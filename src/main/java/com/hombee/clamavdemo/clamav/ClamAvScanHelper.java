package com.hombee.clamavdemo.clamav;


import com.hombee.clamavdemo.exception.ClamAvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@Component
public class ClamAvScanHelper {

    @Value("${document.scan.clamav.failed}")
    private String failedToScanMessage;
    @Value("${document.scan.clamav.virus}")
    private String suspectedFileMessage;

    @Autowired
    private ClamAvClient clamAvClient;


    /**
     * Check the healt status of the clamAV scanner, if its still online
     * @return boolean value, True if the scanner is reachable and False otherwise
     */
    public boolean checkScannerStatus() {
        return clamAvClient.ping();
    }

    /**
     * Use the ClamAV client to analyse the input files
     *
     * @param fichier      The Multipart file to be scanned
     * @param throwIfVirus True if you want to throw ClamAvException if file contains virus, False otherwise
     * @return True if the file may contain a virus and False otherwise
     * @throws IOException     Thrown when there is an issue reading the file content
     * @throws ClamAvException Thrown if the ClamAV server is not reachable
     */

    public boolean checkIfVirus(@NonNull MultipartFile fichier, boolean throwIfVirus) throws IOException, ClamAvException {
        ClamAvScanStatus status = checkIfVirus(fichier.getBytes());
        return processStatus(throwIfVirus, status);
    }

    /**
     * Use the ClamAV client to analyse the input files
     *
     * @param resource     The Multipart file to be scanned
     * @param throwIfVirus True if you want to throw ClamAvException if file contains virus, False otherwise
     * @return True if the file may contain a virus and False otherwise
     * @throws IOException     Thrown when there is an issue reading the file content
     * @throws ClamAvException Thrown if the ClamAV server is not reachable
     */
    public boolean checkIfVirus(@NonNull Resource resource, boolean throwIfVirus) throws IOException, ClamAvException {
        ClamAvScanStatus status = checkIfVirus(Files.readAllBytes(resource.getFile().toPath()));
        return processStatus(throwIfVirus, status);
    }

    private boolean processStatus(boolean throwIfVirus, ClamAvScanStatus status) throws ClamAvException {
        boolean hasVirus = false;

        if (throwIfVirus && status == ClamAvScanStatus.VIRUS) {
            throw new ClamAvException(suspectedFileMessage);
        } else if (status == ClamAvScanStatus.VIRUS) {
            hasVirus = true;
        } else if (status == ClamAvScanStatus.FAILED) {
            throw new ClamAvException(failedToScanMessage);
        }
        return hasVirus;
    }

    private ClamAvScanStatus checkIfVirus(@NonNull byte[] data) {
        return clamAvClient.scan(data);
    }
}
