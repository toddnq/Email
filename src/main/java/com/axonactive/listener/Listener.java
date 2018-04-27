package com.axonactive.listener;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;

public class Listener {

	public static ArrayList<String> pendingFiles;
	private static String CEO_PATH_FILE = "\\\\hcmc-fsr\\Teams\\JuniorClass\\Legion\\ScanFile\\CEO";
	private static String CIO_PATH_FILE = "\\\\hcmc-fsr\\Teams\\JuniorClass\\Legion\\ScanFile\\CIO";
	private static String CTO_PATH_FILE = "\\\\hcmc-fsr\\Teams\\JuniorClass\\Legion\\ScanFile\\CTO";

	public static void watchMyFolder() {
		Path ceoDir = new File(CEO_PATH_FILE).toPath();
		Path cioDir = new File(CIO_PATH_FILE).toPath();
		Path ctoDir = new File(CTO_PATH_FILE).toPath();
		System.out.println("Listening...");
		pendingFiles = new ArrayList<>();
		String fileName = "";

		try {
			if (!isFolder(ceoDir)) {
				throw new IllegalArgumentException("Path: " + ceoDir + " is not a folder");
			}

			if (!isFolder(cioDir)) {
				throw new IllegalArgumentException("Path: " + cioDir + " is not a folder");
			}

			if (!isFolder(ctoDir)) {
				throw new IllegalArgumentException("Path: " + ctoDir + " is not a folder");
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// System.out.println("Watching path: " + myDir);

		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			ceoDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
			cioDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
			ctoDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

			while (true) {

				WatchKey watckKey = watcher.take();
				Thread.sleep(50);
				List<WatchEvent<?>> events = watckKey.pollEvents();

				for (WatchEvent event : events) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						// System.out.println("Created: " + event.context().toString());
						fileName = event.context().toString();
						if (!isElementExists(fileName)) {
							pendingFiles.add(fileName);
							System.out.println(pendingFiles.get(pendingFiles.size() - 1));
						}

					}
				}
				events.clear();
				watckKey.reset();
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}

	}

	private static boolean isFolder(Path directory) throws IOException {
		return (Boolean) Files.getAttribute(directory, "basic:isDirectory", NOFOLLOW_LINKS);
	}

	private static boolean isElementExists(String fileName) {
		if (pendingFiles != null) {
			if (pendingFiles.contains(fileName)) {
				// System.out.println("last index: " +pendingFiles.get(pendingFiles.size()-1));
				// System.out.println("check file name: " +fileName);

				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		watchMyFolder();
	}
}
