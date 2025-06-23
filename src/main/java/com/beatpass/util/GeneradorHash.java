package com.beatpass.util;

public class GeneradorHash {

    public static void main(String[] args) {
        // ---------------------------------------------------------------
        // 1. DEFINE AQUÍ LA CONTRASEÑA QUE QUIERES HASHEAR
        // ---------------------------------------------------------------
        String password = "pass";
        // ---------------------------------------------------------------

        System.out.println("Generando hash para la contraseña: " + password);

        try {
            // 2. Llama al método de tu utilidad para generar el hash
            String hashGenerado = PasswordUtil.hashPassword(password);

            // 3. Imprime el hash resultante en la consola
            System.out.println("---------------------------------------------------------------");
            System.out.println("HASH GENERADO (BCrypt):");
            System.out.println(hashGenerado);
            System.out.println("---------------------------------------------------------------");
            System.out.println("Copia y pega este hash en la columna 'password' de tu tabla 'usuarios' para el usuario.");

        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al generar el hash: " + e.getMessage());
        }
    }
}
