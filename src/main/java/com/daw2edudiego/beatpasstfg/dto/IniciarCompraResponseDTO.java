package com.daw2edudiego.beatpasstfg.dto;

/**
 * DTO para enviar la respuesta al frontend al iniciar el proceso de compra.
 * Contiene el client_secret necesario para Stripe Elements/Checkout.
 */
public class IniciarCompraResponseDTO {

    private String clientSecret;

    public IniciarCompraResponseDTO(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
