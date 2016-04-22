package co.kleder.homesecurity.shadow;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;

import java.nio.ByteBuffer;

/**
 * Created by rafal on 20.04.2016.
 */
public class UpdateShadowTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

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

    CognitoCachingCredentialsProvider credentialsProvider;

    AWSIotDataClient iotDataClient;

    private String thingName;

    private String updateState;

    Context context;

    public UpdateShadowTask(String name, Context appContext){

        thingName = name;
        context = appContext;

        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );


        // Initialize the Amazon Cognito credentials provider
        iotDataClient = new AWSIotDataClient(credentialsProvider);
        String iotDataEndpoint = String.format("%s.iot.%s.amazonaws.com",
                CUSTOMER_SPECIFIC_ENDPOINT_PREFIX, MY_REGION.getName());

        iotDataClient.setEndpoint(iotDataEndpoint);
    }


    public void setState(String state) {
        updateState = state;
    }

    @Override
    protected AsyncTaskResult<String> doInBackground(Void... voids) {
        try {
            UpdateThingShadowRequest request = new UpdateThingShadowRequest();
            request.setThingName(thingName);

            ByteBuffer payloadBuffer = ByteBuffer.wrap(updateState.getBytes());
            request.setPayload(payloadBuffer);

            UpdateThingShadowResult result = iotDataClient.updateThingShadow(request);

            byte[] bytes = new byte[result.getPayload().remaining()];
            result.getPayload().get(bytes);
            String resultString = new String(bytes);
            return new AsyncTaskResult<String>(resultString);
        } catch (Exception e) {
            Log.e(UpdateShadowTask.class.getCanonicalName(), "updateShadowTask", e);
            return new AsyncTaskResult<String>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<String> result) {
        if (result.getError() == null) {
            Log.i(UpdateShadowTask.class.getCanonicalName(), result.getResult());
        } else {
            Log.e(UpdateShadowTask.class.getCanonicalName(), "Error in Update Shadow",
                    result.getError());
        }
    }
}

