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

package in.flinbor.bitcoin.executorservice.command;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import in.flinbor.bitcoin.app.App;
import in.flinbor.bitcoin.contentprovider.BitcoinProvider;
import in.flinbor.bitcoin.executorservice.dto.BitcoinsDTO;
import in.flinbor.bitcoin.executorservice.dto.ValueDTO;

/**
 * Command works in background
 * get bitcoints from server and store it to DB
 */
public class GetBitcoinsCommand extends BaseCommand {
    public static final String ERROR = "error";
    private final String TAG = GetBitcoinsCommand.class.getSimpleName();
    private final int READ_TIMEOUT_MILLIS = 10000;
    private final int CONNECTION_TIMEOUT_MILLIS = 15000;

    @Override
    public void doExecute(Intent intent, Context context, ResultReceiver callback) {
        BitcoinsDTO bitcoinsDTO = loadBitcoins();
        if (bitcoinsDTO != null && bitcoinsDTO.getValues().size() > 0) {
            insertIntoDB(bitcoinsDTO);

            notifySuccess(null);
        } else {
            Bundle b = new Bundle();
            b.putString(ERROR, "bitcoins size == null or size is 0");
            notifyFailure(b);
        }
    }

    private BitcoinsDTO loadBitcoins() {
//debug        try {
//            Thread.sleep(60000*3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        https://blockchain.info/charts/market-price?format=json
        Uri.Builder builtUri = new Uri.Builder()
                .scheme("https")
                .authority("blockchain.info")
                .path("charts")
                .appendPath("market-price")
                .appendQueryParameter("format", "json");


        String stringUrl = builtUri.build().toString();
        Log.d(TAG, "GET bitcoins ulr: " + stringUrl);

        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(stringUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setReadTimeout(READ_TIMEOUT_MILLIS);
            connection.setConnectTimeout(CONNECTION_TIMEOUT_MILLIS);
            connection.connect();

            //read headers, used for debug (headers also contain info about pagination)
            for (Map.Entry<String, List<String>> k : connection.getHeaderFields().entrySet()) {
                for (String v : k.getValue()) {
                    Log.d(TAG, k.getKey() + ":" + v);
                }
            }

            //read response as string to StringBuilder
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();

            String jsonString = sb.toString();
            Log.d(TAG, jsonString);

            //create object from json string
            Gson gson = new Gson();
            BitcoinsDTO bitcoinsDTO = gson.fromJson(jsonString, BitcoinsDTO.class);
            return bitcoinsDTO;
        } catch (Exception e) {
            e.printStackTrace();
            Bundle b = new Bundle();
            b.putString(ERROR, e.toString());
            notifyFailure(b);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private void insertIntoDB(BitcoinsDTO bitcoinsDTO) {
        /** WORN: all records stored
         * better implementation would be removing records that not presented in new data set
         * or remove all and insert new
         */
        for (int i = 0; i < bitcoinsDTO.getValues().size(); i++) {
            ValueDTO value = bitcoinsDTO.getValues().get(i);
            ContentValues values = new ContentValues();
            values.put(BitcoinProvider.timestamp, value.getTimestamp());
            values.put(BitcoinProvider.value, value.getValue());

            App.getApp().getContentResolver().insert(BitcoinProvider.CONTENT_URI, values);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    public static final Parcelable.Creator<GetBitcoinsCommand> CREATOR = new Parcelable.Creator<GetBitcoinsCommand>() {
        public GetBitcoinsCommand createFromParcel(Parcel in) {return new GetBitcoinsCommand(in);}

        public GetBitcoinsCommand[] newArray(int size) {return new GetBitcoinsCommand[size];}
    };

    private GetBitcoinsCommand(Parcel in) {}
    public GetBitcoinsCommand() {}

}
