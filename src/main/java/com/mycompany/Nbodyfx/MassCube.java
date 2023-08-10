/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.Nbodyfx;

import static com.mycompany.Nbodyfx.Body.lim_n_center;
import java.util.ArrayList;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;


/**
 * Clase MassCube. Se trata de nodos de un árbol octal. Cada nodo tiene un
 * centro de masas, una masa, 8 nodos hijos y un cuerpo (en caso de ser un nodo
 * hoja (leaf=true cm!=null))
 *
 * @author Daniel del Canto
 */
public class MassCube {

    v3D cm;
    double mass;
    MassCube _000 = null;     
    MassCube _001 = null;
    MassCube _010 = null;
    MassCube _011 = null;
    MassCube _100 = null;
    MassCube _101 = null;
    MassCube _111 = null;
    MassCube _110 = null;
    Body body = null;
    static PhongMaterial pm = new PhongMaterial(Color.YELLOWGREEN);
    boolean leaf = true;
    static boolean elastic = true;

    static double G = 1.966666666667 * Math.pow(10.0, -9.0); //Unidades m->u.a, kg->m=1*10^20, s->año
    public static boolean hola(){return false;}
    @Override
    public String toString() {
        return "MassCube{" + "cm=" + cm + ", mass=" + mass + ", _000=" + _000 + ", _001=" + _001 + ", _010=" + _010 + ", _011=" + _011 + ", _100=" + _100 + ", _101=" + _101 + ", _111=" + _111 + ", _110=" + _110 + ", leaf=" + leaf + '}';
    }

    public MassCube() {
    }

    /**
     * Actualizar el vector del centro de masas de un nodo.
     *
     * @param body Cuerpo a partir del cual se recalcula el centro de masas.
     */
    public void updateCm(Body body) {
        this.cm = body.position.product(body.mass / (body.mass + mass)).add(cm.product(mass / (mass + body.mass)));
        this.mass = this.mass + body.mass;
    }

    //insertBody: Función recursiva que inserta un cuerpo en un árbol octal.
    //Objetos de entrada: body, cuerpo a insertar y this, el nodo padre del árbol. Center es el centro geométrico del cubo del nodo raiz y  size su tamaño
    /**
     * Función recursiva que inserta un cuerpo en un árbol octal.
     *
     * @param body cuerpo a insertar.
     * @param center centro geométrico del cubo del nodo.
     * @param size tamaño del cubo del nodo.
     */
    public void insertBody(Body body, v3D center, double size) {

        //Body newbody;
        v3D newspeed, newposition;
        double newradius;

        if (cm == null) {
            cm = body.position;
            mass = body.mass;
            this.body = body;
        } else {

            if (this.leaf) { //Al llegar a haber 2 cuerpos en un cubo, debemos dividirlo.
                if (!elastic) {
                    if (body.position.distance(this.body.position) <= body.radius + this.body.radius) {
                        //Aquellos objetos que hayan colisionado se añadirán como el centro de masas de ambos objetos.
                        //Se eliminarán de la lista de cuerpos del BHTree una vez se haya construido. Para ello definimos befcoll y aftcoll
                        newradius = Math.cbrt(Math.pow(this.body.radius, 3) + Math.pow(body.radius, 3));
                        newposition = body.position.product(body.mass / (body.mass + this.mass)).add(this.cm.product(this.mass / (mass + body.mass)));
                        newspeed = body.speed.product(body.mass / (body.mass + this.mass)).add(this.body.speed.product(this.mass / (this.mass + body.mass)));

                        BHTree.befcoll.add(body);
                        BHTree.befcoll.add(this.body);

                        //Añadimos estas líneas para borrar objetos colisionados y añadir los resultantes.
                        this.body = new Body(body.mass + this.mass, newradius, newposition, newspeed);
                        BHTree.aftcoll.add(this.body);

                        this.cm = this.body.position;
                        this.mass = this.body.mass;

                    } else {
                        this.leaf = false;
                        subInsertBody(body, center, size);

                        subInsertBody(this.body, center, size);
                        this.body = null;
                        updateCm(body);

                    }

                } else {
                    this.leaf = false;
                    if (body.position.distance(this.body.position) == 0.0) {
                        body.setPosition(new v3D(body.position.x + 0.0000000001, body.position.y, body.position.z));
                    }
                    subInsertBody(body, center, size);

                    subInsertBody(this.body, center, size);
        
                    this.body = null;
                    updateCm(body);

                    
                }

            } else {
                updateCm(body);
                subInsertBody(body, center, size);

            }

        }

    }

    /**
     * Inserta un cuerpo en el nodo hijo apropiado de un nodo raiz con centro
     * "centro" y tamaño "size."
     *
     * @param body cuerpo a insertar.
     * @param center centro geométrico del cubo del nodo padre del nodo en el
     * que se insertará el cuerpo.
     * @param size tamaño del cubo del nodo padre.
     */
    public void subInsertBody(Body body, v3D center, double size) {

        v3D p = body.position;
        if (p.z >= center.z) {
            if (p.y >= center.y) {
                if (p.x >= center.x) {
                    if (_000 == null) {
                        _000 = new MassCube();
                    }

                    _000.insertBody(body, center.add(new v3D(size / 4, size / 4, size / 4)), size / 2);
                } else {
                    if (_001 == null) {
                        _001 = new MassCube();
                    }
                    _001.insertBody(body, center.add(new v3D(-size / 4, size / 4, size / 4)), size / 2);

                }
            } else if (p.x >= center.x) {
                if (_011 == null) {
                    _011 = new MassCube();
                }
                _011.insertBody(body, center.add(new v3D(size / 4, -size / 4, size / 4)), size / 2);

            } else {
                if (_010 == null) {
                    _010 = new MassCube();
                }
                _010.insertBody(body, center.add(new v3D(-size / 4, -size / 4, size / 4)), size / 2);

            }

        } else {
            if (p.y >= center.y) {
                if (p.x >= center.x) {

                    if (_100 == null) {
                        _100 = new MassCube();
                    }
                    _100.insertBody(body, center.add(new v3D(size / 4, size / 4, -size / 4)), size / 2);
                } else {
                    if (_101 == null) {
                        _101 = new MassCube();
                    }
                    _101.insertBody(body, center.add(new v3D(-size / 4, size / 4, -size / 4)), size / 2);
                }

            } else {
                if (p.x >= center.x) {
                    if (_111 == null) {
                        _111 = new MassCube();
                    }
                    _111.insertBody(body, center.add(new v3D(+size / 4, -size / 4, -size / 4)), size / 2);
                } else {
                    if (_110 == null) {
                        _110 = new MassCube();
                    }
                    {
                        _110.insertBody(body, center.add(new v3D(-size / 4, -size / 4, -size / 4)), size / 2);
                    }

                }
            }
        }

    }

    /**
     * Función que calcula la aceleración que experimenta un cuerpo debido a la
     * fuerza que sobre él ejerce un conjunto de cuerpos en un espacio
     * discretizado en forma de arbol octal, siguiendo el algoritmo de
     * Barnes-Hut, con un coste computacional del orden de Nlog(N), siendo N el
     * número de cuerpos del árbol.
     *
     * @param body cuerpo sobre el que actúan las fuerzas a calcular.
     * @param theta parámetro de precisión del algoritmo de Barnes-Hut, que
     * determinará el tamaño máximo de las celdas para que este algoritmo sea
     * utilizado.
     * @param size tamaño del cubo del nodo raiz del árbol.
     * @param center centro geométrico del cubo del nodo raíz del árbol.
     * @return Vector v3D que representa la aceleración total que experimenta el
     * cuerpo.
     */
    public v3D BH(Body body, double theta, double size, v3D center) {
        v3D acc = new v3D(0.0, 0.0, 0.0);
        v3D dist;
        v3D ncenter;
        double distd, d;

        if (this != null) {
            if (!this.leaf) {

                dist = body.position.distance3D(cm);
                distd = body.position.distance3D(cm).module();

                if (size / distd < theta & !v3D.inside(body.position, center, size)) {

                    acc = acc.add(dist.product(G * this.mass / Math.pow(distd, 3)));
                } //acc+=-G*M*r_ij/|r_ij|^3
                else {
                    if (_000 != null) {
                        acc = acc.add(_000.BH(body, theta, size / 2, center.add(new v3D(size / 4, size / 4, size / 4))));
                    }

                    if (_001 != null) {
                        acc = acc.add(_001.BH(body, theta, size / 2, center.add(new v3D(-size / 4, size / 4, size / 4))));
                    }

                    if (_010 != null) {
                        acc = acc.add(_010.BH(body, theta, size / 2, center.add(new v3D(-size / 4, -size / 4, size / 4))));
                    }

                    if (_011 != null) {
                        acc = acc.add(_011.BH(body, theta, size / 2, center.add(new v3D(size / 4, -size / 4, size / 4))));
                    }

                    if (_100 != null) {
                        acc = acc.add(_100.BH(body, theta, size / 2, center.add(new v3D(size / 4, size / 4, -size / 4))));
                    }

                    if (_101 != null) {
                        acc = acc.add(_101.BH(body, theta, size / 2, center.add(new v3D(-size / 4, size / 4, -size / 4))));
                    }

                    if (_110 != null) {
                        acc = acc.add(_110.BH(body, theta, size / 2, center.add(new v3D(-size / 4, -size / 4, -size / 4))));
                    }

                    if (_111 != null) {
                        acc = acc.add(_111.BH(body, theta, size / 2, center.add(new v3D(+size / 4, -size / 4, -size / 4))));
                    }
                }
            } else {

                if (this.body != body) {
                    dist = body.position.distance3D(this.body.position);
                    distd = dist.module();
                    d = body.radius + this.body.radius;
                    if (distd <= d) {

                        acc.add(dist.product(-(-4.0 * d + 3.0 * distd) * G * this.mass / Math.pow(d, 4.0)));

                    } else {

                        acc = acc.add(dist.product(G * this.mass / Math.pow(distd, 3)));
                    }//acc+=-G*M*r_ij/|r_ij|^3
                } //acc+=-G*M*r_ij/|r_ij|^3

            }

        }

        return acc;

    }

    /**
     * Crea "líneas"(cilindros finos) con forma de cubo de tamaño size y centro
     * geométrico center
     *
     * @param center Centro geométrico del cubo
     * @param size Tamaño del cubo
     * @param width Ancho de línea
     * @return lines : Grupo formado por objetos de clase SmartCylinder
     */
    public Group createLines(v3D center, double size, double width) {

        Group lines = new Group();
        if (_000 != null) {
            lines.getChildren().add(_000.createLines(center.add(new v3D(size / 4, size / 4, size / 4)), size / 2, width));
        }

        if (_001 != null) {
            lines.getChildren().add(_001.createLines(center.add(new v3D(-size / 4, size / 4, size / 4)), size / 2, width));
        }

        if (_010 != null) {
            lines.getChildren().add(_010.createLines(center.add(new v3D(-size / 4, -size / 4, size / 4)), size / 2, width));
        }

        if (_011 != null) {
            lines.getChildren().add(_011.createLines(center.add(new v3D(size / 4, -size / 4, +size / 4)), size / 2, width));
        }

        if (_100 != null) {
            lines.getChildren().add(_100.createLines(center.add(new v3D(+size / 4, +size / 4, -size / 4)), size / 2, width));
        }

        if (_101 != null) {
            lines.getChildren().add(_101.createLines(center.add(new v3D(-size / 4, size / 4, -size / 4)), size / 2, width));
        }

        if (_110 != null) {
            lines.getChildren().add(_110.createLines(center.add(new v3D(-size / 4, -size / 4, -size / 4)), size / 2, width));
        }

        if (_111 != null) {
            lines.getChildren().add(_111.createLines(center.add(new v3D(size / 4, -size / 4, -size / 4)), size / 2, width));
        }

        SmartCylinder sc = new SmartCylinder(width, size);
        sc.setTranslateX(center.x + size / 2);
        sc.setTranslateZ(center.z + size / 2);
        sc.setTranslateY(center.y);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.setTranslateX(center.x + size / 2);
        sc.setTranslateZ(center.z - size / 2);
        sc.setTranslateY(center.y);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.setTranslateX(center.x - size / 2);
        sc.setTranslateZ(center.z + size / 2);
        sc.setTranslateY(center.y);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.setTranslateX(center.x - size / 2);
        sc.setTranslateZ(center.z - size / 2);
        sc.setTranslateY(center.y);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByX(90);
        sc.setTranslateY(center.y + size / 2);
        sc.setTranslateX(center.x + size / 2);
        sc.setTranslateZ(center.z);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByX(90);
        sc.setTranslateY(center.y + size / 2);
        sc.setTranslateX(center.x - size / 2);
        sc.setTranslateZ(center.z);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByX(90);
        sc.setTranslateY(center.y - size / 2);
        sc.setTranslateX(center.x + size / 2);
        sc.setTranslateZ(center.z);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByX(90);
        sc.setTranslateY(center.y - size / 2);
        sc.setTranslateX(center.x - size / 2);
        sc.setTranslateZ(center.z);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByZ(90);
        sc.setTranslateY(center.y + size / 2);
        sc.setTranslateX(center.x);
        sc.setTranslateZ(center.z + size / 2);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByZ(90);
        sc.setTranslateY(center.y + size / 2);
        sc.setTranslateX(center.x);
        sc.setTranslateZ(center.z - size / 2);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByZ(90);
        sc.setTranslateY(center.y - size / 2);
        sc.setTranslateX(center.x);
        sc.setTranslateZ(center.z + size / 2);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        sc = new SmartCylinder(width, size);
        sc.rotateByZ(90);
        sc.setTranslateY(center.y - size / 2);
        sc.setTranslateX(center.x);
        sc.setTranslateZ(center.z - size / 2);
        sc.setMaterial(pm);
        lines.getChildren().add(sc);

        return lines;

    }

}

/**
 * Clase BHTree. Se trata de un objeto con estructura de árbol octal. Tiene como
 * campos el nodo raiz del árbol, el centro geométrico del cubo del nodo raiz y
 * el tamaño del mismo. Por otro lado, están los cuerpos que están situados en
 * el espacio interior del cubo del nodo raiz.
 *
 * @author Daniel del Canto
 */
class BHTree {

    static ArrayList<Body> befcoll = new ArrayList<Body>();
    static ArrayList<Body> aftcoll = new ArrayList<Body>();
    private v3D center;
    private double size;
    static double minrad, minmass, maxmass;
    MassCube mc;
    protected final ArrayList<Body> bodies = new ArrayList<>();
    Group lines = new Group();
    static double width;

    /**
     * Generamos un árbol octal como un nodo padre con un cubo con centro
     * geométrico y tamaño.
     *
     * @param center centro geométrico del cubo del nodo padre.
     * @param size tamaño del cubo del nodo padre.
     */
    public BHTree(v3D center, double size) {
        this.center = center;
        this.size = size;
        this.mc = new MassCube();
        this.width = size / 5000.0;

    }

    /**
     * Insertamos un cuerpo en el árbol.
     *
     * @param bhtree Árbol octal.
     * @param body Cuerpo a insertar.
     * @return Se devuelve el árbol actualizado.
     */
    public static BHTree insertBody(BHTree bhtree, Body body) {
        bhtree.bodies.add(body);
        Object[] limncenter;
        if (bhtree.bodies.size() == 1) {
            bhtree.center = body.position;
            bhtree.size = body.radius * 2.0;
            bhtree.mc = new MassCube();
            bhtree.mc.insertBody(body, bhtree.center, bhtree.size);
            return bhtree;
        }
        //    if (!v3D.inside(body.position, bhtree.center, bhtree.size)) {//Se tiene en cuenta que el cuerpo podría estar fuera de los límites, caso en el cuál se recalcularía la estructura del árbol.
        return new BHTree(bhtree.bodies);

        //    } else {
        //    bhtree.mc.insertBody(body, bhtree.center, bhtree.size);
        //   }
        //  return bhtree;
    }

    public v3D getCenter() {
        return center;
    }

    public double getSize() {
        return size;
    }

    public BHTree() {
    }

    /**
     * Instanciamos un árbol octal a partir de un conjunto de cuerpos.
     *
     * @param bodies Los cuerpos a partir de los cuales se crea el árbol octal.
     */
    public BHTree(ArrayList<Body> bodies) {
        if (!bodies.isEmpty()) {
            Object[] limncenter = lim_n_center(bodies);
            this.center = (v3D) limncenter[0];
            this.size = (double) limncenter[1];
            this.bodies.addAll(bodies);
            this.mc = new MassCube();

            bodies.forEach(body -> {
                // System.out.println(mc);
                this.mc.insertBody(body, center, size);
            });

            if (!MassCube.elastic) {
                BHTree.befcoll.forEach(body -> {
                    this.bodies.remove(body);
                });
                BHTree.aftcoll.forEach(body -> {
                    this.bodies.add(body);
                });

                BHTree.befcoll.clear();
                BHTree.aftcoll.clear();
            }
        }

    }

    /**
     * Instanciamos un árbol octal a partir de un conjunto de cuerpos.
     *
     * @param group Los cuerpos a partir de los cuales se crea el árbol octal.
     */
    public BHTree(SmartGroup group) {
        Object[] limncenter = lim_n_center(group);
        this.center = (v3D) limncenter[0];

        this.size = (double) limncenter[1];

        this.mc = new MassCube();
        group.getChildren().stream().map(node -> {
            this.mc.insertBody((Body) node, center, size);
            return node;
        }).forEachOrdered(node -> {
            this.bodies.add((Body) node);
        });
        if (!MassCube.elastic) {
            BHTree.befcoll.forEach(body -> {
                this.bodies.remove(body);
            });
            BHTree.aftcoll.forEach(body -> {
                this.bodies.add(body);
            });
            BHTree.befcoll.clear();
            BHTree.aftcoll.clear();
        }
    }

    /**
     * Función que calcula la aceleración que experimenta un cuerpo debido a la
     * fuerza que sobre el ejerce un conjunto de cuerpos en un espacio
     * discretizado en forma de arbol octal,siguiendo el algoritmo de
     * Barnes-Hut, con un coste computacional del orden de Nlog(N), siendo N el
     * número de cuerpos del árbol.
     *
     * @param body cuerpo sobre el que actúan las fuerzas a calcular.
     * @param theta parámetro de precisión del algoritmo de Barnes-Hut, que
     * determinará el tamaño máximo de las celdas para que este algoritmo sea
     * utilizado.
     * @return Vector v3D que representa la aceleración total que experimenta el
     * cuerpo.
     */
    public v3D BH(Body body, double theta) {
        return this.mc.BH(body, theta, this.size, this.center);

    }

    public void createLines() {

        this.lines = this.mc.createLines(center, size, width / 6.0);

    }

}

/**
 * Clase que va a ser empleada para crear objetos que parezcan visualmente
 * líneas que definan cubos en un espacio 3D. Se trata de cilindros susceptibles de ser rotados
 *sobre sus ejes X y Z.
 * @author Daniel del Canto
 */
class SmartCylinder extends Cylinder {

    Rotate ro;
    Transform t = new Rotate();

    public SmartCylinder(double radius, double height) {
        super(radius, height);

    }

    /**
     * rotateByX rota un cilindro respecto del eje x.
     *
     * @param ang Ángulo de rotación respecto del eje x.
     */
    void rotateByX(int ang) {
        ro = new Rotate(ang, Rotate.X_AXIS);
        t = t.createConcatenation(ro);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);

    }

    /**
     * rotateByZ rota un cilindro respecto del eje z.
     *
     * @param ang Ángulo de rotación respecto del eje z.
     */
    void rotateByZ(int ang) {
        ro = new Rotate(ang, Rotate.Z_AXIS);
        t = t.createConcatenation(ro);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);

    }

}
