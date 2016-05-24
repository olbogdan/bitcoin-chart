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

package in.flinbor.bitcoin.view.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;

import java.util.List;

import in.flinbor.bitcoin.presenter.BitcoinChartPresenter;

/**
 * Passive interface that displays data and routes user commands to the presenter to act upon that data
 */
public interface BitcoinView {

    /**
     * set presenter interface to View layer
     * @param presenter {@link BitcoinChartPresenter}
     */
    void setPresenter(@NonNull BitcoinChartPresenter presenter);


    /**
     * show bitcoins on UI
     * @param yValues The values to show on Y-Axis
     * @param xAxisValues The values to show on X-Axis
     */
    void showBitcoinList(@NonNull List<Float> yValues, @NonNull List<String> xAxisValues);

    /**
     * @param textDynamicsPercent text to show
     * @param isGrowing is dynamic growing
     */
    void setTextDynamicsPercent(String textDynamicsPercent, boolean isGrowing);

    /**
     * @param textExchangeRate text to show
     * @param isGrowing is dynamic growing
     */
    void setTextExchangeRate(String textExchangeRate, boolean isGrowing);

    /**
     * @param textDynamicsValue text to show
     * @param isGrowing is dynamic growing
     */
    void setTextDynamicsValueDiff(String textDynamicsValue, boolean isGrowing);

    /**
     * shwo error on UI
     * @param error text of error
     */
    void showError(@NonNull String error);

    /**
     * get support loader manger, for using with cursor loader
     * @return LoaderManager
     */
    LoaderManager getSupportLoaderManager();

    /**
     * get context, for using with cursor loader
     * @return LoaderManager
     */
    Context getViewContext();

}
