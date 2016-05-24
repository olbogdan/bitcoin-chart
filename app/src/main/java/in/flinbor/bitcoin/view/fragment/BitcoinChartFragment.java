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

package in.flinbor.bitcoin.view.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.flinbor.bitcoin.R;
import in.flinbor.bitcoin.presenter.BitcoinChartPresenter;
import in.flinbor.bitcoin.view.view.LineChartView;


/**
 * Implementation of view layer, responsible for showing bitcoints in Chart
 */
public class BitcoinChartFragment extends Fragment implements BitcoinView {

    private BitcoinChartPresenter presenter;
    private HorizontalScrollView horizontalScrollView;
    private LineChartView lineChartView;
    private TextView textViewDynamicsPercent;
    private TextView textViewExchangeRate;
    private TextView textViewDynamicsValueDiff;

    @Override
    public void setPresenter(@NonNull BitcoinChartPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bitcoins_chart, container, false);
        horizontalScrollView = (HorizontalScrollView) view.findViewById(R.id.fragment_bitcoins_scrollview);
        lineChartView = (LineChartView) view.findViewById(R.id.fragment_bitcoins_chart);
        lineChartView.setDataList(new ArrayList<Float>(), new ArrayList<String>());
        textViewDynamicsPercent = (TextView) view.findViewById(R.id.fragment_bitcoins_dynamics_percent);
        textViewDynamicsValueDiff = (TextView) view.findViewById(R.id.fragment_bitcoins_dynamics_value);
        textViewExchangeRate = (TextView) view.findViewById(R.id.fragment_bitcoins_exchange_rate);

        presenter.onCreateView(savedInstanceState);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        presenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showBitcoinList(@NonNull List<Float> yValues, @NonNull List<String> xAxisValues) {
        lineChartView.setDataList(yValues, xAxisValues);
        //scroll view to the end
        horizontalScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100L);
    }

    @Override
    public void showError(@NonNull String error) {
        Snackbar.make(horizontalScrollView, error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public LoaderManager getSupportLoaderManager() {
        return getLoaderManager();
    }

    @Override
    public Context getViewContext() {
        return getContext();
    }

    @Override
    public void setTextDynamicsPercent(String textDynamicsPercent, boolean isGrowing) {
        int color;
        if (isGrowing) {
            color = getResources().getColor(R.color.green);
        } else {
            color = getResources().getColor(R.color.red);
        }
        textViewDynamicsPercent.setTextColor(color);
        textViewDynamicsPercent.setText(textDynamicsPercent);
    }

    @Override
    public void setTextExchangeRate(String textExchangeRate, boolean isGrowing) {
        Drawable drawable;
        if (isGrowing) {
            drawable = getResources().getDrawable(R.drawable.arrow_top);
        } else {
            drawable = getResources().getDrawable(R.drawable.arrow_down);
        }
        textViewExchangeRate.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        textViewExchangeRate.setText(textExchangeRate);
    }

    @Override
    public void setTextDynamicsValueDiff(String textDynamicsValue, boolean isGrowing) {
        int color;
        if (isGrowing) {
            color = getResources().getColor(R.color.green);
        } else {
            color = getResources().getColor(R.color.red);
        }
        textViewDynamicsValueDiff.setTextColor(color);
        textViewDynamicsValueDiff.setText(textDynamicsValue);
    }
}
