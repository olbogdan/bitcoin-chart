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

package in.flinbor.bitcoin.app;

import android.app.Application;
import android.os.Handler;

import in.flinbor.bitcoin.executorservice.ServiceHelper;
import in.flinbor.bitcoin.model.Model;
import in.flinbor.bitcoin.model.ModelImpl;

/**
 * Custom application for
 * access to global variables
 * initialization global variables
 */
public class App extends Application {

    private static App context;
    private ServiceHelper serviceHelper;
    private Model model;

    @Override
    public void onCreate() {
        super.onCreate();
        context             = this;
        this.serviceHelper  = new ServiceHelper(this, new Handler());
        this.model          = new ModelImpl();
    }

    /**
     * @return instance of {@link ServiceHelper} - helper class to work with CommandService
     */
    public ServiceHelper getServiceHelper() {
        return serviceHelper;
    }

    /**
     * @return static instance of {@link App}
     */
    public static App getApp() {
        return context;
    }

    /**
     * @return instance of {@link Model} for MVP
     */
    public Model getModelInstance() {
        return model;
    }

}
