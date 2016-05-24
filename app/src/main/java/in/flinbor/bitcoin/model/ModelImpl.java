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

package in.flinbor.bitcoin.model;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import in.flinbor.bitcoin.app.App;
import in.flinbor.bitcoin.contentprovider.BitcoinProvider;
import in.flinbor.bitcoin.presenter.vo.Bitcoin;


/**
 * Implementation of Model layer for work with database
 */
public class ModelImpl implements Model {


    @Override
    public Cursor getBitcoinsCursor() {
        return App.getApp().getContentResolver().query(BitcoinProvider.CONTENT_URI, null, null, null,
                BitcoinProvider.timestamp + " ASC");
    }

    @Override
    public List<Bitcoin> retrieveBitcoinsFromCursor(Cursor cursor) {
        List<Bitcoin> list = new ArrayList<>();
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    Bitcoin bitcoin = new Bitcoin();
                    bitcoin.setId(cursor.getInt(cursor.getColumnIndex(BitcoinProvider.bitcoinId)));
                    bitcoin.setPrice(cursor.getFloat(cursor.getColumnIndex(BitcoinProvider.value)));
                    bitcoin.setTimeStamp(cursor.getLong(cursor.getColumnIndex(BitcoinProvider.timestamp)));

                    list.add(bitcoin);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

}
