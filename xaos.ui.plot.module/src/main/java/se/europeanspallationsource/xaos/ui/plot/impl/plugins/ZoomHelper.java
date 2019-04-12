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


import chart.Plugin;
import se.europeanspallationsource.xaos.ui.plot.impl.util.ChartUndoManager;


/**
 * Helper class supporting chart zoom operations.
 *
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings( "ClassWithoutLogger" )
public class ZoomHelper {

	private final Plugin plugin;

	public ZoomHelper( Plugin plugin ) {
		this.plugin = plugin;
	}

	public void autoScale() {
		ChartUndoManager.get(plugin.getChart()).captureUndoable(plugin);
		plugin.getXValueAxis().setAutoRanging(true);
		plugin.getYValueAxis().setAutoRanging(true);
	}

	public void zoomIn() {

		ChartUndoManager.get(plugin.getChart()).captureUndoable(plugin);

		double plotHeight = plugin.getYValueAxis().getUpperBound() - plugin.getYValueAxis().getLowerBound();
		double plotWidth  = plugin.getXValueAxis().getUpperBound() - plugin.getXValueAxis().getLowerBound();

		plugin.getXValueAxis().setAutoRanging(false);
		plugin.getYValueAxis().setAutoRanging(false);
		plugin.getXValueAxis().setLowerBound(plugin.getXValueAxis().getLowerBound() + 0.1 * plotWidth);
		plugin.getXValueAxis().setUpperBound(plugin.getXValueAxis().getUpperBound() - 0.1 * plotWidth);
		plugin.getYValueAxis().setLowerBound(plugin.getYValueAxis().getLowerBound() + 0.1 * plotHeight);
		plugin.getYValueAxis().setUpperBound(plugin.getYValueAxis().getUpperBound() - 0.1 * plotHeight);

	}

	public void zoomOut() {

		ChartUndoManager.get(plugin.getChart()).captureUndoable(plugin);

		double plotHeight = plugin.getYValueAxis().getUpperBound() - plugin.getYValueAxis().getLowerBound();
		double plotWidth  = plugin.getXValueAxis().getUpperBound() - plugin.getXValueAxis().getLowerBound();

		plugin.getXValueAxis().setAutoRanging(false);
		plugin.getYValueAxis().setAutoRanging(false);
		plugin.getXValueAxis().setLowerBound(plugin.getXValueAxis().getLowerBound() - 0.1 * plotWidth);
		plugin.getXValueAxis().setUpperBound(plugin.getXValueAxis().getUpperBound() + 0.1 * plotWidth);
		plugin.getYValueAxis().setLowerBound(plugin.getYValueAxis().getLowerBound() - 0.1 * plotHeight);
		plugin.getYValueAxis().setUpperBound(plugin.getYValueAxis().getUpperBound() + 0.1 * plotHeight);

	}

}