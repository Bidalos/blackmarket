/*
 * Copyright (C) 2015 thirdy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.thirdy.blackmarket.fxcontrols;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

/** Animates a node on and off screen to the left. */
 public class SlidingPane extends GridPane {
    /** @return a control button to hide and show the sidebar */
    public Button getControlButton() { return controlButton; }
    private final Button controlButton;
    
    private final AtomicBoolean expanded = new AtomicBoolean(true);

    /** creates a sidebar containing a vertical alignment of the given nodes */
    public SlidingPane(final double expandedHeight, final double collapse, Node node) {
      
      this.setPrefHeight(expandedHeight);
      this.setMinHeight(0);

      // create a bar to hide and show.
//      setAlignment(Pos.CENTER);
//      getChildren().addAll(nodes);
//      setCenter(node);
      GridPane.setHgrow(node, Priority.ALWAYS);
      GridPane.setVgrow(node, Priority.ALWAYS);
      add(node, 0, 0);

      // create a button to hide and show the sidebar.
      controlButton = new Button();
      controlButton.getStyleClass().add("controlPane-min");

      // apply the animations when the button is pressed.
      controlButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override public void handle(ActionEvent actionEvent) {
          // create an animation to hide sidebar.
          final Animation hideSidebar = new Transition() {
            { setCycleDuration(Duration.millis(100)); }
            protected void interpolate(double frac) {
              final double curHeight = expandedHeight * (1.0 - frac);
              
              double translateValue = -(-expandedHeight + curHeight);
              if (translateValue >= (expandedHeight - collapse)) {
            	  // STOP!
            	  SlidingPane.this.setTranslateY(expandedHeight - collapse);
              } else {
            	  SlidingPane.this.setTranslateY(translateValue);
              }
              
            }
          };
          hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
              expanded.set(false);
//              controlButton.setText("Show");
              controlButton.getStyleClass().remove("controlPane-min");
              controlButton.getStyleClass().add("controlPane-max");
            }
          });
  
          // create an animation to show a sidebar.
          final Animation showSidebar = new Transition() {
            { setCycleDuration(Duration.millis(200)); }
            protected void interpolate(double frac) {
              final double curHeight = expandedHeight * frac;
              double translateValue = -(-expandedHeight + curHeight + collapse);
              if(translateValue > 0) SlidingPane.this.setTranslateY(translateValue);
            }
          };
          showSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
            	expanded.set(true);
//              controlButton.setText("Collapse");
              controlButton.getStyleClass().add("controlPane-min");
              controlButton.getStyleClass().remove("controlPane-max");
            }
          });
  
          if (showSidebar.statusProperty().get() == Animation.Status.STOPPED && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
            if (expanded.get()) {
              hideSidebar.play();
            } else {
              expanded.set(true);
              showSidebar.play();
            }
          }
        }
      });
    }
  }