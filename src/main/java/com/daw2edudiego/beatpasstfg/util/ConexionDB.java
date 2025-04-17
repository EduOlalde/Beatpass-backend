/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.daw2edudiego.beatpasstfg.util;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo Olalde
 */
public class ConexionDB {

    private static final String URL = "jdbc:mysql://localhost/beatpassTFG";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            // Cargar el driver de MySQL
            Class.forName("com.mysql.jdbc.Driver");
            return (Connection) DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException ex) {
            // Manejar excepciones de conexión
            Logger.getLogger(ConexionDB.class.getName()).log(Level.SEVERE, "Error al conectar a la base de datos", ex);
            return null;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                // Log para indicar que la conexión se cerró correctamente
                Logger.getLogger(ConexionDB.class.getName()).log(Level.INFO, "Conexión cerrada correctamente");
            } catch (SQLException ex) {
                // Manejar excepciones al cerrar la conexión
                Logger.getLogger(ConexionDB.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión", ex);
            }
        }
    }
}
