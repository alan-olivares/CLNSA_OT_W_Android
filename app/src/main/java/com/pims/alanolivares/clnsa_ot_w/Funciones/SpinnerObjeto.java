package com.pims.alanolivares.clnsa_ot_w.Funciones;


/**
 * <p>Objeto que almacena los pares clave-valor que se usan en los Spinner
 * encontrados dentro de la aplicación
 * </p>
 *
 * @author Alan Israel Olivares Mora
 * @version v1.0
 *
 */
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

    @Override
    public String toString() {
        return valor;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SpinnerObjeto){
            SpinnerObjeto c = (SpinnerObjeto )obj;
            return (c.getValor().equals(valor) && c.getId()==id);
        }
        return false;
    }

}
