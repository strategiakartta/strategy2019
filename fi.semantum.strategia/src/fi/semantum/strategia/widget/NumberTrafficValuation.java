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
package fi.semantum.strategia.widget;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fi.semantum.strategia.Main;
import fi.semantum.strategy.db.Indicator;
import fi.semantum.strategy.db.Meter;

public class NumberTrafficValuation implements TrafficValuation, Serializable {

	private static final long serialVersionUID = 7573677506428373762L;

	/*
	 * State is coded in limits the following way: 
	 * -INCREASE3 => greenLimit > redLimit || (redLimit == null && makeThree)
	 * -DECREASE3 => redLimit > greenLimit || (greenLimit == null && makeThree)
	 * -INCREASE2 => redLimit = null && !makeThree
	 * -DECREASE2 => greenLimit = null && !makeThree
	 */
	enum State {
		INCREASE3("Kolme väriä - suurempi on parempi"),
		DECREASE3("Kolme väriä - pienempi on parempi"),
		INCREASE2("Kaksi väriä - suurempi on parempi"),
		DECREASE2("Kaksi väriä - pienempi on parempi");
		private String description;
		private State(String description) {
			this.description = description;
		}
		@Override
		public String toString() {
			return description;
		}
	}
	
	private BigDecimal redLimit;
	private BigDecimal greenLimit;
	
	private transient boolean inApply = false;
	private transient boolean makeThree = false;
	
	public NumberTrafficValuation() {
		redLimit = BigDecimal.valueOf(0);
		greenLimit = BigDecimal.valueOf(1);
	}
	
	// This is for migration
	public NumberTrafficValuation(BigDecimal low, BigDecimal high) {
		this.redLimit = low;
		this.greenLimit = high;
		if(greenLimit != null && redLimit != null)
			if(greenLimit.equals(redLimit))
				redLimit = null;
	}

	private void updateMakeThree() {
		if(makeThree && redLimit != null && greenLimit != null) {
			makeThree = false;
		}
	}
	
	private State getRawState() {
		if(redLimit == null) return State.INCREASE2;
		else if(greenLimit == null) return State.DECREASE2;
		else if(greenLimit.compareTo(redLimit) > 0) return State.INCREASE3;
		else return State.DECREASE3;
	}
	
	private State getState() {
		State raw = getRawState();
		if(makeThree) {
			if(raw == State.INCREASE2) return State.INCREASE3;
			if(raw == State.DECREASE2) return State.DECREASE3;
		}
		return raw;
	}

	private boolean isIncrease() {
		State state = getState();
		return state == State.INCREASE2 || state == State.INCREASE3;
	}
	
	private boolean isTwo() {
		State state = getState();
		return state == State.INCREASE2 || state == State.DECREASE2;
	}
	
	private void swap() {
		BigDecimal temp = redLimit;
		redLimit = greenLimit;
		greenLimit = temp;
	}
	
	BigDecimal getLowLimit() {

		State state = getState();
		
		if(State.INCREASE3.equals(state)) {
			return makeThree ? greenLimit : redLimit;
		} else if(State.DECREASE3.equals(state)) {
			return makeThree ? redLimit : greenLimit;
		} else if(State.INCREASE2.equals(state)) {
			return greenLimit;
		} else {
			return redLimit;
		}
		
	}

	BigDecimal getHighLimit() {

		State state = getState();
		if(State.INCREASE3.equals(state)) {
			return greenLimit;
		} else if(State.DECREASE3.equals(state)) {
			return redLimit;
		} else if(State.INCREASE2.equals(state)) {
			return greenLimit;
		} else {
			return redLimit;
		}
		
	}
	
	@Override
	public String getTrafficValue(Object value) {
		
		if(!(value instanceof BigDecimal)) return TrafficValuation.RED;
		BigDecimal number = (BigDecimal)value;

		State state = getState();
		
		if(State.DECREASE2.equals(state))
			return number.compareTo(redLimit) < 0 ? TrafficValuation.GREEN : TrafficValuation.RED;
		else if(State.INCREASE2.equals(state))
			return number.compareTo(greenLimit) > 0 ? TrafficValuation.GREEN : TrafficValuation.RED;
			
		boolean less = number.compareTo(getLowLimit()) < 0;
		if(less) {
			if(State.INCREASE3.equals(state)) return TrafficValuation.RED;
			else return TrafficValuation.GREEN;
		}
		boolean more = number.compareTo(getHighLimit()) > 0;
		if(more) {
			if(State.INCREASE3.equals(state)) return TrafficValuation.GREEN;
			else return TrafficValuation.RED;
		}
		
		return TrafficValuation.YELLOW;
		
	}
	
	static final DecimalFormat df = new DecimalFormat();
	
	static {
		df.setGroupingUsed(false);
	}

	@Override
	public Runnable getEditor(VerticalLayout layout, final Main main, final Meter meter) {
		
		Indicator indicator = meter.getPossibleIndicator(main.getDatabase());
		String unit = indicator.getUnit();
		
		final ComboBox combo = new ComboBox("Valitse mittarin määritystapa");
		combo.addItem(State.INCREASE3);
		combo.addItem(State.DECREASE3);
		combo.addItem(State.INCREASE2);
		combo.addItem(State.DECREASE2);
		combo.setInvalidAllowed(false);
		combo.setNullSelectionAllowed(false);
		combo.setWidth("100%");
		combo.addStyleName(ValoTheme.COMBOBOX_TINY);
		layout.addComponent(combo);
		layout.setComponentAlignment(combo, Alignment.TOP_CENTER);
		layout.setExpandRatio(combo, 0.0f);
		
		final VerticalLayout vl1 = new VerticalLayout();
		vl1.setHeight("50px");
		vl1.setWidth("100%");
		vl1.setStyleName("redBlock");
		layout.addComponent(vl1);
		layout.setComponentAlignment(vl1, Alignment.TOP_CENTER);
		layout.setExpandRatio(vl1, 0.0f);
		final Label l1 = new Label(" > " + df.format(greenLimit.doubleValue()) + " " + unit);
		l1.setSizeUndefined();
		l1.addStyleName(ValoTheme.LABEL_LARGE);
		vl1.addComponent(l1);
		vl1.setComponentAlignment(l1, Alignment.MIDDLE_CENTER);

		HorizontalLayout hl1 = new HorizontalLayout();
		hl1.setSpacing(true);
		layout.addComponent(hl1);
		layout.setComponentAlignment(hl1, Alignment.TOP_LEFT);

		final TextField tf1 = new TextField();
		tf1.setValue(df.format(greenLimit.doubleValue()));
		tf1.setWidth("150px");
		tf1.setCaption("Suurempi raja-arvo");
		tf1.setStyleName(ValoTheme.TEXTFIELD_TINY);
		tf1.setValidationVisible(true);
		hl1.addComponent(tf1);
		
		Label unit1 = new Label();
		unit1.setSizeUndefined();
		unit1.setCaption("");
		unit1.setValue(unit);
		hl1.addComponent(unit1);
		hl1.setComponentAlignment(unit1, Alignment.MIDDLE_LEFT);

		final VerticalLayout vl2 = new VerticalLayout();
		vl2.setHeight("50px");
		vl2.setWidth("100%");
		vl2.addStyleName("yellowBlock");
		layout.addComponent(vl2);
		layout.setComponentAlignment(vl2, Alignment.TOP_CENTER);
		layout.setExpandRatio(vl2, 0.0f);
		final Label l2 = new Label();
		l2.setSizeUndefined();
		l2.addStyleName(ValoTheme.LABEL_LARGE);
		vl2.addComponent(l2);
		vl2.setComponentAlignment(l2, Alignment.MIDDLE_CENTER);

		HorizontalLayout hl2 = new HorizontalLayout();
		hl2.setSpacing(true);
		layout.addComponent(hl2);
		layout.setComponentAlignment(hl2, Alignment.TOP_LEFT);

		final TextField tf2 = new TextField();
		tf2.setWidth("150px");
		tf2.setCaption("Pienempi raja-arvo");
		tf2.setStyleName(ValoTheme.TEXTFIELD_TINY);
		tf2.setValidationVisible(true);
		hl2.addComponent(tf2);
		hl2.setComponentAlignment(tf2, Alignment.TOP_CENTER);

		Label unit2 = new Label();
		unit2.setSizeUndefined();
		unit2.setCaption("");
		unit2.setValue("" + unit);
		hl2.addComponent(unit2);
		hl2.setComponentAlignment(unit2, Alignment.MIDDLE_LEFT);
		
		final VerticalLayout vl3 = new VerticalLayout();
		vl3.setHeight("50px");
		vl3.setWidth("100%");
		vl3.addStyleName("greenBlock");
		layout.addComponent(vl3);
		layout.setComponentAlignment(vl3, Alignment.TOP_CENTER);
		layout.setExpandRatio(vl3, 0.0f);
		final Label l3 = new Label();
		l3.setSizeUndefined();
		l3.addStyleName(ValoTheme.LABEL_LARGE);
		vl3.addComponent(l3);
		vl3.setComponentAlignment(l3, Alignment.MIDDLE_CENTER);

		applyValues(main, meter, combo, vl1, l1, vl2, l2, vl3, l3, tf1, tf2);

		combo.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 8396168732300003038L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				
				if(inApply) return;
				
				State currentState = getState();
				
				if(currentState.equals(combo.getValue())) return;
				
				if(State.DECREASE3.equals(combo.getValue())) {
					makeThree = true;
					if(State.INCREASE3.equals(currentState)) {
						swap();
					} else if(State.INCREASE2.equals(currentState)) {
						redLimit = greenLimit;
						greenLimit = null;
					}
				} else if (State.INCREASE3.equals(combo.getValue())) {
					makeThree = true;
					if(State.DECREASE3.equals(currentState)) {
						swap();
					} else if(State.DECREASE2.equals(currentState)) {
						greenLimit = redLimit;
						redLimit = null;
					}
				} else if (State.INCREASE2.equals(combo.getValue())) {
					if(State.DECREASE3.equals(currentState)) {
						greenLimit = redLimit;
					} else if(State.DECREASE2.equals(currentState)) {
						greenLimit = redLimit;
					}
					redLimit = null;
					makeThree = false;
				} else if (State.DECREASE2.equals(combo.getValue())) {
					if(State.INCREASE3.equals(currentState)) {
						redLimit = greenLimit;
					} else if(State.INCREASE2.equals(currentState)) {
						redLimit = greenLimit;
					}
					greenLimit = null;
					makeThree = false;
				}
				
				updateMakeThree();
				
				applyValues(main, meter, combo, vl1, l1, vl2, l2, vl3, l3, tf1, tf2);
				
			}
			
		});
		
		tf1.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = -5484608577999300097L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				if(inApply) return;

				if(State.DECREASE3.equals(combo.getValue())) {
					if(makeThree) greenLimit = redLimit;
					redLimit = new BigDecimal(tf1.getValue());
				} else if (State.INCREASE3.equals(combo.getValue())) {
					if(makeThree) redLimit = greenLimit;
					greenLimit = new BigDecimal(tf1.getValue());
				} else if (State.INCREASE2.equals(combo.getValue())) {
					greenLimit = new BigDecimal(tf1.getValue());
				} else if (State.DECREASE2.equals(combo.getValue())) {
					redLimit = new BigDecimal(tf1.getValue());
				}
				
				updateMakeThree();
				
				applyValues(main, meter, combo, vl1, l1, vl2, l2, vl3, l3, tf1, tf2);
				
			}
			
		});
		
		tf2.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 5825320869230527588L;

			@Override
			public void valueChange(ValueChangeEvent event) {

				if(inApply) return;

				// This should not happen!
				if (State.INCREASE2.equals(combo.getValue()) || State.DECREASE2.equals(combo.getValue())) return;
				
				if(State.DECREASE3.equals(combo.getValue())) {
					greenLimit = new BigDecimal(tf2.getValue());
				} else if (State.INCREASE3.equals(combo.getValue())) {
					redLimit = new BigDecimal(tf2.getValue());
				}
				
				updateMakeThree();

				applyValues(main, meter, combo, vl1, l1, vl2, l2, vl3, l3, tf1, tf2);
				
			}
			
		});
		
		return null;
		
	}
	
	private void applyValues(Main main, Meter meter, ComboBox combo, VerticalLayout vl1, Label l1, VerticalLayout vl2, Label l2, VerticalLayout vl3, Label l3, TextField tf1, TextField tf2) {
		
		inApply = true;
		
		Indicator indicator = meter.getPossibleIndicator(main.getDatabase());
		String unit = indicator.getUnit();
		
		combo.select(getState());
		
		final double highDouble = getHighLimit().doubleValue();
		final double lowDouble = getLowLimit().doubleValue();
		
		String highLimit = df.format(highDouble);
		String lowLimit = df.format(lowDouble);
		
		tf1.setValue(highLimit);
		l1.setValue(highLimit + " < arvo " + unit);

		if(isIncrease()) {
			vl1.setStyleName("greenBlock");
		} else {
			vl1.setStyleName("redBlock");
		}

		tf1.removeAllValidators();
		tf1.addValidator(new Validator() {

			private static final long serialVersionUID = 5929569929930454714L;

			@Override
			public void validate(Object value) throws InvalidValueException {
				try {
					double d = Double.parseDouble((String)value);
					if(d < lowDouble) throw new InvalidValueException("Value is too small");
				} catch (NumberFormatException e) {
					throw new InvalidValueException(e.getMessage());
				}
			}
			
		});

		if(isTwo()) {

			l3.setVisible(false);
			vl3.setVisible(false);
			tf2.setVisible(false);
			tf1.setCaption("Raja-arvo");

			if(isIncrease()) {
				vl2.setStyleName("redBlock");
			} else {
				vl2.setStyleName("greenBlock");
			}
			
			l2.setValue("arvo < " + lowLimit + " " + unit);

		} else {
			
			vl3.setVisible(true);
			l3.setVisible(true);
			tf2.setVisible(true);
			tf1.setCaption("Suurempi raja-arvo");
			tf2.setCaption("Pienempi raja-arvo");
			vl2.setStyleName("yellowBlock");
			
			l2.setValue(lowLimit + " < arvo < " + highLimit + " " + unit);
			l3.setValue("arvo < " + lowLimit + " " + unit);
			tf2.setValue(lowLimit);

			if(isIncrease()) {
				vl3.setStyleName("redBlock");
			} else {
				vl3.setStyleName("greenBlock");
			}
		
			tf2.removeAllValidators();
			tf2.addValidator(new Validator() {

				private static final long serialVersionUID = 5929569929930454714L;

				@Override
				public void validate(Object value) throws InvalidValueException {
					try {
						double d = Double.parseDouble((String)value);
						if(d > highDouble) throw new InvalidValueException("Value is too large");
					} catch (NumberFormatException e) {
						throw new InvalidValueException(e.getMessage());
					}
				}
				
			});
			
			
		}
		
		inApply = false;
		
	}
	
}
