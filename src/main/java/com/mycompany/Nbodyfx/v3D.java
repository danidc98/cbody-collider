/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.Nbodyfx;

import static java.lang.Math.abs;
import static java.lang.Math.sin;
import javafx.scene.Node;

import javafx.scene.transform.Transform;

/**
 * Clase de vectores 3D. Empleada para los cálculos vectoriales en los
 * movimientos de los cuerpos celestes.
 *
 * @author Daniel del Canto
 */
public class v3D {

    public double x, y, z;

    /**
     * Genera vector v3D a partir de sus coordenadas 3D
     *
     * @param x Coordenada x
     * @param y Coordenada y
     * @param z Coordenada z
     */
    public v3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public v3D() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    /**
     * Genera un vector con dirección aleatoria en coordendas esféricas de
     * tamaño máximo scale.
     *
     * @param scale Módulo máximo del vector
     * @return Vector v3D con módulo máximo scale y dirección aleatoria.
     */
    public static v3D getRandomInstance(double scale) {
        double mod = scale * Math.random(), phi = 2 * Math.PI * Math.random(), theta = Math.PI * Math.random();
        return new v3D(mod * Math.sin(theta) * Math.cos(phi), mod * Math.sin(phi) * sin(theta), mod * Math.cos(theta));
    }
    
    /**Transformed coordinate vector.
     * 
     * @param node Nodo en cuyo sistema de coordenadas está dado el vector.
     * @return 
     */
    public v3D tcVector(Node node){
                 Transform  transform= node.getLocalToParentTransform();
                 v3D newvector=new v3D();
                 newvector.x= transform.getMxx()*this.x+transform.getMxy()*this.y+transform.getMxz()*this.z+ transform.getTx();
                 newvector.y= transform.getMyx()*this.x+transform.getMyy()*this.y+transform.getMyz()*this.z+transform.getTy();
                 newvector.z= transform.getMzx()*this.x+transform.getMzy()*this.y+transform.getMzz()*this.z+ transform.getTz();
                 return newvector;
    
    
    }
    

    /**
     * Función que calcula el vector relativo entre dos puntos del espacio.
     *
     * @param c Punto respecto del cual se calcula el vector relativo
     * @return Vector v3D , resta de c y nuestro objeto.
     */
    public v3D distance3D(v3D c) {
        return new v3D(c.x - x, c.y - y, c.z - z);
    }

    /**
     * Módulo de un vector
     *
     * @return Resultado escalar correspondiente al módulo del vector.
     */
    public double module() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Función que devuelve el ángulo con el eje x de la proyección sobre el
     * plano XY de un vector.
     *
     * @return Ángulo phi
     */
    public double phi() {
        return Math.atan2(y, x);
    }

    /**
     * Función que devuelve el ángulo con el eje z de un vector
     *
     * @return Ángulo theta
     */
    public double theta() {

        return Math.atan2(z, Math.sqrt(x * x + y * y));
    }

    /**
     * Producto de un vector por un escalar (versión no estática)
     *
     * @param d Escalar por el que se multiplica el vector
     * @return Vector resultante de multiplicar v y d.
     */
    public v3D product(double d) {
        return product(this, d);
    }

    /**
     * Producto de un vector por un escalar.
     *
     * @param d Escalar por el que se multiplica el vector
     * @param v Vector que se va a multiplicar por el escalar.
     * @return Resultado del producto de d y v en forma de vector.
     */
    public static v3D product(v3D v, double d) {

        return new v3D(v.x * d, v.y * d, v.z * d);
    }

    
    //
    /**
     * Devuelve True si el vector posición de un punto se halla dentro de un
     * cubo con centro en center y tamaño size.
     *
     * @param p Punto cuya posición se va a comprobar.
     * @param center Centro del cubo dentro del cual se va a comprobar que está
     * el punto p.
     * @param size Tamaño del cubo
     * @return Booleano que será true si p está contenido en el espacio del
     * cubo.
     */
    public static boolean inside(v3D p, v3D center, double size) {
        return abs(p.x - center.x) <= size / 2.0 & abs(p.y - center.y) <= size / 2.0 & abs(p.z - center.z) <= size / 2.0;

    }

    
    /**
     * Módulo de la distancia de un punto a otro.
     *
     * @param c Punto respecto del cual se calcula la distancia.
     * @return Módulo de la distancia entre c y this.
     */
    public double distance(v3D c) {
        return this.distance3D(c).module();
    }

    /**
     * Suma de dos vectores.
     *
     * @param v Vector a sumar a nuestro vector.
     * @return Suma de los dos vectores.
     */
    public v3D add(v3D v) {
        return new v3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Función que normaliza un vector.
     *
     * @return Vector normalizado (dividido por su módulo).
     */
    public v3D normalize() {
        double m = this.module();
        x = x / m;
        y = y / m;
        z = z / m;
        return this;
    }

    @Override
    public String toString() {
        return "v3d{" + x + "," + y + "," + z + '}';
    }

    v3D myClone() {
        return new v3D(x, y, z);
    }

    /**
     * Dada una transformacion afín, esta función aplica la transformación a un
     * vector v3D a partir de los elementos de matriz de la misma, es decir,
     * rota y traslada el vector.
     *
     * @param t Transformación a aplicar al vector
     * @return Vector resultante de la transformación.
     */
    public v3D getRot_trans(Transform t) {  //Las coordenadas x,y,z son el resultado de aplicarle al vector
        //una transformación compuesta de una rotación alrededor del eje x y otra del eje y con origen en center.
        double x, y, z;
        x = this.x * t.getMxx() + this.y * t.getMxy() + this.z * t.getMxz() + t.getTx();
        y = this.x * t.getMyx() + this.y * t.getMyy() + this.z * t.getMyz() + t.getTy();
        z = this.x * t.getMzx() + this.y * t.getMzy() + this.z * t.getMzz() + t.getTz();
        return new v3D(x, y, z);

    }

    /**
     * Dada una transformacion afín t, esta función aplica su componente de
     * rotación a un vector v3D.
     *
     * @param t a aplicar al vector.
     * @return Vector resultante de la rotación.
     */
    public v3D getRot(Transform t) {  //Las coordenadas x,y,z son el resultado de aplicarle al vector
        //una transformación compuesta de una rotación alrededor del eje x y otra del eje y con origen en center.
        double x, y, z;
        x = this.x * t.getMxx() + this.y * t.getMxy() + this.z * t.getMxz();
        y = this.x * t.getMyx() + this.y * t.getMyy() + this.z * t.getMyz();
        z = this.x * t.getMzx() + this.y * t.getMzy() + this.z * t.getMzz();
        return new v3D(x, y, z);

    }

}
