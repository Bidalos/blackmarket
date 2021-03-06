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

import java.util.Optional;

import io.jexiletools.es.model.json.Range;
import javafx.scene.layout.HBox;
import net.thirdy.blackmarket.domain.RangeOptional;

/**
 * @author thirdy
 *
 */
public class RangeDoubleTextField extends HBox implements Clearable {
	DoubleTextField min = new DoubleTextField("Min");
	DoubleTextField max = new DoubleTextField("Max");
	public RangeDoubleTextField() {
		getChildren().addAll(min, max);
	}
	public Optional<RangeOptional> val() {
		if(min.getOptionalValue().isPresent() || max.getOptionalValue().isPresent()) {
			return Optional.of(new RangeOptional(
					min.getOptionalValue(), 
					max.getOptionalValue()));
		} else {
			return Optional.empty();
		}
	}
	public DoubleTextField getMin() {
		return min;
	}
	public DoubleTextField getMax() {
		return max;
	}
	@Override
	public void clear() {
		min.setText("");
		max.setText("");
	}
	
}
