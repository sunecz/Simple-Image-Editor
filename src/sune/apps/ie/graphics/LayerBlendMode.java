/**
 * Copyright (c) 2015, Petr Cipra.
 * All rights reserved.*/
package sune.apps.ie.graphics;

import javafx.scene.effect.BlendMode;
import sune.apps.ie.translation.Translation;

public enum LayerBlendMode
{
	NORMAL		(BlendMode.SRC_OVER),
	LINEAR_DODGE(BlendMode.ADD),
	MULTIPLY	(BlendMode.MULTIPLY),
	SCREEN		(BlendMode.SCREEN),
	OVERLAY		(BlendMode.OVERLAY),
	DARKEN		(BlendMode.DARKEN),
	LIGHTEN		(BlendMode.LIGHTEN),
	COLOR_DODGE	(BlendMode.COLOR_DODGE),
	COLOR_BURN	(BlendMode.COLOR_BURN),
	HARD_LIGHT	(BlendMode.HARD_LIGHT),
	SOFT_LIGHT	(BlendMode.SOFT_LIGHT),
	DIFFERENCE	(BlendMode.DIFFERENCE),
	EXCLUSION	(BlendMode.EXCLUSION);
	
	private BlendMode mode;
	
	private LayerBlendMode(BlendMode mode)
	{
		this.mode = mode;
	}
	
	public BlendMode getBlendMode()
	{
		return mode;
	}
	
	@Override
	public String toString()
	{
		return Translation.getTranslation("layerBlendModes." + name());
	}
}