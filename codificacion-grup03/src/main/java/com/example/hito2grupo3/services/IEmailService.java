package com.example.hito2grupo3.services;

public interface IEmailService {
    void enviarCredenciales(String email, String passwordPlano);
    String construirCuerpo(String email, String passwordPlano);
}
