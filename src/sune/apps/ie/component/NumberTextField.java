/**
 * Copyright (c) 2015, Sune.
 * All rights reserved.*/
package sune.apps.ie.component;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class NumberTextField extends TextField
{
	public NumberTextField()
	{
		this("");
	}
	
	public NumberTextField(String text)
	{
		super(text);
		
		this.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				char c = event.getCharacter().charAt(0);
				
				if(!((c >= '0' && c <= '9') || (getSelection().getStart() == 0 && c == '-') ||
				   (getText().length() > 0 && c == '.' && !getText().contains("."))) ||
				   (getText().length() >= getPrefColumnCount() && getSelection().getLength() == 0))
					event.consume();
			}
		});
	}
}