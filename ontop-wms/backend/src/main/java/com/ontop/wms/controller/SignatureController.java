package com.ontop.wms.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ontop.wms.entity.Signature;
import com.ontop.wms.service.SignatureService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureService signatureService;

    @GetMapping("/{type}/{id}")
    public ResponseEntity<List<Signature>> getSignatures(@PathVariable String type, @PathVariable Integer id) {
        return ResponseEntity.ok(signatureService.getSignaturesForTransaction(type.toUpperCase(), id));
    }

    @GetMapping("/verify")
    public ResponseEntity<Signature> getSignatureByToken(@RequestParam String token) {
        return ResponseEntity.ok(signatureService.getSignatureByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token")));
    }

    @PostMapping("/sign")
    public ResponseEntity<Signature> signDocument(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String token = payload.get("token");
        String signerName = payload.get("signerName");
        String ipAddress = request.getRemoteAddr();
        
        return ResponseEntity.ok(signatureService.verifyAndSign(token, signerName, ipAddress));
    }

    @PostMapping("/initiate/{type}/{id}")
    public ResponseEntity<Void> initiate(@PathVariable String type, @PathVariable Integer id, @RequestBody Map<String, List<String>> payload) {
        signatureService.initiateSignatures(type.toUpperCase(), id, payload.get("emails"));
        return ResponseEntity.ok().build();
    }
}
