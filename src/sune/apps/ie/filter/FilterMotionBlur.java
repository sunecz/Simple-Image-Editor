/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.filter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import sune.apps.ie.Editor;
import sune.apps.ie.component.NumberTextField;
import sune.apps.ie.plugin.PluginWindow;
import sune.apps.ie.translation.Translation;

public class FilterMotionBlur extends PluginWindow
{
	private double min;
	private double max;
	private double val;
	private double angle;
	private double minAngle;
	private double maxAngle;
	
	private HBox topBox;
	private Label label;
	private TextField textField; 
	private Slider slider;
	private HBox angleBox;
	private Label lblAngle;
	private TextField txtAngle;
	private Slider sliderAngle;
	
	public FilterMotionBlur(Editor editor)
	{
		super("filters.motionBlur", editor, 250, 180);

		this.min = 0;
		this.max = 20;
		this.val = 0;
		
		this.angle 	  = 0;
		this.minAngle = 0;
		this.maxAngle = 360;
	}
	
	@Override
	public void load()
	{
		super.load();
		
		topBox			= new HBox(5);
		label			= new Label(Translation.getTranslation("plugins." + getName() + ".window.valueTitle"));
		textField 		= new NumberTextField(Double.toString(val));
		slider			= new Slider(min, max, val);
		angleBox		= new HBox(5);
		lblAngle		= new Label(Translation.getTranslation("plugins." + getName() + ".window.lblAngle"));
		txtAngle		= new NumberTextField(Double.toString(val));
		sliderAngle		= new Slider(minAngle, maxAngle, angle);
		
		topBox.setAlignment(Pos.CENTER_LEFT);
		
		label.setAlignment(Pos.CENTER_LEFT);
		label.setFont(new Font(14));
		
		lblAngle.setAlignment(Pos.CENTER_LEFT);
		lblAngle.setFont(new Font(14));
		
		textField.setPrefWidth(45);
		textField.setPadding(new Insets(2));
		
		txtAngle.setPrefWidth(45);
		txtAngle.setPadding(new Insets(2));
		
		textField.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if(!newValue.isEmpty())
				{
					try
					{
						slider.setValue(val = Double.parseDouble(newValue));
					}
					catch(Exception ex) {}
				}	
			}
		});
		
		txtAngle.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
			{
				if(!newValue.isEmpty())
				{
					try
					{
						sliderAngle.setValue(angle = Double.parseDouble(newValue));
					}
					catch(Exception ex) {}
				}	
			}
		});
		
		slider.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if(Math.abs(newValue.intValue() - oldValue.intValue()) < 1)
					return;
				
				val = newValue.intValue();
				textField.setText(Double.toString(val));
				
				apply();
				editor.getCanvas().redraw();
			}
		});
		
		sliderAngle.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if(Math.abs(newValue.intValue() - oldValue.intValue()) < 1)
					return;
				
				angle = newValue.intValue();
				txtAngle.setText(Double.toString(angle));
				
				apply();
				editor.getCanvas().redraw();
			}
		});
		
		Pane p0 = new Pane();
		HBox.setHgrow(p0, Priority.ALWAYS);
		
		topBox.getChildren().add(label);
		topBox.getChildren().add(p0);
		topBox.getChildren().add(textField);
		
		Pane pa = new Pane();
		HBox.setHgrow(pa, Priority.ALWAYS);
		
		angleBox.getChildren().add(lblAngle);
		angleBox.getChildren().add(pa);
		angleBox.getChildren().add(txtAngle);
		
		VBox.setMargin(slider, new Insets(10, 0, 0, 0));
		box.getChildren().add(topBox);
		box.getChildren().add(slider);
		
		VBox.setMargin(sliderAngle, new Insets(10, 0, 0, 0));
		box.getChildren().add(angleBox);
		box.getChildren().add(sliderAngle);
		
		show();
	}
	
	@Override
	public void unload()
	{
		super.unload();
		angle = 0;
		val   = 0;
		
		close();
	}

	@Override
	public synchronized void applyToImage()
	{
		image.filters.motionBlur((int) angle, (int) val);
	}
}