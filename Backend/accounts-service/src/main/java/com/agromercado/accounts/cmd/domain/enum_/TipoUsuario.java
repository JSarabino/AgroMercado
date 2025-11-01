package com.agromercado.accounts.cmd.domain.enum_;

/**
 * Tipo de usuario en el sistema.
 * Define el rol principal del usuario al momento de registrarse.
 */
public enum TipoUsuario {
    CLIENTE,        // Usuario que compra productos
    PRODUCTOR,      // Usuario que vende/produce productos
    ADMIN_ZONA,     // Administrador de una zona específica
    ADMIN_GLOBAL    // Administrador del sistema completo
}
