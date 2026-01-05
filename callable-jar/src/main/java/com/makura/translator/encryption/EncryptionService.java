package com.makura.translator.encryption;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;

/**
 * Encryption/Decryption service supporting AES and PGP
 * Standalone implementation (no Spring dependencies)
 */
public class EncryptionService {

    private final String keysPath;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public EncryptionService(String keysPath) {
        this.keysPath = keysPath;
    }

    /**
     * Encrypt content using AES
     */
    public String encryptAes(String content, String keyRef) throws EncryptionException {
        try {
            byte[] key = loadKeyFromFile(keyRef, "aes");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encrypted = cipher.doFinal(content.getBytes("UTF-8"));
            byte[] iv = cipher.getIV();
            
            // Combine IV and encrypted data
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new EncryptionException("AES encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt content using AES
     */
    public String decryptAes(String encryptedContent, String keyRef) throws EncryptionException {
        try {
            byte[] key = loadKeyFromFile(keyRef, "aes");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            
            byte[] combined = Base64.getDecoder().decode(encryptedContent);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            throw new EncryptionException("AES decryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Encrypt content using PGP
     */
    public String encryptPgp(String content, String keyRef) throws EncryptionException {
        try {
            PGPPublicKey publicKey = loadPgpPublicKey(keyRef);
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(
                new JcePGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
                    .setWithIntegrityPacket(true)
                    .setSecureRandom(new java.security.SecureRandom())
            );
            encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(publicKey));
            
            OutputStream encryptedOut = encryptedDataGenerator.open(out, new byte[4096]);
            encryptedOut.write(content.getBytes("UTF-8"));
            encryptedOut.close();
            
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new EncryptionException("PGP encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt content using PGP
     */
    public String decryptPgp(String encryptedContent, String keyRef) throws EncryptionException {
        try {
            PGPPrivateKey privateKey = loadPgpPrivateKey(keyRef);
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedContent);
            PGPObjectFactory pgpFactory = new PGPObjectFactory(
                PGPUtil.getDecoderStream(new ByteArrayInputStream(encryptedBytes)),
                new org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator()
            );
            
            PGPEncryptedDataList encryptedDataList = (PGPEncryptedDataList) pgpFactory.nextObject();
            PGPPublicKeyEncryptedData encryptedData = (PGPPublicKeyEncryptedData) encryptedDataList.get(0);
            
            InputStream clear = encryptedData.getDataStream(
                new org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder()
                    .setProvider("BC")
                    .build(privateKey)
            );
            
            PGPObjectFactory plainFact = new PGPObjectFactory(
                clear,
                new org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator()
            );
            PGPCompressedData compressedData = (PGPCompressedData) plainFact.nextObject();
            PGPObjectFactory pgpFact = new PGPObjectFactory(
                compressedData.getDataStream(),
                new org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator()
            );
            PGPLiteralData literalData = (PGPLiteralData) pgpFact.nextObject();
            
            InputStream inputStream = literalData.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            throw new EncryptionException("PGP decryption failed: " + e.getMessage(), e);
        }
    }

    private byte[] loadKeyFromFile(String keyRef, String type) throws IOException {
        Path keyPath = Paths.get(keysPath, type, keyRef + ".key");
        if (!Files.exists(keyPath)) {
            throw new IOException("Key file not found: " + keyPath);
        }
        return Files.readAllBytes(keyPath);
    }

    private PGPPublicKey loadPgpPublicKey(String keyRef) throws Exception {
        Path keyPath = Paths.get(keysPath, "pgp", keyRef + "_public.asc");
        InputStream keyIn = new FileInputStream(keyPath.toFile());
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(
            PGPUtil.getDecoderStream(keyIn),
            new org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator()
        );
        return pgpPub.getKeyRings().next().getPublicKey();
    }

    private PGPSecretKey loadPgpSecretKey(String keyRef) throws Exception {
        Path keyPath = Paths.get(keysPath, "pgp", keyRef + "_private.asc");
        InputStream keyIn = new FileInputStream(keyPath.toFile());
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
            PGPUtil.getDecoderStream(keyIn),
            new org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator()
        );
        return pgpSec.getKeyRings().next().getSecretKey();
    }

    private PGPPrivateKey loadPgpPrivateKey(String keyRef) throws Exception {
        PGPSecretKey secretKey = loadPgpSecretKey(keyRef);
        // Note: In production, you'd need to prompt for passphrase or use a secure key store
        // For now, assuming no passphrase
        return secretKey.extractPrivateKey(
            new org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder()
                .setProvider("BC")
                .build(new char[0])
        );
    }

    public static class EncryptionException extends Exception {
        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}




