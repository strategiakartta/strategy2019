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
package fi.semantum.strategia.filter;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.semantum.strategia.LinkSpec;
import fi.semantum.strategia.Main;
import fi.semantum.strategia.VisuSpec;
import fi.semantum.strategia.widget.BrowserLink;
import fi.semantum.strategia.widget.BrowserNode;
import fi.semantum.strategy.db.Base;
import fi.semantum.strategy.db.BrowserNodeState;
import fi.semantum.strategy.db.Database;
import fi.semantum.strategy.db.InnerBox;
import fi.semantum.strategy.db.MapBase;
import fi.semantum.strategy.db.Meter;
import fi.semantum.strategy.db.OuterBox;
import fi.semantum.strategy.db.StrategyMap;
import fi.semantum.strategy.db.UIState;

public class FilterState {

	public static class ReportCell {
		
		public static ReportCell EMPTY = new ReportCell("-", "-", "-");
		
		public String id;
		public String caption;
		public String cell;
		
		public ReportCell(String id, String caption, String cell) {
			this.id = id;
			this.caption = caption;
			this.cell = cell;
		}
		
		public ReportCell(String text) {
			this(text, text, text);
		}
		
		public ReportCell(String shortText, String longText) {
			this(shortText, shortText, longText);
		}

		public String get() {
			if(id.equals(caption)) {
				return id;
			} else {
				return caption;
			}
		}

		public String getLong() {
			if(cell.length() > 0) return cell;
			return get();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((caption == null) ? 0 : caption.hashCode());
			result = prime * result + ((cell == null) ? 0 : cell.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReportCell other = (ReportCell) obj;
			if (caption == null) {
				if (other.caption != null)
					return false;
			} else if (!caption.equals(other.caption))
				return false;
			if (cell == null) {
				if (other.cell != null)
					return false;
			} else if (!cell.equals(other.cell))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

	}
	
	private Map<Base, Integer> indexMap = new HashMap<Base, Integer>();

	public List<BrowserNode> nodes = new ArrayList<BrowserNode>();
	public List<BrowserLink> links = new ArrayList<BrowserLink>();

	public LinkedList<LinkSpec> linkSpecs = new LinkedList<LinkSpec>();
	public Collection<Map<String,ReportCell>> report = new HashSet<Map<String,ReportCell>>();
	public List<String> reportColumns = new ArrayList<String>();

	private HashSet<List<MapBase>> acceptSet = new HashSet<List<MapBase>>();
	private List<List<MapBase>> accepted = new ArrayList<List<MapBase>>();
	
	final private int windowWidth;
	final private int windowHeight;
	
	final private Main main;

	public FilterState(Main main, int windowWidth, int windowHeight) {
		this.main = main;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
	}
	
	public int countAccepted() {
		return accepted.size();
	}
	
	public Set<List<MapBase>> getPreAccept() {
		return acceptSet;
	}
	
	public void accept(NodeFilter filter, List<MapBase> mutablePath) {
		ArrayList<MapBase> path = new ArrayList<MapBase>(mutablePath);
		if(acceptSet.add(path)) {
			accepted.add(path);
		}
	}
	
	public boolean isPrefix(List<MapBase> path) {
		int pathLen = path.size();
		loop: for(List<MapBase> p : accepted) {
			if(p.size() > pathLen) {
				for(int i=0;i<pathLen;i++) {
					if(!path.get(i).equals(p.get(i))) continue loop;
				}
				return true;
			}
		}
		return false;
	}
	
	public Collection<List<MapBase>> getAcceptedNodes() {
		return accepted;
	}
	
	public void reject(List<MapBase> path) {
		accepted.remove(path);
	}
	
	BrowserNode addNode(MapBase b) {

		if(indexMap.containsKey(b)) {
			return nodes.get(indexMap.get(b));
		}
		
		BrowserNode node = makeNode(b);
		// Might be null also
		indexMap.put(b, nodes.size());
		nodes.add(node);

		// Default parents
		MapBase parent = main.getDatabase().getDefaultParent(b);
		if(parent != null) {
			BrowserNode parentNode = addNode(parent);
			parentNode.addChild(b);
			addLink(b, parent, true);
		}
		
		return node;

	}
	
	private Point2D randomPos(int x, int y) {
		return new Point2D.Double(x + (Math.random() - 0.5) * 100, y
				+ (Math.random() - 0.5) * 100);
	}

	public void linkIfAbsent(MapBase source, MapBase target, String color, String dash,
			double weight, double linkDistance, double linkStrength) {
		Integer sId = indexMap.get(source);
		if (sId == null)
			return;
		Integer tId = indexMap.get(target);
		if (tId == null) {
			List<MapBase> path = main.getDatabase().getDefaultPath(target);
			if(path == null) return;
			addPath(path);
			tId = indexMap.get(target);
			if (tId == null) return;
		}
		for(BrowserLink l : links)
			if((sId == l.source && tId == l.target) || (sId == l.target && tId == l.source))
				return;
		links.add(new BrowserLink(color, dash, sId, tId, weight, linkDistance, linkStrength));
	}
	
	private Map<Integer,List<BrowserNode>> maps = new HashMap<Integer,List<BrowserNode>>();
	
	private int computeSize(Base b) {
		if(b instanceof StrategyMap) {
			return 500;
		} else if(b instanceof OuterBox) {
			return 325;
		} else if(b instanceof InnerBox) {
			return 325;
		} else if(b instanceof Meter) {
			return 325;
		} else {
			return 325;
		}
	}

	private int computeSize(VisuSpec spec, Base b) {
		
		double pathFactor = (8.0 / (spec.pathLength+5.0));
		pathFactor = Math.max(Math.min(pathFactor, 1.0), 0.2);
		int objectSize = computeSize(b);
		return (int)(pathFactor*objectSize);
		
	}
	
	BrowserNode makeNode(MapBase b) {
		
		final Database database = main.getDatabase();

		UIState state = main.getUIState();
		VisuSpec spec = main.getCurrentFilter().acceptNode(Collections.singletonList(b), this);
		if(spec == null) {
			return null;
		}
		
		Point2D random = randomPos(windowWidth / 2, windowHeight / 2);
		StrategyMap containerMap = database.getMap(b);
		if(state.browserStates == null) state.browserStates = new HashMap<String, BrowserNodeState>();
		BrowserNodeState ns = state.browserStates.get(b.uuid); 
		
		int size = computeSize(spec, b);
		int height = (int)(0.4*size);
		int nameSize = (int)(0.3*height);
		int descriptionSize = (int)(0.2*height);
		int repel = -30*size;
		
		if(b instanceof StrategyMap) {
			StrategyMap map = (StrategyMap)b;
			int level = map.getLevel(database);
			BrowserNode node = new BrowserNode(nodeCaption(database, map), map.uuid, ns, true, map.innerColor, nameSize, descriptionSize, 0, 0,
					size, height, repel, spec);
			if(main.getUIState().requiredMapItems.contains(b))
				node.required = true;
			List<BrowserNode> list = maps.get(level);
			if(list == null) {
				list = new ArrayList<BrowserNode>();
				maps.put(level, list);
			}
			list.add(node);
			return node;
		} else if(b instanceof OuterBox) {
			OuterBox goal = (OuterBox)b;
			BrowserNode node = new BrowserNode(nodeCaption(database, goal), goal.uuid, ns, false, containerMap.outerColor, nameSize, descriptionSize,
					(int) random.getX(), (int) random.getY(), size, height, repel, spec); 
			if(main.getUIState().requiredMapItems.contains(b))
				node.required = true;
			return node;
		} else if(b instanceof InnerBox) {
			InnerBox focus = (InnerBox)b;
			BrowserNode node = new BrowserNode(nodeCaption(database, focus), focus.uuid, ns, false, containerMap.innerColor, nameSize, descriptionSize,
					(int) random.getX(), (int) random.getY(), size, height, repel, spec); 
			if(main.getUIState().requiredMapItems.contains(b))
				node.required = true;
			return node;
		} else if(b instanceof Meter) {
			Meter m = (Meter)b;
			return new BrowserNode(nodeCaption(database, m), m.uuid, ns, false, "#222", nameSize, descriptionSize,
			(int) random.getX(), (int) random.getY(), size, height, repel, spec);
		}
		
		return null;
		
	}
	
	String nodeCaption(Database database, Base b) {
		String id = b.getId(database);
		if(id != null && !id.isEmpty()) return id;
		String text = b.getText(database);
		if(text.length() > 15) text = text.substring(0, 15);
		return text;
	}
	
	void addLink(MapBase b, MapBase parent, boolean useDefaultColoring) {
		
		StrategyMap containerMap = main.getDatabase().getMap(b);
		
		if(!useDefaultColoring) {
			linkIfAbsent(parent, b, "#777", "15, 15", 20, 200, 0.5);
			return;
		}
		
		if(b instanceof StrategyMap) {
			StrategyMap map = (StrategyMap)b;
			linkIfAbsent(parent, b, "#d22", "35, 15", 60, 1750, 0.2);
		} else if(b instanceof OuterBox) {
			linkIfAbsent(parent, b, containerMap.outerColor, "45, 15", 80, 550, 0.2);
		} else if(b instanceof InnerBox) {
			linkIfAbsent(parent, b, containerMap.innerColor, "55, 15", 80, 200, 0.2);
		} else {
			linkIfAbsent(parent, b, "#777", "15, 15", 20, 200, 0.2);
		}
		
	}

	void addPath(List<MapBase> path) {
//		for(MapBase b : path) {
//			addNode(b);
//		}
		if(path.isEmpty()) return;
		addNode(path.get(0));
		for(int i=1;i<path.size();i++) {
			BrowserNode n1 = addNode(path.get(i-1));
			BrowserNode n2 = addNode(path.get(i));
			if(n1 != null && n2 != null) {
				n1.addChild(path.get(i));
				addLink(path.get(i), path.get(i-1), false);
			}
		}
	}
	
	public void process(NodeFilter filter, boolean setPositions) {
		
		long s1 = System.nanoTime();
		
		if(main.getUIState().reportAll) {
			filter.report(getPreAccept(), this);
		} else {
			filter.report(accepted, this);
		}

		long s2 = System.nanoTime();
		//System.err.println("report: " + (1e-6*(s2-s1) + "ms."));

		main.reportStatus.setCaption(" " + report.size() + " tulosta.");
		
		// Create nodes and links
		for(List<MapBase> path : new ArrayList<List<MapBase>>(accepted)) {
			addPath(path);
		}
		
		for(LinkSpec spec : linkSpecs) {
			linkIfAbsent(spec.source, spec.target, spec.stroke, "15, 15", 15, 500, 0.1);
		}
		
		for(int level : maps.keySet()) {
			
			List<BrowserNode> ns = maps.get(level);
			Collections.sort(ns, new Comparator<BrowserNode>() {

				@Override
				public int compare(BrowserNode arg0, BrowserNode arg1) {
					return arg0.uuid.compareTo(arg1.uuid);
				}
				
			});
			
			if(!setPositions) {
				int span = 1500 * level;
				for(int i=0;i<ns.size();i++) {
					BrowserNode node = ns.get(i);
					node.x = windowWidth/3 + 2*span;
					node.y = windowHeight/2 + (int)((Math.random() - 0.5) * span);
				}
			}
			
		}
		
	}

}
