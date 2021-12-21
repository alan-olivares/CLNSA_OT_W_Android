package com.pims.alanolivares.clnsa_ot_w.Funciones;

public class SpinnerObjeto {
    int id;
    String valor;

    public SpinnerObjeto(int id, String valor) {
        this.id = id;
        this.valor = valor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
    //to display object as a string in spinner
    @Override
    public String toString() {
        return valor;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SpinnerObjeto){
            SpinnerObjeto c = (SpinnerObjeto )obj;
            if(c.getValor().equals(valor) && c.getId()==id ) return true;
        }

        return false;
    }

}
