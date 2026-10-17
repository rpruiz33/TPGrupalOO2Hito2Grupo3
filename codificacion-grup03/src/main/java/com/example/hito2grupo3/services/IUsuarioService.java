package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.Usuario;

public interface IUsuarioService {

    Usuario crearUsuario(Usuario usuario);

    void reenviarCredenciales(Long usuarioId);

    void cambiarPassword(String email, String passwordActual, String nuevaPassword, String confirmarPassword);
}
