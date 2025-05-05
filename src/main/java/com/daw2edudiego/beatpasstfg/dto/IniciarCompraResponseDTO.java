package com.daw2edudiego.beatpasstfg.dto;

/**
 * DTO para enviar la respuesta al frontend al iniciar el proceso de compra.
 * Contiene el client_secret necesario para Stripe Elements/Checkout.
 *
 * @author Eduardo Olalde
 */
public class IniciarCompraResponseDTO {

    private String clientSecret;

    public IniciarCompraResponseDTO(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    // Getter (Setter no es necesario si solo se construye)
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
