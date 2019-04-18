/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2018-2019 by European Spallation Source ERIC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.xaos.ui.plot.impl.plugins;


import java.text.Format;
import javafx.geometry.Point2D;
import javafx.scene.chart.XYChart.Data;


/**
 * A {@link Plugin} displaying a {@link Label} next to the mouse cursor, showing
 * the chart abscissa value at the mouse cursor.
 * <p>
 * The display's relative position to the mouse cursor can be adjusted using the
 * {@link #positionProperty() position} property. By default its set to
 * {@link Position#BOTTOM}.</p>
 * <p>
 * The formatter used can be adjusted by the
 * {@link #formatterProperty() formatter} property.</p>
 *
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings( "ClassWithoutLogger" )
public final class AbscissaCursorDisplay extends FormattedCursorDisplay {

	public AbscissaCursorDisplay() {
		super(Position.BOTTOM);
	}

	public AbscissaCursorDisplay( Position position ) {
		super(position);
	}

	public AbscissaCursorDisplay( Position position, Format formatter ) {
		super(position, formatter);
	}

	@Override
	protected Object valueAtPosition( Point2D mouseLocation ) {

		Data<?, ?> dataPoint = toDataPoint(mouseLocation);

		return ( dataPoint != null ) ? dataPoint.getXValue() : null;

	}

}