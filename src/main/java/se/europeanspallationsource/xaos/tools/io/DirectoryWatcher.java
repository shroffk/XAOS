/*
 * Copyright 2018 claudiorosati.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.xaos.tools.io;


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import org.reactfx.EventSource;
import org.reactfx.EventStream;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


/**
 * Watches for changes in files and directories.
 * <p>
 * Usage:
 * </p>
 * <pre>
 *   ExecutorService executor = Executors.newSingleThreadExecutor();
 *   DirectoryWatcher watcher = create(executor);
 *
 *   watcher.getSignalledKeysStream().subscribe(key -&gt; {
 *     key.pollEvents().stream().forEach(e -&gt; {
 *       if ( StandardWatchEventKinds.ENTRY_CREATE.equals(e.kind()) ) {
 *         ...
 *       } else if ( StandardWatchEventKinds.ENTRY_DELETE.equals(e.kind()) ) {
 *         ...
 *       } else if ( StandardWatchEventKinds.ENTRY_MODIFY.equals(e.kind()) ) {
 *         ...
 *       }
 *     });
 *     key.reset();
 *   });
 *
 *   Path root = ...
 *
 *   watcher.watch(root);</pre>
 * <p>
 * <b>Note:</b> don't forget to call {@code key.reset()} on the event handling
 * code. This step is critical if you want to receive further watch events.
 * </p>
 *
 * @author claudio.rosati@esss.se
 * @see <a href="https://github.com/ESSICS/LiveDirsFX">LiveDirsFX</a>
 */
@SuppressWarnings( "ClassWithoutLogger" )
public class DirectoryWatcher {

	/**
	 * Creates a {@link DirectoryWatcher} instance.
	 *
	 * @param eventThreadExecutor The {@link Executor} used to queue I/O events.
	 * @return A newly created {@link DirectoryWatcher} instance.
	 * @throws IOException If an I/O error occurs.
	 */
	public static DirectoryWatcher create( Executor eventThreadExecutor ) throws IOException {
		return new DirectoryWatcher(eventThreadExecutor);
	}

	private final EventSource<Throwable> errors = new EventSource<>();
	private final Executor eventThreadExecutor;
	private final LinkedBlockingQueue<Runnable> executorQueue = new LinkedBlockingQueue<>();
	private boolean interrupted = false;
	private final Thread ioThread;
	private boolean mayInterrupt = false;
	private volatile boolean shutdown = false;
	private final EventSource<WatchKey> signalledKeys = new EventSource<>();
	private final WatchService watcher;

	protected DirectoryWatcher( Executor eventThreadExecutor ) throws IOException {

		this.watcher = FileSystems.getDefault().newWatchService();
		this.ioThread = new Thread(this::ioLoop, "DirectoryWatcherIO");
		this.eventThreadExecutor = eventThreadExecutor;

		startIOThread();

	}

	/**
	 * Create a new directory by creating all nonexistent parent directories
	 * first. One of the two given {@link Consumer}s will be called on success
	 * or on failure.
	 *
	 * @param dir       The pathname of the file to be created.
	 * @param onSuccess The {@link Consumer} called on success, where the passed
	 *                  parameter is the new directory {@link Path}.
	 * @param onError   The {@link Consumer} called on failure.
	 * @param attrs     An optional list of file attributes to set atomically
	 *                  when creating the directory.
	 */
	public void createDirectories(
		Path dir,
		Consumer<Path> onSuccess,
		Consumer<Throwable> onError,
		FileAttribute<?>... attrs
	) {
		executeIOOperation(() -> Files.createDirectories(dir, attrs), onSuccess, onError);
	}

	/**
	 * Create a new directory using the given path. One of the two given
	 * {@link Consumer}s will be called on success or on failure.
	 * <p>
	 * The {@link #createDirectories createDirectories} method should be used
	 * where it is required to create all nonexistent parent directories first.
	 * </p>
	 *
	 * @param dir       The pathname of the file to be created.
	 * @param onSuccess The {@link Consumer} called on success, where the passed
	 *                  parameter is the new directory {@link Path}.
	 * @param onError   The {@link Consumer} called on failure.
	 * @param attrs     An optional list of file attributes to set atomically
	 *                  when creating the directory.
	 */
	public void createDirectory(
		Path dir,
		Consumer<Path> onSuccess,
		Consumer<Throwable> onError,
		FileAttribute<?>... attrs
	) {
		executeIOOperation(() -> Files.createDirectory(dir, attrs), onSuccess, onError);
	}

	/**
	 * Create a new file using the given path. One of the two given
	 * {@link Consumer}s will be called on success or on failure.
	 * <p>
	 * {@link Files#createFile(java.nio.file.Path, java.nio.file.attribute.FileAttribute...)}
	 * will be called to actually create the file.
	 * </p><p>
	 * <b>Note:</b> the operation is executed in by the {@link Executor} passed
	 * to the {@link #create(java.util.concurrent.Executor)} method, i.e. in a
	 * different thread from the caller's one.
	 * </p>
	 *
	 * @param file      The pathname of the file to be created.
	 * @param onSuccess The {@link Consumer} called on success, where the passed
	 *                  parameter is the new file timestamp.
	 * @param onError   The {@link Consumer} called on failure.
	 * @param attrs     An optional list of file attributes to set atomically
	 *                  when creating the file.
	 */
	public void createFile(
		Path file,
		Consumer<FileTime> onSuccess,
		Consumer<Throwable> onError,
		FileAttribute<?>... attrs
	) {
		executeIOOperation(
			() -> {

				Files.createFile(file, attrs);

				return Files.getLastModifiedTime(file);

			},
			onSuccess,
			onError
		);
	}

	/**
	 * @return The {@link EventStream} of thrown errors.
	 */
	public EventStream<Throwable> errorsStream() {
		return errors;
	}

	/**
	 * Returns {@code true} if this watcher was shutdown, meaning that no elements
	 * will be posted to the errors and signalled keys streams.
	 *
	 * @return {@code true} if this watcher was shutdown,
	 */
	public final boolean isShutdown() {
		return shutdown;
	}

	/**
	 * Shutdown this watcher.
	 * <p>
	 * <b>Note:</b> this method will not shutdown the event thread {@link Executor}
	 * used to create this watcher.
	 * </p>
	 */
	public final void shutdown() {

		shutdown = true;

		interrupt();

	}

	/**
	 * @return The {@link EventStream} of signalled {@link WatchKey}s.
	 */
	public EventStream<WatchKey> signalledKeysStream() {
		return signalledKeys;
	}

	/**
	 * Returns a {@link CompletionStage} containing the tree structure of
	 * {@link PathElement} instances representing the content of the given
	 * {@code root} directory. The returned stage is completed exceptionally in
	 * case an I/O error occurs.
	 *
	 * @param root The pathname of the tree's root directory.
	 * @return A {@link CompletionStage} containing the tree structure of
	 *         {@link PathElement} instances rooted at the given {@link Path},
	 *         or completed exceptionally if an I/o error occurred.
	 */
	public CompletableFuture<PathElement> tree( Path root ) {

		CompletableFuture<PathElement> future = new CompletableFuture<>();

		executeOnIOThread(() -> {
			try {
				future.complete(PathElement.tree(root));
			} catch ( IOException e ) {
				future.completeExceptionally(e);
			}
		});

		return future;

	}

	/**
	 * Watches the given directory for entry create, delete, and modify events.
	 *
	 * @param dir The directory to be watched.
	 * @throws IOException If an I/O error occurs.
	 */
	public void watch( Path dir ) throws IOException {
		dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	}

	/**
	 * Watches the given directory for entry create, delete, and modify events.
	 * If an I/O error occurs, the exception is logged (emitted).
	 *
	 * @param dir The directory to be watched.
	 */
	public void watchOrStreamError( Path dir ) {
		try {
			watch(dir);
		} catch ( IOException e ) {
			emitError(e);
		}
	}

	private void emitError( Throwable e ) {
		executeOnEventThread(() -> errors.push(e));
	}

	private void emitKey( WatchKey key ) {
		executeOnEventThread(() -> signalledKeys.push(key));
	}

	@SuppressWarnings( { "UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch" } )
	private <T> void executeIOOperation( Callable<T> operation, Consumer<T> onSuccess, Consumer<Throwable> onError ) {
		executeOnIOThread(() -> {
			try {

				T result = operation.call();

				executeOnEventThread(() -> onSuccess.accept(result));

			} catch ( Throwable t ) {
				executeOnEventThread(() -> onError.accept(t));
			}
		});
	}

	private void executeOnEventThread( Runnable task ) {
		eventThreadExecutor.execute(task);
	}

	private void executeOnIOThread( Runnable action ) throws RejectedExecutionException {
		if ( !isShutdown() ) {
			executorQueue.add(action);
			interrupt();
		} else {
			throw new RejectedExecutionException("Directory watcher is shutdown.");
		}
	}

	private synchronized void interrupt() {
		if ( mayInterrupt ) {
			ioThread.interrupt();
		} else {
			interrupted = true;
		}
	}

	private void ioLoop() {
		while ( true ) {

			WatchKey key = take();

			if ( key != null ) {
				emitKey(key);
			} else if ( isShutdown() ) {
				try {
					watcher.close();
					break;
				} catch ( IOException e ) {
					emitError(e);
				}
				break;
			} else {
				processIOQueues();
			}

		}
	}

	@SuppressWarnings( "NestedAssignment" )
	private void processIOQueues() {

		Runnable operation;

		while ( ( operation = executorQueue.poll() ) != null ) {
			try {
				operation.run();
			} catch ( Throwable t ) {
				errors.push(t);
			}
		}

	}

	private void startIOThread() {
		ioThread.setPriority(Thread.NORM_PRIORITY - 2);
		ioThread.start();
	}

	private WatchKey take() {
		try {

			synchronized ( this ) {
				if ( interrupted ) {

					interrupted = false;

					throw new InterruptedException();

				} else {
					mayInterrupt = true;
				}
			}

			try {
				return watcher.poll();
			} finally {
				synchronized ( this ) {
					mayInterrupt = false;
				}
			}

		} catch ( InterruptedException e ) {
			return null;
		}
	}

}
