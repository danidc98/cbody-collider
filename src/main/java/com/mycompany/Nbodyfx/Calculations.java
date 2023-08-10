/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.Nbodyfx;

import java.util.ArrayList;
import javafx.collections.ObservableList;
import javafx.scene.Node;

/**
 * Clase Calculations: Creamos métodos que sirven para calcular parámetros
 * físicos de nuestras galaxias y hacer transformaciones en las variables del
 * sistema para obtener integradores simplécticos .
 *
 * @author Daniel del Canto
 */
public class Calculations {   //Constantes del integrador simpléctico

    static protected double h = 0.00001;//Paso temporal
    static protected double theta = 0.6; //Parámetro de BH.
    static double G = MassCube.G;
    double[] coefficients = new double[17];

    /**
     * Función que calcula las aceleraciones que experimentan todos los cuerpos
     * de un conjunto debido a la fuerza gravitatoia que se ejercen unos sobre
     * otros en un espacio discretizado en forma de arbol octal siguiendo el
     * algoritmo de Barnes-Hut, con un coste computacional del orden de Nlog(N),
     * siendo N el número de cuerpos del árbol.
     *
     * @param bhtree árbol octal en el que se hallan los cuerpos cuyas
     * aceleraciones gravitatorias van a ser calculadas.
     * @return Vector de vectores aceleración v3D.
     */
    public static v3D[] aVec(BHTree bhtree) {

        v3D[] vec = new v3D[bhtree.bodies.size()];
        for (int i = 0; i < bhtree.bodies.size(); i++) {
            vec[i] = bhtree.BH(bhtree.bodies.get(i), theta);
        }

        return vec;

    }

    /**
     * Función que devuelve la energia y el momento angular de un sistema de
     * cuerpos sometidos a fuerza gravitatoria.
     *
     *
     * @param galaxy Galaxia cuya energía y momento angular se desean calcular.
     *
     * @return Dos valores reales: el primero es la energía del sistema y el
     * segundo su momento angular. Las unidades nos son irrelevantes, pues solo
     * mostraremos variaciones porcentuales.
     */
    public static Object[] energia_momento(Galaxy galaxy) {
        double energy = 0.0;
        double J = 0.0;
        v3D Jv = new v3D(0.0, 0.0, 0.0);
        ObservableList<Node> bodies = galaxy.getChildren();
        Body body, body1;
        for (int i = 0; i < bodies.size(); i++) {
            if (bodies.get(i).getClass() == Body.class) {
                body = (Body) bodies.get(i);
                energy += Math.pow(body.speed.module(), 2.0) * body.mass / 2.0;
                Jv = Jv.add(new v3D(body.position.y * body.speed.z - body.position.z * body.speed.y, body.position.z * body.speed.x - body.position.x * body.speed.z, body.position.x * body.speed.y - body.position.y * body.speed.x).product(body.mass));
                for (int j = 0; j < i; j++) {
                    if (bodies.get(j).getClass() == Body.class) {
                        body1 = (Body) bodies.get(j);
                        energy -= MassCube.G * body.mass * body1.mass / (body.position.distance(body1.position));
                    }
                }
            }
        }
        Object[] values = new Object[2];
        values[0] = energy;
        values[1] = Jv;
        return values;
    }

    /**
     * Produce una variación en las posiciones de las partículas del sistema de
     * acuerdo a una transformación simpléctica de STORMER_VERLET.
     *
     * @param c Parámetro que define el avance en las posiciones. Su valor
     * depende del orden del integrador deseado.
     */
    public static void updateR(ArrayList<Body> galaxy, double c) {

        for (int i = 0; i < galaxy.size(); i++) {
            Body body = galaxy.get(i);
            double tmp = c * h;

            body.setPosition(body.position.add(body.speed.product(tmp)));

        }
    }

    /**
     * Produce una variación de las velocidades de las partículas del sistema de acuerdo a una
     * transformación simpléctica de STORMER_VERLET
     *
     * @param c Parámetro que define el avance en las velocidades. Su valor
     * depende del orden del integrador deseado.
     */
    public static void updateV(ArrayList<Body> galaxy, double c) {
        boolean check = true;
        if (!MassCube.elastic) { //En caso de que haya colisiones inelásticas, no queremos que se reconozcan mientras se realizan pasos intermedios de cálculo
            check = false;
            MassCube.elastic = true;
        }
        BHTree bhtree = new BHTree(galaxy);
        for (int i = 0; i < galaxy.size(); i++) {

            v3D a = bhtree.BH(galaxy.get(i), theta);
            galaxy.get(i).speed = galaxy.get(i).speed.add(a.product(c * h));

        }
        if (!check) {
            MassCube.elastic = false;
        }

    }

}
