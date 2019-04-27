package net.quantium.modrequire.configuration.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

public class DirectoryWatcher implements IWatcher {
	private volatile boolean accepting = true;
	
	private final Runnable callback;
	private final WatchService watcher;
	
	public DirectoryWatcher(Path path, Runnable callback, Kind<?>... kinds) throws IOException {
		Preconditions.checkNotNull(path);
		Preconditions.checkNotNull(callback);
		
		this.callback = callback;
		this.watcher = FileSystems.getDefault().newWatchService();
		
		path.register(watcher, kinds);
		
		Thread thread = new Thread(this::watch);
		thread.start();
	}
	
	public void setAccept(boolean accept) {
		this.accepting = accept;
	}
	
	private void watch() {
		 while (true) {
			 WatchKey key = null;
             try { key = watcher.poll(500, TimeUnit.MILLISECONDS); }
             catch (InterruptedException e) { return; }
             if (key == null) { Thread.yield(); continue; }

             for (WatchEvent<?> event : key.pollEvents()) {
                 WatchEvent.Kind<?> kind = event.kind();

                 @SuppressWarnings("unchecked")
                 WatchEvent<Path> ev = (WatchEvent<Path>) event;
                 Path filename = ev.context();

                 if (kind == StandardWatchEventKinds.OVERFLOW) {
                     Thread.yield();
                     continue;
                 } else if(this.accepting) {
                     this.callback.run();
                 }
                 boolean valid = key.reset();
                 if (!valid) { break; }
             }
             Thread.yield();
         }
	}
}
