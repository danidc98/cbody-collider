package com.mycompany.Nbodyfx;

import java.util.ArrayList;
import java.util.Objects;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;

/**
 * Clase Body. Cuerpos con masa, posición, volumen y velocidad en un espacio de
 * 3 dimensiones
 *
 * @author Daniel del Canto
 */
public class Body extends Sphere {

    protected double mass;
    protected double radius;
    protected v3D position, speed;
 
    //Sphere sphere;
    public Body() {
    }

    /**
     * Función que nos genera el tamaño de un cubo que contenga a todos los
     * cuerpos del ArrayList bodies y su centro geométrico.
     *
     * @param bodies ArrayList que contiene los cuerpos para los cuales se va a
     * calcular un cubo.
     * @return Posición del centro del cubo y el tamaño del lado del cubo.
     */
    public static Object[] lim_n_center(ArrayList<Body> bodies) {
        double xmax, xmin, ymax, ymin, zmax, zmin, mincubesize, mass;
        if (bodies.size() > 1) {
            xmax = bodies.get(0).position.x;
            xmin = xmax;
            ymax = bodies.get(0).position.y;
            ymin = ymax;
            zmax = bodies.get(0).position.z;
            zmin = zmax;
            v3D center;
            mass = 0;
            v3D cm = new v3D(0, 0, 0);
            for (Body body : bodies) {
                if (body.position.x > xmax) {
                    xmax = body.position.x;
                }
                if (body.position.x < xmin) {
                    xmin = body.position.x;
                }

                if (body.position.y > ymax) {
                    ymax = body.position.y;
                }
                if (body.position.y < ymin) {
                    ymin = body.position.y;
                }
                if (body.position.z > zmax) {
                    zmax = body.position.z;
                }
                if (body.position.z < zmin) {
                    zmin = body.position.z;
                }
                cm = cm.product(mass / (body.mass + mass)).add(body.position.product(body.mass / (body.mass + mass)));
                mass = mass + body.mass;

            }
            mincubesize = 2.0 * Math.max(xmax - xmin, ymax - ymin);
            mincubesize = Math.max(mincubesize, 2.0 * (zmax - zmin));

            center = new v3D(cm.x, cm.y, cm.z);

            return new Object[]{center, mincubesize};

        } else if (bodies.size() == 1) {
            return new Object[]{bodies.get(0).position, bodies.get(0).radius * 2.0};
        } else {
            return null;
        }

    }

    /**
     * Función que nos genera el tamaño de un cubo que contenga a todos los
     * cuerpos del panel bodies y su centro geométrico.
     *
     * @param bodies Panel que contiene los cuerpos para los cuales se va a
     * calcular un cubo.
     * @return Posición del centro del cubo y el tamaño del lado del cubo.
     */
    public static Object[] lim_n_center(Group bodies) {
        double xmax, xmin, ymax, ymin, zmax, zmin, mincubesize;

        xmax = bodies.getChildren().get(0).getTranslateX();
        xmin = xmax;
        ymax = bodies.getChildren().get(0).getTranslateY();
        ymin = ymax;
        zmax = ymax = bodies.getChildren().get(0).getTranslateZ();
        zmin = zmax;
        v3D center;
        Body body;
        for (Node node : bodies.getChildren()) {
            body = (Body) node;
            if (body.position.x > xmax) {
                xmax = body.position.x;
            }
            if (body.position.x < xmin) {
                xmin = body.position.x;
            }

            if (body.position.y > ymax) {
                ymax = body.position.y;
            }
            if (body.position.y < ymin) {
                ymin = body.position.y;
            }

            if (body.position.z > zmax) {
                zmax = body.position.z;
            }
            if (body.position.z < zmin) {
                zmin = body.position.z;
            }

        }
        mincubesize = Math.max(xmax - xmin, ymax - ymin);
        mincubesize = Math.max(mincubesize, zmax - zmin);
        center = new v3D(xmin + mincubesize / 2, ymin + mincubesize / 2, zmin + mincubesize / 2);

        return new Object[]{center, mincubesize};

    }

    /**
     * Inicializamos un cuerpo con su vector posición (v3D para realizar
     * cálculos) y fijamos sus coordenadas como objeto de clase Sphere para su
     * representación gráfica.
     *
     * @param mass Masa del cuerpo
     * @param radius Radio del cuerpo esférico
     * @param position Posición del cuerpo en el espacio 3D
     * @param speed Velocidad del cuerpo en el espacio 3D
     *
     *
     */
    public Body(double mass, double radius, v3D position, v3D speed) {
        super(radius);
        this.position = position.myClone();
        this.translateXProperty().set(this.position.x);
        this.translateYProperty().set(this.position.y);
        this.translateZProperty().set(this.position.z);

        this.mass = mass;
        this.radius = radius;

        this.speed = speed.myClone();

    }

    public v3D getPosition() {
        return position;
    }

    /**
     * Constructor que inicializa un cuerpo sin especificar la velocidad
     * inicial. Inicializamos un cuerpo con su vector posición (v3D para
     * realizar cálculos) y fijamos sus coordenadas como objeto de clase Sphere
     * para su representación gráfica.
     *
     * @param mass Masa del cuerpo.
     * @param radius Radio del cuerpo esférico.
     * @param position Posición en el espacio 3D.
     */
    public Body(double mass, double radius, v3D position) {
        super(radius);
        this.position = position;
        this.translateXProperty().set(this.position.x);
        this.translateYProperty().set(this.position.y);
        this.translateZProperty().set(this.position.z);
        this.mass = mass;
        this.radius = radius;

    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }
        final Body other = (Body) obj;
        if (Double.doubleToLongBits(this.mass) != Double.doubleToLongBits(other.mass)) {
            return false;
        }
        if (Double.doubleToLongBits(this.radius) != Double.doubleToLongBits(other.radius)) {
            return false;
        }
        if (!Objects.equals(this.position, other.position)) {
            return false;
        }

        return Objects.equals(this.speed, other.speed);
    }

    public void setPosition(v3D position) {
        this.position = position;
        this.translateXProperty().set(this.position.x);
        this.translateYProperty().set(this.position.y);
        this.translateZProperty().set(this.position.z);
    }

    public void setSpeed(v3D speed) {
        this.speed = speed;

    }

    @Override
    public String toString() {
        return "Body{" + "mass=" + mass + ", radius=" + radius + ", position=" + position + ", speed=" + speed + '}';
    }

    /**
     *
     * @param c Cuerpo respecto del cual se calcula el vector relativo a nuestro
     * cuerpo.
     * @return Vector relativo de ambos cuerpos.
     */
    public v3D distance3D(Body c) {
        return position.distance3D(c.position);
    }

    /**
     *
     * @param c Cuerpos respecto del cual se calcula la distancia.
     * @return Distancia en valor absoluto entre los dos cuerpos.
     */
    public double distance(Body c) {
        return position.distance(c.position);
    }

    public Body myClone() {

        Body clon = new Body(this.mass, this.radius, this.position, this.speed);
        clon.setMaterial(this.getMaterial());

        return clon;
    }

    public static ArrayList<Body> myClone(ArrayList<Body> bodies) {
        ArrayList<Body> copy = new ArrayList<Body>();
        for (Body body : bodies) {
            copy.add(body.myClone());

        }

        return copy;
    }

}
