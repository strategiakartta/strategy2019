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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class UtilsDB {
	
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * This is used for naming database backup copies on file-system.
	 */
	public static final SimpleDateFormat backupDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
	public static final SimpleDateFormat dailyDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat clockDateFormat = new SimpleDateFormat("H.mm:ss");
	public static final SimpleDateFormat dayDateFormat = new SimpleDateFormat("d.M.yyyy");
	private static final List<Extension> commonMarkExtensions = Arrays.asList(TablesExtension.create());
	
	public static interface HTMLMapTransform {
		
		public String tag();
		public Map<String, String> possibleParametersGuide(Database database);
		public String replace(Database database, StrategyMap map, Attributes attributes);
		
	}
	
	/**
	 * 
	 * @param database
	 * @param docs
	 * @param index
	 * @param is_shared
	 */
	public static void synchronizeChapterChange(Database database, ResultAgreementDocuments docs, int index, boolean is_shared) {
		ResultAgreementConfiguration rac = ResultAgreementConfiguration.getInstance(database);
		rac.setIsShared(index, is_shared);
		
		deleteMatchingChaptersByIndex(database, docs, index);
		
		if(is_shared) {
			System.out.println("Adding new shared TextChapter at index " + index);
			TextChapter chapter = TextChapter.create(database, "Auto-Generated Description", "Auto-Generated Text", index, "Auto-Generated Content", null);
			docs.registerChapter(database, chapter);
		} else {
			for(Office o : Office.enumerate(database, false, false)) {
				System.out.println("Adding new TextChapter for office " + o.text + " at index " + index);
				TextChapter chapter = TextChapter.create(database,
						rac.getChapterDescription(index),
						rac.getChapterLabel(index),
						index,
						"Auto-Generated Stub",
						o);
				docs.registerChapter(database, chapter);
			}
		}
	}
	
	/**
	 * Register a new chapter at the end.
	 * @param database
	 * @param docs
	 * @return
	 */
	public static boolean appendNewSharedChapter(Database database, ResultAgreementDocuments docs) {
		ResultAgreementConfiguration rac = ResultAgreementConfiguration.getInstance(database);
		int new_index = rac.addEntry(database, true);
		if(new_index >= 0) {
			TextChapter chapter = TextChapter.create(database,
					rac.getChapterDescription(new_index),
					rac.getChapterLabel(new_index),
					new_index,
					"Auto-Generated Stub",
					null);
			System.out.println("Adding new shared TextChapter at index " + new_index);
			return docs.registerChapter(database, chapter);
		} else {
			return false;
		}
	}
	
	/**
	 * Deletes all chapters with the chapterIndex to_delete
	 * @param chapters
	 * @param to_delete
	 * @return The remaining chapters
	 */
	private static List<TextChapter> deleteMatchingChaptersByIndex(Database database, ResultAgreementDocuments docs, int to_delete) {
		List<TextChapter> chapters = docs.getAllChapters(database);
		
		System.out.println("Total chapters: " + chapters.size());
		
		for(int i = 0; i < chapters.size(); i++) {
			TextChapter chapter = chapters.get(i);
			if(chapter.getChapterIndex() == to_delete) {
				System.out.println("Deleting chapter " + chapter.text + ", at index: " + chapter.chapterIndex);
				chapters.remove(i);
				deleteChapterFull(database, docs, chapter);
			}
			
		}
		
		return chapters;
	}
	
	/**
	 * 
	 * @param database
	 * @param docs
	 * @param chapter
	 */
	public static void deleteChapterFull(Database database, ResultAgreementDocuments docs, TextChapter chapter) {
		docs.unregisterChapter(database, chapter);
		database.remove(chapter);
	}
	
	/**
	 * Re-indexes chapters to have 1 smaller index if above to_delete index, to make indexes contiguous
	 * @param database
	 * @param chapters
	 * @param to_delete
	 * @return
	 */
	private static void reindexChapters(Database database, List<TextChapter> chapters, int to_delete) {
		for(int i = 0; i < chapters.size(); i++) {
			TextChapter chapter = chapters.get(i);
			int old_index = chapter.getChapterIndex();
			if(old_index > to_delete) {
				System.out.println("Reindexed chapterIndex from: " + old_index + " to: " + (old_index - 1));
				chapter.setChapterIndex(old_index - 1);
			}
		}
	}
	
	/**
	 * Deletes a chapter from the database permanently.
	 * Remove all associated TextChapters from the database.
	 * Updates all indexes of all elements in database to reflect the new order.
	 * @param database
	 * @param i
	 */
	public static boolean deleteResultAgreementChapterPermanently(Database database, ResultAgreementDocuments docs, int to_delete) {
		if(docs == null) {
			return false;
		}
		
		ResultAgreementConfiguration rac = ResultAgreementConfiguration.getInstance(database);
		boolean delete_ok = rac.deleteEntry(to_delete);
		if(delete_ok) {
			List<TextChapter> all_chapters = docs.getAllChapters(database);
			
			List<TextChapter> remaining_chapters = deleteMatchingChaptersByIndex(database, docs, to_delete);
			if(remaining_chapters.size() < all_chapters.size()) {
				reindexChapters(database, remaining_chapters, to_delete);
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}
	
	private static List<String> mapBaseAndParentsToStringPath(Database database, MapBase base){
		List<String> result = new ArrayList<>();
		Base parent = base.getOwner(database);
		if(parent != null && parent instanceof MapBase) {
			MapBase targetParent = MapBase.findTargetedMapBoxElement(database, (MapBase)parent);
			
			if(targetParent != null) {
				List<String> parentMapVises = mapBaseAndParentsToStringPath(database, targetParent);
				result.addAll(parentMapVises);
			} else {
				List<String> parentMapVises = mapBaseAndParentsToStringPath(database, (MapBase)parent);
				result.addAll(parentMapVises);	
			}
		}
		
		String text = mapBaseText(database, base);
		
		if(text != null) {
			result.add(database.getType(base) + ": " + text);
		} else {
			System.err.println("Text for base " + base.uuid + " was null!");
		}
		return result;
	}
	
	
	/*
	 * The last segment of this path is null
	 */
	public static PathVis[] mapPath(Database database, StrategyMap map) {

		List<MapBase> pathBases = database.getDefaultPath(map);
		PathVis[] path = new PathVis[pathBases.size()];
		for(int i=0;i<pathBases.size();i++) {

			StrategyMap b = (StrategyMap)pathBases.get(i);
			path[i] = makePathVis(database, b);
			
		}

		return path;
	}
	

	public static PathVis makePathVis(Database database, StrategyMap b) {

		b.prepare(database);

		String desc = b.getText(database);
		MapBase b2 = b.getImplemented(database);
		if(b2 instanceof OuterBox) {
			StrategyMap p = database.getMap(b2);
			if(p != null) {
				p.prepare(database);
				desc = p.outerDescription + ": " + desc;
			}
		}

		return new PathVis(b.uuid, desc, b.outerTextColor, b.outerColor);

	}
	
	public static List<String> changeSuggestionTargetPathAsList(Database database, ChangeSuggestion cs) {
		List<String> result = new ArrayList<>();
		String uuid = cs.getPossibleBaseTargetUUID();
		if(uuid != null) {
			Base base = database.find(uuid);
			if(base instanceof MapBase) {
				List<String> mapVises = mapBaseAndParentsToStringPath(database, (MapBase)base);
				for(String t : mapVises) {
					result.add(t);
				}
			} else {
				if(base instanceof TextChapter) {
					TextChapter chapter = (TextChapter)base;
					result.add(UtilsDBComments.textChapterToSummary(database, chapter));
				}
			}
		} else {
			result.add("(muutosehdotuksella ei ole kohdetta)");
		}
		return result;
	}
	
	public static String mapBaseText(Database database, MapBase base) {
		String text = base.getText(database);
		if(text.isEmpty()) return base.getId(database);
		else return text;
	}
	
	public static List<String> subChapterHtmlsTargetedByChangeSuggestions(Database database, Account account, TextChapter chapter){
		List<ChangeSuggestion> unfilteredCSs = ChangeSuggestion.changeSuggestionsForBase(database, chapter);
		List<ChangeSuggestion> changeSuggestions = UtilsDBComments.filterChangeSuggestionVisibleToAccount(database, account, unfilteredCSs,
				ChangeSuggestion.ChangeSuggestionState.OPEN_ACTIVE);
		
		List<String> possibleClickableList = new ArrayList<>();
		for(ChangeSuggestion cs : changeSuggestions) {
			if(!cs.isClosed()) {
				String possibleTargetString = cs.getPossibleTargetString();
				if(possibleTargetString != null) {
					possibleClickableList.add(possibleTargetString);
				}
			}
		}
		List<String> innerHTMLListWithChangeSuggestions = UtilsDB.findHTMLPositionsFromMarkdown(chapter.unformattedContent, possibleClickableList);
		return innerHTMLListWithChangeSuggestions;
	}
	
	/**
	 * 
	 * @param database
	 * @param attachedMap
	 * @param objectType
	 * @return
	 */
	public static Office createAndSynchronizeNewOffice(Database database, StrategyMap attachedMap, ObjectType objectType) {
		StrategyMap rootMap = database.getRoot();
	
		CommentToolBase commentToolBase = CommentToolBase.fetchOrCreateSingleton(database);
		Office newOffice = Office.create(database, attachedMap, false);
		AccountGroup group = AccountGroup.ensureAccountGroupMappedToOffice(database, newOffice);
		
		ResultAgreementConfiguration resultAgreementConfiguration = ResultAgreementConfiguration.getInstance(database);
		
		if(newOffice != null && group != null) {
			for(ResultAgreementDocuments doc : UtilsDB.getAllLinkedResultAgreementDocuments(database, rootMap)) {
				group.addRightIfMissing(new AccessRight(doc, false)); //Only read access
				Integer[] officeChapters = resultAgreementConfiguration.getOfficeIndeces();
				for(int i : officeChapters) {
					TextChapter chapter = TextChapter.create(database,
							resultAgreementConfiguration.getChapterDescription(i),
							resultAgreementConfiguration.getChapterLabel(i),
							i, "Auto-Generated Stub" , newOffice);
					boolean success = doc.registerChapter(database, chapter);
					if(!success) {
						System.err.println("Failed to add chapter to ResultAgreementDocument" + doc.id);
					}
				}
			}
			
			//This account is associated with the office, and is also associated with the account group
			group.addRightIfMissing(new AccessRight(newOffice, false)); //Read access only to the office
			group.addRightIfMissing(new AccessRight(commentToolBase, true)); //Read+Write access to comments
			UtilsDB.associateAccountGroupWithOffice(database, newOffice, group);
		} else {
			System.err.println("Error creating new office! NewOffice or group was null");
		}

		return newOffice;
	}
	
	public static StrategyMap removeMapAndRelatedObjects(Database database, StrategyMap map) {
		StrategyMap parent = map.getPossibleParent(database);
		
		List<ResultAgreementDocuments> documents = getAllLinkedResultAgreementDocuments(database, map);
		
		for(ResultAgreementDocuments doc : documents) {
			unlinkMap(database, doc, map);
		}
		
		Office office = Office.getPossibleOfficeByMap(database, map);
		if(office != null) {
			UtilsDB.removeAllAssociatedObjectsByOffice(database, office);
			database.remove(office);
		}
		
		database.remove(map);
		return parent;
	}
	
	public static boolean renameStrategyMapAndRelatedObjects(Database database, StrategyMap map, Account account, String id, String text) {
		
		boolean nameExists = false;
		Office office = Office.getPossibleOfficeByMap(database, map);

		if(map.getText(database) != null && !map.getText(database).equals(text)) {
			if(office != null) {
				if(UtilsDB.canWrite(database, account, office)){
					AccountGroup group = getPossibleAccountGroupByOffice(database, office);
					if(UtilsDB.canWrite(database, account, group)){
						if(group != null) {
							for(AccountGroup g : AccountGroup.enumerate(database)) {
								if(g.text.equals(text)) {
									nameExists = true;
									break;
								}
							}
							
							if(!nameExists) {
								group.text = text;
							} else {
								System.err.println("Name already exists! Tried renameStrategyMapAndRelatedObjects of map that has offices and account groups to "
							+ text + ", but an AccountGroup with that name exists!");
							}
						}
					} else {
						System.err.println("Account without permissions tried to renameStrategyMapAndRelatedObjects for group object. Map ID: "
								+ map.getId(database) + ", account ID: " + account.getId(database) + ", group ID: " + group.getId(database));
						return false;
					}
				} else {
					System.err.println("Account without permissions tried to renameStrategyMapAndRelatedObjects for office object. Map ID: "
							+ map.getId(database) + ", account ID: " + account.getId(database) + ", office ID: " + office.getId(database));
					return false;
				}
			}
		}
		
		if(!nameExists) {
			map.modifyId(database, account, id);
			map.modifyText(database, account, text);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Only the first layer is considered. Compared to getCompleteImplementationSet, which recursively finds all.
	 * @param database
	 * @param b
	 * @return
	 */
	public static Set<MapBase> getImmediateParentImplementationSet(Database database, MapBase b) {
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<Base> impB = b.getRelatedObjects(database, implementsRelation);
		Collection<MapBase> imp = new ArrayList<>();
		for(Base b2 : impB) {
			if(b2 instanceof MapBase) {
				imp.add((MapBase)b2);
			}
		}
		if(imp.isEmpty()) return Collections.emptySet();
		Set<MapBase> result = new TreeSet<MapBase>();
		result.addAll(imp);
		
		if(b instanceof InnerBox) {
			result.add(((InnerBox)b).getGoal(database));
		}
		return result;
	}
	
	/**
	 * 
	 * @param database
	 * @param base
	 * @return set of accounts that have seen the current state of the Base
	 */
	public static Set<Account> getSeenByAccounts(Database database, Base base){
		Collection<Base> seenBy = base.getRelatedObjects(database, Relation.find(database, Relation.SEEN_BY_USER));
		Set<Account> result = new HashSet<>(seenBy.size());
		for(Base b : seenBy) {
			if(b instanceof Account) {
				result.add((Account)b);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @param database
	 * @param account
	 * @param base
	 */
	public static void setSeenByAccount(Database database, Account account, Base base) {
		base.addRelation(Relation.find(database, Relation.SEEN_BY_USER), account);
	}
	
	/**
	 * 
	 * @param database
	 * @param base
	 */
	public static void removeAllSeenByAccount(Database database, Base base) {
		base.denyRelation(database, Relation.find(database, Relation.SEEN_BY_USER));
	}
	
	/**
	 * 
	 * @param database
	 * @param account
	 * @param base
	 * @return 
	 */
	public static boolean seenByAccount(Database database, Account account, Base base) {
		Set<Account> seenBy = getSeenByAccounts(database, base);
		for(Account a : seenBy) {
			if(a.uuid.equals(account.uuid)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * All layers are considered
	 * @param database
	 * @param b
	 * @return
	 */
	public static Set<Base> getCompleteImplementationSet(Database database, Base b) {
		
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<Base> imp = b.getRelatedObjects(database, implementsRelation);
		if(imp.isEmpty()) return Collections.emptySet();
		Set<Base> result = new TreeSet<Base>();
		
		if(b instanceof InnerBox) {
			result.add(((InnerBox)b).getGoal(database));
		}
		
		for(Base b2 : imp) {
			result.add(b2);
			Set<Base> bs = getCompleteImplementationSet(database, b2);
			result.addAll(bs);
		}
		return result;
	}
	
	public static Base getPossibleRootParentImplementation(Database database, Base b) {
		Relation implementsRelation = Relation.find(database, Relation.IMPLEMENTS);
		Collection<Base> imp = b.getRelatedObjects(database, implementsRelation);
		if(imp.isEmpty()) return null;
		Base parentCandidate = imp.iterator().next();
		Base possibleNewParent = getPossibleRootParentImplementation(database, parentCandidate);
		if(possibleNewParent != null) {
			return possibleNewParent;
		} else {
			return parentCandidate;
		}
	}
	
	public static String getValidity(Database database, Base base) {

		Property aika = Property.find(database, Property.AIKAVALI);
		String result = aika.getPropertyValue(base);
		if(result == null) result = Property.AIKAVALI_KAIKKI;
		return result;

	}
	
	/**
	 * Call the markdown parser as-is
	 * @param markdown
	 * @return
	 */
	public static String markdownToPureHTML(String markdown) {
		Parser parser = Parser.builder()
				.extensions(commonMarkExtensions)
				.build();
		Node document = parser.parse(markdown);
		
		HtmlRenderer renderer = HtmlRenderer.builder()
				.extensions(commonMarkExtensions)
				.build();
		
		String html2 = renderer.render(document);
		return html2;
	}
	
	public static List<String> findHTMLPositionsFromMarkdown(String markdown, List<String> targetedStrings) {
		String html = markdownToPureHTML(markdown);
		Document doc = Jsoup.parse(html);
		
		List<String> innerHTMLs = new ArrayList<>();
		for(Element element : doc.body().getAllElements()) {
			String prefix = element.tagName();
			if(markableSubElements.contains(prefix)) {
				String elementHTML = element.html();
				boolean contains = false;
				for(String ts : targetedStrings) {
					if(elementHTML.contains(ts)) {
						contains = true;
						break;
					}
				}
				if(contains) {
					innerHTMLs.add(elementHTML);
				}
			}
		}
		
		return innerHTMLs;
	}
	
	/**
	 * Markdown to HTML
	 * @param unescapedMarkdown
	 * @param addClickableHTMLStylingForElements
	 * @return
	 */
	public static String markdownToHTML(String unescapedMarkdown,
			boolean addClickableHTMLStylingForElements) {
		
		String html = markdownToPureHTML(unescapedMarkdown);
		html = customTableWidthReplaces(html);
		
		if(addClickableHTMLStylingForElements) {
			html = markSubElements(html);
		}
		
		Document doc = Jsoup.parse(html);
		return doc.body().html();
	}
	
	/**
		1) Example input as Markdown:
	  	
	  	| Header1 | Header2 | Header3 |
		| ---- | ---- | ---- |
		| <cols /> 50% | 30% | 20%|
		| ContentCell1 |  ContentCell2 | ContentCell3 |
		
		2) Converts first to HTML that is provided to this function:
	 	
	 	<table>
		<thead>
		<tr><th>Header1</th><th>Header2</th><th>Header3</th></tr>
		</thead>
		<tbody>
		<tr><td><cols /> 50%</td><td>30%</td><td>20%</td></tr>
		<tr><td>ContentCell1</td><td>ContentCell2</td><td>ContentCell3</td></tr>
		</tbody>
		</table>
		
		3) Which returns this HTML:
		
		<table>
		 <col width="50%">
		 <col width="30%">
		 <col width="20%"> 
		 <thead> 
		  <tr>
		   <th>Header1</th>
		   <th>Header2</th>
		   <th>Header3</th>
		  </tr> 
		 </thead> 
		 <tbody>  
		  <tr>
		   <td>ContentCell1</td>
		   <td>ContentCell2</td>
		   <td>ContentCell3</td>
		  </tr> 
		 </tbody> 
		</table>
	 * 
	 * @param html
	 * @return
	 */
	private static String customTableWidthReplaces(String html) {
		Document doc = Jsoup.parse(html);
		
		for(Element possibleTableElement : doc.body().children()) {
			String prefix = possibleTableElement.tagName();
			if(prefix.equals("table")) {
				List<String> possibleWidths = new ArrayList<>();
				
				List<Element> widthElements = possibleTableElement.getElementsByTag("cols");
				if(widthElements.size() == 1) {
					Element widthWidthCell = widthElements.get(0);
					Element parentCell = widthWidthCell.parent();
					if(parentCell != null && parentCell.tagName().equals("td")) {
						Element parentRow = parentCell.parent();
						if(parentRow != null && parentRow.tagName().equals("tr")) {
							for(Element cell : parentRow.children()) {
								if(cell.tagName().equals("td")) {
									String cellContent = cell.wholeText().trim();
									//^Find only the text, not the inner html elements. In other words, <cols /> element is not in this String.
									possibleWidths.add(cellContent);
								} else {
									possibleWidths.add("");
								}
							}
							parentRow.remove();
						}
					}
				}
				
				if(widthElements.size() != 0) {
					List<Element> children = new ArrayList<>(possibleWidths.size());
					for(String width : possibleWidths) {
						Element element = new Element("col");
						element.attr("width", width);
						children.add(element);
					}
					possibleTableElement.insertChildren(0, children);
				}
			}
		}
		
		return doc.body().html();
	}
	
	public static final List<String> markableSubElements = initMarkableElements();
	
	private static List<String> initMarkableElements(){
		List<String> result = new ArrayList<String>();
		result.add("ul");
		result.add("div");
		result.add("p");
		result.add("h1");
		result.add("h2");
		result.add("h3");
		result.add("h4");
		result.add("h5");
		result.add("h6");
		return result;
	}
	
	private static String markSubElements(String html) {
		for(String e : markableSubElements) {
			html = html.replaceAll("<" + e + ">", "<" + e +" class=\"htmlDocumentSubElement\">");
		}
		return html;
	}
	
	
	public static String dateString(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		int minutes = cal.get(Calendar.MINUTE);
		int seconds = cal.get(Calendar.SECOND);
		return "Strategiakartta_" + day + "_" + month + "_" + year + "__" + hours + "_" + minutes + "_" + seconds;
	}
	
	/**
	 * Try to find a single year from a time String
	 * @param time
	 * @return
	 */
	public static Integer parseTimeToYear(String time) {
		if(time == null) return null;
		
		TimeInterval ti = TimeInterval.parse(time);
		if(ti.isSingle()) {
			return ti.startYear;
		}
		
		return null;
	}
	
	public static Set<String> getResponsibilityFields(Database database, MapBase b) {
		
		Set<String> result = new TreeSet<String>();
		buildResponsibilityFieldsInternal(database, b, result);
		return result;

	}

	public static MapBase getPossibleImplemented(Database db, MapBase b) {
		Relation implementsRelation = Relation.find(db, Relation.IMPLEMENTS);
	   	Pair p = implementsRelation.getPossibleRelation(b);
	   	if(p != null) {
	   		return db.find(p.second);
	   	}
	   	return null;
	}
	
	public static MapBox getPossibleImplemented(Database database, MapBox box, String characterDescription) {
		ArrayList<MapBox> result = new ArrayList<MapBox>();
		for(Base b : box.getBase().getImplementedSet(database)) {
			if(b instanceof MapBox) {
				MapBox mb = (MapBox)b;
				if(characterDescription.contentEquals(mb.getBoxDescription(database))) {
					result.add(mb);
				}
			}
		}
		if(result.size() == 1) return result.get(0);
		else return null;
	}
	
	private static void buildResponsibilityFieldsInternal(Database database, MapBase b, Set<String> result) {
		
		StrategyMap map = database.getMap(b);
		
		Base currentLevel = map.currentLevel(database);

		Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
		ResponsibilityModel model = modelRelation.getPossibleRelationObject(database, currentLevel);
		if(model != null) {
			List<String> fields = model.getFields();
			result.addAll(fields);
		}
		
		MapBase implemented = UtilsDB.getPossibleImplemented(database, b);
		if(implemented != null)
			buildResponsibilityFieldsInternal(database, implemented, result);
		
	}

	public static Map<String,String> getResponsibilityMap(Database database, MapBase b) {
		
		TreeMap<String,String> result = new TreeMap<String,String>();
		buildResponsibilityMapInternal(database, b, result);
		List<String> overrides = new ArrayList<String>();
		for(Map.Entry<String, String> e : result.entrySet())
			if("-".equals(e.getValue()))
				overrides.add(e.getKey());
		for(String override : overrides)
			result.remove(override);
		return result;

	}

	private static void buildResponsibilityMapInternal(Database database, MapBase b, Map<String,String> result) {
		
		StrategyMap map = database.getMap(b);

		Base currentLevel = map.currentLevel(database);
		
		Relation modelRelation =  Relation.find(database, Relation.RESPONSIBILITY_MODEL);
		ResponsibilityModel model = modelRelation.getPossibleRelationObject(database, currentLevel);
		if(model == null) return;
		
		Relation instanceRelation =  Relation.find(database, Relation.RESPONSIBILITY_INSTANCE);
		ResponsibilityInstance instance = instanceRelation.getPossibleRelationObject(database, b);
		ResponsibilityInstance defaultInstance = instanceRelation.getPossibleRelationObject(database, map);

		Set<String> fields = getResponsibilityFields(database, b);
		for(String field : fields) {
			if(result.containsKey(field)) continue;
			String value = null;
			if(instance != null) {
				value = instance.getValue(field);
				if(value != null && !value.isEmpty())
					result.put(field, value);
			} else if(defaultInstance != null) {
				value = defaultInstance.getValue(field);
				if(value != null && !value.isEmpty())
					result.put(field, value);
			}
		}
		
		MapBase implemented = UtilsDB.getPossibleImplemented(database, b);
		if(implemented != null)
			buildResponsibilityMapInternal(database, implemented, result);
		
	}

	
	/**
	 * Return the office that is PART_OF this TextChapter
	 * @param database
	 * @return
	 */
	public static Office getTextChaptersPossibleRelatedOffice(Database database, MarkdownToHTMLContent chapter) {
		if(chapter instanceof TextChapter) {
			Collection<Base> partOfOffices = chapter.getRelatedObjects(database, Relation.find(database, Relation.PART_OF));
			if(partOfOffices.size() == 0) {
				return null;
			}
			else if(partOfOffices.size() > 1) {
				System.err.println("Multiple offices defined for TextChapter!");
				return(null);
			} else {
				return (Office)partOfOffices.iterator().next();
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Adds a linked to statement both the map and the doc, and the doc and the map
	 * @param database
	 * @param map
	 */
	public static void linkMap(Database database, ResultAgreementDocuments doc, StrategyMap map) {
		doc.addRelation(Relation.find(database, Relation.LINKED_TO), map);
		map.addRelation(Relation.find(database, Relation.LINKED_TO), doc);
	}
	
	/**
	 * Denies the linked to statement between both the map and the doc, and the doc and the map
	 * @param database
	 * @param map
	 */
	public static void unlinkMap(Database database, ResultAgreementDocuments doc, StrategyMap map) {
		doc.denyRelation(database, Relation.find(database, Relation.LINKED_TO), map);
		map.denyRelation(database, Relation.find(database, Relation.LINKED_TO), doc);
	}
	
	/**
	 * Find the linked map (if there's one)
	 * @param database
	 * @return
	 */
	public static StrategyMap getPossibleLinkedMap(Database database, ResultAgreementDocuments doc) {
		Collection<Base> linkedMaps = doc.getRelatedObjects(database, Relation.find(database, Relation.LINKED_TO));
		if(linkedMaps.size() == 0) {
			return null;
		}
		else if(linkedMaps.size() > 1) {
			System.err.println("Multiple maps linked to ResultAgreementDocument!");
			return(null);
		} else {
			return (StrategyMap)linkedMaps.iterator().next();
		}
	}
	
	public static List<ResultAgreementDocuments> getAllLinkedResultAgreementDocuments(Database database, StrategyMap map){
		Collection<Base> linkedDocs = map.getRelatedObjects(database, Relation.find(database, Relation.LINKED_TO));
		List<ResultAgreementDocuments> result = new ArrayList<>();
		for(Base b : linkedDocs) {
			if(b instanceof ResultAgreementDocuments) {
				ResultAgreementDocuments doc = (ResultAgreementDocuments)b;
				result.add(doc);
			}
		}
		return result;
	}
	
	/**
	 * Find the linked map per year
	 * @param database
	 * @return
	 */
	public static ResultAgreementDocuments getPossibleLinkedResultAgreementDocuments(Database database, StrategyMap map) {
		List<ResultAgreementDocuments> docs = getAllLinkedResultAgreementDocuments(database, map);
		if(docs.size() == 1) {
			return docs.get(0);
		} else {
			System.err.println("Failed to find a single ResultAgreementDocuments object for map " + map.uuid);
			return null;
		}
	}

	
	public static void modifyValidity(Database database, Account account, Base base, String newValidity) {
		Property aika = Property.find(database, Property.AIKAVALI);
		aika.set(true, database, account, base, newValidity);
	}
	
	public static Meter addIndicatorMeter(Database database, Account account,
			MapBase b, Indicator indicator, String year) {
		Relation measures = Relation.find(database, Relation.MEASURES);

		Datatype type = indicator.getDatatype(database);
		Meter.TrafficValuationEnum defaultValuationEnum = UtilsDB.getDefaultTrafficValuationEnum(type);
		
		Meter m = Meter.create(database, b, indicator.getId(database), indicator.getText(database), defaultValuationEnum);
		m.addRelation(measures, indicator);
		
		UtilsDB.modifyValidity(database, account, m, year);
		
		return m;
		
	}
	
	public static Meter.TrafficValuationEnum getDefaultTrafficValuationEnum(Datatype datatype){
		return datatype.getTrafficValuationEnum();
	}
	
	public static String hex(double value) {
		int i = (int)value;
		int upper = i >> 4;
		int lower = i & 15;
		return hexI(upper) + hexI(lower);
	}

	public static String hexI(int i) {
		if(i < 10) return "" + i;
		else {
			char offset = (char) (i-10);
			char c = (char) ('A' + offset);
			return "" + c;
		}
	}
	
	public static String trafficColor(double value) {
		
		double redR = 218.0;
		double redG = 37.0;
		double redB = 29.0;
		
		double yellowR = 244.0;
		double yellowG = 192.0;
		double yellowB = 0.0;

		double greenR = 0.0;
		double greenG = 146.0;
		double greenB = 63.0;
		
		if(value < 0.5) {
			double r = (1-2.0*value)*redR + (2.0*value)*yellowR;
			double g = (1-2.0*value)*redG + (2.0*value)*yellowG;
			double b = (1-2.0*value)*redB + (2.0*value)*yellowB;
			return "#" + hex(r) + hex(g) + hex(b);
		} else {
			double r = (2.0*value - 1)*greenR + (2 - 2.0*value)*yellowR;
			double g = (2.0*value - 1)*greenG + (2 - 2.0*value)*yellowG;
			double b = (2.0*value - 1)*greenB + (2 - 2.0*value)*yellowB;
			return "#" + hex(r) + hex(g) + hex(b);
		}
		
	}
	
	public static String printPath(Database database, MapBase b) {
		StringBuilder result = new StringBuilder();
		StrategyMap map = b.getMap(database);
		result.append(b.getText(database));
		StrategyMap parent = map.getPossibleParent(database);
		while(parent != null) {
			result.append(" / ");
			result.append(parent.getText(database));
			parent = parent.getPossibleParent(database);
		}
		return result.toString();
		
	}

	
	public static Collection<MapBase> getDirectImplementors(Database database, MapBase b) {
		return getDirectImplementors(database, b, Property.AIKAVALI_KAIKKI);
	}

	public static Collection<MapBase> getDirectImplementors(Database database, MapBase b, String requiredValidityPeriod) {
		return getDirectImplementors(database, b, true, requiredValidityPeriod);
	}

	public static Collection<MapBase> getDirectImplementors(Database database, MapBase b, boolean filterMaps, String requiredValidityPeriod) {
		
		ArrayList<MapBase> implementors = new ArrayList<MapBase>(database.getInverse(b, Relation.find(database, Relation.IMPLEMENTS)));
		Collections.sort(implementors);
		
		if(implementors.isEmpty()) return Collections.emptyList();

		TreeMap<Double,MapBase> sorting = new TreeMap<Double,MapBase>();

		StrategyMap map = database.getMap(b);
		
		Property aika = Property.find(database, Property.AIKAVALI);

		for(int i=0;i<implementors.size();i++) {
			
			MapBase b2 = implementors.get(i);
			if(filterMaps && b2 instanceof StrategyMap) continue;
			
			String a = aika.getPropertyValue(b2);
			if(acceptTime(a, requiredValidityPeriod)) {
				StrategyMap child = database.getPossibleMap(b2);
				if(child == null) continue;
				double key = 1.0 / Double.valueOf(i+1);
				for(int j=0;j<map.alikartat.length;j++) {
					Linkki l = map.alikartat[j];
					if(l.uuid.equals(child.uuid))
						key = j+2;
				}
				sorting.put(key, (MapBase)b2);
			}
			
		}
		
		return sorting.values();
		
	}
	
	
	public static boolean acceptTime(String itemValidityPeriod, String requiredValidityPeriod) {

		if (Property.AIKAVALI_KAIKKI.equals(requiredValidityPeriod))
			return true;

		TimeInterval itemInterval = TimeInterval.parse(itemValidityPeriod);
		TimeInterval requiredInterval = TimeInterval.parse(requiredValidityPeriod);
		
		return itemInterval.intersects(requiredInterval);

	}
	
	public static void createProperties(Database database, StrategyMap map, Base b) {
		if (b instanceof OuterBox) {
			for (Pair p : Database.goalProperties(database, map))
				b.properties.add(p);
		} else if (b instanceof InnerBox) {
			for (Pair p : Database.focusProperties(database, map))
				b.properties.add(p);

		}
	}
	
	/**
	 * 
	 * @param plainText
	 * @return
	 */
	public static String hash(String plainText) {
	
    	Formatter formatter = new Formatter();

		try {
	    	
	    	MessageDigest md5  = MessageDigest.getInstance("MD5");
	    	byte[] digest = md5.digest(plainText.getBytes());
	        for (byte b : digest) {
	            formatter.format("%02x", b);
	        }
	        return formatter.toString();
	        
	    } catch (NoSuchAlgorithmException e) {
	    	// Nothing to do
	    	e.printStackTrace();
	    	return null;
	    } finally {
	        formatter.close();
	    }
	    
	}
	
	
	/**
	 * 
	 * @param database
	 * @param account
	 * @param currentTime
	 * @param map
	 * @param goal
	 * @param ref
	 * @return
	 */
	public static InnerBox createInnerBoxCopy(Database database, Account account, String currentTime,
			StrategyMap map, OuterBox goal, Base ref) {
		
		String uuid = UUID.randomUUID().toString();
		InnerBox result = InnerBox.create(database, currentTime, map, goal, uuid, "", null);
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		result.addRelation(Relation.find(database, Relation.COPY), ref);
		
		Property time = Property.find(database, Property.AIKAVALI);
		time.set(false, database, account, result, currentTime);

		return result;
	}
	
	/**
	 * 
	 * @param database
	 * @param account
	 * @param map
	 * @param ref
	 * @param currentTime
	 * @return
	 */
	public static OuterBox createOuterBoxCopy(Database database, Account account,
			StrategyMap map, Base ref, String currentTime) {
		
		String uuid = UUID.randomUUID().toString();
		OuterBox result = OuterBox.create(database, map, uuid, "");
		result.addRelation(Relation.find(database, Relation.IMPLEMENTS), ref);
		result.addRelation(Relation.find(database, Relation.COPY), ref);

		Property time = Property.find(database, Property.AIKAVALI);
		time.set(false, database, account, result, currentTime);

		return result;

	}
	
	public static boolean doesImplement(Database db, Base b, Base target) {
		return doesImplement(db, b, target, new HashMap<Base, Boolean>());
	}

	private static boolean doesImplement(Database db, Base b, Base target, Map<Base,Boolean> cache) {
		Boolean c = cache.get(b);
		if(c != null) return c;
		if(b.equals(target)) {
			cache.put(b, true);
			return true;
		}
		Relation implementsRelation = Relation.find(db, Relation.IMPLEMENTS);
		for(Base b2 : b.getRelatedObjects(db, implementsRelation)) {
			if(doesImplement(db, b2, target, cache)) {
				cache.put(b, true);
				return true;
			}
		}
		cache.put(b, false);
		return false;
	}
	
	public static Set<MapBase> getImplementors(Database db, MapBase target, Map<Base,Boolean> cache) {
		Set<MapBase> result = new HashSet<MapBase>();
		for(MapBase b : db.enumerateMapBase()) {
			if(doesImplement(db, b, target, cache))
				result.add(b);
		}
		return result;
	}
	
	public static List<String> excelRow(String ... cells) {
		ArrayList<String> result = new ArrayList<String>();
		for(String s : cells) result.add(s);
		return result;
	}
	
	public static Set<AccountGroup> getAllAssociatedAccountGroups(Database database, Account account){
		Office office = UtilsDB.getPossibleAssociatedOfficeFromAccount(database, account);
		if(office == null) {
			return account.getAccountGroups(database);
		}
		else {
			AccountGroup group = UtilsDB.getPossibleAccountGroupByOffice(database, office);
			if(group == null) {
				return account.getAccountGroups(database);
			} else {
				Set<AccountGroup> groups = account.getAccountGroups(database);
				groups.add(group);
				return groups;
			}
		}
		
	}
	
	
	public static boolean canRead(Database database, Account account, Base b) {
		for(AccountGroup group : UtilsDB.getAllAssociatedAccountGroups(database, account)) {
			if(group.canRead(database, b)) return true;
		}
		return false;
	}

	/**
	 * Check if the specific account can read the Base b
	 * @param database
	 * @param account
	 * @param b
	 * @return
	 */
	public static boolean canWrite(Database database, Account account, Base b) {
		if(account == null) {
			System.err.println("Account is null in canWrite! Null account cannot write to any Base");
			return false;
		}
		
		for(AccountGroup group : UtilsDB.getAllAssociatedAccountGroups(database, account)) {
			if(group.canWrite(database, b)) return true;
		}
		return false;
	}
	
	/**
	 * Find the account group for the office, if it exists.
	 * @param database
	 * @param office
	 * @return
	 */
	public static AccountGroup getPossibleAccountGroupByOffice(Database database, Office office) {
		for(AccountGroup group : AccountGroup.enumerate(database)) {
			Office o = getPossibleAssociatedOfficeFromAccountGroup(database, group);
			if(o != null && o.uuid.equals(office.uuid)) {
				return group;
			}
		}
		return null;
	}
	
	public static void disassociateAccountFromAllOffices(Database database, Account account){
		account.denyRelation(database, Relation.find(database, Relation.FROM_OFFICE));
	}
	
	public static void associateAccountWithOffice(Database database, Office office, Account account) {
		account.denyRelation(database, Relation.find(database, Relation.FROM_OFFICE));
		account.addRelation(Relation.find(database, Relation.FROM_OFFICE), office);
	}
	
	public static void associateAccountGroupWithOffice(Database database, Office office, AccountGroup group) {
		group.denyRelation(database, Relation.find(database, Relation.FROM_OFFICE));
		group.addRelation(Relation.find(database, Relation.FROM_OFFICE), office);
	}
	
	public static Office getPossibleAssociatedOfficeFromAccountGroup(Database database, AccountGroup group){
		return getPossibleAssociatedOffice(database, (Base)group);
	}
	
	public static Office getPossibleAssociatedOfficeFromAccount(Database database, Account account){
		return getPossibleAssociatedOffice(database, (Base)account);
	}
	
	/**
	 * A Base object can be associated with an Office with the FROM_OFFICE relation. Only 1 or 0 offices can be associated to a single Base object.
	 * @param database
	 * @param b
	 * @return
	 */
	public static Office getPossibleAssociatedOffice(Database database, Base b){
		Collection<Base> bases = b.getRelatedObjects(database, Relation.find(database, Relation.FROM_OFFICE));
		if(bases.size() == 0) {
			return null;
		}
		else if(bases.size() == 1) {
			return (Office)bases.iterator().next();
		}
		else {
			System.err.println("Multiple offices match the base " + b.uuid);
			return null;
		}
	}
	
	/**
	 * 
	 * @param database
	 * @param office
	 */
	public static void removeAllAssociatedObjectsByOffice(Database database, Office office) {
		StrategyMap map = database.getRoot();
		String t = office.getText(database);
		if(map != null) {
			List<ResultAgreementDocuments> docs = UtilsDB.getAllLinkedResultAgreementDocuments(database, map);
			for(ResultAgreementDocuments doc : docs) {
				System.out.println("Purging ResultAgreementDocument with ID: " + doc.id + " chapters associated with office " + t);
				doc.denyAllChaptersForOffice(database, office);
			}
		}
		
		AccountGroup group = getPossibleAccountGroupByOffice(database, office);
		if(group != null) {
			System.out.println("Purging AccountGroup: " + group.id + " associated with office " + t);
			group.remove(database);
		}
		
		//Purge all ChangeSuggestions made by this Office:
		List<ChangeSuggestion> allChangeSuggestions = ChangeSuggestion.enumerate(database);
		for(ChangeSuggestion cs : allChangeSuggestions) {
			Office associatedOffice = UtilsDB.getPossibleAssociatedOffice(database, cs);
			if(associatedOffice != null && associatedOffice.uuid.equals(office.uuid)) {
				System.out.println("Purging ChangeSuggestion: " + cs.text + " associated with office " + t);
				cs.remove(database);
			}
		}
		
		List<Comment> comments = Comment.enumerate(database);
		for(Comment c: comments) {
			Office associatedOffice = UtilsDB.getPossibleAssociatedOffice(database, c);
			if(associatedOffice != null && associatedOffice.uuid.equals(office.uuid)) {
				System.out.println("Purging Comment: " + c.text + " associated with office " + t);
				c.remove(database);
			}
		}
	}
	
	public static boolean addReadRightToOffice(Database database, Base b, Office office) {
		boolean ok = false;
		AccountGroup group = UtilsDB.getPossibleAccountGroupByOffice(database, office);
		if(group != null) {
			if(!group.canRead(database, b)) {
				group.addRightIfMissing(new AccessRight(b, false));
				ok = true;
			} else {
				//System.err.println("Office " + office.text + " already had read access to " + b.uuid);
				return false;
			}
		}
		return ok;
	}
	

	/**
	 * Sort your base objects according to their creation time.
	 * @param unsorted
	 * @param reverse
	 */
	public static void sortBaseByCreationTime(List<Base> unsorted, boolean reverse) {
		Collections.sort(unsorted, new Comparator<Base>(){
		    public int compare(Base o1, Base o2){
		        if(o1.creationTime == o2.creationTime)
		             return 0;
		 		int smaller = -1;
				int higher = 1;
				if(reverse) {
					smaller = 1;
					higher = -1;
				}
		        return o1.creationTime < o2.creationTime ? smaller : higher;
		    }
		});
	}
	
	/**
	 * Remove the AccountGroup and deny all relations to this object from the DB
	 * @param database
	 * @param selectedGroup
	 */
	public static void removeAccountGroupAndAssociations(Database database, AccountGroup selectedGroup) {
		selectedGroup.remove(database);
	}

	public static String baseToAccessRightString(Database database, Base b) {
		if(b == null) return "tyhjä elementti";
		String typeId = b.clientIdentity();
		String text = b.getText(database);
		if(text == null) text = "-";
		else if(text.length() > 44) text = text.substring(0, 44) + "...";
		return "("+typeId+") " + text;
	}
	
	public static boolean isLeaf(Database database, UIState uistate, MapBase b) {
		return b.isLeaf(database, uistate.getTime());
	}

	
	public static boolean isActive(Database database, String ref, Base b) {
		Property aika = Property.find(database, Property.AIKAVALI);
		String a = aika.getPropertyValue(b);
		if(a != null) {
			if(UtilsDB.acceptTime(a, ref))
				return true;
		} else {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param database
	 * @param base
	 * @param basetime
	 * @return
	 */
	public static List<String> allAllowedYearsForBase(Database database, MapBase base, String basetime){
		List<String> years = new ArrayList<>();
		Base owner = base.getOwner(database);
		if(owner != null) {
			if(owner instanceof MapBase) {
				MapBase o = (MapBase)owner;
				String parentTime = (Property.find(database, Property.AIKAVALI)).getPropertyValue(o);
				if(parentTime != null) {
					if(parentTime.equals(Property.AIKAVALI_KAIKKI)) {
						return allAllowedYears(database, basetime);
					} else {
						years.add(parentTime);
					}
				}
			} else {
				System.err.println("Expected MapBase parent to base " + base.getText(database));
			}
			
		}
		Collections.sort(years);
		return years;
	}
	
	/**
	 * 
	 * @param database
	 * @param basetime
	 * @return
	 */
	public static List<String> allAllowedYears(Database database, String basetime) {
		List<String> years = new ArrayList<>();
		years.add(Property.AIKAVALI_KAIKKI);
		
		TimeConfiguration tc = TimeConfiguration.getInstance(database);
		TimeInterval ti = TimeInterval.parse(tc.getRange());
		
		for(int year : ti.years()) {
			years.add(Integer.toString(year));
		}
		
		if(! years.contains(basetime)) {
			years.add(basetime);
		}
		Collections.sort(years);
		return years;
	}
	
}
