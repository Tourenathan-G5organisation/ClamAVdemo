package com.hombee.clamavdemo.controller;

import com.hombee.clamavdemo.clamav.ClamAvScanHelper;
import com.hombee.clamavdemo.common.ErrorCodes;
import com.hombee.clamavdemo.dto.response.ResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/scanner", produces = MediaType.APPLICATION_JSON_VALUE)
public class FileScanner {

    private ClamAvScanHelper scanHelper;
    public FileScanner(ClamAvScanHelper scanHelper){
        this.scanHelper = scanHelper;
    }


    /**
     * Check the Scanner status
     * @return ResposeDTO indicating if the ClamAV scanner is reachable or not
     */
    @GetMapping("/status")
    public ResponseEntity<ResponseDTO> healthCheck() {
        ResponseDTO responseDTO = new ResponseDTO();
        boolean pong = scanHelper.checkScannerStatus();
        responseDTO.setErc(pong? ErrorCodes.SUCCESS : ErrorCodes.FAIL);
        responseDTO.setMessage(pong?"Pong": "Scanner not availbale");
        return ResponseEntity.ok(responseDTO);
    }


    /**
     * Scan a file for virus
     * @param file The input file to be scanned
     * @return ResponseDTO indicating the status of the file
     * @throws IOException
     */
    @PostMapping(value = "/scan")
    public ResponseEntity<ResponseDTO> fileScan(MultipartFile file) throws IOException {
        ResponseDTO responseDTO = new ResponseDTO();
        boolean isSuspected = scanHelper.checkIfVirus(file, true);
        responseDTO.setErc(!isSuspected? ErrorCodes.SUCCESS : ErrorCodes.FAIL);
        responseDTO.setMessage(!isSuspected?"File is Ok": "Suspected file");
        return ResponseEntity.ok(responseDTO);
    }
}
