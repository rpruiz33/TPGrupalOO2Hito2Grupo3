package com.example.hito2grupo3.services;

import com.example.hito2grupo3.entities.Usuario;

public interface IUsuarioService {

    Usuario crearUsuario(Usuario usuario);

    java.util.List<Usuario> listarTodos();

    Usuario buscarPorEmail(String email);

    boolean debeCambiarPassword(String email);

    void reenviarCredenciales(Long usuarioId);

    void cambiarPassword(String email, String passwordActual, String nuevaPassword, String confirmarPassword);
}

