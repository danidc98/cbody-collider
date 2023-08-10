/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.Nbodyfx;

import static com.mycompany.Nbodyfx.BHTree.insertBody;
import static com.mycompany.Nbodyfx.Calculations.energia_momento;
import static com.mycompany.Nbodyfx.Integrator.STORMER_VERLET_4;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;

/**
 * Aplicación simuladora de colisiones de cuerpos celestes.En esta clase se
 * generará una animación de N-cuerpos sometidos a interacción gravitatoria y
 * utilizará interfaz gráfica de usuario generada en SceneBuilder. Asimismo, se
 * creará una serie de controladores de eventos que permitirá rotar el conjunto
 * de los cuerpos de la animación y moverse con una cámara en perspectiva en un
 * espacio en 3 dimensiones. Se fijarán las acciones para cada botón de la
 * interfaz gráfica. Estas acciones darán lugar a las simulaciones que escoja el
 * usuario.
 *
 *
 * @author Daniel del Canto
 */
public class App extends Application {


    private double anchorX, anchorY;    //Ángulos con el eje x e y previos para rotar la galaxia con ratón
    static boolean running = false;
    static protected final DoubleProperty cmx = new SimpleDoubleProperty(0);

    static protected final DoubleProperty cmy = new SimpleDoubleProperty(0);
    static protected final DoubleProperty cmz = new SimpleDoubleProperty(0);
    Rotate xRotate, yRotate; //Rotaciones de la galaxia.

    static final Camera camera = new PerspectiveCamera(true);
    static double stdDev, gal_mass, rad_gal, mass_b, bod_rad, angleX1, angleY1, fps;
    static FXMLController controller;
    static Body body;
    static int N;
    static SmartGroup prov = new SmartGroup();
    static double sens_key = 0.5;
    static boolean galaxy_selected, body_selected, cm_camera = false;
    static double sens_scroll = 0.05;
    static v3D speed, pos = new v3D(0, 0, 0);
    static Galaxy galaxy;
    static boolean voctree = false;
    static Integrator integrator = STORMER_VERLET_4;
    static ArrayList<Body> galaxy_ini = new ArrayList<Body>();
    static AnimationTimer animation = new AnimationTimer() {
        final LongProperty lastUpdateTime = new SimpleLongProperty(0);

        @Override
        public void handle(long currentNanoTime) {
            long timen = System.nanoTime();
            if (lastUpdateTime.get() > 0) {

                integrator.solve(galaxy);

                if (!MassCube.elastic) {
                    galaxy.bhtree = new BHTree(galaxy.bhtree.bodies);
                    galaxy.getChildren().clear();
                    galaxy.getChildren().addAll(galaxy.bhtree.bodies);
                    galaxy.getChildren().add(new AmbientLight());

                }

                App.cmx.set(galaxy.bhtree.mc.cm.x);         //Se fija siempre el centro de rotación de la galaxia en el centro de masas
                App.cmy.set(galaxy.bhtree.mc.cm.y);         //
                App.cmz.set(galaxy.bhtree.mc.cm.z);
                fps = 1.0 / (currentNanoTime - lastUpdateTime.get());

                try {
                    App.controller.info.setText("Fps: " + Math.round(1000000000.0 * 1000.0 * fps) / 1000.0 + "\nAños de simu"
                            + "lación: " + Math.round(galaxy.time * 100000.0) / 100000.0 + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (App.cm_camera) {
                    //Si está el parámetro cm_camera activado (mediante doble click), la cámara seguirá al centro de masas, de lo contrario,
                    //será movida por el usuario.
                    //Para situar la cámara, transformamos las coordenadas al sistema de la escena
                    v3D cm_scene = galaxy.bhtree.mc.cm.tcVector(galaxy);

                    App.camera.setTranslateX(cm_scene.x);
                    App.camera.setTranslateY(cm_scene.y);
                    App.camera.setTranslateZ(cm_scene.z - galaxy.bhtree.getSize());
                }
            }
            lastUpdateTime.set(currentNanoTime);
        }
    };

    @Override
    @SuppressWarnings("empty-statement")
    public void start(Stage primaryStage) throws IOException {
        galaxy_selected = false;
        body_selected = false;
        //Cargamos al archivo FXML con los gráficos generados en SceneBuilder para la GUI.
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("FXML.fxml"));
        ScrollPane interf = fxmlLoader.load();
        controller = (FXMLController) fxmlLoader.getController(); //INTERFAZ DE USUARIO DEL SIMULADOR//INTERFAZ DE USUARIO DEL SIMULADOR//INTERFAZ DE USUARIO DEL SIMULADOR
        controller.info.setEditable(false);
        integrator = STORMER_VERLET_4;
        integrator.init();//Inicializamos el integrador con su selección por defecto STORMER_VERLET_4
        galaxy = new Galaxy(); //Inicializamos la galaxia
        galaxy.getChildren().add(new AmbientLight());

        primaryStage.setResizable(true);
        controller.info.setWrapText(true);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ACCIONES DE LOS BOTONES  // ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ACCIONES DE LOS BOTONES  // ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ACCIONES DE LOS BOTONES  // ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES
        //Añadimos una galaxia mediante el botón asignado en la interfaz y los parámetros definidos por el usuario en los campos de texto.  
        controller.add_galaxy.setOnAction((ActionEvent event) -> {
            animation.stop();
            running = false;
            if (!galaxy_selected) {
                try {
                    Galaxy.color = controller.cp.getValue();
                    N = Integer.parseInt(controller.gal_nbody.getText());
                    String[] string = new String[3];
                    stdDev = getDouble(controller.gal_sd.getText());
                    rad_gal = getDouble(controller.rad_g.getText());
                    gal_mass = getDouble(controller.gal_mass_c.getText()) * Math.pow(10.0, -20.0);
                    speed = new v3D(0.0, 0.0, 0.0);
                    pos = new v3D(0.0, 0.0, 0.0);

                    Transform transform = galaxy.getLocalToSceneTransform();
                    Transform invtransform = galaxy.getLocalToSceneTransform().createInverse();

                    if (!"".equals(controller.vel_g.getText())) {

                        string = controller.vel_g.getText().split(",");
                        speed.x = getDouble(string[0]);
                        speed.y = getDouble(string[1]);
                        speed.z = getDouble(string[2]);

                    }
                    if (!"".equals(controller.pos_g.getText())) {
                        string = controller.pos_g.getText().split(",");
                        pos.x = getDouble(string[0]);
                        pos.y = getDouble(string[1]);
                        pos.z = getDouble(string[2]);
                        //Hacemos aparecer a la cámara exactamente delante de la galaxia a añadir.

                        Point3D p3D = transform.transform(pos.x, pos.y, pos.z);
                        App.camera.setTranslateX(p3D.getX());
                        App.camera.setTranslateY(p3D.getY());
                        App.camera.setTranslateZ(p3D.getZ() - 20.0 * stdDev);

                    } else {
                        //Colocamos la galaxia delante de la cámara
                        //Las coordenadas de la cámara respecto de la escena no son las mismas que las de los
                        //cuerpos respecto de la escena, ya que sufren transformaciones por parte del usuario.

                        Point3D p3D = transform.inverseTransform(App.camera.getTranslateX(), App.camera.getTranslateY(), App.camera.getTranslateZ() + 20.0 * stdDev);
                        pos.x = p3D.getX();
                        pos.y = p3D.getY();
                        pos.z = p3D.getZ();
                    }

                    prov = Galaxy.gaussianGalaxy(N, pos, speed, stdDev, gal_mass, rad_gal);
                    prov.setTranslateX(pos.x);
                    prov.setTranslateY(pos.y);
                    prov.setTranslateZ(pos.z);

                    galaxy.getChildren().add(prov);
                    //Tras añadir este objeto a al galaxia, el usuario podrá moverlo con ratón y teclado.

                    galaxy_selected = true;

                } catch (NumberFormatException exception) {
                    controller.info.setText("Faltan parámetros para generar una galaxia o no \nse ha introducido correctamente el formato.");
                    galaxy_selected = false;
                } catch (ArrayIndexOutOfBoundsException exception) {
                    galaxy_selected = false;
                    controller.info.setText("No se han introducido correctamente los vectores\nvelocidad/posición.");

                } catch (RuntimeException exception) {
                    galaxy_selected = false;
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                ;
            }
        });

        //Añadimos un cuerpo mediante el botón asignado en la interfaz y los parámetros definidos por el usuario en los campos de texto.   
        controller.add_body.setOnAction((ActionEvent event) -> {
            animation.stop();
            running = false;
            try {
                if (!galaxy_selected) {

                    Galaxy.color = controller.cp.getValue();
                    mass_b = getDouble(controller.mass.getText()) * Math.pow(10.0, -20.0);
                    bod_rad = getDouble(controller.rad_b.getText());
                    String[] string;
                    speed = new v3D(0.0, 0.0, 0.0);
                    pos = new v3D(0.0, 0.0, 0.0);
                    Transform transform = galaxy.getLocalToParentTransform();
                    //En caso de no introducir ninguna cadena en velocidad/posición, se tomarán como 0.0,0.0,0.0.

                    if (!"".equals(controller.vel_b.getText())) {
                        string = controller.vel_b.getText().split(",");
                        speed.x = getDouble(string[0]);
                        speed.y = getDouble(string[1]);
                        speed.z = getDouble(string[2]);
                    }

                    if (!"".equals(controller.pos_b.getText())) {
                        string = controller.pos_b.getText().split(",");
                        pos.x = getDouble(string[0]);
                        pos.y = getDouble(string[1]);         //Situamos la cámara delante del objeto añadido. El sistema de coordenadas 
                        pos.z = getDouble(string[2]);         //en el que se mide la posición de la cámara es diferente. Hay que transformar las coordenadas.

                        Point3D p3D = transform.transform(pos.x, pos.y, pos.z);
                        App.camera.setTranslateX(p3D.getX());
                        App.camera.setTranslateY(p3D.getY());
                        App.camera.setTranslateZ(p3D.getZ() - 200.0 * bod_rad);

                        body = new Body(mass_b, bod_rad, pos, speed);

                        body.setMaterial(new PhongMaterial(Galaxy.color));
                        galaxy.getChildren().add(body);
                        galaxy.bhtree = insertBody(galaxy.bhtree, body);   //Si la posición estaba indicada por el usuario, se añadirá directamente el cuerpo.
                        App.cmx.set(galaxy.bhtree.mc.cm.x);
                        App.cmy.set(galaxy.bhtree.mc.cm.y);
                        App.cmz.set(galaxy.bhtree.mc.cm.z);
                        Object[] obj = energia_momento(galaxy);
                        galaxy.energy_o = (double) obj[0];
                        galaxy.L_o = (v3D) obj[1];
                        body_selected = false;
                        controller.info.setText("Cuerpo añadido en (" + Math.round(body.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateY() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + getCamPosition());
                        galaxy_ini.clear();
                        BHTree.minrad = galaxy.bhtree.bodies.get(0).radius;
                        BHTree.maxmass = galaxy.bhtree.bodies.get(0).mass;
                        BHTree.minmass = BHTree.maxmass;

                        for (int i = 0; i < galaxy.bhtree.bodies.size(); i++) {
                            galaxy_ini.add(galaxy.bhtree.bodies.get(i).myClone());
                            if (galaxy_ini.get(i).radius < BHTree.minrad) {
                                BHTree.minrad = galaxy_ini.get(i).radius;
                            }
                            if (galaxy_ini.get(i).mass < BHTree.maxmass) {
                                BHTree.minmass = galaxy_ini.get(i).mass;
                            }
                            if (galaxy_ini.get(i).mass > BHTree.maxmass) {
                                BHTree.maxmass = galaxy_ini.get(i).mass;
                            }

                        }
                        BHTree.width=BHTree.minrad/3.0;

                    } else {

                        //Las coordenadas de la cámara respecto de la escena no son las mismas que las de los
                        //cuerpos respecto de la escena.
                        Point3D p3D = transform.inverseTransform(App.camera.getTranslateX(), App.camera.getTranslateY(), App.camera.getTranslateZ() + 200.0 * bod_rad);
                        pos.x = p3D.getX();
                        pos.y = p3D.getY();
                        pos.z = p3D.getZ();
                        body = new Body(mass_b, bod_rad, pos, speed);   //Añadimos el cuerpo
                        body_selected = true;
                        body.setMaterial(new PhongMaterial(Galaxy.color));
                        galaxy.getChildren().add(body);

                    }

                    //Tras añadir este objeto a al galaxia, el usuario podrá moverlo con ratón y teclado para fijarlo posteriormente,
                }

            } catch (NumberFormatException exception) {
                body_selected = false;
                controller.info.setText("Faltan parámetros para generar un cuerpo o no \nse han introducido correctamente.");
            } catch (ArrayIndexOutOfBoundsException exception) {
                body_selected = false;
                controller.info.setText("No se han introducido correctamente los vectores\nvelocidad/posición.");
            } catch (RuntimeException exception) {
                body_selected = false;

            } catch (NonInvertibleTransformException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
            ;
        });

        //Limpiamos el panel de objetos. Esto conlleva reiniciar el tiempo de simulación y parar la línea de tiempo de animation.
        //La posición de la cámara queda reiniciada.
        controller.clear.setOnAction((ActionEvent event) -> {
            animation.stop();
            voctree = false;
            if (!galaxy_selected & !body_selected) {
                galaxy.bhtree = new BHTree();
                galaxy.getChildren().clear();
                running = false;
                App.cmx.set(0.0); //El centro de rotación  de la galaxia volverá a ser el origen
                App.cmy.set(0.0);
                App.cmz.set(0.0);
                galaxy.time = 0.0;

                App.camera.translateXProperty().set(0.0);
                App.camera.translateYProperty().set(0.0);
                App.camera.translateZProperty().set(-6.0);

                prov.getChildren().clear();

                try {
                    App.controller.info.setText("Fps: " + Math.round(1000000000.0 * 1000.0 * fps) / 1000.0 + "\nAños de simulación: " + Math.round(galaxy.time * 1000.0) / 1000.0 + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (!galaxy_selected & body_selected) {
                galaxy.getChildren().remove(body);
                body_selected = false;

            } else if (galaxy_selected) {
                prov.getChildren().clear();
                galaxy.getChildren().remove(prov);
                galaxy_selected = false;

            }
            ;
        });

        //Comienza la animación.
        controller.run.setOnAction((ActionEvent event) -> {
            if (galaxy.getChildren().size() > 0 & !galaxy_selected & !body_selected) {
                running = true;
                if (voctree) {
                    galaxy.getChildren().clear();
                    galaxy.getChildren().addAll(galaxy.bhtree.bodies);
                    voctree = false;
                }
                animation.start();
            }
            ;
        });

        controller.informacion.setOnAction((ActionEvent event) -> {
            controller.info.setText("                       -N-body simulator-\n"
                    + "\n"
                    + "-Si desea añadir un cuerpo, introduzca los parámetros del cuerpo, incluyendo el radio, y pulse `Cuerpo´.\n"
                    + "\n"
                    + "-Si desea añadir una galaxia, repita el mismo proceso. Una vez rellene estos campos, "
                    + "podrá mover los cuerpos y galaxias con W, S, A, D y el scroll del ratón y podrá rotar las galaxias añadidas con R, F, Q, E.\n"
                    + "\n"
                    + "-Si desea fijar su selección, haga click en la escena.\n\n-En los campos `posición´ y `velocidad´, introduzca "
                    + "vectores en la forma x,x,x.\n\n-Todos números salvo `N cuerpos´ (entero) serán números reales que podrán ser dados en notación científica (XeY) o en forma normal.\n"
                    + "\n" + "-Las unidades de longitud se dan en u.a. y las temporales en años.\n\n-Si no introduce `posición´ o `velocidad´, se tomarán nulos.\n");

        });

        //Paramos la evolución del sistema.
        controller.stop.setOnAction((ActionEvent event) -> {
            if (running) {
                running = false;
                animation.stop();
                galaxy.energy_f = (double) Calculations.energia_momento(galaxy)[0];
                galaxy.L_f = (v3D) Calculations.energia_momento(galaxy)[1];

                controller.info.appendText("\nVariación relativa de energía: " + (galaxy.energy_f - galaxy.energy_o) / galaxy.energy_o + " \nVariación relativa de momento angular: " + galaxy.L_f.add(galaxy.L_o.product(-1.0)).module() / galaxy.L_o.module());
                galaxy.energy_o = galaxy.energy_f;
                galaxy.L_o = galaxy.L_f;
            }

        });
        controller.voctree.setOnAction((ActionEvent event) -> {
            animation.stop();
            running = false;
            if (!voctree & galaxy.bhtree.mc != null) {
                voctree = true;
                galaxy.bhtree = new BHTree(galaxy.bhtree.bodies);
                galaxy.bhtree.createLines();
                galaxy.getChildren().add(galaxy.bhtree.lines);

            } else if (voctree) {
                galaxy.getChildren().clear();
                galaxy.getChildren().addAll(galaxy.bhtree.bodies);
                voctree = false;
            }
            ;

        });
        controller.reiniciar.setOnAction((ActionEvent event) -> {
            if (galaxy.bhtree.mc != null) {
                animation.stop();
                running = false;
                ArrayList<Body> aux = new ArrayList<>();

                galaxy_ini.forEach(body -> {
                    aux.add(body.myClone());
                });

                galaxy.bhtree = new BHTree(aux);

                galaxy.getChildren().clear();
                galaxy.getChildren().addAll(galaxy.bhtree.bodies);

                Object[] e_j = energia_momento(galaxy);   //Calculamos la energía del sistema de nuevo
                galaxy.energy_f = (double) e_j[0];
                galaxy.L_f = (v3D) e_j[1];
                galaxy.time = 0.0;
                galaxy.getChildren().add(new AmbientLight());

                App.cmx.set(galaxy.bhtree.mc.cm.x);
                App.cmy.set(galaxy.bhtree.mc.cm.y);
                App.cmz.set(galaxy.bhtree.mc.cm.z);
            }
            ;
        });

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ACCIONES DE LOS BOTONES  // ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ACCIONES DE LOS BOTONES  // ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // ACCIONES DE LOS BOTONES  // ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES// ACCIONES DE LOS BOTONES
        interf.setBackground(new Background(new BackgroundFill(Color.BLUEVIOLET, CornerRadii.EMPTY, Insets.EMPTY)));
        BorderPane pane = new BorderPane();

        int screenWidth = (int) Screen.getPrimary().getBounds().getWidth();
        int screenHeight = (int) Screen.getPrimary().getBounds().getHeight();
        int sceneWidth = 0;
        int sceneHeight = 0;

        if (screenWidth <= 800 && screenHeight <= 600) {
            sceneWidth = 600;
            sceneHeight = 350;

        } else if (screenWidth <= 1280 && screenHeight <= 768) {
            sceneWidth = 800;
            sceneHeight = 450;

        } else if (screenWidth <= 1920 && screenHeight <= 1080) {
            sceneWidth = 1000;
            sceneHeight = 650;

        }

        primaryStage.setMinWidth(sceneWidth);
        primaryStage.setMinHeight(sceneHeight);
        SubScene anim3D = new SubScene(galaxy, primaryStage.getWidth() - interf.getMinWidth(), primaryStage.getHeight(), true, SceneAntialiasing.DISABLED);
        anim3D.setFill(Color.BLACK);

        SubScene GUI = new SubScene(interf, interf.getMinWidth(), sceneHeight, true, SceneAntialiasing.DISABLED);
        Scene scene = new Scene(pane, sceneWidth, sceneHeight);

        primaryStage.getIcons().add(new Image("/images/espacio.jpg"));

        anim3D.widthProperty().bind(primaryStage.widthProperty().subtract(GUI.widthProperty()).subtract(new SimpleDoubleProperty(15.0)));
        anim3D.heightProperty().bind(primaryStage.heightProperty());
        GUI.heightProperty().bind(primaryStage.heightProperty());

        interf.vbarPolicyProperty().set(ScrollBarPolicy.ALWAYS);

        //Colocamos a la izquierda del panel la escena 3D y a la derecha el interfaz de usuario.
        pane.setRight(GUI);
        pane.setCenter(anim3D);
        camera.setFarClip(1000);
        camera.translateZProperty().set(-6);
        anim3D.setCamera(camera);

        galaxy.translateXProperty().set(0);
        galaxy.translateYProperty().set(0);
        galaxy.translateZProperty().set(0);

        primaryStage.setTitle("N-body simulator");
        primaryStage.setScene(scene); //Asignamos a nuestro espacio, definido por el objeto de clase Stage, la escena scene, en la que
        primaryStage.show();          //se muestran todos los eventos observables de la aplicación.
        initMouse_KeyBoard_Control(galaxy, primaryStage, anim3D);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    //EVENTOS DE RATÓN Y TECLADO        //EVENTOS DE RATÓN Y TECLADO  //EVENTOS DE RATÓN Y TECLADO  //EVENTOS DE RATÓN Y TECLADO  //EVENTOS DE RATÓN Y TECLADO 
    void initMouse_KeyBoard_Control(Galaxy galaxy, Stage stage, SubScene scene) {

        //PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN
        //PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN//PRESIONAR BOTÓN DE RATÓN
        scene.setOnMousePressed(event -> {
            if (!galaxy_selected) {
                anchorX = event.getSceneX(); //Capturamos la posición inicial del ratón antes de rotar
                anchorY = event.getSceneY();

            }

        });

        scene.setOnMouseDragged(event -> {
            if (!galaxy_selected & !body_selected) {

                Point3D point = new Point3D(App.camera.getTranslateX(), App.camera.getTranslateY(), App.camera.getTranslateZ());
                Transform transform = null;
                try {
                    transform = galaxy.getLocalToSceneTransform().createInverse();
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                point = transform.transform(point);
                double distance = point.distance(cmx.doubleValue(), cmy.doubleValue(), cmz.doubleValue());

                //Queremos que los ejes de rotación sean fijos en la escena. Esto significa que, desde el sistema de referencia de la 
                //galaxia, estarán rotando esos ejes.
                //Para ello, transformamos los vectores unitarios de los ejes de la escena a los de la galaxia.
                Point3D p3Dx = new Point3D(1.0, 0.0, 0.0);
                p3Dx = transform.transform(p3Dx).subtract(transform.getTx(), transform.getTy(), transform.getTz());
                Point3D p3Dy = new Point3D(0.0, 1.0, 0.0);
                p3Dy = transform.transform(p3Dy).subtract(transform.getTx(), transform.getTy(), transform.getTz());

                double anglex = (event.getSceneY() - anchorY) / distance;//Cuando arrastramos, se girará la galaxia partiendo de anchorX,anchorY.
                double angley = (anchorX - event.getSceneX()) / distance;
                Rotate rox = new Rotate(anglex, cmx.doubleValue(), cmy.doubleValue(), cmz.doubleValue(), p3Dx);
                Rotate roy = new Rotate(angley, cmx.doubleValue(), cmy.doubleValue(), cmz.doubleValue(), p3Dy);

                galaxy.t = galaxy.t.createConcatenation(rox).createConcatenation(roy);
                galaxy.getTransforms().clear();
                galaxy.getTransforms().add(galaxy.t);

                try {
                    App.controller.info.setText("Fps: " + Math.round(1000000000.0 * 1000.0 * fps) / 1000.0 + "\nAños de simu"
                            + "lación: " + Math.round(galaxy.time * 100000.0) / 100000.0 + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        );

        // ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN
        // ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN// ARRASTRAR RATÓN
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN
        // CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN
        //El click será utilizado para añadir cuerpos/galaxias o para fijar las coordenadas previas a rotar la galaxia con el ratón.
        scene.setOnMouseClicked((MouseEvent event) -> {
            if (galaxy_selected & !body_selected) {

                Transform t = prov.getLocalToParentTransform();     //Obtenemos la transformación resultante efectuada 
                //en el grupo provisional prov tras eventos de teclado y ratón.
                galaxy.getChildren().remove(prov);

                prov.getChildren().stream().map(node -> (Body) node).map(body1 -> body1.myClone()).map(body2 -> {
                    body2.setPosition(body2.position.getRot_trans(t)); //Utilizamos el objeto transformación para extraer sus elementos de matriz con el fin de rotar y trasladar vectores.
                    return body2;
                }).map(body3 -> {
                    body3.speed = body3.speed.getRot(t);
                    return body3;
                }).forEachOrdered(galaxy.bhtree.bodies::add);

                galaxy.bhtree = new BHTree(galaxy.bhtree.bodies); //No es necesario calcular el árbol para obtener el centro de masas que utilizamos a
                App.cmx.set(galaxy.bhtree.mc.cm.x);               //continuación, pero nos aprovechamos de que esa función lo calcula y su coste temporal
                App.cmy.set(galaxy.bhtree.mc.cm.y);               //no nos importa porque esta función se da cuando el tiempo está parado.
                App.cmz.set(galaxy.bhtree.mc.cm.z);               //Además, en caso de colisiones inelásticas, nos las calculará.

                try {
                    controller.info.setText("Galaxia añadida en (" + Math.round(prov.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(prov.getTranslateY() * 100000.0) / 100000.0 + ", " + Math.round(prov.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

                galaxy.getChildren().clear();                     //Borramos todos los objetos del espacio 3D, incluido el "prov".
                galaxy.getChildren().addAll(galaxy.bhtree.bodies);
                galaxy.getChildren().add(new AmbientLight());

                galaxy_selected = false;
                galaxy.energy_o = (double) Calculations.energia_momento(galaxy)[0];
                galaxy.L_o = (v3D) Calculations.energia_momento(galaxy)[1];

                galaxy_ini.clear();

                BHTree.minrad = galaxy.bhtree.bodies.get(0).radius;
                        BHTree.maxmass = galaxy.bhtree.bodies.get(0).mass;
                        BHTree.minmass = BHTree.maxmass;

                        for (int i = 0; i < galaxy.bhtree.bodies.size(); i++) {
                            galaxy_ini.add(galaxy.bhtree.bodies.get(i).myClone());
                            if (galaxy_ini.get(i).radius < BHTree.minrad) {
                                BHTree.minrad = galaxy_ini.get(i).radius;
                            }
                            if (galaxy_ini.get(i).mass < BHTree.maxmass) {
                                BHTree.minmass = galaxy_ini.get(i).mass;
                            }
                            if (galaxy_ini.get(i).mass > BHTree.maxmass) {
                                BHTree.maxmass = galaxy_ini.get(i).mass;
                            }

                        }
                        BHTree.width=BHTree.minrad/3.0;


            } else if (!galaxy_selected & body_selected) {

                galaxy.bhtree = insertBody(galaxy.bhtree, body);

                body_selected = false;

                galaxy_ini.clear();
                galaxy.L_o = (v3D) Calculations.energia_momento(galaxy)[1];
                galaxy.energy_o = (double) Calculations.energia_momento(galaxy)[0];
                BHTree.minrad = galaxy.bhtree.bodies.get(0).radius;
                        BHTree.maxmass = galaxy.bhtree.bodies.get(0).mass;
                        BHTree.minmass = BHTree.maxmass;

                        for (int i = 0; i < galaxy.bhtree.bodies.size(); i++) {
                            galaxy_ini.add(galaxy.bhtree.bodies.get(i).myClone());
                            if (galaxy_ini.get(i).radius < BHTree.minrad) {
                                BHTree.minrad = galaxy_ini.get(i).radius;
                            }
                            if (galaxy_ini.get(i).mass < BHTree.maxmass) {
                                BHTree.minmass = galaxy_ini.get(i).mass;
                            }
                            if (galaxy_ini.get(i).mass > BHTree.maxmass) {
                                BHTree.maxmass = galaxy_ini.get(i).mass;
                            }

                        }
                        BHTree.width=BHTree.minrad/3.0;

                App.cmx.set(galaxy.bhtree.mc.cm.x);
                App.cmy.set(galaxy.bhtree.mc.cm.y);
                App.cmz.set(galaxy.bhtree.mc.cm.z);

                try {
                    controller.info.setText("Cuerpo añadido en (" + Math.round(body.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateY() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        // CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN
        // CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN/ CLICK DE RATÓN
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN
        //SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN
        //Utilizamos el Scroll de ratón tanto para mover en el eje z las galaxias y cuerpos que añadimos como para mover la cámara, dependiendo de
        //qué botones de selección haya pulsado el usuario.
        scene.addEventHandler(ScrollEvent.SCROLL, (var event) -> {
            Transform invtransform = null;
            try {
                invtransform = galaxy.getLocalToParentTransform().createInverse();
            } catch (NonInvertibleTransformException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!galaxy_selected & !body_selected) {
                //Obtenemos cuánto se ha desplazado el scroll
                double delta = event.getDeltaY();
                //Añadimos ese desplazamiento a la cámara en el eje z.
                camera.translateZProperty().set(camera.getTranslateZ() + delta * sens_scroll);

                try {
                    controller.info.setText("Fps: " + Math.round(1000000000.0 * 1000.0 * fps) / 1000.0 + "\nAños de simulación: " + Math.round(galaxy.time * 1000.0) / 1000.0 + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (galaxy_selected & !body_selected) {

                double delta = event.getDeltaY();
                prov.translateZProperty().set(prov.getTranslateZ() + delta * sens_scroll * invtransform.getMzz());
                prov.translateYProperty().set(prov.getTranslateY() + delta * sens_scroll * invtransform.getMyz());
                prov.translateXProperty().set(prov.getTranslateX() + delta * sens_scroll * invtransform.getMxz());
                try {
                    controller.info.setText("Galaxia situada en (" + Math.round(prov.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(prov.getTranslateY() * 100000.0) / 100000.0 + ", " + Math.round(prov.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (!galaxy_selected & body_selected) {

                try {
                    //Hallamos cuánto ha avanzado el scroll del ratón
                    double delta = event.getDeltaY();
                    //Añadimos el avance a la posición del cuerpo en el eje z.

                    body.setPosition(new v3D(body.getTranslateX() + delta * sens_scroll * invtransform.getMxz(), body.getTranslateY() + delta * sens_scroll * invtransform.getMyz(), body.getTranslateZ() + delta * sens_scroll * invtransform.getMzz()));
                    controller.info.setText("Cuerpo situado en (" + Math.round(body.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateY() * 1000.0) / 1000.0 + ", " + Math.round(body.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + App.getCamPosition());
                } catch (NonInvertibleTransformException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        //SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN
        //SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN//SCROLL DE RATÓN
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO
        //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO //EVENTOS DE TECLADO
        //Añadiendo este controlador de eventos, conseguimos que, una vez se haya seleccionado el botón de añadir una galaxia, el usuario pueda moverla libremente.
        //Lo mismo sucederá con los cuerpos.
        //Por otro lado, cuando no haya selección de galaxia o cuerpo, el usuario podrá mover la cámara libremente por el espacio 3D.
        stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            try {
                Transform invtransform = null;

                invtransform = galaxy.getLocalToParentTransform().createInverse();
                v3D change = new v3D();
                int anglex = 0;
                int angley = 0;   //Recogemos los eventos de teclado y los aplicamos según cuál haya sido la selección del usuario. Si no hay selección, el movimiento será de camara.

                switch (event.getCode()) {

                    case W:
                        change.y = -sens_key;
                        break;

                    case S:
                        change.y = +sens_key;
                        break;

                    case D:

                        change.x = +sens_key;
                        break;

                    case A:

                        change.x = -sens_key;

                        break;

                    case Q:
                        anglex = -5;
                        break;

                    case E:
                        anglex = 5;
                        break;

                    case R:
                        angley = 5;
                        break;

                    case F:
                        angley = - 5;
                        break;
                }
                if (galaxy_selected) {  //Queremos que las traslaciones se den siempre en la misma dirección a ojos del usuario, para un manejo más comodo.
                    //Transformamos las variaciones de coordenadas entre el sistema de la galaxia y el de la escena cuando se trate de movimiento de cuerpos

                    prov.translateZProperty().set(prov.getTranslateZ() + change.x * invtransform.getMzx() + change.y * invtransform.getMzy());
                    prov.translateYProperty().set(prov.getTranslateY() + change.x * invtransform.getMyx() + change.y * invtransform.getMyy());
                    prov.translateXProperty().set(prov.getTranslateX() + change.x * invtransform.getMxx() + change.y * invtransform.getMxy());
                    prov.rotateByX(anglex);
                    prov.rotateByY(angley);
                    controller.info.setText("Galaxia situada en (" + Math.round(prov.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(prov.getTranslateY() * 100000.0) / 100000.0 + ", " + Math.round(prov.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + App.getCamPosition());
                } else if (body_selected) {
                    body.setPosition(new v3D(body.position.x + change.x * invtransform.getMxx() + change.y * invtransform.getMxy(), body.position.y + change.x * invtransform.getMyx() + change.y * invtransform.getMyy(), body.getTranslateZ() + change.x * invtransform.getMzx() + change.y * invtransform.getMzy()));
                    controller.info.setText("Cuerpo situado en (" + Math.round(body.getTranslateX() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateY() * 100000.0) / 100000.0 + ", " + Math.round(body.getTranslateZ() * 100000.0) / 100000.0 + ") u.a." + App.getCamPosition());

                } else {

                    camera.translateYProperty().set(camera.getTranslateY() + change.y);
                    camera.translateXProperty().set(camera.getTranslateX() + change.x);
                    App.controller.info.setText("Fps: " + Math.round(1000000000.0 * 1000.0 * fps) / 1000.0 + "\nAños de simu"
                            + "lación: " + Math.round(galaxy.time * 100000.0) / 100000.0 + App.getCamPosition());

                }
            } catch (NonInvertibleTransformException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

    }
    //EVENTOS DE RATóN Y TECLADO  //EVENTOS DE RATóN Y TECLADO  //EVENTOS DE RATóN Y TECLADO  //EVENTOS DE RATóN Y TECLADO  //EVENTOS DE RATóN Y TECLADO  //EVENTOS DE RATóN Y TECLADO 

    /**
     * Función que devuelve un número de doble precisión partiendo de una cadena
     * introducida por el usuario en un campo de texto en dos posibles formatos:
     * notación científica XeY o un número real normal.
     *
     * @param st Cadena de caracteres a transformar
     * @return Número de doble precisión.
     */
    static double getDouble(String st) {
        double number;

        if (st.split("e").length < 2) {
            number = Double.parseDouble(st);
        } else {
            number = Double.parseDouble(st.split("e")[0]) * Math.pow(10.0, Double.parseDouble(st.split("e")[1]));
        }

        return number;
    }

    /**
     * Función que devuelve una cadena con la posición de la cámara en el
     * sistema de coordenadas de la galaxia.
     *
     * @return Cadena de caracteres con la posición de la cámara en las
     * coordenadas de la galaxia.
     * @throws NonInvertibleTransformException
     */
    static String getCamPosition() throws NonInvertibleTransformException {
        Transform invtransform = galaxy.getLocalToParentTransform().createInverse();
        v3D v = new v3D();
        Point3D p3d = invtransform.transform(camera.getTranslateX(), camera.getTranslateY(), camera.getTranslateZ());
        String string = "\nPos. de cám.: ("
                + Math.round(p3d.getX() * 100000.0) / 100000.0 + ", " + Math.round(p3d.getY() * 100000.0) / 100000.0
                + ", " + Math.round(p3d.getZ() * 100000.0) / 100000.0 + ") u.a";
        if (string.length() > 46) {
            string = "\nPos. de cám.:\n("
                    + Math.round(p3d.getX() * 100000.0) / 100000.0 + ", " + Math.round(p3d.getY() * 100000.0) / 100000.0
                    + ", " + Math.round(p3d.getZ() * 100000.0) / 100000.0 + ") u.a";
        }
        return string;
    }

}

//Clase SmartGroup para rotar los elementos de un grupo.
class SmartGroup extends Group {

    Rotate ro;
    Transform t = new Rotate();

    /**
     * Función que rota un grupo alrededor del eje X con un angulo ang.
     *
     * @param ang Ángulo de rotación sobre el eje x.
     */
    void rotateByX(int ang) {
        ro = new Rotate(ang, Rotate.X_AXIS);
        t = t.createConcatenation(ro);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);

    }

    /**
     * Función que rota un grupo alrededor del eje Y con un angulo ang.
     *
     * @param ang Ángulo de rotación sobre el eje y.
     */
    void rotateByY(int ang) {
        ro = new Rotate(ang, Rotate.Y_AXIS);
        t = t.createConcatenation(ro);
        this.getTransforms().clear();
        this.getTransforms().addAll(t);

    }

}
