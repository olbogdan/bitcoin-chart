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

package in.flinbor.bitcoin.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.flinbor.bitcoin.R;
import in.flinbor.bitcoin.app.App;
import in.flinbor.bitcoin.model.Model;
import in.flinbor.bitcoin.presenter.BitcoinChartPresenter;
import in.flinbor.bitcoin.presenter.BitcoinChartPresenterImpl;
import in.flinbor.bitcoin.view.fragment.BitcoinChartFragment;
import in.flinbor.bitcoin.view.fragment.BitcoinView;


/**
 * main activity of application
 */
public class MainActivity extends AppCompatActivity {
    private static final String DEFAULT_FRAGMENT = BitcoinChartFragment.class.getSimpleName();
    private static final String TAG              = MainActivity.class.getSimpleName();
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.activity_main_toolbar));

        fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(DEFAULT_FRAGMENT);

        if (fragment == null) {
            fragment = new BitcoinChartFragment();
            replaceFragment(fragment, false);
        }
        if (fragment instanceof BitcoinView) {
            bindMVP((BitcoinView) fragment, new BitcoinChartPresenterImpl(), App.getApp().getModelInstance());
        }

    }

    /**
     * creating links between model-view-presenter
     */
    private void bindMVP(@NonNull BitcoinView view, @NonNull BitcoinChartPresenter presenter, @NonNull Model model) {
        view.setPresenter(presenter);
        presenter.setView(view);
        presenter.setModel(model);
    }


    /**
     * set fragment to default activity container
     * @param fragment fragment to insert
     * @param addBackStack if true -> fragment will be added to backStack
     */
    private void replaceFragment(@NonNull Fragment fragment, boolean addBackStack) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.activity_main_fragment_container, fragment, DEFAULT_FRAGMENT);
        if (addBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

}
