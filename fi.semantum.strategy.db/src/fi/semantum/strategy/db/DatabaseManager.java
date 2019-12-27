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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class DatabaseManager {
	
	private static DatabaseManager INSTANCE;
	
	private Map<String,Date> backups = new HashMap<String,Date>();
	
	public static DatabaseManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new DatabaseManager();
		}
		return INSTANCE;
	}
	
	public static boolean isDatabase(String id) {
		File dir = Database.getDatabaseDirectory(id);
		return dir.exists();
	}
	
	public static void copyDatabase(String source, String target) {
		
		File targetDir = Database.getDatabaseDirectory(target);
		targetDir.mkdirs();
		
		try {
			
			java.nio.file.Files.copy( 
					Database.getDatabaseFile(source).toPath(), 
					Database.getDatabaseFile(target).toPath(),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING,
					java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
					java.nio.file.LinkOption.NOFOLLOW_LINKS );

		} catch (IOException e) {

			e.printStackTrace();

		}

	}
	
	/*
	 * Backup scheme
	 * -Main directory contains changes from today with 5min throttle
	 * -'hourly*' directory contains 2 days of history one per hour
	 * -'daily*' directory contains infinite history, last change per day
	 * 
	 */
	
	void reorganizeBackups(Database database, Date nowDate) {
		
		synchronized(database.getDatabaseId().intern()) {
			/*
			 * Possibly move snapshots to hourly directory 
			 */
			Date last = backups.get(database.getDatabaseId());
			if(last == null || !isSameDay(nowDate, last)) {
				if(reorganizeBackupsHourly(database, nowDate)) {
					/*
					 * Possibly move hourlies to dailies 
					 */
					reorganizeBackupsDaily(database, nowDate);
				}
				backups.put(database.getDatabaseId(), nowDate);
			}
		}
		
	}
	
	private Date yesterday(Date nowDate) {
		return new Date(nowDate.getTime() - 24*60*60*1000);
	}
	
	private boolean isSameDay(Date a, Date b) {
		LocalDate lA = a.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate lB = b.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		boolean newV = lA.getYear() == lB.getYear() && lA.getMonth() == lB.getMonth() && lA.getDayOfMonth() == lB.getDayOfMonth();
		return newV;
	}
	
	public static Date hourlyDirectoryDate(Path path) {
		return fileDate(path, UtilsDB.dailyDateFormat, Database.HOURLY_DIRECTORY_PREFIX);
	}

	public static Date backupFileDate(Path path) {
		return fileDate(path, UtilsDB.backupDateFormat, Database.BACKUP_FILE_PREFIX);
	}
	
	public static Date fileDate(Path path, SimpleDateFormat format, String prefix) {
		
		String fileName = path.toFile().getName();
		
		if(!fileName.startsWith(prefix))
			return null;

		String suffix = fileName.substring(prefix.length());
		
		try {
			return format.parse(suffix);
		} catch (ParseException e) {
			return null;
		}
		
	}
	
	/**
	 * 
	 * @param databasePath -> database.getDatabaseDirectory().toPath()
	 * @return
	 */
	public static List<Path> collectHourlyDirectories(Path databasePath) {

		ArrayList<Path> dirNames = new ArrayList<Path>();
		try (Stream<Path> dirs = java.nio.file.Files.list(databasePath)) {
			dirs.forEach(dirPath -> {
				if(dirPath.toFile().isDirectory()) {
					String fileName = dirPath.toFile().getName();
					if(fileName.startsWith(Database.HOURLY_DIRECTORY_PREFIX)) {
						dirNames.add(dirPath);
					}
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
		
		Collections.reverse(dirNames);
		
		return dirNames;

	}

	public static Collection<Path> collectBackupFiles(Path directory) {
		
		ArrayList<Path> fileNames = new ArrayList<Path>();
		try (Stream<Path> files = java.nio.file.Files.list(directory)) {
			files.forEach(filePath -> {
				String fileName = filePath.toFile().getName();
				if(fileName.startsWith(Database.BACKUP_FILE_PREFIX)) {
					fileNames.add(filePath);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
		}
		
		return fileNames;

	}
	

	private void reorganizeBackupsDaily(Database database, Date nowDate) {

		try {
			
			Date threeDaysAgo = yesterday(yesterday(yesterday(nowDate)));
			
			ArrayList<Path> directoriesToRemove = new ArrayList<Path>();
			ArrayList<Path> filesToMove = new ArrayList<Path>();

			Collection<Path> dirNames = collectHourlyDirectories(database.getDatabaseDirectory().toPath());
			
			for(Path dirPath : dirNames) {
				
				Date d = hourlyDirectoryDate(dirPath);
				if(d != null) {

					if(d.before(threeDaysAgo)) {

						Collection<Path> fileNames = collectBackupFiles(dirPath);

						if(!fileNames.isEmpty()) {

							TreeMap<Date, Path> paths = new TreeMap<Date, Path>();
							for(Path hourlyPath : fileNames) {
								Date d2 = backupFileDate(hourlyPath);
								paths.put(d2, hourlyPath);
							}

							filesToMove.add(paths.lastEntry().getValue());

						}

						directoriesToRemove.add(dirPath);
						
					}
					
				}
			}
			
			if(!filesToMove.isEmpty()) {
				
				File dailies = database.getBackupDirectory("dailies");
				dailies.mkdirs();
				
				for(Path f : filesToMove) {
					Path target = dailies.toPath().resolve(f.getParent().relativize(f));
					System.err.println("move to dailies: " + target.toFile().getAbsolutePath());
					try {
						java.nio.file.Files.move( f, target,
								java.nio.file.StandardCopyOption.REPLACE_EXISTING);
					} finally {
					}
				}
				
			}
			
			ArrayList<Path> pathsToDelete = new ArrayList<Path>();

			for(Path dir : directoriesToRemove) {
				
				try (Stream<Path> walk  = java.nio.file.Files.walk(dir)) {
				    walk.sorted(Comparator.reverseOrder()).forEach(p -> pathsToDelete.add(p));
				}
				
			}
			
			for(Path p : pathsToDelete) {
				System.err.println("delete " + p.toFile().getAbsolutePath());
				try {
					
					java.nio.file.Files.delete(p);
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			}

		} catch (IOException e) {

		}

	}

	private boolean reorganizeBackupsHourly(Database database, Date nowDate) {
		
		boolean newHourliesCreated = false;
		
		Date yesterday = yesterday(nowDate);
		String hourlyDirectory = Database.HOURLY_DIRECTORY_PREFIX + UtilsDB.dailyDateFormat.format(yesterday);
		File hourlyYesterday = database.getBackupDirectory(hourlyDirectory);
		
		System.err.println("hourlyYesterday " + hourlyYesterday.getName());
		
		if(hourlyYesterday.exists()) {
			
			// OK we can now be sure that backups have been moved - can exit
			return false;
			
		} else {
		
			try {
				
				ArrayList<Path> removes = new ArrayList<Path>();
				TreeMap<Long, Path> filesToMove = new TreeMap<Long,Path>();
				
				Collection<Path> files = collectBackupFiles(database.getDatabaseDirectory().toPath());
				for(Path filePath : files) {
					Date d = backupFileDate(filePath);
					if(d != null) {
						if(isSameDay(yesterday, d)) {
							filesToMove.put(-d.getTime(), filePath);
						} else if (isSameDay(nowDate, d)) {
							// Do nothing!
						} else {
							removes.add(filePath);
						}
					}
				}
				
				ArrayList<Path> finalFiles = new ArrayList<Path>();
				int targetHour = 23;
				for(Map.Entry<Long, Path> entry : filesToMove.entrySet()) {
					Date d = new Date(entry.getKey());
					if(d.getHours() <= targetHour) {
						finalFiles.add(entry.getValue());
						targetHour = d.getHours() - 1;
					}
				}
				
				if(!finalFiles.isEmpty()) {
					hourlyYesterday.mkdirs();
					newHourliesCreated = true;
					for(Path f : finalFiles) {
						Path rel = f.getParent().relativize(f);
						System.err.println("move to hourlies: " + hourlyYesterday.toPath().resolve(rel).toFile().getAbsolutePath());
						try {
							java.nio.file.Files.move( f, hourlyYesterday.toPath().resolve(rel),
									java.nio.file.StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
						}
					}
				}
				
				for(Path p : removes) {
					System.err.println("Removing invalid database file '" + p.toFile().getName() + "'");
					try {
						java.nio.file.Files.delete(p);
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
					}
				}
				
			
			} finally {
				
			}
			
		}
		
		return newHourliesCreated;
		
	}
	
	
}
