package com.axonactive.listener;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Listener {
	public static ArrayList<String> pendingFiles;
	private final WatchService watchService;
	private final Map<WatchKey, Path> keys;
	private static final String SCAN_FILE_PATH = "\\\\hcmc-fsr\\Teams\\JuniorClass\\Legion\\ScanFile\\";

	public Listener() throws IOException {
		this.watchService = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
		Path directory = Paths.get(SCAN_FILE_PATH);
		registerDirectoryAndSubDirectory(directory);
	}

	private void registerDirectory(Path path) throws IOException {
		WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
		keys.put(watchKey, path);
	}

	private void registerDirectoryAndSubDirectory(final Path startPath) throws IOException {
		Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) throws IOException {
				registerDirectory(directory);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Process all events for keys queued to the watchService
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public void processEvents() throws InterruptedException {
		pendingFiles = new ArrayList<>();
		String fileName = "";

		System.out.println("Listening...");
		while (true) {
			WatchKey watchKey;
			try {
				watchKey = this.watchService.take();
			} catch (InterruptedException e) {
				System.out.println("Interrupted Watch Service");
				return;
			}

			Path dir = keys.get(watchKey);

			if (dir == null) {
				System.out.println("Null WatchKey");
				continue;
			}

			Thread.sleep(50);

			for (WatchEvent<?> event : watchKey.pollEvents()) {
				@SuppressWarnings("rawtypes")
				WatchEvent.Kind kind = event.kind();

				@SuppressWarnings("unchecked")
				Path name = ((WatchEvent<Path>) event).context();
				Path child = dir.resolve(name);

				fileName = child.toString();
				if (!isElementExists(fileName)) {
					pendingFiles.add(fileName);
				}

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					try {
						if (Files.isDirectory(child)) {
							registerDirectoryAndSubDirectory(child);
						}
					} catch (IOException x) {
						System.out.println("Create error");
					}
				}
			}

			boolean valid = watchKey.reset();
			if (!valid) {
				keys.remove(watchKey);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	private static boolean isElementExists(String fileName) {
		if (pendingFiles != null) {
			if (pendingFiles.contains(fileName)) {
				return true;
			}
		}
		return false;
	}
}