package io.sol.loanmanagementsystemspringbootserver.utilities;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileEncryptor {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY_FILE = "secret.key";

    public void generateKey() throws Exception{
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();

        try(FileOutputStream fos = new FileOutputStream(SECRET_KEY_FILE)){
            fos.write(key.getEncoded());
        }
    }

    private static SecretKeySpec getSecretKey() throws Exception{
        byte[] keyBytes = new byte[16];
        try(FileInputStream fis = new FileInputStream(SECRET_KEY_FILE)){
            fis.read(keyBytes);
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public static void encryptFile(String inputFile, String outputFile) throws Exception{
        SecretKey secretKey = getSecretKey();
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        try(FileInputStream fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile, true);
        ){
           byte[] inputBytes = fis.readAllBytes();
           byte[] outputBytes = cipher.doFinal(inputBytes);
           fos.write(outputBytes);
        }
    }

    public static void decrypt(String inputFile, String outputFile) throws Exception {
        SecretKey secerteKey = getSecretKey();

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secerteKey);

        try(FileInputStream  fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile, true);
        ){
            byte[] inputBytes = fis.readAllBytes();
            byte[] outputBytes = cipher.doFinal(inputBytes);
            fos.write(outputBytes);
        }

    }
}
