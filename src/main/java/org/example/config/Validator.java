package org.example.config;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.model.Funcionario;

public class Validator {


    /**
     * Valida campos obligatorios y reglas básicas.
     * Si hay errores devuelve lista con mensajes (vacía = sin errores).
     */
    public static List<String> validarFuncionario(Funcionario f) {
        List<String> errores = new ArrayList<>();

        if (f == null) {
            errores.add("Funcionario nulo.");
            return errores;
        }

        if (isNullOrEmpty(f.getTipoIdentificacion()))
            errores.add("Tipo de identificación es obligatorio.");

        if (isNullOrEmpty(f.getNumeroIdentificacion()))
            errores.add("Número de identificación es obligatorio.");

        if (isNullOrEmpty(f.getNombres()))
            errores.add("Nombres son obligatorios.");

        if (isNullOrEmpty(f.getApellidos()))
            errores.add("Apellidos son obligatorios.");

        if (isNullOrEmpty(f.getSexo()))
            errores.add("Sexo es obligatorio (M/F/O).");
        else {
            String s = f.getSexo().trim().toUpperCase();
            if (!s.equals("M") && !s.equals("F") && !s.equals("O"))
                errores.add("Sexo inválido. Use 'M' (masculino), 'F' (femenino) o 'O' (otro).");
        }

        // Ejemplo: número mínimo de caracteres para documento (solo ejemplo)
        if (f.getNumeroIdentificacion() != null && f.getNumeroIdentificacion().trim().length() < 10)
            errores.add("Número de identificación muy corto.");

        // Fecha nacimiento: opcional pero si existe validar que no sea futura
        LocalDate fn = f.getFechaNacimiento();
        if (fn != null && fn.isAfter(LocalDate.now()))
            errores.add("Fecha de nacimiento no puede ser futura.");

        // Teléfono: si existe, validar longitud mínima
        if (f.getTelefono() != null && f.getTelefono().trim().length() > 0 && f.getTelefono().trim().length() < 10)
            errores.add("Teléfono inválido o muy corto.");

        return errores;
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
