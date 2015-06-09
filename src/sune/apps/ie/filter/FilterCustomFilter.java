/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.filter;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import sune.apps.ie.Editor;
import sune.apps.ie.component.NumberTextField;
import sune.apps.ie.plugin.PluginWindow;
import sune.apps.ie.translation.Translation;

public class FilterCustomFilter extends PluginWindow
{
	private double min;
	private double max;
	private double val;
	
	private HBox topBox;
	private Label label;
	private TextField textField; 
	private Slider slider;
	
	private Label lblKernelSize;
	private GridPane gridPane;
	private ComboBox<KernelSize> cmbSizes;
	private List<TextField> textFields = new ArrayList<>();
	private CheckBox chbAlphaChannel;
	private boolean alphaChannel;
	
	private enum KernelSize
	{
		SIZE_3x3("3x3", 3, 3),
		SIZE_5x5("5x5", 5, 5),
		SIZE_7x7("7x7", 7, 7);
		
		public final String name;
		public final int rows;
		public final int cols;
		
		private KernelSize(String name, int rows, int cols)
		{
			this.name = name;
			this.rows = rows;
			this.cols = cols;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	public FilterCustomFilter(Editor editor)
	{
		super("filters.customFilter", editor, 450, 410);

		this.min = 1;
		this.max = 20;
		this.val = 1;
		
		this.alphaChannel = true;
	}
	
	@Override
	public void load()
	{
		super.load();
		
		topBox			= new HBox(5);
		label			= new Label(Translation.getTranslation("plugins." + getName() + ".window.valueTitle"));
		textField 		= new NumberTextField(Double.toString(val));
		slider			= new Slider(min, max, val);
		gridPane		= new GridPane();
		cmbSizes		= new ComboBox<>();
		chbAlphaChannel	= new CheckBox(Translation.getTranslation("plugins." + getName() + ".window.chbAlphaChannel"));
		lblKernelSize	= new Label(Translation.getTranslation("plugins." + getName() + ".window.lblKernelSize"));
		
		cmbSizes.getItems().addAll(KernelSize.values());
		cmbSizes.valueProperty().addListener(new ChangeListener<KernelSize>()
		{
			@Override
			public void changed(ObservableValue<? extends KernelSize> observable, KernelSize oldValue, KernelSize newValue)
			{
				gridPane.getChildren().clear();
				textFields.clear();
				
				int rows = newValue.rows;
				int cols = newValue.cols;
				for(int r = 0; r < rows; r++)
				{
					for(int c = 0; c < cols; c++)
					{
						TextField textField = new NumberTextField(
							r == Math.ceil(rows / 2) && c == Math.ceil(cols / 2) ? "1" : "0");
						textField.setPrefWidth(50);
						
						textField.textProperty().addListener(new ChangeListener<String>()
						{
							@Override
							public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
							{
								if(preview)
									apply();	
							}
						});
						
						GridPane.setRowIndex(textField, r);
						GridPane.setColumnIndex(textField, c);
						GridPane.setMargin(textField, new Insets(5));
						gridPane.getChildren().add(textField);
						textFields.add(textField);
					}
				}
				
				apply();
			}
		});
		
		cmbSizes.getSelectionModel().select(KernelSize.SIZE_3x3);
		gridPane.setPadding(new Insets(10));
		gridPane.setAlignment(Pos.CENTER);
		
		chbAlphaChannel.selectedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
			{
				alphaChannel = newValue.booleanValue();
				apply();
			}
		});
		
		topBox.setAlignment(Pos.CENTER_LEFT);
		chbAlphaChannel.setSelected(alphaChannel);
		
		label.setAlignment(Pos.CENTER_LEFT);
		label.setFont(new Font(14));
		
		lblKernelSize.setAlignment(Pos.CENTER_LEFT);
		lblKernelSize.setFont(new Font(14));
		
		textField.setPrefWidth(45);
		textField.setPadding(new Insets(2));
		
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
		
		slider.valueProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if(Math.abs(newValue.intValue() - oldValue.intValue()) < 1)
					return;
				
				val = newValue.intValue();
				textField.setText(Double.toString(val));
				
				if(preview)
					apply();
			}
		});
		
		Pane p0 = new Pane();
		HBox.setHgrow(p0, Priority.ALWAYS);
		
		topBox.getChildren().add(label);
		topBox.getChildren().add(p0);
		topBox.getChildren().add(textField);
		
		VBox.setMargin(slider, new Insets(10, 0, 0, 0));
		HBox hbox0 = new HBox(5);
		hbox0.setPadding(new Insets(5, 0, 5, 0));
		hbox0.setAlignment(Pos.CENTER_LEFT);
		hbox0.getChildren().add(lblKernelSize);
		hbox0.getChildren().add(cmbSizes);
		hbox0.getChildren().add(chbAlphaChannel);
		box.getChildren().add(topBox);
		box.getChildren().add(slider);
		box.getChildren().add(hbox0);
		box.getChildren().add(gridPane);
		
		show();
	}
	
	@Override
	public void unload()
	{
		super.unload();
		val = 0;
		
		close();
	}

	@Override
	public synchronized void applyToImage()
	{
		KernelSize selectedKSize = cmbSizes.getValue();
		int rows = selectedKSize.rows;
		int cols = selectedKSize.cols;
		
		float[] kernel = new float[rows*cols];
		for(int i = 0; i < kernel.length; i++)
		{
			TextField textField = textFields.get(i);
			String textValue	= textField.getText();
			
			float val = 0;
			try { val = Float.parseFloat(textValue); }
			catch(Exception ex) {}
			
			kernel[i] 			= val;
		}
		
		for(int i = 0; i < val; i++)
			image.filters.kernelFilter(kernel, alphaChannel);
	}
}