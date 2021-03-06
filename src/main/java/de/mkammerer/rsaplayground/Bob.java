package de.mkammerer.rsaplayground;

import de.mkammerer.rsaplayground.crypto.AES;
import de.mkammerer.rsaplayground.crypto.RSA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;

class Bob {
    private final KeyPair bobsKeys;

    Bob(KeyPair bobsKeys) {
        this.bobsKeys = bobsKeys;
    }

    public PublicKey getPublicKey() {
        return bobsKeys.getPublic();
    }

    public String decrypt(InsecureWire insecureWire, PublicKey senderPublicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        // First, verify that the message is really from the sender using her public key
        if (!RSA.verify(insecureWire.getEncryptedSessionKey(), insecureWire.getSignature(), senderPublicKey)) {
            throw new IllegalStateException("Unable to verify the authenticity of the message!");
        }

        // Then decrypt the session key with our own RSA private key
        byte[] sessionKey = RSA.decrypt(insecureWire.getEncryptedSessionKey(), bobsKeys.getPrivate());

        // And use that session key to decrypt the AES data
        byte[] messageAsBytes = AES.decrypt(insecureWire.getEncryptedMessage(), sessionKey);
        return new String(messageAsBytes, StandardCharsets.UTF_8);
    }
}
