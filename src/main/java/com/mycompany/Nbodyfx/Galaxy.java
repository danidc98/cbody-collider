/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.Nbodyfx;

import java.util.ArrayList;
import java.util.Random;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

/**
 * Clase Galaxia. Nos permite instanciar objetos que contienen un conjunto de
 * cuerpos celestes.
 *
 * @author Daniel del Canto
 */
public class Galaxy extends SmartGroup {

    private static final Random rng = new Random(0); //Inicializamos el objeto de clsae Random, que servirá para situar cuerpos en el espacio de forma aleatoria.
    BHTree bhtree = new BHTree();
    static Color color;
    double time = 0.0;
    double energy_o,energy_f; //Máxima distancia de un cuerpo con respecto al centro de masas (para fijar la cámara en z) y energías inicial y final.
    v3D L_o,L_f;
    /**
     * Inicializamos la galaxía como objeto cuya clase es extensión de la clase
     * Grupo.
     */
    public Galaxy() {
             
             
             
       
    }

    /**
     *
     * @param N Número de partículas de la galaxia, sin considerar el la
     * partícula masiva central.
     * @param pos Posición del centro de la galaxia.
     * @param speed Velocidad de la galaxia.
     * @param stdDev Estimación del tamaño de la galaxia (desviación estándar de
     * la distribución gaussiana que se emplea para posisionar los cuerpos).
     * @param mass Masa de cada cuerpo que forma parte de la galaxia.
     * @param radius Radio de los cuerpos esféricos.
     * @return Objeto de clase SmartGroup, extensión de la clase Group y
     * susceptible de ser rotado. Creamos una galaxia cuya distribución de masas
     * es gaussiana en forma de disco con cierto espesor en el eje perpendicular
     * Hay un cuerpo más masivo en el centro de la galaxia, con una masa igual a
     * la suma del resto de masas.
     *
     */
    public static SmartGroup gaussianGalaxy(int N, v3D pos, v3D speed, double stdDev, double mass, double radius) {

        ArrayList<Body> bodies = new ArrayList<Body>();
        double x, y, z;
        var pm = new PhongMaterial(Galaxy.color);
        Body body = new Body(mass * N, radius, new v3D(0, 0, 0), speed);
        body.setMaterial(pm);

        SmartGroup group = new SmartGroup();

        group.getChildren().add(body);

        for (int i = 1; i < N;) {
            x = rng.nextGaussian() * stdDev;
            y = rng.nextGaussian() * stdDev;
            z = rng.nextGaussian() * stdDev * 0.05;

            // if (x * x + y * y + z * z > stdDev * 0.1 & x * x + y * y + z * z < stdDev * 0.5) {
            body = new Body(mass, radius, new v3D(x, y, z), speed);
            body.setMaterial(pm);
           
            group.getChildren().add(body);

            i++;
            // }
            //https://github.com/oliverzh2000/barnes-hut-simulator/blob/master/src/Simulation.java
        }   //Fijamos las velocidades de las masas para que estén en orbita.

        BHTree bhtree = new BHTree(group);
        v3D a, r;
        double amod, rmod, vmod;

        for (int i = 0; i < bhtree.bodies.size(); i++) {

            body = bhtree.bodies.get(i);
            a = bhtree.BH(body, Calculations.theta);
            amod = a.module();
            r = body.position;
            rmod = r.module();
            vmod = Math.sqrt(amod * rmod);
            body.speed = new v3D(a.y * vmod / amod + speed.x, -a.x * vmod / amod + speed.y, 0);
        }

        group.getChildren().clear();
        group.getChildren().addAll(bhtree.bodies);
        return group;
        
        
    }

   

}
