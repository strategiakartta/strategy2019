/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/
package fi.semantum.strategia;

import java.math.BigDecimal;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.widget.ContinuousTrafficValuation;
import fi.semantum.strategia.widget.EnumeratedTrafficValuation;
import fi.semantum.strategia.widget.NumberTrafficValuation;
import fi.semantum.strategia.widget.TrafficValuation;
import fi.semantum.strategy.db.AbstractCommentCallback;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.CommentCallback;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.Datatype;
import fi.semantum.strategy.db.EnumerationDatatype;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.Meter;

public class UtilsDataTypes {
	
	private static TrafficValuation translateTrafficValuationEnum(Database database, Meter meter, Meter.TrafficValuationEnum trafficValuationEnum) {
		switch(trafficValuationEnum) {
			case Continuous: return new ContinuousTrafficValuation();
			case Number: return new NumberTrafficValuation(BigDecimal.valueOf(meter.limits[0]), BigDecimal.valueOf(meter.limits[1]));
			case Enumerated: {
				Indicator indicator = meter.getPossibleIndicator(database);
				if(indicator != null) {
					Datatype datatype = indicator.getDatatype(database);
					if(datatype instanceof EnumerationDatatype) {
						EnumerationDatatype edt = (EnumerationDatatype)datatype; 
						return new EnumeratedTrafficValuation(edt.getTreeMapValues());
					}
				}
				// This is an error situation, the valuation shall return RED
				return new EnumeratedTrafficValuation(null);
				
			}
			default: return null;
		}
	}
	
	/**
	 * 
	 * @param meter
	 * @return
	 */
	public static TrafficValuation getTrafficValuation(Database database, Meter meter) {
		return translateTrafficValuationEnum(database, meter, meter.getTrafficValuationEnum());
	}
	
	/**
	 * 
	 * @param datatype
	 * @return
	 */
	/*
	public static TrafficValuation getDefaultTrafficValuation(Datatype datatype) {
		if(datatype instanceof NumberDatatype) {
			return new NumberTrafficValuation();
		} else if(datatype instanceof StringDatatype) {
			return new StringTrafficValuation();
		} else if(datatype instanceof EnumerationDatatype) {
			EnumerationDatatype edt = (EnumerationDatatype)datatype;
			return new EnumeratedTrafficValuation(edt.getTreeMapValues());
		}
		
		return null;
	}
	*/
	
	/**
	 * 
	 * @param valuation
	 * @param layout
	 * @param main
	 * @param meter
	 * @return
	 */
	public static Runnable createTrafficValuationEditor(TrafficValuation valuation, VerticalLayout layout, Main main, Meter meter) {
		return null;
	}
	
	public static String getTrafficValuationDescription(Database database, Meter meter) {
		TrafficValuation trafficValuation = getTrafficValuation(database, meter);
		return trafficValuation.toString();
	}
	
	/**
	 * 
	 * @param main
	 * @param datatype
	 * @param base
	 * @param indicator
	 * @param forecast
	 * @param callback
	 * @return
	 */
	private static AbstractField<?> createEnumerationDatatypeEditor(final Main main, EnumerationDatatype datatype,
			final Base base, final Indicator indicator,
			final boolean forecast, final CommentCallback callback){
		final Object value = forecast ? indicator.getForecast() : indicator.getValue();
		
		final ComboBox combo = new ComboBox();
		for(String s : datatype.getValues()) {
			combo.addItem(s);
		}
		
		combo.setStyleName(ValoTheme.COMBOBOX_TINY);
		combo.select(value);
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");

		if(main.canWrite(base)) {
			combo.addValueChangeListener(new ValueChangeListener() {
				
				private static final long serialVersionUID = 3547126051252580446L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
					UtilsIndicators.updateWithComment(main, indicator, base, combo.getValue(), forecast, new AbstractCommentCallback() {
						
						@Override
						public void canceled() {
							combo.select(value);
							if(callback != null)
								callback.canceled();
						}
						
						@Override
						public void runWithComment(String shortComment, String comment) {
							if(callback != null)
								callback.runWithComment(shortComment, comment);
						}
						
					});
				}
				
			});
		} else {
			combo.setReadOnly(true);
		}
		
		return combo;

	}
	
	/**
	 * 
	 * @param main
	 * @param datatype
	 * @param base
	 * @param indicator
	 * @param forecast
	 * @param callback
	 * @return
	 */
	private static AbstractField<?> createDefaultDatatypeEditor(final Main main, Datatype datatype,
			final Base base, final Indicator indicator,
			final boolean forecast, final CommentCallback callback){
		Object value = forecast ? indicator.getForecast() : indicator.getValue();
		final String formatted = indicator.getDatatype(main.getDatabase()).format(value);

		final TextField tf = new TextField();
		tf.setValue(formatted);
		
		if(main.canWrite(base)) {
		
			tf.addValidator(new Validator() {
	
				private static final long serialVersionUID = 9043601075831736114L;
	
				@Override
				public void validate(Object value) throws InvalidValueException {
					
					try {
						BigDecimal.valueOf(Double.parseDouble((String)value));
					} catch (NumberFormatException e) {
						throw new InvalidValueException("Arvon tulee olla numero");
					}
					
				}
				
			});
			tf.addValueChangeListener(new ValueChangeListener() {
				
				private static final long serialVersionUID = 3547126051252580446L;
	
				@Override
				public void valueChange(ValueChangeEvent event) {
					
					try {
						final BigDecimal number = BigDecimal.valueOf(Double.parseDouble(tf.getValue()));
						UtilsIndicators.updateWithComment(main, indicator, base, number, forecast, new AbstractCommentCallback() {
							
							public void canceled() {
								tf.setValue(formatted);
								if(callback != null) callback.canceled();
							}
							
							public void runWithComment(String shortComment, String comment) {
								if(callback != null) callback.runWithComment(shortComment, comment);
							}
							
						});
					} catch (NumberFormatException e) {
						tf.setComponentError(new UserError("Arvon tulee olla numero"));
					}
					
				}
				
			});
			
		} else {
			
			tf.setReadOnly(true);
			
		}
		
		return tf;
	}
	
	/**
	 * 
	 * @param main
	 * @param datatype
	 * @param base
	 * @param indicator
	 * @param forecast
	 * @param callback
	 * @return
	 */
	public static AbstractField<?> createDatatypeEditor(final Main main, Datatype datatype,
			final Base base, final Indicator indicator,
			final boolean forecast, final CommentCallback callback) {
		
		if(datatype instanceof EnumerationDatatype) {
			return createEnumerationDatatypeEditor(main, (EnumerationDatatype)datatype,
					base, indicator,
					forecast, callback);
		} else {
			return createDefaultDatatypeEditor(main, datatype,
					base, indicator,
					forecast, callback);
		}
		
	}
	
}
