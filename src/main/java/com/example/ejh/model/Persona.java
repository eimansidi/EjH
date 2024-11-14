package com.example.ejh.model;

import java.util.Objects;

public class Persona {
    private int id;
    private String nombre;
    private String apellidos;
    private int edad;

    /**
     * Constructor para crear una persona con id, nombre, apellidos y edad.
     *
     * @param id El identificador de la persona.
     * @param nombre El nombre de la persona.
     * @param apellidos Los apellidos de la persona.
     * @param edad La edad de la persona.
     */
    public Persona(int id, String nombre, String apellidos, int edad) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
    }

    /**
     * Obtiene el id de la persona.
     *
     * @return El id de la persona.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el nombre de la persona.
     *
     * @return El nombre de la persona.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre de la persona.
     *
     * @param nombre El nuevo nombre de la persona.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene los apellidos de la persona.
     *
     * @return Los apellidos de la persona.
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Establece los apellidos de la persona.
     *
     * @param apellidos Los nuevos apellidos de la persona.
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Obtiene la edad de la persona.
     *
     * @return La edad de la persona.
     */
    public int getEdad() {
        return edad;
    }

    /**
     * Establece la edad de la persona.
     *
     * @param edad La nueva edad de la persona.
     */
    public void setEdad(int edad) {
        this.edad = edad;
    }

    /**
     * Compara dos objetos Persona para verificar si son iguales.
     *
     * @param obj El objeto con el que se va a comparar.
     * @return true si los objetos son iguales, false si no lo son.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Persona persona = (Persona) obj;

        if (edad != persona.edad) return false;
        if (!nombre.equals(persona.nombre)) return false;
        return apellidos.equals(persona.apellidos);
    }

    /**
     * Genera un código hash para la persona.
     *
     * @return El código hash basado en el nombre y los apellidos de la persona.
     */
    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellidos);
    }
}
