package com.healthchain.backend.model.network;

import lombok.*;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.X509Identity;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
@Data
@Builder
public class CustomIdentity implements Identity{
    private int version;
    private String mspId;
    private String type;
    private String hospName;

    private String username;
    private Role role;

    private Credentials credentials;

//    public static class Builder {
//        private int version;
//        private String mspId;
//        private String type;
//        private String hospName;
//        private String username;
//        private Role role;
//        private Credentials credentials;
//
////        public Builder version(int version) {
////            this.version = version;
////            return this;
////        }
////
////        public Builder mspId(String mspId) {
////            this.mspId = mspId;
////            return this;
////        }
//
//        // Add similar methods for other fields
//
//        public CustomIdentity build() {
//            CustomIdentity customIdentity = new CustomIdentity();
//            customIdentity.version = this.version;
//            customIdentity.mspId = this.mspId;
//            customIdentity.type = this.type;
//            customIdentity.hospName = this.hospName;
//            customIdentity.username = this.username;
//            customIdentity.role = this.role;
//            customIdentity.credentials = this.credentials;
//            return customIdentity;
//        }
//    }
//    @Override
//    public X509Certificate getCertificate() {
//        if (credentials == null || credentials.getCertificate() == null) {
//            return null;
//        }
//        try {
//            return Identities.readX509Certificate(credentials.getCertificate());
//        } catch (CertificateException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    public PrivateKey getPrivateKey() {
//        if (credentials == null || credentials.getPrivateKey() == null) {
//            return null;
//        }
//        try {
//            return Identities.readPrivateKey(credentials.getPrivateKey());
//        } catch (InvalidKeyException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public String getMspId() {
        return this.mspId;
    }
}




