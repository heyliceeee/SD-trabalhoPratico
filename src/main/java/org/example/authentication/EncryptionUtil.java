package org.example.authentication;

import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionUtil {
    public static String encryptPassword(String password) {

        try {
            // Gerar par de chaves
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Obter chave pública e privada
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // Salvar as chaves em arquivos
            saveKeyToFile("public.key", publicKey.getEncoded());
            saveKeyToFile("private.key", privateKey.getEncoded());

            System.out.println("Chaves geradas e salvas com sucesso!");
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Erro ao gerar as chaves: " + e.getMessage());
        }

        try {
            // Carregar a chave pública
            FileInputStream fis = new FileInputStream("public.key");
            byte[] keyBytes = fis.readAllBytes();
            fis.close();

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            // Encriptar a senha
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes());

            // Converter para Base64 para armazenar
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveKeyToFile(String fileName, byte[] keyBytes) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(keyBytes);
        }
    }
}
