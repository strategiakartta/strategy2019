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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;

import org.apache.commons.io.IOUtils;

public class PhantomJSDriver {

	private final static String printHtmlLocalPath = "/print.html";
	private final static String printJSLocalPath = "/print.js";
	
	//TODO: NullPointer - no such file, make sure white-space is treated right and the correct file is found
	public static String printHtml(String svgText, String csspath) {

		try {
			//File f = Main.getAppFile(printHtmlLocalPath);
			File f = new File("./WebContent/print.html");
			String template = new String(Files.readAllBytes(f.toPath()));
			template = template.replace("%%svg", svgText);
			template = template.replace("%%csspath", csspath);
			return template;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	//TODO: This probably doesn't find the print.js file either
	public static String printCommand(String url, String outFile) {

		try {
			//File f = Main.getAppFile(printJSLocalPath);
			File f = new File("./WebContent/print.js");
			String template = new String(Files.readAllBytes(f.toPath()));
			template = template.replace("%%url", url);
			template = template.replace("%%file", outFile.replace("\\", "/"));
			return template;
		} catch (IOException e) {
			return null;
		}
		
	}
	
	public enum ARCHType {
	    X86, X86_64, UNKNOWN;

	    public static ARCHType calculate() {
	        String osArch = System.getProperty("os.arch");
	        assert osArch != null;
	        osArch = osArch.toLowerCase();
	        if (osArch.equals("i386") || osArch.equals("i586") || osArch.equals("i686") || osArch.equals("x86"))
	            return X86;
	        if (osArch.startsWith("amd64") || osArch.startsWith("x86_64"))
	            return X86_64;
	        return UNKNOWN;
	    }
	}
	
	public enum OSType {
	    LINUX, WINDOWS, UNKNOWN;

	    public static OSType calculate() {
	        String osName = System.getProperty("os.name");
	        assert osName != null;
	        osName = osName.toLowerCase();
	        if (osName.startsWith("windows"))
	            return WINDOWS;
	        if (osName.startsWith("linux"))
	            return LINUX;
	        return UNKNOWN;
	    }
	}
	
	 public static String formOsArchSuffix() {
	        String osName = System.getProperty("os.name");
	        String osArch = System.getProperty("os.arch");
	        OSType os = OSType.calculate();
	        ARCHType arch = ARCHType.calculate();

	        if (os == OSType.UNKNOWN)
	            throw new UnsatisfiedLinkError("unknown OS '" + osName + "' cannot load native fastlz library");
	        if (arch == ARCHType.UNKNOWN)
	            throw new UnsatisfiedLinkError("unknown architecture '" + osArch + "' cannot load native fastlz library");

	        String lib = "";
	        switch (os) {
	            case LINUX:
	                lib += "-linux";
	                switch (arch) {
	                    case X86:
	                        lib += "-x86";
	                        break;
	                    case X86_64:
	                        lib += "-x86_64";
	                        break;
	                    default:
	                        throw new UnsatisfiedLinkError("Unsupported architecture for Linux OS: " + osArch);
	                }
	                break;
	            case WINDOWS:
	                lib += "-windows";
	                switch (arch) {
	                    case X86:
	                        lib += "-x86.exe";
	                        break;
	                    case X86_64:
	                        lib += "-x86_64.exe";
	                        break;
	                    default:
	                        throw new UnsatisfiedLinkError("Unsupported architecture for Windows: " + osArch);
	                }
	                break;
	            default:
	                throw new UnsatisfiedLinkError("Unsupported operating system: " + os);
	        }
	        return lib;
	    }
	 
	public static boolean execute(File javascript) {
		
		try {
			
			//TODO: Ensure phantomjs exe is found
	        File filePath = DatabaseLoader.bootstrapFile("phantomjs" + formOsArchSuffix());

	        //TODO: Ensure operation is supported
	        if(OSType.LINUX.equals(OSType.calculate()))
	        	Files.setPosixFilePermissions(filePath.toPath(), EnumSet.of(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE, PosixFilePermission.OWNER_EXECUTE));

			String[] args = { filePath.getAbsolutePath(),
					javascript.getAbsolutePath()					
			};
			Process process = new ProcessBuilder(args).start();
			try {
				InputStream input = process.getInputStream();
				InputStreamReader reader = new InputStreamReader(input);
				StringBuilder sb = new StringBuilder();
				while (true) {
					try {
						while (reader.ready()) {
							int r = reader.read();
							if (r != -1) {
								char c = (char)r;
								if ((c != '\r') && (c != '\n')) {
									sb.append(c);
								} else if (c == '\n') {
									sb.setLength(0);
								}
							} else {
							}
						}
						
						int error = process.exitValue();
						
						sb.append(IOUtils.toString(input));
						
						reader.close();
						return error == 0;
						
					} catch (IllegalThreadStateException e) {
						Thread.sleep(100);
					}
				}
			} finally {
				process.destroy();
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;

	}
	
}
