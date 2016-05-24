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

package in.flinbor.bitcoin.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import in.flinbor.bitcoin.model.Model;
import in.flinbor.bitcoin.view.fragment.BitcoinView;

/**
 * The presenter acts upon the model and the view.
 * Retrieves data from Model, and formats it for display in the View.
 */
public interface BitcoinChartPresenter {

    /**
     * set View interface to Presenter layer
     * @param view {@link BitcoinView}
     */
    void setView(@NonNull BitcoinView view);

    /**
     * set model interface to Presenter layer
     * @param model {@link Model}
     */
    void setModel(Model model);

    /**
     * restore state of fragment
     * @param savedInstanceState Bundle with data to restore
     */
    void onCreateView(@Nullable Bundle savedInstanceState);

    /**
     * lifecycle of fragment - in fragment onResume called
     */
    void onResume();

    /**
     * lifecycle of fragment - in fragment onPause called
     */
    void onPause();

    /**
     * save state
     * @param outState to store into
     */
    void onSaveInstanceState(@NonNull Bundle outState);
}
