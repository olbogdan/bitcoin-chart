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

package in.flinbor.bitcoin.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import in.flinbor.bitcoin.app.App;
import in.flinbor.bitcoin.executorservice.ServiceCallbackListener;
import in.flinbor.bitcoin.executorservice.command.GetBitcoinsCommand;
import in.flinbor.bitcoin.model.Model;
import in.flinbor.bitcoin.presenter.vo.Bitcoin;
import in.flinbor.bitcoin.view.fragment.BitcoinView;


/**
 * Concrete implementation of BitcoinChartPresenter
 * responsible for bitcoins business logic
 */
public class BitcoinChartPresenterImpl implements BitcoinChartPresenter, ServiceCallbackListener {

    private BitcoinView  view;
    private Model        model;
    private int          requestId = -1;
    private final String REQUEST_ID = "REQUEST_ID";

    @Override
    public void setView(@NonNull BitcoinView view) {
        this.view = view;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public void onCreateView(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            requestId = savedInstanceState.getInt(REQUEST_ID, -1);
        }
    }

    @Override
    public void onResume() {
        App.getApp().getServiceHelper().addListener(this);
        loadBitcoinsFromDatabaseInBackground();
        downloadBitcoins();
    }

    @Override
    public void onPause() {
        App.getApp().getServiceHelper().removeListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(REQUEST_ID, requestId);
        App.getApp().getServiceHelper().cancelCommand(requestId);
        view.getSupportLoaderManager().destroyLoader(0);
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle resultData) {
        if (this.requestId == requestId && App.getApp().getServiceHelper().check(requestIntent, GetBitcoinsCommand.class)) {
            if (resultCode == GetBitcoinsCommand.RESPONSE_SUCCESS) {
                loadBitcoinsFromDatabaseInBackground();
            } else if (resultCode == GetBitcoinsCommand.RESPONSE_FAILURE) {
                String error = "onServiceCallback response error";
                if (resultData != null) {
                    error = resultData.getString(GetBitcoinsCommand.ERROR);
                }
                error = error == null ? "onServiceCallback response error": error;
                view.showError(error);
            }
            this.requestId = -1;
        }
    }

    private void downloadBitcoins() {
        if (!App.getApp().getServiceHelper().isPending(requestId)) {
            requestId = App.getApp().getServiceHelper().getBitcoins();
        }
    }

    private void prepareDateForChart(List<Bitcoin> list) {
        Date time = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd\nyyyy");
        SimpleDateFormat dateFormatPlutTime = new SimpleDateFormat("hh:mm\nMMM dd\nyyyy");
        List<String> xValues = new ArrayList<>();
        List<Float> yValues = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Bitcoin bitcoin = list.get(i);
            time.setTime(bitcoin.getUnixTimeStamp() * 1000);
            String formatedTime;
            if (i > list.size()-4) {
                //for last 3 elements show hours and minutes
                formatedTime = dateFormatPlutTime.format(time);
            } else {
                formatedTime = dateFormat.format(time);
            }

            xValues.add(formatedTime);

            yValues.add(bitcoin.getPrice());
        }
        view.showBitcoinList(yValues, xValues);
        setChageOfDynamics(yValues);
    }

    private void setChageOfDynamics(List<Float> yValues) {
        for (int i = yValues.size()-1; i > 1; i--) {
            float iLast = yValues.get(i);
            float iPrev = yValues.get(i-1);
            if (iLast != iPrev) {
                float diff = iLast - iPrev;
                diff = round(diff, 2);
                if (diff > 0) {
                    float percent = (iLast * 100 / iPrev) - 100;
                    percent = round(percent, 2);
                    view.setTextDynamicsPercent("+"+percent+"%", true);
                    view.setTextDynamicsValueDiff(String.valueOf(diff), true);
                    view.setTextExchangeRate("$"+iLast, true);
                } else {
                    float percent = (iPrev * 100 / iLast) - 100;
                    percent = round(percent, 2);
                    view.setTextDynamicsPercent("-"+percent+"%", false);
                    view.setTextDynamicsValueDiff(String.valueOf(diff), false);
                    view.setTextExchangeRate("$"+iLast, false);
                }
                break;
            }
        }
    }

    /**
     * fast round float to 2 decimals
     * @param number Float to manipulate
     * @param scale Count to decimals to leave
     * @return rounded float
     */
    public float round(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }


    /*******************************************Loader**************************************/

    private void loadBitcoinsFromDatabaseInBackground() {
        if (view.getSupportLoaderManager().getLoader(0) != null) {
            view.getSupportLoaderManager().restartLoader(0, null, loaderCallback);
        } else {
            view.getSupportLoaderManager().initLoader(0, null, loaderCallback);
        }
    }

    private final LoaderManager.LoaderCallbacks<Cursor> loaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new BitcoinsLoader(view.getViewContext(), model);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            List<Bitcoin> list = model.retrieveBitcoinsFromCursor(cursor);
            prepareDateForChart(list);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };



    private static class BitcoinsLoader extends CursorLoader {

        private final Model model;

        public BitcoinsLoader(Context context, Model model) {
            super(context);
            this.model = model;
        }

        @Override
        public Cursor loadInBackground() {
            return model.getBitcoinsCursor();
        }
    }

    /***************************************************************************************/
}
