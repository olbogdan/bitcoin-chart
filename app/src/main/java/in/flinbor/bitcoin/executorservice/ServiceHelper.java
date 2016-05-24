/*
 * Copyright 2016 Flinbor Bogdanov Oleksandr
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

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import in.flinbor.bitcoin.executorservice.command.BaseCommand;
import in.flinbor.bitcoin.executorservice.command.GetBitcoinsCommand;

/**
 * Helper class for work with {@link CommandExecutorService}
 * run new commands
 * chalk status
 * cancel commands
 */
public class ServiceHelper {

    private final ArrayList<ServiceCallbackListener> currentListeners  = new ArrayList<>();
    private final AtomicInteger                      idCounter         = new AtomicInteger();
    private final SparseArray<Intent>                pendingActivities = new SparseArray<>();
    private final Application                        application;
    private final Handler                            handler;

    public ServiceHelper(Application app, Handler handler) {
        this.application = app;
        this.handler = handler;
    }

    public void addListener(ServiceCallbackListener currentListener) {
        currentListeners.add(currentListener);
    }

    public void removeListener(ServiceCallbackListener currentListener) {
        currentListeners.remove(currentListener);
    }

    public int getBitcoins() {
        final int requestId = createId();

        Intent i = createIntent(application, new GetBitcoinsCommand(), requestId);
        return runRequest(requestId, i);
    }

    public void cancelCommand(int requestId) {
        Intent i = new Intent(application, CommandExecutorService.class);
        i.setAction(CommandExecutorService.ACTION_CANCEL_COMMAND);
        i.putExtra(CommandExecutorService.EXTRA_REQUEST_ID, requestId);

        application.startService(i);
        pendingActivities.remove(requestId);
    }

    public boolean isPending(int requestId) {
        return pendingActivities.get(requestId) != null;
    }

    public boolean check(Intent intent, Class<? extends BaseCommand> clazz) {
        Parcelable commandExtra = intent.getParcelableExtra(CommandExecutorService.EXTRA_COMMAND);
        return commandExtra != null && commandExtra.getClass().equals(clazz);
    }

    private int createId() {
        return idCounter.getAndIncrement();
    }

    private int runRequest(final int requestId, Intent i) {
        pendingActivities.append(requestId, i);
        application.startService(i);
        return requestId;
    }

    private Intent createIntent(final Context context, BaseCommand command, final int requestId) {
        Intent i = new Intent(context, CommandExecutorService.class);
        i.setAction(CommandExecutorService.ACTION_EXECUTE_COMMAND);

        i.putExtra(CommandExecutorService.EXTRA_COMMAND, command);
        i.putExtra(CommandExecutorService.EXTRA_REQUEST_ID, requestId);
        i.putExtra(CommandExecutorService.EXTRA_STATUS_RECEIVER, new ResultReceiver(handler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Intent originalIntent = pendingActivities.get(requestId);
                if (isPending(requestId)) {
                    if (resultCode != BaseCommand.RESPONSE_PROGRESS) {
                        pendingActivities.remove(requestId);
                    }

                    for (ServiceCallbackListener currentListener : currentListeners) {
                        if (currentListener != null) {
                            currentListener.onServiceCallback(requestId, originalIntent, resultCode, resultData);
                        }
                    }
                }
            }
        });

        return i;
    }

}
