/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.Nbodyfx;

import static com.mycompany.Nbodyfx.App.galaxy;
import static com.mycompany.Nbodyfx.App.sens_key;
import static com.mycompany.Nbodyfx.App.sens_scroll;
import static com.mycompany.Nbodyfx.Integrator.RUNGE_KUTTA_12;
import static com.mycompany.Nbodyfx.Integrator.RUNGE_KUTTA_4;
import static com.mycompany.Nbodyfx.Integrator.RUNGE_KUTTA_NYSTROM_8;
import static com.mycompany.Nbodyfx.Integrator.STORMER_VERLET_10;
import static com.mycompany.Nbodyfx.Integrator.STORMER_VERLET_2;
import static com.mycompany.Nbodyfx.Integrator.STORMER_VERLET_4;
import static com.mycompany.Nbodyfx.Integrator.STORMER_VERLET_6;
import static com.mycompany.Nbodyfx.Integrator.STORMER_VERLET_8;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * FXML Controller class Clase controladora de la interfaz de usuario generada
 * en SceneBuilder.
 *
 * @author Daniel del Canto
 */
public class FXMLController implements Initializable {

    @FXML
    protected Button add_body;

    @FXML
    protected TextField mass;
    @FXML
    protected Button add_galaxy;
    @FXML
    protected Label N_body;
    @FXML
    protected Label masa_g;
    protected TextField mass_pos;

    @FXML
    protected Label std_dev;
    @FXML
    protected TextField gal_nbody;
    @FXML
    protected TextField gal_mass_c;
    @FXML
    protected TextField gal_sd;
    static double radius, mass_1;
    static v3D speed;

    @FXML
    protected Button run;
    @FXML
    protected Button stop;
    @FXML
    protected TextArea info;
    @FXML
    protected Button clear;
    protected Button save;
    @FXML
    protected Slider h;
    @FXML
    protected Slider theta;
    @FXML
    protected ColorPicker cp;
    @FXML
    protected TextField pos_g;
    @FXML
    protected TextField vel_b;
    @FXML
    protected TextField pos_b;
    @FXML
    protected TextField vel_g;
    @FXML
    protected Slider raton;
    @FXML
    protected Slider teclado;
    @FXML
    protected Button informacion;
    @FXML
    protected TextField rad_g;
    @FXML
    protected TextField rad_b;
    @FXML
    protected Button voctree;
    @FXML
    protected CheckBox elastic;
    @FXML
    protected Button reiniciar;
    @FXML
    protected MenuButton integrador;
    protected Separator sep;
    @FXML
    protected CheckBox cm;
    @FXML
    protected Pane sub_pane;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Listeners para los sliders de la sensibilidad de ratón, teclado, paso temporal y parámetro de BH.
        raton.valueProperty().addListener((ObservableValue<? extends Number> argo0, Number arg1, Number arg2) -> {
            sens_scroll = Math.pow(10.0, raton.getValue());
        });

        teclado.valueProperty().addListener((ObservableValue<? extends Number> argo0, Number arg1, Number arg2) -> {
            sens_key = Math.pow(10.0,1.0+ teclado.getValue());
        });
        h.valueProperty().addListener((ObservableValue<? extends Number> argo0, Number arg1, Number arg2) -> {
            Calculations.h = Math.pow(10.0, h.getValue());
        });

        theta.valueProperty().addListener((ObservableValue<? extends Number> argo0, Number arg1, Number arg2) -> {
            Calculations.theta = theta.getValue();
        });

        elastic.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            MassCube.elastic = !MassCube.elastic;
        });

        cm.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            App.cm_camera = !App.cm_camera;
            if (App.cm_camera) {
                v3D cm_scene = new v3D(App.cmx.doubleValue(), App.cmy.doubleValue(), App.cmz.doubleValue()).tcVector(galaxy);

                App.camera.setTranslateX(cm_scene.x);
                App.camera.setTranslateY(cm_scene.y);
                App.camera.setTranslateZ(cm_scene.z - galaxy.bhtree.getSize());
            }

        });

        for (MenuItem menuitem : integrador.getItems()) {
            menuitem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    String string;
                    string = menuitem.getText();
                    switch (string) {
                        case "STORMER VERLET 2o ORDEN":
                            App.integrator = STORMER_VERLET_2;
                            break;
                        case "STORMER VERLET 4o ORDEN":
                            App.integrator = STORMER_VERLET_4;
                            break;
                        case "STORMER VERLET 6o ORDEN":
                            App.integrator = STORMER_VERLET_6;
                            break;
                        case "STORMER VERLET 8o ORDEN":
                            App.integrator = STORMER_VERLET_8;
                            break;
                        case "STORMER VERLET 10o ORDEN":
                            App.integrator = STORMER_VERLET_10;
                            break;

                        case "RUNGE KUTTA NYSTROM 8o ORDEN":
                            App.integrator = RUNGE_KUTTA_NYSTROM_8;
                            break;
                        case "RUNGE KUTTA 12o ORDEN":
                            App.integrator = RUNGE_KUTTA_12;
                            break;
                        case "RUNGE KUTTA 4o ORDEN":
                            App.integrator = RUNGE_KUTTA_4;
                            break;

                    }

                    App.integrator.init();
                }
            });

        }
    }

    @FXML
    private void initialize(MouseEvent event) {
    }

    /*  @FXML
    private void setIntegrator(ActionEvent event) {
        
        
        
    }*/
    @FXML
    private void handle(ActionEvent event) {
    }

}
