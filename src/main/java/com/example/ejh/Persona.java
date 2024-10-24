package com.example.ejh;

import java.util.Objects;

public class Persona {
    private int id;
    private String nombre;
    private String apellidos;
    private int edad;

    public Persona(int id, String nombre, String apellidos, int edad) {
        this.id=id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre=nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String nombre) {
        this.apellidos=apellidos;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Persona persona = (Persona) obj;

        if (edad != persona.edad) return false;
        if (!nombre.equals(persona.nombre)) return false;
        return apellidos.equals(persona.apellidos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellidos);
    }

}
