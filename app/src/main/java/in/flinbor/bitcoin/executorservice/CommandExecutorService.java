/*
 * Copyright ${year} Flinbor Bogdanov Oleksandr
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package in.flinbor.bitcoin.executorservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.flinbor.bitcoin.app.App;
import in.flinbor.bitcoin.executorservice.command.BaseCommand;


/**
 * Executor for command,
 * Commands will be run in background
 * based on FixedThreadPool
 * available notification about progress and final result of Commands
 * available canceling of Commands
 */
public class CommandExecutorService extends Service {

    private static final int NUM_THREADS = 4;

    public static final String ACTION_EXECUTE_COMMAND	= App.getApp().getPackageName().concat(".ACTION_EXECUTE_COMMAND");

    public static final String ACTION_CANCEL_COMMAND 	= App.getApp().getPackageName().concat(".ACTION_CANCEL_COMMAND");

    public static final String EXTRA_REQUEST_ID 		= App.getApp().getPackageName().concat(".EXTRA_REQUEST_ID");

    public static final String EXTRA_STATUS_RECEIVER 	= App.getApp().getPackageName().concat(".STATUS_RECEIVER");

    public static final String EXTRA_COMMAND			= App.getApp().getPackageName().concat(".EXTRA_COMMAND");

    private final ExecutorService executor 				= Executors.newFixedThreadPool(NUM_THREADS);

    private final SparseArray<RunningCommand> runningCommands = new SparseArray<>();

    protected void onHandleIntent(Intent intent) {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		String action = intent.getAction();
		if (!TextUtils.isEmpty(action)) {
			getCommand(intent).execute(intent, getApplicationContext(), getReceiver(intent));
		}
	}

    @Override
    public IBinder onBind(Intent intent) {
	return null;
    }

    @Override
    public void onDestroy() {
		super.onDestroy();
		executor.shutdownNow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		if (ACTION_EXECUTE_COMMAND.equals(intent.getAction())) {
			RunningCommand runningCommand = new RunningCommand(intent);

			synchronized (runningCommands) {
			runningCommands.append(getCommandId(intent), runningCommand);
			}

			executor.submit(runningCommand);
		}
		if (ACTION_CANCEL_COMMAND.equals(intent.getAction())) {
			RunningCommand runningCommand = runningCommands.get(getCommandId(intent));
			if (runningCommand != null) {
			runningCommand.cancel();
			}
		}
		return START_NOT_STICKY;
    }

	private class RunningCommand implements Runnable {

		private final Intent 	  intent;

		private final BaseCommand command;

		public RunningCommand(Intent intent) {
			this.intent = intent;
			this.command = getCommand(intent);
		}

		public void cancel() {
			command.cancel();
		}

		@Override
		public void run() {
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			command.execute(intent, getApplicationContext(), getReceiver(intent));
			shutdown();
		}

		private void shutdown() {
			synchronized (runningCommands) {
				runningCommands.remove(getCommandId(intent));
				if (runningCommands.size() == 0) {
					stopSelf();
				}
			}
		}

	}

	private ResultReceiver getReceiver(Intent intent) {
		return intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
	}

	private BaseCommand getCommand(Intent intent) {
		return intent.getParcelableExtra(EXTRA_COMMAND);
	}

	private int getCommandId(Intent intent) {
		return intent.getIntExtra(EXTRA_REQUEST_ID, -1);
	}

}
