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
package fi.semantum.strategy.db;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StrategyMap extends MapBase implements Serializable {

	private static final long serialVersionUID = 7736595146150973561L;

	public int width;
	public int extraRows;
	public int columns = 2;
	public String vision;
	public OuterBox[] outerBoxes = new OuterBox[0];
	public Linkki[] parents = new Linkki[0];
	public Linkki[] alikartat = new Linkki[0];
	public OuterBox voimavarat = null;
	
	public List<Tag> generators = new ArrayList<Tag>();

	public String outerDescription = "";
	public String innerDescription = "";
	
	public String outerColor = "";
	public String outerTextColor = "";
	public String innerColor = "";
	public String innerTextColor = "";
	
	public boolean linkWithParent = true;
	public boolean linkGoalsAndSubmaps = false;

	public boolean showVision = true;
	
	private StrategyMap(final Database database, String id, String text, String vision, Collection<Pair> relations,
			Pair[] properties) {
		super(UUID.randomUUID().toString(), id, text);
		this.vision = vision;
		this.relations.addAll(relations);
		for (Pair property : properties) {
			Property p = database.find(property.first);
			p.set(false, database, database.getDefaultAdminAccount(), this, property.second);
			//Utils.setProperty(p, null, database, this, property.second);
		}
	}

	public static StrategyMap create(Database database, String id, String text, String vision, Collection<Pair> relations,
			Pair[] properties) {
		StrategyMap p = new StrategyMap(database, id, text, vision, relations, properties);
		database.register(p);
		return p;
	}
	
	@Override
	public String getId(Database database) {
		Base imp = getImplemented(database);
		if (imp != null)
			return imp.getId(database);
		return super.getId(database);
	}

	@Override
	public String getText(Database database) {
		Base imp = getImplemented(database);
		if (imp != null)
			return imp.getText(database);
		return super.getText(database);
	}

	@Override
	public boolean modifyText(Database database, Account account, String text) {
		MapBase imp = getImplemented(database);
		if (imp != null)
			return imp.modifyText(database, account, text);

		return super.modifyText(database, account, text);

	}

	@Override
	public MapBase getOwner(Database database) {
		return getPossibleParent(database);
	}

	public void addTavoite(OuterBox t) {
		outerBoxes = Arrays.copyOf(outerBoxes, outerBoxes.length + 1);
		outerBoxes[outerBoxes.length - 1] = t;
		fixRows();
	}

	public void moveUp(OuterBox t) {
		int pos = findTavoite(t);
		if (pos != -1 && pos > 0) {
			OuterBox previous = outerBoxes[pos - 1];
			outerBoxes[pos - 1] = t;
			outerBoxes[pos] = previous;
		}
	}

	public void moveDown(OuterBox t) {
		int pos = findTavoite(t);
		if (pos != -1 && pos < outerBoxes.length - 1) {
			OuterBox next = outerBoxes[pos + 1];
			outerBoxes[pos + 1] = t;
			outerBoxes[pos] = next;
		}
	}

	public int findTavoite(OuterBox t) {
		for (int i = 0; i < outerBoxes.length; i++) {
			if (t.equals(outerBoxes[i]))
				return i;
		}
		return -1;
	}

	public void removeOuterBox(Database database, OuterBox t) {
		int index = findTavoite(t);
		if (index == -1)
			throw new IllegalArgumentException("Not found: " + t.getId(database));
		removeOuterBox(index);
	}

	public void removeOuterBox(int index) {
		OuterBox[] old = outerBoxes;
		outerBoxes = new OuterBox[outerBoxes.length - 1];
		for (int i = 0, pos = 0; i < old.length; i++) {
			if (i == index)
				continue;
			outerBoxes[pos++] = old[i];
		}
		fixRows();
	}

	public void addAlikartta(StrategyMap map) {
		alikartat = Arrays.copyOf(alikartat, alikartat.length + 1);
		alikartat[alikartat.length - 1] = new Linkki(map.uuid);
		map.addParent(this);
	}

	public void removeAlikartta(String uuid) {
		Linkki[] old = alikartat;
		alikartat = new Linkki[alikartat.length - 1];
		for (int i = 0, pos = 0; i < old.length; i++) {
			if (old[i].uuid.equals(uuid))
				continue;
			alikartat[pos++] = old[i];
		}
	}

	public void addParent(StrategyMap map) {
		parents = Arrays.copyOf(parents, parents.length + 1);
		parents[parents.length - 1] = new Linkki(map.uuid);
	}

	private static final int painopisteColumns = 2;

	public int extra(OuterBox t) {
		return Math.max(0, (t.innerboxes.length - 1) / painopisteColumns);
	}

	public void fixRows() {

		this.extraRows = 0;

		for (int i = 0; i < outerBoxes.length; i++) {
			OuterBox t = outerBoxes[i];
			if ((i % 2) == 0) {

				if (i == 0) {
					t.rows = 0;
				} else {
					int l1 = extra(outerBoxes[i - 2]);
					int l2 = extra(outerBoxes[i - 1]);
					t.rows = outerBoxes[i - 2].rows + Math.max(0, Math.max(l1, l2));
				}

				if (outerBoxes.length == (i + 1)) {
					t.extraRows = extra(t);
				} else {
					t.extraRows = Math.max(extra(t), extra(outerBoxes[i + 1]));
				}

				this.extraRows += t.extraRows;

			} else {
				t.rows = outerBoxes[i - 1].rows;
				t.extraRows = outerBoxes[i - 1].extraRows;
			}
		}
	}

	@Override
	public String searchText(Database database) {
		return vision + " - " + super.searchText(database);
	}

	@Override
	public String getDescription(Database database) {
		return getId(database) + " (Strategiakartta)";
	}

	public static List<StrategyMap> enumerate(Database database) {
		ArrayList<StrategyMap> result = new ArrayList<StrategyMap>();
		for (Base b : database.enumerate()) {
			if (b instanceof StrategyMap)
				result.add((StrategyMap) b);
		}
		return result;
	}

	public boolean genTest(Database database, MapBase b) {
		for (Tag req : generators) {
			if (!b.hasRelatedTag(database, req))
				return false;
		}
		return true;
	}

	public void generate(final Database database, String currentTime) {
		
		if (!generators.isEmpty()) {

			try {

				Lucene.startWrite(database.getDatabaseId());

				Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);

				StrategyMap parent = getPossibleParent(database);

				Map<Base, List<InnerBox>> goals = new HashMap<Base, List<InnerBox>>();

				Map<Base, Boolean> implCache = new HashMap<Base, Boolean>();

				for (Linkki l : parent.alikartat) {
					StrategyMap child = database.find(l.uuid);
					for (OuterBox t : child.outerBoxes) {
						boolean tGen = genTest(database, t);
						Pair p = implementsRelation.getPossibleRelation(t);
						if (p != null) {
							Base targetPP = database.find(p.second);
							List<InnerBox> curr = goals.get(targetPP);
							if (curr == null) {
								curr = new ArrayList<InnerBox>();
								goals.put(targetPP, curr);
							}
							loop: for (InnerBox pp : t.innerboxes) {
								if (tGen) {
									curr.add(pp);
									continue loop;
								}
								for (MapBase b : UtilsDB.getImplementors(database, pp, implCache)) {
									if (genTest(database, b)) {
										curr.add(pp);
										continue loop;
									}
								}
							}
						}
					}
				}

				this.outerBoxes = new OuterBox[0];

				for (Map.Entry<Base, List<InnerBox>> entry : goals.entrySet()) {
					if (entry.getValue().isEmpty())
						continue;

					OuterBox t = OuterBox.create(database, this, entry.getKey().getId(database),
							entry.getKey().getText(database));
					for (InnerBox p : entry.getValue()) {
						InnerBox p2 = InnerBox.create(database, currentTime,
								this, t,
								p.getId(database) + " (" + database.getMap(p).getId(database) + ")",
								p.getText(database), null);
						
						//InnerBox p2 = InnerBox.create(main, this, t,
						//		p.getId(database) + " (" + database.getMap(p).getId(database) + ")",
						//		p.getText(database), null);
						p2.meters.clear();
						for (Meter m : p.getMeters(database))
							p2.addMeter(m);
					}
				}

			} catch (IOException e) {

				e.printStackTrace();

			} finally {

				Lucene.endWrite();

			}

		}

	}

	public static class CharacterInfo {
		public ObjectType mapType;
		public String goalDescription;
		public String focusDescription;
		public String color;
		public String textColor;
		public boolean linkWithParent;
		public boolean linkGoalsAndSubmaps;
		public boolean hasVision;
		public boolean hasMeter;
		public boolean allowsOwnOuterBox;
		public ObjectType goalSubmapType;
		public int amountOfLevels;

		public CharacterInfo getGoalSubmapInfo(Database database) {
			if (goalSubmapType != null) {
				CharacterInfo ci = StrategyMap.getCharacterInfo(database, goalSubmapType);
				if (ci.focusDescription.isEmpty()) {
					ci.focusDescription = goalDescription;
				}
				return ci;
			} else {
				return null;
			}
		}

	}
	
	public static int parsePossibleInteger(String value, int defaultValue) {
		if(value == null) return defaultValue;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static CharacterInfo getCharacterInfo(Database database, ObjectType mt) {

		CharacterInfo result = new CharacterInfo();

		Property characterColorP = Property.find(database, Property.CHARACTER_COLOR);
		Property goalDescriptionP = Property.find(database, Property.GOAL_DESCRIPTION);
		Property characterDescriptionP = Property.find(database, Property.CHARACTER_DESCRIPTION);
		Property characterTextColorP = Property.find(database, Property.CHARACTER_TEXT_COLOR);
		Property linkWithParentP = Property.find(database, Property.LINK_WITH_PARENT);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		Property amountOfLevelsR = Property.find(database, Property.AMOUNT_OF_LEVELS);
		Property hasVisionP = Property.find(database, Property.HAS_VISION);
		Property hasMeterP = Property.find(database, Property.HAS_METER);
		Property allowsOwnOuterBoxP = Property.find(database, Property.ALLOWS_OWN_OUTER_BOX);
		//Property definesOfficesP = Property.find(database, Property.DEFINES_OFFICES);
		
		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);

		result.mapType = mt;
		result.color = characterColorP.getPropertyValue(mt);
		if (result.color == null)
			result.color = "#034ea2";
		result.goalDescription = goalDescriptionP.getPropertyValue(mt);
		if (result.goalDescription == null)
			result.goalDescription = "";
		result.focusDescription = characterDescriptionP.getPropertyValue(mt);
		if (result.focusDescription == null)
			result.focusDescription = "";
		result.textColor = characterTextColorP.getPropertyValue(mt);
		result.linkWithParent = !"false".equals(linkWithParentP.getPropertyValue(mt));
		result.linkGoalsAndSubmaps = "true".equals(goalSubmapP.getPropertyValue(mt));
		result.amountOfLevels = parsePossibleInteger(amountOfLevelsR.getPropertyValue(mt), 3);
		result.hasVision = !"false".equals(hasVisionP.getPropertyValue(mt));
		result.hasMeter = "true".equals(hasMeterP.getPropertyValue(mt));
		result.allowsOwnOuterBox = "true".equals(allowsOwnOuterBoxP.getPropertyValue(mt));

		Collection<Base> subTypes = mt.getRelatedObjects(database, goalSubmapTypeR);
		if (subTypes.size() == 1)
			result.goalSubmapType = (ObjectType) subTypes.iterator().next();

		return result;

	}

	public ObjectType getLevelType(Database database) {
		Property level = Property.find(database, Property.LEVEL);
		String typeUUID = level.getPropertyValue(this);
		if (typeUUID != null) {
			return (ObjectType) database.find(typeUUID);
		} else {
			return null;
		}
	}

	/**
	 * Only maps with the provided type are returned
	 * @param database
	 * @param newType
	 * @return
	 */
	public static Set<StrategyMap> getMapsByObjectType(Database database, ObjectType newType){
		List<StrategyMap> possibleMaps = StrategyMap.enumerate(database);
		Set<StrategyMap> result = new HashSet<>();
		for(StrategyMap map : possibleMaps) {
			List<Pair> properties = map.properties;
			for(Pair p : properties) {
				Base found = database.find(p.second);
				if(found != null && found instanceof ObjectType) {
					ObjectType t = (ObjectType)found;
					if(t.uuid.equals(newType.uuid)) {
						result.add(map);
					}
				}
			}
			
		}
		return result;
	}
	
	public CharacterInfo getCharacterInfo(Database database) {

		Property level = Property.find(database, Property.LEVEL);
		String typeUUID = level.getPropertyValue(this);
		if (typeUUID != null) {
			ObjectType mt = (ObjectType) database.find(typeUUID);
			CharacterInfo ci = getCharacterInfo(database, mt);
			if (ci.goalDescription.isEmpty()) {
				StrategyMap parent = getPossibleParent(database);
				if (parent != null) {
					CharacterInfo parentInfo = parent.getCharacterInfo(database);
					ci.goalDescription = parentInfo.focusDescription;
				}
			}
			return ci;
		} else {
			CharacterInfo result = new CharacterInfo();
			result.focusDescription = Terminology.STRATEGIC_GOAL_OR_TARGET;
			result.goalDescription = "";
			result.color = "#034ea2";
			result.textColor = "#fff";
			result.linkWithParent = false;
			result.linkGoalsAndSubmaps = false;
			return result;
		}

	}

	public void prepare(Database database) {
		for (Linkki l : alikartat) {
			StrategyMap k = database.find(l.uuid);
			String text = k.getText(database);
			if (text.isEmpty())
				text = k.getId(database);
			if (text.isEmpty())
				text = "<anna teksti tai lyhytnimi kartalle>";
			l.text = text;
		}
		for (Linkki l : parents) {
			StrategyMap k = database.find(l.uuid);
			String text = k.getText(database);
			if (text.isEmpty())
				text = k.getId(database);
			if (text.isEmpty())
				text = "<anna teksti tai lyhytnimi kartalle>";
			l.text = text;
		}
		for (OuterBox t : outerBoxes) {
			t.synchronizeCopy(database);
			t.copy = t.isCopy(database);
			for (InnerBox p : t.innerboxes) {
				p.synchronizeCopy(database);
				p.copy = p.isCopy(database);
			}

		}

		fixRows();

		CharacterInfo info = getCharacterInfo(database);
		innerDescription = info.focusDescription;
		innerColor = info.color;
		innerTextColor = info.textColor;
		linkWithParent = info.linkWithParent;
		linkGoalsAndSubmaps = info.linkGoalsAndSubmaps;

		StrategyMap parent = getPossibleParent(database);
		if (parent != null) {
			CharacterInfo parentInfo = parent.getCharacterInfo(database);
			outerDescription = info.goalDescription.isEmpty() ? parentInfo.focusDescription : info.goalDescription;
			outerColor = parentInfo.color;
			outerTextColor = parentInfo.textColor;
		} else {
			outerDescription = info.goalDescription.isEmpty() ? Terminology.STRATEGIC_GOAL_OR_TARGET
					: info.goalDescription;
			outerColor = "#034ea2";
			outerTextColor = "#fff";
		}

	}

	public StrategyMap getPossibleParent(Database database) {
		for (Linkki l : parents) {
			return database.find(l.uuid);
		}
		return null;
	}

	public boolean isUnder(Database database, StrategyMap map) {
		if (this.equals(map))
			return true;
		StrategyMap parent = map.getPossibleParent(database);
		if (parent == null)
			return false;
		return isUnder(database, parent);
	}

	public int getLevel(Database database) {
		StrategyMap parent = getPossibleParent(database);
		if (parent == null)
			return 0;
		return 1 + parent.getLevel(database);
	}

	public List<Base> getMaps(Database database) {
		StrategyMap parent = getPossibleParent(database);
		if (parent == null) {
			List<Base> result = new ArrayList<Base>();
			result.add(this);
			return result;
		} else {
			List<Base> result = parent.getMaps(database);
			result.add(this);
			return result;
		}
	}

	public Base currentLevel(Database database) {
		Property levelProperty = Property.find(database, Property.LEVEL);
		return levelProperty.getPropertyValueObject(database, this);
	}

	public void setCurrentLevel(Database database, Base level) {
		Property levelProperty = Property.find(database, Property.LEVEL);
		levelProperty.set(true, database, database.getDefaultAdminAccount(), this, level.uuid);
	}

	public static Collection<Base> availableLevels(Database database) {
		ObjectType levelType = ObjectType.find(database, ObjectType.LEVEL_TYPE);
		return database.instances(levelType);
	}

	public boolean linkGoalsToSubmaps(Database database) {

		Base level = currentLevel(database);
		Property goalSubmapP = Property.find(database, Property.LINK_GOALS_AND_SUBMAPS);
		String goalSubmapValue = goalSubmapP.getPropertyValue(level);
		boolean tavoiteSubmap = false;
		if ("true".equals(goalSubmapValue))
			tavoiteSubmap = true;
		return tavoiteSubmap;

	}

	public Base getPossibleSubmapType(Database database) throws Exception {

		if(!linkGoalsToSubmaps(database))
			return null;
		
		return getPossibleSubmapTypeImpl(database);

	}

	Base getPossibleSubmapTypeImpl(Database database) throws Exception {

		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String) levelProperty.getPropertyValue(this));

		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);
		if(level != null) {
		Collection<Base> subTypes = level.getRelatedObjects(database, goalSubmapTypeR);
			if (subTypes.size() == 1)
				return subTypes.iterator().next();
			else if (subTypes.size() == 0)
				return null;
			else
				throw new Exception("Multiple submap types.");
		} else {
			return null;
		}
		
	}
	
	public boolean pruneSubmapType(Database database) {
		
		if(linkGoalsToSubmaps(database))
			return false;
		
		Property levelProperty = Property.find(database, Property.LEVEL);
		ObjectType level = database.find((String) levelProperty.getPropertyValue(this));
		Relation goalSubmapTypeR = Relation.find(database, Relation.TAVOITE_SUBMAP);
		
		Collection<Base> subTypes = level.getRelatedObjects(database, goalSubmapTypeR);
		
		// Clear all
		level.denyRelation(database, goalSubmapTypeR);

		// Something was removed
		return !subTypes.isEmpty();
		
	}
	
	@Override
	public String clientIdentity() {
		return "Strategiakartta";
	}
	
}
