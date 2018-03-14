package com.gramevapp.web.model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "timemodel")
public class TimeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column
    int anio;

    @Column
    int mes;

    @Column
    int dia;

    @Column
    int hora;

    @Column
    Date date;

    public TimeModel(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        this.date = date;
        this.anio = cal.get(Calendar.YEAR);
        this.mes = cal.get(Calendar.MONTH);
        this.dia = cal.get(Calendar.DAY_OF_MONTH);
        this.hora = cal.get(Calendar.HOUR);
    }

    // sin idTiempo
    public TimeModel(int dia, int mes, int anio, int hora) {
        this.anio = anio;
        this.mes = mes;
        this.dia = dia;
        this.hora = hora;
    }

    public TimeModel(Long id, int dia, int mes, int anio, int hora) {
        this.id = id;
        this.anio = anio;
        this.mes = mes;
        this.dia = dia;
        this.hora = hora;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getIdTiempo() {
        return id;
    }

    public void setIdTiempo(Long id) {
        this.id = id;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }
}