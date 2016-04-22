package co.kleder.homesecurity.shadow;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.GetThingShadowRequest;
import com.amazonaws.services.iotdata.model.GetThingShadowResult;
import com.google.gson.Gson;

/**
 * Created by rafal on 20.04.2016.
 */
public class GetShadowTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

    private final String thingName;

    // Endpoint Prefix = random characters at the beginning of the custom AWS
    // IoT endpoint
    // describe endpoint call returns: XXXXXXXXXX.iot.<region>.amazonaws.com,
    // endpoint prefix string is XXXXXXX
    private static final String CUSTOMER_SPECIFIC_ENDPOINT_PREFIX = "XXXXXXXXXX";
    // Cognito pool ID. For this app, pool needs to be unauthenticated pool with
    // AWS IoT permissions.
    private static final String COGNITO_POOL_ID = "XXXXXXXXXX";
    // Region of AWS IoT
    private static final Regions MY_REGION = Regions.EU_WEST_1;

    private final IotStatusAware iotStatusAware;

    CognitoCachingCredentialsProvider credentialsProvider;

    AWSIotDataClient iotDataClient;

    final Context mContext;

    public GetShadowTask(String name, Context appContext, IotStatusAware activity) {
        thingName = name;
        mContext = appContext;
        iotStatusAware = activity;

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                mContext,
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );


        // Initialize the Amazon Cognito credentials provider
        iotDataClient = new AWSIotDataClient(credentialsProvider);
        String iotDataEndpoint = String.format("%s.iot.%s.amazonaws.com",
                CUSTOMER_SPECIFIC_ENDPOINT_PREFIX, MY_REGION.getName());

        iotDataClient.setEndpoint(iotDataEndpoint);
    }

    @Override
    protected AsyncTaskResult<String> doInBackground(Void... voids) {
        try {
            GetThingShadowRequest getThingShadowRequest = new GetThingShadowRequest()
                    .withThingName(thingName);
            GetThingShadowResult result = iotDataClient.getThingShadow(getThingShadowRequest);
            byte[] bytes = new byte[result.getPayload().remaining()];
            result.getPayload().get(bytes);
            String resultString = new String(bytes);
            return new AsyncTaskResult<String>(resultString);
        } catch (Exception e) {
            Log.e("APP", "getShadowTask", e);
            return new AsyncTaskResult<String>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<String> result) {
        if (result.getError() == null) {
            Log.i(GetShadowTask.class.getCanonicalName(), result.getResult());

            alarmStatusUpdated(result.getResult());


        } else {
            Log.e(GetShadowTask.class.getCanonicalName(), "getShadowTask", result.getError());
        }
    }

    private void alarmStatusUpdated(String result) {
        Gson gson = new Gson();
        AlarmStatus as = gson.fromJson(result, AlarmStatus.class);

        Log.i("APP", "Parsing incoming data...");

        if (iotStatusAware != null) {
            iotStatusAware.setLastStatus(as);
        }

    }
}